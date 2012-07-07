package hadukiclient.serv_connection;

import java.util.List;
import java.util.LinkedList;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>タイトル: 「葉月」</p>
 *
 * <p>説明: HTTPの要求を行うキューです。</p>
 *
 * <p>著作権: Copyright (c) 2007 PSI</p>
 *
 * <p>会社名: </p>
 *
 * @author 未入力
 * @version 1.0
 */
public class RequestQueue extends AbstractQueue<Request> {
    private List<Request> ReqList = new LinkedList<Request>();
    private final Lock Lock = new ReentrantLock();
    private final Condition Cond = Lock.newCondition();
    public RequestQueue() {
    }

    public Iterator<Request> iterator() {
        synchronized (ReqList) {
            return ReqList.iterator();
        }
    }

    public int size() {
        synchronized (ReqList) {
            return ReqList.size();
        }
    }

    public boolean equals(Object o) {
        return false;
    }

    public int hashCode() {
        return 0;
    }

    public Request element() {
        synchronized (ReqList) {
            if (size() <= 0) {
                return null;
            }
            return (Request) ReqList.get(0);
        }
    }

    public boolean offer(Request o) {
        synchronized (ReqList) {
            ReqList.add(o);
            change_signal(true);
            return true;
        }
    }

    public boolean change_wait(boolean must_offered) {
        Lock.lock();
        try {
            if (this.size() <= 0) {
                Cond.await();
                if (!Offered && must_offered) change_wait(true);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            Lock.unlock();
        }
        return Offered;
    }
    private boolean Offered = false;
    public void change_signal(boolean offered) {
        Offered = offered;
        Lock.lock();
        Cond.signalAll();
        Lock.unlock();
    }

    public Request peek() {
        synchronized (ReqList) {
            if (size() <= 0) {
                return null;
            }
            return (Request) ReqList.get(0);
        }
    }

    public Request poll() {
        synchronized (ReqList) {
            if (size() <= 0) {
                return null;
            }
            Request ret = (Request) ReqList.get(0);
            ReqList.remove(0);
            change_signal(true);
            return ret;
        }
    }

    public Request remove() {
        synchronized (ReqList) {
            if (size() <= 0) {
                return null;
            }
            Request ret = (Request) ReqList.get(0);
            ReqList.remove(0);
            change_signal(true);
            return ret;
        }
    }
}
