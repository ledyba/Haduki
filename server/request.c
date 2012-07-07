#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h>
#include <signal.h>
#include "main.h"
#include "utils.h"
#include "request.h"


int parse_request(REQUEST* req);
int init_request(REQUEST* req,USER_INFO* info,Uint32 action_code,char* enc_data,int size){
	req->user_id = info->user_id;
	req->action_code = action_code;
	req->info = info;
	req->enc_size = size;
	req->enc_data = enc_data;
	req->host = null;
	req->data = null;
	return (parse_request(req));
}

int parse_request(REQUEST* req){
	char* recv = req->enc_data;
	int idx = 0;
	int total_size = req->enc_size;
	int host_size;
	int state = false;

	memcpy(req->pass,&recv[idx],USER_INFO_KEY_SIZE);
	idx+=USER_INFO_KEY_SIZE;

	req->session_id = Utl_readInt(&recv[idx]);
	idx+=4;

	host_size = Utl_readInt(&recv[idx]);
	idx+=4;

	if(host_size > 0 && host_size <= total_size - idx - 4 -2){
		req->host = malloc(host_size+1);
		memcpy(req->host,&recv[idx],host_size);
		req->host[host_size] = '\0';
		idx+=host_size;

		req->host_port = Utl_readShort(&recv[idx]);
		idx+=2;
	}else if(host_size == 0 && total_size - idx - 4 == 0){
		req->host = null;
	}else{
		req->host = null;
		goto end;
	}

	req->data_size = Utl_readInt(&recv[idx]);
	idx+=4;

	if(req->data_size > 0 && req->data_size <= total_size - idx){
		req->data = malloc(req->data_size);
		memcpy(req->data,&recv[idx],req->data_size);
		idx+=req->data_size;
	}else if(req->data_size == 0){
		req->data = null;
	}else{
		free(req->host);
		req->host = null;
		req->data = null;
		goto end;
	}
	if(idx != total_size){
		free(req->host);
		free(req->data);
		req->host = null;
		req->data = null;
		goto end;
	}
	state = true;
end:
	free(recv);
	req->enc_data = null;
	return state;
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
	free(req->enc_data);
}
