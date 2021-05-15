package com.google.android.gms.tasks;

import java.util.concurrent.Executor;

final class zzo implements Executor {
    zzo() {
    }

    public final void execute(Runnable runnable) {
        runnable.run();
    }
}
