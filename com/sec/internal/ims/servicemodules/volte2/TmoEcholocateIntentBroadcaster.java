package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Message;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.icdverification.ICDVerification;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.SignalStrengthWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent;
import com.sec.internal.interfaces.ims.core.handler.IMiscHandler;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class TmoEcholocateIntentBroadcaster extends Handler {
    private static final int EVENT_ECHOLOCATE_RECEIVED = 1;
    private static final int EVENT_ECHOLOCATE_REMOVE_CALLID_CACHE = 3;
    private static final int EVENT_ECHOLOCATE_SIP_RECEIVED = 2;
    private static final String LOG_TAG = "EcholocateBroadcaster";
    private static final int NR_STATUS_CONNECTED = 3;
    private static final Signature SIGNATURES = new Signature("308203623082024aa00302010202044df1bf45300d06092a864886f70d01010505003073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e301e170d3131303631303036353235335a170d3338313032363036353235335a3073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e30820122300d06092a864886f70d01010105000382010f003082010a0282010100c1456176d31c8989df7e0b30569da5c9b782380d3ff28fb48b4a17c8a125f40ba14862518397800f7a1030bf7cc188b9296d84af5cc5dc37752a1ca2c33d654258a3fdd29d19f2a0dd4e24b328b03bfef8c17bb8da11a25fdae10c1e1e288e3c1f47ee47617972382b0854474da1d6b526b9787d9a2f8e00600a4e436bfa790d04a0376fd7bd5c6ee78a6e522bbaa969d63667d17ca8fd90087fcc4acf2a2676d341a8e19dc46beb82bb1990710bd4101df8943ef8a3f2d7cb0bac6677ae69f9f3d25c134c08dfeb82000f44dea4164f90a65e352387fdd203c3479cfb380a2f8af5af3219a726ba9d82d72229a8d32979ce84be52006f4b71fe75011e8e2d090203010001300d06092a864886f70d01010505000382010100188d18ea72a49334736e118e766744489c7a5c47543cc35cc62a8cce35e84dfd426af3595fe55192dcb2a54c594a8d0de064dad96d72969fbc873c7a9fe7e14b11aed16c6d4bf90c1911b7d8a054c0c34c7a58c4a434d46e72f6142b654af24d461089c4633aa21cead0b154efac0aec4d68403c51bceab76c33a819857531c6a459a266f495f810417e9583d71f3f53a533f1e7013007253e9ed3466432a21977837669cff2b6b20612c055ff09b44ca15ca6830cdb289398d290852d3b0204deecbb00292194cc7533e5ae593e0d355883ea8022eb6fe5e807d6c059b3f6d6f637cd4014da425742f21b54ec37c6f55d3f0b8b6ced1cbc09376e8ea023396f");
    private static final Signature SIGNATURES_ECHO_APP = new Signature("308203623082024aa00302010202044df1bf45300d06092a864886f70d01010505003073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e301e170d3131303631303036353235335a170d3338313032363036353235335a3073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e30820122300d06092a864886f70d01010105000382010f003082010a0282010100c1456176d31c8989df7e0b30569da5c9b782380d3ff28fb48b4a17c8a125f40ba14862518397800f7a1030bf7cc188b9296d84af5cc5dc37752a1ca2c33d654258a3fdd29d19f2a0dd4e24b328b03bfef8c17bb8da11a25fdae10c1e1e288e3c1f47ee47617972382b0854474da1d6b526b9787d9a2f8e00600a4e436bfa790d04a0376fd7bd5c6ee78a6e522bbaa969d63667d17ca8fd90087fcc4acf2a2676d341a8e19dc46beb82bb1990710bd4101df8943ef8a3f2d7cb0bac6677ae69f9f3d25c134c08dfeb82000f44dea4164f90a65e352387fdd203c3479cfb380a2f8af5af3219a726ba9d82d72229a8d32979ce84be52006f4b71fe75011e8e2d090203010001300d06092a864886f70d01010505000382010100188d18ea72a49334736e118e766744489c7a5c47543cc35cc62a8cce35e84dfd426af3595fe55192dcb2a54c594a8d0de064dad96d72969fbc873c7a9fe7e14b11aed16c6d4bf90c1911b7d8a054c0c34c7a58c4a434d46e72f6142b654af24d461089c4633aa21cead0b154efac0aec4d68403c51bceab76c33a819857531c6a459a266f495f810417e9583d71f3f53a533f1e7013007253e9ed3466432a21977837669cff2b6b20612c055ff09b44ca15ca6830cdb289398d290852d3b0204deecbb00292194cc7533e5ae593e0d355883ea8022eb6fe5e807d6c059b3f6d6f637cd4014da425742f21b54ec37c6f55d3f0b8b6ced1cbc09376e8ea023396f");
    private static final Signature SPRINT_HUB_SIGNATURES = new Signature("3082036c30820254a00302010202044d23332e300d06092a864886f70d01010505003078310b3009060355040613025553310b3009060355040813024b53311630140603550407130d4f7665726c616e64205061726b310f300d060355040a1306537072696e74310b3009060355040b13024345312630240603550403131d537072696e7420416e64726f69642050726f64756374696f6e204b6579301e170d3131303130343134343831345a170d3338303532323134343831345a3078310b3009060355040613025553310b3009060355040813024b53311630140603550407130d4f7665726c616e64205061726b310f300d060355040a1306537072696e74310b3009060355040b13024345312630240603550403131d537072696e7420416e64726f69642050726f64756374696f6e204b657930820122300d06092a864886f70d01010105000382010f003082010a0282010100b3cca5f477ea6e744a61b7c19706d7976da388ea4b8598c4fbc5c31cc95abb3a7b949d5b10692d397f3d980eb7c5e305b2eac5329d485c76a2df1b530d3cffa5f4c436735449bd676eabc403e2981edfe883b296dbf89bdd655e2b8a065d68189db9763681aee66e1c0bed05defc4dbc9d749a04a4206b89cc9d6765ab726d3301fdffe21285fcffe8ba2c3069048e3435c8b73b0aeb79433e3dd5d19e35f3c618dc95103b89a562f4952543cf1221797fa3cbb224184e17fcb95c5c7474db377f106918cf84bbecb2da57c3bb2e01d4d4939dcf7e3c01288a9d3909606f99b040a62a920112a21b23602f1473966d3d3379018a2e0088e0209587ea06e084dd0203010001300d06092a864886f70d01010505000382010100766f3c7d3e9db4364856693f6acb07af7269d0524d5b6bb6072e78fd0873a102f427de9affa72d3b297c997d601d9678f6d670beaf0425653527ec327dc4817082b9afaa1ce10d3f979b5d950efe1ef5eeeecc06c0aebab6e941cc25983a6be2c724c7e2b2bbe52de9ffd10e0cb4b99f83c1680c5a5927e3752d9d5b7f30c53a93f83b17c708cb338550dc2d64b6f58f2594f6af3bef770dd4d2551818dbd8cbe6b853b9e8b611d2766dcadf57e2b2c42aa3bb7c914461686df500c0a9cc01ab3df1bc997a1c8608df7a3e335cf628682f8015ca274d10476b3b3eaa34c224301d6a92a85624a4c56473a54e56a7ae395edb012472c1b07bc84202da98433238");
    private Map<String, String> mCallIDList = new HashMap();
    private final Context mContext;
    private boolean mICDVResult = false;
    private IMiscHandler mMiscHandler = null;
    private VolteServiceModuleInternal mModule = null;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (signalStrength != null) {
                SignalStrengthWrapper unused = TmoEcholocateIntentBroadcaster.this.mSignalStrength = new SignalStrengthWrapper(signalStrength);
            } else {
                Log.i(TmoEcholocateIntentBroadcaster.LOG_TAG, "getLteSignalStrength is null");
            }
        }
    };
    private String mSalesCode = "";
    /* access modifiers changed from: private */
    public SignalStrengthWrapper mSignalStrength = null;
    private TelephonyManager mTelephonyManager;
    private PackageManager pm = null;

    private static class EchoSignallingData {
        private String networkBand;
        private String networkSignal;
        private String networkType;
        private EcholocateEvent.EcholocateSignalMessage signalMsg;
        private String time;

        EchoSignallingData(EcholocateEvent.EcholocateSignalMessage signalMsg2, String networkBand2, String networkSignal2, String networkType2, String time2) {
            this.signalMsg = signalMsg2;
            this.networkBand = networkBand2;
            this.networkSignal = networkSignal2;
            this.networkType = networkType2;
            this.time = time2;
        }

        public EcholocateEvent.EcholocateSignalMessage getSignalMsg() {
            return this.signalMsg;
        }

        public String getNetworkBand() {
            return this.networkBand;
        }

        public String getNetworkSignal() {
            return this.networkSignal;
        }

        public String getNetworkType() {
            return this.networkType;
        }

        public String getTime() {
            return this.time;
        }
    }

    public TmoEcholocateIntentBroadcaster(Context context, VolteServiceModuleInternal module) {
        this.mModule = module;
        this.mContext = context;
        this.pm = context.getPackageManager();
        this.mMiscHandler = ImsRegistry.getHandlerFactory().getMiscHandler();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
    }

    public void start() {
        this.mMiscHandler.registerForEcholocateEvent(this, 1, (Object) null);
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 256);
        }
        this.mICDVResult = checkICDVerification();
        getSalesCode();
        Log.i(LOG_TAG, "start");
    }

    public void stop() {
        this.mMiscHandler.unregisterForEcholocateEvent(this);
        this.mICDVResult = false;
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 0);
        }
        Log.i(LOG_TAG, "stop");
    }

    public void handleMessage(Message msg) {
        Log.i(LOG_TAG, "handleMessage: evt " + msg.what);
        int i = msg.what;
        if (i == 1) {
            onEcholocateEventReceived((AsyncResult) msg.obj);
        } else if (i == 2) {
            sendTmoEcholocateSignallingMSG((EchoSignallingData) msg.obj);
        } else if (i != 3) {
            Log.i(LOG_TAG, "This message is not supported");
        } else {
            String callId = (String) msg.obj;
            if (this.mCallIDList.containsKey(callId)) {
                Log.i(LOG_TAG, "Remove Call id on cache");
                if (this.mCallIDList.size() == 1) {
                    this.mCallIDList.clear();
                } else {
                    this.mCallIDList.remove(callId);
                }
            }
        }
    }

    private String getNetworkType(boolean isEpdgCall) {
        if (isEpdgCall) {
            return "WFC2";
        }
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager == null) {
            return "NA";
        }
        if (telephonyManager.getNetworkType() == 20) {
            return "SA5G";
        }
        if (this.mTelephonyManager.getServiceState() == null) {
            return "NA";
        }
        if (this.mTelephonyManager.getServiceState().getNrState() == 3) {
            return "ENDC";
        }
        return "LTE";
    }

    private void onEcholocateEventReceived(AsyncResult result) {
        if (checkSecurity()) {
            EcholocateEvent noti = (EcholocateEvent) result.result;
            if (noti.getType() == EcholocateEvent.EcholocateType.signalMsg) {
                EcholocateEvent.EcholocateSignalMessage signalMsg = noti.getSignalData();
                int sessionId = Integer.parseInt(signalMsg.getSessionid());
                boolean isEpdgCall = signalMsg.isEpdgCall();
                int phoneId = getPhoneIdFromSessionId(sessionId);
                String nwType = getNetworkType(isEpdgCall);
                sendMessageDelayed(obtainMessage(2, new EchoSignallingData(signalMsg, getLteBand(phoneId, isEpdgCall, nwType), getNwStateSignal(phoneId, isEpdgCall), nwType, getTimeStamp(0))), 5000);
            } else if (noti.getType() == EcholocateEvent.EcholocateType.rtpMsg) {
                sendTmoEcholocateRTP(noti.getRtpData());
            }
        } else {
            Log.i(LOG_TAG, "Do not broadcast. ICDV or Signature key is wrong");
        }
    }

    private void sendTmoEcholocateSignallingMSG(EchoSignallingData echoSignallingData) {
        String sdpContents;
        String str;
        String str2;
        EcholocateEvent.EcholocateSignalMessage signalMsg = echoSignallingData.getSignalMsg();
        Intent tmoEchoLocateIntentSignallingMSG = new Intent("diagandroid.phone.imsSignallingMessage");
        String sipCallId = signalMsg.getCallId();
        String peerNumber = "";
        String cseq = "CSeq: " + signalMsg.getCseq();
        String sdpContents2 = signalMsg.getContents();
        if (TextUtils.isEmpty(sdpContents2)) {
            sdpContents = "NA";
        } else {
            sdpContents = getSDPContents(sdpContents2);
        }
        tmoEchoLocateIntentSignallingMSG.putExtra("VoiceAccessNetworkStateType", echoSignallingData.getNetworkType());
        tmoEchoLocateIntentSignallingMSG.putExtra("VoiceAccessNetworkStateBand", echoSignallingData.getNetworkBand());
        tmoEchoLocateIntentSignallingMSG.putExtra("VoiceAccessNetworkStateSignal", echoSignallingData.getNetworkSignal());
        tmoEchoLocateIntentSignallingMSG.putExtra("IMSSignallingMessageCallID", sipCallId);
        tmoEchoLocateIntentSignallingMSG.putExtra("IMSSignallingCSeq", cseq);
        tmoEchoLocateIntentSignallingMSG.putExtra("IMSSignallingMessageLine1", signalMsg.getLine1());
        tmoEchoLocateIntentSignallingMSG.putExtra("IMSSignallingMessageOrigin", signalMsg.getOrigin());
        String sdpReason = "NA";
        if (isEndCall(cseq)) {
            if (!"SENT".equals(signalMsg.getOrigin()) || (!signalMsg.getLine1().contains("CANCEL") && !signalMsg.getLine1().contains("BYE"))) {
                if (TextUtils.isEmpty(signalMsg.getReason())) {
                    str = "NA";
                } else {
                    str = "Reason:" + signalMsg.getReason();
                }
                sdpReason = str;
            } else {
                if (TextUtils.isEmpty(signalMsg.getReason())) {
                    str2 = "DeviceReason:Normal";
                } else {
                    str2 = "DeviceReason:" + signalMsg.getReason();
                }
                sdpReason = str2;
            }
        }
        tmoEchoLocateIntentSignallingMSG.putExtra("IMSSignallingMessageReason", sdpReason);
        tmoEchoLocateIntentSignallingMSG.putExtra("IMSSignallingMessageSDP", sdpContents);
        tmoEchoLocateIntentSignallingMSG.putExtra("oemIntentTimestamp", echoSignallingData.getTime());
        ImsCallSession session = this.mModule.getSession(Integer.parseInt(signalMsg.getSessionid()));
        if (!(session == null || session.getCallProfile().getDialingNumber() == null)) {
            peerNumber = session.getCallProfile().getDialingNumber();
        }
        String echoAppCallIdList = Settings.System.getString(this.mContext.getContentResolver(), "echolocate_id");
        Log.i(LOG_TAG, " echoAppCallIdList [" + IMSLog.checker(echoAppCallIdList) + "] peer [" + IMSLog.checker(peerNumber) + "]");
        if (updateCallIDList(sipCallId, echoAppCallIdList, peerNumber, signalMsg)) {
            if (this.mCallIDList.get(sipCallId) != null) {
                String callNumber = this.mCallIDList.get(sipCallId).split(":")[0].trim();
                String callId = this.mCallIDList.get(sipCallId).split(":")[1].trim();
                if (isEndCall(cseq)) {
                    String str3 = sipCallId;
                    String str4 = peerNumber;
                    sendMessageDelayed(obtainMessage(3, sipCallId), 10000);
                } else {
                    String str5 = peerNumber;
                }
                tmoEchoLocateIntentSignallingMSG.putExtra("CallID", callId);
                tmoEchoLocateIntentSignallingMSG.putExtra("CallNumber", callNumber);
                this.mContext.sendBroadcast(tmoEchoLocateIntentSignallingMSG, "diagandroid.phone.receiveDetailedCallState");
                Log.i(LOG_TAG, "sendTmoEcholocateSignallingMSG :: Origin [" + signalMsg.getOrigin() + "] Cseq [" + signalMsg.getCseq() + "] Reason [" + signalMsg.getReason() + "] callId_IMS [" + signalMsg.getCallId() + "] sdpContents [" + sdpContents + "]");
                return;
            }
            Log.e(LOG_TAG, "There is no related call ID");
        }
    }

    private boolean updateCallIDList(String sipCallId, String echoAppCallIdList, String peerNumber, EcholocateEvent.EcholocateSignalMessage signalMsg) {
        if (this.mCallIDList.containsKey(sipCallId)) {
            return true;
        }
        if (!TextUtils.isEmpty(echoAppCallIdList)) {
            String[] individualCallID = echoAppCallIdList.split("\\$");
            if ("".equals(peerNumber)) {
                for (String split : individualCallID) {
                    peerNumber = split.split(":")[0].trim();
                    Log.e(LOG_TAG, "Set peerNumber[" + IMSLog.checker(peerNumber) + "] for CSFB");
                }
            }
            for (int i = 0; i < individualCallID.length; i++) {
                if ((individualCallID[i].contains(peerNumber) || individualCallID[i].contains(ImsCallUtil.removeUriPlusPrefix(peerNumber, Debug.isProductShip())) || peerNumber.contains(ImsCallUtil.removeUriPlusPrefix(individualCallID[i], Debug.isProductShip()))) && !this.mCallIDList.containsKey(sipCallId)) {
                    this.mCallIDList.put(sipCallId, individualCallID[i]);
                }
            }
            return true;
        } else if (!signalMsg.getOrigin().contains("RECEIVED") || !signalMsg.getLine1().contains("INVITE")) {
            Log.e(LOG_TAG, "Callid is null");
            return false;
        } else {
            String id = String.valueOf(System.currentTimeMillis()).substring(4, 12);
            Log.i(LOG_TAG, "makeCallID id :" + id);
            this.mCallIDList.put(sipCallId, peerNumber + ":" + id);
            return true;
        }
    }

    private boolean isEndCall(String cseqName) {
        if (cseqName.contains("CANCEL") || cseqName.contains("BYE")) {
            return true;
        }
        return false;
    }

    private String getSDPContents(String lawSdp) {
        StringBuffer resultBuffer = new StringBuffer("");
        String[] sdpParse = lawSdp.split("\r\n");
        resultBuffer.append(";");
        for (int i = 0; i < sdpParse.length; i++) {
            if (sdpParse[i].contains("c=") || sdpParse[i].contains("a=rtpmap") || sdpParse[i].contains("a=recvonly") || sdpParse[i].contains("a=sendonly") || sdpParse[i].contains("a=sendrecv")) {
                resultBuffer.append("\"");
                resultBuffer.append(sdpParse[i]);
                resultBuffer.append("\"");
                resultBuffer.append(";");
            }
        }
        return resultBuffer.toString();
    }

    private void sendTmoEcholocateRTP(EcholocateEvent.EcholocateRtpMessage rtpMsg) {
        Intent tmoEchoLocateIntentRTP;
        String callNumber;
        if (TextUtils.isEmpty(rtpMsg.getId())) {
            Log.i(LOG_TAG, "sendTmoEcholocateRTP :: Session Id is NULL");
            return;
        }
        String dir = rtpMsg.getDir();
        if ("DL".equals(dir)) {
            Intent tmoEchoLocateIntentRTP2 = new Intent("diagandroid.phone.RTPDLStat");
            tmoEchoLocateIntentRTP2.putExtra("RTPDownlinkStatusLossRate", rtpMsg.getLossrate());
            tmoEchoLocateIntentRTP2.putExtra("RTPDownlinkStatusDelay", rtpMsg.getDelay());
            tmoEchoLocateIntentRTP2.putExtra("RTPDownlinkStatusJitter", rtpMsg.getJitter());
            tmoEchoLocateIntentRTP2.putExtra("RTPDownlinkStatusMeasuredPeriod", rtpMsg.getMeasuredperiod());
            tmoEchoLocateIntentRTP = tmoEchoLocateIntentRTP2;
        } else {
            Intent tmoEchoLocateIntentRTP3 = new Intent("diagandroid.phone.RTPULStat");
            tmoEchoLocateIntentRTP3.putExtra("RTPUplinkStatusLossRate", rtpMsg.getLossrate());
            tmoEchoLocateIntentRTP3.putExtra("RTPUplinkStatusDelay", rtpMsg.getDelay());
            tmoEchoLocateIntentRTP3.putExtra("RTPUplinkStatusJitter", rtpMsg.getJitter());
            tmoEchoLocateIntentRTP3.putExtra("RTPUplinkStatusMeasuredPeriod", rtpMsg.getMeasuredperiod());
            tmoEchoLocateIntentRTP = tmoEchoLocateIntentRTP3;
        }
        int sessionId = Integer.parseInt(rtpMsg.getId());
        int phoneId = getPhoneIdFromSessionId(sessionId);
        ImsCallSession session = this.mModule.getSession(sessionId);
        if (session == null) {
            Log.e(LOG_TAG, "Can't get call num from sessionID");
            return;
        }
        boolean isEpdgCall = session.isEpdgCall();
        String nwType = getNetworkType(isEpdgCall);
        tmoEchoLocateIntentRTP.putExtra("VoiceAccessNetworkStateType", nwType);
        tmoEchoLocateIntentRTP.putExtra("VoiceAccessNetworkStateSignal", getNwStateSignal(phoneId, isEpdgCall));
        tmoEchoLocateIntentRTP.putExtra("VoiceAccessNetworkStateBand", getLteBand(phoneId, isEpdgCall, nwType));
        String callNumber2 = session.getCallProfile().getDialingNumber();
        if (TextUtils.isEmpty(callNumber2)) {
            callNumber = "null";
        } else {
            callNumber = callNumber2;
        }
        String echoAppCallIdListForRTP = Settings.System.getString(this.mContext.getContentResolver(), "echolocate_id");
        if (TextUtils.isEmpty(echoAppCallIdListForRTP)) {
            Log.e(LOG_TAG, "EchoAppCallIdListForRTP is empty");
            return;
        }
        String[] individualCallIDForRTP = echoAppCallIdListForRTP.split("\\$");
        String rtpCallId = null;
        int i = 0;
        while (i < individualCallIDForRTP.length) {
            StringBuilder sb = new StringBuilder();
            String echoAppCallIdListForRTP2 = echoAppCallIdListForRTP;
            sb.append("individualCallIDForRTP[");
            sb.append(i);
            sb.append("] : [");
            sb.append(individualCallIDForRTP[i]);
            sb.append("]");
            Log.v(LOG_TAG, sb.toString());
            if (individualCallIDForRTP[i].contains(callNumber)) {
                try {
                    rtpCallId = individualCallIDForRTP[i].split(":")[1].trim();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
            }
            i++;
            echoAppCallIdListForRTP = echoAppCallIdListForRTP2;
        }
        if (TextUtils.isEmpty(rtpCallId)) {
            Log.e(LOG_TAG, "Can't find CallId from cache");
            return;
        }
        tmoEchoLocateIntentRTP.putExtra("CallNumber", callNumber);
        tmoEchoLocateIntentRTP.putExtra("CallID", rtpCallId);
        tmoEchoLocateIntentRTP.putExtra("oemIntentTimestamp", getTimeStamp(0));
        this.mContext.sendBroadcast(tmoEchoLocateIntentRTP, "diagandroid.phone.receiveDetailedCallState");
        Log.i(LOG_TAG, "sendTmoEcholocateRTP :: dir [" + dir + "] LossRate [" + rtpMsg.getLossrate() + "] Jitter [" + rtpMsg.getJitter() + "] Measuredperiod [" + rtpMsg.getMeasuredperiod() + "] Delay [" + rtpMsg.getDelay() + "]");
    }

    private void getSalesCode() {
        try {
            this.mSalesCode = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Problem getting sales code!");
        }
        if (this.mSalesCode == null) {
            this.mSalesCode = "";
        }
        Log.d(LOG_TAG, "sales_code : " + this.mSalesCode);
    }

    private boolean checkICDVerification() {
        boolean isLite = false;
        PackageManager mPackageManager = this.mContext.getPackageManager();
        if (mPackageManager != null) {
            isLite = mPackageManager.hasSystemFeature("com.samsung.feature.samsung_experience_mobile_lite");
        }
        if (isLite) {
            return "1".equals(SemSystemProperties.get("ro.boot.flash.locked", "0"));
        }
        new ICDVerification();
        int ret = ICDVerification.check();
        if (ret == 1) {
            Log.i(LOG_TAG, "ICDV OK");
            return true;
        }
        Log.e(LOG_TAG, "ICDV return error : " + ret);
        return false;
    }

    private boolean checkSecurity() {
        String str = this.mSalesCode;
        if (str == null || !str.equalsIgnoreCase("TMB")) {
            String str2 = this.mSalesCode;
            if (str2 == null || !str2.equalsIgnoreCase("SPR")) {
                return false;
            }
            if (!this.mICDVResult || !checkPackageSprintHubSignatureKey()) {
                return false;
            }
            return true;
        } else if (!this.mICDVResult || (!checkPackageSignatureKey() && !checkEchoAppSignatureKey())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkPackageSignatureKey() {
        try {
            Signature[] signatures = this.pm.getPackageInfo(ImsConstants.Packages.PACKAGE_MY_TMOBILE, 64).signatures;
            if (signatures == null) {
                return false;
            }
            for (Signature equals : signatures) {
                if (equals.equals(SIGNATURES)) {
                    return true;
                }
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(LOG_TAG, "Package is not installed");
            return false;
        }
    }

    private boolean checkEchoAppSignatureKey() {
        boolean sign = false;
        try {
            Signature[] signatures = this.pm.getPackageInfo("com.tmobile.echolocate", 64).signatures;
            if (signatures != null) {
                for (Signature signature : signatures) {
                    int i = 0;
                    while (true) {
                        if (i >= signatures.length) {
                            break;
                        } else if (signatures[i].equals(SIGNATURES_ECHO_APP)) {
                            sign = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
            }
            return sign;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, "Package is not installed");
            return false;
        }
    }

    private boolean checkPackageSprintHubSignatureKey() {
        boolean sign = false;
        try {
            Signature[] signatures = this.pm.getPackageInfo("com.sprint.ms.smf.services", 64).signatures;
            if (signatures != null) {
                for (Signature signature : signatures) {
                    int i = 0;
                    while (true) {
                        if (i >= signatures.length) {
                            break;
                        } else if (signatures[i].equals(SPRINT_HUB_SIGNATURES)) {
                            sign = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
            }
            return sign;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, "Package is not installed");
            return false;
        }
    }

    private String getLteBand(int phoneId, boolean isEpdgCall, String nwType) {
        String strband = String.valueOf(SemSystemProperties.getInt("ril.lteband" + phoneId, 0));
        if (TextUtils.isEmpty(strband) || isEpdgCall || TextUtils.equals(strband, "255")) {
            strband = "NA";
        }
        if (nwType != null && nwType.equals("SA5G")) {
            strband = "n" + strband;
        }
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (!(telephonyManager == null || telephonyManager.getServiceState() == null || !this.mTelephonyManager.getServiceState().isUsingCarrierAggregation())) {
            Log.e(LOG_TAG, "isUsingCarrierAggregation()");
            strband = String.valueOf(SemSystemProperties.getInt("ril.ltescellbands" + phoneId, 0));
            Log.e(LOG_TAG, "isUsingCarrierAggregation() strband= " + strband + " nwType== " + nwType);
        }
        Log.e(LOG_TAG, "strband= " + strband);
        return strband;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x01ea  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0200  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0255  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getNwStateSignal(int r24, boolean r25) {
        /*
            r23 = this;
            r0 = r23
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "ril.signal.param"
            r1.append(r2)
            r2 = r24
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r1 = android.os.SemSystemProperties.get(r1)
            java.lang.StringBuffer r3 = new java.lang.StringBuffer
            java.lang.String r4 = ""
            r3.<init>(r4)
            java.lang.String r5 = "NA"
            java.lang.String r6 = "NA"
            java.lang.String r7 = "NA"
            java.lang.String r8 = "NA"
            java.lang.String r9 = "NA"
            r10 = 13
            android.telephony.TelephonyManager r11 = r0.mTelephonyManager
            if (r11 == 0) goto L_0x0035
            int r10 = r11.getNetworkType()
        L_0x0035:
            com.sec.internal.helper.os.SignalStrengthWrapper r11 = r0.mSignalStrength
            if (r11 == 0) goto L_0x0041
            int r11 = r11.getDbm(r10)
            java.lang.String r8 = java.lang.Integer.toString(r11)
        L_0x0041:
            java.lang.String r11 = "NA"
            android.content.Context r12 = r0.mContext
            java.lang.String r13 = "wifi"
            java.lang.Object r12 = r12.getSystemService(r13)
            android.net.wifi.WifiManager r12 = (android.net.wifi.WifiManager) r12
            r13 = 3
            r14 = 1
            java.lang.String r15 = ";"
            if (r25 == 0) goto L_0x0085
            if (r12 == 0) goto L_0x006d
            android.net.wifi.WifiInfo r16 = r12.getConnectionInfo()
            if (r16 == 0) goto L_0x0069
            android.net.wifi.WifiInfo r17 = r12.getConnectionInfo()
            int r17 = r17.getRssi()
            java.lang.String r17 = java.lang.Integer.toString(r17)
            goto L_0x006b
        L_0x0069:
            r17 = r4
        L_0x006b:
            r11 = r17
        L_0x006d:
            android.content.Context r2 = r0.mContext
            int r2 = com.sec.internal.constants.ims.VowifiConfig.getPrefMode(r2, r14)
            if (r2 != r13) goto L_0x0085
            r3.append(r15)
            r3.append(r11)
            java.lang.String r2 = ";NA;NA;NA;NA;NA;NA;"
            r3.append(r2)
            java.lang.String r2 = r3.toString()
            return r2
        L_0x0085:
            boolean r2 = android.text.TextUtils.isEmpty(r8)
            java.lang.String r14 = "]"
            java.lang.String r13 = "EcholocateBroadcaster"
            r18 = r5
            java.lang.String r5 = ";NA;"
            if (r2 == 0) goto L_0x00a6
            r3.append(r15)
            r3.append(r11)
            r3.append(r5)
            r19 = r6
            r20 = r7
            r21 = r8
            r22 = r10
            goto L_0x0178
        L_0x00a6:
            com.sec.internal.helper.os.SignalStrengthWrapper r2 = r0.mSignalStrength
            if (r2 == 0) goto L_0x0170
            int r2 = r2.getNrLevel()
            r19 = r6
            com.sec.internal.helper.os.SignalStrengthWrapper r6 = r0.mSignalStrength
            int r6 = r6.getInvalidSignalStrength()
            r20 = r7
            java.lang.String r7 = " "
            if (r2 == r6) goto L_0x0103
            com.sec.internal.helper.os.SignalStrengthWrapper r2 = r0.mSignalStrength
            int r2 = r2.getNrSsRsrp()
            java.lang.String r2 = java.lang.Integer.toString(r2)
            com.sec.internal.helper.os.SignalStrengthWrapper r6 = r0.mSignalStrength
            int r6 = r6.getNrSsRsrq()
            java.lang.String r6 = java.lang.Integer.toString(r6)
            r21 = r8
            com.sec.internal.helper.os.SignalStrengthWrapper r8 = r0.mSignalStrength
            int r8 = r8.getNrSsSinr()
            java.lang.String r8 = java.lang.Integer.toString(r8)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r22 = r10
            java.lang.String r10 = "Nr [ rsrp: rsrq: sinr: "
            r9.append(r10)
            r9.append(r2)
            r9.append(r7)
            r9.append(r6)
            r9.append(r7)
            r9.append(r8)
            r9.append(r14)
            java.lang.String r7 = r9.toString()
            android.util.Log.d(r13, r7)
            r9 = r8
            goto L_0x0138
        L_0x0103:
            r21 = r8
            r22 = r10
            com.sec.internal.helper.os.SignalStrengthWrapper r2 = r0.mSignalStrength
            int r2 = r2.getLteRsrp()
            java.lang.String r2 = java.lang.Integer.toString(r2)
            com.sec.internal.helper.os.SignalStrengthWrapper r6 = r0.mSignalStrength
            int r6 = r6.getLteRsrq()
            java.lang.String r6 = java.lang.Integer.toString(r6)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r10 = "Default LTE [ rsrp: rsrq:"
            r8.append(r10)
            r8.append(r2)
            r8.append(r7)
            r8.append(r6)
            r8.append(r14)
            java.lang.String r7 = r8.toString()
            android.util.Log.d(r13, r7)
        L_0x0138:
            com.sec.internal.helper.os.SignalStrengthWrapper r7 = r0.mSignalStrength
            boolean r7 = r7.isGsm()
            if (r7 == 0) goto L_0x015c
            com.sec.internal.helper.os.SignalStrengthWrapper r7 = r0.mSignalStrength
            boolean r7 = r7.isValidSignal()
            if (r7 != 0) goto L_0x015c
            android.telephony.TelephonyManager r7 = r0.mTelephonyManager
            if (r7 != 0) goto L_0x014f
            r10 = 16
            goto L_0x0151
        L_0x014f:
            r10 = r22
        L_0x0151:
            com.sec.internal.helper.os.SignalStrengthWrapper r7 = r0.mSignalStrength
            int r7 = r7.getDbm(r10)
            java.lang.String r7 = java.lang.Integer.toString(r7)
            goto L_0x0160
        L_0x015c:
            r7 = r20
            r10 = r22
        L_0x0160:
            r3.append(r15)
            r3.append(r11)
            r3.append(r15)
            r3.append(r7)
            r3.append(r15)
            goto L_0x0180
        L_0x0170:
            r19 = r6
            r20 = r7
            r21 = r8
            r22 = r10
        L_0x0178:
            r2 = r18
            r6 = r19
            r7 = r20
            r10 = r22
        L_0x0180:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r18 = r7
            java.lang.String r7 = "signal["
            r8.append(r7)
            r8.append(r1)
            r8.append(r14)
            java.lang.String r7 = r8.toString()
            android.util.Log.i(r13, r7)
            java.lang.String r7 = ","
            java.lang.String[] r7 = r1.split(r7)
            int r8 = r7.length
            java.lang.String r13 = "NA"
            r14 = 3
            if (r8 < r14) goto L_0x0255
            r8 = 0
            r14 = r7[r8]
            boolean r14 = r4.equals(r14)
            java.lang.String r8 = "255"
            if (r14 != 0) goto L_0x01c0
            r17 = r1
            r14 = 0
            r1 = r7[r14]
            boolean r1 = r8.equals(r1)
            if (r1 == 0) goto L_0x01bd
            goto L_0x01c2
        L_0x01bd:
            r1 = r7[r14]
            goto L_0x01c3
        L_0x01c0:
            r17 = r1
        L_0x01c2:
            r1 = r13
        L_0x01c3:
            r3.append(r1)
            r3.append(r15)
            r3.append(r2)
            r3.append(r15)
            r3.append(r6)
            if (r9 == 0) goto L_0x0200
            boolean r1 = r9.equals(r13)
            if (r1 != 0) goto L_0x0200
            com.sec.internal.helper.os.SignalStrengthWrapper r1 = r0.mSignalStrength
            if (r1 == 0) goto L_0x0200
            int r1 = r1.getNrLevel()
            com.sec.internal.helper.os.SignalStrengthWrapper r14 = r0.mSignalStrength
            int r14 = r14.getInvalidSignalStrength()
            if (r1 == r14) goto L_0x0200
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r15)
            r1.append(r9)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            r3.append(r1)
            goto L_0x026b
        L_0x0200:
            r1 = 2
            r14 = r7[r1]
            boolean r14 = r4.equals(r14)
            if (r14 != 0) goto L_0x0214
            r14 = r7[r1]
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x0212
            goto L_0x0214
        L_0x0212:
            r13 = r7[r1]
        L_0x0214:
            r7[r1] = r13
            r13 = 1
            r14 = r7[r13]
            boolean r4 = r4.equals(r14)
            if (r4 != 0) goto L_0x023d
            r4 = r7[r13]
            boolean r4 = r8.equals(r4)
            if (r4 == 0) goto L_0x0228
            goto L_0x023d
        L_0x0228:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r15)
            r4 = r7[r13]
            r1.append(r4)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            goto L_0x0251
        L_0x023d:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r15)
            r1 = r7[r1]
            r4.append(r1)
            r4.append(r5)
            java.lang.String r1 = r4.toString()
        L_0x0251:
            r3.append(r1)
            goto L_0x026b
        L_0x0255:
            r17 = r1
            r3.append(r13)
            r3.append(r15)
            r3.append(r2)
            r3.append(r15)
            r3.append(r6)
            java.lang.String r1 = ";NA;NA;"
            r3.append(r1)
        L_0x026b:
            java.lang.String r1 = r3.toString()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.TmoEcholocateIntentBroadcaster.getNwStateSignal(int, boolean):java.lang.String");
    }

    private String getTimeStamp(int ms) {
        if (ms == 0) {
            return Long.toString(System.currentTimeMillis());
        }
        long currentTimeMillis = System.currentTimeMillis();
        return Long.toString(System.currentTimeMillis() - ((long) ms));
    }

    private int getPhoneIdFromSessionId(int sessionId) {
        int phoneId;
        if (sessionId >= 1 && (phoneId = sessionId / 10) < 2) {
            return phoneId;
        }
        return 0;
    }
}
