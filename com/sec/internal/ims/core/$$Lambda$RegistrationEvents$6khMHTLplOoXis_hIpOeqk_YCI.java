package com.sec.internal.ims.core;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$RegistrationEvents$6khMH-TLplOoXis_hIpOeqk_YCI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RegistrationEvents$6khMHTLplOoXis_hIpOeqk_YCI implements Predicate {
    public static final /* synthetic */ $$Lambda$RegistrationEvents$6khMHTLplOoXis_hIpOeqk_YCI INSTANCE = new $$Lambda$RegistrationEvents$6khMHTLplOoXis_hIpOeqk_YCI();

    private /* synthetic */ $$Lambda$RegistrationEvents$6khMHTLplOoXis_hIpOeqk_YCI() {
    }

    public final boolean test(Object obj) {
        return ((Field) obj).getType().isAssignableFrom(Integer.TYPE);
    }
}
