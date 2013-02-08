package hadukiclient.serv_connection;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.util.concurrent.locks.Lock;

/**
 * <p>�^�C�g��: �u�t���v</p>
 *
 * <p>����: �N���C�A���g����t���ւ́A���N�G�X�g��\���܂��B</p>
 *
 * <p>���쌠: Copyright (c) 2007 PSI</p>
 *
 * <p>��Ж�: </p>
 *
 * @author ������
 * @version 1.0
 */
public class Request {
    public static final int BUFF_SIZE = 4096;
    private LoginInfo LoginInfo;
    //���M
    private byte[] Data;
    //�X�g���[��
    private PipedInputStream RIS = new PipedInputStream();
    private PipedOutputStream ROS = new PipedOutputStream();
    //���U���g
    private int ResultCode = -1;
    //�R���X�g���N�^
    public Request(LoginInfo info, int action_code, short host_port,
                   String host, byte[] data) {
        init(info, action_code, host_port, host, data);
    }

    public Request(LoginInfo info, int action_code) {
        init(info, action_code, (short) 0, null, null);
    }

    //������
    private void init(LoginInfo info, int action_code, short host_port,
                      String host, byte[] data) {
        LoginInfo = info;
        //��M�p�o�b�t�@�̏���
        try {
            RIS.connect(ROS);
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        //�X�g���[���̏���
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFF_SIZE);
        DataOutputStream dos = new DataOutputStream(baos);
        //�f�[�^�̍쐬
        try {
            dos.writeInt(LoginInfo.getUserID());
            dos.writeInt(action_code);
            dos.write(LoginInfo.getPassword());
            dos.writeInt(LoginInfo.getSessionID());
            dos.writeInt(host == null ? 0 : host.length());
            if (host != null) {
                dos.write(host.getBytes());
                dos.writeShort(host_port);
                if (data == null) {
                    throw new IOException("�z�X�g�͂���̂Ƀf�[�^������");
                }
            }
            dos.writeInt(data == null ? 0 : data.length);
            if (data != null) {
                dos.write(data, 0, data.length);
            }
            dos.flush();
            baos.flush();
            Data = baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //���M�f�[�^
    public byte[] getSendingData() {
        //�Í���
        LoginInfo.getCrypt().outputData(Data, 8, Data.length - 8);
        return Data;
    }

    //��M�f�[�^�̎󂯎��̓X�g���[���ōs��
    public OutputStream getRecvOutputStream() {
        return ROS;
    }

    //���U���g��M
    public int getResultCode() {
        if (!getReceived()) {
            return -1;
        }
        return ResultCode;
    }

    //�����������f�[�^���󂯎��
    public InputStream getRecvInputStream() {
        if (!getReceived()) {
            return null;
        }
        return RIS;
    }

    private final Lock ReceivedLock = new ReentrantLock();
    private final Condition ReceivedCond = ReceivedLock.newCondition();
    private boolean ReceivedDecided = false;
    private boolean Received = false;

    public synchronized void signalReceived(boolean flag) {
        ReceivedLock.lock();
        if (flag == true) {
            try {
                DataInputStream dis = new DataInputStream(RIS);
                ResultCode = dis.readInt();
                Received = true;
            } catch (IOException ex) {
                ex.printStackTrace();
                Received = false;
            }
        } else {
            Received = false;
        }
        ReceivedDecided = true;
        ReceivedCond.signalAll();
        ReceivedLock.unlock();
    }

    public boolean getReceived() {
        ReceivedLock.lock();
        if (ReceivedDecided) {
            ReceivedLock.unlock();
            return Received;
        }
        try {
            ReceivedCond.await();
        } catch (InterruptedException ex1) {
            ex1.printStackTrace();
        } finally {
            ReceivedLock.unlock();
        }
        return Received;
    }

    private final Lock ConnectedLock = new ReentrantLock();
    private final Condition ConnectedCond = ConnectedLock.newCondition();
    private boolean Connected = false;
    private boolean ConnectedDecided = false;
    public synchronized void setConnected(boolean flag) {
        ConnectedLock.lock();
        Connected = flag;
        ConnectedDecided = true;
        ConnectedCond.signalAll();
        ConnectedLock.unlock();
    }

    public boolean getConnected() {
        ConnectedLock.lock();
        if (ConnectedDecided) {
            ConnectedLock.unlock();
            return Connected;
        }
        try {
            ConnectedCond.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            ConnectedLock.unlock();
        }
        return Connected;
    }

    public static boolean strcmp_start(final char[] a, final char[] b) {
        if (a.length < b.length) {
            return false;
        }
        int min = b.length;
        for (int i = 0; i < min; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean strcmp_end(final char[] a, final char[] b) {
        if (a.length < b.length) {
            return false;
        }
        int a_l = a.length;
        int b_l = b.length;
        for (int i = 1; i <= b_l; i++) {
            if (a[a_l - i] != b[b_l - i]) {
                return false;
            }
        }
        return true;
    }
}
