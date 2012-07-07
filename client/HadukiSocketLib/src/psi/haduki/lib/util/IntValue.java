package psi.haduki.lib.util;

/**
 * <p>タイトル: 「はづき」汎用ソケットライブラリ</p>
 *
 * <p>説明: Socketクラスのサブクラスとして実装した汎用ライブラリ</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class IntValue {
    private int value;
    public IntValue(int v) {
        value = v;
    }
    public int getValue(){
        return value;
    }
    public void setValue(int v){
        value = v;
    }
}
