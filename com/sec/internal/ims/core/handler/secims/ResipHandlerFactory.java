package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.os.Looper;
import com.sec.internal.ims.core.handler.HandlerFactory;
import com.sec.internal.ims.core.handler.MiscHandler;
import com.sec.internal.ims.core.handler.RawSipHandler;
import com.sec.internal.ims.core.handler.VolteHandler;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.handler.IRegistrationInterface;

public class ResipHandlerFactory extends HandlerFactory {
    public ResipHandlerFactory(Looper serviceLooper, Context context, IImsFramework imsFramework) {
        super(serviceLooper);
        mRegistrationHandler = new ResipRegistrationManager(serviceLooper, context, imsFramework);
        mAndroidVolteHandler = new ResipVolteHandler(serviceLooper, context, imsFramework);
        mMediaHandler = new ResipMediaHandler(serviceLooper, context, imsFramework);
        mVshHandler = new ResipVshHandler(serviceLooper, context, imsFramework);
        mIshHandler = new ResipIshHandler(serviceLooper, imsFramework);
        mSmsHandler = new ResipSmsHandler(serviceLooper, imsFramework);
        mOptionsHandler = new ResipOptionsHandler(serviceLooper, imsFramework);
        mPresenceHandler = new ResipPresenceHandler(serviceLooper, imsFramework);
        ResipImdnHandler imdnHandler = new ResipImdnHandler(serviceLooper, imsFramework);
        mImHandler = new ResipImHandler(serviceLooper, imsFramework, imdnHandler);
        mSlmHandler = new ResipSlmHandler(serviceLooper, imsFramework, imdnHandler);
        mEucHandler = new ResipEucHandler(serviceLooper, imsFramework);
        mMiscHandler = new ResipMiscHandler(serviceLooper, context, imsFramework);
        mRawSipHandler = new ResipRawSipHandler(serviceLooper, imsFramework);
        mCmcHandler = new ResipCmcHandler(serviceLooper, context, imsFramework);
        mVolteHandler = new VolteHandler(serviceLooper) {
        };
    }

    public IRegistrationInterface getRegistrationStackAdaptor() {
        return mRegistrationHandler;
    }

    public VolteHandler getVolteStackAdaptor() {
        return mAndroidVolteHandler;
    }

    public RawSipHandler getRawSipHandler() {
        return mRawSipHandler;
    }

    public MiscHandler getMiscHandler() {
        return mMiscHandler;
    }
}
