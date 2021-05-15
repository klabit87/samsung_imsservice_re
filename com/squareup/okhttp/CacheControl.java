package com.squareup.okhttp;

import java.util.concurrent.TimeUnit;
import org.xbill.DNS.TTL;

public final class CacheControl {
    public static final CacheControl FORCE_CACHE = new Builder().onlyIfCached().maxStale(Integer.MAX_VALUE, TimeUnit.SECONDS).build();
    public static final CacheControl FORCE_NETWORK = new Builder().noCache().build();
    String headerValue;
    private final boolean isPrivate;
    private final boolean isPublic;
    private final int maxAgeSeconds;
    private final int maxStaleSeconds;
    private final int minFreshSeconds;
    private final boolean mustRevalidate;
    private final boolean noCache;
    private final boolean noStore;
    private final boolean noTransform;
    private final boolean onlyIfCached;
    private final int sMaxAgeSeconds;

    private CacheControl(boolean noCache2, boolean noStore2, int maxAgeSeconds2, int sMaxAgeSeconds2, boolean isPrivate2, boolean isPublic2, boolean mustRevalidate2, int maxStaleSeconds2, int minFreshSeconds2, boolean onlyIfCached2, boolean noTransform2, String headerValue2) {
        this.noCache = noCache2;
        this.noStore = noStore2;
        this.maxAgeSeconds = maxAgeSeconds2;
        this.sMaxAgeSeconds = sMaxAgeSeconds2;
        this.isPrivate = isPrivate2;
        this.isPublic = isPublic2;
        this.mustRevalidate = mustRevalidate2;
        this.maxStaleSeconds = maxStaleSeconds2;
        this.minFreshSeconds = minFreshSeconds2;
        this.onlyIfCached = onlyIfCached2;
        this.noTransform = noTransform2;
        this.headerValue = headerValue2;
    }

    private CacheControl(Builder builder) {
        this.noCache = builder.noCache;
        this.noStore = builder.noStore;
        this.maxAgeSeconds = builder.maxAgeSeconds;
        this.sMaxAgeSeconds = -1;
        this.isPrivate = false;
        this.isPublic = false;
        this.mustRevalidate = false;
        this.maxStaleSeconds = builder.maxStaleSeconds;
        this.minFreshSeconds = builder.minFreshSeconds;
        this.onlyIfCached = builder.onlyIfCached;
        this.noTransform = builder.noTransform;
    }

    public boolean noCache() {
        return this.noCache;
    }

    public boolean noStore() {
        return this.noStore;
    }

    public int maxAgeSeconds() {
        return this.maxAgeSeconds;
    }

    public int sMaxAgeSeconds() {
        return this.sMaxAgeSeconds;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public boolean mustRevalidate() {
        return this.mustRevalidate;
    }

    public int maxStaleSeconds() {
        return this.maxStaleSeconds;
    }

    public int minFreshSeconds() {
        return this.minFreshSeconds;
    }

    public boolean onlyIfCached() {
        return this.onlyIfCached;
    }

    public boolean noTransform() {
        return this.noTransform;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00b9  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.squareup.okhttp.CacheControl parse(com.squareup.okhttp.Headers r30) {
        /*
            r0 = r30
            r1 = 0
            r2 = 0
            r3 = -1
            r4 = -1
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = -1
            r9 = -1
            r10 = 0
            r11 = 0
            r12 = 1
            r13 = 0
            r14 = 0
            int r15 = r30.size()
        L_0x0014:
            if (r14 >= r15) goto L_0x019a
            r16 = r15
            java.lang.String r15 = r0.name(r14)
            r29 = r11
            java.lang.String r11 = r0.value(r14)
            java.lang.String r0 = "Cache-Control"
            boolean r0 = r15.equalsIgnoreCase(r0)
            if (r0 == 0) goto L_0x0030
            if (r13 == 0) goto L_0x002e
            r12 = 0
            goto L_0x0039
        L_0x002e:
            r13 = r11
            goto L_0x0039
        L_0x0030:
            java.lang.String r0 = "Pragma"
            boolean r0 = r15.equalsIgnoreCase(r0)
            if (r0 == 0) goto L_0x0190
            r12 = 0
        L_0x0039:
            r0 = 0
        L_0x003a:
            r17 = r1
            int r1 = r11.length()
            if (r0 >= r1) goto L_0x0187
            r1 = r0
            r18 = r2
            java.lang.String r2 = "=,;"
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.skipUntil(r11, r0, r2)
            java.lang.String r2 = r11.substring(r1, r0)
            java.lang.String r2 = r2.trim()
            r19 = r1
            int r1 = r11.length()
            if (r0 == r1) goto L_0x00a3
            char r1 = r11.charAt(r0)
            r20 = r3
            r3 = 44
            if (r1 == r3) goto L_0x00a5
            char r1 = r11.charAt(r0)
            r3 = 59
            if (r1 != r3) goto L_0x006e
            goto L_0x00a5
        L_0x006e:
            int r0 = r0 + 1
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.skipWhitespace(r11, r0)
            int r1 = r11.length()
            if (r0 >= r1) goto L_0x0093
            char r1 = r11.charAt(r0)
            r3 = 34
            if (r1 != r3) goto L_0x0093
            int r0 = r0 + 1
            r1 = r0
            java.lang.String r3 = "\""
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.skipUntil(r11, r0, r3)
            java.lang.String r3 = r11.substring(r1, r0)
            int r0 = r0 + 1
            goto L_0x00a8
        L_0x0093:
            r1 = r0
            java.lang.String r3 = ",;"
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.skipUntil(r11, r0, r3)
            java.lang.String r3 = r11.substring(r1, r0)
            java.lang.String r3 = r3.trim()
            goto L_0x00a8
        L_0x00a3:
            r20 = r3
        L_0x00a5:
            int r0 = r0 + 1
            r3 = 0
        L_0x00a8:
            java.lang.String r1 = "no-cache"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x00b9
            r1 = 1
            r21 = r0
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x00b9:
            java.lang.String r1 = "no-store"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x00cb
            r1 = 1
            r21 = r0
            r2 = r1
            r1 = r17
            r3 = r20
            goto L_0x0183
        L_0x00cb:
            java.lang.String r1 = "max-age"
            boolean r1 = r1.equalsIgnoreCase(r2)
            r21 = r0
            r0 = -1
            if (r1 == 0) goto L_0x00e1
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.parseSeconds(r3, r0)
            r3 = r0
            r1 = r17
            r2 = r18
            goto L_0x0183
        L_0x00e1:
            java.lang.String r1 = "s-maxage"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x00f7
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.parseSeconds(r3, r0)
            r4 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x00f7:
            java.lang.String r1 = "private"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x010a
            r0 = 1
            r5 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x010a:
            java.lang.String r1 = "public"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x011d
            r0 = 1
            r6 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x011d:
            java.lang.String r1 = "must-revalidate"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x012e
            r0 = 1
            r7 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x012e:
            java.lang.String r1 = "max-stale"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x0145
            r0 = 2147483647(0x7fffffff, float:NaN)
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.parseSeconds(r3, r0)
            r8 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x0145:
            java.lang.String r1 = "min-fresh"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x0159
            int r0 = com.squareup.okhttp.internal.http.HeaderParser.parseSeconds(r3, r0)
            r9 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x0159:
            java.lang.String r0 = "only-if-cached"
            boolean r0 = r0.equalsIgnoreCase(r2)
            if (r0 == 0) goto L_0x016b
            r0 = 1
            r10 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x016b:
            java.lang.String r0 = "no-transform"
            boolean r0 = r0.equalsIgnoreCase(r2)
            if (r0 == 0) goto L_0x017d
            r0 = 1
            r29 = r0
            r1 = r17
            r2 = r18
            r3 = r20
            goto L_0x0183
        L_0x017d:
            r1 = r17
            r2 = r18
            r3 = r20
        L_0x0183:
            r0 = r21
            goto L_0x003a
        L_0x0187:
            r18 = r2
            r20 = r3
            r1 = r17
            r11 = r29
            goto L_0x0192
        L_0x0190:
            r11 = r29
        L_0x0192:
            int r14 = r14 + 1
            r0 = r30
            r15 = r16
            goto L_0x0014
        L_0x019a:
            r29 = r11
            r16 = r15
            if (r12 != 0) goto L_0x01a1
            r13 = 0
        L_0x01a1:
            com.squareup.okhttp.CacheControl r0 = new com.squareup.okhttp.CacheControl
            r16 = r0
            r17 = r1
            r18 = r2
            r19 = r3
            r20 = r4
            r21 = r5
            r22 = r6
            r23 = r7
            r24 = r8
            r25 = r9
            r26 = r10
            r27 = r29
            r28 = r13
            r16.<init>(r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.okhttp.CacheControl.parse(com.squareup.okhttp.Headers):com.squareup.okhttp.CacheControl");
    }

    public String toString() {
        String result = this.headerValue;
        if (result != null) {
            return result;
        }
        String headerValue2 = headerValue();
        this.headerValue = headerValue2;
        return headerValue2;
    }

    private String headerValue() {
        StringBuilder result = new StringBuilder();
        if (this.noCache) {
            result.append("no-cache, ");
        }
        if (this.noStore) {
            result.append("no-store, ");
        }
        if (this.maxAgeSeconds != -1) {
            result.append("max-age=");
            result.append(this.maxAgeSeconds);
            result.append(", ");
        }
        if (this.sMaxAgeSeconds != -1) {
            result.append("s-maxage=");
            result.append(this.sMaxAgeSeconds);
            result.append(", ");
        }
        if (this.isPrivate) {
            result.append("private, ");
        }
        if (this.isPublic) {
            result.append("public, ");
        }
        if (this.mustRevalidate) {
            result.append("must-revalidate, ");
        }
        if (this.maxStaleSeconds != -1) {
            result.append("max-stale=");
            result.append(this.maxStaleSeconds);
            result.append(", ");
        }
        if (this.minFreshSeconds != -1) {
            result.append("min-fresh=");
            result.append(this.minFreshSeconds);
            result.append(", ");
        }
        if (this.onlyIfCached) {
            result.append("only-if-cached, ");
        }
        if (this.noTransform) {
            result.append("no-transform, ");
        }
        if (result.length() == 0) {
            return "";
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }

    public static final class Builder {
        int maxAgeSeconds = -1;
        int maxStaleSeconds = -1;
        int minFreshSeconds = -1;
        boolean noCache;
        boolean noStore;
        boolean noTransform;
        boolean onlyIfCached;

        public Builder noCache() {
            this.noCache = true;
            return this;
        }

        public Builder noStore() {
            this.noStore = true;
            return this;
        }

        public Builder maxAge(int maxAge, TimeUnit timeUnit) {
            if (maxAge >= 0) {
                long maxAgeSecondsLong = timeUnit.toSeconds((long) maxAge);
                this.maxAgeSeconds = maxAgeSecondsLong > TTL.MAX_VALUE ? Integer.MAX_VALUE : (int) maxAgeSecondsLong;
                return this;
            }
            throw new IllegalArgumentException("maxAge < 0: " + maxAge);
        }

        public Builder maxStale(int maxStale, TimeUnit timeUnit) {
            if (maxStale >= 0) {
                long maxStaleSecondsLong = timeUnit.toSeconds((long) maxStale);
                this.maxStaleSeconds = maxStaleSecondsLong > TTL.MAX_VALUE ? Integer.MAX_VALUE : (int) maxStaleSecondsLong;
                return this;
            }
            throw new IllegalArgumentException("maxStale < 0: " + maxStale);
        }

        public Builder minFresh(int minFresh, TimeUnit timeUnit) {
            if (minFresh >= 0) {
                long minFreshSecondsLong = timeUnit.toSeconds((long) minFresh);
                this.minFreshSeconds = minFreshSecondsLong > TTL.MAX_VALUE ? Integer.MAX_VALUE : (int) minFreshSecondsLong;
                return this;
            }
            throw new IllegalArgumentException("minFresh < 0: " + minFresh);
        }

        public Builder onlyIfCached() {
            this.onlyIfCached = true;
            return this;
        }

        public Builder noTransform() {
            this.noTransform = true;
            return this;
        }

        public CacheControl build() {
            return new CacheControl(this);
        }
    }
}
