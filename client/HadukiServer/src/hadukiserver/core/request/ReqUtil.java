package hadukiserver.core.request;

import java.io.*;

/**
 * <p>タイトル: 「はづき」サーバ</p>
 *
 * <p>説明: 「はづき」のサーバです。</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class ReqUtil {
    public static void writeHeader(OutputStream os, boolean isChunked,
                                   boolean ishtml, int length) throws
            IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeChars("HTTP/1.1 200 OK\r\n");
        dos.writeChars("Server: HadukiServer(written in Java)\r\n");
        if (isChunked) {
            dos.writeChars("Transfer-Encoding: chunked\r\n");
        } else {
            dos.writeChars("Content-Length: " + Integer.toString(length) +
                           "\r\n");
        }
        if (ishtml) {
            dos.writeChars("Content-Type: text/html\r\n");
            dos.writeChars("Connection: close\r\n");
        } else {
            dos.writeChars("Content-Type: img/x-png\r\n");
            dos.writeChars("Connection: Keep-Alive\r\n");
        }
        dos.writeChars("\r\n");
        dos.flush();
    }

    public static void writeChunkedHeader(OutputStream os, int length) throws
            IOException {
        String str = Integer.toHexString(length) + "\r\n";
        os.write(str.getBytes());
    }
}
