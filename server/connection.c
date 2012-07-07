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

/*このファイル内のみで使う関数を宣言*/
void connection_return_req_data_header(CONNECTION_DATA* con,Uint32 result_code);
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size);
void connection_connect(CONNECTION_DATA* con);
void connection_send_error(TCPsocket* sock);
int connection_do_request(CONNECTION_DATA* con,int content_length);
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
		/*ログに追加*/
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"%s\n",str);
		unlock_log_file();
		//最後まで受信するだけ
		while(*(str = NetUtl_readLine(sock)) != END_CHAR){
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
int connection_do_request(CONNECTION_DATA* con,int content_length){
	FILE* log_file;
	TCPsocket* c_sock = &con->socket;
	char* recv;
	int idx = 0;
	int size;
	int total_size = 0;
	USER_INFO* info;
	REQUEST* req = &con->request;
	//リクエスト関係
	Uint32 user_id;
	Uint32 action_code;
	//非暗号化データ
	user_id = NetUtl_recvInt(c_sock);
	idx+=4;
	action_code = NetUtl_recvInt(c_sock);
	idx+=4;
	content_length -= idx;
	idx = 0;

	//この時点でユーザ検索
	info = get_user(user_id);
	if(info == null){//ユーザが見つからない
		char ch;
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"User NOT FOUND ID:%08x\n",user_id);
		unlock_log_file();
		//最後まで受信しないと、エラーになりがち。
		while(SDLNet_TCP_Recv(*c_sock, &ch, 1) == 1){
		}
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		return false;
	}

	//鍵のバックアップ
	backup_crypt_request(req,info);

	//暗号化データ受信
	recv = malloc(content_length);
	while((size = recvCrypt(&info->crypt,c_sock,
					&recv[total_size],content_length-total_size)) > 0){
		total_size += size;
		if(total_size >= content_length){
			break;
		}
	}

	//接続
	if(!connect_user(info)){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Already connected.\n",info->name);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//リストア
		restore_crypt_request(req);
		return false;
	}

	//エラー
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
		//リストア
		restore_crypt_request(req);
		return false;
	}
	//リクエスト完成
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
		//リストア
		restore_crypt_request(req);
		return false;
	}
	//スイッチ
	switch_request(con);
	disconnect_user(info);
	free_request(&con->request);
	return true;
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
	int code;
	USER_INFO* info = req->info;
	//ログイン
	code = login_user(info,req->pass,req->session_id,con->ip);
	if(code == USER_LOGOFF_SUCCESS){//時間切れでログオフ
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Login Error:Time out and Loggoff\n",info->name);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
		initCrypt(&info->crypt);//この時点で暗号処理の初期化。
		return;
	}else if(code != USER_LOGIN_SUCCESS){//それ以外でエラー
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Login Error:%d\n",info->name,code);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//リストア
		restore_crypt_request(req);
		return;
	}
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
	int code;
	USER_INFO* info = req->info;
	//ログオフ
	if((code = logoff_user(info,req->pass,req->session_id,con->ip)) != USER_LOGOFF_SUCCESS){
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Logoff Error:%d\n",info->name,code);
		unlock_log_file();
		//KICKED
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//リストア
		restore_crypt_request(req);
		return;
	}
	//成功
	log_file = lock_log_file();
	time_output();
	ip_output(con->ip);
	fprintf(log_file,"(%s)Logoff Success\n",info->name);
	unlock_log_file();
	connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
	initCrypt(&info->crypt);//この時点で暗号処理の初期化。
}

inline char* connection_get_req_url(const char* str,int max);
/*ケース：HTTPリクエストの処理*/
inline void case_action_request(CONNECTION_DATA* con){
	REQUEST* req = &con->request;
	FILE* log_file;
	int code;
	USER_INFO* info = req->info;

	//チェック
	code = check_user(info,req->pass,req->session_id,con->ip);
	if(code == USER_LOGOFF_SUCCESS){//時間切れでログオフ
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Request Error:Time out and Loggoff\n",info->name);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_DISCONNECT);
		initCrypt(&info->crypt);//この時点で暗号処理の初期化。
		return;
	}else if(code != USER_CHECK_SUCCESS){//それ以外のエラー
		log_file = lock_log_file();
		time_output();
		ip_output(con->ip);
		fprintf(log_file,"(%s)Request Error:%d\n",info->name,code);
		unlock_log_file();
		//KICK
		connection_return_req_data_header(con,CONNECTION_ACTION_KICKED);
		//リストア
		restore_crypt_request(req);
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
		//リストア
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

/*エラーを送信する。*/
void connection_send_error(TCPsocket* sock){
	FILE* err_file = fopen("err_reply.txt", "rb");
	int start,end,size;
	char buff[4096];
	/*ファイルサイズの取得*/
	start = ftell(err_file);
	fseek(err_file, 0, SEEK_END);
	end = ftell(err_file);
	fseek(err_file, 0, SEEK_SET);
	size = end - start;
	/*送信*/
	while(size > 0){
		fread(buff,4096,1,err_file);
		size -= SDLNet_TCP_Send(*sock, buff, min(4096,size));
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
	"HTTP/1.1 200 OK\r\n"
	"Date: Thu, 26 Apr 2007 09:04:24 GMT\r\n"
	"Server: Haduki\r\n"
	"Connection: close\r\n"
	"Content-Type: image/x-png\r\n"
	"\r\n"
	);
	//リザルトコード
	sendCrypt(&con->request.info->crypt,sock,(char*)&res_code_swapped,4);
}
void connection_return_req_data(CONNECTION_DATA* con,char* data,int size){
	//データ
	if(data != NULL && size > 0)sendCrypt(&con->request.info->crypt,&con->socket,data,size);
}
