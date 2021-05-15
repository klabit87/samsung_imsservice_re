package com.sec.internal.constants.ims.servicemodules.volte2;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.log.IMSLog;

public class CallParams {
    private Bundle composerData;
    private String mAlertInfo;
    private String mAudioBitRate;
    private String mAudioCodec;
    private int mAudioEarlyMediaDir;
    private int mAudioRxTrackId;
    private String mCmcDeviceId;
    private String mConferenceSupported;
    private String mDtmfEvent;
    private String mFeatureCaps;
    private boolean mHasDiversion;
    private String mHistoryInfo;
    private boolean mIncomingCall = false;
    private int mIndicationFlag;
    private String mIsFocus;
    private boolean mLocalHoldTone;
    private int mLocalVideoRTCPPort;
    private int mLocalVideoRTPPort;
    private String mModifySupported;
    private String mNumberPlus;
    private String mPLettering;
    private String mPhotoRing;
    private String mReferredBy;
    private int mRemoteVideoRTCPPort;
    private int mRemoteVideoRTPPort;
    private String mReplaces;
    private int mRetryAfter;
    private String mSipCallId;
    private String mSipInviteMsg;
    private ImsUri mTerminatingId;
    private String mVerstat;
    private int mVideoCrbtType;
    private int mVideoHeight = 640;
    private int mVideoOrientation;
    private int mVideoWidth = NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE;
    private int misHDIcon;

    public Bundle getComposerData() {
        return this.composerData;
    }

    public void setComposerData(Bundle composerData2) {
        this.composerData = composerData2;
    }

    public String getPLettering() {
        return this.mPLettering;
    }

    public String getHistoryInfo() {
        return this.mHistoryInfo;
    }

    public void setHistoryInfo(String text) {
        this.mHistoryInfo = text;
    }

    public String getDtmfEvent() {
        return this.mDtmfEvent;
    }

    public void setDtmfEvent(String dtmfEvent) {
        this.mDtmfEvent = dtmfEvent;
    }

    public String getModifyHeader() {
        return this.mModifySupported;
    }

    public void setModifyHeader(String text) {
        this.mModifySupported = text;
    }

    public String getAudioCodec() {
        return this.mAudioCodec;
    }

    public void setAudioCodec(String codec) {
        this.mAudioCodec = codec;
    }

    public String getNumberPlus() {
        return this.mNumberPlus;
    }

    public void setNumberPlus(String text) {
        this.mNumberPlus = text;
    }

    public String getConferenceSupported() {
        return this.mConferenceSupported;
    }

    public void setConferenceSupported(String confSupported) {
        this.mConferenceSupported = confSupported;
    }

    public String getIsFocus() {
        return this.mIsFocus;
    }

    public void setIsFocus(String isFocus) {
        this.mIsFocus = isFocus;
    }

    public int getLocalVideoRTPPort() {
        return this.mLocalVideoRTPPort;
    }

    public void setLocalVideoRTPPort(int port) {
        this.mLocalVideoRTPPort = port;
    }

    public int getLocalVideoRTCPPort() {
        return this.mLocalVideoRTCPPort;
    }

    public void setLocalVideoRTCPPort(int port) {
        this.mLocalVideoRTCPPort = port;
    }

    public int getRemoteVideoRTPPort() {
        return this.mRemoteVideoRTPPort;
    }

    public void setRemoteVideoRTPPort(int port) {
        this.mRemoteVideoRTPPort = port;
    }

    public int getRemoteVideoRTCPPort() {
        return this.mRemoteVideoRTCPPort;
    }

    public void setRemoteVideoRTCPPort(int port) {
        this.mRemoteVideoRTCPPort = port;
    }

    public int getIndicationFlag() {
        return this.mIndicationFlag;
    }

    public void setIndicationFlag(int flag) {
        this.mIndicationFlag = flag;
    }

    public int getisHDIcon() {
        return this.misHDIcon;
    }

    public void setisHDIcon(int flag) {
        this.misHDIcon = flag;
    }

    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    public String getPhotoRing() {
        return this.mPhotoRing;
    }

    public void setPhotoRing(String photoRing) {
        this.mPhotoRing = photoRing;
    }

    public String getAlertInfo() {
        return this.mAlertInfo;
    }

    public void setAlertInfo(String alertInfo) {
        this.mAlertInfo = alertInfo;
    }

    public int getVideoCrbtType() {
        return this.mVideoCrbtType;
    }

    public void setVideoCrbtType(int videoCrbtType) {
        this.mVideoCrbtType = videoCrbtType;
    }

    public int getVideoOrientation() {
        return this.mVideoOrientation;
    }

    public void setVideoOrientation(int videoOrientation) {
        this.mVideoOrientation = videoOrientation;
    }

    public void setReferredBy(String referredBy) {
        this.mReferredBy = referredBy;
    }

    public String getSipCallId() {
        return this.mSipCallId;
    }

    public void setSipCallId(String sipCallId) {
        this.mSipCallId = sipCallId;
    }

    public String getSipInviteMsg() {
        return this.mSipInviteMsg;
    }

    public void setSipInviteMsg(String invite) {
        this.mSipInviteMsg = invite;
    }

    public ImsUri getTerminatingId() {
        return this.mTerminatingId;
    }

    public void setTerminatingId(ImsUri terminatingId) {
        this.mTerminatingId = terminatingId;
    }

    public void setReplaces(String value) {
        this.mReplaces = value;
    }

    public String getReplaces() {
        return this.mReplaces;
    }

    public void setLocalHoldTone(boolean value) {
        this.mLocalHoldTone = value;
    }

    public boolean getLocalHoldTone() {
        return this.mLocalHoldTone;
    }

    public void setVerstat(String verstat) {
        this.mVerstat = verstat;
    }

    public String getVerstat() {
        return this.mVerstat;
    }

    public void setVideoWidth(int value) {
        this.mVideoWidth = value;
    }

    public int getVideoWidth() {
        return this.mVideoWidth;
    }

    public void setVideoHeight(int value) {
        this.mVideoHeight = value;
    }

    public int getVideoHeight() {
        return this.mVideoHeight;
    }

    public String getCmcDeviceId() {
        return this.mCmcDeviceId;
    }

    public void setCmcDeviceId(String cmcDeviceId) {
        this.mCmcDeviceId = cmcDeviceId;
    }

    public void setAudioRxTrackId(int value) {
        this.mAudioRxTrackId = value;
    }

    public int getAudioRxTrackId() {
        return this.mAudioRxTrackId;
    }

    public void setAudioBitRate(String value) {
        this.mAudioBitRate = value;
    }

    public String getAudioBitRate() {
        return this.mAudioBitRate;
    }

    public void setFeatureCaps(String featureCaps) {
        this.mFeatureCaps = featureCaps;
    }

    public String getFeatureCaps() {
        return this.mFeatureCaps;
    }

    public boolean isIncomingCall() {
        return this.mIncomingCall;
    }

    public void setAsIncomingCall() {
        this.mIncomingCall = true;
    }

    public void setAudioEarlyMediaDir(int audioEarlyMediaDir) {
        this.mAudioEarlyMediaDir = audioEarlyMediaDir;
    }

    public int getAudioEarlyMediaDir() {
        return this.mAudioEarlyMediaDir;
    }

    public void setHasDiversion(boolean value) {
        this.mHasDiversion = value;
    }

    public boolean getHasDiversion() {
        return this.mHasDiversion;
    }

    public String toString() {
        String historyInfo = TextUtils.isEmpty(this.mHistoryInfo) ? this.mHistoryInfo : IMSLog.checker(this.mHistoryInfo);
        return "CallParams [mPLettering=" + this.mPLettering + ", mHistoryInfo=" + historyInfo + ", mDtmfEvent=" + this.mDtmfEvent + ", mModifySupported=" + this.mModifySupported + ", mAudioCodec=" + this.mAudioCodec + ", mNumberPlus=" + this.mNumberPlus + ", mConferenceSupported=" + this.mConferenceSupported + ", mIsFocus=" + this.mIsFocus + ", mIndicationFlag=" + this.mIndicationFlag + ", misHDIcon=" + this.misHDIcon + ", mPhotoRing=" + this.mPhotoRing + ", mLocalVideoRTPPort=" + this.mLocalVideoRTPPort + ", mLocalVideoRTCPPort=" + this.mLocalVideoRTCPPort + ", mRemoteVideoRTPPort=" + this.mRemoteVideoRTPPort + ", mRemoteVideoRTCPPort=" + this.mRemoteVideoRTCPPort + ", mRetryAfter=" + this.mRetryAfter + ", mAlertInfo=" + this.mAlertInfo + ", mVideoOrientation=" + this.mVideoOrientation + ", mReferredBy=" + IMSLog.checker(this.mReferredBy) + ", mSipCallId=" + this.mSipCallId + ", mLocalHoldTone=" + this.mLocalHoldTone + ", mVideoWidth=" + this.mVideoWidth + ", mVideoHeight=" + this.mVideoHeight + ", mVideoCrbtType=" + this.mVideoCrbtType + ", mFeatureCaps=" + this.mFeatureCaps + ", mAudioEarlyMediaDir=" + this.mAudioEarlyMediaDir + ", mVerstat=" + IMSLog.checker(this.mVerstat) + ", mHasDiversion=" + this.mHasDiversion + "]";
    }
}
