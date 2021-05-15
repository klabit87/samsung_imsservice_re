package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfjc;

public abstract class zzfgm<MessageType extends zzfjc> implements zzfjl<MessageType> {
    private static final zzfhm zzpns = zzfhm.zzczf();

    public final /* synthetic */ Object zzc(zzfhb zzfhb, zzfhm zzfhm) throws zzfie {
        zzfjc zzfjc = (zzfjc) zze(zzfhb, zzfhm);
        if (zzfjc == null || zzfjc.isInitialized()) {
            return zzfjc;
        }
        throw (!(zzfjc instanceof zzfgj) ? zzfjc instanceof zzfgl ? new zzfkm((zzfgl) zzfjc) : new zzfkm(zzfjc) : new zzfkm((zzfgj) zzfjc)).zzdbz().zzi(zzfjc);
    }
}
