package com.verizon.loginclient;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Base64;
import com.motricity.verizon.ssoengine.SSOContentProviderConstants;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class IdentityLoginClient {
    public static final long DEFAULT_TIMEOUT_MS = 60000;
    private static final int MSG_CONTENT_NOTIFY = 1;
    private static final int MSG_TIMEOUT_EXPIRED = 2;
    public static final int SUBSCRIPTION_ID_UNKNOWN = -1;
    private IIdentityResultReceiver mActiveEventReceiver;
    private boolean mAlwaysReturnSubscriptionId;
    private final IIdentityResultReceiver mAsyncEventReceiver;
    private boolean mBypassDeviceFeatureCheck;
    private boolean mBypassEnginePackageCheck;
    private final Context mContext;
    private final IIdentityResultReceiver mDirectEventReceiver;
    private boolean mInteractiveQueryMode;
    /* access modifiers changed from: private */
    public final Object mLockObj;
    /* access modifiers changed from: private */
    public final IdentityMsgHandler mMsgHandler;
    private boolean mObserveOnNullResult;
    private ContentObserver mObserver;
    private final InternalCallbackReceiver mSyncEventReceiver;
    private Integer mTargetSubscriptionId;
    private long mTimeoutMs;

    public interface IIdentityResultReceiver {
        void onErrorResult(ResultCode resultCode, Throwable th);

        void onIdentityResult(IdenResultData idenResultData);
    }

    public enum ResultCode {
        success,
        failure,
        waitingOnObserver,
        timeout,
        deviceNotCapable,
        engineNotInstalled,
        rogueEngineInstalled,
        securityException
    }

    public IdentityLoginClient(Context context, IIdentityResultReceiver evtRec) {
        this(context, evtRec, (Looper) null, (Integer) null);
    }

    public IdentityLoginClient(Context context, IIdentityResultReceiver evtRec, Looper looper, Integer targetSubscriptionId) {
        Looper looper2;
        this.mLockObj = new Object();
        this.mTimeoutMs = 60000;
        this.mInteractiveQueryMode = false;
        this.mBypassDeviceFeatureCheck = false;
        this.mBypassEnginePackageCheck = false;
        this.mObserveOnNullResult = true;
        this.mAlwaysReturnSubscriptionId = true;
        if (context != null) {
            this.mContext = context.getApplicationContext();
            this.mTargetSubscriptionId = targetSubscriptionId;
            AsyncEventReceiver asyncEventReceiver = null;
            this.mSyncEventReceiver = new InternalCallbackReceiver();
            this.mDirectEventReceiver = evtRec;
            this.mAsyncEventReceiver = evtRec != null ? new AsyncEventReceiver(evtRec) : asyncEventReceiver;
            if (looper != null) {
                looper2 = looper;
            } else {
                looper2 = context.getMainLooper();
            }
            this.mMsgHandler = new IdentityMsgHandler(looper2, this);
            return;
        }
        throw new IllegalArgumentException("context cannot be null");
    }

    public void queryIdentityAsync() {
        IIdentityResultReceiver iIdentityResultReceiver = this.mAsyncEventReceiver;
        if (iIdentityResultReceiver != null) {
            this.mActiveEventReceiver = iIdentityResultReceiver;
            new Thread(new Runnable() {
                public void run() {
                    IdentityQueryResult result = IdentityLoginClient.this.doIdentityQuery();
                    if (result.getResultCode() != ResultCode.waitingOnObserver) {
                        IdentityLoginClient.this.callbackWithResult(result);
                    }
                }
            }).start();
            return;
        }
        throw new IllegalStateException("cannot perform async query with null callback receiver (constructor)");
    }

    public IdentityQueryResult queryIdentitySynchronous() {
        IdentityQueryResult result;
        if (!isRunningOnLooperThread()) {
            synchronized (this.mLockObj) {
                this.mActiveEventReceiver = this.mSyncEventReceiver;
                this.mSyncEventReceiver.result = null;
                result = doIdentityQuery();
                if (result.mResultCode == ResultCode.waitingOnObserver) {
                    try {
                        this.mLockObj.wait(this.mTimeoutMs + 1000);
                        result = this.mSyncEventReceiver.result;
                        this.mSyncEventReceiver.result = null;
                    } catch (InterruptedException e) {
                    }
                    if (result == null) {
                        result = new IdentityQueryResult(ResultCode.failure, (IdenResultData) null, (Throwable) null);
                    }
                }
            }
            return result;
        }
        throw new IllegalStateException("synchronous query cannot be run on Handler's Looper thread");
    }

    public IdentityQueryResult queryIdentityDirect() {
        this.mActiveEventReceiver = this.mDirectEventReceiver;
        return doIdentityQuery();
    }

    public void invalidateToken() throws SecurityException {
        doTokenDelete(false);
    }

    public void invalidateAllTokens() throws SecurityException {
        doTokenDelete(true);
    }

    public void cancelQuery() {
        unregisterContentObserver();
        stopTimeoutWait();
        this.mActiveEventReceiver = null;
        synchronized (this.mLockObj) {
            this.mLockObj.notifyAll();
        }
    }

    public String getPackageName() {
        return getLoginClientPackage();
    }

    public boolean isDozeWhitelisted() {
        PowerManager pm;
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        String packageName = getLoginClientPackage();
        if (packageName == null || (pm = (PowerManager) this.mContext.getSystemService("power")) == null) {
            return false;
        }
        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    public void setTargetSubscriptionId(Integer subscriptionId) {
        this.mTargetSubscriptionId = subscriptionId;
    }

    public void setTimeout(long timeoutMs) {
        this.mTimeoutMs = timeoutMs;
    }

    public void setInteractiveQuery(boolean interactive) {
        this.mInteractiveQueryMode = interactive;
    }

    public void bypassDeviceFeatureCheck(boolean bypass) {
        this.mBypassDeviceFeatureCheck = bypass;
    }

    public void bypassEnginePackageCheck(boolean bypass) {
        this.mBypassEnginePackageCheck = bypass;
    }

    public void setObserveOnNullResult(boolean observeOnNull) {
        this.mObserveOnNullResult = observeOnNull;
    }

    public void setAlwaysRequestSubsciptionId(boolean requestSubId) {
        this.mAlwaysReturnSubscriptionId = requestSubId;
    }

    private void doTokenDelete(boolean allTokens) throws SecurityException {
        Uri uri = getLoginClientUri();
        SelectParameters params = buildDeleteParams(allTokens);
        if (uri != null) {
            try {
                this.mContext.getContentResolver().delete(uri, params.getSelectString(), params.getSelectParams());
            } catch (IllegalArgumentException | IllegalStateException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public IdentityQueryResult doIdentityQuery() {
        if (!isDeviceCapable()) {
            return new IdentityQueryResult(ResultCode.deviceNotCapable, (IdenResultData) null, (Throwable) null);
        }
        return queryContentProvider(buildQueryParams(), this.mObserveOnNullResult);
    }

    /* access modifiers changed from: private */
    public IdentityQueryResult queryContentProvider(SelectParameters params, boolean observeOnNull) {
        Uri uri = getLoginClientUri();
        if (uri == null) {
            return new IdentityQueryResult(ResultCode.engineNotInstalled, (IdenResultData) null, (Throwable) null);
        }
        try {
            Cursor cursor = this.mContext.getContentResolver().query(uri, (String[]) null, params.getSelectString(), params.getSelectParams(), (String) null);
            if (cursor == null) {
                return new IdentityQueryResult(ResultCode.engineNotInstalled, (IdenResultData) null, (Throwable) null);
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                if (!observeOnNull || this.mTimeoutMs <= 0) {
                    return new IdentityQueryResult(ResultCode.failure, (IdenResultData) null, (Throwable) null);
                }
                registerContentObserver(uri);
                startTimeoutWait();
                return new IdentityQueryResult(ResultCode.waitingOnObserver, (IdenResultData) null, (Throwable) null);
            }
            IdenResultData idenResult = processIdentityQueryResult(cursor);
            cursor.close();
            if (idenResult != null) {
                return new IdentityQueryResult(ResultCode.success, idenResult, (Throwable) null);
            }
            return new IdentityQueryResult(ResultCode.failure, (IdenResultData) null, (Throwable) null);
        } catch (SecurityException ex) {
            return new IdentityQueryResult(ResultCode.securityException, (IdenResultData) null, ex);
        } catch (IllegalArgumentException | IllegalStateException ex2) {
            return new IdentityQueryResult(ResultCode.failure, (IdenResultData) null, ex2);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 18 */
    private IdenResultData processIdentityQueryResult(Cursor cursor) throws IllegalArgumentException {
        Cursor cursor2 = cursor;
        try {
            String tokenValueRaw = cursor2.getString(cursor2.getColumnIndexOrThrow("token"));
            if (tokenValueRaw != null) {
                String token = Base64.encodeToString(tokenValueRaw.getBytes(), 2);
                int subIdColIndx = cursor2.getColumnIndex("subscriptionId");
                return new IdenResultData(cursor2.getString(cursor2.getColumnIndexOrThrow(SSOContentProviderConstants.ResultFields.IMEI)), cursor2.getString(cursor2.getColumnIndexOrThrow("imsi")), cursor2.getString(cursor2.getColumnIndexOrThrow(SSOContentProviderConstants.ResultFields.MDN)), cursor2.getString(cursor2.getColumnIndexOrThrow(SSOContentProviderConstants.ResultFields.SIGNATURE)), cursor2.getLong(cursor2.getColumnIndexOrThrow(SSOContentProviderConstants.ResultFields.SIGNATURE_CREATE_TIME)), cursor2.getLong(cursor2.getColumnIndexOrThrow(SSOContentProviderConstants.ResultFields.SIGNATURE_EXPIRE_TIME)), Integer.valueOf(subIdColIndx > 0 ? cursor2.getInt(subIdColIndx) : -1), cursor2.getLong(cursor2.getColumnIndexOrThrow(SSOContentProviderConstants.ResultFields.TID)), token);
            }
            throw new IllegalArgumentException("token value was null");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public SelectParameters buildQueryParams() {
        SelectParameters.Builder builder = new SelectParameters.Builder().setSubscriptionId(this.mTargetSubscriptionId);
        if (this.mAlwaysReturnSubscriptionId) {
            builder.setAlwaysSendSubscriberId(true);
        }
        return builder.build();
    }

    private SelectParameters buildDeleteParams(boolean deleteAll) {
        SelectParameters.Builder builder = new SelectParameters.Builder();
        if (deleteAll) {
            builder.setDeleteAllTokens(true);
        } else {
            builder.setSubscriptionId(this.mTargetSubscriptionId);
        }
        return builder.build();
    }

    private synchronized void registerContentObserver(Uri uri) {
        unregisterContentObserver();
        this.mObserver = new TokenContentObserver(this.mMsgHandler);
        this.mContext.getContentResolver().registerContentObserver(uri, false, this.mObserver);
    }

    /* access modifiers changed from: private */
    public synchronized void unregisterContentObserver() {
        if (this.mObserver != null) {
            try {
                this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            } catch (IllegalStateException e) {
            }
            this.mObserver = null;
        }
    }

    private boolean isDeviceCapable() {
        if (this.mBypassDeviceFeatureCheck) {
            return true;
        }
        PackageManager pm = this.mContext.getPackageManager();
        for (String feature : SSOContentProviderConstants.LTE_FEATURES) {
            if (pm.hasSystemFeature(feature)) {
                return true;
            }
        }
        return false;
    }

    private Uri getLoginClientUri() {
        PackageManager pm = this.mContext.getPackageManager();
        for (String authority : SSOContentProviderConstants.AUTHORITIES) {
            ProviderInfo cpInfo = pm.resolveContentProvider(authority, 0);
            if (cpInfo != null) {
                for (String officalPackage : SSOContentProviderConstants.OFFICIAL_PACKAGES) {
                    if (officalPackage.equals(cpInfo.packageName)) {
                        return buildQueryUri(authority);
                    }
                }
                if (this.mBypassEnginePackageCheck) {
                    return buildQueryUri(authority);
                }
            }
        }
        return null;
    }

    private Uri buildQueryUri(String authority) {
        if (authority == null) {
            return null;
        }
        return Uri.parse(String.format(Locale.ENGLISH, "content://%s/%s%s", new Object[]{authority, SSOContentProviderConstants.IDEN_PATH, this.mInteractiveQueryMode ? "" : SSOContentProviderConstants.SILENT_PATH_SUFFIX}));
    }

    private String getLoginClientPackage() {
        PackageManager pm = this.mContext.getPackageManager();
        for (String authority : SSOContentProviderConstants.AUTHORITIES) {
            ProviderInfo cpInfo = pm.resolveContentProvider(authority, 0);
            if (cpInfo != null) {
                for (String officalPackage : SSOContentProviderConstants.OFFICIAL_PACKAGES) {
                    if (officalPackage.equals(cpInfo.packageName)) {
                        return cpInfo.packageName;
                    }
                }
                if (this.mBypassEnginePackageCheck) {
                    return cpInfo.packageName;
                }
            }
        }
        return null;
    }

    private boolean isRunningOnLooperThread() {
        IdentityMsgHandler identityMsgHandler = this.mMsgHandler;
        Looper msgLooper = identityMsgHandler == null ? null : identityMsgHandler.getLooper();
        if (msgLooper == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return msgLooper.isCurrentThread();
        }
        if (Thread.currentThread() == msgLooper.getThread()) {
            return true;
        }
        return false;
    }

    private void startTimeoutWait() {
        this.mMsgHandler.sendMessageDelayed(this.mMsgHandler.obtainMessage(2), this.mTimeoutMs);
    }

    /* access modifiers changed from: private */
    public void stopTimeoutWait() {
        this.mMsgHandler.removeMessages(2);
    }

    /* access modifiers changed from: private */
    public void callbackWithResult(IdentityQueryResult result) {
        IIdentityResultReceiver callback = this.mActiveEventReceiver;
        if (callback != null) {
            if (result == null) {
                result = new IdentityQueryResult(ResultCode.failure, (IdenResultData) null, (Throwable) null);
            }
            if (result.getResultCode() == ResultCode.success) {
                callback.onIdentityResult(result.getIdentityData());
            } else {
                callback.onErrorResult(result.getResultCode(), result.getException());
            }
        }
    }

    private static class IdentityMsgHandler extends Handler {
        private final WeakReference<IdentityLoginClient> mParent;

        public IdentityMsgHandler(Looper looper, IdentityLoginClient parent) {
            super(looper);
            this.mParent = new WeakReference<>(parent);
        }

        public void handleMessage(Message msg) {
            IdentityLoginClient parent = (IdentityLoginClient) this.mParent.get();
            if (parent != null) {
                if (msg.what == 1) {
                    parent.stopTimeoutWait();
                    parent.callbackWithResult(parent.queryContentProvider(parent.buildQueryParams(), false));
                }
                if (msg.what == 2) {
                    parent.unregisterContentObserver();
                    parent.callbackWithResult(new IdentityQueryResult(ResultCode.timeout, (IdenResultData) null, (Throwable) null));
                }
            }
        }
    }

    class TokenContentObserver extends ContentObserver {
        private final Handler mHandler;

        TokenContentObserver(Handler hndlr) {
            super(hndlr);
            this.mHandler = hndlr;
        }

        public void onChange(boolean selfChange) {
            IdentityLoginClient.this.unregisterContentObserver();
            Handler handler = this.mHandler;
            if (handler != null) {
                this.mHandler.sendMessage(handler.obtainMessage(1));
            }
        }
    }

    public static class IdenResultData {
        public final String imei;
        public final String imsi;
        public final String mdn;
        public final String signature;
        public final long signatureCreateTime;
        public final long signatureExpireTime;
        public final int subscriptionId;
        public final long tid;
        public final String token;

        public IdenResultData(String imei2, String imsi2, String mdn2, String sig, long sigCreate, long sigExpire, Integer subId, long tid2, String token2) {
            this.imei = imei2;
            this.imsi = imsi2;
            this.mdn = mdn2;
            this.signature = sig;
            this.signatureCreateTime = sigCreate;
            this.signatureExpireTime = sigExpire;
            this.subscriptionId = subId.intValue();
            this.tid = tid2;
            this.token = token2;
        }
    }

    public static class IdentityQueryResult {
        private final Throwable mException;
        private final IdenResultData mIdentityData;
        /* access modifiers changed from: private */
        public final ResultCode mResultCode;

        private IdentityQueryResult(ResultCode rc, IdenResultData idenData, Throwable ex) {
            this.mResultCode = rc;
            this.mIdentityData = idenData;
            this.mException = ex;
        }

        public ResultCode getResultCode() {
            return this.mResultCode;
        }

        public IdenResultData getIdentityData() {
            return this.mIdentityData;
        }

        public Throwable getException() {
            return this.mException;
        }
    }

    private static class SelectParameters {
        private final String[] mSelectParams;
        private final String mSelectString;

        private SelectParameters(String selectString, String[] selectParams) {
            this.mSelectString = selectString;
            this.mSelectParams = selectParams;
        }

        public String getSelectString() {
            return this.mSelectString;
        }

        public String[] getSelectParams() {
            return this.mSelectParams;
        }

        static class Builder {
            private Boolean mAlwaysSendSubscriberId = null;
            private Boolean mDeleteAllTokens = null;
            private Integer mTargetSubscriptionId = null;

            Builder() {
            }

            public SelectParameters build() {
                int i;
                int paramCount = getParamCount();
                if (paramCount == 0) {
                    return new SelectParameters((String) null, (String[]) null);
                }
                String[] vs = new String[paramCount];
                String s = "";
                int i2 = 0;
                Integer num = this.mTargetSubscriptionId;
                if (num != null) {
                    s = addParam(s, vs, "subscriptionId", Integer.toString(num.intValue()), 0);
                    i2 = 0 + 1;
                }
                Boolean bool = this.mAlwaysSendSubscriberId;
                if (bool != null) {
                    i = i2 + 1;
                    s = addParam(s, vs, SSOContentProviderConstants.SelectParams.ALWAYS_RETURN_SUBID, bool.toString(), i2);
                } else {
                    i = i2;
                }
                Boolean bool2 = this.mDeleteAllTokens;
                if (bool2 != null) {
                    s = addParam(s, vs, SSOContentProviderConstants.SelectParams.DELETE_ALL, bool2.toString(), i);
                }
                return new SelectParameters(s, vs);
            }

            public Builder setAlwaysSendSubscriberId(Boolean b) {
                this.mAlwaysSendSubscriberId = b;
                return this;
            }

            public Builder setSubscriptionId(Integer i) {
                this.mTargetSubscriptionId = i;
                return this;
            }

            public Builder setDeleteAllTokens(Boolean b) {
                this.mDeleteAllTokens = b;
                return this;
            }

            private int getParamCount() {
                int c = 0;
                if (this.mAlwaysSendSubscriberId != null) {
                    c = 0 + 1;
                }
                if (this.mDeleteAllTokens != null) {
                    c++;
                }
                if (this.mTargetSubscriptionId != null) {
                    return c + 1;
                }
                return c;
            }

            private String addParam(String s, String[] vs, String name, String value, int indx) {
                if (s.length() > 0) {
                    s = s + " AND ";
                }
                String s2 = s + name + " = ?";
                vs[indx] = value;
                return s2;
            }
        }
    }

    private class InternalCallbackReceiver implements IIdentityResultReceiver {
        IdentityQueryResult result;

        private InternalCallbackReceiver() {
            this.result = null;
        }

        public void onIdentityResult(IdenResultData idenResult) {
            handleEvent(ResultCode.success, idenResult, (Throwable) null);
        }

        public void onErrorResult(ResultCode status, Throwable ex) {
            handleEvent(status, (IdenResultData) null, ex);
        }

        private void handleEvent(ResultCode rc, IdenResultData idenData, Throwable ex) {
            synchronized (IdentityLoginClient.this.mLockObj) {
                this.result = new IdentityQueryResult(rc, idenData, ex);
                IdentityLoginClient.this.mLockObj.notifyAll();
            }
        }
    }

    private class AsyncEventReceiver implements IIdentityResultReceiver {
        /* access modifiers changed from: private */
        public final IIdentityResultReceiver mClientReceiver;

        AsyncEventReceiver(IIdentityResultReceiver clientReceiver) {
            if (clientReceiver != null) {
                this.mClientReceiver = clientReceiver;
                return;
            }
            throw new IllegalArgumentException("client receiver cannot be null");
        }

        public void onIdentityResult(final IdenResultData idenResult) {
            IdentityLoginClient.this.mMsgHandler.post(new Runnable() {
                public void run() {
                    AsyncEventReceiver.this.mClientReceiver.onIdentityResult(idenResult);
                }
            });
        }

        public void onErrorResult(final ResultCode status, final Throwable ex) {
            IdentityLoginClient.this.mMsgHandler.post(new Runnable() {
                public void run() {
                    AsyncEventReceiver.this.mClientReceiver.onErrorResult(status, ex);
                }
            });
        }
    }
}
