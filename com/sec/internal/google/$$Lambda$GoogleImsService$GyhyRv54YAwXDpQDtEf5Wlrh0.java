package com.sec.internal.google;

import android.net.Uri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.helper.UriUtil;
import java.util.function.Function;

/* renamed from: com.sec.internal.google.-$$Lambda$GoogleImsService$GyhyR-v54YAwXDpQD-tEf5Wlrh0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GoogleImsService$GyhyRv54YAwXDpQDtEf5Wlrh0 implements Function {
    public static final /* synthetic */ $$Lambda$GoogleImsService$GyhyRv54YAwXDpQDtEf5Wlrh0 INSTANCE = new $$Lambda$GoogleImsService$GyhyRv54YAwXDpQDtEf5Wlrh0();

    private /* synthetic */ $$Lambda$GoogleImsService$GyhyRv54YAwXDpQDtEf5Wlrh0() {
    }

    public final Object apply(Object obj) {
        return Uri.parse("tel:" + UriUtil.getMsisdnNumber(((NameAddr) obj).getUri()));
    }
}
