package com.sec.internal.ims.servicemodules.volte2.data;

import android.os.Bundle;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class CallSetupData {
    private String mAlertInfo;
    private int mCallType;
    private String mCli;
    private int mCmcBoundSessionId = -1;
    private Bundle mComposerData = null;
    private ImsUri mDestUri;
    private String mDialingNumber;
    private boolean mIsEmergency = false;
    private boolean mIsLteEpsOnlyAttached = false;
    private String mLetteringText;
    private ImsUri mOrigUri;
    private List<String> mP2p = null;
    private String mPEmergencyInfoOfAtt;
    private String mReplaceCallId = null;

    public CallSetupData(ImsUri destUri, String dialingNumber, int callType, String cli) {
        this.mDestUri = destUri;
        this.mDialingNumber = dialingNumber;
        this.mCallType = callType;
        this.mCli = cli;
        this.mIsEmergency = isE911Call(callType);
    }

    public void setOriginatingUri(ImsUri uri) {
        this.mOrigUri = uri;
    }

    public void setLetteringText(String lettering) {
        this.mLetteringText = lettering;
    }

    public void setAlertInfo(String alertInfo) {
        this.mAlertInfo = alertInfo;
    }

    public void setCli(String cli) {
        this.mCli = cli;
    }

    public void setP2p(List<String> p2p) {
        this.mP2p = p2p;
    }

    public void setLteEpsOnlyAttached(boolean isLteEpsOnlyAttached) {
        this.mIsLteEpsOnlyAttached = isLteEpsOnlyAttached;
    }

    public void setCmcBoundSessionId(int cmcBoundSessionId) {
        this.mCmcBoundSessionId = cmcBoundSessionId;
    }

    public ImsUri getOriginatingUri() {
        return this.mOrigUri;
    }

    public ImsUri getDestinationUri() {
        return this.mDestUri;
    }

    public String getDialingNumber() {
        return this.mDialingNumber;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public String getLetteringText() {
        return this.mLetteringText;
    }

    public String getAlertInfo() {
        return this.mAlertInfo;
    }

    public String getCli() {
        return this.mCli;
    }

    public List<String> getP2p() {
        return this.mP2p;
    }

    public boolean isEmergency() {
        return this.mIsEmergency;
    }

    public void setPEmergencyInfoOfAtt(String PEmergencyInfo) {
        this.mPEmergencyInfoOfAtt = PEmergencyInfo;
    }

    public String getPEmergencyInfoOfAtt() {
        return this.mPEmergencyInfoOfAtt;
    }

    public boolean getLteEpsOnlyAttached() {
        return this.mIsLteEpsOnlyAttached;
    }

    public int getCmcBoundSessionId() {
        return this.mCmcBoundSessionId;
    }

    private static boolean isE911Call(int callType) {
        if (callType == 7 || callType == 8 || callType == 13 || callType == 18 || callType == 19) {
            return true;
        }
        return false;
    }

    public Bundle getComposerData() {
        return this.mComposerData;
    }

    public void setComposerData(Bundle composerData) {
        this.mComposerData = composerData;
    }

    public String getReplaceCallId() {
        return this.mReplaceCallId;
    }

    public void setReplaceCallId(String replaceCallId) {
        this.mReplaceCallId = replaceCallId;
    }

    public String toString() {
        return "CallSetupData [mOrigUri=" + IMSLog.checker(this.mOrigUri + "") + ", mDestUri=" + IMSLog.checker(this.mDestUri + "") + ", mDialingNumber=" + IMSLog.checker(this.mDialingNumber) + ", mCallType=" + this.mCallType + ", mLetteringText=" + this.mLetteringText + ", mIsEmergency=" + this.mIsEmergency + ", mPEmergencyInfoOfAtt=" + this.mPEmergencyInfoOfAtt + ", mCli=" + this.mCli + ", mAlertInfo=" + this.mAlertInfo + ", mIsLteEpsOnlyAttached=" + this.mIsLteEpsOnlyAttached + ", mCmcBoundSessionId=" + this.mCmcBoundSessionId + ", mReplaceCallId=" + this.mReplaceCallId + "]";
    }
}
