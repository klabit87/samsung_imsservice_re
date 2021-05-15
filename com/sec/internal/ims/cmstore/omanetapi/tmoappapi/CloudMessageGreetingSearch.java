package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.data.SortOrderEnum;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.helper.TMOVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.nms.ObjectsOpSearch;
import com.sec.internal.omanetapi.nms.data.Reference;
import com.sec.internal.omanetapi.nms.data.SearchCriteria;
import com.sec.internal.omanetapi.nms.data.SearchCriterion;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;
import com.sec.internal.omanetapi.nms.data.SortCriteria;
import com.sec.internal.omanetapi.nms.data.SortCriterion;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class CloudMessageGreetingSearch extends ObjectsOpSearch {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageGreetingSearch.class.getSimpleName();
    private static final long serialVersionUID = 1;
    private final SimpleDateFormat mFormatOfName;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public CloudMessageGreetingSearch(IAPICallFlowListener callFlowListener, String searchCursor, String currentLine, BufferDBChangeParam param, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), currentLine);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        this.mFormatOfName = simpleDateFormat;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.mIAPICallFlowListener = callFlowListener;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        SelectionCriteria selectionCriteria = new SelectionCriteria();
        constructSearchParam(currentLine, SyncMsgType.VM_GREETINGS, selectionCriteria);
        if (!TextUtils.isEmpty(searchCursor)) {
            selectionCriteria.fromCursor = searchCursor;
        }
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(currentLine));
        initPostRequest(selectionCriteria, true);
        final BufferDBChangeParam bufferDBChangeParam = param;
        final IAPICallFlowListener iAPICallFlowListener = callFlowListener;
        final String str = currentLine;
        final String str2 = searchCursor;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v29, resolved type: java.lang.Object} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v10, resolved type: java.lang.String} */
            /* JADX WARNING: Multi-variable type inference failed */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onComplete(com.sec.internal.helper.httpclient.HttpResponseParams r9) {
                /*
                    r8 = this;
                    java.lang.String r0 = r9.getDataString()
                    java.lang.String r1 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
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
                    java.lang.String r4 = ""
                    if (r1 != r3) goto L_0x00b0
                    java.lang.String r1 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
                    java.lang.String r5 = "302 redirect"
                    android.util.Log.d(r1, r5)
                    java.util.Map r1 = r9.getHeaders()
                    java.lang.String r5 = "Location"
                    java.lang.Object r1 = r1.get(r5)
                    java.util.List r1 = (java.util.List) r1
                    r5 = 0
                    if (r1 == 0) goto L_0x005b
                    int r6 = r1.size()
                    if (r6 <= 0) goto L_0x005b
                    java.lang.String r6 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
                    java.lang.String r7 = r1.toString()
                    android.util.Log.i(r6, r7)
                    java.lang.Object r2 = r1.get(r2)
                    r5 = r2
                    java.lang.String r5 = (java.lang.String) r5
                L_0x005b:
                    boolean r2 = android.text.TextUtils.isEmpty(r5)
                    if (r2 != 0) goto L_0x00a2
                    java.net.URL r2 = new java.net.URL     // Catch:{ MalformedURLException -> 0x0074 }
                    r2.<init>(r5)     // Catch:{ MalformedURLException -> 0x0074 }
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r6 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this     // Catch:{ MalformedURLException -> 0x0074 }
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r6 = r6.mICloudMessageManagerHelper     // Catch:{ MalformedURLException -> 0x0074 }
                    java.lang.String r7 = r2.getHost()     // Catch:{ MalformedURLException -> 0x0074 }
                    r6.saveNcHost(r7)     // Catch:{ MalformedURLException -> 0x0074 }
                    goto L_0x0092
                L_0x0074:
                    r2 = move-exception
                    java.lang.String r6 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
                    java.lang.StringBuilder r7 = new java.lang.StringBuilder
                    r7.<init>()
                    r7.append(r4)
                    java.lang.String r4 = r2.getMessage()
                    r7.append(r4)
                    java.lang.String r4 = r7.toString()
                    android.util.Log.d(r6, r4)
                    r2.printStackTrace()
                L_0x0092:
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r2 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r2 = r2.mIAPICallFlowListener
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r4 = r2
                    java.lang.String r3 = java.lang.String.valueOf(r3)
                    r2.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r4, (java.lang.String) r3)
                    return
                L_0x00a2:
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r2 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r2 = r2.mIAPICallFlowListener
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r3 = r2
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r4 = r3
                    r2.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r3, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r4)
                    return
                L_0x00b0:
                    int r1 = r9.getStatusCode()
                    r3 = 401(0x191, float:5.62E-43)
                    if (r1 != r3) goto L_0x00ef
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r1.<init>()
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r1.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r2)
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r2 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
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
                    r5 = 0
                    if (r1 == 0) goto L_0x0177
                    int r6 = r1.size()
                    if (r6 <= 0) goto L_0x0177
                    java.lang.String r4 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
                    java.lang.String r6 = r1.toString()
                    android.util.Log.i(r4, r6)
                    java.lang.Object r2 = r1.get(r2)
                    java.lang.String r2 = (java.lang.String) r2
                    java.lang.String r3 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
                    java.lang.StringBuilder r4 = new java.lang.StringBuilder
                    r4.<init>()
                    java.lang.String r6 = "retryAfter is "
                    r4.append(r6)
                    r4.append(r2)
                    java.lang.String r6 = "seconds"
                    r4.append(r6)
                    java.lang.String r4 = r4.toString()
                    android.util.Log.d(r3, r4)
                    int r3 = java.lang.Integer.parseInt(r2)     // Catch:{ NumberFormatException -> 0x0161 }
                    r5 = r3
                    if (r5 <= 0) goto L_0x0157
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r4     // Catch:{ NumberFormatException -> 0x0161 }
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r4 = r2     // Catch:{ NumberFormatException -> 0x0161 }
                    java.lang.String r6 = "retry_header"
                    r3.onOverRequest(r4, r6, r5)     // Catch:{ NumberFormatException -> 0x0161 }
                    goto L_0x0160
                L_0x0157:
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r4     // Catch:{ NumberFormatException -> 0x0161 }
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r4 = r2     // Catch:{ NumberFormatException -> 0x0161 }
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r6 = r3     // Catch:{ NumberFormatException -> 0x0161 }
                    r3.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r4, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r6)     // Catch:{ NumberFormatException -> 0x0161 }
                L_0x0160:
                    return
                L_0x0161:
                    r3 = move-exception
                    java.lang.String r4 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
                    java.lang.String r6 = r3.getMessage()
                    android.util.Log.e(r4, r6)
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r4 = r4
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r6 = r2
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r7 = r3
                    r4.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r6, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r7)
                    return
                L_0x0177:
                    int r1 = r9.getStatusCode()
                    r2 = 204(0xcc, float:2.86E-43)
                    if (r1 != r2) goto L_0x01c3
                    android.os.Message r1 = new android.os.Message
                    r1.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r2.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setActionType(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_GREETING
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setOMASyncEventType(r3)
                    java.lang.String r3 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setLine(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM_GREETINGS
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setSyncType(r3)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setCursor(r4)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r3)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r3 = r2.build()
                    r1.obj = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_GREETING
                    int r3 = r3.getId()
                    r1.what = r3
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r3 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r3.mIAPICallFlowListener
                    r3.onFixedFlowWithMessage(r1)
                    return
                L_0x01c3:
                    int r1 = r9.getStatusCode()
                    r2 = 200(0xc8, float:2.8E-43)
                    if (r1 == r2) goto L_0x01e1
                    int r1 = r9.getStatusCode()
                    r2 = 206(0xce, float:2.89E-43)
                    if (r1 == r2) goto L_0x01e1
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r1 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r1 = r1.mIAPICallFlowListener
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2 = r2
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = r3
                    r1.onFailedCall((com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r2, (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r3)
                    return
                L_0x01e1:
                    com.google.gson.Gson r1 = new com.google.gson.Gson
                    r1.<init>()
                    r2 = 0
                    java.lang.Class<com.sec.internal.omanetapi.common.data.OMAApiResponseParam> r3 = com.sec.internal.omanetapi.common.data.OMAApiResponseParam.class
                    java.lang.Object r3 = r1.fromJson(r0, r3)     // Catch:{ Exception -> 0x02d0 }
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r3 = (com.sec.internal.omanetapi.common.data.OMAApiResponseParam) r3     // Catch:{ Exception -> 0x02d0 }
                    r2 = r3
                    if (r2 != 0) goto L_0x01f4
                    return
                L_0x01f4:
                    com.sec.internal.omanetapi.nms.data.ObjectList r3 = r2.objectList
                    android.os.Message r5 = new android.os.Message
                    r5.<init>()
                    if (r3 == 0) goto L_0x0293
                    java.lang.String r4 = r3.cursor
                    boolean r6 = android.text.TextUtils.isEmpty(r4)
                    if (r6 != 0) goto L_0x0250
                    java.lang.String r6 = r6
                    boolean r6 = r4.equals(r6)
                    if (r6 != 0) goto L_0x0250
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r6.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r7 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_PARTIAL_SYNC_SUMMARY
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setActionType(r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setObjectList(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setOMASyncEventType(r7)
                    java.lang.String r7 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setLine(r7)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM_GREETINGS
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setSyncType(r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setCursor(r4)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r7 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r7 = r6.build()
                    r5.obj = r7
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH
                    int r7 = r7.getId()
                    r5.what = r7
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r7 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r7 = r7.mIAPICallFlowListener
                    r7.onFixedFlowWithMessage(r5)
                    goto L_0x0292
                L_0x0250:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r6.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r7 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setActionType(r7)
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_GREETING
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setOMASyncEventType(r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setObjectList(r3)
                    java.lang.String r7 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setLine(r7)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM_GREETINGS
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setSyncType(r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setCursor(r4)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r7 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r7 = r6.build()
                    r5.obj = r7
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_GREETING
                    int r7 = r7.getId()
                    r5.what = r7
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r7 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r7 = r7.mIAPICallFlowListener
                    r7.onFixedFlowWithMessage(r5)
                L_0x0292:
                    goto L_0x02cf
                L_0x0293:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r6.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r7 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setActionType(r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setObjectList(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM_GREETINGS
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r6 = r6.setSyncType(r7)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r4 = r6.setCursor(r4)
                    java.lang.String r6 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r4 = r4.setLine(r6)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r6 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r4 = r4.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r6)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r6 = r4.build()
                    r5.obj = r6
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r6 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_GREETING
                    int r6 = r6.getId()
                    r5.what = r6
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch r6 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r6 = r6.mIAPICallFlowListener
                    r6.onFixedFlowWithMessage(r5)
                L_0x02cf:
                    return
                L_0x02d0:
                    r3 = move-exception
                    java.lang.String r4 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.TAG
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
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch.AnonymousClass1.onComplete(com.sec.internal.helper.httpclient.HttpResponseParams):void");
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageGreetingSearch.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
            }
        });
    }

    private void constructSearchParam(String currentLine, SyncMsgType type, SelectionCriteria selectionCriteria) {
        String date;
        SyncMsgType syncMsgType = type;
        SelectionCriteria selectionCriteria2 = selectionCriteria;
        selectionCriteria2.maxEntries = this.mICloudMessageManagerHelper.getMaxSearchEntry();
        Reference searchScope = null;
        if (!SyncMsgType.VM_GREETINGS.equals(syncMsgType)) {
            String str = TAG;
            Log.e(str, "illegal type " + syncMsgType);
            return;
        }
        long period = TMOVariables.TmoMessageSyncPeriod.GREETING;
        if (period == 0) {
            date = this.mFormatOfName.format(0);
        } else {
            date = this.mFormatOfName.format(Long.valueOf(System.currentTimeMillis() - period));
        }
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriterion[] searchCriterion = {new SearchCriterion()};
        searchCriterion[0].type = "Date";
        SearchCriterion searchCriterion2 = searchCriterion[0];
        searchCriterion2.value = "minDate=" + date;
        searchCriteria.criterion = searchCriterion;
        SortCriteria sortCriteria = new SortCriteria();
        SortCriterion[] sortCriterion = {new SortCriterion()};
        sortCriterion[0].type = "Date";
        sortCriterion[0].order = SortOrderEnum.Date;
        sortCriteria.criterion = sortCriterion;
        String folderId = TMOVariables.TmoMessageFolderId.mVVMailGreeting;
        String protocol = this.mICloudMessageManagerHelper.getProtocol();
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(protocol).encodedAuthority(this.mICloudMessageManagerHelper.getNmsHost()).appendPath("nms").appendPath(this.mICloudMessageManagerHelper.getOMAApiVersion()).appendPath(this.mICloudMessageManagerHelper.getStoreName()).appendPath(currentLine).appendPath("folders").appendPath(folderId);
        try {
            searchScope = new Reference();
            searchScope.resourceURL = new URL(builder.build().toString());
            long j = period;
        } catch (MalformedURLException e) {
            String str2 = TAG;
            StringBuilder sb = new StringBuilder();
            long j2 = period;
            sb.append(e.getMessage());
            sb.append("");
            Log.e(str2, sb.toString());
            searchScope.resourceURL = null;
        }
        selectionCriteria2.searchScope = searchScope;
        selectionCriteria2.sortCriteria = sortCriteria;
        selectionCriteria2.searchCriteria = searchCriteria;
    }
}
