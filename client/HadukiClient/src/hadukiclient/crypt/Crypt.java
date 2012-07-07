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
public class Crypt {

    private RC4Stream MasterStream = new RC4Stream(true);
    private RC4Stream Backup;
    private RC4Stream Stream = new RC4Stream(false);
    byte[] next_buff = new byte[RC4Stream.RC4_KEY_LENGTH];

    public Crypt(){
        MasterStream.getArray(this.next_buff);
        Backup = new RC4Stream(this.next_buff);
    }

    private byte encrypt_char(byte n) {
        byte k = Stream.getNext();
        n = (byte)((n + k) & 0xff);
        return (byte)(n ^ k);
    }

    private byte decrypt_char(byte n) {
        byte k = Stream.getNext();
        n ^= k;
        return (byte)((n - k) & 0xff);
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

    public void startCrypt() {
        Backup.copy(Stream);
    }
    public void nextStream() {
        MasterStream.getArray(next_buff);
        Backup.init(this.next_buff);
    }
}
