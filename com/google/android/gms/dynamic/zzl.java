package com.google.android.gms.dynamic;

import com.google.android.gms.internal.zzew;

public abstract class zzl extends zzew implements zzk {
    public zzl() {
        attachInterface(this, "com.google.android.gms.dynamic.IFragmentWrapper");
    }

    /* JADX WARNING: type inference failed for: r4v1, types: [android.os.IInterface] */
    /* JADX WARNING: type inference failed for: r4v3, types: [android.os.IInterface] */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0067, code lost:
        r5.writeNoException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ce, code lost:
        r5.writeNoException();
        com.google.android.gms.internal.zzex.zza(r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00e3, code lost:
        r5.writeNoException();
        r5.writeInt(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f9, code lost:
        r5.writeNoException();
        com.google.android.gms.internal.zzex.zza(r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ff, code lost:
        return true;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 2 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTransact(int r3, android.os.Parcel r4, android.os.Parcel r5, int r6) throws android.os.RemoteException {
        /*
            r2 = this;
            boolean r6 = r2.zza(r3, r4, r5, r6)
            r0 = 1
            if (r6 == 0) goto L_0x0008
            return r0
        L_0x0008:
            r6 = 0
            java.lang.String r1 = "com.google.android.gms.dynamic.IObjectWrapper"
            switch(r3) {
                case 2: goto L_0x00f5;
                case 3: goto L_0x00ea;
                case 4: goto L_0x00df;
                case 5: goto L_0x00da;
                case 6: goto L_0x00d5;
                case 7: goto L_0x00ca;
                case 8: goto L_0x00bf;
                case 9: goto L_0x00ba;
                case 10: goto L_0x00b5;
                case 11: goto L_0x00b0;
                case 12: goto L_0x00ab;
                case 13: goto L_0x00a6;
                case 14: goto L_0x00a1;
                case 15: goto L_0x009c;
                case 16: goto L_0x0097;
                case 17: goto L_0x0092;
                case 18: goto L_0x008d;
                case 19: goto L_0x0088;
                case 20: goto L_0x006c;
                case 21: goto L_0x0060;
                case 22: goto L_0x0058;
                case 23: goto L_0x0050;
                case 24: goto L_0x0048;
                case 25: goto L_0x003c;
                case 26: goto L_0x002c;
                case 27: goto L_0x0010;
                default: goto L_0x000e;
            }
        L_0x000e:
            r3 = 0
            return r3
        L_0x0010:
            android.os.IBinder r3 = r4.readStrongBinder()
            if (r3 != 0) goto L_0x0017
            goto L_0x0028
        L_0x0017:
            android.os.IInterface r4 = r3.queryLocalInterface(r1)
            boolean r6 = r4 instanceof com.google.android.gms.dynamic.IObjectWrapper
            if (r6 == 0) goto L_0x0023
            r6 = r4
            com.google.android.gms.dynamic.IObjectWrapper r6 = (com.google.android.gms.dynamic.IObjectWrapper) r6
            goto L_0x0028
        L_0x0023:
            com.google.android.gms.dynamic.zzm r6 = new com.google.android.gms.dynamic.zzm
            r6.<init>(r3)
        L_0x0028:
            r2.zzx(r6)
            goto L_0x0067
        L_0x002c:
            android.os.Parcelable$Creator r3 = android.content.Intent.CREATOR
            android.os.Parcelable r3 = com.google.android.gms.internal.zzex.zza((android.os.Parcel) r4, r3)
            android.content.Intent r3 = (android.content.Intent) r3
            int r4 = r4.readInt()
            r2.startActivityForResult(r3, r4)
            goto L_0x0067
        L_0x003c:
            android.os.Parcelable$Creator r3 = android.content.Intent.CREATOR
            android.os.Parcelable r3 = com.google.android.gms.internal.zzex.zza((android.os.Parcel) r4, r3)
            android.content.Intent r3 = (android.content.Intent) r3
            r2.startActivity(r3)
            goto L_0x0067
        L_0x0048:
            boolean r3 = com.google.android.gms.internal.zzex.zza(r4)
            r2.setUserVisibleHint(r3)
            goto L_0x0067
        L_0x0050:
            boolean r3 = com.google.android.gms.internal.zzex.zza(r4)
            r2.setRetainInstance(r3)
            goto L_0x0067
        L_0x0058:
            boolean r3 = com.google.android.gms.internal.zzex.zza(r4)
            r2.setMenuVisibility(r3)
            goto L_0x0067
        L_0x0060:
            boolean r3 = com.google.android.gms.internal.zzex.zza(r4)
            r2.setHasOptionsMenu(r3)
        L_0x0067:
            r5.writeNoException()
            goto L_0x00ff
        L_0x006c:
            android.os.IBinder r3 = r4.readStrongBinder()
            if (r3 != 0) goto L_0x0073
            goto L_0x0084
        L_0x0073:
            android.os.IInterface r4 = r3.queryLocalInterface(r1)
            boolean r6 = r4 instanceof com.google.android.gms.dynamic.IObjectWrapper
            if (r6 == 0) goto L_0x007f
            r6 = r4
            com.google.android.gms.dynamic.IObjectWrapper r6 = (com.google.android.gms.dynamic.IObjectWrapper) r6
            goto L_0x0084
        L_0x007f:
            com.google.android.gms.dynamic.zzm r6 = new com.google.android.gms.dynamic.zzm
            r6.<init>(r3)
        L_0x0084:
            r2.zzw(r6)
            goto L_0x0067
        L_0x0088:
            boolean r3 = r2.isVisible()
            goto L_0x00ce
        L_0x008d:
            boolean r3 = r2.isResumed()
            goto L_0x00ce
        L_0x0092:
            boolean r3 = r2.isRemoving()
            goto L_0x00ce
        L_0x0097:
            boolean r3 = r2.isInLayout()
            goto L_0x00ce
        L_0x009c:
            boolean r3 = r2.isHidden()
            goto L_0x00ce
        L_0x00a1:
            boolean r3 = r2.isDetached()
            goto L_0x00ce
        L_0x00a6:
            boolean r3 = r2.isAdded()
            goto L_0x00ce
        L_0x00ab:
            com.google.android.gms.dynamic.IObjectWrapper r3 = r2.getView()
            goto L_0x00f9
        L_0x00b0:
            boolean r3 = r2.getUserVisibleHint()
            goto L_0x00ce
        L_0x00b5:
            int r3 = r2.getTargetRequestCode()
            goto L_0x00e3
        L_0x00ba:
            com.google.android.gms.dynamic.zzk r3 = r2.zzark()
            goto L_0x00f9
        L_0x00bf:
            java.lang.String r3 = r2.getTag()
            r5.writeNoException()
            r5.writeString(r3)
            goto L_0x00ff
        L_0x00ca:
            boolean r3 = r2.getRetainInstance()
        L_0x00ce:
            r5.writeNoException()
            com.google.android.gms.internal.zzex.zza((android.os.Parcel) r5, (boolean) r3)
            goto L_0x00ff
        L_0x00d5:
            com.google.android.gms.dynamic.IObjectWrapper r3 = r2.zzarj()
            goto L_0x00f9
        L_0x00da:
            com.google.android.gms.dynamic.zzk r3 = r2.zzari()
            goto L_0x00f9
        L_0x00df:
            int r3 = r2.getId()
        L_0x00e3:
            r5.writeNoException()
            r5.writeInt(r3)
            goto L_0x00ff
        L_0x00ea:
            android.os.Bundle r3 = r2.getArguments()
            r5.writeNoException()
            com.google.android.gms.internal.zzex.zzb(r5, r3)
            goto L_0x00ff
        L_0x00f5:
            com.google.android.gms.dynamic.IObjectWrapper r3 = r2.zzarh()
        L_0x00f9:
            r5.writeNoException()
            com.google.android.gms.internal.zzex.zza((android.os.Parcel) r5, (android.os.IInterface) r3)
        L_0x00ff:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.dynamic.zzl.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
    }
}
