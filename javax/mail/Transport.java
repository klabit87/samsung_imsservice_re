package javax.mail;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public abstract class Transport extends Service {
    private Vector transportListeners = null;

    public abstract void sendMessage(Message message, Address[] addressArr) throws MessagingException;

    public Transport(Session session, URLName urlname) {
        super(session, urlname);
    }

    public static void send(Message msg) throws MessagingException {
        msg.saveChanges();
        send0(msg, msg.getAllRecipients());
    }

    public static void send(Message msg, Address[] addresses) throws MessagingException {
        msg.saveChanges();
        send0(msg, addresses);
    }

    private static void send0(Message msg, Address[] addresses) throws MessagingException {
        Session session;
        Message message = msg;
        Address[] addressArr = addresses;
        if (addressArr == null || addressArr.length == 0) {
            throw new SendFailedException("No recipient addresses");
        }
        Hashtable protocols = new Hashtable();
        Vector invalid = new Vector();
        Vector validSent = new Vector();
        Vector validUnsent = new Vector();
        int i = 0;
        while (i < addressArr.length) {
            if (protocols.containsKey(addressArr[i].getType())) {
                ((Vector) protocols.get(addressArr[i].getType())).addElement(addressArr[i]);
            } else {
                Vector w = new Vector();
                w.addElement(addressArr[i]);
                protocols.put(addressArr[i].getType(), w);
            }
            i++;
            message = msg;
        }
        int i2 = protocols.size();
        if (i2 != 0) {
            Address[] addressArr2 = null;
            if (message.session != null) {
                session = message.session;
            } else {
                session = Session.getDefaultInstance(System.getProperties(), (Authenticator) null);
            }
            Session s = session;
            char c = 0;
            if (i2 == 1) {
                Transport transport = s.getTransport(addressArr[0]);
                try {
                    transport.connect();
                    transport.sendMessage(message, addressArr);
                } finally {
                    transport.close();
                }
            } else {
                Enumeration e = protocols.elements();
                boolean sendFailed = false;
                MessagingException chainedEx = null;
                while (e.hasMoreElements()) {
                    Vector v = (Vector) e.nextElement();
                    Address[] protaddresses = new Address[v.size()];
                    v.copyInto(protaddresses);
                    Transport transport2 = s.getTransport(protaddresses[c]);
                    Transport transport3 = transport2;
                    if (transport2 == null) {
                        for (Address addElement : protaddresses) {
                            invalid.addElement(addElement);
                        }
                    } else {
                        try {
                            transport3.connect();
                            transport3.sendMessage(message, protaddresses);
                            transport3.close();
                        } catch (SendFailedException e2) {
                            sex = e2;
                            sendFailed = true;
                            if (chainedEx == null) {
                                chainedEx = sex;
                            } else {
                                chainedEx.setNextException(sex);
                            }
                            Address[] a = sex.getInvalidAddresses();
                            if (a != null) {
                                int j = 0;
                                while (j < a.length) {
                                    invalid.addElement(a[j]);
                                    j++;
                                    Message message2 = msg;
                                }
                            }
                            Address[] a2 = sex.getValidSentAddresses();
                            if (a2 != null) {
                                for (Address addElement2 : a2) {
                                    validSent.addElement(addElement2);
                                }
                            }
                            Address[] c2 = sex.getValidUnsentAddresses();
                            if (c2 != null) {
                                int l = 0;
                                while (true) {
                                    SendFailedException sex = sex;
                                    if (l >= c2.length) {
                                        break;
                                    }
                                    validUnsent.addElement(c2[l]);
                                    l++;
                                    sex = sex;
                                }
                            }
                            transport3.close();
                            addressArr2 = null;
                            c = 0;
                            message = msg;
                        } catch (MessagingException mex) {
                            sendFailed = true;
                            if (chainedEx == null) {
                                chainedEx = mex;
                            } else {
                                chainedEx.setNextException(mex);
                            }
                            transport3.close();
                            addressArr2 = null;
                        } catch (Throwable th) {
                            transport3.close();
                            throw th;
                        }
                    }
                    addressArr2 = null;
                }
                if (sendFailed || invalid.size() != 0 || validUnsent.size() != 0) {
                    Address[] a3 = addressArr2;
                    Address[] b = addressArr2;
                    Address[] c3 = addressArr2;
                    if (validSent.size() > 0) {
                        a3 = new Address[validSent.size()];
                        validSent.copyInto(a3);
                    }
                    if (validUnsent.size() > 0) {
                        b = new Address[validUnsent.size()];
                        validUnsent.copyInto(b);
                    }
                    if (invalid.size() > 0) {
                        c3 = new Address[invalid.size()];
                        invalid.copyInto(c3);
                    }
                    throw new SendFailedException("Sending failed", chainedEx, a3, b, c3);
                }
            }
        } else {
            throw new SendFailedException("No recipient addresses");
        }
    }

    public synchronized void addTransportListener(TransportListener l) {
        if (this.transportListeners == null) {
            this.transportListeners = new Vector();
        }
        this.transportListeners.addElement(l);
    }

    public synchronized void removeTransportListener(TransportListener l) {
        if (this.transportListeners != null) {
            this.transportListeners.removeElement(l);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg) {
        if (this.transportListeners != null) {
            queueEvent(new TransportEvent(this, type, validSent, validUnsent, invalid, msg), this.transportListeners);
        }
    }
}
