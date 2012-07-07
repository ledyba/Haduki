package hadukiclient;

import hadukiclient.serv_connection.ServerThread;
import hadukiclient.client.Client;
import hadukiclient.serv_connection.RequestQueue;
import hadukiclient.serv_connection.LoginInfo;
import hadukiclient.serv_connection.HTTP_Proxy;
import hadukiclient.client.ClientThread;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.*;

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
public class Manager extends Thread {
    private static final String USER_INFO_DATA_FILENAME = "user.dat";
    private static final int PASS_LENGTH = 32;
    private HadukiGUIsupport Sup;
    private static boolean hasReadUserInfo = false;
    private static int UserID;
    private static byte[] Password = new byte[PASS_LENGTH];
    private String Server;
    private int Port;
    private HTTP_Proxy Proxy;
    private ServerThread ServTh;
    private Client ClientTh;
    private RequestQueue ReqQue;
    private boolean SuccessInitializing = true;
    public Manager(HadukiGUIsupport sup,
                   String server, int port) {
        Sup = sup;
        Server = server;
        if(sup == null || server == null){
            SuccessInitializing = false;
            return;
        }
        Port = port;
        if(port < 0 || port > 0xffff){
            SuccessInitializing = false;
            return;
        }
        Proxy = null;
        if(hasReadUserInfo)return;
            try {
                DataInputStream dis = new DataInputStream(
                        new FileInputStream(USER_INFO_DATA_FILENAME));
                UserID = dis.readInt();
                int length = dis.read(Password,0,PASS_LENGTH);
                dis.close();
                if(length < PASS_LENGTH){
                    SuccessInitializing = false;
                    return;
                }
                hasReadUserInfo = true;
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                SuccessInitializing = false;
                return;
            } catch (IOException ex) {
                ex.printStackTrace();
                SuccessInitializing = false;
                return;
            }
    }

    public Manager(HadukiGUIsupport sup, String server, int port, String proxy,
                   int proxy_port, String proxy_user, String proxy_pass) {
        this(sup, server, port);
        if(!SuccessInitializing){
            return;
        }
        Proxy = new HTTP_Proxy(proxy, proxy_port, proxy_user, proxy_pass);
        if(Proxy.isBad()){
            SuccessInitializing = false;
            return;
        }
    }

    public void run() {
        if(!SuccessInitializing){
            Sup.setState("Invalid server info.");
            return;
        }
        Sup.setConnected(Sup.CODE_CONNECTING);
        Sup.setState("connecting...");
        ReqQue = new RequestQueue();
        LoginInfo info = new LoginInfo(UserID, Password);
        //サーバ接続
        ServTh = new ServerThread(Server, Port, Proxy, info, ReqQue);
        ServTh.start();
        //接続が確定するまで待機
        int code = ServTh.isConnectedWithCond();
        if (code != ServerThread.CODE_CONNECTED) {
            Sup.setState("connection error occured. code:" +
                         Integer.toString(code));
            Sup.setConnected(Sup.CODE_NOT_CONNECTED);
            return;
        }
        Sup.setState("connected.");
        Sup.setConnected(Sup.CODE_CONNECTED);
        ClientTh = new Client(info, ReqQue);
        ClientTh.start();
        while (Running) {
            if (ReqQue.change_wait(false)) {
                Sup.setState("Queue:" + Integer.toString(ReqQue.size()));
            }
        }
        ServTh.disConnect();
        ClientTh.disConnect();
        ReqQue.change_signal(false);
        ServTh.waitDisconnection();
        ClientTh.waitDisconnection();
        Sup.setState("disconnected.");
        Sup.setConnected(Sup.CODE_NOT_CONNECTED);
    }

    private boolean Running = true;
    public void stopRunning() {
        Sup.setState("disconnecting...");
        Sup.setConnected(Sup.CODE_CONNECTING);
        Running = false;
        ReqQue.change_signal(false);
    }
}
