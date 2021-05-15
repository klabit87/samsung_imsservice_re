package com.sec.internal.ims.servicemodules.sms;

import android.util.Log;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SmsLogger {
    private static final int LOG_BUFFER_SIZE = 100;
    private static final String TAG = "SmsLogger";
    private static ConcurrentHashMap<String, LinkedList<String>> mEventLogs = new ConcurrentHashMap<>();
    private static SmsLogger sInstance = null;

    public static synchronized SmsLogger getInstance() {
        SmsLogger smsLogger;
        synchronized (SmsLogger.class) {
            if (sInstance == null) {
                sInstance = new SmsLogger();
            }
            smsLogger = sInstance;
        }
        return smsLogger;
    }

    private String currentTime() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date());
    }

    public void add(String mName, String log) {
        LinkedList<String> mTmpLog = mEventLogs.getOrDefault(mName, new LinkedList());
        synchronized (mTmpLog) {
            mTmpLog.add(currentTime() + "   " + log);
            while (mTmpLog.size() > 100) {
                mTmpLog.removeFirst();
            }
        }
        mEventLogs.put(mName, mTmpLog);
    }

    public void logAndAdd(String mName, String log) {
        Log.i(mName, log);
        add(mName, log);
    }

    public void dump() {
        IMSLog.dump(TAG, "Dump of SMS :");
        IMSLog.increaseIndent(TAG);
        for (Map.Entry<String, LinkedList<String>> e : mEventLogs.entrySet()) {
            IMSLog.dump(TAG, e.getKey());
            IMSLog.increaseIndent(TAG);
            LinkedList<String> _logList = e.getValue();
            synchronized (_logList) {
                Iterator it = _logList.iterator();
                while (it.hasNext()) {
                    IMSLog.dump(TAG, (String) it.next());
                }
            }
            IMSLog.decreaseIndent(TAG);
        }
        IMSLog.decreaseIndent(TAG);
    }
}
