package com.sec.internal.log;

import java.nio.file.Path;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.log.-$$Lambda$EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$gaQJIM5t1NJpsOLIWU7wD9LSf1I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$gaQJIM5t1NJpsOLIWU7wD9LSf1I implements Predicate {
    public static final /* synthetic */ $$Lambda$EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$gaQJIM5t1NJpsOLIWU7wD9LSf1I INSTANCE = new $$Lambda$EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$gaQJIM5t1NJpsOLIWU7wD9LSf1I();

    private /* synthetic */ $$Lambda$EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$gaQJIM5t1NJpsOLIWU7wD9LSf1I() {
    }

    public final boolean test(Object obj) {
        return ((Path) obj).toString().contains("main");
    }
}
