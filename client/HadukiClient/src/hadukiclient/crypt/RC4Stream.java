package hadukiclient.crypt;

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
public class RC4Stream implements Cloneable{
    private int i = 0, j = 0;
    private byte[] key = new byte[RC4_KEY_LENGTH];
    public static final int RC4_KEY_LENGTH = 256;
    private static final byte RC4_KEY[] = {
                                         67, -49, -10, 49, 95, -55, -58, 34, 30,
                                         -64, -12, -23, 111, 17, -50, 64, -73,
                                         -105, -79, 117, 11, 22, 39, 1, 28, 35,
                                         85, -25, 15, -104, 106, 65, -14, -54,
                                         12, -51, 71, -66, -60, -124, -82, -99,
                                         100, -116, 64, -21, -63, 32, 10, 127,
                                         38, -4, 11, 93, 9, 76, 57, 53, -115,
                                         -88, 42, -54, -34, 66, -76, 8, 121,
                                         -111, 2, -104, -55, -116, 14, 21, 124,
                                         -67, -22, -12, 3, 105, -11, -123, -14,
                                         -37, -121, 51, -92, -93, -46, 1, 122,
                                         -24, -127, 113, -34, 30, -42, -38,
                                         -106, -90, -88, -93, -85, 46, -4, -62,
                                         -26, -124, -34, 91, -100, -29, 42, -7,
                                         -62, 46, -43, 114, -126, 2, -45, -47,
                                         -44, -97, -39, -64, -79, -25, 66, -65,
                                         -36, 99, -3, -60, -128, 59, 64, 103,
                                         66, 119, -82, -33, 85, 1, -94, 95, 119,
                                         44, -107, 45, 100, 111, -9, 29, -88,
                                         -58, 95, -101, -76, -75, -85, 124, -52,
                                         -67, 114, 77, -88, 126, -18, 54, -114,
                                         -77, -37, -89, 34, -59, -104, 51, 117,
                                         -82, 22, -18, 16, 84, -29, 45, 85, 98,
                                         116, 105, 8, -110, 88, -118, -103, -1,
                                         -56, -114, -79, 72, 25, -121, -97, 47,
                                         -122, -19, -16, -9, 12, 79, -7, -70, 8,
                                         69, -26, 64, 107, -125, 86, 10, 106,
                                         -65, -121, -63, -38, 58, 53, 76, -43,
                                         37, 64, -25, -49, 22, -83, -111, 15, 1,
                                         -53, 6, 67, -26, 16, 69, 31, 109, -38,
                                         52, 1, 92, 60, 18, -3, -122, 5, -52,
    };
    public RC4Stream(boolean def) {
        if(def){
            init(RC4_KEY);
        }
    }

    public RC4Stream(final byte[] pass) {
        init(pass);
    }
    public void init(final byte[] pass){
        int i, j;
        byte tmp;
        //Sボックス初期化
        for (i = 0; i < RC4_KEY_LENGTH; i++) {
            key[i] = (byte)i;
        }
        //キーを使ってシャッフル
        j = 0;
        for (i = 0; i < RC4_KEY_LENGTH; i++) {
            j = (j + key[i] + pass[i]) & 0xff;
            tmp = key[i];
            key[i] = key[j];
            key[j] = tmp;
        }
    }

    public byte getNext() {
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
    public void getArray(byte[] b){
        for(int i=0;i<b.length;i++){
            b[i] = getNext();
        }
    }
    public void copy(RC4Stream str){
        str.i = this.i;
        str.j = this.j;
        for(int i=0;i<RC4_KEY_LENGTH;i++){
            str.key[i] = this.key[i];
        }
    }
}
