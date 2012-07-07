#ifndef NET_UTILS_H_
#define NET_UTILS_H_

#define NEED_SWAP (SDL_BYTEORDER == SDL_LIL_ENDIAN)

char* NetUtl_readLine(TCPsocket* sock);
inline void NetUtl_sendInt(TCPsocket* sock,Uint32 num);
inline Uint32 NetUtl_recvInt(TCPsocket* sock);
inline int min(int a,int b);
inline int max(int a,int b);
inline Uint32 Utl_readInt(char* data);
inline void Utl_writeInt(Uint32 num,char* data);
#endif /*NET_UTILS_H_*/
