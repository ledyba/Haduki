package hadukiserver;

import java.awt.Toolkit;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import hadukiserver.core.Server;
import hadukiserver.core.Logger;

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
public class HadukiServer {
    boolean packFrame = false;

    /**
     * アプリケーションの構築と表示。
     */
    public HadukiServer() {
        MainFrame frame = new MainFrame();
        // validate() はサイズを調整する
        // pack() は有効なサイズ情報をレイアウトなどから取得する
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }

        // ウィンドウを中央に配置
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
     * アプリケーションエントリポイント。
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
