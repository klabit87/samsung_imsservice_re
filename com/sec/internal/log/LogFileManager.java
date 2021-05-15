package com.sec.internal.log;

import android.text.TextUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;

class LogFileManager {
    private String mFullPath;
    private int mMaxCount;
    private int mMaxSize;
    private MeteredWriter mMeter;
    private Path[] mPaths;

    LogFileManager(String path, int maxSize, int maxCount) {
        if (TextUtils.isEmpty(path) || maxSize < 0 || maxCount < 1) {
            throw new IllegalArgumentException();
        }
        this.mFullPath = path;
        this.mMaxSize = maxSize;
        this.mMaxCount = maxCount;
    }

    /* access modifiers changed from: package-private */
    public void init() {
        this.mPaths = new Path[this.mMaxCount];
        for (int i = 0; i < this.mMaxCount; i++) {
            this.mPaths[i] = Paths.get(String.format(Locale.US, "%s.%d", new Object[]{this.mFullPath, Integer.valueOf(i)}), new String[0]);
        }
        try {
            open(this.mPaths[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void open(Path path) throws IOException {
        long len = 0;
        OpenOption openOption = StandardOpenOption.WRITE;
        if (Files.exists(path, new LinkOption[0])) {
            len = Files.size(path);
            openOption = StandardOpenOption.APPEND;
        } else {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.createFile(path, new FileAttribute[0]);
        }
        this.mMeter = new MeteredWriter(Files.newBufferedWriter(path, new OpenOption[]{openOption}), len);
    }

    private synchronized void rotate() throws IOException {
        for (int i = this.mMaxCount - 2; i >= 0; i--) {
            if (Files.exists(this.mPaths[i], new LinkOption[0])) {
                Files.move(this.mPaths[i], this.mPaths[i + 1], new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            }
        }
        open(this.mPaths[0]);
    }

    /* access modifiers changed from: package-private */
    public synchronized void write(String msg) {
        boolean success = true;
        try {
            if (this.mMeter == null || Files.notExists(this.mPaths[0], new LinkOption[0])) {
                open(this.mPaths[0]);
            }
            this.mMeter.write(msg);
        } catch (IOException e) {
            success = false;
        }
        if (!success) {
            try {
                open(this.mPaths[0]);
                this.mMeter.write(msg);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        if (this.mMeter.written > ((long) this.mMaxSize)) {
            rotate();
        }
        return;
    }

    private static class MeteredWriter {
        final Writer writer;
        long written;

        MeteredWriter(Writer writer2, long written2) {
            this.writer = writer2;
            this.written = written2;
        }

        public void write(String msg) throws IOException {
            this.writer.write(msg);
            this.writer.flush();
            this.written += (long) msg.length();
        }
    }
}
