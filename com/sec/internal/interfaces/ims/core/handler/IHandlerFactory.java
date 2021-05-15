package com.sec.internal.interfaces.ims.core.handler;

import com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.presence.IPresenceStackInterface;
import com.sec.internal.ims.servicemodules.sms.ISmsServiceInterface;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;

public interface IHandlerFactory extends ISequentialInitializable {
    ICmcMediaServiceInterface getCmcHandler();

    IEucServiceInterface getEucHandler();

    IImServiceInterface getImHandler();

    IIshServiceInterface getIshHandler();

    IMediaServiceInterface getMediaHandler();

    IMiscHandler getMiscHandler();

    IOptionsServiceInterface getOptionsHandler();

    IPresenceStackInterface getPresenceHandler();

    ISipDialogInterface getRawSipHandler();

    IRegistrationInterface getRegistrationStackAdaptor();

    ISlmServiceInterface getSlmHandler();

    ISmsServiceInterface getSmsHandler();

    IVolteServiceInterface getVolteStackAdaptor();

    IvshServiceInterface getVshHandler();
}
