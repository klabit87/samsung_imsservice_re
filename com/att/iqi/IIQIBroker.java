package com.att.iqi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.att.iqi.lib.Metric;

public interface IIQIBroker extends IInterface {

    public static abstract class Stub extends Binder implements IIQIBroker {
        private static final String DESCRIPTOR = "com.att.iqi.IIQIBroker";
        static final int TRANSACTION_getTimestamp = 9;
        static final int TRANSACTION_registerMetricQueryCallback = 3;
        static final int TRANSACTION_registerMetricSourcingCallback = 5;
        static final int TRANSACTION_registerProfileChangedCallback = 7;
        static final int TRANSACTION_registerServiceChangedCallback = 10;
        static final int TRANSACTION_shouldSubmitMetric = 1;
        static final int TRANSACTION_submitMetric = 2;
        static final int TRANSACTION_unregisterMetricQueryCallback = 4;
        static final int TRANSACTION_unregisterMetricSourcingCallback = 6;
        static final int TRANSACTION_unregisterProfileChangedCallback = 8;
        static final int TRANSACTION_unregisterServiceChangedCallback = 11;

        private static class Proxy implements IIQIBroker {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public long getTimestamp() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readLong();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void registerMetricQueryCallback(Metric.ID id, IMetricQueryCallback iMetricQueryCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (id != null) {
                        obtain.writeInt(1);
                        id.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iMetricQueryCallback != null ? iMetricQueryCallback.asBinder() : null);
                    this.mRemote.transact(3, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void registerMetricSourcingCallback(Metric.ID id, IMetricSourcingCallback iMetricSourcingCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (id != null) {
                        obtain.writeInt(1);
                        id.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iMetricSourcingCallback != null ? iMetricSourcingCallback.asBinder() : null);
                    this.mRemote.transact(5, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void registerProfileChangedCallback(IProfileChangedCallback iProfileChangedCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iProfileChangedCallback != null ? iProfileChangedCallback.asBinder() : null);
                    this.mRemote.transact(7, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void registerServiceChangedCallback(IServiceStateChangeCallback iServiceStateChangeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iServiceStateChangeCallback != null ? iServiceStateChangeCallback.asBinder() : null);
                    this.mRemote.transact(10, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public boolean shouldSubmitMetric(Metric.ID id) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (id != null) {
                        obtain.writeInt(1);
                        id.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void submitMetric(Metric metric) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (metric != null) {
                        obtain.writeInt(1);
                        metric.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(2, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void unregisterMetricQueryCallback(Metric.ID id, IMetricQueryCallback iMetricQueryCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (id != null) {
                        obtain.writeInt(1);
                        id.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iMetricQueryCallback != null ? iMetricQueryCallback.asBinder() : null);
                    this.mRemote.transact(4, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void unregisterMetricSourcingCallback(Metric.ID id, IMetricSourcingCallback iMetricSourcingCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (id != null) {
                        obtain.writeInt(1);
                        id.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iMetricSourcingCallback != null ? iMetricSourcingCallback.asBinder() : null);
                    this.mRemote.transact(6, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void unregisterProfileChangedCallback(IProfileChangedCallback iProfileChangedCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iProfileChangedCallback != null ? iProfileChangedCallback.asBinder() : null);
                    this.mRemote.transact(8, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void unregisterServiceChangedCallback(IServiceStateChangeCallback iServiceStateChangeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iServiceStateChangeCallback != null ? iServiceStateChangeCallback.asBinder() : null);
                    this.mRemote.transact(11, obtain, (Parcel) null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIQIBroker asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IIQIBroker)) ? new Proxy(iBinder) : (IIQIBroker) queryLocalInterface;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: com.att.iqi.lib.Metric$ID} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: com.att.iqi.lib.Metric$ID} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v11, resolved type: com.att.iqi.lib.Metric$ID} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v14, resolved type: com.att.iqi.lib.Metric$ID} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v17, resolved type: com.att.iqi.lib.Metric$ID} */
        /* JADX WARNING: type inference failed for: r2v1 */
        /* JADX WARNING: type inference failed for: r2v5, types: [com.att.iqi.lib.Metric] */
        /* JADX WARNING: type inference failed for: r2v20 */
        /* JADX WARNING: type inference failed for: r2v21 */
        /* JADX WARNING: type inference failed for: r2v22 */
        /* JADX WARNING: type inference failed for: r2v23 */
        /* JADX WARNING: type inference failed for: r2v24 */
        /* JADX WARNING: type inference failed for: r2v25 */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTransact(int r4, android.os.Parcel r5, android.os.Parcel r6, int r7) throws android.os.RemoteException {
            /*
                r3 = this;
                r0 = 1
                java.lang.String r1 = "com.att.iqi.IIQIBroker"
                r2 = 1598968902(0x5f4e5446, float:1.4867585E19)
                if (r4 == r2) goto L_0x0106
                r2 = 0
                switch(r4) {
                    case 1: goto L_0x00e9;
                    case 2: goto L_0x00d3;
                    case 3: goto L_0x00b5;
                    case 4: goto L_0x0097;
                    case 5: goto L_0x0079;
                    case 6: goto L_0x005b;
                    case 7: goto L_0x004c;
                    case 8: goto L_0x003d;
                    case 9: goto L_0x002f;
                    case 10: goto L_0x0020;
                    case 11: goto L_0x0011;
                    default: goto L_0x000c;
                }
            L_0x000c:
                boolean r4 = super.onTransact(r4, r5, r6, r7)
                return r4
            L_0x0011:
                r5.enforceInterface(r1)
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IServiceStateChangeCallback r4 = com.att.iqi.IServiceStateChangeCallback.Stub.asInterface(r4)
                r3.unregisterServiceChangedCallback(r4)
                return r0
            L_0x0020:
                r5.enforceInterface(r1)
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IServiceStateChangeCallback r4 = com.att.iqi.IServiceStateChangeCallback.Stub.asInterface(r4)
                r3.registerServiceChangedCallback(r4)
                return r0
            L_0x002f:
                r5.enforceInterface(r1)
                long r4 = r3.getTimestamp()
                r6.writeNoException()
                r6.writeLong(r4)
                return r0
            L_0x003d:
                r5.enforceInterface(r1)
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IProfileChangedCallback r4 = com.att.iqi.IProfileChangedCallback.Stub.asInterface(r4)
                r3.unregisterProfileChangedCallback(r4)
                return r0
            L_0x004c:
                r5.enforceInterface(r1)
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IProfileChangedCallback r4 = com.att.iqi.IProfileChangedCallback.Stub.asInterface(r4)
                r3.registerProfileChangedCallback(r4)
                return r0
            L_0x005b:
                r5.enforceInterface(r1)
                int r4 = r5.readInt()
                if (r4 == 0) goto L_0x006d
                android.os.Parcelable$Creator<com.att.iqi.lib.Metric$ID> r4 = com.att.iqi.lib.Metric.ID.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r5)
                r2 = r4
                com.att.iqi.lib.Metric$ID r2 = (com.att.iqi.lib.Metric.ID) r2
            L_0x006d:
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IMetricSourcingCallback r4 = com.att.iqi.IMetricSourcingCallback.Stub.asInterface(r4)
                r3.unregisterMetricSourcingCallback(r2, r4)
                return r0
            L_0x0079:
                r5.enforceInterface(r1)
                int r4 = r5.readInt()
                if (r4 == 0) goto L_0x008b
                android.os.Parcelable$Creator<com.att.iqi.lib.Metric$ID> r4 = com.att.iqi.lib.Metric.ID.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r5)
                r2 = r4
                com.att.iqi.lib.Metric$ID r2 = (com.att.iqi.lib.Metric.ID) r2
            L_0x008b:
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IMetricSourcingCallback r4 = com.att.iqi.IMetricSourcingCallback.Stub.asInterface(r4)
                r3.registerMetricSourcingCallback(r2, r4)
                return r0
            L_0x0097:
                r5.enforceInterface(r1)
                int r4 = r5.readInt()
                if (r4 == 0) goto L_0x00a9
                android.os.Parcelable$Creator<com.att.iqi.lib.Metric$ID> r4 = com.att.iqi.lib.Metric.ID.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r5)
                r2 = r4
                com.att.iqi.lib.Metric$ID r2 = (com.att.iqi.lib.Metric.ID) r2
            L_0x00a9:
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IMetricQueryCallback r4 = com.att.iqi.IMetricQueryCallback.Stub.asInterface(r4)
                r3.unregisterMetricQueryCallback(r2, r4)
                return r0
            L_0x00b5:
                r5.enforceInterface(r1)
                int r4 = r5.readInt()
                if (r4 == 0) goto L_0x00c7
                android.os.Parcelable$Creator<com.att.iqi.lib.Metric$ID> r4 = com.att.iqi.lib.Metric.ID.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r5)
                r2 = r4
                com.att.iqi.lib.Metric$ID r2 = (com.att.iqi.lib.Metric.ID) r2
            L_0x00c7:
                android.os.IBinder r4 = r5.readStrongBinder()
                com.att.iqi.IMetricQueryCallback r4 = com.att.iqi.IMetricQueryCallback.Stub.asInterface(r4)
                r3.registerMetricQueryCallback(r2, r4)
                return r0
            L_0x00d3:
                r5.enforceInterface(r1)
                int r4 = r5.readInt()
                if (r4 == 0) goto L_0x00e5
                android.os.Parcelable$Creator<com.att.iqi.lib.Metric> r4 = com.att.iqi.lib.Metric.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r5)
                r2 = r4
                com.att.iqi.lib.Metric r2 = (com.att.iqi.lib.Metric) r2
            L_0x00e5:
                r3.submitMetric(r2)
                return r0
            L_0x00e9:
                r5.enforceInterface(r1)
                int r4 = r5.readInt()
                if (r4 == 0) goto L_0x00fb
                android.os.Parcelable$Creator<com.att.iqi.lib.Metric$ID> r4 = com.att.iqi.lib.Metric.ID.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r5)
                r2 = r4
                com.att.iqi.lib.Metric$ID r2 = (com.att.iqi.lib.Metric.ID) r2
            L_0x00fb:
                boolean r4 = r3.shouldSubmitMetric(r2)
                r6.writeNoException()
                r6.writeInt(r4)
                return r0
            L_0x0106:
                r6.writeString(r1)
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.att.iqi.IIQIBroker.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    long getTimestamp() throws RemoteException;

    void registerMetricQueryCallback(Metric.ID id, IMetricQueryCallback iMetricQueryCallback) throws RemoteException;

    void registerMetricSourcingCallback(Metric.ID id, IMetricSourcingCallback iMetricSourcingCallback) throws RemoteException;

    void registerProfileChangedCallback(IProfileChangedCallback iProfileChangedCallback) throws RemoteException;

    void registerServiceChangedCallback(IServiceStateChangeCallback iServiceStateChangeCallback) throws RemoteException;

    boolean shouldSubmitMetric(Metric.ID id) throws RemoteException;

    void submitMetric(Metric metric) throws RemoteException;

    void unregisterMetricQueryCallback(Metric.ID id, IMetricQueryCallback iMetricQueryCallback) throws RemoteException;

    void unregisterMetricSourcingCallback(Metric.ID id, IMetricSourcingCallback iMetricSourcingCallback) throws RemoteException;

    void unregisterProfileChangedCallback(IProfileChangedCallback iProfileChangedCallback) throws RemoteException;

    void unregisterServiceChangedCallback(IServiceStateChangeCallback iServiceStateChangeCallback) throws RemoteException;
}
