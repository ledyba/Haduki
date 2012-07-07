package psi.haduki.lib;

import psi.security.rc4.RC4Cipher;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;
import java.io.InputStream;
import org.apache.commons.httpclient.HttpStatus;
import java.io.*;
import psi.haduki.lib.stream.RequestInputStream;
import psi.security.rsa.*;
import psi.util.*;
import psi.haduki.lib.util.IntValue;

/**
 * <p>タイトル: 「はづき」汎用ソケットライブラリ</p>
 *
 * <p>説明: Socketクラスのサブクラスとして実装した汎用ライブラリ</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class ChannelSet {
    private final byte[] Password;
    private final int UserID;
    private final byte[] UserID_Array;
    private final int ChannelNo;
    private final byte[] ChannelNo_Array;
    private final RC4Cipher Cipher;
    private final HttpClient Client;
    private final String URL;
    private PipedInputStream PipedIn;
    private PipedOutputStream PipedOut;
    protected ChannelSet(String url, HttpClient client, int user_id,
                         int channel_no, byte[] pass, byte[] key) {
        Password = pass;
        URL = url;
        Client = client;
        UserID = user_id;
        byte[] user_id_array = {
                               (byte) ((user_id & 0xff000000) >> 24),
                               (byte) ((user_id & 0x00ff0000) >> 16),
                               (byte) ((user_id & 0x0000ff00) >> 8),
                               (byte) ((user_id & 0x000000ff) >> 0),
        };
        UserID_Array = user_id_array;
        ChannelNo = channel_no;
        byte[] channel_no_array = {
                                  (byte) ((channel_no & 0xff000000) >> 24),
                                  (byte) ((channel_no & 0x00ff0000) >> 16),
                                  (byte) ((channel_no & 0x0000ff00) >> 8),
                                  (byte) ((channel_no & 0x000000ff) >> 0),
        };
        ChannelNo_Array = channel_no_array;
        Cipher = new RC4Cipher(key);
    }

    protected ChannelSet(String url, HttpClient client, int user_id,
                         byte[] pass) {
        Password = pass;
        URL = url;
        Client = client;
        UserID = user_id;
        byte[] user_id_array = {
                               (byte) ((user_id & 0xff000000) >> 24),
                               (byte) ((user_id & 0x00ff0000) >> 16),
                               (byte) ((user_id & 0x0000ff00) >> 8),
                               (byte) ((user_id & 0x000000ff) >> 0),
        };
        UserID_Array = user_id_array;
        ChannelNo = 0xFFFFFFFF;
        byte[] channel_no_array = {(byte) 0xff, (byte) 0xff, (byte) 0xff,
                                  (byte) 0xff, };
        ChannelNo_Array = channel_no_array;
        Cipher = null;
    }

    protected byte[] sendLoginRequest(RSACipher cipher, IntValue max_channel) throws
            IOException {
        if (ChannelNo != 0xFFFFFFFF) {
            throw new IOException("ログイン・チャンネルではありません。");
        }
        initializePipe();
        RSAKeyPair pair = cipher.getKeyPair();
        /*ログインであることを示す。*/
        NumUtil.writeInteger(PipedOut, HadukiCode.LOGIN_ACCEPT);
        /*法N*/
        byte[] module = pair.getModuleBytes();
        NumUtil.writeInteger(PipedOut, module.length);
        NumUtil.writeInteger(PipedOut, pair.getModuleBits());
        PipedOut.write(module);
        /*公開鍵*/
        byte[] public_power = pair.getPowerPublicBytes();
        NumUtil.writeInteger(PipedOut, public_power.length);
        NumUtil.writeInteger(PipedOut, pair.getPowerBits());
        PipedOut.write(public_power);
        /*パスワード*/
        byte[] enc_pass = cipher.encData(Password, 0, Password.length);
        NumUtil.writeInteger(PipedOut, enc_pass.length);
        NumUtil.writeInteger(PipedOut, Password.length);
        PipedOut.write(enc_pass);
        /*送信*/
        PipedOut.flush();
        PipedOut.close();
        InputStream is = null;
        try {
            is = sendPost();
            System.out.println("ここまでおｋ");
            /*結果を取得*/
            int code = NumUtil.readInteger(is);
            if (code != HadukiCode.LOGIN_SUCCESS) {
                return null;
            }
            System.out.println("ここまでおｋ2");
            /*チャンネル数*/
            int channel = NumUtil.readInteger(is);
            max_channel.setValue(channel);
            /*データサイズ*/
            int data_size = NumUtil.readInteger(is);
            /*オリジナルサイズ*/
            int org_size = NumUtil.readInteger(is);
            if (org_size > data_size) {
                throw new IOException("オリジナルデータサイズ＞暗号化済みサイズ　はありえません。");
            }
            /*暗号化済み鍵データ読み込み*/
            byte enc_data[] = new byte[data_size];
            int read_size = BuffUtil.readStream(is, enc_data, 0, data_size);
            if (read_size != data_size) {
                throw new IOException("データサイズが足りません。");
            }
            /*復号化*/
            byte[] dec_data = new byte[org_size];
            byte[] tmp = cipher.decData(enc_data, 0, data_size);
            if (tmp.length < org_size) {
                throw new IOException("復号化したRC4鍵の長さがおかしい。");
            }
            System.arraycopy(tmp, 0, dec_data, 0, org_size);
            return dec_data;
        } finally {
            if(is != null){
                is.close();
            }
        }
    }

    protected boolean sendLogoffRequest(byte[] data) throws IOException {
        initializePipe();
        /*ログオフであることを示す。*/
        NumUtil.writeInteger(PipedOut, HadukiCode.LOGOFF_ACCEPT);
        /*データ*/
        NumUtil.writeInteger(PipedOut, data.length);
        PipedOut.write(data);
        InputStream is = null;
        try {
            is = sendPost();
            int code = NumUtil.readInteger(is);
            if (code != HadukiCode.LOGOFF_SUCCESS) {
                return false;
            }
            return true;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private InputStream sendPost() throws IOException {
        PostMethod method = new PostMethod(URL);
        method.setRequestHeader("User-Agent", "Haduki");
        method.setRequestEntity(
                new InputStreamRequestEntity(PipedIn, "image/x-png"));
        int code = Client.executeMethod(method);
        if (code != HttpStatus.SC_OK) {
            throw new IOException("POSTメソッド実行に失敗:コード" + code);
        }
        return new RequestInputStream(method);
    }

    private void initializePipe() throws IOException {
        PipedIn = new PipedInputStream();
        PipedOut = new PipedOutputStream(PipedIn);
        PipedOut.write(this.getUserID_Array());
        PipedOut.write(this.getChannelNo_Array());
    }

    protected int getUserID() {
        return this.UserID;
    }

    protected int getChannelNo() {
        return this.ChannelNo;
    }

    protected byte[] getUserID_Array() {
        return this.UserID_Array;
    }

    protected byte[] getChannelNo_Array() {
        return this.ChannelNo_Array;
    }
}
