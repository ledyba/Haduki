# Makefile ( n source file 1 execute file version )
# 2002/12/ 4

# ���ܸ�Υ�������ʬ�˽��äƽ������Ʋ�������
# $(HOGEHOGE) �Ȥ����Τϥޥ����ʰ����ѿ��ˤǡ�
# HOGEHOGE=... ��������줿���Ƥ�Ÿ�����Ʋ�ᤵ��ޤ���
# ��Ƭ�Υ��֤ˤϰ�̣������ޤ��Τǡ����ڡ������Ѵ����ʤ��ǲ�������
# �Ǥ���С��ǽ�Ū�����ܸ쥳���ȤϺ�����Ʋ�������

#	 ���ѥå�����̾���ļ¹ԥե�����̾�˽񤭴�����
PACKAGE	= Haduki
#	 ���������ե��������¤٤�
SRCS	= main.c connection.c manager.c utils.c request.c user_info.c crypt.c
#	 ���إå��ե��������¤٤�
HEADS	= main.h connection.h manager.h utils.h request.h user_info.h crypt.h
OBJS	= $(SRCS:.c=.o)

#	 ���ѥå������˴ޤ��ե�����̾����¤٤�
FILES	= README Makefile $(HEADS) $(SRCS) err_reply.txt restart stop deny.txt allow.txt
VER	= `date +%Y%m%d`


### command and flags ###
# uncomment when debugging
#DEBUG	= -ggdb -pg # -lefence
RELEASE = -s

# common (*.o)
LD	= gcc
LDFLAGS	= -g $(DEBUG) $(RELEASE)
LDLIBS	= -lm `sdl-config --libs` -lSDL_net

# C (*.c)
CC	= gcc
CFLAGS	= -g -O3 -Wall $(DEBUG) $(RELEASE)
CPPFLAGS= -I. `sdl-config --cflags`

# C++ (*.cc)
CXX	= g++
CXXFLAGS= -g -O3 -Wall $(DEBUG)

# Fortran77 (*.f)
FC	= f77
FFLAGS	= -Wall $(DEBUG)

# Pascal (*.p)
PC	= pc
PFLAGS	= -Wall $(DEBUG)

# etc
SHELL	= /bin/sh
RM	= rm -f
PROF	= gprof


### rules ###

.SUFFIXES:
.SUFFIXES: .o .c .cc .f .p

all: $(PACKAGE)

$(PACKAGE): $(OBJS)
	$(LD) $(LDFLAGS) $(OBJS) -o $@ $(LDLIBS)

$(OBJS): $(HEADS) Makefile

.c.o:
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $< -o $@
.cc.o:
	$(CXX) $(CXXFLAGS) $(CPPFLAGS) -c $< -o $@
.f.o:
	$(FC) $(FFLAGS) -c $< -o $@
.p.o:
	$(PC) $(PFLAGS) $(CPPFLAGS) -c $< -o $@


### useful commands ###

clean:
	$(RM) $(PACKAGE) $(OBJS)
	$(RM) core gmon.out *~ #*#

tar:
	@echo $(PACKAGE)-$(VER) > .package
	@$(RM) -r `cat .package`
	@mkdir `cat .package`
	@ln $(FILES) `cat .package`
	tar cvf - `cat .package` | gzip -9 > `cat .package`.tar.gz
	@$(RM) -r `cat .package` .package

zip:
	zip -9 $(PACKAGE)-$(VER).zip $(FILES)


prof: run
	$(PROF) $(PACKAGE) | less

run: all
#	 ������ץ�ǡ����Ǽ¹Ԥ���褦�˽�ľ��
#	    $(PACKAGE) �ϼ¹ԥե�����̾��Ÿ�������
#	    ��Ƭ�ϥ��֡ʥ��ڡ����ǤϤʤ���
	./$(PACKAGE) < sample-data | less