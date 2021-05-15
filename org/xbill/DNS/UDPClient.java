package org.xbill.DNS;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;

final class UDPClient extends Client {
    private static final int EPHEMERAL_RANGE = 64511;
    private static final int EPHEMERAL_START = 1024;
    private static final int EPHEMERAL_STOP = 65535;
    /* access modifiers changed from: private */
    public static SecureRandom prng = new SecureRandom();
    /* access modifiers changed from: private */
    public static volatile boolean prng_initializing = true;
    private boolean bound = false;

    static {
        new Thread(new Runnable() {
            public void run() {
                int nextInt = UDPClient.prng.nextInt();
                boolean unused = UDPClient.prng_initializing = false;
            }
        }).start();
    }

    public UDPClient(long endTime) throws IOException {
        super(DatagramChannel.open(), endTime);
    }

    private void bind_random(InetSocketAddress addr) throws IOException {
        InetSocketAddress temp;
        if (prng_initializing) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
            }
            if (prng_initializing) {
                return;
            }
        }
        DatagramChannel channel = (DatagramChannel) this.key.channel();
        int i = 0;
        while (i < 1024) {
            try {
                int port = prng.nextInt(EPHEMERAL_RANGE) + 1024;
                if (addr != null) {
                    temp = new InetSocketAddress(addr.getAddress(), port);
                } else {
                    temp = new InetSocketAddress(port);
                }
                channel.socket().bind(temp);
                this.bound = true;
                return;
            } catch (SocketException e2) {
                i++;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void bind(SocketAddress addr) throws IOException {
        if (addr == null || ((addr instanceof InetSocketAddress) && ((InetSocketAddress) addr).getPort() == 0)) {
            bind_random((InetSocketAddress) addr);
            if (this.bound) {
                return;
            }
        }
        if (addr != null) {
            ((DatagramChannel) this.key.channel()).socket().bind(addr);
            this.bound = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void connect(SocketAddress addr) throws IOException {
        if (!this.bound) {
            bind((SocketAddress) null);
        }
        ((DatagramChannel) this.key.channel()).connect(addr);
    }

    /* access modifiers changed from: package-private */
    public void send(byte[] data) throws IOException {
        DatagramChannel channel = (DatagramChannel) this.key.channel();
        verboseLog("UDP write", channel.socket().getLocalSocketAddress(), channel.socket().getRemoteSocketAddress(), data);
        channel.write(ByteBuffer.wrap(data));
    }

    /* access modifiers changed from: package-private */
    public byte[] recv(int max) throws IOException {
        DatagramChannel channel = (DatagramChannel) this.key.channel();
        byte[] temp = new byte[max];
        this.key.interestOps(1);
        while (!this.key.isReadable()) {
            try {
                blockUntil(this.key, this.endTime);
            } finally {
                if (this.key.isValid()) {
                    this.key.interestOps(0);
                }
            }
        }
        long ret = (long) channel.read(ByteBuffer.wrap(temp));
        if (ret > 0) {
            int len = (int) ret;
            byte[] data = new byte[len];
            System.arraycopy(temp, 0, data, 0, len);
            verboseLog("UDP read", channel.socket().getLocalSocketAddress(), channel.socket().getRemoteSocketAddress(), data);
            return data;
        }
        throw new EOFException();
    }

    static byte[] sendrecv(SocketAddress local, SocketAddress remote, byte[] data, int max, long endTime) throws IOException {
        UDPClient client = new UDPClient(endTime);
        try {
            client.bind(local);
            client.connect(remote);
            client.send(data);
            return client.recv(max);
        } finally {
            client.cleanup();
        }
    }

    static byte[] sendrecv(SocketAddress addr, byte[] data, int max, long endTime) throws IOException {
        return sendrecv((SocketAddress) null, addr, data, max, endTime);
    }
}
