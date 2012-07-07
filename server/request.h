#ifndef REQUEST_H_
#define REQUEST_H_

typedef struct RECEIVED_REQUEST{
	Uint32 user_id;
	Uint32 action_code;
	Uint32 size;
	char* data;
}RECEIVED_REQUEST;

#endif /*REQUEST_H_*/
