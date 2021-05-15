package com.sec.internal.ims.servicemodules.im.listener;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import java.util.Set;

public interface FtMessageListener {
    void onAutoResumeTransfer(FtMessage ftMessage);

    void onCancelRequestFailed(FtMessage ftMessage);

    void onFileResizingNeeded(FtMessage ftMessage, long j);

    void onFtErrorReport(ImError imError);

    void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result);

    void onMessageSendingSucceeded(MessageBase messageBase);

    ChatData.ChatType onRequestChatType(String str);

    Message onRequestCompleteCallback(String str);

    String onRequestIncomingFtTransferPath();

    Set<ImsUri> onRequestParticipantUris(String str);

    Integer onRequestRegistrationType();

    void onSendDeliveredNotification(FtMessage ftMessage);

    void onTransferCanceled(FtMessage ftMessage);

    void onTransferCompleted(FtMessage ftMessage);

    void onTransferCreated(FtMessage ftMessage);

    void onTransferInProgress(FtMessage ftMessage);

    void onTransferProgressReceived(FtMessage ftMessage);

    void onTransferReceived(FtMessage ftMessage);
}
