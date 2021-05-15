package com.google.firebase.iid;

import android.os.Bundle;

final class zzv extends zzt<Bundle> {
    zzv(int i, int i2, Bundle bundle) {
        super(i, 1, bundle);
    }

    /* access modifiers changed from: package-private */
    public final boolean zzaww() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public final void zzx(Bundle bundle) {
        Bundle bundle2 = bundle.getBundle("data");
        if (bundle2 == null) {
            bundle2 = Bundle.EMPTY;
        }
        finish(bundle2);
    }
}
