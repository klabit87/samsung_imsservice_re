package com.google.android.gms.common.api.internal;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.ArrayMap;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class zzcg extends Fragment implements zzcf {
    private static WeakHashMap<Activity, WeakReference<zzcg>> zzgan = new WeakHashMap<>();
    /* access modifiers changed from: private */
    public int zzcfl = 0;
    private Map<String, LifecycleCallback> zzgao = new ArrayMap();
    /* access modifiers changed from: private */
    public Bundle zzgap;

    public static zzcg zzp(Activity activity) {
        zzcg zzcg;
        WeakReference weakReference = zzgan.get(activity);
        if (weakReference != null && (zzcg = (zzcg) weakReference.get()) != null) {
            return zzcg;
        }
        try {
            zzcg zzcg2 = (zzcg) activity.getFragmentManager().findFragmentByTag("LifecycleFragmentImpl");
            if (zzcg2 == null || zzcg2.isRemoving()) {
                zzcg2 = new zzcg();
                activity.getFragmentManager().beginTransaction().add(zzcg2, "LifecycleFragmentImpl").commitAllowingStateLoss();
            }
            zzgan.put(activity, new WeakReference(zzcg2));
            return zzcg2;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Fragment with tag LifecycleFragmentImpl is not a LifecycleFragmentImpl", e);
        }
    }

    public final void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(str, fileDescriptor, printWriter, strArr);
        for (LifecycleCallback dump : this.zzgao.values()) {
            dump.dump(str, fileDescriptor, printWriter, strArr);
        }
    }

    public final void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        for (LifecycleCallback onActivityResult : this.zzgao.values()) {
            onActivityResult.onActivityResult(i, i2, intent);
        }
    }

    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.zzcfl = 1;
        this.zzgap = bundle;
        for (Map.Entry next : this.zzgao.entrySet()) {
            ((LifecycleCallback) next.getValue()).onCreate(bundle != null ? bundle.getBundle((String) next.getKey()) : null);
        }
    }

    public final void onDestroy() {
        super.onDestroy();
        this.zzcfl = 5;
        for (LifecycleCallback onDestroy : this.zzgao.values()) {
            onDestroy.onDestroy();
        }
    }

    public final void onResume() {
        super.onResume();
        this.zzcfl = 3;
        for (LifecycleCallback onResume : this.zzgao.values()) {
            onResume.onResume();
        }
    }

    public final void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (bundle != null) {
            for (Map.Entry next : this.zzgao.entrySet()) {
                Bundle bundle2 = new Bundle();
                ((LifecycleCallback) next.getValue()).onSaveInstanceState(bundle2);
                bundle.putBundle((String) next.getKey(), bundle2);
            }
        }
    }

    public final void onStart() {
        super.onStart();
        this.zzcfl = 2;
        for (LifecycleCallback onStart : this.zzgao.values()) {
            onStart.onStart();
        }
    }

    public final void onStop() {
        super.onStop();
        this.zzcfl = 4;
        for (LifecycleCallback onStop : this.zzgao.values()) {
            onStop.onStop();
        }
    }

    public final <T extends LifecycleCallback> T zza(String str, Class<T> cls) {
        return (LifecycleCallback) cls.cast(this.zzgao.get(str));
    }

    public final void zza(String str, LifecycleCallback lifecycleCallback) {
        if (!this.zzgao.containsKey(str)) {
            this.zzgao.put(str, lifecycleCallback);
            if (this.zzcfl > 0) {
                new Handler(Looper.getMainLooper()).post(new zzch(this, lifecycleCallback, str));
                return;
            }
            return;
        }
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 59);
        sb.append("LifecycleCallback with tag ");
        sb.append(str);
        sb.append(" already added to this fragment.");
        throw new IllegalArgumentException(sb.toString());
    }

    public final Activity zzakw() {
        return getActivity();
    }
}
