#ifndef CRYPT_H_
#define CRYPT_H_

#define RC4_KEY_LENGTH 256

typedef struct STREAM{
	unsigned char key[RC4_KEY_LENGTH];
	unsigned int i,j;
}STREAM;

typedef struct CRYPT{
	STREAM master;
	STREAM backup;
	STREAM stream;
}CRYPT;

void initCrypt(CRYPT* cry);
void startCrypt(CRYPT* cry);
void nextStream(CRYPT* cry);

void encryptData(CRYPT* cry,char* data,int size);
void decryptData(CRYPT* cry,char* data,int size);

int sendCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size);
int recvCrypt(CRYPT* cry,TCPsocket* sock,char* data,int size);

#endif /*CRYPT_H_*/
