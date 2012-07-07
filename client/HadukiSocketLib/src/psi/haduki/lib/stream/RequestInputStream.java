package psi.haduki.lib.stream;

import java.io.*;
import org.apache.commons.httpclient.HttpMethod;

/**
 * <p>タイトル: 「はづき」汎用ソケットライブラリ</p>
 *
 * <p>説明: Socketクラスのサブクラスとして実装した汎用ライブラリ</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class RequestInputStream extends InputStream {
    private HttpMethod Method;
    private InputStream In;
    public RequestInputStream(HttpMethod method) throws IOException{
        Method = method;
        In = Method.getResponseBodyAsStream();
    }
    /**
     * Reads the next byte of data from the input stream.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *   stream is reached.
     * @throws IOException if an I/O error occurs.
     * @todo この java.io.InputStream メソッドを実装
     */
    public int read() throws IOException {
        return In.read();
    }
    public void close() throws IOException {
        In.close();
        Method.releaseConnection();
    }
}
