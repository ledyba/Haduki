package psi.http.ext;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;
import java.io.*;
import psi.util.NumUtil;
import java.util.Random;

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
public class OriginalNTLMScheme implements AuthScheme {
    private enum Stage {
        DEFAULT, TYPE1_SENT, TYPE2_RECV, TYPE3_SENT
    }


    private Stage stage;
    private Random rnd;
    private static final String AUTH_CHARSET = "UnicodeLittleUnmarked";
    private static final byte[] NTLM_HEADER =
            EncodingUtil.getAsciiBytes("NTLMSSP\0");
    private static final byte[] TYPE1_HEADER = {0x01, 0x00, 0x00, 0x00};
    private static final byte[] TYPE3_HEADER = {0x03, 0x00, 0x00, 0x00};
    public OriginalNTLMScheme() {
        super();
        stage = Stage.DEFAULT;
        rnd = new Random();
    }

    /**
     * Produces an authorization string for the given set of {@link
     * Credentials}.
     *
     * @param credentials The set of credentials to be used for athentication
     * @param method The method being authenticated
     * @throws AuthenticationException if authorization string cannot be
     *   generated due to an authentication failure
     * @return the authorization string
     * @todo この org.apache.commons.httpclient.auth.AuthScheme メソッドを実装
     */
    public String authenticate(Credentials credentials, HttpMethod method) throws
            AuthenticationException {
        if (credentials == null) {
            throw new IllegalArgumentException("Cred is null.");
        }
        String resp = "";
        NTCredentials cre = null;
        try {
            cre = (NTCredentials) credentials;
        } catch (ClassCastException ex) {
            throw new AuthenticationException("This is not NTLM cred.");
        }
        switch (stage) {
        case DEFAULT: //Type1を送る
            resp = EncodingUtil.getAsciiString
                   (Base64.encodeBase64(getType1Msg(
                           cre)));
            stage = Stage.TYPE1_SENT;
            break;
        case TYPE1_SENT:
            throw new AuthenticationException("Has not received Type2 msg.");
        case TYPE2_RECV: //Type3
            resp = EncodingUtil.getAsciiString
                   (Base64.encodeBase64(getType3Msg(cre)));
            stage = Stage.TYPE3_SENT;
            break;
        case TYPE3_SENT:
        default:
            throw new AuthenticationException("Authentication finished.");
        }
        return "NTLM " + resp;
    }

    private byte[] getType1Msg(NTCredentials cre) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        //これらは必ずASCII、しかも大文字
        byte[] domain = EncodingUtil.getBytes
                        (cre.getDomain().toUpperCase(), "ASCII");
        byte[] work = EncodingUtil.getBytes
                      (cre.getHost().toUpperCase(), "ASCII");
        //
        try {
            baos.write(NTLM_HEADER);
            baos.write(TYPE1_HEADER);
            //フラグ
            NumUtil.writeInteger(baos, 0x00003205);
            /*ドメイン・セキュリティバッファ*/
            //長さを二回
            byte[] dm_length = convertShort(domain.length);
            baos.write(dm_length);
            baos.write(dm_length);
            //オフセット
            baos.write(convertShort(32));
            //0パディング
            baos.write(0);
            baos.write(0);
            /*ワークステーション・セキュリティバッファ*/
            //長さを二回
            byte[] wk_length = convertShort(work.length);
            baos.write(wk_length);
            baos.write(wk_length);
            //オフセット
            baos.write(convertShort(32 + domain.length));
            //0パディング
            baos.write(0);
            baos.write(0);
            /*実際のデータ*/
            //ドメイン
            baos.write(domain);
            //ワークステーション
            baos.write(work);
            //一応
            baos.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return baos.toByteArray();
    }

    private byte[] getType3Msg(NTCredentials cre) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        byte[] domain = EncodingUtil.getBytes(cre.getDomain(), AUTH_CHARSET);
        byte[] user = EncodingUtil.getBytes(cre.getUserName(), AUTH_CHARSET);
        byte[] work = EncodingUtil.getBytes(cre.getHost(), AUTH_CHARSET);
        //LMレスポンス計算
        byte[] lm_resp = null;
        try {
            byte[] lm_nonce = new byte[8];
            rnd.nextBytes(lm_nonce);
            lm_resp =
                    /*
                    Responses.getLMv2Response(cre.getDomain(),
                                              cre.toString(),
                                              cre.getPassword(),
                                              Challenge,
                                              lm_nonce);
             */
            Responses.getLMResponse(cre.getPassword(),Challenge);
        } catch (Exception ex2) {
            ex2.printStackTrace();
            return null;
        }
        //NTLMレスポンス計算
        byte[] ntlm_resp = null;
        try {
            byte[] ntlm_nonce = new byte[8];
            rnd.nextBytes(ntlm_nonce);
            ntlm_resp =
                    /*
                    Responses.getNTLMv2Response(cre.getDomain(),
                                                cre.getUserName(),
                                                cre.getPassword(),
                                                TargetInfo,
                                                Challenge,
                                                ntlm_nonce);
                     */
            Responses.getNTLMResponse(cre.getPassword(),Challenge);
        } catch (Exception ex1) {
            ex1.printStackTrace();
            return null;
        }

        final int max_length = 64 + lm_resp.length + ntlm_resp.length +
                               domain.length + work.length + user.length;
        try {
            //len:0
            baos.write(NTLM_HEADER);
            baos.write(TYPE3_HEADER);
            //len:12
            //LMレスポンスの長さ(二回)
            byte lm_len[] = convertShort(lm_resp.length);
            baos.write(lm_len);
            baos.write(lm_len);
            //LMレスポンスのオフセット
            baos.write(convertShort
                       (max_length - lm_resp.length - ntlm_resp.length));
            baos.write(0);
            baos.write(0);
            //len:20

            //NTLMレスポンスの長さ
            byte[] ntlm_len = convertShort(ntlm_resp.length);
            baos.write(ntlm_len);
            baos.write(ntlm_len);
            //NTLMレスポンスオフセット
            baos.write(convertShort(max_length - ntlm_resp.length));
            baos.write(0);
            baos.write(0);
            //len:28

            //ドメイン長さ
            byte[] dm_len = convertShort(domain.length);
            baos.write(dm_len);
            baos.write(dm_len);
            //ドメインオフセット
            baos.write(convertShort(64));
            baos.write(0);
            baos.write(0);
            //len:36

            //ユーザ長さ
            byte[] us_len = convertShort(user.length);
            baos.write(us_len);
            baos.write(us_len);
            //ユーザオフセット
            baos.write(convertShort(64 + domain.length));
            baos.write(0);
            baos.write(0);
            //len:44

            //ワークステーション長さ
            byte[] wk_len = convertShort(work.length);
            baos.write(wk_len);
            baos.write(wk_len);
            //ユーザオフセット
            baos.write(convertShort(64 + domain.length + user.length));
            baos.write(0);
            baos.write(0);
            //len:52
            //セッション鍵長さ
            baos.write(0);
            baos.write(0);
            //二回目
            baos.write(0);
            baos.write(0);
            //セッション鍵　オフセット：最後(なし)
            baos.write(convertShort(max_length));
            baos.write(0);
            baos.write(0);
            //len:60
            ///フラグ
            NumUtil.writeInteger(baos, 0x00000201);
            //len:64

            //ドメイン　データ
            baos.write(domain);
            //ユーザ　データ
            baos.write(user);
            //ワークステーション　データ
            baos.write(work);
            //LMレスポンス
            baos.write(lm_resp);
            //NTLMレスポンス
            baos.write(ntlm_resp);
            //一応
            baos.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return baos.toByteArray();
    }

    private byte[] convertShort(int num) {
        byte[] val = new byte[2];
        val[0] = (byte) (num & 0xff);
        val[1] = (byte) ((num >> 8) & 0xff);
        return val;
    }

    /**
     * @deprecated Use {@link #authenticate(Credentials, HttpMethod)}
     * Produces an authorization string for the given set of {@link
     * Credentials}, method name and URI using the given authentication
     * scheme in response to the actual authorization challenge.
     *
     * @param credentials The set of credentials to be used for athentication
     * @param method The name of the method that requires authorization.
     *   This parameter may be ignored, if it is irrelevant or not
     *   applicable to the given authentication scheme
     * @param uri The URI for which authorization is needed. This parameter
     *   may be ignored, if it is irrelevant or not applicable to the given
     *   authentication scheme
     * @throws AuthenticationException if authorization string cannot be
     *   generated due to an authentication failure
     * @return the authorization string
     */
    public String authenticate(Credentials credentials, String method,
                               String uri) throws AuthenticationException {
        return authenticate(credentials, null);
    }

    /**
     * Returns a String identifying the authentication challenge.
     *
     * @return String a String identifying the authentication challenge. The
     *   returned value may be null.
     */
    public String getID() {
        StringBuffer sb = new StringBuffer(16);
        for (int i = 0; i < Challenge.length; i++) {
            sb.append(Integer.toHexString
                      (Challenge[i] < 0 ? Challenge[i] + 256 : Challenge[i]));
        }
        return sb.substring(0);
    }

    /**
     * Returns authentication parameter with the given name, if available.
     *
     * @param name The name of the parameter to be returned
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        return null;
    }

    /**
     * Returns authentication realm.
     *
     * @return the authentication realm
     */
    public String getRealm() {
        return null;
    }

    /**
     * Returns textual designation of the given authentication scheme.
     *
     * @return the name of the given authentication scheme
     */
    public String getSchemeName() {
        return "ntlm";
    }

    /**
     * Authentication process may involve a series of challenge-response
     * exchanges.
     *
     * @return <tt>true</tt> if the authentication process has been
     *   completed, <tt>false</tt> otherwise.
     */
    public boolean isComplete() {
        return stage == Stage.TYPE3_SENT;
    }

    /**
     * @return <tt>true</tt> if the scheme is connection based,
     * <tt>false</tt> if the scheme is request based.
     *
     * @return <tt>true</tt> if the scheme is connection based,
     *   <tt>false</tt> if the scheme is request based.
     */
    public boolean isConnectionBased() {
        //Connection Based
        return true;
    }

    /**
     * Processes the given challenge token.
     *
     * @param challenge the challenge string
     * @throws MalformedChallengeException
     */
    private byte[] Challenge = new byte[8];
    private byte[] TargetInfo;
    public void processChallenge(String challenge) throws
            MalformedChallengeException {
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException("Invalud scheme: " +
                                                  challenge);
        }
        int idx = challenge.indexOf(' ');
        if (idx < 0) { //デフォルト
            stage = Stage.DEFAULT;
            return;
        }
        if (stage != Stage.TYPE1_SENT) {
            throw new MalformedChallengeException("Have not sent Type1 Msg.");
        }
        byte[] msg = Base64.decodeBase64(
                EncodingUtil.getAsciiBytes(challenge.substring(idx).trim()));
        if (msg.length < 48) {
            throw new MalformedChallengeException("Data length is " +
                                                  msg.length + " (< 48)");
        }
        //Challengeは24から。
        for (int i = 0; i < 8; i++) {
            Challenge[i] = msg[i + 24];
        }
        //ターゲット
        int target_length = NumUtil.toInt(msg[0x28]) + (NumUtil.toInt(msg[0x29]) << 8);
        int target_off = NumUtil.toInt(msg[0x2C]) + (NumUtil.toInt(msg[0x2d]) << 8);
        if (target_length + target_off > msg.length) {
            throw new MalformedChallengeException("Data length is too short.");
        }
        TargetInfo = new byte[target_length];
        for (int i = 0; i < target_length; i++) {
            TargetInfo[i] = msg[target_off + i];
        }
        stage = Stage.TYPE2_RECV;
    }
}
