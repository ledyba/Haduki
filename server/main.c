#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include <time.h>
#include "main.h"
#include "connection.h"
#include "manager.h"
#include "utils.h"
//SDLによるスレッド版

int com_pipe[THREAD_MAX][2];
void init_time();
void free_time();

/*子プロセス起動用*/
int init_child_process(){
		int pid = fork();
		if(pid == 0){//子プロセス
			{//コミュニケーションスッドレ作成
			int i=0;
			for(;i<THREAD_MAX;i++){
				CONNECTION_DATA* con_data = &ConnectionData[i];
				//初期化
				init_connection(con_data,com_pipe[i]);
				//スッドレ作成
				ConnectionThreads[i] = SDL_CreateThread(connection_main, con_data);
			}}
			//メインスッドレはマネージャになります。
			manager_main();
			return EXIT_SUCCESS;
		}else if(pid > 0){//親プロセス
			return pid;
		}else{//エラー
			return -1;
		}
}

/*
//開放
void main_free(int sig){
	int i;
	//マネージャのフリー
	manager_free();
	//スレッドに終了シグナルを送る
	for(i=0;i<THREAD_MAX;i++){
		
	}
	free_log_file();
	free_time();
	exit(EXIT_SUCCESS);
}
*/

//ログ
SDL_mutex* log_mutex;
FILE* log_file;
int file_locked = false;
void init_log_file(){
	log_mutex = SDL_CreateMutex();
}
void free_log_file(){
	SDL_DestroyMutex(log_mutex);
}
FILE* lock_log_file(){
	SDL_mutexP(log_mutex);
	file_locked = true;
	return (log_file = fopen("Haduki.log","a"));
}
void unlock_log_file(){
    fclose(log_file);
	file_locked = false;
	SDL_mutexV(log_mutex);
}
//時間
SDL_mutex* time_mutex;
struct tm* now_time = null;
time_t now_time_sec;
void time_update(){
	SDL_mutexP(time_mutex);
	time(&now_time_sec);
	now_time = localtime(&now_time_sec);
	SDL_mutexV(time_mutex);
}
void init_time(){
	time_mutex = SDL_CreateMutex();
}
void free_time(){
	SDL_DestroyMutex(time_mutex);
}
void time_output(){
	if(!file_locked)return;
	time_update();
	fprintf(log_file,"[%04d/%02d/%02d %02d:%02d:%02d]",
	now_time->tm_year+1900,
	now_time->tm_mon+1,
	now_time->tm_mday,
	now_time->tm_hour,
	now_time->tm_min,
	now_time->tm_sec
	);
}
void ip_output(const IPaddress* ip){
	int ip_addr = Utl_readInt((char*)&ip->host);
	int port = ip->port;
	fprintf(log_file,"<%d.%d.%d.%d:%d>",
		(ip_addr & 0xff000000) >> 24,
		(ip_addr & 0x00ff0000) >> 16,
		(ip_addr & 0x0000ff00) >>  8,
		(ip_addr & 0x000000ff) >>  0,
		port
	);
}

int main(int argc,char *argv[]){
	FILE* pid_file = fopen("Haduki.pid","w");
	int pid = 0;
	int i;
	//ログファイル初期化
	init_log_file();
	//複数プロセスの起動
	sigignore( SIGCLD );
	//処理の設定
//	signal( SIGTERM , main_free );
	//パイプ初期化
	for(i=0;i<THREAD_MAX;i++){
			pipe(com_pipe[i]);
	}
	//子プロセス起動
	pid = init_child_process();
	fprintf(pid_file,"%d",pid);
    fclose(pid_file);
    //はい、おしまい。
	return EXIT_SUCCESS;
}
