package hadukiclient.serv_connection;

import hadukiclient.crypt.Crypt;

/**
 * <p>�^�C�g��: �u�t���v</p>
 *
 * <p>����: </p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class LoginInfo{
    private int UserID;
    private int SessionID;
    private byte[] Password;
    private Crypt Crypt = new Crypt();
    public LoginInfo(int user_id,byte[] password) {
        UserID = user_id;
        Password = password;
        SessionID = (new java.util.Random()).nextInt();
    }

    public int getUserID() {
        return UserID;
    }

    public final byte[] getPassword() {
        return Password;
    }

    public int getSessionID(){
        return SessionID;
    }
    public Crypt getCrypt(){
        return Crypt;
    }
    private Crypt backup;
    public void backupCrypt(){
        backup = (Crypt)Crypt.clone();
    }
    public void restoreCrypt(){
        Crypt = (Crypt)backup.clone();
    }
}
