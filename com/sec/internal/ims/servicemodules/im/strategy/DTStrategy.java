package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;

public final class DTStrategy extends EmeiaStrategy {
    public DTStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.RETRY_AFTER_SESSION);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }
}
