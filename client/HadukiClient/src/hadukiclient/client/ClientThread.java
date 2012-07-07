package hadukiclient.client;

import java.net.Socket;
import hadukiclient.serv_connection.Request;
import hadukiclient.serv_connection.RequestQueue;
import java.util.concurrent.Semaphore;
import java.io.OutputStream;
import java.io.*;
import hadukiclient.serv_connection.ServerCommunicator;
import hadukiclient.serv_connection.LoginInfo;
import hadukiclient.HTTP_SocketStreamReader;

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
public class ClientThread extends Thread {
    private Socket ClientSocket;
    private RequestQueue ReqQueue;
    private Semaphore Sem;
    private LoginInfo LoginInfo;
    //データ専用　どちらもビッグエンディアンなので楽。
    private DataInputStream DataIn;
    private DataOutputStream DataOut;
    //テキスト専用
    private BufferedReader TextIn;
    private BufferedWriter TextOut;
    public ClientThread(Socket sock, RequestQueue queue, Semaphore sem,
                        LoginInfo info) {
        ClientSocket = sock;
        ReqQueue = queue;
        LoginInfo = info;
        Sem = sem;
        //バッファの設定
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
        }
    }


    private final String CONTNT_LENGTH_HEADER = "Content-Length: ";
    private final char[] CONTNT_LENGTH_HEADER_C = CONTNT_LENGTH_HEADER.
                                                  toLowerCase().toCharArray();

    private final String PROXY_CONNECTION_HEADER = "Proxy-Connection:";
    private final char[] PROXY_CONNECTION_HEADER_C = PROXY_CONNECTION_HEADER.
            toLowerCase().toCharArray();

    private final String CONNECTION_HEADER = "Connection: close";
    private final String KEEP_ALIVE_HEADER = "Keep-Alive:";
    private final char[] KEEP_ALIVE_HEADER_C = KEEP_ALIVE_HEADER.toLowerCase().
                                               toCharArray();

    private final String CHACHE_HEADER = "Cache";
    private final char[] CHACHE_HEADER_C = CHACHE_HEADER.toLowerCase().
                                           toCharArray();

    private final String HTTP_URL_START = "http://";
    byte[] Buffer = new byte[Request.BUFF_SIZE];
    public void run() {
        try {
            //リクエストの解析
            ByteArrayOutputStream baos = null;
            short host_port = 80;
            String host = null;
            byte[] req_data = null;
            baos = new ByteArrayOutputStream(1024);
            String str;
            char[] str_c;
            int line = 0;
            int content_length = -1;
            boolean connection_changed = false;
            do {
                str = TextIn.readLine();
                if (str == null) {
                    break;
                }
                str_c = str.toLowerCase().toCharArray();
                if (line == 0) {
                    int start = str.indexOf(HTTP_URL_START);
                    int host_start = start + HTTP_URL_START.length();
                    int end = str.indexOf("/", host_start);
                    int pt_start;
                    //ホストとデータの処理
                    host = str.substring(host_start, end);
                    if ((pt_start = host.indexOf(":")) >= 0) {
                        String port_str = host.substring(pt_start + 1);
                        host_port = Short.parseShort(port_str);
                        host = host.substring(0, pt_start);
                    }
                    str = str.substring(0, start) + str.substring(end);
                } else if (content_length < 0 &&
                           Request.strcmp_start(str_c, CONTNT_LENGTH_HEADER_C)) {
                    content_length = Integer.valueOf(str.substring(
                            CONTNT_LENGTH_HEADER.length()));
                } else if (!connection_changed && Request.strcmp_start(str_c,
                                                PROXY_CONNECTION_HEADER_C)) {
                    str = CONNECTION_HEADER;
                    connection_changed = true;
                } else if (
                        Request.strcmp_start(str_c, KEEP_ALIVE_HEADER_C) ||
                        Request.strcmp_start(str_c, CHACHE_HEADER_C)) {
                    continue;
                }
                baos.write(str.getBytes());
                baos.write("\r\n".getBytes());
                line++;
            } while (!(str_c.length == 0));
            //データの受信
            int total_size = 0;
            int size;
            while (total_size < content_length &&
                   (size = DataIn.read(Buffer, 0, Request.BUFF_SIZE)) > 0) {
                baos.write(Buffer, 0, size);
                total_size += size;
            }
            baos.flush();
            req_data = baos.toByteArray();
            baos.close();
            //要求の追加
            Request req = new Request(LoginInfo,
                                      ServerCommunicator.ACTION_REQUEST,
                                      host_port, host, req_data);
            ReqQueue.offer(req);
            //結果を返す
            if (req.getConnected() && req.getReceived()) {
                if (req.getResultCode() != ServerCommunicator.ACTION_RESULT) {
                    TextOut.write("Kicked.");
                    TextOut.flush();
                } else {
                    InputStream res_dis = req.getRecvInputStream();
                    try {
                        byte[] data = new byte[Request.BUFF_SIZE];
                        int in;
                        while ((in = res_dis.read(data, 0,
                                                  Request.BUFF_SIZE)) > 0) {
                            DataOut.write(data, 0, in);
                        }
                        DataOut.flush();
                    } finally {
                        res_dis.close();
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try { //ソケットを閉じる
                TextIn.close();
                DataIn.close();
                DataOut.close();
                TextOut.close();
                ClientSocket.close();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
            //セマフォを返す
            Sem.release();
        }
    }
}
