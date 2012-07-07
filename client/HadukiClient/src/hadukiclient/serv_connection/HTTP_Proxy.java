package hadukiclient.serv_connection;

import java.net.*;
import java.io.*;
import hadukiclient.client.BasicEncode;

/**
 * <p>タイトル: 「葉月」</p>
 *
 * <p>説明: プロキシを管理するクラスです。</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class HTTP_Proxy {
    private InetAddress Proxy;
    private boolean login = false;
    private int ProxyPort;
    private String Proxy_Auth_Header;
    public HTTP_Proxy(String proxy, int proxy_port, String user, String pass) {
        if (proxy != null && proxy_port >= 0 && proxy_port <= 0xffff) {
            try {
                Proxy = InetAddress.getByName(proxy);
                ProxyPort = proxy_port;
            } catch (UnknownHostException ex) {
                Proxy = null;
                ProxyPort = -1;
                ex.printStackTrace();
            }
            //プロキシ認証必要？
            if (user == null || pass == null || user.equals("") ||
                pass.equals("")) {
                login = false;
            } else {
                login = true;
                Proxy_Auth_Header = "Proxy-Authorization: Basic " +
                                    BasicEncode.encode(user + ":" + pass)+"\r\n";
            }
        } else {
            Proxy = null;
            ProxyPort = -1;
        }
    }

    public boolean isBad() {
        return Proxy == null || ProxyPort < 0;
    }

    public InetAddress getProxy() {
        return Proxy;
    }

    public int getProxyPort() {
        return ProxyPort;
    }

    public boolean isProxyAuth() {
        return login;
    }

    public String getProxyAuthHeader() {
        if (login) {
            return Proxy_Auth_Header;
        } else {
            return null;
        }
    }

    public Socket getSocket() {
        Socket sock = null;
        try {
            sock = new Socket(getProxy(), getProxyPort());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return sock;
    }
}
