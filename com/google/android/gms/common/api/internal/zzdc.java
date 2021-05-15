package com.google.android.gms.common.api.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class zzdc extends Fragment implements zzcf {
    private static WeakHashMap<FragmentActivity, WeakReference<zzdc>> zzgan = new WeakHashMap<>();
    /* access modifiers changed from: private */
    public int zzcfl = 0;
    private Map<String, LifecycleCallback> zzgao = new ArrayMap();
    /* access modifiers changed from: private */
    public Bundle zzgap;

    public static zzdc zza(FragmentActivity fragmentActivity) {
        zzdc zzdc;
        WeakReference weakReference = zzgan.get(fragmentActivity);
        if (weakReference != null && (zzdc = (zzdc) weakReference.get()) != null) {
            return zzdc;
        }
        try {
            zzdc findFragmentByTag = fragmentActivity.getSupportFragmentManager().findFragmentByTag("SupportLifecycleFragmentImpl");
            if (findFragmentByTag == null || findFragmentByTag.isRemoving()) {
                findFragmentByTag = new zzdc();
                fragmentActivity.getSupportFragmentManager().beginTransaction().add(findFragmentByTag, "SupportLifecycleFragmentImpl").commitAllowingStateLoss();
            }
            zzgan.put(fragmentActivity, new WeakReference(findFragmentByTag));
            return findFragmentByTag;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Fragment with tag SupportLifecycleFragmentImpl is not a SupportLifecycleFragmentImpl", e);
        }
    }

    public final void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        zzdc.super.dump(str, fileDescriptor, printWriter, strArr);
        for (LifecycleCallback dump : this.zzgao.values()) {
            dump.dump(str, fileDescriptor, printWriter, strArr);
        }
    }

    public final void onActivityResult(int i, int i2, Intent intent) {
        zzdc.super.onActivityResult(i, i2, intent);
        for (LifecycleCallback onActivityResult : this.zzgao.values()) {
            onActivityResult.onActivityResult(i, i2, intent);
        }
    }

    public final void onCreate(Bundle bundle) {
        zzdc.super.onCreate(bundle);
        this.zzcfl = 1;
        this.zzgap = bundle;
        for (Map.Entry next : this.zzgao.entrySet()) {
            ((LifecycleCallback) next.getValue()).onCreate(bundle != null ? bundle.getBundle((String) next.getKey()) : null);
        }
    }

    public final void onDestroy() {
        zzdc.super.onDestroy();
        this.zzcfl = 5;
        for (LifecycleCallback onDestroy : this.zzgao.values()) {
            onDestroy.onDestroy();
        }
    }

    public final void onResume() {
        zzdc.super.onResume();
        this.zzcfl = 3;
        for (LifecycleCallback onResume : this.zzgao.values()) {
            onResume.onResume();
        }
    }

    public final void onSaveInstanceState(Bundle bundle) {
        zzdc.super.onSaveInstanceState(bundle);
        if (bundle != null) {
            for (Map.Entry next : this.zzgao.entrySet()) {
                Bundle bundle2 = new Bundle();
                ((LifecycleCallback) next.getValue()).onSaveInstanceState(bundle2);
                bundle.putBundle((String) next.getKey(), bundle2);
            }
        }
    }

    public final void onStart() {
        zzdc.super.onStart();
        this.zzcfl = 2;
        for (LifecycleCallback onStart : this.zzgao.values()) {
            onStart.onStart();
        }
    }

    public final void onStop() {
        zzdc.super.onStop();
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
                new Handler(Looper.getMainLooper()).post(new zzdd(this, lifecycleCallback, str));
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

    public final /* synthetic */ Activity zzakw() {
        return getActivity();
    }
}
