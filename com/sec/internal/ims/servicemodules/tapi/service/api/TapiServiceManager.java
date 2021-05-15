package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import com.gsma.services.rcs.CommonServiceConfiguration;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.tapi.service.api.interfaces.ITapiServiceManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.ims.util.TapiServiceUtil;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;

public class TapiServiceManager extends ServiceModuleBase implements ITapiServiceManager {
    private static CapabilityServiceImpl mCapabilityServiceImpl = null;
    private static ChatServiceImpl mChatServiceImpl = null;
    private static ContactServiceImpl mContactServiceImpl = null;
    private static FileTransferingServiceImpl mFileTransferingServiceImpl = null;
    private static FileUploadServiceImpl mFileUploadServiceImpl = null;
    private static GeolocSharingServiceImpl mGeolocSharingServiceImpl = null;
    private static HistoryLogServiceImpl mHistoryLogServiceImpl = null;
    private static ImageSharingServiceImpl mImageSharingServiceImpl = null;
    private static MultimediaSessionServiceImpl mMultimediaSessionServiceImpl = null;
    private static VideoSharingServiceImpl mVideoSharingServiceImpl = null;
    private final String LOG_TAG = TapiServiceManager.class.getSimpleName();
    private Context mContext;

    public TapiServiceManager(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        createTapiServices();
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        String str = this.LOG_TAG;
        Log.d(str, "onRegistered() services : " + regiInfo.getServices());
        if (RcsSettingsUtils.getInstance(this.mContext) != null) {
            RcsSettingsUtils.getInstance().updateSettings();
            RcsSettingsUtils.getInstance().updateTapiSettings();
            RcsSettingsUtils.getInstance().loadCCAndAC();
            PhoneUtils.initialize();
        }
        notifyRegistrationStatusToTapiClient(true, 0);
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
        notifyRegistrationStatusToTapiClient(false, errorCode);
    }

    public static boolean isSupportTapi() {
        return TapiServiceUtil.isSupportTapi();
    }

    public void broadcastServiceUp() {
        Log.i(this.LOG_TAG, "broadcastServiceUp");
        if (isSupportTapi()) {
            this.mContext.sendBroadcast(new Intent("com.gsma.services.rcs.action.SERVICE_UP"));
        }
    }

    public void notifyRegistrationStatusToTapiClient(boolean status, int errorCode) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyRegistrationStatusToTapiClient : " + status);
        RcsServiceRegistration.ReasonCode reasonCode = RcsServiceRegistration.ReasonCode.UNSPECIFIED;
        if (errorCode != 200) {
            reasonCode = RcsServiceRegistration.ReasonCode.CONNECTION_LOST;
        }
        if (isSupportTapi()) {
            ChatServiceImpl chatServiceImpl = mChatServiceImpl;
            if (chatServiceImpl != null) {
                chatServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
            FileTransferingServiceImpl fileTransferingServiceImpl = mFileTransferingServiceImpl;
            if (fileTransferingServiceImpl != null) {
                fileTransferingServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
            FileUploadServiceImpl fileUploadServiceImpl = mFileUploadServiceImpl;
            if (fileUploadServiceImpl != null) {
                fileUploadServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
            ImageSharingServiceImpl imageSharingServiceImpl = mImageSharingServiceImpl;
            if (imageSharingServiceImpl != null) {
                imageSharingServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
            GeolocSharingServiceImpl geolocSharingServiceImpl = mGeolocSharingServiceImpl;
            if (geolocSharingServiceImpl != null) {
                geolocSharingServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
            VideoSharingServiceImpl videoSharingServiceImpl = mVideoSharingServiceImpl;
            if (videoSharingServiceImpl != null) {
                videoSharingServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
            CapabilityServiceImpl capabilityServiceImpl = mCapabilityServiceImpl;
            if (capabilityServiceImpl != null) {
                capabilityServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
            MultimediaSessionServiceImpl multimediaSessionServiceImpl = mMultimediaSessionServiceImpl;
            if (multimediaSessionServiceImpl != null) {
                multimediaSessionServiceImpl.notifyRegistrationEvent(status, reasonCode);
            }
        }
    }

    public void createTapiServices() {
        Log.i(this.LOG_TAG, "createTapiServices");
        IServiceModuleManager serviceModuleManager = getServiceModuleManager();
        IImModule imModule = serviceModuleManager.getImModule();
        if (imModule != null) {
            setmFileTransferingServiceImpl(new FileTransferingServiceImpl(this.mContext, imModule));
            setmChatServiceImpl(new ChatServiceImpl(this.mContext, imModule));
            setmFileUploadServiceImpl(new FileUploadServiceImpl(this.mContext, imModule));
        }
        IImageShareModule imageShareModule = serviceModuleManager.getImageShareModule();
        if (imageShareModule != null) {
            setmImageSharingServiceImpl(new ImageSharingServiceImpl(imageShareModule));
        }
        IGlsModule glsModule = serviceModuleManager.getGlsModule();
        if (glsModule != null) {
            setmGeolocSharingServiceImpl(new GeolocSharingServiceImpl(this.mContext, glsModule));
        }
        IVideoShareModule videoShareModule = serviceModuleManager.getVideoShareModule();
        if (videoShareModule != null) {
            setmVideoSharingServiceImpl(new VideoSharingServiceImpl(videoShareModule));
        }
        setmContactServiceImpl(new ContactServiceImpl(this.mContext));
        setmCapabilityServiceImpl(new CapabilityServiceImpl(this.mContext));
        ISessionModule sessionModule = serviceModuleManager.getSessionModule();
        if (sessionModule != null) {
            setmMultimediaSessionServiceImpl(new MultimediaSessionServiceImpl(sessionModule));
        }
        setmHistoryLogServiceImpl(new HistoryLogServiceImpl());
        if (RcsSettingsUtils.getInstance(this.mContext) != null) {
            RcsSettingsUtils.getInstance().updateTapiSettings();
        }
        broadcastServiceUp();
    }

    public static void setmChatServiceImpl(ChatServiceImpl mChatServiceImpl2) {
        mChatServiceImpl = mChatServiceImpl2;
    }

    public static void setmFileTransferingServiceImpl(FileTransferingServiceImpl mFileTransferingServiceImpl2) {
        mFileTransferingServiceImpl = mFileTransferingServiceImpl2;
    }

    public static void setmFileUploadServiceImpl(FileUploadServiceImpl mFileUploadServiceImpl2) {
        mFileUploadServiceImpl = mFileUploadServiceImpl2;
    }

    public static void setmImageSharingServiceImpl(ImageSharingServiceImpl mImageSharingServiceImpl2) {
        mImageSharingServiceImpl = mImageSharingServiceImpl2;
    }

    public static void setmGeolocSharingServiceImpl(GeolocSharingServiceImpl mGeolocSharingServiceImpl2) {
        mGeolocSharingServiceImpl = mGeolocSharingServiceImpl2;
    }

    public static void setmVideoSharingServiceImpl(VideoSharingServiceImpl mVideoSharingServiceImpl2) {
        mVideoSharingServiceImpl = mVideoSharingServiceImpl2;
    }

    public static void setmContactServiceImpl(ContactServiceImpl mContactServiceImpl2) {
        mContactServiceImpl = mContactServiceImpl2;
    }

    public static void setmCapabilityServiceImpl(CapabilityServiceImpl mCapabilityServiceImpl2) {
        mCapabilityServiceImpl = mCapabilityServiceImpl2;
    }

    public static void setmMultimediaSessionServiceImpl(MultimediaSessionServiceImpl mMultimediaSessionServiceImpl2) {
        mMultimediaSessionServiceImpl = mMultimediaSessionServiceImpl2;
    }

    public static void setmHistoryLogServiceImpl(HistoryLogServiceImpl mHistoryLogServiceImpl2) {
        mHistoryLogServiceImpl = mHistoryLogServiceImpl2;
    }

    public static ChatServiceImpl getChatService() {
        return mChatServiceImpl;
    }

    public static FileTransferingServiceImpl getFtService() {
        return mFileTransferingServiceImpl;
    }

    public static FileUploadServiceImpl getfileUpService() {
        return mFileUploadServiceImpl;
    }

    public static ImageSharingServiceImpl getIshService() {
        return mImageSharingServiceImpl;
    }

    public static VideoSharingServiceImpl getVshService() {
        return mVideoSharingServiceImpl;
    }

    public static GeolocSharingServiceImpl getGlsService() {
        return mGeolocSharingServiceImpl;
    }

    public static ContactServiceImpl getContactService() {
        return mContactServiceImpl;
    }

    public static CapabilityServiceImpl getCapService() {
        return mCapabilityServiceImpl;
    }

    public static HistoryLogServiceImpl getHistoryService() {
        return mHistoryLogServiceImpl;
    }

    public static MultimediaSessionServiceImpl getMulSessionService() {
        return mMultimediaSessionServiceImpl;
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        if (phoneId != SimUtil.getDefaultPhoneId()) {
            Log.i(this.LOG_TAG, "ServiceSwitch not updated for defaultphoneId, return.");
            return;
        }
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, phoneId) != 1) {
            z = false;
        }
        boolean bIsRcsEnable = z;
        Log.i(this.LOG_TAG + "[" + phoneId + "]", "ImsServiceSwitch active:" + bIsRcsEnable);
        ContentResolver cr = this.mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ImsConstants.Intents.EXTRA_UPDATED_VALUE, Boolean.toString(bIsRcsEnable));
        cr.update(CommonServiceConfiguration.Settings.CONTENT_URI, values, "key" + "=?", new String[]{"ServiceActivated"});
    }

    public String[] getServicesRequiring() {
        return new String[]{"im", "slm", "ft", "ft_http", "options", "presence", "is", "vs"};
    }

    public void handleIntent(Intent intent) {
    }
}
