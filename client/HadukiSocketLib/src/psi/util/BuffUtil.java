package psi.util;

import java.io.InputStream;
import java.io.IOException;

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
public class BuffUtil {
    public static int readStream(InputStream is, byte[] buff, int off, int len) throws
            IOException {
        if(off+len > buff.length){
            throw new IOException("off+len > buff.length�B�͈͂����������B");
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
