package hadukiclient.serv_connection;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.util.concurrent.locks.Lock;

/**
 * <p>タイトル: 「葉月」</p>
 *
 * <p>説明: クライアントから葉月への、リクエストを表します。</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class Request {
    public static final int BUFF_SIZE = 4096;
    private final Lock Lock = new ReentrantLock();
    private boolean Encrypted = false;
    private final Condition EncryptedCond = Lock.newCondition();
    private LoginInfo LoginInfo;
    //送信
    private byte[] Data;
    //ストリーム
    private RequestOutputStream ROS = new RequestOutputStream();
    private RequestInputStream RIS = new RequestInputStream();
    //リザルト
    private int ResultCode = -1;
    //コンストラクタ
    public Request(LoginInfo info, int action_code, short host_port,
                   String host, byte[] data) {
        init(info, action_code, host_port, host, data);
    }

    public Request(LoginInfo info, int action_code) {
        if (action_code != ServerCommunicator.ACTION_CONNECT) {
            //切断
            init(info, action_code, (short) 0, null, null);
        } else { //接続
            init(info, action_code, (short) 0, null, info.getPublicKey());
        }
    }

    //初期化
    private void init(LoginInfo info, int action_code, short host_port,
                      String host, byte[] data) {
        LoginInfo = info;
        //受信用バッファの準備
        try {
            RIS.connect(ROS);
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        //ストリームの準備
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFF_SIZE);
        DataOutputStream dos = new DataOutputStream(baos);
        //データの作成
        try {
            dos.writeInt(LoginInfo.getUserID());
            dos.write(LoginInfo.getPassword());
            dos.writeInt(LoginInfo.getSessionID());
            dos.writeInt(action_code);
            dos.writeInt(host == null ? 0 : host.length());
            if (host != null) {
                dos.write(host.getBytes());
                dos.writeShort(host_port);
                if (data == null) {
                    throw new IOException("ホストはあるのにデータが無い");
                }
            }
            dos.writeInt(data == null ? 0 : data.length);
            if (data != null) {
                dos.write(data, 0, data.length);
            }
            dos.flush();
            baos.flush();
            Data = baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //ロック
        Lock.lock();
        /*テスト*/
        for (int i = 0; i < Data.length; i++) {
            //Data[i] ^= 45;
        }
        /*テスト*/
        //ロック解除
        try {
            Encrypted = true;
            EncryptedCond.signalAll();
        } finally {
            Lock.unlock();
        }
    }

    //送信データ
    public byte[] getSendingData() {
        if (!Encrypted) {
            Lock.lock();
            try {
                EncryptedCond.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                Lock.unlock();
            }
        }
        return Data;
    }

    //受信データの受け取りはストリームで行う
    public OutputStream getReceivedStream() {
        return ROS;
    }

    //リザルト受信
    public int getResultCode() {
        if (!getReceived()) {
            return -1;
        }
        return ResultCode;
    }

    //復号化したデータを受け取る
    public InputStream getResultStream() {
        if (!getReceived()) {
            return null;
        }
        return RIS;
    }

    private final Lock ReceivedLock = new ReentrantLock();
    private final Condition ReceivedCond = ReceivedLock.newCondition();
    private boolean ReceivedDecided = false;
    private boolean Received = false;

    public synchronized void signalReceived(boolean flag) {
        ReceivedLock.lock();
        if(flag == true){
            try {
                DataInputStream dis = new DataInputStream(RIS);
                ResultCode = dis.readInt();
                Received = true;
            } catch (IOException ex) {
                ex.printStackTrace();
                Received = false;
            }
        }else{
            Received = false;
        }
        ReceivedDecided = true;
        ReceivedCond.signalAll();
        ReceivedLock.unlock();
    }

    public boolean getReceived() {
        ReceivedLock.lock();
        if (ReceivedDecided) {
            ReceivedLock.unlock();
            return Received;
        }
        try {
            ReceivedCond.await();
        } catch (InterruptedException ex1) {
            ex1.printStackTrace();
        } finally {
            ReceivedLock.unlock();
        }
        return Received;
    }

    private final Lock ConnectedLock = new ReentrantLock();
    private final Condition ConnectedCond = ConnectedLock.newCondition();
    private boolean Connected = false;
    private boolean ConnectedDecided = false;
    public synchronized void setConnected(boolean flag) {
        ConnectedLock.lock();
        Connected = flag;
        ConnectedDecided = true;
        ConnectedCond.signalAll();
        ConnectedLock.unlock();
    }

    public boolean getConnected() {
        if (ConnectedDecided) {
            return Connected;
        }
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

    public static boolean strcmp_start(final String a, final char[] b) {
        if (a.length() < b.length) {
            return false;
        }
        char[] a2 = a.toLowerCase().toCharArray();
        int min = b.length;
        for (int i = 0; i < min; i++) {
            if (a2[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean strcmp_end(final String a, final char[] b) {
        if (a.length() < b.length) {
            return false;
        }
        char[] a2 = a.toLowerCase().toCharArray();
        int a_l = a.length();
        int b_l = b.length;
        for (int i = 1; i <= b_l; i++) {
            if (a2[a_l - i] != b[b_l - i]) {
                return false;
            }
        }
        return true;
    }
}
