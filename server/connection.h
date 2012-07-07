#ifndef CONNECTION_H_
#define CONNECTION_H_

#define NOT_CONNECTED 	0
#define CONNECTED 		1

#define CONNECTION_ACTION_CONNECT		0x2F5B8D6E;
#define CONNECTION_ACTION_ACCEPT		0xDBFBD3B9;
#define CONNECTION_ACTION_KICKED		0xF8E9A58E;
#define CONNECTION_ACTION_REQUEST		0x444581CC;
#define CONNECTION_ACTION_RESULT		0xC3FF7E28;
#define CONNECTION_ACTION_DISCONNEST	0x9097FB4B;

//構造体
typedef struct CONNECTION_DATA{
	int is_connected;
	int com_pipe[2];
	IPaddress ip;
	TCPsocket socket;
}CONNECTION_DATA;

//変数
CONNECTION_DATA ConnectionData[THREAD_MAX];
SDL_Thread* ConnectionThreads[THREAD_MAX];

//関数
int connection_main(void* data);
void connection_free(CONNECTION_DATA* connection_data);

#endif /*CONNECTION_H_*/
