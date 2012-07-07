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
/*　これだと、コンソールが起動してしまう。
	system("lolifox\\bin\\lolifox.exe -profile lolifox\\Data");
	system("jre\\bin\\javaw.exe -jar HadukiClient.jar");
*/
CreateProcessA(
    NULL,										// 実行ファイル名
    "jre\\bin\\javaw.exe -jar HadukiClient.jar",// コマンドラインパラメータ
    NULL,								// プロセスの保護属性
    NULL,								// スレッドの保護属性
    FALSE,								// オブジェクトハンドル継承のフラグ
    DETACHED_PROCESS | 
	CREATE_NEW_PROCESS_GROUP | 
	NORMAL_PRIORITY_CLASS,				// 属性フラグ
    NULL,								// 環境変数情報へのポインタ
    NULL,								// 起動時カレントディレクトリ
    &java_startup_info,					// ウィンドウ表示設定
    &java_process_info					// プロセス・スレッドの情報
);
CloseHandle(&java_process_info.hProcess);
CloseHandle(&java_process_info.hThread);

CreateProcessA(
    NULL,		// 実行ファイル名
    "lolifox\\bin\\lolifox.exe -profile lolifox\\Data",// コマンドラインパラメータ
    NULL,								// プロセスの保護属性
    NULL,								// スレッドの保護属性
    FALSE,								// オブジェクトハンドル継承のフラグ
    DETACHED_PROCESS | 
	CREATE_NEW_PROCESS_GROUP | 
	NORMAL_PRIORITY_CLASS,				// 属性フラグ
    NULL,								// 環境変数情報へのポインタ
    NULL,								// 起動時カレントディレクトリ
    &lolifox_startup_info,				// ウィンドウ表示設定
    &lolifox_process_info				// プロセス・スレッドの情報
);
CloseHandle(lolifox_process_info.hProcess);
CloseHandle(lolifox_process_info.hThread);
 	return (0);
}
