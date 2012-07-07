package hadukiclient.serv_connection;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.*;
import hadukiclient.crypt.Crypt;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;


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
public class ServerCommunicator {
    public static final int ACTION_CONNECT = 0x2F5B8D6E;
    public static final int ACTION_ACCEPT = 0xDBFBD3B9;
    public static final int ACTION_KICKED = 0xF8E9A58E;
    public static final int ACTION_REQUEST = 0x444581CC;
    public static final int ACTION_RESULT = 0xC3FF7E28;
    public static final int ACTION_DISCONNEST = 0x9097FB4B;
    public static final int ACTION_RESET = 0xBE0F74BA;
    private String ServerForHost;
    private LoginInfo LoginInfo;
    //ソケット
    private HttpClient Client;
    //private boolean isProxy = false;
    private HTTP_Proxy Proxy = null;
    //データ専用　どちらもビッグエンディアンなので楽。
    private DataInputStream DataIn;
    public ServerCommunicator(String server, int port, HTTP_Proxy proxy,
                              LoginInfo info) {
        LoginInfo = info;
        ServerForHost = server + ":" + Integer.toString(port);
        Client = new HttpClient();
        if (proxy != null && proxy.isCollect()) {
            Proxy = proxy;
            Proxy.setConfig(Client);
            //isProxy = true;
        }
    }

    public boolean sendConnectionStart() {
        Request req = new Request(LoginInfo, ServerCommunicator.ACTION_CONNECT);
        if (!sendPost(req)) {
            return false;
        }
        int res_code = req.getResultCode();
        if (res_code == this.ACTION_ACCEPT) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sendRequest(Request req) {
        if (!sendPost(req)) {
            return false;
        }
        int res_code = req.getResultCode();
        if (res_code == this.ACTION_RESULT) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sendConnectionEnd() {
        Request req = new Request(LoginInfo,
                                  ServerCommunicator.ACTION_DISCONNEST);
        if (!sendPost(req)) {
            return false;
        }
        int res_code = req.getResultCode();
        if (res_code == this.ACTION_ACCEPT) {
            return true;
        } else {
            return false;
        }
    }

    private boolean initInputStream(PostMethod method) {
        InputStream is = null;
        try {
            is = method.getResponseBodyAsStream();
            DataIn = new DataInputStream(is);
        } catch (IOException ex) {
            try{
                is.close();
                DataIn.close();
            }catch (IOException ex1) {
                ex1.printStackTrace();
            }
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    byte[] Buffer = new byte[Request.BUFF_SIZE];
    private boolean sendPost(Request req) {
        byte[] sending_data = null;
        Crypt Crypt = null;
        int status;
        //メソッドの設定
        PostMethod method = new PostMethod("http://" + ServerForHost +
                                           "/index.cgi");
        method.setRequestHeader("User-Agent", "Haduki");
        method.setRequestHeader("Connection", "close");
        //暗号化の設定
        Crypt = LoginInfo.getCrypt();
        Crypt.startCrypt();
        //送信データの設定
        sending_data = req.getSendingData();
        method.setRequestEntity(
                new ByteArrayRequestEntity(sending_data, "image/x-png"));
        try {
            try {
                //メソッド実行
                status = Client.executeMethod(method);
            } catch (IOException ex2) {
                ex2.printStackTrace();
                req.setConnected(false);
                req.signalReceived(false);
                return false;
            }
            //接続は、出来た。
            req.setConnected(true);
            //結果を取得
            if (status != HttpStatus.SC_OK) {
                req.signalReceived(false);
                return false;
            }
            //出力ストリーム初期化
            if (!initInputStream(method)) {
                req.signalReceived(false);
                return false;
            }
            //データを取得する
            int size = 0;
            int total_size = 0;
            boolean signal = false;
            OutputStream req_os = req.getRecvOutputStream();
            try {
                while ((size = Crypt.inputData(DataIn, Buffer, 0,
                                               Request.BUFF_SIZE)) > 0) {
                    total_size += size;
                    if(req.isCanceled()){
                        return false;
                    }
                    try {
                        req_os.write(Buffer, 0, size);
                        if (!signal && total_size >= 4) {
                            req.signalReceived(true);
                            signal = true;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return false;
                    }
                    req_os.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (!signal) {
                    req.signalReceived(false);
                }
                if (total_size > 0) {
                    Crypt.nextStream();
                }
                try {
                    req_os.close();
                    DataIn.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                    return false;
                }
            }
            //もどる
            return true;
        } finally {
            method.releaseConnection();
        }
    }
}
