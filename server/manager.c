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
	SDLNet_SocketSet SockSet;
	IPaddress IP;
	TCPsocket WaitSocket;
	TCPsocket Socket;

/*���Υե�������ΤߤǻȤ��ؿ������*/
void manager_write_action_code(int pipe,int code,const void* data,int size);
void manager_init_filter_list();
void manager_free_filter_list();
inline int filter_check_ip_addr(const IPaddress* ip);
inline int ipcmp(const IPaddress* a,const IPaddress* b);

//����
void manager_free(){
	SDLNet_FreeSocketSet(SockSet);
	SDLNet_TCP_Close(WaitSocket);
	SDLNet_TCP_Close(Socket);
	free_login();
	manager_free_filter_list();
}

//�ѿ��ν���
void manager_init(){
	//�ե��륿�ꥹ��
	manager_init_filter_list();
	//�����󥷥��ƥ�ν����
	init_login();
	//�����åȥޥ͡�����
	SockSet=SDLNet_AllocSocketSet(1);
	if(SockSet == NULL){
		fprintf(stderr,"�ޥ͡����㡧SDLNet_AllocSocketSet: %s\n",SDLNet_GetError());
		return;
	}
	//�ꥹ�˥󥰥����åȤκ���
	if(SDLNet_ResolveHost(&IP,NULL,PORT)==-1) {
	    fprintf(stderr,"�ޥ͡����㡧SDLNet_ResolveHost: %s\n", SDLNet_GetError());
	    return;
	}
	WaitSocket=SDLNet_TCP_Open(&IP);
	if(WaitSocket == NULL) {
	    fprintf(stderr,"�ޥ͡����㡧SDLNet_TCP_Open: %s\n", SDLNet_GetError());
	    return;
	}
	//��Ͽ����
	if(SDLNet_TCP_AddSocket(SockSet,WaitSocket) < 0){
	    fprintf(stderr,"�ޥ͡����㡧SDLNet_TCP_AddSocket: %s\n", SDLNet_GetError());
	    return;
	}
}

void manager_init_filter_list(){
	FILE* list_file;
	FILE* log_file;
	char* host = NULL;
	int size = 0;
	IPaddress ip;
	/*ALLOW*/
	FilterList.allow_size = 0;
	FilterList.allow_all = false;
	list_file = fopen(FILTER_ALLOW_LIST_NAME,"r");
	if(list_file != null){
		while((host = freadLine(list_file)) != null){
			if(strncmp(host,FILTER_COMMENT,strlen(FILTER_COMMENT)) == 0){
				continue;
			}
			if(strncmp(host,FILTER_ALL,strlen(FILTER_ALL)) == 0){
				free(FilterList.allow);
				size = 0;
				FilterList.allow_all = true;
				break;
			}
			if(SDLNet_ResolveHost(&ip,host,PORT) == 0){
				size++;
				FilterList.allow = realloc(FilterList.allow,sizeof(ip) * size);
				memcpy(&FilterList.allow[size-1],&ip,sizeof(ip));
			}
		}
		FilterList.allow_size = size;
		fclose(list_file);
	}
	/*DENY*/
	FilterList.deny_size = 0;
	FilterList.deny_all = false;
	list_file = fopen(FILTER_DENY_LIST_NAME,"r");
	if(list_file != null){
		size = 0;
		while((host = freadLine(list_file)) != null){
			if(strncmp(host,FILTER_COMMENT,strlen(FILTER_COMMENT)) == 0){
				continue;
			}
			if(strncmp(host,FILTER_ALL,strlen(FILTER_ALL)) == 0){
				free(FilterList.deny);
				size = 0;
				FilterList.deny_all = true;
				break;
			}
			if(SDLNet_ResolveHost(&ip,host,PORT) == 0){
				size++;
				FilterList.deny = realloc(FilterList.deny,sizeof(ip) * size);
				memcpy(&FilterList.deny[size-1],&ip,sizeof(ip));
			}
		}
		FilterList.deny_size = size;
		fclose(list_file);
	}
	//�ե��륿�������λ
	log_file = lock_log_file();
	time_output();
	fprintf(log_file,"Initialized Filtering System.\n");
	unlock_log_file();
}

void manager_free_filter_list(){
	free(FilterList.deny);
	free(FilterList.allow);
}

void manager_write_action_code_all(int code,const void* data,int size){
	int i;
	for(i=0;i<THREAD_MAX;i++){
		CONNECTION_DATA* con = &ConnectionData[i];
		manager_write_action_code(	con->com_pipe[PIPE_WRITE],
									code,
									data,
									size
								);
	}
}

void manager_write_action_code(int pipe,int code,const void* data,int size){
	write(pipe,&code,sizeof(code));
	if(data != null && size > 0) write(pipe,data,size);
}

void manager_main(){
	int end = false;
	int i,connected;
	FILE* log_file;
	IPaddress* ip;
	/*�����ɲ�*/
	log_file = lock_log_file();
	time_output();
	fprintf(log_file,"Haduki started.\n");
	unlock_log_file();
	/*�������館*/
	manager_init();
	/*�̿�*/
	while(!end){
		if(SDLNet_CheckSockets(SockSet, -1) < 0)continue;
		Socket = SDLNet_TCP_Accept(WaitSocket);
		//�ե��륿�����å�
		if(!filter_check_ip_addr(ip = SDLNet_TCP_GetPeerAddress(Socket))){
			SDLNet_TCP_Close(Socket);//����
			log_file = lock_log_file();
			time_output();
			ip_output(ip);
			fprintf(log_file,"Access refused.\n");
			unlock_log_file();
			continue;
		}
		/*��������åɸ���*/
		connected = false;
		for(i=0;i<THREAD_MAX;i++){
			CONNECTION_DATA* con = &ConnectionData[i];
			if(!is_locked_connection(con)){
				/*�ؼ�*/
				manager_write_action_code(	con->com_pipe[PIPE_WRITE],
											MANAGER_ACTION_CONNECT,
											&Socket,
											sizeof(Socket)
										);
				connected = true;
				break;
			}
		}
		/*��������åɤ�̵���ä����̿�����*/
		if(!connected){
			log_file = lock_log_file();
			time_output();
			ip_output(ip);
			fprintf(log_file,"Connection busy.\n");
			unlock_log_file();
			SDLNet_TCP_Close(Socket);
		}
	}
	/*����*/
	manager_free();
}

inline int filter_check_ip_addr(const IPaddress* ip){
	int i;
	int max;
	const IPaddress* filter_list;
	//allow_all�ʤ���������
	if(FilterList.allow_all)return true;
	//allow�ꥹ�Ȥ����äƤ���е���
	max = FilterList.allow_size;
	filter_list = FilterList.allow;
	for(i=0;i<max;i++){
		if(ipcmp(ip,&filter_list[i]))return true;
	}
	//deny_all�ʤ���������Ĥ��ʤ���
	if(FilterList.deny_all)return false;
	//deny�ꥹ�Ȥ����äƤ���н���
	max = FilterList.deny_size;
	filter_list = FilterList.deny;
	for(i=0;i<max;i++){
		if(ipcmp(ip,&filter_list[i]))return false;
	}
	return true;
}
inline int ipcmp(const IPaddress* a,const IPaddress* b){
	return (a->host == b->host);
}
