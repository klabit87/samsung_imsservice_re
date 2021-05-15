package com.sec.internal.ims.translate;

import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.PersistentMessage;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;

public class PersistentMessageTranslator extends EucMessageTranslator implements TypeTranslator<PersistentMessage, IEucRequest> {
    public IEucRequest translate(PersistentMessage value) throws TranslationException {
        return translate(value.request(), (Long) null, IEucRequest.EucRequestType.PERSISTENT);
    }
}
