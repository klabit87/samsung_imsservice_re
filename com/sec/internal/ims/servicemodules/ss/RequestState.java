package com.sec.internal.ims.servicemodules.ss;

import android.os.Message;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.ims.util.httpclient.GbaHttpController;
import com.sec.internal.log.IMSLog;

public class RequestState extends State {
    private UtStateMachine mUsm;

    public RequestState(UtStateMachine utStateMachine) {
        this.mUsm = utStateMachine;
    }

    public void enter() {
    }

    private void requestPdn() {
        this.mUsm.mPdnRetryCounter = 0;
        this.mUsm.removeMessages(2);
        String ssRouting = this.mUsm.mUtServiceModule.getSetting(this.mUsm.mPhoneId, GlobalSettingsConstants.SS.DOMAIN, "PS");
        if ((DiagnosisConstants.PSCI_KEY_CALL_BEARER.equalsIgnoreCase(ssRouting) || "PS_ONLY_VOLTEREGIED".equalsIgnoreCase(ssRouting)) && !this.mUsm.mUtServiceModule.isVolteServiceRegistered(this.mUsm.mPhoneId)) {
            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "IMS is not registered, UT request must fail ");
            this.mUsm.sendMessage(12, 1013);
        } else if (!this.mUsm.hasConnection()) {
            int result = this.mUsm.mPdnController.startPdnConnectivity(this.mUsm.mPdnType, this.mUsm.mPdnListener, this.mUsm.mPhoneId);
            if (result != 1) {
                String str = UtStateMachine.LOG_TAG;
                int i = this.mUsm.mPhoneId;
                IMSLog.i(str, i, "startPDN fails " + result);
                this.mUsm.sendMessage(12, 1014);
            }
        } else {
            this.mUsm.sendMessage(1);
        }
    }

    private void processGetRequest() {
        GbaHttpController.getInstance().execute(this.mUsm.makeHttpParams());
    }

    private void processPutRequest() {
        this.mUsm.mPrevGetType = -1;
        if (this.mUsm.mProfile.type == 101) {
            if (this.mUsm.mFeature.setAllMediaCF && this.mUsm.mFeature.support_media && this.mUsm.mFeature.isCFSingleElement && UtUtils.convertToMedia(this.mUsm.mProfile.serviceClass) == MEDIA.ALL) {
                IMSLog.i(UtStateMachine.LOG_TAG, "Separated requests for media, send requests for audio and video conditions");
                this.mUsm.mSeparatedMedia = true;
                UtStateMachine utStateMachine = this.mUsm;
                utStateMachine.mMainCondition = utStateMachine.mProfile.condition;
            }
            if (this.mUsm.mFeature.isCFSingleElement && this.mUsm.mProfile.condition == 2 && this.mUsm.mFeature.isNeedSeparateCFNRY && this.mUsm.mProfile.timeSeconds > 0 && (this.mUsm.mProfile.action == 1 || this.mUsm.mProfile.action == 3)) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "SeparatedRequest CFNRY");
                this.mUsm.mSeparatedCFNRY = true;
            } else if (this.mUsm.mProfile.condition == 3 && this.mUsm.mFeature.isNeedSeparateCFNL) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "SeparatedRequest CFNL");
                this.mUsm.mSeparatedCFNL = true;
            } else if (this.mUsm.mFeature.isCFSingleElement && this.mUsm.mFeature.isNeedSeparateCFA && (this.mUsm.mProfile.condition == 4 || this.mUsm.mProfile.condition == 5)) {
                this.mUsm.mProfile.condition = this.mUsm.mProfile.condition == 4 ? 0 : 1;
                this.mUsm.mSeparatedCfAll = true;
                String str = UtStateMachine.LOG_TAG;
                int i = this.mUsm.mPhoneId;
                IMSLog.i(str, i, "SeparatedRequest CF ALL - start from " + this.mUsm.mProfile.condition);
            }
        }
        GbaHttpController.getInstance().execute(this.mUsm.makeHttpParams());
    }

    private void initPdnInfo() {
        if (this.mUsm.hasConnection() || this.mUsm.mPdnController.isNetworkRequested(this.mUsm.mPdnListener)) {
            this.mUsm.mPdnController.stopPdnConnectivity(this.mUsm.mPdnType, this.mUsm.mPhoneId, this.mUsm.mPdnListener);
        }
        this.mUsm.mPdnType = -1;
        this.mUsm.mSocketFactory = null;
    }

    public boolean processMessage(Message msg) {
        IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "RequestState::ProcessMessage " + msg.what);
        int i = msg.what;
        if (i != 12) {
            if (i == 14) {
                return false;
            }
            if (i != 15) {
                if (i == 100) {
                    requestPdn();
                } else if (i != 101) {
                    switch (i) {
                        case 1:
                            processPdnConnected();
                            break;
                        case 2:
                            this.mUsm.disconnectPdn();
                            break;
                        case 3:
                            initPdnInfo();
                            break;
                        case 4:
                            this.mUsm.processTerminalRequest();
                            break;
                        case 5:
                            this.mUsm.mHasCFCache = false;
                            this.mUsm.mHasOCBCache = false;
                            this.mUsm.mHasICBCache = false;
                            break;
                        case 6:
                            this.mUsm.mProfile.condition = 6;
                            processPutRequest();
                            UtStateMachine utStateMachine = this.mUsm;
                            utStateMachine.transitionTo(utStateMachine.mResponseState);
                            break;
                        case 7:
                            this.mUsm.mProfile.condition = 7;
                            processPutRequest();
                            UtStateMachine utStateMachine2 = this.mUsm;
                            utStateMachine2.transitionTo(utStateMachine2.mResponseState);
                            break;
                        case 8:
                            this.mUsm.mProfile.condition++;
                            if (this.mUsm.mProfile.action == 0) {
                                this.mUsm.mProfile.number = "";
                            }
                            processPutRequest();
                            UtStateMachine utStateMachine3 = this.mUsm;
                            utStateMachine3.transitionTo(utStateMachine3.mResponseState);
                            break;
                        case 9:
                            this.mUsm.mProfile.serviceClass = 16;
                            this.mUsm.mProfile.condition = this.mUsm.mMainCondition;
                            if (this.mUsm.mProfile.action == 0) {
                                this.mUsm.mProfile.number = "";
                            }
                            processPutRequest();
                            UtStateMachine utStateMachine4 = this.mUsm;
                            utStateMachine4.transitionTo(utStateMachine4.mResponseState);
                            break;
                    }
                } else {
                    processGetRequest();
                    UtStateMachine utStateMachine5 = this.mUsm;
                    utStateMachine5.transitionTo(utStateMachine5.mResponseState);
                }
            }
        }
        return !this.mUsm.hasProfile();
    }

    private void processPdnConnected() {
        if (this.mUsm.hasProfile()) {
            this.mUsm.needPdnRequestForCW = false;
            this.mUsm.isRetryingCreatePdn = false;
            if (this.mUsm.mUtServiceModule.isTerminalRequest(this.mUsm.mPhoneId, this.mUsm.mProfile)) {
                this.mUsm.removeMessages(2);
                this.mUsm.sendMessage(2);
                this.mUsm.sendMessageDelayed(4, 100);
                return;
            }
            if (this.mUsm.mFeature.isNeedFirstGet && this.mUsm.mPrevGetType == -1) {
                handleNeedFirstGet();
            }
            if (this.mUsm.isGetAfter412 && this.mUsm.mProfile.type == 115) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "Send GET after PUT error 412");
                this.mUsm.mProfile.type = 114;
            }
            if (this.mUsm.isPutRequest()) {
                if (SimUtil.getSimMno(this.mUsm.mPhoneId) == Mno.WIND_GREECE && this.mUsm.isServiceActive()) {
                    IMSLog.e(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "Service is disabled on network side");
                    this.mUsm.mThisSm.sendMessageDelayed(12, 1011, 100);
                    return;
                } else if (this.mUsm.isPutRequestBlocked()) {
                    this.mUsm.sendMessageDelayed(12, 1012, 100);
                    return;
                } else {
                    processPutRequest();
                }
            } else if ((this.mUsm.mProfile.type != 100 || !this.mUsm.mHasCFCache) && ((this.mUsm.mProfile.type != 104 || !this.mUsm.mHasOCBCache) && (this.mUsm.mProfile.type != 102 || !this.mUsm.mHasICBCache))) {
                processGetRequest();
            } else {
                UtStateMachine utStateMachine = this.mUsm;
                utStateMachine.transitionTo(utStateMachine.mResponseState);
                this.mUsm.sendMessage(13);
                return;
            }
            UtStateMachine utStateMachine2 = this.mUsm;
            utStateMachine2.transitionTo(utStateMachine2.mResponseState);
        }
    }

    private void handleNeedFirstGet() {
        if (this.mUsm.mProfile.type == 101 && this.mUsm.mCFCache == null) {
            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "Send GET before PUT due to no cache.");
            this.mUsm.isGetBeforePut = true;
            this.mUsm.mProfile.type = 100;
        } else if (this.mUsm.mProfile.type == 103 && this.mUsm.mICBCache == null) {
            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "Send GET before PUT due to no cache.");
            this.mUsm.isGetBeforePut = true;
            this.mUsm.mProfile.type = 102;
        } else if (this.mUsm.mProfile.type == 105 && this.mUsm.mOCBCache == null) {
            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "Send GET before PUT due to no cache.");
            this.mUsm.isGetBeforePut = true;
            this.mUsm.mProfile.type = 104;
        }
    }
}
