package com.sec.internal.interfaces.ims.cmstore;

import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;

public interface INetAPIEventListener {
    void onCloudObjectNotificationUpdated(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onCloudSyncStopped(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onDeviceFlagUpdateCompleted(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onDeviceFlagUpdateSchedulerStarted();

    void onFallbackToProvision(IControllerCommonInterface iControllerCommonInterface, IHttpAPICommonInterface iHttpAPICommonInterface, int i);

    void onInitSyncCompleted(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onInitSyncSummaryCompleted(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onInitialSyncStarted();

    void onMessageDownloadCompleted(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onMessageUploadCompleted(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onNotificationObjectDownloaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onOmaAuthenticationFailed(ParamOMAresponseforBufDB paramOMAresponseforBufDB, long j);

    void onOmaFailExceedMaxCount();

    void onOmaSuccess(IHttpAPICommonInterface iHttpAPICommonInterface);

    void onOneDeviceFlagUpdated(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onOneMessageDownloaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onOneMessageUploaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onPartialSyncSummaryCompleted(ParamOMAresponseforBufDB paramOMAresponseforBufDB);

    void onPauseCMNNetApiWithResumeDelay(int i);

    void onSyncFailed(ParamOMAresponseforBufDB paramOMAresponseforBufDB);
}
