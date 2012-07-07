package psi;

import psi.haduki.lib.HadukiManager;
import java.io.*;

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
public class Prompt {
    public static void main(String[] args){
        try {
            byte[] pass = {
                          (byte)0x62,(byte)0xD1,(byte)0x72,(byte)0x14,(byte)0xA3,(byte)0x7A,(byte)0x03,(byte)0xD1,(byte)0xB2,(byte)0x9A,(byte)0xA0,(byte)0x07,(byte)0x6D,(byte)0x68,(byte)0x8A,(byte)0xA7,(byte)0x9D,(byte)0xFD,(byte)0xB0,(byte)0x21,(byte)0x4B,(byte)0x96,(byte)0xD2,(byte)0x73,(byte)0x86,(byte)0xBE,(byte)0x59,(byte)0x01,(byte)0xA1,(byte)0x11,(byte)0x2F,(byte)0xB0
            };
            HadukiManager manager = new HadukiManager(
                    "http://127.0.0.1:44444/", 0xBCAE3593, pass, 128);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
