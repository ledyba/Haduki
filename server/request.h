#ifndef REQUEST_H_
#define REQUEST_H_

#include "user_info.h"
typedef struct REQUEST{
	/*受信したリクエスト*/
	Uint32 user_id;
	char pass[USER_INFO_KEY_SIZE];
	Uint32 session_id;
	Uint32 action_code;
	Uint16 host_port;
	char* host;
	Uint32 data_size;
	char* data;
	//通信
	USER_INFO* info;
	CRYPT backup;
	int enc_size;
	char* enc_data;
	int connected;
	IPaddress ip;
	TCPsocket sock;
}REQUEST;

int init_request(REQUEST* req,USER_INFO* info,Uint32 action_code,char* enc_data,int size);

int connect_request(REQUEST* req);
int send_request(REQUEST* req);
TCPsocket* request_get_sock(REQUEST* req);
void free_request(REQUEST* req);
void request_close_connection(REQUEST* req);
inline int request_get_action_code(const REQUEST* req);
#endif /*REQUEST_H_*/
