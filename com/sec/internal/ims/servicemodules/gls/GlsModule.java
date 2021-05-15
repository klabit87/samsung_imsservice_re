package com.sec.internal.ims.servicemodules.gls;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.CmccStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class GlsModule extends ServiceModuleBase implements IGlsModule {
    private static final int AUTO_ACCEPT_FT_GLS = 0;
    private static final int AUTO_SEND_FT_GLS = 1;
    private static final String LOG_TAG = GlsModule.class.getSimpleName();
    private final PhoneIdKeyMap<ImConfig> mConfigs;
    private final Context mContext;
    private final IImModule mImModule;
    private boolean[] mPushEnabled = {false, false};
    private final PhoneIdKeyMap<Integer> mRegistrationIds;
    private final GlsTranslation mTranslation;
    private int phoneCount = 0;

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            acceptLocationShare((long) msg.arg1);
        } else if (i != 1) {
            super.handleMessage(msg);
        } else {
            startLocationShareInCall((long) msg.arg1);
        }
    }

    public GlsModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        int size = SimManagerFactory.getAllSimManagers().size();
        this.phoneCount = size;
        this.mConfigs = new PhoneIdKeyMap<>(size, null);
        this.mRegistrationIds = new PhoneIdKeyMap<>(this.phoneCount, null);
        this.mImModule = getServiceModuleManager().getImModule();
        this.mTranslation = new GlsTranslation(this.mContext, this);
        for (int i = 0; i < this.phoneCount; i++) {
            this.mConfigs.put(i, this.mImModule.getImConfig(i));
        }
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        String str = LOG_TAG;
        Log.i(str, "onServiceSwitched: " + phoneId);
        updateFeatures(phoneId);
    }

    public void start() {
        super.start();
        Log.i(LOG_TAG, "start");
    }

    public void stop() {
        super.stop();
        Log.i(LOG_TAG, "stop");
    }

    public String[] getServicesRequiring() {
        return new String[]{"im", "gls"};
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        if (regiInfo != null && regiInfo.hasRcsService()) {
            String str = LOG_TAG;
            Log.i(str, "onRegistered() phoneId = " + regiInfo.getPhoneId() + ", services : " + regiInfo.getServices());
            this.mRegistrationIds.put(regiInfo.getPhoneId(), Integer.valueOf(getRegistrationInfoId(regiInfo)));
        }
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        Log.i(LOG_TAG, "onDeregistered");
        super.onDeregistered(regiInfo, errorCode);
        if (regiInfo != null && regiInfo.hasRcsService()) {
            this.mRegistrationIds.remove(regiInfo.getPhoneId());
        }
    }

    public void onConfigured(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "onConfigured : phoneId = " + phoneId);
        updateFeatures(phoneId);
    }

    public void handleIntent(Intent intent) {
        this.mTranslation.handleIntent(intent);
    }

    public void registerMessageEventListener(ImConstants.Type type, IMessageEventListener listener) {
        this.mImModule.registerMessageEventListener(type, listener);
    }

    public void registerFtEventListener(ImConstants.Type type, IFtEventListener listener) {
        this.mImModule.registerFtEventListener(type, listener);
    }

    public ImsRegistration getImsRegistration() {
        return getImsRegistration(SimUtil.getDefaultPhoneId());
    }

    public ImsRegistration getImsRegistration(int phoneId) {
        if (this.mRegistrationIds.get(phoneId) != null) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationIds.get(phoneId).intValue());
        }
        return null;
    }

    public Future<ImMessage> shareLocationInChat(String cid, Set<NotificationStatus> disposition, Location location, String label, String requestAppId, String locationLink, ImsUri contactUri, boolean isGroupChat, String maapTrafficType) {
        return shareLocationInChat(SimUtil.getDefaultPhoneId(), cid, disposition, location, label, requestAppId, locationLink, contactUri, isGroupChat, maapTrafficType);
    }

    public Future<ImMessage> shareLocationInChat(int phoneId, String cid, Set<NotificationStatus> disposition, Location location, String label, String requestAppId, String locationLink, ImsUri contactUri, boolean isGroupChat, String maapTrafficType) {
        int phoneId2;
        String str = cid;
        Location location2 = location;
        String str2 = label;
        Log.i(LOG_TAG, "shareLocationInChat()");
        int phoneId3 = phoneId;
        if (phoneId3 == -1) {
            phoneId2 = SimUtil.getDefaultPhoneId();
        } else {
            phoneId2 = phoneId3;
        }
        if (!isPushServiceAvailable(phoneId2)) {
            this.mTranslation.onShareLocationInChatResponse(str, requestAppId, -1, false);
            return null;
        }
        String str3 = requestAppId;
        boolean isImCapAlwaysOn = this.mConfigs.get(phoneId2).isImCapAlwaysOn();
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId2);
        if (!isGroupChat) {
            ICapabilityDiscoveryModule discoveryModule = getServiceModuleManager().getCapabilityDiscoveryModule();
            Capabilities capx = discoveryModule.getCapabilities(contactUri, CapabilityRefreshType.DISABLED, phoneId2);
            if (sm == null || sm.getSimMno() != Mno.CMCC) {
                Capabilities capx2 = capx;
                String str4 = str2;
                Location location3 = location2;
                int i = phoneId2;
                ICapabilityDiscoveryModule iCapabilityDiscoveryModule = discoveryModule;
                if ((capx2 != null && capx2.hasFeature(Capabilities.FEATURE_GEOLOCATION_PUSH)) || isImCapAlwaysOn) {
                    return this.mImModule.sendMessage(cid, generateXML(str, ImsUri.parse("sip:anonymous@anonymous.invalid"), location3, str4), disposition, MIMEContentType.LOCATION_PUSH, requestAppId, -1, false, false, true, (String) null, (String) null, (String) null, (List<ImsUri>) null, false, maapTrafficType, (String) null, (String) null, (String) null);
                } else if ((sm == null || sm.getSimMno() != Mno.TMOUS) && (capx2 == null || (!capx2.hasFeature(Capabilities.FEATURE_CHAT_CPM) && !capx2.hasFeature(Capabilities.FEATURE_CHAT_SIMPLE_IM)))) {
                    this.mTranslation.onReceiveShareLocationInChatResponse(cid, requestAppId, -1, false, (IMnoStrategy.StrategyResponse) null, RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()), (Result) null);
                    return null;
                } else {
                    return this.mImModule.sendMessage(cid, str4 + " " + locationLink, disposition, "text/plain", requestAppId, -1, false, false, true, (String) null, (String) null, (String) null, (List<ImsUri>) null, false, maapTrafficType, (String) null, (String) null, (String) null);
                }
            } else {
                Capabilities capabilities = capx;
                ICapabilityDiscoveryModule iCapabilityDiscoveryModule2 = discoveryModule;
                int i2 = phoneId2;
                String str5 = str2;
                Location location4 = location2;
                String str6 = str;
                return this.mImModule.sendMessage(cid, generateGeoSms(cid, ImsUri.parse("sip:anonymous@anonymous.invalid"), location, label, phoneId2), disposition, "text/plain", requestAppId, -1, false, false, true, (String) null, (String) null, (String) null, (List<ImsUri>) null, false, maapTrafficType, (String) null, (String) null, (String) null);
            }
        } else {
            String str7 = str2;
            Location location5 = location2;
            int phoneId4 = phoneId2;
            if (sm == null || sm.getSimMno() != Mno.CMCC) {
                ImsUri anonymousContactUri = ImsUri.parse("sip:anonymous@anonymous.invalid");
                String str8 = cid;
                return this.mImModule.sendMessage(cid, generateXML(str8, anonymousContactUri, location5, str7), disposition, MIMEContentType.LOCATION_PUSH, requestAppId, -1, false, false, true, (String) null, (String) null, (String) null, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null);
            }
            Location location6 = location5;
            String str9 = str7;
            Location location7 = location6;
            return this.mImModule.sendMessage(cid, generateGeoSms(cid, ImsUri.parse("sip:anonymous@anonymous.invalid"), location, label, phoneId4), disposition, "text/plain", requestAppId, -1, false, false, true, (String) null, (String) null, (String) null, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null);
        }
    }

    public Future<FtMessage> createInCallLocationShare(String chatId, ImsUri contactUri, Set<NotificationStatus> disposition, Location location, String label, String requestAppId, boolean isPublicAccountMsg, boolean isGroupChat) {
        Log.i(LOG_TAG, "createInCallLocationShare()");
        if (!isPushServiceAvailable()) {
            this.mTranslation.onCreateInCallLocationShareResponse((String) null, -1, requestAppId, false);
            return null;
        }
        String body = generateXML("0", contactUri, location, label);
        String filePath = save2FileSystem(body);
        String extinfo = new GlsXmlParser().getGlsExtInfo(body);
        if (filePath == null) {
            return null;
        }
        if (!isGroupChat) {
            String str = body;
            return this.mImModule.attachFileToSingleChat(SimUtil.getDefaultPhoneId(), filePath, contactUri, disposition, requestAppId, MIMEContentType.LOCATION_PUSH, isPublicAccountMsg, false, false, false, extinfo, FileDisposition.ATTACH);
        }
        return this.mImModule.attachFileToGroupChat(chatId, filePath, disposition, requestAppId, MIMEContentType.LOCATION_PUSH, false, false, false, false, extinfo, FileDisposition.ATTACH);
    }

    public void startLocationShareInCall(long sessionId) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onStartLocationShareInCallResponse(sessionId, false);
        } else {
            this.mImModule.sendFile(sessionId);
        }
    }

    public void acceptLocationShare(long sessionId) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onAcceptLocationShareInCallResponse(sessionId, false);
        } else {
            this.mImModule.acceptFileTransfer((int) sessionId);
        }
    }

    public void cancelLocationShare(long sessionId) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onCancelLocationShareInCallResponse(sessionId, false);
        } else {
            this.mImModule.cancelFileTransfer((int) sessionId);
        }
    }

    public void deleteGeolocSharings(List<String> msg) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onDeleteAllLocationShareResponse(false);
        } else {
            this.mImModule.deleteMessages(msg, false);
        }
    }

    public void rejectLocationShare(long sessionId) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onRejectLocationShareInCallResponse(sessionId, false);
        } else {
            this.mImModule.rejectFileTransfer((int) sessionId);
        }
    }

    private String save2FileSystem(String xmlData) {
        long curmils = System.currentTimeMillis();
        if (this.mContext.getExternalCacheDir() == null) {
            return null;
        }
        String path = this.mContext.getExternalCacheDir().getAbsolutePath() + "/gls" + curmils + ".xml";
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(new File(path));
            fo.write(xmlData.getBytes());
            try {
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            return null;
        } catch (IOException e4) {
            e4.printStackTrace();
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            return null;
        } catch (Throwable th) {
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            throw th;
        }
    }

    private GlsData makeGlsData(String id, ImsUri uri, Location loc, String label, LocationType type) {
        Date date = new Date();
        return new GlsData(id, uri, loc, type, date, label, new GlsValidityTime(date, date.getTimezoneOffset()));
    }

    private String generateXML(String id, ImsUri uri, Location loc, String label) {
        LocationType locationType;
        String label2 = label.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
        if (label2 == null) {
            locationType = LocationType.OWN_LOCATION;
        } else {
            locationType = LocationType.OTHER_LOCATION;
        }
        return new GlsXmlComposer().compose(makeGlsData(id, uri, loc, label2, locationType));
    }

    private String generateGeoSms(String id, ImsUri uri, Location loc, String label, int phoneId) {
        LocationType locationType;
        String label2 = label.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
        if (label2 == null) {
            locationType = LocationType.OWN_LOCATION;
        } else {
            locationType = LocationType.OTHER_LOCATION;
        }
        String geoSms = new GlsGeoSmsComposer().compose(makeGlsData(id, uri, loc, label2, locationType), this.mConfigs.get(phoneId).getPagerModeLimit());
        String str = LOG_TAG;
        Log.d(str, "generateGeoSms: " + geoSms + " by limit: " + this.mConfigs.get(phoneId).getPagerModeLimit());
        return geoSms;
    }

    private boolean isPushServiceAvailable() {
        return isPushServiceAvailable(SimUtil.getDefaultPhoneId());
    }

    private boolean isPushServiceAvailable(int phoneId) {
        ImsRegistration imsRegistration = getImsRegistration(phoneId);
        if (imsRegistration == null || !imsRegistration.hasRcsService() || imsRegistration.getPhoneId() != phoneId || !this.mPushEnabled[imsRegistration.getPhoneId()]) {
            Mno mno = SimUtil.getSimMno(phoneId);
            if (imsRegistration != null && mno == Mno.ATT && imsRegistration.hasService(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
                return true;
            }
            Log.i(LOG_TAG, "geolocation push is disabled.");
            return false;
        }
        String str = LOG_TAG;
        Log.i(str, "imsRegistration:" + imsRegistration + ", mPushEnabled: true");
        return true;
    }

    public void onTransferCompleted(FtMessage msg) {
        String str = LOG_TAG;
        Log.i(str, "onTransferCompleted: " + msg.getStateId());
        updateExtInfo(msg);
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) instanceof CmccStrategy) {
            this.mTranslation.onLocationShareInCallCompleted((long) msg.getId(), msg.getDirection(), true, msg);
        } else {
            this.mTranslation.onLocationShareInCallCompleted((long) msg.getId(), msg.getDirection(), true);
        }
    }

    public void onTransferCanceled(FtMessage msg) {
        String str = LOG_TAG;
        Log.i(str, "onTransferCanceled: " + msg.getStateId());
        updateExtInfo(msg);
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) instanceof CmccStrategy) {
            this.mTranslation.onLocationShareInCallCompleted((long) msg.getId(), msg.getDirection(), false, msg);
        } else {
            this.mTranslation.onLocationShareInCallCompleted((long) msg.getId(), msg.getDirection(), false);
        }
    }

    public void onOutgoingTransferAttached(FtMessage msg) {
        this.mTranslation.onCreateInCallLocationShareResponse(msg.getChatId(), (long) msg.getId(), msg.getRequestMessageId(), true);
        obtainMessage(1, msg.getId(), 0).sendToTarget();
    }

    public void onIncomingTransferUndecided(FtMessage msg) {
        this.mTranslation.onIncomingLoactionShareInCall(msg);
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority());
        if (mnoStrategy != null && mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.AUTO_ACCEPT_GLS)) {
            obtainMessage(0, msg.getId(), 0).sendToTarget();
        }
    }

    public void updateExtInfo(MessageBase message) {
        String extinfo;
        String body = null;
        if (message.getExtInfo() != null) {
            Log.v(LOG_TAG, "Already has ext info, no need update!!!");
            return;
        }
        if (message instanceof ImMessage) {
            body = ((ImMessage) message).getBody();
        } else if (message instanceof FtMessage) {
            BufferedReader br = null;
            String filePath = ((FtMessage) message).getFilePath();
            StringBuilder builder = new StringBuilder();
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
                while (true) {
                    String readLine = br2.readLine();
                    String line = readLine;
                    if (readLine != null) {
                        builder.append(line);
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                        }
                    }
                }
                br2.close();
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                if (br != null) {
                    br.close();
                }
            } catch (IOException e3) {
                e3.printStackTrace();
                if (br != null) {
                    br.close();
                }
            } catch (Throwable th) {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
            body = builder.toString();
        }
        if (TextUtils.isEmpty(body)) {
            Log.e(LOG_TAG, "Error!!! no gls data in message");
            return;
        }
        String str = LOG_TAG;
        Log.v(str, "XML BODY IS " + IMSLog.checker(body));
        if (body.toLowerCase().startsWith("geo")) {
            extinfo = new GlsGeoSmsParser().getGlsExtInfo(body);
        } else {
            extinfo = new GlsXmlParser().getGlsExtInfo(body);
        }
        String str2 = LOG_TAG;
        Log.i(str2, "THE EXTINFO IS " + IMSLog.checker(extinfo));
        message.updateExtInfo(extinfo);
    }

    private void updateFeatures(int phoneId) {
        boolean isRcsEnabled = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, phoneId) != 1) {
            isRcsEnabled = false;
        }
        if (!isRcsEnabled) {
            Log.i(LOG_TAG, "updateFeatures: RCS is disabled");
            this.mPushEnabled[phoneId] = false;
            return;
        }
        this.mPushEnabled[phoneId] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, phoneId), false).booleanValue();
        Log.i(LOG_TAG, "updateFeatures mPushEnabled: " + this.mPushEnabled[phoneId]);
        for (int i = 0; i < this.phoneCount; i++) {
            this.mConfigs.put(i, this.mImModule.getImConfig(i));
        }
    }
}
