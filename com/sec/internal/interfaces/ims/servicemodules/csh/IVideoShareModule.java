package com.sec.internal.interfaces.ims.servicemodules.csh;

import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.csh.VideoShare;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.concurrent.Future;

public interface IVideoShareModule extends IServiceModule {
    void changeSurfaceOrientation(long j, int i);

    Future<VideoShare> createShare(ImsUri imsUri, String str);

    int getMaxDurationTime();
}
