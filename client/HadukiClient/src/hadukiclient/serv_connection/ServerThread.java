package hadukiclient.serv_connection;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.net.ConnectException;

/**
 * <p>�^�C�g��: �u�t���v</p>
 *
 * <p>����: </p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class ServerThread extends Thread {
    private RequestQueue Queue;
    private ServerCommunicator ServerCom;
    //�����ϐ��֌W
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
        //������
        if (!ServerCom.prepareKeys()) {
            //�G���[
            Connected = CODE_CANNNOT_PREPARE_KEY;
            ConnectedCond.signalAll();
            return;
        }
        try {/*�ڑ��v��*/
            ConnectedLock.lock();
            boolean start_flag = ServerCom.sendConnectionStart();
            if (!start_flag) {/*���ʂ͂��߂�����*/
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
            //�T�C�Y��0�Ȃ��
            if (Queue.size() <= 0) {
                //�����ϐ���҂�
                if(!Queue.change_wait(false)){
                    continue;
                }
            }
            //���N�G�X�g���擾
            Request req = Queue.remove();
            if(req == null)continue;
            //���N�G�X�g���M
            ServerCom.sendRequest(req);
            //�N���C�A���g�ɖ߂��̂͏����ϐ����󂯂�ClientThread������Ă����
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
