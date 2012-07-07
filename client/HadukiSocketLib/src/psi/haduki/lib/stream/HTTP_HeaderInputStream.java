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
