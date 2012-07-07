package psi.haduki.lib;

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
public class HadukiCode {
    /*チャンネル*/
    public static final int CHANNEL_BROADCAST = 0xFFFFFFFF;
    /*ログイン*/
    public static final int LOGIN_ACCEPT = 0x00000001;
    public static final int LOGIN_SUCCESS = 0x00000001;
    public static final int LOGIN_FAILURE = 0xFFFFFFFF;
    /*ログオフ*/
    public static final byte[] LOGOFF_SPELL = {
                          (byte)0xE3,(byte)0x83,(byte)0x91,(byte)0xE3,(byte)0x82,
                          (byte)0xA4,(byte)0xE3,(byte)0x83,(byte)0x91,(byte)0xE3,
                          (byte)0x82,(byte)0xA4,(byte)0xE3,(byte)0x83,(byte)0x9D,
                          (byte)0xE3,(byte)0x83,(byte)0xB3,(byte)0xE3,(byte)0x83,
                          (byte)0x9D,(byte)0xE3,(byte)0x82,(byte)0xA4,(byte)0xE3,
                          (byte)0x80,(byte)0x80,(byte)0xE3,(byte)0x83,(byte)0x97,
                          (byte)0xE3,(byte)0x83,(byte)0xAF,(byte)0xE3,(byte)0x83,
                          (byte)0x97,(byte)0xE3,(byte)0x83,(byte)0xAF,(byte)0xE3,
                          (byte)0x83,(byte)0x97,(byte)0x0D,(byte)0x0A,(byte)0xE3,
                          (byte)0x83,(byte)0x91,(byte)0xE3,(byte)0x82,(byte)0xA4,
                          (byte)0xE3,(byte)0x83,(byte)0x91,(byte)0xE3,(byte)0x82,
                          (byte)0xA4,(byte)0xE3,(byte)0x83,(byte)0x9D,(byte)0xE3,
                          (byte)0x83,(byte)0xB3,(byte)0xE3,(byte)0x83,(byte)0x9D,
                          (byte)0xE3,(byte)0x82,(byte)0xA4,(byte)0xE3,(byte)0x80,
                          (byte)0x80,(byte)0xE3,(byte)0x81,(byte)0x97,(byte)0xE3,
                          (byte)0x81,(byte)0xAA,(byte)0xE3,(byte)0x82,(byte)0x84,
                          (byte)0xE3,(byte)0x81,(byte)0x8B,(byte)0xE3,(byte)0x81,
                          (byte)0xAB
    };
    public static final int LOGOFF_ACCEPT = 0x00000002;
    public static final int LOGOFF_SUCCESS = 0x00000001;
    public static final int LOGOFF_FAILURE = 0xFFFFFFFF;
}
