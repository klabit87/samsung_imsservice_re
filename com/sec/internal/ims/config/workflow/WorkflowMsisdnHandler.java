package com.sec.internal.ims.config.workflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class WorkflowMsisdnHandler {
    protected static final String IS_NEEDED = "isNeeded";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowMsisdnHandler.class.getSimpleName();
    protected static final String MSISDN_KEYS_ARRAY = "msisdnArray";
    protected static final long MSISDN_MAX_TIMEOUT = 604800;
    protected static final String MSISDN_VALUE = "msisdnValue";
    protected static final String SET_SHOW_MSISDN_DIALOG = "com.sec.rcs.config.action.SET_SHOW_MSISDN_DIALOG";
    protected Set<String> mMsisdnKeys;
    protected WorkflowBase mWorkflowBase;

    public WorkflowMsisdnHandler(WorkflowBase base) {
        this.mWorkflowBase = base;
    }

    /* access modifiers changed from: protected */
    public int getMsisdnSkipCount() {
        Log.i(LOG_TAG, "getMsisdnSkipCount");
        int result = 0;
        if (!TextUtils.isEmpty(this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.MSISDN_SKIP_COUNT))) {
            try {
                result = Integer.parseInt(this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.MSISDN_SKIP_COUNT));
            } catch (NullPointerException | NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            result = -1;
        }
        String str = LOG_TAG;
        Log.i(str, "getMsisdnSkipCount :" + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public void setMsisdnSkipCount(int count) {
        Log.i(LOG_TAG, "setMsisdnSkipValue");
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.MSISDN_SKIP_COUNT, String.valueOf(count));
    }

    /* access modifiers changed from: protected */
    public void setMsisdnMsguiDisplay(String value) {
        Log.i(LOG_TAG, "setMsisdnMsguiDisplay");
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.MSISDN_MSGUI_DISPLAY, value);
    }

    /* access modifiers changed from: protected */
    public String getLastMsisdnValue() {
        SharedPreferences sharedPreferences = this.mWorkflowBase.mSharedPreferences;
        String lastMsisdn = sharedPreferences.getString(MSISDN_VALUE + this.mWorkflowBase.mTelephony.getImsi(), (String) null);
        String str = LOG_TAG;
        Log.i(str, "getLastMsisdnValue: " + IMSLog.checker(lastMsisdn));
        return lastMsisdn;
    }

    /* access modifiers changed from: protected */
    public void setMsisdnValue(String msisdnValue) {
        String str = LOG_TAG;
        Log.i(str, "setMsisdnValue: " + IMSLog.checker(msisdnValue));
        SharedPreferences.Editor editor = this.mWorkflowBase.mSharedPreferences.edit();
        editor.putString(MSISDN_VALUE + this.mWorkflowBase.mTelephony.getImsi(), msisdnValue);
        Set<String> stringSet = this.mWorkflowBase.mSharedPreferences.getStringSet(MSISDN_KEYS_ARRAY, new HashSet());
        this.mMsisdnKeys = stringSet;
        stringSet.add(MSISDN_VALUE + this.mWorkflowBase.mTelephony.getImsi());
        editor.putStringSet(MSISDN_KEYS_ARRAY, this.mMsisdnKeys);
        editor.apply();
    }

    /* access modifiers changed from: protected */
    public void setMsisdnTimer(CountDownTimer msisdnTimer) {
        long startMsisdnTime = getStartMsisdnTime();
        String str = LOG_TAG;
        Log.i(str, "startMsisdnTime: " + startMsisdnTime);
        if (startMsisdnTime == -1) {
            Log.i(LOG_TAG, "msisdn timer was already called, so skip");
            return;
        }
        Date current = new Date();
        if (startMsisdnTime == 0) {
            startMsisdnTime = current.getTime() + ImsConstants.SimMobilityKitTimer.BASIC_INTERVAL;
            this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.START_MSISDN_TIMER, String.valueOf(startMsisdnTime));
        }
        startMsisdnTimer(msisdnTimer, (int) ((startMsisdnTime - current.getTime()) / 1000));
    }

    /* access modifiers changed from: protected */
    public long getStartMsisdnTime() {
        long result = 0;
        try {
            result = Long.parseLong(this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.START_MSISDN_TIMER));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
        String str = LOG_TAG;
        Log.i(str, "getStartMsisdnTime: " + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public void startMsisdnTimer(CountDownTimer msisdnTimer, int nextMsisdnTime) {
        String str = LOG_TAG;
        Log.i(str, "start msisdnTimer(" + nextMsisdnTime + "sec)");
        cancelMsisdnTimer(msisdnTimer, false);
        final int i = nextMsisdnTime;
        new CountDownTimer(1000 * ((long) nextMsisdnTime), 100 * ((long) nextMsisdnTime)) {
            public void onTick(long millisUntilFinished) {
                String access$000 = WorkflowMsisdnHandler.LOG_TAG;
                Log.i(access$000, "validity tick(" + millisUntilFinished + ")");
            }

            public void onFinish() {
                String access$000 = WorkflowMsisdnHandler.LOG_TAG;
                Log.i(access$000, "msisdnTimer expired(" + i + ").");
                WorkflowMsisdnHandler.this.expiredMsisdnTimer();
            }
        }.start();
    }

    /* access modifiers changed from: protected */
    public void cancelMsisdnTimer(CountDownTimer msisdnTimer, boolean disableDB) {
        Log.i(LOG_TAG, "cancelMsisdnTimer");
        if (msisdnTimer != null) {
            msisdnTimer.cancel();
        }
        if (disableDB) {
            Log.i(LOG_TAG, "cancelMsisdnTimer: disable DB of START_MSISDN_TIMER");
            this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.START_MSISDN_TIMER, "-1");
        }
    }

    /* access modifiers changed from: protected */
    public void expiredMsisdnTimer() {
        boolean userSetting = ConfigUtil.isRcsAvailable(this.mWorkflowBase.mContext, this.mWorkflowBase.mPhoneId, this.mWorkflowBase.mSm);
        String str = LOG_TAG;
        Log.i(str, "expiredMsisdnTimer: userSetting: " + userSetting);
        if (userSetting) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(SET_SHOW_MSISDN_DIALOG);
            sendIntent.putExtra(IS_NEEDED, getIsNeeded());
            ContextExt.sendBroadcastAsUser(this.mWorkflowBase.mContext, sendIntent, ContextExt.ALL);
            this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.MSISDN_MSGUI_DISPLAY, CloudMessageProviderContract.JsonData.TRUE);
        }
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.START_MSISDN_TIMER, "-1");
    }

    /* access modifiers changed from: protected */
    public boolean getIsNeeded() {
        return true;
    }
}
