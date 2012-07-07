package psi.haduki.lib;

import psi.security.rsa.RSACipher;
import java.io.IOException;
import psi.haduki.lib.util.IntValue;
import psi.security.rc4.RC4Cipher;
import org.apache.commons.httpclient.HttpClient;

/**
 * <p>タイトル: 「はづき」汎用ソケットライブラリ</p>
 *
 * <p>説明: 「はづき」ソケットの生成はこれを使ってもらいます。</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class HadukiManager {
    private static HadukiManager DefManager;
    private String URL;
    private HttpClient Client = new HttpClient();
    private RSACipher RSACipher;
    private RC4Cipher RC4Cipher;
    private int MaxChannel;
    private int UserID;
    private byte[] Password;
    private ChannelSet LoginChannel;
    private byte[] LogoffData;
    private ChannelSet ChannelSet[];
    static {
        try {
            DefManager = new HadukiManager();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public HadukiManager() throws IOException {
    }

    public HadukiManager(String url, int user_id, byte[] password, int security) throws
            IOException {
        initialize(url, user_id, password, security);
    }

    private void initialize(String url, int user_id, byte[] password,
                            int security) throws IOException {
        URL = url;
        UserID = user_id;
        Password = password;
        LoginChannel = new ChannelSet(url, Client, user_id, password);
        RSACipher = new RSACipher(security, 32);
        IntValue max_channel = new IntValue(0);
        byte[] key = LoginChannel.sendLoginRequest(RSACipher, max_channel);
        if(key == null){
            throw new IOException("ログインに失敗しました。");
        }
        MaxChannel = max_channel.getValue();
        RC4Cipher = new RC4Cipher(key);
        LogoffData = RC4Cipher.encData(HadukiCode.LOGOFF_SPELL.clone()
                                       , 0, HadukiCode.LOGOFF_SPELL.length);
        initializeChannels();
    }

    private void initializeChannels() {
        ChannelSet = new ChannelSet[MaxChannel];
        for (int i = 0; i < ChannelSet.length; i++) {
            byte[] key = new byte[256];
            RC4Cipher.encData(key, 0, key.length);
            ChannelSet[i] = new ChannelSet(URL, Client, UserID, i, Password,
                                           key);
        }
    }

    public boolean logout() throws IOException {
        return LoginChannel.sendLogoffRequest(LogoffData);
    }

    public HttpClient getHttpClient() {
        return Client;
    }

    public static boolean setDefaultManager(HadukiManager manager) {
        DefManager = manager;
        return true;
    }

}
