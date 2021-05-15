package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfCallSetupData {
    private int mCallType;
    private String mConferenceUri;
    private HashMap<String, String> mExtraSipHeaders;
    private ImsUri mOrigUri;
    private List<String> mParticipants;
    private String mReferRemoveUriType;
    private String mReferUriAsserted;
    private String mReferUriType;
    private List<Integer> mSessionIds;
    private String mSubscribeDialogType;
    private String mSubscribeRequired;
    private boolean mSupportPrematureEnd;
    private String mUseAnonymousUpdate;

    public ConfCallSetupData(String uri, int first, int second, int callType) {
        ArrayList arrayList = new ArrayList();
        this.mSessionIds = arrayList;
        this.mExtraSipHeaders = null;
        this.mConferenceUri = uri;
        arrayList.add(Integer.valueOf(first));
        this.mSessionIds.add(Integer.valueOf(second));
        this.mCallType = callType;
    }

    public ConfCallSetupData(String uri, List<String> participants, int callType) {
        this.mSessionIds = new ArrayList();
        this.mExtraSipHeaders = null;
        this.mConferenceUri = uri;
        this.mParticipants = new ArrayList(participants);
        this.mCallType = callType;
    }

    public String getConferenceUri() {
        return this.mConferenceUri;
    }

    public void setOriginatingUri(ImsUri uri) {
        this.mOrigUri = uri;
    }

    public ImsUri getOriginatingUri() {
        return this.mOrigUri;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public List<Integer> getSessionIds() {
        return this.mSessionIds;
    }

    public List<String> getParticipants() {
        return this.mParticipants;
    }

    public void enableSubscription(String enable) {
        this.mSubscribeRequired = enable;
    }

    public String isSubscriptionEnabled() {
        return this.mSubscribeRequired;
    }

    public void setSubscribeDialogType(String type) {
        this.mSubscribeDialogType = type;
    }

    public String getSubscribeDialogType() {
        return this.mSubscribeDialogType;
    }

    public void setReferUriType(String type) {
        this.mReferUriType = type;
    }

    public String getReferUriType() {
        return this.mReferUriType;
    }

    public void setRemoveReferUriType(String type) {
        this.mReferRemoveUriType = type;
    }

    public String getRemoveReferUriType() {
        return this.mReferRemoveUriType;
    }

    public void setReferUriAsserted(String type) {
        this.mReferUriAsserted = type;
    }

    public String getReferUriAsserted() {
        return this.mReferUriAsserted;
    }

    public void setUseAnonymousUpdate(String type) {
        this.mUseAnonymousUpdate = type;
    }

    public String getUseAnonymousUpdate() {
        return this.mUseAnonymousUpdate;
    }

    public void setSupportPrematureEnd(boolean value) {
        this.mSupportPrematureEnd = value;
    }

    public boolean getSupportPrematureEnd() {
        return this.mSupportPrematureEnd;
    }

    public void setExtraSipHeaders(HashMap<String, String> headers) {
        this.mExtraSipHeaders = headers;
    }

    public HashMap<String, String> getExtraSipHeaders() {
        return this.mExtraSipHeaders;
    }

    public String toString() {
        return "ConfCallSetupData [mConferenceUri=" + IMSLog.checker(this.mConferenceUri) + ", mOrigUri=" + IMSLog.checker(this.mOrigUri + "") + ", mSessionIds=" + this.mSessionIds + ", mParticipants=" + IMSLog.checker(this.mParticipants + "") + ", mCallType=" + this.mCallType + ", mSubscribeRequired=" + this.mSubscribeRequired + ", mSubscribeDialogType=" + this.mSubscribeDialogType + ", mReferUriType=" + this.mReferUriType + ", mReferRemoveUriType=" + this.mReferRemoveUriType + ", use Asserted=" + this.mReferUriAsserted + ", useAnonymousUpdate=" + this.mUseAnonymousUpdate + ", mSupportPrematureEnd=" + this.mSupportPrematureEnd + "]";
    }
}
