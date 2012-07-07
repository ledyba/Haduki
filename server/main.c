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
//SDL�ˤ�륹��å���

int com_pipe[THREAD_MAX][2];
void init_time();
void free_time();

/*�ҥץ�����ư��*/
int init_child_process(){
		int pid = fork();
		if(pid == 0){//�ҥץ���
			{//���ߥ�˥�������󥹥åɥ����
			int i=0;
			for(;i<THREAD_MAX;i++){
				CONNECTION_DATA* con_data = &ConnectionData[i];
				//�����
				init_connection(con_data,com_pipe[i]);
				//���åɥ����
				ConnectionThreads[i] = SDL_CreateThread(connection_main, con_data);
			}}
			//�ᥤ�󥹥åɥ�ϥޥ͡�����ˤʤ�ޤ���
			manager_main();
			return EXIT_SUCCESS;
		}else if(pid > 0){//�ƥץ���
			return pid;
		}else{//���顼
			return -1;
		}
}

/*
//����
void main_free(int sig){
	int i;
	//�ޥ͡�����Υե꡼
	manager_free();
	//����åɤ˽�λ�����ʥ������
	for(i=0;i<THREAD_MAX;i++){
		
	}
	free_log_file();
	free_time();
	exit(EXIT_SUCCESS);
}
*/

//��
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
//����
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
	//���ե���������
	init_log_file();
	//ʣ���ץ����ε�ư
	sigignore( SIGCLD );
	//����������
//	signal( SIGTERM , main_free );
	//�ѥ��׽����
	for(i=0;i<THREAD_MAX;i++){
			pipe(com_pipe[i]);
	}
	//�ҥץ�����ư
	pid = init_child_process();
	fprintf(pid_file,"%d",pid);
    fclose(pid_file);
    //�Ϥ��������ޤ���
	return EXIT_SUCCESS;
}
