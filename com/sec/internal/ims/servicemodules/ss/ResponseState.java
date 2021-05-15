package com.sec.internal.ims.servicemodules.ss;

import android.os.Bundle;
import android.os.Message;
import android.telephony.ims.ImsSsInfo;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ResponseState extends State {
    HttpResponseParams mResponseData = null;
    private UtStateMachine mUsm;

    public ResponseState(UtStateMachine utStateMachine) {
        this.mUsm = utStateMachine;
    }

    public void enter() {
    }

    public boolean processMessage(Message msg) {
        String str = UtStateMachine.LOG_TAG;
        int i = this.mUsm.mPhoneId;
        IMSLog.i(str, i, "ResponseState::ProcessMessage " + msg.what + ", mUsm.mIsSuspended " + this.mUsm.mIsSuspended);
        Mno mno = SimUtil.getSimMno(this.mUsm.mPhoneId);
        int i2 = msg.what;
        if (i2 == 3) {
            this.mUsm.sendMessage(12);
        } else if (i2 != 100) {
            switch (i2) {
                case 10:
                    this.mResponseData = (HttpResponseParams) msg.obj;
                    getResultSuccess();
                    break;
                case 11:
                    this.mUsm.mIsUtConnectionError = true;
                    if (!mno.isChn()) {
                        this.mUsm.sendMessage(12, 1015);
                        break;
                    } else {
                        this.mUsm.sendMessage(12, 1015, 0, msg.obj);
                        break;
                    }
                case 12:
                    if ((mno == Mno.CTC || mno == Mno.CTCMO) && this.mUsm.mIsSuspended) {
                        this.mUsm.mIsFailedBySuspended = true;
                        UtStateMachine utStateMachine = this.mUsm;
                        utStateMachine.transitionTo(utStateMachine.mRequestState);
                    }
                    return false;
                case 13:
                    responseGetFromCache();
                    break;
                case 14:
                case 15:
                    return false;
            }
        } else {
            this.mUsm.sendMessage(12, 1016);
        }
        return true;
    }

    private void getResultSuccess() {
        if (this.mResponseData.getStatusCode() == 200 || this.mResponseData.getStatusCode() == 201) {
            if (this.mUsm.isPutRequest()) {
                responsePutResult(true);
            } else if (!TextUtils.isEmpty(this.mResponseData.getDataString())) {
                responseGetResult();
            } else {
                this.mUsm.sendMessage(12, this.mResponseData.getStatusCode());
            }
        } else if (this.mResponseData.getStatusCode() != 404 || !this.mUsm.mFeature.supportSimservsRetry || this.mUsm.isPutRequest() || this.mUsm.mProfile.type == 116) {
            if (this.mResponseData.getStatusCode() == 412 && this.mUsm.mCount412RetryDone < 3) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "mUsm.mProfile.type : " + this.mUsm.mProfile.type);
                int i = this.mUsm.mProfile.type;
                if (i == 101 || i == 103 || i == 105) {
                    this.mUsm.mPrevGetType = -1;
                    this.mUsm.mCount412RetryDone++;
                    UtStateMachine utStateMachine = this.mUsm;
                    utStateMachine.clearCachedSsData(utStateMachine.mProfile.type);
                    UtStateMachine utStateMachine2 = this.mUsm;
                    utStateMachine2.transitionTo(utStateMachine2.mRequestState);
                    this.mUsm.sendMessage(1);
                    return;
                } else if (i == 115) {
                    this.mUsm.isGetAfter412 = true;
                    this.mUsm.mCount412RetryDone++;
                    UtStateMachine utStateMachine3 = this.mUsm;
                    utStateMachine3.transitionTo(utStateMachine3.mRequestState);
                    this.mUsm.sendMessage(1);
                    return;
                }
            }
            if (this.mResponseData.getStatusCode() == 404 && this.mUsm.mProfile.type == 116 && this.mUsm.mIsGetSdBy404) {
                this.mUsm.mProfile.type = this.mUsm.mPrevGetType;
                this.mUsm.mPrevGetType = -1;
                this.mUsm.mIsGetSdBy404 = false;
            }
            if (!TextUtils.isEmpty(this.mResponseData.getDataString())) {
                this.mUsm.sendMessage(12, this.mResponseData.getStatusCode(), 0, this.mResponseData.getDataString());
            } else {
                this.mUsm.sendMessage(12, this.mResponseData.getStatusCode());
            }
        } else {
            this.mUsm.mIsGetSdBy404 = true;
            UtStateMachine utStateMachine4 = this.mUsm;
            utStateMachine4.mPrevGetType = utStateMachine4.mProfile.type;
            this.mUsm.mProfile.type = 116;
            UtStateMachine utStateMachine5 = this.mUsm;
            utStateMachine5.transitionTo(utStateMachine5.mRequestState);
            this.mUsm.sendMessage(1);
        }
    }

    private void responsePutResult(boolean success) {
        if (!success) {
            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "PUT Failed");
            this.mUsm.sendMessage(12);
            return;
        }
        if (this.mUsm.mProfile.type == 109) {
            if (SimUtil.getMno(this.mUsm.mPhoneId).isOneOf(Mno.VINAPHONE)) {
                UtStateMachine utStateMachine = this.mUsm;
                utStateMachine.setUserSet(utStateMachine.mPhoneId, "ss_clir_pref", this.mUsm.mProfile.condition);
            }
        } else if (this.mUsm.mProfile.type == 101 && this.mUsm.mProfile.action == 3) {
            this.mUsm.mPreviousCFCache.copyRule(this.mUsm.mCFCache.getRule(this.mUsm.mProfile.condition, UtUtils.convertToMedia(this.mUsm.mProfile.serviceClass)));
        }
        if (this.mUsm.mSeparatedCFNRY && !this.mUsm.mSeparatedMedia) {
            UtStateMachine utStateMachine2 = this.mUsm;
            utStateMachine2.transitionTo(utStateMachine2.mRequestState);
            this.mUsm.sendMessage(7);
            this.mUsm.mSeparatedCFNRY = false;
        } else if (this.mUsm.mSeparatedCFNL) {
            UtStateMachine utStateMachine3 = this.mUsm;
            utStateMachine3.transitionTo(utStateMachine3.mRequestState);
            this.mUsm.sendMessage(6);
            this.mUsm.mSeparatedCFNL = false;
        } else {
            if (this.mUsm.mSeparatedCfAll) {
                String str = UtStateMachine.LOG_TAG;
                int i = this.mUsm.mPhoneId;
                IMSLog.i(str, i, "mUsm.mProfile.condition : " + this.mUsm.mProfile.condition);
                if (this.mUsm.mProfile.condition == 3 || this.mUsm.mProfile.condition == 6) {
                    this.mUsm.mSeparatedCfAll = false;
                } else {
                    this.mUsm.removeMessages(15);
                    this.mUsm.mThisSm.sendMessageDelayed(15, 1017, 32500);
                    if (this.mUsm.mProfile.condition == 7) {
                        this.mUsm.mProfile.condition = 2;
                    }
                    UtStateMachine utStateMachine4 = this.mUsm;
                    utStateMachine4.transitionTo(utStateMachine4.mRequestState);
                    this.mUsm.sendMessage(8);
                    return;
                }
            }
            if (this.mUsm.mSeparatedMedia) {
                UtStateMachine utStateMachine5 = this.mUsm;
                utStateMachine5.transitionTo(utStateMachine5.mRequestState);
                this.mUsm.sendMessage(9);
                this.mUsm.mSeparatedMedia = false;
                return;
            }
            if (this.mUsm.mFeature.isNeedFirstGet) {
                this.mUsm.clearCachedSsData(-1);
            }
            this.mUsm.completeUtRequest();
        }
    }

    private void cfInfoFromCache() {
        List<Bundle> callForwardList = new ArrayList<>();
        this.mUsm.isGetBeforePut = false;
        if (this.mUsm.mProfile.condition == 4 || this.mUsm.mProfile.condition == 5) {
            callForwardList = cfAllInfoFromCache(callForwardList, (CallForwardingData.Rule) null);
            if (callForwardList.isEmpty()) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "There is no matched rule for CF ALL.");
                Bundle bundle = new Bundle();
                bundle.putInt("status", 0);
                bundle.putInt("serviceClass", this.mUsm.mProfile.serviceClass);
                bundle.putInt("condition", this.mUsm.mProfile.condition);
                this.mUsm.completeUtRequest(bundle);
                return;
            }
        } else {
            if (this.mUsm.mFeature.support_media) {
                for (MEDIA m : MEDIA.values()) {
                    if (m != MEDIA.ALL) {
                        CallForwardingData.Rule cfRule = this.mUsm.mCFCache.getRule(this.mUsm.mProfile.condition, m);
                        IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + m + "] " + cfRule.ruleId);
                        if (!TextUtils.isEmpty(cfRule.ruleId)) {
                            callForwardList.add(makeCFBundle(cfRule));
                        }
                    }
                }
            } else {
                CallForwardingData.Rule cfRule2 = this.mUsm.mCFCache.getRule(this.mUsm.mProfile.condition, MEDIA.ALL);
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + MEDIA.ALL + "] " + cfRule2.ruleId);
                if (!TextUtils.isEmpty(cfRule2.ruleId)) {
                    callForwardList.add(makeCFBundle(cfRule2));
                }
                if (callForwardList.isEmpty()) {
                    for (MEDIA m2 : MEDIA.values()) {
                        if (m2 != MEDIA.ALL) {
                            CallForwardingData.Rule cfRule3 = this.mUsm.mCFCache.getRule(this.mUsm.mProfile.condition, m2);
                            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + m2 + "] " + cfRule3.ruleId);
                            if (!TextUtils.isEmpty(cfRule3.ruleId)) {
                                callForwardList.add(makeCFBundle(cfRule3));
                            }
                        }
                    }
                }
            }
            if (callForwardList.isEmpty()) {
                CallForwardingData.Rule cfRule4 = this.mUsm.mCFCache.getRule(this.mUsm.mProfile.condition, MEDIA.ALL);
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + MEDIA.ALL + "] " + cfRule4.ruleId);
                callForwardList.add(makeCFBundle(cfRule4));
            }
        }
        this.mUsm.mHasCFCache = true;
        this.mUsm.removeMessages(5);
        this.mUsm.sendMessageDelayed(5, 1000);
        this.mUsm.completeUtRequest((Bundle[]) callForwardList.toArray(new Bundle[0]));
    }

    private List<Bundle> cfAllInfoFromCache(List<Bundle> callForwardList, CallForwardingData.Rule cfRule) {
        int startCond = 0;
        if (this.mUsm.mProfile.condition == 5) {
            startCond = 1;
        }
        for (MEDIA m : MEDIA.values()) {
            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "MEDIA = " + m);
            String tmpTarget = null;
            int tmpState = -1;
            boolean valid = true;
            int cond = startCond;
            while (true) {
                if (cond >= 4) {
                    break;
                }
                cfRule = this.mUsm.mCFCache.getRule(cond, m);
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID " + cond + " = " + cfRule.ruleId);
                if (cfRule.ruleId == null) {
                    valid = false;
                    break;
                }
                if (tmpState != -1) {
                    if (tmpState != cfRule.conditions.state) {
                        valid = false;
                        break;
                    }
                } else {
                    tmpState = cfRule.conditions.state;
                }
                if (tmpState != 0) {
                    if (tmpTarget == null) {
                        tmpTarget = cfRule.fwdElm.target;
                    } else if (!tmpTarget.equals(cfRule.fwdElm.target)) {
                        valid = false;
                        break;
                    }
                }
                cond++;
            }
            if (valid) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "This target number is valid for CF ALL.");
                callForwardList.add(makeCFBundle(cfRule));
            }
        }
        return callForwardList;
    }

    private void responseGetFromCache() {
        CallBarringData cbCache;
        int i = this.mUsm.mProfile.type;
        if (i == 100) {
            cfInfoFromCache();
        } else if (i == 102 || i == 104) {
            List<Bundle> callBarringList = new ArrayList<>();
            if (this.mUsm.mProfile.type == 104) {
                cbCache = this.mUsm.mOCBCache;
            } else {
                cbCache = this.mUsm.mICBCache;
            }
            int i2 = 0;
            if (!this.mUsm.mFeature.support_media || this.mUsm.mFeature.noMediaForCB) {
                CallBarringData.Rule cbRule = cbCache.getRule(this.mUsm.mProfile.condition, MEDIA.ALL);
                String str = UtStateMachine.LOG_TAG;
                int i3 = this.mUsm.mPhoneId;
                IMSLog.i(str, i3, "GET RULE ID [" + MEDIA.ALL + "] " + cbRule.ruleId);
                if (this.mUsm.mProfile.condition == 10) {
                    callBarringList = createRuleId(cbCache);
                } else if (!TextUtils.isEmpty(cbRule.ruleId)) {
                    callBarringList.add(makeCBBundle(cbRule));
                }
                if (callBarringList.isEmpty()) {
                    MEDIA[] values = MEDIA.values();
                    int length = values.length;
                    while (i2 < length) {
                        MEDIA m = values[i2];
                        if (m != MEDIA.ALL) {
                            CallBarringData.Rule cbRule2 = cbCache.getRule(this.mUsm.mProfile.condition, m);
                            String str2 = UtStateMachine.LOG_TAG;
                            int i4 = this.mUsm.mPhoneId;
                            IMSLog.i(str2, i4, "GET RULE ID [" + m + "] " + cbRule2.ruleId);
                            if (!TextUtils.isEmpty(cbRule2.ruleId)) {
                                callBarringList.add(makeCBBundle(cbRule2));
                            }
                        }
                        i2++;
                    }
                }
            } else {
                MEDIA[] values2 = MEDIA.values();
                int length2 = values2.length;
                while (i2 < length2) {
                    MEDIA m2 = values2[i2];
                    if (m2 != MEDIA.ALL) {
                        CallBarringData.Rule cbRule3 = cbCache.getRule(this.mUsm.mProfile.condition, m2);
                        String str3 = UtStateMachine.LOG_TAG;
                        int i5 = this.mUsm.mPhoneId;
                        IMSLog.i(str3, i5, "GET RULE ID [" + m2 + "] " + cbRule3.ruleId);
                        if (!TextUtils.isEmpty(cbRule3.ruleId)) {
                            callBarringList.add(makeCBBundle(cbRule3));
                        }
                    }
                    i2++;
                }
                if (callBarringList.isEmpty()) {
                    CallBarringData.Rule cbRule4 = cbCache.getRule(this.mUsm.mProfile.condition, MEDIA.ALL);
                    String str4 = UtStateMachine.LOG_TAG;
                    int i6 = this.mUsm.mPhoneId;
                    IMSLog.i(str4, i6, "GET RULE ID [" + MEDIA.ALL + "] " + cbRule4.ruleId);
                    if (!TextUtils.isEmpty(cbRule4.ruleId)) {
                        callBarringList.add(makeCBBundle(cbRule4));
                    }
                }
            }
            if (this.mUsm.mProfile.type == 104) {
                this.mUsm.mHasOCBCache = true;
            } else {
                this.mUsm.mHasICBCache = true;
            }
            this.mUsm.removeMessages(5);
            this.mUsm.sendMessageDelayed(5, 1000);
            this.mUsm.completeUtRequest((Bundle[]) callBarringList.toArray(new Bundle[callBarringList.size()]));
        }
    }

    public void responseGetResult() {
        UtXmlParse parse = new UtXmlParse();
        IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "Print Result" + IMSLog.numberChecker(this.mResponseData.getDataString()));
        int i = this.mUsm.mProfile.type;
        int status = 1;
        if (i == 100) {
            this.mUsm.mCFCache = parse.parseCallForwarding(this.mResponseData.getDataString());
            if (this.mUsm.isGetBeforePut) {
                this.mUsm.mProfile.type = 101;
                this.mUsm.isGetBeforePut = false;
                this.mUsm.mHasCFCache = false;
                UtStateMachine utStateMachine = this.mUsm;
                utStateMachine.transitionTo(utStateMachine.mRequestState);
                this.mUsm.sendMessage(1);
                return;
            }
            responseGetFromCache();
        } else if (i == 102 || i == 104) {
            CallBarringData cbData = parse.parseCallBarring(this.mResponseData.getDataString());
            if (this.mUsm.mProfile.type == 104) {
                this.mUsm.mOCBCache = cbData;
                if (this.mUsm.isGetBeforePut) {
                    this.mUsm.mProfile.type = 105;
                    this.mUsm.isGetBeforePut = false;
                    this.mUsm.mHasOCBCache = false;
                    UtStateMachine utStateMachine2 = this.mUsm;
                    utStateMachine2.transitionTo(utStateMachine2.mRequestState);
                    this.mUsm.sendMessage(1);
                    return;
                }
            } else {
                this.mUsm.mICBCache = cbData;
                if (this.mUsm.isGetBeforePut) {
                    this.mUsm.mProfile.type = 103;
                    this.mUsm.isGetBeforePut = false;
                    this.mUsm.mHasICBCache = false;
                    UtStateMachine utStateMachine3 = this.mUsm;
                    utStateMachine3.transitionTo(utStateMachine3.mRequestState);
                    this.mUsm.sendMessage(1);
                    return;
                }
            }
            responseGetFromCache();
        } else if (i == 106) {
            if (!parse.parseCallWaitingOrClip(this.mResponseData.getDataString())) {
                status = 0;
            }
            ImsSsInfo ssInfo = new ImsSsInfo(status, "");
            Bundle clipData = new Bundle();
            clipData.putParcelable("imsSsInfo", ssInfo);
            this.mUsm.completeUtRequest(clipData);
        } else if (i == 108) {
            int[] clir = {parse.parseClir(this.mResponseData.getDataString()), 4};
            if (SimUtil.getMno(this.mUsm.mPhoneId).isOneOf(Mno.VINAPHONE) && clir[0] != 1) {
                UtStateMachine utStateMachine4 = this.mUsm;
                clir[0] = utStateMachine4.getUserSetToInt(utStateMachine4.mPhoneId, "ss_clir_pref", 0);
            }
            Bundle clirData = new Bundle();
            clirData.putIntArray("queryClir", clir);
            this.mUsm.completeUtRequest(clirData);
        } else if (i != 114) {
            if (i == 116) {
                if (this.mUsm.mPrevGetType == -1) {
                    this.mUsm.mPrevGetType = -1;
                    this.mUsm.completeUtRequest();
                } else if (this.mUsm.isGetBeforePut) {
                    this.mUsm.isGetBeforePut = false;
                    if (this.mUsm.mPrevGetType == 104) {
                        this.mUsm.mProfile.type = 105;
                        this.mUsm.mHasOCBCache = false;
                    } else {
                        this.mUsm.mProfile.type = 103;
                        this.mUsm.mHasICBCache = false;
                    }
                    UtStateMachine utStateMachine5 = this.mUsm;
                    utStateMachine5.transitionTo(utStateMachine5.mRequestState);
                    this.mUsm.sendMessage(1);
                } else {
                    this.mUsm.mProfile.type = this.mUsm.mPrevGetType;
                    this.mUsm.mPrevGetType = -1;
                    Bundle bundle = new Bundle();
                    bundle.putInt("status", 0);
                    bundle.putInt("serviceClass", 255);
                    bundle.putInt("condition", this.mUsm.mProfile.condition);
                    this.mUsm.completeUtRequest(new Bundle[]{bundle});
                }
            }
        } else if (this.mUsm.isGetAfter412) {
            this.mUsm.mProfile.type = 115;
            this.mUsm.isGetAfter412 = false;
            UtStateMachine utStateMachine6 = this.mUsm;
            utStateMachine6.transitionTo(utStateMachine6.mRequestState);
            this.mUsm.sendMessage(1);
        } else {
            this.mUsm.completeUtRequest(parse.parseCallWaitingOrClip(this.mResponseData.getDataString()));
        }
    }

    private List<Bundle> createRuleId(CallBarringData cbData) {
        List<Bundle> sibData = new ArrayList<>();
        for (SsRuleData.SsRule tempRule : cbData.rules) {
            CallBarringData.Rule rule = (CallBarringData.Rule) tempRule;
            if (rule.conditions.condition == 10 && rule.ruleId.contains("DBL")) {
                Bundle tempBundle = new Bundle();
                Boolean bOneId = false;
                StringBuilder number = new StringBuilder();
                for (String uri : rule.target) {
                    if (bOneId.booleanValue()) {
                        number.append("$");
                    }
                    bOneId = true;
                    number.append(uri);
                }
                tempBundle.putString("number", rule.ruleId + "," + number.toString());
                if (rule.conditions.state) {
                    tempBundle.putInt("status", 1);
                } else {
                    tempBundle.putInt("status", 0);
                }
                tempBundle.putInt("condition", rule.conditions.condition);
                sibData.add(tempBundle);
            }
        }
        return sibData;
    }

    private Bundle makeCFBundle(CallForwardingData.Rule cfRule) {
        Mno mno = SimUtil.getSimMno(this.mUsm.mPhoneId);
        Bundle bundle = new Bundle();
        if (!cfRule.conditions.state || TextUtils.isEmpty(cfRule.fwdElm.target)) {
            bundle.putInt("status", 0);
        } else {
            bundle.putInt("status", 1);
        }
        bundle.putInt("condition", this.mUsm.mProfile.condition);
        if (!TextUtils.isEmpty(cfRule.fwdElm.target)) {
            if ("+".contains(cfRule.fwdElm.target)) {
                bundle.putInt("ToA", 145);
            } else {
                bundle.putInt("ToA", 129);
            }
            String tmpTarget = UtUtils.getNumberFromURI(cfRule.fwdElm.target);
            if (mno != Mno.SINGTEL || tmpTarget.startsWith("+")) {
                if (mno == Mno.VODAFONE_QATAR && !tmpTarget.startsWith("+")) {
                    tmpTarget = UtUtils.makeInternationNumber(tmpTarget, "+974");
                }
            } else if (!tmpTarget.startsWith("0")) {
                tmpTarget = "+65" + tmpTarget;
            } else {
                tmpTarget = "+65" + tmpTarget.substring(1);
            }
            bundle.putString("number", UtUtils.removeUriPlusPrefix(tmpTarget, mno));
        }
        int ssClass = UtUtils.doconvertMediaTypeToSsClass(cfRule.conditions.media);
        if (mno == Mno.ATT && ssClass == 255) {
            bundle.putInt("serviceClass", 1);
        } else if (mno == Mno.VODAFONE_SPAIN && ssClass == 255) {
            bundle.putInt("serviceClass", 49);
        } else {
            bundle.putInt("serviceClass", ssClass);
        }
        if (!(this.mUsm.mCFCache == null || this.mUsm.mCFCache.replyTimer == 0)) {
            bundle.putInt(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER, this.mUsm.mCFCache.replyTimer);
        }
        return bundle;
    }

    private Bundle makeCBBundle(CallBarringData.Rule cbRule) {
        Bundle bundle = new Bundle();
        if (cbRule.conditions.state) {
            bundle.putInt("status", 1);
        } else {
            bundle.putInt("status", 0);
        }
        bundle.putInt("condition", cbRule.conditions.condition);
        bundle.putInt("serviceClass", UtUtils.doconvertMediaTypeToSsClass(cbRule.conditions.media));
        return bundle;
    }
}
