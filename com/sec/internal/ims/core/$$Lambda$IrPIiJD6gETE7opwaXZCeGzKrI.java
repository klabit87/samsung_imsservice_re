package com.sec.internal.ims.core;

import com.sec.ims.settings.ImsProfile;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$IrPIiJD6-gETE7opwaXZCeGzKrI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IrPIiJD6gETE7opwaXZCeGzKrI implements Predicate {
    public static final /* synthetic */ $$Lambda$IrPIiJD6gETE7opwaXZCeGzKrI INSTANCE = new $$Lambda$IrPIiJD6gETE7opwaXZCeGzKrI();

    private /* synthetic */ $$Lambda$IrPIiJD6gETE7opwaXZCeGzKrI() {
    }

    public final boolean test(Object obj) {
        return ImsProfile.hasRcsService((ImsProfile) obj);
    }
}
