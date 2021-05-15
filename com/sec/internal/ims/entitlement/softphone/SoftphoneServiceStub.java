package com.sec.internal.ims.entitlement.softphone;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.SparseArray;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.entitlement.util.EncryptionHelper;
import com.sec.internal.ims.entitlement.util.SharedPrefHelper;
import com.sec.internal.log.IndentingPrintWriter;
import com.sec.vsim.attsoftphone.IEmergencyServiceListener;
import com.sec.vsim.attsoftphone.IProgressListener;
import com.sec.vsim.attsoftphone.ISoftphoneService;
import com.sec.vsim.attsoftphone.ISupplementaryServiceListener;
import com.sec.vsim.attsoftphone.data.CallForwardingInfo;
import com.sec.vsim.attsoftphone.data.CallWaitingInfo;
import com.sec.vsim.attsoftphone.data.DeviceInfo;
import java.util.List;
import javax.crypto.SecretKey;

public class SoftphoneServiceStub extends ISoftphoneService.Stub {
    private final String LOG_TAG;
    private IntentFilter mAirplaneModeIntentFilter = null;
    private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int airPlaneModeOn = Settings.Global.getInt(SoftphoneServiceStub.this.mContext.getContentResolver(), "airplane_mode_on", 1);
            SimpleEventLog simpleEventLog = SoftphoneServiceStub.this.mEventLog;
            simpleEventLog.logAndAdd("mAirplaneModeReceiver onChange: " + airPlaneModeOn);
            if (airPlaneModeOn == 1) {
                for (int i = 0; i < SoftphoneServiceStub.this.mClients.size(); i++) {
                    SoftphoneClient client = (SoftphoneClient) SoftphoneServiceStub.this.mClients.valueAt(i);
                    if (client.getUserId() == SoftphoneServiceStub.this.mCurrentUserId) {
                        client.onAirplaneModeOn();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public SparseArray<SoftphoneClient> mClients = new SparseArray<>();
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    public SimpleEventLog mEventLog;
    /* access modifiers changed from: private */
    public boolean mNetworkConnected = false;
    private IntentFilter mNetworkConnectivityFilter = null;
    private BroadcastReceiver mNetworkConnectivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager mgr = (ConnectivityManager) context.getSystemService("connectivity");
            if (mgr != null) {
                NetworkInfo info = mgr.getActiveNetworkInfo();
                SimpleEventLog simpleEventLog = SoftphoneServiceStub.this.mEventLog;
                simpleEventLog.logAndAdd("onReceive CONNECTIVITY_CHANGE Intent with NetworkInfo: " + info);
                if (info == null || !info.isConnected()) {
                    boolean unused = SoftphoneServiceStub.this.mNetworkConnected = false;
                } else if (!SoftphoneServiceStub.this.mNetworkConnected) {
                    boolean unused2 = SoftphoneServiceStub.this.mNetworkConnected = true;
                    SoftphoneServiceStub softphoneServiceStub = SoftphoneServiceStub.this;
                    softphoneServiceStub.validateTokens(softphoneServiceStub.mCurrentUserId);
                    for (int i = 0; i < SoftphoneServiceStub.this.mClients.size(); i++) {
                        SoftphoneClient client = (SoftphoneClient) SoftphoneServiceStub.this.mClients.valueAt(i);
                        if (client.getUserId() == SoftphoneServiceStub.this.mCurrentUserId) {
                            client.onNetworkConnected();
                        }
                    }
                }
            }
        }
    };

    public SoftphoneServiceStub(Context context) {
        this.mContext = context;
        this.mCurrentUserId = Extensions.ActivityManager.getCurrentUser();
        this.LOG_TAG = SoftphoneServiceStub.class.getSimpleName() + '-' + this.mCurrentUserId;
        this.mEventLog = new SimpleEventLog(context, this.LOG_TAG, 100);
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        this.mNetworkConnectivityFilter = intentFilter;
        this.mContext.registerReceiver(this.mNetworkConnectivityReceiver, intentFilter);
        this.mEventLog.logAndAdd("SoftphoneServiceStub(): registering mNetworkConnectivityReceiver");
        IntentFilter intentFilter2 = new IntentFilter(ImsConstants.Intents.ACTION_AIRPLANE_MODE);
        this.mAirplaneModeIntentFilter = intentFilter2;
        this.mContext.registerReceiver(this.mAirplaneModeReceiver, intentFilter2);
        this.mEventLog.logAndAdd("SoftphoneServiceStub(): registering mAirplaneModeReceiver");
        reloadAccounts();
        clearUnusedAddresses();
        IntentFilter userSwitchIntentfilter = new IntentFilter();
        userSwitchIntentfilter.addAction(Extensions.Intent.ACTION_USER_SWITCHED);
        this.mContext.registerReceiver(new UserSwitchReceiver(), userSwitchIntentfilter);
    }

    private void reloadAccounts() {
        this.mEventLog.logAndAdd("reloadAccounts()");
        Uri uri = SoftphoneContract.SoftphoneAccount.buildFunctionalAccountUri();
        ContentValues values = new ContentValues();
        values.put("status", 1);
        this.mContext.getContentResolver().update(uri, values, (String) null, (String[]) null);
        Uri uri2 = SoftphoneContract.SoftphoneAccount.buildActiveAccountUri();
        values.clear();
        values.put("status", 0);
        this.mContext.getContentResolver().update(uri2, values, (String) null, (String[]) null);
    }

    private void clearUnusedAddresses() {
        this.mEventLog.logAndAdd("clearUnusedAddresses()");
        this.mContext.getContentResolver().delete(SoftphoneContract.SoftphoneAddress.CONTENT_URI, "account_id is null OR account_id =?", new String[]{""});
    }

    private class UserSwitchReceiver extends BroadcastReceiver {
        private UserSwitchReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (Extensions.Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                int newUserId = Extensions.ActivityManager.getCurrentUser();
                SimpleEventLog simpleEventLog = SoftphoneServiceStub.this.mEventLog;
                simpleEventLog.logAndAdd("UserSwitchReceiver(): newUserId: " + newUserId);
                SoftphoneServiceStub.this.onUserSwitched(newUserId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserSwitched(int newUserId) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onUserSwitched(): newUserId: " + newUserId + ", mCurrentUserId: " + this.mCurrentUserId + ", size: " + this.mClients.size());
        this.mCurrentUserId = newUserId;
        for (int i = 0; i < this.mClients.size(); i++) {
            SoftphoneClient client = this.mClients.valueAt(i);
            if (client.getUserId() != newUserId) {
                client.onUserSwitch();
                client.onUserSwitchedAway();
            } else {
                client.onUserSwitchedBack();
                if (this.mNetworkConnected) {
                    client.onNetworkConnected();
                }
            }
        }
    }

    private void updateAccountStatus(String accountId, int userId, int status) {
        Uri uri = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(accountId, (long) userId);
        ContentValues values = new ContentValues();
        values.put("status", Integer.valueOf(status));
        this.mContext.getContentResolver().update(uri, values, (String) null, (String[]) null);
    }

    /* access modifiers changed from: private */
    public void validateTokens(int newUserId) {
        Throwable th;
        int i = newUserId;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("validateTokens(): newUserId: " + i);
        Cursor cursor = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildPendingAccountUri((long) i), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd("validateTokens found " + cursor.getCount() + " records");
                if (cursor.getCount() > 0) {
                    SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(SoftphoneNamespaces.SoftphoneSharedPref.SHARED_PREF_NAME);
                    EncryptionHelper encryptionHelper = EncryptionHelper.getInstance(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
                    while (cursor.moveToNext()) {
                        String accountId = cursor.getString(cursor.getColumnIndex("account_id"));
                        updateAccountStatus(accountId, i, 0);
                        SecretKey secretKey = EncryptionHelper.getSecretKey(cursor);
                        if (secretKey == null) {
                            SimpleEventLog simpleEventLog3 = this.mEventLog;
                            simpleEventLog3.logAndAdd("Cannot obtain secret key for account: " + accountId);
                            cursor.close();
                            if (cursor != null) {
                                cursor.close();
                                return;
                            }
                            return;
                        }
                        Context context = this.mContext;
                        String encodedTGaurdToken = sharedPrefHelper.get(context, accountId + ":" + i + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_TOKEN);
                        Context context2 = this.mContext;
                        String encodedTGaurdAppId = sharedPrefHelper.get(context2, accountId + ":" + i + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_APPID);
                        Context context3 = this.mContext;
                        String environment = sharedPrefHelper.get(context3, accountId + ":" + i + ":" + "environment");
                        SimpleEventLog simpleEventLog4 = this.mEventLog;
                        StringBuilder sb = new StringBuilder();
                        sb.append("encodedTGaurdToken ");
                        sb.append(encodedTGaurdToken);
                        simpleEventLog4.logAndAdd(sb.toString());
                        String decodedTGaurdToken = encryptionHelper.decrypt(encodedTGaurdToken, secretKey);
                        String decodedTGaurdAppId = encryptionHelper.decrypt(encodedTGaurdAppId, secretKey);
                        SimpleEventLog simpleEventLog5 = this.mEventLog;
                        simpleEventLog5.logAndAdd("decodedTGaurdToken: " + decodedTGaurdToken + ", decodedTGaurdAppId: " + decodedTGaurdAppId);
                        if (decodedTGaurdToken == null || decodedTGaurdAppId == null) {
                            String str = decodedTGaurdToken;
                            String str2 = encodedTGaurdAppId;
                        } else {
                            int clientId = getClientId(accountId);
                            int i2 = clientId;
                            String str3 = decodedTGaurdToken;
                            String str4 = encodedTGaurdAppId;
                            getClient(clientId).restoreAccessToken(decodedTGaurdToken, accountId, true, decodedTGaurdAppId, Integer.parseInt(environment));
                        }
                    }
                }
                cursor.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cursor != null) {
            cursor.close();
            return;
        }
        return;
        throw th;
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("Dump of " + this.LOG_TAG);
        pw.increaseIndent();
        this.mEventLog.dump(pw);
        pw.decreaseIndent();
        for (int i = 0; i < this.mClients.size(); i++) {
            this.mClients.valueAt(i).dump(pw);
        }
        pw.close();
    }

    private synchronized SoftphoneClient getClient(int clientId) {
        SoftphoneClient client;
        client = this.mClients.get(clientId);
        if (client == null) {
            throw new RuntimeException("client " + clientId + " cannot be found");
        }
        return client;
    }

    public synchronized int getClientId(String accountId) {
        int clientId;
        String userAccountId = accountId + "-" + this.mCurrentUserId;
        clientId = userAccountId.hashCode();
        if (this.mClients.get(clientId) == null) {
            HandlerThread ht = new HandlerThread("SoftphoneClient-" + userAccountId);
            ht.start();
            this.mClients.put(clientId, new SoftphoneClient(accountId, this.mContext, ht.getLooper()));
            this.mEventLog.logAndAdd("getClientId(): create new client SoftphoneClient-" + userAccountId);
        }
        return clientId;
    }

    public void registerProgressListener(int clientId, IProgressListener listener) {
        getClient(clientId).registerProgressListener(getCallingUid(), listener);
    }

    public void deregisterProgressListener(int clientId, IProgressListener listener) {
        getClient(clientId).deregisterProgressListener(getCallingUid());
    }

    public void exchangeForAccessToken(int clientId, String authorizationCode, String accountId, String tGuardAppId, int environment) {
        getClient(clientId).exchangeForAccessToken(authorizationCode, accountId, false, tGuardAppId, environment);
    }

    public void provisionAccount(int clientId) {
        getClient(clientId).provisionAccount();
    }

    public void validateE911Address(int clientId, int addressId, boolean confirmed, IEmergencyServiceListener listener) {
        getClient(clientId).validateE911Address(addressId, confirmed, listener);
    }

    public void tryRegister(int clientId) {
        getClient(clientId).tryRegister();
    }

    public void tryDeregister(int clientId) {
        getClient(clientId).tryDeregister();
    }

    public void logOut(int clientId) {
        getClient(clientId).logOut();
    }

    public void registerSupplementaryServiceListener(int clientId, ISupplementaryServiceListener listener) {
        getClient(clientId).registerSupplementaryServiceListener(getCallingUid(), listener);
    }

    public void deregisterSupplementaryServiceListener(int clientId, ISupplementaryServiceListener listener) {
        getClient(clientId).deregisterSupplementaryServiceListener(getCallingUid());
    }

    public void getCallWaitingInfo(int clientId) {
        getClient(clientId).getCallWaitingInfo();
    }

    public void getCallForwardingInfo(int clientId) {
        getClient(clientId).getCallForwardingInfo();
    }

    public void setCallWaitingInfo(int clientId, CallWaitingInfo info) {
        getClient(clientId).setCallWaitingInfo(info);
    }

    public void setCallForwardingInfo(int clientId, CallForwardingInfo info) {
        getClient(clientId).setCallForwardingInfo(info);
    }

    public void getTermsConditions(int clientId) {
        getClient(clientId).getTermsAndConditions();
    }

    public List<DeviceInfo> getDeviceList(int clientId) {
        return getClient(clientId).getDeviceList();
    }
}
