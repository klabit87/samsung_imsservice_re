package com.google.android.gms.internal;

import java.util.Arrays;
import java.util.Stack;

final class zzfjs {
    private final Stack<zzfgs> zzprx;

    private zzfjs() {
        this.zzprx = new Stack<>();
    }

    private final void zzbb(zzfgs zzfgs) {
        while (!zzfgs.zzcxs()) {
            if (zzfgs instanceof zzfjq) {
                zzfjq zzfjq = (zzfjq) zzfgs;
                zzbb(zzfjq.zzprt);
                zzfgs = zzfjq.zzpru;
            } else {
                String valueOf = String.valueOf(zzfgs.getClass());
                StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 49);
                sb.append("Has a new type of ByteString been created? Found ");
                sb.append(valueOf);
                throw new IllegalArgumentException(sb.toString());
            }
        }
        int zzmp = zzmp(zzfgs.size());
        int i = zzfjq.zzprr[zzmp + 1];
        if (this.zzprx.isEmpty() || this.zzprx.peek().size() >= i) {
            this.zzprx.push(zzfgs);
            return;
        }
        int i2 = zzfjq.zzprr[zzmp];
        zzfgs pop = this.zzprx.pop();
        while (!this.zzprx.isEmpty() && this.zzprx.peek().size() < i2) {
            pop = new zzfjq(this.zzprx.pop(), pop);
        }
        zzfjq zzfjq2 = new zzfjq(pop, zzfgs);
        while (!this.zzprx.isEmpty()) {
            if (this.zzprx.peek().size() >= zzfjq.zzprr[zzmp(zzfjq2.size()) + 1]) {
                break;
            }
            zzfjq2 = new zzfjq(this.zzprx.pop(), zzfjq2);
        }
        this.zzprx.push(zzfjq2);
    }

    /* access modifiers changed from: private */
    public final zzfgs zzc(zzfgs zzfgs, zzfgs zzfgs2) {
        zzbb(zzfgs);
        zzbb(zzfgs2);
        zzfgs pop = this.zzprx.pop();
        while (!this.zzprx.isEmpty()) {
            pop = new zzfjq(this.zzprx.pop(), pop);
        }
        return pop;
    }

    private static int zzmp(int i) {
        int binarySearch = Arrays.binarySearch(zzfjq.zzprr, i);
        return binarySearch < 0 ? (-(binarySearch + 1)) - 1 : binarySearch;
    }
}
