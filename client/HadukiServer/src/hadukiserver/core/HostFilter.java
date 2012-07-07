package hadukiserver.core;

import java.net.InetAddress;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.*;
import java.util.Hashtable;

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
public class HostFilter {
    private Hashtable<InetAddress, Boolean>
            FilterTable = new Hashtable<InetAddress, Boolean>();
    private boolean DenyAll = false;
    private boolean AllowAll = false;
    public HostFilter() {
        this("allow.txt", "deny.txt");
    }

    public HostFilter(String allow_file, String deny_file) {
        File allow = new File(allow_file);
        File deny = new File(deny_file);
        InputStream a_is = null;
        InputStream d_is = null;
        try {
            if (allow.canRead()) {
                a_is = new FileInputStream(allow);
            }
            if (deny.canRead()) {
                d_is = new FileInputStream(deny);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        initialize(a_is, d_is);
    }

    public HostFilter(InputStream allow, InputStream deny) {
        initialize(allow, deny);
    }

    private void initialize(InputStream allow, InputStream deny) {
        initializeAllowList(allow);
        initializeDenyList(deny);
        Logger.debug("Initialized Host Filtering System.");
    }

    private void initializeAllowList(InputStream is) {
        if(is == null){
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().equals("all")) {
                    AllowAll = true;
                    break;
                }
                FilterTable.put(InetAddress.getByName(line), Boolean.TRUE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    private void initializeDenyList(InputStream is) {
        if(is == null){
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().equals("all")) {
                    DenyAll = true;
                    break;
                }
                FilterTable.put(InetAddress.getByName(line), Boolean.FALSE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public boolean check(InetAddress addr) {
        if (AllowAll) {
            if (Logger.isDebug()) {
                Logger.debug(addr.getHostName() + " is allowed.[AllowAll]");
            }
            return true;
        }
        Boolean Value = FilterTable.get(addr);
        if (Value != null && Value.booleanValue()) {
            if (Logger.isDebug()) {
                Logger.debug(addr.getHostName() + " is allowed.[AllowList]");
            }
            return true;
        }
        if (DenyAll) {
            if (Logger.isDebug()) {
                Logger.debug(addr.getHostName() + " is denied.[DenyAll]");
            }
            return false;
        }
        if (Value != null && !Value.booleanValue()) {
            if (Logger.isDebug()) {
                Logger.debug(addr.getHostName() + " is denied.[DenyList]");
            }
            return false;
        }
        if (Logger.isDebug()) {
            Logger.debug(addr.getHostName() + " is allowed.[NoRule]");
        }
        return true;
    }
}
