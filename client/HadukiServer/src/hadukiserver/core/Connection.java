package hadukiserver.core;

import java.util.concurrent.Semaphore;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
import psi.haduki.lib.stream.*;
import psi.util.NumUtil;
import psi.haduki.lib.HadukiCode;
import hadukiserver.core.request.*;
import hadukiserver.core.user.*;

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
public class Connection extends Thread {
    private final Semaphore Sem;
    private final Socket Sock;
    private final UserManager UserManager;
    public Connection(Semaphore sem, UserManager user_manager, Socket sock) {
        Sem = sem;
        Sock = sock;
        UserManager = user_manager;
    }

    public void run() {
        try {
            exec();
        } finally {
            try {
                Sock.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Sem.release();
        }
    }

    /**
     * 実際に実行されるのはこれ。
     */
    public void exec() {
        InputStream is;
        OutputStream os;
        try {
            is = Sock.getInputStream();
            os = Sock.getOutputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        boolean running = true;
        while (running) {
            try {
                running &= exec_request(is, os);
                os.flush();
            } catch (IOException ex1) {
                running = false;
                ex1.printStackTrace();
            }
        }
    }

    private User LastUser;
    private Channel LastChannel;
    private boolean exec_request(InputStream is, OutputStream os) throws
            IOException {
        InputStream hi = new HTTP_HeaderInputStream(is);
        BufferedReader hbr = new BufferedReader(new InputStreamReader(hi));
        String line;
        boolean keep_alive = true;
        boolean is_chunked = false;
        boolean is_post = true;
        int content_length = -1;
        int count = 0;
        while (!(line = hbr.readLine()).equals("")) {
            if (!is_post) {
                continue;
            }
            line = line.toLowerCase();
            if (count == 0 && !line.startsWith("post")) {
                is_post = false;
            }
            if (line.indexOf("transfer-encoding") >= 0 &&
                line.indexOf("chunked") >= 0) {
                is_chunked = true;
                break;
            } else if (line.startsWith("content-length:")) {
                content_length = Integer.parseInt(
                        line.substring("content-length: ".length()));
            }
            count++;
        }
        if (!is_post) {
            InputStream fis = new FileInputStream("default.html");
            byte[] file = new byte[fis.available()];
            fis.read(file);
            fis.close();
            ReqUtil.writeHeader(os, false, true, file.length);
            os.write(file);
            return false;
        }
        InputStream dis;
        if (is_chunked) {
            dis = new HTTP_ChunkedInputStream(is);
        } else {
            if (content_length < 0) {
                dis = is;
                keep_alive = false;
            } else {
	            dis = new HTTP_ContentLengthInputStream(is, content_length);
            }
        }
        int user_id = NumUtil.readInteger(dis);
        int channel_id = NumUtil.readInteger(dis);
        Request req = null;
        if (channel_id == HadukiCode.CHANNEL_BROADCAST) {
            //ログイン・ログオフ関連
            if (LastUser != null && LastUser.getUserID() == user_id) {
                req = new UserRequest(LastUser, dis, os);
            } else {
                LastUser = UserManager.searchUser(user_id);
                req = new UserRequest(LastUser, dis, os);
            }
        } else {
            //チャンネル
        }
        req.exec();
        return keep_alive;
    }
}
