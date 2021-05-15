package com.google.android.gms.common.internal;

import android.os.Parcel;
import android.os.Parcelable;

final class zzq implements Parcelable.Creator<BinderWrapper> {
    zzq() {
    }

    public final /* synthetic */ Object createFromParcel(Parcel parcel) {
        return new BinderWrapper(parcel, (zzq) null);
    }

    public final /* synthetic */ Object[] newArray(int i) {
        return new BinderWrapper[i];
    }
}
