#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h>
#include <signal.h>
#include "main.h"
#include "request.h"

void init_request(REQUEST* req,
			Uint32 user_id,
			const char pass[USER_INFO_KEY_SIZE],
			Uint32 session_id,
			Uint32 action_code,
			Uint16 host_port,
			char* host,
			Uint32 data_size,
			char* data){
	req->user_id = user_id;
	memcpy(req->pass,pass,USER_INFO_KEY_SIZE);
	req->session_id = session_id;
	req->action_code = action_code;
	req->host_port = host_port;
	req->host = host;
	req->data_size = data_size;
	req->data = data;
	req->connected = false;
}
int connect_request(REQUEST* req){
	if(SDLNet_ResolveHost(&req->ip,req->host,req->host_port)==-1){
	    printf("connect_request: %s\n", SDLNet_GetError());
	    return false;
	}
	req->sock = SDLNet_TCP_Open(&req->ip);
	if(!(req->sock)){
		printf("connect_request: %s\n", SDLNet_GetError());
		return false;
	}
	req->connected = true;
	return true;
}
int send_request(REQUEST* req){
	int ret = (req->data_size == 
				SDLNet_TCP_Send(req->sock, req->data, req->data_size));
	return ret;
}
inline int request_get_action_code(const REQUEST* req){
	return req->action_code;
}
TCPsocket* request_get_sock(REQUEST* req){
	return &req->sock;
}
void request_close_connection(REQUEST* req){
	if(req->connected){
		SDLNet_TCP_Close(req->sock);
		req->connected = false;
	}
}
void free_request(REQUEST* req){
	free(req->host);
	free(req->data);
}
