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
#include "crypt.h"
#include "utils.h"

/*���Υե�������ΤߤǻȤ��ؿ������*/
void connection_return_req_data_header(CONNECTION_DATA* con,Uint32 result_code);
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size);
void connection_connect(CONNECTION_DATA* con);
void connection_send_error(TCPsocket* sock);
int connection_do_request(CONNECTION_DATA* con,int content_length);
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
		/*�����ɲ�*/
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"%s\n",str);
		unlock_log_file();
		//�Ǹ�ޤǼ����������
		while(*(str = NetUtl_readLine(sock)) != END_CHAR){
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
int connection_do_request(CONNECTION_DATA* con,int content_length){
	FILE* log_file;
	TCPsocket* c_sock = &con->socket;
	char* recv;
	int idx = 0;
	int size;
	int total_size = 0;
	USER_INFO* info;
	REQUEST* req = &con->request;
	//�ꥯ�����ȴط�
	Uint32 user_id;
	Uint32 action_code;
	//��Ź沽�ǡ���
	user_id = NetUtl_recvInt(c_sock);
	idx+=4;
	action_code = NetUtl_recvInt(c_sock);
	idx+=4;
	content_length -= idx;
	idx = 0;

	//���λ����ǥ桼������
	info = get_user(user_id);
	if(info == null){//�桼�������Ĥ���ʤ�
		char ch;
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"User NOT FOUND ID:%08x\n",user_id);
		unlock_log_file();
		//�Ǹ�ޤǼ������ʤ��ȡ����顼�ˤʤ꤬����
		while(SDLNet_TCP_Recv(*c_sock, &ch, 1) == 1){
		}
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return false;
	}

	//���ΥХå����å�
	backup_crypt_request(req,info);

	//�Ź沽�ǡ�������
	recv = malloc(content_length);
	while((size = recvCrypt(&info->crypt,c_sock,
					&recv[total_size],content_length-total_size)) > 0){
		total_size += size;
		if(total_size >= content_length){
			break;
		}
	}

	//��³
	if(!connect_user(info)){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Already connected.\n",info->name);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//�ꥹ�ȥ�
		restore_crypt_request(req);
		return false;
	}

	//���顼
	if(total_size <= 0){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Zero Sized Request.\n",info->name);
		unlock_log_file();
		free_request(&con->request);
		disconnect_user(info);
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//�ꥹ�ȥ�
		restore_crypt_request(req);
		return false;
	}
	//�ꥯ�����ȴ���
	if(!init_request(&con->request,info,action_code,recv,total_size)){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Invalid Request.\n",info->name);
		unlock_log_file();
		free_request(&con->request);
		disconnect_user(info);
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//�ꥹ�ȥ�
		restore_crypt_request(req);
		return false;
	}
	//�����å�
	switch_request(con);
	disconnect_user(info);
	free_request(&con->request);
	return true;
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
	int code;
	USER_INFO* info = req->info;
	//������
	code = login_user(info,req->pass,req->session_id,con->ip);
	if(code == USER_LOGOFF_SUCCESS){//�����ڤ�ǥ�����
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Login Error:Time out and Loggoff\n",info->name);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
		initCrypt(&info->crypt);//���λ����ǰŹ�����ν������
		return;
	}else if(code != USER_LOGIN_SUCCESS){//����ʳ��ǥ��顼
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Login Error:%d\n",info->name,code);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//�ꥹ�ȥ�
		restore_crypt_request(req);
		return;
	}
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
	int code;
	USER_INFO* info = req->info;
	//������
	if((code = logoff_user(info,req->pass,req->session_id,con->ip)) != USER_LOGOFF_SUCCESS){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Logoff Error:%d\n",info->name,code);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//�ꥹ�ȥ�
		restore_crypt_request(req);
		return;
	}
	//����
	log_file = lock_log_file();
	time_output();
	ip_output(con->ip);
	fprintf(log_file,"(%s)Logoff Success\n",info->name);
	unlock_log_file();
	connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
	initCrypt(&info->crypt);//���λ����ǰŹ�����ν������
}

inline char* connection_get_req_url(const char* str,int max);
/*��������HTTP�ꥯ�����Ȥν���*/
inline void case_action_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int code;
	USER_INFO* info = req->info;

	//�����å�
	code = check_user(info,req->pass,req->session_id,con->ip);
	if(code == USER_LOGOFF_SUCCESS){//�����ڤ�ǥ�����
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Request Error:Time out and Loggoff\n",info->name);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
		initCrypt(&info->crypt);//���λ����ǰŹ�����ν������
		return;
	}else if(code != USER_CHECK_SUCCESS){//����ʳ��Υ��顼
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Request Error:%d\n",info->name,code);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//�ꥹ�ȥ�
		restore_crypt_request(req);
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
		//�ꥹ�ȥ�
		restore_crypt_request(req);
	}
	request_close_connection(req);
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
	char buff[4096];
	/*�ե����륵�����μ���*/
	start = ftell(err_file);
	fseek(err_file, 0, SEEK_END);
	end = ftell(err_file);
	fseek(err_file, 0, SEEK_SET);
	size = end - start;
	/*����*/
	while(size > 0){
		fread(buff,4096,1,err_file);
		size -= SDLNet_TCP_Send(*sock, buff, min(4096,size));
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
	"HTTP/1.1 200 OK\r\n"
	"Date: Thu, 26 Apr 2007 09:04:24 GMT\r\n"
	"Server: Haduki\r\n"
	"Connection: close\r\n"
	"Content-Type: image/x-png\r\n"
	"\r\n"
	);
	//�ꥶ��ȥ�����
	sendCrypt(&con->request.info->crypt,sock,(char*)&res_code_swapped,4);
}
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size){
	//�ǡ���
	if(data != NULL && size > 0)sendCrypt(&con->request.info->crypt,&con->socket,data,size);
}
