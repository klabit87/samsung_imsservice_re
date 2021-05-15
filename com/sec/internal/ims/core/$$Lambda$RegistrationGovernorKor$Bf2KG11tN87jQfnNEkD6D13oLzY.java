package com.sec.internal.ims.core;

import android.os.Bundle;
import java.util.function.Function;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$RegistrationGovernorKor$Bf2KG11tN87jQfnNEkD6D13oLzY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RegistrationGovernorKor$Bf2KG11tN87jQfnNEkD6D13oLzY implements Function {
    public static final /* synthetic */ $$Lambda$RegistrationGovernorKor$Bf2KG11tN87jQfnNEkD6D13oLzY INSTANCE = new $$Lambda$RegistrationGovernorKor$Bf2KG11tN87jQfnNEkD6D13oLzY();

    private /* synthetic */ $$Lambda$RegistrationGovernorKor$Bf2KG11tN87jQfnNEkD6D13oLzY() {
    }

    public final Object apply(Object obj) {
        return Boolean.valueOf(((Bundle) obj).getBoolean("state", false));
    }
}
