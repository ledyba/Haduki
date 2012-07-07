package hadukiserver.core;

import java.net.*;
import java.io.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import hadukiserver.core.user.UserManager;

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
public class Server extends Thread {
    private final int Port;
    private boolean Running = true;
    private final HostFilter Filter;
    private final Semaphore Sem;
    private final UserManager UserManager;
    public Server(int port, int connections) {
        Port = port;
        Sem = new Semaphore(connections);
        Filter = new HostFilter();
        UserManager = new UserManager();
    }

    public void run() {
        Logger.info("Haduki Server started.");
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(" Haduki Server");
        System.out.println(" ver 1.0(2007/08/17)");
        System.out.println("          (C)PSI/Pegasus 2007");
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        try {
            ServerSocket s_sock = new ServerSocket(Port);
            s_sock.setSoTimeout(500);
            Socket sock;
            while (Running) {
                try {
                    sock = s_sock.accept();
                } catch (SocketTimeoutException e) {
                    continue;
                }
                InetAddress addr = sock.getInetAddress();
                if (!Filter.check(addr)) {
                    continue;
                }
                if (!Sem.tryAcquire()) {
                    Logger.info("<" + addr.getHostName() +
                                ">Server Busy. Kicked.");
                    continue;
                }
                System.out.println("おめでとう！");
                Thread connection = new Connection(Sem, UserManager, sock);
                connection.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Logger.info("Haduki Server stopped.");
    }

    public void stopServer() {
        Running = false;
    }
}
