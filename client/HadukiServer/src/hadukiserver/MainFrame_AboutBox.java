package hadukiserver;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

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
public class MainFrame_AboutBox extends JDialog implements ActionListener {
    JPanel panel1 = new JPanel();
    JPanel panel2 = new JPanel();
    JPanel insetsPanel1 = new JPanel();
    JPanel insetsPanel2 = new JPanel();
    JPanel insetsPanel3 = new JPanel();
    JButton button1 = new JButton();
    JLabel imageLabel = new JLabel();
    JLabel label1 = new JLabel();
    JLabel label2 = new JLabel();
    JLabel label3 = new JLabel();
    JLabel label4 = new JLabel();
    JLabel label5 = new JLabel();
    ImageIcon image1 = new ImageIcon();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    String product = "「はづき」サーバ";
    String version = "1.0";
    String copyright = "Copyright (c) 2007 PSI";
    String comments = "パイパイポンポイ しなやかに～！";
    String comments2 = "・・・はぁorz";
    GridBagLayout gridBagLayout1 = new GridBagLayout();

    public MainFrame_AboutBox(Frame parent) {
        super(parent);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public MainFrame_AboutBox() {
        this(null);
    }

    /**
     * コンポーネントの初期化。
     *
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        image1 = new ImageIcon(hadukiserver.MainFrame.class.getResource(
                "icon_100.png"));
        imageLabel.setIcon(image1);
        setTitle("バージョン情報");
        panel1.setLayout(borderLayout1);
        panel2.setLayout(borderLayout2);
        insetsPanel1.setLayout(flowLayout1);
        insetsPanel2.setLayout(flowLayout1);
        insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label1.setText(product);
        label2.setText(version);
        label3.setText(copyright);
        label4.setText(comments);
        label5.setText(comments2);
        insetsPanel3.setLayout(gridBagLayout1);
        button1.setText("OK");
        button1.addActionListener(this);
        insetsPanel2.add(imageLabel, null);
        panel2.add(insetsPanel2, BorderLayout.WEST);
        getContentPane().add(panel1, null);
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(button1, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
        insetsPanel3.add(label1, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(10, 10, 0, 10), 0, 0));
        insetsPanel3.add(label2, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 10, 0, 10), 0, 0));
        insetsPanel3.add(label4, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 10, 0, 10), 0, 0));
        insetsPanel3.add(label3, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 10, 0, 10), 0, 0));
        insetsPanel3.add(label5, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 10, 10, 10), 0, 0));
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
