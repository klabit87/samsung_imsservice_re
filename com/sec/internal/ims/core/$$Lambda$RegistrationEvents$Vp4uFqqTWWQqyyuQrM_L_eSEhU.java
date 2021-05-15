package com.sec.internal.ims.core;

import java.util.function.Consumer;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$RegistrationEvents$Vp4uFqqTWWQqyyuQrM_L_-eSEhU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RegistrationEvents$Vp4uFqqTWWQqyyuQrM_L_eSEhU implements Consumer {
    public static final /* synthetic */ $$Lambda$RegistrationEvents$Vp4uFqqTWWQqyyuQrM_L_eSEhU INSTANCE = new $$Lambda$RegistrationEvents$Vp4uFqqTWWQqyyuQrM_L_eSEhU();

    private /* synthetic */ $$Lambda$RegistrationEvents$Vp4uFqqTWWQqyyuQrM_L_eSEhU() {
    }

    public final void accept(Object obj) {
        ((RegisterTask) obj).getGovernor().onContactActivated();
    }
}
