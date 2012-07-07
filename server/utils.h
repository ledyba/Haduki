#ifndef NET_UTILS_H_
#define NET_UTILS_H_

#define NEED_SWAP (SDL_BYTEORDER == SDL_LIL_ENDIAN)

char* NetUtl_readLine(TCPsocket* sock);
inline void NetUtl_sendLine(TCPsocket* sock,const char* str);

inline void NetUtl_sendInt(TCPsocket* sock,Uint32 num);
inline Uint32 NetUtl_recvInt(TCPsocket* sock);

inline void NetUtl_sendShort(TCPsocket* sock,Uint16 num);
inline Uint16 NetUtl_recvShort(TCPsocket* sock);

inline int min(int a,int b);
inline int max(int a,int b);

inline Uint32 Utl_readInt(char* data);
inline void Utl_writeInt(Uint32 num,char* data);

inline Uint16 Utl_readShort(char* data);
inline void Utl_writeShort(Uint16 num,char* data);
#endif /*NET_UTILS_H_*/
