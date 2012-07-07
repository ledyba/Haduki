package hadukiclient.client;

import hadukiclient.serv_connection.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.InetAddress;
import java.util.concurrent.Semaphore;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

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
public class Client extends Thread {
    private RequestQueue ReqQueue;
    private final int MAX_CONNECTION = 1;
    private Semaphore Sem = new Semaphore(MAX_CONNECTION); //コネクション限定用セマフォ
    private LoginInfo LoginInfo;
    public Client(LoginInfo info, RequestQueue queue) {
        LoginInfo = info;
        ReqQueue = queue;
    }

    public void run() {
        ServerSocket serv_sock = null;
        final InetAddress local, local_2;
        Socket sock = null;
        try {
            local = InetAddress.getLocalHost(); //比較用
            local_2 = InetAddress.getByName("127.0.0.1");
            serv_sock = new ServerSocket(7743); //サーバソケット
            serv_sock.setSoTimeout(500);

            while (Running) { //基本的に無限ループ
                //接続をうける
                try {
                    sock = serv_sock.accept();
                } catch (java.net.SocketTimeoutException e) {
                    continue;
                }
                try {
                    //ローカルでなければ蹴る
                    InetAddress remote = sock.getInetAddress();
                    if (!(remote.equals(local) || remote.equals(local_2))) {
                        sock.close();
                        continue;
                    }
                    //セマフォをひとつ減らす
                    System.out.println("2semA"+Sem.availablePermits());
                    Sem.acquire();
                    System.out.println("2semG"+Sem.availablePermits());
                    //接続要求の承諾/スレッドの開始
                    Thread ct = new ClientThread(sock, ReqQueue, Sem, LoginInfo);
                    ct.start();
                } catch (InterruptedException ex2) {
                    ex2.printStackTrace();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
            ConnectedLock.lock();
            Disconneted = true;
            DisconnectedCond.signalAll();
            ConnectedLock.unlock();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
                if (serv_sock != null) {
                    serv_sock.close();
                }
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    private boolean Running = true;
    private boolean Disconneted = false;
    private final Lock ConnectedLock = new ReentrantLock();
    private final Condition DisconnectedCond = ConnectedLock.newCondition();
    public void disConnect() {
        Running = false;
    }

    public void waitDisconnection() {
        if(Disconneted)return;
        ConnectedLock.lock();
        try {
            DisconnectedCond.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            ConnectedLock.unlock();
        }
    }
}
