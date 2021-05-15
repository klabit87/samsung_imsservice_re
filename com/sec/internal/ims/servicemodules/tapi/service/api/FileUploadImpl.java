package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.net.Uri;
import com.gsma.services.rcs.upload.FileUpload;
import com.gsma.services.rcs.upload.FileUploadInfo;
import com.gsma.services.rcs.upload.IFileUpload;
import com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload.FileUploadMessage;
import com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload.IFileUploadTaskListener;

public class FileUploadImpl extends IFileUpload.Stub {
    private FileUploadMessage mMessage;

    public FileUploadImpl(FileUploadMessage message, IFileUploadTaskListener listener) {
        this.mMessage = message;
        message.addListener(listener);
    }

    public String getUploadId() {
        return this.mMessage.getFileUploadId();
    }

    public FileUploadInfo getUploadInfo() {
        return this.mMessage.getFileUploadInfo();
    }

    public Uri getFile() {
        return this.mMessage.getFileUri();
    }

    public FileUpload.State getState() {
        return this.mMessage.getState();
    }

    public void startUpload() {
        this.mMessage.startUploadTask();
    }

    public void abortUpload() {
        this.mMessage.abortUploadTask();
    }
}
