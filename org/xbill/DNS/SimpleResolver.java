package org.xbill.DNS;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;

public class SimpleResolver implements Resolver {
    public static final int DEFAULT_EDNS_PAYLOADSIZE = 1280;
    public static final int DEFAULT_PORT = 53;
    private static final short DEFAULT_UDPSIZE = 512;
    private static String defaultResolver = "localhost";
    private static int uniqueID = 0;
    private InetSocketAddress address;
    private boolean ignoreTruncation;
    private InetSocketAddress localAddress;
    private OPTRecord queryOPT;
    private long timeoutValue;
    private TSIG tsig;
    private boolean useTCP;

    public SimpleResolver(String hostname) throws UnknownHostException {
        InetAddress addr;
        this.timeoutValue = 10000;
        if (hostname == null && (hostname = ResolverConfig.getCurrentConfig().server()) == null) {
            hostname = defaultResolver;
        }
        if (hostname.equals("0")) {
            addr = InetAddress.getLocalHost();
        } else {
            addr = InetAddress.getByName(hostname);
        }
        this.address = new InetSocketAddress(addr, 53);
    }

    public SimpleResolver() throws UnknownHostException {
        this((String) null);
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public static void setDefaultResolver(String hostname) {
        defaultResolver = hostname;
    }

    public void setPort(int port) {
        this.address = new InetSocketAddress(this.address.getAddress(), port);
    }

    public void setAddress(InetSocketAddress addr) {
        this.address = addr;
    }

    public void setAddress(InetAddress addr) {
        this.address = new InetSocketAddress(addr, this.address.getPort());
    }

    public void setLocalAddress(InetSocketAddress addr) {
        this.localAddress = addr;
    }

    public void setLocalAddress(InetAddress addr) {
        this.localAddress = new InetSocketAddress(addr, 0);
    }

    public void setTCP(boolean flag) {
        this.useTCP = flag;
    }

    public void setIgnoreTruncation(boolean flag) {
        this.ignoreTruncation = flag;
    }

    public void setEDNS(int level, int payloadSize, int flags, List options) {
        if (level == 0 || level == -1) {
            if (payloadSize == 0) {
                payloadSize = DEFAULT_EDNS_PAYLOADSIZE;
            }
            this.queryOPT = new OPTRecord(payloadSize, 0, level, flags, options);
            return;
        }
        throw new IllegalArgumentException("invalid EDNS level - must be 0 or -1");
    }

    public void setEDNS(int level) {
        setEDNS(level, 0, 0, (List) null);
    }

    public void setTSIGKey(TSIG key) {
        this.tsig = key;
    }

    /* access modifiers changed from: package-private */
    public TSIG getTSIGKey() {
        return this.tsig;
    }

    public void setTimeout(int secs, int msecs) {
        this.timeoutValue = (((long) secs) * 1000) + ((long) msecs);
    }

    public void setTimeout(int secs) {
        setTimeout(secs, 0);
    }

    /* access modifiers changed from: package-private */
    public long getTimeout() {
        return this.timeoutValue;
    }

    private Message parseMessage(byte[] b) throws WireParseException {
        try {
            return new Message(b);
        } catch (IOException e) {
            e = e;
            if (Options.check("verbose")) {
                e.printStackTrace();
            }
            if (!(e instanceof WireParseException)) {
                e = new WireParseException("Error parsing message");
            }
            throw ((WireParseException) e);
        }
    }

    private void verifyTSIG(Message query, Message response, byte[] b, TSIG tsig2) {
        if (tsig2 != null) {
            int error = tsig2.verify(response, b, query.getTSIG());
            if (Options.check("verbose")) {
                PrintStream printStream = System.err;
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("TSIG verify: ");
                stringBuffer.append(Rcode.TSIGstring(error));
                printStream.println(stringBuffer.toString());
            }
        }
    }

    private void applyEDNS(Message query) {
        if (this.queryOPT != null && query.getOPT() == null) {
            query.addRecord(this.queryOPT, 3);
        }
    }

    private int maxUDPSize(Message query) {
        OPTRecord opt = query.getOPT();
        if (opt == null) {
            return 512;
        }
        return opt.getPayloadSize();
    }

    public Message send(Message query) throws IOException {
        boolean tcp;
        byte[] in;
        Message response;
        Record question;
        if (Options.check("verbose")) {
            PrintStream printStream = System.err;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Sending to ");
            stringBuffer.append(this.address.getAddress().getHostAddress());
            stringBuffer.append(":");
            stringBuffer.append(this.address.getPort());
            printStream.println(stringBuffer.toString());
        }
        if (query.getHeader().getOpcode() == 0 && (question = query.getQuestion()) != null && question.getType() == 252) {
            return sendAXFR(query);
        }
        Message query2 = (Message) query.clone();
        applyEDNS(query2);
        TSIG tsig2 = this.tsig;
        if (tsig2 != null) {
            tsig2.apply(query2, (TSIGRecord) null);
        }
        byte[] out = query2.toWire((int) Message.MAXLENGTH);
        int udpSize = maxUDPSize(query2);
        boolean in2 = false;
        long endTime = System.currentTimeMillis() + this.timeoutValue;
        while (true) {
            if (this.useTCP || out.length > udpSize) {
                tcp = true;
            } else {
                tcp = in2;
            }
            if (tcp) {
                in = TCPClient.sendrecv(this.localAddress, this.address, out, endTime);
            } else {
                in = UDPClient.sendrecv(this.localAddress, this.address, out, udpSize, endTime);
            }
            if (in.length >= 12) {
                int id = ((in[0] & 255) << 8) + (in[1] & 255);
                int qid = query2.getHeader().getID();
                if (id != qid) {
                    StringBuffer stringBuffer2 = new StringBuffer();
                    stringBuffer2.append("invalid message id: expected ");
                    stringBuffer2.append(qid);
                    stringBuffer2.append("; got id ");
                    stringBuffer2.append(id);
                    String error = stringBuffer2.toString();
                    if (!tcp) {
                        if (Options.check("verbose")) {
                            System.err.println(error);
                        }
                        in2 = tcp;
                    } else {
                        throw new WireParseException(error);
                    }
                } else {
                    response = parseMessage(in);
                    verifyTSIG(query2, response, in, this.tsig);
                    if (tcp || this.ignoreTruncation || !response.getHeader().getFlag(6)) {
                        return response;
                    }
                    in2 = true;
                }
            } else {
                throw new WireParseException("invalid DNS header - too short");
            }
        }
        return response;
    }

    public Object sendAsync(Message query, ResolverListener listener) {
        Object id;
        String qname;
        synchronized (this) {
            int i = uniqueID;
            uniqueID = i + 1;
            id = new Integer(i);
        }
        Record question = query.getQuestion();
        if (question != null) {
            qname = question.getName().toString();
        } else {
            qname = "(none)";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getClass());
        stringBuffer.append(": ");
        stringBuffer.append(qname);
        String name = stringBuffer.toString();
        Thread thread = new ResolveThread(this, query, id, listener);
        thread.setName(name);
        thread.setDaemon(true);
        thread.start();
        return id;
    }

    private Message sendAXFR(Message query) throws IOException {
        ZoneTransferIn xfrin = ZoneTransferIn.newAXFR(query.getQuestion().getName(), (SocketAddress) this.address, this.tsig);
        xfrin.setTimeout((int) (getTimeout() / 1000));
        xfrin.setLocalAddress(this.localAddress);
        try {
            xfrin.run();
            List<Record> records = xfrin.getAXFR();
            Message response = new Message(query.getHeader().getID());
            response.getHeader().setFlag(5);
            response.getHeader().setFlag(0);
            response.addRecord(query.getQuestion(), 0);
            for (Record addRecord : records) {
                response.addRecord(addRecord, 1);
            }
            return response;
        } catch (ZoneTransferException e) {
            throw new WireParseException(e.getMessage());
        }
    }
}
