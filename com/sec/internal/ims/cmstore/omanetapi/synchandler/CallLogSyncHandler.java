package com.sec.internal.ims.cmstore.omanetapi.synchandler;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;

public class CallLogSyncHandler extends BaseSyncHandler {
    public static final String TAG = CallLogSyncHandler.class.getSimpleName();

    public CallLogSyncHandler(Looper looper, Context context, INetAPIEventListener APIEventListener, IUIEventCallback uicallback, String line, SyncMsgType type, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper, context, APIEventListener, uicallback, line, type, iCloudMessageManagerHelper);
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParam param, BaseSyncHandler.SyncOperation syncParam) {
        String str = TAG;
        Log.i(str, "setWorkingQueue :: " + param + " setMsgDownloadQueue size : " + this.mWorkingDownloadQueue.size());
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams peekUploadQueue() {
        return null;
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams peekDownloadQueue() {
        return null;
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams peekBulkUploadQueue() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void setBulkUploadQueue(BufferDBChangeParamList param) {
    }

    /* access modifiers changed from: protected */
    public void makeBulkUploadparameter() {
    }
}