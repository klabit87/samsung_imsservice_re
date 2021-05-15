package com.google.android.gms.common.internal;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public final class BinderWrapper implements Parcelable {
    public static final Parcelable.Creator<BinderWrapper> CREATOR = new zzq();
    private IBinder zzgfp;

    public BinderWrapper() {
        this.zzgfp = null;
    }

    public BinderWrapper(IBinder iBinder) {
        this.zzgfp = null;
        this.zzgfp = iBinder;
    }

    private BinderWrapper(Parcel parcel) {
        this.zzgfp = null;
        this.zzgfp = parcel.readStrongBinder();
    }

    /* synthetic */ BinderWrapper(Parcel parcel, zzq zzq) {
        this(parcel);
    }

    public final int describeContents() {
        return 0;
    }

    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeStrongBinder(this.zzgfp);
    }
}
