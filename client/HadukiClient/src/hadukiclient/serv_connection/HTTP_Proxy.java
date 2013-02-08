package hadukiclient.serv_connection;

import java.net.*;
import java.io.*;
import hadukiclient.client.BasicEncode;

/**
 * <p>�^�C�g��: �u�t���v</p>
 *
 * <p>����: �v���L�V���Ǘ�����N���X�ł��B</p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class HTTP_Proxy {
    private InetAddress Proxy;
    private int ProxyPort;
    private String login;
    public HTTP_Proxy(String proxy,int proxy_port,String user,String pass) {
        if (proxy != null && proxy_port >= 0 && proxy_port <= 0xffff) {
            try {
                Proxy = InetAddress.getByName(proxy);
                ProxyPort = proxy_port;
            } catch (UnknownHostException ex) {
                Proxy = null;
                ProxyPort = -1;
                ex.printStackTrace();
            }
            if(user == null || pass == null || user.equals("") || pass.equals("")){
                login = null;
            }else{
                login = BasicEncode.encode(user + ":" + pass);
            }
        } else {
            Proxy = null;
            ProxyPort = -1;
        }
    }
    public boolean isBad(){
        return Proxy == null || ProxyPort < 0;
    }

    public InetAddress getProxy() {
        return Proxy;
    }

    public int getProxyPort() {
        return ProxyPort;
    }
    public boolean isProxyAuth(){
        return login != null;
    }
    public String getProxyAuthHeader(){
        if(login != null){
            return "Proxy-Authorization: Basic " + login + "\n";
        }else{
            return null;
        }
    }
    public Socket getSocket(){
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
