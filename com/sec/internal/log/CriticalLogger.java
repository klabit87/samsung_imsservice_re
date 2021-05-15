package com.sec.internal.log;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CriticalLogger {
    private static final long FLUSH_TIMEOUT = 500;
    private static final String IMS_CR_LOG_PATH = "/data/log/imscr/imscr.log";
    static final int LIMIT_LOG_RECORD = 30;
    private static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);
    static final int MAX_LOG_SIZE = 1048576;
    private static final String NAME = "IMSCR";
    static final int NUM_OF_LOGS = 5;
    private static final long SAVE_PERIOD = 600000;
    /* access modifiers changed from: private */
    public static ArrayList<Object> mBuffer = new ArrayList<>(31);
    protected LogFileManager mLogFileManager;
    private LoggingHandler mLoggingHandler;
    private HandlerThread mLoggingThread;

    private CriticalLogger() {
        init();
    }

    public static CriticalLogger getInstance() {
        return HOLDER.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public void init() {
        HandlerThread handlerThread = new HandlerThread(NAME);
        this.mLoggingThread = handlerThread;
        handlerThread.start();
        this.mLoggingHandler = new LoggingHandler(this.mLoggingThread.getLooper());
        LogFileManager logFileManager = new LogFileManager(IMS_CR_LOG_PATH, MAX_LOG_SIZE, 5);
        this.mLogFileManager = logFileManager;
        logFileManager.init();
    }

    /* access modifiers changed from: package-private */
    public int getLogRecordCount() {
        return mBuffer.size();
    }

    /* access modifiers changed from: package-private */
    public Looper getLooper() {
        return this.mLoggingHandler.getLooper();
    }

    public void write(int logClass, String description) {
        LOG_TIME_FORMAT.setTimeZone(TimeZone.getDefault());
        StringBuilder log = new StringBuilder(String.format("%s 0x%08X", new Object[]{LOG_TIME_FORMAT.format(new Date()), Integer.valueOf(logClass)}));
        if (!TextUtils.isEmpty(description)) {
            if (description.length() > 50) {
                description = description.substring(0, 50);
            }
            log.append(":");
            log.append(description);
        }
        String logStr = log.toString();
        Log.e("#IMSCR", logStr);
        LoggingHandler loggingHandler = this.mLoggingHandler;
        loggingHandler.sendMessage(loggingHandler.obtainMessage(1, logStr));
        if (!this.mLoggingHandler.hasMessages(2)) {
            this.mLoggingHandler.sendEmptyMessageDelayed(2, SAVE_PERIOD);
        }
    }

    public void flush() {
        Log.e("#IMSCR", "Flush " + mBuffer.size());
        if (this.mLoggingHandler.hasMessages(2)) {
            this.mLoggingHandler.removeMessages(2);
        }
        CountDownLatch flushLock = new CountDownLatch(1);
        LoggingHandler loggingHandler = this.mLoggingHandler;
        loggingHandler.sendMessage(loggingHandler.obtainMessage(2, flushLock));
        try {
            flushLock.await(FLUSH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
    }

    private static class HOLDER {
        /* access modifiers changed from: private */
        public static final CriticalLogger INSTANCE = new CriticalLogger();

        private HOLDER() {
        }
    }

    private class LoggingHandler extends Handler {
        static final int EVENT_ADD = 1;
        static final int EVENT_SAVE = 2;

        LoggingHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                CriticalLogger.mBuffer.add(msg.obj);
                if (CriticalLogger.mBuffer.size() >= 30) {
                    save((CountDownLatch) null);
                }
            } else if (i == 2) {
                save((CountDownLatch) msg.obj);
                removeMessages(2);
                sendEmptyMessageDelayed(2, CriticalLogger.SAVE_PERIOD);
            }
        }

        private void save(CountDownLatch latch) {
            if (!CriticalLogger.mBuffer.isEmpty()) {
                CriticalLogger.this.mLogFileManager.write((String) CriticalLogger.mBuffer.stream().map($$Lambda$JsVbJ5mpbRjwJuW_A3bDJMqYpF0.INSTANCE).collect(Collectors.joining("\n", "", "\n")));
                CriticalLogger.mBuffer.clear();
            }
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
