package com.sec.internal.constants.ims.os;

import android.telephony.CellLocation;
import com.sec.ims.extensions.ServiceStateExt;

public class NetworkState {
    private CellLocation mCellLocation;
    private boolean mDataConnectionState;
    private int mDataNetworkType = 0;
    private int mDataRegState = 1;
    private boolean mDataRoaming;
    private EmcBsIndication mEmcbsIndication;
    private boolean mEmergencyOnly;
    private boolean mEpdgAvailable;
    private boolean mInternationalRoaming;
    private boolean mIsEmergencyEpdgConnected;
    private boolean mIsEpdgConnected;
    private boolean mIsPsOnlyReg;
    private int mLastRequestedNetworkType = 0;
    private int mMobileDataNetworkType;
    private int mMobileDataRegState = 1;
    private String mOperatorNumeric;
    private boolean mPendedEPDGWeakSignal;
    private int mSimSlot;
    private int mSnapshotState = ServiceStateExt.SNAPSHOT_STATUS_DEACTIVATED;
    private int mVoiceNetworkType;
    private int mVoiceRegState = 1;
    private boolean mVoiceRoaming;
    private VoPsIndication mVopsIndication;

    public NetworkState(int simSlot) {
        this.mSimSlot = simSlot;
    }

    public int getSimSlot() {
        return this.mSimSlot;
    }

    public void setDataNetworkType(int dataNetworkType) {
        this.mDataNetworkType = dataNetworkType;
    }

    public void setMobileDataNetworkType(int mobileDataNetworkType) {
        this.mMobileDataNetworkType = mobileDataNetworkType;
    }

    public int getDataNetworkType() {
        return this.mDataNetworkType;
    }

    public int getMobileDataNetworkType() {
        return this.mMobileDataNetworkType;
    }

    public int getMobileDataRegState() {
        return this.mMobileDataRegState;
    }

    public void setMobileDataRegState(int mobileDataRegState) {
        this.mMobileDataRegState = mobileDataRegState;
    }

    public void setDataRegState(int dataRegState) {
        this.mDataRegState = dataRegState;
    }

    public int getDataRegState() {
        return this.mDataRegState;
    }

    public void setVoiceRegState(int voiceRegState) {
        this.mVoiceRegState = voiceRegState;
    }

    public int getVoiceRegState() {
        return this.mVoiceRegState;
    }

    public void setSnapshotState(int snapshotState) {
        this.mSnapshotState = snapshotState;
    }

    public int getSnapshotState() {
        return this.mSnapshotState;
    }

    public void setLastRequestedNetworkType() {
        this.mLastRequestedNetworkType = this.mDataNetworkType;
    }

    public void setLastRequestedNetworkType(int lastRequestedNetworkType) {
        this.mLastRequestedNetworkType = lastRequestedNetworkType;
    }

    public int getLastRequestedNetworkType() {
        return this.mLastRequestedNetworkType;
    }

    public void setVopsIndication(VoPsIndication vops) {
        this.mVopsIndication = vops;
    }

    public VoPsIndication getVopsIndication() {
        return this.mVopsIndication;
    }

    public void setEmcBsIndication(EmcBsIndication emc) {
        this.mEmcbsIndication = emc;
    }

    public EmcBsIndication getEmcBsIndication() {
        return this.mEmcbsIndication;
    }

    public void setCellLocation(CellLocation cl) {
        this.mCellLocation = cl;
    }

    public CellLocation getCellLocation() {
        return this.mCellLocation;
    }

    public void setDataRaoming(boolean isDataRoaming) {
        this.mDataRoaming = isDataRoaming;
    }

    public void setDataConnectionState(boolean isDataConnected) {
        this.mDataConnectionState = isDataConnected;
    }

    public boolean isDataRoaming() {
        return this.mDataRoaming;
    }

    public boolean isDataConnectedState() {
        return this.mDataConnectionState;
    }

    public void setVoiceRoaming(boolean isVoiceRoaming) {
        this.mVoiceRoaming = isVoiceRoaming;
    }

    public boolean isVoiceRoaming() {
        return this.mVoiceRoaming;
    }

    public void setOperatorNumeric(String operatorNumeric) {
        this.mOperatorNumeric = operatorNumeric;
    }

    public String getOperatorNumeric() {
        return this.mOperatorNumeric;
    }

    public void setEmergencyOnly(boolean isEmer) {
        this.mEmergencyOnly = isEmer;
    }

    public boolean isEmergencyOnly() {
        return this.mEmergencyOnly;
    }

    public void setPsOnlyReg(boolean isSet) {
        this.mIsPsOnlyReg = isSet;
    }

    public boolean isPsOnlyReg() {
        return this.mIsPsOnlyReg;
    }

    public void setEpdgConnected(boolean isSet) {
        this.mIsEpdgConnected = isSet;
    }

    public boolean isEpdgConnected() {
        return this.mIsEpdgConnected;
    }

    public void setEpdgAvailable(boolean isAvail) {
        this.mEpdgAvailable = isAvail;
    }

    public boolean isEpdgAVailable() {
        return this.mEpdgAvailable;
    }

    public void setEmergencyEpdgConnected(boolean isEmer) {
        this.mIsEmergencyEpdgConnected = isEmer;
    }

    public boolean isEmergencyEpdgConnected() {
        return this.mIsEmergencyEpdgConnected;
    }

    public void setVoiceNetworkType(int voiceNet) {
        this.mVoiceNetworkType = voiceNet;
    }

    public int getVoiceNetworkType() {
        return this.mVoiceNetworkType;
    }

    public void setPendedEpdgWeakSignal(boolean set) {
        this.mPendedEPDGWeakSignal = set;
    }

    public boolean isPendedEPDGWeakSignal() {
        return this.mPendedEPDGWeakSignal;
    }

    public void setInternationalRoaming(boolean isInternationalRoaming) {
        this.mInternationalRoaming = isInternationalRoaming;
    }

    public boolean isInternationalRoaming() {
        return this.mInternationalRoaming;
    }
}
