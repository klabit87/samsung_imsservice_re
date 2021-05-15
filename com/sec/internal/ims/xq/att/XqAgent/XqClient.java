package com.sec.internal.ims.xq.att.XqAgent;

import android.util.Log;
import com.att.iqi.lib.IQIManager;
import com.att.iqi.lib.Metric;
import java.lang.reflect.Method;

public class XqClient {
    /* access modifiers changed from: private */
    public String LOG_TAG = "XqAgent";
    Object XqMgr;
    public boolean mLoaded = false;
    public boolean mRunning = false;
    Method mSubmitMetric;
    IQIManager mXqMgr = null;
    private IQIManager.ServiceStateChangeListener mXqServiceListener = null;

    public XqClient() {
        setupXq();
    }

    private void setupXq() {
        if (this.mXqMgr == null) {
            this.mXqMgr = IQIManager.getInstance();
        }
        if (this.mXqMgr != null && !this.mLoaded) {
            this.mLoaded = true;
            AnonymousClass1 r0 = new IQIManager.ServiceStateChangeListener() {
                public void onServiceChange(boolean enabled) {
                    String access$000 = XqClient.this.LOG_TAG;
                    Log.d(access$000, "onServiceChange : " + enabled);
                    XqClient.this.mRunning = enabled;
                }
            };
            this.mXqServiceListener = r0;
            this.mXqMgr.registerServiceStateChangeListener(r0);
        }
    }

    public boolean isMgrReady() {
        if (!this.mLoaded || !this.mRunning) {
            setupXq();
        }
        return this.mLoaded && this.mRunning;
    }

    public int submitMetric(Metric mtrip, Metric.ID id) {
        if (!this.mLoaded || !this.mRunning) {
            Log.d(this.LOG_TAG, "Not running");
            return -1;
        }
        IQIManager iQIManager = this.mXqMgr;
        if (iQIManager == null) {
            Log.d(this.LOG_TAG, "Manager is null");
            return -1;
        } else if (!iQIManager.shouldSubmitMetric(id)) {
            return 0;
        } else {
            Log.d(this.LOG_TAG, "should submit");
            this.mXqMgr.submitMetric(mtrip);
            return 0;
        }
    }

    public void resetXqClient() {
        IQIManager iQIManager = this.mXqMgr;
        if (iQIManager != null) {
            iQIManager.unregisterServiceStateChangeListener(this.mXqServiceListener);
            this.mRunning = false;
            this.mLoaded = false;
        }
    }
}
