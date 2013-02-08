package hadukiclient.serv_connection;

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
public class LoginInfo {
    private int UserID;
    private int SessionID;
    private byte[] Password;
    private byte[] PublicKey;
    private byte[] PrivateKey;

    public LoginInfo(int user_id,byte[] password) {
        UserID = user_id;
        Password = password;
        SessionID = (new java.util.Random()).nextInt();
    }

    public int getUserID() {
        return UserID;
    }

    public byte[] getPassword() {
        return Password;
    }

    public boolean calcKey(){
        PublicKey = new byte[32];
        PrivateKey = new byte[32];
        return true;
    }

    public int getSessionID(){
        return SessionID;
    }

    public byte[] getPublicKey() {
        return PublicKey;
    }

    public byte[] getPrivateKey() {
        return PrivateKey;
    }
}
