package com.google.android.gms.tasks;

import com.google.android.gms.common.internal.zzbq;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Tasks {

    static final class zza implements zzb {
        private final CountDownLatch zzapc;

        private zza() {
            this.zzapc = new CountDownLatch(1);
        }

        /* synthetic */ zza(zzq zzq) {
            this();
        }

        public final void await() throws InterruptedException {
            this.zzapc.await();
        }

        public final boolean await(long j, TimeUnit timeUnit) throws InterruptedException {
            return this.zzapc.await(j, timeUnit);
        }

        public final void onFailure(Exception exc) {
            this.zzapc.countDown();
        }

        public final void onSuccess(Object obj) {
            this.zzapc.countDown();
        }
    }

    interface zzb extends OnFailureListener, OnSuccessListener<Object> {
    }

    static final class zzc implements zzb {
        private final Object mLock = new Object();
        private final zzp<Void> zzlel;
        private Exception zzleq;
        private final int zzlet;
        private int zzleu;
        private int zzlev;

        public zzc(int i, zzp<Void> zzp) {
            this.zzlet = i;
            this.zzlel = zzp;
        }

        private final void zzblg() {
            if (this.zzleu + this.zzlev != this.zzlet) {
                return;
            }
            if (this.zzleq == null) {
                this.zzlel.setResult(null);
                return;
            }
            zzp<Void> zzp = this.zzlel;
            int i = this.zzlev;
            int i2 = this.zzlet;
            StringBuilder sb = new StringBuilder(54);
            sb.append(i);
            sb.append(" out of ");
            sb.append(i2);
            sb.append(" underlying tasks failed");
            zzp.setException(new ExecutionException(sb.toString(), this.zzleq));
        }

        public final void onFailure(Exception exc) {
            synchronized (this.mLock) {
                this.zzlev++;
                this.zzleq = exc;
                zzblg();
            }
        }

        public final void onSuccess(Object obj) {
            synchronized (this.mLock) {
                this.zzleu++;
                zzblg();
            }
        }
    }

    private Tasks() {
    }

    public static <TResult> TResult await(Task<TResult> task) throws ExecutionException, InterruptedException {
        zzbq.zzgw("Must not be called on the main application thread");
        zzbq.checkNotNull(task, "Task must not be null");
        if (task.isComplete()) {
            return zzc(task);
        }
        zza zza2 = new zza((zzq) null);
        zza(task, zza2);
        zza2.await();
        return zzc(task);
    }

    public static <TResult> TResult await(Task<TResult> task, long j, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
        zzbq.zzgw("Must not be called on the main application thread");
        zzbq.checkNotNull(task, "Task must not be null");
        zzbq.checkNotNull(timeUnit, "TimeUnit must not be null");
        if (task.isComplete()) {
            return zzc(task);
        }
        zza zza2 = new zza((zzq) null);
        zza(task, zza2);
        if (zza2.await(j, timeUnit)) {
            return zzc(task);
        }
        throw new TimeoutException("Timed out waiting for Task");
    }

    public static <TResult> Task<TResult> call(Callable<TResult> callable) {
        return call(TaskExecutors.MAIN_THREAD, callable);
    }

    public static <TResult> Task<TResult> call(Executor executor, Callable<TResult> callable) {
        zzbq.checkNotNull(executor, "Executor must not be null");
        zzbq.checkNotNull(callable, "Callback must not be null");
        zzp zzp = new zzp();
        executor.execute(new zzq(zzp, callable));
        return zzp;
    }

    public static <TResult> Task<TResult> forException(Exception exc) {
        zzp zzp = new zzp();
        zzp.setException(exc);
        return zzp;
    }

    public static <TResult> Task<TResult> forResult(TResult tresult) {
        zzp zzp = new zzp();
        zzp.setResult(tresult);
        return zzp;
    }

    public static Task<Void> whenAll(Collection<? extends Task<?>> collection) {
        if (collection.isEmpty()) {
            return forResult((Object) null);
        }
        for (Task task : collection) {
            if (task == null) {
                throw new NullPointerException("null tasks are not accepted");
            }
        }
        zzp zzp = new zzp();
        zzc zzc2 = new zzc(collection.size(), zzp);
        for (Task zza2 : collection) {
            zza(zza2, zzc2);
        }
        return zzp;
    }

    public static Task<Void> whenAll(Task<?>... taskArr) {
        return taskArr.length == 0 ? forResult((Object) null) : whenAll((Collection<? extends Task<?>>) Arrays.asList(taskArr));
    }

    public static Task<List<Task<?>>> whenAllComplete(Collection<? extends Task<?>> collection) {
        return whenAll(collection).continueWith(new zzs(collection));
    }

    public static Task<List<Task<?>>> whenAllComplete(Task<?>... taskArr) {
        return whenAllComplete((Collection<? extends Task<?>>) Arrays.asList(taskArr));
    }

    public static <TResult> Task<List<TResult>> whenAllSuccess(Collection<? extends Task<?>> collection) {
        return whenAll(collection).continueWith(new zzr(collection));
    }

    public static <TResult> Task<List<TResult>> whenAllSuccess(Task<?>... taskArr) {
        return whenAllSuccess((Collection<? extends Task<?>>) Arrays.asList(taskArr));
    }

    private static void zza(Task<?> task, zzb zzb2) {
        task.addOnSuccessListener(TaskExecutors.zzlem, (OnSuccessListener<? super Object>) zzb2);
        task.addOnFailureListener(TaskExecutors.zzlem, (OnFailureListener) zzb2);
    }

    private static <TResult> TResult zzc(Task<TResult> task) throws ExecutionException {
        if (task.isSuccessful()) {
            return task.getResult();
        }
        throw new ExecutionException(task.getException());
    }
}
