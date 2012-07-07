package psi.haduki.lib.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.net.SocketAddress;
import java.net.Socket;
import java.net.SocketImplFactory;

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
public class HadukiServerSocket extends ServerSocket {
    public HadukiServerSocket() throws IOException {
        super();
    }

    public HadukiServerSocket(int port) throws IOException {
        super(port);
    }

    public HadukiServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    public HadukiServerSocket(int port, int backlog, InetAddress bindAddr) throws
            IOException {
        super(port, backlog, bindAddr);
    }

    public Socket accept() {
        return null;
    }

    public void bind(SocketAddress endpoint) {
    }

    public void bind(SocketAddress endpoint, int backlog) {
    }

    public void close() {
    }

    public ServerSocketChannel getChannel() {
        return null;
    }

    public InetAddress getInetAddress() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }

    public SocketAddress getLocalSocketAddress() {
        return null;
    }

    public int getReceiveBufferSize() {
        return 0;
    }

    public boolean getReuseAddress() {
        return false;
    }

    public int getSoTimeout() {
        return 0;
    }
    /*
    protected void implAccept(Socket s) {
    }*/

    public boolean isBound() {
        return false;
    }

    public boolean isClosed() {
        return false;
    }

    public void setPerformancePreferences(int connectionTime, int latency,
                                          int bandwidth) {
    }

    public void setReceiveBufferSize(int size) {
    }

    public void setReuseAddress(boolean on) {
    }

    public static void setSocketFactory(SocketImplFactory fac) throws IOException{
        throw new IOException("このメソッドは使用不可能です。HadukiManagerを利用してください。");
    }

    public void setSoTimeout(int timeout) {
    }

    public String toString() {
        return "HadukiServerSocket";
    }
}
