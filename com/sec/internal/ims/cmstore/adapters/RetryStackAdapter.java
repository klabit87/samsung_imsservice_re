package com.sec.internal.ims.cmstore.adapters;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.utils.CloudMessagePreferenceConstants;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.Stack;

public class RetryStackAdapter {
    public static final String TAG = RetryStackAdapter.class.getSimpleName();
    private static final RetryStackAdapter sInstance = new RetryStackAdapter();
    private Stack<IHttpAPICommonInterface> mStack = new Stack<>();

    private RetryStackAdapter() {
        String savedData = CloudMessagePreferenceManager.getInstance().getRetryStackData();
        try {
            if (!TextUtils.isEmpty(savedData)) {
                this.mStack = (Stack) new ObjectInputStream(new ByteArrayInputStream(Base64.decode(savedData, 0))).readObject();
            }
        } catch (OptionalDataException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        } catch (IOException e2) {
            Log.e(getClass().getSimpleName(), e2.getMessage());
        } catch (ClassNotFoundException e3) {
            Log.e(getClass().getSimpleName(), e3.getMessage());
        } catch (IllegalArgumentException e4) {
            Log.e(getClass().getSimpleName(), e4.getMessage());
            this.mStack = new Stack<>();
            clearRetryHistory();
        }
    }

    public static RetryStackAdapter getInstance() {
        return sInstance;
    }

    public synchronized boolean checkRequestRetried(IHttpAPICommonInterface request) {
        if (this.mStack != null) {
            if (!this.mStack.empty()) {
                return this.mStack.peek().getClass().getSimpleName().equals(request.getClass().getSimpleName());
            }
        }
        return false;
    }

    public synchronized IHttpAPICommonInterface getLastFailedRequest() {
        if (this.mStack != null) {
            if (!this.mStack.empty()) {
                return this.mStack.peek();
            }
        }
        return null;
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003d, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean searchAndPush(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r4) {
        /*
            r3 = this;
            monitor-enter(r3)
            r0 = 0
            if (r4 == 0) goto L_0x003c
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r1 = r3.mStack     // Catch:{ all -> 0x0039 }
            if (r1 == 0) goto L_0x003c
            boolean r1 = r4 instanceof com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest     // Catch:{ all -> 0x0039 }
            if (r1 != 0) goto L_0x000d
            goto L_0x003c
        L_0x000d:
            boolean r1 = r3.checkRequestRetried(r4)     // Catch:{ all -> 0x0039 }
            if (r1 == 0) goto L_0x001c
            java.lang.String r1 = TAG     // Catch:{ all -> 0x0039 }
            java.lang.String r2 = "equal to the top api. Ignore"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0039 }
            monitor-exit(r3)
            return r0
        L_0x001c:
            java.lang.String r0 = TAG     // Catch:{ all -> 0x0039 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0039 }
            r1.<init>()     // Catch:{ all -> 0x0039 }
            java.lang.String r2 = "request = "
            r1.append(r2)     // Catch:{ all -> 0x0039 }
            r1.append(r4)     // Catch:{ all -> 0x0039 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0039 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x0039 }
            r3.push(r4)     // Catch:{ all -> 0x0039 }
            r0 = 1
            monitor-exit(r3)
            return r0
        L_0x0039:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        L_0x003c:
            monitor-exit(r3)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryStackAdapter.searchAndPush(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface):boolean");
    }

    private void push(IHttpAPICommonInterface request) {
        if (request != null) {
            this.mStack.push(request);
            saveRetryStack();
        }
    }

    public synchronized IHttpAPICommonInterface pop() {
        if (this.mStack != null) {
            if (!this.mStack.empty()) {
                IHttpAPICommonInterface rel = this.mStack.pop();
                saveRetryStack();
                return rel;
            }
        }
        return null;
    }

    public synchronized void saveRetryLastFailedTime(long time) {
        CloudMessagePreferenceManager.getInstance().saveLastRetryTime(time);
    }

    public synchronized void clearRetryHistory() {
        Log.i(TAG, "clearRetryCounter: retry history cleared");
        if (this.mStack != null) {
            this.mStack.clear();
            saveRetryStack();
        }
        CloudMessagePreferenceManager.getInstance().removeKey(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER);
        CloudMessagePreferenceManager.getInstance().removeKey(CloudMessagePreferenceConstants.LAST_RETRY_TIME);
    }

    public synchronized boolean isEmpty() {
        return this.mStack.isEmpty();
    }

    private void saveRetryStack() {
        Log.i(TAG, "save retryStack");
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(this.mStack);
            so.flush();
            CloudMessagePreferenceManager.getInstance().saveRetryStackData(Base64.encodeToString(bo.toByteArray(), 0));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isRetryTimesFinished(ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        int totalCounter = iCloudMessageManagerHelper.getTotalRetryCounter();
        String str = TAG;
        Log.i(str, "totalCounter: " + totalCounter);
        return iCloudMessageManagerHelper.getMaxRetryCounter() <= totalCounter;
    }

    public void retryApi(IHttpAPICommonInterface retry, IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelper) {
        if (callback != null) {
            String str = TAG;
            Log.i(str, "retryApi: " + retry.getClass().getSimpleName());
            HttpController.getInstance().execute(retry.getRetryInstance(callback, cloudMessageManagerHelper, retryStackAdapterHelper));
        }
    }

    public synchronized boolean retryLastApi(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelper) {
        IHttpAPICommonInterface lastretry = null;
        if (!(this.mStack == null || this.mStack.isEmpty() || callback == null)) {
            lastretry = this.mStack.peek();
        }
        if (lastretry == null) {
            return false;
        }
        String str = TAG;
        Log.i(str, "retryLastApi: " + lastretry.getClass().getSimpleName());
        HttpController.getInstance().execute(lastretry.getRetryInstance(callback, cloudMessageManagerHelper, retryStackAdapterHelper));
        return true;
    }
}
