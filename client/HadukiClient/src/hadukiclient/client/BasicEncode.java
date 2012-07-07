package hadukiclient.client;

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
public class BasicEncode {
    private static final byte encode_table[] = {
                                               'A', 'B', 'C', 'D', 'E', 'F',
                                               'G', 'H', 'I', 'J', 'K', 'L',
                                               'M', 'N', 'O', 'P', 'Q', 'R',
                                               'S', 'T', 'U', 'V', 'W', 'X',
                                               'Y', 'Z', 'a', 'b', 'c', 'd',
                                               'e', 'f', 'g', 'h', 'i', 'j',
                                               'k', 'l', 'm', 'n', 'o', 'p',
                                               'q', 'r', 's', 't', 'u', 'v',
                                               'w', 'x', 'y', 'z', '0', '1',
                                               '2', '3', '4', '5', '6', '7',
                                               '8', '9', '+', '/'
    };
    /**
     * encode
     * @param str String
     * @return String
     */
    public static String encode(String str) {
        final byte[] data = str.getBytes();
        final int length = data.length;
        final int end = length - (length % 3);
        String ret = "";
        for (int i = 0; i < length; i += 3) {
            ret += encode_table[data[i] >> 2];
            ret += encode_table[((data[i] & 3) << 4) + data[i+1] >> 4];
            ret += encode_table[((data[i+1] & 0xf) << 2) + data[i+2]>>6];
            ret += encode_table[(data[i+2] & 0x3f)];
        }
        for (int i = end; i < length; i++) {
            ret += "=";
        }
        return ret;
    }
}
