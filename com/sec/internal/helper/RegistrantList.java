package com.sec.internal.helper;

import android.os.Handler;
import java.util.ArrayList;
import java.util.Iterator;

public class RegistrantList {
    ArrayList<Registrant> registrants = new ArrayList<>();

    public synchronized void add(Handler h, int what, Object obj) {
        add(new Registrant(h, what, obj));
    }

    public synchronized void addUnique(Handler h, int what, Object obj) {
        remove(h);
        add(new Registrant(h, what, obj));
    }

    public synchronized void add(Registrant r) {
        removeCleared();
        this.registrants.add(r);
    }

    public synchronized void removeCleared() {
        for (int i = this.registrants.size() - 1; i >= 0; i--) {
            if (this.registrants.get(i).refH == null) {
                this.registrants.remove(i);
            }
        }
    }

    public synchronized int size() {
        return this.registrants.size();
    }

    private synchronized void internalNotifyRegistrants(Object result, Throwable exception) {
        int s = this.registrants.size();
        for (int i = 0; i < s; i++) {
            this.registrants.get(i).internalNotifyRegistrant(result, exception);
        }
    }

    public void notifyRegistrants() {
        internalNotifyRegistrants((Object) null, (Throwable) null);
    }

    public void notifyResult(Object result) {
        internalNotifyRegistrants(result, (Throwable) null);
    }

    public void notifyRegistrants(AsyncResult ar) {
        internalNotifyRegistrants(ar.result, ar.exception);
    }

    public synchronized void remove(Handler h) {
        int s = this.registrants.size();
        for (int i = 0; i < s; i++) {
            Registrant r = this.registrants.get(i);
            Handler rh = r.getHandler();
            if (rh == null || rh == h) {
                r.clear();
            }
        }
        removeCleared();
    }

    public int find(Handler h) {
        int count = 0;
        Iterator<Registrant> it = this.registrants.iterator();
        while (it.hasNext()) {
            if (it.next().getHandler() == h) {
                count++;
            }
        }
        return count;
    }
}
