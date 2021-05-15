package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PublishResponse;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.PresenceHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ContactInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.NewPresenceInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.PresencePublishStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.PresenceSubscribeStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.PresenceServiceStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.SubscriptionFailureReason;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.SubscriptionState;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResipPresenceHandler extends PresenceHandler {
    public static final int EVENT_PRESENCE_NOTIFY = 103;
    public static final int EVENT_PRESENCE_PUBLISH_RESPONSE = 101;
    public static final int EVENT_PRESENCE_SUBSCRIBE_RESPONSE = 102;
    private static final String LOG_TAG = ResipPresenceHandler.class.getSimpleName();
    private HashMap<Message, String> mCallbackMessageToSubscriptionId;
    private final IImsFramework mImsFramework;
    private Mno mMno = Mno.DEFAULT;
    private Registrant mPresenceInfoRegistrant = null;
    protected final PhoneIdKeyMap<Integer> mPresenceServiceHandles;
    private Registrant mPublishResponseRegistrant = null;
    private HashMap<Message, Message> mRequestCallbackMessages;
    private StackIF mStackIf = null;
    protected HashMap<String, Message> mSubscriptionIdToCallbackMessage;

    public ResipPresenceHandler(Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
        this.mRequestCallbackMessages = new HashMap<>();
        this.mSubscriptionIdToCallbackMessage = new HashMap<>();
        this.mCallbackMessageToSubscriptionId = new HashMap<>();
        this.mPresenceServiceHandles = new PhoneIdKeyMap<>(SimManagerFactory.getAllSimManagers().size(), -1);
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerPresenceEvent(this, 103, (Object) null);
        this.mRequestCallbackMessages.clear();
    }

    public void publish(PresenceInfo presenceInfo, Message onComplete, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "presence publish:");
        UserAgent ua = getUa("presence", phoneId);
        if (ua == null) {
            IMSLog.e(LOG_TAG, phoneId, "publish: UserAgent not found.");
            return;
        }
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "presence publish: handle = " + ua.getHandle());
        this.mPresenceServiceHandles.put(phoneId, Integer.valueOf(ua.getHandle()));
        this.mMno = SimUtil.getSimMno(phoneId);
        Message message = obtainMessage(101);
        this.mRequestCallbackMessages.put(message, onComplete);
        ua.requestPublish(presenceInfo, message);
    }

    private UserAgent getUa(String service, int phoneId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(service, phoneId);
    }

    private UserAgent getUa(int handle) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(handle);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 101:
                Log.i(LOG_TAG, "handleMessage(), EVENT_PRESENCE_PUBLISH_RESPONSE.");
                handlePublishResponse(msg);
                return;
            case 102:
                Log.i(LOG_TAG, "handleMessage(), EVENT_PRESENCE_SUBSCRIBE_RESPONSE.");
                handleSubscribeResponse(msg);
                return;
            case 103:
                Log.i(LOG_TAG, "handleMessage(), EVENT_PRESENCE_NOTIFY.");
                handleNotify(msg);
                return;
            default:
                return;
        }
    }

    private void handlePublishResponse(Message msg) {
        PresenceResponse pr = getPresenceResponse(msg);
        String str = LOG_TAG;
        Log.i(str, "handlePublishResponse() isSuccess = " + pr.isSuccess());
        if (!pr.isSuccess()) {
            Log.i(LOG_TAG, "handlePublishResponse(): ");
            callbackPresenceResponse(msg, pr);
            return;
        }
        this.mRequestCallbackMessages.remove(msg);
    }

    private void handleSubscribeResponse(Message msg) {
        PresenceResponse pr = getPresenceResponse(msg);
        String str = LOG_TAG;
        Log.i(str, "handleSubscribeResponse() isSuccess = " + pr.isSuccess());
        Message callback = this.mRequestCallbackMessages.get(msg);
        if (!pr.isSuccess()) {
            Log.i(LOG_TAG, "handleSubscribeResponse(): ");
            this.mSubscriptionIdToCallbackMessage.remove(this.mCallbackMessageToSubscriptionId.get(callback));
            this.mCallbackMessageToSubscriptionId.remove(callback);
            callbackPresenceResponse(msg, pr);
            return;
        }
        this.mCallbackMessageToSubscriptionId.remove(callback);
        this.mRequestCallbackMessages.remove(msg);
    }

    private void callbackPresenceResponse(Message msg, PresenceResponse pr) {
        Message onComplete = this.mRequestCallbackMessages.get(msg);
        this.mRequestCallbackMessages.remove(msg);
        if (onComplete != null) {
            Log.i(LOG_TAG, "callbackPresenceResponse() : callback found");
            AsyncResult.forMessage(onComplete, pr, (Throwable) null);
            onComplete.sendToTarget();
            return;
        }
        Log.i(LOG_TAG, "callbackPresenceResponse() : cannot find callback");
    }

    private PresenceResponse getPresenceResponse(Message msg) {
        GeneralResponse gr = (GeneralResponse) ((AsyncResult) msg.obj).result;
        UserAgent userAgent = getUa((int) gr.handle());
        int phoneId = SimUtil.getDefaultPhoneId();
        if (userAgent != null) {
            phoneId = userAgent.getPhoneId();
        }
        return new PresenceResponse(gr.result() == 0, (int) gr.sipError(), gr.errorStr(), 0, phoneId);
    }

    private void handleNotify(Message msg) {
        Notify notify = (Notify) ((AsyncResult) msg.obj).result;
        switch (notify.notifyid()) {
            case Id.NOTIFY_PRESENCE_SUBSCRIBE /*13001*/:
                Log.i(LOG_TAG, "handleNotify(), EVENT_PRESENCE_SUBSCRIBE.");
                handleNewPresenceInfo(notify);
                return;
            case Id.NOTIFY_PRESENCE_PUBLISH_STATUS /*13002*/:
                Log.i(LOG_TAG, "handleNotify(), EVENT_PUBLISH_STATUS.");
                handlePublishStatusUpdate(notify);
                return;
            case Id.NOTIFY_PRESENCE_UNPUBLISH_STATUS /*13003*/:
                Log.i(LOG_TAG, "handleNotify(), NOTIFY_PRESENCE_UNPUBLISH_STATUS, just ignore...");
                return;
            case Id.NOTIFY_PRESENCE_SUBSCRIBE_STATUS /*13004*/:
                Log.i(LOG_TAG, "handleNotify(), EVENT_SUBSCRIBE_STATUS.");
                handleSubscribeStatusUpdate(notify);
                return;
            default:
                Log.w(LOG_TAG, "handleNotify(): unexpected id");
                return;
        }
    }

    private void handlePublishStatusUpdate(Notify notify) {
        PresencePublishStatus publishStatus = (PresencePublishStatus) notify.noti(new PresencePublishStatus());
        UserAgent userAgent = getUa((int) publishStatus.handle());
        int phoneId = SimUtil.getDefaultPhoneId();
        if (userAgent != null) {
            phoneId = userAgent.getPhoneId();
        }
        PublishResponse pr = new PublishResponse(publishStatus.isSuccess(), (int) publishStatus.sipErrorCode(), publishStatus.sipErrorPhrase(), (int) publishStatus.minExpires(), publishStatus.etag(), publishStatus.remoteExpires(), publishStatus.isRefresh(), publishStatus.retryAfter(), phoneId);
        String str = LOG_TAG;
        Log.i(str, "handlePublishStatusUpdate: " + pr);
        this.mPublishResponseRegistrant.notifyResult(pr);
    }

    private void handleSubscribeStatusUpdate(Notify notify) {
        PresenceSubscribeStatus subscribeStatus = (PresenceSubscribeStatus) notify.noti(new PresenceSubscribeStatus());
        UserAgent userAgent = getUa((int) subscribeStatus.handle());
        int phoneId = SimUtil.getDefaultPhoneId();
        if (userAgent != null) {
            phoneId = userAgent.getPhoneId();
        }
        PresenceResponse pr = new PresenceResponse(subscribeStatus.isSuccess(), (int) subscribeStatus.sipErrorCode(), subscribeStatus.sipErrorPhrase(), (int) subscribeStatus.minExpires(), phoneId);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "handleSubscribeStatusUpdate: " + pr);
        String subscriptionId = subscribeStatus.subscriptionId();
        Message onComplete = this.mSubscriptionIdToCallbackMessage.get(subscriptionId);
        this.mSubscriptionIdToCallbackMessage.remove(subscriptionId);
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, pr, (Throwable) null);
            onComplete.sendToTarget();
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "handleSubscribeStatusUpdate: no call back");
    }

    private void handleNewPresenceInfo(Notify notify) {
        NewPresenceInfo newPresenceInfo;
        NewPresenceInfo newPresenceInfo2;
        NewPresenceInfo newPresenceInfo3 = (NewPresenceInfo) notify.noti(new NewPresenceInfo());
        UserAgent userAgent = getUa((int) newPresenceInfo3.handle());
        int phoneId = SimUtil.getDefaultPhoneId();
        if (userAgent != null) {
            phoneId = userAgent.getPhoneId();
        }
        int contactInfoListSize = newPresenceInfo3.contactInfoLength();
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "handleNewPresenceInfo(): subscriptionID = " + newPresenceInfo3.subscriptionId());
        String str2 = LOG_TAG;
        IMSLog.i(str2, phoneId, "handleNewPresenceInfo(): contactinfo size = " + contactInfoListSize);
        int index = 0;
        while (index < contactInfoListSize) {
            ContactInfo contactInfo = newPresenceInfo3.contactInfo(index);
            if (contactInfo == null) {
                String str3 = LOG_TAG;
                IMSLog.i(str3, phoneId, "contact info is null for index: " + index);
                newPresenceInfo = newPresenceInfo3;
            } else {
                int serviceStatusListSize = contactInfo.serviceStatusLength();
                PresenceInfo presenceInfo = new PresenceInfo(phoneId);
                String uri = contactInfo.uri();
                String str4 = LOG_TAG;
                IMSLog.s(str4, "handleNewPresenceInfo(): entity uri = " + contactInfo.uri());
                presenceInfo.setUri(uri);
                presenceInfo.setTelUri(uri);
                presenceInfo.setPhoneId(phoneId);
                presenceInfo.setSubscriptionId(newPresenceInfo3.subscriptionId());
                int statusListIndex = 0;
                while (statusListIndex < serviceStatusListSize) {
                    PresenceServiceStatus serviceStatus = contactInfo.serviceStatus(statusListIndex);
                    if (serviceStatus != null) {
                        int listSize = serviceStatus.mediaCapabilitiesLength();
                        String[] mediaCapabilities = new String[listSize];
                        for (int i = 0; i < listSize; i++) {
                            mediaCapabilities[i] = serviceStatus.mediaCapabilities(i);
                        }
                        newPresenceInfo2 = newPresenceInfo3;
                        ServiceTuple serviceTuple = ServiceTuple.getServiceTuple(serviceStatus.serviceId(), serviceStatus.version(), mediaCapabilities);
                        if (serviceTuple == null) {
                            ServiceTuple serviceTuple2 = serviceTuple;
                            String[] mediaCapabilities2 = mediaCapabilities;
                            serviceTuple = new ServiceTuple((long) Capabilities.FEATURE_OFFLINE_RCS_USER, serviceStatus.serviceId(), serviceStatus.version(), mediaCapabilities2);
                        } else {
                            ServiceTuple serviceTuple3 = serviceTuple;
                            String[] strArr = mediaCapabilities;
                        }
                        if (serviceStatus.status() == null || "".equals(serviceStatus.status())) {
                            IMSLog.i(LOG_TAG, phoneId, "handleNewPresenceInfo(): status is null");
                        } else {
                            serviceTuple.basicStatus = serviceStatus.status();
                        }
                        presenceInfo.addService(serviceTuple);
                        String str5 = LOG_TAG;
                        IMSLog.i(str5, phoneId, "handleNewPresenceInfo(): " + serviceTuple.toString());
                    } else {
                        newPresenceInfo2 = newPresenceInfo3;
                    }
                    statusListIndex++;
                    Notify notify2 = notify;
                    newPresenceInfo3 = newPresenceInfo2;
                }
                newPresenceInfo = newPresenceInfo3;
                if (contactInfo.rawPidf() != null) {
                    presenceInfo.setPidf(contactInfo.rawPidf());
                } else {
                    IMSLog.e(LOG_TAG, phoneId, "handleNewPresenceInfo(): empty pidf");
                }
                String str6 = LOG_TAG;
                IMSLog.i(str6, phoneId, "handleNewPresenceInfo: state - " + contactInfo.subscriptionState() + ", state reason - " + contactInfo.subscriptionFailureReason());
                try {
                    if (contactInfo.subscriptionState() != null && contactInfo.subscriptionState().toUpperCase().equals(SubscriptionState.name(4))) {
                        if (contactInfo.subscriptionFailureReason() != null) {
                            String subscriptionFailureReason = contactInfo.subscriptionFailureReason().toUpperCase();
                            if (subscriptionFailureReason.equals(SubscriptionFailureReason.name(6))) {
                                if (this.mMno == Mno.VZW) {
                                    presenceInfo.addService(new ServiceTuple((long) Capabilities.FEATURE_NOT_UPDATED, (String) null, (String) null));
                                } else {
                                    presenceInfo.addService(new ServiceTuple((long) Capabilities.FEATURE_NON_RCS_USER, (String) null, (String) null));
                                }
                            } else if (!subscriptionFailureReason.equals(SubscriptionFailureReason.name(3))) {
                                if (!subscriptionFailureReason.equals(SubscriptionFailureReason.name(1)) && !subscriptionFailureReason.equals(SubscriptionFailureReason.name(5))) {
                                    if (!subscriptionFailureReason.equals(SubscriptionFailureReason.name(2))) {
                                        String str7 = LOG_TAG;
                                        IMSLog.i(str7, phoneId, "handleNewPresenceInfo: state failure reason - " + contactInfo.subscriptionFailureReason());
                                    }
                                }
                                presenceInfo.setFetchState(false);
                            } else if (this.mMno == Mno.VZW) {
                                presenceInfo.addService(new ServiceTuple((long) Capabilities.FEATURE_NOT_UPDATED, (String) null, (String) null));
                            } else {
                                if (this.mMno != Mno.TMOUS) {
                                    if (this.mMno != Mno.BELL) {
                                        presenceInfo.setFetchState(false);
                                    }
                                }
                                presenceInfo.addService(new ServiceTuple((long) Capabilities.FEATURE_NON_RCS_USER, (String) null, (String) null));
                            }
                        } else {
                            IMSLog.i(LOG_TAG, phoneId, "handleNewPresenceInfo: no reason");
                            if (this.mMno.isKor()) {
                                presenceInfo.addService(new ServiceTuple((long) Capabilities.FEATURE_NOT_UPDATED, (String) null, (String) null));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    IMSLog.e(LOG_TAG, phoneId, "State or Reason is not understandable.");
                }
                this.mPresenceInfoRegistrant.notifyResult(presenceInfo);
            }
            index++;
            Notify notify3 = notify;
            newPresenceInfo3 = newPresenceInfo;
        }
    }

    public void registerForWatcherInfo(Handler h, int what, Object obj) {
    }

    public void registerForPresenceInfo(Handler h, int what, Object obj) {
        this.mPresenceInfoRegistrant = new Registrant(h, what, obj);
    }

    public void registerForPublishFailure(Handler h, int what, Object obj) {
        this.mPublishResponseRegistrant = new Registrant(h, what, obj);
    }

    public void subscribeList(List<ImsUri> uris, boolean isAnonymousFetch, Message onComplete, String subscriptionId, boolean isGzipEnabled, int expiry, int phoneId) {
        Message message = onComplete;
        String str = subscriptionId;
        int i = phoneId;
        UserAgent ua = getUa("presence", i);
        if (ua == null) {
            IMSLog.e(LOG_TAG, i, "subscribeList: UserAgent not found");
            return;
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, i, "subscribeList: subscription id =" + str);
        Message message2 = obtainMessage(102);
        this.mRequestCallbackMessages.put(message2, message);
        this.mSubscriptionIdToCallbackMessage.put(str, message);
        this.mCallbackMessageToSubscriptionId.put(message, str);
        this.mStackIf.requestSubscribeList(ua.getHandle(), uris, isAnonymousFetch, subscriptionId, isGzipEnabled, expiry, message2);
    }

    public void subscribe(ImsUri uri, boolean isAnonymousFetch, Message onComplete, String subscriptionId, int phoneId) {
        UserAgent ua = getUa("presence", phoneId);
        if (ua == null) {
            IMSLog.e(LOG_TAG, phoneId, "subscribe: UserAgent not found.");
            return;
        }
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "subscribe: subscription id =" + subscriptionId);
        Message message = obtainMessage(102);
        this.mRequestCallbackMessages.put(message, onComplete);
        this.mSubscriptionIdToCallbackMessage.put(subscriptionId, onComplete);
        this.mCallbackMessageToSubscriptionId.put(onComplete, subscriptionId);
        this.mStackIf.requestSubscribe(ua.getHandle(), uri, isAnonymousFetch, subscriptionId, message);
    }

    public void unpublish(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "presence unpublish:");
        UserAgent ua = getUa(this.mPresenceServiceHandles.get(phoneId).intValue());
        if (ua == null) {
            IMSLog.e(LOG_TAG, phoneId, "unpublish: UserAgent not found. UserAgent already was de-registerd");
            this.mPresenceServiceHandles.put(phoneId, -1);
            return;
        }
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "presence unpublish: handle = " + ua.getHandle());
        ua.requestUnpublish();
    }

    public void updateServiceVersion(int phoneId, HashMap<String, String> svMap) {
        Log.i(LOG_TAG, "presence updateServiceVersion:");
        for (Map.Entry<String, String> entry : svMap.entrySet()) {
            Log.i(LOG_TAG + "[" + phoneId + "]", entry.getKey() + " : " + entry.getValue());
        }
        this.mStackIf.updateServiceVersion(phoneId, svMap);
    }
}
