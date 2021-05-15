package com.sec.internal.interfaces.ims.servicemodules.volte2;

import com.android.internal.telephony.PublishDialog;
import com.sec.ims.cmc.CmcCallInfo;
import java.util.List;

public interface ICmcServiceHelper {
    CmcCallInfo getCmcCallInfo();

    boolean isCmcRegExist(int i);

    void sendPublishDialog(int i, PublishDialog publishDialog, int i2);

    void setP2pServiceInfo(String str, String str2);

    void startP2p(String str, String str2);

    void startP2pDiscovery(List<String> list);
}
