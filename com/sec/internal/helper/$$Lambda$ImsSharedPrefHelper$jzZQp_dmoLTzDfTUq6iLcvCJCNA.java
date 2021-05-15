package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Consumer;

/* renamed from: com.sec.internal.helper.-$$Lambda$ImsSharedPrefHelper$jzZQp_dmoLTzDfTUq6iLcvCJCNA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImsSharedPrefHelper$jzZQp_dmoLTzDfTUq6iLcvCJCNA implements Consumer {
    public static final /* synthetic */ $$Lambda$ImsSharedPrefHelper$jzZQp_dmoLTzDfTUq6iLcvCJCNA INSTANCE = new $$Lambda$ImsSharedPrefHelper$jzZQp_dmoLTzDfTUq6iLcvCJCNA();

    private /* synthetic */ $$Lambda$ImsSharedPrefHelper$jzZQp_dmoLTzDfTUq6iLcvCJCNA() {
    }

    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().clear().apply();
    }
}
