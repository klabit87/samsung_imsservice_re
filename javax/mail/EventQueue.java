package javax.mail;

import java.util.Vector;
import javax.mail.event.MailEvent;

class EventQueue implements Runnable {
    private QueueElement head = null;
    private Thread qThread;
    private QueueElement tail = null;

    static class QueueElement {
        MailEvent event = null;
        QueueElement next = null;
        QueueElement prev = null;
        Vector vector = null;

        QueueElement(MailEvent event2, Vector vector2) {
            this.event = event2;
            this.vector = vector2;
        }
    }

    public EventQueue() {
        Thread thread = new Thread(this, "JavaMail-EventQueue");
        this.qThread = thread;
        thread.setDaemon(true);
        this.qThread.start();
    }

    public synchronized void enqueue(MailEvent event, Vector vector) {
        QueueElement newElt = new QueueElement(event, vector);
        if (this.head == null) {
            this.head = newElt;
            this.tail = newElt;
        } else {
            newElt.next = this.head;
            this.head.prev = newElt;
            this.head = newElt;
        }
        notifyAll();
    }

    private synchronized QueueElement dequeue() throws InterruptedException {
        QueueElement elt;
        while (this.tail == null) {
            wait();
        }
        elt = this.tail;
        QueueElement queueElement = elt.prev;
        this.tail = queueElement;
        if (queueElement == null) {
            this.head = null;
        } else {
            queueElement.next = null;
        }
        elt.next = null;
        elt.prev = null;
        return elt;
    }

    public void run() {
        while (true) {
            try {
                QueueElement dequeue = dequeue();
                QueueElement qe = dequeue;
                if (dequeue != null) {
                    MailEvent e = qe.event;
                    Vector v = qe.vector;
                    for (int i = 0; i < v.size(); i++) {
                        try {
                            e.dispatch(v.elementAt(i));
                        } catch (Throwable t) {
                            if (t instanceof InterruptedException) {
                                return;
                            }
                        }
                    }
                } else {
                    return;
                }
            } catch (InterruptedException e2) {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stop() {
        Thread thread = this.qThread;
        if (thread != null) {
            thread.interrupt();
            this.qThread = null;
        }
    }
}
