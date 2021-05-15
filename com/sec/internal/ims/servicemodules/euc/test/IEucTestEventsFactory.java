package com.sec.internal.ims.servicemodules.euc.test;

import android.content.Intent;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucSystemRequest;

public interface IEucTestEventsFactory {
    IEucAcknowledgment createAcknowledgement(Intent intent);

    IEucData createEucData(Intent intent);

    IEucNotification createNotification(Intent intent);

    IEucRequest createPersistent(Intent intent);

    IEucSystemRequest createSystemRequest(Intent intent);

    AutoconfUserConsentData createUserConsent(Intent intent);

    IEucRequest createVolatile(Intent intent);
}
