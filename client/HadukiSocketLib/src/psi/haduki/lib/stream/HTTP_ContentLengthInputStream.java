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
public class HTTP_ContentLengthInputStream extends InputStream {
    private InputStream In;
    private int ContentLength;
    public HTTP_ContentLengthInputStream(InputStream is,int length) {
        In = is;
        ContentLength = length;
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
        if(ContentLength <= 0){
            return -1;
        }else{
            ContentLength--;
            return In.read();
        }
    }
}
