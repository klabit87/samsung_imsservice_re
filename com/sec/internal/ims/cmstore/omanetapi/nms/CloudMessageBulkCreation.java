package com.sec.internal.ims.cmstore.omanetapi.nms;

import com.sec.internal.omanetapi.nms.BulkCreation;

public class CloudMessageBulkCreation extends BulkCreation {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageBulkCreation.class.getSimpleName();
    private static final long serialVersionUID = 3193513166884750667L;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CloudMessageBulkCreation(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r17, com.sec.internal.ims.cmstore.params.ParamBulkCreation r18, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r19) {
        /*
            r16 = this;
            r6 = r16
            r7 = r18
            java.lang.String r0 = r19.getNmsHost()
            java.lang.String r1 = r19.getOMAApiVersion()
            java.lang.String r2 = r19.getStoreName()
            java.lang.String r3 = r7.mLine
            r6.<init>(r0, r1, r2, r3)
            android.util.Pair<com.sec.internal.omanetapi.nms.data.ObjectList, java.util.List<com.sec.internal.helper.httpclient.HttpPostBody>> r8 = r7.uploadObjectInfo
            java.lang.Object r0 = r8.first
            r9 = r0
            com.sec.internal.omanetapi.nms.data.ObjectList r9 = (com.sec.internal.omanetapi.nms.data.ObjectList) r9
            java.lang.Object r0 = r8.second
            r10 = r0
            java.util.List r10 = (java.util.List) r10
            java.lang.String r11 = r7.mLine
            r12 = r19
            java.lang.String r13 = r12.getValidTokenByLine(r11)
            java.lang.String r0 = r19.getContentType()
            r6.initCommonRequestHeaders(r0, r13)
            r0 = 1
            r6.initPostRequest(r9, r0, r10)
            r0 = 300000(0x493e0, double:1.482197E-318)
            r6.setWriteTimeout(r0)
            r5 = r16
            r2 = r9
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r14 = r7.bufferDbParamList
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkCreation$1 r15 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkCreation$1
            r0 = r15
            r1 = r16
            r3 = r14
            r4 = r17
            r0.<init>(r2, r3, r4, r5)
            r6.setCallback(r15)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkCreation.<init>(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener, com.sec.internal.ims.cmstore.params.ParamBulkCreation, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper):void");
    }
}
