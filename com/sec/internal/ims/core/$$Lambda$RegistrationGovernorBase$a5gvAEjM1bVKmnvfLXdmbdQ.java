package com.sec.internal.ims.core;

import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$RegistrationGovernorBase$a5gv-AEjM-1bVKmnv-fLX-dmbdQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RegistrationGovernorBase$a5gvAEjM1bVKmnvfLXdmbdQ implements Predicate {
    public static final /* synthetic */ $$Lambda$RegistrationGovernorBase$a5gvAEjM1bVKmnvfLXdmbdQ INSTANCE = new $$Lambda$RegistrationGovernorBase$a5gvAEjM1bVKmnvfLXdmbdQ();

    private /* synthetic */ $$Lambda$RegistrationGovernorBase$a5gvAEjM1bVKmnvfLXdmbdQ() {
    }

    public final boolean test(Object obj) {
        return ((IRegisterTask) obj).isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.REGISTERING);
    }
}
