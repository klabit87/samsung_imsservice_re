package com.sec.internal.ims.servicemodules.volte2;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import java.util.Arrays;

public class SsacManager extends Handler {
    private static final String LOG_TAG = "SsacManager";
    private static final int UNAVAILABE_FACTOR = 100;
    boolean[] mIsAlwaysBarred;
    /* access modifiers changed from: private */
    public final VolteServiceModuleInternal mModule;
    private final IRegistrationManager mRegiMgr;
    SSACController mVideo;
    SSACController mVoice;
    boolean[] needReRegiAfterCall;

    public SsacManager(VolteServiceModuleInternal module, IRegistrationManager regiMgr, int phoneCount) {
        this.mModule = module;
        this.mRegiMgr = regiMgr;
        boolean[] zArr = new boolean[phoneCount];
        this.needReRegiAfterCall = zArr;
        this.mIsAlwaysBarred = new boolean[phoneCount];
        Arrays.fill(zArr, false);
        Arrays.fill(this.mIsAlwaysBarred, false);
        this.mVoice = new SSACController(1, this, phoneCount);
        this.mVideo = new SSACController(2, this, phoneCount);
    }

    public boolean isCallBarred(int phoneId, int callType) {
        if (ImsCallUtil.isE911Call(callType)) {
            return false;
        }
        if (ImsCallUtil.isVideoCall(callType)) {
            return this.mVideo.isCallBarred(phoneId);
        }
        return this.mVoice.isCallBarred(phoneId);
    }

    private void reRegisterBySSAC(int phoneId) {
        StringBuilder sb = new StringBuilder();
        sb.append("reRegisterBySSAC[");
        sb.append(phoneId);
        sb.append("] : updateRegistrationBySSAC (");
        sb.append(!this.mIsAlwaysBarred[phoneId]);
        sb.append(")");
        Log.i(LOG_TAG, sb.toString());
        this.mRegiMgr.updateRegistrationBySSAC(phoneId, !this.mIsAlwaysBarred[phoneId]);
    }

    public void updateSSACInfo(int phoneId, int voiceFactor, int voiceTime, int videoFactor, int videoTime) {
        Log.i(LOG_TAG, "updateSSACInfo[" + phoneId + "] : Voice(" + voiceFactor + ":" + voiceTime + ") Video(" + videoFactor + ":" + videoTime + ")");
        if (voiceFactor == 100) {
            if (videoFactor != 100) {
                this.mVideo.updateSSACInfo(phoneId, videoFactor, videoTime);
                Log.i(LOG_TAG, "Update Video SSAC Info.");
            }
            Log.i(LOG_TAG, "Voice factor 100 is unavailable value.");
            return;
        }
        ImsRegistration reg = this.mModule.getImsRegistration(phoneId);
        this.mVoice.updateSSACInfo(phoneId, voiceFactor, voiceTime);
        this.mVideo.updateSSACInfo(phoneId, videoFactor, videoTime);
        if (reg == null && voiceFactor == 0) {
            Log.i(LOG_TAG, "set regiMgr.setSSACPolicy as false.");
            this.mIsAlwaysBarred[phoneId] = true;
            this.mRegiMgr.setSSACPolicy(phoneId, false);
            return;
        }
        Mno mno = SimUtil.getSimMno(phoneId);
        if (reg != null) {
            mno = Mno.fromName(reg.getImsProfile().getMnoName());
        }
        if (mno == Mno.VZW) {
            boolean isAlwaysBarredNow = this.mVoice.isAlwaysBarred(phoneId);
            boolean[] zArr = this.mIsAlwaysBarred;
            if (zArr[phoneId] != isAlwaysBarredNow) {
                zArr[phoneId] = isAlwaysBarredNow;
                if (this.mRegiMgr.getTelephonyCallStatus(phoneId) == 0) {
                    reRegisterBySSAC(phoneId);
                    return;
                }
                Log.i(LOG_TAG, "A call is exist now. update Regi after this call terminated.");
                this.needReRegiAfterCall[phoneId] = true;
            }
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == 0) {
            int phoneId = ((Integer) msg.obj).intValue();
            boolean[] zArr = this.needReRegiAfterCall;
            if (zArr[phoneId]) {
                zArr[phoneId] = false;
                Log.i(LOG_TAG, "Call Ended. Now update Registration By SSAC.");
                reRegisterBySSAC(phoneId);
            }
        }
    }

    public static class SSACController extends Handler {
        private static final int EVT_SSAC_BARRING = 1;
        private static final int MAX_BARRING_FACTOR = 100;
        public static final boolean STATE_BARRED = true;
        public static final boolean STATE_NOT_BARRED = false;
        boolean[] mBarredState;
        int mCallType;
        String mCallTypeName;
        int[] mFactor;
        SsacManager mSSACManager;
        int[] mTime;

        public SSACController(int calltype, SsacManager manager, int phoneCount) {
            this.mCallType = calltype;
            if (calltype == 1) {
                this.mCallTypeName = "Voice Call";
            } else {
                this.mCallTypeName = "Video Call";
            }
            boolean[] zArr = new boolean[phoneCount];
            this.mBarredState = zArr;
            this.mFactor = new int[phoneCount];
            this.mTime = new int[phoneCount];
            Arrays.fill(zArr, false);
            Arrays.fill(this.mFactor, 100);
            Arrays.fill(this.mTime, 0);
            this.mSSACManager = manager;
        }

        public boolean isAlwaysBarred(int phoneId) {
            return this.mFactor[phoneId] == 0;
        }

        public boolean isCallBarred(int phoneId) {
            if (this.mBarredState[phoneId]) {
                return true;
            }
            double rand1 = Math.random();
            double rand2 = Math.random();
            Log.i(SsacManager.LOG_TAG, this.mCallTypeName + "[" + phoneId + "]: isCallBarred:rand1:[" + rand1 + "] rand2:[" + rand2 + "]");
            if (100.0d * rand1 < ((double) this.mFactor[phoneId])) {
                return false;
            }
            int barringTime = (int) (((0.6d * rand2) + 0.7d) * ((double) this.mTime[phoneId]));
            Log.i(SsacManager.LOG_TAG, this.mCallTypeName + "[" + phoneId + "]: Barred for " + barringTime + " ms");
            if (barringTime == 0) {
                return false;
            }
            this.mBarredState[phoneId] = true;
            sendMessageDelayed(obtainMessage(1, Integer.valueOf(phoneId)), (long) barringTime);
            return true;
        }

        public void updateSSACInfo(int phoneId, int factor, int time) {
            Mno mno = SimUtil.getSimMno(phoneId);
            ImsRegistration reg = this.mSSACManager.mModule.getImsRegistration(phoneId);
            if (reg != null) {
                mno = Mno.fromName(reg.getImsProfile().getMnoName());
            }
            if (factor != 100) {
                if (factor >= 0) {
                    this.mFactor[phoneId] = factor;
                    this.mTime[phoneId] = time;
                } else if ((mno == Mno.RAKUTEN_JAPAN || mno == Mno.KDDI || DeviceUtil.getGcfMode()) && hasMessages(1, Integer.valueOf(phoneId)) && factor == -1) {
                    Log.i(SsacManager.LOG_TAG, this.mCallTypeName + "[" + phoneId + "]: Ignored updateSSACInfo : f[" + factor + "], t[" + time + "]");
                    return;
                } else {
                    this.mFactor[phoneId] = 100;
                    this.mTime[phoneId] = 0;
                    this.mBarredState[phoneId] = false;
                    removeMessages(1, Integer.valueOf(phoneId));
                }
            } else if (!((mno == Mno.RAKUTEN_JAPAN || mno == Mno.KDDI) && this.mBarredState[phoneId])) {
                this.mFactor[phoneId] = 100;
                this.mTime[phoneId] = 0;
                this.mBarredState[phoneId] = false;
                removeMessages(1, Integer.valueOf(phoneId));
            }
            Log.i(SsacManager.LOG_TAG, this.mCallTypeName + " updateSSACInfo[" + phoneId + "] : f[" + this.mFactor[phoneId] + "], t[" + this.mTime[phoneId] + "]");
        }

        public void handleMessage(Message msg) {
            Log.i(SsacManager.LOG_TAG, "handleMessage: evt " + msg.what);
            if (msg.what == 1) {
                int phoneId = ((Integer) msg.obj).intValue();
                this.mBarredState[phoneId] = false;
                removeMessages(1, Integer.valueOf(phoneId));
                Mno mno = SimUtil.getSimMno(phoneId);
                ImsRegistration reg = this.mSSACManager.mModule.getImsRegistration(phoneId);
                if (reg != null) {
                    mno = Mno.fromName(reg.getImsProfile().getMnoName());
                }
                if (mno == Mno.KDDI || DeviceUtil.getGcfMode() || mno == Mno.RAKUTEN_JAPAN) {
                    this.mFactor[phoneId] = 100;
                    this.mTime[phoneId] = 0;
                }
                Log.i(SsacManager.LOG_TAG, this.mCallTypeName + "Barring Timed out");
            }
        }
    }
}
