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

/*このファイル内のみで使う関数を宣言*/
void manager_write_action_code(int pipe,int code,void* data,int size);

//開放
void manager_free(){
	SDLNet_FreeSocketSet(SockSet);
	SDLNet_TCP_Close(WaitSocket);
	SDLNet_TCP_Close(Socket);
}

//変数の準備
void manager_init(){
	//ソケットマネージャ
	SockSet=SDLNet_AllocSocketSet(1);
	if(SockSet == NULL){
		fprintf(stderr,"マネージャ：SDLNet_AllocSocketSet: %s\n",SDLNet_GetError());
		return;
	}
	//リスニングソケットの作成
	if(SDLNet_ResolveHost(&IP,NULL,PORT)==-1) {
	    fprintf(stderr,"マネージャ：SDLNet_ResolveHost: %s\n", SDLNet_GetError());
	    return;
	}
	WaitSocket=SDLNet_TCP_Open(&IP);
	if(WaitSocket == NULL) {
	    fprintf(stderr,"マネージャ：SDLNet_TCP_Open: %s\n", SDLNet_GetError());
	    return;
	}
	//登録する
	if(SDLNet_TCP_AddSocket(SockSet,WaitSocket) < 0){
	    fprintf(stderr,"マネージャ：SDLNet_TCP_AddSocket: %s\n", SDLNet_GetError());
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
	/*下ごしらえ*/
	manager_init();
	/*通信*/
	while(!end){
		if(SDLNet_CheckSockets(SockSet, -1) < 0)continue;
		Socket = SDLNet_TCP_Accept(WaitSocket);
		/*空きスレッド発見*/
		connected = false;
		for(i=0;i<THREAD_MAX;i++){
			CONNECTION_DATA* con = &ConnectionData[i];
			if(con->is_connected == NOT_CONNECTED){
				/*指示*/
				manager_write_action_code(	con->com_pipe[PIPE_WRITE],
											MANAGER_ACTION_CONNET,
											&Socket,
											sizeof(Socket)
										);
				connected = true;
				break;
			}
		}
		/*空きスレッドが無かった＝通信断絶*/
		if(!connected){
			SDLNet_TCP_Close(Socket);
		}
	}
	/*開放*/
	manager_free();
}

