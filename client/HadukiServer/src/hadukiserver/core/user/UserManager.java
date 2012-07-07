package hadukiserver.core.user;

import java.util.Hashtable;
import java.io.*;
import psi.util.*;

/**
 * <p>�^�C�g��: �u�͂Â��v�T�[�o</p>
 *
 * <p>����: �u�͂Â��v�̃T�[�o�ł��B</p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class UserManager {
    private final Hashtable<Integer,User> UserTable = new Hashtable<Integer,User>();
    public UserManager() {
        this("user.db");
    }
    public UserManager(String filename){
        InputStream is;
        try {
            is = new FileInputStream(filename);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            int user_count = NumUtil.readInteger(is);
            for (int i = 0; i < user_count; i++) {
                int user_id = NumUtil.readInteger(is);
                int channels = NumUtil.readInteger(is);

                int name_length = NumUtil.readInteger(is);
                byte name[] = new byte[name_length];
                int name_read = BuffUtil.readStream(is, name, 0, name.length);
                if (name_read != name.length) {
                    throw new IOException("���[�U�f�[�^�ُ̈�");
                }

                int pass_length = NumUtil.readInteger(is);
                byte[] pass = new byte[pass_length];
                int pass_read = BuffUtil.readStream(is, pass, 0, pass.length);
                if (pass_read != pass.length) {
                    throw new IOException("���[�U�f�[�^�ُ̈�");
                }
                User user = new User(new String(name),channels,user_id,pass);
                UserTable.put(new Integer(user_id),user);
            }
        } catch (IOException ex1) {
            ex1.printStackTrace();
            return;
        }
    }
    public User searchUser(int user_id){
        return UserTable.get(new Integer(user_id));
    }
}
