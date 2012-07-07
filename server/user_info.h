#ifndef USER_INFO_H_
#define USER_INFO_H_

#define LOGIN	1
#define LOGOFF	2

#define DISCONNECTED	0
#define CONNECTED		1

#define USER_INFO_KEY_SIZE 32
#define MAX_USER 32
#define MAX_USER_NAME_LENGTH 128

#define USER_INFO_FILE_NAME "user.db"

#include "crypt.h"
typedef struct USER_INFO{
	Uint32 user_id;
	char pass[USER_INFO_KEY_SIZE];
	const char* name;
	CRYPT crypt;
	int login_state;
	int connection_state;
	Uint32 session_id;
	time_t last_access;
	IPaddress ip;
	SDL_mutex* mutex;
}USER_INFO;

int UserMax;
USER_INFO UserInfo[MAX_USER];

void init_login();
void free_login();
USER_INFO* get_user(Uint32 user_id);
#define USER_LOGIN_SUCCESS				100
#define USER_LOGIN_ALREADY_LOGIN		101
#define USER_LOGIN_PASSWORD_ERROR		102
#define USER_LOGIN_DEFINED_TIME		((double)300.0)
int login_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id,IPaddress* ip);
#define USER_LOGOFF_SUCCESS				200
#define USER_LOGOFF_NOT_LOGIN			201
#define USER_LOGOFF_PASSWORD_ERROR		202
#define USER_LOGOFF_INVALID_SESSIONID	203
#define USER_LOGOFF_DIFFERENT_IP		204
int logoff_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id,IPaddress* ip);
#define USER_CHECK_SUCCESS				300
#define USER_CHECK_NOT_LOGIN			301
#define USER_CHECK_PASSWORD_ERROR		302
#define USER_CHECK_INVALID_SESSIONID	303
#define USER_CHECK_DIFFERENT_IP			304
int check_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id,IPaddress* ip);

int connect_user(USER_INFO* info);
int disconnect_user(USER_INFO* info);


#endif /*USER_INFO_H_*/
