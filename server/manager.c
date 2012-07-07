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
	SDLNet_SocketSet SockSet;
	IPaddress IP;
	TCPsocket WaitSocket;
	TCPsocket Socket;

/*���Υե�������ΤߤǻȤ��ؿ������*/
void manager_write_action_code(int pipe,int code,void* data,int size);

//����
void manager_free(){
	SDLNet_FreeSocketSet(SockSet);
	SDLNet_TCP_Close(WaitSocket);
	SDLNet_TCP_Close(Socket);
}

//�ѿ��ν���
void manager_init(){
	//�����åȥޥ͡�����
	SockSet=SDLNet_AllocSocketSet(1);
	if(SockSet == NULL){
		fprintf(stderr,"�ޥ͡����㡧SDLNet_AllocSocketSet: %s\n",SDLNet_GetError());
		return;
	}
	//�ꥹ�˥󥰥����åȤκ���
	if(SDLNet_ResolveHost(&IP,NULL,PORT)==-1) {
	    fprintf(stderr,"�ޥ͡����㡧SDLNet_ResolveHost: %s\n", SDLNet_GetError());
	    return;
	}
	WaitSocket=SDLNet_TCP_Open(&IP);
	if(WaitSocket == NULL) {
	    fprintf(stderr,"�ޥ͡����㡧SDLNet_TCP_Open: %s\n", SDLNet_GetError());
	    return;
	}
	//��Ͽ����
	if(SDLNet_TCP_AddSocket(SockSet,WaitSocket) < 0){
	    fprintf(stderr,"�ޥ͡����㡧SDLNet_TCP_AddSocket: %s\n", SDLNet_GetError());
	    return;
	}
}

void manager_write_action_code(int pipe,int code,void* data,int size){
	write(pipe,&code,sizeof(code));
	write(pipe,data,size);
}

void manager_main(){
	int end = false;
	int i,connected;
	/*�������館*/
	manager_init();
	/*�̿�*/
	while(!end){
		if(SDLNet_CheckSockets(SockSet, -1) < 0)continue;
		Socket = SDLNet_TCP_Accept(WaitSocket);
		/*��������å�ȯ��*/
		connected = false;
		for(i=0;i<THREAD_MAX;i++){
			CONNECTION_DATA* con = &ConnectionData[i];
			if(con->is_connected == NOT_CONNECTED){
				/*�ؼ�*/
				manager_write_action_code(	con->com_pipe[PIPE_WRITE],
											MANAGER_ACTION_CONNET,
											&Socket,
											sizeof(Socket)
										);
				connected = true;
				break;
			}
		}
		/*��������åɤ�̵���ä����̿�����*/
		if(!connected){
			SDLNet_TCP_Close(Socket);
		}
	}
	/*����*/
	manager_free();
}

