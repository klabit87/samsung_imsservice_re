package com.samsung.android.cmcnsd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.samsung.android.cmcnsd.network.NsdNetwork;
import com.samsung.android.cmcnsd.network.NsdNetworkCapabilities;
import com.samsung.android.cmcnsd.network.NsdNetworkMessage;

public interface INsdNetworkCallback extends IInterface {

    public static class Default implements INsdNetworkCallback {
        public IBinder asBinder() {
            return null;
        }

        public void onWifiApConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException {
        }

        public void onWifiApNetworkMessageReceived(NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException {
        }

        public void onWifiDirectConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException {
        }
    }

    void onWifiApConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException;

    void onWifiApNetworkMessageReceived(NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException;

    void onWifiDirectConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException;

    public static abstract class Stub extends Binder implements INsdNetworkCallback {
        public static final String DESCRIPTOR = "com.samsung.android.cmcnsd.INsdNetworkCallback";
        public static final int TRANSACTION_onWifiApConnectionChanged = 1;
        public static final int TRANSACTION_onWifiApNetworkMessageReceived = 3;
        public static final int TRANSACTION_onWifiDirectConnectionChanged = 2;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INsdNetworkCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof INsdNetworkCallback)) {
                return new Proxy(iBinder);
            }
            return (INsdNetworkCallback) queryLocalInterface;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: com.samsung.android.cmcnsd.network.NsdNetwork} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: com.samsung.android.cmcnsd.network.NsdNetwork} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: com.samsung.android.cmcnsd.network.NsdNetworkMessage} */
        /* JADX WARNING: type inference failed for: r0v0 */
        /* JADX WARNING: type inference failed for: r0v10 */
        /* JADX WARNING: type inference failed for: r0v11 */
        /* JADX WARNING: type inference failed for: r0v12 */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTransact(int r5, android.os.Parcel r6, android.os.Parcel r7, int r8) throws android.os.RemoteException {
            /*
                r4 = this;
                r0 = 0
                r1 = 1
                java.lang.String r2 = "com.samsung.android.cmcnsd.INsdNetworkCallback"
                if (r5 == r1) goto L_0x0060
                r3 = 2
                if (r5 == r3) goto L_0x0045
                r3 = 3
                if (r5 == r3) goto L_0x001a
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                if (r5 == r0) goto L_0x0016
                boolean r5 = super.onTransact(r5, r6, r7, r8)
                return r5
            L_0x0016:
                r7.writeString(r2)
                return r1
            L_0x001a:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x002c
                android.os.Parcelable$Creator<com.samsung.android.cmcnsd.network.NsdNetworkCapabilities> r5 = com.samsung.android.cmcnsd.network.NsdNetworkCapabilities.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r6)
                com.samsung.android.cmcnsd.network.NsdNetworkCapabilities r5 = (com.samsung.android.cmcnsd.network.NsdNetworkCapabilities) r5
                goto L_0x002d
            L_0x002c:
                r5 = r0
            L_0x002d:
                int r8 = r6.readInt()
                if (r8 == 0) goto L_0x003d
                android.os.Parcelable$Creator<com.samsung.android.cmcnsd.network.NsdNetworkMessage> r8 = com.samsung.android.cmcnsd.network.NsdNetworkMessage.CREATOR
                java.lang.Object r6 = r8.createFromParcel(r6)
                r0 = r6
                com.samsung.android.cmcnsd.network.NsdNetworkMessage r0 = (com.samsung.android.cmcnsd.network.NsdNetworkMessage) r0
                goto L_0x003e
            L_0x003d:
            L_0x003e:
                r4.onWifiApNetworkMessageReceived(r5, r0)
                r7.writeNoException()
                return r1
            L_0x0045:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x0058
                android.os.Parcelable$Creator<com.samsung.android.cmcnsd.network.NsdNetwork> r5 = com.samsung.android.cmcnsd.network.NsdNetwork.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r6)
                r0 = r5
                com.samsung.android.cmcnsd.network.NsdNetwork r0 = (com.samsung.android.cmcnsd.network.NsdNetwork) r0
                goto L_0x0059
            L_0x0058:
            L_0x0059:
                r4.onWifiDirectConnectionChanged(r0)
                r7.writeNoException()
                return r1
            L_0x0060:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x0073
                android.os.Parcelable$Creator<com.samsung.android.cmcnsd.network.NsdNetwork> r5 = com.samsung.android.cmcnsd.network.NsdNetwork.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r6)
                r0 = r5
                com.samsung.android.cmcnsd.network.NsdNetwork r0 = (com.samsung.android.cmcnsd.network.NsdNetwork) r0
                goto L_0x0074
            L_0x0073:
            L_0x0074:
                r4.onWifiApConnectionChanged(r0)
                r7.writeNoException()
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.samsung.android.cmcnsd.INsdNetworkCallback.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }

        public static class Proxy implements INsdNetworkCallback {
            public static INsdNetworkCallback sDefaultImpl;
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

            public void onWifiApConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nsdNetwork != null) {
                        obtain.writeInt(1);
                        nsdNetwork.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(1, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                    } else {
                        Stub.getDefaultImpl().onWifiApConnectionChanged(nsdNetwork);
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onWifiDirectConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nsdNetwork != null) {
                        obtain.writeInt(1);
                        nsdNetwork.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(2, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                    } else {
                        Stub.getDefaultImpl().onWifiDirectConnectionChanged(nsdNetwork);
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onWifiApNetworkMessageReceived(NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    if (this.mRemote.transact(3, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                    } else {
                        Stub.getDefaultImpl().onWifiApNetworkMessageReceived(nsdNetworkCapabilities, nsdNetworkMessage);
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INsdNetworkCallback iNsdNetworkCallback) {
            if (Proxy.sDefaultImpl != null || iNsdNetworkCallback == null) {
                return false;
            }
            Proxy.sDefaultImpl = iNsdNetworkCallback;
            return true;
        }

        public static INsdNetworkCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
