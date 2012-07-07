package psi.haduki.lib.stream;

import java.io.*;

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
     * @todo ���� java.io.InputStream ���\�b�h������
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
        /*���s�J�n�܂œǂݍ���*/
        byte[] array = new byte[1];
        while (!(b == 0x0d || b == 0x0a || b == -1)){
            array[0] = (byte)b;
            sb.append(new String(array));
            b = In.read();
        }
        /*�`�����N�̒l���m��*/
        try {
            Chunked = Integer.parseInt(sb.substring(0), 16);
        } catch (NumberFormatException ex) {
            Chunked = 0;
        }
        if(Chunked <= 0){
            end = true;
            return -1;
        }
        /*���s�I���܂œǂݍ���*/
        while(b != 0x0a){
            b = In.read();
        }
        /*���ʂɓǂݍ���*/
        Chunked--;
        return In.read();
    }
}
