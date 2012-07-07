package hadukiclient;

import javax.swing.JLabel;
import javax.swing.JButton;

/**
 * <p>タイトル: 「葉月」</p>
 *
 * <p>説明: </p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class HadukiGUIsupport {
    private JLabel Status;
    private JButton Con;
    private JButton Dis;
    public HadukiGUIsupport(JLabel status,JButton con,JButton dis) {
        Status = status;
        Con = con;
        Dis = dis;
    }
    public void setState(String str){
        Status.setText(str);
    }
    public static final int CODE_NOT_CONNECTED = 0;
    public static final int CODE_CONNECTING = 1;
    public static final int CODE_CONNECTED = 2;
    public void setConnected(int code){
        switch(code){
        case CODE_NOT_CONNECTED:
            Con.setEnabled(true);
            Dis.setEnabled(false);
            break;
        case CODE_CONNECTING:
            Con.setEnabled(false);
            Dis.setEnabled(false);
            break;
        case CODE_CONNECTED:
            Con.setEnabled(false);
            Dis.setEnabled(true);
            break;
        }
    }
}
