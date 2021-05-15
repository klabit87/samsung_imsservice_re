package com.sec.internal.interfaces.ims.servicemodules.csh;

import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.csh.IImageShareEventListener;
import com.sec.internal.ims.servicemodules.csh.ImageShare;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.concurrent.Future;

public interface IImageShareModule extends IServiceModule {
    Future<ImageShare> createShare(ImsUri imsUri, String str);

    long getMaxSize();

    long getWarnSize();

    void registerImageShareEventListener(IImageShareEventListener iImageShareEventListener);
}
