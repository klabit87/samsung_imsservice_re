package com.att.iqi.lib;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;
import com.att.iqi.IIQIBroker;
import com.att.iqi.IMetricQueryCallback;
import com.att.iqi.IMetricSourcingCallback;
import com.att.iqi.IProfileChangedCallback;
import com.att.iqi.IServiceStateChangeCallback;
import com.att.iqi.lib.Metric;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IQIManager {
    private static IQIManager j;
    private static final Object k = new Object();
    private IIQIBroker a;
    /* access modifiers changed from: private */
    public final SparseArray<MetricQueryCallback> b = new SparseArray<>();
    /* access modifiers changed from: private */
    public final List<ProfileChangeListener> c = new ArrayList();
    /* access modifiers changed from: private */
    public final SparseArray<MetricSourcingListener> d = new SparseArray<>();
    /* access modifiers changed from: private */
    public final List<ServiceStateChangeListener> e = new ArrayList();
    private final IMetricQueryCallback f = new IMetricQueryCallback.Stub() {
        public void onMetricQueried(Metric.ID id, byte[] bArr) throws RemoteException {
            MetricQueryCallback metricQueryCallback;
            if (id != null && (metricQueryCallback = (MetricQueryCallback) IQIManager.this.b.get(id.asInt())) != null) {
                if (bArr == null) {
                    bArr = new byte[0];
                }
                ByteBuffer wrap = ByteBuffer.wrap(bArr);
                long clearCallingIdentity = Binder.clearCallingIdentity();
                metricQueryCallback.onMetricQueried(id, wrap);
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    };
    private final IMetricSourcingCallback g = new IMetricSourcingCallback.Stub() {
        public void onMetricSourced(Metric.ID id, byte[] bArr) throws RemoteException {
            MetricSourcingListener metricSourcingListener;
            if (id != null && (metricSourcingListener = (MetricSourcingListener) IQIManager.this.d.get(id.asInt())) != null) {
                if (bArr == null) {
                    bArr = new byte[0];
                }
                ByteBuffer wrap = ByteBuffer.wrap(bArr);
                long clearCallingIdentity = Binder.clearCallingIdentity();
                metricSourcingListener.onMetricSourcing(id, wrap);
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    };
    private final IProfileChangedCallback h = new IProfileChangedCallback.Stub() {
        public void onProfileChanged() throws RemoteException {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            synchronized (IQIManager.this.c) {
                for (ProfileChangeListener onProfileChanged : IQIManager.this.c) {
                    onProfileChanged.onProfileChanged();
                }
            }
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    };
    private final IServiceStateChangeCallback i = new IServiceStateChangeCallback.Stub() {
        public void onServiceChange(boolean z) throws RemoteException {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            synchronized (IQIManager.this.e) {
                for (ServiceStateChangeListener onServiceChange : IQIManager.this.e) {
                    onServiceChange.onServiceChange(z);
                }
            }
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    };

    public interface MetricQueryCallback {
        void onMetricQueried(Metric.ID id, ByteBuffer byteBuffer);
    }

    public interface MetricSourcingListener {
        void onMetricSourcing(Metric.ID id, ByteBuffer byteBuffer);
    }

    public interface ProfileChangeListener {
        void onProfileChanged();
    }

    public interface ServiceStateChangeListener {
        void onServiceChange(boolean z);
    }

    private IQIManager() {
        a();
    }

    private void a() {
        if (this.a == null) {
            try {
                try {
                    try {
                        IBinder iBinder = (IBinder) Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class}).invoke((Object) null, new String[]{"iqi"});
                        if (iBinder != null) {
                            this.a = IIQIBroker.Stub.asInterface(iBinder);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e2) {
                    }
                } catch (NoSuchMethodException e3) {
                }
            } catch (ClassNotFoundException e4) {
            }
        }
    }

    public static IQIManager getInstance() {
        IQIManager iQIManager;
        synchronized (k) {
            if (j == null) {
                j = new IQIManager();
            }
            iQIManager = j;
        }
        return iQIManager;
    }

    public void registerMetricSourcingListener(Metric.ID id, MetricSourcingListener metricSourcingListener) {
        if (id != null && metricSourcingListener != null) {
            try {
                a();
                this.a.registerMetricSourcingCallback(id, this.g);
                synchronized (this.d) {
                    this.d.append(id.asInt(), metricSourcingListener);
                }
            } catch (IllegalArgumentException e2) {
                throw new IllegalArgumentException("Callback already registered for metric ID " + id.asString());
            } catch (Exception e3) {
            }
        }
    }

    public void registerProfileChangeListener(ProfileChangeListener profileChangeListener) {
        boolean isEmpty;
        if (profileChangeListener != null) {
            synchronized (this.c) {
                isEmpty = this.c.isEmpty();
                this.c.add(profileChangeListener);
            }
            if (isEmpty) {
                try {
                    a();
                    this.a.registerProfileChangedCallback(this.h);
                } catch (Exception e2) {
                }
            }
        }
    }

    public void registerQueryCallback(Metric.ID id, MetricQueryCallback metricQueryCallback) {
        if (id != null && metricQueryCallback != null) {
            try {
                a();
                this.a.registerMetricQueryCallback(id, this.f);
                synchronized (this.b) {
                    this.b.append(id.asInt(), metricQueryCallback);
                }
            } catch (IllegalArgumentException e2) {
                throw new IllegalArgumentException("Callback already registered for metric ID " + id.asString());
            } catch (Exception e3) {
            }
        }
    }

    public void registerServiceStateChangeListener(ServiceStateChangeListener serviceStateChangeListener) {
        boolean isEmpty;
        if (serviceStateChangeListener != null) {
            synchronized (this.e) {
                isEmpty = this.e.isEmpty();
                this.e.add(serviceStateChangeListener);
            }
            if (isEmpty) {
                try {
                    a();
                    this.a.registerServiceChangedCallback(this.i);
                } catch (Exception e2) {
                }
            }
        }
    }

    public boolean shouldSubmitMetric(Metric.ID id) {
        if (id == null) {
            return false;
        }
        try {
            a();
            return this.a.shouldSubmitMetric(id);
        } catch (Exception e2) {
            return false;
        }
    }

    public void submitMetric(Metric metric) {
        if (metric != null) {
            try {
                a();
                this.a.submitMetric(metric);
            } catch (Exception e2) {
            }
        }
    }

    public void unregisterMetricSourcingListener(Metric.ID id, MetricSourcingListener metricSourcingListener) {
        if (id != null && metricSourcingListener != null) {
            try {
                a();
                this.a.unregisterMetricSourcingCallback(id, this.g);
                synchronized (this.d) {
                    this.d.remove(id.asInt());
                }
            } catch (Exception e2) {
            }
        }
    }

    public void unregisterProfileChangeListener(ProfileChangeListener profileChangeListener) {
        boolean isEmpty;
        if (profileChangeListener != null) {
            synchronized (this.c) {
                this.c.remove(profileChangeListener);
                isEmpty = this.c.isEmpty();
            }
            if (isEmpty) {
                try {
                    a();
                    this.a.unregisterProfileChangedCallback(this.h);
                } catch (Exception e2) {
                }
            }
        }
    }

    public void unregisterQueryCallback(Metric.ID id, MetricQueryCallback metricQueryCallback) {
        if (id != null && metricQueryCallback != null) {
            try {
                a();
                this.a.unregisterMetricQueryCallback(id, this.f);
                synchronized (this.b) {
                    this.b.remove(id.asInt());
                }
            } catch (Exception e2) {
            }
        }
    }

    public void unregisterServiceStateChangeListener(ServiceStateChangeListener serviceStateChangeListener) {
        boolean isEmpty;
        if (serviceStateChangeListener != null) {
            synchronized (this.e) {
                this.e.remove(serviceStateChangeListener);
                isEmpty = this.e.isEmpty();
            }
            if (isEmpty) {
                try {
                    a();
                    this.a.unregisterServiceChangedCallback(this.i);
                } catch (Exception e2) {
                }
            }
        }
    }
}
