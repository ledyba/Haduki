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
//SDL�ˤ�륹��å���

int com_pipe[THREAD_MAX][2];

/*�ҥץ�����ư��*/
int init_child_process(){
		int pid = fork();
		if(pid == 0){//�ҥץ���
			{//���ߥ�˥�������󥹥åɥ����
			int i=0;
			for(;i<THREAD_MAX;i++){
				CONNECTION_DATA* con_data = &ConnectionData[i];
				//�ѥ���
				memcpy(&con_data->com_pipe,com_pipe[i],sizeof(com_pipe[i]));
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
	//�Ե�����
	for(i=0;i<THREAD_MAX;i++){
		
	}
	exit(EXIT_SUCCESS);
}
*/

int main(int argc,char *argv[]){
	FILE* pid_file = fopen("Haduki.pid","w");
	int pid = 0;
	int i;
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

