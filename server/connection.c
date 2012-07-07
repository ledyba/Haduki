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

/*このファイル内のみで使う関数を宣言*/
void connection_return_req_data_header(CONNECTION_DATA* con,Uint32 result_code);
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size);
void connection_connect(CONNECTION_DATA* con);
void connection_send_error(TCPsocket* sock);
void connection_do_request(CONNECTION_DATA* con,int content_length);
void switch_request(CONNECTION_DATA* con);
inline void case_action_request(CONNECTION_DATA* con);
inline void case_action_connect(CONNECTION_DATA* con);
inline void case_action_dis_connect(CONNECTION_DATA* con);
/*初期化*/
void init_connection(CONNECTION_DATA* con,int pipe[2]){
	con->connected = false;
	con->connected_mutex = SDL_CreateMutex();
	//パイプ
	memcpy(&con->com_pipe,pipe,sizeof(pipe[0]) * 2);
}
/*ビジー状態管理*/
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
/*メインの流れ*/
int connection_main(void* data){
	CONNECTION_DATA* con = (CONNECTION_DATA*)data;
	int action_code;
	int running = true;
	//シグナルの受信
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
/*接続*/
void connection_connect(CONNECTION_DATA* con){
	/*変数宣言*/
	TCPsocket *sock = &con->socket;
	char* str;
	int content_length = -1;
	int is_err = false;
	FILE* log_file;
	/*接続先IPを取得*/
	con->ip = SDLNet_TCP_GetPeerAddress(*sock);
	/*ビジー状態に設定*/
	if(!lock_connection(con)){
		SDLNet_TCP_Close(*sock);
		return;
	}
	/*ヘッダを判断する*/
	str = NetUtl_readLine(sock);
	if(str == null){
		/*通信終わり*/
		SDLNet_TCP_Close(*sock);
		/*接続終了フラグ*/
		unlock_connection(con);
		return;
	}
	if(strncmp(str,POST_HEADER,strlen(POST_HEADER)) != 0){
		is_err = true;
	}
	/*とりあえず最後まで受信する。*/
	if(is_err){//エラー
		char ch;
		/*ログに追加*/
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"%s\n",str);
		unlock_log_file();
		//最後まで受信するだけ
		while(SDLNet_TCP_Recv(*sock, &ch, 1) == 1){
		}
	}else{//ヘッダを取得する
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
	if(!is_err && content_length >= 0){/*とりあえず通信するに値する*/
		connection_do_request(con,content_length);
	}else{/*まったく関係ない*/
		connection_send_error(sock);
	}
	/*通信終わり*/
	SDLNet_TCP_Close(*sock);
	/*接続終了フラグ*/
	unlock_connection(con);
}
/*リクエストを処理するに値する*/
void connection_do_request(CONNECTION_DATA* con,int content_length){
		TCPsocket* c_sock = &con->socket;
		char* recv;
		int idx = 0;
		int size;
		int total_size = 0;
		//リクエスト関係
		char pass[USER_INFO_KEY_SIZE];
		char* host = NULL;
		char* data = NULL;
		Uint32 user_id;
		Uint32 session_id;
		Uint32 action_code;
		Uint32 host_size;
		Uint16 host_port = 0;
		Uint32 data_size;
		//データ受信
		recv = malloc(content_length);
		while((size = recvCrypt(&con->info->crypt,c_sock,
						&recv[total_size],content_length-total_size)) > 0){
			total_size += size;
			if(total_size >= content_length){
				break;
			}
		}

		//エラー
		if(total_size <= 0){
			free(recv);
			return;
		}
		//リクエスト完成
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
		if(idx == total_size){//データサイズが合わない。
			//リクエストの結果で分ける。
			switch_request(con);
		}
		free_request(&con->request);
}
/*リクエストの分岐*/
void switch_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	switch(request_get_action_code(req)){
		case CONNECTION_ACTION_CONNECT://接続
			case_action_connect(con);
			break;
		case CONNECTION_ACTION_REQUEST://HTTPリクエスト
			case_action_request(con);
			break;
		case CONNECTION_ACTION_DISCONNECT://切断
			case_action_dis_connect(con);
			break;
		default://不正コード
			connection_send_error(&con->socket);
			break;
	}
}
/*ケース：接続*/
inline void case_action_connect(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int user_id = req->user_id;
	int code;
	USER_INFO* info = get_user(user_id);
	if(info == null){//ユーザが見つからない
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"Login: USER NOT FOUND ID:%08x\n",user_id);
		unlock_log_file();
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}
	//接続
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
	//ログイン
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
	//成功
	log_file = lock_log_file();
	time_output();
	ip_output(con->ip);
	fprintf(log_file,"(%s)Login Success\n",info->name);
	unlock_log_file();
	connection_return_req_data_header(con,CONNECTION_ACTION_ACCEPT);
}
/*ケース：切断*/
inline void case_action_dis_connect(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int user_id = req->user_id;
	int code;
	USER_INFO* info = get_user(req->user_id);
	if(info == null){//ユーザが見つからない
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"Logoff: USER NOT FOUND ID:%08x\n",user_id);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return;
	}
	//接続
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
	//ログオフ
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
	//成功
	log_file = lock_log_file();
	time_output();
	ip_output(con->ip);
	fprintf(log_file,"(%s)Logoff Success\n",info->name);
	unlock_log_file();
	connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
}

inline char* connection_get_req_url(const char* str,int max);
/*ケース：HTTPリクエストの処理*/
inline void case_action_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int code;
	USER_INFO* info = get_user(req->user_id);

	if(info == null){//ユーザが見つからない
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"Request: USER NOT FOUND ID:%08x\n",req->user_id);
		unlock_log_file();
		return;
	}

	//接続
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

	//チェック
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

	//送信
	if(connect_request(req) && send_request(req)){
		TCPsocket* s_sock = request_get_sock(req);
		char data[4096];
		int total_size = 0;
		int size;
		//ヘッダを返す
		connection_return_req_data_header(con,CONNECTION_ACTION_RESULT);
		//データ
		do{
			size = SDLNet_TCP_Recv(*s_sock,data,4096);
			total_size+=size;
			connection_return_req_data(con,data,size);
		}while(size > 0);
		{//データ送信終了
		char* request_str = connection_get_req_url(req->data,req->data_size);
		log_file = lock_log_file();
		time_output();
		fprintf(log_file,"(%s)<%s:%d>%s %dbytes\n",info->name,req->host,req->host_port,request_str,total_size);
		unlock_log_file();
		free(request_str);
		}
	}else{//エラー
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

/*エラーを送信する。*/
void connection_send_error(TCPsocket* sock){
	FILE* err_file = fopen("err_reply.txt", "rb");
	int start,end,size;
	char buff[1024];
	/*ファイルサイズの取得*/
	start = ftell(err_file);
	fseek(err_file, 0, SEEK_END);
	end = ftell(err_file);
	fseek(err_file, 0, SEEK_SET);
	size = end - start;
	/*送信*/
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
/*結果を返すための出力用*/
void connection_return_req_data_header(CONNECTION_DATA* con,Uint32 result_code){
	//static char content_length_header[1024];
	int res_code_swapped = Utl_readInt((char*)&result_code);
	TCPsocket* sock = &con->socket;
	//ヘッダ
	NetUtl_sendLine(sock,
	"HTTP/1.1 200 OK\n"
	"Date: Thu, 26 Apr 2007 09:04:24 GMT\n"
	"Server: Haduki\n"
	"Content-Type: image/x-png\n"
	);
	//sprintf(content_length_header,"Content-Length: %d\n",size+4);
	//NetUtl_sendLine(sock,content_length_header);
	//データ送信
	NetUtl_sendLine(sock,"\n");
	//リザルトコード
	sendCrypt(&con->info->crypt,sock,(char*)&res_code_swapped,4);
}
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size){
	//データ
	if(data != NULL && size > 0)sendCrypt(&con->info->crypt,&con->socket,data,size);
}
