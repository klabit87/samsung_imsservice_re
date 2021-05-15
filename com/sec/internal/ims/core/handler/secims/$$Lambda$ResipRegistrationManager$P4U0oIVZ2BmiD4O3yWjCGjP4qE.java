package com.sec.internal.ims.core.handler.secims;

import java.util.function.Function;

/* renamed from: com.sec.internal.ims.core.handler.secims.-$$Lambda$ResipRegistrationManager$P4U0oIV-Z2BmiD4O3yWjCGjP4qE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ResipRegistrationManager$P4U0oIVZ2BmiD4O3yWjCGjP4qE implements Function {
    public static final /* synthetic */ $$Lambda$ResipRegistrationManager$P4U0oIVZ2BmiD4O3yWjCGjP4qE INSTANCE = new $$Lambda$ResipRegistrationManager$P4U0oIVZ2BmiD4O3yWjCGjP4qE();

    private /* synthetic */ $$Lambda$ResipRegistrationManager$P4U0oIVZ2BmiD4O3yWjCGjP4qE() {
    }

    public final Object apply(Object obj) {
        return Boolean.valueOf(((UserAgent) obj).isRegistered(false));
    }
}
