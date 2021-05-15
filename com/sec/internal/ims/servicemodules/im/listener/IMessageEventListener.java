package com.sec.internal.ims.servicemodules.im.listener;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;

public interface IMessageEventListener {
    void onImdnNotificationReceived(MessageBase messageBase, ImsUri imsUri, NotificationStatus notificationStatus, boolean z);

    void onMessageReceived(MessageBase messageBase, ImSession imSession);

    void onMessageSendResponse(MessageBase messageBase);

    void onMessageSendResponseFailed(String str, int i, int i2, String str2);

    void onMessageSendResponseTimeout(MessageBase messageBase);

    void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result);

    void onMessageSendingSucceeded(MessageBase messageBase);
}
