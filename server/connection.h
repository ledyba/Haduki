#ifndef CONNECTION_H_
#define CONNECTION_H_

#define CONNECTION_ACTION_CONNECT		0x2F5B8D6E
#define CONNECTION_ACTION_ACCEPT		0xDBFBD3B9
#define CONNECTION_ACTION_KICKED		0xF8E9A58E
#define CONNECTION_ACTION_REQUEST		0x444581CC
#define CONNECTION_ACTION_RESULT		0xC3FF7E28
#define CONNECTION_ACTION_DISCONNECT	0x9097FB4B
#define CONNECTION_ACTION_RESET			0xBE0F74BA


//構造体
#include "request.h"
typedef struct CONNECTION_DATA{
	int connected;
	SDL_mutex* connected_mutex;
	int com_pipe[2];
	IPaddress* ip;
	TCPsocket socket;
	REQUEST request;
}CONNECTION_DATA;

//変数
CONNECTION_DATA ConnectionData[THREAD_MAX];
SDL_Thread* ConnectionThreads[THREAD_MAX];
//関数
void init_connection(CONNECTION_DATA* con,int pipe[2]);
int connection_main(void* data);
void connection_free(CONNECTION_DATA* connection_data);
/*ロック関係*/
inline int lock_connection(CONNECTION_DATA* con);
inline int unlock_connection(CONNECTION_DATA* con);
inline int is_locked_connection(CONNECTION_DATA* con);

#endif /*CONNECTION_H_*/
