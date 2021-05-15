package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.ICmcServiceHelper;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistrationGovernorCmc extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnCmc";
    private Map<Integer, Integer> mP2pSdList = new HashMap();
    private int mPermanentErrorCount = 0;

    public RegistrationGovernorCmc(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        setUpsmEventReceiver();
        this.mUpsmEnabled = SystemUtil.checkUltraPowerSavingMode(SemEmergencyManager.getInstance(this.mContext));
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        Log.e(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistrationError : ");
        sb.append(error);
        sb.append(", fail count : ");
        sb.append(this.mFailureCounter);
        eventLog.logAndAdd(sb.toString());
        if (!SipErrorBase.isImsForbiddenError(error)) {
            this.mPermanentErrorCount = 0;
            super.onRegistrationError(error, retryAfter, unsolicit);
        } else if (this.mTask.getProfile().getCmcType() != 4 && this.mTask.getProfile().getCmcType() != 8) {
            handleForbiddenError(retryAfter);
            onCmcRegistrationError();
        }
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1) {
            Log.i("RegiGvnCmc[" + this.mTask.getPhoneId() + "]", "releaseThrottle: flight mode on");
            this.mTask.setDeregiReason(23);
            this.mRegMan.deregister(this.mTask, false, false, "flight mode enabled");
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 7 || releaseCase == 8) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mCurImpu = 0;
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + releaseCase);
        }
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        if (ImsRegistry.getCmcAccountManager().isCmcEnabled()) {
            for (String service : ImsProfile.getVoLteServiceList()) {
                enabledServices.add(service);
            }
        }
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices);
        }
        return filteredServices;
    }

    public void onDeregistrationDone(boolean requested) {
        if (this.mTask.getProfile().getCmcType() != 0) {
            ImsRegistry.getCmcAccountManager().notifyCmcDeviceChanged();
        }
    }

    public void onRegistrationDone() {
        this.mPermanentErrorCount = 0;
    }

    public List<String> addIpv4Addr(List<String> pcscfs, List<String> validPcscfIp, LinkPropertiesWrapper lp) {
        if (validPcscfIp.isEmpty()) {
            Log.i(LOG_TAG, "ipv4");
            for (String pcscf : pcscfs) {
                if (NetworkUtil.isIPv4Address(pcscf)) {
                    validPcscfIp.add(pcscf);
                }
            }
        }
        return validPcscfIp;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        if (rat == 18 || getVoiceTechType(this.mTask.getPhoneId()) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        ICmcAccountManager cmcMgr = ImsRegistry.getCmcAccountManager();
        int lineSlotIdx = cmcMgr.getCurrentLineSlotIndex();
        if (cmcMgr.getProfileUpdatedResult() == ICmcAccountManager.ProfileUpdateResult.FAILED) {
            IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "isReadyToRegister: profile update failed");
            return false;
        } else if (this.mUpsmEnabled) {
            IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "isReadyToRegister: UPMS ON");
            return false;
        } else if (!cmcMgr.isWifiOnly() || rat == 18) {
            if (cmcMgr.isSecondaryDevice()) {
                if (this.mTask.getPhoneId() != SimUtil.getDefaultPhoneId()) {
                    int phoneId = this.mTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: cmc non dds - line slot:" + lineSlotIdx);
                    return false;
                }
            } else if (!cmcMgr.hasSecondaryDevice()) {
                return false;
            } else {
                if (rat == 18) {
                    if (this.mTask.getPhoneId() != lineSlotIdx) {
                        int phoneId2 = this.mTask.getPhoneId();
                        IMSLog.i(LOG_TAG, phoneId2, "isReadyToRegister: wifi : non line slot: " + lineSlotIdx);
                        return false;
                    } else if (TelephonyManagerWrapper.getInstance(this.mContext).getSimState(lineSlotIdx) == 1) {
                        int phoneId3 = this.mTask.getPhoneId();
                        IMSLog.i(LOG_TAG, phoneId3, "isReadyToRegister: wifi : SIM ABSENT at slot: " + lineSlotIdx);
                        return false;
                    }
                } else if (!(this.mTask.getPhoneId() == lineSlotIdx && this.mTask.getPhoneId() == SimUtil.getDefaultPhoneId())) {
                    int phoneId4 = this.mTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId4, "isReadyToRegister: cmc non dds or line slot" + lineSlotIdx);
                    return false;
                }
            }
            return super.isReadyToRegister(rat);
        } else {
            int phoneId5 = this.mTask.getPhoneId();
            IMSLog.i(LOG_TAG, phoneId5, "isReadyToRegister: cmc WiFi preferred rat:" + rat + ",lineslot:" + lineSlotIdx);
            return false;
        }
    }

    public void onRegEventContactUriNotification(List<ImsUri> uris, int isRegi, String contactUriType) {
        int size;
        ICmcServiceHelper csm;
        List<String> hostlist = new ArrayList<>();
        String pcscfIp = getCurrentPcscfIp();
        if (pcscfIp.isEmpty()) {
            IMSLog.e(LOG_TAG, this.mTask.getPhoneId(), "current pcscfIp is empty");
            return;
        }
        int localIpType = NetworkUtil.isIPv6Address(pcscfIp) ? 2 : 1;
        IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "localIpType : " + localIpType);
        StringBuilder strUriList = new StringBuilder();
        for (ImsUri uri : uris) {
            if (uri.getHost() == null) {
                break;
            }
            boolean isIPv6 = NetworkUtil.isIPv6Address(uri.getHost());
            strUriList.append("(IP : ");
            strUriList.append(uri.getHost());
            strUriList.append(", isIPv6 : ");
            strUriList.append(isIPv6);
            strUriList.append(")");
            if ((localIpType == 2 && isIPv6) || (localIpType == 1 && !isIPv6)) {
                hostlist.add(uri.getHost());
            }
        }
        IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "onRegEventContactUriNotification : " + strUriList);
        int cmcType = this.mTask.getProfile().getCmcType();
        int size2 = getP2pListSize(cmcType);
        if (isRegi == 1) {
            size = size2 + 1;
        } else {
            size = size2 - 1;
            if (size < 0) {
                size = 0;
            }
        }
        this.mP2pSdList.put(Integer.valueOf(cmcType), Integer.valueOf(size));
        IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "cmcType: " + cmcType + ", isRegi:" + isRegi + ", size: " + this.mP2pSdList.get(Integer.valueOf(cmcType)));
        int phoneId = this.mTask.getPhoneId();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegEventContactUriNotification ");
        sb.append(hostlist);
        IMSLog.i(LOG_TAG, phoneId, sb.toString());
        ImsRegistry.getCmcAccountManager().setRegiEventNotifyHostInfo(hostlist);
        if (this.mVsm != null && (csm = this.mVsm.getCmcServiceHelper()) != null) {
            csm.startP2pDiscovery(hostlist);
        }
    }

    /* access modifiers changed from: protected */
    public int onUltraPowerSavingModeChanged() {
        int actionType = super.onUltraPowerSavingModeChanged();
        if (actionType == 0) {
            this.mRegMan.deregister(this.mTask, false, false, 0, "UPSM ON. CMC deregister");
        } else if (actionType == -1) {
            this.mRegHandler.sendTryRegister(this.mTask.getPhoneId());
        }
        return -1;
    }

    private void onCmcRegistrationError() {
        boolean z = true;
        this.mPermanentErrorCount++;
        ICmcAccountManager cmcMgr = ImsRegistry.getCmcAccountManager();
        if (this.mPermanentErrorCount >= 2) {
            z = false;
        }
        cmcMgr.startSAService(z);
    }

    public int getP2pListSize(int cmcType) {
        int size = 0;
        Iterator<Map.Entry<Integer, Integer>> it = this.mP2pSdList.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<Integer, Integer> e = it.next();
            if (e.getKey().intValue() == cmcType) {
                size = e.getValue().intValue();
                break;
            }
        }
        int phoneId = this.mTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "getP2pListSize size: " + size);
        return size;
    }

    public void updatePcscfIpList(List<String> pcscfIpList) {
        if (pcscfIpList == null) {
            Log.e(LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
        } else {
            super.updatePcscfIpList(new ArrayList<>(pcscfIpList));
        }
    }

    public int getFailureType() {
        return 16;
    }
}
