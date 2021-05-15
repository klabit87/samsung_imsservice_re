package com.sec.internal.ims.core;

import android.content.Context;
import android.net.Network;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.handler.secims.UserAgent;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RegisterTask implements IRegisterTask {
    private static final String LOG_TAG = "RegisterTask";
    Context mContext;
    int mDeregiReason;
    private int mDnsQueryRetryCount;
    private List<String> mFilteredReason;
    IRegistrationGovernor mGovernor;
    boolean mHasForcedPendingUpdate;
    private boolean mHasPendingDeregister;
    private boolean mHasPendingEpdgHandover;
    public boolean mHasPendingRegister;
    boolean mHasPendingUpdate;
    private boolean mImmediatePendingUpdate;
    private boolean mIsRefreshReg;
    public boolean mIsUpdateRegistering;
    private boolean mKeepEmergencyTask;
    boolean mKeepPdn;
    private String mLastPani;
    private int mLastRegiFailReason;
    Mno mMno;
    protected Network mNetworkConnected;
    int mNotAvailableReason;
    Object mObject;
    private String mPani;
    private String mPcscfHostname;
    PdnController mPdnController;
    private int mPdnType;
    int mPhoneId;
    protected ImsProfile mProfile;
    boolean mRcsProfile;
    String mReason;
    String mRecoveryReason;
    ImsRegistration mReg;
    RegistrationManagerHandler mRegHandler;
    private int mRegiFailReason;
    private DiagnosisConstants.REGI_REQC mRegiRequestType;
    private int mRegistrationRat;
    RegistrationManagerInternal mRegman;
    private Message mResult;
    protected RegistrationConstants.RegisterTaskState mState;
    boolean mSuspendByIrat;
    boolean mSuspendBySnapshot;
    boolean mSuspended;
    private boolean misEpdgHandoverInProgress;

    protected RegisterTask() {
        this.mRegistrationRat = 0;
        this.mState = RegistrationConstants.RegisterTaskState.IDLE;
        this.mKeepPdn = false;
        this.mObject = null;
        this.mResult = null;
        this.mHasPendingUpdate = false;
        this.mHasForcedPendingUpdate = false;
        this.mImmediatePendingUpdate = false;
        this.mSuspended = false;
        this.mSuspendByIrat = false;
        this.mSuspendBySnapshot = false;
        this.mRcsProfile = false;
        this.mIsUpdateRegistering = false;
        this.mHasPendingRegister = false;
        this.mHasPendingDeregister = false;
        this.mIsRefreshReg = false;
        this.mReason = "";
        this.mRecoveryReason = "";
        this.mDeregiReason = 41;
        this.mNotAvailableReason = 0;
        this.mPhoneId = -1;
        this.mReg = null;
        this.mMno = Mno.DEFAULT;
        this.mContext = null;
        this.mPdnController = null;
        this.mDnsQueryRetryCount = 0;
        this.mNetworkConnected = null;
        this.misEpdgHandoverInProgress = false;
        this.mHasPendingEpdgHandover = false;
        this.mFilteredReason = new CopyOnWriteArrayList();
        this.mKeepEmergencyTask = false;
        this.mPhoneId = 0;
    }

    protected RegisterTask(int phoneId) {
        this.mRegistrationRat = 0;
        this.mState = RegistrationConstants.RegisterTaskState.IDLE;
        this.mKeepPdn = false;
        this.mObject = null;
        this.mResult = null;
        this.mHasPendingUpdate = false;
        this.mHasForcedPendingUpdate = false;
        this.mImmediatePendingUpdate = false;
        this.mSuspended = false;
        this.mSuspendByIrat = false;
        this.mSuspendBySnapshot = false;
        this.mRcsProfile = false;
        this.mIsUpdateRegistering = false;
        this.mHasPendingRegister = false;
        this.mHasPendingDeregister = false;
        this.mIsRefreshReg = false;
        this.mReason = "";
        this.mRecoveryReason = "";
        this.mDeregiReason = 41;
        this.mNotAvailableReason = 0;
        this.mPhoneId = -1;
        this.mReg = null;
        this.mMno = Mno.DEFAULT;
        this.mContext = null;
        this.mPdnController = null;
        this.mDnsQueryRetryCount = 0;
        this.mNetworkConnected = null;
        this.misEpdgHandoverInProgress = false;
        this.mHasPendingEpdgHandover = false;
        this.mFilteredReason = new CopyOnWriteArrayList();
        this.mKeepEmergencyTask = false;
        this.mPhoneId = phoneId;
    }

    public RegisterTask(ImsProfile profile, RegistrationManagerInternal regMgr, ITelephonyManager telephonyManager, PdnController pdnController, Context context, IVolteServiceModule vsm, IConfigModule cm, int phoneId) {
        RegistrationManagerInternal registrationManagerInternal = regMgr;
        this.mRegistrationRat = 0;
        this.mState = RegistrationConstants.RegisterTaskState.IDLE;
        this.mKeepPdn = false;
        this.mObject = null;
        this.mResult = null;
        this.mHasPendingUpdate = false;
        this.mHasForcedPendingUpdate = false;
        this.mImmediatePendingUpdate = false;
        this.mSuspended = false;
        this.mSuspendByIrat = false;
        this.mSuspendBySnapshot = false;
        this.mRcsProfile = false;
        this.mIsUpdateRegistering = false;
        this.mHasPendingRegister = false;
        this.mHasPendingDeregister = false;
        this.mIsRefreshReg = false;
        this.mReason = "";
        this.mRecoveryReason = "";
        this.mDeregiReason = 41;
        this.mNotAvailableReason = 0;
        this.mPhoneId = -1;
        this.mReg = null;
        this.mMno = Mno.DEFAULT;
        this.mContext = null;
        this.mPdnController = null;
        this.mDnsQueryRetryCount = 0;
        this.mNetworkConnected = null;
        this.misEpdgHandoverInProgress = false;
        this.mHasPendingEpdgHandover = false;
        this.mFilteredReason = new CopyOnWriteArrayList();
        this.mKeepEmergencyTask = false;
        this.mProfile = profile;
        this.mContext = context;
        this.mRegman = registrationManagerInternal;
        this.mPdnController = pdnController;
        this.mPhoneId = phoneId;
        this.mKeepEmergencyTask = false;
        if (profile.isSamsungMdmnEnabled()) {
            this.mMno = Mno.MDMN;
        } else {
            this.mMno = Mno.fromName(this.mProfile.getMnoName());
        }
        if (!profile.hasService("mmtel-video") && !profile.hasService("mmtel") && !profile.hasService("smsip")) {
            this.mRcsProfile = true;
        }
        this.mGovernor = RegiGovernorCreator.getInstance(this.mMno, this.mRegman, telephonyManager, this, this.mPdnController, vsm, cm, this.mContext);
        int code = DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode();
        this.mRegiFailReason = code;
        this.mLastRegiFailReason = code;
        this.mRegiRequestType = DiagnosisConstants.REGI_REQC.INITIAL;
        if (registrationManagerInternal != null) {
            this.mRegHandler = regMgr.getRegistrationManagerHandler();
        }
    }

    public void onConnected(int pdnType, Network network) {
        if (pdnType == this.mPdnType) {
            this.mNetworkConnected = network;
            Log.i(LOG_TAG, "onConnected: pdntype=" + pdnType + " network=" + network + " mPdnType=" + this.mPdnType + " profile=" + this.mProfile.getName());
            this.mRegHandler.notifyPdnConnected(this);
        }
    }

    public void onDisconnected(int pdnType, boolean isPdnUp) {
        Message message;
        if (pdnType == this.mPdnType) {
            this.mNetworkConnected = null;
            Log.i(LOG_TAG, "onDisconnected: pdntype=" + pdnType + " mPdnType=" + this.mPdnType + " profile=" + this.mProfile.getName());
            this.mRegHandler.notifyPdnDisconnected(this);
            if (this.mProfile.hasEmergencySupport() && (message = this.mResult) != null) {
                message.arg1 = -1;
                this.mResult.sendToTarget();
                this.mResult = null;
            }
        }
    }

    public void onSuspended(int networkType) {
        Log.i(LOG_TAG, "onSuspended: networkType=" + networkType + "mIsUpdateRegistering=" + this.mIsUpdateRegistering + "mSuspended=" + this.mSuspended + "mSuspendByIrat=" + this.mSuspendByIrat + "mSuspendBySnapshot=" + this.mSuspendBySnapshot);
        if (!this.mSuspended) {
            if (this.mIsUpdateRegistering) {
                this.mHasPendingRegister = true;
            }
            this.mSuspended = true;
            if (!this.mSuspendByIrat && !this.mSuspendBySnapshot) {
                this.mRegHandler.sendSuspend(this, true);
            }
        }
    }

    public void onResumed(int networkType) {
        Log.i(LOG_TAG, "onResumed: networkType=" + networkType + "mIsUpdateRegistering=" + this.mIsUpdateRegistering + "mHasPendingRegister=" + this.mHasPendingRegister + "mSuspended=" + this.mSuspended + "mSuspendByIrat=" + this.mSuspendByIrat + "mSuspendBySnapshot=" + this.mSuspendBySnapshot);
        if (this.mSuspended) {
            if (this.mIsUpdateRegistering && this.mHasPendingRegister) {
                this.mHasPendingRegister = false;
                this.mHasForcedPendingUpdate = true;
                this.mRegman.doPendingUpdateRegistration();
            }
            this.mSuspended = false;
            if (!this.mSuspendByIrat && !this.mSuspendBySnapshot) {
                this.mRegHandler.sendSuspend(this, false);
            }
        }
    }

    public void onSuspendedBySnapshot(int networkType) {
        Log.i(LOG_TAG, "onSuspendedBySnapshot: networkType=" + networkType + "mSuspended=" + this.mSuspended + "mSuspendByIrat=" + this.mSuspendByIrat + "mSuspendBySnapshot=" + this.mSuspendBySnapshot);
        if (!this.mSuspendBySnapshot) {
            this.mSuspendBySnapshot = true;
            if (!this.mSuspended && !this.mSuspendByIrat) {
                this.mRegHandler.sendSuspend(this, true);
            }
        }
    }

    public void onResumedBySnapshot(int networkType) {
        Log.i(LOG_TAG, "onResumedBySnapshot: networkType=" + networkType + "mSuspended=" + this.mSuspended + "mSuspendByIrat=" + this.mSuspendByIrat + "mSuspendBySnapshot=" + this.mSuspendBySnapshot);
        if (this.mSuspendBySnapshot) {
            this.mSuspendBySnapshot = false;
            if (!this.mSuspended && !this.mSuspendByIrat) {
                this.mRegHandler.sendSuspend(this, false);
            }
        }
    }

    public void onPcscfAddressChanged(int networkType, List<String> pcscf) {
        List<String> ret;
        Log.i(LOG_TAG, "onPcscfAddressChanged: networkType=" + networkType + " mPdnType=" + this.mPdnType + " mno=" + this.mMno + " profile=" + this.mProfile.getName() + " Pcscf Preference=" + this.mProfile.getPcscfPreference());
        if (networkType == this.mPdnType) {
            if ((this.mMno != Mno.CMCC || this.mProfile.getPcscfPreference() == 0) && (ret = this.mGovernor.checkValidPcscfIp(pcscf)) != null && !ret.isEmpty()) {
                this.mGovernor.updatePcscfIpList(ret);
            }
        }
    }

    public void onLocalIpChanged(int networkType, boolean isStackedIpChanged) {
        Log.i(LOG_TAG, "onLocalIpChanged: networkType=" + networkType + " isStackedIpChanged=" + isStackedIpChanged + " mPdnType=" + this.mPdnType + " profile=" + this.mProfile.getName());
        if (networkType == this.mPdnType) {
            this.mRegHandler.notifyLocalIpChanged(this, isStackedIpChanged);
        }
    }

    public String toString() {
        return "RegisterTask[" + this.mPhoneId + "][mProfile=" + this.mProfile.getName() + ", mRegistrationRat=" + this.mRegistrationRat + ", mPdnType=" + this.mPdnType + ", mState=" + this.mState + ", mObject=" + this.mObject + ", mReason=" + this.mReason + ", mPcscfHostname=" + this.mPcscfHostname + ", mDeregiReason=" + this.mDeregiReason + "]";
    }

    public void suspendByIrat() {
        Log.i(LOG_TAG, "suspendByIrat:mSuspended=" + this.mSuspended + "mSuspendByIrat=" + this.mSuspendByIrat + "mSuspendBySnapshot=" + this.mSuspendBySnapshot);
        if (!this.mSuspendByIrat) {
            this.mSuspendByIrat = true;
            if (!this.mSuspended && !this.mSuspendBySnapshot) {
                this.mRegHandler.sendSuspend(this, true);
            }
        }
    }

    public void resumeByIrat() {
        Log.i(LOG_TAG, "resumeByIrat:mSuspended=" + this.mSuspended + "mSuspendByIrat=" + this.mSuspendByIrat + "mSuspendBySnapshot=" + this.mSuspendBySnapshot);
        if (this.mSuspendByIrat) {
            this.mSuspendByIrat = false;
            if (!this.mSuspended && !this.mSuspendBySnapshot) {
                this.mRegHandler.sendSuspend(this, false);
            }
        }
    }

    public void setProfile(ImsProfile profile) {
        this.mProfile = profile;
    }

    public void setDeregiCause(int cause) {
        if (cause != 1) {
            if (cause == 42) {
                setDeregiReason(25);
                return;
            } else if (cause == 124) {
                setDeregiReason(27);
                return;
            } else if (cause == 143) {
                setDeregiReason(9);
                return;
            } else if (cause == 802) {
                setDeregiReason(50);
                return;
            } else if (cause == 807) {
                setDeregiReason(51);
                return;
            } else if (!(cause == 3 || cause == 4)) {
                if (cause == 5) {
                    setDeregiReason(31);
                    return;
                } else if (cause == 6) {
                    setDeregiReason(52);
                    return;
                } else if (cause == 12) {
                    setDeregiReason(23);
                    return;
                } else if (cause != 13) {
                    setDeregiReason(21);
                    return;
                } else {
                    setDeregiReason(26);
                    return;
                }
            }
        }
        setDeregiReason(24);
    }

    public void addFilteredReason(String service, String reason) {
        List<String> list = this.mFilteredReason;
        list.add(service + ":" + reason);
    }

    public void clearFilteredReason() {
        this.mFilteredReason.clear();
    }

    public boolean isRcsOnly() {
        return this.mRcsProfile;
    }

    public boolean isOneOf(RegistrationConstants.RegisterTaskState... states) {
        for (RegistrationConstants.RegisterTaskState state : states) {
            if (this.mState == state) {
                return true;
            }
        }
        return false;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public void setPdnType(int pdn) {
        this.mPdnType = pdn;
    }

    public int getPdnType() {
        return this.mPdnType;
    }

    public ImsProfile getProfile() {
        return this.mProfile;
    }

    public Mno getMno() {
        return this.mMno;
    }

    public RegistrationConstants.RegisterTaskState getState() {
        return this.mState;
    }

    public IRegistrationGovernor getGovernor() {
        return this.mGovernor;
    }

    public IUserAgent getUserAgent() {
        return (UserAgent) this.mObject;
    }

    public ImsRegistration getImsRegistration() {
        return this.mReg;
    }

    public void setState(RegistrationConstants.RegisterTaskState state) {
        this.mState = state;
    }

    public boolean isSuspended() {
        Log.i(LOG_TAG, "isSuspended: mSuspended(" + this.mSuspended + ") mSuspendByIrat(" + this.mSuspendByIrat + ")");
        return this.mSuspended || this.mSuspendByIrat || this.mSuspendBySnapshot;
    }

    public void setRegistrationRat(int rat) {
        this.mRegistrationRat = rat;
    }

    public int getRegistrationRat() {
        return this.mRegistrationRat;
    }

    public void clearSuspended() {
        this.mSuspended = false;
    }

    public void clearSuspendedBySnapshot() {
        this.mSuspendBySnapshot = false;
    }

    public int getRegiFailReason() {
        return this.mRegiFailReason;
    }

    public void clearUserAgent() {
        this.mObject = null;
    }

    public String getReason() {
        return this.mReason;
    }

    public String getPcscfHostname() {
        return this.mPcscfHostname;
    }

    public int getLastRegiFailReason() {
        return this.mLastRegiFailReason;
    }

    public void setRegiFailReason(int regiFailReason) {
        this.mRegiFailReason = regiFailReason;
    }

    public void setKeepPdn(boolean keepPdn) {
        this.mKeepPdn = keepPdn;
    }

    public boolean isKeepPdn() {
        return this.mKeepPdn;
    }

    public boolean isNeedOmadmConfig() {
        return this.mProfile.getNeedOmadmConfig();
    }

    public void setDeregiReason(int reason) {
        this.mDeregiReason = reason;
    }

    public int getDeregiReason() {
        return this.mDeregiReason;
    }

    public void setReason(String reason) {
        this.mReason = reason;
    }

    public void setIsRefreshReg(boolean isRefreshReg) {
        this.mIsRefreshReg = isRefreshReg;
    }

    public boolean isRefreshReg() {
        return this.mIsRefreshReg;
    }

    public void setPcscfHostname(String hostname) {
        this.mPcscfHostname = hostname;
    }

    public Network getNetworkConnected() {
        return this.mNetworkConnected;
    }

    public void setHasPendingDeregister(boolean pendingDeregister) {
        this.mHasPendingDeregister = pendingDeregister;
    }

    public boolean hasPendingDeregister() {
        return this.mHasPendingDeregister;
    }

    public int getDeregiCause(SipError error) {
        int phoneId = getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "transportErrorCode: reason : " + this.mDeregiReason + ", error " + error);
        int errorCode = error.getCode();
        int i = this.mDeregiReason;
        if (i < 1 || i > 20) {
            int i2 = this.mDeregiReason;
            if (i2 < 21 || i2 > 40) {
                int i3 = this.mDeregiReason;
                if (i3 < 41 || i3 > 70) {
                    int i4 = this.mDeregiReason;
                    if (i4 >= 71 && i4 <= 80) {
                        errorCode = 32;
                    }
                } else {
                    errorCode = 16;
                }
            } else {
                errorCode = 2;
            }
        } else {
            errorCode = 1;
        }
        if (this.mMno == Mno.TMOUS) {
            if (this.mGovernor.getWFCSubscribeForbiddenCounter() > 0) {
                return 2409;
            }
        } else if (this.mMno == Mno.SPRINT && SipErrorBase.FORBIDDEN.equals(error) && !SipErrorBase.isImsForbiddenError(error)) {
            return 1403;
        }
        return errorCode;
    }

    public DiagnosisConstants.REGI_REQC getRegiRequestType() {
        return this.mRegiRequestType;
    }

    public void setLastRegiFailReason(int lastRegiFailReason) {
        this.mLastRegiFailReason = lastRegiFailReason;
    }

    public List<String> getFilteredReason() {
        return this.mFilteredReason;
    }

    public void setUpdateRegistering(boolean updateRegistering) {
        this.mIsUpdateRegistering = updateRegistering;
    }

    public boolean isUpdateRegistering() {
        return this.mIsUpdateRegistering;
    }

    public void setPendingUpdate(boolean pendingUpdate) {
        this.mHasPendingUpdate = pendingUpdate;
    }

    public boolean hasPendingUpdate() {
        return this.mHasPendingUpdate;
    }

    public void setHasForcedPendingUpdate(boolean forcedPendingUpdate) {
        this.mHasForcedPendingUpdate = forcedPendingUpdate;
    }

    public boolean hasForcedPendingUpdate() {
        return this.mHasForcedPendingUpdate;
    }

    public void setHasPendingEpdgHandover(boolean pendingEpdgHandover) {
        this.mHasPendingEpdgHandover = pendingEpdgHandover;
    }

    public boolean hasPendingEpdgHandover() {
        return this.mHasPendingEpdgHandover;
    }

    public void setImmediatePendingUpdate(boolean immediatePendingUpdate) {
        this.mImmediatePendingUpdate = immediatePendingUpdate;
    }

    public boolean isImmediatePendingUpdate() {
        return this.mImmediatePendingUpdate;
    }

    public void setUserAgent(IUserAgent ua) {
        if (ua != null) {
            this.mObject = ua;
        }
    }

    public void setRegiRequestType(DiagnosisConstants.REGI_REQC regiRequestType) {
        this.mRegiRequestType = regiRequestType;
    }

    public void setEpdgHandoverInProgress(boolean status) {
        this.misEpdgHandoverInProgress = status;
    }

    public boolean isEpdgHandoverInProgress() {
        return this.misEpdgHandoverInProgress;
    }

    public void keepEmergencyTask(boolean keep) {
        this.mKeepEmergencyTask = keep;
    }

    public boolean needKeepEmergencyTask() {
        return this.mKeepEmergencyTask;
    }

    public void setResultMessage(Message result) {
        this.mResult = result;
    }

    public Message getResultMessage() {
        return this.mResult;
    }

    public void setNotAvailableReason(int reason) {
        this.mNotAvailableReason = reason;
    }

    public int getNotAvailableReason() {
        return this.mNotAvailableReason;
    }

    public void clearNotAvailableReason() {
        this.mNotAvailableReason = 0;
    }

    public void clearUpdateRegisteringFlag() {
        this.mIsUpdateRegistering = false;
        this.mHasPendingRegister = false;
        this.mHasPendingDeregister = false;
        this.misEpdgHandoverInProgress = false;
        this.mImmediatePendingUpdate = false;
    }

    public void setDnsQueryRetryCount(int retrycount) {
        this.mDnsQueryRetryCount = retrycount;
    }

    public int getDnsQueryRetryCount() {
        return this.mDnsQueryRetryCount;
    }

    public void setRecoveryReason(String reason) {
        this.mRecoveryReason = reason;
    }

    public void setImsRegistration(ImsRegistration reg) {
        this.mReg = reg;
    }

    public void onNetworkRequestFail() {
        this.mState = RegistrationConstants.RegisterTaskState.IDLE;
    }

    public void resetTaskOnPdnDisconnected() {
        clearSuspended();
        clearSuspendedBySnapshot();
        this.mGovernor.resetPcscfList();
        this.mGovernor.resetPcoType();
    }

    public String getPani() {
        return this.mPani;
    }

    public void setPaniSet(String pani, String lastPani) {
        this.mPani = pani;
        this.mLastPani = lastPani;
    }
}
