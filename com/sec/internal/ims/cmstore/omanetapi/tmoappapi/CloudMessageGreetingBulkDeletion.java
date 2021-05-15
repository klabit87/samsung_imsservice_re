package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.nms.BulkDeletion;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import java.io.IOException;

public class CloudMessageGreetingBulkDeletion extends BulkDeletion {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageGreetingBulkDeletion.class.getSimpleName();
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public CloudMessageGreetingBulkDeletion(IAPICallFlowListener callFlowListener, BulkDelete bulkdelete, String currentLine, SyncMsgType type, BufferDBChangeParam greetingParam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), currentLine);
        this.mIAPICallFlowListener = callFlowListener;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(currentLine));
        initDeleteRequest(bulkdelete, true);
        final BufferDBChangeParam bufferDBChangeParam = greetingParam;
        final IAPICallFlowListener iAPICallFlowListener = callFlowListener;
        final String str = currentLine;
        final SyncMsgType syncMsgType = type;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v34, resolved type: java.lang.Object} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v12, resolved type: java.lang.String} */
            /* JADX WARNING: Multi-variable type inference failed */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onComplete(com.sec.internal.helper.httpclient.HttpResponseParams r9) {
                /*
                    r8 = this;
                    java.lang.String r0 = r9.getDataString()
                    java.lang.String r1 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.StringBuilder r2 = new java.lang.StringBuilder
                    r2.<init>()
                    java.lang.String r3 = "Result code = "
                    r2.append(r3)
                    int r3 = r9.getStatusCode()
                    r2.append(r3)
                    java.lang.String r2 = r2.toString()
                    android.util.Log.d(r1, r2)
                    int r1 = r9.getStatusCode()
                    r2 = 0
                    r3 = 302(0x12e, float:4.23E-43)
                    if (r1 != r3) goto L_0x00b0
                    java.lang.String r1 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.String r4 = "302 redirect"
                    android.util.Log.d(r1, r4)
                    java.util.Map r1 = r9.getHeaders()
                    java.lang.String r4 = "Location"
                    java.lang.Object r1 = r1.get(r4)
                    java.util.List r1 = (java.util.List) r1
                    r4 = 0
                    if (r1 == 0) goto L_0x0059
                    int r5 = r1.size()
                    if (r5 <= 0) goto L_0x0059
                    java.lang.String r5 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.String r6 = r1.toString()
                    android.util.Log.i(r5, r6)
                    java.lang.Object r2 = r1.get(r2)
                    r4 = r2
                    java.lang.String r4 = (java.lang.String) r4
                L_0x0059:
                    boolean r2 = android.text.TextUtils.isEmpty(r4)
                    if (r2 != 0) goto L_0x00a2
                    java.net.URL r2 = new java.net.URL     // Catch:{ MalformedURLException -> 0x0072 }
                    r2.<init>(r4)     // Catch:{ MalformedURLException -> 0x0072 }
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r5 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this     // Catch:{ MalformedURLException -> 0x0072 }
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r5 = r5.mICloudMessageManagerHelper     // Catch:{ MalformedURLException -> 0x0072 }
                    java.lang.String r6 = r2.getHost()     // Catch:{ MalformedURLException -> 0x0072 }
                    r5.saveNcHost(r6)     // Catch:{ MalformedURLException -> 0x0072 }
                    goto L_0x0092
                L_0x0072:
                    r2 = move-exception
                    java.lang.String r5 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.StringBuilder r6 = new java.lang.StringBuilder
                    r6.<init>()
                    java.lang.String r7 = ""
                    r6.append(r7)
                    java.lang.String r7 = r2.getMessage()
                    r6.append(r7)
                    java.lang.String r6 = r6.toString()
                    android.util.Log.d(r5, r6)
                    r2.printStackTrace()
                L_0x0092:
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r2 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r2 = r2.mIAPICallFlowListener
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r5 = r2
                    java.lang.String r3 = java.lang.String.valueOf(r3)
                    r2.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r5, (java.lang.String) r3)
                    return
                L_0x00a2:
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r2 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r2 = r2.mIAPICallFlowListener
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r3 = r2
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r5 = r3
                    r2.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r3, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r5)
                    return
                L_0x00b0:
                    int r1 = r9.getStatusCode()
                    r3 = 401(0x191, float:5.62E-43)
                    if (r1 != r3) goto L_0x00ef
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r1.<init>()
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r1.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r2)
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r2 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    java.lang.String r2 = r2.getBoxId()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r1.setLine(r2)
                    android.os.Message r2 = new android.os.Message
                    r2.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r3 = r1.build()
                    r2.obj = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREDENTIAL_EXPIRED
                    int r3 = r3.getId()
                    r2.what = r3
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r4
                    r3.onFixedFlowWithMessage(r2)
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r4
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r4 = r2
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r5 = r3
                    r3.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r4, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r5)
                    return
                L_0x00ef:
                    int r1 = r9.getStatusCode()
                    r3 = 429(0x1ad, float:6.01E-43)
                    if (r1 == r3) goto L_0x00ff
                    int r1 = r9.getStatusCode()
                    r3 = 503(0x1f7, float:7.05E-43)
                    if (r1 != r3) goto L_0x0177
                L_0x00ff:
                    java.util.Map r1 = r9.getHeaders()
                    java.lang.String r3 = "Retry-After"
                    java.lang.Object r1 = r1.get(r3)
                    java.util.List r1 = (java.util.List) r1
                    r3 = 0
                    r4 = 0
                    if (r1 == 0) goto L_0x0177
                    int r5 = r1.size()
                    if (r5 <= 0) goto L_0x0177
                    java.lang.String r5 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.String r6 = r1.toString()
                    android.util.Log.i(r5, r6)
                    java.lang.Object r2 = r1.get(r2)
                    java.lang.String r2 = (java.lang.String) r2
                    java.lang.String r3 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder
                    r5.<init>()
                    java.lang.String r6 = "retryAfter is "
                    r5.append(r6)
                    r5.append(r2)
                    java.lang.String r6 = "seconds"
                    r5.append(r6)
                    java.lang.String r5 = r5.toString()
                    android.util.Log.d(r3, r5)
                    int r3 = java.lang.Integer.parseInt(r2)     // Catch:{ NumberFormatException -> 0x0161 }
                    r4 = r3
                    if (r4 <= 0) goto L_0x0157
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r4     // Catch:{ NumberFormatException -> 0x0161 }
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r5 = r2     // Catch:{ NumberFormatException -> 0x0161 }
                    java.lang.String r6 = "retry_header"
                    r3.onOverRequest(r5, r6, r4)     // Catch:{ NumberFormatException -> 0x0161 }
                    goto L_0x0160
                L_0x0157:
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r4     // Catch:{ NumberFormatException -> 0x0161 }
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r5 = r2     // Catch:{ NumberFormatException -> 0x0161 }
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r6 = r3     // Catch:{ NumberFormatException -> 0x0161 }
                    r3.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r5, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r6)     // Catch:{ NumberFormatException -> 0x0161 }
                L_0x0160:
                    return
                L_0x0161:
                    r3 = move-exception
                    java.lang.String r5 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.String r6 = r3.getMessage()
                    android.util.Log.e(r5, r6)
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r5 = r4
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r6 = r2
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r7 = r3
                    r5.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r6, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r7)
                    return
                L_0x0177:
                    int r1 = r9.getStatusCode()
                    r2 = 204(0xcc, float:2.86E-43)
                    if (r1 != r2) goto L_0x01ff
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r1 = r4
                    boolean r2 = r1 instanceof com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.OMAObjectUpdateScheduler
                    if (r2 == 0) goto L_0x01bf
                    android.os.Message r1 = new android.os.Message
                    r1.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r2.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setActionType(r3)
                    java.lang.String r3 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setLine(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r3 = r6
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setSyncType(r3)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r3)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r3 = r2.build()
                    r1.obj = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_ONE_SUCCESSFUL
                    int r3 = r3.getId()
                    r1.what = r3
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r3 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r3.mIAPICallFlowListener
                    r3.onFixedFlowWithMessage(r1)
                    goto L_0x01fd
                L_0x01bf:
                    boolean r1 = r1 instanceof com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler
                    if (r1 == 0) goto L_0x01fd
                    android.os.Message r1 = new android.os.Message
                    r1.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r2.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setActionType(r3)
                    java.lang.String r3 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setLine(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r3 = r6
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setSyncType(r3)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r3)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r3 = r2.build()
                    r1.obj = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOAD_GREETING
                    int r3 = r3.getId()
                    r1.what = r3
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r3 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r3.mIAPICallFlowListener
                    r3.onFixedFlowWithMessage(r1)
                    goto L_0x01fe
                L_0x01fd:
                L_0x01fe:
                    return
                L_0x01ff:
                    int r1 = r9.getStatusCode()
                    r2 = 200(0xc8, float:2.8E-43)
                    if (r1 == r2) goto L_0x0225
                    int r1 = r9.getStatusCode()
                    r2 = 206(0xce, float:2.89E-43)
                    if (r1 == r2) goto L_0x0225
                    int r1 = r9.getStatusCode()
                    r2 = 400(0x190, float:5.6E-43)
                    if (r1 == r2) goto L_0x0225
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r1 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r1 = r1.mIAPICallFlowListener
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2 = r2
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = r3
                    r1.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r2, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r3)
                    return
                L_0x0225:
                    com.google.gson.Gson r1 = new com.google.gson.Gson
                    r1.<init>()
                    r2 = 0
                    java.lang.Class<com.sec.internal.omanetapi.common.data.OMAApiResponseParam> r3 = com.sec.internal.omanetapi.common.data.OMAApiResponseParam.class
                    java.lang.Object r3 = r1.fromJson(r0, r3)     // Catch:{ Exception -> 0x02bc }
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r3 = (com.sec.internal.omanetapi.common.data.OMAApiResponseParam) r3     // Catch:{ Exception -> 0x02bc }
                    r2 = r3
                    if (r2 != 0) goto L_0x0238
                    return
                L_0x0238:
                    com.sec.internal.omanetapi.nms.data.BulkResponseList r3 = r2.bulkResponseList
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r4 = r4
                    boolean r5 = r4 instanceof com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.OMAObjectUpdateScheduler
                    if (r5 == 0) goto L_0x0278
                    android.os.Message r4 = new android.os.Message
                    r4.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r5.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r6 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setActionType(r6)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setBulkResponseList(r3)
                    java.lang.String r6 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setLine(r6)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r6 = r6
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setSyncType(r6)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r6 = r5.build()
                    r4.obj = r6
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r6 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_ONE_SUCCESSFUL
                    int r6 = r6.getId()
                    r4.what = r6
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r6 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r6 = r6.mIAPICallFlowListener
                    r6.onFixedFlowWithMessage(r4)
                    goto L_0x02ba
                L_0x0278:
                    boolean r4 = r4 instanceof com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler
                    if (r4 == 0) goto L_0x02ba
                    android.os.Message r4 = new android.os.Message
                    r4.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r5.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r6 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setActionType(r6)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setBulkResponseList(r3)
                    java.lang.String r6 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setLine(r6)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r6 = r6
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setSyncType(r6)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r6 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r6)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r6 = r5.build()
                    r4.obj = r6
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r6 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOAD_GREETING
                    int r6 = r6.getId()
                    r4.what = r6
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion r6 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r6 = r6.mIAPICallFlowListener
                    r6.onFixedFlowWithMessage(r4)
                    goto L_0x02bb
                L_0x02ba:
                L_0x02bb:
                    return
                L_0x02bc:
                    r3 = move-exception
                    java.lang.String r4 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.TAG
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder
                    r5.<init>()
                    java.lang.String r6 = r3.toString()
                    r5.append(r6)
                    java.lang.String r6 = " "
                    r5.append(r6)
                    java.lang.String r5 = r5.toString()
                    android.util.Log.e(r4, r5)
                    r3.printStackTrace()
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.AnonymousClass1.onComplete(com.sec.internal.helper.httpclient.HttpResponseParams):void");
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageGreetingBulkDeletion.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                CloudMessageGreetingBulkDeletion.this.mIAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
            }
        });
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback) {
        initCommonRequestHeaders(this.mICloudMessageManagerHelper.getContentType(), this.mICloudMessageManagerHelper.getValidTokenByLine(getBoxId()));
        return this;
    }
}
