package psi.haduki.lib.stream;

import java.io.*;

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
public class HTTP_ChunkedInputStream extends InputStream {
    private boolean end = false;
    private int Chunked = 0;
    private InputStream In;
    public HTTP_ChunkedInputStream(InputStream is) {
        In = is;
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
        if (end) {
            return -1;
        }
        if (Chunked > 0) {
            Chunked--;
            return In.read();
        }
        int b = 0;
        StringBuffer sb = new StringBuffer();
        /*改行開始まで読み込み*/
        byte[] array = new byte[1];
        while (!(b == 0x0d || b == 0x0a || b == -1)){
            array[0] = (byte)b;
            sb.append(new String(array));
            b = In.read();
        }
        /*チャンクの値を確定*/
        try {
            Chunked = Integer.parseInt(sb.substring(0), 16);
        } catch (NumberFormatException ex) {
            Chunked = 0;
        }
        if(Chunked <= 0){
            end = true;
            return -1;
        }
        /*改行終わるまで読み込み*/
        while(b != 0x0a){
            b = In.read();
        }
        /*普通に読み込み*/
        Chunked--;
        return In.read();
    }
}
