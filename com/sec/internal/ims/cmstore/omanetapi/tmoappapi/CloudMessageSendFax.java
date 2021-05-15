package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import com.sec.internal.omanetapi.nms.AllObjects;

public class CloudMessageSendFax extends AllObjects {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageSendFax.class.getSimpleName();
    private static final long serialVersionUID = -3802278410115462680L;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CloudMessageSendFax(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r17, com.sec.internal.ims.cmstore.params.ParamObjectUpload r18, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r19) {
        /*
            r16 = this;
            r6 = r16
            r7 = r18
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = r19.getProtocol()
            r0.append(r1)
            java.lang.String r1 = "://"
            r0.append(r1)
            java.lang.String r1 = r19.getFaxServerRoot()
            r0.append(r1)
            java.lang.String r1 = "/"
            r0.append(r1)
            java.lang.String r2 = r19.getFaxApiVersion()
            r0.append(r2)
            r0.append(r1)
            java.lang.String r2 = r19.getFaxServiceName()
            r0.append(r2)
            r0.append(r1)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r7.bufferDbParam
            java.lang.String r1 = r1.mLine
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r6.<init>(r0)
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "URL: "
            r1.append(r2)
            java.lang.String r2 = r6.mBaseUrl
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r2)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            android.util.Pair<com.sec.internal.omanetapi.nms.data.Object, com.sec.internal.helper.httpclient.HttpPostBody> r8 = r7.uploadObjectInfo
            java.lang.Object r0 = r8.first
            r9 = r0
            com.sec.internal.omanetapi.nms.data.Object r9 = (com.sec.internal.omanetapi.nms.data.Object) r9
            java.lang.Object r0 = r8.second
            r10 = r0
            com.sec.internal.helper.httpclient.HttpPostBody r10 = (com.sec.internal.helper.httpclient.HttpPostBody) r10
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r7.bufferDbParam
            java.lang.String r11 = r0.mLine
            r12 = r19
            java.lang.String r13 = r12.getValidTokenByLine(r11)
            java.lang.String r0 = r19.getContentType()
            r6.initCommonRequestHeaders(r0, r13)
            r0 = 1
            r6.initPostRequest(r9, r0, r10)
            r3 = r16
            r5 = r9
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r14 = r7.bufferDbParam
            com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageSendFax$1 r15 = new com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageSendFax$1
            r0 = r15
            r1 = r16
            r2 = r17
            r4 = r14
            r0.<init>(r2, r3, r4, r5)
            r6.setCallback(r15)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageSendFax.<init>(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener, com.sec.internal.ims.cmstore.params.ParamObjectUpload, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper):void");
    }
}
