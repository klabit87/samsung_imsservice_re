package com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload;

import com.gsma.services.rcs.upload.FileUpload;
import com.gsma.services.rcs.upload.FileUploadInfo;

public interface IFileUploadTaskListener {
    void onUploadComplete(String str, FileUploadInfo fileUploadInfo);

    void onUploadProgress(String str, long j, long j2);

    void onUploadStateChanged(String str, FileUpload.State state, boolean z);
}
