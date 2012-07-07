package hadukiserver;

import hadukiserver.core.Server;
import java.util.Properties;
import java.io.*;

/**
 * <p>�^�C�g��: �u�͂Â��v�T�[�o</p>
 *
 * <p>����: �u�͂Â��v�̃T�[�o�ł��B</p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class ServerRunner {
    private int Port;
    private int Connection;
    public ServerRunner() {
        Properties prop = new Properties();
        try {
            prop.loadFromXML(new FileInputStream("conf.xml"));
        } catch (IOException ex) {
        }
        Port = Integer.parseInt(prop.getProperty("port", "44444"));
        Connection = Integer.parseInt(prop.getProperty("max_connection", "10"));
    }

    public ServerRunner(int port, int connection) {
        this();
        Port = port;
        Connection = connection;
    }

    public ServerRunner(String args[]) {
        this();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.startsWith("-p")) {
                Port = Integer.parseInt(arg.substring(2));
            } else if (arg.startsWith("-c")) {
                Connection = Integer.parseInt(arg.substring(2));
            }
        }
    }

    public Server runServer() {
        Server serv = new Server(Port, Connection);
        serv.start();
        return serv;
    }
}
