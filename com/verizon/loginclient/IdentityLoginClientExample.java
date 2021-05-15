package com.verizon.loginclient;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.util.Base64;
import android.util.Log;
import com.verizon.loginclient.IdentityLoginClient;
import java.util.Locale;

public class IdentityLoginClientExample implements IdentityLoginClient.IIdentityResultReceiver {
    private static final String TAG = "IdentityExample";
    private final IdentityLoginClient mLc;

    public IdentityLoginClientExample(Context context) {
        this.mLc = new IdentityLoginClient(context, this);
    }

    public void exampleActions() {
        String packageName = this.mLc.getPackageName();
        boolean isDozeWhitelisted = this.mLc.isDozeWhitelisted();
        try {
            this.mLc.invalidateToken();
        } catch (SecurityException e) {
        }
        this.mLc.queryIdentityAsync();
        IdentityLoginClient.IdentityQueryResult queryIdentitySynchronous = this.mLc.queryIdentitySynchronous();
        this.mLc.queryIdentityDirect().getResultCode();
        IdentityLoginClient.ResultCode resultCode = IdentityLoginClient.ResultCode.waitingOnObserver;
        this.mLc.cancelQuery();
    }

    public void exampleAdvancedSettings() {
        this.mLc.setTargetSubscriptionId(Integer.valueOf(SubscriptionManager.getDefaultSubscriptionId()));
        this.mLc.setTimeout(60000);
        this.mLc.setInteractiveQuery(false);
        this.mLc.bypassDeviceFeatureCheck(false);
        this.mLc.bypassEnginePackageCheck(false);
        this.mLc.setObserveOnNullResult(true);
    }

    public void onIdentityResult(IdentityLoginClient.IdenResultData resultData) {
        Log.i(TAG, String.format(Locale.ENGLISH, "Verified User/Device Identites - MDN:%s IMEI:%s IMSI:%s", new Object[]{resultData.mdn, resultData.imei, resultData.imsi}));
        Log.i(TAG, String.format(Locale.ENGLISH, "SPC Signature Info - tid:%s  signatureCreate:%s (epoch-ms) signatureExpire:%d (epoch-ms) signature:%s", new Object[]{Long.valueOf(resultData.tid), Long.valueOf(resultData.signatureCreateTime), Long.valueOf(resultData.signatureExpireTime), resultData.signature}));
        Log.i(TAG, "App Token ready for SPC validation (base64 encoded): " + resultData.token);
        String plainTextAppToken = new String(Base64.decode(resultData.token, 2));
        Log.i(TAG, "App Token (plain text): " + plainTextAppToken);
        if (resultData.subscriptionId != -1) {
            Log.i(TAG, "Subscription Id: " + resultData.subscriptionId);
        }
    }

    /* renamed from: com.verizon.loginclient.IdentityLoginClientExample$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode;

        static {
            int[] iArr = new int[IdentityLoginClient.ResultCode.values().length];
            $SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode = iArr;
            try {
                iArr[IdentityLoginClient.ResultCode.deviceNotCapable.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode[IdentityLoginClient.ResultCode.rogueEngineInstalled.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode[IdentityLoginClient.ResultCode.engineNotInstalled.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode[IdentityLoginClient.ResultCode.failure.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode[IdentityLoginClient.ResultCode.securityException.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode[IdentityLoginClient.ResultCode.timeout.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    public void onErrorResult(IdentityLoginClient.ResultCode status, Throwable ex) {
        switch (AnonymousClass1.$SwitchMap$com$verizon$loginclient$IdentityLoginClient$ResultCode[status.ordinal()]) {
            case 1:
                Log.e(TAG, "This device does not have expected system features indicating LTE is supported");
                return;
            case 2:
            case 3:
                Log.e(TAG, "Official LoginEngine content provider not found!");
                return;
            case 4:
                Log.w(TAG, "Token fetch failed!  (somewhat unusual - success or timeout are most common). Exception may be null", ex);
                return;
            case 5:
                Log.e(TAG, "Login Client threw SecurityException, usually b/c your app failed authorization", ex);
                return;
            case 6:
                Log.w(TAG, "Timed out waiting for content observer after initial null token result");
                return;
            default:
                return;
        }
    }
}
