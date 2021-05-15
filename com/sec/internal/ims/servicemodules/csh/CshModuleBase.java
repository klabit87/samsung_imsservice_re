package com.sec.internal.ims.servicemodules.csh;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.csh.ICshModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CshModuleBase extends ServiceModuleBase implements ICshModule {
    private static String LOG_TAG = CshModuleBase.class.getSimpleName();
    protected Map<Integer, List<ICall>> mActiveCallLists = new HashMap();
    protected int mActiveCallPhoneId = 0;
    protected CshCache mCache;
    protected Context mContext;
    protected boolean mIsDuringMultipartyCall = false;
    protected boolean mIsServiceReady = false;
    protected int mNPrevConnectedCalls = 0;
    protected Capabilities mRemoteCapabilities = new Capabilities();

    protected CshModuleBase(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public void putSession(IContentShare session) {
        this.mCache.putSession(session);
    }

    public void deleteSession(int sessionId) {
        this.mCache.deleteSession(sessionId);
    }

    public IContentShare getSession(long sharedId) {
        if (sharedId < 0) {
            return null;
        }
        return this.mCache.getSession(sharedId);
    }

    public void notifyContentChange(IContentShare session) {
        String str = LOG_TAG;
        Log.d(str, "Update share [" + session.getContent() + "]");
        this.mContext.getContentResolver().notifyChange(ICshConstants.ShareDatabase.ACTIVE_SESSIONS_URI, (ContentObserver) null);
    }

    public void onCallStateChanged(int phoneId, List<ICall> calls) {
        processCallStateChanged(phoneId, new CopyOnWriteArrayList(calls));
    }

    private int countConnectedCall(List<ICall> calls) {
        int numConnectedCalls = 0;
        for (ICall call : calls) {
            if (call.isConnected()) {
                numConnectedCalls++;
            }
        }
        return numConnectedCalls;
    }

    private void processCallStateChanged(int phoneId, CopyOnWriteArrayList<ICall> calls) {
        int nConnectedCalls = countConnectedCall(calls);
        List<ICall> activeCalls = new ArrayList<>();
        if (this.mActiveCallLists.containsKey(Integer.valueOf(phoneId))) {
            activeCalls = this.mActiveCallLists.get(Integer.valueOf(phoneId));
        }
        int nPrevConnectedCalls = countConnectedCall(activeCalls);
        Iterator<ICall> it = calls.iterator();
        while (it.hasNext()) {
            ICall call = it.next();
            ICall prev = getCall(activeCalls, call.getNumber());
            if (prev == null) {
                if (call.isConnected() && nConnectedCalls == 1) {
                    this.mActiveCallPhoneId = phoneId;
                    this.mIsDuringMultipartyCall = false;
                    this.mCache.init();
                } else if (nConnectedCalls > 1) {
                    this.mIsDuringMultipartyCall = true;
                }
            } else if ((!prev.isConnected() || nPrevConnectedCalls > 1) && call.isConnected() && nConnectedCalls == 1) {
                this.mActiveCallPhoneId = phoneId;
                this.mIsDuringMultipartyCall = false;
            } else if ((prev.isConnected() && nPrevConnectedCalls == 1 && (!call.isConnected() || nConnectedCalls > 1)) || (!prev.isConnected() && call.isConnected() && nConnectedCalls > 1)) {
                this.mIsDuringMultipartyCall = true;
            }
            activeCalls.remove(prev);
        }
        this.mActiveCallLists.put(Integer.valueOf(phoneId), calls);
        this.mNPrevConnectedCalls = nConnectedCalls;
        updateServiceStatus(phoneId);
    }

    private ICall getCall(List<ICall> activeCalls, String number) {
        for (ICall call : activeCalls) {
            if (TextUtils.equals(call.getNumber(), number)) {
                return call;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void updateServiceStatus(int phoneId) {
        String str = LOG_TAG;
        Log.d(str, "updateServiceStatus: mIsServiceReady=" + this.mIsServiceReady + ", mEnabledFeatures=" + this.mEnabledFeatures[phoneId] + ", mRemoteCapabilities=" + this.mRemoteCapabilities.getFeature() + ", mIsDuringMultipartyCall=" + this.mIsDuringMultipartyCall);
    }

    public void onRemoteCapabilitiesChanged(Capabilities cap) {
        this.mRemoteCapabilities = cap;
        updateServiceStatus(this.mDefaultPhoneId);
    }
}
