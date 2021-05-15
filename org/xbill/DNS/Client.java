package org.xbill.DNS;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import org.xbill.DNS.utils.hexdump;

class Client {
    private static PacketLogger packetLogger = null;
    protected long endTime;
    protected SelectionKey key;

    protected Client(SelectableChannel channel, long endTime2) throws IOException {
        boolean done = false;
        Selector selector = null;
        this.endTime = endTime2;
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            this.key = channel.register(selector, 1);
            done = true;
        } finally {
            if (!done && selector != null) {
                selector.close();
            }
            if (!done) {
                channel.close();
            }
        }
    }

    protected static void blockUntil(SelectionKey key2, long endTime2) throws IOException {
        long timeout = endTime2 - System.currentTimeMillis();
        int nkeys = 0;
        if (timeout > 0) {
            nkeys = key2.selector().select(timeout);
        } else if (timeout == 0) {
            nkeys = key2.selector().selectNow();
        }
        if (nkeys == 0) {
            throw new SocketTimeoutException();
        }
    }

    protected static void verboseLog(String prefix, SocketAddress local, SocketAddress remote, byte[] data) {
        if (Options.check("verbosemsg")) {
            System.err.println(hexdump.dump(prefix, data));
        }
        PacketLogger packetLogger2 = packetLogger;
        if (packetLogger2 != null) {
            packetLogger2.log(prefix, local, remote, data);
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanup() throws IOException {
        this.key.selector().close();
        this.key.channel().close();
    }

    static void setPacketLogger(PacketLogger logger) {
        packetLogger = logger;
    }
}
