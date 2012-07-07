package hadukiclient.serv_connection;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.net.ConnectException;

/**
 * <p>タイトル: 「葉月」</p>
 *
 * <p>説明: </p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class ServerThread extends Thread {
    private RequestQueue Queue;
    private ServerCommunicator ServerCom;
    //条件変数関係
    private final Lock ConnectedLock = new ReentrantLock();
    private final Condition ConnectedCond = ConnectedLock.newCondition();
    public static int CODE_CONNECTED = 100;
    public static int CODE_CANNNOT_PREPARE_KEY = 201;
    public static int CODE_CANNNOT_START_COMMUNICATION = 202;
    public static int CODE_ERROR = 203;
    private int Connected = 0;
    public ServerThread(String server, int port, HTTP_Proxy proxy,
                        LoginInfo info,RequestQueue queue) {
        Queue = queue;
        ServerCom = new ServerCommunicator(server, port, proxy, info);
    }

    public ServerThread(String server, int port, LoginInfo info,RequestQueue queue) {
        this(server, port, null, info,queue);
    }

    public void run() {
        //鍵生成
        if (!ServerCom.prepareKeys()) {
            //エラー
            Connected = CODE_CANNNOT_PREPARE_KEY;
            ConnectedCond.signalAll();
            return;
        }
        try {/*接続要求*/
            ConnectedLock.lock();
            boolean start_flag = ServerCom.sendConnectionStart();
            if (!start_flag) {/*結果はだめだった*/
                Connected = CODE_CANNNOT_START_COMMUNICATION;
                //KICKED
                return;
            }
            Connected = CODE_CONNECTED;
        } catch (Exception e){
            Connected = CODE_ERROR;
        } finally {
            ConnectedCond.signalAll();
            ConnectedLock.unlock();
        }
        //ACCEPT
        while (Running) {
            //サイズが0ならば
            if (Queue.size() <= 0) {
                //条件変数を待つ
                if(!Queue.change_wait(false)){
                    continue;
                }
            }
            //リクエストを取得
            Request req = Queue.remove();
            if(req == null)continue;
            //リクエスト送信
            ServerCom.sendRequest(req);
            //クライアントに戻すのは条件変数を受けたClientThreadがやってくれる
        }
        ServerCom.sendConnectionEnd();
        ConnectedLock.lock();
        Disconnected = true;
        DisconnectedCond.signalAll();
        ConnectedLock.unlock();
    }

    public RequestQueue getQueue() {
        return Queue;
    }
    private boolean Disconnected = false;
    private boolean Running = true;
    private final Condition DisconnectedCond = ConnectedLock.newCondition();
    public void disConnect(){
        Running = false;
    }
    public void waitDisconnection(){
        if(Disconnected)return;
        ConnectedLock.lock();
        try {
            DisconnectedCond.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            ConnectedLock.unlock();
        }
    }
    public int isConnectedWithCond() {
        if(Connected != 0)return Connected;
        ConnectedLock.lock();
        try {
            ConnectedCond.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            ConnectedLock.unlock();
        }
        return Connected;
    }
}
