#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include "crypt.h"
void initCrypt(CRYPT* cry){
}
void sendCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size){
	int i;
	//テスト
	for(i=0;i<size;i++){
		data[i] ^= 45;
	}
	//テスト終わり
	SDLNet_TCP_Send(*sock,data,size);
}
int recvCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size){
	int ret = SDLNet_TCP_Recv(*sock,data,size);
	int i;
	//テスト
	for(i=0;i<ret;i++){
		data[i] ^= 45;
	}
	//テスト終わり
	return ret;
}
