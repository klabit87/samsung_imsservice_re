package com.sec.internal.ims.servicemodules.presence;

import android.os.Handler;
import android.os.Message;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import java.util.HashMap;
import java.util.List;

public interface IPresenceStackInterface {
    void publish(PresenceInfo presenceInfo, Message message, int i);

    void registerForPresenceInfo(Handler handler, int i, Object obj);

    void registerForPublishFailure(Handler handler, int i, Object obj);

    void registerForWatcherInfo(Handler handler, int i, Object obj);

    void subscribe(ImsUri imsUri, boolean z, Message message, String str, int i);

    void subscribeList(List<ImsUri> list, boolean z, Message message, String str, boolean z2, int i, int i2);

    void unpublish(int i);

    void updateServiceVersion(int i, HashMap<String, String> hashMap);
}
