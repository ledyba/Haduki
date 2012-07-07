package hadukiclient.serv_connection;

import java.net.*;
import java.io.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;

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
    private Credentials Cred;
    private String Host;
    private int Port;
    public HTTP_Proxy(final String proxy, int proxy_port, final String user,
                      final String pass) {
        if (proxy != null && !proxy.equals("") && proxy_port >= 0 &&
            proxy_port <= 0xffff) { //プロキシ必要
            Host = proxy;
            Port = proxy_port;
            //プロキシ認証必要？
            if (user == null || pass == null || user.equals("") ||
                pass.equals("")) {
                Cred = null;
            } else {
                int idx = user.indexOf("\\");
                if (idx >= 0) { //NTLM
                    String localhost = "";
                    //ローカルホスト取得
                    try {
                        localhost = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException ex) {
                        ex.printStackTrace();
                    }
                    //NTLMの書き換え。
                    AuthPolicy.unregisterAuthScheme(AuthPolicy.NTLM);
                    AuthPolicy.registerAuthScheme(AuthPolicy.NTLM,
                                                  OriginalNTLMScheme.class);
                    //Cred
                    Cred = new NTCredentials(user.substring(idx + 1), pass,
                                             localhost, user.substring(0, idx));
                } else { //普通
                    Cred = new UsernamePasswordCredentials(user, pass);
                }

            }
        } else {
            Port = -1;
        }
    }

    public void setConfig(HttpClient client) {
        if (Port >= 0) {
            HostConfiguration hostConfig = new HostConfiguration();
            hostConfig.setProxy(Host, Port);
            client.setHostConfiguration(hostConfig);
            if (Cred != null) { //登録
                client.getState().setProxyCredentials(
                        new AuthScope(Host, Port, null, AuthScope.ANY_SCHEME),
                        Cred
                        );
                OriginalNTLMScheme sh = new OriginalNTLMScheme();
                try {
                    sh.processChallenge("NTLM");
                    System.out.println(sh.authenticate(Cred, null));
                    sh.processChallenge("NTLM TlRMTVNTUAACAAAACwALADgAAAAGAoECw3qCm6QhatAAAAAAAAAAAIQAhABDAAAABQCTCAAAAA9NRURJQUNFTlRFUgIAFgBNAEUARABJAEEAQwBFAE4AVABFAFIAAQAKAFMAVABTAFIAVgAEACIAbQBlAGQAaQBhAGMAZQBuAHQAZQByAC4AbABvAGMAYQBsAAMALgBzAHQAcwByAHYALgBtAGUAZABpAGEAYwBlAG4AdABlAHIALgBsAG8AYwBhAGwAAAAAAA==");
                    System.out.println(sh.authenticate(Cred, null));
                } catch (AuthenticationException ex) {
                    ex.printStackTrace();
                } catch (MalformedChallengeException ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    public boolean isCollect() {
        return Port >= 0;
    }

    public boolean isAuth() {
        return Cred != null;
    }
}
