package com.sec.internal.interfaces.ims.servicemodules.euc;

import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import java.util.List;

public interface IEucFactory {
    Iterable<IEucQuery> combine(List<IEucData> list, List<IDialogData> list2);

    IDialogData createDialogData(EucMessageKey eucMessageKey, String str, String str2, String str3, String str4, String str5);

    IEucQuery createEUC(IEucAcknowledgment iEucAcknowledgment);

    IEucQuery createEUC(IEucNotification iEucNotification);

    IEucQuery createEUC(IEucRequest iEucRequest);

    IEucData createEucData(EucMessageKey eucMessageKey, boolean z, String str, boolean z2, EucState eucState, long j, Long l);
}
