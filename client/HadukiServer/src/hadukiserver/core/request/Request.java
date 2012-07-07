package hadukiserver.core.request;

import java.io.IOException;

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
public interface Request {
    public void exec() throws IOException;
}
