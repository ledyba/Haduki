package hadukiserver.core.user;

import psi.security.rsa.RSACipher;
import hadukiserver.core.Logger;
import java.io.*;
import java.util.Random;
import psi.security.rc4.RC4Cipher;
import psi.haduki.lib.HadukiCode;
import psi.haduki.lib.util.IntValue;

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
public class User {
    private boolean LoggedIn = false;
    private boolean Initialized = false;
    private static Random Rnd = new Random();
    private final String Name;
    private final int MaxChannel;
    private final Channel Channel[];
    private final int UserID;
    private final byte[] Password;
    private RSACipher Cipher;
    private byte[] LogoffSpell;
    public User(String name, int max_channel, int user_id, byte[] password) {
        Name = name;
        MaxChannel = max_channel;
        UserID = user_id;
        Password = password;
        Channel = new Channel[max_channel];
        for (int i = 0; i < max_channel; i++) {
            Channel[i] = new Channel(user_id, i);
        }
    }

    public int getUserID() {
        return UserID;
    }

    public synchronized boolean login(RSACipher rsa, int length, byte[] pass) {
        if (LoggedIn) {
            Logger.info("<" + Name + ">すでにログインしています。");
            return false;
        }
        if (Password.length != length) {
            Logger.info("<" + Name + ">Login failed.[Password error.]");
            return false;
        }
        byte[] enc_password = null;
        try {
            enc_password = rsa.encData(Password, 0, Password.length);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.error("Error while encrypting data.@User#login");
            return false;
        }
        if (enc_password.length != pass.length) {
            Logger.info("<" + Name + ">Login failed.[Password error.2]");
            return false;
        }
        for (int i = 0; i < enc_password.length; i++) {
            if (enc_password[i] != pass[i]) {
                return false;
            }
        }
        Cipher = rsa;
        LoggedIn = true;
        Initialized = false;
        return true;
    }
    public boolean logoff(byte[] spell){
        if(spell.length != LogoffSpell.length){
            return false;
        }
        for(int i=0;i<spell.length;i++){
            return false;
        }
        LoggedIn = false;
        return true;
    }

    public synchronized byte[] initialize(IntValue value) {
        if (!LoggedIn || Initialized) {
            return null;
        }
        byte[] key = new byte[256];
        Rnd.nextBytes(key);
        byte[] encKey = null;
        try {
            encKey = Cipher.encData(key, 0, key.length);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        RC4Cipher cipher = new RC4Cipher(key);
        LogoffSpell = cipher.encData(HadukiCode.LOGOFF_SPELL.clone(), 0,
                                     HadukiCode.LOGOFF_SPELL.length);
        Initialized = true;
        value.setValue(MaxChannel);
        return encKey;
    }
}
