package com.sec.internal.ims.core.iil;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import vendor.samsung.hardware.radio.channel.V2_0.ISehChannel;

public class IpcDispatcher {
    static final int EVENT_SEC_CHANNEL_PROXY_DEAD = 1;
    private static final int EVENT_SEND_IPC = 1;
    static final int ISehChannel_GET_SERVICE_DELAY_MILLIS = 4000;
    private static final String LOG_TAG = "IpcDispatcher";
    private static final String SERVICE_NAME_IMS = "imsd";
    private static final String SERVICE_NAME_IMS2 = "imsd2";
    private static final int VOLTE_TYPE_DUAL = 3;
    private static final int VOLTE_TYPE_SINGLE = 1;
    private static int mSupportVolteType;
    ImsSecChannelCallback mImsSecChannelCallback;
    /* access modifiers changed from: private */
    public ArrayList<Registrant> mRegistrants;
    private ArrayList<Registrant> mRegistrantsForIilConnected;
    final SecChannelHandler mSecChannelHandler;
    volatile ISehChannel mSecChannelProxy = null;
    final AtomicLong mSecChannelProxyCookie = new AtomicLong(0);
    final SecChannelProxyDeathRecipient mSecChannelProxyDeathRecipient;
    private ImsModemSender mSender;
    private HandlerThread mSenderThread;
    /* access modifiers changed from: private */
    public int mSlotId;

    static {
        mSupportVolteType = 1;
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            mSupportVolteType = 3;
        }
    }

    class SecChannelHandler extends Handler {
        SecChannelHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int access$000 = IpcDispatcher.this.mSlotId;
                IMSLog.i(IpcDispatcher.LOG_TAG, access$000, "handleMessage: EVENT_SEC_CHANNEL_PROXY_DEAD cookie = " + msg.obj + " mSecChannelProxyCookie = " + IpcDispatcher.this.mSecChannelProxyCookie.get());
                if (((Long) msg.obj).longValue() == IpcDispatcher.this.mSecChannelProxyCookie.get()) {
                    IpcDispatcher.this.resetProxy();
                    ISehChannel unused = IpcDispatcher.this.getSecChannelProxy();
                }
            }
        }
    }

    final class SecChannelProxyDeathRecipient implements IHwBinder.DeathRecipient {
        SecChannelProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Iterator it = IpcDispatcher.this.mRegistrants.iterator();
            while (it.hasNext()) {
                ((Registrant) it.next()).notifyRegistrant(new AsyncResult((Object) null, (Object) null, new IOException("Disconnected from 'imsd'")));
            }
            IMSLog.i(IpcDispatcher.LOG_TAG, IpcDispatcher.this.mSlotId, "serviceDied");
            IpcDispatcher.this.mSecChannelHandler.sendMessageDelayed(IpcDispatcher.this.mSecChannelHandler.obtainMessage(1, Long.valueOf(cookie)), 4000);
        }
    }

    /* access modifiers changed from: private */
    public ISehChannel getSecChannelProxy() {
        if (this.mSecChannelProxy != null) {
            return this.mSecChannelProxy;
        }
        try {
            if (mSupportVolteType != 3) {
                this.mSecChannelProxy = ISehChannel.getService(SERVICE_NAME_IMS);
            } else if (this.mSlotId == 0) {
                this.mSecChannelProxy = ISehChannel.getService(SERVICE_NAME_IMS);
            } else {
                this.mSecChannelProxy = ISehChannel.getService(SERVICE_NAME_IMS2);
            }
            if (this.mSecChannelProxy != null) {
                this.mSecChannelProxy.linkToDeath(this.mSecChannelProxyDeathRecipient, this.mSecChannelProxyCookie.incrementAndGet());
                this.mSecChannelProxy.setCallback(this.mImsSecChannelCallback);
                IMSLog.s(LOG_TAG, this.mSlotId, "notify IIL Connected");
                Iterator<Registrant> it = this.mRegistrantsForIilConnected.iterator();
                while (it.hasNext()) {
                    it.next().notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
                }
            } else {
                IMSLog.e(LOG_TAG, this.mSlotId, "getSecChannelProxy: mSecChannelProxy == null");
            }
        } catch (RemoteException | RuntimeException e) {
            this.mSecChannelProxy = null;
            int i = this.mSlotId;
            IMSLog.e(LOG_TAG, i, "SecChannelProxy getService/setCallback: " + e);
        }
        if (this.mSecChannelProxy == null) {
            SecChannelHandler secChannelHandler = this.mSecChannelHandler;
            secChannelHandler.sendMessageDelayed(secChannelHandler.obtainMessage(1, Long.valueOf(this.mSecChannelProxyCookie.incrementAndGet())), 4000);
        }
        return this.mSecChannelProxy;
    }

    /* access modifiers changed from: private */
    public void resetProxy() {
        this.mSecChannelProxy = null;
        this.mSecChannelProxyCookie.incrementAndGet();
    }

    /* access modifiers changed from: package-private */
    public void handleSecChannelProxyExceptionForRR(String caller, Exception e) {
        int i = this.mSlotId;
        IMSLog.e(LOG_TAG, i, caller + ": " + e);
        resetProxy();
        SecChannelHandler secChannelHandler = this.mSecChannelHandler;
        secChannelHandler.sendMessageDelayed(secChannelHandler.obtainMessage(1, Long.valueOf(this.mSecChannelProxyCookie.incrementAndGet())), 4000);
    }

    public static byte[] arrayListToPrimitiveArray(ArrayList<Byte> bytes) {
        int messageLength;
        if (bytes.size() <= 2) {
            return null;
        }
        int lower = bytes.get(0).byteValue() & 255;
        int higher = bytes.get(1).byteValue() & 255;
        if (higher == 0) {
            messageLength = lower;
        } else {
            messageLength = lower + (higher << 8);
        }
        byte[] ret = new byte[messageLength];
        for (int i = 0; i < ret.length - 2; i++) {
            ret[i] = bytes.get(i + 2).byteValue();
        }
        return ret;
    }

    public static ArrayList<Byte> primitiveArrayToArrayList(byte[] arr) {
        ArrayList<Byte> arrayList = new ArrayList<>(arr.length);
        for (byte b : arr) {
            arrayList.add(Byte.valueOf(b));
        }
        return arrayList;
    }

    public IpcDispatcher(int slotId) {
        IMSLog.i(LOG_TAG, slotId, "IpcDispatcher Support Volte Type = " + mSupportVolteType);
        this.mSlotId = slotId;
        this.mRegistrants = new ArrayList<>();
        this.mRegistrantsForIilConnected = new ArrayList<>();
        this.mImsSecChannelCallback = new ImsSecChannelCallback(this);
        this.mSecChannelHandler = new SecChannelHandler();
        this.mSecChannelProxyDeathRecipient = new SecChannelProxyDeathRecipient();
    }

    public void initDipatcher() {
        HandlerThread handlerThread = new HandlerThread("ImsModemSender" + this.mSlotId);
        this.mSenderThread = handlerThread;
        handlerThread.start();
        this.mSender = new ImsModemSender(this.mSenderThread.getLooper());
        getSecChannelProxy();
    }

    public boolean setRegistrant(int what, Handler h) {
        this.mRegistrants.add(new Registrant(h, what, (Object) null));
        return true;
    }

    public boolean setRegistrantForIilConnected(int what, Handler h) {
        this.mRegistrantsForIilConnected.add(new Registrant(h, what, (Object) null));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void processResponse(byte[] data, int length) {
        IpcMessage ipcMsg = IpcMessage.parseIpc(data, length);
        if (ipcMsg == null) {
            IMSLog.e(LOG_TAG, this.mSlotId, "cannot parse ipc");
            return;
        }
        int i = this.mSlotId;
        IMSLog.i(LOG_TAG, i, "[Rx]: (M)" + ipcMsg.mainCmdStr() + " (S)" + ipcMsg.subCmdStr() + " (T)" + ipcMsg.typeStr() + " l:" + ipcMsg.getLength());
        int i2 = this.mSlotId;
        StringBuilder sb = new StringBuilder();
        sb.append("[Rx]: ");
        sb.append(ipcMsg.dumpHex(ipcMsg.getBody()));
        IMSLog.s(LOG_TAG, i2, sb.toString());
        Iterator<Registrant> it = this.mRegistrants.iterator();
        while (it.hasNext()) {
            it.next().notifyRegistrant(new AsyncResult((Object) null, ipcMsg, (Throwable) null));
        }
    }

    class ImsModemSender extends Handler implements Runnable {
        public ImsModemSender(Looper looper) {
            super(looper);
        }

        public void run() {
        }

        public void handleMessage(Message msg) {
            ISehChannel secChannelProxy;
            byte[] data = (byte[]) msg.obj;
            if (msg.what == 1 && (secChannelProxy = IpcDispatcher.this.getSecChannelProxy()) != null) {
                try {
                    IMSLog.i(IpcDispatcher.LOG_TAG, IpcDispatcher.this.mSlotId, "ImsModemSender(): send");
                    secChannelProxy.send(IpcDispatcher.primitiveArrayToArrayList(data));
                } catch (RemoteException | RuntimeException e) {
                    IpcDispatcher.this.handleSecChannelProxyExceptionForRR("send", e);
                }
            }
        }
    }

    public boolean sendMessage(IpcMessage msg) {
        if (msg == null) {
            IMSLog.e(LOG_TAG, this.mSlotId, "send IPC message error");
            return false;
        }
        msg.setDir(1);
        int i = this.mSlotId;
        IMSLog.i(LOG_TAG, i, "[Tx]: (M)" + msg.mainCmdStr() + " (S)" + msg.subCmdStr() + " (T)" + msg.typeStr() + " l:" + msg.getLength());
        int i2 = this.mSlotId;
        StringBuilder sb = new StringBuilder();
        sb.append("[Tx]: ");
        sb.append(msg.dumpHex(msg.getBody()));
        IMSLog.s(LOG_TAG, i2, sb.toString());
        ImsModemSender imsModemSender = this.mSender;
        if (imsModemSender != null) {
            imsModemSender.obtainMessage(1, msg.getData()).sendToTarget();
        }
        return true;
    }

    public boolean sendGeneralResponse(boolean result, IpcMessage response) {
        int ipcErrorCause;
        if (result) {
            ipcErrorCause = 32768;
        } else {
            ipcErrorCause = IpcMessage.IPC_GEN_ERR_INVALID_STATE;
        }
        return sendGeneralResponse(ipcErrorCause, response);
    }

    public boolean sendGeneralResponse(int ipcErrorCause, IpcMessage response) {
        IpcMessage packet = new IpcMessage(128, 1, 2);
        packet.encodeGeneralResponse(ipcErrorCause, response);
        return sendMessage(packet);
    }
}
