package com.sec.internal.ims.cmstore;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpCookieParcelable;
import com.sec.internal.helper.httpclient.ParcelableUtil;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.log.IMSLog;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentHttpCookieStore implements CookieStore {
    private static final String COOKIE_NAME_PREFIX = "cookie_";
    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String LOG_TAG = "PersistentHttpCookieStore";
    private final SharedPreferences cookiePrefs;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, HttpCookie>> cookies = new ConcurrentHashMap<>();

    public PersistentHttpCookieStore(Context context) {
        HttpCookie decodedCookie;
        this.cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        for (Map.Entry<String, ?> entry : this.cookiePrefs.getAll().entrySet()) {
            if (entry.getValue() != null && !((String) entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                for (String name : TextUtils.split((String) entry.getValue(), ",")) {
                    String encodedCookie = this.cookiePrefs.getString(COOKIE_NAME_PREFIX + name, (String) null);
                    if (!(encodedCookie == null || (decodedCookie = decodeCookie(encodedCookie)) == null)) {
                        String domain = decodedCookie.getDomain();
                        if (TextUtils.isEmpty(domain)) {
                            decodedCookie.setDomain(entry.getKey());
                            domain = decodedCookie.getDomain();
                        }
                        if (!this.cookies.containsKey(domain)) {
                            this.cookies.put(domain, new ConcurrentHashMap());
                        }
                        this.cookies.get(domain).put(name, decodedCookie);
                    }
                }
            }
        }
    }

    public void add(URI uri, HttpCookie cookie) {
        IMSLog.d(LOG_TAG, "add - url: " + uri + ", cookie: " + cookie);
        if (TextUtils.isEmpty(cookie.getDomain())) {
            cookie.setDomain(uri.getHost());
        }
        if (ATTGlobalVariables.isAmbsPhaseIV()) {
            Log.i(LOG_TAG, "Before==================================");
            for (String domain : this.cookies.keySet()) {
                try {
                    for (String name : this.cookies.get(domain).keySet()) {
                        IMSLog.i(LOG_TAG, "Domain=" + domain + " ,name=" + name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            IMSLog.i(LOG_TAG, "==================================");
            List<String> replacedDomian = new ArrayList<>();
            for (HttpCookie cookie1 : getCookies()) {
                if (cookie.getName().equals(cookie1.getName())) {
                    String newDomain = cookie.getDomain();
                    String oldDomain = cookie1.getDomain();
                    if (!newDomain.equals(oldDomain)) {
                        if (newDomain.endsWith(oldDomain)) {
                            cookie.setDomain(oldDomain);
                        } else if (oldDomain.endsWith(newDomain) && !replacedDomian.contains(oldDomain)) {
                            replacedDomian.add(oldDomain);
                        }
                    }
                }
            }
            if (!replacedDomian.isEmpty()) {
                for (String domainName : replacedDomian) {
                    ConcurrentHashMap<String, HttpCookie> transferredValue = this.cookies.get(domainName);
                    if (transferredValue != null) {
                        if (!this.cookies.containsKey(cookie.getDomain())) {
                            this.cookies.put(cookie.getDomain(), new ConcurrentHashMap());
                        }
                        for (String name2 : transferredValue.keySet()) {
                            HttpCookie cookie2 = transferredValue.get(name2);
                            cookie2.setDomain(cookie.getDomain());
                            this.cookies.get(cookie.getDomain()).put(getCookieToken(cookie2), cookie2);
                        }
                    }
                    this.cookies.remove(domainName);
                }
            }
            IMSLog.i(LOG_TAG, "After==================================");
            for (String domain2 : this.cookies.keySet()) {
                try {
                    for (String name3 : this.cookies.get(domain2).keySet()) {
                        IMSLog.i(LOG_TAG, "Domain=" + domain2 + " ,name=" + name3);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return;
                }
            }
            IMSLog.i(LOG_TAG, "==================================");
        }
        String name4 = getCookieToken(cookie);
        if (!cookie.hasExpired()) {
            if (!this.cookies.containsKey(cookie.getDomain())) {
                this.cookies.put(cookie.getDomain(), new ConcurrentHashMap());
            }
            this.cookies.get(cookie.getDomain()).put(name4, cookie);
        } else if (this.cookies.containsKey(cookie.getDomain())) {
            this.cookies.get(cookie.getDomain()).remove(name4);
        }
        SharedPreferences.Editor prefsWriter = this.cookiePrefs.edit();
        prefsWriter.putString(cookie.getDomain(), TextUtils.join(",", this.cookies.get(cookie.getDomain()).keySet()));
        prefsWriter.putString(COOKIE_NAME_PREFIX + name4, encodeCookie(new HttpCookieParcelable(cookie)));
        prefsWriter.apply();
    }

    /* access modifiers changed from: protected */
    public String getCookieToken(HttpCookie cookie) {
        return cookie.getName() + cookie.getDomain();
    }

    public List<HttpCookie> get(URI uri) {
        ArrayList<HttpCookie> ret = new ArrayList<>();
        for (Map.Entry<String, ConcurrentHashMap<String, HttpCookie>> e : this.cookies.entrySet()) {
            if (uri.getHost().endsWith(e.getKey())) {
                ret.addAll(this.cookies.get(e.getKey()).values());
            }
        }
        IMSLog.d(LOG_TAG, "get - url: " + uri + "cookie: " + ret.toString());
        return ret;
    }

    public boolean removeAll() {
        SharedPreferences.Editor prefsWriter = this.cookiePrefs.edit();
        prefsWriter.clear();
        prefsWriter.apply();
        this.cookies.clear();
        return true;
    }

    public boolean remove(URI uri, HttpCookie cookie) {
        if (TextUtils.isEmpty(cookie.getDomain())) {
            cookie.setDomain(uri.getHost());
        }
        String name = getCookieToken(cookie);
        if (!this.cookies.containsKey(cookie.getDomain()) || !this.cookies.get(cookie.getDomain()).containsKey(name)) {
            return false;
        }
        this.cookies.get(cookie.getDomain()).remove(name);
        SharedPreferences.Editor prefsWriter = this.cookiePrefs.edit();
        SharedPreferences sharedPreferences = this.cookiePrefs;
        if (sharedPreferences.contains(COOKIE_NAME_PREFIX + name)) {
            prefsWriter.remove(COOKIE_NAME_PREFIX + name);
        }
        prefsWriter.putString(cookie.getDomain(), TextUtils.join(",", this.cookies.get(cookie.getDomain()).keySet()));
        prefsWriter.apply();
        return true;
    }

    public List<HttpCookie> getCookies() {
        ArrayList<HttpCookie> ret = new ArrayList<>();
        for (String key : this.cookies.keySet()) {
            ret.addAll(this.cookies.get(key).values());
        }
        return ret;
    }

    public List<URI> getURIs() {
        ArrayList<URI> ret = new ArrayList<>();
        for (String key : this.cookies.keySet()) {
            try {
                ret.add(new URI(key));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public String encodeCookie(HttpCookieParcelable cookie) {
        if (cookie == null) {
            return null;
        }
        return byteArrayToHexString(ParcelableUtil.marshall(cookie));
    }

    /* access modifiers changed from: protected */
    public HttpCookie decodeCookie(String cookieString) {
        return ((HttpCookieParcelable) ParcelableUtil.unmarshall(hexStringToByteArray(cookieString), HttpCookieParcelable.CREATOR)).getCookie();
    }

    /* access modifiers changed from: protected */
    public String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 255;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /* access modifiers changed from: protected */
    public byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
