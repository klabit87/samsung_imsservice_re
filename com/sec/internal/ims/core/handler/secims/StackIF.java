package com.sec.internal.ims.core.handler.secims;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.Dialog;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.DeviceTuple;
import com.sec.ims.presence.PersonTuple;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.XmlElement;
import com.sec.internal.constants.ims.config.RcsConfig;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Element;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Element_.Attribute;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImsBuffer;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.AlarmWakeUp;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallSendCmcInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CancelAlarm;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CdpnInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ContactActivated;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ContactUriInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DTMFDataEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DedicatedBearerEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DialogEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DnsResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DumpMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.IncomingCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ModifyCallData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ModifyVideoData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.NotifyCmcRecordEventData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.NotifyVideoEventData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReceiveSmsNotification;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReferReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReferStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegistrationAuth;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegistrationImpu;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegistrationStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RrcConnectionEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RtpLossRateNoti;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SSGetGbaKey;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SipMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SmsRpAckNotification;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SubscribeStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.TextDataEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.UpdateRouteTable;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.X509CertVerifyRequest;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XCapMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.RegiType;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAlarmWakeUp;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestDeleteTcpClientSocket;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestNtpTimeOffset;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestOpenSipDialog;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestOptionsCapExchange;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestOptionsSendCmcCheckMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestOptionsSendResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestPresencePublish;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestPresenceSubscribe;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestPresenceUnpublish;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestReceiveSmsResp;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRegistration;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRejectCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRtpStatsToStack;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendMediaEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendRelayEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendRpAckResp;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendSip;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSilentLogEnabled;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateGeolocation;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdatePani;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateRat;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateTimeInPlani;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestX509CertVerifyResult;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CallResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CloseSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CshGeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendEucResponseResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendImMessageResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendImNotiResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendMessageRevokeInternalResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendSlmResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendSmsResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SipdialogGeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.StartMediaResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.StartSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.UpdateParticipantsResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.XdmGeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple_.Status;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.log.IMSLog;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StackIF implements IStackIF {
    private static final String LOG_TAG = "StackIF";
    private static final int MAX_STACK_MSG_SIZE = 524288;
    private static volatile StackIF sInstance = null;
    private byte[] mBuffer;
    private final RegistrantList mCallStatusRegistrants = new RegistrantList();
    private final RegistrantList mCdpnInfoRegistrants = new RegistrantList();
    private final RegistrantList mCmcInfoRegistrants = new RegistrantList();
    private final RegistrantList mCmcRecordEventRegistrants = new RegistrantList();
    private final RegistrantList mConferenceUpdateRegistrants = new RegistrantList();
    private final RegistrantList mDedicatedBearerEventRegistrants = new RegistrantList();
    private final RegistrantList mDialogEventRegistrants = new RegistrantList();
    private final RegistrantList mDtmfRegistrants = new RegistrantList();
    private final RegistrantList mEcholocateRegistrants = new RegistrantList();
    private final RegistrantList mEucrRegistrants = new RegistrantList();
    private int mHandle = -1;
    private final RegistrantList mImRegistrants = new RegistrantList();
    private final RegistrantList mImdnRegistrants = new RegistrantList();
    private IImsFramework mImsFramework = null;
    private final RegistrantList mIshRegistrants = new RegistrantList();
    private MiscEventListener mMiscListener;
    private final RegistrantList mModifyCallRegistrants = new RegistrantList();
    private final RegistrantList mModifyVideoRegistrants = new RegistrantList();
    private final RegistrantList mNewIncomingCallRegistrants = new RegistrantList();
    private final RegistrantList mNewIncomingSmsRegistrants = new RegistrantList();
    private final RegistrantList mOptionsRegistrants = new RegistrantList();
    private final RegistrantList mPresenceRegistrants = new RegistrantList();
    private final RegistrantList mRawSipRegistrants = new RegistrantList();
    private final RegistrantList mReferReceivedRegistrants = new RegistrantList();
    private final RegistrantList mReferStatusRegistrants = new RegistrantList();
    private List<ImsRequest> mRequestList = new ArrayList();
    private final RegistrantList mRrcConnectionEventRegistrants = new RegistrantList();
    private final RegistrantList mRtpLossRateNotiRegistrants = new RegistrantList();
    private final RegistrantList mSIPMSGRegistrants = new RegistrantList();
    private final RegistrantList mSSEventRegistrants = new RegistrantList();
    private List<SipDebugMessage> mSipHistory = new ArrayList();
    private final RegistrantList mSlmRegistrants = new RegistrantList();
    private final RegistrantList mSmsRpAckRegistrants = new RegistrantList();
    private List<DumpRequest> mStackDumpData = new ArrayList();
    private final RegistrantList mTextRegistrants = new RegistrantList();
    private Map<Integer, StackEventListener> mUaListenerList = new HashMap();
    private Map<Integer, String> mUaRegisterResponseRawSip = new HashMap();
    private final RegistrantList mVideoEventRegistrants = new RegistrantList();
    private final RegistrantList mVshRegistrants = new RegistrantList();
    private final RegistrantList mXdmRegistrants = new RegistrantList();
    private final RegistrantList mXqMtripRegistrants = new RegistrantList();
    private AtomicInteger sNextSerial = new AtomicInteger(0);

    public interface MiscEventListener {
        void onAlarmCancelled(int i);

        void onAlarmRequested(int i, int i2);
    }

    private native void initCmc(Object obj);

    private native void initStack(Object obj);

    private native void processCommandBuffer(byte[] bArr, int i);

    StackIF() {
    }

    private void init() {
        this.mBuffer = new byte[MAX_STACK_MSG_SIZE];
        try {
            System.loadLibrary("sec-ims");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public static synchronized com.sec.internal.ims.core.handler.secims.StackIF getInstance() {
        /*
            java.lang.Class<com.sec.internal.ims.core.handler.secims.StackIF> r0 = com.sec.internal.ims.core.handler.secims.StackIF.class
            monitor-enter(r0)
            com.sec.internal.ims.core.handler.secims.StackIF r1 = sInstance     // Catch:{ all -> 0x0021 }
            if (r1 != 0) goto L_0x001d
            monitor-enter(r0)     // Catch:{ all -> 0x0021 }
            com.sec.internal.ims.core.handler.secims.StackIF r1 = sInstance     // Catch:{ all -> 0x001a }
            if (r1 != 0) goto L_0x0018
            com.sec.internal.ims.core.handler.secims.StackIF r1 = new com.sec.internal.ims.core.handler.secims.StackIF     // Catch:{ all -> 0x001a }
            r1.<init>()     // Catch:{ all -> 0x001a }
            sInstance = r1     // Catch:{ all -> 0x001a }
            com.sec.internal.ims.core.handler.secims.StackIF r1 = sInstance     // Catch:{ all -> 0x001a }
            r1.init()     // Catch:{ all -> 0x001a }
        L_0x0018:
            monitor-exit(r0)     // Catch:{ all -> 0x001a }
            goto L_0x001d
        L_0x001a:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001a }
            throw r1     // Catch:{ all -> 0x0021 }
        L_0x001d:
            com.sec.internal.ims.core.handler.secims.StackIF r1 = sInstance     // Catch:{ all -> 0x0021 }
            monitor-exit(r0)
            return r1
        L_0x0021:
            r1 = move-exception
            monitor-exit(r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.StackIF.getInstance():com.sec.internal.ims.core.handler.secims.StackIF");
    }

    public void setImsFramework(IImsFramework imsFramework) {
        this.mImsFramework = imsFramework;
    }

    public void initMediaJni(Object mediaJni) {
        initStack(mediaJni);
    }

    public void initCmcJni(Object cmcJni) {
        initCmc(cmcJni);
    }

    public void registerUaListener(int handle, StackEventListener listener) {
        Log.i(LOG_TAG, "registerUaListener Handle : " + handle);
        this.mUaListenerList.put(Integer.valueOf(handle), listener);
    }

    public void unRegisterUaListener(int handle) {
        Log.i(LOG_TAG, "unRegisterUaListener Handle : " + handle);
        this.mUaListenerList.remove(Integer.valueOf(handle));
    }

    public void registerCallStatusEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerCallStatusEvent:");
        this.mCallStatusRegistrants.addUnique(h, what, obj);
    }

    public void registerDtmfEvent(Handler h, int what, Object obj) {
        this.mDtmfRegistrants.addUnique(h, what, obj);
    }

    public void registerTextEvent(Handler h, int what, Object obj) {
        this.mTextRegistrants.addUnique(h, what, obj);
    }

    public void registerSIPMSGEvent(Handler h, int what, Object obj) {
        this.mSIPMSGRegistrants.addUnique(h, what, obj);
    }

    public void registerNewIncomingCallEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerNewIncomingCallEvent:");
        this.mNewIncomingCallRegistrants.addUnique(h, what, obj);
    }

    public void registerModifyCallEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerModifyCallEvent:");
        this.mModifyCallRegistrants.addUnique(h, what, obj);
    }

    public void registerModifyVideoEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerModifyVideoEvent:");
        this.mModifyVideoRegistrants.addUnique(h, what, obj);
    }

    public void registerVideoEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerVideoEvent:");
        this.mVideoEventRegistrants.addUnique(h, what, obj);
    }

    public void registerCmcRecordEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerCmcRecordEvent:");
        this.mCmcRecordEventRegistrants.addUnique(h, what, obj);
    }

    public void registerConferenceUpdateEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerConferenceUpdateEvent:");
        this.mConferenceUpdateRegistrants.addUnique(h, what, obj);
    }

    public void registerNewIncomingSmsEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerNewIncomingSmsEvent: ");
        this.mNewIncomingSmsRegistrants.addUnique(h, what, obj);
    }

    public void registerSmsRpAckEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerSmsRpAckEvent:");
        this.mSmsRpAckRegistrants.addUnique(h, what, obj);
    }

    public void registerReferReceivedEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerReferReceivedEvent: ");
        this.mReferReceivedRegistrants.addUnique(h, what, obj);
    }

    public void registerReferStatusEvent(Handler h, int what, Object obj) {
        this.mReferStatusRegistrants.addUnique(h, what, obj);
    }

    public void registerImHandler(Handler h, int what, Object obj) {
        this.mImRegistrants.addUnique(h, what, obj);
    }

    public void registerImdnHandler(Handler h, int what, Object obj) {
        this.mImdnRegistrants.addUnique(h, what, obj);
    }

    public void registerSlmHandler(Handler h, int what, Object obj) {
        this.mSlmRegistrants.addUnique(h, what, obj);
    }

    public void registerPresenceEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerPresenceEvent: ");
        this.mPresenceRegistrants.addUnique(h, what, obj);
    }

    public void registerOptionsHandler(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerOptionsHandler: ");
        this.mOptionsRegistrants.addUnique(h, what, obj);
    }

    public void registerDialogEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerDialogEvent: ");
        this.mDialogEventRegistrants.addUnique(h, what, obj);
    }

    public void registerCdpnInfoEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerCdpnInfoEvent:");
        this.mCdpnInfoRegistrants.addUnique(h, what, obj);
    }

    public void registerIshEvent(Handler h, int what, Object obj) {
        this.mIshRegistrants.addUnique(h, what, obj);
    }

    public void registerVshEvent(Handler h, int what, Object obj) {
        this.mVshRegistrants.addUnique(h, what, obj);
    }

    public void registerDedicatedBearerEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerDedicatedBearerEvent:");
        this.mDedicatedBearerEventRegistrants.addUnique(h, what, obj);
    }

    public void registerForRrcConnectionEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForRrcConnectionEvent:");
        this.mRrcConnectionEventRegistrants.addUnique(h, what, obj);
    }

    public void registerEcholocateEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerEcholocateEvent:");
        this.mEcholocateRegistrants.addUnique(h, what, obj);
    }

    public void registerRawSipEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerRawSipEvent: ");
        this.mRawSipRegistrants.addUnique(h, what, obj);
    }

    public void registerSSEventRegistrants(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerRawSipEvent: ");
        this.mSSEventRegistrants.addUnique(h, what, obj);
    }

    public void registerRtpLossRateNoti(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerRtpLossRate : ");
        this.mRtpLossRateNotiRegistrants.addUnique(h, what, obj);
    }

    public void registerEucrEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerEucrEvent");
        this.mEucrRegistrants.addUnique(h, what, obj);
    }

    public void registerXqMtrip(Handler h, int what, Object obj) {
        this.mXqMtripRegistrants.addUnique(h, what, obj);
    }

    public void registerCmcInfo(Handler h, int what, Object obj) {
        this.mCmcInfoRegistrants.addUnique(h, what, obj);
    }

    public void registerMiscListener(MiscEventListener listener) {
        this.mMiscListener = listener;
    }

    public void send(ResipStackRequest resipRequest) {
        sendRequest(resipRequest.mRequest, resipRequest.mOffset, resipRequest.mCallback);
    }

    private void send(ImsRequest ir) {
        synchronized (this.mRequestList) {
            this.mRequestList.add(ir);
        }
        synchronized (this.mBuffer) {
            byte[] buffer = ir.getReqBuffer().sizedByteArray();
            Log.i("SECIMSJ", serialString(ir.mTid) + "> " + buffer.length);
            processCommandBuffer(buffer, buffer.length);
        }
    }

    private int updatePaniReq(FlatBufferBuilder builder, long handle, List<String> panis) {
        int paniOffset = builder.createString((CharSequence) panis.get(0));
        int lastPaniOffset = -1;
        if (panis.size() > 1) {
            lastPaniOffset = builder.createString((CharSequence) panis.get(1));
        }
        RequestUpdatePani.startRequestUpdatePani(builder);
        RequestUpdatePani.addHandle(builder, handle);
        RequestUpdatePani.addPani(builder, paniOffset);
        if (lastPaniOffset != -1) {
            RequestUpdatePani.addLastPani(builder, lastPaniOffset);
        }
        return RequestUpdatePani.endRequestUpdatePani(builder);
    }

    private int ratReq(FlatBufferBuilder builder, long handle, long network) {
        RequestUpdateRat.startRequestUpdateRat(builder);
        RequestUpdateRat.addHandle(builder, handle);
        RequestUpdateRat.addRat(builder, network);
        return RequestUpdateRat.endRequestUpdateRat(builder);
    }

    private int planiTimeReq(FlatBufferBuilder builder, long handle, long time) {
        RequestUpdateTimeInPlani.startRequestUpdateTimeInPlani(builder);
        RequestUpdateTimeInPlani.addHandle(builder, handle);
        RequestUpdateTimeInPlani.addTime(builder, time);
        return RequestUpdateTimeInPlani.endRequestUpdateTimeInPlani(builder);
    }

    private void sendRequest(StackRequest request, Message result) {
        sendRequest(request.getBuilder(), request.getOffset(), result);
    }

    private void sendRequest(FlatBufferBuilder builder, int requestOffSet, Message result) {
        ImsBuffer.startImsBuffer(builder);
        int trId = this.sNextSerial.getAndIncrement();
        ImsBuffer.addTrid(builder, (long) trId);
        ImsBuffer.addMsgType(builder, (byte) 1);
        ImsBuffer.addMsg(builder, requestOffSet);
        builder.finish(ImsBuffer.endImsBuffer(builder));
        ImsRequest ir = ImsRequest.obtain(builder, result);
        ir.mTid = trId;
        send(ir);
    }

    public void createUA(UaProfile ua, Message result) {
        Log.i(LOG_TAG, "createUA:");
        sendRequest(RegistrationRequestBuilder.makeCreateUA(ua), result);
    }

    public void deleteUA(int handle, Message result) {
        Log.i(LOG_TAG, "deleteUA: handle " + handle);
        sendRequest(RegistrationRequestBuilder.makeDeleteUA(handle), result);
    }

    public void register(int handle, String pcscfAddr, int pcscfPort, int regExpires, List<String> serviceList, List<String> impuList, Capabilities ownCap, List<String> thirdPartyFeatureTags, String accessToken, String authServerUrl, Message result) {
        Log.i(LOG_TAG, "register: handle " + handle + " pcscfAddr " + pcscfAddr + " port " + pcscfPort + " service " + serviceList);
        this.mHandle = handle;
        sendRequest(RegistrationRequestBuilder.makeRegister(handle, pcscfAddr, pcscfPort, regExpires, serviceList, impuList, ownCap, thirdPartyFeatureTags, accessToken, authServerUrl), result);
    }

    public void networkSuspended(int handle, boolean state) {
        Log.i(LOG_TAG, "register: handle " + handle + " state " + state);
        sendRequest(RegistrationRequestBuilder.makeNetworkSuspended(handle, state), (Message) null);
    }

    public void sendAuthResponse(int handle, int tid, String response) {
        Log.i(LOG_TAG, "sendAuthResponse: handle " + handle + " tid " + tid + " response " + response);
        sendRequest(RegistrationRequestBuilder.makeSendAuthResponse(handle, tid, response), (Message) null);
    }

    public void setPreferredImpu(int handle, String impu) {
        Log.i(LOG_TAG, "setPreferredImpu: handle " + handle + " impu " + hidePrivateInfoFromSipMsg(impu));
        sendRequest(RegistrationRequestBuilder.makeSetPreferredImpu(handle, impu), (Message) null);
    }

    public void updatePani(int handle, List<String> panis) {
        Log.i(LOG_TAG, "updatePani: " + handle + " pani: " + panis);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int paniOffset = updatePaniReq(builder, (long) handle, panis);
        Request.startRequest(builder);
        Request.addReqid(builder, 600);
        Request.addReqType(builder, (byte) 68);
        Request.addReq(builder, paniOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void updateTimeInPlani(int handle, long time) {
        Log.i(LOG_TAG, "updateTimeInPlani: " + handle + " time: " + time);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int planiTimeOffset = planiTimeReq(builder, (long) handle, time);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_UPDATE_TIME_IN_PLANI);
        Request.addReqType(builder, (byte) 80);
        Request.addReq(builder, planiTimeOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void updateRat(int handle, int network) {
        Log.i(LOG_TAG, "updateRat: " + handle + " network: " + network);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int ratOffSet = ratReq(builder, (long) handle, (long) network);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_UPDATE_RAT);
        Request.addReqType(builder, (byte) 79);
        Request.addReq(builder, ratOffSet);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void updateGeolocation(int handle, LocationInfo geolocation) {
        int a3Offset;
        int a6Offset;
        int hnoOffset;
        int pcOffset;
        int locationtimeOffset;
        LocationInfo locationInfo = geolocation;
        Log.i(LOG_TAG, ": " + handle);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int latitudeOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mLatitude)) {
            latitudeOffset = builder.createString((CharSequence) locationInfo.mLatitude);
        }
        int longitudeOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mLongitude)) {
            longitudeOffset = builder.createString((CharSequence) locationInfo.mLongitude);
        }
        int altitudeOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mAltitude)) {
            altitudeOffset = builder.createString((CharSequence) locationInfo.mAltitude);
        }
        int accuracyOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mAccuracy)) {
            accuracyOffset = builder.createString((CharSequence) locationInfo.mAccuracy);
        }
        int providertypeOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mProviderType)) {
            providertypeOffset = builder.createString((CharSequence) locationInfo.mProviderType);
        }
        int retentionexpiresOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mRetentionExpires)) {
            retentionexpiresOffset = builder.createString((CharSequence) locationInfo.mRetentionExpires);
        }
        int srsnameOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mSRSName)) {
            srsnameOffset = builder.createString((CharSequence) locationInfo.mSRSName);
        }
        int radiusuomOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mRadiusUOM)) {
            radiusuomOffset = builder.createString((CharSequence) locationInfo.mRadiusUOM);
        }
        int osOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mOS)) {
            osOffset = builder.createString((CharSequence) locationInfo.mOS);
        }
        int deviceidOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mDeviceId)) {
            deviceidOffset = builder.createString((CharSequence) locationInfo.mDeviceId);
        }
        int countryOffset = -1;
        if (!TextUtils.isEmpty(locationInfo.mCountry)) {
            countryOffset = builder.createString((CharSequence) locationInfo.mCountry);
        }
        int a1Offset = -1;
        if (!TextUtils.isEmpty(locationInfo.mA1)) {
            a1Offset = builder.createString((CharSequence) locationInfo.mA1);
        }
        if (!TextUtils.isEmpty(locationInfo.mA3)) {
            a3Offset = builder.createString((CharSequence) locationInfo.mA3);
        } else {
            a3Offset = -1;
        }
        if (!TextUtils.isEmpty(locationInfo.mA6)) {
            a6Offset = builder.createString((CharSequence) locationInfo.mA6);
        } else {
            a6Offset = -1;
        }
        int latitudeOffset2 = latitudeOffset;
        if (!TextUtils.isEmpty(locationInfo.mHNO)) {
            hnoOffset = builder.createString((CharSequence) locationInfo.mHNO);
        } else {
            hnoOffset = -1;
        }
        int longitudeOffset2 = longitudeOffset;
        if (!TextUtils.isEmpty(locationInfo.mPC)) {
            pcOffset = builder.createString((CharSequence) locationInfo.mPC);
        } else {
            pcOffset = -1;
        }
        int altitudeOffset2 = altitudeOffset;
        if (!TextUtils.isEmpty(locationInfo.mLocationTime)) {
            locationtimeOffset = builder.createString((CharSequence) locationInfo.mLocationTime);
        } else {
            locationtimeOffset = -1;
        }
        RequestUpdateGeolocation.startRequestUpdateGeolocation(builder);
        if (locationtimeOffset != -1) {
            RequestUpdateGeolocation.addLocationtime(builder, locationtimeOffset);
        }
        if (pcOffset != -1) {
            RequestUpdateGeolocation.addPc(builder, pcOffset);
        }
        if (hnoOffset != -1) {
            RequestUpdateGeolocation.addHno(builder, hnoOffset);
        }
        if (a6Offset != -1) {
            RequestUpdateGeolocation.addA6(builder, a6Offset);
        }
        if (a3Offset != -1) {
            RequestUpdateGeolocation.addA3(builder, a3Offset);
        }
        if (a1Offset != -1) {
            RequestUpdateGeolocation.addA1(builder, a1Offset);
        }
        if (countryOffset != -1) {
            RequestUpdateGeolocation.addCountry(builder, countryOffset);
        }
        if (deviceidOffset != -1) {
            RequestUpdateGeolocation.addDeviceid(builder, deviceidOffset);
        }
        if (osOffset != -1) {
            RequestUpdateGeolocation.addOs(builder, osOffset);
        }
        if (radiusuomOffset != -1) {
            RequestUpdateGeolocation.addRadiusuom(builder, radiusuomOffset);
        }
        if (srsnameOffset != -1) {
            RequestUpdateGeolocation.addSrsname(builder, srsnameOffset);
        }
        if (retentionexpiresOffset != -1) {
            RequestUpdateGeolocation.addRetentionexpires(builder, retentionexpiresOffset);
        }
        if (providertypeOffset != -1) {
            RequestUpdateGeolocation.addProvidertype(builder, providertypeOffset);
        }
        if (accuracyOffset != -1) {
            RequestUpdateGeolocation.addAccuracy(builder, accuracyOffset);
        }
        int i = a6Offset;
        int altitudeOffset3 = altitudeOffset2;
        if (altitudeOffset3 != -1) {
            RequestUpdateGeolocation.addAltitude(builder, altitudeOffset3);
        }
        int i2 = altitudeOffset3;
        int altitudeOffset4 = longitudeOffset2;
        if (altitudeOffset4 != -1) {
            RequestUpdateGeolocation.addLongitude(builder, altitudeOffset4);
        }
        int i3 = altitudeOffset4;
        int latitudeOffset3 = latitudeOffset2;
        if (latitudeOffset3 != -1) {
            RequestUpdateGeolocation.addLatitude(builder, latitudeOffset3);
        }
        int i4 = hnoOffset;
        int i5 = pcOffset;
        RequestUpdateGeolocation.addHandle(builder, (long) handle);
        int updateGeoLocationOffset = RequestUpdateGeolocation.endRequestUpdateGeolocation(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_UPDATE_GEOLOCATION);
        Request.addReqType(builder, ReqMsg.request_update_geolocation);
        Request.addReq(builder, updateGeoLocationOffset);
        int i6 = latitudeOffset3;
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void updateVceConfig(int handle, boolean config) {
        Log.i(LOG_TAG, "updateVceConfig: handle: " + handle + ", vceEnabled: " + config);
        sendRequest(RegistrationRequestBuilder.makeUpdateVceConfig(handle, config), (Message) null);
    }

    public void sendSip(int handle, String msg, Message result) {
        Log.i(LOG_TAG, "sendSip: sipMessage: " + msg);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int msgOffset = -1;
        if (!TextUtils.isEmpty(msg)) {
            msgOffset = builder.createString((CharSequence) msg);
        }
        RequestSendSip.startRequestSendSip(builder);
        if (msgOffset != -1) {
            RequestSendSip.addSipMessage(builder, msgOffset);
        }
        RequestSendSip.addHandle(builder, (long) handle);
        int sendSipOffset = RequestSendSip.endRequestSendSip(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_SIP_DIALOG_SEND_SIP);
        Request.addReqType(builder, ReqMsg.request_send_sip);
        Request.addReq(builder, sendSipOffset);
        sendRequest(builder, Request.endRequest(builder), result);
    }

    public void openSipDialog(boolean isRequired) {
        Log.i(LOG_TAG, "openSipDialog");
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestOpenSipDialog.startRequestOpenSipDialog(builder);
        RequestOpenSipDialog.addIsRequired(builder, isRequired);
        int openSipDialogOffset = RequestOpenSipDialog.endRequestOpenSipDialog(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_SIP_DIALOG_OPEN);
        Request.addReqType(builder, ReqMsg.request_open_sip_dialog);
        Request.addReq(builder, openSipDialogOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void sendDnsQuery(int handle, String intf, String hostname, List<String> dnsServers, String type, String transport, String family, long netId) {
        Log.i(LOG_TAG, "dnsQueryByNaptr: hostnames " + hostname + " dnsservers" + dnsServers + " type " + type + " transport " + transport + " family " + family + " handle " + handle);
        sendRequest(RegistrationRequestBuilder.makeSendDnsQuery(handle, intf, hostname, dnsServers, type, transport, family, netId), (Message) null);
    }

    public void requestUpdateFeatureTag(int handle, long features) {
        Log.i(LOG_TAG, "requestUpdateFeatureTag");
        sendRequest(RegistrationRequestBuilder.makeRequestUpdateFeatureTag(handle, features), (Message) null);
    }

    private int createDeviceTuplesOffset(FlatBufferBuilder builder, DeviceTuple deviceTuple) {
        Log.i(LOG_TAG, "createDevicetupleoffset enter");
        int deviceCapsOffset = -1;
        int descriptionsOffset = -1;
        int notesOffset = -1;
        int index = 0;
        if (deviceTuple.mDeviceCapabilities != null) {
            int[] deviceCapsOffsets = new int[deviceTuple.mDeviceCapabilities.size()];
            for (XmlElement cap : XmlDataStructureWrapper.getDeviceCapabilityElements(deviceTuple.mDeviceCapabilities)) {
                deviceCapsOffsets[index] = getElementBuilderDfs(builder, cap);
                index++;
            }
            deviceCapsOffset = com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.createDeviceCapabilitiesVector(builder, deviceCapsOffsets);
        }
        if (deviceTuple.mDescriptions != null) {
            int index2 = 0;
            int[] descriptions = new int[deviceTuple.mDescriptions.size()];
            for (XmlElement descr : XmlDataStructureWrapper.getTextElements("description", deviceTuple.mDescriptions)) {
                descriptions[index2] = getElementBuilderDfs(builder, descr);
                index2++;
            }
            descriptionsOffset = com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.createDescriptionsVector(builder, descriptions);
        }
        if (deviceTuple.mNotes != null) {
            int index3 = 0;
            int[] notes = new int[deviceTuple.mNotes.size()];
            for (XmlElement note : XmlDataStructureWrapper.getTextElements("note", deviceTuple.mNotes)) {
                notes[index3] = getElementBuilderDfs(builder, note);
                index3++;
            }
            notesOffset = com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.createNotesVector(builder, notes);
        }
        int DeviceIdOffset = -1;
        if (deviceTuple.mDeviceId != null) {
            DeviceIdOffset = builder.createString((CharSequence) deviceTuple.mDeviceId);
        }
        int TimestampOffset = -1;
        if (deviceTuple.mTimestamp != null) {
            TimestampOffset = builder.createString((CharSequence) deviceTuple.mTimestamp);
        }
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.startDeviceTuple(builder);
        if (DeviceIdOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.addDeviceId(builder, DeviceIdOffset);
        }
        if (deviceCapsOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.addDeviceCapabilities(builder, deviceCapsOffset);
        }
        if (descriptionsOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.addDescriptions(builder, descriptionsOffset);
        }
        if (notesOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.addNotes(builder, notesOffset);
        }
        if (TimestampOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.addTimestamp(builder, TimestampOffset);
        }
        return com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple.endDeviceTuple(builder);
    }

    private int createServiceTuplesOffset(FlatBufferBuilder builder, ServiceTuple serviceTuple) {
        Log.i(LOG_TAG, "createServiceTupleOffset enter");
        int mediaCapsOffset = -1;
        int index = 0;
        if (serviceTuple.mediaCapabilities != null) {
            List<XmlElement> capList = XmlDataStructureWrapper.getMediaCapabilityElements(serviceTuple.feature);
            int[] mediaCapsOffsets = new int[capList.size()];
            for (XmlElement cap : capList) {
                mediaCapsOffsets[index] = getElementBuilderDfs(builder, cap);
                index++;
            }
            mediaCapsOffset = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.createMediaCapabilitiesVector(builder, mediaCapsOffsets);
        }
        int serviceIdOffset = -1;
        if (serviceTuple.serviceId != null) {
            serviceIdOffset = builder.createString((CharSequence) serviceTuple.serviceId);
        }
        int tupleIdOffset = -1;
        if (serviceTuple.tupleId != null) {
            tupleIdOffset = builder.createString((CharSequence) serviceTuple.tupleId);
        }
        int versionOffset = -1;
        if (serviceTuple.version != null) {
            versionOffset = builder.createString((CharSequence) serviceTuple.version);
        }
        int descriptionOffset = -1;
        if (serviceTuple.description != null) {
            descriptionOffset = builder.createString((CharSequence) serviceTuple.description);
        }
        int statusOffset = -1;
        if (serviceTuple.basicStatus != null) {
            int basicStatusOffset = builder.createString((CharSequence) serviceTuple.basicStatus);
            Status.startStatus(builder);
            Status.addBasic(builder, basicStatusOffset);
            statusOffset = Status.endStatus(builder);
        }
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.startServiceTuple(builder);
        if (serviceIdOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.addServiceId(builder, serviceIdOffset);
        }
        if (tupleIdOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.addTupleId(builder, tupleIdOffset);
        }
        if (versionOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.addVersion(builder, versionOffset);
        }
        if (descriptionOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.addDescription(builder, descriptionOffset);
        }
        if (statusOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.addStatus(builder, statusOffset);
            if (mediaCapsOffset != -1) {
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.addMediaCapabilities(builder, mediaCapsOffset);
            }
            return com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple.endServiceTuple(builder);
        }
        Log.e(LOG_TAG, "requestPublish: service tuple status (either basic or other status) is required");
        return -1;
    }

    private int createPersonTuplesOffset(FlatBufferBuilder builder, PersonTuple personTuple) {
        Log.i(LOG_TAG, "createPersonTupleOffset enter");
        int index = 0;
        int notesLength = personTuple.mNotes == null ? 0 : personTuple.mNotes.size();
        int notesOffset = -1;
        if (notesLength > 0) {
            int[] noteOffset = new int[notesLength];
            for (XmlElement note : XmlDataStructureWrapper.getTextElements("note", personTuple.mNotes)) {
                noteOffset[index] = getElementBuilderDfs(builder, note);
                index++;
            }
            notesOffset = RequestPresencePublish.createNotesVector(builder, noteOffset);
        }
        int TimestampOffset = -1;
        if (personTuple.mTimestamp != null) {
            TimestampOffset = builder.createString((CharSequence) personTuple.mTimestamp);
        }
        int StatusIconOffset = -1;
        if (personTuple.mStatusIcon != null) {
            StatusIconOffset = builder.createString((CharSequence) personTuple.mStatusIcon);
        }
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.PersonTuple.startPersonTuple(builder);
        if (TimestampOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.PersonTuple.addTimestamp(builder, TimestampOffset);
        }
        if (StatusIconOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.PersonTuple.addStatusIcon(builder, StatusIconOffset);
        }
        if (notesOffset != -1) {
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.PersonTuple.addNotes(builder, notesOffset);
        }
        return com.sec.internal.ims.core.handler.secims.imsCommonStruc.PersonTuple.endPersonTuple(builder);
    }

    public void requestPublish(int handle, PresenceInfo presenceInfo, Message result) {
        Log.i(LOG_TAG, "request publish enter");
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int index = 0;
        List<DeviceTuple> deviceList = presenceInfo.getDeviceList();
        List<ServiceTuple> serviceList = presenceInfo.getServiceList();
        List<PersonTuple> personList = presenceInfo.getPersonList();
        int deviceTupleOffset = -1;
        int serviceTupleOffset = -1;
        int personTupleOffset = -1;
        if (personList.size() > 0) {
            int[] personTuples = new int[personList.size()];
            for (PersonTuple personTuple : personList) {
                personTuples[index] = createPersonTuplesOffset(builder, personTuple);
                index++;
            }
            personTupleOffset = RequestPresencePublish.createPersonTuplesVector(builder, personTuples);
        }
        int index2 = 0;
        if (deviceList.size() > 0) {
            int[] deviceTuples = new int[deviceList.size()];
            for (DeviceTuple deviceTuple : deviceList) {
                deviceTuples[index2] = createDeviceTuplesOffset(builder, deviceTuple);
                index2++;
            }
            deviceTupleOffset = RequestPresencePublish.createDeviceTuplesVector(builder, deviceTuples);
        }
        int index3 = 0;
        if (serviceList.size() > 0) {
            int[] serviceTuples = new int[serviceList.size()];
            for (ServiceTuple serviceTuple : serviceList) {
                serviceTuples[index3] = createServiceTuplesOffset(builder, serviceTuple);
                index3++;
            }
            serviceTupleOffset = RequestPresencePublish.createServiceTuplesVector(builder, serviceTuples);
        }
        int ETagOffset = -1;
        if (presenceInfo.getEtag() != null) {
            ETagOffset = builder.createString((CharSequence) presenceInfo.getEtag());
        }
        int UriOffset = -1;
        if (presenceInfo.getUri() != null) {
            UriOffset = builder.createString((CharSequence) presenceInfo.getUri());
        }
        int TimestampOffset = -1;
        if (presenceInfo.getTimestamp() > 0) {
            TimestampOffset = builder.createString((CharSequence) "" + presenceInfo.getTimestamp());
        }
        RequestPresencePublish.startRequestPresencePublish(builder);
        if (serviceTupleOffset != -1) {
            RequestPresencePublish.addServiceTuples(builder, serviceTupleOffset);
            if (presenceInfo.getEtag() != null) {
                RequestPresencePublish.addETag(builder, ETagOffset);
            }
            RequestPresencePublish.addExpireTime(builder, presenceInfo.getExpireTime());
            if (TimestampOffset != -1) {
                RequestPresencePublish.addTimestamp(builder, TimestampOffset);
            }
            if (personTupleOffset != -1) {
                RequestPresencePublish.addPersonTuples(builder, personTupleOffset);
            }
            if (deviceTupleOffset != -1) {
                RequestPresencePublish.addDeviceTuples(builder, deviceTupleOffset);
            }
            if (UriOffset != -1) {
                RequestPresencePublish.addUri(builder, UriOffset);
            }
            RequestPresencePublish.addHandle(builder, (long) handle);
            RequestPresencePublish.addGzipEnable(builder, presenceInfo.getPublishGzipEnabled());
            int requestPresencePublish = RequestPresencePublish.endRequestPresencePublish(builder);
            Request.startRequest(builder);
            Request.addReqid(builder, 701);
            Request.addReqType(builder, (byte) 62);
            Request.addReq(builder, requestPresencePublish);
            int i = index3;
            sendRequest(builder, Request.endRequest(builder), result);
            Log.i(LOG_TAG, "requestPublish: sent");
        }
    }

    public void requestOptionsReqCapabilityExchange(int handle, String uri, long myFeatures, String extFeature) {
        Log.i(LOG_TAG, "requestOptionsReqCapabilityExchange: uri: " + IMSLog.checker(uri) + " handle: " + handle);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int capexReq = requestCapabilityExchange(builder, (long) handle, uri, myFeatures, extFeature);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_OPTIONS_CAP_EXCHANGE);
        Request.addReqType(builder, (byte) 77);
        Request.addReq(builder, capexReq);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
        Log.i(LOG_TAG, "requestOptionsReqCapabilityExchange: sent");
    }

    public void requestOptionsReqSendCmcCheckMsg(int handle, String uri) {
        Log.i(LOG_TAG, "requestOptionsReqSendCmcCheckMsg: uri: " + IMSLog.checker(uri) + " handle: " + handle);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uriOffset = builder.createString((CharSequence) uri);
        RequestOptionsSendCmcCheckMsg.startRequestOptionsSendCmcCheckMsg(builder);
        RequestOptionsSendCmcCheckMsg.addHandle(builder, (long) handle);
        RequestOptionsSendCmcCheckMsg.addUri(builder, uriOffset);
        int capexReq = RequestOptionsSendCmcCheckMsg.endRequestOptionsSendCmcCheckMsg(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_OPTIONS_SEND_CMC_CHECK_MSG);
        Request.addReqType(builder, (byte) 78);
        Request.addReq(builder, capexReq);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
        Log.i(LOG_TAG, "requestOptionsReqSendCmcCheckMsg: sent");
    }

    public int sendCapexResponse(int handle, String uri, long myFeatures, String txId, int lastSeen, Message result, String extFeature) {
        int i = handle;
        String str = txId;
        Log.i(LOG_TAG, "sendCapexResponse: handle " + i);
        if (str == null) {
            return -1;
        }
        Log.i(LOG_TAG, "sendCapexResponse: uri " + IMSLog.checker(uri) + "transaction Id" + str);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uriOffset = builder.createString((CharSequence) uri);
        int txIdOffset = builder.createString((CharSequence) str);
        int extFeatureOffset = builder.createString((CharSequence) extFeature);
        int myFeaturesOffset = -1;
        int i2 = 0;
        if (myFeatures != ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
            List<Integer> featureList = StackRequestBuilderUtil.translateFeatureTag(myFeatures);
            if (featureList.size() > 0) {
                int[] featuretags = new int[featureList.size()];
                for (Integer featureTag : featureList) {
                    featuretags[i2] = featureTag.intValue();
                    i2++;
                }
                myFeaturesOffset = RequestOptionsSendResponse.createMyFeaturesVector(builder, featuretags);
            }
        }
        RequestOptionsSendResponse.startRequestOptionsSendResponse(builder);
        RequestOptionsSendResponse.addHandle(builder, (long) i);
        RequestOptionsSendResponse.addUri(builder, uriOffset);
        RequestOptionsSendResponse.addLastSeen(builder, lastSeen);
        if (myFeaturesOffset != -1) {
            RequestOptionsSendResponse.addMyFeatures(builder, myFeaturesOffset);
        }
        RequestOptionsSendResponse.addTxId(builder, txIdOffset);
        RequestOptionsSendResponse.addExtFeature(builder, extFeatureOffset);
        int requestOptionsSendResponseOffset = RequestOptionsSendResponse.endRequestOptionsSendResponse(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_OPTIONS_SEND_RESPONSE);
        Request.addReqType(builder, (byte) 76);
        Request.addReq(builder, requestOptionsSendResponseOffset);
        sendRequest(builder, Request.endRequest(builder), result);
        Log.i(LOG_TAG, "sendCapexResponse: sent");
        return 0;
    }

    private int requestCapabilityExchange(FlatBufferBuilder builder, long handle, String uri, long myFeatures, String extFeature) {
        FlatBufferBuilder flatBufferBuilder = builder;
        String str = extFeature;
        Log.i(LOG_TAG, "requestCapabilityExchange: uri: " + IMSLog.checker(uri) + " handle: " + handle + "extension iari " + str);
        int uriOffset = flatBufferBuilder.createString((CharSequence) uri);
        int myFeaturesOffset = -1;
        int i = 0;
        int extFeatureOffset = flatBufferBuilder.createString((CharSequence) str);
        if (myFeatures != ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
            List<Integer> featureList = StackRequestBuilderUtil.translateFeatureTag(myFeatures);
            if (featureList.size() > 0) {
                int[] featuretags = new int[featureList.size()];
                for (Integer featureTag : featureList) {
                    featuretags[i] = featureTag.intValue();
                    i++;
                }
                myFeaturesOffset = RequestOptionsCapExchange.createMyFeaturesVector(flatBufferBuilder, featuretags);
            }
        }
        RequestOptionsCapExchange.startRequestOptionsCapExchange(builder);
        RequestOptionsCapExchange.addHandle(builder, handle);
        RequestOptionsCapExchange.addUri(flatBufferBuilder, uriOffset);
        RequestOptionsCapExchange.addExtFeature(flatBufferBuilder, extFeatureOffset);
        if (myFeaturesOffset != -1) {
            RequestOptionsCapExchange.addMyFeatures(flatBufferBuilder, myFeaturesOffset);
        }
        Log.i(LOG_TAG, "requestCapabilityExchange request built");
        return RequestOptionsCapExchange.endRequestOptionsCapExchange(builder);
    }

    private int getElementBuilderDfs(FlatBufferBuilder builder, XmlElement element) {
        FlatBufferBuilder flatBufferBuilder = builder;
        XmlElement xmlElement = element;
        int childElementOffset = -1;
        int index = 0;
        int childElementsLength = xmlElement.mChildElements.size();
        if (childElementsLength > 0) {
            int[] attributeOffsets = new int[childElementsLength];
            for (XmlElement childElement : xmlElement.mChildElements) {
                attributeOffsets[index] = getElementBuilderDfs(flatBufferBuilder, childElement);
                index++;
            }
            childElementOffset = Element.createElementsVector(flatBufferBuilder, attributeOffsets);
        }
        int attrOffset = -1;
        if (xmlElement.mAttributes.size() > 0) {
            int[] attributeOffsets2 = new int[xmlElement.mAttributes.size()];
            int index2 = 0;
            for (XmlElement.Attribute attr : xmlElement.mAttributes) {
                int AtrNamespaceOffset = -1;
                if (attr.mNamespace != null) {
                    AtrNamespaceOffset = flatBufferBuilder.createString((CharSequence) attr.mNamespace);
                }
                int AtrNameOffset = -1;
                if (attr.mName != null) {
                    AtrNameOffset = flatBufferBuilder.createString((CharSequence) attr.mName);
                }
                int AtrValueOffset = -1;
                if (attr.mValue != null) {
                    AtrValueOffset = flatBufferBuilder.createString((CharSequence) attr.mValue);
                }
                Log.i(LOG_TAG, "element attr: ns: " + attr.mNamespace + " name: " + attr.mName + " val: " + attr.mValue);
                Attribute.startAttribute(builder);
                if (AtrNamespaceOffset != -1) {
                    Attribute.addNameSpace(flatBufferBuilder, AtrNamespaceOffset);
                }
                if (AtrNameOffset != -1) {
                    Attribute.addName(flatBufferBuilder, AtrNameOffset);
                }
                if (AtrValueOffset != -1) {
                    Attribute.addValue(flatBufferBuilder, AtrValueOffset);
                }
                attributeOffsets2[index2] = Attribute.endAttribute(builder);
                index2++;
            }
            attrOffset = Element.createAttributesVector(flatBufferBuilder, attributeOffsets2);
        }
        int EleNamespaceOffset = -1;
        int EleNameOffset = -1;
        int EleValueOffset = -1;
        if (xmlElement.mNamespace != null) {
            EleNamespaceOffset = flatBufferBuilder.createString((CharSequence) xmlElement.mNamespace);
        }
        if (xmlElement.mName != null) {
            EleNameOffset = flatBufferBuilder.createString((CharSequence) xmlElement.mName);
        }
        if (xmlElement.mValue != null) {
            EleValueOffset = flatBufferBuilder.createString((CharSequence) xmlElement.mValue);
        }
        Element.startElement(builder);
        if (EleNamespaceOffset != -1) {
            Element.addNameSpace(flatBufferBuilder, EleNamespaceOffset);
        }
        if (EleNameOffset != -1) {
            Element.addName(flatBufferBuilder, EleNameOffset);
        }
        if (EleValueOffset != -1) {
            Element.addValue(flatBufferBuilder, EleValueOffset);
        }
        if (attrOffset != -1) {
            Element.addAttributes(flatBufferBuilder, attrOffset);
        }
        if (childElementOffset != -1) {
            Element.addElements(flatBufferBuilder, childElementOffset);
        }
        return Element.endElement(builder);
    }

    public void requestSubscribe(int handle, ImsUri uri, boolean isAnonymousFetch, String subscriptionId, Message result) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(uri);
        requestSubscribe(handle, arrayList, isAnonymousFetch, false, subscriptionId, false, 0, result);
    }

    public void requestSubscribeList(int handle, List<ImsUri> uriList, boolean isAnonymousFetch, String subscriptionId, boolean isGzipEnabled, int expiry, Message result) {
        requestSubscribe(handle, uriList, isAnonymousFetch, true, subscriptionId, isGzipEnabled, expiry, result);
    }

    private void requestSubscribe(int handle, List<ImsUri> uriList, boolean isAnonymousFetch, boolean isListSubscribe, String subscriptionId, boolean isGzipEnabled, int expiry, Message result) {
        String str = subscriptionId;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uriListOffset = 0;
        int urisSize = uriList.size();
        if (urisSize > 0) {
            int[] uriOffsets = new int[urisSize];
            int index = 0;
            for (ImsUri uri : uriList) {
                uriOffsets[index] = builder.createString((CharSequence) uri.toString());
                index++;
            }
            uriListOffset = RequestPresenceSubscribe.createUriVector(builder, uriOffsets);
        }
        int subscriptionIdOffset = -1;
        if (str != null) {
            subscriptionIdOffset = builder.createString((CharSequence) str);
        }
        RequestPresenceSubscribe.startRequestPresenceSubscribe(builder);
        RequestPresenceSubscribe.addHandle(builder, (long) handle);
        RequestPresenceSubscribe.addIsAnonymous(builder, isAnonymousFetch);
        RequestPresenceSubscribe.addIsListSubscribe(builder, isListSubscribe);
        if (subscriptionIdOffset != -1) {
            RequestPresenceSubscribe.addSubscriptionId(builder, subscriptionIdOffset);
        }
        RequestPresenceSubscribe.addGzipEnable(builder, isGzipEnabled);
        if (urisSize > 0) {
            RequestPresenceSubscribe.addUri(builder, uriListOffset);
        }
        RequestPresenceSubscribe.addExpires(builder, (long) expiry);
        int requestPresenceSubscribe = RequestPresenceSubscribe.endRequestPresenceSubscribe(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_PRESENCE_SUBSCRIBE);
        Request.addReqType(builder, (byte) 64);
        Request.addReq(builder, requestPresenceSubscribe);
        sendRequest(builder, Request.endRequest(builder), result);
        Log.i(LOG_TAG, "requestSubscribe: sent");
    }

    public void requestUnpublish(int handle) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestPresenceUnpublish.startRequestPresenceUnpublish(builder);
        RequestPresenceUnpublish.addHandle(builder, (long) handle);
        int requestPresenceUnpublish = RequestPresenceUnpublish.endRequestPresenceUnpublish(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_PRESENCE_UNPUBLISH);
        Request.addReqType(builder, (byte) 63);
        Request.addReq(builder, requestPresenceUnpublish);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
        Log.i(LOG_TAG, "requestUnpublish: sent");
    }

    public void makeCall(int handle, String destUri, String origUri, int type, String dispName, String dialedNumber, String ecscf, int port, AdditionalContents ac, String cli, String pEmergencyInfoOfAtt, HashMap<String, String> additionalSipHeaders, String alertInfo, boolean isLteEpsOnlyAttached, List<String> p2p, int cmcBoundSessionId, Bundle composerData, String replaceCallId, Message result) {
        StringBuilder sb = new StringBuilder();
        sb.append("makeCall: handle ");
        int i = handle;
        sb.append(handle);
        sb.append(" destUri ");
        sb.append(IMSLog.checker(destUri));
        sb.append(" origUri ");
        sb.append(IMSLog.checker(origUri));
        sb.append(" type ");
        sb.append(type);
        sb.append(" dispName ");
        sb.append(dispName);
        sb.append(" ecscf ");
        sb.append(ecscf);
        sb.append(" cli ");
        sb.append(cli);
        sb.append(" PEmergencyInfoOfAtt ");
        sb.append(pEmergencyInfoOfAtt);
        sb.append(" alertInfo ");
        sb.append(alertInfo);
        sb.append(" isLteEpsOnlyAttached ");
        sb.append(isLteEpsOnlyAttached);
        sb.append(" p2p ");
        sb.append(p2p != null ? p2p.toString() : "null");
        sb.append(" cmcBoundSessionId ");
        sb.append(cmcBoundSessionId);
        sb.append(" replaceCallId ");
        sb.append(replaceCallId);
        Log.i(LOG_TAG, sb.toString());
        sendRequest(CallRequestBuilder.makeMakeCall(handle, destUri, origUri, type, dispName, dialedNumber, ecscf, port, ac, cli, pEmergencyInfoOfAtt, additionalSipHeaders, alertInfo, isLteEpsOnlyAttached, p2p, cmcBoundSessionId, composerData, replaceCallId), result);
    }

    public void deregister(int handle, boolean local, Message result) {
        Log.i(LOG_TAG, "deregister: handle " + handle);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestRegistration.startRequestRegistration(builder);
        RequestRegistration.addHandle(builder, (long) handle);
        RequestRegistration.addPcscfPort(builder, 0);
        RequestRegistration.addRegExp(builder, 0);
        RequestRegistration.addIsExplicitDeregi(builder, !local);
        int regReqOffset = RequestRegistration.endRequestRegistration(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 104);
        Request.addReqType(builder, (byte) 4);
        Request.addReq(builder, regReqOffset);
        sendRequest(builder, Request.endRequest(builder), result);
    }

    public void rejectCall(int handle, int sessionId, SipError response, Message result) {
        Log.i(LOG_TAG, "rejectCall: handle " + handle + " sessionId " + sessionId + " response " + response);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int respOffset = builder.createString((CharSequence) response.getReason());
        RequestRejectCall.startRequestRejectCall(builder);
        RequestRejectCall.addSession(builder, (long) sessionId);
        RequestRejectCall.addStatusCode(builder, (long) response.getCode());
        RequestRejectCall.addReasonPhrase(builder, respOffset);
        int reqOffset = RequestRejectCall.endRequestRejectCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 214);
        Request.addReqType(builder, (byte) 21);
        Request.addReq(builder, reqOffset);
        sendRequest(builder, Request.endRequest(builder), result);
    }

    public void progressIncomingCall(int handle, int sessionId, HashMap<String, String> headers, Message result) {
        Log.i(LOG_TAG, "progressIncomingCall: handle " + handle + " sessionId " + sessionId);
        sendRequest(CallRequestBuilder.makeProgressIncomingCall(handle, sessionId, headers), result);
    }

    public void deleteTcpClientSocket(int handle) {
        Log.i(LOG_TAG, "deleteTcpClientSocket: handle " + handle);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestDeleteTcpClientSocket.startRequestDeleteTcpClientSocket(builder);
        RequestDeleteTcpClientSocket.addHandle(builder, (long) handle);
        int reqOffset = RequestDeleteTcpClientSocket.endRequestDeleteTcpClientSocket(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 1300);
        Request.addReqType(builder, (byte) 8);
        Request.addReq(builder, reqOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void endCall(int handle, int sessionId, SipReason reason, Message result) {
        Log.i(LOG_TAG, "endCall: handle " + handle + " sessionId " + sessionId + " reason " + reason);
        sendRequest(CallRequestBuilder.makeEndCall(handle, sessionId, reason), result);
    }

    public void answerCall(int handle, int sessionId, int callType, String cmcCallTime) {
        Log.i(LOG_TAG, "answerCall: handle " + handle + " sessionId " + sessionId + " cmcCallTime " + cmcCallTime);
        sendRequest(CallRequestBuilder.makeAnswerCall(handle, sessionId, callType, cmcCallTime), (Message) null);
    }

    public void holdCall(int handle, int sessionId, Message result) {
        Log.i(LOG_TAG, "holdCall: handle " + handle + " sessionId " + sessionId);
        sendRequest(CallRequestBuilder.makeHoldCall(handle, sessionId), result);
    }

    public void resumeCall(int handle, int sessionId, Message result) {
        Log.i(LOG_TAG, "resumeCall: handle " + handle + " sessionId " + sessionId);
        sendRequest(CallRequestBuilder.makeResumeCall(handle, sessionId), result);
    }

    public void holdVideo(int handle, int sessionId, Message result) {
        Log.i(LOG_TAG, "holdVideo: handle " + handle + " sessionId " + sessionId);
        sendRequest(CallRequestBuilder.makeHoldVideo(handle, sessionId), result);
    }

    public void resumeVideo(int handle, int sessionId, Message result) {
        Log.i(LOG_TAG, "resumeVideo: handle " + handle + " sessionId " + sessionId);
        sendRequest(CallRequestBuilder.makeResumeVideo(handle, sessionId), result);
    }

    public void startCamera(int handle, int sessionId, int cameraId) {
        Log.i(LOG_TAG, "startCamera: handle " + handle + ", sessionId: " + sessionId + ", cameraId: " + cameraId);
        sendRequest(CallRequestBuilder.makeStartCamera(handle, sessionId, cameraId), (Message) null);
    }

    public void stopCamera(int handle) {
        Log.i(LOG_TAG, "stopCamera: handle " + handle);
        sendRequest(CallRequestBuilder.makeStopCamera(handle), (Message) null);
    }

    public void mergeCall(int handle, int sessionId1, int sessionId2, String confUri, int callType, String eventSubscribe, String dialogType, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd, HashMap<String, String> extraHeaders, Message result) {
        StringBuilder sb = new StringBuilder();
        sb.append("mergeCall: handle ");
        int i = handle;
        sb.append(handle);
        sb.append(" session1 ");
        int i2 = sessionId1;
        sb.append(sessionId1);
        sb.append(" session2 ");
        sb.append(sessionId2);
        sb.append(" confUri ");
        sb.append(IMSLog.checker(confUri));
        sb.append(" callType ");
        sb.append(callType);
        sb.append(" eventSubscribe ");
        sb.append(eventSubscribe);
        sb.append(" dialogType ");
        sb.append(dialogType);
        sb.append(" origUri ");
        sb.append(IMSLog.checker(origUri));
        sb.append(" referUriType ");
        sb.append(referUriType);
        sb.append(" removeReferUriType ");
        sb.append(removeReferUriType);
        sb.append(" referUseAsserted ");
        sb.append(referUriAsserted);
        sb.append(" useAnonymousUpdate ");
        sb.append(useAnonymousUpdate);
        Log.i(LOG_TAG, sb.toString());
        sendRequest(CallRequestBuilder.makeMergeCall(handle, sessionId1, sessionId2, confUri, callType, eventSubscribe, dialogType, origUri, referUriType, removeReferUriType, referUriAsserted, useAnonymousUpdate, supportPrematureEnd, extraHeaders), result);
    }

    public void conference(int handle, String confuri, int callType, String eventSubscribe, String dialogType, String[] participants, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd, Message result) {
        StringBuilder sb = new StringBuilder();
        sb.append("conference: handle ");
        int i = handle;
        sb.append(handle);
        sb.append(" confUri ");
        String str = confuri;
        sb.append(confuri);
        sb.append(" subscribe ");
        String str2 = eventSubscribe;
        sb.append(eventSubscribe);
        sb.append(" dialogType ");
        String str3 = dialogType;
        sb.append(dialogType);
        sb.append(" origUri ");
        sb.append(IMSLog.checker(origUri));
        sb.append(" useAnonymousUpdate ");
        sb.append(useAnonymousUpdate);
        Log.i(LOG_TAG, sb.toString());
        Log.i(LOG_TAG, "participants: " + Arrays.toString(participants));
        sendRequest(CallRequestBuilder.makeConference(handle, confuri, callType, eventSubscribe, dialogType, participants, origUri, referUriType, removeReferUriType, referUriAsserted, useAnonymousUpdate, supportPrematureEnd), result);
    }

    public void extendToConfCall(int handle, String confuri, int callType, String eventSubscribe, String dialogType, String[] participants, int sessId, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd) {
        StringBuilder sb = new StringBuilder();
        sb.append("extendToConfCall: handle ");
        int i = handle;
        sb.append(handle);
        sb.append(" confUri ");
        sb.append(IMSLog.checker(confuri));
        sb.append(" subscribe ");
        String str = eventSubscribe;
        sb.append(eventSubscribe);
        sb.append(" dialogType ");
        String str2 = dialogType;
        sb.append(dialogType);
        sb.append(" currSession ");
        int i2 = sessId;
        sb.append(sessId);
        Log.i(LOG_TAG, sb.toString());
        Log.i(LOG_TAG, "participants: " + IMSLog.checker(Arrays.toString(participants)));
        sendRequest(CallRequestBuilder.makeExtendToConfCall(handle, confuri, callType, eventSubscribe, dialogType, participants, sessId, origUri, referUriType, removeReferUriType, referUriAsserted, useAnonymousUpdate, supportPrematureEnd), (Message) null);
    }

    public void updateConfCall(int handle, int session, int cmd, int participantId, String participant) {
        Log.i(LOG_TAG, "updateConfCall: handle " + handle + " confSession " + session + " updateCmd " + cmd + " participantID " + participantId + " " + participant);
        sendRequest(CallRequestBuilder.makeUpdateConfCall(handle, session, cmd, participantId, participant), (Message) null);
    }

    public void transferCall(int handle, int sessionId, String targetUri, int replacingSessionId, Message result) {
        sendRequest(CallRequestBuilder.makeTransferCall(handle, sessionId, targetUri, replacingSessionId), result);
    }

    public void cancelTransferCall(int handle, int sessionId, Message result) {
        sendRequest(CallRequestBuilder.makeCancelTransferCall(handle, sessionId), result);
    }

    public void pullingCall(int handle, String pullingUri, String targetUri, String origUri, Dialog targetDialog, List<String> p2p, Message result) {
        sendRequest(CallRequestBuilder.makePullingCall(handle, pullingUri, targetUri, origUri, targetDialog, p2p), result);
    }

    public void publishDialog(int handle, String origUri, String dispName, String xmlBody, int expireTime, Message result) {
        sendRequest(CallRequestBuilder.makePublishDialog(handle, origUri, dispName, xmlBody, expireTime), result);
    }

    public void acceptCallTransfer(int handle, int sessionId, boolean accept, int status, String reason, Message result) {
        Log.i(LOG_TAG, "acceptTransferCall:");
        sendRequest(CallRequestBuilder.makeAcceptCallTransfer(handle, sessionId, accept, status, reason), result);
    }

    public void handleDtmf(int handle, int sessionId, int code, int mode, int operation, Message result) {
        Log.i(LOG_TAG, "handleDtmf: sessionId " + sessionId + " code " + code + " mode " + mode + " operation " + operation);
        sendRequest(CallRequestBuilder.makeHandleDtmf(handle, sessionId, code, mode, operation), result);
    }

    public void sendText(int handle, int sessionId, String text, int len) {
        Log.i(LOG_TAG, "sendText: sessionId " + sessionId + " ,text " + text + "len " + len);
        sendRequest(CallRequestBuilder.makeSendText(handle, sessionId, text, len), (Message) null);
    }

    public void modifyCallType(int sessionId, int oldType, int newType) {
        Log.i(LOG_TAG, "modifyCallType(): sessionId " + sessionId + ", oldType " + oldType + ", newType " + newType);
        sendRequest(CallRequestBuilder.makeModifyCallType(sessionId, oldType, newType), (Message) null);
    }

    public void modifyVideoQuality(int sessionId, int oldQual, int newQual) {
        Log.i(LOG_TAG, "modifyVideoQuality(): sessionId " + sessionId + ", oldQual " + oldQual + ", newQual " + newQual);
        sendRequest(CallRequestBuilder.makeModifyVideoQuality(sessionId, oldQual, newQual), (Message) null);
    }

    public void replyModifyCallType(int sessionId, int reqType, int curType, int repType, String cmcCallTime) {
        Log.i(LOG_TAG, "replyModifyCallType(): sessionId " + sessionId + ", reqType " + reqType + ", curType " + curType + ", repType " + repType + ", cmcCallTime " + cmcCallTime);
        sendRequest(CallRequestBuilder.makeReplyModifyCallType(sessionId, reqType, curType, repType, cmcCallTime), (Message) null);
    }

    public void rejectModifyCallType(int sessionId, int reason) {
        Log.i(LOG_TAG, "rejectModifyCallType(): sessionId " + sessionId + ", reason" + reason);
        sendRequest(CallRequestBuilder.makeRejectModifyCallType(sessionId, reason), (Message) null);
    }

    public void updateCall(int sessionId, int action, int codecType, int cause, String reasonText) {
        sendRequest(CallRequestBuilder.makeUpdateCall(sessionId, action, codecType, cause, reasonText), (Message) null);
    }

    public void sendInfo(int handle, int sessionId, int callType, int ussdType, AdditionalContents ac, Message result) {
        Log.i(LOG_TAG, "sendInfo");
        sendRequest(CallRequestBuilder.makeSendInfo(handle, sessionId, callType, ussdType, ac), result);
    }

    public void sendCmcInfo(int handle, int sessionId, AdditionalContents ac) {
        Log.i(LOG_TAG, "sendCmcInfo");
        sendRequest(CallRequestBuilder.makeSendCmcInfo(handle, sessionId, ac), (Message) null);
    }

    public void updateCmcExtCallCount(int phoneId, int callCnt) {
        sendRequest(CallRequestBuilder.makeUpdateCmcExtCallCount(phoneId, callCnt), (Message) null);
    }

    public void startVideoEarlyMedia(int handle, int sessionId) {
        sendRequest(CallRequestBuilder.makeStartVideoEarlyMedia(handle, sessionId), (Message) null);
    }

    public void handleCmcCsfb(int handle, int sessionId) {
        sendRequest(CallRequestBuilder.makeHandleCmcCsfb(handle, sessionId), (Message) null);
    }

    /* access modifiers changed from: package-private */
    public void configCall(int phoneId, boolean ttySessionRequired, boolean rttSessionRequired, boolean automode) {
        IMSLog.i(LOG_TAG, phoneId, "configCall: ttySessionRequired " + ttySessionRequired + " rttSessionRequired " + rttSessionRequired + " automode " + automode);
        sendRequest(RegistrationRequestBuilder.makeConfigCall(phoneId, ttySessionRequired, rttSessionRequired, automode), (Message) null);
    }

    public void configSrvcc(int phone_id, int version) {
        Log.i("StackIF[" + phone_id + "]", "configSrvcc():  mode: " + version);
        sendRequest(RegistrationRequestBuilder.makeConfigSrvcc(phone_id, version), (Message) null);
    }

    public void updateXqEnable(int phone_id, boolean enable) {
        Log.i("StackIF[" + phone_id + "]", "updateXqEnable():  enable: " + enable);
        sendRequest(RegistrationRequestBuilder.makeUpdateXqEnable(phone_id, enable), (Message) null);
    }

    public void configRCSOff(int phoneId, String suspenduser) {
        Log.i(LOG_TAG, "configRCS: suspenduser = " + suspenduser);
        sendRequest(RegistrationRequestBuilder.makeConfigRCSOff(phoneId, suspenduser), (Message) null);
    }

    public void configRCS(int phoneId, RcsConfig config) {
        IMSLog.i(LOG_TAG, phoneId, "configRCS: " + config);
        int i = phoneId;
        sendRequest(RegistrationRequestBuilder.makeConfigRCS(i, config.getFtChunkSize(), config.getIshChunkSize(), config.getConfUri(), config.isMsrpCema(), config.getDownloadsPath(), config.isConfSubscribeEnabled(), config.getExploderUri(), config.getPagerModeLimit(), config.isUseMsrpDiscardPort(), config.isAggrImdnSupported(), config.isPrivacyDisable(), config.getCbMsgTech(), config.getEndUserConfReqId()), (Message) null);
    }

    public void updateScreenOnOff(int phoneId, int on) {
        IMSLog.i(LOG_TAG, phoneId, "updateScreenOnOff: on " + on);
        sendRequest(RegistrationRequestBuilder.makeUpdateScreenOnOff(phoneId, on), (Message) null);
    }

    public void updateServiceVersion(int phoneId, HashMap<String, String> svMap) {
        Log.i("StackIF[" + phoneId + "]", "updateServiceVersion:phoneId:" + phoneId);
        for (Map.Entry<String, String> entry : svMap.entrySet()) {
            Log.i("StackIF[" + phoneId + "]", entry.getKey() + " : " + entry.getValue());
        }
        sendRequest(RegistrationRequestBuilder.makeUpdateServiceVersion(phoneId, svMap), (Message) null);
    }

    public void configRegistration(int phoneId, String imei) {
        if (imei == null) {
            IMSLog.e(LOG_TAG, phoneId, "configRegistration: no imei");
        } else {
            sendRequest(RegistrationRequestBuilder.makeConfigRegistration(phoneId, imei), (Message) null);
        }
    }

    public void updateAudioInterface(int handle, String mode, Message result) {
        Log.i(LOG_TAG, "updateAudioInterface: handle " + handle + " mode " + mode);
        sendRequest(CallRequestBuilder.makeUpdateAudioInterface(handle, mode), result);
    }

    public void startLocalRingBackTone(int handle, int streamType, int volume, int toneType, Message result) {
        Log.i(LOG_TAG, "startLocalRingBackTone: handle " + handle + ", " + streamType + ", " + volume + ", " + toneType);
        sendRequest(CallRequestBuilder.makeStartLocalRingBackTone(handle, streamType, volume, toneType), result);
    }

    public void stopLocalRingBackTone(int handle) {
        Log.i(LOG_TAG, "stopLocalRingBackTone: handle " + handle);
        sendRequest(CallRequestBuilder.makeStopLocalRingBackTone(handle), (Message) null);
    }

    public void startRecord(int handle, int sessionId, String filePath) {
        Log.i(LOG_TAG, "startRecord: handle " + handle);
        sendRequest(CallRequestBuilder.makeStartRecord(handle, sessionId, filePath), (Message) null);
    }

    public void stopRecord(int handle, int sessionId) {
        Log.i(LOG_TAG, "stopRecord: handle " + handle);
        sendRequest(CallRequestBuilder.makeStopRecord(handle, sessionId), (Message) null);
    }

    public void clearAllCallInternal(int cmcType) {
        Log.i(LOG_TAG, "clearAllCallInternal: cmcType " + cmcType);
        sendRequest(CallRequestBuilder.makeClearAllCallInternal(cmcType), (Message) null);
    }

    public void startCmcRecord(int handle, int sessionId, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) {
        StringBuilder sb = new StringBuilder();
        sb.append("startCmcRecord: handle ");
        int i = handle;
        sb.append(handle);
        Log.i(LOG_TAG, sb.toString());
        sendRequest(CallRequestBuilder.makeStartCmcRecord(handle, sessionId, audioSource, outputFormat, maxFileSize, maxDuration, outputPath, audioEncodingBR, audioChannels, audioSamplingR, audioEncoder, durationInterval, fileSizeInterval, author), (Message) null);
    }

    public void sendSms(int handle, String scaUri, String localUri, String hexStr, String contentType, String subContentType, String callId, Message result) {
        String str = callId;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int smscOffset = builder.createString((CharSequence) scaUri);
        int localUriOffeset = builder.createString((CharSequence) localUri);
        int bodyOffset = builder.createString((CharSequence) hexStr);
        int typeOffset = builder.createString((CharSequence) contentType);
        int subTypeOffset = builder.createString((CharSequence) subContentType);
        int callIdOffset = -1;
        if (str != null) {
            callIdOffset = builder.createString((CharSequence) str);
        }
        RequestSendMsg.startRequestSendMsg(builder);
        RequestSendMsg.addHandle(builder, (long) handle);
        RequestSendMsg.addSmsc(builder, smscOffset);
        RequestSendMsg.addLocalUri(builder, localUriOffeset);
        RequestSendMsg.addContentLen(builder, hexStr.length() / 2);
        RequestSendMsg.addContentBody(builder, bodyOffset);
        RequestSendMsg.addContentType(builder, typeOffset);
        RequestSendMsg.addContentSubType(builder, subTypeOffset);
        if (callIdOffset != -1) {
            RequestSendMsg.addInReplyTo(builder, callIdOffset);
        }
        int reqOffset = RequestSendMsg.endRequestSendMsg(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 401);
        Request.addReqType(builder, (byte) 33);
        Request.addReq(builder, reqOffset);
        sendRequest(builder, Request.endRequest(builder), result);
    }

    public void sendSmsRpAckResponse(int handle, String callId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int callIdOffset = -1;
        if (callId != null) {
            callIdOffset = builder.createString((CharSequence) callId);
        }
        RequestSendRpAckResp.startRequestSendRpAckResp(builder);
        RequestSendRpAckResp.addHandle(builder, (long) handle);
        if (callIdOffset != -1) {
            RequestSendRpAckResp.addCallId(builder, callIdOffset);
        }
        int reqOffset = RequestSendRpAckResp.endRequestSendRpAckResp(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 402);
        Request.addReqType(builder, (byte) 34);
        Request.addReq(builder, reqOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void sendSmsResponse(int handle, String callId, int status) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int callIdOffset = -1;
        if (callId != null) {
            callIdOffset = builder.createString((CharSequence) callId);
        }
        RequestReceiveSmsResp.startRequestReceiveSmsResp(builder);
        RequestReceiveSmsResp.addHandle(builder, (long) handle);
        if (callIdOffset != -1) {
            RequestReceiveSmsResp.addCallId(builder, callIdOffset);
        }
        RequestReceiveSmsResp.addStatus(builder, (long) status);
        int reqOffset = RequestReceiveSmsResp.endRequestReceiveSmsResp(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 403);
        Request.addReqType(builder, (byte) 35);
        Request.addReq(builder, reqOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void sendAlarmWakeUp(int id) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestAlarmWakeUp.startRequestAlarmWakeUp(builder);
        RequestAlarmWakeUp.addId(builder, (long) id);
        int reqOffset = RequestAlarmWakeUp.endRequestAlarmWakeUp(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_ALARM_WAKE_UP);
        Request.addReqType(builder, ReqMsg.request_alarm_wake_up);
        Request.addReq(builder, reqOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void sendX509CertVerifyResponse(boolean result, String reason) {
        Log.i(LOG_TAG, "sendX509CertVerifyResponse(): result " + result + ", reason " + reason);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int reasonOffset = -1;
        if (!TextUtils.isEmpty(reason)) {
            reasonOffset = builder.createString((CharSequence) reason);
        }
        RequestX509CertVerifyResult.startRequestX509CertVerifyResult(builder);
        if (reasonOffset != -1) {
            RequestX509CertVerifyResult.addReason(builder, reasonOffset);
        }
        RequestX509CertVerifyResult.addResult(builder, result);
        int sendX509CertVerifyResponseOffset = RequestX509CertVerifyResult.endRequestX509CertVerifyResult(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_X509_CERT_VERIFY_RESULT);
        Request.addReqType(builder, ReqMsg.request_x509_cert_verify_result);
        Request.addReq(builder, sendX509CertVerifyResponseOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void sendMediaEvent(int handle, int target, int event, int eventType) {
        Log.i(LOG_TAG, "sendMediaEvent(): target " + target + ", event " + event + ", type " + eventType);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestSendMediaEvent.startRequestSendMediaEvent(builder);
        RequestSendMediaEvent.addEventType(builder, (long) eventType);
        RequestSendMediaEvent.addEvent(builder, (long) event);
        RequestSendMediaEvent.addTarget(builder, (long) target);
        RequestSendMediaEvent.addHandle(builder, (long) handle);
        int sendMediaEventOffset = RequestSendMediaEvent.endRequestSendMediaEvent(builder);
        Request.startRequest(builder);
        if (eventType == 1 && (target == 0 || target == 1)) {
            Request.addReqid(builder, 236);
        } else {
            Request.addReqid(builder, 230);
        }
        Request.addReqType(builder, (byte) 74);
        Request.addReq(builder, sendMediaEventOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void sendRelayEvent(int streamId, int event) {
        Log.i(LOG_TAG, "sendRelayEvent(): stream " + streamId + ", event " + event);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestSendRelayEvent.startRequestSendRelayEvent(builder);
        RequestSendRelayEvent.addStreamId(builder, (long) streamId);
        RequestSendRelayEvent.addEvent(builder, (long) event);
        int sendRelayEventOffset = RequestSendRelayEvent.endRequestSendRelayEvent(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_SEND_RELAY_EVENT);
        Request.addReqType(builder, (byte) 75);
        Request.addReq(builder, sendRelayEventOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void setTextMode(int phoneId, int mode) {
        IMSLog.i(LOG_TAG, phoneId, "setTextMode(): mode: " + mode);
        sendRequest(RegistrationRequestBuilder.makeSetTextMode(phoneId, mode), (Message) null);
    }

    public void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats rtpStats) {
        Log.i(LOG_TAG, "sendRtpStatsToStack()");
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestRtpStatsToStack.startRequestRtpStatsToStack(builder);
        RequestRtpStatsToStack.addDirection(builder, (long) rtpStats.mDirection);
        RequestRtpStatsToStack.addMeasuredperiod(builder, (long) rtpStats.mMeasuredPeriod);
        RequestRtpStatsToStack.addJitter(builder, (long) rtpStats.mJitter);
        RequestRtpStatsToStack.addDelay(builder, (long) rtpStats.mDelay);
        RequestRtpStatsToStack.addLossrate(builder, (long) rtpStats.mLossData);
        RequestRtpStatsToStack.addChannelid(builder, (long) rtpStats.mChannelId);
        int sendRtpStatsToStackOffset = RequestRtpStatsToStack.endRequestRtpStatsToStack(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_RTP_STATS_TO_STACK);
        Request.addReqType(builder, (byte) 101);
        Request.addReq(builder, sendRtpStatsToStackOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    private ImsRequest findAndRemoveRequest(int tid) {
        synchronized (this.mRequestList) {
            Iterator<ImsRequest> it = this.mRequestList.iterator();
            while (it.hasNext()) {
                ImsRequest ir = it.next();
                if (ir.mTid == tid) {
                    it.remove();
                    return ir;
                }
            }
            return null;
        }
    }

    private static synchronized void processMessage(byte[] buffer, int len) {
        synchronized (StackIF.class) {
            byte[] msg = new byte[len];
            synchronized (msg) {
                System.arraycopy(buffer, 0, msg, 0, len);
                ImsBuffer imsBuffer = ImsBuffer.getRootAsImsBuffer(ByteBuffer.wrap(msg));
                long tid = imsBuffer.trid();
                int messageType = imsBuffer.msgType();
                Log.i(LOG_TAG, "processMessage " + messageType);
                if (messageType == 3) {
                    Log.i(LOG_TAG, "Processing Notify");
                    getInstance().processNotify((Notify) imsBuffer.msg(new Notify()));
                } else if (messageType == 2) {
                    Log.i(LOG_TAG, "Processing Response");
                    getInstance().processResponse((int) tid, (Response) imsBuffer.msg(new Response()));
                }
            }
        }
    }

    private static void ImsLogC(int logClass, String description, boolean flush) {
        IMSLog.c(logClass, description, flush);
    }

    private void processResponse(int transactionId, Response res) {
        int reqId = res.resid();
        Object ret = null;
        Log.i("SECIMSJ", serialString(transactionId) + "< " + reqId);
        StringBuilder sb = new StringBuilder();
        sb.append("processResponse: reqId ");
        sb.append(reqId);
        Log.i(LOG_TAG, sb.toString());
        if (res.respType() == 1) {
            GeneralResponse gr = (GeneralResponse) res.resp(new GeneralResponse());
            ret = gr;
            int handle = ImsUtil.getHandle(gr.handle());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("processResponse: handle ");
            sb2.append(handle == 0 ? -1 : handle);
            sb2.append(" result ");
            sb2.append(gr.result());
            sb2.append(" reason ");
            sb2.append(gr.reason());
            Log.i(LOG_TAG, sb2.toString());
        } else if (res.respType() == 2) {
            CallResponse cr = (CallResponse) res.resp(new CallResponse());
            ret = cr;
            Log.i(LOG_TAG, "processCallResponse: handle " + ImsUtil.getHandle(cr.handle()));
        } else if (res.respType() == 4) {
            Log.i(LOG_TAG, "processSendSmsResp:");
            ret = res.resp(new SendSmsResponse());
        } else if (res.respType() == 5) {
            Log.i(LOG_TAG, "processStartSessionResp:");
            ret = res.resp(new StartSessionResponse());
        } else if (res.respType() == 6) {
            Log.i(LOG_TAG, "processCloseSessionResp:");
            ret = res.resp(new CloseSessionResponse());
        } else if (res.respType() == 7) {
            Log.i(LOG_TAG, "processStartMediaResp:");
            ret = res.resp(new StartMediaResponse());
        } else if (res.respType() == 8) {
            Log.i(LOG_TAG, "processSendImMessageResp:");
            ret = res.resp(new SendImMessageResponse());
        } else if (res.respType() == 9) {
            Log.i(LOG_TAG, "processSendImNotiResp:");
            ret = res.resp(new SendImNotiResponse());
        } else if (res.respType() == 11) {
            Log.i(LOG_TAG, "processSendSlmResponse:");
            ret = res.resp(new SendSlmResponse());
        } else if (res.respType() == 13) {
            Log.i(LOG_TAG, "processXdmGeneralResponse");
            ret = res.resp(new XdmGeneralResponse());
        } else if (res.respType() == 14) {
            Log.i(LOG_TAG, "processCshGeneralResponse");
            ret = res.resp(new CshGeneralResponse());
        } else if (res.respType() == 10) {
            Log.i(LOG_TAG, "processUpdateParticipantsResp");
            ret = res.resp(new UpdateParticipantsResponse());
        } else if (res.respType() == 12) {
            Log.i(LOG_TAG, "processSendMessageRevokeInternalResp");
            ret = res.resp(new SendMessageRevokeInternalResponse());
        } else if (res.respType() == 15) {
            Log.i(LOG_TAG, "processSendEucResponseResponse");
            ret = res.resp(new SendEucResponseResponse());
        } else if (res.respType() == 16) {
            Log.i(LOG_TAG, "processSipdialogGeneralResp");
            SipdialogGeneralResponse resp = (SipdialogGeneralResponse) res.resp(new SipdialogGeneralResponse());
            if (!resp.success() || resp.sipmessage() == null) {
                ret = null;
            } else {
                ret = resp.sipmessage();
            }
        }
        ImsRequest ir = findAndRemoveRequest(transactionId);
        if (ir != null && ir.mResult != null) {
            AsyncResult.forMessage(ir.mResult, ret, (Throwable) null);
            ir.mResult.sendToTarget();
        }
    }

    private String hidePrivateInfoFromSipMsg(String sipMsg) {
        if (!Debug.isProductShip()) {
            return sipMsg;
        }
        return sipMsg.replaceAll("sip:[#*0-9+-]*[0-9+-]+", "sip:xxxxxxxxxxxxxxx").replaceAll("tel:[#*0-9+-]*[0-9+-]+", "tel:xxxxxxxxxxxxxxx").replaceAll("imei:+[0-9+-]+", "imei:xxxxxxxx").replaceAll("username=\"+[^\"]+", "username=xxxxxxxxxxxxxxx").replaceAll("P-Access-Network-Info:+[^\n]+", "P-Access-Network-Info: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx").replaceAll("o=+[0-9:+-]+", "o=xxxxxxxxxxxxxxx").replaceAll("\"+[0-9+-]+\"", "\"xxxxxxxxxxxxxxx\"").replaceAll(">[0-9+-]{4,}<", ">xxxxxxxxxxxxxxx<").replaceAll("target>+.+</.*target", "target>xxxxxxxxxxxxxxx</target").replaceAll("From: +[#*0-9+-]*[0-9+-]+", "From: xxxxxxxxxxxxxxx").replaceAll("To: +[#*0-9+-]*[0-9+-]+", "To: xxxxxxxxxxxxxxx").replaceAll("(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}", "xxx.xxx.xxx.xxx").replaceAll("((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)", "xxxx:xxxx:xxxx:xxxx").replaceAll("session-description>.+</session-description", "session-description>xxxxxxxxxxxxxxx</session-description");
    }

    private void processNotify(Notify noti) {
        int handle;
        boolean isUseEncryptedLogger;
        String historyMessage;
        Notify notify = noti;
        int id = noti.notifyid();
        Log.i("SECIMSJ", "[UNSL]< " + id);
        Log.i(LOG_TAG, "processNotify: id " + id);
        if (id == 10001) {
            if (noti.notiType() == 1) {
                RegistrationStatus rs = (RegistrationStatus) notify.noti(new RegistrationStatus());
                int handle2 = ImsUtil.getHandle(rs.handle());
                List<String> arrayList = new ArrayList<>();
                for (int i = 0; i < rs.serviceListLength(); i++) {
                    arrayList.add(rs.serviceList(i));
                }
                ArrayList arrayList2 = new ArrayList();
                for (int i2 = 0; i2 < rs.impuListLength(); i2++) {
                    arrayList2.add(rs.impuList(i2));
                }
                Log.i(LOG_TAG, "RegistrationStatus - handle " + handle2 + " serviceList " + arrayList + " regiType " + RegiType.name(rs.regiType()) + " code " + rs.respCode() + " reason " + rs.respReason() + " ecmpMode " + rs.ecmpMode() + " retryAfter " + rs.retryAfter());
                SipError error = new SipError(rs.respCode(), rs.respReason());
                StackEventListener listener = this.mUaListenerList.get(Integer.valueOf(handle2));
                if (listener != null) {
                    int type = rs.regiType();
                    if (type == 0) {
                        ArrayList arrayList3 = arrayList;
                        int i3 = type;
                        StackEventListener stackEventListener = listener;
                        listener.onRegistered(handle2, arrayList, arrayList2, error, rs.retryAfter(), rs.ecmpMode(), this.mUaRegisterResponseRawSip.get(Integer.valueOf(handle2)));
                        return;
                    }
                    List<String> serviceList = arrayList;
                    StackEventListener listener2 = listener;
                    if (type == 1) {
                        this.mUaRegisterResponseRawSip.remove(Integer.valueOf(handle2));
                        listener2.onDeregistered(handle2, error, rs.retryAfter());
                        return;
                    }
                    return;
                }
                List<String> serviceList2 = arrayList;
                StackEventListener stackEventListener2 = listener;
                return;
            }
            Log.i(LOG_TAG, "processNotify: msg not found.");
            handle = -1;
        } else if (id == 10025) {
            Log.i(LOG_TAG, "receive registered impu");
            if (noti.notiType() == 4) {
                RegistrationImpu regImpu = (RegistrationImpu) notify.noti(new RegistrationImpu());
                int handle3 = ImsUtil.getHandle(regImpu.handle());
                String impu = regImpu.impu();
                Log.v(LOG_TAG, "Handle: " + handle3 + " - impu: " + IMSLog.checker(impu));
                StackEventListener listener3 = this.mUaListenerList.get(Integer.valueOf(handle3));
                if (listener3 != null) {
                    listener3.onRegImpuNotification(handle3, impu);
                    return;
                }
                return;
            }
            handle = -1;
        } else if (id == 10013) {
            if (noti.notiType() == 3) {
                SubscribeStatus ss = (SubscribeStatus) notify.noti(new SubscribeStatus());
                SipError error2 = new SipError((int) ss.respCode(), ss.respReason());
                int handle4 = ImsUtil.getHandle(ss.handle());
                StackEventListener listener4 = this.mUaListenerList.get(Integer.valueOf(handle4));
                if (listener4 != null) {
                    listener4.onSubscribed(handle4, error2);
                    return;
                }
                return;
            }
            handle = -1;
        } else if (id == 10007) {
            if (noti.notiType() == 9) {
                Log.i(LOG_TAG, "RegiInfoChanged");
                RegInfoChanged ri = (RegInfoChanged) notify.noti(new RegInfoChanged());
                int handle5 = ImsUtil.getHandle(ri.handle());
                StackEventListener listener5 = this.mUaListenerList.get(Integer.valueOf(handle5));
                if (listener5 != null) {
                    listener5.onRegInfoNotification(handle5, ri);
                    return;
                }
                return;
            }
            handle = -1;
        } else if (id == 10002) {
            if (noti.notiType() == 2) {
                RegistrationAuth ra = (RegistrationAuth) notify.noti(new RegistrationAuth());
                int handle6 = ImsUtil.getHandle(ra.handle());
                Log.i(LOG_TAG, "RegistrationAuth - handle " + handle6 + " nonce " + ra.nonce());
                StackEventListener listener6 = this.mUaListenerList.get(Integer.valueOf(handle6));
                if (listener6 != null) {
                    Log.i(LOG_TAG, "calling onISIMAuthRequested.");
                    listener6.onISIMAuthRequested(handle6, ra.nonce(), (int) ra.recvMng());
                    return;
                }
                Log.i(LOG_TAG, " mUaListener not found.");
                return;
            }
            Log.i(LOG_TAG, "processNotify: msg not found.");
            handle = -1;
        } else if (id == 10004) {
            if (noti.notiType() == 6) {
                CallStatus cs = (CallStatus) notify.noti(new CallStatus());
                Log.i(LOG_TAG, "CallStatus - handle " + ImsUtil.getHandle(cs.handle()) + " session " + cs.session() + " status " + cs.state());
                this.mCallStatusRegistrants.notifyResult(cs);
                return;
            }
            handle = -1;
        } else if (id == 10005) {
            if (noti.notiType() == 7) {
                this.mNewIncomingCallRegistrants.notifyResult((IncomingCall) notify.noti(new IncomingCall()));
                handle = -1;
            } else {
                handle = -1;
            }
        } else if (id == 10014) {
            if (noti.notiType() == 15) {
                this.mModifyVideoRegistrants.notifyResult((ModifyVideoData) notify.noti(new ModifyVideoData()));
                handle = -1;
            } else {
                handle = -1;
            }
        } else if (id == 10015) {
            if (noti.notiType() == 16) {
                this.mVideoEventRegistrants.notifyResult((NotifyVideoEventData) notify.noti(new NotifyVideoEventData()));
                handle = -1;
            } else {
                handle = -1;
            }
        } else if (id == 10035) {
            if (noti.notiType() == 17) {
                this.mCmcRecordEventRegistrants.notifyResult((NotifyCmcRecordEventData) notify.noti(new NotifyCmcRecordEventData()));
                handle = -1;
            } else {
                handle = -1;
            }
        } else if (id == 10006) {
            if (noti.notiType() == 8) {
                ConfCallChanged cc = (ConfCallChanged) notify.noti(new ConfCallChanged());
                if (cc == null) {
                    Log.e(LOG_TAG, "cc is null");
                    return;
                }
                Log.i(LOG_TAG, "ConfCallChanged: session " + cc.session() + " event " + cc.event() + " participants " + cc.participantsLength());
                Participant[] participants = new Participant[cc.participantsLength()];
                for (int i4 = 0; i4 < cc.participantsLength(); i4++) {
                    participants[i4] = cc.participants(i4);
                }
                for (Participant participant : participants) {
                    Log.i(LOG_TAG, "   " + IMSLog.checker(participant.uri()) + " : " + participant.status());
                }
                this.mConferenceUpdateRegistrants.notifyResult(cc);
                handle = -1;
            } else {
                handle = -1;
            }
        } else if (id == 10008) {
            Log.i(LOG_TAG, "ReferReceived:");
            this.mReferReceivedRegistrants.notifyResult((ReferReceived) notify.noti(new ReferReceived()));
            handle = -1;
        } else if (id == 10009) {
            ReferStatus rs2 = (ReferStatus) notify.noti(new ReferStatus());
            if (rs2 == null) {
                Log.e(LOG_TAG, "rs is null");
                return;
            }
            Log.i(LOG_TAG, "ReferStatus: session=" + rs2.session() + " resp=" + rs2.statusCode());
            this.mReferStatusRegistrants.notifyResult(rs2);
            handle = -1;
        } else if (id == 10011) {
            if (noti.notiType() == 13) {
                ModifyCallData modCallData = (ModifyCallData) notify.noti(new ModifyCallData());
                if (modCallData == null) {
                    Log.e(LOG_TAG, "modCallData is null");
                    return;
                }
                Log.i(LOG_TAG, "ModifyCall - session: " + modCallData.session() + ", oldCallType: " + modCallData.oldType() + ", newCallType: " + modCallData.newType());
                this.mModifyCallRegistrants.notifyResult(modCallData);
                handle = -1;
            } else {
                handle = -1;
            }
        } else if (id == 10010) {
            if (noti.notiType() == 12) {
                UpdateRouteTable ur = (UpdateRouteTable) notify.noti(new UpdateRouteTable());
                if (ur == null) {
                    Log.e(LOG_TAG, "ur is null");
                    return;
                }
                int handle7 = ImsUtil.getHandle(ur.handle());
                Log.i(LOG_TAG, "UpdateRouteTable - handle " + handle7 + " op " + ur.operation() + " addr " + ur.address());
                StackEventListener listener7 = this.mUaListenerList.get(Integer.valueOf(handle7));
                if (listener7 != null) {
                    Log.i(LOG_TAG, "calling UpdateRouteTable.");
                    listener7.onUpdateRouteTableRequested(handle7, ur.operation(), ur.address());
                    return;
                }
                return;
            }
            Log.i(LOG_TAG, "processNotify: msg not found.");
            handle = -1;
        } else if (id == 10023) {
            if (noti.notiType() == 1) {
                int handle8 = ImsUtil.getHandle(((RegistrationStatus) notify.noti(new RegistrationStatus())).handle());
                Log.i(LOG_TAG, "calling onUpdate Pani");
                StackEventListener listener8 = this.mUaListenerList.get(Integer.valueOf(handle8));
                if (listener8 != null) {
                    listener8.onUpdatePani();
                    return;
                }
                return;
            }
            handle = -1;
        } else if (id == 10026) {
            if (noti.notiType() == 1) {
                RegistrationStatus rs3 = (RegistrationStatus) notify.noti(new RegistrationStatus());
                int handle9 = ImsUtil.getHandle(rs3.handle());
                Log.i("StackIF[" + rs3.handle() + "]", "calling onRefreshRegNotification");
                StackEventListener listener9 = this.mUaListenerList.get(Integer.valueOf(handle9));
                if (listener9 != null) {
                    listener9.onRefreshRegNotification(handle9);
                    return;
                }
                return;
            }
            handle = -1;
        } else if (id == 20008) {
            Log.i(LOG_TAG, "Echolocate Notify receive");
            if (noti.notiType() == 56) {
                this.mEcholocateRegistrants.notifyResult((EcholocateMsg) notify.noti(new EcholocateMsg()));
                handle = -1;
            } else {
                handle = -1;
            }
        } else if (id == 20004) {
            Log.i(LOG_TAG, "ReceiveSmsNotification: ");
            this.mNewIncomingSmsRegistrants.notifyResult((ReceiveSmsNotification) notify.noti(new ReceiveSmsNotification()));
            handle = -1;
        } else if (id == 20003) {
            Log.i(LOG_TAG, "SmsRpAckNotification: ");
            this.mSmsRpAckRegistrants.notifyResult((SmsRpAckNotification) notify.noti(new SmsRpAckNotification()));
            handle = -1;
        } else if (id != 10003) {
            handle = true;
            if (id == 10018) {
                XCapMessage xcap = (XCapMessage) notify.noti(new XCapMessage());
                String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                String message = hidePrivateInfoFromSipMsg(xcap.xcapMessage());
                IImsFramework iImsFramework = this.mImsFramework;
                if (iImsFramework != null) {
                    iImsFramework.getImsDiagMonitor().onIndication(1, message, 100, xcap.direction(), timestamp, "", "", "");
                }
                recordSipHistory(new SipDebugMessage(message, "", timestamp, xcap.direction() != 0, -1));
            } else if (id == 10019) {
                this.mSSEventRegistrants.notifyResult((SSGetGbaKey) notify.noti(new SSGetGbaKey()));
            } else if (id == 20001) {
                if (noti.notiType() == 72) {
                    AlarmWakeUp alarm = (AlarmWakeUp) notify.noti(new AlarmWakeUp());
                    int alarmId = (int) alarm.id();
                    int delay = (int) alarm.delay();
                    MiscEventListener miscEventListener = this.mMiscListener;
                    if (miscEventListener != null) {
                        miscEventListener.onAlarmRequested(alarmId, delay);
                    }
                } else {
                    Log.i(LOG_TAG, "processNotify: msg not found.");
                }
            } else if (id == 20002) {
                if (noti.notiType() == 73) {
                    int alarmId2 = (int) ((CancelAlarm) notify.noti(new CancelAlarm())).id();
                    MiscEventListener miscEventListener2 = this.mMiscListener;
                    if (miscEventListener2 != null) {
                        miscEventListener2.onAlarmCancelled(alarmId2);
                    }
                } else {
                    Log.i(LOG_TAG, "processNotify: msg not found.");
                }
            } else if (id == 11004 || id == 11002 || id == 11005 || id == 11007 || id == 11003 || id == 19000 || id == 11008 || id == 12001 || id == 12004 || id == 12005 || id == 12003 || id == 12002 || id == 11001 || id == 11009 || id == 11010 || id == 11011 || id == 11012 || id == 11013 || id == 11014 || id == 20013 || id == 20012 || id == 20011) {
                Log.i(LOG_TAG, "processNotify: IM/FT notify received " + id);
                this.mImRegistrants.notifyResult(notify);
            } else if (id == 11006 || id == 11015) {
                this.mImdnRegistrants.notifyResult(notify);
            } else if (id == 18000 || id == 18001 || id == 18003 || id == 18002 || id == 18004 || id == 18005) {
                this.mSlmRegistrants.notifyResult(notify);
            } else if (id == 13001 || id == 13002 || id == 13003 || id == 13004) {
                this.mPresenceRegistrants.notifyResult(notify);
            } else if (id == 14001 || id == 14002 || id == 14003 || id == 14004 || id == 14005) {
                this.mXdmRegistrants.notifyResult(notify);
            } else if (id == 15001) {
                Log.i(LOG_TAG, "received NOTIFY_OPTIONS_RECEIVED");
                this.mOptionsRegistrants.notifyResult(notify);
            } else if (id == 20005) {
                this.mDialogEventRegistrants.notifyResult((DialogEvent) notify.noti(new DialogEvent()));
            } else if (id == 20006) {
                Log.i(LOG_TAG, "received NOTIFY_X509_CERT_VERIFY_REQUEST");
                this.mUaListenerList.get(0).onX509CertVerifyRequested((X509CertVerifyRequest) notify.noti(new X509CertVerifyRequest()));
            } else if (id == 10012) {
                this.mCdpnInfoRegistrants.notifyResult(((CdpnInfo) notify.noti(new CdpnInfo())).calledPartyNumber());
            } else if (id == 20007) {
                DnsResponse dnsResp = (DnsResponse) notify.noti(new DnsResponse());
                int handle10 = ImsUtil.getHandle(dnsResp.handle());
                for (int i5 = 0; i5 < 2; i5++) {
                    StackEventListener listener10 = this.mUaListenerList.get(Integer.valueOf(i5));
                    List<String> ipaddrList = new ArrayList<>();
                    for (int k = 0; k < dnsResp.ipAddrListLength(); k++) {
                        ipaddrList.add(dnsResp.ipAddrList(k));
                    }
                    if (listener10 != null) {
                        listener10.onDnsResponse(dnsResp.hostname(), ipaddrList, (int) dnsResp.port(), handle10);
                    }
                }
                int i6 = handle10;
                return;
            } else if (id == 16001 || id == 16002 || id == 16003 || id == 16004) {
                this.mIshRegistrants.notifyResult(notify);
            } else if (id == 17001 || id == 17002 || id == 17003) {
                this.mVshRegistrants.notifyResult(notify);
            } else if (id == 10016) {
                if (noti.notiType() == 18) {
                    this.mDedicatedBearerEventRegistrants.notifyResult((DedicatedBearerEvent) notify.noti(new DedicatedBearerEvent()));
                }
            } else if (id == 10017) {
                if (noti.notiType() == 19) {
                    this.mRrcConnectionEventRegistrants.notifyResult((RrcConnectionEvent) notify.noti(new RrcConnectionEvent()));
                }
            } else if (id == 10022) {
                if (noti.notiType() == 21) {
                    this.mRtpLossRateNotiRegistrants.notifyResult((RtpLossRateNoti) notify.noti(new RtpLossRateNoti()));
                }
            } else if (id == 20009) {
                if (noti.notiType() == 78) {
                    DumpMessage dumpMsg = (DumpMessage) notify.noti(new DumpMessage());
                    DumpRequest dump = new DumpRequest(dumpMsg.tag(), dumpMsg.value(), new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date()));
                    if (this.mSipHistory.size() >= 100) {
                        this.mSipHistory.remove(0);
                    }
                    this.mStackDumpData.add(dump);
                }
            } else if (id == 10021) {
                if (noti.notiType() == 20) {
                    DTMFDataEvent dtmfInfo = (DTMFDataEvent) notify.noti(new DTMFDataEvent());
                    Log.i(LOG_TAG, "DTMF Event: " + dtmfInfo.event() + " Volume: " + dtmfInfo.volume() + " Duration: " + dtmfInfo.duration() + " Endbit: " + dtmfInfo.endbit());
                    this.mDtmfRegistrants.notifyResult(dtmfInfo);
                }
            } else if (id == 10030 || id == 10031 || id == 10032 || id == 10033 || id == 10034) {
                this.mEucrRegistrants.notifyResult(notify);
            } else if (id == 20010) {
                if (noti.notiType() == 79) {
                    this.mXqMtripRegistrants.notifyResult((XqMessage) notify.noti(new XqMessage()));
                }
            } else if (id == 10024) {
                if (noti.notiType() == 22) {
                    TextDataEvent textInfo = (TextDataEvent) notify.noti(new TextDataEvent());
                    Log.i(LOG_TAG, " Text: " + textInfo.text() + " len: " + textInfo.len());
                    this.mTextRegistrants.notifyResult(textInfo);
                }
            } else if (id == 10028) {
                IMSLog.i(LOG_TAG, "receive contact activated");
                int handle11 = ImsUtil.getHandle(((ContactActivated) notify.noti(new ContactActivated())).handle());
                IMSLog.i(LOG_TAG, "Handle: " + handle11);
                StackEventListener listener11 = this.mUaListenerList.get(Integer.valueOf(handle11));
                if (listener11 != null) {
                    listener11.onContactActivated(handle11);
                }
                int i7 = handle11;
                return;
            } else if (id == 10029) {
                IMSLog.i(LOG_TAG, "receive contact uri in reg-event");
                ContactUriInfo cu = (ContactUriInfo) notify.noti(new ContactUriInfo());
                int handle12 = ImsUtil.getHandle(cu.handle());
                List<String> contactUriList = new ArrayList<>();
                for (int i8 = 0; i8 < cu.uriListLength(); i8++) {
                    contactUriList.add(cu.uriList(i8));
                }
                IMSLog.i(LOG_TAG, "Handle: " + handle12 + " uri size:" + contactUriList.size() + " uri_list:" + contactUriList);
                int isRegi = ImsUtil.getHandle(cu.isRegi());
                String contactUriType = cu.uriType();
                IMSLog.d(LOG_TAG, "isRegi: " + isRegi + ", contactUriType: " + contactUriType);
                StackEventListener listener12 = this.mUaListenerList.get(Integer.valueOf(handle12));
                if (listener12 != null && contactUriList.size() > 0) {
                    listener12.onRegEventContactUriNotification(handle12, contactUriList, isRegi, contactUriType);
                }
                int i9 = handle12;
                return;
            } else if (id == 10036) {
                IMSLog.i(LOG_TAG, "receive cmc info");
                CallSendCmcInfo ci = (CallSendCmcInfo) notify.noti(new CallSendCmcInfo());
                int handle13 = ImsUtil.getHandle(ci.handle());
                Log.i(LOG_TAG, "CmcInfo - handle " + handle13 + " sessionId: " + ci.sessionId());
                this.mCmcInfoRegistrants.notifyResult(ci);
                int i10 = handle13;
                return;
            }
        } else if (noti.notiType() == 55) {
            SipMessage sip = (SipMessage) notify.noti(new SipMessage());
            String timestamp2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date());
            if (sip.origin() != 0) {
                String message2 = "";
                if (sip.sipMessage() != null) {
                    message2 = sip.sipMessage();
                }
                String method = "";
                String[] lines = message2.split("\r\n");
                int length = lines.length;
                int i11 = 0;
                while (true) {
                    if (i11 >= length) {
                        break;
                    }
                    String line = lines[i11];
                    if (method.isEmpty()) {
                        method = line;
                    }
                    if (line.toLowerCase().contains("cseq")) {
                        method = method + " [" + line + "]\n";
                        break;
                    }
                    i11++;
                }
                String method2 = hidePrivateInfoFromSipMsg(method);
                int phoneId = (int) sip.phoneId();
                String sipMsgTag = "SIPMSG[" + phoneId + "]";
                if (sip.direction() == 0) {
                    Log.i(sipMsgTag, "[-->] " + method2);
                } else {
                    Log.i(sipMsgTag, "[<--] " + method2);
                    this.mRawSipRegistrants.notifyResult(message2);
                    if (method2.toLowerCase().contains(SoftphoneContract.SoftphoneAccount.REGISTER) && method2.contains("200")) {
                        this.mUaRegisterResponseRawSip.put(Integer.valueOf(this.mHandle), message2);
                    }
                }
                IImsFramework iImsFramework2 = this.mImsFramework;
                if (iImsFramework2 != null) {
                    String[] strArr = lines;
                    iImsFramework2.getImsDiagMonitor().onIndication(0, message2, 0, sip.direction(), phoneId, timestamp2, "", "", sip.hexContents());
                }
                if (IMSLog.isEngMode()) {
                    handle = true;
                } else if (!Debug.isProductShip()) {
                    handle = -1;
                } else {
                    boolean isLoggableSipMessageForUserShipBinary = false;
                    if (!sip.isRcsProfile()) {
                        isLoggableSipMessageForUserShipBinary = true;
                        isUseEncryptedLogger = true;
                    } else {
                        isUseEncryptedLogger = false;
                    }
                    if (isLoggableSipMessageForUserShipBinary) {
                        String hidedMessage = hidePrivateInfoFromSipMsg(message2);
                        if (isUseEncryptedLogger) {
                            historyMessage = IMSLog.dx(sipMsgTag, hidedMessage);
                        } else {
                            Log.i(sipMsgTag, hidedMessage);
                            historyMessage = hidedMessage;
                        }
                    } else {
                        historyMessage = "";
                    }
                    handle = -1;
                    SipDebugMessage sipDebugMessage = r12;
                    SipDebugMessage sipDebugMessage2 = new SipDebugMessage(historyMessage, method2, timestamp2, sip.direction() == 1, phoneId, isUseEncryptedLogger);
                    recordSipHistory(sipDebugMessage);
                    this.mSIPMSGRegistrants.notifyResult(notify);
                }
                Log.i(sipMsgTag, message2);
                recordSipHistory(new SipDebugMessage(message2, method2, timestamp2, sip.direction() == 1, phoneId));
                this.mSIPMSGRegistrants.notifyResult(notify);
            } else {
                return;
            }
        } else {
            handle = -1;
        }
    }

    private String serialString(int serial) {
        StringBuilder sb = new StringBuilder(8);
        long j = (((long) serial) - -2147483648L) % 10000;
        String sn = Long.toString(((long) serial) % 10000);
        sb.append('[');
        int s = sn.length();
        for (int i = 0; i < 4 - s; i++) {
            sb.append('0');
        }
        sb.append(sn);
        sb.append(']');
        return sb.toString();
    }

    public static boolean checkLogEnable() {
        return Extensions.Build.IS_DEBUGGABLE || DeviceUtil.isOtpAuthorized();
    }

    private static class SipDebugMessage {
        /* access modifiers changed from: private */
        public boolean mIsEncrypted;
        private boolean mIsRx;
        private String mMethod;
        private int mPhoneId;
        private String mSipMessage;
        private String mTimestamp;

        private SipDebugMessage(String sip, String method, String timestamp, boolean isRx, int phoneId) {
            this.mSipMessage = sip;
            this.mMethod = method;
            this.mTimestamp = timestamp;
            this.mIsRx = isRx;
            this.mPhoneId = phoneId;
            this.mIsEncrypted = false;
        }

        private SipDebugMessage(String sip, String method, String timestamp, boolean isRx, int phoneId, boolean isEncrypted) {
            this.mSipMessage = sip;
            this.mMethod = method;
            this.mTimestamp = timestamp;
            this.mIsRx = isRx;
            this.mPhoneId = phoneId;
            this.mIsEncrypted = isEncrypted;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.mTimestamp);
            sb.append("   slot[");
            sb.append(this.mPhoneId);
            sb.append("] ");
            sb.append(this.mIsRx ? "[<--] " : "[-->] ");
            sb.append(this.mMethod);
            sb.append(this.mSipMessage);
            return sb.toString();
        }
    }

    private void recordSipHistory(SipDebugMessage sipDebugMessage) {
        if (this.mSipHistory.size() >= 100) {
            this.mSipHistory.remove(0);
        }
        this.mSipHistory.add(sipDebugMessage);
    }

    public void dump() {
        if (checkLogEnable()) {
            IMSLog.dump(LOG_TAG, "Dump of IMS Stack:", false);
            IMSLog.increaseIndent(LOG_TAG);
            for (DumpRequest dump : this.mStackDumpData) {
                IMSLog.dump(LOG_TAG, dump.toString(), false);
            }
            IMSLog.decreaseIndent(LOG_TAG);
        }
        IMSLog.dump(LOG_TAG, "Dump of IMS SIP messages history:");
        IMSLog.increaseIndent(LOG_TAG);
        for (SipDebugMessage sip : this.mSipHistory) {
            IMSLog.dump(LOG_TAG, sip.toString(), !sip.mIsEncrypted);
        }
        IMSLog.decreaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Dump of IMS log data:");
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dumpSecretKey(LOG_TAG);
        IMSLog.decreaseIndent(LOG_TAG);
    }

    public void setSilentLogEnabled(boolean onoff) {
        Log.i(LOG_TAG, "setSilentLogEnabled: onoff " + onoff);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestSilentLogEnabled.startRequestSilentLogEnabled(builder);
        RequestSilentLogEnabled.addOnoff(builder, onoff);
        int setSilentLogEnabledOffset = RequestSilentLogEnabled.endRequestSilentLogEnabled(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_SILENT_LOG_ENABLED);
        Request.addReqType(builder, ReqMsg.request_silent_log_enabled);
        Request.addReq(builder, setSilentLogEnabledOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }

    public void updateNtpTimeOffset(long ntpTimeOffset) {
        Log.i(LOG_TAG, "updateNtpTimeOffset : " + ntpTimeOffset);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestNtpTimeOffset.startRequestNtpTimeOffset(builder);
        RequestNtpTimeOffset.addOffset(builder, ntpTimeOffset);
        int sendNtpTimeOffset = RequestNtpTimeOffset.endRequestNtpTimeOffset(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_NTP_TIME_OFFSET);
        Request.addReqType(builder, ReqMsg.request_ntp_time_offset);
        Request.addReq(builder, sendNtpTimeOffset);
        sendRequest(builder, Request.endRequest(builder), (Message) null);
    }
}
