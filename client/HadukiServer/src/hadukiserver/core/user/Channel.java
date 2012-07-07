package hadukiserver.core.user;

import java.net.Socket;
import psi.security.rc4.RC4Cipher;

/**
 * <p>タイトル: 「はづき」サーバ</p>
 *
 * <p>説明: 「はづき」のサーバです。</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class Channel {
    private Socket Sock;
    private RC4Cipher Cipher;
    private final int UserID;
    private final int ChannelNo;
    public Channel(int user_id,int channel_no) {
        UserID = user_id;
        ChannelNo = channel_no;
    }
    public void initialize(byte[] key){
        Cipher = new RC4Cipher(key);
    }
    public int getUserID(){
        return UserID;
    }
    public int getChannelNo(){
        return ChannelNo;
    }
}
