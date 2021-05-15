package com.verizon.loginclient;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.util.Base64;
import android.util.Log;
import com.verizon.loginclient.TokenLoginClient;

public class TokenLoginClientExample {
    private static final String TAG = "TokenLoginClientExample";
    TokenLoginClient.ILoginClientReceiver mClientReceiver;
    private final TokenLoginClient mTokenClient;

    public TokenLoginClientExample(Context context) {
        AnonymousClass1 r0 = new TokenLoginClient.ILoginClientReceiver() {
            public void onTokenResult(TokenLoginClient.TokenQueryData result) {
                Log.i(TokenLoginClientExample.TAG, "Token ready for SPC validation (base64 encoded): " + result.token);
                String plainTextToken = new String(Base64.decode(result.token, 2));
                Log.i(TokenLoginClientExample.TAG, "Token (plain text): " + plainTextToken);
                if (result.subscriptionId != -1) {
                    Log.i(TokenLoginClientExample.TAG, "Target SubscriptionId: " + result.subscriptionId);
                }
            }

            public void onErrorResult(TokenLoginClient.ResultCode status, Throwable ex) {
                switch (AnonymousClass2.$SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode[status.ordinal()]) {
                    case 1:
                        Log.e(TokenLoginClientExample.TAG, "This device does not have expected system features indicating LTE is supported");
                        return;
                    case 2:
                    case 3:
                        Log.e(TokenLoginClientExample.TAG, "Official LoginEngine content provider not found!");
                        return;
                    case 4:
                        Log.w(TokenLoginClientExample.TAG, "Token fetch failed!  (somewhat unusual - success or timeout are most common). Exception may be null", ex);
                        return;
                    case 5:
                        Log.e(TokenLoginClientExample.TAG, "Login Client threw SecurityException, usually b/c your app failed authorization", ex);
                        return;
                    case 6:
                        Log.w(TokenLoginClientExample.TAG, "Timed out waiting for content observer after initial null token result");
                        return;
                    default:
                        return;
                }
            }
        };
        this.mClientReceiver = r0;
        this.mTokenClient = new TokenLoginClient(context, r0);
    }

    public void exampleActions() {
        String packageName = this.mTokenClient.getPackageName();
        boolean isDozeWhitelisted = this.mTokenClient.isDozeWhitelisted();
        try {
            this.mTokenClient.invalidateToken();
            this.mTokenClient.invalidateAllTokens();
        } catch (SecurityException e) {
        }
        this.mTokenClient.queryTokenAsync();
        TokenLoginClient.TokenQueryResult queryTokenSynchronous = this.mTokenClient.queryTokenSynchronous();
        this.mTokenClient.queryTokenDirect().getResultCode();
        TokenLoginClient.ResultCode resultCode = TokenLoginClient.ResultCode.waitingOnObserver;
        this.mTokenClient.cancelQuery();
        this.mTokenClient.establishToken();
    }

    public void exampleAdvancedSettings() {
        this.mTokenClient.setTargetSubscriptionId(Integer.valueOf(SubscriptionManager.getDefaultDataSubscriptionId()));
        this.mTokenClient.setTimeout(60000);
        this.mTokenClient.setUseAuthTokens(false);
        this.mTokenClient.setInteractiveQuery(false);
        this.mTokenClient.bypassDeviceFeatureCheck(false);
        this.mTokenClient.bypassEnginePackageCheck(false);
        this.mTokenClient.setObserveOnNullResult(true);
    }

    /* renamed from: com.verizon.loginclient.TokenLoginClientExample$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode;

        static {
            int[] iArr = new int[TokenLoginClient.ResultCode.values().length];
            $SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode = iArr;
            try {
                iArr[TokenLoginClient.ResultCode.deviceNotCapable.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode[TokenLoginClient.ResultCode.rogueEngineInstalled.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode[TokenLoginClient.ResultCode.engineNotInstalled.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode[TokenLoginClient.ResultCode.failure.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode[TokenLoginClient.ResultCode.securityException.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$verizon$loginclient$TokenLoginClient$ResultCode[TokenLoginClient.ResultCode.timeout.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }
}
