package org.xbill.DNS;

class ResolveThread extends Thread {
    private Object id;
    private ResolverListener listener;
    private Message query;
    private Resolver res;

    public ResolveThread(Resolver res2, Message query2, Object id2, ResolverListener listener2) {
        this.res = res2;
        this.query = query2;
        this.id = id2;
        this.listener = listener2;
    }

    public void run() {
        try {
            this.listener.receiveMessage(this.id, this.res.send(this.query));
        } catch (Exception e) {
            this.listener.handleException(this.id, e);
        }
    }
}
