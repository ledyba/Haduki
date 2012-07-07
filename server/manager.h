#ifndef MANAGER_H_
#define MANAGER_H_

typedef struct HOST_FILTER_LIST{
	int deny_all;
	int deny_size;
	IPaddress* deny;
	int allow_all;
	int allow_size;
	IPaddress* allow;
}HOST_FILTER_LIST;

#define FILTER_DENY_LIST_NAME "deny.txt"
#define FILTER_ALLOW_LIST_NAME "allow.txt"
#define FILTER_ALL "all"
#define FILTER_COMMENT ";"
HOST_FILTER_LIST FilterList;

typedef struct{
	int user_id;
	int user_pass;
	int status;
	char secret[16];
}USER_LOGIN_DATA;

//´Ø¿ô
void manager_main();
void manager_free();
void manager_write_action_code_all(int code,const void* data,int size);

#define MANAGER_ACTION_CONNECT	0x12451256
#define MANAGER_ACTION_KILL		0x12568975

#endif /*MANAGER_H_*/
