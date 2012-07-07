#ifndef MAIN_H_
#define MAIN_H_

#define THREAD_MAX 4
#define PIPE_READ 0
#define PIPE_WRITE 1
#define PORT 44444
#define false	0
#define true	1
#define null NULL

int main(int argc,char *argv[]);

//ログファイル
void init_log_file();
void free_log_file();
FILE* lock_log_file();
void unlock_log_file();
//時間
void time_output();
//IP
void ip_output(const IPaddress* ip);

#endif /*MAIN_H_*/
