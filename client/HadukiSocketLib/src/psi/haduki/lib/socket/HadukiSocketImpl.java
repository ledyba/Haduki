package psi.haduki.lib.socket;

import java.io.*;
import java.net.*;
import psi.haduki.lib.ChannelSet;

/**
 * <p>�^�C�g��: �u�͂Â��v�ėp�\�P�b�g���C�u����</p>
 *
 * <p>����: Socket�N���X�̃T�u�N���X�Ƃ��Ď��������ėp���C�u����</p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class HadukiSocketImpl extends SocketImpl {
    ChannelSet Channel;
    public HadukiSocketImpl() {
        super();
    }

    /**
     * Accepts a connection.
     *
     * @param s the accepted connection.
     * @throws IOException if an I/O error occurs when accepting the
     *   connection.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void accept(SocketImpl s) throws IOException {
        throw new IOException("�u�͂Â��v�ł͌��ݑΉ����Ă��܂���B");
    }

    /**
     * Returns the number of bytes that can be read from this socket without
     * blocking.
     *
     * @return the number of bytes that can be read from this socket without
     *   blocking.
     * @throws IOException if an I/O error occurs when determining the
     *   number of bytes available.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected int available() throws IOException {
        return 0;
    }

    /**
     * Binds this socket to the specified local IP address and port number.
     *
     * @param host an IP address that belongs to a local interface.
     * @param port the port number.
     * @throws IOException if an I/O error occurs when binding this socket.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void bind(InetAddress host, int port) throws IOException {
    }

    /**
     * Closes this socket.
     *
     * @throws IOException if an I/O error occurs when closing this socket.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void close() throws IOException {
    }

    /**
     * Connects this socket to the specified port number on the specified
     * host.
     *
     * @param address the IP address of the remote host.
     * @param port the port number.
     * @throws IOException if an I/O error occurs when attempting a
     *   connection.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void connect(InetAddress address, int port) throws IOException {
    }

    /**
     * Connects this socket to the specified port on the named host.
     *
     * @param host the name of the remote host.
     * @param port the port number.
     * @throws IOException if an I/O error occurs when connecting to the
     *   remote host.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void connect(String host, int port) throws IOException {
    }

    /**
     * Connects this socket to the specified port number on the specified
     * host.
     *
     * @param address the Socket address of the remote host.
     * @param timeout the timeout value, in milliseconds, or zero for no
     *   timeout.
     * @throws IOException if an I/O error occurs when attempting a
     *   connection.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void connect(SocketAddress address, int timeout) throws
            IOException {
    }

    /**
     * Creates either a stream or a datagram socket.
     *
     * @param stream if <code>true</code>, create a stream socket;
     *   otherwise, create a datagram socket.
     * @throws IOException if an I/O error occurs while creating the socket.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void create(boolean stream) throws IOException {
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return a stream for reading from this socket.
     * @throws IOException if an I/O error occurs when creating the input
     *   stream.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected InputStream getInputStream() throws IOException {
        return null;
    }

    /**
     * Fetch the value of an option.
     *
     * @param optID an <code>int</code> identifying the option to fetch
     * @return the value of the option
     * @throws SocketException if <I>optID</I> is unknown along the protocol
     *   stack (including the SocketImpl)
     * @todo ���� java.net.SocketOptions ���\�b�h������
     */
    public Object getOption(int optID) throws SocketException {
        return null;
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return an output stream for writing to this socket.
     * @throws IOException if an I/O error occurs when creating the output
     *   stream.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected OutputStream getOutputStream() throws IOException {
        return null;
    }

    /**
     * Sets the maximum queue length for incoming connection indications (a
     * request to connect) to the <code>count</code> argument.
     *
     * @param backlog the maximum length of the queue.
     * @throws IOException if an I/O error occurs when creating the queue.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void listen(int backlog) throws IOException {
    }

    /**
     * Send one byte of urgent data on the socket.
     *
     * @param data The byte of data to send
     * @throws IOException if there is an error sending the data.
     * @todo ���� java.net.SocketImpl ���\�b�h������
     */
    protected void sendUrgentData(int data) throws IOException {
    }

    /**
     * Enable/disable the option specified by <I>optID</I>.
     *
     * @param optID identifies the option
     * @param value the parameter of the socket option
     * @throws SocketException if the option is unrecognized, the socket is
     *   closed, or some low-level error occurred
     * @todo ���� java.net.SocketOptions ���\�b�h������
     */
    public void setOption(int optID, Object value) throws SocketException {
    }
}
