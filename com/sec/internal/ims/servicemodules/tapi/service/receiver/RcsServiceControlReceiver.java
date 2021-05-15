package com.sec.internal.ims.servicemodules.tapi.service.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import com.gsma.services.rcs.CommonServiceConfiguration;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager;
import com.sec.internal.ims.util.RcsSettingsUtils;

public class RcsServiceControlReceiver extends BroadcastReceiver {
    private static final String[] PROJECTION = {ImsConstants.Intents.EXTRA_UPDATED_VALUE};
    private static final String WHERE_CLAUSE = ("key" + "=?");
    private static String mServiceActivated = null;
    private final String LOG_TAG = RcsServiceControlReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        Bundle results;
        String str = this.LOG_TAG;
        Log.d(str, "RcsServiceControlReceiver.onReceive() intent: " + intent);
        if ("com.gsma.services.rcs.action.GET_ACTIVATION_MODE_CHANGEABLE".equals(intent.getAction())) {
            Bundle results2 = getResultExtras(true);
            if (results2 != null) {
                results2.putBoolean("get_activation_mode_changeable", Boolean.parseBoolean(getStringValueSetting(context, "ModeChangeable")));
                setResultExtras(results2);
            }
        } else if ("com.gsma.services.rcs.action.GET_ACTIVATION_MODE".equals(intent.getAction())) {
            Bundle results3 = getResultExtras(true);
            if (results3 != null) {
                if (RcsSettingsUtils.getInstance() != null) {
                    RcsSettingsUtils.getInstance().updateTapiSettings();
                }
                if (mServiceActivated == null) {
                    Log.d(this.LOG_TAG, "mServiceActivated is null");
                    mServiceActivated = getStringValueSetting(context, "ServiceActivated");
                }
                boolean value = Boolean.parseBoolean(mServiceActivated);
                String str2 = this.LOG_TAG;
                Log.d(str2, "ACTION_GET_ACTIVATION_MODE result value " + value);
                results3.putBoolean("get_activation_mode", value);
                setResultExtras(results3);
            }
        } else if ("com.gsma.services.rcs.action.SET_ACTIVATION_MODE".equals(intent.getAction())) {
            if (Boolean.parseBoolean(getStringValueSetting(context, "ModeChangeable"))) {
                boolean active = intent.getBooleanExtra("set_activation_mode", true);
                ContentResolver cr = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(ImsConstants.Intents.EXTRA_UPDATED_VALUE, Boolean.toString(active));
                cr.update(CommonServiceConfiguration.Settings.CONTENT_URI, values, "ServiceActivated", new String[]{"ServiceActivated"});
            }
        } else if ("com.gsma.services.rcs.action.GET_SERVICE_STARTING_STATE".equals(intent.getAction()) && (results = getResultExtras(true)) != null) {
            boolean value2 = TapiServiceManager.isSupportTapi();
            results.putBoolean("get_service_starting_state", value2);
            String str3 = this.LOG_TAG;
            Log.d(str3, "EXTRA_GET_SERVICE_STARTING_STATE" + value2);
            setResultExtras(results);
        }
    }

    private String getStringValueSetting(Context ctx, String key) {
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor c = contentResolver.query(CommonServiceConfiguration.Settings.CONTENT_URI, PROJECTION, WHERE_CLAUSE, new String[]{key}, (String) null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String string = c.getString(0);
                    if (c != null) {
                        c.close();
                    }
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (c == null) {
            return null;
        }
        c.close();
        return null;
        throw th;
    }
}
