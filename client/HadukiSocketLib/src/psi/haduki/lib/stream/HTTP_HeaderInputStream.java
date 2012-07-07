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
public class HTTP_HeaderInputStream extends InputStream {
    InputStream is;
    public HTTP_HeaderInputStream(InputStream in) {
        is = in;
    }

    private boolean eof = false;
    private int data = 0;
    public int read() throws IOException {
        if (eof) {
            return -1;
        }
        int b = is.read();
        data = data << 8 | b;
        eof = (b == -1 || data == 0x0d0a0d0a || (data & 0xffff) == 0x0a0a);
        return b;
    }
}
