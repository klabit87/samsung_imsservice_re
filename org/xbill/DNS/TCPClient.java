package org.xbill.DNS;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

final class TCPClient extends Client {
    public TCPClient(long endTime) throws IOException {
        super(SocketChannel.open(), endTime);
    }

    /* access modifiers changed from: package-private */
    public void bind(SocketAddress addr) throws IOException {
        ((SocketChannel) this.key.channel()).socket().bind(addr);
    }

    /* access modifiers changed from: package-private */
    public void connect(SocketAddress addr) throws IOException {
        SocketChannel channel = (SocketChannel) this.key.channel();
        if (!channel.connect(addr)) {
            this.key.interestOps(8);
            while (!channel.finishConnect()) {
                try {
                    if (!this.key.isConnectable()) {
                        blockUntil(this.key, this.endTime);
                    }
                } finally {
                    if (this.key.isValid()) {
                        this.key.interestOps(0);
                    }
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    /* access modifiers changed from: package-private */
    public void send(byte[] data) throws IOException {
        SocketChannel channel = (SocketChannel) this.key.channel();
        verboseLog("TCP write", channel.socket().getLocalSocketAddress(), channel.socket().getRemoteSocketAddress(), data);
        ByteBuffer[] buffers = {ByteBuffer.wrap(new byte[]{(byte) (data.length >>> 8), (byte) (data.length & 255)}), ByteBuffer.wrap(data)};
        int nsent = 0;
        this.key.interestOps(4);
        while (nsent < data.length + 2) {
            try {
                if (this.key.isWritable()) {
                    long n = channel.write(buffers);
                    if (n >= 0) {
                        nsent += (int) n;
                        if (nsent < data.length + 2) {
                            if (System.currentTimeMillis() > this.endTime) {
                                throw new SocketTimeoutException();
                            }
                        }
                    } else {
                        throw new EOFException();
                    }
                } else {
                    blockUntil(this.key, this.endTime);
                }
            } finally {
                if (this.key.isValid()) {
                    this.key.interestOps(0);
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    private byte[] _recv(int length) throws IOException {
        SocketChannel channel = (SocketChannel) this.key.channel();
        int nrecvd = 0;
        byte[] data = new byte[length];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.key.interestOps(1);
        while (nrecvd < length) {
            try {
                if (this.key.isReadable()) {
                    long n = (long) channel.read(buffer);
                    if (n >= 0) {
                        nrecvd += (int) n;
                        if (nrecvd < length) {
                            if (System.currentTimeMillis() > this.endTime) {
                                throw new SocketTimeoutException();
                            }
                        }
                    } else {
                        throw new EOFException();
                    }
                } else {
                    blockUntil(this.key, this.endTime);
                }
            } catch (Throwable th) {
                if (this.key.isValid()) {
                    this.key.interestOps(0);
                }
                throw th;
            }
        }
        if (this.key.isValid()) {
            this.key.interestOps(0);
        }
        return data;
    }

    /* access modifiers changed from: package-private */
    public byte[] recv() throws IOException {
        byte[] buf = _recv(2);
        byte[] data = _recv(((buf[0] & 255) << 8) + (buf[1] & 255));
        SocketChannel channel = (SocketChannel) this.key.channel();
        verboseLog("TCP read", channel.socket().getLocalSocketAddress(), channel.socket().getRemoteSocketAddress(), data);
        return data;
    }

    static byte[] sendrecv(SocketAddress local, SocketAddress remote, byte[] data, long endTime) throws IOException {
        TCPClient client = new TCPClient(endTime);
        if (local != null) {
            try {
                client.bind(local);
            } catch (Throwable th) {
                client.cleanup();
                throw th;
            }
        }
        client.connect(remote);
        client.send(data);
        byte[] recv = client.recv();
        client.cleanup();
        return recv;
    }

    static byte[] sendrecv(SocketAddress addr, byte[] data, long endTime) throws IOException {
        return sendrecv((SocketAddress) null, addr, data, endTime);
    }
}
