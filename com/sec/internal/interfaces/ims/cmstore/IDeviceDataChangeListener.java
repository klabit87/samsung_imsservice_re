package com.sec.internal.interfaces.ims.cmstore;

import android.os.Handler;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;

public interface IDeviceDataChangeListener {
    boolean isNativeMsgAppDefault();

    void onInitialDBCopyDone();

    void onMailBoxResetBufferDbDone();

    void registerForUpdateFromCloud(Handler handler, int i, Object obj);

    void registerForUpdateOfWorkingStatus(Handler handler, int i, Object obj);

    void sendAppSync(SyncParam syncParam);

    void sendDeviceFax(BufferDBChangeParamList bufferDBChangeParamList);

    void sendDeviceInitialSyncDownload(BufferDBChangeParamList bufferDBChangeParamList);

    void sendDeviceNormalSyncDownload(BufferDBChangeParamList bufferDBChangeParamList);

    void sendDeviceUpdate(BufferDBChangeParamList bufferDBChangeParamList);

    void sendDeviceUpload(BufferDBChangeParamList bufferDBChangeParamList);

    void stopAppSync(SyncParam syncParam);
}
