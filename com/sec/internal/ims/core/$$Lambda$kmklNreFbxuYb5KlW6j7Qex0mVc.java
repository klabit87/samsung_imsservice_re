package com.sec.internal.ims.core;

import com.sec.ims.settings.ImsProfile;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.ims.core.-$$Lambda$kmklNreFbxuYb5KlW6j7Qex0mVc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$kmklNreFbxuYb5KlW6j7Qex0mVc implements Predicate {
    public static final /* synthetic */ $$Lambda$kmklNreFbxuYb5KlW6j7Qex0mVc INSTANCE = new $$Lambda$kmklNreFbxuYb5KlW6j7Qex0mVc();

    private /* synthetic */ $$Lambda$kmklNreFbxuYb5KlW6j7Qex0mVc() {
    }

    public final boolean test(Object obj) {
        return ImsProfile.hasChatService((ImsProfile) obj);
    }
}
