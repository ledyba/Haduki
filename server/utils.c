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
	static char* buff = NULL;
	char ch;
	int buff_len = 0,length;
	//バッファクリア
	free(buff);
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
int NetUtl_readAll(TCPsocket* sock,char** data){
	static char* buff = NULL;
	char ch;
	int size = 0;
	//バッファクリア
	free(buff);
	buff = NULL;
	//ループ
	while(SDLNet_TCP_Recv(*sock, &ch, 1) == 1){
		buff = realloc(buff,size+1);
		buff[size] = ch;
		size++;
	}
	data = &buff;
	return size;
}
inline void NetUtl_sendLine(TCPsocket* sock,const char* str){
	SDLNet_TCP_Send(*sock, str, strlen(str));
}

inline int min(int a,int b){
	return a > b ? b :a;
}

inline int max(int a,int b){
	return a < b ? b :a;
}

/*送信*/
inline void NetUtl_sendInt(TCPsocket* sock,Uint32 num){
	#if NEED_SWAP
		num = SDL_Swap32(num);
	#endif
	SDLNet_TCP_Send(*sock,&num,sizeof(num));
}
inline Uint32 NetUtl_recvInt(TCPsocket* sock){
	Uint32 num;
	SDLNet_TCP_Recv(*sock,&num,sizeof(num));
	#if NEED_SWAP
		num = SDL_Swap32(num);
	#endif
	return num;
}

inline void NetUtl_sendShort(TCPsocket* sock,Uint16 num){
	#if NEED_SWAP
		num = SDL_Swap16(num);
	#endif
	SDLNet_TCP_Send(*sock,&num,sizeof(num));
}
inline Uint16 NetUtl_recvShort(TCPsocket* sock){
	Uint16 num;
	SDLNet_TCP_Recv(*sock,&num,sizeof(num));
	#if NEED_SWAP
		num = SDL_Swap16(num);
	#endif
	return num;
}


inline Uint32 Utl_readInt(char* data){
	Uint32 num;
	num =	(data[0] << 24) + 
			(data[1] << 16) + 
			(data[2] <<  8) + 
			(data[3] <<  0);
	#if NEED_SWAP
		num = SDL_Swap32(num);
	#endif
	return num;
}
inline void Utl_writeInt(Uint32 num,char* data){
	#if NEED_SWAP
		num = SDL_Swap32(num);
	#endif
		data[0] = (num & 0xFF000000) >> 24;
		data[1] = (num & 0x00FF0000) >> 16;
		data[2] = (num & 0x0000FF00) >>  8;
		data[3] = (num & 0x000000FF) >>  0;
}

inline Uint16 Utl_readShort(char* data){
	Uint16 num;
	num =	(data[0] << 8) + 
			(data[1] << 0);
	#if NEED_SWAP
		num = SDL_Swap16(num);
	#endif
	return num;
}
inline void Utl_writeShort(Uint16 num,char* data){
	#if NEED_SWAP
		num = SDL_Swap16(num);
	#endif
		data[0] = (num & 0xFF00) >> 8;
		data[1] = (num & 0x00FF) >> 0;
}
