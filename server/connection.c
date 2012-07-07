#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include "main.h"
#include "crypt.h"
#include "user_info.h"
#include "request.h"
#include "connection.h"
#include "manager.h"
#include "utils.h"

/*���Υե�������ΤߤǻȤ��ؿ������*/
void connection_return_req_data_header(CONNECTION_DATA* con,Uint32 result_code);
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size);
void connection_connect(CONNECTION_DATA* con);
void connection_send_error(TCPsocket* sock);
void connection_do_request(CONNECTION_DATA* con,int content_length);
void switch_request(CONNECTION_DATA* con);
inline void case_action_request(CONNECTION_DATA* con);
inline void case_action_connect(CONNECTION_DATA* con);
inline void case_action_dis_connect(CONNECTION_DATA* con);
/*�����*/
void init_connection(CONNECTION_DATA* con,int pipe[2]){
	con->connected = false;
	con->connected_mutex = SDL_CreateMutex();
	//�ѥ���
	memcpy(&con->com_pipe,pipe,sizeof(pipe[0]) * 2);
}
/*�ӥ������ִ���*/
inline int lock_connection(CONNECTION_DATA* con){
	int ret;
	SDL_mutexP(con->connected_mutex);
	if(con->connected == false){
		con->connected = true;
		ret = true;
	}else{
		ret = false;
	}
	SDL_mutexV(con->connected_mutex);
	return ret;
}
inline int unlock_connection(CONNECTION_DATA* con){
	int ret;
	SDL_mutexP(con->connected_mutex);
	if(con->connected == true){
		con->connected = false;
		ret = true;
	}else{
		ret = false;
	}
	SDL_mutexV(con->connected_mutex);
	return ret;
}
inline int is_locked_connection(CONNECTION_DATA* con){
	int connected;
	SDL_mutexP(con->connected_mutex);
	connected = con->connected;
	SDL_mutexV(con->connected_mutex);
	return connected;
}
/*�ᥤ���ή��*/
int connection_main(void* data){
	CONNECTION_DATA* con = (CONNECTION_DATA*)data;
	int action_code;
	int running = true;
	//�����ʥ�μ���
	while(running && read(con->com_pipe[PIPE_READ],&action_code,sizeof(action_code))){
		switch(action_code){
			case MANAGER_ACTION_CONNECT:
				read(	con->com_pipe[PIPE_READ],
						&con->socket,
						sizeof(con->socket)
					);
				connection_connect(con);
				break;
			case MANAGER_ACTION_KILL:
				running = false;
				break;
		}
	}
	connection_free(con);
	return EXIT_SUCCESS;
}

#define POST_HEADER "POST"

#define END_CHAR '\0'

#define CONTENT_LENGTH_HEADER "Content-Length: "
#define CONTENT_LENGTH_HEADER_F "Content-Length: %d"

#define UA_LENGTH_HEADER "User-Agent: "
#define UA_LENGTH_HEADER_F "User-Agent: %s"
/*��³*/
void connection_connect(CONNECTION_DATA* con){
	/*�ѿ����*/
	TCPsocket *sock = &con->socket;
	char* str;
	int content_length = -1;
	int is_err = false;
	FILE* log_file;
	/*��³��IP�����*/
	con->ip = SDLNet_TCP_GetPeerAddress(*sock);
	/*�ӥ������֤�����*/
	if(!lock_connection(con)){
		SDLNet_TCP_Close(*sock);
		return;
	}
	/*�إå���Ƚ�Ǥ���*/
	str = NetUtl_readLine(sock);
	if(str == null){
		/*�̿������*/
		SDLNet_TCP_Close(*sock);
		/*��³��λ�ե饰*/
		unlock_connection(con);
		return;
	}
	if(strncmp(str,POST_HEADER,strlen(POST_HEADER)) != 0){
		is_err = true;
	}
	/*�Ȥꤢ�����Ǹ�ޤǼ������롣*/
	if(is_err){//���顼
		char ch;
		/*�����ɲ�*/
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"%s\n",str);
		unlock_log_file();
		//�Ǹ�ޤǼ����������
		while(SDLNet_TCP_Recv(*sock, &ch, 1) == 1){
		}
	}else{//�إå����������
		while(*(str = NetUtl_readLine(sock)) != END_CHAR){
			if(content_length < 0){
				if(strncmp(	str,
							CONTENT_LENGTH_HEADER,
							strlen(CONTENT_LENGTH_HEADER)
						) == 0){
					sscanf(str,CONTENT_LENGTH_HEADER_F,&content_length);
				}
			}//else if(){}
		}
	}
	if(!is_err && content_length >= 0){/*�Ȥꤢ�����̿�������ͤ���*/
		connection_do_request(con,content_length);
	}else{/*�ޤä����ط��ʤ�*/
		connection_send_error(sock);
	}
	/*�̿������*/
	SDLNet_TCP_Close(*sock);
	/*��³��λ�ե饰*/
	unlock_connection(con);
}
/*�ꥯ�����Ȥ����������ͤ���*/
void connection_do_request(CONNECTION_DATA* con,int content_length){
		TCPsocket* c_sock = &con->socket;
		char* recv;
		int idx = 0;
		int size;
		int total_size = 0;
		//�ꥯ�����ȴط�
		char pass[USER_INFO_KEY_SIZE];
		char* host = NULL;
		char* data = NULL;
		Uint32 user_id;
		Uint32 session_id;
		Uint32 action_code;
		Uint32 host_size;
		Uint16 host_port = 0;
		Uint32 data_size;
		//�ǡ�������
		recv = malloc(content_length);
		while((size = recvCrypt(&con->info->crypt,c_sock,
						&recv[total_size],content_length-total_size)) > 0){
			total_size += size;
			if(total_size >= content_length){
				break;
			}
		}

		//���顼
		if(total_size <= 0){
			free(recv);
			return;
		}
		//�ꥯ�����ȴ���
		user_id = Utl_readInt(&recv[0]);
		idx+=4;

		memcpy(pass,&recv[idx],USER_INFO_KEY_SIZE);
		idx+=USER_INFO_KEY_SIZE;

		session_id = Utl_readInt(&recv[idx]);
		idx+=4;

		action_code = Utl_readInt(&recv[idx]);
		idx+=4;

		host_size = Utl_readInt(&recv[idx]);
		idx+=4;

		if(host_size > 0 && host_size <= content_length - idx - 4 - 2){
			host = malloc(host_size+1);
			memcpy(host,&recv[idx],host_size);
			host[host_size] = '\0';
			idx+=host_size;

			host_port = Utl_readShort(&recv[idx]);
			idx+=2;

		}

		data_size = Utl_readInt(&recv[idx]);
		idx+=4;

		if(data_size > 0 && data_size <= content_length - idx){
			data = malloc(data_size);
			memcpy(data,&recv[idx],data_size);
			idx+=data_size;
		}

		init_request(&con->request,user_id,pass,session_id,
						action_code,host_port,host,data_size,data);
		free(recv);
		if(idx == total_size){//�ǡ��������������ʤ���
			//�ꥯ�����Ȥη�̤�ʬ���롣
			switch_request(con);
		}
		free_request(&con->request);
}
/*�ꥯ�����Ȥ�ʬ��*/
void switch_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	switch(request_get_action_code(req)){
		case CONNECTION_ACTION_CONNECT://��³
			case_action_connect(con);
			break;
		case CONNECTION_ACTION_REQUEST://HTTP�ꥯ������
			case_action_request(con);
			break;
		case CONNECTION_ACTION_DISCONNECT://����
			case_action_dis_connect(con);
			break;
		default://����������
			connection_send_error(&con->socket);
			break;
	}
}
/*����������³*/
inline void case_action_connect(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int user_id = req->user_id;
	int code;
	USER_INFO* info = get_user(user_id);
	if(info == null){//�桼�������Ĥ���ʤ�
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"Login: USER NOT FOUND ID:%08x\n",user_id);
		unlock_log_file();
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}
	//��³
	if(!connect_user(info)){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Login: Already connected.\n",info->name);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}
	//������
	if((code = login_user(info,req->pass,req->session_id,con->ip)) != USER_LOGIN_SUCCESS){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Login Error:%d\n",info->name,code);
		unlock_log_file();
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		disconnect_user(info);
		return;
	}
	disconnect_user(info);
	//����
	log_file = lock_log_file();
	time_output();
	ip_output(con->ip);
	fprintf(log_file,"(%s)Login Success\n",info->name);
	unlock_log_file();
	connection_return_req_data_header(con,CONNECTION_ACTION_ACCEPT);
}
/*������������*/
inline void case_action_dis_connect(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int user_id = req->user_id;
	int code;
	USER_INFO* info = get_user(req->user_id);
	if(info == null){//�桼�������Ĥ���ʤ�
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"Logoff: USER NOT FOUND ID:%08x\n",user_id);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}
	//��³
	if(!connect_user(info)){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Logoff: Already connected.\n",info->name);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}
	//������
	if((code = logoff_user(info,req->pass,req->session_id,con->ip)) != USER_LOGOFF_SUCCESS){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Logoff Error:%d\n",info->name,code);
		unlock_log_file();
		//KICKED
		disconnect_user(info);
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}
	disconnect_user(info);
	//����
	log_file = lock_log_file();
	time_output();
	ip_output(con->ip);
	fprintf(log_file,"(%s)Logoff Success\n",info->name);
	unlock_log_file();
	connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
}

inline char* connection_get_req_url(const char* str,int max);
/*��������HTTP�ꥯ�����Ȥν���*/
inline void case_action_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int code;
	USER_INFO* info = get_user(req->user_id);

	if(info == null){//�桼�������Ĥ���ʤ�
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"Request: USER NOT FOUND ID:%08x\n",req->user_id);
		unlock_log_file();
		return;
	}

	//��³
	if(!connect_user(info)){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Request: Already connected.\n",info->name);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}

	//�����å�
	if((code = check_user(info,req->pass,req->session_id,con->ip)) != USER_CHECK_SUCCESS){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Request Error:%d\n",info->name,code);
		unlock_log_file();
		//KICKED
		disconnect_user(info);
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}

	//����
	if(connect_request(req) && send_request(req)){
		TCPsocket* s_sock = request_get_sock(req);
		char data[4096];
		int total_size = 0;
		int size;
		//�إå����֤�
		connection_return_req_data_header(con,CONNECTION_ACTION_RESULT);
		//�ǡ���
		do{
			size = SDLNet_TCP_Recv(*s_sock,data,4096);
			total_size+=size;
			connection_return_req_data(con,data,size);
		}while(size > 0);
		{//�ǡ���������λ
		char* request_str = connection_get_req_url(req->data,req->data_size);
		log_file = lock_log_file();
		time_output();
		fprintf(log_file,"(%s)<%s:%d>%s %dbytes\n",info->name,req->host,req->host_port,request_str,total_size);
		unlock_log_file();
		free(request_str);
		}
	}else{//���顼
		{
		char* request_str = connection_get_req_url(req->data,req->data_size);
		log_file = lock_log_file();
		time_output();
		fprintf(log_file,"(%s)<%s:%d>%s ConnectionError\n",info->name,req->host,req->host_port,request_str);
		unlock_log_file();
		free(request_str);
		}
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
	}
	request_close_connection(req);
	disconnect_user(info);
}

inline char* connection_get_req_url(const char* str,int max){
	char* req;
	int i;
	for(i=0;i<max;i++){
		if(str[i] == '\n' || str[i] == '\r')break;
	}
	req = malloc(i+1);
	memcpy(req,str,i);
	req[i] = '\0';
	return req;
}

/*���顼���������롣*/
void connection_send_error(TCPsocket* sock){
	FILE* err_file = fopen("err_reply.txt", "rb");
	int start,end,size;
	char buff[1024];
	/*�ե����륵�����μ���*/
	start = ftell(err_file);
	fseek(err_file, 0, SEEK_END);
	end = ftell(err_file);
	fseek(err_file, 0, SEEK_SET);
	size = end - start;
	/*����*/
	while(size > 0){
		fread(buff,1024,1,err_file);
		SDLNet_TCP_Send(*sock, buff, min(1024,size));
		size -= 1024;
	}
	fclose(err_file);
}

void connection_free(CONNECTION_DATA* con){
	//SDLNet_TCP_Close(connection_data->socket);
}
/*��̤��֤�����ν�����*/
void connection_return_req_data_header(CONNECTION_DATA* con,Uint32 result_code){
	//static char content_length_header[1024];
	int res_code_swapped = Utl_readInt((char*)&result_code);
	TCPsocket* sock = &con->socket;
	//�إå�
	NetUtl_sendLine(sock,
	"HTTP/1.1 200 OK\n"
	"Date: Thu, 26 Apr 2007 09:04:24 GMT\n"
	"Server: Haduki\n"
	"Content-Type: image/x-png\n"
	);
	//sprintf(content_length_header,"Content-Length: %d\n",size+4);
	//NetUtl_sendLine(sock,content_length_header);
	//�ǡ�������
	NetUtl_sendLine(sock,"\n");
	//�ꥶ��ȥ�����
	sendCrypt(&con->info->crypt,sock,(char*)&res_code_swapped,4);
}
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size){
	//�ǡ���
	if(data != NULL && size > 0)sendCrypt(&con->info->crypt,&con->socket,data,size);
}
