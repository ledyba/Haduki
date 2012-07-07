#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include "main.h"
#include "connection.h"
#include "manager.h"
#include "utils.h"

/*このファイル内のみで使う関数を宣言*/
void connection_connect(CONNECTION_DATA* con);
void connection_send_error(TCPsocket* sock);

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

#define END_STR "\0"

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
	/*接続できないように状態を設定*/
	con->is_connected = CONNECTED;
	/*ヘッダを判断する*/
	str = NetUtl_readLine(sock);
	if(strncmp(str,POST_HEADER,strlen(POST_HEADER)) != 0){
		is_err = true;
	}
	/*とりあえず最後まで受信する。*/
	if(is_err){//最後まで受信するだけ
		while(strncmp( (str = NetUtl_readLine(sock))  ,END_STR,1)){
		}
	}else{//ヘッダを取得する
		while(strncmp( (str = NetUtl_readLine(sock))  ,END_STR,1)){
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
		
	}else{/*まったく関係ない*/
		connection_send_error(sock);
	}
	/*通信終わり*/
	SDLNet_TCP_Close(*sock);
	/*接続終了フラグ*/
	con->is_connected = NOT_CONNECTED;
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
