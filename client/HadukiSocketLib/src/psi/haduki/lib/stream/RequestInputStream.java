package psi.haduki.lib.stream;

import java.io.*;
import org.apache.commons.httpclient.HttpMethod;

/**
 * <p>�^�C�g��: �u�͂Â��v�ėp�\�P�b�g���C�u����</p>
 *
 * <p>����: Socket�N���X�̃T�u�N���X�Ƃ��Ď��������ėp���C�u����</p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
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
     * @todo ���� java.io.InputStream ���\�b�h������
     */
    public int read() throws IOException {
        return In.read();
    }
    public void close() throws IOException {
        In.close();
        Method.releaseConnection();
    }
}
