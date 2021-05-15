package com.sec.internal.ims.core;

import android.net.Uri;
import android.os.Message;
import android.os.SystemClock;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import java.util.List;

public abstract class RegistrationGovernor implements IRegistrationGovernor {
    protected static final int DEFAULT_RETRY_AFTER = 1;
    public static final int PREFERED_IMPU_TYPE_ANY_FIRST = 0;
    public static final int PREFERED_IMPU_TYPE_IMSI_BASED = 1;
    public static final int RELEASE_AIRPLANEMODE_ON = 1;
    public static final int RELEASE_ALWAYS = 0;
    public static final int RELEASE_AUTOCONFIG_UPDATED = 7;
    public static final int RELEASE_CMC_UPDATED = 8;
    public static final int RELEASE_DETACH_WITH_REATTACH = 10;
    public static final int RELEASE_NETWORK_CHANGED = 6;
    public static final int RELEASE_PDN_DISCONNECTED = 5;
    public static final int RELEASE_PLMN_CHANGED = 9;
    public static final int RELEASE_SIM_REMOVED = 4;
    public static final int RELEASE_WFC_TURNED_OFF = 3;
    public static final int RELEASE_WIFI_TURNED_OFF = 2;
    public static final int RETRY_AFTER_EPDGDEREGI = 1;
    public static final int RETRY_AFTER_PDNLOST = 3;
    protected int mCallStatus = 0;
    protected String mCountry = null;
    protected int mCurImpu = 0;
    protected int mCurPcscfIpIdx = 0;
    protected boolean mDelayedDeregisterTimerRunning = false;
    protected boolean mDiscardCurrentNetwork = false;
    protected int mFailureCounter = 0;
    protected boolean mHandlePcscfOnAlternativeCall = false;
    protected boolean mHasPdnFailure = false;
    protected boolean mHasVoLteCall = false;
    protected boolean mIPsecAllow = true;
    protected boolean mIsPermanentPdnFailed = false;
    protected boolean mIsPermanentStopped = false;
    protected boolean mIsReadyToGetReattach = false;
    protected boolean mIsValid = false;
    protected boolean mMoveToNextPcscfAfterTimerB = false;
    protected boolean mNeedToCheckLocationSetting = true;
    protected boolean mNeedToCheckSrvcc = false;
    protected boolean mNonVoLTESimByPdnFail = false;
    protected int mNumOfPcscfIp = 0;
    protected PcoType mPcoType = PcoType.PCO_POSTPAY;
    protected List<String> mPcscfIpList = null;
    protected long mPdnFailRetryTime = -1;
    protected int mPdnRejectCounter = 0;
    protected int mPhoneId;
    protected boolean mPse911Prohibited = false;
    protected int mRegBaseTime = 30;
    protected int mRegMaxTime = 1800;
    protected long mRegiAt = 0;
    protected int mRetryAfter = 0;
    protected Message mRetryTimeout = null;
    protected int mSubscribeForbiddenCounter = 0;
    protected Message mTimEshtablishTimeout = null;
    protected Message mTimEshtablishTimeoutRCS = null;
    protected boolean mUpsmEnabled = false;
    protected int mWFCSubscribeForbiddenCounter = 0;

    public enum PcoType {
        PCO_DEFAULT(-2),
        PCO_AWAITING(-1),
        PCO_POSTPAY(0),
        PCO_RESTRICTED_ACCESS(2),
        PCO_ZERO_BALANCE(3),
        PCO_RATE_THROTTLING(4),
        PCO_SELF_ACTIVATION(5);
        
        private int mType;

        private PcoType(int pcoType) {
            this.mType = -1;
            this.mType = pcoType;
        }

        public static PcoType fromType(int pcoType) {
            for (PcoType t : values()) {
                if (t.mType == pcoType) {
                    return t;
                }
            }
            return PCO_DEFAULT;
        }
    }

    public void resetPdnFailureInfo() {
        this.mPdnRejectCounter = 0;
        this.mHasPdnFailure = false;
    }

    public boolean hasPdnFailure() {
        return this.mHasPdnFailure;
    }

    public boolean isNonVoLteSimByPdnFail() {
        return this.mNonVoLTESimByPdnFail;
    }

    public void makeThrottle() {
        this.mIsPermanentStopped = true;
    }

    public void resetPermanentFailure() {
        this.mDiscardCurrentNetwork = false;
    }

    public void resetPcscfList() {
        this.mIsValid = false;
    }

    public void setPcoType(PcoType type) {
        this.mPcoType = type;
    }

    public void resetPcoType() {
        this.mPcoType = PcoType.PCO_POSTPAY;
    }

    public void addDelay(int delay) {
        this.mRegiAt = SystemClock.elapsedRealtime() + ((long) delay);
    }

    public void setRetryTimeOnPdnFail(long retryTime) {
        this.mPdnFailRetryTime = retryTime;
    }

    /* access modifiers changed from: protected */
    public void setCallStatus(int callStatus) {
        this.mCallStatus = callStatus;
    }

    public boolean isThrottled() {
        return getThrottleState() != IRegistrationGovernor.ThrottleState.IDLE;
    }

    public IRegistrationGovernor.ThrottleState getThrottleState() {
        IRegistrationGovernor.ThrottleState state = IRegistrationGovernor.ThrottleState.IDLE;
        if (this.mIsPermanentStopped) {
            return IRegistrationGovernor.ThrottleState.PERMANENTLY_STOPPED;
        }
        if (this.mRegiAt > SystemClock.elapsedRealtime()) {
            return IRegistrationGovernor.ThrottleState.TEMPORARY_THROTTLED;
        }
        return state;
    }

    public boolean hasValidPcscfIpList() {
        return this.mIsValid;
    }

    public int getNextImpuType() {
        return this.mCurImpu;
    }

    public int getFailureCount() {
        return this.mFailureCounter;
    }

    public boolean isExistRetryTimer() {
        return this.mRetryTimeout != null;
    }

    public boolean isPse911Prohibited() {
        return this.mPse911Prohibited;
    }

    public PcoType getPcoType() {
        return this.mPcoType;
    }

    public int getWFCSubscribeForbiddenCounter() {
        return this.mWFCSubscribeForbiddenCounter;
    }

    public int getPcscfOrdinal() {
        return this.mCurPcscfIpIdx;
    }

    public long getRetryTimeOnPdnFail() {
        return this.mPdnFailRetryTime;
    }

    /* access modifiers changed from: protected */
    public int getCallStatus() {
        return this.mCallStatus;
    }

    public void onTelephonyCallStatusChanged(int callState) {
    }

    public boolean isNeedDelayedDeregister() {
        return false;
    }

    public boolean onUpdatedPcoInfo(String pdn, int pco) {
        return false;
    }

    public boolean isDelayedDeregisterTimerRunning() {
        return false;
    }

    public boolean isOmadmConfigAvailable() {
        return true;
    }

    public boolean isIPSecAllow() {
        return true;
    }

    public boolean isDeregisterOnLocationUpdate() {
        return false;
    }

    public boolean needPendingPdnConnected() {
        return false;
    }

    public boolean checkEmergencyInProgress() {
        return false;
    }

    public boolean isReadyToDualRegister(boolean isCmcDualRegi) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkEpdgEvent(int rat) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int rat) {
        return true;
    }

    public boolean onUpdateGeolocation(LocationInfo geolocation) {
        return false;
    }

    public boolean needImsNotAvailable() {
        return false;
    }

    public int getP2pListSize(int cmcType) {
        return 0;
    }

    public void notifyLocationTimeout() {
    }

    public void requestLocation(int phoneId) {
    }

    public void onLocationCacheExpiry() {
    }

    public void onServiceStateDataChanged(boolean isPsOnly, boolean isRoaming) {
    }

    public void onDeregistrationDone(boolean requested) {
    }

    public void onSubscribeError(int event, SipError error) {
    }

    public void onPublishError(SipError error) {
    }

    public void resetIPSecAllow() {
    }

    public void resetPcscfPreference() {
    }

    public void startTimsTimer(String reason) {
    }

    public void stopTimsTimer(String reason) {
    }

    public void startOmadmProvisioningUpdate() {
    }

    public void finishOmadmProvisioningUpdate() {
    }

    public void retryDNSQuery() {
    }

    public void onDelayedDeregister() {
    }

    public void resetAllPcscfChecked() {
    }

    public void onRegEventContactUriNotification(List<ImsUri> list, int isRegi, String contactUriType) {
    }

    public void onContactActivated() {
    }

    public void checkAcsPcscfListChange() {
    }

    public void setNeedDelayedDeregister(boolean val) {
    }

    public void notifyVoLteOnOffToRil(boolean enabled) {
    }

    public void notifyReattachToRil() {
    }

    public void onWfcProfileChanged(byte[] data) {
    }

    public void resetAllRetryFlow() {
    }

    public void onVolteRoamingSettingChanged(boolean enabled) {
    }

    public void onLteDataNetworkModeSettingChanged(boolean enabled) {
    }

    public void onRoamingDataChanged(boolean enabled) {
    }

    public void onRoamingLteChanged(boolean enabled) {
    }

    public void onVolteSettingChanged() {
    }

    public void checkProfileUpdateFromDM(boolean force) {
    }

    public void onConfigUpdated() {
    }

    public void unRegisterIntentReceiver() {
    }

    public void onPdnConnected() {
    }

    public void onPackageDataCleared(Uri data) {
    }

    public void enableRcsOverIms(ImsProfile rcsProfile) {
    }

    public boolean hasNetworkFailure() {
        return false;
    }

    public boolean isMobilePreferredForRcs() {
        return false;
    }

    public boolean isReadyToGetReattach() {
        return this.mIsReadyToGetReattach;
    }
}
