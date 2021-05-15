package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorRcsJibe extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnRcs";

    public RegistrationGovernorRcsJibe(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices = super.filterService(services, network);
        if (network == 18 || NetworkUtil.isMobileDataOn(this.mContext)) {
            return filteredServices;
        }
        Log.i(LOG_TAG, "filterService: Mobile data OFF!");
        return new HashSet();
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> pcscfs, List<String> validPcscfIp, LinkPropertiesWrapper lp) {
        boolean hasipv4address = false;
        Iterator<String> it = pcscfs.iterator();
        while (true) {
            if (it.hasNext()) {
                if (NetworkUtil.isIPv4Address(it.next())) {
                    hasipv4address = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (hasipv4address) {
            Log.i(LOG_TAG, "ipv4");
            if (this.mTask.isRcsOnly()) {
                Log.i(LOG_TAG, "Don't use ipv6 addr for RCS");
                validPcscfIp.clear();
            }
            if (validPcscfIp.isEmpty()) {
                for (String pcscf : pcscfs) {
                    if (NetworkUtil.isIPv4Address(pcscf)) {
                        validPcscfIp.add(pcscf);
                    }
                }
            }
        } else if (this.mTask.isRcsOnly()) {
            Log.i(LOG_TAG, "Ipv4 pcscf addr isn't exist for RCS");
            validPcscfIp.clear();
        }
        return validPcscfIp;
    }

    public SipError onSipError(String service, SipError error) {
        Log.i(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if (("im".equals(service) || "ft".equals(service)) && SipErrorBase.FORBIDDEN.equals(error)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, this.mIsValid, "SIP ERROR[IM] : FORBIDDEN. DeRegister..");
        }
        return error;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mRegMan.getTelephonyCallStatus(this.mTask.getPhoneId()) != 0) {
            if (this.mMno.isOneOf(Mno.USCC, Mno.VZW, Mno.SPRINT) && this.mVsm != null && this.mVsm.hasCsCall(this.mTask.getPhoneId())) {
                Log.i(LOG_TAG, "isReadyToRegister: TelephonyCallStatus is not idle (CS call)");
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int rat) {
        if ((ImsRegistry.getInt(this.mTask.getPhoneId(), GlobalSettingsConstants.RCS.PRE_CONSENT, 0) == 1 || (!this.mMno.isEur() && !this.mMno.isSea() && !this.mMno.isOce() && !this.mMno.isMea() && !this.mMno.isSwa())) && RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("version", this.mTask.getPhoneId()), -1).intValue() <= 0 && ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mTask.getPhoneId()) == -1) {
            Log.i(LOG_TAG, "isReadyToRegister: User don't try RCS service yet");
            return false;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mTask.getPhoneId()) == 1) {
            return true;
        } else {
            Log.i(LOG_TAG, "isReadyToRegister: Default MSG app isn't used for RCS");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int rat) {
        if (rat == 18 || !this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).isDataRoaming || allowRoaming()) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: IMS roaming is not allowed.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkRegiStatus() && checkRoamingStatus(rat) && checkCallStatus() && checkRcsEvent(rat);
    }

    public void onTelephonyCallStatusChanged(int callState) {
        ImsRegistration reg = this.mTask.getImsRegistration();
        Log.i(LOG_TAG, "onTelephonyCallStatusChanged: callState = " + callState);
        if (this.mVsm != null && this.mVsm.hasCsCall(this.mTask.getPhoneId()) && reg != null && this.mTask.getRegistrationRat() != 18) {
            if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
                if (!this.mMno.isOneOf(Mno.USCC, Mno.VZW, Mno.SPRINT)) {
                    Log.i(LOG_TAG, "CS call. Don't Trigger deregister for Google RCS");
                    return;
                }
                Log.i(LOG_TAG, "CS call. Trigger deregister for RCS");
                this.mTask.setDeregiReason(7);
                this.mRegMan.deregister(this.mTask, false, true, 0, "CS call. Trigger deregister for RCS");
            }
        }
    }
}
