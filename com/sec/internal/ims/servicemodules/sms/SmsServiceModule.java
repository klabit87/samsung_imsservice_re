package com.sec.internal.ims.servicemodules.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.sms.ISmsServiceEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.IMessagingAppInfoListener;
import com.sec.internal.ims.util.MessagingAppInfoReceiver;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.log.IMSLog;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class SmsServiceModule extends ServiceModuleBase implements ISmsServiceModule, IMessagingAppInfoListener {
    private static final String ACTION_EMERGENCY_CALLBACK_MODE_INTERNAL = "com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL";
    private static final String ALTERNATIVE_SERVICE = "application/3gpp-ims+xml";
    private static final String ASVC_INITIAL_REGISTRATION = "initial-registration";
    private static final String ASVC_RESTORATION = "restoration";
    protected static final int EMERGENCY_REGISTER_DONE_EVENT = 5;
    protected static final int EMERGENCY_REGISTER_FAIL_EVENT = 6;
    protected static final int EMERGENCY_REGISTER_START_EVENT = 4;
    /* access modifiers changed from: private */
    public static final String LOG_TAG;
    private static final int MAX_RETRANS_COUNT_ON_RP_ERR = 1;
    public static final String NAME;
    private static final int NOTI_503_OUTAGE = 777;
    private static final int NOTI_DEREGISTERED = 999;
    public static final int NOTI_INTERNAL_ADDR_ERR = 10001;
    public static final int NOTI_INTERNAL_BASE = 10000;
    public static final int NOTI_INTERNAL_EMERGENCY_REGI_FAIL = 10002;
    public static final int NOTI_INTERNAL_END = 11000;
    public static final int NOTI_INTERNAL_LIMITED_REGI = 10004;
    public static final int NOTI_INTERNAL_NO_RP_ACK = 10003;
    private static final int NOTI_SUBMIT_REPORT_TIMEOUT = 801;
    private static final int RETRANS_ON_RP_ERROR_TIMEOUT = 3;
    protected static final int RRC_CONNECTION_EVENT = 2;
    protected static final int SCBM_TIMEOUT_EVENT = 7;
    protected static final int SEND_SMS_EVENT = 3;
    private static final int SIP_R_CAUSE_200_OK = 200;
    private static final int SIP_R_CAUSE_LIMITED = 404;
    private static final int SIP_R_CAUSE_TEMP_ERROR = 480;
    protected static final int SMS_EVENT = 1;
    private static final int STATE_TIMEOUT = 1;
    private static final int SUBMIT_REPORT_TIMEOUT = 2;
    private static final int TIMER_EMERGENCY_REGISTER_FAIL = 10000;
    private static final int TIMER_STATE = 180000;
    protected static int TIMER_SUBMIT_REPORT = 40000;
    private static final int TIMER_SUBMIT_REPORT_SPR = 10000;
    private static final int TIMER_VZW_SCBM = 300000;
    private static final int VZW_E911_FALSE = 0;
    private static final int VZW_E911_REREGI = 2;
    private static final int VZW_E911_TRUE = 1;
    /* access modifiers changed from: private */
    public int MAX_RETRANS_COUNT = 3;
    /* access modifiers changed from: private */
    public int MAX_RETRANS_COUNT_SPR = 2;
    private int m3GPP2SendingMsgId = -1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r12, android.content.Intent r13) {
            /*
                r11 = this;
                java.lang.String r0 = r13.getAction()
                r1 = 0
                int r2 = r0.hashCode()
                r3 = 4
                r4 = 3
                r5 = 2
                r6 = 1
                r7 = -1
                r8 = 0
                switch(r2) {
                    case -1926447105: goto L_0x003b;
                    case -1664867553: goto L_0x0031;
                    case -1326089125: goto L_0x0027;
                    case 1262364259: goto L_0x001d;
                    case 2038466647: goto L_0x0013;
                    default: goto L_0x0012;
                }
            L_0x0012:
                goto L_0x0045
            L_0x0013:
                java.lang.String r2 = "android.intent.action.DEVICE_STORAGE_FULL"
                boolean r2 = r0.equals(r2)
                if (r2 == 0) goto L_0x0012
                r2 = r8
                goto L_0x0046
            L_0x001d:
                java.lang.String r2 = "android.intent.action.DEVICE_STORAGE_NOT_FULL"
                boolean r2 = r0.equals(r2)
                if (r2 == 0) goto L_0x0012
                r2 = r6
                goto L_0x0046
            L_0x0027:
                java.lang.String r2 = "android.intent.action.PHONE_STATE"
                boolean r2 = r0.equals(r2)
                if (r2 == 0) goto L_0x0012
                r2 = r3
                goto L_0x0046
            L_0x0031:
                java.lang.String r2 = "com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL"
                boolean r2 = r0.equals(r2)
                if (r2 == 0) goto L_0x0012
                r2 = r4
                goto L_0x0046
            L_0x003b:
                java.lang.String r2 = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED"
                boolean r2 = r0.equals(r2)
                if (r2 == 0) goto L_0x0012
                r2 = r5
                goto L_0x0046
            L_0x0045:
                r2 = r7
            L_0x0046:
                java.lang.String r9 = "mBroadcastReceiver.onReceive: "
                if (r2 == 0) goto L_0x0182
                if (r2 == r6) goto L_0x0160
                r6 = 7
                if (r2 == r5) goto L_0x00fa
                if (r2 == r4) goto L_0x00fa
                if (r2 == r3) goto L_0x0055
                goto L_0x01a4
            L_0x0055:
                r1 = 0
                r2 = 0
                java.lang.String r3 = "subscription"
                int r3 = r13.getIntExtra(r3, r7)
                java.lang.String r4 = "state"
                java.lang.String r4 = r13.getStringExtra(r4)
                if (r3 == r7) goto L_0x0069
                goto L_0x01a4
            L_0x0069:
                if (r4 != 0) goto L_0x0071
                com.android.internal.telephony.PhoneConstants$State r5 = com.android.internal.telephony.PhoneConstants.State.IDLE
                java.lang.String r4 = r5.toString()
            L_0x0071:
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r5 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r5 = r5.mContext
                java.lang.String r7 = "telecom"
                java.lang.Object r5 = r5.getSystemService(r7)
                android.telecom.TelecomManager r5 = (android.telecom.TelecomManager) r5
                if (r5 == 0) goto L_0x0084
                boolean r2 = r5.isInEmergencyCall()
            L_0x0084:
                java.lang.String r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r10 = new java.lang.StringBuilder
                r10.<init>()
                r10.append(r9)
                r10.append(r0)
                java.lang.String r9 = ", newCallState: "
                r10.append(r9)
                r10.append(r4)
                java.lang.String r9 = ", ine911: "
                r10.append(r9)
                r10.append(r2)
                java.lang.String r9 = r10.toString()
                android.util.Log.d(r7, r9)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r7 = r7.mIsInScbm
                boolean r7 = r7[r1]
                if (r7 == 0) goto L_0x00f0
                if (r2 == 0) goto L_0x00f0
                com.android.internal.telephony.PhoneConstants$State r7 = com.android.internal.telephony.PhoneConstants.State.OFFHOOK
                java.lang.String r7 = r7.toString()
                boolean r7 = r7.equals(r4)
                if (r7 == 0) goto L_0x00f0
                com.android.internal.telephony.PhoneConstants$State r7 = com.android.internal.telephony.PhoneConstants.State.OFFHOOK
                java.lang.String r7 = r7.toString()
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r9 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.String[] r9 = r9.mCallState
                r9 = r9[r1]
                boolean r7 = r7.equals(r9)
                if (r7 != 0) goto L_0x00f0
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r7 = r7.mIsInScbm
                r7[r1] = r8
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.Integer[] r9 = r7.mIntForHandler
                r9 = r9[r1]
                r7.removeMessages(r6, r9)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r6 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r6 = r6.mContext
                com.sec.internal.ims.servicemodules.sms.SmsUtil.broadcastSCBMState(r6, r8, r1)
            L_0x00f0:
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r6 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.String[] r6 = r6.mCallState
                r6[r1] = r4
                goto L_0x01a4
            L_0x00fa:
                java.lang.String r2 = "phone"
                int r1 = r13.getIntExtra(r2, r8)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r2 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                int r2 = r2.mMaxPhoneCount
                if (r1 >= r2) goto L_0x010b
                r2 = r1
                goto L_0x010c
            L_0x010b:
                r2 = r8
            L_0x010c:
                r1 = r2
                java.lang.String r2 = "android.telephony.extra.PHONE_IN_ECM_STATE"
                boolean r2 = r13.getBooleanExtra(r2, r8)
                java.lang.String r3 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                r4.append(r9)
                r4.append(r0)
                java.lang.String r5 = ", ecmState: "
                r4.append(r5)
                r4.append(r2)
                java.lang.String r5 = ", phoneId: "
                r4.append(r5)
                r4.append(r1)
                java.lang.String r4 = r4.toString()
                android.util.Log.d(r3, r4)
                if (r2 == 0) goto L_0x01a4
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r3 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r3 = r3.mIsInScbm
                boolean r3 = r3[r1]
                if (r3 == 0) goto L_0x01a4
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r3 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r3 = r3.mIsInScbm
                r3[r1] = r8
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r3 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.Integer[] r4 = r3.mIntForHandler
                r4 = r4[r1]
                r3.removeMessages(r6, r4)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r3 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r3 = r3.mContext
                com.sec.internal.ims.servicemodules.sms.SmsUtil.broadcastSCBMState(r3, r8, r1)
                goto L_0x01a4
            L_0x0160:
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r2 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean unused = r2.mStorageAvailable = r6
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r2 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                com.sec.internal.ims.servicemodules.sms.SmsLogger r2 = r2.mSmsLogger
                java.lang.String r3 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                r4.append(r9)
                r4.append(r0)
                java.lang.String r4 = r4.toString()
                r2.logAndAdd(r3, r4)
                goto L_0x01a4
            L_0x0182:
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r2 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean unused = r2.mStorageAvailable = r8
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r2 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                com.sec.internal.ims.servicemodules.sms.SmsLogger r2 = r2.mSmsLogger
                java.lang.String r3 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                r4.append(r9)
                r4.append(r0)
                java.lang.String r4 = r4.toString()
                r2.logAndAdd(r3, r4)
            L_0x01a4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
        }
    };
    /* access modifiers changed from: private */
    public String[] mCallState;
    Context mContext;
    private ArrayList<LinkedList<SmsEvent>> mEmergencyPendingQueue;
    private boolean[] mEmergencyRegiProcessiong;
    /* access modifiers changed from: private */
    public final ISmsServiceInterface mImsService;
    /* access modifiers changed from: private */
    public Integer[] mIntForHandler;
    private boolean[] mIsDeregisterTimerRunning;
    /* access modifiers changed from: private */
    public boolean[] mIsInScbm;
    private boolean mIsRetryIfNoSubmitReport = false;
    /* access modifiers changed from: private */
    public boolean mIsSamsungMsgApp = true;
    private String mLastMOContentType = null;
    ConcurrentHashMap<Integer, RemoteCallbackList<ISmsServiceEventListener>> mListeners = new ConcurrentHashMap<>();
    /* access modifiers changed from: private */
    public int mMaxPhoneCount = 1;
    /* access modifiers changed from: private */
    public MessagingAppInfoReceiver mMessagingAppInfoReceiver = null;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<Integer, SmsEvent> mPendingQueue = new ConcurrentHashMap<>();
    /* access modifiers changed from: private */
    public int mRetransCount = 0;
    /* access modifiers changed from: private */
    public String mSamsungMsgAppVersion = "";
    /* access modifiers changed from: private */
    public SmsLogger mSmsLogger = SmsLogger.getInstance();
    /* access modifiers changed from: private */
    public boolean mStorageAvailable = true;
    /* access modifiers changed from: private */
    public final TelephonyManager mTelephonyManager;
    private Handler mTimeoutHandler = null;

    private static class AlternativeService {
        String mAction;
        String mType;
    }

    static {
        String simpleName = SmsServiceModule.class.getSimpleName();
        NAME = simpleName;
        LOG_TAG = simpleName;
    }

    public static class AlternativeServiceXmlParser {
        public static AlternativeService parseXml(String xml) throws XPathExpressionException {
            AlternativeService svc = new AlternativeService();
            String access$100 = SmsServiceModule.LOG_TAG;
            Log.d(access$100, "AlternativeServiceXmlParser parseXml:" + xml);
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression expAlternativeService = xPath.compile("/ims-3gpp/alternative-service");
            XPathExpression expType = xPath.compile("type");
            XPathExpression expReason = xPath.compile("reason");
            XPathExpression expAction = xPath.compile("action");
            Node NodeAs = (Node) expAlternativeService.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODE);
            if (NodeAs == null) {
                return svc;
            }
            String strType = expType.evaluate(NodeAs);
            String strReason = expReason.evaluate(NodeAs);
            String strAction = expAction.evaluate(NodeAs);
            String strType2 = strType.trim();
            String strReason2 = strReason.trim();
            String strAction2 = strAction.trim();
            String access$1002 = SmsServiceModule.LOG_TAG;
            Log.d(access$1002, "parseXml:" + strType2 + "," + strReason2 + "," + strAction2);
            svc.mType = strType2;
            svc.mAction = strAction2;
            return svc;
        }
    }

    public SmsServiceModule(Looper looper, Context context, ISmsServiceInterface smsServiceInterface) {
        super(looper);
        int phoneCount = SimUtil.getPhoneCount();
        this.mMaxPhoneCount = phoneCount;
        this.mIntForHandler = new Integer[phoneCount];
        this.mEmergencyRegiProcessiong = new boolean[phoneCount];
        this.mIsInScbm = new boolean[phoneCount];
        this.mEmergencyPendingQueue = new ArrayList<>();
        int i = this.mMaxPhoneCount;
        this.mCallState = new String[i];
        this.mIsDeregisterTimerRunning = new boolean[i];
        for (int i2 = 0; i2 < this.mMaxPhoneCount; i2++) {
            this.mIntForHandler[i2] = Integer.valueOf(i2);
            this.mEmergencyRegiProcessiong[i2] = false;
            this.mIsInScbm[i2] = false;
            this.mEmergencyPendingQueue.add(new LinkedList());
            this.mCallState[i2] = PhoneConstants.State.IDLE.toString();
            this.mIsDeregisterTimerRunning[i2] = false;
        }
        this.mContext = context;
        this.mImsService = smsServiceInterface;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(com.sec.internal.constants.ims.os.PhoneConstants.PHONE_KEY);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ImsConstants.Intents.ACTION_DEVICE_STORAGE_FULL);
        filter.addAction(ImsConstants.Intents.ACTION_DEVICE_STORAGE_NOT_FULL);
        filter.addAction(ImsConstants.Intents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        filter.addAction("com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL");
        filter.addAction("android.intent.action.PHONE_STATE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public String[] getServicesRequiring() {
        return new String[]{"smsip"};
    }

    public void init() {
        super.init();
        super.start();
        boolean z = true;
        this.mImsService.registerForSMSEvent(this, 1, (Object) null);
        this.mImsService.registerForRrcConnectionEvent(this, 2, (Object) null);
        if (this.mMessagingAppInfoReceiver == null) {
            MessagingAppInfoReceiver messagingAppInfoReceiver = new MessagingAppInfoReceiver(this.mContext, this);
            this.mMessagingAppInfoReceiver = messagingAppInfoReceiver;
            messagingAppInfoReceiver.registerReceiver();
        }
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, 0) != 1) {
            z = false;
        }
        this.mIsSamsungMsgApp = z;
        this.mSamsungMsgAppVersion = this.mMessagingAppInfoReceiver.getMessagingAppVersion();
        this.mTimeoutHandler = new Handler(getLooper()) {
            public void handleMessage(Message msg) {
                int msgId;
                String access$100 = SmsServiceModule.LOG_TAG;
                Log.e(access$100, "message timeout - what : " + msg.what + ", obj : " + msg.obj + ", mRetransCount :" + SmsServiceModule.this.mRetransCount);
                SmsLogger access$200 = SmsServiceModule.this.mSmsLogger;
                StringBuilder sb = new StringBuilder();
                sb.append(SmsServiceModule.LOG_TAG);
                sb.append("_TIMEOUT");
                String sb2 = sb.toString();
                access$200.add(sb2, "message timeout - what : " + msg.what + ", obj : " + msg.obj);
                SmsEvent sendMessage = (SmsEvent) msg.obj;
                if (sendMessage == null) {
                    Log.e(SmsServiceModule.LOG_TAG, "the pending message doesn't exist");
                    return;
                }
                int phoneId = sendMessage.getImsRegistration() != null ? sendMessage.getImsRegistration().getPhoneId() : 0;
                SmsServiceModule.this.mPendingQueue.remove(Integer.valueOf(sendMessage.getMessageID()));
                Mno mno = SimUtil.getSimMno(phoneId);
                if (mno.isOrange() || mno.isTmobile()) {
                    int unused = SmsServiceModule.this.MAX_RETRANS_COUNT = 1;
                }
                int msgId2 = sendMessage.getMessageID();
                if (sendMessage.getContentType().equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
                    msgId = sendMessage.getTpMr();
                } else {
                    msgId = msgId2;
                }
                String access$1002 = SmsServiceModule.LOG_TAG;
                Log.d(access$1002, "msgId = " + sendMessage.getMessageID() + " tpMR = " + sendMessage.getTpMr());
                int i = msg.what;
                if (i != 1) {
                    if (i != 2) {
                        if (i == 3 && SmsServiceModule.this.mRetransCount < 1) {
                            SmsServiceModule.this.retryToSendMessage(phoneId, sendMessage);
                        }
                    } else if ((mno == Mno.DOCOMO || mno.isOrange() || mno.isTmobile()) && SmsServiceModule.this.mRetransCount >= SmsServiceModule.this.MAX_RETRANS_COUNT) {
                        if (mno.isOrange() || mno.isTmobile()) {
                            SmsUtil.sendISMOInfoToHQM(SmsServiceModule.this.mContext, DiagnosisConstants.RCSM_ORST_REGI, 404, (String) null, true, phoneId);
                            SmsServiceModule.this.onReceiveSMSAckInternal(phoneId, msgId, 404, sendMessage.getContentType(), (byte[]) null, SmsServiceModule.this.mRetransCount);
                            return;
                        }
                        SmsUtil.sendISMOInfoToHQM(SmsServiceModule.this.mContext, DiagnosisConstants.RCSM_ORST_REGI, 408, (String) null, true, phoneId);
                    } else if (!mno.isSprint() || SmsServiceModule.this.mRetransCount < SmsServiceModule.this.MAX_RETRANS_COUNT_SPR) {
                        SmsServiceModule.this.retryToSendMessage(phoneId, sendMessage);
                    } else {
                        SmsUtil.sendISMOInfoToHQM(SmsServiceModule.this.mContext, DiagnosisConstants.RCSM_ORST_REGI, 801, (String) null, true, phoneId);
                        SmsServiceModule.this.onReceiveSMSAckInternal(phoneId, msgId, 801, sendMessage.getContentType(), (byte[]) null, -1);
                    }
                } else if (sendMessage.getState() == 102) {
                    SmsUtil.sendISMOInfoToHQM(SmsServiceModule.this.mContext, DiagnosisConstants.RCSM_ORST_REGI, 0, "FF", true, phoneId);
                    SmsServiceModule.this.onReceiveSMSAckInternal(phoneId, msgId, 10003, sendMessage.getContentType(), (byte[]) null, -1);
                }
            }
        };
    }

    public void onConfigured(int phoneId) {
        Log.d(LOG_TAG, "onConfigured:");
        this.mEnabledFeatures[phoneId] = 0;
        if (SimUtil.getSimMno(phoneId).isOrange()) {
            TIMER_SUBMIT_REPORT = Id.NOTIFY_MISC_BASE_ID;
        } else {
            TIMER_SUBMIT_REPORT = 40000;
        }
    }

    public void onSimReady(int phoneId) {
        SmsUtil.broadcastDcnNumber(this.mContext, this.mDefaultPhoneId);
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        String str = LOG_TAG;
        Log.i(str, "Registered to SMS service. " + regiInfo);
        updateCapabilities(regiInfo.getPhoneId());
        if (this.mIsSamsungMsgApp) {
            this.mImsService.setMsgAppInfoToSipUa(regiInfo.getPhoneId(), this.mSamsungMsgAppVersion);
        } else {
            this.mImsService.setMsgAppInfoToSipUa(regiInfo.getPhoneId(), "");
        }
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        String str = LOG_TAG;
        Log.i(str, "Deregistered from SMS service. reason " + errorCode);
        updateCapabilities(regiInfo.getPhoneId());
        if (SimUtil.getSimMno(regiInfo.getPhoneId()) == Mno.BSNL && this.mLastMOContentType != null) {
            fallbackForSpecificReason(NOTI_DEREGISTERED);
        }
        super.onDeregistered(regiInfo, errorCode);
    }

    public void handleIntent(Intent intent) {
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String str = LOG_TAG;
        Log.i(str, "handleMessage() - what : " + msg.what);
        switch (msg.what) {
            case 1:
                handleSmsEvent((SmsEvent) ((AsyncResult) msg.obj).result);
                return;
            case 2:
                handleRRCConnection((RrcConnectionEvent) ((AsyncResult) msg.obj).result);
                return;
            case 3:
                SmsEvent sms = (SmsEvent) msg.obj;
                sendSMSOverIMS(sms.getEventType(), sms.getData(), sms.getSmscAddr(), sms.getContentType(), sms.getMessageID(), false);
                return;
            case 4:
                SmsEvent sms2 = (SmsEvent) msg.obj;
                ImsRegistry.getRegistrationManager().startEmergencyRegistration(sms2.getEventType(), obtainMessage(5, sms2));
                return;
            case 5:
                handleEmergencyRegisterDone((SmsEvent) msg.obj);
                return;
            case 6:
                handleEmergencyRegisterFail((SmsEvent) msg.obj);
                return;
            case 7:
                int phoneId = ((Integer) msg.obj).intValue();
                this.mIsInScbm[phoneId] = false;
                removeMessages(7, this.mIntForHandler[phoneId]);
                ImsRegistry.getRegistrationManager().stopEmergencyRegistration(phoneId);
                SmsUtil.broadcastSCBMState(this.mContext, false, phoneId);
                return;
            default:
                return;
        }
    }

    private void handleSmsEvent(SmsEvent event) {
        String str = LOG_TAG;
        Log.i(str, "handleSmsEvent coming " + event.toString());
        int eventType = event.getEventType();
        if (eventType == 11) {
            onReceiveOtherInfo(event);
        } else if (eventType != 12) {
            onReceiveSmsMessage(event);
        } else {
            onReceiveNotiInfo(event);
        }
    }

    private void handleRRCConnection(RrcConnectionEvent rrcEvent) {
        String str = LOG_TAG;
        Log.d(str, "rrcEvent.getEvent() : " + rrcEvent.getEvent());
        if (SimManagerFactory.getSimManager().getSimMno() == Mno.VZW) {
            if ((rrcEvent.getEvent() == RrcConnectionEvent.RrcEvent.REJECTED || rrcEvent.getEvent() == RrcConnectionEvent.RrcEvent.TIMER_EXPIRED) && this.mLastMOContentType != null) {
                fallbackForSpecificReason(800);
            }
        }
    }

    private void onReceiveSmsMessage(SmsEvent event) {
        int subId;
        int phoneId;
        String contentType = event.getContentType();
        int errorCode = GsmSmsUtil.get3gppRPError(event.getContentType(), event.getData());
        ImsRegistration regInfo = event.getImsRegistration();
        if (regInfo != null) {
            phoneId = regInfo.getPhoneId();
            subId = regInfo.getSubscriptionId();
        } else {
            phoneId = 0;
            subId = -1;
        }
        Log.i(LOG_TAG, "onReceiveSmsMessage: errorCode=" + errorCode);
        if (errorCode > 0 || GsmSmsUtil.isAck(contentType, event.getData())) {
            onReceiveAck(event, contentType, phoneId, subId, regInfo, errorCode);
        } else {
            onReceiveIncomingSms(event, contentType, phoneId, subId, regInfo);
        }
    }

    private void onReceiveIncomingSms(SmsEvent event, String contentType, int phoneId, int subId, ImsRegistration regInfo) {
        String imsi = TelephonyManagerExt.getSubscriberId(this.mTelephonyManager, subId);
        boolean isLimitedRegi = false;
        if (SimUtil.getSimMno(phoneId) == Mno.VZW && regInfo != null) {
            if (!TextUtils.isEmpty(imsi) && regInfo.isImsiBased(imsi)) {
                Log.d(LOG_TAG, "onReceiveIncomingSms: isLimitedRegi = true");
                isLimitedRegi = true;
            }
            if (regInfo.getImsProfile().hasEmergencySupport() && this.mIsInScbm[phoneId]) {
                Message msg = obtainMessage(7, this.mIntForHandler[phoneId]);
                removeMessages(7, this.mIntForHandler[phoneId]);
                sendMessageDelayed(msg, 300000);
            }
        }
        if (contentType.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
            if (!isLimitedRegi || event.getData() == null || GsmSmsUtil.isAdminMsg(GsmSmsUtil.get3gppTpduFromPdu(event.getData()))) {
                onReceive3GPPIncomingSms(event);
            } else {
                this.mImsService.sendSMSResponse(phoneId, event.getCallID(), 404);
                return;
            }
        } else if (!contentType.equals(CdmaSmsUtil.CONTENT_TYPE_3GPP2)) {
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "1", "SSM_onReceiveIncomingSms_noContentType", true, phoneId);
        } else if (isLimitedRegi && event.getData() != null && !CdmaSmsUtil.isAdminMsg(event.getData())) {
            this.mImsService.sendSMSResponse(phoneId, event.getCallID(), 404);
            return;
        } else if (!this.mStorageAvailable) {
            this.mSmsLogger.logAndAdd(LOG_TAG, "incoming sms but mStorageAvailable = false");
            this.mImsService.sendSMSResponse(phoneId, event.getCallID(), 480);
            onReceive3GPP2IncomingSms(event);
            return;
        } else {
            onReceive3GPP2IncomingSms(event);
        }
        this.mImsService.sendSMSResponse(phoneId, event.getCallID(), 200);
    }

    private void onReceive3GPPIncomingSms(SmsEvent event) {
        int phoneId;
        RemoteCallbackList<ISmsServiceEventListener> listeners;
        ImsRegistration reg = event.getImsRegistration();
        if (reg != null) {
            phoneId = reg.getPhoneId();
        } else {
            phoneId = 0;
        }
        if (event.getData() == null || event.getCallID() == null || event.getSmscAddr() == null) {
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "1", "SSM_onReceive3GPPIncomingSms_WrongFormat", true, phoneId);
            return;
        }
        byte[] tPdu = GsmSmsUtil.get3gppTpduFromPdu(event.getData());
        int i = 0;
        if (tPdu == null) {
            Log.e(LOG_TAG, "incoming tpdu is null. send RP Error report" + event.getCallID() + "] SmscAddr [" + event.getSmscAddr() + "]");
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "1", "SSM_onReceive3GPPIncomingSms_tPduNull", true, phoneId);
            String trimSmscAddr = GsmSmsUtil.trimSipAddr(event.getSmscAddr());
            byte[] deliverPdu = GsmSmsUtil.makeRPErrorPdu(event.getData());
            if (deliverPdu == null) {
                SmsUtil.sendSMOTInfoToHQM(this.mContext, "1", "SSM_onReceive3GPPIncomingSms_deliverPduNull", true, phoneId);
                return;
            }
            this.mLastMOContentType = GsmSmsUtil.CONTENT_TYPE_3GPP;
            ISmsServiceInterface iSmsServiceInterface = this.mImsService;
            String localUri = SmsUtil.getLocalUri(reg);
            String callID = event.getCallID();
            if (event.getImsRegistration() != null) {
                i = event.getImsRegistration().getHandle();
            }
            iSmsServiceInterface.sendMessage(trimSmscAddr, localUri, GsmSmsUtil.CONTENT_TYPE_3GPP, deliverPdu, true, callID, 0, i);
        } else if (event.getData().length <= 1) {
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "1", "SSM_onReceive3GPPIncomingSms_DataError", true, phoneId);
        } else {
            SmsEvent incomingMessage = new SmsEvent();
            incomingMessage.setContentType(event.getContentType());
            incomingMessage.setRpRef(event.getData()[1] & 255);
            incomingMessage.setSmscAddr(GsmSmsUtil.trimSipAddr(GsmSmsUtil.removeDisplayName(event.getSmscAddr())));
            incomingMessage.setMessageID(event.getMessageID() & 255);
            incomingMessage.setCallID(event.getCallID());
            incomingMessage.setData(tPdu);
            if (!(incomingMessage.getRpRef() == -1 || incomingMessage.getSmscAddr() == null)) {
                if (GsmSmsUtil.isStatusReport(tPdu)) {
                    incomingMessage.setMessageID(SmsUtil.getNewMsgId() & 255);
                    incomingMessage.setState(104);
                    Handler handler = this.mTimeoutHandler;
                    if (handler != null) {
                        handler.sendMessageDelayed(handler.obtainMessage(1, incomingMessage), 180000);
                    }
                } else {
                    incomingMessage.setMessageID(SmsUtil.getNewMsgId() & 255);
                    incomingMessage.setState(103);
                    byte[] tpPidDcs = GsmSmsUtil.getTPPidDcsFromPdu(tPdu);
                    if (tpPidDcs != null) {
                        incomingMessage.setTpPid(tpPidDcs[0]);
                        incomingMessage.setTpDcs(tpPidDcs[1]);
                        Log.i(LOG_TAG, "Incoming SMS new setMessageID : " + incomingMessage.getMessageID() + " TpPid : " + incomingMessage.getTpPid() + " TpDcs : " + incomingMessage.getTpDcs());
                    }
                    Handler handler2 = this.mTimeoutHandler;
                    if (handler2 != null) {
                        handler2.sendMessageDelayed(handler2.obtainMessage(1, incomingMessage), 180000);
                    }
                }
                this.mPendingQueue.put(Integer.valueOf(incomingMessage.getMessageID()), incomingMessage);
            }
            this.mSmsLogger.logAndAdd(LOG_TAG, "onReceive3GPPIncomingSms: " + incomingMessage);
            Log.i(LOG_TAG + '/' + phoneId, "onReceive3GPPIncomingSms");
            IMSLog.c(LogClass.SMS_RECEIVE_MSG_3GPP, phoneId + "," + incomingMessage.toKeyDump());
            SmsUtil.sendDailyReport(this.mContext, phoneId);
            if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (listeners = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
                try {
                    int i2 = listeners.beginBroadcast();
                    while (i2 > 0) {
                        int i3 = i2 - 1;
                        try {
                            listeners.getBroadcastItem(i3).onReceiveIncomingSMS(incomingMessage.getMessageID(), incomingMessage.getContentType(), incomingMessage.getData());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        i2 = i3;
                    }
                } catch (IllegalStateException e2) {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    listeners.finishBroadcast();
                    throw th;
                }
                listeners.finishBroadcast();
            }
        }
    }

    private void onReceive3GPP2IncomingSms(SmsEvent event) {
        RemoteCallbackList<ISmsServiceEventListener> listeners;
        ImsRegistration reg = event.getImsRegistration();
        int phoneId = 0;
        if (reg != null) {
            phoneId = reg.getPhoneId();
        }
        if (event.getData() == null) {
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "1", "SSM_onReceive3GPP2IncomingSms_WrongFormat", true, phoneId);
        } else if (!CdmaSmsUtil.isValid3GPP2PDU(event.getData())) {
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "1", "SSM_onReceive3GPP2IncomingSms_InvalidPdu", true, phoneId);
        } else {
            this.mSmsLogger.logAndAdd(LOG_TAG, "onReceive3GPP2IncomingSms: " + event);
            Log.i(LOG_TAG + '/' + phoneId, "onReceive3GPP2IncomingSms");
            IMSLog.c(LogClass.SMS_RECEIVE_MSG_3GPP2, phoneId + "," + event.toKeyDump());
            SmsUtil.sendDailyReport(this.mContext, phoneId);
            if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (listeners = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
                try {
                    int i = listeners.beginBroadcast();
                    while (i > 0) {
                        i--;
                        try {
                            listeners.getBroadcastItem(i).onReceiveIncomingSMS(event.getMessageID(), event.getContentType(), event.getData());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IllegalStateException e2) {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    listeners.finishBroadcast();
                    throw th;
                }
                listeners.finishBroadcast();
            }
        }
    }

    private void onReceiveAck(SmsEvent event, String contentType, int phoneId, int subId, ImsRegistration regInfo, int errorCode) {
        boolean result;
        SmsEvent smsEvent = event;
        String str = contentType;
        int i = errorCode;
        if (regInfo == null && event.getReasonCode() == 408) {
            this.m3GPP2SendingMsgId = -1;
            SmsUtil.sendISMOInfoToHQM(this.mContext, DiagnosisConstants.RCSM_ORST_REGI, 408, (String) null, false, phoneId);
            return;
        }
        boolean result2 = false;
        if (event.getData() == null) {
            result = onReceiveSipResponse(event);
        } else {
            if (str.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
                if (SimUtil.getSimMno(phoneId) != Mno.KT || !GsmSmsUtil.isRPErrorForRetransmission(errorCode)) {
                    result2 = onReceive3GPPSmsAck(event);
                } else {
                    SmsUtil.sendISMOInfoToHQM(this.mContext, "2", 0, String.format("%02X", new Object[]{Byte.valueOf((byte) i)}), true, phoneId);
                    result2 = onReceive3GPPSmsRpError(event);
                }
            } else if (str.equals(CdmaSmsUtil.CONTENT_TYPE_3GPP2)) {
                result = onReceive3GPP2SmsAck(event);
            }
            result = result2;
        }
        if (!result) {
            if (i > 0) {
                smsEvent.setReasonCode(GsmSmsUtil.getRilRPErrCode(errorCode));
                smsEvent.setData(GsmSmsUtil.get3gppTpduFromPdu(event.getData()));
                if (ImsGateConfig.isGateEnabled()) {
                    IMSLog.g("GATE", "<GATE-M>SMS_GENERIC_FAILURE</GATE-M>");
                }
            }
            Log.i(LOG_TAG + '/' + phoneId, "onReceiveAck");
            broadcastOnReceiveSMSAck(phoneId, event.getTpMr(), event.getReasonCode(), event.getContentType(), event.getData(), event.getRetryAfter());
            this.m3GPP2SendingMsgId = -1;
            return;
        }
        int i2 = phoneId;
    }

    private boolean onReceiveSipResponse(SmsEvent event) {
        int phoneId;
        int pending;
        int code;
        SmsEvent smsEvent = event;
        String callId = event.getCallID();
        boolean result = false;
        ImsRegistration regInfo = event.getImsRegistration();
        if (regInfo != null) {
            phoneId = regInfo.getPhoneId();
        } else {
            phoneId = 0;
        }
        Mno mno = SimUtil.getSimMno(phoneId);
        if (!mno.isEur() && event.getReasonCode() == 708) {
            smsEvent.setReasonCode(408);
        }
        this.mSmsLogger.logAndAdd(LOG_TAG, "onReceiveSipResponse: " + smsEvent);
        IMSLog.c(LogClass.SMS_RECEIVE_SIP_RESPONSE, phoneId + "," + event.toKeyDump());
        if (callId != null) {
            pending = SmsUtil.getMessageIdByCallId(this.mPendingQueue, event.getCallID());
        } else {
            pending = -1;
        }
        if (pending >= 0) {
            SmsEvent pendingMessage = this.mPendingQueue.remove(Integer.valueOf(pending));
            int state = pendingMessage.getState();
            if (state == 101) {
                return handleMOReceivingCallID(event, pendingMessage, regInfo, phoneId, mno);
            }
            if (state != 106) {
                return false;
            }
            handleMTReceivingDeliverReportAck(smsEvent, pendingMessage, regInfo, phoneId);
            return true;
        }
        if (event.getData() == null) {
            Log.i(LOG_TAG + '/' + phoneId, "onReceiveSipResponse");
            int code2 = event.getReasonCode();
            String reason = event.getReason();
            Mno mno2 = mno;
            SmsUtil.sendISMOInfoToHQM(this.mContext, "1", code2, (String) null, true, phoneId);
            if (mno2 != Mno.VZW || code2 != 503 || TextUtils.isEmpty(reason) || !reason.contains("Outage")) {
                code = code2;
            } else {
                code = 777;
            }
            int code3 = this.m3GPP2SendingMsgId;
            if (code3 < 0) {
                code3 = event.getMessageID();
            }
            this.m3GPP2SendingMsgId = -1;
            Mno mno3 = mno2;
            String str = reason;
            int i = phoneId;
            broadcastOnReceiveSMSAck(phoneId, code3, code, CdmaSmsUtil.CONTENT_TYPE_3GPP2, (byte[]) null, -1);
            result = true;
            if (event.getReasonCode() >= 300 && regInfo != null) {
                SmsUtil.onSipError(regInfo, event.getReasonCode(), event.getReason());
            }
        } else {
            Mno mno4 = mno;
        }
        return result;
    }

    private boolean onReceive3GPPSmsRpError(SmsEvent event) {
        ImsRegistration reg = event.getImsRegistration();
        int phoneId = 0;
        if (reg != null) {
            phoneId = reg.getPhoneId();
        }
        if (event.getData() != null) {
            SmsEvent pendingMessage = null;
            if (event.getData().length > 0) {
                pendingMessage = this.mPendingQueue.remove(Integer.valueOf(SmsUtil.getMessageIdByPdu(this.mPendingQueue, event.getData())));
            }
            if (pendingMessage == null) {
                Log.e(LOG_TAG, "unexpected RP-ERROR");
                return false;
            }
            SmsLogger smsLogger = this.mSmsLogger;
            String str = LOG_TAG;
            smsLogger.logAndAdd(str, "onReceive3GPPSmsRpError: " + pendingMessage);
            IMSLog.c(LogClass.SMS_RECEIVE_3GPP_RP_ERR, phoneId + "," + pendingMessage.toKeyDump());
            Handler handler = this.mTimeoutHandler;
            if (handler != null) {
                handler.removeMessages(1, pendingMessage);
                if (this.mIsRetryIfNoSubmitReport) {
                    this.mTimeoutHandler.removeMessages(2, pendingMessage);
                }
            }
            if (this.mRetransCount < 1) {
                Log.i(LOG_TAG, "retry to send message on RP-ERROR");
                Handler handler2 = this.mTimeoutHandler;
                if (handler2 != null) {
                    handler2.sendMessage(handler2.obtainMessage(3, pendingMessage));
                }
                this.mPendingQueue.put(Integer.valueOf(pendingMessage.getMessageID()), pendingMessage);
                return true;
            }
        }
        return false;
    }

    private boolean onReceive3GPPSmsAck(SmsEvent event) {
        int phoneId;
        int pending;
        SmsEvent pendingMessage;
        ConcurrentHashMap<Integer, RemoteCallbackList<ISmsServiceEventListener>> concurrentHashMap;
        ImsRegistration reg = event.getImsRegistration();
        if (reg != null) {
            phoneId = reg.getPhoneId();
        } else {
            phoneId = 0;
        }
        if (event.getData() != null) {
            if (event.getData().length > 0) {
                int pending2 = SmsUtil.getMessageIdByPdu(this.mPendingQueue, event.getData());
                pendingMessage = this.mPendingQueue.remove(Integer.valueOf(pending2));
                pending = pending2;
            } else {
                pendingMessage = null;
                pending = -1;
            }
            if (pendingMessage == null) {
                this.mSmsLogger.logAndAdd(LOG_TAG, "unexpected SUBMIT report - pendingMessage is null");
                return false;
            }
            int pendingState = pendingMessage.getState();
            if (pendingState < 100) {
                SmsEvent smsEvent = event;
            } else if (pendingState > 102) {
                SmsEvent smsEvent2 = event;
            } else {
                Handler handler = this.mTimeoutHandler;
                if (handler != null) {
                    handler.removeMessages(1, pendingMessage);
                    if (this.mIsRetryIfNoSubmitReport) {
                        this.mTimeoutHandler.removeMessages(2, pendingMessage);
                    }
                }
                pendingMessage.setData(GsmSmsUtil.get3gppTpduFromPdu(event.getData()));
                pendingMessage.setContentType(event.getContentType());
                pendingMessage.setRetryAfter(event.getRetryAfter());
                int errCode = GsmSmsUtil.get3gppRPError(event.getContentType(), event.getData());
                if (errCode > 0) {
                    SmsUtil.sendISMOInfoToHQM(this.mContext, "2", 0, String.format("%02X", new Object[]{Byte.valueOf((byte) errCode)}), true, phoneId);
                    pendingMessage.setReasonCode(GsmSmsUtil.getRilRPErrCode(errCode));
                } else {
                    SmsUtil.sendISMOInfoToHQM(this.mContext, "0", 0, "00", true, phoneId);
                    pendingMessage.setReasonCode(0);
                }
                this.mSmsLogger.logAndAdd(LOG_TAG, "onReceive3GPPSmsAck: " + pendingMessage);
                IMSLog.c(LogClass.SMS_RECEIVE_3GPP_ACK, phoneId + "," + pendingMessage.toKeyDump());
                if (GsmSmsUtil.isAck(event.getContentType(), event.getData())) {
                    ConcurrentHashMap<Integer, RemoteCallbackList<ISmsServiceEventListener>> concurrentHashMap2 = this.mListeners;
                    synchronized (concurrentHashMap2) {
                        try {
                            Log.i(LOG_TAG + '/' + phoneId, "onReceive3GPPSmsAck");
                            concurrentHashMap = concurrentHashMap2;
                            broadcastOnReceiveSMSAck(phoneId, pendingMessage.getTpMr(), pendingMessage.getReasonCode(), pendingMessage.getContentType(), pendingMessage.getData(), pendingMessage.getRetryAfter());
                            return true;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } else if (event.getTpMr() == 0) {
                    event.setTpMr(pendingMessage.getTpMr());
                } else {
                    SmsEvent smsEvent3 = event;
                }
            }
            this.mSmsLogger.logAndAdd(LOG_TAG, "unexpected SUBMIT report - pendingState is " + pendingState);
            this.mPendingQueue.put(Integer.valueOf(pending), pendingMessage);
            return false;
        }
        SmsEvent smsEvent4 = event;
        return false;
    }

    private boolean onReceive3GPP2SmsAck(SmsEvent event) {
        int phoneId;
        int reasonCode = event.getReasonCode();
        if (reasonCode == 100) {
            return true;
        }
        ImsRegistration reg = event.getImsRegistration();
        if (reg != null) {
            phoneId = reg.getPhoneId();
        } else {
            phoneId = 0;
        }
        int phoneId2 = this.m3GPP2SendingMsgId;
        if (phoneId2 < 0) {
            phoneId2 = event.getMessageID();
        }
        SmsUtil.sendISMOInfoToHQM(this.mContext, "1", reasonCode, (String) null, true, phoneId);
        Log.i(LOG_TAG + '/' + phoneId, "onReceive3GPP2SmsAck");
        int i = phoneId;
        broadcastOnReceiveSMSAck(phoneId, phoneId2, reasonCode, event.getContentType(), event.getData(), event.getRetryAfter());
        return true;
    }

    private void onReceiveNotiInfo(SmsEvent event) {
        int messageId = event.getMessageID();
        if (messageId >= 0) {
            SmsEvent pendingEvent = this.mPendingQueue.remove(Integer.valueOf(messageId));
            if (pendingEvent != null) {
                int state = pendingEvent.getState();
                if (state == 100) {
                    pendingEvent.setState(101);
                    pendingEvent.setCallID(event.getCallID());
                    this.mPendingQueue.put(Integer.valueOf(messageId), pendingEvent);
                } else if (state == 105) {
                    pendingEvent.setState(106);
                    pendingEvent.setCallID(event.getCallID());
                    this.mPendingQueue.put(Integer.valueOf(messageId), pendingEvent);
                }
            } else {
                Log.e(LOG_TAG, "no pending message");
            }
        }
    }

    private void onReceiveOtherInfo(SmsEvent event) {
        int phoneId;
        int messageId;
        int messageId2 = event.getMessageID();
        String contentType = event.getContentType();
        if (messageId2 >= 0 && event.getReasonCode() == NOTI_DEREGISTERED) {
            Log.e(LOG_TAG, "cannot send message as NOTI_DEREGISTERED");
            ImsRegistration reg = event.getImsRegistration();
            if (reg != null) {
                phoneId = reg.getPhoneId();
            } else {
                phoneId = 0;
            }
            if (contentType.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
                SmsEvent sendMessage = this.mPendingQueue.remove(Integer.valueOf(messageId2));
                if (sendMessage == null) {
                    Log.e(LOG_TAG, "no pending message");
                    return;
                }
                Log.d(LOG_TAG, "remove pending message");
                sendMessage.setReasonCode(NOTI_DEREGISTERED);
                sendMessage.setRetryAfter(-1);
                this.m3GPP2SendingMsgId = messageId2;
                messageId = sendMessage.getTpMr();
            } else {
                messageId = messageId2;
            }
            SmsUtil.sendISMOInfoToHQM(this.mContext, "1", NOTI_DEREGISTERED, (String) null, false, phoneId);
            Log.i(LOG_TAG + '/' + phoneId, "onReceiveOtherInfo");
            int i = phoneId;
            broadcastOnReceiveSMSAck(phoneId, messageId, NOTI_DEREGISTERED, contentType, (byte[]) null, -1);
            int i2 = messageId;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00bc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleMOReceivingCallID(com.sec.internal.ims.servicemodules.sms.SmsEvent r19, com.sec.internal.ims.servicemodules.sms.SmsEvent r20, com.sec.ims.ImsRegistration r21, int r22, com.sec.internal.constants.Mno r23) {
        /*
            r18 = this;
            r8 = r18
            r9 = r20
            r10 = r21
            int r0 = r19.getReasonCode()
            r11 = 1
            r1 = 100
            if (r0 != r1) goto L_0x0010
            return r11
        L_0x0010:
            int r0 = r19.getReasonCode()
            r1 = 200(0xc8, float:2.8E-43)
            r2 = 300(0x12c, float:4.2E-43)
            if (r0 < r1) goto L_0x004f
            int r0 = r19.getReasonCode()
            if (r0 >= r2) goto L_0x004f
            r0 = 102(0x66, float:1.43E-43)
            r9.setState(r0)
            android.os.Handler r0 = r8.mTimeoutHandler
            if (r0 == 0) goto L_0x0041
            boolean r1 = r8.mIsRetryIfNoSubmitReport
            if (r1 == 0) goto L_0x0041
            r1 = 2
            android.os.Message r1 = r0.obtainMessage(r1, r9)
            boolean r2 = r23.isSprint()
            if (r2 == 0) goto L_0x003b
            r2 = 10000(0x2710, double:4.9407E-320)
            goto L_0x003e
        L_0x003b:
            int r2 = TIMER_SUBMIT_REPORT
            long r2 = (long) r2
        L_0x003e:
            r0.sendMessageDelayed(r1, r2)
        L_0x0041:
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, com.sec.internal.ims.servicemodules.sms.SmsEvent> r0 = r8.mPendingQueue
            int r1 = r20.getMessageID()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r0.put(r1, r9)
            return r11
        L_0x004f:
            int r0 = r19.getReasonCode()
            if (r0 < r2) goto L_0x0101
            if (r10 == 0) goto L_0x0101
            android.content.Context r12 = r8.mContext
            int r14 = r19.getReasonCode()
            r15 = 0
            r16 = 1
            java.lang.String r13 = "1"
            r17 = r22
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendISMOInfoToHQM(r12, r13, r14, r15, r16, r17)
            r1 = 0
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.KT
            r12 = r23
            if (r12 != r0) goto L_0x00af
            int r0 = r19.getReasonCode()
            r2 = 504(0x1f8, float:7.06E-43)
            if (r0 != r2) goto L_0x00a4
            java.lang.String r0 = r19.getContentType()
            java.lang.String r2 = "application/3gpp-ims+xml"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x00a4
            java.lang.String r0 = r19.getContent()     // Catch:{ XPathExpressionException -> 0x00a2 }
            com.sec.internal.ims.servicemodules.sms.SmsServiceModule$AlternativeService r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.AlternativeServiceXmlParser.parseXml(r0)     // Catch:{ XPathExpressionException -> 0x00a2 }
            java.lang.String r2 = "restoration"
            java.lang.String r3 = r0.mType     // Catch:{ XPathExpressionException -> 0x00a2 }
            boolean r2 = r2.equals(r3)     // Catch:{ XPathExpressionException -> 0x00a2 }
            if (r2 == 0) goto L_0x00a0
            java.lang.String r2 = "initial-registration"
            java.lang.String r3 = r0.mAction     // Catch:{ XPathExpressionException -> 0x00a2 }
            boolean r2 = r2.equals(r3)     // Catch:{ XPathExpressionException -> 0x00a2 }
            if (r2 == 0) goto L_0x00a0
            r1 = 1
        L_0x00a0:
            r0 = r1
            goto L_0x00b0
        L_0x00a2:
            r0 = move-exception
            goto L_0x00af
        L_0x00a4:
            int r0 = r19.getReasonCode()
            r2 = 408(0x198, float:5.72E-43)
            if (r0 != r2) goto L_0x00af
            r1 = 1
            r0 = r1
            goto L_0x00b0
        L_0x00af:
            r0 = r1
        L_0x00b0:
            if (r0 == 0) goto L_0x00bc
            int r1 = r19.getReasonCode()
            java.lang.String r2 = "initial_registration"
            com.sec.internal.ims.servicemodules.sms.SmsUtil.onSipError(r10, r1, r2)
            goto L_0x00c7
        L_0x00bc:
            int r1 = r19.getReasonCode()
            java.lang.String r2 = r19.getReason()
            com.sec.internal.ims.servicemodules.sms.SmsUtil.onSipError(r10, r1, r2)
        L_0x00c7:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = LOG_TAG
            r1.append(r2)
            r2 = 47
            r1.append(r2)
            r13 = r22
            r1.append(r13)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "onReceiveSipResponse"
            android.util.Log.i(r1, r2)
            int r3 = r20.getTpMr()
            int r4 = r19.getReasonCode()
            java.lang.String r5 = r19.getContentType()
            byte[] r6 = r19.getData()
            int r7 = r19.getRetryAfter()
            r1 = r18
            r2 = r22
            r1.broadcastOnReceiveSMSAck(r2, r3, r4, r5, r6, r7)
            return r11
        L_0x0101:
            r13 = r22
            r12 = r23
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.handleMOReceivingCallID(com.sec.internal.ims.servicemodules.sms.SmsEvent, com.sec.internal.ims.servicemodules.sms.SmsEvent, com.sec.ims.ImsRegistration, int, com.sec.internal.constants.Mno):boolean");
    }

    private void handleMTReceivingDeliverReportAck(SmsEvent event, SmsEvent pendingMessage, ImsRegistration regInfo, int phoneId) {
        RemoteCallbackList<ISmsServiceEventListener> listeners;
        Handler handler = this.mTimeoutHandler;
        if (handler != null) {
            handler.removeMessages(1, pendingMessage);
        }
        if (event.getReasonCode() >= 300 && regInfo != null) {
            if (event.getRetryAfter() > 0) {
                this.mPendingQueue.put(Integer.valueOf(pendingMessage.getMessageID()), pendingMessage);
                Handler handler2 = this.mTimeoutHandler;
                if (handler2 != null) {
                    handler2.sendMessageDelayed(handler2.obtainMessage(1, pendingMessage), 180000);
                }
            }
            SmsUtil.onSipError(regInfo, event.getReasonCode(), event.getReason());
        }
        Log.i(LOG_TAG + '/' + phoneId, "onReceiveSipResponse");
        if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (listeners = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
            try {
                int i = listeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        listeners.getBroadcastItem(i).onReceiveSMSDeliveryReportAck(pendingMessage.getMessageID(), event.getReasonCode(), event.getRetryAfter());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            } catch (Throwable th) {
                listeners.finishBroadcast();
                throw th;
            }
            listeners.finishBroadcast();
        }
    }

    private void sendPendingEmergencySms(int phoneId) {
        Log.d(LOG_TAG, "sendPendingEmergencySms");
        LinkedList<SmsEvent> tempQueue = this.mEmergencyPendingQueue.get(phoneId);
        this.mEmergencyPendingQueue.set(phoneId, new LinkedList());
        while (!tempQueue.isEmpty()) {
            sendMessage(obtainMessage(3, tempQueue.remove()));
        }
    }

    private void failPendingEmergencySms(int phoneId) {
        Log.d(LOG_TAG, "failPendingEmergencySms");
        LinkedList<SmsEvent> tempQueue = this.mEmergencyPendingQueue.get(phoneId);
        this.mEmergencyPendingQueue.set(phoneId, new LinkedList());
        while (!tempQueue.isEmpty()) {
            SmsEvent sms = tempQueue.remove();
            onReceiveSMSAckInternal(phoneId, sms.getMessageID(), 10002, sms.getContentType(), (byte[]) null, -1);
        }
    }

    private void handleEmergencyRegisterDone(SmsEvent sms) {
        Log.d(LOG_TAG, "handleEmergencyRegisterDone");
        int phoneId = sms.getEventType();
        if (this.mEmergencyRegiProcessiong[phoneId]) {
            removeMessages(6, sms);
            if (getImsRegistration(phoneId, true) != null) {
                this.mEmergencyRegiProcessiong[phoneId] = false;
                sendPendingEmergencySms(phoneId);
                return;
            }
            Log.d(LOG_TAG, "handleEmergencyRegisterDone: Emergency Regi failed.");
            sendMessage(obtainMessage(6, sms));
        }
    }

    private void handleEmergencyRegisterFail(SmsEvent sms) {
        Log.d(LOG_TAG, "handleEmergencyRegisterFail");
        int phoneId = sms.getEventType();
        boolean[] zArr = this.mEmergencyRegiProcessiong;
        if (zArr[phoneId]) {
            zArr[phoneId] = false;
            failPendingEmergencySms(phoneId);
        }
    }

    public void registerForSMSStateChange(int phoneId, ISmsServiceEventListener listener) {
        Log.i(LOG_TAG + phoneId, "registerForSMSStateChange[" + phoneId + "]");
        if (!this.mListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        RemoteCallbackList<ISmsServiceEventListener> list = this.mListeners.get(Integer.valueOf(phoneId));
        if (list != null) {
            Log.i(LOG_TAG + phoneId, "registerForSMSStateChange register");
            list.register(listener);
        }
    }

    public void deRegisterForSMSStateChange(int phoneId, ISmsServiceEventListener listener) {
        RemoteCallbackList<ISmsServiceEventListener> list;
        Log.i(LOG_TAG + phoneId, "deRegisterForSMSStateChange[" + phoneId + "]");
        if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (list = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
            list.unregister(listener);
        }
    }

    public void handleEventDefaultAppChanged() {
        Log.d(LOG_TAG, "handleEventDefaultAppChanged");
        for (int phoneId = 0; phoneId < this.mTelephonyManager.getPhoneCount(); phoneId++) {
            if (isRegistered(phoneId)) {
                boolean z = true;
                if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, phoneId) != 1) {
                    z = false;
                }
                this.mIsSamsungMsgApp = z;
                String str = LOG_TAG;
                Log.i(str, "onChange[" + phoneId + "] : MessageApplication is changed. mIsSamsungMsgApp = " + this.mIsSamsungMsgApp + " mSamsungMsgAppVersion = " + this.mSamsungMsgAppVersion);
                if (this.mIsSamsungMsgApp) {
                    this.mImsService.setMsgAppInfoToSipUa(phoneId, this.mSamsungMsgAppVersion);
                } else {
                    this.mImsService.setMsgAppInfoToSipUa(phoneId, "");
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0151 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0152  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSMSOverIMS(int r36, byte[] r37, java.lang.String r38, java.lang.String r39, int r40, boolean r41) {
        /*
            r35 = this;
            r10 = r35
            r11 = r36
            r7 = r38
            r12 = r39
            r13 = r40
            com.sec.ims.ImsRegistration r8 = r35.getImsRegistration(r36)
            r14 = 1
            com.sec.ims.ImsRegistration r15 = r10.getImsRegistration(r11, r14)
            com.sec.internal.constants.Mno r9 = com.sec.internal.helper.SimUtil.getSimMno(r36)
            r16 = 0
            boolean r0 = com.sec.internal.helper.os.Debug.isProductShip()
            java.lang.String r1 = " destAddr="
            java.lang.String r6 = "sendSMSOverIMS: "
            r5 = 0
            if (r0 == 0) goto L_0x004d
            if (r7 == 0) goto L_0x004d
            int r0 = r38.length()
            r2 = 3
            if (r0 <= r2) goto L_0x004d
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r6)
            r3.append(r11)
            r3.append(r1)
            java.lang.String r1 = r7.substring(r5, r2)
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            android.util.Log.i(r0, r1)
            goto L_0x0067
        L_0x004d:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r6)
            r2.append(r11)
            r2.append(r1)
            r2.append(r7)
            java.lang.String r1 = r2.toString()
            android.util.Log.i(r0, r1)
        L_0x0067:
            r0 = -1
            if (r8 == 0) goto L_0x0071
            int r0 = r8.getSubscriptionId()
            r17 = r0
            goto L_0x0073
        L_0x0071:
            r17 = r0
        L_0x0073:
            com.sec.internal.ims.servicemodules.sms.SmsEvent r0 = new com.sec.internal.ims.servicemodules.sms.SmsEvent
            r0.<init>()
            r4 = r0
            r4.setContentType(r12)
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.VZW
            if (r9 != r0) goto L_0x00b8
            r0 = r35
            r1 = r17
            r2 = r36
            r3 = r40
            r18 = r4
            r4 = r39
            r5 = r8
            boolean r0 = r0.vzwSendSmsLimitedRegi(r1, r2, r3, r4, r5)
            if (r0 == 0) goto L_0x0094
            return
        L_0x0094:
            r0 = r35
            r1 = r38
            r2 = r36
            r3 = r40
            r4 = r37
            r5 = r18
            r20 = r6
            r6 = r15
            int r0 = r0.vzwSendSmsE911(r1, r2, r3, r4, r5, r6)
            if (r0 != r14) goto L_0x00ac
            r1 = r15
            r8 = r1
            goto L_0x00b0
        L_0x00ac:
            r1 = 2
            if (r0 != r1) goto L_0x00b0
            return
        L_0x00b0:
            java.lang.String r1 = r10.vzwSendSmsDestAddr(r7)
            r6 = r0
            r7 = r8
            r8 = r1
            goto L_0x00c3
        L_0x00b8:
            r18 = r4
            r20 = r6
            r6 = r16
            r34 = r8
            r8 = r7
            r7 = r34
        L_0x00c3:
            boolean r0 = com.sec.internal.ims.servicemodules.sms.SmsUtil.isProhibited(r7)
            java.lang.String r5 = "SSM_sendSMSOverIMS_notRegi"
            java.lang.String r4 = "0"
            if (r0 != 0) goto L_0x0224
            if (r7 != 0) goto L_0x00dd
            r14 = r4
            r23 = r7
            r24 = r9
            r21 = r15
            r16 = r18
            r15 = r5
            r9 = r6
            r6 = 0
            goto L_0x0230
        L_0x00dd:
            r3 = r18
            r3.setImsRegistration(r7)
            if (r8 == 0) goto L_0x013f
            java.lang.String r0 = "application/vnd.3gpp.sms"
            boolean r0 = r12.equals(r0)
            if (r0 == 0) goto L_0x0111
            r0 = r35
            r1 = r3
            r2 = r37
            r16 = r3
            r3 = r8
            r14 = r4
            r4 = r9
            r21 = r15
            r15 = r5
            r5 = r36
            r22 = r6
            r6 = r40
            r23 = r7
            r7 = r39
            r38 = r8
            r8 = r23
            r24 = r9
            r9 = r41
            com.sec.internal.ims.servicemodules.sms.SmsEvent r4 = r0.make3gppSMS(r1, r2, r3, r4, r5, r6, r7, r8, r9)
            r7 = r4
            goto L_0x014f
        L_0x0111:
            r16 = r3
            r14 = r4
            r22 = r6
            r23 = r7
            r38 = r8
            r24 = r9
            r21 = r15
            r15 = r5
            java.lang.String r0 = "application/vnd.3gpp2.sms"
            boolean r0 = r12.equals(r0)
            if (r0 == 0) goto L_0x014d
            r0 = r35
            r1 = r16
            r2 = r37
            r3 = r38
            r4 = r24
            r5 = r36
            r6 = r40
            r7 = r39
            r8 = r23
            com.sec.internal.ims.servicemodules.sms.SmsEvent r4 = r0.make3gpp2SMS(r1, r2, r3, r4, r5, r6, r7, r8)
            r7 = r4
            goto L_0x014f
        L_0x013f:
            r16 = r3
            r14 = r4
            r22 = r6
            r23 = r7
            r38 = r8
            r24 = r9
            r21 = r15
            r15 = r5
        L_0x014d:
            r7 = r16
        L_0x014f:
            if (r7 != 0) goto L_0x0152
            return
        L_0x0152:
            com.sec.ims.util.NameAddr r0 = r23.getPreferredImpu()
            if (r0 != 0) goto L_0x017d
            android.content.Context r0 = r10.mContext
            r2 = 999(0x3e7, float:1.4E-42)
            r3 = 0
            r4 = 0
            java.lang.String r1 = "1"
            r5 = r36
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendISMOInfoToHQM(r0, r1, r2, r3, r4, r5)
            android.content.Context r0 = r10.mContext
            r6 = 0
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendSMOTInfoToHQM(r0, r14, r15, r6, r11)
            r10.m3GPP2SendingMsgId = r13
            r3 = 999(0x3e7, float:1.4E-42)
            r5 = 0
            r6 = -1
            r0 = r35
            r1 = r36
            r2 = r40
            r4 = r39
            r0.broadcastOnReceiveSMSAck(r1, r2, r3, r4, r5, r6)
            return
        L_0x017d:
            r6 = 0
            java.lang.String r0 = com.sec.internal.ims.servicemodules.sms.SmsUtil.getLocalUri(r23)
            r7.setLocalUri(r0)
            r10.mRetransCount = r6
            java.lang.String r0 = r7.getContentType()
            r10.mLastMOContentType = r0
            com.sec.internal.ims.servicemodules.sms.SmsLogger r0 = r10.mSmsLogger
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r3 = r20
            r2.append(r3)
            r2.append(r7)
            java.lang.String r2 = r2.toString()
            r0.logAndAdd(r1, r2)
            java.lang.String r0 = ""
            r8 = r38
            if (r8 == 0) goto L_0x01b3
            java.lang.String r1 = "(?<=.{2}).(?=.{2})"
            java.lang.String r2 = ""
            java.lang.String r0 = r8.replaceAll(r1, r2)
        L_0x01b3:
            r1 = 1342177281(0x50000001, float:8.5899356E9)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r11)
            java.lang.String r3 = ","
            r2.append(r3)
            r2.append(r0)
            r2.append(r3)
            java.lang.String r3 = r7.toKeyDump()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.c(r1, r2)
            com.sec.internal.ims.servicemodules.sms.ISmsServiceInterface r1 = r10.mImsService
            java.lang.String r26 = r7.getSmscAddr()
            java.lang.String r27 = r7.getLocalUri()
            java.lang.String r28 = r7.getContentType()
            byte[] r29 = r7.getData()
            r30 = 0
            r31 = 0
            int r32 = r7.getMessageID()
            int r33 = r23.getHandle()
            r25 = r1
            r25.sendMessage(r26, r27, r28, r29, r30, r31, r32, r33)
            r9 = r22
            r1 = 1
            if (r9 != r1) goto L_0x0223
            java.lang.Integer[] r1 = r10.mIntForHandler
            r1 = r1[r11]
            r2 = 7
            android.os.Message r1 = r10.obtainMessage(r2, r1)
            java.lang.Integer[] r3 = r10.mIntForHandler
            r3 = r3[r11]
            r10.removeMessages(r2, r3)
            r2 = 300000(0x493e0, double:1.482197E-318)
            r10.sendMessageDelayed(r1, r2)
            boolean[] r2 = r10.mIsInScbm
            boolean r3 = r2[r11]
            if (r3 != 0) goto L_0x0223
            r3 = 1
            r2[r11] = r3
            android.content.Context r2 = r10.mContext
            com.sec.internal.ims.servicemodules.sms.SmsUtil.broadcastSCBMState(r2, r3, r11)
        L_0x0223:
            return
        L_0x0224:
            r14 = r4
            r23 = r7
            r24 = r9
            r21 = r15
            r16 = r18
            r15 = r5
            r9 = r6
            r6 = 0
        L_0x0230:
            r0 = 999(0x3e7, float:1.4E-42)
            com.sec.internal.interfaces.ims.core.ISimManager r7 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r36)
            if (r7 == 0) goto L_0x023d
            com.sec.internal.constants.Mno r1 = r7.getNetMno()
            goto L_0x023e
        L_0x023d:
            r1 = 0
        L_0x023e:
            r5 = r1
            if (r23 == 0) goto L_0x0249
            boolean r1 = r23.isProhibited()
            if (r1 == 0) goto L_0x0249
            r0 = 777(0x309, float:1.089E-42)
        L_0x0249:
            java.lang.String r1 = "911"
            boolean r1 = r1.equals(r8)
            if (r1 == 0) goto L_0x025a
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.VZW
            if (r5 != r1) goto L_0x025a
            r0 = 10002(0x2712, float:1.4016E-41)
            r18 = r0
            goto L_0x025c
        L_0x025a:
            r18 = r0
        L_0x025c:
            r10.m3GPP2SendingMsgId = r13
            android.content.Context r0 = r10.mContext
            r2 = 999(0x3e7, float:1.4E-42)
            r3 = 0
            r4 = 0
            java.lang.String r1 = "1"
            r19 = r5
            r5 = r36
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendISMOInfoToHQM(r0, r1, r2, r3, r4, r5)
            android.content.Context r0 = r10.mContext
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendSMOTInfoToHQM(r0, r14, r15, r6, r11)
            r5 = 0
            r6 = -1
            r0 = r35
            r1 = r36
            r2 = r40
            r3 = r18
            r4 = r39
            r0.broadcastOnReceiveSMSAck(r1, r2, r3, r4, r5, r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.sendSMSOverIMS(int, byte[], java.lang.String, java.lang.String, int, boolean):void");
    }

    private SmsEvent make3gppSMS(SmsEvent sendMessage, byte[] pdu, String destAddr, Mno mno, int phoneId, int msgId, String contentType, ImsRegistration regInfo, boolean isSMMA) {
        SmsEvent smsEvent = sendMessage;
        byte[] bArr = pdu;
        String str = destAddr;
        Mno mno2 = mno;
        int i = phoneId;
        int i2 = msgId;
        ImsRegistration imsRegistration = regInfo;
        boolean z = isSMMA;
        smsEvent.setRpRef(SmsUtil.getIncreasedRPRef());
        String scaForRpDa = GsmSmsUtil.getScaForRpDa(z, bArr, str, mno2);
        if ("noSCA".equals(scaForRpDa)) {
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "0", "SSM_sendSMSOverIMS_emptySCA", true, i);
            String str2 = scaForRpDa;
            onReceiveSMSAckInternal(phoneId, msgId, 10001, contentType, (byte[]) null, -1);
            return null;
        }
        String scaForRpDa2 = scaForRpDa;
        String sca = GsmSmsUtil.getSca(scaForRpDa2, str, mno2, imsRegistration);
        if (!z) {
            String scaForRpDa3 = mno2 == Mno.VZW ? scaForRpDa2 : sca;
            smsEvent.setData(GsmSmsUtil.get3gppPduFromTpdu(bArr, sendMessage.getRpRef(), GsmSmsUtil.removeSipPrefix(scaForRpDa3), ""));
            String str3 = scaForRpDa3;
        } else {
            smsEvent.setData(GsmSmsUtil.getRpSMMAPdu(sendMessage.getRpRef()));
        }
        String str4 = sca;
        String str5 = "0";
        String sca2 = GsmSmsUtil.getScaFromPsismscPSI(this.mContext, sca, mno, this.mTelephonyManager, phoneId, regInfo);
        if (mno2 != Mno.LGU || !"noPSI".equals(sca2)) {
            if (mno2 == Mno.DOCOMO || mno.isOrange() || mno.isSprint() || mno.isTmobile()) {
                this.mIsRetryIfNoSubmitReport = true;
            }
            smsEvent.setSmscAddr(SmsUtil.getNetworkPreferredUri(imsRegistration, sca2, mno2 == Mno.ATT || mno2 == Mno.VZW || mno2 == Mno.CU));
            if (!z) {
                smsEvent.setMessageID(SmsUtil.getNewMsgId() & 255);
                smsEvent.setTpMr(GsmSmsUtil.getTPMRFromPdu(pdu));
                int i3 = msgId;
            } else {
                int i4 = msgId;
                smsEvent.setMessageID(i4);
                smsEvent.setTpMr(i4);
            }
            if (this.mPendingQueue.containsKey(Integer.valueOf(sendMessage.getMessageID()))) {
                Log.e(LOG_TAG, "send message already pending");
            } else {
                smsEvent.setState(100);
                Handler handler = this.mTimeoutHandler;
                if (handler != null) {
                    handler.sendMessageDelayed(handler.obtainMessage(1, smsEvent), 180000);
                }
                this.mPendingQueue.put(Integer.valueOf(sendMessage.getMessageID()), smsEvent);
            }
            return smsEvent;
        }
        SmsUtil.sendSMOTInfoToHQM(this.mContext, str5, "SSM_sendSMSOverIMS_LguNoPSI", true, i);
        return null;
    }

    private SmsEvent make3gpp2SMS(SmsEvent sendMessage, byte[] pdu, String destAddr, Mno mno, int phoneId, int msgId, String contentType, ImsRegistration regInfo) {
        SmsEvent smsEvent = sendMessage;
        Mno mno2 = mno;
        int i = phoneId;
        int i2 = msgId;
        try {
            try {
                smsEvent.setSmscAddr(SmsUtil.getNetworkPreferredUri(regInfo, destAddr, mno2 == Mno.VZW));
                sendMessage.setData(pdu);
                if (mno2 != Mno.VZW) {
                    byte[] bArr = pdu;
                } else if (pdu.length > 256) {
                    SmsUtil.sendSMOTInfoToHQM(this.mContext, "0", "SSM_sendSMSOverIMS_overSize", true, i);
                    return null;
                }
                smsEvent.setMessageID(i2);
                this.m3GPP2SendingMsgId = i2;
                return smsEvent;
            } catch (NullPointerException e) {
                e = e;
                byte[] bArr2 = pdu;
                e.printStackTrace();
                SmsUtil.sendSMOTInfoToHQM(this.mContext, "0", "SSM_sendSMSOverIMS_AddrErr", false, i);
                onReceiveSMSAckInternal(phoneId, msgId, 10001, contentType, (byte[]) null, -1);
                return null;
            }
        } catch (NullPointerException e2) {
            e = e2;
            byte[] bArr3 = pdu;
            String str = destAddr;
            ImsRegistration imsRegistration = regInfo;
            e.printStackTrace();
            SmsUtil.sendSMOTInfoToHQM(this.mContext, "0", "SSM_sendSMSOverIMS_AddrErr", false, i);
            onReceiveSMSAckInternal(phoneId, msgId, 10001, contentType, (byte[]) null, -1);
            return null;
        }
    }

    private boolean vzwSendSmsLimitedRegi(int subId, int phoneId, int msgId, String contentType, ImsRegistration regInfo) {
        String imsi = TelephonyManagerExt.getSubscriberId(this.mTelephonyManager, subId);
        if (regInfo == null || TextUtils.isEmpty(imsi) || !regInfo.isImsiBased(imsi)) {
            return false;
        }
        Log.d(LOG_TAG, "Limited Regi Mode, fallback to 1xRTT");
        onReceiveSMSAckInternal(phoneId, msgId, 10004, contentType, (byte[]) null, -1);
        return true;
    }

    private int vzwSendSmsE911(String destAddr, int phoneId, int msgId, byte[] pdu, SmsEvent sendMessage, ImsRegistration eRegInfo) {
        if (!"911".equals(destAddr) || SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 29) {
            return 0;
        }
        String str = LOG_TAG;
        Log.d(str, "sendSMSOverIMS: isVzwE911 = true, mEmergencyRegiProcessiong = " + this.mEmergencyRegiProcessiong[phoneId]);
        if (eRegInfo != null) {
            Log.d(LOG_TAG, "sendSMSOverIMS: regInfo = eRegInfo");
            return 1;
        }
        sendMessage.setEventType(phoneId);
        sendMessage.setMessageID(msgId);
        sendMessage.setData(pdu);
        sendMessage.setSmscAddr(destAddr);
        this.mEmergencyPendingQueue.get(phoneId).add(sendMessage);
        boolean[] zArr = this.mEmergencyRegiProcessiong;
        if (zArr[phoneId]) {
            return 2;
        }
        zArr[phoneId] = true;
        sendMessage(obtainMessage(4, sendMessage));
        sendMessageDelayed(obtainMessage(6, sendMessage), 10000);
        return 2;
    }

    private String vzwSendSmsDestAddr(String destAddr) {
        if (destAddr == null || destAddr.length() != 14 || !destAddr.startsWith("0111") || !GsmSmsUtil.isNanp(destAddr.substring(4))) {
            return destAddr;
        }
        Log.i(LOG_TAG, "6.5.2b is applied");
        return destAddr.substring(3);
    }

    public void sendSMSResponse(boolean isSuccess, int responseCode) {
    }

    public void sendDeliverReport(int phoneId, byte[] data) {
        RemoteCallbackList<ISmsServiceEventListener> listeners;
        int tpDcs;
        int tpPid;
        int i = phoneId;
        byte[] bArr = data;
        if (bArr != null && bArr.length >= 4) {
            int msgId = bArr[2] & 255;
            SmsEvent pendingMessage = this.mPendingQueue.remove(Integer.valueOf(msgId));
            if (pendingMessage != null) {
                Handler handler = this.mTimeoutHandler;
                if (handler != null) {
                    handler.removeMessages(1, pendingMessage);
                }
                ImsRegistration regInfo = getImsRegistration(phoneId);
                if (regInfo == null || regInfo.getPreferredImpu() == null) {
                    Log.e(LOG_TAG, "sendDeliverReport() called. but not registered IMS");
                    Log.i(LOG_TAG + '/' + i, "sendDeliverReport: msgId = " + msgId);
                    if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (listeners = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
                        try {
                            int i2 = listeners.beginBroadcast();
                            while (i2 > 0) {
                                int i3 = i2 - 1;
                                try {
                                    listeners.getBroadcastItem(i3).onReceiveSMSDeliveryReportAck(msgId, NOTI_DEREGISTERED, -1);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                i2 = i3;
                            }
                        } catch (IllegalStateException e2) {
                            e2.printStackTrace();
                        } catch (Throwable th) {
                            listeners.finishBroadcast();
                            throw th;
                        }
                        listeners.finishBroadcast();
                    }
                } else if (pendingMessage.getRpRef() == -1 || pendingMessage.getCallID() == null || pendingMessage.getSmscAddr() == null) {
                    Log.e(LOG_TAG, "sendDeliverReport wrong format");
                } else {
                    int tpPid2 = pendingMessage.getTpPid();
                    int tpDcs2 = pendingMessage.getTpDcs();
                    if ((tpPid2 & 63) == 63 && (tpDcs2 & 2) == 2) {
                        Log.i(LOG_TAG, "sendDeliverReport() set TP-PID and TP-DCS");
                        tpPid = tpPid2;
                        tpDcs = tpDcs2;
                    } else {
                        Log.i(LOG_TAG, "sendDeliverReport() do not set TP-PID and TP-DCS");
                        tpPid = 0;
                        tpDcs = 0;
                    }
                    pendingMessage.setData(GsmSmsUtil.getDeliverReportFromPdu(i, pendingMessage.getRpRef(), bArr, tpPid, tpDcs));
                    pendingMessage.setState(105);
                    pendingMessage.setImsRegistration(regInfo);
                    pendingMessage.setLocalUri(SmsUtil.getLocalUri(regInfo));
                    Handler handler2 = this.mTimeoutHandler;
                    if (handler2 != null) {
                        handler2.sendMessageDelayed(handler2.obtainMessage(1, pendingMessage), 180000);
                    }
                    this.mPendingQueue.put(Integer.valueOf(msgId), pendingMessage);
                    this.mSmsLogger.logAndAdd(LOG_TAG, "sendDeliverReport: " + pendingMessage);
                    IMSLog.c(LogClass.SMS_SEND_DELIVER_REPROT, i + "," + pendingMessage.toKeyDump());
                    int i4 = tpDcs;
                    this.mImsService.sendMessage(pendingMessage.getSmscAddr(), pendingMessage.getLocalUri(), pendingMessage.getContentType(), pendingMessage.getData(), false, pendingMessage.getCallID(), msgId, pendingMessage.getImsRegistration().getHandle());
                    this.mLastMOContentType = pendingMessage.getContentType();
                }
            } else {
                Log.e(LOG_TAG, "sendDeliverReport no incoming Message to send DeliverReport!");
            }
        }
    }

    public boolean getSmsFallback(int phoneId) {
        boolean smsFallback = ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.RCS.ENABLE_DEFAULT_SMS_FALLBACK, false);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "getSmsFallback: " + smsFallback);
        return smsFallback;
    }

    /* access modifiers changed from: private */
    public boolean isRegistered(int phoneId) {
        return getImsRegistration(phoneId) != null;
    }

    public void onMessagingAppPackageReplaced() {
        post(new Runnable() {
            public void run() {
                if (SmsServiceModule.this.mMessagingAppInfoReceiver != null) {
                    SmsServiceModule smsServiceModule = SmsServiceModule.this;
                    String unused = smsServiceModule.mSamsungMsgAppVersion = smsServiceModule.mMessagingAppInfoReceiver.getMessagingAppVersion();
                    String access$100 = SmsServiceModule.LOG_TAG;
                    Log.i(access$100, "onMessagingAppPackageReplaced: " + SmsServiceModule.this.mSamsungMsgAppVersion);
                    for (int phoneId = 0; phoneId < SmsServiceModule.this.mTelephonyManager.getPhoneCount(); phoneId++) {
                        if (SmsServiceModule.this.isRegistered(phoneId)) {
                            SmsServiceModule smsServiceModule2 = SmsServiceModule.this;
                            boolean z = true;
                            if (DmConfigHelper.getImsSwitchValue(smsServiceModule2.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, phoneId) != 1) {
                                z = false;
                            }
                            boolean unused2 = smsServiceModule2.mIsSamsungMsgApp = z;
                            if (SmsServiceModule.this.mIsSamsungMsgApp) {
                                SmsServiceModule.this.mImsService.setMsgAppInfoToSipUa(phoneId, SmsServiceModule.this.mSamsungMsgAppVersion);
                            } else {
                                SmsServiceModule.this.mImsService.setMsgAppInfoToSipUa(phoneId, "");
                            }
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void retryToSendMessage(int phoneId, SmsEvent sendMessage) {
        Log.i(LOG_TAG, "retry to send message");
        if (!isRegistered(phoneId)) {
            sendMessage.setReasonCode(NOTI_DEREGISTERED);
            sendMessage.setRetryAfter(-1);
            onReceiveSmsMessage(sendMessage);
            return;
        }
        byte[] pdu = sendMessage.getData();
        GsmSmsUtil.set3gppTPRD(pdu);
        Log.i(LOG_TAG, sendMessage.toString());
        this.mImsService.sendMessage(sendMessage.getSmscAddr(), sendMessage.getLocalUri(), sendMessage.getContentType(), pdu, false, (String) null, sendMessage.getMessageID(), sendMessage.getImsRegistration() != null ? sendMessage.getImsRegistration().getHandle() : 0);
        sendMessage.setState(100);
        Handler handler = this.mTimeoutHandler;
        if (handler != null) {
            handler.sendMessageDelayed(handler.obtainMessage(1, sendMessage), 180000);
        }
        this.mPendingQueue.put(Integer.valueOf(sendMessage.getMessageID()), sendMessage);
        this.mRetransCount++;
    }

    private void fallbackForSpecificReason(int reason) {
        if (this.mLastMOContentType.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
            for (Integer intValue : this.mPendingQueue.keySet()) {
                int pending = intValue.intValue();
                if (pending >= 0) {
                    SmsEvent pendingMessage = this.mPendingQueue.remove(Integer.valueOf(pending));
                    if (pendingMessage != null) {
                        pendingMessage.setReasonCode(reason);
                        if (pendingMessage.getData() != null) {
                            String str = LOG_TAG;
                            Log.i(str, "Fallback 3gpp message with reason " + reason);
                            Handler handler = this.mTimeoutHandler;
                            if (handler != null && handler.hasMessages(1, Integer.valueOf(pending))) {
                                this.mTimeoutHandler.removeMessages(1, Integer.valueOf(pending));
                            }
                            Handler handler2 = this.mTimeoutHandler;
                            if (handler2 != null && this.mIsRetryIfNoSubmitReport && handler2.hasMessages(2, Integer.valueOf(pending))) {
                                this.mTimeoutHandler.removeMessages(2, Integer.valueOf(pending));
                            }
                            ImsRegistration reg = pendingMessage.getImsRegistration();
                            int phoneId = 0;
                            if (reg != null) {
                                phoneId = reg.getPhoneId();
                            }
                            broadcastOnReceiveSMSAck(phoneId, pendingMessage.getTpMr(), pendingMessage.getReasonCode(), pendingMessage.getContentType(), GsmSmsUtil.get3gppTpduFromPdu(pendingMessage.getData()), pendingMessage.getRetryAfter());
                        }
                    } else {
                        return;
                    }
                }
            }
        } else if (this.mLastMOContentType.equals(CdmaSmsUtil.CONTENT_TYPE_3GPP2) && this.m3GPP2SendingMsgId != -1) {
            int saveMsgId = this.m3GPP2SendingMsgId;
            this.m3GPP2SendingMsgId = -1;
            String str2 = LOG_TAG;
            Log.i(str2, "Fallback 3gpp2 message with reason " + reason);
            broadcastOnReceiveSMSAck(0, saveMsgId, 800, CdmaSmsUtil.CONTENT_TYPE_3GPP2, (byte[]) null, -1);
        }
    }

    /* access modifiers changed from: private */
    public void onReceiveSMSAckInternal(int phoneId, int messageID, int reasonCode, String contentType, byte[] data, int retryAfter) {
        Log.i(LOG_TAG + '/' + phoneId, "onReceiveSMSAckInternal: " + reasonCode);
        broadcastOnReceiveSMSAck(phoneId, messageID, reasonCode, contentType, data, retryAfter);
    }

    private synchronized void broadcastOnReceiveSMSAck(int phoneId, int messageID, int reasonCode, String contentType, byte[] data, int retryAfter) {
        RemoteCallbackList<ISmsServiceEventListener> listeners;
        Log.d(LOG_TAG + '/' + phoneId, "broadcastOnReceiveSMSAck: " + reasonCode);
        if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (listeners = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
            try {
                int i = listeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        listeners.getBroadcastItem(i).onReceiveSMSAck(messageID, reasonCode, contentType, data, retryAfter);
                    } catch (RemoteException e) {
                        try {
                            e.printStackTrace();
                        } catch (IllegalStateException e2) {
                            e = e2;
                        } catch (Throwable th) {
                            th = th;
                            listeners.finishBroadcast();
                            throw th;
                        }
                    }
                }
                listeners.finishBroadcast();
            } catch (IllegalStateException e3) {
                e = e3;
                try {
                    e.printStackTrace();
                    listeners.finishBroadcast();
                } catch (Throwable th2) {
                    th = th2;
                    listeners.finishBroadcast();
                    throw th;
                }
            }
        }
    }

    public boolean isSmsOverIpEnabled(int phoneId) {
        ImsRegistration regInfo = getImsRegistration(phoneId);
        String str = LOG_TAG;
        Log.i(str, "regInfo: " + regInfo);
        if (regInfo == null || !isRunning()) {
            Log.i(LOG_TAG, "disallow sms Service");
            return false;
        }
        if (regInfo.hasService("smsip")) {
            if (SimUtil.getSimMno(phoneId) == Mno.ORANGE) {
                return true;
            }
            if (SmsUtil.disallowReregistration(phoneId)) {
                if (SmsUtil.isServiceAvailable(this.mTelephonyManager, phoneId)) {
                    return true;
                }
            } else if (this.mIsDeregisterTimerRunning[phoneId]) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean isVolteSupported(int phoneId) {
        ImsRegistration regInfo = getImsRegistration(phoneId);
        if (regInfo == null) {
            return false;
        }
        String str = LOG_TAG;
        Log.d(str, "IsVolteSupported= " + regInfo.hasService("mmtel"));
        return regInfo.hasService("mmtel");
    }

    public void updateCapabilities(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "Sms Service updateCapabilities [" + phoneId + "]");
        int[] capabilities = {8};
        boolean[] capables = new boolean[capabilities.length];
        Arrays.fill(capables, false);
        capables[0] = isSmsOverIpEnabled(phoneId);
        ImsRegistry.getGoogleImsAdaptor().updateCapabilities(phoneId, capabilities, capables);
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int phoneId) {
        ImsFeature.Capabilities capabilities = new ImsFeature.Capabilities();
        if (isSmsOverIpEnabled(phoneId)) {
            String str = LOG_TAG;
            Log.i(str, "Sms Service queryCapabilityStatus[" + phoneId + "]: addCapabilities CAPABILITY_TYPE_SMS");
            capabilities.addCapabilities(8);
        } else {
            String str2 = LOG_TAG;
            Log.i(str2, "Sms Service queryCapabilityStatus[" + phoneId + "]: removeCapabilities CAPABILITY_TYPE_SMS");
            capabilities.removeCapabilities(8);
        }
        return capabilities;
    }

    public void setDelayedDeregisterTimerRunning(int phoneId, boolean deregiTimerRunning) {
        this.mIsDeregisterTimerRunning[phoneId] = deregiTimerRunning;
        updateCapabilities(phoneId);
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        String str2 = LOG_TAG;
        IMSLog.dump(str2, "mIncommingMagId : " + SmsUtil.getIncommingMagId());
        String str3 = LOG_TAG;
        IMSLog.dump(str3, "mRPMsgRef : " + SmsUtil.getRPMsgRef());
        String str4 = LOG_TAG;
        IMSLog.dump(str4, "m3GPP2SendingMsgId : " + this.m3GPP2SendingMsgId);
        String str5 = LOG_TAG;
        IMSLog.dump(str5, "mLastMOContentType : " + this.mLastMOContentType);
        String str6 = LOG_TAG;
        IMSLog.dump(str6, "mRetransCount : " + this.mRetransCount);
        String str7 = LOG_TAG;
        IMSLog.dump(str7, "mStorageAvailable : " + this.mStorageAvailable);
        IMSLog.dump(LOG_TAG, "mPendingQueue :");
        IMSLog.increaseIndent(LOG_TAG);
        for (Map.Entry<Integer, SmsEvent> e : this.mPendingQueue.entrySet()) {
            String str8 = LOG_TAG;
            IMSLog.dump(str8, "key : " + e.getKey() + ", value : " + e.getValue());
        }
        this.mSmsLogger.dump();
        IMSLog.decreaseIndent(LOG_TAG);
        IMSLog.decreaseIndent(LOG_TAG);
    }
}
