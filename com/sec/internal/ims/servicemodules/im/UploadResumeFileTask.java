package com.sec.internal.ims.servicemodules.im;

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Process;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.im.UploadFileTask;
import com.sec.internal.ims.servicemodules.im.data.info.FtHttpResumeInfo;

public class UploadResumeFileTask extends UploadFileTask {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = UploadResumeFileTask.class.getSimpleName();

    public UploadResumeFileTask(int phoneId) {
        super(phoneId);
        String str = LOG_TAG;
        Log.i(str, "phoneId: " + phoneId);
    }

    private String getRequestUrl() {
        Uri requestUri = Uri.parse(this.mRequest.mUrl);
        if (requestUri.getPath() == null || requestUri.getPath().equals("/")) {
            if (this.mRequest.mUrl.endsWith("/")) {
                return this.mRequest.mUrl;
            }
            return this.mRequest.mUrl + "/";
        } else if (this.mRequest.mUrl.endsWith("/")) {
            return this.mRequest.mUrl.substring(0, this.mRequest.mUrl.length() - 1);
        } else {
            return this.mRequest.mUrl;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:70:0x022e, code lost:
        if (r1.mHttpRequest != null) goto L_0x0252;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0241, code lost:
        if (r1.mHttpRequest == null) goto L_0x0290;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0250, code lost:
        if (r1.mHttpRequest == null) goto L_0x0290;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0252, code lost:
        r1.mHttpRequest.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x028d, code lost:
        if (r1.mHttpRequest == null) goto L_0x0290;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0290, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sec.internal.ims.servicemodules.im.data.info.FtHttpResumeInfo getUploadInfo() {
        /*
            r19 = this;
            r1 = r19
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "?tid="
            r2.append(r3)
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r3 = r1.mRequest
            java.lang.String r3 = r3.mTid
            r2.append(r3)
            java.lang.String r3 = "&get_upload_info"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r3 = r1.mRequest
            java.lang.String r4 = r19.getRequestUrl()
            r3.mUrl = r4
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "getUploadInfo: params="
            r4.append(r5)
            r4.append(r2)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            r3 = 0
            r1.mHttpRequest = r3
            r4 = 3
            r5 = 0
            r6 = 1
            r7 = -1
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r8.<init>()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r9 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r9 = r9.mUrl     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r8.append(r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r8.append(r2)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r8 = r8.toString()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r8 = com.sec.internal.helper.HttpRequest.get(r8)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.mHttpRequest = r8     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r19.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r8 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r9 = "mHttpRequest is null"
            if (r8 != 0) goto L_0x0078
            java.lang.String r8 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            android.util.Log.e(r8, r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r8 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.cancelRequest(r8, r4, r7, r6)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x0077
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            r4.disconnect()
        L_0x0077:
            return r3
        L_0x0078:
            com.sec.internal.helper.HttpRequest r8 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            int r8 = r8.code()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r10 = " "
            r11 = 200(0xc8, float:2.8E-43)
            if (r8 == r11) goto L_0x01bf
            r12 = 302(0x12e, float:4.23E-43)
            if (r8 == r12) goto L_0x015f
            r12 = 401(0x191, float:5.62E-43)
            if (r8 == r12) goto L_0x00ef
            r9 = 503(0x1f7, float:7.05E-43)
            if (r8 == r9) goto L_0x00c8
            java.lang.String r9 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r11.<init>()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r12 = "getUploadInfo: Receive "
            r11.append(r12)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r12 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            int r12 = r12.code()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r11.append(r12)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r11.append(r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r10 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r10 = r10.message()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r11.append(r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r10 = r11.toString()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            android.util.Log.e(r9, r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.cancelRequest(r9, r6)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x00c7
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            r4.disconnect()
        L_0x00c7:
            return r3
        L_0x00c8:
            java.lang.String r9 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r10 = "Receive 503 Unavailable"
            android.util.Log.e(r9, r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r9 = r1.mMnoStrategy     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r10 = "fthttp_upload_resume_from_the_start"
            boolean r9 = r9.boolSetting(r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            if (r9 == 0) goto L_0x00df
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r9 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.cancelRequest(r9, r4, r8, r6)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            goto L_0x00e4
        L_0x00df:
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.cancelRequest(r9, r5)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
        L_0x00e4:
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x00ee
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            r4.disconnect()
        L_0x00ee:
            return r3
        L_0x00ef:
            int r12 = r1.mPhoneId     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r13 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r14.<init>()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r15 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r15 = r15.mUrl     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r14.append(r15)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r14.append(r2)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r14 = r14.toString()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r15 = "GET"
            java.lang.String r12 = r1.getAuthorizationHeader(r12, r13, r14, r15)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            boolean r13 = android.text.TextUtils.isEmpty(r12)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            if (r13 == 0) goto L_0x0114
            goto L_0x01c7
        L_0x0114:
            com.sec.internal.helper.HttpRequest r13 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r13.disconnect()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r13.<init>()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r14 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r14 = r14.mUrl     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r13.append(r14)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r13.append(r2)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r13 = r13.toString()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r13 = com.sec.internal.helper.HttpRequest.get(r13)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.mHttpRequest = r13     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r19.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r13 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r13 = r13.authorization(r12)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r13.chunk(r5)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r13 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            if (r13 != 0) goto L_0x0157
            java.lang.String r10 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            android.util.Log.e(r10, r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r9 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.cancelRequest(r9, r4, r7, r6)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x0156
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            r4.disconnect()
        L_0x0156:
            return r3
        L_0x0157:
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            int r9 = r9.code()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r8 = r9
            goto L_0x01c7
        L_0x015f:
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r12 = "Location"
            java.lang.String r15 = r9.header(r12)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            boolean r9 = android.text.TextUtils.isEmpty(r15)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            if (r9 != 0) goto L_0x01a8
            com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest r9 = new com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            int r14 = r1.mPhoneId     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r12 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            android.net.Network r12 = r12.mNetwork     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r13 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r13 = r13.mUserAgent     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            boolean r4 = r4.mTrustAllCerts     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r17 = r13
            r13 = r9
            r16 = r12
            r18 = r4
            r13.<init>(r14, r15, r16, r17, r18)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r4 = com.sec.internal.ims.util.OpenIdAuth.sendAuthRequest(r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            if (r4 == 0) goto L_0x01a8
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r9.disconnect()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r9 = com.sec.internal.helper.HttpRequest.get(r4)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.mHttpRequest = r9     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r19.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r9.chunk(r5)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            int r9 = r9.code()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r8 = r9
            goto L_0x01c7
        L_0x01a8:
            java.lang.String r4 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r9 = "getUploadInfo: openId process failed"
            android.util.Log.e(r4, r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.cancelRequest(r4, r6)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x01be
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            r4.disconnect()
        L_0x01be:
            return r3
        L_0x01bf:
            java.lang.String r4 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r9 = "Receive 200 OK"
            android.util.Log.i(r4, r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
        L_0x01c7:
            if (r11 != r8) goto L_0x01ff
            java.lang.String r4 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r9 = "getUploadInfo: Success"
            android.util.Log.i(r4, r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r4 = r4.body()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r9 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r9.disconnect()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r9 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r10.<init>()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r11 = "getUploadInfo: Received. body="
            r10.append(r11)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r10.append(r4)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r10 = r10.toString()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.log.IMSLog.s(r9, r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.ims.servicemodules.im.data.info.FtHttpResumeInfo r3 = com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser.parseResume(r4)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r5 = r1.mHttpRequest
            if (r5 == 0) goto L_0x01fe
            com.sec.internal.helper.HttpRequest r5 = r1.mHttpRequest
            r5.disconnect()
        L_0x01fe:
            return r3
        L_0x01ff:
            java.lang.String r4 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r9.<init>()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r11 = "getUploadInfo: Failed, Receive "
            r9.append(r11)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r11 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            int r11 = r11.code()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r9.append(r11)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r9.append(r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r10 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r10 = r10.message()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r9.append(r10)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            java.lang.String r9 = r9.toString()     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            android.util.Log.e(r4, r9)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            r1.cancelRequest(r4, r6)     // Catch:{ HttpRequestException -> 0x0258, NullPointerException | OutOfMemoryError -> 0x0244, IOException | XmlPullParserException -> 0x0234 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x0290
            goto L_0x0252
        L_0x0231:
            r0 = move-exception
            r3 = r0
            goto L_0x0291
        L_0x0234:
            r0 = move-exception
            r4 = r0
            r4.printStackTrace()     // Catch:{ all -> 0x0231 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r5 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0231 }
            r8 = 3
            r1.cancelRequest(r5, r8, r7, r6)     // Catch:{ all -> 0x0231 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x0290
            goto L_0x0252
        L_0x0244:
            r0 = move-exception
            r4 = r0
            r4.printStackTrace()     // Catch:{ all -> 0x0231 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r6 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0231 }
            r1.cancelRequest(r6, r7, r7, r5)     // Catch:{ all -> 0x0231 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x0290
        L_0x0252:
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            r4.disconnect()
            goto L_0x0290
        L_0x0258:
            r0 = move-exception
            r4 = r0
            r4.printStackTrace()     // Catch:{ all -> 0x0231 }
            boolean r6 = r1.isPermanentFailCause(r4)     // Catch:{ all -> 0x0231 }
            if (r6 == 0) goto L_0x026b
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r6 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0231 }
            r8 = 30
            r1.cancelRequest(r6, r8, r7, r5)     // Catch:{ all -> 0x0231 }
            goto L_0x028b
        L_0x026b:
            java.lang.String r6 = LOG_TAG     // Catch:{ all -> 0x0231 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0231 }
            r8.<init>()     // Catch:{ all -> 0x0231 }
            java.io.IOException r9 = r4.getCause()     // Catch:{ all -> 0x0231 }
            r8.append(r9)     // Catch:{ all -> 0x0231 }
            java.lang.String r9 = " happened. Retry Upload Task."
            r8.append(r9)     // Catch:{ all -> 0x0231 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0231 }
            android.util.Log.e(r6, r8)     // Catch:{ all -> 0x0231 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r6 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0231 }
            r8 = 3
            r1.cancelRequest(r6, r8, r7, r5)     // Catch:{ all -> 0x0231 }
        L_0x028b:
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x0290
            goto L_0x0252
        L_0x0290:
            return r3
        L_0x0291:
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            if (r4 == 0) goto L_0x029a
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            r4.disconnect()
        L_0x029a:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.UploadResumeFileTask.getUploadInfo():com.sec.internal.ims.servicemodules.im.data.info.FtHttpResumeInfo");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:124:0x02dd, code lost:
        if (r1.mHttpRequest == null) goto L_0x0328;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x02df, code lost:
        r1.mHttpRequest.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x0325, code lost:
        if (r1.mHttpRequest != null) goto L_0x02df;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0328, code lost:
        return r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x034d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x034e, code lost:
        r18 = r7;
        r16 = r19;
        r12 = false;
        r4 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0355, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0356, code lost:
        r18 = r7;
        r16 = r19;
        r12 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x02f6 A[Catch:{ all -> 0x0329 }] */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x02ff A[Catch:{ all -> 0x0329 }] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x0355 A[ExcHandler: IOException (e java.io.IOException), Splitter:B:35:0x014d] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:118:0x02cc=Splitter:B:118:0x02cc, B:128:0x02ed=Splitter:B:128:0x02ed} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doResumeFile(java.net.URL r26, long r27, long r29, long r31) {
        /*
            r25 = this;
            r1 = r25
            r9 = r27
            r11 = r29
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "doResumeFile: "
            r2.append(r3)
            r2.append(r9)
            java.lang.String r3 = " - "
            r2.append(r3)
            r2.append(r11)
            java.lang.String r3 = " / "
            r2.append(r3)
            r13 = r31
            r2.append(r13)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r0, r2)
            r0 = 0
            r1.mHttpRequest = r0
            com.sec.internal.helper.HttpRequest r0 = com.sec.internal.helper.HttpRequest.put((java.net.URL) r26)
            r1.mHttpRequest = r0
            r25.setDefaultHeaders()
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest
            r3 = r27
            r5 = r29
            r7 = r31
            r2.contentRange(r3, r5, r7)
            r7 = 0
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ Exception -> 0x03b7 }
            int r0 = r0.code()     // Catch:{ Exception -> 0x03b7 }
            r8 = r0
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Receive "
            r2.append(r3)
            r2.append(r8)
            java.lang.String r5 = " "
            r2.append(r5)
            com.sec.internal.helper.HttpRequest r3 = r1.mHttpRequest
            java.lang.String r3 = r3.message()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r0, r2)
            r0 = 200(0xc8, float:2.8E-43)
            r6 = 1
            java.lang.String r4 = " happened. Retry Upload Task."
            if (r8 == r0) goto L_0x0129
            r0 = 302(0x12e, float:4.23E-43)
            if (r8 == r0) goto L_0x00d1
            r0 = 401(0x191, float:5.62E-43)
            if (r8 == r0) goto L_0x009f
            r0 = 404(0x194, float:5.66E-43)
            if (r8 == r0) goto L_0x0094
            r0 = 410(0x19a, float:5.75E-43)
            if (r8 == r0) goto L_0x0094
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r0.disconnect()     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r1.cancelRequest(r0, r7)     // Catch:{ HttpRequestException -> 0x0120 }
            return r7
        L_0x0094:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r0.disconnect()     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r1.cancelRequest(r0, r6)     // Catch:{ HttpRequestException -> 0x0120 }
            return r7
        L_0x009f:
            int r0 = r1.mPhoneId     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            java.lang.String r3 = r26.toString()     // Catch:{ HttpRequestException -> 0x0120 }
            java.lang.String r6 = "PUT"
            java.lang.String r0 = r1.getAuthorizationHeader(r0, r2, r3, r6)     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r2.disconnect()     // Catch:{ HttpRequestException -> 0x0120 }
            boolean r2 = android.text.TextUtils.isEmpty(r0)     // Catch:{ HttpRequestException -> 0x0120 }
            if (r2 == 0) goto L_0x00be
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r1.cancelRequest(r2, r7)     // Catch:{ HttpRequestException -> 0x0120 }
            return r7
        L_0x00be:
            com.sec.internal.helper.HttpRequest r2 = com.sec.internal.helper.HttpRequest.put((java.net.URL) r26)     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r3 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0120 }
            android.net.Network r3 = r3.mNetwork     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r2 = r2.useNetwork(r3)     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r2 = r2.authorization(r0)     // Catch:{ HttpRequestException -> 0x0120 }
            r1.mHttpRequest = r2     // Catch:{ HttpRequestException -> 0x0120 }
            goto L_0x0130
        L_0x00d1:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            java.lang.String r2 = "Location"
            java.lang.String r21 = r0.header(r2)     // Catch:{ HttpRequestException -> 0x0120 }
            boolean r0 = android.text.TextUtils.isEmpty(r21)     // Catch:{ HttpRequestException -> 0x0120 }
            if (r0 != 0) goto L_0x010e
            com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest r0 = new com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest     // Catch:{ HttpRequestException -> 0x0120 }
            int r2 = r1.mPhoneId     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r3 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0120 }
            android.net.Network r3 = r3.mNetwork     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r6 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0120 }
            java.lang.String r6 = r6.mUserAgent     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r15 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0120 }
            boolean r15 = r15.mTrustAllCerts     // Catch:{ HttpRequestException -> 0x0120 }
            r19 = r0
            r20 = r2
            r22 = r3
            r23 = r6
            r24 = r15
            r19.<init>(r20, r21, r22, r23, r24)     // Catch:{ HttpRequestException -> 0x0120 }
            java.lang.String r0 = com.sec.internal.ims.util.OpenIdAuth.sendAuthRequest(r0)     // Catch:{ HttpRequestException -> 0x0120 }
            if (r0 == 0) goto L_0x010e
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r2.disconnect()     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r2 = com.sec.internal.helper.HttpRequest.put((java.lang.CharSequence) r0)     // Catch:{ HttpRequestException -> 0x0120 }
            r1.mHttpRequest = r2     // Catch:{ HttpRequestException -> 0x0120 }
            goto L_0x0130
        L_0x010e:
            java.lang.String r0 = LOG_TAG     // Catch:{ HttpRequestException -> 0x0120 }
            java.lang.String r2 = "doResumeFile: OpenId process failed!"
            android.util.Log.e(r0, r2)     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r0.disconnect()     // Catch:{ HttpRequestException -> 0x0120 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0120 }
            r1.cancelRequest(r0, r7)     // Catch:{ HttpRequestException -> 0x0120 }
            return r7
        L_0x0120:
            r0 = move-exception
            r15 = r4
            r12 = r7
            r16 = r8
            r3 = 30
            goto L_0x0387
        L_0x0129:
            com.sec.internal.helper.HttpRequest r0 = com.sec.internal.helper.HttpRequest.put((java.net.URL) r26)     // Catch:{ HttpRequestException -> 0x0380 }
            r1.mHttpRequest = r0     // Catch:{ HttpRequestException -> 0x0380 }
        L_0x0130:
            r2 = 512000(0x7d000, double:2.529616E-318)
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r0 = r1.mRequest
            r19 = r8
            long r7 = r0.mTotalBytes
            r20 = 20
            long r7 = r7 / r20
            r6 = r4
            r0 = r5
            r4 = 61440(0xf000, double:3.03554E-319)
            long r4 = java.lang.Math.max(r7, r4)
            long r2 = java.lang.Math.min(r2, r4)
            int r7 = (int) r2
            r2 = 0
            java.io.BufferedInputStream r3 = new java.io.BufferedInputStream     // Catch:{ FileNotFoundException -> 0x0370, IOException -> 0x0355 }
            java.io.FileInputStream r4 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x034d, IOException -> 0x0355 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r5 = r1.mRequest     // Catch:{ FileNotFoundException -> 0x034d, IOException -> 0x0355 }
            java.lang.String r5 = r5.mFilePath     // Catch:{ FileNotFoundException -> 0x034d, IOException -> 0x0355 }
            r4.<init>(r5)     // Catch:{ FileNotFoundException -> 0x034d, IOException -> 0x0355 }
            r3.<init>(r4, r7)     // Catch:{ FileNotFoundException -> 0x034d, IOException -> 0x0355 }
            r8 = r3
            long r2 = r8.skip(r9)     // Catch:{ FileNotFoundException -> 0x0343, IOException -> 0x033a }
            int r4 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            if (r4 >= 0) goto L_0x01c3
            r8.close()     // Catch:{ IOException -> 0x0172, FileNotFoundException -> 0x0168 }
            goto L_0x0178
        L_0x0168:
            r0 = move-exception
            r18 = r7
            r2 = r8
            r16 = r19
            r4 = -1
            r12 = 0
            goto L_0x0377
        L_0x0172:
            r0 = move-exception
            r4 = r0
            r0 = r4
            r0.printStackTrace()     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
        L_0x0178:
            java.lang.String r0 = LOG_TAG     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            r4.<init>()     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            java.lang.String r5 = "Try to skip "
            r4.append(r5)     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            r4.append(r9)     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            java.lang.String r5 = " bytes. "
            r4.append(r5)     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            r4.append(r2)     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            java.lang.String r5 = " bytes actually skipped"
            r4.append(r5)     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            java.lang.String r4 = r4.toString()     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            android.util.Log.i(r0, r4)     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ FileNotFoundException -> 0x01b8, IOException -> 0x01ae }
            r4 = -1
            r15 = 0
            r1.cancelRequest(r0, r4, r4, r15)     // Catch:{ FileNotFoundException -> 0x01a5, IOException -> 0x01a3 }
            return r15
        L_0x01a3:
            r0 = move-exception
            goto L_0x01b0
        L_0x01a5:
            r0 = move-exception
            r18 = r7
            r2 = r8
            r12 = r15
            r16 = r19
            goto L_0x0377
        L_0x01ae:
            r0 = move-exception
            r15 = 0
        L_0x01b0:
            r18 = r7
            r2 = r8
            r12 = r15
            r16 = r19
            goto L_0x035b
        L_0x01b8:
            r0 = move-exception
            r15 = 0
            r18 = r7
            r2 = r8
            r12 = r15
            r16 = r19
            r4 = -1
            goto L_0x0377
        L_0x01c3:
            r15 = 0
            java.io.File r2 = new java.io.File     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r3 = r1.mRequest     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            java.lang.String r3 = r3.mFilePath     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            r2.<init>(r3)     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            r20 = r2
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r2 = r1.mRequest     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            java.lang.String r2 = r2.mContentType     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            if (r2 == 0) goto L_0x01f8
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r2 = r1.mRequest     // Catch:{ HttpRequestException -> 0x01ee, IllegalStateException -> 0x01e5, all -> 0x01dc }
            java.lang.String r2 = r2.mContentType     // Catch:{ HttpRequestException -> 0x01ee, IllegalStateException -> 0x01e5, all -> 0x01dc }
            r5 = r2
            goto L_0x01fd
        L_0x01dc:
            r0 = move-exception
            r2 = r0
            r18 = r7
            r11 = r8
            r16 = r19
            goto L_0x032b
        L_0x01e5:
            r0 = move-exception
            r18 = r7
            r11 = r8
            r12 = r15
            r16 = r19
            goto L_0x02cc
        L_0x01ee:
            r0 = move-exception
            r18 = r7
            r11 = r8
            r12 = r15
            r16 = r19
            r15 = r6
            goto L_0x02ed
        L_0x01f8:
            java.lang.String r2 = com.sec.internal.ims.servicemodules.im.ImCache.getContentType(r20)     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            r5 = r2
        L_0x01fd:
            r25.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            com.sec.internal.helper.HttpRequest r2 = r2.bufferSize(r7)     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            com.sec.internal.helper.HttpRequest r2 = r2.contentType(r5)     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            long r3 = r11 - r9
            r21 = 1
            long r3 = r3 + r21
            java.lang.String r3 = java.lang.Long.toString(r3)     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            com.sec.internal.helper.HttpRequest r2 = r2.contentLength((java.lang.String) r3)     // Catch:{ HttpRequestException -> 0x02e5, IllegalStateException -> 0x02c5, all -> 0x02bc }
            r3 = 3
            r11 = r3
            r15 = r6
            r6 = 30
            r3 = r27
            r12 = r5
            r17 = 1
            r5 = r29
            r18 = r7
            r11 = r8
            r21 = r12
            r16 = r19
            r12 = 0
            r7 = r31
            com.sec.internal.helper.HttpRequest r2 = r2.contentRange(r3, r5, r7)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            com.sec.internal.ims.servicemodules.im.UploadResumeFileTask$1 r3 = new com.sec.internal.ims.servicemodules.im.UploadResumeFileTask$1     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r3.<init>(r9)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r2.progress(r3)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r2.send((java.io.InputStream) r11)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            boolean r2 = r25.isCancelled()     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            if (r2 == 0) goto L_0x0255
            r11.close()     // Catch:{ IOException -> 0x024a }
            goto L_0x024b
        L_0x024a:
            r0 = move-exception
        L_0x024b:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x0254
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            r0.disconnect()
        L_0x0254:
            return r12
        L_0x0255:
            java.lang.String r2 = LOG_TAG     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            java.lang.String r3 = "Upload file done. Read http response."
            android.util.Log.i(r2, r3)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            boolean r2 = r2.ok()     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            if (r2 != 0) goto L_0x02a1
            java.lang.String r2 = LOG_TAG     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r3.<init>()     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            java.lang.String r4 = "doResumeFile: Failed, "
            r3.append(r4)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            int r4 = r4.code()     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r3.append(r4)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r3.append(r0)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            java.lang.String r0 = r0.message()     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r3.append(r0)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            java.lang.String r0 = r3.toString()     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            android.util.Log.e(r2, r0)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r1.cancelRequest(r0, r12)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r11.close()     // Catch:{ IOException -> 0x0296 }
            goto L_0x0297
        L_0x0296:
            r0 = move-exception
        L_0x0297:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x02a0
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            r0.disconnect()
        L_0x02a0:
            return r12
        L_0x02a1:
            java.lang.String r0 = LOG_TAG     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            java.lang.String r2 = "doResumeFile: Success"
            android.util.Log.i(r0, r2)     // Catch:{ HttpRequestException -> 0x02ba, IllegalStateException -> 0x02b8 }
            r11.close()     // Catch:{ IOException -> 0x02ad }
            goto L_0x02ae
        L_0x02ad:
            r0 = move-exception
        L_0x02ae:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x02b7
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            r0.disconnect()
        L_0x02b7:
            return r17
        L_0x02b8:
            r0 = move-exception
            goto L_0x02cc
        L_0x02ba:
            r0 = move-exception
            goto L_0x02ed
        L_0x02bc:
            r0 = move-exception
            r18 = r7
            r11 = r8
            r16 = r19
            r2 = r0
            goto L_0x032b
        L_0x02c5:
            r0 = move-exception
            r18 = r7
            r11 = r8
            r12 = r15
            r16 = r19
        L_0x02cc:
            r0.printStackTrace()     // Catch:{ all -> 0x0329 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0329 }
            r3 = 3
            r4 = -1
            r1.cancelRequest(r2, r3, r4, r12)     // Catch:{ all -> 0x0329 }
            r11.close()     // Catch:{ IOException -> 0x02da }
            goto L_0x02db
        L_0x02da:
            r0 = move-exception
        L_0x02db:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x0328
        L_0x02df:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            r0.disconnect()
            goto L_0x0328
        L_0x02e5:
            r0 = move-exception
            r18 = r7
            r11 = r8
            r12 = r15
            r16 = r19
            r15 = r6
        L_0x02ed:
            r0.printStackTrace()     // Catch:{ all -> 0x0329 }
            boolean r2 = r1.isPermanentFailCause(r0)     // Catch:{ all -> 0x0329 }
            if (r2 == 0) goto L_0x02ff
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0329 }
            r3 = 30
            r4 = -1
            r1.cancelRequest(r2, r3, r4, r12)     // Catch:{ all -> 0x0329 }
            goto L_0x031e
        L_0x02ff:
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0329 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0329 }
            r3.<init>()     // Catch:{ all -> 0x0329 }
            java.io.IOException r4 = r0.getCause()     // Catch:{ all -> 0x0329 }
            r3.append(r4)     // Catch:{ all -> 0x0329 }
            r3.append(r15)     // Catch:{ all -> 0x0329 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0329 }
            android.util.Log.e(r2, r3)     // Catch:{ all -> 0x0329 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0329 }
            r3 = 3
            r4 = -1
            r1.cancelRequest(r2, r3, r4, r12)     // Catch:{ all -> 0x0329 }
        L_0x031e:
            r11.close()     // Catch:{ IOException -> 0x0322 }
            goto L_0x0323
        L_0x0322:
            r0 = move-exception
        L_0x0323:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x0328
            goto L_0x02df
        L_0x0328:
            return r12
        L_0x0329:
            r0 = move-exception
            r2 = r0
        L_0x032b:
            r11.close()     // Catch:{ IOException -> 0x032f }
            goto L_0x0330
        L_0x032f:
            r0 = move-exception
        L_0x0330:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x0339
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            r0.disconnect()
        L_0x0339:
            throw r2
        L_0x033a:
            r0 = move-exception
            r18 = r7
            r11 = r8
            r16 = r19
            r12 = 0
            r2 = r11
            goto L_0x035b
        L_0x0343:
            r0 = move-exception
            r18 = r7
            r11 = r8
            r16 = r19
            r12 = 0
            r2 = r11
            r4 = -1
            goto L_0x0377
        L_0x034d:
            r0 = move-exception
            r18 = r7
            r16 = r19
            r12 = 0
            r4 = -1
            goto L_0x0377
        L_0x0355:
            r0 = move-exception
            r18 = r7
            r16 = r19
            r12 = 0
        L_0x035b:
            r3 = r0
            r3.printStackTrace()
            r2.close()     // Catch:{ IOException -> 0x0363 }
            goto L_0x0369
        L_0x0363:
            r0 = move-exception
            r4 = r0
            r0 = r4
            r0.printStackTrace()
        L_0x0369:
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r4 = -1
            r1.cancelRequest(r0, r4, r4, r12)
            return r12
        L_0x0370:
            r0 = move-exception
            r18 = r7
            r16 = r19
            r4 = -1
            r12 = 0
        L_0x0377:
            r0.printStackTrace()
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r3 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r1.cancelRequest(r3, r4, r4, r12)
            return r12
        L_0x0380:
            r0 = move-exception
            r15 = r4
            r12 = r7
            r16 = r8
            r3 = 30
        L_0x0387:
            r0.printStackTrace()
            boolean r2 = r1.isPermanentFailCause(r0)
            if (r2 == 0) goto L_0x0397
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r4 = -1
            r1.cancelRequest(r2, r3, r4, r12)
            goto L_0x03b6
        L_0x0397:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.io.IOException r4 = r0.getCause()
            r3.append(r4)
            r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.e(r2, r3)
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r3 = 3
            r4 = -1
            r1.cancelRequest(r2, r3, r4, r12)
        L_0x03b6:
            return r12
        L_0x03b7:
            r0 = move-exception
            r12 = r7
            r0.printStackTrace()
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r3 = -1
            r1.cancelRequest(r2, r3, r3, r12)
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.UploadResumeFileTask.doResumeFile(java.net.URL, long, long, long):boolean");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0199, code lost:
        if (r14.mHttpRequest != null) goto L_0x01d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01d3, code lost:
        if (r14.mHttpRequest == null) goto L_0x01da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x01d5, code lost:
        r14.mHttpRequest.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01da, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getDownloadInfo() {
        /*
            r14 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "?tid="
            r0.append(r1)
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r1 = r14.mRequest
            java.lang.String r1 = r1.mTid
            r0.append(r1)
            java.lang.String r1 = "&get_download_info"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r1 = r14.mRequest
            java.lang.String r2 = r14.getRequestUrl()
            r1.mUrl = r2
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getDownloadInfo: params="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r2 = r14.mRequest
            java.lang.String r2 = r2.mUrl
            r1.append(r2)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            com.sec.internal.helper.HttpRequest r1 = com.sec.internal.helper.HttpRequest.get(r1)
            r14.mHttpRequest = r1
            r14.setDefaultHeaders()
            r1 = 0
            r2 = 0
            com.sec.internal.helper.HttpRequest r3 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            int r3 = r3.code()     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r4 = " "
            r5 = 200(0xc8, float:2.8E-43)
            if (r3 == r5) goto L_0x0149
            r6 = 302(0x12e, float:4.23E-43)
            if (r3 == r6) goto L_0x00f4
            r6 = 401(0x191, float:5.62E-43)
            if (r3 == r6) goto L_0x009d
            java.lang.String r5 = LOG_TAG     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x019e }
            r6.<init>()     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r7 = "Receive "
            r6.append(r7)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            int r7 = r7.code()     // Catch:{ HttpRequestException -> 0x019e }
            r6.append(r7)     // Catch:{ HttpRequestException -> 0x019e }
            r6.append(r4)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r4 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r4 = r4.message()     // Catch:{ HttpRequestException -> 0x019e }
            r6.append(r4)     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r4 = r6.toString()     // Catch:{ HttpRequestException -> 0x019e }
            android.util.Log.e(r5, r4)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            if (r2 == 0) goto L_0x009c
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            r2.disconnect()
        L_0x009c:
            return r1
        L_0x009d:
            int r6 = r14.mPhoneId     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x019e }
            r8.<init>()     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r9 = r14.mRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r9 = r9.mUrl     // Catch:{ HttpRequestException -> 0x019e }
            r8.append(r9)     // Catch:{ HttpRequestException -> 0x019e }
            r8.append(r0)     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r8 = r8.toString()     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r9 = "GET"
            java.lang.String r6 = r14.getAuthorizationHeader(r6, r7, r8, r9)     // Catch:{ HttpRequestException -> 0x019e }
            boolean r7 = android.text.TextUtils.isEmpty(r6)     // Catch:{ HttpRequestException -> 0x019e }
            if (r7 == 0) goto L_0x00c2
            goto L_0x0151
        L_0x00c2:
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            r7.disconnect()     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x019e }
            r7.<init>()     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r8 = r14.mRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r8 = r8.mUrl     // Catch:{ HttpRequestException -> 0x019e }
            r7.append(r8)     // Catch:{ HttpRequestException -> 0x019e }
            r7.append(r0)     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r7 = r7.toString()     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = com.sec.internal.helper.HttpRequest.get(r7)     // Catch:{ HttpRequestException -> 0x019e }
            r14.mHttpRequest = r7     // Catch:{ HttpRequestException -> 0x019e }
            r14.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r7.authorization(r6)     // Catch:{ HttpRequestException -> 0x019e }
            r7.chunk(r2)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            int r7 = r7.code()     // Catch:{ HttpRequestException -> 0x019e }
            r3 = r7
            goto L_0x0151
        L_0x00f4:
            com.sec.internal.helper.HttpRequest r6 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r7 = "Location"
            java.lang.String r10 = r6.header(r7)     // Catch:{ HttpRequestException -> 0x019e }
            boolean r6 = android.text.TextUtils.isEmpty(r10)     // Catch:{ HttpRequestException -> 0x019e }
            if (r6 != 0) goto L_0x0137
            com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest r6 = new com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest     // Catch:{ HttpRequestException -> 0x019e }
            int r9 = r14.mPhoneId     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r7 = r14.mRequest     // Catch:{ HttpRequestException -> 0x019e }
            android.net.Network r11 = r7.mNetwork     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r7 = r14.mRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r12 = r7.mUserAgent     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r7 = r14.mRequest     // Catch:{ HttpRequestException -> 0x019e }
            boolean r13 = r7.mTrustAllCerts     // Catch:{ HttpRequestException -> 0x019e }
            r8 = r6
            r8.<init>(r9, r10, r11, r12, r13)     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r6 = com.sec.internal.ims.util.OpenIdAuth.sendAuthRequest(r6)     // Catch:{ HttpRequestException -> 0x019e }
            if (r6 == 0) goto L_0x0137
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            r7.disconnect()     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = com.sec.internal.helper.HttpRequest.get(r6)     // Catch:{ HttpRequestException -> 0x019e }
            r14.mHttpRequest = r7     // Catch:{ HttpRequestException -> 0x019e }
            r14.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            r7.chunk(r2)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            int r7 = r7.code()     // Catch:{ HttpRequestException -> 0x019e }
            r3 = r7
            goto L_0x0151
        L_0x0137:
            java.lang.String r4 = LOG_TAG     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r5 = "getDownloadInfo: OPenID Process failed!"
            android.util.Log.e(r4, r5)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            if (r2 == 0) goto L_0x0148
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            r2.disconnect()
        L_0x0148:
            return r1
        L_0x0149:
            java.lang.String r6 = LOG_TAG     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r7 = "Receive 200 OK"
            android.util.Log.i(r6, r7)     // Catch:{ HttpRequestException -> 0x019e }
        L_0x0151:
            if (r5 != r3) goto L_0x016a
            java.lang.String r4 = LOG_TAG     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r5 = "getDownloadInfo: Success"
            android.util.Log.i(r4, r5)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r4 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r1 = r4.body()     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            if (r2 == 0) goto L_0x0169
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            r2.disconnect()
        L_0x0169:
            return r1
        L_0x016a:
            java.lang.String r5 = LOG_TAG     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x019e }
            r6.<init>()     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r7 = "getDownloadInfo: Failed, "
            r6.append(r7)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            int r7 = r7.code()     // Catch:{ HttpRequestException -> 0x019e }
            r6.append(r7)     // Catch:{ HttpRequestException -> 0x019e }
            r6.append(r4)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r4 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r4 = r4.message()     // Catch:{ HttpRequestException -> 0x019e }
            r6.append(r4)     // Catch:{ HttpRequestException -> 0x019e }
            java.lang.String r4 = r6.toString()     // Catch:{ HttpRequestException -> 0x019e }
            android.util.Log.e(r5, r4)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r4 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x019e }
            r14.cancelRequest(r4, r2)     // Catch:{ HttpRequestException -> 0x019e }
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            if (r2 == 0) goto L_0x01da
            goto L_0x01d5
        L_0x019c:
            r1 = move-exception
            goto L_0x01db
        L_0x019e:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ all -> 0x019c }
            boolean r4 = r14.isPermanentFailCause(r3)     // Catch:{ all -> 0x019c }
            r5 = -1
            if (r4 == 0) goto L_0x01b1
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r4 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x019c }
            r6 = 30
            r14.cancelRequest(r4, r6, r5, r2)     // Catch:{ all -> 0x019c }
            goto L_0x01d1
        L_0x01b1:
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x019c }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x019c }
            r6.<init>()     // Catch:{ all -> 0x019c }
            java.io.IOException r7 = r3.getCause()     // Catch:{ all -> 0x019c }
            r6.append(r7)     // Catch:{ all -> 0x019c }
            java.lang.String r7 = " happened. Retry Upload Task."
            r6.append(r7)     // Catch:{ all -> 0x019c }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x019c }
            android.util.Log.e(r4, r6)     // Catch:{ all -> 0x019c }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r4 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x019c }
            r6 = 3
            r14.cancelRequest(r4, r6, r5, r2)     // Catch:{ all -> 0x019c }
        L_0x01d1:
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            if (r2 == 0) goto L_0x01da
        L_0x01d5:
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            r2.disconnect()
        L_0x01da:
            return r1
        L_0x01db:
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            if (r2 == 0) goto L_0x01e4
            com.sec.internal.helper.HttpRequest r2 = r14.mHttpRequest
            r2.disconnect()
        L_0x01e4:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.UploadResumeFileTask.getDownloadInfo():java.lang.String");
    }

    /* access modifiers changed from: protected */
    public Long doInBackground(UploadFileTask.UploadRequest... params) {
        String result;
        if (this.mMnoStrategy == null) {
            Log.e(LOG_TAG, "mMnoStrategy is null");
            cancelRequest(CancelReason.ERROR, -1, -1, false);
            return Long.valueOf(this.mTransferred);
        }
        Preconditions.checkNotNull(params[0].mCallback);
        Preconditions.checkNotNull(params[0].mUrl);
        Preconditions.checkNotNull(params[0].mTid);
        Preconditions.checkNotNull(params[0].mFilePath);
        TrafficStats.setThreadStatsTag(Process.myTid());
        String str = LOG_TAG;
        Log.i(str, "doInBackground: " + params[0]);
        this.mRequest = params[0];
        this.mTotal = this.mRequest.mTotalBytes;
        FtHttpResumeInfo info = getUploadInfo();
        if (info == null) {
            Log.e(LOG_TAG, "Failed to get upload info.");
            return Long.valueOf(this.mTransferred);
        }
        boolean success = false;
        if (info.getEnd() + 1 > this.mRequest.mTotalBytes) {
            String str2 = LOG_TAG;
            Log.i(str2, "Uploaded over than requested size.  : " + (info.getEnd() + 1));
            success = true;
        } else if (info.getEnd() + 1 == this.mRequest.mTotalBytes) {
            Log.i(LOG_TAG, "Already uploaded.");
            success = true;
        } else if (info.getUrl() != null) {
            success = doResumeFile(info.getUrl(), info.getEnd() + 1, this.mRequest.mTotalBytes - 1, this.mRequest.mTotalBytes);
        }
        if (success && (result = getDownloadInfo()) != null) {
            this.mRequest.mCallback.onCompleted(result);
        }
        return Long.valueOf(this.mTransferred);
    }
}
