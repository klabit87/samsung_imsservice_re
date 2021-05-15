package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.filetransfer.FileTransferServiceConfiguration;
import com.gsma.services.rcs.filetransfer.IFileTransferServiceConfiguration;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.tapi.service.defaultconst.FileTransferDefaultConst;
import com.sec.internal.ims.util.RcsSettingsUtils;

public class FileTransferServiceConfigurationImpl extends IFileTransferServiceConfiguration.Stub {
    private static final String LOG_TAG = FileTransferServiceConfigurationImpl.class.getSimpleName();
    private ImConfig mConfig = null;
    private RcsSettingsUtils rcsSetting = null;

    public FileTransferServiceConfigurationImpl(ImConfig config) {
        this.mConfig = config;
        this.rcsSetting = RcsSettingsUtils.getInstance();
        String str = LOG_TAG;
        Log.d(str, "rcsSetting: " + this.rcsSetting);
    }

    public void setAutoAccept(boolean enable) throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FILE_TRANSFER, enable);
        }
    }

    public void setAutoAcceptInRoaming(boolean enable) throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FT_IN_ROAMING, enable);
        }
    }

    public void setImageResizeOption(int option) throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeParameter(ImSettings.KEY_IMAGE_RESIZE_OPTION, String.valueOf(option));
        }
    }

    public int getImageResizeOption() throws RemoteException {
        FileTransferServiceConfiguration.ImageResizeOption imageResizeOption = FileTransferDefaultConst.DEFALUT_IMAGERESIZEOPTION;
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            String mValue = rcsSettingsUtils.readParameter(ImSettings.KEY_IMAGE_RESIZE_OPTION);
            int value = Integer.parseInt(mValue);
            imageResizeOption = FileTransferServiceConfiguration.ImageResizeOption.valueOf(value);
            String str = LOG_TAG;
            Log.d(str, "start : getImageResizeOption() mValue:" + mValue + ", value:" + value + ", imageResizeOption=" + imageResizeOption);
        }
        int vv = imageResizeOption.toInt();
        String str2 = LOG_TAG;
        Log.d(str2, "start : getImageResizeOption() vv=" + vv);
        return vv;
    }

    public int getMaxFileTransfers() throws RemoteException {
        return 10;
    }

    public long getMaxSize() throws RemoteException {
        ImConfig imConfig = this.mConfig;
        if (imConfig != null) {
            return imConfig.getMaxSizeFileTr();
        }
        return 0;
    }

    public long getWarnSize() throws RemoteException {
        ImConfig imConfig = this.mConfig;
        if (imConfig != null) {
            return imConfig.getFtWarnSize();
        }
        return 0;
    }

    public boolean isAutoAcceptEnabled() throws RemoteException {
        boolean bAutoAccept = false;
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            bAutoAccept = Boolean.parseBoolean(rcsSettingsUtils.readParameter(ImSettings.AUTO_ACCEPT_FILE_TRANSFER));
        }
        ImConfig imConfig = this.mConfig;
        if (imConfig != null) {
            return imConfig.isFtAutAccept();
        }
        return bAutoAccept;
    }

    public boolean isAutoAcceptInRoamingEnabled() throws RemoteException {
        boolean bAutoAcceptInRoaming = false;
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            bAutoAcceptInRoaming = Boolean.parseBoolean(rcsSettingsUtils.readParameter(ImSettings.AUTO_ACCEPT_FT_IN_ROAMING));
        }
        ImConfig imConfig = this.mConfig;
        if (imConfig != null) {
            return imConfig.isFtAutAccept();
        }
        return bAutoAcceptInRoaming;
    }

    public boolean isAutoAcceptModeChangeable() throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            return Boolean.parseBoolean(rcsSettingsUtils.readParameter(ImSettings.AUTO_ACCEPT_FT_CHANGEABLE));
        }
        return false;
    }

    public boolean isGroupFileTransferSupported() throws RemoteException {
        return true;
    }

    public long getMaxAudioMessageLength() throws RemoteException {
        return 600;
    }
}
