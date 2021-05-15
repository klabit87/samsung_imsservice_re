package com.sec.internal.ims.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorImpl extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnImpl";

    public RegistrationGovernorImpl(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    public Set<String> filterService(Set<String> services, int network) {
        if (!DeviceUtil.getGcfMode().booleanValue() || this.mMno != Mno.GCF || SemSystemProperties.getInt(ImsConstants.SystemProperties.IMS_TEST_MODE_PROP, 0) != 1) {
            return super.filterService(services, network);
        }
        Log.i(LOG_TAG, "by GCF(VZW) IMS_TEST_MODE_PROP - remove all service");
        return new HashSet();
    }

    private boolean checkGcfStatus(int rat) {
        if (this.mTask.getProfile().getPdnType() != 11 || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            return true;
        }
        List<String> gcfIntialRegistrationRat = getGcfInitialRegistrationRat();
        Log.i(LOG_TAG, "gcfIntialRegistrationRat = " + gcfIntialRegistrationRat);
        boolean mathRat = false;
        Iterator<String> it = gcfIntialRegistrationRat.iterator();
        while (true) {
            if (it.hasNext()) {
                if (ImsProfile.getNetworkType(it.next()) == rat) {
                    mathRat = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (mathRat) {
            Log.i(LOG_TAG, "GCF, Initial Rat condition is matched");
            return true;
        }
        Log.i(LOG_TAG, "GCF, Initial Rat condition is not matched");
        return false;
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    private List<String> getGcfInitialRegistrationRat() {
        Cursor cr;
        String gcfRat = "";
        try {
            cr = this.mContext.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/gcfinitrat"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cr != null) {
                if (cr.moveToFirst()) {
                    gcfRat = cr.getString(cr.getColumnIndex("rat"));
                }
            }
            if (cr != null) {
                cr.close();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to get getGcfInitialRegistrationRat");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return Arrays.asList(TextUtils.split(gcfRat, ","));
        throw th;
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(int retryAfter) {
        Log.e(LOG_TAG, "onRegistrationError: Permanently prohibited.");
        this.mIsPermanentStopped = true;
    }

    public boolean isReadyToRegister(int rat) {
        return super.isReadyToRegister(rat) && checkGcfStatus(rat);
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        if (foundBestRat != 0 || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "determineDeRegistration: no IMS service for network " + currentRat + ". Deregister.");
        RegisterTask registerTask = this.mTask;
        registerTask.setReason("no IMS service for network : " + currentRat);
        this.mTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(this.mTask, false, true);
        return true;
    }

    public RegisterTask onManualDeregister(boolean isExplicit) {
        if ((!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.EMERGENCY) && (this.mTask.getState() != RegistrationConstants.RegisterTaskState.DEREGISTERING || this.mTask.getUserAgent() != null)) || !this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
            return super.onManualDeregister(isExplicit);
        }
        Log.i(LOG_TAG, "onManualDeregister: gcf enabled. Do nothing..");
        return null;
    }
}
