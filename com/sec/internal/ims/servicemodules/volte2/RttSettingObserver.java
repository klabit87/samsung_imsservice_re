package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.sec.internal.log.IMSLog;

public class RttSettingObserver {
    /* access modifiers changed from: private */
    public static String LOG_TAG;
    private static String NAME;
    /* access modifiers changed from: private */
    public static String rttSettingDb = "preferred_rtt_mode";
    /* access modifiers changed from: private */
    public static String rttSettingDb1 = "preferred_rtt_mode1";
    /* access modifiers changed from: private */
    public Context mContext;
    private ContentObserver mRttSettingObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int rttModeSetting = Settings.Secure.getInt(RttSettingObserver.this.mContext.getContentResolver(), RttSettingObserver.rttSettingDb, 0);
            String access$200 = RttSettingObserver.LOG_TAG;
            IMSLog.i(access$200, "RttSettingObserver onChange: " + rttModeSetting);
            RttSettingObserver.this.mVsm.setRttMode(0, rttModeSetting);
        }
    };
    private ContentObserver mRttSettingObserver1 = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int rttModeSetting1 = Settings.Secure.getInt(RttSettingObserver.this.mContext.getContentResolver(), RttSettingObserver.rttSettingDb1, 0);
            String access$200 = RttSettingObserver.LOG_TAG;
            IMSLog.i(access$200, "RttSettingObserver1 onChange: " + rttModeSetting1);
            RttSettingObserver.this.mVsm.setRttMode(1, rttModeSetting1);
        }
    };
    /* access modifiers changed from: private */
    public IVolteServiceModuleInternal mVsm = null;

    static {
        String simpleName = VolteServiceModule.class.getSimpleName();
        NAME = simpleName;
        LOG_TAG = simpleName;
    }

    RttSettingObserver(Context context, IVolteServiceModuleInternal vsm) {
        this.mContext = context;
        this.mVsm = vsm;
    }

    /* access modifiers changed from: protected */
    public void init() {
        Context context = this.mContext;
        if (context != null) {
            registerRttSettingObserver(context);
        }
    }

    private void registerRttSettingObserver(Context context) {
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(rttSettingDb), false, this.mRttSettingObserver);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(rttSettingDb1), false, this.mRttSettingObserver1);
    }
}
