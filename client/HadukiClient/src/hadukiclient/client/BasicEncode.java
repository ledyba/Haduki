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
    private static final char encode_table[] = {
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
    private static StringBuffer sb = new StringBuffer();
    public static String encode(String str) {
        sb.delete(0, sb.length());
        final byte[] str_b = str.getBytes();
        final int div = 3 - (str_b.length % 3);
        final byte[] data = new byte[str_b.length + div];

        for (int i = 0; i < str_b.length; i++) {
            data[i] = str_b[i];
        }
        int tmp;
        for (int i = 0; i < str_b.length; i += 3) {
            tmp = (data[i] << 16) | (data[i + 1] << 8) | data[i + 2];
            sb.append(encode_table[(tmp >> 18) & 0x3f]);
            sb.append(encode_table[(tmp >> 12) & 0x3f]);
            sb.append(encode_table[(tmp >> 6) & 0x3f]);
            sb.append(encode_table[(tmp >> 0) & 0x3f]);
        }
        if (div < 3) {
            sb.delete(sb.length() - div, sb.length());
            for (int j = 0; j < div; j++) {
                sb.append('=');
            }
        }
        return sb.toString();
    }
}
