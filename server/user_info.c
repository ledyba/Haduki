#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include <time.h>
#include "main.h"
#include "user_info.h"
#include "utils.h"
#include "crypt.h"


void init_login_user(USER_INFO* info,Uint32 user_id,const char pass[USER_INFO_KEY_SIZE],const char* name);
void free_login_user(USER_INFO* info);
inline int login_pass_cmp(const USER_INFO* info,const char pass[USER_INFO_KEY_SIZE]);
int logoff_user_np(USER_INFO* info,time_t now);

void init_login(){
	FILE* log_file;
	FILE* user_file = fopen(USER_INFO_FILE_NAME,"rb");
	int i;
	Uint32 size;

	Uint32 max;
	int valid = true;

	Uint32 user_id;
	char pass[USER_INFO_KEY_SIZE];
	char* name;

	if(user_file == null){
		log_file = lock_log_file();
		time_output();
		fprintf(log_file,"Couldn't initialize User Login System.File not found.\n");
		unlock_log_file();
	}
	size = fread(&max,4,1,user_file);
	max = Utl_readInt((char*)&max);
	if(size < 1 || max > MAX_USER || max < 0){//エラー
		fclose(user_file);
		UserMax = 0;
		//ログ
		log_file = lock_log_file();
		time_output();
		fprintf(log_file,"Couldn't initialize User Login System.Invalid user size:%d\n",max);
		unlock_log_file();
		return;
	}
	UserMax = max;
	for(i=0;i<max;i++){
		//ユーザID
		if(fread(&user_id,4,1,user_file) < 1){
			valid = false;
			break;
		}
		user_id = Utl_readInt((char*)&user_id);
		//パスワード
		if(fread(pass,USER_INFO_KEY_SIZE,1,user_file) < 1){
			valid = false;
			break;
		}
		//ユーザ文字サイズ
		if(fread(&size,4,1,user_file) < 1){
			valid = false;
			break;
		}
		//文字列の長さを操作
		size = Utl_readInt((char*)&size);
		if(size < 0 && size > MAX_USER_NAME_LENGTH){
			valid = false;
			break;
		}
		//名前
		name = malloc(size+1);
		if(fread(name,size,1,user_file) < 1){
			free(name);
			valid = false;
			break;
		}
		name[size] = '\0';
		init_login_user(&UserInfo[i],user_id,pass,name);
	}
	if(!valid){
		UserMax = i;
		log_file = lock_log_file();
		time_output();
		fprintf(log_file,"Couldn't initialize User Login System.Invalid user data:%d\n",i);
		unlock_log_file();
		//ファイル閉じる
		fclose(user_file);
		return;
	}
	//ファイル閉じる
	fclose(user_file);
	//読み込み完了
	log_file = lock_log_file();
	time_output();
	fprintf(log_file,"Initialized User Login System.\n");
	unlock_log_file();
}
void free_login(){
	int max = UserMax;
	int i;
	for(i=0;i<max;i++){
		free_login_user(&UserInfo[i]);
	}
}
void init_login_user(USER_INFO* info,Uint32 user_id,const char pass[USER_INFO_KEY_SIZE],const char* name){
	info->user_id = user_id;
	memcpy(info->pass,pass,USER_INFO_KEY_SIZE);
	initCrypt(&info->crypt);
	info->mutex = SDL_CreateMutex();
	info->name = name;
}
void free_login_user(USER_INFO* info){
	SDL_DestroyMutex(info->mutex);
	free((void*)info->name);
}
/*ユーザを探す*/
USER_INFO* get_user(Uint32 user_id){
	int i;
	for(i=0;i<UserMax;i++){
		USER_INFO* info = &UserInfo[i];
		if(info->user_id == user_id)return info;
	}
	return null;
}
/*ログイン*/
int login_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id,IPaddress* ip){
	time_t now = time(NULL);
	//ログインかつ最終アクセスから一定時間以内の場合は蹴る
	if(info->login_state == LOGIN){
		if(difftime(now,info->last_access) < USER_LOGIN_DEFINED_TIME){
			return USER_LOGIN_ALREADY_LOGIN;
		}
		return logoff_user_np(info,now);
	}
	//パスワードが一致しなければ当然蹴る
	if(!login_pass_cmp(info,pass))	return USER_LOGIN_PASSWORD_ERROR;
	//書き換える前にはmutex
	SDL_mutexP(info->mutex);
		info->last_access = now;//アクセス時間の記録
		info->login_state = LOGIN;
		info->session_id = session_id;
		info->ip = *ip;
	SDL_mutexV(info->mutex);
	return USER_LOGIN_SUCCESS;
}
int logoff_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id,IPaddress* ip){
	time_t now = time(NULL);
	//すでにログオフしている
	if(info->login_state == LOGOFF)			return USER_LOGOFF_NOT_LOGIN;
	//最終アクセスから一定時間たっている場合はログオフします。
	if(difftime(now,info->last_access) >= USER_LOGIN_DEFINED_TIME){
		return (logoff_user_np(info,now));
	}
	//IPアドレスが異なれば蹴る
	if(info->ip.host != ip->host)			return USER_LOGOFF_DIFFERENT_IP;
	//パスワードが一致しなければ当然蹴る
	if(!login_pass_cmp(info,pass))			return USER_LOGOFF_PASSWORD_ERROR;
	//セッションIDが一致しなければ蹴る
	if(info->session_id != session_id)		return USER_LOGOFF_INVALID_SESSIONID;
	logoff_user_np(info,now);
	return USER_LOGOFF_SUCCESS;
}

int logoff_user_np(USER_INFO* info,time_t now){
	//書き換える前にはmutex
	SDL_mutexP(info->mutex);
		info->last_access = now;//アクセス時間の記録
		info->login_state = LOGOFF;
		info->session_id = 0;
	SDL_mutexV(info->mutex);
	return USER_LOGOFF_SUCCESS;
}

int check_user(USER_INFO* info,const char pass[USER_INFO_KEY_SIZE],Uint32 session_id,IPaddress* ip){
	time_t now = time(NULL);
	//ログインしていない
	if(info->login_state == LOGOFF)			return USER_CHECK_NOT_LOGIN;
	//最終アクセスから一定時間経っている場合はログオフして、蹴る
	if(difftime(now,info->last_access) >= USER_LOGIN_DEFINED_TIME){
		return (logoff_user_np(info,now));
	}
	//IPが異なる
	if(info->ip.host != ip->host)			return USER_CHECK_DIFFERENT_IP;
	//パスワードが一致しなければ当然蹴る
	if(!login_pass_cmp(info,pass))			return USER_CHECK_PASSWORD_ERROR;
	//セッションIDが一致しなければ蹴る
	if(info->session_id != session_id)		return USER_CHECK_INVALID_SESSIONID;
	//書き換える前にはmutex
	SDL_mutexP(info->mutex);
		info->last_access = now;//アクセス時間の記録
	SDL_mutexV(info->mutex);
	return USER_CHECK_SUCCESS;
}
int connect_user(USER_INFO* info){
	SDL_mutexP(info->mutex);
		if(info->connection_state != CONNECTED){
			info->connection_state = CONNECTED;
			SDL_mutexV(info->mutex);
			return true;
		}else{
			SDL_mutexV(info->mutex);
			return false;
		}
}
int disconnect_user(USER_INFO* info){
	SDL_mutexP(info->mutex);
		if(info->connection_state == CONNECTED){
			info->connection_state = DISCONNECTED;
			SDL_mutexV(info->mutex);
			return true;
		}else{
			SDL_mutexV(info->mutex);
			return false;
		}
}

inline int login_pass_cmp(const USER_INFO* info,const char pass[USER_INFO_KEY_SIZE]){
	const char* info_pass = info->pass;
	int i;
	for(i=0;i<USER_INFO_KEY_SIZE;i++){
		if(*(info_pass++) != *(pass++))return false;
	}
	return true;
}
