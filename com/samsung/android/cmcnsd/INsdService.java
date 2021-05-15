package com.samsung.android.cmcnsd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.samsung.android.cmcnsd.network.NsdNetworkCapabilities;
import com.samsung.android.cmcnsd.network.NsdNetworkMessage;

public interface INsdService extends IInterface {

    public static class Default implements INsdService {
        public boolean acquireNetwork(int i, NsdNetworkCapabilities nsdNetworkCapabilities) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }

        public boolean registerNetworkCallback(int i, INsdNetworkCallback iNsdNetworkCallback) throws RemoteException {
            return false;
        }

        public void releaseNetwork(int i) throws RemoteException {
        }

        public boolean sendNetworkMessage(int i, String str, NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException {
            return false;
        }

        public boolean unregisterNetworkCallback(INsdNetworkCallback iNsdNetworkCallback) throws RemoteException {
            return false;
        }
    }

    boolean acquireNetwork(int i, NsdNetworkCapabilities nsdNetworkCapabilities) throws RemoteException;

    boolean registerNetworkCallback(int i, INsdNetworkCallback iNsdNetworkCallback) throws RemoteException;

    void releaseNetwork(int i) throws RemoteException;

    boolean sendNetworkMessage(int i, String str, NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException;

    boolean unregisterNetworkCallback(INsdNetworkCallback iNsdNetworkCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements INsdService {
        public static final String DESCRIPTOR = "com.samsung.android.cmcnsd.INsdService";
        public static final int TRANSACTION_acquireNetwork = 3;
        public static final int TRANSACTION_registerNetworkCallback = 1;
        public static final int TRANSACTION_releaseNetwork = 4;
        public static final int TRANSACTION_sendNetworkMessage = 5;
        public static final int TRANSACTION_unregisterNetworkCallback = 2;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INsdService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof INsdService)) {
                return new Proxy(iBinder);
            }
            return (INsdService) queryLocalInterface;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: com.samsung.android.cmcnsd.network.NsdNetworkCapabilities} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: com.samsung.android.cmcnsd.network.NsdNetworkMessage} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v7 */
        /* JADX WARNING: type inference failed for: r3v8 */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTransact(int r5, android.os.Parcel r6, android.os.Parcel r7, int r8) throws android.os.RemoteException {
            /*
                r4 = this;
                r0 = 1
                java.lang.String r1 = "com.samsung.android.cmcnsd.INsdService"
                r2 = 1598968902(0x5f4e5446, float:1.4867585E19)
                if (r5 == r2) goto L_0x00b4
                if (r5 == r0) goto L_0x009a
                r2 = 2
                if (r5 == r2) goto L_0x0084
                r2 = 3
                r3 = 0
                if (r5 == r2) goto L_0x0061
                r2 = 4
                if (r5 == r2) goto L_0x0053
                r2 = 5
                if (r5 == r2) goto L_0x001c
                boolean r5 = super.onTransact(r5, r6, r7, r8)
                return r5
            L_0x001c:
                r6.enforceInterface(r1)
                int r5 = r6.readInt()
                java.lang.String r8 = r6.readString()
                int r1 = r6.readInt()
                if (r1 == 0) goto L_0x0036
                android.os.Parcelable$Creator<com.samsung.android.cmcnsd.network.NsdNetworkCapabilities> r1 = com.samsung.android.cmcnsd.network.NsdNetworkCapabilities.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r6)
                com.samsung.android.cmcnsd.network.NsdNetworkCapabilities r1 = (com.samsung.android.cmcnsd.network.NsdNetworkCapabilities) r1
                goto L_0x0037
            L_0x0036:
                r1 = r3
            L_0x0037:
                int r2 = r6.readInt()
                if (r2 == 0) goto L_0x0047
                android.os.Parcelable$Creator<com.samsung.android.cmcnsd.network.NsdNetworkMessage> r2 = com.samsung.android.cmcnsd.network.NsdNetworkMessage.CREATOR
                java.lang.Object r6 = r2.createFromParcel(r6)
                r3 = r6
                com.samsung.android.cmcnsd.network.NsdNetworkMessage r3 = (com.samsung.android.cmcnsd.network.NsdNetworkMessage) r3
                goto L_0x0048
            L_0x0047:
            L_0x0048:
                boolean r5 = r4.sendNetworkMessage(r5, r8, r1, r3)
                r7.writeNoException()
                r7.writeInt(r5)
                return r0
            L_0x0053:
                r6.enforceInterface(r1)
                int r5 = r6.readInt()
                r4.releaseNetwork(r5)
                r7.writeNoException()
                return r0
            L_0x0061:
                r6.enforceInterface(r1)
                int r5 = r6.readInt()
                int r8 = r6.readInt()
                if (r8 == 0) goto L_0x0078
                android.os.Parcelable$Creator<com.samsung.android.cmcnsd.network.NsdNetworkCapabilities> r8 = com.samsung.android.cmcnsd.network.NsdNetworkCapabilities.CREATOR
                java.lang.Object r6 = r8.createFromParcel(r6)
                r3 = r6
                com.samsung.android.cmcnsd.network.NsdNetworkCapabilities r3 = (com.samsung.android.cmcnsd.network.NsdNetworkCapabilities) r3
                goto L_0x0079
            L_0x0078:
            L_0x0079:
                boolean r5 = r4.acquireNetwork(r5, r3)
                r7.writeNoException()
                r7.writeInt(r5)
                return r0
            L_0x0084:
                r6.enforceInterface(r1)
                android.os.IBinder r5 = r6.readStrongBinder()
                com.samsung.android.cmcnsd.INsdNetworkCallback r5 = com.samsung.android.cmcnsd.INsdNetworkCallback.Stub.asInterface(r5)
                boolean r5 = r4.unregisterNetworkCallback(r5)
                r7.writeNoException()
                r7.writeInt(r5)
                return r0
            L_0x009a:
                r6.enforceInterface(r1)
                int r5 = r6.readInt()
                android.os.IBinder r6 = r6.readStrongBinder()
                com.samsung.android.cmcnsd.INsdNetworkCallback r6 = com.samsung.android.cmcnsd.INsdNetworkCallback.Stub.asInterface(r6)
                boolean r5 = r4.registerNetworkCallback(r5, r6)
                r7.writeNoException()
                r7.writeInt(r5)
                return r0
            L_0x00b4:
                r7.writeString(r1)
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.samsung.android.cmcnsd.INsdService.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }

        public static class Proxy implements INsdService {
            public static INsdService sDefaultImpl;
            public IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public boolean registerNetworkCallback(int i, INsdNetworkCallback iNsdNetworkCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeStrongBinder(iNsdNetworkCallback != null ? iNsdNetworkCallback.asBinder() : null);
                    boolean z = false;
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerNetworkCallback(i, iNsdNetworkCallback);
                    }
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean unregisterNetworkCallback(INsdNetworkCallback iNsdNetworkCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iNsdNetworkCallback != null ? iNsdNetworkCallback.asBinder() : null);
                    boolean z = false;
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterNetworkCallback(iNsdNetworkCallback);
                    }
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean acquireNetwork(int i, NsdNetworkCapabilities nsdNetworkCapabilities) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    boolean z = true;
                    if (nsdNetworkCapabilities != null) {
                        obtain.writeInt(1);
                        nsdNetworkCapabilities.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acquireNetwork(i, nsdNetworkCapabilities);
                    }
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void releaseNetwork(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (this.mRemote.transact(4, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                    } else {
                        Stub.getDefaultImpl().releaseNetwork(i);
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean sendNetworkMessage(int i, String str, NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    boolean z = true;
                    if (nsdNetworkCapabilities != null) {
                        obtain.writeInt(1);
                        nsdNetworkCapabilities.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (nsdNetworkMessage != null) {
                        obtain.writeInt(1);
                        nsdNetworkMessage.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (!this.mRemote.transact(5, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendNetworkMessage(i, str, nsdNetworkCapabilities, nsdNetworkMessage);
                    }
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INsdService iNsdService) {
            if (Proxy.sDefaultImpl != null || iNsdService == null) {
                return false;
            }
            Proxy.sDefaultImpl = iNsdService;
            return true;
        }

        public static INsdService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
