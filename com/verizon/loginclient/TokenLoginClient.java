package com.verizon.loginclient;

import android.content.Context;
import android.content.Intent;
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
import com.motricity.verizon.ssoengine.SSOClientIntentAction;
import com.motricity.verizon.ssoengine.SSOContentProviderConstants;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class TokenLoginClient {
    public static final long DEFAULT_TIMEOUT_MS = 60000;
    private static final int MSG_CONTENT_NOTIFY = 1;
    private static final int MSG_TIMEOUT_EXPIRED = 2;
    public static final int SUBSCRIPTION_ID_UNKNOWN = -1;
    private ILoginClientReceiver mActiveEventReceiver;
    private boolean mAlwaysReturnSubscriptionId;
    private final ILoginClientReceiver mAsyncEventReceiver;
    private boolean mBypassDeviceFeatureCheck;
    private boolean mBypassEnginePackageCheck;
    private final Context mContext;
    private final ILoginClientReceiver mDirectEventReceiver;
    private boolean mInteractiveQueryMode;
    /* access modifiers changed from: private */
    public final Object mLockObj;
    /* access modifiers changed from: private */
    public final TokenMsgHandler mMsgHandler;
    private boolean mObserveOnNullResult;
    private ContentObserver mObserver;
    private final InternalCallbackReceiver mSyncEventReceiver;
    private Integer mTargetSubscriptionId;
    private long mTimeoutMs;
    private boolean mTokenTypeAuth;

    public interface ILoginClientReceiver {
        void onErrorResult(ResultCode resultCode, Throwable th);

        void onTokenResult(TokenQueryData tokenQueryData);
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

    public TokenLoginClient(Context context, ILoginClientReceiver evtRec) {
        this(context, evtRec, (Looper) null, (Integer) null);
    }

    public TokenLoginClient(Context context, ILoginClientReceiver evtRec, Looper looper, Integer targetSubscriptionId) {
        Looper looper2;
        this.mLockObj = new Object();
        this.mTimeoutMs = 60000;
        this.mInteractiveQueryMode = false;
        this.mTokenTypeAuth = false;
        this.mBypassDeviceFeatureCheck = false;
        this.mBypassEnginePackageCheck = false;
        this.mObserveOnNullResult = true;
        this.mAlwaysReturnSubscriptionId = true;
        if (context != null) {
            this.mContext = context.getApplicationContext();
            this.mTargetSubscriptionId = targetSubscriptionId;
            this.mSyncEventReceiver = new InternalCallbackReceiver();
            this.mDirectEventReceiver = evtRec;
            this.mAsyncEventReceiver = evtRec == null ? null : new AsyncEventReceiver(evtRec);
            if (looper != null) {
                looper2 = looper;
            } else {
                looper2 = context.getMainLooper();
            }
            this.mMsgHandler = new TokenMsgHandler(looper2, this);
            return;
        }
        throw new IllegalArgumentException("context cannot be null");
    }

    public void queryTokenAsync() {
        ILoginClientReceiver iLoginClientReceiver = this.mAsyncEventReceiver;
        if (iLoginClientReceiver != null) {
            this.mActiveEventReceiver = iLoginClientReceiver;
            new Thread(new Runnable() {
                public void run() {
                    TokenQueryResult result = TokenLoginClient.this.doTokenQuery();
                    if (result.getResultCode() != ResultCode.waitingOnObserver) {
                        TokenLoginClient.this.callbackWithResult(result);
                    }
                }
            }).start();
            return;
        }
        throw new IllegalStateException("cannot perform async query with null callback receiver (constructor)");
    }

    public TokenQueryResult queryTokenSynchronous() {
        TokenQueryResult result;
        if (!isRunningOnLooperThread()) {
            synchronized (this.mLockObj) {
                this.mActiveEventReceiver = this.mSyncEventReceiver;
                TokenQueryResult unused = this.mSyncEventReceiver.result = null;
                result = doTokenQuery();
                if (result.mResultCode == ResultCode.waitingOnObserver) {
                    try {
                        this.mLockObj.wait(this.mTimeoutMs + 1000);
                        result = this.mSyncEventReceiver.result;
                        TokenQueryResult unused2 = this.mSyncEventReceiver.result = null;
                    } catch (InterruptedException e) {
                    }
                    if (result == null) {
                        result = new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, (Throwable) null);
                    }
                }
            }
            return result;
        }
        throw new IllegalStateException("synchronous query cannot be run on Handler's Looper thread");
    }

    public TokenQueryResult queryTokenDirect() {
        this.mActiveEventReceiver = this.mDirectEventReceiver;
        return doTokenQuery();
    }

    public void establishToken() {
        String lcPackage = getLoginClientPackage();
        if (lcPackage != null && !lcPackage.isEmpty()) {
            Intent intent = new Intent(SSOClientIntentAction.ESTABLISH_TOKEN);
            intent.setPackage(lcPackage);
            Integer num = this.mTargetSubscriptionId;
            if (num != null) {
                intent.putExtra("subscriptionId", num);
            }
            this.mContext.sendBroadcast(intent);
        }
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

    public void setUseAuthTokens(boolean useAuthTokens) {
        this.mTokenTypeAuth = useAuthTokens;
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
    public TokenQueryResult doTokenQuery() {
        if (!isDeviceCapable()) {
            return new TokenQueryResult(ResultCode.deviceNotCapable, (TokenQueryData) null, (Throwable) null);
        }
        return queryContentProvider(buildQueryParams(), this.mObserveOnNullResult);
    }

    /* access modifiers changed from: private */
    public TokenQueryResult queryContentProvider(SelectParameters params, boolean observeOnNull) {
        Uri uri = getLoginClientUri();
        if (uri == null) {
            return new TokenQueryResult(ResultCode.engineNotInstalled, (TokenQueryData) null, (Throwable) null);
        }
        try {
            Cursor cursor = this.mContext.getContentResolver().query(uri, (String[]) null, params.getSelectString(), params.getSelectParams(), (String) null);
            if (cursor == null) {
                return new TokenQueryResult(ResultCode.engineNotInstalled, (TokenQueryData) null, (Throwable) null);
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, (Throwable) null);
            }
            String tokenValueRaw = cursor.getString(cursor.getColumnIndex("token"));
            int subIdColIndx = cursor.getColumnIndex("subscriptionId");
            int subscriptionId = subIdColIndx > 0 ? cursor.getInt(subIdColIndx) : -1;
            cursor.close();
            if (tokenValueRaw != null) {
                return new TokenQueryResult(ResultCode.success, new TokenQueryData(Base64.encodeToString(tokenValueRaw.getBytes(), 2), subscriptionId), (Throwable) null);
            } else if (!observeOnNull || this.mTimeoutMs <= 0) {
                return new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, (Throwable) null);
            } else {
                registerContentObserver(uri);
                startTimeoutWait();
                return new TokenQueryResult(ResultCode.waitingOnObserver, (TokenQueryData) null, (Throwable) null);
            }
        } catch (SecurityException ex) {
            return new TokenQueryResult(ResultCode.securityException, (TokenQueryData) null, ex);
        } catch (IllegalArgumentException | IllegalStateException ex2) {
            return new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, ex2);
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
        return Uri.parse(String.format(Locale.ENGLISH, "content://%s/%s%s", new Object[]{authority, this.mTokenTypeAuth ? SSOContentProviderConstants.AUTHTOKEN_PATH : "token", this.mInteractiveQueryMode ? "" : SSOContentProviderConstants.SILENT_PATH_SUFFIX}));
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
        TokenMsgHandler tokenMsgHandler = this.mMsgHandler;
        Looper msgLooper = tokenMsgHandler == null ? null : tokenMsgHandler.getLooper();
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
    public void callbackWithResult(TokenQueryResult result) {
        ILoginClientReceiver callback = this.mActiveEventReceiver;
        if (callback != null) {
            if (result == null) {
                result = new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, (Throwable) null);
            }
            if (result.getResultCode() == ResultCode.success) {
                callback.onTokenResult(result.getTokenData());
            } else {
                callback.onErrorResult(result.getResultCode(), result.getException());
            }
        }
    }

    private static class TokenMsgHandler extends Handler {
        private final WeakReference<TokenLoginClient> mParent;

        private TokenMsgHandler(Looper looper, TokenLoginClient parent) {
            super(looper);
            this.mParent = new WeakReference<>(parent);
        }

        public void handleMessage(Message msg) {
            TokenLoginClient parent = (TokenLoginClient) this.mParent.get();
            if (parent != null) {
                if (msg.what == 1) {
                    parent.stopTimeoutWait();
                    parent.callbackWithResult(parent.queryContentProvider(parent.buildQueryParams(), false));
                }
                if (msg.what == 2) {
                    parent.unregisterContentObserver();
                    parent.callbackWithResult(new TokenQueryResult(ResultCode.timeout, (TokenQueryData) null, (Throwable) null));
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
            TokenLoginClient.this.unregisterContentObserver();
            Handler handler = this.mHandler;
            if (handler != null) {
                this.mHandler.sendMessage(handler.obtainMessage(1));
            }
        }
    }

    public static class TokenQueryData {
        public final int subscriptionId;
        public final String token;

        public TokenQueryData(String token2, int subscriptionId2) {
            this.token = token2;
            this.subscriptionId = subscriptionId2;
        }
    }

    public static class TokenQueryResult {
        private final Throwable mException;
        /* access modifiers changed from: private */
        public final ResultCode mResultCode;
        private final TokenQueryData mTokenData;

        private TokenQueryResult(ResultCode rc, TokenQueryData tokenData, Throwable ex) {
            this.mResultCode = rc;
            this.mTokenData = tokenData;
            this.mException = null;
        }

        public ResultCode getResultCode() {
            return this.mResultCode;
        }

        public TokenQueryData getTokenData() {
            return this.mTokenData;
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

    private class InternalCallbackReceiver implements ILoginClientReceiver {
        /* access modifiers changed from: private */
        public TokenQueryResult result;

        private InternalCallbackReceiver() {
            this.result = null;
        }

        public void onTokenResult(TokenQueryData tokenData) {
            handleEvent(ResultCode.success, tokenData, (Throwable) null);
        }

        public void onErrorResult(ResultCode status, Throwable ex) {
            handleEvent(status, (TokenQueryData) null, ex);
        }

        private void handleEvent(ResultCode rc, TokenQueryData tokenData, Throwable ex) {
            synchronized (TokenLoginClient.this.mLockObj) {
                this.result = new TokenQueryResult(rc, tokenData, ex);
                TokenLoginClient.this.mLockObj.notifyAll();
            }
        }
    }

    private class AsyncEventReceiver implements ILoginClientReceiver {
        /* access modifiers changed from: private */
        public final ILoginClientReceiver mClientReceiver;

        AsyncEventReceiver(ILoginClientReceiver clientReceiver) {
            if (clientReceiver != null) {
                this.mClientReceiver = clientReceiver;
                return;
            }
            throw new IllegalArgumentException("client receiver cannot be null");
        }

        public void onTokenResult(final TokenQueryData result) {
            TokenLoginClient.this.mMsgHandler.post(new Runnable() {
                public void run() {
                    AsyncEventReceiver.this.mClientReceiver.onTokenResult(result);
                }
            });
        }

        public void onErrorResult(final ResultCode status, final Throwable ex) {
            TokenLoginClient.this.mMsgHandler.post(new Runnable() {
                public void run() {
                    AsyncEventReceiver.this.mClientReceiver.onErrorResult(status, ex);
                }
            });
        }
    }
}
