package com.sec.internal.ims.servicemodules.openapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IImsService;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.openapi.ISipDialogListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IVolteService;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.os.RemoteCallbackListWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.handler.ISipDialogInterface;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IOpenApiServiceModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class OpenApiServiceModule extends ServiceModuleBase implements IOpenApiServiceModule {
    private static final int EVENT_INCOMING_SIP_MESSAGE = 100;
    private static final int EVENT_SIP_DIALOG_SEND_SIP_RESP = 101;
    private static final String LOG_TAG = "OpenApiServiceModule";
    private Context mContext;
    /* access modifiers changed from: private */
    public ArrayList<IDialogEventListener> mDialogEventListener = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<IImsCallEventListener> mImsCallEventListener = new ArrayList<>();
    /* access modifiers changed from: private */
    public IImsService mImsService = null;
    private ISipDialogInterface mRawSipIntf;
    /* access modifiers changed from: private */
    public ArrayList<IImsRegistrationListener> mRegiListener = new ArrayList<>();
    private int mRegistrationId = -1;
    /* access modifiers changed from: private */
    public RemoteCallbackListWrapper<ISipDialogListener> mSipDialogListeners = new RemoteCallbackListWrapper<>();
    /* access modifiers changed from: private */
    public IVolteService mVolteService = null;

    public OpenApiServiceModule(Looper looper, Context context, ISipDialogInterface sipDialogInterface) {
        super(looper);
        this.mContext = context;
        this.mRawSipIntf = sipDialogInterface;
        sipDialogInterface.registerForEvent(this, 100, (Object) null);
    }

    public String[] getServicesRequiring() {
        return new String[]{"mmtel", "presence"};
    }

    public void start() {
        Log.i(LOG_TAG, "connect VoLteService/ImsService");
        super.start();
        connectVoLteService();
        connectImsService();
    }

    public void onRegistered(ImsRegistration regiInfo) {
        if (regiInfo == null) {
            Log.d(LOG_TAG, "regiInfo is null");
            return;
        }
        super.onRegistered(regiInfo);
        ImsProfile imsProfile = regiInfo.getImsProfile();
        if (imsProfile != null && !imsProfile.hasEmergencySupport()) {
            this.mRegistrationId = getRegistrationInfoId(regiInfo);
        }
    }

    public void onDeregistering(ImsRegistration reg) {
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
        this.mRegistrationId = -1;
        super.onDeregistered(regiInfo, errorCode);
    }

    public void registerDialogEventListener(IDialogEventListener listener) throws RemoteException {
        IImsService iImsService = this.mImsService;
        if (iImsService != null) {
            iImsService.registerDialogEventListener(this.mDefaultPhoneId, listener);
        } else {
            this.mDialogEventListener.add(listener);
        }
    }

    public void unregisterDialogEventListener(IDialogEventListener listener) throws RemoteException {
        this.mImsService.unregisterDialogEventListener(this.mDefaultPhoneId, listener);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        IImsService iImsService = this.mImsService;
        if (iImsService != null) {
            iImsService.registerImsRegistrationListener(listener);
        } else {
            this.mRegiListener.add(listener);
        }
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        this.mImsService.unregisterImsRegistrationListener(listener);
    }

    public void setFeatureTags(String[] featureTags) {
        Log.d(LOG_TAG, "setFeatureTags: featureTags[" + Arrays.asList(featureTags) + "]");
        ImsRegistry.getRegistrationManager().setThirdPartyFeatureTags(featureTags);
    }

    public void registerIncomingSipMessageListener(final ISipDialogListener listener) {
        try {
            listener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                public void binderDied() {
                    Log.i(OpenApiServiceModule.LOG_TAG, "binder died, " + listener);
                    synchronized (OpenApiServiceModule.this.mSipDialogListeners) {
                        OpenApiServiceModule.this.mSipDialogListeners.unregister(listener);
                    }
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        synchronized (this.mSipDialogListeners) {
            this.mSipDialogListeners.register(listener);
        }
        this.mRawSipIntf.openSipDialog(true);
    }

    public void unregisterIncomingSipMessageListener(ISipDialogListener listener) {
        synchronized (this.mSipDialogListeners) {
            this.mSipDialogListeners.unregister(listener);
        }
        if (this.mSipDialogListeners.getRegisteredCallbackCount() == 0) {
            this.mRawSipIntf.openSipDialog(false);
        }
    }

    public void registerImsCallEventListener(IImsCallEventListener listener) {
        try {
            if (this.mVolteService != null) {
                this.mVolteService.registerForCallStateEvent(listener);
            } else {
                this.mImsCallEventListener.add(listener);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterImsCallEventListener(IImsCallEventListener listener) {
        try {
            this.mVolteService.deregisterForCallStateEvent(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean sendSip(String sipMessage, ISipDialogListener listener) {
        ImsRegistration imsRegistration = getImsRegistration();
        if (imsRegistration == null) {
            return false;
        }
        return this.mRawSipIntf.sendSip(imsRegistration.getHandle(), sipMessage, obtainMessage(101, listener));
    }

    public void setupMediaPath(String[] remoteIp) {
        IPdnController pdncontroller = ImsRegistry.getPdnController();
        for (String address : remoteIp) {
            pdncontroller.requestRouteToHostAddress(11, address);
        }
    }

    private void onSipMessageReceived(AsyncResult ar) {
        this.mSipDialogListeners.broadcastCallback(new RemoteCallbackListWrapper.Broadcaster() {
            public final void broadcast(IInterface iInterface) {
                ((ISipDialogListener) iInterface).onSipReceived((String) AsyncResult.this.result);
            }
        });
    }

    public void handleIntent(Intent intent) {
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Log.d(LOG_TAG, "handleMessage: what " + msg.what);
        int i = msg.what;
        if (i == 100) {
            onSipMessageReceived((AsyncResult) msg.obj);
        } else if (i == 101) {
            AsyncResult ar = (AsyncResult) msg.obj;
            try {
                ((ISipDialogListener) ar.userObj).onSipReceived((String) ar.result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectVoLteService() {
        if (this.mVolteService == null) {
            Intent intent = new Intent();
            intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.VolteService2");
            ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Connected to VolteService.");
                    IVolteService unused = OpenApiServiceModule.this.mVolteService = IVolteService.Stub.asInterface(service);
                    if (OpenApiServiceModule.this.mVolteService == null) {
                        Log.e(OpenApiServiceModule.LOG_TAG, "Failed to get IVolteService with " + service);
                        return;
                    }
                    try {
                        if (!OpenApiServiceModule.this.mImsCallEventListener.isEmpty()) {
                            Iterator it = OpenApiServiceModule.this.mImsCallEventListener.iterator();
                            while (it.hasNext()) {
                                IImsCallEventListener temp = (IImsCallEventListener) it.next();
                                OpenApiServiceModule.this.mVolteService.registerForCallStateEvent(temp);
                                OpenApiServiceModule.this.mImsCallEventListener.remove(temp);
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                public void onServiceDisconnected(ComponentName name) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Disconnected from VolteService.");
                    IVolteService unused = OpenApiServiceModule.this.mVolteService = null;
                }
            }, 1, ContextExt.CURRENT_OR_SELF);
        }
    }

    private void connectImsService() {
        if (this.mImsService == null) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.ImsService");
            ContextExt.bindServiceAsUser(this.mContext, serviceIntent, new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Connected to ImsService.");
                    IImsService unused = OpenApiServiceModule.this.mImsService = IImsService.Stub.asInterface(service);
                    try {
                        if (!OpenApiServiceModule.this.mDialogEventListener.isEmpty()) {
                            Iterator it = OpenApiServiceModule.this.mDialogEventListener.iterator();
                            while (it.hasNext()) {
                                IDialogEventListener temp = (IDialogEventListener) it.next();
                                OpenApiServiceModule.this.mImsService.registerDialogEventListener(OpenApiServiceModule.this.mDefaultPhoneId, temp);
                                OpenApiServiceModule.this.mDialogEventListener.remove(temp);
                            }
                        }
                        if (!OpenApiServiceModule.this.mRegiListener.isEmpty()) {
                            Iterator it2 = OpenApiServiceModule.this.mRegiListener.iterator();
                            while (it2.hasNext()) {
                                IImsRegistrationListener temp2 = (IImsRegistrationListener) it2.next();
                                OpenApiServiceModule.this.mImsService.registerImsRegistrationListener(temp2);
                                OpenApiServiceModule.this.mRegiListener.remove(temp2);
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                public void onServiceDisconnected(ComponentName name) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Disconnected from ImsService.");
                    IImsService unused = OpenApiServiceModule.this.mImsService = null;
                }
            }, 1, ContextExt.CURRENT_OR_SELF);
        }
    }

    public ImsRegistration getImsRegistration() {
        if (this.mRegistrationId != -1) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId);
        }
        return null;
    }
}
