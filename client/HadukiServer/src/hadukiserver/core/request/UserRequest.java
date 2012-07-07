package hadukiserver.core.request;

import hadukiserver.core.user.User;
import java.io.InputStream;
import java.io.OutputStream;
import psi.util.BuffUtil;
import psi.util.NumUtil;
import java.io.IOException;
import hadukiserver.core.Logger;
import psi.haduki.lib.HadukiCode;
import psi.security.rsa.RSACipher;
import psi.haduki.lib.util.IntValue;

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
public class UserRequest implements Request {
    private final User User;
    private final InputStream In;
    private final OutputStream Out;
    public UserRequest(User user, InputStream is, OutputStream os) {
        User = user;
        In = is;
        Out = os;
    }

    public void exec() throws IOException {
        int mode = NumUtil.readInteger(In);
        boolean ret = false;
        switch (mode) {
        case HadukiCode.LOGIN_ACCEPT:
            execLogin();
            break;
        case HadukiCode.LOGOFF_ACCEPT:
            execLogoff();
            break;
        default:
            throw new IOException("���N�G�X�g���ُ�ł��B@UserRequest#exec");
        }
    }

    public void execLogin() throws IOException {
        /*���J���̎擾*/
        int module_data_size = NumUtil.readInteger(In);
        int module_security = NumUtil.readInteger(In);
        byte[] module = new byte[module_data_size];
        int module_read = BuffUtil.readStream(In, module, 0, module_data_size);
        if (module_read != module_data_size) {
            Logger.debug("���N�G�X�g���ُ�ł��B@UserRequest#execLogin#1");
            throw new IOException("���N�G�X�g���ُ�ł��B");
        }
        int power_data_size = NumUtil.readInteger(In);
        int power_security = NumUtil.readInteger(In);
        byte[] power = new byte[power_data_size];
        int power_read = BuffUtil.readStream(In, power, 0, power.length);
        if (power_read != power_data_size) {
            Logger.debug("���N�G�X�g���ُ�ł��B@UserRequest#execLogin#2");
            throw new IOException("���N�G�X�g���ُ�ł��B");
        }
        /*���J������Í��@���쐬*/
        RSACipher cipher =
                new RSACipher(power_security, power, module_security, module); ;
        /*�Í������ꂽ�p�X���[�h�̎擾*/
        int pass_data_size = NumUtil.readInteger(In);
        int pass_org_size = NumUtil.readInteger(In);
        byte[] pass = new byte[pass_data_size];
        int pass_read = BuffUtil.readStream(In, pass, 0, pass.length);
        if (pass_read != pass_data_size) {
            Logger.debug("���N�G�X�g���ُ�ł��B@UserRequest#execLogin#3");
            throw new IOException("���N�G�X�g���ُ�ł��B");
        }
        /*���ʂ̏o��*/
        if (User.login(cipher, pass_org_size, pass)) {
            IntValue max_channel = new IntValue(0);
            byte[] key = User.initialize(max_channel);
            ReqUtil.writeHeader(Out, false,false, key.length + 16);
            NumUtil.writeInteger(Out, HadukiCode.LOGIN_SUCCESS);
            NumUtil.writeInteger(Out, max_channel.getValue());
            NumUtil.writeInteger(Out, key.length);
            NumUtil.writeInteger(Out, 256);
            Out.write(key);
        } else {
            ReqUtil.writeHeader(Out, false,false, 4);
            NumUtil.writeInteger(Out, HadukiCode.LOGIN_FAILURE);
        }
    }

    public void execLogoff() throws IOException {
        int data_size = NumUtil.readInteger(In);
        byte[] spell = new byte[data_size];
        int spell_read = BuffUtil.readStream(In, spell, 0, data_size);
        if(spell_read != data_size){
            Logger.debug("���N�G�X�g���ُ�ł�@UserRequest#execLogoff");
            throw new IOException("���N�G�X�g���ُ�ł�@UserRequest#execLogoff");
        }
        ReqUtil.writeHeader(Out,false,false,4);
        if(User.logoff(spell)){
            NumUtil.writeInteger(Out,HadukiCode.LOGOFF_SUCCESS);
        }else{
            NumUtil.writeInteger(Out,HadukiCode.LOGOFF_FAILURE);
        }
    }
}
