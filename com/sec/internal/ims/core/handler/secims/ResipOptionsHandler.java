package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.ims.core.handler.OptionsHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.OptionsReceivedInfo;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResipOptionsHandler extends OptionsHandler {
    public static final int EVENT_OPTIONS_RECEIVED_NOTIFY = 102;
    public static final int EVENT_OPTIONS_REQ_RESPONSE = 101;
    private static final String LOG_TAG = "ResipOptionsHandler";
    static Map<Long, Integer> mFeatureMap;
    private Registrant mCmcRegistrant = null;
    private final IImsFramework mImsFramework;
    private Registrant mP2pRegistrant = null;
    private Registrant mRegistrant = null;
    private StackIF mStackIf;

    static {
        HashMap hashMap = new HashMap();
        mFeatureMap = hashMap;
        hashMap.put(Long.valueOf((long) Capabilities.FEATURE_CHAT_CPM), 20);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_CHAT_SIMPLE_IM), 20);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_STANDALONE_MSG), 10);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_SF_GROUP_CHAT), 21);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_INTEGRATED_MSG), 27);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_PRESENCE_DISCOVERY), 19);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_SOCIAL_PRESENCE), 28);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT), 22);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_HTTP), 25);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_STORE), 24);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_THUMBNAIL), 23);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_GEOLOCATION_PUSH), 31);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_MMTEL), 9);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_MMTEL_VIDEO), 6);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_MMTEL_CALL_COMPOSER), 53);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_IPCALL), 0);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_IPCALL_VIDEO), 1);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_IPCALL_VIDEO_ONLY), 2);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_ISH), 18);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_VSH), 3);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_VIA_SMS), 38);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_STICKER), 37);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_GEO_VIA_SMS), 39);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_CALL_COMPOSER), 14);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_SHARED_MAP), 15);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_SHARED_SKETCH), 17);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_POST_CALL), 16);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_PUBLIC_MSG), 42);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_LAST_SEEN_ACTIVE), 43);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_CHAT_SESSION), 44);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_STANDALONE_MSG), 45);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG), 46);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_ROLE), 51);
    }

    public void registerForOptionsEvent(Handler h, int what, Object obj) {
        this.mRegistrant = new Registrant(h, what, obj);
    }

    public void registerForCmcOptionsEvent(Handler h, int what, Object obj) {
        this.mCmcRegistrant = new Registrant(h, what, obj);
    }

    public void registerForP2pOptionsEvent(Handler h, int what, Object obj) {
        this.mP2pRegistrant = new Registrant(h, what, obj);
    }

    private void notifyEvent(OptionsEvent evt) {
        Registrant registrant = this.mRegistrant;
        if (registrant != null) {
            registrant.notifyResult(evt);
        }
    }

    private void notifyCmcEvent(OptionsEvent evt) {
        Registrant registrant = this.mCmcRegistrant;
        if (registrant != null) {
            registrant.notifyResult(evt);
        }
    }

    private void notifyP2pEvent(OptionsEvent evt) {
        Registrant registrant = this.mP2pRegistrant;
        if (registrant != null) {
            registrant.notifyResult(evt);
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 101) {
            Log.i(LOG_TAG, "SessionHandler():EVENT_OPTIONS_REQ_RESPONSE received.");
        } else if (i != 102) {
            Log.i(LOG_TAG, "SessionHandler():Cannot understand the event.");
        } else {
            handleNotify((Notify) ((AsyncResult) msg.obj).result);
            Log.i(LOG_TAG, "SessionHandler():EVENT_OPTIONS_RECEIVED_NOTIFY received.");
        }
    }

    public ResipOptionsHandler(Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerOptionsHandler(this, 102, (Object) null);
    }

    public void setOwnCapabilities(long features, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "setOwnCapabilities: " + features);
        UserAgent ua = getUa("options", phoneId);
        if (ua == null) {
            Log.e(LOG_TAG, "setOwnCapabilities: UserAgent not found.");
            return;
        }
        Log.i(LOG_TAG, "setOwnCapabilities: handle = " + ua.getHandle());
        this.mStackIf.requestUpdateFeatureTag(ua.getHandle(), features);
    }

    public void requestCapabilityExchange(ImsUri uri, long myFeatures, int phoneId, String extFeature) {
        IMSLog.s(LOG_TAG, phoneId, "requestCapabilityExchange: uri " + uri + Constants.IARI_ELT + extFeature);
        UserAgent ua = getUa("options", phoneId);
        if (ua == null) {
            Log.e(LOG_TAG, "requestCapabilityExchange: UserAgent not found.");
            return;
        }
        Log.i(LOG_TAG, "requestCapabilityExchange: handle = " + ua.getHandle());
        this.mStackIf.requestOptionsReqCapabilityExchange(ua.getHandle(), uri.toString(), myFeatures, extFeature);
    }

    public void requestSendCmcCheckMsg(int phoneId, int regId, String uriStr) {
        IMSLog.s(LOG_TAG, phoneId, "requestSendCmcCheckMsg: regId: " + regId + ",uri: " + uriStr);
        UserAgent ua = getUaByRegId(regId);
        if (ua == null) {
            Log.e(LOG_TAG, "requestSendCmcCheckMsg: UserAgent not found.");
            return;
        }
        Log.i(LOG_TAG, "requestSendCmcCheckMsg: handle = " + ua.getHandle());
        this.mStackIf.requestOptionsReqSendCmcCheckMsg(ua.getHandle(), uriStr);
    }

    public void sendCapexResponse(ImsUri uri, long myFeatures, String txId, int lastSeen, Message result, int phoneId, String extFeature) {
        IMSLog.s(LOG_TAG, "sendCapexResponse: uri " + uri);
        UserAgent ua = getUa("options", phoneId);
        if (ua == null) {
            Log.e(LOG_TAG, "sendCapexResponse: UserAgent not found.");
            return;
        }
        Log.i(LOG_TAG, "sendCapexResponse: handle = " + ua.getHandle());
        this.mStackIf.sendCapexResponse(ua.getHandle(), uri.toString(), myFeatures, txId, lastSeen, result, extFeature);
    }

    public int updateCmcExtCallCount(int phoneId, int callCnt) {
        Log.i(LOG_TAG, "updateCmcExtCallCount: phoneId= " + phoneId + ", callCnt= " + callCnt);
        this.mStackIf.updateCmcExtCallCount(phoneId, callCnt);
        return 0;
    }

    private UserAgent getUa(String service, int phoneId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(service, phoneId);
    }

    private UserAgent getUaByRegId(int regId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByRegId(regId);
    }

    private void handleNotify(Notify notify) {
        if (notify.notifyid() != 15001) {
            Log.w(LOG_TAG, "handleNotify(): unexpected id");
            return;
        }
        Log.i(LOG_TAG, "handleNotify(), NOTIFY_OPTIONS_RECEIVED.");
        handleOptionsReceived(notify);
    }

    private void handleOptionsReceived(Notify notify) {
        long FeatureToCaps;
        int phoneId;
        String extFeature;
        OptionsEvent.OptionsFailureReason reason;
        int sessionId;
        String txId;
        Log.i(LOG_TAG, "handleOptionsReceived()");
        if (notify.notiType() != 61) {
            Log.e(LOG_TAG, "Invalid notify");
            return;
        }
        int index = 0;
        OptionsReceivedInfo optionsReceivedInfo = (OptionsReceivedInfo) notify.noti(new OptionsReceivedInfo());
        String remoteUri = optionsReceivedInfo.remoteUri();
        boolean isResponse = optionsReceivedInfo.isResponse();
        boolean success = optionsReceivedInfo.success();
        int error = optionsReceivedInfo.reason();
        int sessionId2 = (int) optionsReceivedInfo.sessionId();
        String txId2 = optionsReceivedInfo.txId();
        String extFeature2 = optionsReceivedInfo.extFeature();
        boolean isChatbotParticipant = optionsReceivedInfo.isChatbotParticipant();
        boolean isCmcCheck = optionsReceivedInfo.isCmcCheck();
        Log.i(LOG_TAG, "handleOptionsReceived: isResponse: " + isResponse + "success: " + success + "txId: " + txId2 + "extfeature: " + extFeature2 + ", isCmcCheck: " + isCmcCheck);
        int tagLength = optionsReceivedInfo.tagsLength();
        StringBuilder sb = new StringBuilder();
        sb.append("handleOptionsReceived: tagLength ");
        sb.append(tagLength);
        Log.i(LOG_TAG, sb.toString());
        if (tagLength != 0) {
            index = 0;
            FeatureToCaps = 0;
            while (index < tagLength) {
                int feature = optionsReceivedInfo.tags(index);
                for (Map.Entry<Long, Integer> entry : mFeatureMap.entrySet()) {
                    int tagLength2 = tagLength;
                    if (entry.getValue().equals(Integer.valueOf(feature))) {
                        FeatureToCaps |= entry.getKey().longValue();
                        Log.i(LOG_TAG, "handleOptionsReceived: key = " + entry.getKey());
                    }
                    Notify notify2 = notify;
                    tagLength = tagLength2;
                }
                index++;
                Notify notify3 = notify;
            }
            Log.i(LOG_TAG, "handleOptionsReceived: received tags " + FeatureToCaps);
        } else {
            FeatureToCaps = 0;
        }
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        if (rm.getUserAgent(sessionId2) != null) {
            phoneId = ((UserAgent) rm.getUserAgent(sessionId2)).getPhoneId();
        } else {
            Log.i(LOG_TAG, "handleOptionsReceived: uaHandle is invalid ");
            phoneId = 0;
        }
        IRegistrationManager iRegistrationManager = rm;
        StringBuilder sb2 = new StringBuilder();
        int i = index;
        sb2.append("handleOptionsReceived(), sessionId = ");
        sb2.append(sessionId2);
        IMSLog.i(LOG_TAG, phoneId, sb2.toString());
        UriGenerator generator = UriGeneratorFactory.getInstance().get(phoneId);
        if (generator == null) {
            IMSLog.e(LOG_TAG, phoneId, "UriGenerator is null. IMS URIs won't be normalized!");
        }
        ImsUri uri = ImsUri.parse(remoteUri);
        if (generator != null) {
            uri = generator.normalize(uri);
        }
        List<String> assertedIdStrings = new ArrayList<>();
        String str = remoteUri;
        int i2 = 0;
        while (true) {
            extFeature = extFeature2;
            if (i2 >= optionsReceivedInfo.pAssertedIdLength()) {
                break;
            }
            assertedIdStrings.add(optionsReceivedInfo.pAssertedId(i2));
            i2++;
            extFeature2 = extFeature;
        }
        List<String> assertedIdStrings2 = assertedIdStrings;
        boolean isCmcCheck2 = isCmcCheck;
        Set<ImsUri> pAssertedIdSet = new HashSet<>(2, 1.0f);
        ImsUri parsedUri = assertedIdStrings2.iterator();
        boolean isTokenUsed = false;
        while (parsedUri.hasNext()) {
            List<String> assertedIdStrings3 = assertedIdStrings2;
            String assertedId = parsedUri.next();
            ImsUri imsUri = parsedUri;
            ImsUri parsedUri2 = ImsUri.parse(assertedId);
            if (parsedUri2 != null) {
                if (isTokenUsed || !isChatbotParticipant) {
                    txId = txId2;
                    sessionId = sessionId2;
                } else {
                    txId = txId2;
                    if (parsedUri2.getParam("tk") != null) {
                        sessionId = sessionId2;
                        if (parsedUri2.getParam("tk").equals("on")) {
                            isTokenUsed = true;
                        }
                    } else {
                        sessionId = sessionId2;
                    }
                }
                if (generator != null) {
                    parsedUri2 = generator.normalize(parsedUri2);
                }
                IMSLog.s(LOG_TAG, phoneId, "adding " + parsedUri2 + " to PAssertedIdSet");
                pAssertedIdSet.add(parsedUri2);
            } else {
                txId = txId2;
                sessionId = sessionId2;
                IMSLog.s(LOG_TAG, phoneId, "parsing P-Asserted-Identity " + assertedId + " returned null");
            }
            parsedUri = imsUri;
            txId2 = txId;
            sessionId2 = sessionId;
            assertedIdStrings2 = assertedIdStrings3;
        }
        List<String> list = assertedIdStrings2;
        String txId3 = txId2;
        String str2 = txId3;
        String extFeature3 = extFeature;
        UriGenerator uriGenerator = generator;
        boolean success2 = success;
        int error2 = error;
        OptionsEvent optionsEvent = new OptionsEvent(success, uri, FeatureToCaps, phoneId, isResponse, sessionId2, txId3, pAssertedIdSet, extFeature3);
        if (!success2) {
            if (error2 == 7) {
                reason = OptionsEvent.OptionsFailureReason.INVALID_DATA;
            } else if (error2 == 5) {
                reason = OptionsEvent.OptionsFailureReason.REQUEST_TIMED_OUT;
            } else if (error2 == 6) {
                reason = OptionsEvent.OptionsFailureReason.AUTOMATA_PRESENT;
            } else if (error2 == 1) {
                reason = OptionsEvent.OptionsFailureReason.USER_NOT_AVAILABLE;
            } else if (error2 == 2) {
                reason = OptionsEvent.OptionsFailureReason.DOES_NOT_EXIST_ANYWHERE;
            } else if (error2 == 4) {
                reason = OptionsEvent.OptionsFailureReason.USER_NOT_REACHABLE;
            } else if (error2 == 3) {
                reason = OptionsEvent.OptionsFailureReason.USER_NOT_REGISTERED;
            } else if (error2 == 8) {
                reason = OptionsEvent.OptionsFailureReason.FORBIDDEN_403;
            } else if (error2 == 0) {
                reason = OptionsEvent.OptionsFailureReason.USER_AVAILABLE_OFFLINE;
            } else {
                reason = OptionsEvent.OptionsFailureReason.ERROR;
            }
            Log.i(LOG_TAG, "handleOptionsReceived: received error " + reason);
        } else {
            reason = null;
        }
        optionsEvent.setReason(reason);
        optionsEvent.setIsTokenUsed(isTokenUsed);
        Log.i(LOG_TAG, "handleOptionsReceived: lastSeen " + IMSLog.checker(Integer.valueOf(optionsReceivedInfo.lastSeen())));
        if (optionsReceivedInfo.lastSeen() >= 0) {
            optionsEvent.setLastSeen(optionsReceivedInfo.lastSeen());
        }
        if (isCmcCheck2) {
            Log.i(LOG_TAG, "handleOptionsReceived: recevied OPTION response msg for CMC");
            List<String> list2 = list;
            notifyCmcEvent(optionsEvent);
            String str3 = extFeature3;
            return;
        }
        List<String> list3 = list;
        if ("d2d.push".equals(extFeature3)) {
            notifyP2pEvent(optionsEvent);
        } else {
            notifyEvent(optionsEvent);
        }
    }
}
