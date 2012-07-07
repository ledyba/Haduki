#include <SDL.h>
#include <SDL_net.h>
#include <stdio.h>
#include <sys/types.h> 
#include <unistd.h> 
#include <signal.h>
#include "main.h"
#include "connection.h"
#include "manager.h"
#include "utils.h"
//SDLによるスレッド版

int com_pipe[THREAD_MAX][2];

/*子プロセス起動用*/
int init_child_process(){
		int pid = fork();
		if(pid == 0){//子プロセス
			{//コミュニケーションスッドレ作成
			int i=0;
			for(;i<THREAD_MAX;i++){
				CONNECTION_DATA* con_data = &ConnectionData[i];
				//パイプ
				memcpy(&con_data->com_pipe,com_pipe[i],sizeof(com_pipe[i]));
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
	//待機する
	for(i=0;i<THREAD_MAX;i++){
		
	}
	exit(EXIT_SUCCESS);
}
*/

int main(int argc,char *argv[]){
	FILE* pid_file = fopen("Haduki.pid","w");
	int pid = 0;
	int i;
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

