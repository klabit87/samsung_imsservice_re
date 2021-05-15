package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface;

public abstract class CmcHandler extends BaseHandler implements ICmcMediaServiceInterface {
    protected final RegistrantList mCmcMediaEventRegistrants = new RegistrantList();

    protected CmcHandler(Looper looper) {
        super(looper);
    }

    public void registerForCmcMediaEvent(Handler handler, int what, Object obj) {
        this.mCmcMediaEventRegistrants.addUnique(handler, what, obj);
    }

    public void unregisterForCmcMediaEvent(Handler handler) {
        this.mCmcMediaEventRegistrants.remove(handler);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }

    public boolean startCmcRecord(int phoneId, int sessionId, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) {
        return false;
    }

    public boolean stopCmcRecord(int phoneId, int sessionId) {
        return false;
    }
}
