package com.sec.internal.ims.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$RegistrationEvents$M5gCxB0Gq43JjzJXJ3zGNQv-IwU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RegistrationEvents$M5gCxB0Gq43JjzJXJ3zGNQvIwU implements Predicate {
    public static final /* synthetic */ $$Lambda$RegistrationEvents$M5gCxB0Gq43JjzJXJ3zGNQvIwU INSTANCE = new $$Lambda$RegistrationEvents$M5gCxB0Gq43JjzJXJ3zGNQvIwU();

    private /* synthetic */ $$Lambda$RegistrationEvents$M5gCxB0Gq43JjzJXJ3zGNQvIwU() {
    }

    public final boolean test(Object obj) {
        return Modifier.isStatic(((Field) obj).getModifiers());
    }
}
