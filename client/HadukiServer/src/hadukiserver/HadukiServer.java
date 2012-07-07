package hadukiserver;

import java.awt.Toolkit;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import hadukiserver.core.Server;
import hadukiserver.core.Logger;

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
public class HadukiServer {
    boolean packFrame = false;

    /**
     * �A�v���P�[�V�����̍\�z�ƕ\���B
     */
    public HadukiServer() {
        MainFrame frame = new MainFrame();
        // validate() �̓T�C�Y�𒲐�����
        // pack() �͗L���ȃT�C�Y�������C�A�E�g�Ȃǂ���擾����
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }

        // �E�B���h�E�𒆉��ɔz�u
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
    }

    /**
     * �A�v���P�[�V�����G���g���|�C���g�B
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        if(args.length != 0){
            Logger.setDebugLevel(Logger.DEBUG_LEVEL_DEBUG);
            ServerRunner sr = new ServerRunner(args);
            Server serv = sr.runServer();
            try {
                serv.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.
                                             getSystemLookAndFeelClassName());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                new HadukiServer();
            }
        });
    }
}
