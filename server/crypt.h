#ifndef CRYPT_H_
#define CRYPT_H_

typedef struct CRYPT{
}CRYPT;

void initCrypt(CRYPT* cry);
void sendCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size);
int recvCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size);

#endif /*CRYPT_H_*/
