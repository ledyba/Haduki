package psi.util;

import java.io.InputStream;
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
public class NumUtil {
    public static byte[] toByteArray(int num) {
        byte[] array = {
                       (byte) ((num & 0xff000000) >> 24),
                       (byte) ((num & 0x00ff0000) >> 16),
                       (byte) ((num & 0x0000ff00) >> 8),
                       (byte) ((num & 0x000000ff) >> 0),
        };
        return array;
    }

    public static int toInteger(byte[] array) {
        if (array.length != 4) {
            return -1;
        }
        int ret;
        ret =
                ((NumUtil.toInt(array[3])) << 0) +
                ((NumUtil.toInt(array[2])) << 8) +
                ((NumUtil.toInt(array[1])) << 16) +
                ((NumUtil.toInt(array[0])) << 24);
        return ret;
    }

    public static int toInt(byte num) {
        if (num < 0) {
            return ((int) num) + 256;
        } else {
            return ((int) num);
        }
    }

    public static int readInteger(InputStream is) {
        byte[] tmp = new byte[4];
        try {
            is.read(tmp, 0, tmp.length);
        } catch (IOException ex) {
        }
        return NumUtil.toInteger(tmp);
    }

    public static void writeInteger(OutputStream os, int num) {
        try {
            os.write(NumUtil.toByteArray(num));
        } catch (IOException ex) {
        }
    }

}
