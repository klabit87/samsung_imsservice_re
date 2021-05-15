package com.sec.internal.ims.translate;

import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendEucResponseResponse;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.util.ImsUtil;

public class EucResponseStatusTranslator implements TypeTranslator<SendEucResponseResponse, EucSendResponseStatus> {
    private static final String LOG_TAG = EucResponseStatusTranslator.class.getSimpleName();

    public EucSendResponseStatus translate(SendEucResponseResponse eucSendResponseResponse) {
        EucSendResponseStatus.Status moduleStatus;
        EucType originalEucType;
        String responseId = eucSendResponseResponse.id();
        if (responseId != null) {
            int handle = ImsUtil.getHandle(eucSendResponseResponse.handle());
            int status = eucSendResponseResponse.status();
            if (status == 0) {
                moduleStatus = EucSendResponseStatus.Status.SUCCESS;
            } else if (status == 1) {
                moduleStatus = EucSendResponseStatus.Status.FAILURE_INTERNAL;
            } else if (status == 2) {
                moduleStatus = EucSendResponseStatus.Status.FAILURE_NETWORK;
            } else {
                throw new TranslationException(Integer.valueOf(status));
            }
            int type = eucSendResponseResponse.type();
            if (type == 0) {
                originalEucType = EucType.PERSISTENT;
            } else if (type == 1) {
                originalEucType = EucType.VOLATILE;
            } else {
                Log.e(LOG_TAG, "Unknown or unsupported type of the original EUCR message.");
                throw new TranslationException(Integer.valueOf(type));
            }
            return new EucSendResponseStatus(responseId, originalEucType, ImsUri.parse(eucSendResponseResponse.remoteUri()), EucTranslatorUtil.getOwnIdentity(handle), moduleStatus);
        }
        throw new TranslationException("ID of EUC related to response is null!");
    }
}
