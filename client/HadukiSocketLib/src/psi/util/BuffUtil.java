package psi.util;

import java.io.InputStream;
import java.io.IOException;

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
public class BuffUtil {
    public static int readStream(InputStream is, byte[] buff, int off, int len) throws
            IOException {
        if(off+len > buff.length){
            throw new IOException("off+len > buff.length。範囲がおかしい。");
        }
        int total_size = 0;
        int size = 0;
        while (total_size < len &&
               (size = is.read(buff, off+total_size, len - total_size)) > 0) {
            total_size += size;
        }
        return total_size;
    }
}
