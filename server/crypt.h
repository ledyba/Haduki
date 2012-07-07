#ifndef CRYPT_H_
#define CRYPT_H_

#define RC4_KEY_LENGTH 256

typedef struct CRYPT{
	unsigned char key[RC4_KEY_LENGTH];
	unsigned int i,j;
}CRYPT;

void initCrypt(CRYPT* cry);
void encryptData(CRYPT* cry,char* data,int size);
void decryptData(CRYPT* cry,char* data,int size);
void sendCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size);
int recvCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size);

void copyCrypt(CRYPT* dst,const CRYPT* src);
#define backupCrypt(dst,src)	copyCrypt(dst,src)
#define restoreCrypt(dst,src)	copyCrypt(dst,src)

#endif /*CRYPT_H_*/
