package com.squareup.okhttp.internal.http;

import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpController;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Challenge;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;

public final class AuthenticatorAdapter implements Authenticator {
    public static final Authenticator INSTANCE = new AuthenticatorAdapter();

    public Request authenticate(Proxy proxy, Response response) throws IOException {
        List<Challenge> challenges = response.challenges();
        Request request = response.request();
        HttpUrl url = request.httpUrl();
        int size = challenges.size();
        for (int i = 0; i < size; i++) {
            Challenge challenge = challenges.get(i);
            if (!WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME.equalsIgnoreCase(challenge.getScheme())) {
                Proxy proxy2 = proxy;
            } else {
                PasswordAuthentication auth = java.net.Authenticator.requestPasswordAuthentication(url.host(), getConnectToInetAddress(proxy, url), url.port(), url.scheme(), challenge.getRealm(), challenge.getScheme(), url.url(), Authenticator.RequestorType.SERVER);
                if (auth != null) {
                    return request.newBuilder().header("Authorization", Credentials.basic(auth.getUserName(), new String(auth.getPassword()))).build();
                }
            }
        }
        Proxy proxy3 = proxy;
        return null;
    }

    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        List<Challenge> challenges = response.challenges();
        Request request = response.request();
        HttpUrl url = request.httpUrl();
        int size = challenges.size();
        for (int i = 0; i < size; i++) {
            Challenge challenge = challenges.get(i);
            if (WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME.equalsIgnoreCase(challenge.getScheme())) {
                InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
                PasswordAuthentication auth = java.net.Authenticator.requestPasswordAuthentication(proxyAddress.getHostName(), getConnectToInetAddress(proxy, url), proxyAddress.getPort(), url.scheme(), challenge.getRealm(), challenge.getScheme(), url.url(), Authenticator.RequestorType.PROXY);
                if (auth != null) {
                    return request.newBuilder().header(HttpController.HEADER_PROXY_AUTHORIZATION, Credentials.basic(auth.getUserName(), new String(auth.getPassword()))).build();
                }
            }
        }
        return null;
    }

    private InetAddress getConnectToInetAddress(Proxy proxy, HttpUrl url) throws IOException {
        if (proxy == null || proxy.type() == Proxy.Type.DIRECT) {
            return InetAddress.getByName(url.host());
        }
        return ((InetSocketAddress) proxy.address()).getAddress();
    }
}
