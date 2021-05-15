package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.handler.VolteHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallSendCmcInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DTMFDataEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DedicatedBearerEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.IncomingCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ModifyCallData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReferReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReferStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RrcConnectionEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RtpLossRateNoti;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SipMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.TextDataEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CallResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.servicemodules.volte2.data.SIPDataEvent;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.ims.servicemodules.volte2.util.DialogXmlParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.CmcPingTestLogger;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

public class ResipVolteHandler extends VolteHandler {
    private static final int ADD_CONF_PARTICIPANT = 0;
    private static final String ALTERNATIVE_SERVICE = "application/3gpp-ims+xml";
    private static final String CMC_INFO_MIME_TYPE = "application/cmc-info+xml";
    private static final String DIALOG_EVENT_MIME_TYPE = "application/dialog-info+xml";
    private static final int EVENT_CALL_STATE_CHANGE = 100;
    private static final int EVENT_CDPN_INFO = 107;
    private static final int EVENT_CMC_INFO = 115;
    private static final int EVENT_CONFERENCE_UPDATE = 102;
    private static final int EVENT_DEDICATED_BEARER_EVENT = 110;
    private static final int EVENT_DELAYED_CALL_STATE_CHANGE = 200;
    private static final int EVENT_DIALOG_EVENT_RECEIVED = 105;
    private static final int EVENT_DTMF_INFO = 112;
    private static final int EVENT_END_CALL_RESPONSE = 2;
    private static final int EVENT_HOLD_CALL_RESPONSE = 4;
    private static final int EVENT_INFO_CALL_RESPONSE = 7;
    private static final int EVENT_MAKE_CALL_RESPONSE = 1;
    private static final int EVENT_MERGE_CALL_RESPONSE = 3;
    private static final int EVENT_MODIFY_CALL = 106;
    private static final int EVENT_NEW_INCOMING_CALL = 101;
    private static final int EVENT_PULLING_CALL_RESPONSE = 6;
    private static final int EVENT_REFER_RECEIVED = 103;
    private static final int EVENT_REFER_STATUS = 104;
    private static final int EVENT_RESUME_CALL_RESPONSE = 5;
    private static final int EVENT_RRC_CONNECTION = 111;
    private static final int EVENT_RTP_LOSS_RATE_NOTI = 108;
    private static final int EVENT_SIPMSG_INFO = 114;
    private static final int EVENT_TEXT_INFO = 113;
    private static final int EVENT_UPDATE_AUDIO_INTEFACE_RESPONSE = 8;
    private static final String LOG_TAG = "ResipVolteHandler";
    private static final int MO_TIMEOUT_MILLIS = 30000;
    private static final int REMOVE_CONF_PARTICIPANT = 1;
    private static final String URN_SOS = "urn:service:sos";
    private static final String URN_SOS_AMBULANCE = "urn:service:sos.ambulance";
    private static final String URN_SOS_FIRE = "urn:service:sos.fire";
    private static final String URN_SOS_MARINE = "urn:service:sos.marine";
    private static final String URN_SOS_MOUNTAIN = "urn:service:sos.mountain";
    private static final String URN_SOS_POLICE = "urn:service:sos.police";
    private static final String USSD_INDI_BY_MESSAGE_MIME_TYPE = "application/ussd";
    private static final String USSD_MIME_TYPE = "application/vnd.3gpp.ussd+xml";
    private static final Set<String> mMainSosSubserviceSet = new HashSet(Arrays.asList(new String[]{"urn:service:sos", URN_SOS_AMBULANCE, URN_SOS_FIRE, URN_SOS_MARINE, URN_SOS_MOUNTAIN, URN_SOS_POLICE}));
    private AudioInterfaceHandler mAudioInterfaceHandler = null;
    private HandlerThread mAudioInterfaceThread = null;
    protected boolean[] mAutomaticMode;
    private final SparseArray<Call> mCallList = new SparseArray<>();
    private final RegistrantList mCallStateEventRegistrants = new RegistrantList();
    private final RegistrantList mCdpnInfoRegistrants = new RegistrantList();
    private final RegistrantList mCmcInfoEventRegistrants = new RegistrantList();
    private final Context mContext;
    private final RegistrantList mDedicatedBearerEventRegistrants = new RegistrantList();
    private final RegistrantList mDialogEventRegistrants = new RegistrantList();
    private final RegistrantList mDtmfEventRegistrants = new RegistrantList();
    private final IImsFramework mImsFramework;
    private final RegistrantList mIncomingCallEventRegistrants = new RegistrantList();
    private final RegistrantList mReferStatusRegistrants = new RegistrantList();
    private final RegistrantList mRrcConnectionEventRegistrants = new RegistrantList();
    private final RegistrantList mRtpLossRateNotiRegistrants = new RegistrantList();
    protected int[] mRttMode;
    private final RegistrantList mSIPMSGNotiRegistrants = new RegistrantList();
    private StackIF mStackIf;
    private ITelephonyManager mTelephonyManager;
    private final RegistrantList mTextEventRegistrants = new RegistrantList();
    protected int[] mTtyMode;
    private final RegistrantList mUssdEventRegistrants = new RegistrantList();

    private static class AlternativeService {
        CallStateEvent.ALTERNATIVE_SERVICE mAction = CallStateEvent.ALTERNATIVE_SERVICE.NONE;
        String mReason;
        String mType;
    }

    static class Call {
        boolean isConference = false;
        int mCallType;
        CountDownLatch mLock = null;
        CallParams mParam;
        NameAddr mPeer;
        CallResponse mResponse = null;
        int mSessionId = -1;
        UserAgent mUa;

        public Call(UserAgent ua, ImsUri uri, String mdn) {
            this.mUa = ua;
            this.mPeer = new NameAddr(mdn, uri);
            this.mSessionId = -1;
        }

        public Call(UserAgent ua, NameAddr addr) {
            this.mUa = ua;
            this.mPeer = addr;
            this.mSessionId = -1;
        }
    }

    private static class UssdReceived {
        boolean hasErrorCode;
        String mString;
        Type mType;

        enum Type {
            RESPONSE_TO_USER_INIT,
            NET_INIT_REQUEST,
            NET_INIT_NOTIFY
        }

        private UssdReceived() {
            this.hasErrorCode = false;
        }

        /* synthetic */ UssdReceived(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: package-private */
        public int getVolteConstantsUssdStatus() {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type[this.mType.ordinal()];
            if (i == 1) {
                return 1;
            }
            if (i == 2 || i == 3) {
                return 2;
            }
            Log.e(ResipVolteHandler.LOG_TAG, "Invalid USSD type! - " + this.mType);
            return -1;
        }
    }

    /* renamed from: com.sec.internal.ims.core.handler.secims.ResipVolteHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type;

        static {
            int[] iArr = new int[UssdReceived.Type.values().length];
            $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type = iArr;
            try {
                iArr[UssdReceived.Type.NET_INIT_NOTIFY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type[UssdReceived.Type.NET_INIT_REQUEST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type[UssdReceived.Type.RESPONSE_TO_USER_INIT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public static class UssdXmlParser {
        private static UssdXmlParser sInstance = null;
        XPath mXPath;
        XPathExpression mXPathErrorCode;
        XPathExpression mXPathNiNotify;
        XPathExpression mXPathNiRequest;
        XPathExpression mXPathUssdData;
        XPathExpression mXPathUssdString;

        public static UssdXmlParser getInstance() {
            if (sInstance == null) {
                sInstance = new UssdXmlParser();
            }
            return sInstance;
        }

        private UssdXmlParser() {
            init();
        }

        private void init() {
            XPath newXPath = XPathFactory.newInstance().newXPath();
            this.mXPath = newXPath;
            try {
                this.mXPathUssdData = newXPath.compile("/ussd-data");
                this.mXPathUssdString = this.mXPath.compile("ussd-string");
                this.mXPathErrorCode = this.mXPath.compile("error-code");
                this.mXPathNiRequest = this.mXPath.compile("boolean(anyExt/UnstructuredSS-Request)");
                this.mXPathNiNotify = this.mXPath.compile("boolean(anyExt/UnstructuredSS-Notify)");
            } catch (XPathExpressionException e) {
                Log.e(ResipVolteHandler.LOG_TAG, "XPath compile failed!", e);
            }
        }

        /* access modifiers changed from: private */
        public UssdReceived parseUssdXml(String ussdXml) throws XPathExpressionException {
            UssdReceived ret = new UssdReceived((AnonymousClass1) null);
            if (ussdXml.contains("&")) {
                ussdXml = ussdXml.replaceAll("(?i)&(?!(#x?[\\d\\w]+;)|(quot;)|(lt;)|(gt;)|(apos;)|(amp;))", "&amp;");
            }
            Node ussdDataNode = (Node) this.mXPathUssdData.evaluate(new InputSource(new StringReader(ussdXml)), XPathConstants.NODE);
            String tmpErrorCode = this.mXPathErrorCode.evaluate(ussdDataNode);
            String tmpUssdStr = this.mXPathUssdString.evaluate(ussdDataNode);
            if (TextUtils.isEmpty(tmpErrorCode) || !TextUtils.isEmpty(tmpUssdStr)) {
                ret.mString = tmpUssdStr;
            } else {
                ret.mString = "error-code" + tmpErrorCode;
                ret.hasErrorCode = true;
            }
            Boolean isNiNotify = (Boolean) this.mXPathNiNotify.evaluate(ussdDataNode, XPathConstants.BOOLEAN);
            if (((Boolean) this.mXPathNiRequest.evaluate(ussdDataNode, XPathConstants.BOOLEAN)).booleanValue()) {
                ret.mType = UssdReceived.Type.NET_INIT_REQUEST;
            } else if (isNiNotify.booleanValue()) {
                ret.mType = UssdReceived.Type.NET_INIT_NOTIFY;
            } else {
                ret.mType = UssdReceived.Type.RESPONSE_TO_USER_INIT;
            }
            return ret;
        }
    }

    public ResipVolteHandler(Looper looper, Context context, IImsFramework imsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = imsFramework;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerNewIncomingCallEvent(this, 101, (Object) null);
        this.mStackIf.registerCallStatusEvent(this, 100, (Object) null);
        this.mStackIf.registerModifyCallEvent(this, 106, (Object) null);
        this.mStackIf.registerConferenceUpdateEvent(this, 102, (Object) null);
        this.mStackIf.registerReferReceivedEvent(this, 103, (Object) null);
        this.mStackIf.registerReferStatusEvent(this, 104, (Object) null);
        this.mStackIf.registerDialogEvent(this, 105, (Object) null);
        this.mStackIf.registerCdpnInfoEvent(this, 107, (Object) null);
        this.mStackIf.registerDedicatedBearerEvent(this, 110, (Object) null);
        this.mStackIf.registerForRrcConnectionEvent(this, 111, (Object) null);
        this.mStackIf.registerRtpLossRateNoti(this, 108, (Object) null);
        this.mStackIf.registerDtmfEvent(this, 112, (Object) null);
        this.mStackIf.registerTextEvent(this, 113, (Object) null);
        this.mStackIf.registerSIPMSGEvent(this, 114, (Object) null);
        this.mStackIf.registerCmcInfo(this, 115, (Object) null);
        int phoneCount = SimManagerFactory.getAllSimManagers().size();
        int[] iArr = new int[phoneCount];
        this.mTtyMode = iArr;
        this.mRttMode = new int[phoneCount];
        this.mAutomaticMode = new boolean[phoneCount];
        Arrays.fill(iArr, Extensions.TelecomManager.TTY_MODE_OFF);
        Arrays.fill(this.mRttMode, -1);
        Arrays.fill(this.mAutomaticMode, false);
        HandlerThread handlerThread = new HandlerThread("AudioInterfaceThread");
        this.mAudioInterfaceThread = handlerThread;
        handlerThread.start();
        this.mAudioInterfaceHandler = new AudioInterfaceHandler(this.mAudioInterfaceThread.getLooper());
    }

    public void registerForCallStateEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForCallStateEvent:");
        this.mCallStateEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallStateEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForCallStateEvent:");
        this.mCallStateEventRegistrants.remove(h);
    }

    public void registerForIncomingCallEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForCallStateEvent:");
        this.mIncomingCallEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIncomingCallEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForCallStateEvent:");
        this.mIncomingCallEventRegistrants.remove(h);
    }

    public void registerForUssdEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForUssdEvent:");
        this.mUssdEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForUssdEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForUssdEvent:");
        this.mUssdEventRegistrants.remove(h);
    }

    public void registerForReferStatus(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForReferStatus:");
        this.mReferStatusRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForReferStatus(Handler h) {
        Log.i(LOG_TAG, "unregisterForReferStatus:");
        this.mReferStatusRegistrants.remove(h);
    }

    public void registerForDialogEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForDialogEvent:");
        this.mDialogEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDialogEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForDialogEvent:");
        this.mDialogEventRegistrants.remove(h);
    }

    public void registerForCmcInfoEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForCmcInfoEvent:");
        this.mCmcInfoEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCmcInfoEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForCmcInfoEvent:");
        this.mCmcInfoEventRegistrants.remove(h);
    }

    public void registerForCdpnInfoEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForCdpnInfoEvent:");
        this.mCdpnInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void registerForDedicatedBearerNotifyEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForDedicatedBearerNotifyEvent:");
        this.mDedicatedBearerEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDedicatedBearerNotifyEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForDedicatedBearerNotifyEvent:");
        this.mDedicatedBearerEventRegistrants.remove(h);
    }

    public void registerForRrcConnectionEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForRrcConnectionEvent:");
        this.mRrcConnectionEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRrcConnectionEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForRrcConnectionEvent:");
        this.mRrcConnectionEventRegistrants.remove(h);
    }

    public void registerForDtmfEvent(Handler handler, int what, Object obj) {
        Log.i(LOG_TAG, "registerForDtmfEvent:");
        this.mDtmfEventRegistrants.add(handler, what, obj);
    }

    public void unregisterForDtmfEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForDtmfEvent:");
        this.mDtmfEventRegistrants.remove(handler);
    }

    public void registerForTextEvent(Handler handler, int what, Object obj) {
        Log.i(LOG_TAG, "registerForTextEvent:");
        this.mTextEventRegistrants.add(handler, what, obj);
    }

    public void unregisterForTextEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForTextEvent:");
        this.mTextEventRegistrants.remove(handler);
    }

    public void registerForSIPMSGEvent(Handler handler, int what, Object obj) {
        Log.i(LOG_TAG, "registerForSIPMSGEvent:");
        this.mSIPMSGNotiRegistrants.add(handler, what, obj);
    }

    public void unregisterForSIPMSGEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForSIPMSGEvent:");
        this.mSIPMSGNotiRegistrants.remove(handler);
    }

    public void registerForRtpLossRateNoti(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForRtpLossRateNoti:");
        this.mRtpLossRateNotiRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRtpLossRateNoti(Handler h) {
        Log.i(LOG_TAG, "unregisterForRtpLossRateNoti:");
        this.mRtpLossRateNotiRegistrants.remove(h);
    }

    public void unregisterForCdpnInfoEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForCdpnInfoEvent:");
        this.mCdpnInfoRegistrants.remove(h);
    }

    private AdditionalContents createUssdContents(int phoneId, String dialString, int type) {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter xmlStringWriter = new StringWriter();
        try {
            xmlSerializer.setOutput(xmlStringWriter);
            xmlSerializer.startDocument("UTF-8", (Boolean) null);
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xmlSerializer.startTag("", "ussd-data");
            Mno mno = SimUtil.getSimMno(phoneId);
            if (mno != Mno.SMART_CAMBODIA) {
                xmlSerializer.startTag("", CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE);
                if (mno == Mno.HK3) {
                    xmlSerializer.text("un");
                } else {
                    if (mno != Mno.H3G_AT) {
                        if (mno != Mno.TIGO_BOLIVIA) {
                            xmlSerializer.text("en");
                        }
                    }
                    xmlSerializer.text("undefined");
                }
                xmlSerializer.endTag("", CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE);
            }
            if (type == 3) {
                Log.i(LOG_TAG, "createUssdContents: error - \n" + dialString);
                xmlSerializer.startTag("", "error-code");
                xmlSerializer.text(dialString);
                xmlSerializer.endTag("", "error-code");
            } else if (type == 4) {
                Log.i(LOG_TAG, "createUssdContents: notify response");
                xmlSerializer.startTag("", "UnstructuredSS-Notify");
                xmlSerializer.endTag("", "UnstructuredSS-Notify");
            } else {
                Log.i(LOG_TAG, "createUssdContents: dialstring - \n" + dialString);
                xmlSerializer.startTag("", "ussd-string");
                xmlSerializer.text(dialString);
                xmlSerializer.endTag("", "ussd-string");
            }
            xmlSerializer.endTag("", "ussd-data");
            xmlSerializer.endDocument();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            Log.e(LOG_TAG, "Failed to createUssdContents()", e);
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int mimeOffset = builder.createString((CharSequence) USSD_MIME_TYPE);
        int xmlStringOffset = builder.createString((CharSequence) xmlStringWriter.toString());
        AdditionalContents.startAdditionalContents(builder);
        AdditionalContents.addMimeType(builder, mimeOffset);
        AdditionalContents.addContents(builder, xmlStringOffset);
        builder.finish(AdditionalContents.endAdditionalContents(builder));
        AdditionalContents ret = AdditionalContents.getRootAsAdditionalContents(builder.dataBuffer());
        Log.i(LOG_TAG, "createUssdContents: built contents - \n" + ret.contents());
        return ret;
    }

    private AdditionalContents createCmcInfoContents(int phoneId, int recordEvent, int extra, String extSipCallId) {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter xmlStringWriter = new StringWriter();
        try {
            xmlSerializer.setOutput(xmlStringWriter);
            xmlSerializer.startDocument("UTF-8", (Boolean) null);
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xmlSerializer.startTag("", "cmc-info-data");
            xmlSerializer.startTag("", "record-event");
            xmlSerializer.text(Integer.toString(recordEvent));
            xmlSerializer.endTag("", "record-event");
            xmlSerializer.startTag("", "extra");
            xmlSerializer.text(Integer.toString(extra));
            xmlSerializer.endTag("", "extra");
            xmlSerializer.startTag("", "external-call-id");
            xmlSerializer.text(extSipCallId);
            xmlSerializer.endTag("", "external-call-id");
            xmlSerializer.endTag("", "cmc-info-data");
            xmlSerializer.endDocument();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            Log.e(LOG_TAG, "Failed to createCmcInfoContents()", e);
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int mimeOffset = builder.createString((CharSequence) CMC_INFO_MIME_TYPE);
        int xmlStringOffset = builder.createString((CharSequence) xmlStringWriter.toString());
        AdditionalContents.startAdditionalContents(builder);
        AdditionalContents.addMimeType(builder, mimeOffset);
        AdditionalContents.addContents(builder, xmlStringOffset);
        builder.finish(AdditionalContents.endAdditionalContents(builder));
        AdditionalContents ret = AdditionalContents.getRootAsAdditionalContents(builder.dataBuffer());
        Log.i(LOG_TAG, "createCmcInfoContents: built contents - \n" + ret.contents());
        return ret;
    }

    public int makeCall(int regId, CallSetupData data, HashMap<String, String> additionalSipHeaders, int phoneId) {
        UserAgent ua;
        String origUri;
        int i = regId;
        int i2 = phoneId;
        Log.i(LOG_TAG, "makeCall: regId=" + i + " " + data + " additionalSipHeaders=" + additionalSipHeaders);
        ImsUri uri = data.getDestinationUri();
        int type = data.getCallType();
        boolean isEmergency = data.isEmergency();
        boolean isUssd = type == 12;
        if (!isEmergency || i >= 0) {
            ua = getUaByRegId(regId);
        } else {
            Log.i(LOG_TAG, "makeCall: using emergency UA.");
            ua = getEmergencyUa(i2);
        }
        if (ua == null) {
            Log.e(LOG_TAG, "makeCall: UserAgent not found.");
            return -1;
        }
        if (data.getOriginatingUri() != null) {
            origUri = data.getOriginatingUri().toString();
        } else {
            origUri = null;
        }
        CountDownLatch lock = new CountDownLatch(1);
        Call call = new Call(ua, uri, data.getDialingNumber());
        call.mCallType = convertToCallTypeForward(type);
        CallParams param = new CallParams();
        String audioCodec = ua.getImsProfile().getAudioCodec();
        Mno mno = Mno.fromName(call.mUa.getImsProfile().getMnoName());
        if ((mno == Mno.KDDI || mno == Mno.DOCOMO) && audioCodec.contains("EVS")) {
            String send_evs_bandwidth = ua.getImsProfile().getEvsBandwidthSend();
            if (send_evs_bandwidth.contains("fb")) {
                param.setAudioCodec("EVS-FB");
            } else if (send_evs_bandwidth.contains("swb")) {
                param.setAudioCodec("EVS-SWB");
            } else if (send_evs_bandwidth.contains("wb")) {
                param.setAudioCodec("EVS-WB");
            } else if (send_evs_bandwidth.contains("nb")) {
                param.setAudioCodec("EVS-NB");
            }
        } else if (audioCodec.contains("AMR-WB") || audioCodec.contains("AMRBE-WB")) {
            param.setAudioCodec("AMR-WB");
        } else {
            param.setAudioCodec("AMR-NB");
        }
        Mno mno2 = mno;
        String cli = ua.getImsProfile().getSupportClir() ? data.getCli() : null;
        call.mParam = param;
        call.mLock = lock;
        Log.i(LOG_TAG, "makeCall: Do device support 3gpp 24.390 USSI?" + ua.getImsProfile().getSupport3gppUssi());
        boolean is24390Ussd = isUssd && ua.getImsProfile().getSupport3gppUssi();
        String imsUri = uri.toString();
        int i3 = call.mCallType;
        String letteringText = data.getLetteringText();
        String dialingNumber = data.getDialingNumber();
        AdditionalContents createUssdContents = is24390Ussd ? createUssdContents(ua.getPhoneId(), data.getDialingNumber(), 0) : null;
        String audioCodec2 = audioCodec;
        String audioCodec3 = data.getPEmergencyInfoOfAtt();
        String alertInfo = data.getAlertInfo();
        boolean lteEpsOnlyAttached = data.getLteEpsOnlyAttached();
        List<String> p2p = data.getP2p();
        int cmcBoundSessionId = data.getCmcBoundSessionId();
        Bundle composerData = data.getComposerData();
        String replaceCallId = data.getReplaceCallId();
        Message obtainMessage = obtainMessage(1, call);
        CallParams callParams = param;
        Call call2 = call;
        String str = audioCodec2;
        CountDownLatch lock2 = lock;
        UserAgent ua2 = ua;
        String str2 = letteringText;
        int i4 = type;
        String str3 = dialingNumber;
        ImsUri imsUri2 = uri;
        String str4 = LOG_TAG;
        ua.makeCall(imsUri, origUri, i3, str2, str3, createUssdContents, cli, audioCodec3, additionalSipHeaders, alertInfo, lteEpsOnlyAttached, p2p, cmcBoundSessionId, composerData, replaceCallId, obtainMessage);
        try {
            if (!lock2.await(30000, TimeUnit.MILLISECONDS)) {
                Log.e(str4, "makeCall: timeout.");
                return -1;
            } else if (call2.mResponse == null || call2.mResponse.result() == 0) {
                IMSLog.c(LogClass.VOLTE_MAKE_CALL, "MakeCall," + i2 + "," + call2.mSessionId);
                if (!Debug.isProductShip() && ua2.getImsProfile().getCmcType() > 0) {
                    CmcPingTestLogger.ping(ua2.getImsProfile().getPcscfList());
                }
                return call2.mSessionId;
            } else {
                Log.e(str4, "makeCall: call failed. reason " + call2.mResponse.reason());
                return -1;
            }
        } catch (InterruptedException e) {
            return -1;
        }
    }

    public int rejectCall(int sessionId, int callType, SipError sipError) {
        Log.i(LOG_TAG, "rejectCall: sessionId " + sessionId + " callType " + callType + " error " + sipError);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "rejectCall: session not found.");
            return -1;
        }
        call.mUa.rejectCall(call.mSessionId, sipError);
        return 0;
    }

    public int DeleteTcpSocket(int sessionId, int callType) {
        Log.i(LOG_TAG, "DeleteTcpSocket: sessionId " + sessionId + " callType " + callType);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "DeleteTcpSocket: session not found.");
            return -1;
        }
        call.mUa.deleteTcpClientSocket();
        return 0;
    }

    public int endCall(int sessionId, int callType, SipReason reason) {
        Log.i(LOG_TAG, "endCall: sessionId " + sessionId + " callType " + callType + " reason " + reason);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "endCall: session not found.");
            return -1;
        }
        Mno mno = Mno.fromName(call.mUa.getImsProfile().getMnoName());
        if (reason != null) {
            Log.i(LOG_TAG, "endCall: reason : " + reason.getText());
            if (mno.isJpn()) {
                if (mno == Mno.DOCOMO && ("PS BARRING".equals(reason.getText()) || "RRC CONNECTION REJECT".equals(reason.getText()))) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    call.mUa.deleteTcpClientSocket();
                }
                if ((mno == Mno.KDDI || mno == Mno.DOCOMO) && "INVITE FLUSH".equals(reason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket() at INVITE FLUSH");
                    call.mUa.deleteTcpClientSocket();
                }
            } else if (mno == Mno.CMCC) {
                if ("SRVCC".equals(reason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    call.mUa.deleteTcpClientSocket();
                }
            } else if (mno == Mno.VZW) {
                if ("RRC CONNECTION REJECT".equals(reason.getText()) || "TIMER VZW EXPIRED".equals(reason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    call.mUa.deleteTcpClientSocket();
                }
            } else if (mno == Mno.ORANGE || mno == Mno.KDDI || mno == Mno.FET) {
                if ("SESSIONPROGRESS TIMEOUT".equals(reason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    call.mUa.deleteTcpClientSocket();
                }
            } else if (mno.isKor() && "INVITE FLUSH".equals(reason.getText())) {
                Log.i(LOG_TAG, "deleteTcpClientSocket() at INVITE FLUSH");
                call.mUa.deleteTcpClientSocket();
            }
        }
        call.mUa.endCall(call.mSessionId, reason);
        return 0;
    }

    public int proceedIncomingCall(int sessionId, HashMap<String, String> headers) {
        Log.i(LOG_TAG, "proceedIncomingCall: sessoinId " + sessionId);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "proceedIncomingCall: session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_INCOMING_CALL, "IncomingCall," + call.mUa.getPhoneId() + "," + call.mSessionId);
        call.mUa.progressIncomingCall(call.mSessionId, headers);
        return 0;
    }

    public int answerCallWithCallType(int sessionId, int callType) {
        return answerCall(sessionId, convertToCallTypeForward(callType), "0");
    }

    public int answerCallWithCallType(int sessionId, int callType, String cmcCallTime) {
        return answerCall(sessionId, convertToCallTypeForward(callType), cmcCallTime);
    }

    private int answerCall(int sessionId, int callType, String cmcCallTime) {
        Log.i(LOG_TAG, "answerCallWithCallType: sessionId " + sessionId + " callType " + callType + " cmcCallEstablishTime " + cmcCallTime);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "answerCallWithCallType: session not found.");
            dumpCall();
            return -1;
        }
        if (callType == -1) {
            callType = call.mCallType;
        }
        call.mUa.answerCall(sessionId, callType, cmcCallTime);
        if (Debug.isProductShip() || call.mUa.getImsProfile().getCmcType() <= 0) {
            return 0;
        }
        CmcPingTestLogger.ping(call.mUa.getImsProfile().getPcscfList());
        return 0;
    }

    public int sendText(int sessionId, String text, int len) {
        Log.i(LOG_TAG, "sendText: sessionId " + sessionId + ", text: " + text + " len : " + len);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "sendText: session not found.");
            return -1;
        }
        call.mUa.sendText(sessionId, text, len);
        return 0;
    }

    public int handleDtmf(int sessionId, int code, int mode, int operation, Message result) {
        Log.i(LOG_TAG, "handleDtmf: sessionId " + sessionId + " code " + code + " mode " + mode + " operation " + operation);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "sendDtmf: session not found.");
            return -1;
        }
        call.mUa.handleDtmf(sessionId, code, mode, operation, result);
        return 0;
    }

    public int holdCall(int sessionId) {
        Log.i(LOG_TAG, "holdCall: sessionId " + sessionId);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "holdCall: session not found.");
            dumpCall();
            return -1;
        }
        call.mParam.setIndicationFlag(0);
        IMSLog.c(LogClass.VOLTE_HOLD_CALL, "HoldCall," + call.mUa.getPhoneId() + "," + sessionId);
        call.mUa.holdCall(sessionId, obtainMessage(4));
        return 0;
    }

    public int resumeCall(int sessionId) {
        Log.i(LOG_TAG, "resumeCall: sessionId " + sessionId);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "resumeCall: session not found.");
            dumpCall();
            return -1;
        }
        call.mParam.setIndicationFlag(0);
        IMSLog.c(LogClass.VOLTE_RESUME_CALL, "ResumeCall," + call.mUa.getPhoneId() + "," + sessionId);
        call.mUa.resumeCall(sessionId, obtainMessage(5));
        return 0;
    }

    public int startNWayConferenceCall(int regId, ConfCallSetupData data) {
        String origUri;
        ConfCallSetupData confCallSetupData = data;
        Log.i(LOG_TAG, "startNWayConferenceCall: regId=" + regId + " " + confCallSetupData);
        UserAgent ua = getUaByRegId(regId);
        if (ua == null) {
            Log.e(LOG_TAG, "startNWayConferenceCall: no UserAgent found.");
            return -1;
        } else if (checkConfererenceCallData(confCallSetupData) == -1) {
            return -1;
        } else {
            if (data.getOriginatingUri() != null) {
                origUri = data.getOriginatingUri().toString();
            } else {
                origUri = null;
            }
            boolean supportPrematureEnd = data.getSupportPrematureEnd();
            if (data.getParticipants() != null) {
                return startNWayConferenceCall(ua, data.getConferenceUri(), origUri, data.getParticipants(), data.getCallType(), data.isSubscriptionEnabled(), data.getSubscribeDialogType(), data.getReferUriType(), data.getRemoveReferUriType(), data.getReferUriAsserted(), data.getUseAnonymousUpdate(), supportPrematureEnd);
            } else if (data.getSessionIds().size() < 2) {
                Log.e(LOG_TAG, "startNWayConferenceCall: not enough sessionIds");
                return -1;
            } else {
                return startNWayConferenceCall(ua, data.getConferenceUri(), origUri, data.getSessionIds().get(0).intValue(), data.getSessionIds().get(1).intValue(), data.getCallType(), data.isSubscriptionEnabled(), data.getSubscribeDialogType(), data.getReferUriType(), data.getRemoveReferUriType(), data.getReferUriAsserted(), data.getUseAnonymousUpdate(), supportPrematureEnd, data.getExtraSipHeaders());
            }
        }
    }

    private int startNWayConferenceCall(UserAgent ua, String confUri, String origUri, int session1, int session2, int callType, String confSubscribe, String subscribeDialogType, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd, HashMap<String, String> extraHeaders) {
        Call fgCall = getCallBySession(session1);
        Call bgCall = getCallBySession(session2);
        if (fgCall == null || bgCall == null) {
            return -1;
        }
        Call confCall = new Call(ua, ImsUri.parse(confUri), "");
        int type = convertToCallTypeForward(callType);
        confCall.mCallType = type;
        confCall.isConference = true;
        CallParams param = new CallParams();
        confCall.mParam = param;
        CountDownLatch lock = new CountDownLatch(1);
        confCall.mLock = lock;
        Message obtainMessage = obtainMessage(3, confCall);
        CountDownLatch lock2 = lock;
        CallParams callParams = param;
        int i = type;
        Call confCall2 = confCall;
        ua.mergeCall(session1, session2, confUri, type, confSubscribe, subscribeDialogType, origUri, referUriType, removeReferUriType, referUriAsserted, useAnonymousUpdate, supportPrematureEnd, extraHeaders, obtainMessage);
        try {
            if (!lock2.await(30000, TimeUnit.MILLISECONDS)) {
                try {
                    Log.e(LOG_TAG, "startNWayConferenceCall: timeout.");
                    return -1;
                } catch (InterruptedException e) {
                    Call call = confCall2;
                    return -1;
                }
            } else {
                Call confCall3 = confCall2;
                if (confCall3.mResponse == null || confCall3.mResponse.result() == 0) {
                    return confCall3.mSessionId;
                }
                Log.i(LOG_TAG, "startNWayConferenceCall: call failed. reason " + confCall3.mResponse.reason());
                return -1;
            }
        } catch (InterruptedException e2) {
            Call call2 = confCall2;
            return -1;
        }
    }

    private int startNWayConferenceCall(UserAgent ua, String confUri, String origUri, List<String> participants, int callType, String confSubscribe, String subscribeDialogType, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd) {
        Call confCall = new Call(ua, ImsUri.parse(confUri), "");
        int type = convertToCallTypeForward(callType);
        confCall.mCallType = type;
        confCall.isConference = true;
        CallParams param = new CallParams();
        confCall.mParam = param;
        CountDownLatch lock = new CountDownLatch(1);
        confCall.mLock = lock;
        Message obtainMessage = obtainMessage(3, confCall);
        CountDownLatch lock2 = lock;
        CallParams callParams = param;
        int i = type;
        ua.conference((String[]) participants.toArray(new String[participants.size()]), confUri, type, confSubscribe, subscribeDialogType, origUri, referUriType, removeReferUriType, referUriAsserted, useAnonymousUpdate, supportPrematureEnd, obtainMessage);
        try {
            if (!lock2.await(30000, TimeUnit.MILLISECONDS)) {
                Log.e(LOG_TAG, "startNWayConferenceCall: timeout.");
                return -1;
            } else if (confCall.mResponse == null || confCall.mResponse.result() == 0) {
                return confCall.mSessionId;
            } else {
                Log.e(LOG_TAG, "startNWayConferenceCall: call failed. reason " + confCall.mResponse.reason());
                return -1;
            }
        } catch (InterruptedException e) {
            return -1;
        }
    }

    public int addParticipantToNWayConferenceCall(int confCallSessionId, int participantId) {
        Log.i(LOG_TAG, "addParticipantToNWayConferenceCall (" + confCallSessionId + ") participantId " + participantId);
        Call confCall = getCallBySession(confCallSessionId);
        if (confCall == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        confCall.mUa.updateConfCall(confCallSessionId, 0, participantId, "");
        return 0;
    }

    public int removeParticipantFromNWayConferenceCall(int confCallSessionId, int participantId) {
        Log.i(LOG_TAG, "removeParticipantFromNWayConferenceCall (" + confCallSessionId + ") removeSession " + participantId);
        Call confCall = getCallBySession(confCallSessionId);
        if (confCall == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        confCall.mUa.updateConfCall(confCallSessionId, 1, participantId, "");
        return 0;
    }

    public int addParticipantToNWayConferenceCall(int confCallSessionId, String participant) {
        Log.i(LOG_TAG, "addParticipantToNWayConferenceCall (" + confCallSessionId + ") participant " + participant);
        Call confCall = getCallBySession(confCallSessionId);
        if (confCall == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        confCall.mUa.updateConfCall(confCallSessionId, 0, -1, participant);
        return 0;
    }

    public int removeParticipantFromNWayConferenceCall(int confCallSessionId, String participant) {
        Log.i(LOG_TAG, "removeParticipantFromNWayConferenceCall (" + confCallSessionId + ") participant " + participant);
        Call confCall = getCallBySession(confCallSessionId);
        if (confCall == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        confCall.mUa.updateConfCall(confCallSessionId, 1, -1, participant);
        return 0;
    }

    public int modifyCallType(int sessionId, int oldType, int newType) {
        Log.i(LOG_TAG, "modifyCallType(): sessionId " + sessionId + ", oldType " + oldType + ", newType " + newType);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "modifyCallType(): session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_MODIFY_CALL, "ModifyCall," + call.mUa.getPhoneId() + "," + call.mSessionId + "," + oldType + "," + newType);
        call.mUa.modifyCallType(call.mSessionId, oldType, newType);
        return 0;
    }

    public int replyModifyCallType(int sessionId, int curType, int repType, int reqType) {
        return replyModifyCallType(sessionId, curType, repType, reqType, "");
    }

    public int replyModifyCallType(int sessionId, int curType, int repType, int reqType, String cmcCallTime) {
        Log.i(LOG_TAG, "replyModifyCallType(): sessionId " + sessionId + ", reqType " + reqType + ", curType " + curType + ", repType " + repType + ", cmcCallTime " + cmcCallTime);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "replyModifyCallType(): session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_MODIFY_REPLY, "ReplyModifyCall," + call.mUa.getPhoneId() + "," + call.mSessionId + "," + reqType + "," + curType + "," + repType);
        call.mUa.replyModifyCallType(call.mSessionId, curType, repType, reqType, cmcCallTime);
        return 0;
    }

    public int rejectModifyCallType(int sessionId, int reason) {
        Log.i(LOG_TAG, "rejectModifyCallType(): sessionId " + sessionId + ", reason" + reason);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "rejectModifyCallType(): session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_MODIFY_REJECT, "RejectModifyCall," + call.mUa.getPhoneId() + "," + call.mSessionId + "," + reason);
        call.mUa.rejectModifyCallType(call.mSessionId, reason);
        return 0;
    }

    public int sendReInvite(int sessionId, SipReason reason) {
        Log.i(LOG_TAG, "sendReInvite(): sessionId " + sessionId + ", reason " + reason);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "sendReInvite(): session not found.");
            return -1;
        }
        call.mUa.updateCall(call.mSessionId, 0, -1, reason);
        return 0;
    }

    private int checkConfererenceCallData(ConfCallSetupData data) {
        if (data.getConferenceUri() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: conference server uri is not configured.");
            return -1;
        } else if (data.isSubscriptionEnabled() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: confSubscribe no global xml file");
            return -1;
        } else if (data.getSubscribeDialogType() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: subscribeDialogType no global xml file");
            return -1;
        } else if (data.getReferUriType() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: referUriType no global xml file");
            return -1;
        } else if (data.getRemoveReferUriType() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: removeReferUriType no global xml file");
            return -1;
        } else if (data.getReferUriAsserted() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: referUriAsserted no global xml file");
            return -1;
        } else if (data.getUseAnonymousUpdate() != null) {
            return 1;
        } else {
            Log.e(LOG_TAG, "checkConfererenceCallData: useAnonymousUpdate no global xml file");
            return -1;
        }
    }

    public int addUserForConferenceCall(int sessionId, ConfCallSetupData data, boolean create) {
        String origUri;
        ConfCallSetupData confCallSetupData = data;
        Log.i(LOG_TAG, "addUserForConferenceCall: sessionId=" + sessionId + " " + confCallSetupData + " create " + create);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "addUserForConferenceCall: session not found.");
            return -1;
        }
        if (data.getOriginatingUri() != null) {
            origUri = data.getOriginatingUri().toString();
        } else {
            origUri = null;
        }
        if (checkConfererenceCallData(confCallSetupData) == -1) {
            return -1;
        }
        boolean supportPrematureEnd = data.getSupportPrematureEnd();
        int type = convertToCallTypeForward(data.getCallType());
        UserAgent ua = call.mUa;
        UserAgent userAgent = ua;
        ua.extendToConfCall((String[]) data.getParticipants().toArray(new String[data.getParticipants().size()]), data.getConferenceUri(), type, data.isSubscriptionEnabled(), data.getSubscribeDialogType(), sessionId, origUri, data.getReferUriType(), data.getRemoveReferUriType(), data.getReferUriAsserted(), data.getUseAnonymousUpdate(), supportPrematureEnd);
        return 0;
    }

    public int transferCall(int sessionId, String taruri) {
        Log.i(LOG_TAG, "transferCall: sessionId " + sessionId + " taruri " + IMSLog.checker(taruri));
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "transferCall: session not found.");
            return -1;
        }
        call.mUa.transferCall(call.mSessionId, taruri, 0, (Message) null);
        return 0;
    }

    public int cancelTransferCall(int sessionId) {
        Log.i(LOG_TAG, "cancelTransferCall: sessionId " + sessionId);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "cancelTransferCall: session not found.");
            return -1;
        }
        call.mUa.cancelTransferCall(call.mSessionId, (Message) null);
        return 0;
    }

    public int pullingCall(int regId, String taruri, String msisdn, String origUri, Dialog targetDialog, List<String> p2p) {
        Dialog dialog = targetDialog;
        StringBuilder sb = new StringBuilder();
        sb.append("pullingCall: regId=");
        sb.append(regId);
        sb.append(" taruri ");
        sb.append(IMSLog.checker(taruri));
        sb.append(" msisdn ");
        sb.append(IMSLog.checker(msisdn));
        sb.append(" targetDialog ");
        sb.append(IMSLog.checker(dialog + ""));
        Log.i(LOG_TAG, sb.toString());
        UserAgent ua = getUaByRegId(regId);
        if (ua == null) {
            Log.e(LOG_TAG, "pullingCall: UserAgent not found.");
            return -1;
        }
        ImsUri pullingUri = ImsUri.parse(taruri);
        if (pullingUri == null) {
            Log.e(LOG_TAG, "Pulling Uri is wrong");
            return -1;
        }
        String mno = ua.getImsProfile().getMnoName();
        Log.i(LOG_TAG, "targetDialog.getCallType()= " + targetDialog.getCallType() + ", mno=" + mno + ", " + targetDialog.isVideoPortZero() + ", " + targetDialog.isPullAvailable());
        if (mno.contains("VZW") && targetDialog.isVideoPortZero() && targetDialog.isPullAvailable() && targetDialog.getCallType() == 1) {
            dialog.setCallType(2);
            Log.i(LOG_TAG, "recover call type= " + targetDialog.getCallType());
        }
        CountDownLatch lock = new CountDownLatch(1);
        Call call = new Call(ua, pullingUri, msisdn);
        call.mCallType = convertToCallTypeForward(targetDialog.getCallType());
        CallParams param = new CallParams();
        param.setAudioCodec("AMR-WB");
        call.mParam = param;
        call.mLock = lock;
        CallParams callParams = param;
        Call call2 = call;
        CountDownLatch lock2 = lock;
        ua.pullingCall(pullingUri.toString(), pullingUri.toString(), origUri, targetDialog, p2p, obtainMessage(6, call));
        try {
            if (!lock2.await(30000, TimeUnit.MILLISECONDS)) {
                try {
                    Log.e(LOG_TAG, "pullingCall: timeout.");
                    return -1;
                } catch (InterruptedException e) {
                    Call call3 = call2;
                    return -1;
                }
            } else {
                Call call4 = call2;
                if (call4.mResponse == null || call4.mResponse.result() == 0) {
                    return call4.mSessionId;
                }
                Log.i(LOG_TAG, "pullingCall: call failed. reason " + call4.mResponse.reason());
                return -1;
            }
        } catch (InterruptedException e2) {
            Call call5 = call2;
            return -1;
        }
    }

    public int publishDialog(int regId, String origUri, String dispName, String xmlBody, int expires, boolean needDelay) {
        StringBuilder sb = new StringBuilder();
        sb.append("publishDialog: regId=");
        int i = regId;
        sb.append(regId);
        Log.i(LOG_TAG, sb.toString());
        UserAgent ua = getUaByRegId(regId);
        if (ua == null) {
            Log.e(LOG_TAG, "publishDialog: UserAgent not found.");
            return -1;
        }
        ImsUri publishUri = ImsUri.parse(origUri);
        if (publishUri == null) {
            Log.e(LOG_TAG, "publishUri Uri is wrong");
            return -1;
        }
        ua.publishDialog(publishUri.toString(), dispName, xmlBody, expires, (Message) null, needDelay);
        return 0;
    }

    public int setTtyMode(int phoneId, int sessionID, int ttyMode) {
        int textMode;
        int[] iArr = this.mTtyMode;
        if (iArr[phoneId] != ttyMode) {
            iArr[phoneId] = ttyMode;
            StackIF stackIF = this.mStackIf;
            boolean z = true;
            boolean z2 = (ttyMode == Extensions.TelecomManager.TTY_MODE_OFF || ttyMode == Extensions.TelecomManager.RTT_MODE) ? false : true;
            if (this.mRttMode[phoneId] != Extensions.TelecomManager.RTT_MODE) {
                z = false;
            }
            stackIF.configCall(phoneId, z2, z, this.mAutomaticMode[phoneId]);
            UserAgent ua = getUa(phoneId, "mmtel");
            if (ua == null) {
                ua = getUa(phoneId, "mmtel-video");
            }
            if (ua != null && ua.getImsProfile().getTtyType() == 4) {
                if (ttyMode == Extensions.TelecomManager.RTT_MODE) {
                    textMode = 3;
                } else {
                    textMode = 2;
                }
                Log.i(LOG_TAG, "TTY mode " + ttyMode + " convert to TextMode " + textMode);
                this.mStackIf.setTextMode(phoneId, textMode);
            }
        }
        return 0;
    }

    public void setAutomaticMode(int phoneId, boolean mode) {
        this.mAutomaticMode[phoneId] = mode;
        StackIF stackIF = this.mStackIf;
        boolean z = true;
        boolean z2 = (this.mTtyMode[phoneId] == Extensions.TelecomManager.TTY_MODE_OFF || this.mTtyMode[phoneId] == Extensions.TelecomManager.RTT_MODE) ? false : true;
        if (this.mRttMode[phoneId] != Extensions.TelecomManager.RTT_MODE) {
            z = false;
        }
        stackIF.configCall(phoneId, z2, z, mode);
    }

    public void setRttMode(int phoneId, int mode) {
        int[] iArr = this.mRttMode;
        if (iArr[phoneId] != mode) {
            iArr[phoneId] = mode;
            StackIF stackIF = this.mStackIf;
            boolean z = true;
            boolean z2 = (this.mTtyMode[phoneId] == Extensions.TelecomManager.TTY_MODE_OFF || this.mTtyMode[phoneId] == Extensions.TelecomManager.RTT_MODE) ? false : true;
            if (mode != Extensions.TelecomManager.RTT_MODE) {
                z = false;
            }
            stackIF.configCall(phoneId, z2, z, this.mAutomaticMode[phoneId]);
            UserAgent ua = getUa(phoneId, "mmtel");
            if (ua == null) {
                ua = getUa(phoneId, "mmtel-video");
            }
            int textMode = 1;
            if (ua != null) {
                if (ua.getImsProfile().getTtyType() == 4) {
                    if (mode == Extensions.TelecomManager.RTT_MODE || mode == Extensions.TelecomManager.RTT_MODE_OFF) {
                        textMode = 3;
                    } else {
                        textMode = 2;
                    }
                } else if (ua.getImsProfile().getTtyType() == 3) {
                    if (mode == Extensions.TelecomManager.RTT_MODE) {
                        textMode = 3;
                    } else {
                        textMode = 0;
                    }
                }
                this.mStackIf.setTextMode(phoneId, textMode);
                Log.i(LOG_TAG, "RTT mode " + mode + " convert to TextMode " + textMode);
            }
        }
    }

    public void updateAudioInterface(int regId, String mode) {
        UserAgent ua = getUaByRegId(regId);
        if (ua == null) {
            Log.e(LOG_TAG, "Not Registered Volte Services");
            return;
        }
        CountDownLatch lock = new CountDownLatch(1);
        ua.updateAudioInterface(mode, this.mAudioInterfaceHandler.obtainMessage(8, lock));
        try {
            if (!lock.await(1000, TimeUnit.MILLISECONDS)) {
                Log.e(LOG_TAG, "updateAudioInterface: timeout.");
            }
        } catch (InterruptedException e) {
        }
    }

    public int sendInfo(int sessionId, int callType, String request, int ussdType) {
        Log.i(LOG_TAG, "sendInfo: " + request);
        Call call = getCallBySession(sessionId);
        int type = convertToCallTypeForward(callType);
        if (call == null) {
            Log.e(LOG_TAG, "sendInfo: session not found.");
            return -1;
        }
        call.mUa.sendInfo(sessionId, type, ussdType, createUssdContents(call.mUa.getPhoneId(), request, ussdType), obtainMessage(7));
        return 0;
    }

    public int sendCmcInfo(int sessionId, Bundle cmcInfoData) {
        Log.i(LOG_TAG, "sendCmcInfo");
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "sendInfo: session not found.");
            return -1;
        }
        call.mUa.sendCmcInfo(sessionId, createCmcInfoContents(call.mUa.getPhoneId(), cmcInfoData.getInt("record_event"), cmcInfoData.getInt("extra"), cmcInfoData.getString("sip_call_id")));
        return 0;
    }

    public int startVideoEarlyMedia(int sessionId) {
        Log.i(LOG_TAG, "startVideoEarlyMedia(): sessionId " + sessionId);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "startVideoEarlyMedia(): session not found.");
            return -1;
        }
        call.mUa.startVideoEarlyMedia(call.mSessionId);
        return 0;
    }

    public void updateScreenOnOff(int phoneId, int on) {
        this.mStackIf.updateScreenOnOff(phoneId, on);
    }

    public void updateXqEnable(int phoneId, boolean enable) {
        this.mStackIf.updateXqEnable(phoneId, enable);
    }

    public int handleCmcCsfb(int sessionId) {
        Log.i(LOG_TAG, "handleCmcCsfb(): sessionId " + sessionId);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "handleCmcCsfb(): session not found.");
            return -1;
        }
        call.mUa.handleCmcCsfb(call.mSessionId);
        return 0;
    }

    public void replaceSipCallId(int sessionId, String sipCallId) {
        Log.i(LOG_TAG, "replaceSipCallId(): sessionId " + sessionId + ", callId " + sipCallId);
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.e(LOG_TAG, "replaceSipCallId(): session not found.");
        } else {
            call.mParam.setSipCallId(sipCallId);
        }
    }

    public void replaceUserAgent(int replaceSessionId, int newSessionId) {
        Call replaceCall = getCallBySession(replaceSessionId);
        Call newCall = getCallBySession(newSessionId);
        if (replaceCall == null || newCall == null) {
            Log.i(LOG_TAG, "call not found with session id " + newSessionId);
            return;
        }
        replaceCall.mUa = newCall.mUa;
        Log.i(LOG_TAG, "session(" + replaceSessionId + ") ProfileHandle changed to " + replaceCall.mUa.getHandle());
    }

    public void clearAllCallInternal(int cmcType) {
        this.mStackIf.clearAllCallInternal(cmcType);
    }

    private UserAgent getUa(int phoneId, String service) {
        return getUa(phoneId, service, 0);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUa(int phoneId, String service, int cmcType) {
        IUserAgent[] uaList = this.mImsFramework.getRegistrationManager().getUserAgentByPhoneId(phoneId, service);
        if (uaList.length == 0) {
            return null;
        }
        for (IUserAgent ua : uaList) {
            if (ua != null && ua.getImsProfile().getCmcType() == cmcType) {
                return (UserAgent) ua;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public UserAgent getUaByRegId(int regId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByRegId(regId);
    }

    /* access modifiers changed from: protected */
    public UserAgent getEmergencyUa(int phoneId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentOnPdn(15, phoneId);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUa(int handle) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(handle);
    }

    private void notifyUssdEvent(Call call, UssdEvent.USSD_STATE state, CallStatus cs) {
        Mno fromName = Mno.fromName(call.mUa.getImsProfile().getMnoName());
        Log.i(LOG_TAG, "notifyUssdEvent() session: " + call.mSessionId);
        UssdEvent ussdEvent = new UssdEvent();
        ussdEvent.setSessionID(call.mSessionId);
        ussdEvent.setState(state);
        if (state == UssdEvent.USSD_STATE.USSD_RESPONSE) {
            ussdEvent.setErrorCode(new SipError((int) cs.statusCode(), cs.reasonPhrase()));
        } else if (state == UssdEvent.USSD_STATE.USSD_INDICATION) {
            if (!(cs == null || cs.additionalContents() == null)) {
                if (cs.additionalContents().mimeType().equals(USSD_MIME_TYPE)) {
                    try {
                        UssdReceived ur = UssdXmlParser.getInstance().parseUssdXml(cs.additionalContents().contents());
                        ussdEvent.setData((Object) ur.mString.getBytes("UTF-8"));
                        if (cs.state() == 11) {
                            ussdEvent.setStatus(3);
                            if (ur.hasErrorCode) {
                                Log.i(LOG_TAG, "BYE from NW has <error-code>");
                                ussdEvent.setData((Object) null);
                            }
                        } else {
                            ussdEvent.setStatus(ur.getVolteConstantsUssdStatus());
                        }
                        ussdEvent.setDCS(148);
                    } catch (UnsupportedEncodingException | XPathExpressionException e) {
                        Log.e(LOG_TAG, "notifyCallStatus: error parsing USSD xml", e);
                    }
                } else if (cs.additionalContents().mimeType().equals(USSD_INDI_BY_MESSAGE_MIME_TYPE)) {
                    int size = cs.additionalContents().rawContentsLength();
                    byte[] buffer = new byte[size];
                    for (int i = 0; i < size; i++) {
                        buffer[i] = (byte) cs.additionalContents().rawContents(i);
                    }
                    int length = buffer.length;
                    if (length > 1 && buffer[length - 1] == 0) {
                        Log.i(LOG_TAG, "Remove invalid last byte (0x00)");
                        length--;
                    }
                    byte[] ussdData = new byte[length];
                    System.arraycopy(buffer, 0, ussdData, 0, length);
                    ussdEvent.setData((Object) ussdData);
                    if (cs.state() == 11) {
                        ussdEvent.setStatus(3);
                    } else {
                        ussdEvent.setStatus(1);
                    }
                    ussdEvent.setDCS(0);
                }
            }
            if (ussdEvent.getData() == null) {
                ussdEvent.setData((Object) new byte[0]);
                ussdEvent.setStatus(3);
            }
        }
        if (cs == null || !ImsCallUtil.isCSFBbySIPErrorCode((int) cs.statusCode()) || state == UssdEvent.USSD_STATE.USSD_RESPONSE) {
            this.mUssdEventRegistrants.notifyResult(ussdEvent);
            return;
        }
        UssdEvent ussdError = new UssdEvent();
        ussdError.setSessionID(call.mSessionId);
        ussdError.setState(UssdEvent.USSD_STATE.USSD_ERROR);
        ussdError.setErrorCode(new SipError((int) cs.statusCode(), cs.reasonPhrase()));
        this.mUssdEventRegistrants.notifyResult(ussdError);
    }

    private void notifyIncomingCall(Call call, CallStatus cs) {
        int callType;
        int height;
        int width;
        boolean hasRemoteVideoCapa;
        Call call2 = call;
        if (call2 == null) {
            Log.i(LOG_TAG, "notifyIncomingCall : incoming call instance is null!!");
            return;
        }
        boolean z = true;
        if (cs != null) {
            callType = convertToCallTypeBackward(cs.callType());
        } else {
            callType = 1;
        }
        if (cs != null) {
            int i = 0;
            if (!cs.remoteVideoCapa() || !getLocalVideoCapa(call)) {
                z = false;
            }
            boolean hasRemoteVideoCapa2 = z;
            int width2 = (int) cs.width();
            int height2 = (int) cs.height();
            Mno mno = Mno.fromName(call2.mUa.getImsProfile().getMnoName());
            if (cs.isFocus() && (mno == Mno.SKT || mno == Mno.KT || mno == Mno.LGU || mno == Mno.KDDI)) {
                call2.mParam.setIsFocus("1");
            }
            CallParams callParams = call2.mParam;
            if (!cs.cvoEnabled()) {
                i = -1;
            }
            callParams.setVideoOrientation(i);
            hasRemoteVideoCapa = hasRemoteVideoCapa2;
            width = width2;
            height = height2;
        } else {
            hasRemoteVideoCapa = false;
            width = 480;
            height = 640;
        }
        call2.mParam.setVideoWidth(width);
        call2.mParam.setVideoHeight(height);
        IncomingCallEvent incomingCallEvent = new IncomingCallEvent(call2.mUa.getImsRegistration(), call2.mSessionId, callType, call2.mPeer, false, hasRemoteVideoCapa, call2.mParam);
        Log.i(LOG_TAG, "notifyIncomingCall() session: " + call2.mSessionId + ", callType: " + callType);
        this.mIncomingCallEventRegistrants.notifyResult(incomingCallEvent);
    }

    private boolean getLocalVideoCapa(Call call) {
        ImsRegistration reg;
        if (call == null || (reg = call.mUa.getImsRegistration()) == null) {
            return false;
        }
        return reg.hasService("mmtel-video");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:142:0x02dd, code lost:
        if (r5 == 5) goto L_0x02e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x031d, code lost:
        if (r5 != 5) goto L_0x0321;
     */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x0350  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0357  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x0397  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x03cf  */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x03d2  */
    /* JADX WARNING: Removed duplicated region for block: B:245:0x040c  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0417  */
    /* JADX WARNING: Removed duplicated region for block: B:249:0x0449  */
    /* JADX WARNING: Removed duplicated region for block: B:250:0x044d  */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x04e9  */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x05af A[SYNTHETIC, Splitter:B:306:0x05af] */
    /* JADX WARNING: Removed duplicated region for block: B:321:0x0605 A[Catch:{ XPathExpressionException -> 0x065c }] */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x061a A[Catch:{ XPathExpressionException -> 0x065c }] */
    /* JADX WARNING: Removed duplicated region for block: B:343:0x0688  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x0696  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x06a5  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyCallStatus(com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call r39, com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE r40, com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus r41, int r42) {
        /*
            r38 = this;
            r1 = r38
            r2 = r39
            r0 = r40
            r3 = r42
            if (r41 == 0) goto L_0x0013
            int r5 = r41.callType()
            int r5 = r1.convertToCallTypeBackward(r5)
            goto L_0x0014
        L_0x0013:
            r5 = 1
        L_0x0014:
            if (r41 == 0) goto L_0x001d
            long r7 = r41.statusCode()
            int r7 = (int) r7
            goto L_0x001e
        L_0x001d:
            r7 = 0
        L_0x001e:
            java.lang.String r8 = ""
            if (r41 == 0) goto L_0x0027
            java.lang.String r9 = r41.reasonPhrase()
            goto L_0x0028
        L_0x0027:
            r9 = r8
        L_0x0028:
            if (r41 == 0) goto L_0x0032
            boolean r10 = r41.remoteVideoCapa()
            if (r10 == 0) goto L_0x0032
            r10 = 1
            goto L_0x0033
        L_0x0032:
            r10 = 0
        L_0x0033:
            if (r41 == 0) goto L_0x003b
            long r11 = r41.width()
            int r11 = (int) r11
            goto L_0x003d
        L_0x003b:
            r11 = 480(0x1e0, float:6.73E-43)
        L_0x003d:
            if (r41 == 0) goto L_0x0045
            long r12 = r41.height()
            int r12 = (int) r12
            goto L_0x0047
        L_0x0045:
            r12 = 640(0x280, float:8.97E-43)
        L_0x0047:
            if (r41 == 0) goto L_0x004e
            java.lang.String r14 = r41.conferenceSupport()
            goto L_0x004f
        L_0x004e:
            r14 = 0
        L_0x004f:
            if (r41 == 0) goto L_0x0059
            boolean r15 = r41.isFocus()
            if (r15 == 0) goto L_0x0059
            r15 = 1
            goto L_0x005a
        L_0x0059:
            r15 = 0
        L_0x005a:
            if (r10 == 0) goto L_0x0065
            boolean r16 = r38.getLocalVideoCapa(r39)
            if (r16 == 0) goto L_0x0065
            r16 = 1
            goto L_0x0067
        L_0x0065:
            r16 = 0
        L_0x0067:
            if (r41 == 0) goto L_0x0071
            r18 = r14
            long r13 = r41.localVideoRtpPort()
            int r13 = (int) r13
            goto L_0x0074
        L_0x0071:
            r18 = r14
            r13 = 0
        L_0x0074:
            if (r41 == 0) goto L_0x007e
            r19 = r7
            long r6 = r41.localVideoRtcpPort()
            int r6 = (int) r6
            goto L_0x0081
        L_0x007e:
            r19 = r7
            r6 = 0
        L_0x0081:
            if (r41 == 0) goto L_0x008a
            r7 = r15
            long r14 = r41.remoteVideoRtpPort()
            int r14 = (int) r14
            goto L_0x008c
        L_0x008a:
            r7 = r15
            r14 = 0
        L_0x008c:
            if (r41 == 0) goto L_0x0096
            r21 = r5
            long r4 = r41.remoteVideoRtcpPort()
            int r4 = (int) r4
            goto L_0x0099
        L_0x0096:
            r21 = r5
            r4 = 0
        L_0x0099:
            if (r41 == 0) goto L_0x00a0
            java.lang.String r5 = r41.serviceUrn()
            goto L_0x00a1
        L_0x00a0:
            r5 = 0
        L_0x00a1:
            if (r41 == 0) goto L_0x00ab
            r22 = r4
            long r3 = r41.retryAfter()
            int r3 = (int) r3
            goto L_0x00ae
        L_0x00ab:
            r22 = r4
            r3 = 0
        L_0x00ae:
            if (r41 == 0) goto L_0x00b9
            boolean r4 = r41.localHoldTone()
            if (r4 == 0) goto L_0x00b7
            goto L_0x00b9
        L_0x00b7:
            r4 = 0
            goto L_0x00ba
        L_0x00b9:
            r4 = 1
        L_0x00ba:
            if (r41 == 0) goto L_0x00c1
            java.lang.String r23 = r41.historyInfo()
            goto L_0x00c3
        L_0x00c1:
            r23 = r8
        L_0x00c3:
            r24 = r23
            if (r41 == 0) goto L_0x00cc
            java.lang.String r23 = r41.dtmfEvent()
            goto L_0x00ce
        L_0x00cc:
            r23 = r8
        L_0x00ce:
            r25 = r23
            if (r41 == 0) goto L_0x00dc
            boolean r23 = r41.cvoEnabled()
            if (r23 == 0) goto L_0x00d9
            goto L_0x00dc
        L_0x00d9:
            r23 = 0
            goto L_0x00de
        L_0x00dc:
            r23 = 1
        L_0x00de:
            r26 = r23
            if (r41 == 0) goto L_0x00e7
            java.lang.String r23 = r41.alertInfo()
            goto L_0x00e9
        L_0x00e7:
            r23 = 0
        L_0x00e9:
            r27 = r23
            if (r41 == 0) goto L_0x00f7
            r23 = r3
            r28 = r4
            long r3 = r41.videoCrbtType()
            int r3 = (int) r3
            goto L_0x00fc
        L_0x00f7:
            r23 = r3
            r28 = r4
            r3 = 0
        L_0x00fc:
            if (r41 == 0) goto L_0x0103
            java.lang.String r4 = r41.cmcDeviceId()
            goto L_0x0104
        L_0x0103:
            r4 = r8
        L_0x0104:
            if (r41 == 0) goto L_0x0110
            r29 = r3
            r30 = r4
            long r3 = r41.audioRxTrackId()
            int r3 = (int) r3
            goto L_0x0115
        L_0x0110:
            r29 = r3
            r30 = r4
            r3 = 0
        L_0x0115:
            if (r41 == 0) goto L_0x011c
            java.lang.String r4 = r41.audioBitRate()
            goto L_0x011d
        L_0x011c:
            r4 = r8
        L_0x011d:
            if (r41 == 0) goto L_0x0123
            java.lang.String r8 = r41.cmcCallTime()
        L_0x0123:
            if (r41 == 0) goto L_0x012a
            java.lang.String r17 = r41.featureCaps()
            goto L_0x012c
        L_0x012a:
            r17 = 0
        L_0x012c:
            r31 = r17
            if (r41 == 0) goto L_0x013a
            r17 = r3
            r32 = r4
            long r3 = r41.audioEarlyMediaDir()
            int r3 = (int) r3
            goto L_0x013f
        L_0x013a:
            r17 = r3
            r32 = r4
            r3 = 0
        L_0x013f:
            com.sec.internal.ims.core.handler.secims.UserAgent r4 = r2.mUa
            com.sec.ims.settings.ImsProfile r4 = r4.getImsProfile()
            java.lang.String r4 = r4.getMnoName()
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.fromName(r4)
            boolean r33 = android.text.TextUtils.isEmpty(r5)
            if (r33 == 0) goto L_0x0156
            r33 = r5
            goto L_0x015a
        L_0x0156:
            java.lang.String r33 = com.sec.internal.log.IMSLog.checker(r5)
        L_0x015a:
            r34 = r33
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r35 = r5
            java.lang.String r5 = "notifyCallStatus() session: "
            r15.append(r5)
            int r5 = r2.mSessionId
            r15.append(r5)
            java.lang.String r5 = ", callstate: "
            r15.append(r5)
            r15.append(r0)
            java.lang.String r5 = ", callType: "
            r15.append(r5)
            r5 = r21
            r15.append(r5)
            java.lang.String r1 = ", statusCode: "
            r15.append(r1)
            r1 = r19
            r15.append(r1)
            java.lang.String r1 = ", reasonPhrase: "
            r15.append(r1)
            r15.append(r9)
            java.lang.String r1 = ", remoteVideoCapa: "
            r15.append(r1)
            r15.append(r10)
            java.lang.String r1 = ", localVideoCapa: "
            r15.append(r1)
            boolean r1 = r38.getLocalVideoCapa(r39)
            r15.append(r1)
            java.lang.String r1 = ", width: "
            r15.append(r1)
            r15.append(r11)
            java.lang.String r1 = ", height: "
            r15.append(r1)
            r15.append(r12)
            java.lang.String r1 = ", isFocus: "
            r15.append(r1)
            r15.append(r7)
            java.lang.String r1 = ", conferenceSupport: "
            r15.append(r1)
            r1 = r18
            r15.append(r1)
            r18 = r10
            java.lang.String r10 = ", localVideoRtpPort: "
            r15.append(r10)
            r15.append(r13)
            java.lang.String r10 = ", localVideoRtcpPort: "
            r15.append(r10)
            r15.append(r6)
            java.lang.String r10 = ", RemoteVideoRtpPort: "
            r15.append(r10)
            r15.append(r14)
            java.lang.String r10 = ", RemoteVideoRtcpPort: "
            r15.append(r10)
            r10 = r22
            r15.append(r10)
            r21 = r12
            java.lang.String r12 = ", ServiceUrn: "
            r15.append(r12)
            r12 = r34
            r15.append(r12)
            r22 = r12
            java.lang.String r12 = ", retryAfter: "
            r15.append(r12)
            r12 = r23
            r15.append(r12)
            java.lang.String r12 = ", historyInfo: "
            r15.append(r12)
            r12 = r24
            r15.append(r12)
            java.lang.String r12 = ", dtmfEvent: "
            r15.append(r12)
            r12 = r25
            r15.append(r12)
            java.lang.String r12 = ", holdTone: "
            r15.append(r12)
            r12 = r28
            r15.append(r12)
            java.lang.String r12 = ", cvoEnabled : "
            r15.append(r12)
            r12 = r26
            r15.append(r12)
            java.lang.String r12 = ", AlertInfo : "
            r15.append(r12)
            r12 = r27
            r15.append(r12)
            java.lang.String r12 = ", videoCrbtType : "
            r15.append(r12)
            r12 = r29
            r15.append(r12)
            java.lang.String r12 = ", cmcDeviceId : "
            r15.append(r12)
            r12 = r30
            r15.append(r12)
            java.lang.String r12 = ", audioRxTrackId : "
            r15.append(r12)
            r12 = r17
            r15.append(r12)
            java.lang.String r12 = ", audioBitRate : "
            r15.append(r12)
            r12 = r32
            r15.append(r12)
            java.lang.String r12 = ", cmcCallTime : "
            r15.append(r12)
            r15.append(r8)
            java.lang.String r12 = ", featureCaps: "
            r15.append(r12)
            r12 = r31
            r15.append(r12)
            r31 = r8
            java.lang.String r8 = ", audioEarlyMediaDir: "
            r15.append(r8)
            r15.append(r3)
            java.lang.String r8 = r15.toString()
            java.lang.String r15 = "ResipVolteHandler"
            android.util.Log.i(r15, r8)
            if (r1 == 0) goto L_0x0289
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r2.mParam
            r8.setConferenceSupported(r1)
        L_0x0289:
            if (r7 == 0) goto L_0x02e6
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r2.mParam
            r36 = r1
            java.lang.String r1 = "1"
            r8.setIsFocus(r1)
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.VZW
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TELSTRA
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.KDDI
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.VODAFONE_EG
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.SKT
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.KT
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.LGU
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.RJIL
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.PROXIMUS
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TELENOR_NORWAY
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.AIRTEL
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.ZAIN_KSA
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.MTN_SOUTHAFRICA
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.ETISALAT_EG
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.DIGI_HUNGARY
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.XL_ID
            if (r4 == r1) goto L_0x02e2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TMOUS
            if (r4 != r1) goto L_0x02e0
            r1 = 1
            if (r5 == r1) goto L_0x02e2
            r1 = r15
            r8 = 5
            if (r5 != r8) goto L_0x02e9
            goto L_0x02e3
        L_0x02e0:
            r1 = r15
            goto L_0x02e9
        L_0x02e2:
            r1 = r15
        L_0x02e3:
            r16 = 0
            goto L_0x02e9
        L_0x02e6:
            r36 = r1
            r1 = r15
        L_0x02e9:
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.TELSTRA
            if (r4 == r8) goto L_0x0326
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.TELENOR_SWE
            if (r4 == r8) goto L_0x0326
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.VODAFONE_EG
            if (r4 == r8) goto L_0x0326
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.VODAFONE_AUSTRALIA
            if (r4 == r8) goto L_0x0326
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.RJIL
            if (r4 == r8) goto L_0x0326
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.SWISSCOM
            if (r4 == r8) goto L_0x0326
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.WE_EG
            if (r4 == r8) goto L_0x0326
            boolean r8 = r4.isCanada()
            if (r8 != 0) goto L_0x0324
            boolean r8 = r4.isIndia()
            if (r8 != 0) goto L_0x0324
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.XL_ID
            if (r4 == r8) goto L_0x0324
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.TMOUS
            if (r4 != r8) goto L_0x0320
            r8 = 1
            if (r5 == r8) goto L_0x0327
            r15 = 5
            if (r5 != r15) goto L_0x0321
            goto L_0x0327
        L_0x0320:
            r8 = 1
        L_0x0321:
            r33 = 0
            goto L_0x0329
        L_0x0324:
            r8 = 1
            goto L_0x0327
        L_0x0326:
            r8 = 1
        L_0x0327:
            r33 = r8
        L_0x0329:
            r15 = r33
            boolean r8 = r2.isConference
            if (r8 == 0) goto L_0x0333
            if (r15 == 0) goto L_0x0333
            r16 = 0
        L_0x0333:
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r8 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE
            if (r0 != r8) goto L_0x033d
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.ATT
            if (r4 != r8) goto L_0x033d
            r16 = 0
        L_0x033d:
            boolean r8 = r4.isChn()
            if (r8 != 0) goto L_0x0357
            boolean r8 = r4.isJpn()
            if (r8 != 0) goto L_0x0357
            boolean r8 = r4.isKor()
            if (r8 == 0) goto L_0x0350
            goto L_0x0357
        L_0x0350:
            r34 = r15
            r8 = r16
            r16 = r7
            goto L_0x038f
        L_0x0357:
            boolean r8 = r4.isKor()
            if (r8 == 0) goto L_0x036b
            r8 = 2
            if (r5 != r8) goto L_0x036b
            r8 = 176(0xb0, float:2.47E-43)
            if (r11 != r8) goto L_0x036b
            java.lang.String r8 = "force to set modifiable to false for 3G QCIF Video Call"
            android.util.Log.i(r1, r8)
            r8 = 0
            goto L_0x036d
        L_0x036b:
            r8 = r16
        L_0x036d:
            r16 = r7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r34 = r15
            java.lang.String r15 = "setModifyHeader : "
            r7.append(r15)
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.i(r1, r7)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r7 = r2.mParam
            java.lang.String r15 = java.lang.Boolean.toString(r8)
            r7.setModifyHeader(r15)
        L_0x038f:
            r7 = r38
            boolean r15 = r7.IsModifiableNeedToBeIgnored(r0, r4)
            if (r15 == 0) goto L_0x039d
            r8 = 0
            java.lang.String r15 = "force to set modifiable to false"
            android.util.Log.i(r1, r15)
        L_0x039d:
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r15 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_LOCAL
            if (r0 == r15) goto L_0x03a9
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r15 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE
            if (r0 == r15) goto L_0x03a9
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r15 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_BOTH
            if (r0 != r15) goto L_0x03b7
        L_0x03a9:
            com.sec.internal.constants.Mno r15 = com.sec.internal.constants.Mno.RJIL
            if (r4 == r15) goto L_0x03b1
            com.sec.internal.constants.Mno r15 = com.sec.internal.constants.Mno.TELSTRA
            if (r4 != r15) goto L_0x03b7
        L_0x03b1:
            r8 = 0
            java.lang.String r15 = "force to set modifiable to false when call is held!!"
            android.util.Log.i(r1, r15)
        L_0x03b7:
            com.sec.internal.constants.Mno r15 = com.sec.internal.constants.Mno.DOCOMO
            if (r4 != r15) goto L_0x03c4
            r15 = 7
            if (r5 != r15) goto L_0x03c4
            r8 = 1
            java.lang.String r15 = "force to set modifiable to true for Docomo"
            android.util.Log.i(r1, r15)
        L_0x03c4:
            com.sec.internal.constants.Mno r15 = com.sec.internal.constants.Mno.DOCOMO
            r0 = 709(0x2c5, float:9.94E-43)
            if (r4 == r15) goto L_0x03d2
            com.sec.internal.constants.Mno r15 = com.sec.internal.constants.Mno.KDDI
            if (r4 != r15) goto L_0x03cf
            goto L_0x03d2
        L_0x03cf:
            r15 = r19
            goto L_0x03e0
        L_0x03d2:
            r15 = r19
            if (r15 != r0) goto L_0x03e0
            java.lang.String r0 = "deleteTcpClientSocket() at INVITE FLUSH"
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.core.handler.secims.UserAgent r0 = r2.mUa
            r0.deleteTcpClientSocket()
        L_0x03e0:
            boolean r0 = r4.isKor()
            if (r0 == 0) goto L_0x03fc
            r0 = 406(0x196, float:5.69E-43)
            if (r15 == r0) goto L_0x03f2
            r0 = 408(0x198, float:5.72E-43)
            if (r15 == r0) goto L_0x03f2
            r0 = 709(0x2c5, float:9.94E-43)
            if (r15 != r0) goto L_0x03fc
        L_0x03f2:
            java.lang.String r0 = "deleteTcpClientSocket() at INVITE FLUSH for KOR"
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.core.handler.secims.UserAgent r0 = r2.mUa
            r0.deleteTcpClientSocket()
        L_0x03fc:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.TELEFONICA_GERMANY
            if (r4 != r0) goto L_0x0417
            r0 = 5487(0x156f, float:7.689E-42)
            if (r15 != r0) goto L_0x0417
            java.lang.String r0 = "Session Terminated by UE"
            boolean r0 = r0.equals(r9)
            if (r0 == 0) goto L_0x0417
            java.lang.String r0 = "Remote side ends the call normally."
            android.util.Log.i(r1, r0)
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r0 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ENDED
            r15 = 0
            r7 = r15
            r15 = r0
            goto L_0x041a
        L_0x0417:
            r7 = r15
            r15 = r40
        L_0x041a:
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r0.setLocalVideoRTPPort(r13)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r0.setLocalVideoRTCPPort(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r0.setRemoteVideoRTPPort(r14)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r0.setRemoteVideoRTCPPort(r10)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r19 = r6
            r6 = r28
            r0.setLocalHoldTone(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r6 = r24
            r0.setHistoryInfo(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r6 = r25
            r0.setDtmfEvent(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            if (r26 == 0) goto L_0x044d
            r40 = r6
            r6 = 0
            goto L_0x0453
        L_0x044d:
            r25 = -1
            r40 = r6
            r6 = r25
        L_0x0453:
            r0.setVideoOrientation(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r6 = r27
            r0.setAlertInfo(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r6 = r29
            r0.setVideoCrbtType(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r0.setVideoWidth(r11)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r6 = r21
            r0.setVideoHeight(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r6 = r17
            r0.setAudioRxTrackId(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r6 = r32
            r0.setAudioBitRate(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r0.setFeatureCaps(r12)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r0.setAudioEarlyMediaDir(r3)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r25 = r3
            java.lang.String r3 = "setVideoOrientation_resip"
            r0.append(r3)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r3 = r2.mParam
            int r3 = r3.getVideoOrientation()
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r1, r0)
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent r0 = new com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent
            r0.<init>()
            r3 = r0
            r3.setCallType(r5)
            int r0 = r2.mSessionId
            r3.setSessionID(r0)
            com.sec.ims.util.NameAddr r0 = r2.mPeer
            r3.setPeerAddr(r0)
            r3.setState(r15)
            com.sec.ims.util.SipError r0 = new com.sec.ims.util.SipError
            r0.<init>(r7, r9)
            r3.setErrorCode(r0)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r2.mParam
            r3.setParams(r0)
            r3.setRemoteVideoCapa(r8)
            r32 = r5
            r5 = r23
            r3.setRetryAfter(r5)
            boolean r0 = r2.isConference
            r3.setConference(r0)
            r5 = r30
            r3.setCmcDeviceId(r5)
            r5 = r31
            r3.setCmcCallTime(r5)
            if (r41 == 0) goto L_0x0688
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r0 = r41.additionalContents()
            if (r0 == 0) goto L_0x0688
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r0 = r41.additionalContents()
            java.lang.String r0 = r0.mimeType()
            r31 = r5
            java.lang.String r5 = "application/3gpp-ims+xml"
            boolean r0 = r0.equals(r5)
            if (r0 == 0) goto L_0x067f
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r0 = r41.additionalContents()     // Catch:{ XPathExpressionException -> 0x0670 }
            java.lang.String r0 = r0.contents()     // Catch:{ XPathExpressionException -> 0x0670 }
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$AlternativeService r0 = com.sec.internal.ims.core.handler.secims.ResipVolteHandler.AlternativeServiceXmlParser.parseXml(r0)     // Catch:{ XPathExpressionException -> 0x0670 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r5 = r0.mAction     // Catch:{ XPathExpressionException -> 0x0670 }
            r37 = r6
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r6 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.ALTERNATIVE_SERVICE.INITIAL_REGISTRATION     // Catch:{ XPathExpressionException -> 0x0668 }
            if (r5 != r6) goto L_0x053f
            java.lang.String r5 = "initial registration handling required!"
            android.util.Log.i(r1, r5)     // Catch:{ XPathExpressionException -> 0x0536 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r5 = r0.mAction     // Catch:{ XPathExpressionException -> 0x0536 }
            r3.setAlternativeService(r5)     // Catch:{ XPathExpressionException -> 0x0536 }
            java.lang.String r5 = r0.mType     // Catch:{ XPathExpressionException -> 0x0536 }
            r3.setAlternativeServiceType(r5)     // Catch:{ XPathExpressionException -> 0x0536 }
            java.lang.String r5 = r0.mReason     // Catch:{ XPathExpressionException -> 0x0536 }
            r3.setAlternativeServiceReason(r5)     // Catch:{ XPathExpressionException -> 0x0536 }
            r5 = r35
            r3.setAlternativeServiceUrn(r5)     // Catch:{ XPathExpressionException -> 0x052f }
            r35 = r7
            r20 = r8
            goto L_0x065b
        L_0x052f:
            r0 = move-exception
            r35 = r7
            r20 = r8
            goto L_0x0679
        L_0x0536:
            r0 = move-exception
            r5 = r35
            r35 = r7
            r20 = r8
            goto L_0x0679
        L_0x053f:
            r5 = r35
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r6 = r0.mAction     // Catch:{ XPathExpressionException -> 0x0662 }
            r35 = r7
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION     // Catch:{ XPathExpressionException -> 0x065e }
            if (r6 == r7) goto L_0x0581
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r6 = r0.mAction     // Catch:{ XPathExpressionException -> 0x057c }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY     // Catch:{ XPathExpressionException -> 0x057c }
            if (r6 != r7) goto L_0x0550
            goto L_0x0581
        L_0x0550:
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.CMCC     // Catch:{ XPathExpressionException -> 0x057c }
            if (r4 != r6) goto L_0x0578
            boolean r6 = r5.isEmpty()     // Catch:{ XPathExpressionException -> 0x057c }
            if (r6 != 0) goto L_0x0578
            java.lang.String r6 = r0.mType     // Catch:{ XPathExpressionException -> 0x057c }
            boolean r6 = r6.isEmpty()     // Catch:{ XPathExpressionException -> 0x057c }
            if (r6 != 0) goto L_0x0578
            java.lang.String r6 = "For CMCC emergency call alternative-service handling required!"
            android.util.Log.i(r1, r6)     // Catch:{ XPathExpressionException -> 0x057c }
            java.lang.String r6 = r0.mType     // Catch:{ XPathExpressionException -> 0x057c }
            r3.setAlternativeServiceType(r6)     // Catch:{ XPathExpressionException -> 0x057c }
            java.lang.String r6 = r0.mReason     // Catch:{ XPathExpressionException -> 0x057c }
            r3.setAlternativeServiceReason(r6)     // Catch:{ XPathExpressionException -> 0x057c }
            r3.setAlternativeServiceUrn(r5)     // Catch:{ XPathExpressionException -> 0x057c }
            r20 = r8
            goto L_0x065b
        L_0x0578:
            r20 = r8
            goto L_0x065b
        L_0x057c:
            r0 = move-exception
            r20 = r8
            goto L_0x0679
        L_0x0581:
            com.sec.internal.ims.core.handler.secims.UserAgent r6 = r2.mUa     // Catch:{ XPathExpressionException -> 0x065e }
            com.sec.internal.interfaces.ims.core.IPdnController r6 = r6.getPdnController()     // Catch:{ XPathExpressionException -> 0x065e }
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = r2.mUa     // Catch:{ XPathExpressionException -> 0x065e }
            int r7 = r7.getPhoneId()     // Catch:{ XPathExpressionException -> 0x065e }
            com.sec.internal.constants.ims.os.EmcBsIndication r6 = r6.getEmcBsIndication(r7)     // Catch:{ XPathExpressionException -> 0x065e }
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = r2.mUa     // Catch:{ XPathExpressionException -> 0x065e }
            com.sec.ims.settings.ImsProfile r7 = r7.getImsProfile()     // Catch:{ XPathExpressionException -> 0x065e }
            boolean r7 = r7.getSupport380PolicyByEmcbs()     // Catch:{ XPathExpressionException -> 0x065e }
            if (r7 == 0) goto L_0x05a4
            com.sec.internal.constants.ims.os.EmcBsIndication r7 = com.sec.internal.constants.ims.os.EmcBsIndication.NOT_SUPPORTED     // Catch:{ XPathExpressionException -> 0x057c }
            if (r6 != r7) goto L_0x05a4
            r33 = 1
            goto L_0x05a6
        L_0x05a4:
            r33 = 0
        L_0x05a6:
            r7 = r33
            r20 = r8
            java.lang.String r8 = "urn:service:sos"
            if (r7 == 0) goto L_0x05e7
            boolean r33 = android.text.TextUtils.isEmpty(r5)     // Catch:{ XPathExpressionException -> 0x065c }
            if (r33 != 0) goto L_0x05e7
            r33 = r7
            java.lang.String r7 = r5.toLowerCase()     // Catch:{ XPathExpressionException -> 0x065c }
            boolean r7 = r7.contains(r8)     // Catch:{ XPathExpressionException -> 0x065c }
            if (r7 == 0) goto L_0x05e9
            java.util.Set<java.lang.String> r7 = mMainSosSubserviceSet     // Catch:{ XPathExpressionException -> 0x065c }
            boolean r7 = r7.contains(r5)     // Catch:{ XPathExpressionException -> 0x065c }
            if (r7 != 0) goto L_0x05e9
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ XPathExpressionException -> 0x065c }
            r7.<init>()     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r8 = "alternative-service handling NOT required! serviceUrn: "
            r7.append(r8)     // Catch:{ XPathExpressionException -> 0x065c }
            r7.append(r5)     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r8 = " eMCBS: "
            r7.append(r8)     // Catch:{ XPathExpressionException -> 0x065c }
            r7.append(r6)     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r7 = r7.toString()     // Catch:{ XPathExpressionException -> 0x065c }
            android.util.Log.e(r1, r7)     // Catch:{ XPathExpressionException -> 0x065c }
            goto L_0x065a
        L_0x05e7:
            r33 = r7
        L_0x05e9:
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = r2.mUa     // Catch:{ XPathExpressionException -> 0x065c }
            com.sec.ims.settings.ImsProfile r7 = r7.getImsProfile()     // Catch:{ XPathExpressionException -> 0x065c }
            boolean r7 = r7.getSosUrnRequired()     // Catch:{ XPathExpressionException -> 0x065c }
            if (r7 == 0) goto L_0x061a
            boolean r7 = android.text.TextUtils.isEmpty(r5)     // Catch:{ XPathExpressionException -> 0x065c }
            if (r7 != 0) goto L_0x0605
            java.lang.String r7 = r5.toLowerCase()     // Catch:{ XPathExpressionException -> 0x065c }
            boolean r7 = r7.contains(r8)     // Catch:{ XPathExpressionException -> 0x065c }
            if (r7 != 0) goto L_0x061a
        L_0x0605:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ XPathExpressionException -> 0x065c }
            r7.<init>()     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r8 = "alternative-service handling NOT required!, eMCBS: "
            r7.append(r8)     // Catch:{ XPathExpressionException -> 0x065c }
            r7.append(r6)     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r7 = r7.toString()     // Catch:{ XPathExpressionException -> 0x065c }
            android.util.Log.e(r1, r7)     // Catch:{ XPathExpressionException -> 0x065c }
            goto L_0x065a
        L_0x061a:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ XPathExpressionException -> 0x065c }
            r7.<init>()     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r2 = "alternative-service handling required!, eMCBS: "
            r7.append(r2)     // Catch:{ XPathExpressionException -> 0x065c }
            r7.append(r6)     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r2 = r7.toString()     // Catch:{ XPathExpressionException -> 0x065c }
            android.util.Log.e(r1, r2)     // Catch:{ XPathExpressionException -> 0x065c }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r2 = r0.mAction     // Catch:{ XPathExpressionException -> 0x065c }
            r3.setAlternativeService(r2)     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r2 = r0.mType     // Catch:{ XPathExpressionException -> 0x065c }
            r3.setAlternativeServiceType(r2)     // Catch:{ XPathExpressionException -> 0x065c }
            java.lang.String r2 = r0.mReason     // Catch:{ XPathExpressionException -> 0x065c }
            r3.setAlternativeServiceReason(r2)     // Catch:{ XPathExpressionException -> 0x065c }
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CMCC     // Catch:{ XPathExpressionException -> 0x065c }
            if (r4 == r2) goto L_0x0647
            boolean r2 = r4.isEur()     // Catch:{ XPathExpressionException -> 0x065c }
            if (r2 == 0) goto L_0x0657
        L_0x0647:
            boolean r2 = r5.isEmpty()     // Catch:{ XPathExpressionException -> 0x065c }
            if (r2 == 0) goto L_0x0657
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r2 = r0.mAction     // Catch:{ XPathExpressionException -> 0x065c }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION     // Catch:{ XPathExpressionException -> 0x065c }
            if (r2 != r7) goto L_0x0657
            r3.setAlternativeServiceUrn(r8)     // Catch:{ XPathExpressionException -> 0x065c }
            goto L_0x065a
        L_0x0657:
            r3.setAlternativeServiceUrn(r5)     // Catch:{ XPathExpressionException -> 0x065c }
        L_0x065a:
        L_0x065b:
            goto L_0x0692
        L_0x065c:
            r0 = move-exception
            goto L_0x0679
        L_0x065e:
            r0 = move-exception
            r20 = r8
            goto L_0x0679
        L_0x0662:
            r0 = move-exception
            r35 = r7
            r20 = r8
            goto L_0x0679
        L_0x0668:
            r0 = move-exception
            r20 = r8
            r5 = r35
            r35 = r7
            goto L_0x0679
        L_0x0670:
            r0 = move-exception
            r37 = r6
            r20 = r8
            r5 = r35
            r35 = r7
        L_0x0679:
            java.lang.String r2 = "notifyCallStatus: error parsing AlternativeService xml"
            android.util.Log.e(r1, r2, r0)
            goto L_0x0692
        L_0x067f:
            r37 = r6
            r20 = r8
            r5 = r35
            r35 = r7
            goto L_0x0692
        L_0x0688:
            r31 = r5
            r37 = r6
            r20 = r8
            r5 = r35
            r35 = r7
        L_0x0692:
            r1 = r42
            if (r1 <= 0) goto L_0x06a5
            r0 = 200(0xc8, float:2.8E-43)
            r2 = r38
            r6 = r35
            android.os.Message r0 = r2.obtainMessage(r0, r3)
            long r7 = (long) r1
            r2.sendMessageDelayed(r0, r7)
            return
        L_0x06a5:
            r2 = r38
            com.sec.internal.helper.RegistrantList r0 = r2.mCallStateEventRegistrants
            r0.notifyResult(r3)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.notifyCallStatus(com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call, com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE, com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus, int):void");
    }

    public static class AlternativeServiceXmlParser {
        public static AlternativeService parseXml(String xml) throws XPathExpressionException {
            AlternativeService svc = new AlternativeService();
            Log.i(ResipVolteHandler.LOG_TAG, "AlternativeServiceXmlParser parseXml:" + xml);
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression expAlternativeService = xPath.compile("//ims-3gpp/alternative-service");
            XPathExpression expType = xPath.compile("type");
            XPathExpression expReason = xPath.compile("reason");
            XPathExpression expAction = xPath.compile("action");
            Node NodeAs = (Node) expAlternativeService.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODE);
            if (NodeAs == null) {
                Log.i(ResipVolteHandler.LOG_TAG, "parseXml not found Node '/ims-3gpp/alternative-service");
                return svc;
            }
            String strType = expType.evaluate(NodeAs);
            String strReason = expReason.evaluate(NodeAs);
            String strAction = expAction.evaluate(NodeAs).replace("\n", "");
            Log.i(ResipVolteHandler.LOG_TAG, "parseXml:" + strType + "," + strReason + "," + strAction);
            if ("initial-registration".equals(strAction)) {
                Log.i(ResipVolteHandler.LOG_TAG, "initial-registration is found");
                svc.mAction = CallStateEvent.ALTERNATIVE_SERVICE.INITIAL_REGISTRATION;
                svc.mType = strType;
                svc.mReason = strReason;
            } else if ("emergency-registration".equals(strAction)) {
                svc.mAction = CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION;
                svc.mType = strType;
                svc.mReason = strReason;
            } else if ("emergency".equals(strType)) {
                svc.mAction = CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY;
                svc.mType = strType;
                svc.mReason = strReason;
            }
            return svc;
        }
    }

    public static class CmcInfoXmlParser {
        public static CmcInfoEvent parseXml(String xml) throws XPathExpressionException {
            CmcInfoEvent cmcInfoEvent = new CmcInfoEvent();
            Log.i(ResipVolteHandler.LOG_TAG, "CmcInfoXmlParser parseXml:" + xml);
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression expCmcInfoData = xPath.compile("//cmc-info-data");
            XPathExpression expRecordEvent = xPath.compile("record-event");
            XPathExpression expExternalCallId = xPath.compile("external-call-id");
            Node NodeAs = (Node) expCmcInfoData.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODE);
            if (NodeAs == null) {
                Log.i(ResipVolteHandler.LOG_TAG, "parseXml not found Node : cmc-info-data");
                return cmcInfoEvent;
            }
            String strRecordEvent = expRecordEvent.evaluate(NodeAs);
            String strExternalCallId = expExternalCallId.evaluate(NodeAs);
            Log.i(ResipVolteHandler.LOG_TAG, "parseXml: " + strRecordEvent + ", " + strExternalCallId);
            cmcInfoEvent.setRecordEvent(Integer.parseInt(strRecordEvent));
            cmcInfoEvent.setExternalCallId(strExternalCallId);
            return cmcInfoEvent;
        }
    }

    private void onMakeCallResponse(AsyncResult result) {
        Log.i(LOG_TAG, "onMakeCallResponse:");
        CallResponse cr = (CallResponse) result.result;
        int sessionId = cr.session();
        int success = cr.result();
        int reason = cr.reason();
        Call call = (Call) result.userObj;
        Log.i(LOG_TAG, "onMakeCallResponse: nameAddr=" + IMSLog.checker(call.mPeer + "") + " session=" + sessionId + " success=" + success + " reason=" + reason);
        call.mSessionId = sessionId;
        call.mResponse = cr;
        if (cr.sipCallId() != null) {
            call.mParam.setSipCallId(cr.sipCallId());
        }
        if (success == 1) {
            call.mUa.stopCamera();
        } else {
            addCall(sessionId, call);
        }
        call.mLock.countDown();
    }

    private void onHoldResumeResponse(AsyncResult result, boolean isHold) {
        CallResponse cr = (CallResponse) result.result;
        int sessionId = cr.session();
        int success = cr.result();
        int reason = cr.reason();
        StringBuilder sb = new StringBuilder();
        sb.append("onHoldResumeResponse: ");
        sb.append(isHold ? "hold" : "resume");
        sb.append(" session=");
        sb.append(sessionId);
        sb.append(" success=");
        sb.append(success);
        sb.append(" reason=");
        sb.append(reason);
        Log.i(LOG_TAG, sb.toString());
    }

    private void onInfoResponse(AsyncResult result) {
        UssdEvent ussdRsp = new UssdEvent();
        GeneralResponse gr = (GeneralResponse) result.result;
        if (gr.result() == 0) {
            ussdRsp.setState(UssdEvent.USSD_STATE.USSD_RESPONSE);
        } else {
            ussdRsp.setState(UssdEvent.USSD_STATE.USSD_ERROR);
        }
        ussdRsp.setErrorCode(new SipError((int) gr.sipError(), gr.errorStr()));
        this.mUssdEventRegistrants.notifyResult(ussdRsp);
    }

    /* access modifiers changed from: private */
    public void onUpdateAudioInterfaceResponse(AsyncResult result) {
        Log.i(LOG_TAG, "onUpdateAudioInterfaceResponse:");
        ((CountDownLatch) result.userObj).countDown();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00b2, code lost:
        android.util.Log.i(LOG_TAG, "Find conference call!!");
        r6 = r10;
        r6.mCallType = r3.callType();
        r6.isConference = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onCallStateChange(com.sec.internal.helper.AsyncResult r17) {
        /*
            r16 = this;
            r1 = r16
            r2 = r17
            java.lang.Object r0 = r2.result
            r3 = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus r3 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus) r3
            long r4 = r3.session()
            int r4 = (int) r4
            int r5 = r3.state()
            r0 = 0
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r6 = r1.getCallBySession(r4)
            if (r6 == 0) goto L_0x0025
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = r6.mUa
            if (r7 == 0) goto L_0x0025
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = r6.mUa
            int r0 = r7.getPhoneId()
            r7 = r0
            goto L_0x0026
        L_0x0025:
            r7 = r0
        L_0x0026:
            com.sec.internal.constants.Mno r8 = com.sec.internal.helper.SimUtil.getSimMno(r7)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r9 = "onCallStateChange() session: "
            r0.append(r9)
            r0.append(r4)
            java.lang.String r9 = " state: "
            r0.append(r9)
            int r9 = r3.state()
            r0.append(r9)
            java.lang.String r9 = " calltype : "
            r0.append(r9)
            int r9 = r3.callType()
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            java.lang.String r9 = "ResipVolteHandler"
            android.util.Log.i(r9, r0)
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.SKT
            r9 = 18
            r10 = 14
            r11 = 8
            r12 = 6
            r13 = 11
            if (r8 != r0) goto L_0x00d4
            int r0 = r3.callType()
            if (r0 != r12) goto L_0x00d4
            if (r5 == r11) goto L_0x0073
            if (r5 == r13) goto L_0x0073
            if (r5 == r10) goto L_0x0073
            if (r5 != r9) goto L_0x00d4
        L_0x0073:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r15 = r1.mCallList
            monitor-enter(r15)
            r0 = 0
        L_0x0077:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r9 = r1.mCallList     // Catch:{ all -> 0x00d1 }
            int r9 = r9.size()     // Catch:{ all -> 0x00d1 }
            if (r0 >= r9) goto L_0x00cf
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r9 = r1.mCallList     // Catch:{ all -> 0x00d1 }
            int r9 = r9.keyAt(r0)     // Catch:{ all -> 0x00d1 }
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r10 = r1.mCallList     // Catch:{ all -> 0x00d1 }
            java.lang.Object r10 = r10.get(r9)     // Catch:{ all -> 0x00d1 }
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r10 = (com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call) r10     // Catch:{ all -> 0x00d1 }
            if (r10 == 0) goto L_0x00a7
            java.lang.String r13 = "ResipVolteHandler"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d1 }
            r11.<init>()     // Catch:{ all -> 0x00d1 }
            java.lang.String r14 = "candidate callType :  "
            r11.append(r14)     // Catch:{ all -> 0x00d1 }
            int r14 = r10.mCallType     // Catch:{ all -> 0x00d1 }
            r11.append(r14)     // Catch:{ all -> 0x00d1 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x00d1 }
            android.util.Log.i(r13, r11)     // Catch:{ all -> 0x00d1 }
        L_0x00a7:
            if (r10 == 0) goto L_0x00c4
            int r11 = r10.mCallType     // Catch:{ all -> 0x00d1 }
            r13 = 2
            if (r11 == r13) goto L_0x00b2
            int r11 = r10.mCallType     // Catch:{ all -> 0x00d1 }
            if (r11 != r12) goto L_0x00c4
        L_0x00b2:
            java.lang.String r11 = "ResipVolteHandler"
            java.lang.String r13 = "Find conference call!!"
            android.util.Log.i(r11, r13)     // Catch:{ all -> 0x00d1 }
            r6 = r10
            int r11 = r3.callType()     // Catch:{ all -> 0x00d1 }
            r6.mCallType = r11     // Catch:{ all -> 0x00d1 }
            r11 = 1
            r6.isConference = r11     // Catch:{ all -> 0x00d1 }
            goto L_0x00cf
        L_0x00c4:
            int r0 = r0 + 1
            r9 = 18
            r10 = 14
            r11 = 8
            r13 = 11
            goto L_0x0077
        L_0x00cf:
            monitor-exit(r15)     // Catch:{ all -> 0x00d1 }
            goto L_0x00d4
        L_0x00d1:
            r0 = move-exception
            monitor-exit(r15)     // Catch:{ all -> 0x00d1 }
            throw r0
        L_0x00d4:
            if (r6 != 0) goto L_0x0116
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r9 = "onCallStateChange: unknown sessionId "
            r0.append(r9)
            r0.append(r4)
            java.lang.String r0 = r0.toString()
            java.lang.String r9 = "ResipVolteHandler"
            android.util.Log.i(r9, r0)
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.TELEFONICA_UK
            if (r8 != r0) goto L_0x0115
            long r9 = r3.statusCode()
            r11 = 708(0x2c4, double:3.5E-321)
            int r0 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1))
            if (r0 != 0) goto L_0x0115
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r9 = "onCallStateChange: notifyCallStatus if 708"
            android.util.Log.i(r0, r9)
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent r0 = new com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent
            r0.<init>()
            com.sec.ims.util.SipError r9 = new com.sec.ims.util.SipError
            r10 = 708(0x2c4, float:9.92E-43)
            r9.<init>(r10)
            r0.setErrorCode(r9)
            com.sec.internal.helper.RegistrantList r9 = r1.mCallStateEventRegistrants
            r9.notifyResult(r0)
        L_0x0115:
            return
        L_0x0116:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.LGU
            if (r8 != r0) goto L_0x013a
            int r0 = r3.callType()
            if (r0 == r12) goto L_0x0127
            int r0 = r3.callType()
            r9 = 5
            if (r0 != r9) goto L_0x013a
        L_0x0127:
            r0 = 8
            if (r5 == r0) goto L_0x0137
            r0 = 11
            if (r5 == r0) goto L_0x0137
            r0 = 14
            if (r5 == r0) goto L_0x0137
            r0 = 18
            if (r5 != r0) goto L_0x013a
        L_0x0137:
            r0 = 1
            r6.isConference = r0
        L_0x013a:
            int r0 = r3.callType()
            r9 = 12
            if (r0 != r9) goto L_0x0181
            r0 = 8
            if (r5 != r0) goto L_0x0161
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r6.mParam
            boolean r0 = r0.isIncomingCall()
            if (r0 == 0) goto L_0x015b
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r9 = "USSD indicated from network"
            android.util.Log.i(r0, r9)
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r0 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_INDICATION
            r1.notifyUssdEvent(r6, r0, r3)
            goto L_0x0181
        L_0x015b:
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r0 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_RESPONSE
            r1.notifyUssdEvent(r6, r0, r3)
            goto L_0x0181
        L_0x0161:
            r0 = 11
            if (r5 != r0) goto L_0x016e
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r0 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_INDICATION
            r1.notifyUssdEvent(r6, r0, r3)
            r1.removeCall(r4)
            return
        L_0x016e:
            r0 = 17
            if (r5 != r0) goto L_0x0178
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r0 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_INDICATION
            r1.notifyUssdEvent(r6, r0, r3)
            goto L_0x0181
        L_0x0178:
            r0 = 19
            if (r5 != r0) goto L_0x0181
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r0 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_RESPONSE
            r1.notifyUssdEvent(r6, r0, r3)
        L_0x0181:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r9 = "audioCodec="
            r0.append(r9)
            java.lang.String r9 = r3.audioCodecName()
            r0.append(r9)
            java.lang.String r9 = " remoteMmtelCapa="
            r0.append(r9)
            boolean r9 = r3.remoteMmtelCapa()
            r0.append(r9)
            java.lang.String r9 = " remoteVideoCapa="
            r0.append(r9)
            boolean r9 = r3.remoteVideoCapa()
            r0.append(r9)
            java.lang.String r9 = " height="
            r0.append(r9)
            long r9 = r3.height()
            r0.append(r9)
            java.lang.String r9 = " width="
            r0.append(r9)
            long r9 = r3.width()
            r0.append(r9)
            java.lang.String r9 = " isFocus="
            r0.append(r9)
            boolean r9 = r3.isFocus()
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            java.lang.String r9 = "ResipVolteHandler"
            android.util.Log.i(r9, r0)
            r0 = 805306368(0x30000000, float:4.656613E-10)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r9.append(r7)
            java.lang.String r10 = ","
            r9.append(r10)
            r9.append(r4)
            java.lang.String r10 = ","
            r9.append(r10)
            r9.append(r5)
            java.lang.String r10 = ","
            r9.append(r10)
            int r10 = r3.callType()
            r9.append(r10)
            java.lang.String r10 = ","
            r9.append(r10)
            java.lang.String r10 = r3.audioCodecName()
            r9.append(r10)
            java.lang.String r10 = ","
            r9.append(r10)
            boolean r10 = r3.remoteVideoCapa()
            r11 = 0
            if (r10 == 0) goto L_0x021d
            boolean r10 = r1.getLocalVideoCapa(r6)
            if (r10 == 0) goto L_0x021d
            r10 = 1
            goto L_0x021e
        L_0x021d:
            r10 = r11
        L_0x021e:
            r9.append(r10)
            java.lang.String r10 = ","
            r9.append(r10)
            long r12 = r3.height()
            r9.append(r12)
            java.lang.String r10 = ","
            r9.append(r10)
            long r12 = r3.width()
            r9.append(r12)
            java.lang.String r10 = ","
            r9.append(r10)
            boolean r10 = r3.isFocus()
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            com.sec.internal.log.IMSLog.c(r0, r9)
            com.sec.internal.ims.core.handler.secims.UserAgent r0 = r6.mUa
            com.sec.ims.settings.ImsProfile r0 = r0.getImsProfile()
            java.lang.String r0 = r0.getMnoName()
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.fromName(r0)
            java.lang.String r9 = r3.audioCodecName()
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r10 = "onCallStateChange: audioCodec "
            r0.append(r10)
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            java.lang.String r10 = "ResipVolteHandler"
            android.util.Log.i(r10, r0)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r6.mParam
            r10 = 1
            r0.setisHDIcon(r10)
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.VZW
            if (r8 == r0) goto L_0x0282
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.SINGTEL
            if (r8 != r0) goto L_0x0294
        L_0x0282:
            boolean r0 = r3.remoteMmtelCapa()
            if (r0 != 0) goto L_0x0294
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r10 = "disable HD icon by remote doesn't have MMTEL capability"
            android.util.Log.i(r0, r10)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r6.mParam
            r0.setisHDIcon(r11)
        L_0x0294:
            boolean r0 = android.text.TextUtils.isEmpty(r9)
            if (r0 != 0) goto L_0x02a0
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r6.mParam
            r0.setAudioCodec(r9)
            goto L_0x02b5
        L_0x02a0:
            r0 = 4
            if (r5 != r0) goto L_0x02b5
            boolean r0 = r8.isKor()
            if (r0 == 0) goto L_0x02b5
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r10 = "KOR model need to update audio codec as NULL"
            android.util.Log.i(r0, r10)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r6.mParam
            r0.setAudioCodec(r9)
        L_0x02b5:
            r0 = 3
            if (r5 != r0) goto L_0x02c3
            r1.notifyIncomingCall(r6, r3)
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r10 = "onCallStateChange: Incoming call event"
            android.util.Log.i(r0, r10)
            return
        L_0x02c3:
            r0 = 10
            if (r5 != r0) goto L_0x02cd
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r0 = r6.mParam
            r10 = 1
            r0.setIndicationFlag(r10)
        L_0x02cd:
            long r10 = r3.statusCode()
            int r0 = (int) r10
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r10 = r1.convertToVolteState(r5, r0)
            if (r10 != 0) goto L_0x02ef
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r11 = "onCallStateChange: unknown event "
            r0.append(r11)
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            java.lang.String r11 = "ResipVolteHandler"
            android.util.Log.i(r11, r0)
            return
        L_0x02ef:
            int r0 = r3.callType()
            r6.mCallType = r0
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r0 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.EXTEND_TO_CONFERENCE
            if (r10 != r0) goto L_0x0314
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r11 = "extend to conference event "
            r0.append(r11)
            long r11 = r3.statusCode()
            int r11 = (int) r11
            r0.append(r11)
            java.lang.String r0 = r0.toString()
            java.lang.String r11 = "ResipVolteHandler"
            android.util.Log.i(r11, r0)
        L_0x0314:
            r0 = -1
            r1.notifyCallStatus(r6, r10, r3, r0)
            r0 = 11
            if (r5 != r0) goto L_0x0373
            boolean r0 = r6.isConference
            if (r0 == 0) goto L_0x0340
            long r11 = r3.statusCode()
            int r0 = (int) r11
            r11 = 800(0x320, float:1.121E-42)
            if (r0 == r11) goto L_0x0338
            long r11 = r3.statusCode()
            int r0 = (int) r11
            r11 = 606(0x25e, float:8.49E-43)
            if (r0 != r11) goto L_0x0340
            boolean r0 = r8.isChn()
            if (r0 != 0) goto L_0x0340
        L_0x0338:
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r11 = "conference call error received; don't remove session yet."
            android.util.Log.i(r0, r11)
            goto L_0x0373
        L_0x0340:
            boolean r0 = r6.isConference
            if (r0 == 0) goto L_0x0363
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.SKT
            if (r8 != r0) goto L_0x0363
            long r11 = r3.statusCode()
            int r0 = (int) r11
            if (r0 != 0) goto L_0x0363
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r11 = "conference call is ended; clear all call List"
            android.util.Log.i(r0, r11)
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r11 = r1.mCallList
            monitor-enter(r11)
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r0 = r1.mCallList     // Catch:{ all -> 0x0360 }
            r0.clear()     // Catch:{ all -> 0x0360 }
            monitor-exit(r11)     // Catch:{ all -> 0x0360 }
            goto L_0x0373
        L_0x0360:
            r0 = move-exception
            monitor-exit(r11)     // Catch:{ all -> 0x0360 }
            throw r0
        L_0x0363:
            boolean r0 = r6.isConference
            if (r0 == 0) goto L_0x0370
            long r11 = r3.statusCode()
            int r0 = (int) r11
            r11 = 486(0x1e6, float:6.81E-43)
            if (r0 == r11) goto L_0x0373
        L_0x0370:
            r1.removeCall(r4)
        L_0x0373:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.onCallStateChange(com.sec.internal.helper.AsyncResult):void");
    }

    private void onModifyCall(AsyncResult result) {
        ModifyCallData modifyData = (ModifyCallData) result.result;
        int sessionId = (int) modifyData.session();
        int oldCallType = (int) modifyData.oldType();
        int newCallType = (int) modifyData.newType();
        boolean isSdToSdPull = modifyData.isSdToSdPull();
        Call call = getCallBySession(sessionId);
        if (call == null) {
            Log.i(LOG_TAG, "onModifyCall: unknown sessionId " + sessionId);
            return;
        }
        Mno mno = Mno.fromName(call.mUa.getImsProfile().getMnoName());
        Log.i(LOG_TAG, "onModifyCall() session: " + sessionId + ", oldCallType: " + oldCallType + ", newCallType: " + newCallType + ", isSdToSdPull: " + isSdToSdPull);
        if (ImsCallUtil.isUpgradeCall(oldCallType, newCallType) && ((this.mTtyMode[call.mUa.getPhoneId()] != Extensions.TelecomManager.TTY_MODE_OFF && this.mTtyMode[call.mUa.getPhoneId()] != Extensions.TelecomManager.RTT_MODE) || getCall(9) != null)) {
            Log.i(LOG_TAG, "Rejecting modify request since TTY call(" + this.mTtyMode[call.mUa.getPhoneId()] + ") exists");
            rejectModifyCallType(sessionId, Id.REQUEST_UPDATE_TIME_IN_PLANI);
        } else if (mno == Mno.ATT && oldCallType == 1 && newCallType == 3) {
            Log.i(LOG_TAG, "onModifyCall: ATT - RX upgrade to videoshare with recvonly -> automatically reject with audio only 200 OK");
            replyModifyCallType(sessionId, oldCallType, oldCallType, newCallType);
        } else if (mno != Mno.RJIL || !ImsCallUtil.isOneWayVideoCall(newCallType)) {
            call.mCallType = convertToCallTypeBackward(newCallType);
            CallStateEvent callStateEvent = new CallStateEvent();
            callStateEvent.setState(CallStateEvent.CALL_STATE.MODIFY_REQUESTED);
            callStateEvent.setCallType(newCallType);
            callStateEvent.setSessionID(sessionId);
            callStateEvent.setIsSdToSdPull(isSdToSdPull);
            callStateEvent.setParams(call.mParam);
            this.mCallStateEventRegistrants.notifyResult(callStateEvent);
        } else {
            Log.i(LOG_TAG, "onModifyCall: RJIL - network does not support 1-way videoreject with 603");
            rejectModifyCallType(sessionId, Id.REQUEST_UPDATE_TIME_IN_PLANI);
        }
    }

    private void onNewIncomingCall(AsyncResult result) {
        IncomingCall nc = (IncomingCall) result.result;
        ImsUri uri = null;
        UserAgent ua = getUa((int) nc.handle());
        if (ua == null) {
            Log.i(LOG_TAG, "onNewIncomingCall: UserAgent not found.");
            return;
        }
        if (nc.peeruri() != null) {
            uri = ImsUri.parse(nc.peeruri());
        }
        Call call = new Call(ua, new NameAddr(nc.displayName(), uri));
        call.mSessionId = (int) nc.session();
        call.mCallType = nc.callType();
        CallParams param = new CallParams();
        param.setAsIncomingCall();
        if (nc.referredBy() != null) {
            param.setReferredBy(nc.referredBy());
        }
        if (nc.sipCallId() != null) {
            param.setSipCallId(nc.sipCallId());
        }
        if (nc.rawSipmsg() != null) {
            param.setSipInviteMsg(nc.rawSipmsg());
        }
        if (nc.terminatingId() != null) {
            param.setTerminatingId(ImsUri.parse(nc.terminatingId()));
        }
        if (nc.numberPlus() != null) {
            param.setNumberPlus(nc.numberPlus());
        }
        if (nc.replaces() != null) {
            param.setReplaces(nc.replaces());
        }
        if (nc.photoRing() != null) {
            param.setPhotoRing(nc.photoRing());
        }
        if (nc.alertInfo() != null) {
            param.setAlertInfo(nc.alertInfo());
        }
        if (nc.historyInfo() != null) {
            param.setHistoryInfo(nc.historyInfo());
        }
        if (nc.verstat() != null) {
            param.setVerstat(nc.verstat());
        }
        if (nc.cmcDeviceId() != null) {
            param.setCmcDeviceId(nc.cmcDeviceId());
        }
        if (nc.composerData() != null) {
            Log.i(LOG_TAG, "onNewIncomingCall: has composer data");
            ComposerData cd = nc.composerData();
            Bundle cBundle = new Bundle();
            if (!TextUtils.isEmpty(cd.image())) {
                cBundle.putString(CallConstants.ComposerData.IMAGE, cd.image());
            }
            if (!TextUtils.isEmpty(cd.subject())) {
                cBundle.putString("subject", cd.subject());
            }
            if (!TextUtils.isEmpty(cd.latitude())) {
                cBundle.putString(CallConstants.ComposerData.LATITUDE, cd.latitude());
            }
            if (!TextUtils.isEmpty(cd.longitude())) {
                cBundle.putString(CallConstants.ComposerData.LONGITUDE, cd.longitude());
            }
            if (!TextUtils.isEmpty(cd.radius())) {
                cBundle.putString(CallConstants.ComposerData.RADIUS, cd.radius());
            }
            cBundle.putBoolean(CallConstants.ComposerData.IMPORTANCE, cd.importance());
            param.setComposerData(cBundle);
        }
        param.setHasDiversion(nc.hasDiversion());
        call.mParam = param;
        addCall(call.mSessionId, call);
        StringBuilder sb = new StringBuilder();
        sb.append("onNewIncomingCall: sessionId ");
        sb.append(call.mSessionId);
        sb.append(" peer ");
        sb.append(IMSLog.checker(call.mPeer + ""));
        Log.i(LOG_TAG, sb.toString());
        IncomingCallEvent incomingCallEvent = new IncomingCallEvent(ua.getImsRegistration(), call.mSessionId, convertToCallTypeForward(call.mCallType), call.mPeer, true, false, call.mParam);
        Log.i(LOG_TAG, "notifyIncomingCall() pre Alerting session: " + call.mSessionId + ", callType: " + call.mCallType);
        this.mIncomingCallEventRegistrants.notifyResult(incomingCallEvent);
    }

    private int convertDedicatedBearerState(int state) {
        if (state == 1) {
            return 1;
        }
        if (state == 2) {
            return 2;
        }
        if (state != 3) {
            return 0;
        }
        return 3;
    }

    private void onDedicatedBearerEventReceived(AsyncResult result) {
        Log.i(LOG_TAG, "onDedicatedBearerEventReceived:");
        DedicatedBearerEvent notiInfo = (DedicatedBearerEvent) result.result;
        this.mDedicatedBearerEventRegistrants.notifyResult(new com.sec.internal.ims.servicemodules.volte2.data.DedicatedBearerEvent(convertDedicatedBearerState(notiInfo.bearerState()), (int) notiInfo.qci(), (int) notiInfo.session()));
    }

    private void onRtpLossRateNoti(AsyncResult result) {
        Log.i(LOG_TAG, "onRtpLossRateNoti:");
        RtpLossRateNoti noti = (RtpLossRateNoti) result.result;
        this.mRtpLossRateNotiRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti((int) noti.interval(), (float) ((int) noti.lossrate()), noti.jitter(), (int) noti.notification()));
    }

    private void onRrcConnectionEventReceived(AsyncResult result) {
        Log.i(LOG_TAG, "onRrcConnectionEventReceived:");
        RrcConnectionEvent rrcEvent = (RrcConnectionEvent) result.result;
        if (rrcEvent.event() == 1) {
            this.mRrcConnectionEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent(RrcConnectionEvent.RrcEvent.REJECTED));
        } else if (rrcEvent.event() == 2) {
            this.mRrcConnectionEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent(RrcConnectionEvent.RrcEvent.TIMER_EXPIRED));
        }
    }

    private int getParticipantStatus(int status) {
        if (status == 1) {
            return 1;
        }
        if (status == 2) {
            return 2;
        }
        if (status == 3) {
            return 3;
        }
        if (status == 4) {
            return 4;
        }
        if (status == 5) {
            return 5;
        }
        if (status == 6) {
            return 6;
        }
        return 0;
    }

    private void onConferenceUpdate(AsyncResult result) {
        int phoneId;
        CallStateEvent.CALL_STATE state;
        ConfCallChanged cc = (ConfCallChanged) result.result;
        Log.i(LOG_TAG, "onConferenceUpdate: session " + cc.session() + " event " + cc.event());
        Call call = getCallBySession((int) cc.session());
        if (call == null || call.mUa == null) {
            phoneId = 0;
        } else {
            phoneId = call.mUa.getPhoneId();
        }
        if (SimUtil.getSimMno(phoneId) == Mno.SKT) {
            synchronized (this.mCallList) {
                int i = 0;
                while (true) {
                    if (i < this.mCallList.size()) {
                        Call tempCall = this.mCallList.get(this.mCallList.keyAt(i));
                        if (tempCall != null) {
                            Log.i(LOG_TAG, "tempCall.mCallType :  " + tempCall.mCallType);
                        }
                        if (tempCall != null && tempCall.mCallType == 6) {
                            Log.i(LOG_TAG, "Find confcall!!");
                            call = tempCall;
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
        }
        if (call == null) {
            Log.i(LOG_TAG, "onConferenceUpdate: session not found.");
            return;
        }
        int sessionId = call.mSessionId;
        int event = cc.event();
        CallStateEvent callStateEvent = new CallStateEvent();
        callStateEvent.setCallType(convertToCallTypeBackward(call.mCallType));
        callStateEvent.setSessionID(sessionId);
        callStateEvent.setParams(call.mParam);
        callStateEvent.setConference(call.isConference);
        if (event == 0) {
            state = CallStateEvent.CALL_STATE.CONFERENCE_ADDED;
            List<Participant> added = new ArrayList<>();
            for (int i2 = 0; i2 < cc.participantsLength(); i2++) {
                added.add(cc.participants(i2));
            }
            for (Participant p : added) {
                Log.i(LOG_TAG, "onConferenceUpdate: " + IMSLog.checker(p.uri()) + " added.  partid " + p.participantid());
                int participantState = getParticipantStatus(p.status());
                int participantSessionId = (int) p.sessionId();
                CallStateEvent.CALL_STATE state2 = state;
                callStateEvent.addUpdatedParticipantsList(p.uri(), (int) p.participantid(), participantSessionId, participantState);
                if (participantState == 2) {
                    Log.i(LOG_TAG, "Session (" + participantSessionId + ") join to conference");
                    CallStateEvent participantStateEvent = new CallStateEvent(CallStateEvent.CALL_STATE.CONFERENCE_ADDED);
                    participantStateEvent.setSessionID(participantSessionId);
                    this.mCallStateEventRegistrants.notifyResult(participantStateEvent);
                }
                state = state2;
            }
        } else if (event == 1) {
            state = CallStateEvent.CALL_STATE.CONFERENCE_REMOVED;
            List<Participant> removed = new ArrayList<>();
            for (int i3 = 0; i3 < cc.participantsLength(); i3++) {
                removed.add(cc.participants(i3));
            }
            for (Participant p2 : removed) {
                Log.i(LOG_TAG, "onConferenceUpdate: " + IMSLog.checker(p2.uri()) + " removed.");
                callStateEvent.addUpdatedParticipantsList(p2.uri(), (int) p2.participantid(), (int) p2.sessionId(), getParticipantStatus(p2.status()));
            }
        } else if (event == 2) {
            Log.i(LOG_TAG, "onConferenceUpdate: CONF_PARTICIPANT_UPDATED");
            state = CallStateEvent.CALL_STATE.CONFERENCE_PARTICIPANTS_UPDATED;
            List<Participant> updated = new ArrayList<>();
            for (int i4 = 0; i4 < cc.participantsLength(); i4++) {
                updated.add(cc.participants(i4));
            }
            for (Participant p3 : updated) {
                callStateEvent.setPeerAddr(new NameAddr("", ImsUri.parse(p3.uri())));
                callStateEvent.addUpdatedParticipantsList(p3.uri(), (int) p3.participantid(), (int) p3.sessionId(), getParticipantStatus(p3.status()));
                Log.i(LOG_TAG, "onConferenceUpdate: " + IMSLog.checker(p3.uri()) + " update id . " + p3.participantid());
            }
        } else {
            Log.i(LOG_TAG, "onConferenceUpdate: unknown event. ignore.");
            return;
        }
        callStateEvent.setState(state);
        this.mCallStateEventRegistrants.notifyResult(callStateEvent);
    }

    private void onReferReceived(AsyncResult result) {
        ReferReceived rr = (ReferReceived) result.result;
        UserAgent ua = getUa((int) rr.handle());
        if (ua == null) {
            Log.e(LOG_TAG, "onReferReceived: unknown handle " + rr.handle());
            return;
        }
        ua.acceptCallTranfer((int) rr.session(), true, 0, (String) null);
    }

    private void onReferStatus(AsyncResult result) {
        ReferStatus rr = (ReferStatus) result.result;
        Log.i(LOG_TAG, "onReferStatus: session " + rr.session() + " respCode " + rr.statusCode());
        this.mReferStatusRegistrants.notifyResult(new com.sec.internal.ims.servicemodules.volte2.data.ReferStatus((int) rr.session(), (int) rr.statusCode()));
    }

    private void onDialogEventReceived(AsyncResult result) {
        DialogEvent deParsed;
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DialogEvent de = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DialogEvent) result.result;
        String contentType = "";
        String dialogXml = "";
        UserAgent ua = null;
        if (de != null) {
            AdditionalContents ac = de.additionalContents();
            if (!(ac == null || ac.mimeType() == null)) {
                contentType = ac.mimeType();
            }
            if (!(ac == null || ac.contents() == null)) {
                dialogXml = ac.contents();
            }
            ua = getUa((int) de.handle());
        }
        if (ua == null) {
            Log.e(LOG_TAG, "ignore dialog event UA is null");
            return;
        }
        ImsRegistration reg = ua.getImsRegistration();
        if (reg == null) {
            Log.e(LOG_TAG, "ignore dialog event without registration");
            return;
        }
        Log.i(LOG_TAG, "onDialogEventReceived: has AdditionalContents of type " + contentType + " (" + dialogXml.length() + " bytes)");
        if (!contentType.equals(DIALOG_EVENT_MIME_TYPE)) {
            Log.e(LOG_TAG, "onDialogEventReceived: contentType mismatch!");
            return;
        }
        try {
            if (!(ua.getImsProfile().getCmcType() == 2 || ua.getImsProfile().getCmcType() == 4)) {
                if (ua.getImsProfile().getCmcType() != 8) {
                    deParsed = DialogXmlParser.getInstance().parseDialogInfoXml(dialogXml);
                    deParsed.setRegId(reg.getHandle());
                    deParsed.setPhoneId(reg.getPhoneId());
                    this.mDialogEventRegistrants.notifyResult(deParsed);
                }
            }
            deParsed = DialogXmlParser.getInstance().parseDialogInfoXml(dialogXml, ua.getImsProfile().getCmcType());
            deParsed.setRegId(reg.getHandle());
            deParsed.setPhoneId(reg.getPhoneId());
            this.mDialogEventRegistrants.notifyResult(deParsed);
        } catch (XPathExpressionException e) {
            Log.e(LOG_TAG, "failed to parse dialog xml!", e);
        }
    }

    private void onCdpnInfoReceived(AsyncResult result) {
        this.mCdpnInfoRegistrants.notifyResult((String) result.result);
    }

    private void onDtmfInfo(AsyncResult event) {
        DTMFDataEvent dtmfEvent = (DTMFDataEvent) event.result;
        this.mDtmfEventRegistrants.notifyResult(new DtmfInfo((int) dtmfEvent.event(), (int) dtmfEvent.volume(), (int) dtmfEvent.duration(), (int) dtmfEvent.endbit()));
    }

    private void onTextInfo(AsyncResult event) {
        TextDataEvent textEvent = (TextDataEvent) event.result;
        this.mTextEventRegistrants.notifyResult(new TextInfo(0, textEvent.text(), (int) textEvent.len()));
    }

    private void onCmcInfoReceived(AsyncResult event) {
        CallSendCmcInfo callSendCmcInfo = (CallSendCmcInfo) event.result;
        String contentType = "";
        String cmcInfoXml = "";
        UserAgent ua = null;
        if (callSendCmcInfo != null) {
            AdditionalContents ac = callSendCmcInfo.additionalContents();
            if (!(ac == null || ac.mimeType() == null)) {
                contentType = ac.mimeType();
            }
            if (!(ac == null || ac.contents() == null)) {
                cmcInfoXml = ac.contents();
            }
            ua = getUa((int) callSendCmcInfo.handle());
        }
        if (ua == null) {
            Log.e(LOG_TAG, "ignore CmcInfo event UA is null");
        } else if (ua.getImsRegistration() == null) {
            Log.e(LOG_TAG, "ignore CmcInfo event without registration");
        } else {
            Log.i(LOG_TAG, "onCmcInfoReceived: has AdditionalContents of type " + contentType + " (" + cmcInfoXml.length() + " bytes)");
            if (!contentType.equals(CMC_INFO_MIME_TYPE)) {
                Log.e(LOG_TAG, "onCmcInfoReceived: contentType mismatch!");
                return;
            }
            try {
                if (ua.getImsProfile().getCmcType() == 2) {
                    this.mCmcInfoEventRegistrants.notifyResult(CmcInfoXmlParser.parseXml(cmcInfoXml));
                }
            } catch (XPathExpressionException e) {
                Log.e(LOG_TAG, "failed to parse cmc info xml!", e);
            }
        }
    }

    private CallStateEvent.CALL_STATE convertToVolteState(int state, int statusCode) {
        if (state == 1) {
            return CallStateEvent.CALL_STATE.TRYING;
        }
        if (state == 2) {
            return CallStateEvent.CALL_STATE.CALLING;
        }
        if (state == 4) {
            return CallStateEvent.CALL_STATE.RINGING_BACK;
        }
        if (state == 5) {
            return CallStateEvent.CALL_STATE.FORWARDED;
        }
        if (state == 18) {
            return CallStateEvent.CALL_STATE.EXTEND_TO_CONFERENCE;
        }
        switch (state) {
            case 8:
                return CallStateEvent.CALL_STATE.ESTABLISHED;
            case 9:
                return CallStateEvent.CALL_STATE.HELD_LOCAL;
            case 10:
                return CallStateEvent.CALL_STATE.HELD_REMOTE;
            case 11:
                if (statusCode != 0) {
                    return CallStateEvent.CALL_STATE.ERROR;
                }
                return CallStateEvent.CALL_STATE.ENDED;
            case 12:
                return CallStateEvent.CALL_STATE.EARLY_MEDIA_START;
            case 13:
                return CallStateEvent.CALL_STATE.HELD_BOTH;
            case 14:
                if (statusCode == 0 || statusCode == 1122) {
                    return CallStateEvent.CALL_STATE.MODIFIED;
                }
                return CallStateEvent.CALL_STATE.ERROR;
            case 15:
                return CallStateEvent.CALL_STATE.SESSIONPROGRESS;
            case 16:
                return CallStateEvent.CALL_STATE.REFRESHFAIL;
            default:
                Log.e(LOG_TAG, "convertToVolteState: unknown Call state " + state);
                return null;
        }
    }

    private int convertToCallTypeForward(int callType) {
        switch (callType) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 13:
                return 1;
            case 14:
                return 14;
            case 15:
                return 15;
            case 18:
                return 18;
            case 19:
                return 19;
            default:
                Log.e(LOG_TAG, "convertToCallType:: unknown call type " + callType);
                return 1;
        }
    }

    private int convertToCallTypeBackward(int callType) {
        switch (callType) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 14:
                return 14;
            case 15:
                return 15;
            case 18:
                return 18;
            case 19:
                return 19;
            default:
                Log.e(LOG_TAG, "convertToCallType: unknown call type " + callType);
                return 1;
        }
    }

    private void addCall(int sessionId, Call call) {
        synchronized (this.mCallList) {
            Log.i(LOG_TAG, "Add Call " + sessionId);
            this.mCallList.append(sessionId, call);
        }
    }

    private void removeCall(int sessionId) {
        synchronized (this.mCallList) {
            Log.i(LOG_TAG, "Remove Call " + sessionId);
            this.mCallList.remove(sessionId);
        }
    }

    private Call getCallBySession(int sessionId) {
        synchronized (this.mCallList) {
            for (int i = 0; i < this.mCallList.size(); i++) {
                Call call = this.mCallList.get(this.mCallList.keyAt(i));
                if (call != null && call.mSessionId == sessionId) {
                    return call;
                }
            }
            return null;
        }
    }

    private Call getCall(int callType) {
        synchronized (this.mCallList) {
            for (int i = 0; i < this.mCallList.size(); i++) {
                Call call = this.mCallList.get(this.mCallList.keyAt(i));
                if (call != null && call.mCallType == callType) {
                    return call;
                }
            }
            return null;
        }
    }

    private void dumpCall() {
        synchronized (this.mCallList) {
            Log.i(LOG_TAG, "Call List Size : " + this.mCallList.size());
            for (int i = 0; i < this.mCallList.size(); i++) {
                Call call = this.mCallList.get(this.mCallList.keyAt(i));
                if (call != null) {
                    Log.i(LOG_TAG, "Session Id : " + call.mSessionId + " in the list");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0066, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0069, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean IsModifiableNeedToBeIgnored(com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE r7, com.sec.internal.constants.Mno r8) {
        /*
            r6 = this;
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r0 = r6.mCallList
            monitor-enter(r0)
            r1 = 0
            r2 = 0
        L_0x0005:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r3 = r6.mCallList     // Catch:{ all -> 0x006a }
            int r3 = r3.size()     // Catch:{ all -> 0x006a }
            if (r2 >= r3) goto L_0x0036
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r3 = r6.mCallList     // Catch:{ all -> 0x006a }
            int r3 = r3.keyAt(r2)     // Catch:{ all -> 0x006a }
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r4 = r6.mCallList     // Catch:{ all -> 0x006a }
            java.lang.Object r4 = r4.get(r3)     // Catch:{ all -> 0x006a }
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r4 = (com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call) r4     // Catch:{ all -> 0x006a }
            if (r4 == 0) goto L_0x0033
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = r4.mUa     // Catch:{ all -> 0x006a }
            com.sec.internal.ims.core.handler.secims.UaProfile r5 = r5.getUaProfile()     // Catch:{ all -> 0x006a }
            if (r5 == 0) goto L_0x0033
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = r4.mUa     // Catch:{ all -> 0x006a }
            com.sec.internal.ims.core.handler.secims.UaProfile r5 = r5.getUaProfile()     // Catch:{ all -> 0x006a }
            int r5 = r5.getCmcType()     // Catch:{ all -> 0x006a }
            if (r5 <= 0) goto L_0x0033
            int r1 = r1 + 1
        L_0x0033:
            int r2 = r2 + 1
            goto L_0x0005
        L_0x0036:
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_LOCAL     // Catch:{ all -> 0x006a }
            r3 = 1
            if (r7 == r2) goto L_0x004c
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE     // Catch:{ all -> 0x006a }
            if (r7 == r2) goto L_0x004c
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_BOTH     // Catch:{ all -> 0x006a }
            if (r7 == r2) goto L_0x004c
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r2 = r6.mCallList     // Catch:{ all -> 0x006a }
            int r2 = r2.size()     // Catch:{ all -> 0x006a }
            int r2 = r2 - r1
            if (r2 <= r3) goto L_0x0065
        L_0x004c:
            boolean r2 = r8.isChn()     // Catch:{ all -> 0x006a }
            if (r2 != 0) goto L_0x0068
            boolean r2 = r8.isHkMo()     // Catch:{ all -> 0x006a }
            if (r2 != 0) goto L_0x0068
            boolean r2 = r8.isKor()     // Catch:{ all -> 0x006a }
            if (r2 != 0) goto L_0x0068
            boolean r2 = r8.isJpn()     // Catch:{ all -> 0x006a }
            if (r2 == 0) goto L_0x0065
            goto L_0x0068
        L_0x0065:
            monitor-exit(r0)     // Catch:{ all -> 0x006a }
            r0 = 0
            return r0
        L_0x0068:
            monitor-exit(r0)     // Catch:{ all -> 0x006a }
            return r3
        L_0x006a:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x006a }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.IsModifiableNeedToBeIgnored(com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE, com.sec.internal.constants.Mno):boolean");
    }

    public void handleMessage(Message msg) {
        Log.i(LOG_TAG, "handleMessage: evt " + msg.what);
        int i = msg.what;
        if (i != 1) {
            if (i == 200) {
                this.mCallStateEventRegistrants.notifyResult(msg.obj);
                return;
            } else if (i != 3) {
                if (i == 4) {
                    onHoldResumeResponse((AsyncResult) msg.obj, true);
                    return;
                } else if (i == 5) {
                    onHoldResumeResponse((AsyncResult) msg.obj, false);
                    return;
                } else if (i != 6) {
                    if (i != 7) {
                        switch (i) {
                            case 100:
                                onCallStateChange((AsyncResult) msg.obj);
                                return;
                            case 101:
                                onNewIncomingCall((AsyncResult) msg.obj);
                                return;
                            case 102:
                                onConferenceUpdate((AsyncResult) msg.obj);
                                return;
                            case 103:
                                onReferReceived((AsyncResult) msg.obj);
                                return;
                            case 104:
                                onReferStatus((AsyncResult) msg.obj);
                                return;
                            case 105:
                                onDialogEventReceived((AsyncResult) msg.obj);
                                return;
                            case 106:
                                onModifyCall((AsyncResult) msg.obj);
                                return;
                            case 107:
                                onCdpnInfoReceived((AsyncResult) msg.obj);
                                return;
                            case 108:
                                onRtpLossRateNoti((AsyncResult) msg.obj);
                                return;
                            default:
                                switch (i) {
                                    case 110:
                                        onDedicatedBearerEventReceived((AsyncResult) msg.obj);
                                        return;
                                    case 111:
                                        onRrcConnectionEventReceived((AsyncResult) msg.obj);
                                        return;
                                    case 112:
                                        onDtmfInfo((AsyncResult) msg.obj);
                                        return;
                                    case 113:
                                        onTextInfo((AsyncResult) msg.obj);
                                        return;
                                    case 114:
                                        sendSIPMSGInfo((Notify) ((AsyncResult) msg.obj).result);
                                        return;
                                    case 115:
                                        onCmcInfoReceived((AsyncResult) msg.obj);
                                        return;
                                    default:
                                        return;
                                }
                        }
                    } else {
                        onInfoResponse((AsyncResult) msg.obj);
                        return;
                    }
                }
            }
        }
        onMakeCallResponse((AsyncResult) msg.obj);
    }

    private void sendSIPMSGInfo(Notify noti) {
        SipMessage sip = (SipMessage) noti.noti(new SipMessage());
        String message = sip.sipMessage();
        if (!TextUtils.isEmpty(message)) {
            boolean isRequest = false;
            if (sip.direction() == 0) {
                isRequest = true;
            }
            this.mSIPMSGNotiRegistrants.notifyResult(new SIPDataEvent(message, isRequest));
        }
    }

    private class AudioInterfaceHandler extends Handler {
        public AudioInterfaceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(ResipVolteHandler.LOG_TAG, "Event " + msg.what);
            if (msg.what != 8) {
                Log.e(ResipVolteHandler.LOG_TAG, "Invalid event");
            } else {
                ResipVolteHandler.this.onUpdateAudioInterfaceResponse((AsyncResult) msg.obj);
            }
        }
    }
}
