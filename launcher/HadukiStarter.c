#include <stdlib.h>
#include <windows.h>
STARTUPINFO lolifox_startup_info;
PROCESS_INFORMATION lolifox_process_info;
STARTUPINFO java_startup_info;
PROCESS_INFORMATION java_process_info;

int WINAPI WinMain (HINSTANCE hInstance, 
			HINSTANCE hPrevInstance, 
			PSTR szCmdLine, 
			int iCmdShow){
/*�@���ꂾ�ƁA�R���\�[�����N�����Ă��܂��B
	system("lolifox\\bin\\lolifox.exe -profile lolifox\\Data");
	system("jre\\bin\\javaw.exe -jar HadukiClient.jar");
*/
CreateProcessA(
    NULL,										// ���s�t�@�C����
    "jre\\bin\\javaw.exe -jar HadukiClient.jar",// �R�}���h���C���p�����[�^
    NULL,								// �v���Z�X�̕ی쑮��
    NULL,								// �X���b�h�̕ی쑮��
    FALSE,								// �I�u�W�F�N�g�n���h���p���̃t���O
    DETACHED_PROCESS | 
	CREATE_NEW_PROCESS_GROUP | 
	NORMAL_PRIORITY_CLASS,				// �����t���O
    NULL,								// ���ϐ����ւ̃|�C���^
    NULL,								// �N�����J�����g�f�B���N�g��
    &java_startup_info,					// �E�B���h�E�\���ݒ�
    &java_process_info					// �v���Z�X�E�X���b�h�̏��
);
CloseHandle(&java_process_info.hProcess);
CloseHandle(&java_process_info.hThread);

CreateProcessA(
    NULL,		// ���s�t�@�C����
    "lolifox\\bin\\lolifox.exe -profile lolifox\\Data",// �R�}���h���C���p�����[�^
    NULL,								// �v���Z�X�̕ی쑮��
    NULL,								// �X���b�h�̕ی쑮��
    FALSE,								// �I�u�W�F�N�g�n���h���p���̃t���O
    DETACHED_PROCESS | 
	CREATE_NEW_PROCESS_GROUP | 
	NORMAL_PRIORITY_CLASS,				// �����t���O
    NULL,								// ���ϐ����ւ̃|�C���^
    NULL,								// �N�����J�����g�f�B���N�g��
    &lolifox_startup_info,				// �E�B���h�E�\���ݒ�
    &lolifox_process_info				// �v���Z�X�E�X���b�h�̏��
);
CloseHandle(lolifox_process_info.hProcess);
CloseHandle(lolifox_process_info.hThread);
 	return (0);
}
