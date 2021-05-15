package com.sec.internal.ims.core;

import com.sec.ims.settings.ImsProfile;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$s8c69XYNgDxHzmtOH8_cwiDgYsI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$s8c69XYNgDxHzmtOH8_cwiDgYsI implements Predicate {
    public static final /* synthetic */ $$Lambda$s8c69XYNgDxHzmtOH8_cwiDgYsI INSTANCE = new $$Lambda$s8c69XYNgDxHzmtOH8_cwiDgYsI();

    private /* synthetic */ $$Lambda$s8c69XYNgDxHzmtOH8_cwiDgYsI() {
    }

    public final boolean test(Object obj) {
        return ((ImsProfile) obj).hasEmergencySupport();
    }
}
