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

/*���Υե�������ΤߤǻȤ��ؿ������*/
void connection_connect(CONNECTION_DATA* con);
void connection_send_error(TCPsocket* sock);

int connection_main(void* data){
	CONNECTION_DATA* con = (CONNECTION_DATA*)data;
	int action_code;
	//�����ʥ�μ���
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
	/*�ѿ����*/
	TCPsocket *sock = &con->socket;
	char* str;
	int content_length = -1;
	int is_err = false;
	/*��³�Ǥ��ʤ��褦�˾��֤�����*/
	con->is_connected = CONNECTED;
	/*�إå���Ƚ�Ǥ���*/
	str = NetUtl_readLine(sock);
	if(strncmp(str,POST_HEADER,strlen(POST_HEADER)) != 0){
		is_err = true;
	}
	/*�Ȥꤢ�����Ǹ�ޤǼ������롣*/
	if(is_err){//�Ǹ�ޤǼ����������
		while(strncmp( (str = NetUtl_readLine(sock))  ,END_STR,1)){
		}
	}else{//�إå����������
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
	if(!is_err && content_length >= 0){/*�Ȥꤢ�����̿�������ͤ���*/
		
	}else{/*�ޤä����ط��ʤ�*/
		connection_send_error(sock);
	}
	/*�̿������*/
	SDLNet_TCP_Close(*sock);
	/*��³��λ�ե饰*/
	con->is_connected = NOT_CONNECTED;
}
/*���顼���������롣*/
void connection_send_error(TCPsocket* sock){
	FILE* err_file = fopen("err_reply.txt", "rb");
	int start,end,size;
	char buff[1024];
	/*�ե����륵�����μ���*/
	start = ftell(err_file);
	fseek(err_file, 0, SEEK_END);
	end = ftell(err_file);
	fseek(err_file, 0, SEEK_SET);
	size = end - start;
	/*����*/
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
