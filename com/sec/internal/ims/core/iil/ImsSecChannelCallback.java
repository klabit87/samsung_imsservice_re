package com.sec.internal.ims.core.iil;

import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import vendor.samsung.hardware.radio.channel.V2_0.ISehChannelCallback;

public class ImsSecChannelCallback extends ISehChannelCallback.Stub {
    static final String LOG_TAG = "ImsSecChannelCallback";
    IpcDispatcher mIpcDispatcher;

    public ImsSecChannelCallback(IpcDispatcher ipcDispatcher) {
        this.mIpcDispatcher = ipcDispatcher;
    }

    public void receive(ArrayList<Byte> data) {
        IMSLog.i(LOG_TAG, "receive");
        try {
            byte[] response = IpcDispatcher.arrayListToPrimitiveArray(data);
            if (response != null) {
                this.mIpcDispatcher.processResponse(response, response.length);
            }
        } catch (RuntimeException e) {
            IMSLog.e(LOG_TAG, "receive " + e);
        }
    }
}
