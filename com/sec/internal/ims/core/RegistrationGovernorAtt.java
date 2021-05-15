package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorAtt extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnAtt";
    protected boolean mIsIpmeDisabledBySipForbidden = false;

    public RegistrationGovernorAtt(Context ctx) {
        this.mContext = ctx;
    }

    public RegistrationGovernorAtt(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegiAt = SystemClock.elapsedRealtime() + 1000;
        startRetryTimer(1000);
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            handleNormalResponse(error, retryAfter);
        } else if (SipErrorBase.isImsForbiddenError(error)) {
            handleForbiddenError(retryAfter);
            return;
        } else if (SipErrorBase.USE_PROXY.equals(error)) {
            int usedPcscf = this.mCurPcscfIpIdx;
            Log.i(LOG_TAG, "usedPcscf : " + usedPcscf);
            this.mCurPcscfIpIdx = usedPcscf > 0 ? 0 : 1;
        } else if (!unsolicit) {
            this.mCurPcscfIpIdx++;
        }
        if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx = 0;
            if (retryAfter == 0) {
                retryAfter = getWaitTime();
            }
        }
        if (retryAfter > 0) {
            this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
            startRetryTimer(((long) retryAfter) * 1000);
            return;
        }
        this.mRegHandler.sendTryRegister(this.mPhoneId, 1000);
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        if (this.mIsIpmeDisabledBySipForbidden) {
            this.mIsIpmeDisabledBySipForbidden = false;
            Log.i(LOG_TAG, "onRegistrationDone: reset IPME after forbidden");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01a6, code lost:
        if (r24 != 18) goto L_0x01b0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.Set<java.lang.String> filterService(java.util.Set<java.lang.String> r23, int r24) {
        /*
            r22 = this;
            r0 = r22
            r1 = r23
            java.util.HashSet r2 = new java.util.HashSet
            r2.<init>()
            java.util.HashSet r3 = new java.util.HashSet
            r3.<init>()
            if (r1 == 0) goto L_0x0013
            r3.addAll(r1)
        L_0x0013:
            android.content.Context r4 = r0.mContext
            int r5 = r0.mPhoneId
            java.lang.String r6 = "ims"
            int r4 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r4, (java.lang.String) r6, (int) r5)
            r6 = 1
            if (r4 != r6) goto L_0x0022
            r4 = r6
            goto L_0x0023
        L_0x0022:
            r4 = 0
        L_0x0023:
            java.lang.String r7 = "RegiGvnAtt"
            if (r4 != 0) goto L_0x003f
            int r5 = r0.mPhoneId
            java.lang.String r6 = "filterEnabledCoreService: IMS is disabled."
            com.sec.internal.log.IMSLog.i(r7, r5, r6)
            com.sec.internal.ims.core.RegisterTask r5 = r0.mTask
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r6 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF
            int r6 = r6.getCode()
            r5.setRegiFailReason(r6)
            java.util.HashSet r5 = new java.util.HashSet
            r5.<init>()
            return r5
        L_0x003f:
            android.content.Context r8 = r0.mContext
            int r9 = r0.mPhoneId
            java.lang.String r10 = "volte"
            int r8 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r8, (java.lang.String) r10, (int) r9)
            if (r8 != r6) goto L_0x004e
            r8 = r6
            goto L_0x004f
        L_0x004e:
            r8 = 0
        L_0x004f:
            android.content.Context r9 = r0.mContext
            int r10 = r0.mPhoneId
            java.lang.String r11 = "rcs"
            int r9 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r9, (java.lang.String) r11, (int) r10)
            if (r9 != r6) goto L_0x005e
            r9 = r6
            goto L_0x005f
        L_0x005e:
            r9 = 0
        L_0x005f:
            android.content.Context r10 = r0.mContext
            int r11 = r0.mPhoneId
            r12 = -1
            int r10 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getRcsUserSetting(r10, r12, r11)
            if (r10 != r6) goto L_0x006c
            r10 = r6
            goto L_0x006d
        L_0x006c:
            r10 = 0
        L_0x006d:
            android.content.Context r11 = r0.mContext
            boolean r11 = com.sec.internal.helper.NetworkUtil.isMobileDataOn(r11)
            com.sec.internal.helper.os.ITelephonyManager r13 = r0.mTelephonyManager
            boolean r13 = r13.isNetworkRoaming()
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r14 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.DATA_ROAMING
            android.content.Context r15 = r0.mContext
            int r5 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN
            int r5 = r14.get(r15, r5)
            int r14 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.ROAMING_DATA_ENABLED
            if (r5 != r14) goto L_0x0089
            r5 = r6
            goto L_0x008a
        L_0x0089:
            r5 = 0
        L_0x008a:
            android.content.Context r14 = r0.mContext
            int r15 = r0.mPhoneId
            int r14 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVoiceCallType(r14, r12, r15)
            if (r14 != 0) goto L_0x0096
            r14 = r6
            goto L_0x0097
        L_0x0096:
            r14 = 0
        L_0x0097:
            android.content.Context r15 = r0.mContext
            int r6 = r0.mPhoneId
            int r6 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVideoCallType(r15, r12, r6)
            if (r6 != 0) goto L_0x00a3
            r6 = 1
            goto L_0x00a4
        L_0x00a3:
            r6 = 0
        L_0x00a4:
            android.content.Context r12 = r0.mContext
            int r15 = r0.mPhoneId
            java.lang.String r1 = "defaultmsgappinuse"
            int r1 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r12, (java.lang.String) r1, (int) r15)
            r12 = 1
            if (r1 != r12) goto L_0x00b3
            r12 = 1
            goto L_0x00b4
        L_0x00b3:
            r12 = 0
        L_0x00b4:
            r1 = r12
            int r12 = r0.mPhoneId
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r16 = r4
            java.lang.String r4 = "filterService:  IPME setting="
            r15.append(r4)
            r15.append(r10)
            java.lang.String r4 = " Video setting="
            r15.append(r4)
            r15.append(r6)
            java.lang.String r4 = " Enhanced Data="
            r15.append(r4)
            r15.append(r14)
            java.lang.String r4 = " Mobile Data="
            r15.append(r4)
            r15.append(r11)
            java.lang.String r4 = " isRoaming="
            r15.append(r4)
            r15.append(r13)
            java.lang.String r4 = " Roaming Data="
            r15.append(r4)
            r15.append(r5)
            java.lang.String r4 = " SIP Forbidden="
            r15.append(r4)
            boolean r4 = r0.mIsIpmeDisabledBySipForbidden
            r15.append(r4)
            java.lang.String r4 = " Default Msg App="
            r15.append(r4)
            r15.append(r1)
            java.lang.String r4 = " RCSonly="
            r15.append(r4)
            com.sec.internal.ims.core.RegisterTask r4 = r0.mTask
            boolean r4 = r4.isRcsOnly()
            r15.append(r4)
            java.lang.String r4 = r15.toString()
            com.sec.internal.log.IMSLog.i(r7, r12, r4)
            java.lang.String r4 = "mmtel"
            java.lang.String r7 = "mmtel-video"
            if (r14 == 0) goto L_0x01f9
            r2.add(r4)
            java.lang.String r12 = "smsip"
            r2.add(r12)
            if (r13 == 0) goto L_0x0128
            if (r5 != 0) goto L_0x012c
        L_0x0128:
            if (r13 != 0) goto L_0x0138
            if (r11 == 0) goto L_0x0138
        L_0x012c:
            if (r6 == 0) goto L_0x0132
            r2.add(r7)
            goto L_0x013d
        L_0x0132:
            java.lang.String r12 = "VideoSetting off"
            r0.removeService(r3, r7, r12)
            goto L_0x013d
        L_0x0138:
            java.lang.String r12 = "MobileData unavailable"
            r0.removeService(r3, r7, r12)
        L_0x013d:
            com.sec.internal.interfaces.ims.config.IConfigModule r12 = r0.mConfigModule
            int r15 = r0.mPhoneId
            boolean r12 = r12.isValidAcsVersion(r15)
            if (r12 == 0) goto L_0x01ee
            if (r9 == 0) goto L_0x01ee
            java.lang.String[] r12 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            java.util.Set r12 = r0.servicesByImsSwitch(r12)
            r2.addAll(r12)
            android.content.Context r12 = r0.mContext
            int r15 = r0.mPhoneId
            r17 = r5
            android.content.Context r5 = r0.mContext
            r18 = r6
            int r6 = r0.mPhoneId
            r19 = r9
            com.sec.internal.ims.core.RegisterTask r9 = r0.mTask
            com.sec.ims.settings.ImsProfile r9 = r9.getProfile()
            java.lang.String r5 = com.sec.internal.ims.util.ConfigUtil.getRcsProfileWithFeature(r5, r6, r9)
            java.util.List r5 = com.sec.internal.ims.config.RcsConfigurationHelper.getRcsEnabledServiceList(r12, r15, r5)
            java.lang.String[] r6 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            java.util.stream.Stream r6 = java.util.Arrays.stream(r6)
            com.sec.internal.ims.core.-$$Lambda$RegistrationGovernorAtt$zvgOZZYmDFRbpINJQW-Lt4wKZ84 r9 = new com.sec.internal.ims.core.-$$Lambda$RegistrationGovernorAtt$zvgOZZYmDFRbpINJQW-Lt4wKZ84
            r9.<init>(r5)
            java.util.stream.Stream r6 = r6.filter(r9)
            com.sec.internal.ims.core.-$$Lambda$RegistrationGovernorAtt$hJT3YNC7NQuFY28O2qV6Bajjw3E r9 = new com.sec.internal.ims.core.-$$Lambda$RegistrationGovernorAtt$hJT3YNC7NQuFY28O2qV6Bajjw3E
            r9.<init>(r2)
            r6.forEach(r9)
            java.lang.String r6 = "im"
            boolean r6 = r2.contains(r6)
            if (r6 != 0) goto L_0x0198
            java.lang.String r6 = "chatbot-communication"
            java.lang.String r9 = "CHAT disabled in autoconfig"
            r0.removeService(r2, r6, r9)
        L_0x0198:
            if (r10 == 0) goto L_0x01ae
            boolean r6 = r0.mIsIpmeDisabledBySipForbidden
            if (r6 != 0) goto L_0x01ae
            if (r1 == 0) goto L_0x01ae
            if (r13 == 0) goto L_0x01a9
            r6 = 18
            r9 = r24
            if (r9 == r6) goto L_0x01ab
            goto L_0x01b0
        L_0x01a9:
            r9 = r24
        L_0x01ab:
            r20 = r1
            goto L_0x01ed
        L_0x01ae:
            r9 = r24
        L_0x01b0:
            com.sec.internal.ims.core.RegisterTask r6 = r0.mTask
            boolean r6 = r6.isRcsOnly()
            if (r6 == 0) goto L_0x01d7
            java.lang.String[] r6 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            int r12 = r6.length
            r15 = 0
        L_0x01be:
            if (r15 >= r12) goto L_0x01d2
            r20 = r1
            r1 = r6[r15]
            r21 = r5
            java.lang.String r5 = "RCS service off"
            r0.removeService(r3, r1, r5)
            int r15 = r15 + 1
            r1 = r20
            r5 = r21
            goto L_0x01be
        L_0x01d2:
            r20 = r1
            r21 = r5
            goto L_0x01ed
        L_0x01d7:
            r20 = r1
            r21 = r5
            java.lang.String[] r1 = com.sec.ims.settings.ImsProfile.getChatServiceList()
            int r5 = r1.length
            r6 = 0
        L_0x01e1:
            if (r6 >= r5) goto L_0x01ed
            r12 = r1[r6]
            java.lang.String r15 = "IMPE service off"
            r0.removeService(r3, r12, r15)
            int r6 = r6 + 1
            goto L_0x01e1
        L_0x01ed:
            goto L_0x0211
        L_0x01ee:
            r20 = r1
            r17 = r5
            r18 = r6
            r19 = r9
            r9 = r24
            goto L_0x0211
        L_0x01f9:
            r20 = r1
            r17 = r5
            r18 = r6
            r19 = r9
            r9 = r24
            r3.clear()
            com.sec.internal.ims.core.RegisterTask r1 = r0.mTask
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r5 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF
            int r5 = r5.getCode()
            r1.setRegiFailReason(r5)
        L_0x0211:
            if (r8 != 0) goto L_0x0236
            java.lang.String[] r1 = com.sec.ims.settings.ImsProfile.getVoLteServiceList()
            int r5 = r1.length
            r6 = 0
        L_0x0219:
            if (r6 >= r5) goto L_0x0246
            r7 = r1[r6]
            java.lang.String r12 = "VoLTE disabled"
            r0.removeService(r3, r7, r12)
            boolean r12 = r7.equalsIgnoreCase(r4)
            if (r12 == 0) goto L_0x0233
            com.sec.internal.ims.core.RegisterTask r12 = r0.mTask
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r15 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF
            int r15 = r15.getCode()
            r12.setRegiFailReason(r15)
        L_0x0233:
            int r6 = r6 + 1
            goto L_0x0219
        L_0x0236:
            android.content.Context r1 = r0.mContext
            int r4 = r0.mPhoneId
            int r1 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r1, (java.lang.String) r7, (int) r4)
            r4 = 1
            if (r1 == r4) goto L_0x0246
            java.lang.String r1 = "ViLTE disabled"
            r0.removeService(r3, r7, r1)
        L_0x0246:
            com.sec.internal.interfaces.ims.config.IConfigModule r1 = r0.mConfigModule
            int r4 = r0.mPhoneId
            boolean r1 = r1.isValidAcsVersion(r4)
            if (r1 != 0) goto L_0x0262
            java.lang.String[] r1 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            int r4 = r1.length
            r5 = 0
        L_0x0256:
            if (r5 >= r4) goto L_0x0262
            r6 = r1[r5]
            java.lang.String r7 = "Invalid autoconf ver"
            r0.removeService(r3, r6, r7)
            int r5 = r5 + 1
            goto L_0x0256
        L_0x0262:
            android.content.Context r1 = r0.mContext
            int r4 = r0.mPhoneId
            boolean r1 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isRegAllowed(r1, r4)
            if (r1 != 0) goto L_0x027e
            java.lang.String[] r1 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            int r4 = r1.length
            r5 = 0
        L_0x0272:
            if (r5 >= r4) goto L_0x027e
            r6 = r1[r5]
            java.lang.String r7 = "No DualRCS"
            r0.removeService(r3, r6, r7)
            int r5 = r5 + 1
            goto L_0x0272
        L_0x027e:
            r3.retainAll(r2)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorAtt.filterService(java.util.Set, int):java.util.Set");
    }

    static /* synthetic */ boolean lambda$filterService$0(List enabledRcsSvcsByAcs, String rcsSvc) {
        return !enabledRcsSvcsByAcs.contains(rcsSvc);
    }

    public /* synthetic */ void lambda$filterService$1$RegistrationGovernorAtt(Set enabledServices, String disabledSvc) {
        removeService(enabledServices, disabledSvc, "Disable from ACS.");
    }

    public SipError onSipError(String service, SipError error) {
        Log.i(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if ("mmtel".equals(service)) {
            if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) || SipErrorBase.SIP_TIMEOUT.equals(error) || SipErrorBase.FORBIDDEN.equals(error) || SipErrorBase.SERVER_TIMEOUT.equals(error)) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, this.mIsValid, "Sip Error[MMTEL]. DeRegister..");
            }
        } else if (("im".equals(service) || "ft".equals(service)) && SipErrorBase.FORBIDDEN_SERVICE_NOT_AUTHORISED.equals(error)) {
            if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1) {
                Log.i(LOG_TAG, "onSipError: [IPME] try re-register after forbidden");
                this.mIsIpmeDisabledBySipForbidden = true;
            }
            this.mRegMan.updateChatService(this.mPhoneId);
        }
        return error;
    }

    public boolean allowRoaming() {
        return this.mTask.getProfile().isAllowedOnRoaming() && getVoiceTechType() == 0;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        int regiRat = this.mTask.getRegistrationRat();
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        if (regiRat == 1 || regiRat == 16 || regiRat == 2 || SlotBasedConfig.getInstance(this.mPhoneId).getTTYMode()) {
            return false;
        }
        return true;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkCallStatus() && checkRegiStatus());
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 0) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 1) {
            this.mIsPermanentStopped = false;
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            stopRetryTimer();
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + releaseCase);
        }
    }

    public void updatePcscfIpList(List<String> pcscfIpList) {
        if (pcscfIpList == null) {
            Log.e(LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
            return;
        }
        this.mNumOfPcscfIp = pcscfIpList.size();
        this.mPcscfIpList = pcscfIpList;
        boolean z = false;
        this.mCurPcscfIpIdx = 0;
        if (this.mNumOfPcscfIp > 0) {
            z = true;
        }
        this.mIsValid = z;
    }

    public boolean isLocationInfoLoaded(int rat) {
        return true;
    }
}
