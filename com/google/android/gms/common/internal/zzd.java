package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.zzc;
import com.google.android.gms.common.zzf;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class zzd<T extends IInterface> {
    private static String[] zzgfi = {"service_esmobile", "service_googleme"};
    private final Context mContext;
    final Handler mHandler;
    private final Object mLock;
    private final Looper zzalj;
    private final zzf zzfwk;
    private int zzgen;
    private long zzgeo;
    private long zzgep;
    private int zzgeq;
    private long zzger;
    private zzam zzges;
    private final zzag zzget;
    /* access modifiers changed from: private */
    public final Object zzgeu;
    /* access modifiers changed from: private */
    public zzay zzgev;
    protected zzj zzgew;
    private T zzgex;
    /* access modifiers changed from: private */
    public final ArrayList<zzi<?>> zzgey;
    private zzl zzgez;
    private int zzgfa;
    /* access modifiers changed from: private */
    public final zzf zzgfb;
    /* access modifiers changed from: private */
    public final zzg zzgfc;
    private final int zzgfd;
    private final String zzgfe;
    /* access modifiers changed from: private */
    public ConnectionResult zzgff;
    /* access modifiers changed from: private */
    public boolean zzgfg;
    protected AtomicInteger zzgfh;

    protected zzd(Context context, Looper looper, int i, zzf zzf, zzg zzg, String str) {
        this(context, looper, zzag.zzcp(context), zzf.zzahf(), i, (zzf) zzbq.checkNotNull(zzf), (zzg) zzbq.checkNotNull(zzg), (String) null);
    }

    protected zzd(Context context, Looper looper, zzag zzag, zzf zzf, int i, zzf zzf2, zzg zzg, String str) {
        this.mLock = new Object();
        this.zzgeu = new Object();
        this.zzgey = new ArrayList<>();
        this.zzgfa = 1;
        this.zzgff = null;
        this.zzgfg = false;
        this.zzgfh = new AtomicInteger(0);
        this.mContext = (Context) zzbq.checkNotNull(context, "Context must not be null");
        this.zzalj = (Looper) zzbq.checkNotNull(looper, "Looper must not be null");
        this.zzget = (zzag) zzbq.checkNotNull(zzag, "Supervisor must not be null");
        this.zzfwk = (zzf) zzbq.checkNotNull(zzf, "API availability must not be null");
        this.mHandler = new zzh(this, looper);
        this.zzgfd = i;
        this.zzgfb = zzf2;
        this.zzgfc = zzg;
        this.zzgfe = str;
    }

    /* access modifiers changed from: private */
    public final void zza(int i, T t) {
        zzbq.checkArgument((i == 4) == (t != null));
        synchronized (this.mLock) {
            this.zzgfa = i;
            this.zzgex = t;
            zzb(i, t);
            if (i != 1) {
                if (i == 2 || i == 3) {
                    if (!(this.zzgez == null || this.zzges == null)) {
                        String zzamx = this.zzges.zzamx();
                        String packageName = this.zzges.getPackageName();
                        StringBuilder sb = new StringBuilder(String.valueOf(zzamx).length() + 70 + String.valueOf(packageName).length());
                        sb.append("Calling connect() while still connected, missing disconnect() for ");
                        sb.append(zzamx);
                        sb.append(" on ");
                        sb.append(packageName);
                        Log.e("GmsClient", sb.toString());
                        this.zzget.zza(this.zzges.zzamx(), this.zzges.getPackageName(), this.zzges.zzamu(), this.zzgez, zzalr());
                        this.zzgfh.incrementAndGet();
                    }
                    this.zzgez = new zzl(this, this.zzgfh.get());
                    zzam zzam = new zzam(zzalq(), zzhm(), false, 129);
                    this.zzges = zzam;
                    if (!this.zzget.zza(new zzah(zzam.zzamx(), this.zzges.getPackageName(), this.zzges.zzamu()), (ServiceConnection) this.zzgez, zzalr())) {
                        String zzamx2 = this.zzges.zzamx();
                        String packageName2 = this.zzges.getPackageName();
                        StringBuilder sb2 = new StringBuilder(String.valueOf(zzamx2).length() + 34 + String.valueOf(packageName2).length());
                        sb2.append("unable to connect to service: ");
                        sb2.append(zzamx2);
                        sb2.append(" on ");
                        sb2.append(packageName2);
                        Log.e("GmsClient", sb2.toString());
                        zza(16, (Bundle) null, this.zzgfh.get());
                    }
                } else if (i == 4) {
                    zza(t);
                }
            } else if (this.zzgez != null) {
                this.zzget.zza(zzhm(), zzalq(), 129, this.zzgez, zzalr());
                this.zzgez = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public final boolean zza(int i, int i2, T t) {
        synchronized (this.mLock) {
            if (this.zzgfa != i) {
                return false;
            }
            zza(i2, t);
            return true;
        }
    }

    private final String zzalr() {
        String str = this.zzgfe;
        return str == null ? this.mContext.getClass().getName() : str;
    }

    private final boolean zzalt() {
        boolean z;
        synchronized (this.mLock) {
            z = this.zzgfa == 3;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public final boolean zzalz() {
        if (this.zzgfg || TextUtils.isEmpty(zzhn()) || TextUtils.isEmpty((CharSequence) null)) {
            return false;
        }
        try {
            Class.forName(zzhn());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public final void zzce(int i) {
        int i2;
        if (zzalt()) {
            i2 = 5;
            this.zzgfg = true;
        } else {
            i2 = 4;
        }
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(i2, this.zzgfh.get(), 16));
    }

    public void disconnect() {
        this.zzgfh.incrementAndGet();
        synchronized (this.zzgey) {
            int size = this.zzgey.size();
            for (int i = 0; i < size; i++) {
                this.zzgey.get(i).removeListener();
            }
            this.zzgey.clear();
        }
        synchronized (this.zzgeu) {
            this.zzgev = null;
        }
        zza(1, (IInterface) null);
    }

    public final void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        int i;
        T t;
        zzay zzay;
        synchronized (this.mLock) {
            i = this.zzgfa;
            t = this.zzgex;
        }
        synchronized (this.zzgeu) {
            zzay = this.zzgev;
        }
        printWriter.append(str).append("mConnectState=");
        printWriter.print(i != 1 ? i != 2 ? i != 3 ? i != 4 ? i != 5 ? "UNKNOWN" : "DISCONNECTING" : "CONNECTED" : "LOCAL_CONNECTING" : "REMOTE_CONNECTING" : "DISCONNECTED");
        printWriter.append(" mService=");
        if (t == null) {
            printWriter.append("null");
        } else {
            printWriter.append(zzhn()).append("@").append(Integer.toHexString(System.identityHashCode(t.asBinder())));
        }
        printWriter.append(" mServiceBroker=");
        if (zzay == null) {
            printWriter.println("null");
        } else {
            printWriter.append("IGmsServiceBroker@").println(Integer.toHexString(System.identityHashCode(zzay.asBinder())));
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        if (this.zzgep > 0) {
            PrintWriter append = printWriter.append(str).append("lastConnectedTime=");
            long j = this.zzgep;
            String format = simpleDateFormat.format(new Date(this.zzgep));
            StringBuilder sb = new StringBuilder(String.valueOf(format).length() + 21);
            sb.append(j);
            sb.append(" ");
            sb.append(format);
            append.println(sb.toString());
        }
        if (this.zzgeo > 0) {
            printWriter.append(str).append("lastSuspendedCause=");
            int i2 = this.zzgen;
            printWriter.append(i2 != 1 ? i2 != 2 ? String.valueOf(i2) : "CAUSE_NETWORK_LOST" : "CAUSE_SERVICE_DISCONNECTED");
            PrintWriter append2 = printWriter.append(" lastSuspendedTime=");
            long j2 = this.zzgeo;
            String format2 = simpleDateFormat.format(new Date(this.zzgeo));
            StringBuilder sb2 = new StringBuilder(String.valueOf(format2).length() + 21);
            sb2.append(j2);
            sb2.append(" ");
            sb2.append(format2);
            append2.println(sb2.toString());
        }
        if (this.zzger > 0) {
            printWriter.append(str).append("lastFailedStatus=").append(CommonStatusCodes.getStatusCodeString(this.zzgeq));
            PrintWriter append3 = printWriter.append(" lastFailedTime=");
            long j3 = this.zzger;
            String format3 = simpleDateFormat.format(new Date(this.zzger));
            StringBuilder sb3 = new StringBuilder(String.valueOf(format3).length() + 21);
            sb3.append(j3);
            sb3.append(" ");
            sb3.append(format3);
            append3.println(sb3.toString());
        }
    }

    public Account getAccount() {
        return null;
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final Looper getLooper() {
        return this.zzalj;
    }

    public Intent getSignInIntent() {
        throw new UnsupportedOperationException("Not a sign in API");
    }

    public final boolean isConnected() {
        boolean z;
        synchronized (this.mLock) {
            z = this.zzgfa == 4;
        }
        return z;
    }

    public final boolean isConnecting() {
        boolean z;
        synchronized (this.mLock) {
            if (this.zzgfa != 2) {
                if (this.zzgfa != 3) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        this.zzgeq = connectionResult.getErrorCode();
        this.zzger = System.currentTimeMillis();
    }

    /* access modifiers changed from: protected */
    public void onConnectionSuspended(int i) {
        this.zzgen = i;
        this.zzgeo = System.currentTimeMillis();
    }

    /* access modifiers changed from: protected */
    public final void zza(int i, Bundle bundle, int i2) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(7, i2, -1, new zzo(this, i, (Bundle) null)));
    }

    /* access modifiers changed from: protected */
    public void zza(int i, IBinder iBinder, Bundle bundle, int i2) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, i2, -1, new zzn(this, i, iBinder, bundle)));
    }

    /* access modifiers changed from: protected */
    public void zza(T t) {
        this.zzgep = System.currentTimeMillis();
    }

    public final void zza(zzan zzan, Set<Scope> set) {
        Bundle zzabt = zzabt();
        zzz zzz = new zzz(this.zzgfd);
        zzz.zzggd = this.mContext.getPackageName();
        zzz.zzggg = zzabt;
        if (set != null) {
            zzz.zzggf = (Scope[]) set.toArray(new Scope[set.size()]);
        }
        if (zzacc()) {
            zzz.zzggh = getAccount() != null ? getAccount() : new Account("<<default account>>", "com.google");
            if (zzan != null) {
                zzz.zzgge = zzan.asBinder();
            }
        } else if (zzalx()) {
            zzz.zzggh = getAccount();
        }
        zzz.zzggi = zzalu();
        try {
            synchronized (this.zzgeu) {
                if (this.zzgev != null) {
                    this.zzgev.zza(new zzk(this, this.zzgfh.get()), zzz);
                } else {
                    Log.w("GmsClient", "mServiceBroker is null, client disconnected");
                }
            }
        } catch (DeadObjectException e) {
            Log.w("GmsClient", "IGmsServiceBroker.getService failed", e);
            zzcd(1);
        } catch (SecurityException e2) {
            throw e2;
        } catch (RemoteException | RuntimeException e3) {
            Log.w("GmsClient", "IGmsServiceBroker.getService failed", e3);
            zza(8, (IBinder) null, (Bundle) null, this.zzgfh.get());
        }
    }

    public void zza(zzj zzj) {
        this.zzgew = (zzj) zzbq.checkNotNull(zzj, "Connection progress callbacks cannot be null.");
        zza(2, (IInterface) null);
    }

    /* access modifiers changed from: protected */
    public final void zza(zzj zzj, int i, PendingIntent pendingIntent) {
        this.zzgew = (zzj) zzbq.checkNotNull(zzj, "Connection progress callbacks cannot be null.");
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(3, this.zzgfh.get(), i, pendingIntent));
    }

    public void zza(zzp zzp) {
        zzp.zzako();
    }

    /* access modifiers changed from: protected */
    public Bundle zzabt() {
        return new Bundle();
    }

    public boolean zzacc() {
        return false;
    }

    public boolean zzacn() {
        return false;
    }

    public Bundle zzagp() {
        return null;
    }

    public boolean zzahn() {
        return true;
    }

    public final IBinder zzaho() {
        synchronized (this.zzgeu) {
            if (this.zzgev == null) {
                return null;
            }
            IBinder asBinder = this.zzgev.asBinder();
            return asBinder;
        }
    }

    public final String zzahp() {
        zzam zzam;
        if (isConnected() && (zzam = this.zzges) != null) {
            return zzam.getPackageName();
        }
        throw new RuntimeException("Failed to connect when checking package");
    }

    /* access modifiers changed from: protected */
    public String zzalq() {
        return "com.google.android.gms";
    }

    public final void zzals() {
        int isGooglePlayServicesAvailable = this.zzfwk.isGooglePlayServicesAvailable(this.mContext);
        if (isGooglePlayServicesAvailable != 0) {
            zza(1, (IInterface) null);
            zza((zzj) new zzm(this), isGooglePlayServicesAvailable, (PendingIntent) null);
            return;
        }
        zza((zzj) new zzm(this));
    }

    public zzc[] zzalu() {
        return new zzc[0];
    }

    /* access modifiers changed from: protected */
    public final void zzalv() {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected. Call connect() and wait for onConnected() to be called.");
        }
    }

    public final T zzalw() throws DeadObjectException {
        T t;
        synchronized (this.mLock) {
            if (this.zzgfa != 5) {
                zzalv();
                zzbq.zza(this.zzgex != null, (Object) "Client is connected but service is null");
                t = this.zzgex;
            } else {
                throw new DeadObjectException();
            }
        }
        return t;
    }

    public boolean zzalx() {
        return false;
    }

    /* access modifiers changed from: protected */
    public Set<Scope> zzaly() {
        return Collections.EMPTY_SET;
    }

    /* access modifiers changed from: package-private */
    public void zzb(int i, T t) {
    }

    public final void zzcd(int i) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(6, this.zzgfh.get(), i));
    }

    /* access modifiers changed from: protected */
    public abstract T zzd(IBinder iBinder);

    /* access modifiers changed from: protected */
    public abstract String zzhm();

    /* access modifiers changed from: protected */
    public abstract String zzhn();
}
