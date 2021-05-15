package com.sec.internal.ims.aec.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/* renamed from: com.sec.internal.ims.aec.util.-$$Lambda$HttpClient$o2W_9uDYzJgp7ktnGMKhRurmusU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HttpClient$o2W_9uDYzJgp7ktnGMKhRurmusU implements HostnameVerifier {
    public static final /* synthetic */ $$Lambda$HttpClient$o2W_9uDYzJgp7ktnGMKhRurmusU INSTANCE = new $$Lambda$HttpClient$o2W_9uDYzJgp7ktnGMKhRurmusU();

    private /* synthetic */ $$Lambda$HttpClient$o2W_9uDYzJgp7ktnGMKhRurmusU() {
    }

    public final boolean verify(String str, SSLSession sSLSession) {
        return HttpClient.lambda$static$0(str, sSLSession);
    }
}
