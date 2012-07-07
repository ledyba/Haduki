package hadukiclient.crypt;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

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
public class Crypt implements Cloneable{
    private static final int RC4_KEY_LENGTH = 256;
    private static final int RC4_KEY[] = {
                                         0x43, 0xcf, 0xf6, 0x31, 0x5f, 0xc9,
                                         0xc6, 0x22, 0x1e, 0xc0, 0xf4, 0xe9,
                                         0x6f, 0x11, 0xce, 0x40, 0xb7, 0x97,
                                         0xb1, 0x75, 0x0b, 0x16, 0x27, 0x01,
                                         0x1c, 0x23, 0x55, 0xe7, 0x0f, 0x98,
                                         0x6a, 0x41, 0xf2, 0xca, 0x0c, 0xcd,
                                         0x47, 0xbe, 0xc4, 0x84, 0xae, 0x9d,
                                         0x64, 0x8c, 0x40, 0xeb, 0xc1, 0x20,
                                         0x0a, 0x7f, 0x26, 0xfc, 0x0b, 0x5d,
                                         0x09, 0x4c, 0x39, 0x35, 0x8d, 0xa8,
                                         0x2a, 0xca, 0xde, 0x42, 0xb4, 0x08,
                                         0x79, 0x91, 0x02, 0x98, 0xc9, 0x8c,
                                         0x0e, 0x15, 0x7c, 0xbd, 0xea, 0xf4,
                                         0x03, 0x69, 0xf5, 0x85, 0xf2, 0xdb,
                                         0x87, 0x33, 0xa4, 0xa3, 0xd2, 0x01,
                                         0x7a, 0xe8, 0x81, 0x71, 0xde, 0x1e,
                                         0xd6, 0xda, 0x96, 0xa6, 0xa8, 0xa3,
                                         0xab, 0x2e, 0xfc, 0xc2, 0xe6, 0x84,
                                         0xde, 0x5b, 0x9c, 0xe3, 0x2a, 0xf9,
                                         0xc2, 0x2e, 0xd5, 0x72, 0x82, 0x02,
                                         0xd3, 0xd1, 0xd4, 0x9f, 0xd9, 0xc0,
                                         0xb1, 0xe7, 0x42, 0xbf, 0xdc, 0x63,
                                         0xfd, 0xc4, 0x80, 0x3b, 0x40, 0x67,
                                         0x42, 0x77, 0xae, 0xdf, 0x55, 0x01,
                                         0xa2, 0x5f, 0x77, 0x2c, 0x95, 0x2d,
                                         0x64, 0x6f, 0xf7, 0x1d, 0xa8, 0xc6,
                                         0x5f, 0x9b, 0xb4, 0xb5, 0xab, 0x7c,
                                         0xcc, 0xbd, 0x72, 0x4d, 0xa8, 0x7e,
                                         0xee, 0x36, 0x8e, 0xb3, 0xdb, 0xa7,
                                         0x22, 0xc5, 0x98, 0x33, 0x75, 0xae,
                                         0x16, 0xee, 0x10, 0x54, 0xe3, 0x2d,
                                         0x55, 0x62, 0x74, 0x69, 0x08, 0x92,
                                         0x58, 0x8a, 0x99, 0xff, 0xc8, 0x8e,
                                         0xb1, 0x48, 0x19, 0x87, 0x9f, 0x2f,
                                         0x86, 0xed, 0xf0, 0xf7, 0x0c, 0x4f,
                                         0xf9, 0xba, 0x08, 0x45, 0xe6, 0x40,
                                         0x6b, 0x83, 0x56, 0x0a, 0x6a, 0xbf,
                                         0x87, 0xc1, 0xda, 0x3a, 0x35, 0x4c,
                                         0xd5, 0x25, 0x40, 0xe7, 0xcf, 0x16,
                                         0xad, 0x91, 0x0f, 0x01, 0xcb, 0x06,
                                         0x43, 0xe6, 0x10, 0x45, 0x1f, 0x6d,
                                         0xda, 0x34, 0x01, 0x5c, 0x3c, 0x12,
                                         0xfd, 0x86, 0x05, 0xcc, };
    int i = 0, j = 0;
    private byte[] key = new byte[RC4_KEY_LENGTH];
    public Crypt() {
        int i, j;
        byte tmp;
        //Sボックス初期化
        for (i = 0; i < RC4_KEY_LENGTH; i++) {
            key[i] = toByte(i);
        }
        //キーを使ってシャッフル
        j = 0;
        for (i = 0; i < RC4_KEY_LENGTH; i++) {
            j = (j + key[i] + toByte(RC4_KEY[i])) & 0xff;
            tmp = key[i];
            key[i] = key[j];
            key[j] = tmp;
        }
    }
    private Crypt(int i,int j,byte[] key) {
        this.i = i;
        this.j = j;
        this.key = key.clone();
    }

    private byte get_crypt_char() {
        int i = this.i;
        int j = this.j;
        byte tmp;
        //インデックスのセット
        i = (i + 1) & 0xff;
        j = (j + key[i]) & 0xff;
        this.i = i;
        this.j = j;
        //入れ替え
        tmp = key[i];
        key[i] = key[j];
        key[j] = tmp;
        return key[(key[i] + key[j]) & 0xff];
    }

    private byte encrypt_char(byte n) {
        byte k = get_crypt_char();
        n = toByte((n + k) & 0xff);
        return toByte(n ^ k);
    }

    private byte decrypt_char(byte n) {
        byte k = get_crypt_char();
        n ^= k;
        return toByte((n - k) & 0xff);
    }

    //Input
    public byte[] inputData(byte[] data, int off, int len) {
        int i;
        for (i = off; i < off + len; i++) {
            data[i] = decrypt_char(data[i]);
        }
        return data;
    }

    public int inputData(InputStream is, byte[] data, int off, int len) throws
            IOException {
        int size = is.read(data, off, len);
        data = inputData(data, off, size);
        return size;
    }

    public int inputData(InputStream is, byte[] data) throws IOException {
        return inputData(is, data, 0, data.length);
    }

    //Output
    public byte[] outputData(byte[] data, int off, int len) {
        int i;
        for (i = off; i < off + len; i++) {
            data[i] = encrypt_char(data[i]);
        }
        return data;
    }

    public void outputData(OutputStream os, byte[] data, int off, int len) throws
            IOException {
        data = outputData(data, off, len);
        os.write(data, off, len);
    }

    public void outputData(OutputStream os, byte[] data) throws IOException {
        outputData(os, data, 0, data.length);
    }

    private static byte toByte(int i) {
        i &= 0xff;
        if (i > 127) {
            i -= 256;
        }
        return (byte) i;
    }
    public Object clone(){
        Crypt c = new Crypt(this.i,this.j,this.key);
        return c;
    }
}
