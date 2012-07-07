package psi.haduki.lib.socket;

import java.io.IOException;
import java.net.*;
import java.nio.channels.SocketChannel;
import org.omg.CORBA.portable.InputStream;
import java.io.OutputStream;

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
public class HadukiSocket extends Socket {
    public HadukiSocket() {
        super();
    }

    public HadukiSocket(Proxy proxy) {
        super(proxy);
    }

    protected HadukiSocket(SocketImpl impl) throws SocketException {
        super(impl);
    }

    public HadukiSocket(String host, int port) throws UnknownHostException,
            IOException {
        super(host, port);
    }

    public HadukiSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    public HadukiSocket(String host, int port, InetAddress localAddr,
                        int localPort) throws IOException {
        super(host, port, localAddr, localPort);
    }

    public HadukiSocket(InetAddress address, int port, InetAddress localAddr,
                        int localPort) throws IOException {
        super(address, port, localAddr, localPort);
    }

    public HadukiSocket(String host, int port, boolean stream) throws
            IOException {
        super(host, port, stream);
    }

    public HadukiSocket(InetAddress host, int port, boolean stream) throws
            IOException {
        super(host, port, stream);
    }

    public void bind(SocketAddress bindpoint) {

    }

    public void close() {

    }

    public void connect(SocketAddress endpoint) {

    }

    public void connect(SocketAddress endpoint, int timeout) {

    }

    public SocketChannel getChannel() {
        return null;
    }

    public InetAddress getInetAddress() {
        return null;
    }

    public InputStream getInputStream() {
        return null;
    }

    public boolean getKeepAlive() {
        return false;
    }

    public InetAddress getLocalAddress() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }

    public SocketAddress getLocalSocketAddress() {
        return null;
    }

    public boolean getOOBInline() {
        return false;
    }

    public OutputStream getOutputStream() {
        return null;
    }

    public int getPort() {
        return 0;
    }

    public int getReceiveBufferSize() {
        return 0;
    }

    public SocketAddress getRemoteSocketAddress() {
        return null;
    }

    public boolean getReuseAddress() {
        return false;
    }

    public int getSendBufferSize() {
        return 0;
    }

    public int getSoLinger() {
        return 0;
    }

    public int getSoTimeout() {
        return 0;
    }

    public boolean getTcpNoDelay() {
        return false;
    }

    public int getTrafficClass() {
        return 0;
    }

    public boolean isBound() {
        return false;
    }

    public boolean isClosed() {
        return false;
    }

    public boolean isConnected() {
        return false;
    }

    public boolean isInputShutdown() {
        return false;
    }

    public boolean isOutputShutdown() {
        return false;
    }

    public void sendUrgentData(int data) {

    }

    public void setKeepAlive(boolean on) {

    }

    public void setOOBInline(boolean on) {

    }

    public void setReceiveBufferSize(int size) {

    }

    public void setReuseAddress(boolean on) {

    }

    public void setSendBufferSize(int size) {

    }

    public static void setSocketImplFactory(SocketImplFactory fac) throws
            IOException {
        throw new IOException("このメソッドは使用不可能です。HadukiManagerを利用してください。");
    }

    public void setSoLinger(boolean on, int linger) {

    }

    public void setSoTimeout(int timeout) {

    }

    public void setTcpNoDelay(boolean on) {

    }

    public void setTrafficClass(int tc) {

    }

    public void shutdownInput() {

    }

    public void shutdownOutput() {

    }

    public String toString() {
        return "HadukiSocket";
    }
}
