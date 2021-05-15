package com.sec.internal.ims.servicemodules.im.listener;

import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;

public interface ImMessageListener {
    void onMessageReceived(ImMessage imMessage);

    void onMessageSendResponse(ImMessage imMessage);

    void onMessageSendResponseTimeout(ImMessage imMessage);

    void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result);

    void onMessageSendingSucceeded(MessageBase messageBase);
}
