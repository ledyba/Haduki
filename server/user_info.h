#ifndef USER_INFO_H_
#define USER_INFO_H_

#define TIME_OUT (10 * 60)
#define LOGIN	1
#define LOGOFF	2
#define USER_INFO_KEY_SIZE 32
#define MAX_USER 32

typedef struct USER_INFO{
	Uint32 user_id;
	char pass[USER_INFO_KEY_SIZE];
	const char* name;
	int login_state;
	Uint32 session_id;
	time_t last_access;
}USER_INFO;

USER_INFO UserInfo[MAX_USER];

void init_login();
void init_login_user(USER_INFO* info,Uint32 user_id,char pass[32],const char* name);
USER_INFO* get_user(Uint32 user_id);
void login_user(USER_INFO* info,Uint32 session_id);

#endif /*USER_INFO_H_*/
