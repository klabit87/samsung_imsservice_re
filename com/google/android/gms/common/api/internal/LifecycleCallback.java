package com.google.android.gms.common.api.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class LifecycleCallback {
    protected final zzcf zzgam;

    protected LifecycleCallback(zzcf zzcf) {
        this.zzgam = zzcf;
    }

    private static zzcf getChimeraLifecycleFragmentImpl(zzce zzce) {
        throw new IllegalStateException("Method not available in SDK.");
    }

    protected static zzcf zzb(zzce zzce) {
        if (zzce.zzaks()) {
            return zzdc.zza(zzce.zzakv());
        }
        if (zzce.zzakt()) {
            return zzcg.zzp(zzce.zzaku());
        }
        throw new IllegalArgumentException("Can't get fragment for unexpected activity.");
    }

    public static zzcf zzo(Activity activity) {
        return zzb(new zzce(activity));
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public final Activity getActivity() {
        return this.zzgam.zzakw();
    }

    public void onActivityResult(int i, int i2, Intent intent) {
    }

    public void onCreate(Bundle bundle) {
    }

    public void onDestroy() {
    }

    public void onResume() {
    }

    public void onSaveInstanceState(Bundle bundle) {
    }

    public void onStart() {
    }

    public void onStop() {
    }
}
