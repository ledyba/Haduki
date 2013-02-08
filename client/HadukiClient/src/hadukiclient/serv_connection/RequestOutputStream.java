package hadukiclient.serv_connection;

import java.io.*;

/**
 * <p>�^�C�g��: �u�t���v</p>
 *
 * <p>����: </p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class RequestOutputStream extends PipedOutputStream {
    public RequestOutputStream() {
    }
    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular, an
     *   <code>IOException</code> may be thrown if the output stream has
     *   been closed.
     * @todo ���� java.io.OutputStream ���\�b�h������
     */
    public void write(int b) throws IOException {
        //super.write(b ^ 45);
    }
    public void write(byte[] data) throws IOException {
        for(int i=0;i<data.length;i++){
            //data[i] ^= 45;
        }
        super.write(data);
    }
    public void write(byte[] data,int off,int len) throws IOException {
        for(int i=off;i<off+len;i++){
            //data[i] ^= 45;
        }
        super.write(data,off,len);
    }
}
