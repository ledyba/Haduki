package hadukiclient.serv_connection;

import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.BufferedWriter;
import java.net.Socket;
import java.io.*;
import hadukiclient.HTTP_SocketStreamReader;
import hadukiclient.crypt.Crypt;

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

    private String Server;
    private int Port;
    private String ServerStrForProxy;
    private LoginInfo LoginInfo;
    //ソケット
    private HTTP_Proxy Proxy;
    //データ専用　どちらもビッグエンディアンなので楽。
    private DataInputStream DataIn;
    private DataOutputStream DataOut;
    //テキスト専用
    private BufferedReader TextIn;
    private BufferedWriter TextOut;
    public ServerCommunicator(String server, int port, HTTP_Proxy proxy,
                              LoginInfo info) {
        Server = server;
        Proxy = proxy;
        LoginInfo = info;
        Port = port;
        ServerStrForProxy = Server + ":" + Integer.toString(port);
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
            LoginInfo.restoreCrypt();
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
            LoginInfo.restoreCrypt();
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
            LoginInfo.restoreCrypt();
            return false;
        }
    }

    private boolean initStream(Socket sock) {
        try {
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            DataIn = new DataInputStream(is);
            DataOut = new DataOutputStream(os);
            TextIn = new BufferedReader(new InputStreamReader(
                    new HTTP_SocketStreamReader(is)));
            TextOut = new BufferedWriter(new OutputStreamWriter(os));
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private static final String HEADER_200 = "200 OK";
    private static final char[] HEADER_200_C = HEADER_200.toLowerCase().
                                               toCharArray();

    byte[] Buffer = new byte[Request.BUFF_SIZE];
    private boolean sendPost(Request req) {
        LoginInfo.backupCrypt();
        Socket sock = null;
        try {
            boolean is_proxy = !(Proxy == null || Proxy.isBad());
            boolean is_err = false;
            try {
                //ソケット準備
                sock = is_proxy ?
                       Proxy.getSocket() :
                       new Socket(Server, Port);
                byte[] sending_data = req.getSendingData();
                if (!initStream(sock)) {
                    req.setConnected(false);
                    req.signalReceived(false);
                    LoginInfo.restoreCrypt();
                    return false;
                }
                req.setConnected(true);
                //ヘッダ送信
                if (is_proxy) {
                    TextOut.write("POST http://" + ServerStrForProxy +
                                  "/index.cgi HTTP/1.1\r\n");
                    TextOut.write("Proxy-Connection: close\r\n");
                    if (Proxy.isProxyAuth()) {
                        TextOut.write(Proxy.getProxyAuthHeader());
                    }
                    TextOut.write("HOST: " + ServerStrForProxy + "\r\n");
                } else {
                    TextOut.write("POST /index.cgi HTTP/1.1\r\n");
                    TextOut.write("Connection: close\r\n");
                    TextOut.write("HOST: " + Server + "\r\n");
                }
                TextOut.write("User-Agent: Haduki\r\n");
                TextOut.write("Accept: */*\r\n");
                TextOut.write("Content-Type: image/x-png\r\n"); //バイナリを送れる
                TextOut.write("Content-Length: " + sending_data.length + "\r\n"); //バイナリ長さ
                TextOut.write("\r\n"); //ヘッダ終わり
                TextOut.flush();
                //データ送信
                DataOut.write(sending_data);
                DataOut.flush();
                /*リターンを受信*/
                String str = null;
                //リクエスト処理
                int line = 0;
                do {
                    str = TextIn.readLine();
                    if (str == null) {
                        is_err = true;
                        break;
                    }
                    if (line == 0 && !Request.strcmp_end(str, HEADER_200_C)) {
                        //リクエスト失敗
                        is_err = true;
                        break;
                    }
                    line++;
                } while (!str.equals(""));
            } catch (Exception ex) {
                req.setConnected(false);
                is_err = true;
                ex.printStackTrace();
                LoginInfo.restoreCrypt();
                return false;
            } finally {
                if (is_err) {
                    req.signalReceived(false);
                }
            }
            /*受信した結果を書き込む*/
            int size;
            int total_size = 0;
            boolean signal = false;
            OutputStream req_os = req.getRecvOutputStream();
            Crypt Crypt = LoginInfo.getCrypt();
            try {
                while ((size = Crypt.inputData(DataIn, Buffer, 0,
                                               Request.BUFF_SIZE)) > 0) {
                    try {
                        req_os.write(Buffer, 0, size);
                        total_size += size;
                        if (!signal && total_size >= 4) {
                            req.signalReceived(true);
                            signal = true;
                        }
                    } catch (IOException ex) {
                    }
                }
                req_os.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (!signal) {
                    req.signalReceived(false);
                }
                try {
                    if (req_os != null) {
                        req_os.close();
                    }
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        } finally {
            try {
                TextIn.close();
                DataIn.close();
                DataOut.close();
                TextOut.close();
                if (sock != null) {
                    sock.close();
                    sock = null;
                }
            } catch (IOException ex1) {
                ex1.printStackTrace();
                return false;
            }
        }
        //もどる
        return true;
    }
}
