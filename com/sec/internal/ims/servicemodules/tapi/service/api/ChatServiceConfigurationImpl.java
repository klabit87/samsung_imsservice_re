package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.chat.IChatServiceConfiguration;
import com.sec.internal.constants.ims.servicemodules.im.ImDefaultConst;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.util.RcsSettingsUtils;

public class ChatServiceConfigurationImpl extends IChatServiceConfiguration.Stub {
    private final String LOG_TAG = ChatServiceConfigurationImpl.class.getSimpleName();
    private ImConfig mImConfig;
    private RcsSettingsUtils rcsSetting = null;

    public ChatServiceConfigurationImpl(ImConfig imConfig) {
        this.mImConfig = imConfig;
        this.rcsSetting = RcsSettingsUtils.getInstance();
    }

    public int getChatTimeout() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig == null) {
            return 0;
        }
        imConfig.getTimerIdle();
        return 0;
    }

    public int getGeolocExpirationTime() throws RemoteException {
        return 1800;
    }

    public int getGeolocLabelMaxLength() throws RemoteException {
        return 200;
    }

    public int getGroupChatMaxParticipants() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig == null) {
            return 0;
        }
        imConfig.getMaxAdhocGroupSize();
        return 0;
    }

    public int getGroupChatMessageMaxLength() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig != null) {
            return (int) imConfig.getMaxSize1ToM();
        }
        return 0;
    }

    public int getGroupChatMinParticipants() throws RemoteException {
        return 3;
    }

    public int getGroupChatSubjectMaxLength() throws RemoteException {
        return 100;
    }

    public int getIsComposingTimeout() throws RemoteException {
        return 20;
    }

    public int getOneToOneChatMessageMaxLength() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig != null) {
            return (int) imConfig.getMaxSize1To1();
        }
        return 0;
    }

    public boolean isChatSf() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig == null) {
            return false;
        }
        imConfig.isImCapAlwaysOn();
        return false;
    }

    public boolean isChatWarnSF() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig == null) {
            return false;
        }
        imConfig.isImWarnSf();
        return false;
    }

    public boolean isGroupChatSupported() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig == null) {
            return false;
        }
        imConfig.getGroupChatEnabled();
        return false;
    }

    public boolean isRespondToDisplayReportsEnabled() throws RemoteException {
        boolean bRespondDisplay = ImDefaultConst.DEFAULT_CHAT_RESPOND_TO_DISPLAY_REPORTS.booleanValue();
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            return Boolean.parseBoolean(rcsSettingsUtils.readParameter(ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS));
        }
        return bRespondDisplay;
    }

    public boolean isSmsFallback() throws RemoteException {
        ImConfig imConfig = this.mImConfig;
        if (imConfig == null) {
            return false;
        }
        imConfig.isSmsFallbackAuth();
        return false;
    }

    public void setRespondToDisplayReports(boolean enable) throws RemoteException {
        String str = this.LOG_TAG;
        Log.d(str, "setRespondToDisplayReports() enable=" + enable);
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeBoolean(ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS, enable);
        }
    }
}
