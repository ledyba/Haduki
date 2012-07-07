#ifndef MANAGER_H_
#define MANAGER_H_

typedef struct{
	int user_id;
	int user_pass;
	int status;
	char secret[16];
}USER_LOGIN_DATA;

//´Ø¿ô
void manager_main();
void manager_free();

#define MANAGER_ACTION_CONNET 0x12451256

#endif /*MANAGER_H_*/
