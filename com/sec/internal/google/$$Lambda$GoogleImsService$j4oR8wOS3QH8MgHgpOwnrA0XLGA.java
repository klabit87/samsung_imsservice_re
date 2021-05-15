package com.sec.internal.google;

import com.sec.ims.util.NameAddr;
import com.sec.internal.helper.UriUtil;
import java.util.function.Predicate;

/* renamed from: com.sec.internal.google.-$$Lambda$GoogleImsService$j4oR8wOS3QH8MgHgpOwnrA0XLGA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GoogleImsService$j4oR8wOS3QH8MgHgpOwnrA0XLGA implements Predicate {
    public static final /* synthetic */ $$Lambda$GoogleImsService$j4oR8wOS3QH8MgHgpOwnrA0XLGA INSTANCE = new $$Lambda$GoogleImsService$j4oR8wOS3QH8MgHgpOwnrA0XLGA();

    private /* synthetic */ $$Lambda$GoogleImsService$j4oR8wOS3QH8MgHgpOwnrA0XLGA() {
    }

    public final boolean test(Object obj) {
        return UriUtil.hasMsisdnNumber(((NameAddr) obj).getUri());
    }
}
