#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include "main.h"
#include "user_info.h"

inline int login_pass_cmp(const USER_INFO* info,const char pass[USER_INFO_KEY_SIZE]);
void init_login(){
/*
	int i;
	for(i=0;i<){
	}
*/
}
void init_login_user(USER_INFO* info,Uint32 user_id,char pass[USER_INFO_KEY_SIZE],const char* name){
	info->user_id = user_id;
	memcpy(info->pass,pass,USER_INFO_KEY_SIZE);
	info->name = name;
}
void free_login_user(USER_INFO* info){
	SDL_DestroyMutex(info->mutex);
	free((void*)info->name);
}
USER_INFO* get_user(Uint32 user_id){
	int i;
	for(i=0;i<MAX_USER;i++){
		USER_INFO* info = &UserInfo[i];
		if(info->user_id == user_id)return info;
	}
	return null;
}
int login_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id){
	//ログインかつ最終ログインから10分以内の場合は蹴る
	if(info->login_state == LOGIN)	return false;
	//パスワードが一致しなければ当然蹴る
	if(!login_pass_cmp(info,pass))	return false;
	//書き換える前にはmutex
	SDL_mutexP(info->mutex);
	SDL_mutexV(info->mutex);
	return true;
}
int logoff_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id){
	return true;
}
inline int login_pass_cmp(const USER_INFO* info,const char pass[USER_INFO_KEY_SIZE]){
	const char* info_pass = info->pass;
	int i;
	for(i=0;i<USER_INFO_KEY_SIZE;i++){
		if(*(info_pass++) != *(pass++))return false;
	}
	return true;
}
