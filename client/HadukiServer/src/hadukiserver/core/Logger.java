package hadukiserver.core;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class Logger {
    private static BufferedWriter log;
    public static final int DEBUG_LEVEL_DEBUG = 10;
    public static final int DEBUG_LEVEL_INFO = 5;
    public static final int DEBUG_LEVEL_ERROR = 0;
    private static int DebugLevel = DEBUG_LEVEL_INFO;
    private static SimpleDateFormat Format;
    static {
        try {
            log = new BufferedWriter(new FileWriter("log.txt", true));
            Format = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static boolean isDebug(){
        return DebugLevel < DEBUG_LEVEL_DEBUG;
    }
    public static void debug(String str) {
        if(DebugLevel < DEBUG_LEVEL_DEBUG){
            return;
        }
        synchronized (log) {
            try {
                log.write("[DEBUG]"+Format.format(new Date())+str);
                log.newLine();
                log.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void info(String str) {
        if(DebugLevel < DEBUG_LEVEL_INFO){
            return;
        }
        synchronized (log) {
            try {
                log.write("[INFO]"+Format.format(new Date())+str);
                log.newLine();
                log.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void error(String str) {
        if(DebugLevel < DEBUG_LEVEL_ERROR){
            return;
        }
        synchronized (log) {
            try {
                log.write("[ERROR]"+Format.format(new Date())+str);
                log.newLine();
                log.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static void setDebugLevel(int level){
        DebugLevel = level;
    }
}
