package hadukiclient;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

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
public class HadukiMainFrame_AboutBox extends JDialog implements ActionListener {
    JPanel panel1 = new JPanel();
    JPanel MainPanel = new JPanel();
    JPanel ButtonPanel = new JPanel();
    JPanel TextPanel = new JPanel();
    JButton button1 = new JButton();
    JLabel imageLabel = new JLabel();
    JLabel label1 = new JLabel();
    JLabel label2 = new JLabel();
    JLabel label3 = new JLabel();
    JLabel label4 = new JLabel();
    JLabel label5 = new JLabel();
    ImageIcon image1 = new ImageIcon();
    BorderLayout borderLayout1 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    String product = "「葉月」";
    String version = "Version 1.0";
    String copyright = "Copyright (c) 2007 PSI";
    String comment1 = "おにいさま、ふるふるふるむーん！";
    String comment2 = "・・・はぁorz";
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    public HadukiMainFrame_AboutBox(Frame parent) {
        super(parent);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public HadukiMainFrame_AboutBox() {
        this(null);
    }

    /**
     * コンポーネントの初期化。
     *
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        image1 = new ImageIcon(hadukiclient.HadukiMainFrame.class.getResource(
                "icon_100.png"));
        imageLabel.setIcon(image1);
        setTitle("バージョン情報");
        panel1.setLayout(borderLayout1);
        MainPanel.setLayout(gridBagLayout2);
        ButtonPanel.setLayout(flowLayout1);
        label1.setText(product);
        label2.setText(version);
        label3.setText(copyright);
        label4.setText(comment1);
        label5.setText(comment2);
        TextPanel.setLayout(gridBagLayout1);
        button1.setText("OK");
        button1.addActionListener(this);
        getContentPane().add(panel1, null);
        ButtonPanel.add(button1, null);
        panel1.add(ButtonPanel, BorderLayout.SOUTH);
        panel1.add(MainPanel, BorderLayout.NORTH);
        TextPanel.add(label5, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 5, 5, 5), 0, 0));
        TextPanel.add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        TextPanel.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        TextPanel.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        TextPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 5, 0, 5), 0, 0));
        MainPanel.add(imageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(10, 10, 10, 0), 0, 0));
        MainPanel.add(TextPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(10, 10, 10, 10), 0, 0));
        setResizable(true);
    }

    /**
     * ボタンイベントでダイアログを閉じる
     *
     * @param actionEvent ActionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == button1) {
            dispose();
        }
    }
}
