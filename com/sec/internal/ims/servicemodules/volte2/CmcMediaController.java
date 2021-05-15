package com.sec.internal.ims.servicemodules.volte2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import android.util.SparseArray;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.RelayStreams;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class CmcMediaController implements ICmcMediaController {
    private static final int EVENT_CMC_MEDIA_EVENT = 11;
    private static final int EVENT_CMC_RECORDER_START = 2;
    private static final int EVENT_CMC_RECORDER_STOP = 3;
    private static final int EVENT_RETRY_CREATE_RELAY_CHANNEL = 12;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CmcMediaController.class.getSimpleName();
    private Handler mCmcMediaEventHandler = null;
    /* access modifiers changed from: private */
    public ICmcMediaServiceInterface mCmcMediaIntf = null;
    private final RemoteCallbackList<ISemCmcRecordingListener> mCmcRecordingCallbacks = new RemoteCallbackList<>();
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    private int mExtStream = -1;
    protected ImsCallSessionManager mImsCallSessionManager;
    private int mIntStream = -1;
    /* access modifiers changed from: private */
    public boolean mPendingRelayChannelCreation = false;
    private int mRelayDirection = 0;
    /* access modifiers changed from: private */
    public SparseArray<RelayStreams> mRelayStreamMap = new SparseArray<>();
    private IVolteServiceModuleInternal mVolteServiceModule = null;

    public CmcMediaController(IVolteServiceModuleInternal vsm, Looper looper, ImsCallSessionManager callSessionManager, SimpleEventLog eventLog) {
        this.mEventLog = eventLog;
        this.mImsCallSessionManager = callSessionManager;
        this.mVolteServiceModule = vsm;
        this.mCmcMediaIntf = ImsRegistry.getHandlerFactory().getCmcHandler();
        this.mCmcMediaEventHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                AsyncResult ar = (AsyncResult) msg.obj;
                int i = msg.what;
                if (i == 11) {
                    CmcMediaController.this.onImsRelayEvent((IMSMediaEvent) ar.result);
                } else if (i == 12) {
                    int extStreamId = msg.arg1;
                    int intStreamId = msg.arg2;
                    String access$100 = CmcMediaController.LOG_TAG;
                    Log.i(access$100, "EVT_RETRY_CREATE_RELAY_CHANNEL extStreamId: " + extStreamId + " intStreamId: " + intStreamId);
                    RelayStreams extRS = (RelayStreams) CmcMediaController.this.mRelayStreamMap.get(extStreamId);
                    RelayStreams intRS = (RelayStreams) CmcMediaController.this.mRelayStreamMap.get(intStreamId);
                    if (!(extRS == null || intRS == null)) {
                        ImsCallSession extSession = CmcMediaController.this.getSession(extRS.getSessionId());
                        ImsCallSession intSession = CmcMediaController.this.getSession(intRS.getSessionId());
                        if (!(!CmcMediaController.this.mPendingRelayChannelCreation || extSession == null || intSession == null)) {
                            int relayChannel = CmcMediaController.this.mCmcMediaIntf.sreCreateRelayChannel(extStreamId, intStreamId);
                            if (relayChannel > -1) {
                                int relayDirection = (extSession.getCallState() == CallConstants.STATE.HeldCall || intSession.getCallState() == CallConstants.STATE.HeldCall) ? 1 : 0;
                                IMSLog.c(LogClass.CMC_START_RELAY, relayChannel + "," + relayDirection);
                                CmcMediaController.this.mCmcMediaIntf.sreStartRelayChannel(relayChannel, relayDirection);
                                extRS.setRelayChannelId(relayChannel);
                                intRS.setRelayChannelId(relayChannel);
                                SimpleEventLog access$600 = CmcMediaController.this.mEventLog;
                                access$600.add("Start Pending RelayChannel " + relayChannel + " with direction " + relayDirection);
                            } else {
                                String access$1002 = CmcMediaController.LOG_TAG;
                                Log.i(access$1002, "failed to create relay channel mRelayChannel: " + relayChannel);
                            }
                        }
                    }
                    CmcMediaController.this.resetCreateRelayChannelParams();
                }
            }
        };
        init();
    }

    public void connectToSve(int phoneId) {
        this.mCmcMediaIntf.sendConnectToSve(phoneId);
    }

    public void disconnectToSve() {
        this.mCmcMediaIntf.sendDisonnectToSve();
    }

    public void init() {
        this.mCmcMediaIntf.registerForCmcMediaEvent(this.mCmcMediaEventHandler, 11, (Object) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0061, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e1, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00fd, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onRelayStreamEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent r10, int r11) {
        /*
            r9 = this;
            monitor-enter(r9)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x01a6 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a6 }
            r1.<init>()     // Catch:{ all -> 0x01a6 }
            java.lang.String r2 = "onRelayStreamEvent : "
            r1.append(r2)     // Catch:{ all -> 0x01a6 }
            int r2 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            r1.append(r2)     // Catch:{ all -> 0x01a6 }
            java.lang.String r2 = " phoneId : "
            r1.append(r2)     // Catch:{ all -> 0x01a6 }
            int r2 = r10.getPhoneId()     // Catch:{ all -> 0x01a6 }
            r1.append(r2)     // Catch:{ all -> 0x01a6 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x01a6 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x01a6 }
            int r0 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            r1 = 3
            if (r0 != r1) goto L_0x0040
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r0 = r9.mCmcMediaIntf     // Catch:{ all -> 0x01a6 }
            int r1 = r10.getPhoneId()     // Catch:{ all -> 0x01a6 }
            int r2 = r10.getSessionID()     // Catch:{ all -> 0x01a6 }
            r3 = 28
            r0.sendMediaEvent(r1, r2, r3, r11)     // Catch:{ all -> 0x01a6 }
            monitor-exit(r9)
            return
        L_0x0040:
            int r0 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            r1 = 4
            if (r0 != r1) goto L_0x0062
            int r0 = r10.getSessionID()     // Catch:{ all -> 0x01a6 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r9.getSession(r0)     // Catch:{ all -> 0x01a6 }
            if (r0 == 0) goto L_0x0060
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r1 = r9.mCmcMediaIntf     // Catch:{ all -> 0x01a6 }
            int r2 = r10.getPhoneId()     // Catch:{ all -> 0x01a6 }
            int r3 = r10.getSessionID()     // Catch:{ all -> 0x01a6 }
            r4 = 61
            r1.sendMediaEvent(r2, r3, r4, r11)     // Catch:{ all -> 0x01a6 }
        L_0x0060:
            monitor-exit(r9)
            return
        L_0x0062:
            int r0 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            r1 = 5
            if (r0 != r1) goto L_0x0074
            com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$AudioRtpStats r0 = r10.getRelayRtpStats()     // Catch:{ all -> 0x01a6 }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r1 = r9.mCmcMediaIntf     // Catch:{ all -> 0x01a6 }
            r1.sendRtpStatsToStack(r0)     // Catch:{ all -> 0x01a6 }
            monitor-exit(r9)
            return
        L_0x0074:
            int r0 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            r1 = 12
            r2 = 11
            r3 = 10
            if (r0 == r3) goto L_0x00fe
            int r0 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            if (r0 == r2) goto L_0x00fe
            int r0 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            if (r0 != r1) goto L_0x008d
            goto L_0x00fe
        L_0x008d:
            int r0 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            r1 = 1
            if (r0 != r1) goto L_0x00e2
            r9.resetCreateRelayChannelParams()     // Catch:{ all -> 0x01a6 }
            int r0 = r10.getStreamId()     // Catch:{ all -> 0x01a6 }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r1 = r9.mRelayStreamMap     // Catch:{ all -> 0x01a6 }
            java.lang.Object r1 = r1.get(r0)     // Catch:{ all -> 0x01a6 }
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r1 = (com.sec.internal.ims.servicemodules.volte2.data.RelayStreams) r1     // Catch:{ all -> 0x01a6 }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r2 = r9.mRelayStreamMap     // Catch:{ all -> 0x01a6 }
            r2.delete(r0)     // Catch:{ all -> 0x01a6 }
            if (r1 == 0) goto L_0x00e0
            int r2 = r1.getBoundStreamId()     // Catch:{ all -> 0x01a6 }
            r3 = -1
            if (r2 <= r3) goto L_0x00e0
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r2 = r9.mRelayStreamMap     // Catch:{ all -> 0x01a6 }
            int r4 = r1.getBoundStreamId()     // Catch:{ all -> 0x01a6 }
            java.lang.Object r2 = r2.get(r4)     // Catch:{ all -> 0x01a6 }
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r2 = (com.sec.internal.ims.servicemodules.volte2.data.RelayStreams) r2     // Catch:{ all -> 0x01a6 }
            if (r2 == 0) goto L_0x00e0
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x01a6 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a6 }
            r5.<init>()     // Catch:{ all -> 0x01a6 }
            java.lang.String r6 = "reset bound stream "
            r5.append(r6)     // Catch:{ all -> 0x01a6 }
            int r6 = r2.getStreamId()     // Catch:{ all -> 0x01a6 }
            r5.append(r6)     // Catch:{ all -> 0x01a6 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x01a6 }
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x01a6 }
            r2.setRelayChannelId(r3)     // Catch:{ all -> 0x01a6 }
            r2.setBoundStreamId(r3)     // Catch:{ all -> 0x01a6 }
        L_0x00e0:
            monitor-exit(r9)
            return
        L_0x00e2:
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r0 = new com.sec.internal.ims.servicemodules.volte2.data.RelayStreams     // Catch:{ all -> 0x01a6 }
            r0.<init>(r10)     // Catch:{ all -> 0x01a6 }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r1 = r9.mRelayStreamMap     // Catch:{ all -> 0x01a6 }
            int r2 = r10.getStreamId()     // Catch:{ all -> 0x01a6 }
            r1.put(r2, r0)     // Catch:{ all -> 0x01a6 }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r1 = r9.mRelayStreamMap     // Catch:{ all -> 0x01a6 }
            int r1 = r1.size()     // Catch:{ all -> 0x01a6 }
            r2 = 2
            if (r1 < r2) goto L_0x00fc
            r9.handleRelayChannel()     // Catch:{ all -> 0x01a6 }
        L_0x00fc:
            monitor-exit(r9)
            return
        L_0x00fe:
            r0 = 0
        L_0x00ff:
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r4 = r9.mRelayStreamMap     // Catch:{ all -> 0x01a6 }
            int r4 = r4.size()     // Catch:{ all -> 0x01a6 }
            if (r0 >= r4) goto L_0x01a4
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r4 = r9.mRelayStreamMap     // Catch:{ all -> 0x01a6 }
            java.lang.Object r4 = r4.valueAt(r0)     // Catch:{ all -> 0x01a6 }
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r4 = (com.sec.internal.ims.servicemodules.volte2.data.RelayStreams) r4     // Catch:{ all -> 0x01a6 }
            int r5 = r4.getSessionId()     // Catch:{ all -> 0x01a6 }
            int r6 = r10.getSessionID()     // Catch:{ all -> 0x01a6 }
            if (r5 != r6) goto L_0x01a0
            int r5 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            if (r5 != r3) goto L_0x0143
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x01a6 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a6 }
            r6.<init>()     // Catch:{ all -> 0x01a6 }
            java.lang.String r7 = "hold relay channel : "
            r6.append(r7)     // Catch:{ all -> 0x01a6 }
            int r7 = r4.getRelayChannelId()     // Catch:{ all -> 0x01a6 }
            r6.append(r7)     // Catch:{ all -> 0x01a6 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01a6 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x01a6 }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r5 = r9.mCmcMediaIntf     // Catch:{ all -> 0x01a6 }
            int r6 = r4.getRelayChannelId()     // Catch:{ all -> 0x01a6 }
            r5.sreHoldRelayChannel(r6)     // Catch:{ all -> 0x01a6 }
            goto L_0x01a0
        L_0x0143:
            int r5 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            if (r5 != r2) goto L_0x016e
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x01a6 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a6 }
            r6.<init>()     // Catch:{ all -> 0x01a6 }
            java.lang.String r7 = "resume relay channel : "
            r6.append(r7)     // Catch:{ all -> 0x01a6 }
            int r7 = r4.getRelayChannelId()     // Catch:{ all -> 0x01a6 }
            r6.append(r7)     // Catch:{ all -> 0x01a6 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01a6 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x01a6 }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r5 = r9.mCmcMediaIntf     // Catch:{ all -> 0x01a6 }
            int r6 = r4.getRelayChannelId()     // Catch:{ all -> 0x01a6 }
            r5.sreResumeRelayChannel(r6)     // Catch:{ all -> 0x01a6 }
            goto L_0x01a0
        L_0x016e:
            int r5 = r10.getRelayStreamEvent()     // Catch:{ all -> 0x01a6 }
            if (r5 != r1) goto L_0x01a0
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x01a6 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a6 }
            r6.<init>()     // Catch:{ all -> 0x01a6 }
            java.lang.String r7 = "start record relay channel : "
            r6.append(r7)     // Catch:{ all -> 0x01a6 }
            int r7 = r4.getRelayChannelId()     // Catch:{ all -> 0x01a6 }
            r6.append(r7)     // Catch:{ all -> 0x01a6 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01a6 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x01a6 }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r5 = r9.mCmcMediaIntf     // Catch:{ all -> 0x01a6 }
            int r6 = r10.getSessionID()     // Catch:{ all -> 0x01a6 }
            int r7 = r4.getStreamId()     // Catch:{ all -> 0x01a6 }
            int r8 = r4.getRelayChannelId()     // Catch:{ all -> 0x01a6 }
            r5.sreStartRecordingChannel(r6, r7, r8)     // Catch:{ all -> 0x01a6 }
        L_0x01a0:
            int r0 = r0 + 1
            goto L_0x00ff
        L_0x01a4:
            monitor-exit(r9)
            return
        L_0x01a6:
            r10 = move-exception
            monitor-exit(r9)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcMediaController.onRelayStreamEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent, int):void");
    }

    private void handleRelayChannel() {
        Log.i(LOG_TAG, "handleRelayChannel");
        int extStream = -1;
        int intStream = -1;
        for (int keyId = 0; keyId < this.mRelayStreamMap.size(); keyId++) {
            RelayStreams rs = this.mRelayStreamMap.valueAt(keyId);
            ImsCallSession relaySession = getSession(rs.getSessionId());
            String str = LOG_TAG;
            Log.i(str, "Streamid : " + rs.getStreamId() + " SessionId : " + rs.getSessionId());
            if (relaySession == null) {
                Log.e(LOG_TAG, "Session is null");
                this.mRelayStreamMap.delete(rs.getStreamId());
            } else {
                int cmcType = relaySession.getCmcType();
                if (cmcType == 0 && relaySession.getCallProfile().getCmcBoundSessionId() > -1 && rs.getRelayChannelId() == -1) {
                    if (extStream > -1) {
                        this.mRelayStreamMap.delete(extStream);
                    }
                    extStream = rs.getStreamId();
                } else if ((cmcType == 1 || cmcType == 3 || cmcType == 7 || cmcType == 5) && relaySession.getCallProfile().getCmcBoundSessionId() > -1 && rs.getRelayChannelId() == -1) {
                    if (intStream > -1) {
                        this.mRelayStreamMap.delete(intStream);
                    }
                    intStream = rs.getStreamId();
                }
            }
        }
        String str2 = LOG_TAG;
        Log.i(str2, "extStream: " + extStream + ", intStream" + intStream);
        if (extStream != -1 && intStream != -1) {
            startRelayChannel(extStream, intStream);
        }
    }

    private void startRelayChannel(int extStream, int intStream) {
        RelayStreams extRS = this.mRelayStreamMap.get(extStream);
        RelayStreams intRS = this.mRelayStreamMap.get(intStream);
        if (extRS != null && intRS != null && extRS.getRelayChannelId() == -1 && intRS.getRelayChannelId() == -1) {
            ImsCallSession extSession = getSession(extRS.getSessionId());
            ImsCallSession intSession = getSession(intRS.getSessionId());
            if (extSession == null || intSession == null) {
                Log.e(LOG_TAG, "extSession or intSession is null");
            } else if (extSession.getCallProfile().getCmcBoundSessionId() == intRS.getSessionId() || intSession.getCallProfile().getCmcBoundSessionId() == extRS.getSessionId()) {
                extRS.setBoundStreamId(intStream);
                intRS.setBoundStreamId(extStream);
                int relayChannel = this.mCmcMediaIntf.sreCreateRelayChannel(extStream, intStream);
                int relayDirection = (extSession.getCallState() == CallConstants.STATE.HeldCall || intSession.getCallState() == CallConstants.STATE.HeldCall) ? 1 : 0;
                String str = LOG_TAG;
                Log.i(str, "Start Relay Channel " + relayChannel + " with direction " + relayDirection);
                if (relayChannel > -1) {
                    IMSLog.c(LogClass.CMC_START_RELAY, relayChannel + "," + relayDirection);
                    this.mCmcMediaIntf.sreStartRelayChannel(relayChannel, relayDirection);
                    extRS.setRelayChannelId(relayChannel);
                    intRS.setRelayChannelId(relayChannel);
                    resetCreateRelayChannelParams();
                    SimpleEventLog simpleEventLog = this.mEventLog;
                    simpleEventLog.add("Start RelayChannel " + relayChannel + " with direction " + relayDirection);
                    return;
                }
                Handler handler = this.mCmcMediaEventHandler;
                handler.sendMessageDelayed(handler.obtainMessage(12, extStream, intStream), 200);
                this.mPendingRelayChannelCreation = true;
                this.mExtStream = extStream;
                this.mIntStream = intStream;
                this.mRelayDirection = relayDirection;
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.add("Pending StartRelayChannel with " + relayDirection);
            }
        }
    }

    /* access modifiers changed from: private */
    public ImsCallSession getSession(int sessionId) {
        List<ImsCallSession> callSessions = this.mImsCallSessionManager.getSessionList();
        synchronized (callSessions) {
            for (ImsCallSession s : callSessions) {
                if (s != null && s.getSessionId() == sessionId) {
                    return s;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void onImsRelayEvent(IMSMediaEvent event) {
        int i;
        if (!event.isRelayChannelEvent()) {
            ImsCallSession session = getSession(event.getSessionID());
            if (session == null) {
                String str = LOG_TAG;
                Log.i(str, "onImsRelayEvent: session " + event.getSessionID() + " not found.");
                return;
            }
            event.setSessionID(session.getSessionId());
            event.setPhoneId(session.getPhoneId());
            if (event.isRelayStreamEvent()) {
                onRelayStreamEvent(event, session.getRegistration().getHandle());
            } else if (event.isCmcRecordingEvent()) {
                onCmcRecordingEvent(event);
            }
        } else if (event.getRelayChannelEvent() == 1) {
            if (!(!this.mPendingRelayChannelCreation || (i = this.mExtStream) == -1 || this.mIntStream == -1)) {
                RelayStreams extRS = this.mRelayStreamMap.get(i);
                RelayStreams intRS = this.mRelayStreamMap.get(this.mIntStream);
                if (!(extRS == null || intRS == null)) {
                    int relayChannel = this.mCmcMediaIntf.sreCreateRelayChannel(this.mExtStream, this.mIntStream);
                    String str2 = LOG_TAG;
                    Log.i(str2, "Retry Start Relay Channel : " + relayChannel);
                    IMSLog.c(LogClass.CMC_START_RELAY, relayChannel + "," + this.mRelayDirection);
                    this.mCmcMediaIntf.sreStartRelayChannel(relayChannel, this.mRelayDirection);
                    extRS.setRelayChannelId(relayChannel);
                    intRS.setRelayChannelId(relayChannel);
                    SimpleEventLog simpleEventLog = this.mEventLog;
                    simpleEventLog.add("Retry StartRelayChannel " + relayChannel + " with direction " + this.mRelayDirection);
                }
            }
            resetCreateRelayChannelParams();
        }
    }

    private void onCmcRecordingEvent(IMSMediaEvent event) {
        int recordEvent;
        int cmcRecordingEvent = event.getCmcRecordingEvent();
        if (cmcRecordingEvent != 0) {
            if (cmcRecordingEvent == 5) {
                recordEvent = 800;
            } else if (cmcRecordingEvent == 7) {
                recordEvent = 801;
            } else if (cmcRecordingEvent == 8) {
                recordEvent = 900;
            } else if (cmcRecordingEvent != 9) {
                recordEvent = 1;
            } else {
                recordEvent = Id.REQUEST_ALARM_WAKE_UP;
            }
            this.mVolteServiceModule.notifyOnCmcRecordingEvent(event.getPhoneId(), recordEvent, event.getCmcRecordingArg(), event.getSessionID());
        }
    }

    /* access modifiers changed from: private */
    public void resetCreateRelayChannelParams() {
        if (this.mCmcMediaEventHandler.hasMessages(12)) {
            this.mCmcMediaEventHandler.removeMessages(12);
        }
        this.mPendingRelayChannelCreation = false;
        this.mExtStream = -1;
        this.mIntStream = -1;
        this.mRelayDirection = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00ac, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void sendCmcRecordingEvent(int r25, int r26, com.samsung.android.ims.cmc.SemCmcRecordingInfo r27) {
        /*
            r24 = this;
            r1 = r24
            r0 = r26
            monitor-enter(r24)
            r2 = 0
            r3 = 1
            r4 = r25
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r1.getActiveCallByCmcType(r4, r3)     // Catch:{ all -> 0x00ad }
            if (r3 == 0) goto L_0x0098
            r5 = 2
            if (r0 == r5) goto L_0x003e
            r5 = 3
            if (r0 == r5) goto L_0x002e
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x00ad }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ad }
            r6.<init>()     // Catch:{ all -> 0x00ad }
            java.lang.String r7 = "sendCmcRecordingEvent: ignore event = "
            r6.append(r7)     // Catch:{ all -> 0x00ad }
            r6.append(r0)     // Catch:{ all -> 0x00ad }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x00ad }
            android.util.Log.e(r5, r6)     // Catch:{ all -> 0x00ad }
            monitor-exit(r24)
            return
        L_0x002e:
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r5 = r1.mCmcMediaIntf     // Catch:{ all -> 0x00ad }
            int r6 = r3.getPhoneId()     // Catch:{ all -> 0x00ad }
            int r7 = r3.getSessionId()     // Catch:{ all -> 0x00ad }
            boolean r5 = r5.stopCmcRecord(r6, r7)     // Catch:{ all -> 0x00ad }
            r2 = r5
            goto L_0x0098
        L_0x003e:
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x00ad }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ad }
            r6.<init>()     // Catch:{ all -> 0x00ad }
            java.lang.String r7 = "sendCmcRecordingEvent: SemCmcRecordingInfo "
            r6.append(r7)     // Catch:{ all -> 0x00ad }
            java.lang.String r7 = r27.toString()     // Catch:{ all -> 0x00ad }
            r6.append(r7)     // Catch:{ all -> 0x00ad }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x00ad }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x00ad }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r7 = r1.mCmcMediaIntf     // Catch:{ all -> 0x00ad }
            int r8 = r3.getPhoneId()     // Catch:{ all -> 0x00ad }
            int r9 = r3.getSessionId()     // Catch:{ all -> 0x00ad }
            int r10 = r27.getAudioSource()     // Catch:{ all -> 0x00ad }
            int r11 = r27.getOutputFormat()     // Catch:{ all -> 0x00ad }
            long r12 = r27.getMaxFileSize()     // Catch:{ all -> 0x00ad }
            int r14 = r27.getMaxDuration()     // Catch:{ all -> 0x00ad }
            java.lang.String r15 = r27.getOutputPath()     // Catch:{ all -> 0x00ad }
            int r16 = r27.getAudioEncodingBitRate()     // Catch:{ all -> 0x00ad }
            int r17 = r27.getAudioChannels()     // Catch:{ all -> 0x00ad }
            int r18 = r27.getAudioSamplingRate()     // Catch:{ all -> 0x00ad }
            int r19 = r27.getAudioEncoder()     // Catch:{ all -> 0x00ad }
            int r20 = r27.getDurationInterval()     // Catch:{ all -> 0x00ad }
            long r21 = r27.getFileSizeInterval()     // Catch:{ all -> 0x00ad }
            java.lang.String r23 = r27.getAuthor()     // Catch:{ all -> 0x00ad }
            boolean r5 = r7.startCmcRecord(r8, r9, r10, r11, r12, r14, r15, r16, r17, r18, r19, r20, r21, r23)     // Catch:{ all -> 0x00ad }
            r2 = r5
        L_0x0098:
            if (r2 != 0) goto L_0x00ab
            com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent r5 = new com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent     // Catch:{ all -> 0x00ad }
            r5.<init>()     // Catch:{ all -> 0x00ad }
            r6 = 4
            r5.setCmcRecordingEvent(r6)     // Catch:{ all -> 0x00ad }
            com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r6 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE     // Catch:{ all -> 0x00ad }
            r5.setState(r6)     // Catch:{ all -> 0x00ad }
            r1.onCmcRecordingEvent(r5)     // Catch:{ all -> 0x00ad }
        L_0x00ab:
            monitor-exit(r24)
            return
        L_0x00ad:
            r0 = move-exception
            monitor-exit(r24)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcMediaController.sendCmcRecordingEvent(int, int, com.samsung.android.ims.cmc.SemCmcRecordingInfo):void");
    }

    private ImsCallSession getActiveCallByCmcType(int phoneId, int cmcType) {
        List<ImsCallSession> callSessions = this.mImsCallSessionManager.getSessionList();
        synchronized (callSessions) {
            for (ImsCallSession s : callSessions) {
                if (s != null && s.getCallState() == CallConstants.STATE.InCall && s.getCmcType() == cmcType && s.getPhoneId() == phoneId) {
                    return s;
                }
            }
            return null;
        }
    }
}
