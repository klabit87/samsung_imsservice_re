package com.sec.internal.ims.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$RegistrationEvents$XQRhlIuebyxD9BrsaAiuVmp8nN4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RegistrationEvents$XQRhlIuebyxD9BrsaAiuVmp8nN4 implements Predicate {
    public static final /* synthetic */ $$Lambda$RegistrationEvents$XQRhlIuebyxD9BrsaAiuVmp8nN4 INSTANCE = new $$Lambda$RegistrationEvents$XQRhlIuebyxD9BrsaAiuVmp8nN4();

    private /* synthetic */ $$Lambda$RegistrationEvents$XQRhlIuebyxD9BrsaAiuVmp8nN4() {
    }

    public final boolean test(Object obj) {
        return Modifier.isFinal(((Field) obj).getModifiers());
    }
}
