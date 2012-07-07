package hadukiclient;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

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
public class HadukiMainFrame extends JFrame {
    public static final Image WinIcon = Toolkit.getDefaultToolkit().createImage(
            hadukiclient.HadukiMainFrame.class.getResource("icon_32.png"));

    private static final String DefaultServerInfoFile = "server.xml";
    private String DefaultServer = "";
    private String DefaultServerPort = "";
    private boolean DefaultProxyServerUsing = false;
    private String DefaultProxyServer = "";
    private String DefaultProxyServerPort = "";
    private String DefaultProxyServerUser = "";

    Manager Manager;
    HadukiGUIsupport Sup;
    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuFile = new JMenu();
    JMenuItem jMenuFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    JLabel statusBar = new JLabel();
    JPanel MainPanel = new JPanel();
    JLabel TitleLabel = new JLabel();
    JCheckBox UsingProxyCheck = new JCheckBox();
    JLabel UserIDLabel = new JLabel();
    JTextField ProxyUserIDField = new JTextField();
    JLabel PassWordLabel = new JLabel();
    JPasswordField ProxyPasswordField = new JPasswordField();
    JPanel ProxyPanel = new JPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel ServerPanel = new JPanel();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JLabel PortLabel = new JLabel();
    JTextField PortField = new JTextField();
    JLabel ServerLabel = new JLabel();
    JTextField ServerField = new JTextField();
    JLabel jLabel1 = new JLabel();
    JLabel ProxyServerLabel = new JLabel();
    JLabel ProxyPortLabel = new JLabel();
    JTextField ProxyServerField = new JTextField();
    JTextField ProxyPortField = new JTextField();
    JPanel ButtonPanel = new JPanel();
    JPanel EMPTYPanel = new JPanel();
    JButton ConnectButton = new JButton();
    JButton DisconnectButton = new JButton();
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    public HadukiMainFrame() {
        try {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            initServerInfo();
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    //サーバ設定の読み込み
    private void initServerInfo() throws IOException {
        File prop_file = new File(DefaultServerInfoFile);
        if (!prop_file.canRead()) {
            return;
        }
        Properties prop = new Properties();
        InputStream is = new FileInputStream(prop_file);
        prop.loadFromXML(is);
        is.close();
        DefaultServer = prop.getProperty("Server");
        DefaultServer = DefaultServer == null ? "" : DefaultServer;

        DefaultServerPort = prop.getProperty("ServerPort");
        DefaultServerPort = DefaultServerPort == null ? "" : DefaultServerPort;

        String using_port = prop.getProperty("ProxyServerUsing");
        DefaultProxyServerUsing = using_port == null ? false :
                                  using_port.toLowerCase().equals("true");

        DefaultProxyServer = prop.getProperty("ProxyServer");
        DefaultProxyServer = DefaultProxyServer == null ? "" :
                             DefaultProxyServer;

        DefaultProxyServerPort = prop.getProperty("ProxyServerPort");
        DefaultProxyServerPort = DefaultProxyServerPort == null ? "" :
                                 DefaultProxyServerPort;

        DefaultProxyServerUser = prop.getProperty("ProxyServerUser");
        DefaultProxyServerUser = DefaultProxyServerUser == null ? "" :
                                 DefaultProxyServerUser;
    }

    /**
     * コンポーネントの初期化。
     *
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        this.setIconImage(WinIcon);
        this.setResizable(false);
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(borderLayout1);
        setSize(new Dimension(300, 400));
        setTitle("「葉月」");
        statusBar.setText(" ");
        jMenuFile.setText("ファイル");
        jMenuFileExit.setText("終了");
        jMenuFileExit.addActionListener(new
                                        HadukiMainFrame_jMenuFileExit_ActionAdapter(this));
        jMenuHelp.setText("ヘルプ");
        jMenuHelpAbout.setText("バージョン情報");
        jMenuHelpAbout.addActionListener(new
                                         HadukiMainFrame_jMenuHelpAbout_ActionAdapter(this));
        TitleLabel.setFont(new java.awt.Font("Dialog", Font.PLAIN, 20));
        TitleLabel.setText("「葉月」");
        UsingProxyCheck.setSelected(DefaultProxyServerUsing);
        UsingProxyCheck.setText("Using proxy");
        MainPanel.setLayout(gridBagLayout1);
        UserIDLabel.setText("User ID");
        PassWordLabel.setText("Password");
        ProxyPanel.setLayout(gridBagLayout2);
        ServerPanel.setLayout(gridBagLayout3);
        PortLabel.setText("Port");
        PortField.setText(DefaultServerPort);
        ServerLabel.setText("Server");
        ServerField.setText(DefaultServer);
        jLabel1.setText("Haduki Server");
        ProxyServerLabel.setText("Server");
        ProxyPortLabel.setText("Port");
        ProxyServerField.setText(DefaultProxyServer);
        ProxyPortField.setText(DefaultProxyServerPort);
        ProxyPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        ServerPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        ButtonPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        ButtonPanel.setLayout(gridBagLayout4);
        ConnectButton.setText("Connect");
        ConnectButton.addActionListener(new
                                        HadukiMainFrame_ConnectButton_actionAdapter(this));
        DisconnectButton.setEnabled(false);
        DisconnectButton.setText("Disconnect");
        DisconnectButton.addActionListener(new
                                           HadukiMainFrame_DisconnectButton_actionAdapter(this));
        ProxyUserIDField.setText(DefaultProxyServerUser);
        jMenuBar1.add(jMenuFile);
        jMenuFile.add(jMenuFileExit);
        jMenuBar1.add(jMenuHelp);
        jMenuHelp.add(jMenuHelpAbout);
        setJMenuBar(jMenuBar1);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        contentPane.add(MainPanel, java.awt.BorderLayout.CENTER);
        contentPane.add(TitleLabel, java.awt.BorderLayout.NORTH);
        ServerPanel.add(PortField,
                        new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                                               , GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5), 0, 0));
        ServerPanel.add(PortLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 0), 0, 0));
        ServerPanel.add(ServerLabel,
                        new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                               , GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(0, 5, 5, 0), 0, 0));
        ServerPanel.add(ServerField,
                        new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                                               , GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5), 0, 0));
        ServerPanel.add(jLabel1, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        MainPanel.add(ServerPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        MainPanel.add(EMPTYPanel, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        MainPanel.add(ButtonPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 5, 5), 0, 0));
        MainPanel.add(ProxyPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.SOUTH, GridBagConstraints.BOTH,
                new Insets(0, 5, 5, 5), 0, 0));
        ButtonPanel.add(DisconnectButton,
                        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                               , GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        ButtonPanel.add(ConnectButton,
                        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                               , GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 0), 0, 0));
        ProxyPanel.add(UsingProxyCheck,
                       new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5, 5, 0, 0), 0, 0));
        ProxyPanel.add(ProxyServerLabel,
                       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5, 5, 5, 5), 0, 0));
        ProxyPanel.add(ProxyServerField,
                       new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(5, 0, 5, 5), 0, 0));
        ProxyPanel.add(ProxyPortLabel,
                       new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5, 5, 5, 5), 0, 0));
        ProxyPanel.add(ProxyPortField,
                       new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(5, 0, 5, 5), 0, 0));
        ProxyPanel.add(UserIDLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        ProxyPanel.add(ProxyUserIDField,
                       new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(5, 0, 5, 5), 0, 0));
        ProxyPanel.add(PassWordLabel,
                       new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(5, 5, 5, 5), 0, 0));
        ProxyPanel.add(ProxyPasswordField,
                       new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.WEST,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(5, 0, 5, 5), 0, 0));
        Sup = new HadukiGUIsupport(statusBar, ConnectButton, DisconnectButton);
    }

    /**
     * [ファイル|終了] アクションが実行されました。
     *
     * @param actionEvent ActionEvent
     */
    void jMenuFileExit_actionPerformed(ActionEvent actionEvent) {
        System.exit(0);
    }

    /**
     * [ヘルプ|バージョン情報] アクションが実行されました。
     *
     * @param actionEvent ActionEvent
     */
    void jMenuHelpAbout_actionPerformed(ActionEvent actionEvent) {
        HadukiMainFrame_AboutBox dlg = new HadukiMainFrame_AboutBox(this);
        dlg.setModal(true);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /*接続*/
    public void ConnectButton_actionPerformed(ActionEvent e) {
        String server = ServerField.getText();
        String port_str = PortField.getText();
        if (server.equals("") || port_str.equals("")) {
            Sup.setState("Input server informations.");
            return;
        }
        int port = Integer.parseInt(port_str);
        if (this.UsingProxyCheck.isSelected()) {
            String proxy_server = ProxyServerField.getText();
            String proxy_port_str = ProxyPortField.getText();
            String proxy_user_id = ProxyUserIDField.getText();
            char[] pass_c = ProxyPasswordField.getPassword();
            String proxy_pass = new String(pass_c);
            for (int i = 0; i < pass_c.length; i++) {
                pass_c[i] = 0;
            }
            if (proxy_server.equals("") || proxy_port_str.equals("")) {
                Sup.setState("Input proxy server informations.");
                return;
            }
            int proxy_port = Integer.parseInt(proxy_port_str);
            Manager = new Manager(Sup, server, port, proxy_server, proxy_port,
                                  proxy_user_id, proxy_pass);
            Manager.start();
        } else {
            Manager = new Manager(Sup, server, port);
            Manager.start();
        }
    }

    /*切断*/
    public void DisconnectButton_actionPerformed(ActionEvent e) {
        Manager.stopRunning();
    }
}


class HadukiMainFrame_DisconnectButton_actionAdapter implements ActionListener {
    private HadukiMainFrame adaptee;
    HadukiMainFrame_DisconnectButton_actionAdapter(HadukiMainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.DisconnectButton_actionPerformed(e);
    }
}


class HadukiMainFrame_ConnectButton_actionAdapter implements ActionListener {
    private HadukiMainFrame adaptee;
    HadukiMainFrame_ConnectButton_actionAdapter(HadukiMainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ConnectButton_actionPerformed(e);
    }
}


class HadukiMainFrame_jMenuFileExit_ActionAdapter implements ActionListener {
    HadukiMainFrame adaptee;

    HadukiMainFrame_jMenuFileExit_ActionAdapter(HadukiMainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        adaptee.jMenuFileExit_actionPerformed(actionEvent);
    }
}


class HadukiMainFrame_jMenuHelpAbout_ActionAdapter implements ActionListener {
    HadukiMainFrame adaptee;

    HadukiMainFrame_jMenuHelpAbout_ActionAdapter(HadukiMainFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        adaptee.jMenuHelpAbout_actionPerformed(actionEvent);
    }
}
