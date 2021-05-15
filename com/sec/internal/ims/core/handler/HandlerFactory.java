package com.sec.internal.ims.core.handler;

import android.content.Context;
import android.os.Looper;
import com.sec.internal.ims.core.handler.secims.ResipHandlerFactory;
import com.sec.internal.ims.core.handler.secims.StackIF;
import com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.presence.IPresenceStackInterface;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;

public abstract class HandlerFactory implements IHandlerFactory {
    protected static VolteHandler mAndroidVolteHandler = null;
    protected static CmcHandler mCmcHandler = null;
    protected static EucHandler mEucHandler = null;
    protected static ImHandler mImHandler = null;
    protected static IshHandler mIshHandler = null;
    protected static MediaHandler mMediaHandler = null;
    protected static MiscHandler mMiscHandler = null;
    protected static OptionsHandler mOptionsHandler = null;
    protected static PresenceHandler mPresenceHandler = null;
    protected static RawSipHandler mRawSipHandler = null;
    protected static RegistrationHandler mRegistrationHandler = null;
    protected static SlmHandler mSlmHandler = null;
    protected static SmsHandler mSmsHandler = null;
    protected static VolteHandler mVolteHandler = null;
    protected static VshHandler mVshHandler = null;
    private static HandlerFactory sHandlerInstance = null;

    public static HandlerFactory createStackHandler(Looper serviceLooper, Context context, IImsFramework imsFramework) {
        StackIF.getInstance().setImsFramework(imsFramework);
        ResipHandlerFactory resipHandlerFactory = new ResipHandlerFactory(serviceLooper, context, imsFramework);
        sHandlerInstance = resipHandlerFactory;
        return resipHandlerFactory;
    }

    public HandlerFactory(Looper serviceLooper) {
    }

    public void initSequentially() {
        mRegistrationHandler.init();
        mVolteHandler.init();
        mAndroidVolteHandler.init();
        MediaHandler mediaHandler = mMediaHandler;
        if (mediaHandler != null) {
            mediaHandler.init();
        }
        mEucHandler.init();
        mImHandler.init();
        IshHandler ishHandler = mIshHandler;
        if (ishHandler != null) {
            ishHandler.init();
        }
        mOptionsHandler.init();
        mPresenceHandler.init();
        mSmsHandler.init();
        mSlmHandler.init();
        VshHandler vshHandler = mVshHandler;
        if (vshHandler != null) {
            vshHandler.init();
        }
        mMiscHandler.init();
        mRawSipHandler.init();
        CmcHandler cmcHandler = mCmcHandler;
        if (cmcHandler != null) {
            cmcHandler.init();
        }
        StackIF.getInstance().initMediaJni(mMediaHandler);
        StackIF.getInstance().initCmcJni(mCmcHandler);
    }

    public VolteHandler getVolteStackAdaptor() {
        return mVolteHandler;
    }

    public MediaHandler getMediaHandler() {
        return mMediaHandler;
    }

    public EucHandler getEucHandler() {
        return mEucHandler;
    }

    public IImServiceInterface getImHandler() {
        return mImHandler;
    }

    public IIshServiceInterface getIshHandler() {
        return mIshHandler;
    }

    public IOptionsServiceInterface getOptionsHandler() {
        return mOptionsHandler;
    }

    public IPresenceStackInterface getPresenceHandler() {
        return mPresenceHandler;
    }

    public SmsHandler getSmsHandler() {
        return mSmsHandler;
    }

    public IvshServiceInterface getVshHandler() {
        return mVshHandler;
    }

    public ISlmServiceInterface getSlmHandler() {
        return mSlmHandler;
    }

    public RawSipHandler getRawSipHandler() {
        return mRawSipHandler;
    }

    public MiscHandler getMiscHandler() {
        return mMiscHandler;
    }

    public CmcHandler getCmcHandler() {
        return mCmcHandler;
    }
}
