package com.sec.internal.helper;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IndentingPrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class SimpleEventLog extends Handler {
    private static final int EVENT_ADD = 1;
    private static final int EVENT_FLUSH = 2;
    private static final int EVENT_RESIZE = 3;
    private final int LOG_FILE_RECORD_LIMIT;
    private final Path LOG_PATH;
    private final String LOG_TAG = "SimpleEventLog";
    private final String NAME;
    private ExecutorService mFileIOExecutor;
    private final List<String> mLogBuffer = new ArrayList();

    public SimpleEventLog(Context context, String name, int size) {
        super(Looper.getMainLooper());
        this.NAME = name;
        this.LOG_FILE_RECORD_LIMIT = size;
        String absolutePath = context.getFilesDir().getAbsolutePath();
        this.LOG_PATH = Paths.get(absolutePath, new String[]{this.NAME + ".log"});
        add("> Created (pid: " + Binder.getCallingPid() + ", binary: " + Build.VERSION.INCREMENTAL + ")");
    }

    public void add(String log) {
        sendMessage(obtainMessage(1, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date()) + "   " + log));
    }

    public void add(int phoneId, String log) {
        add("slot[" + phoneId + "]: " + log);
    }

    public void logAndAdd(String log) {
        Log.i(this.NAME, log);
        add(log);
    }

    public void logAndAdd(int phoneId, String log) {
        logAndAdd("slot[" + phoneId + "]: " + log);
    }

    public void logAndAdd(int phoneId, IRegisterTask task, String log) {
        logAndAdd("slot[" + phoneId + "]: [" + task.getProfile().getName() + "|" + task.getState() + "] " + log);
    }

    public void dump(IndentingPrintWriter pw) {
        flush();
        awaitFlushFinished();
        pw.println("\nDump of " + this.NAME + ":");
        pw.increaseIndent();
        try {
            for (String log : Files.readAllLines(this.LOG_PATH)) {
                pw.println(log);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw.decreaseIndent();
    }

    public void dump() {
        flush();
        awaitFlushFinished();
        String str = this.NAME;
        IMSLog.dump(str, "EventLog(" + this.NAME + "):");
        IMSLog.increaseIndent(this.NAME);
        try {
            for (String log : Files.readAllLines(this.LOG_PATH)) {
                IMSLog.dump(this.NAME, log);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        IMSLog.decreaseIndent(this.NAME);
    }

    private void awaitFlushFinished() {
        try {
            this.mFileIOExecutor.shutdown();
            this.mFileIOExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
            this.mFileIOExecutor = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            this.mLogBuffer.add((String) msg.obj);
            schedulePeriodicEvents();
        } else if (i == 2) {
            flush();
        } else if (i == 3) {
            resize();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void schedulePeriodicEvents() {
        if (!hasMessages(2)) {
            sendMessageDelayed(obtainMessage(2), 300000);
        }
        if (!hasMessages(3)) {
            sendMessageDelayed(obtainMessage(3), 1800000);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void flush() {
        removeMessages(2);
        List<String> writeBuffer = new ArrayList<>(this.mLogBuffer);
        this.mLogBuffer.clear();
        if (this.mFileIOExecutor == null) {
            this.mFileIOExecutor = Executors.newSingleThreadExecutor();
        }
        this.mFileIOExecutor.execute(new Runnable(writeBuffer) {
            public final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                SimpleEventLog.this.lambda$flush$0$SimpleEventLog(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$flush$0$SimpleEventLog(List writeBuffer) {
        writeAll(writeBuffer, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    /* access modifiers changed from: package-private */
    public void resize() {
        removeMessages(3);
        ExecutorService executorService = this.mFileIOExecutor;
        if (executorService != null && !executorService.isTerminated()) {
            try {
                this.mFileIOExecutor.execute(new Runnable() {
                    public final void run() {
                        SimpleEventLog.this.lambda$resize$1$SimpleEventLog();
                    }
                });
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public /* synthetic */ void lambda$resize$1$SimpleEventLog() {
        if (Files.exists(this.LOG_PATH, new LinkOption[0])) {
            try {
                long elapsed = System.currentTimeMillis();
                List<String> writtenLogs = Files.readAllLines(this.LOG_PATH);
                int writtenSize = writtenLogs.size();
                Log.i("SimpleEventLog", this.NAME + " Read written lines: " + writtenSize + "(" + (System.currentTimeMillis() - elapsed) + " ms)");
                int linesToRemove = writtenSize - this.LOG_FILE_RECORD_LIMIT;
                if (linesToRemove > 0) {
                    writeAll(writtenLogs.subList(linesToRemove, writtenSize), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    /* access modifiers changed from: package-private */
    public void writeAll(List<String> logs, OpenOption... options) {
        BufferedWriter writer;
        try {
            writer = Files.newBufferedWriter(this.LOG_PATH, options);
            long elapsed = System.currentTimeMillis();
            for (String log : logs) {
                if (!TextUtils.isEmpty(log)) {
                    writer.write(log);
                    writer.newLine();
                }
            }
            Log.i("SimpleEventLog", this.NAME + " File writing done: " + logs.size() + "(" + (System.currentTimeMillis() - elapsed) + " ms)");
            if (writer != null) {
                writer.close();
                return;
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }
}
