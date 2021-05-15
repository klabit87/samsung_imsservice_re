package com.sec.internal.ims.servicemodules.im.listener;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;

public interface IFtEventListener {
    void onCancelRequestFailed(FtMessage ftMessage);

    void onFileResizingNeeded(FtMessage ftMessage, long j);

    void onFileTransferAttached(FtMessage ftMessage);

    void onFileTransferCreated(FtMessage ftMessage);

    void onFileTransferReceived(FtMessage ftMessage);

    void onImdnNotificationReceived(FtMessage ftMessage, ImsUri imsUri, NotificationStatus notificationStatus, boolean z);

    void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result);

    void onTransferCanceled(FtMessage ftMessage);

    void onTransferCompleted(FtMessage ftMessage);

    void onTransferProgressReceived(FtMessage ftMessage);

    void onTransferStarted(FtMessage ftMessage);
}
