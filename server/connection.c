#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include "main.h"
#include "user_info.h"
#include "request.h"
#include "connection.h"
#include "manager.h"
#include "utils.h"

/*このファイル内のみで使う関数を宣言*/
void connection_connect(CONNECTION_DATA* con);
void connection_send_error(TCPsocket* sock);
void connection_do_request(CONNECTION_DATA* con,int content_length);
void switch_request(CONNECTION_DATA* con);
inline void case_action_request(CONNECTION_DATA* con);
inline void case_action_connect(CONNECTION_DATA* con);
inline void case_action_dis_connect(CONNECTION_DATA* con);
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

int connection_main(void* data){
	CONNECTION_DATA* con = (CONNECTION_DATA*)data;
	int action_code;
	//シグナルの受信
	while(read(con->com_pipe[PIPE_READ],&action_code,sizeof(action_code))){
		switch(action_code){
			case MANAGER_ACTION_CONNET:
				read(	con->com_pipe[PIPE_READ],
						&con->socket,
						sizeof(con->socket)
					);
				connection_connect(con);
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

void connection_connect(CONNECTION_DATA* con){
	/*変数宣言*/
	TCPsocket *sock = &con->socket;
	char* str;
	int content_length = -1;
	int is_err = false;
	FILE* log_file;
	Uint32 ip_addr;
	Uint16 port;
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
		/*ログに追加*/
		ip_addr = con->ip->host;
		port = con->ip->port;
		log_file = lock_log_file();
		time_output();
		fprintf(log_file,"<%d.%d.%d.%d:%d>",
			(ip_addr & 0xff000000) >> 24,
			(ip_addr & 0x00ff0000) >> 16,
			(ip_addr & 0x0000ff00) >>  8,
			(ip_addr & 0x000000ff) >>  0,
			port
		);
		fprintf(log_file,"%s\n",str);
		unlock_log_file();
		//最後まで受信するだけ
		while(*(NetUtl_readLine(sock)) != END_CHAR){
		}
	}else{//ヘッダを取得する
		while(*(str = NetUtl_readLine(sock)) != END_CHAR){
			if(content_length < 0){
				if(strncmp(	str,
							CONTENT_LENGTH_HEADER,
							strlen(CONTENT_LENGTH_HEADER)
						)){
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

void connection_do_request(CONNECTION_DATA* con,int content_length){
		TCPsocket* c_sock = &con->socket;
		char* recv;
		int idx = 0;int i;
		int recv_size;
		//データ受信
		recv = malloc(content_length);
		while(
				(recv_size = SDLNet_TCP_Recv(*c_sock,&recv[idx], content_length-idx)) > 0 
			&&	idx < content_length
			){
			idx += recv_size;
		}
		//エラー
		if(recv_size <= 0){
			return;
		}
		//復号化・チェックサムとか
		/*テスト*/
		for(i=0;i<content_length;i++){
			recv[i] ^= 45;
		}
		/*テスト終わり*/
		//リクエスト完成
		idx = 0;
		{
			char pass[USER_INFO_KEY_SIZE];
			char* host = NULL;
			char* data = NULL;
			Uint32 user_id = 0;
			Uint32 session_id = 0;
			Uint32 action_code = 0;
			Uint32 host_size = 0;
			Uint16 host_port = 0;
			Uint32 data_size = 0;

			user_id = Utl_readInt(&recv[0]);
			idx+=sizeof(user_id);

			memcpy(pass,&recv[idx],USER_INFO_KEY_SIZE);
			idx+=USER_INFO_KEY_SIZE;

			session_id = Utl_readInt(&recv[idx]);
			idx+=sizeof(session_id);

			action_code = Utl_readInt(&recv[idx]);
			idx+=sizeof(action_code);

			host_size = Utl_readInt(&recv[idx]);
			idx+=sizeof(host_size);

			if(host_size > 0){

				host = malloc(host_size);
				memcpy(host,&recv[idx],host_size);
				idx+=host_size;

				host_port = Utl_readShort(&recv[idx]);
				idx+=sizeof(host_port);

			}

			data_size = Utl_readInt(&recv[idx]);
			idx+=sizeof(data_size);

			if(data_size > 0){
				data = malloc(data_size);
				memcpy(data,&recv[idx],data_size);
			}

			init_request(&con->request,user_id,pass,session_id,action_code,
				host_port,host,data_size,data);
		}
		free(recv);
		switch_request(con);
}

void switch_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	switch(request_get_action_code(req)){
		case CONNECTION_ACTION_CONNECT:
			case_action_connect(con);
			break;
		case CONNECTION_ACTION_REQUEST:
			case_action_request(con);
			break;
		case CONNECTION_ACTION_DISCONNECT:
			case_action_dis_connect(con);
			break;
		default:
			break;
	}
	free_request(req);
}

inline void case_action_connect(CONNECTION_DATA* con){
	//ログイン
	//返す
}
inline void case_action_dis_connect(CONNECTION_DATA* con){
}
inline void case_action_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	TCPsocket* c_sock = &con->socket;
	FILE* log_file;
	USER_INFO* info = null;
	//IDのチェック
	if(true){//OK
		log_file = lock_log_file();
		fprintf(log_file,"USER:%s REQ:%s:%d/%s\n",info->name,req->host,req->host_port,req->data);
		unlock_log_file();
	}else{//エラー
		/*ログに追加*/
		log_file = lock_log_file();
		fprintf(log_file,"Already login\n");
		unlock_log_file();
		return;
	}
	//送信
	if(connect_request(req) && send_request(req)){
		//データ受信・暗号化・クライアントへ送信
		TCPsocket* s_sock = request_get_sock(req);
	}
	request_close_connection(req);
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
