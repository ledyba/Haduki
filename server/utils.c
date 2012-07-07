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

/*一行読み込み*/
char* NetUtl_readLine(TCPsocket* sock){
	static char* buff;
	char ch;
	int buff_len = 0,length;
	//バッファクリア
	if(buff != NULL)free(buff);
	buff = NULL;
	//ループ
	while((length = SDLNet_TCP_Recv(*sock, &ch, 1)) == 1){
		if(ch == '\r')continue;
		buff = realloc(buff,buff_len+1);
		if(ch == '\n'){
			buff[buff_len] = '\0';
			break;
		}
		buff[buff_len] = ch;
		buff_len++;
	}
	//エラー
	if(length < 0){
		if(buff != NULL)free(buff);
		buff = NULL;
	}
	return buff;
}

inline int min(int a,int b){
	return a > b ? b :a;
}

inline int max(int a,int b){
	return a < b ? b :a;
}

/*送信*/
inline void NetUtl_sendInt(TCPsocket* sock,Uint32 num){
	#ifdef NEED_SWAP
		num = SDL_Swap32(num);
	#endif
	SDLNet_TCP_Send(*sock,&num,sizeof(num));
}
inline Uint32 NetUtl_recvInt(TCPsocket* sock){
	Uint32 num;
	SDLNet_TCP_Send(*sock,&num,sizeof(num));
	#ifdef NEED_SWAP
		num = SDL_Swap32(num);
	#endif
	return num;
}

inline Uint32 Utl_readInt(char* data){
	Uint32 num;
	num =	(data[0] << 24) + 
			(data[1] << 16) + 
			(data[2] <<  8) + 
			(data[3] <<  0);
	#ifdef NEED_SWAP
		num = SDL_Swap32(num);
	#endif
	return num;
}
inline void Utl_writeInt(Uint32 num,char* data){
	#ifdef NEED_SWAP
		num = SDL_Swap32(num);
	#endif
		data[0] = (num & 0xFF000000) >> 24;
		data[1] = (num & 0x00FF0000) >> 16;
		data[2] = (num & 0x0000FF00) >>  8;
		data[3] = (num & 0x000000FF) >>  0;
}
