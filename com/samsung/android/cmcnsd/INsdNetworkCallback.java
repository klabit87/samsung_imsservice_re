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
    void onWifiApConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException;

    void onWifiApNetworkMessageReceived(NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException;

    void onWifiDirectConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException;

    public static class Default implements INsdNetworkCallback {
        public void onWifiApConnectionChanged(NsdNetwork network) throws RemoteException {
        }

        public void onWifiDirectConnectionChanged(NsdNetwork network) throws RemoteException {
        }

        public void onWifiApNetworkMessageReceived(NsdNetworkCapabilities capabilities, NsdNetworkMessage message) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INsdNetworkCallback {
        private static final String DESCRIPTOR = "com.samsung.android.cmcnsd.INsdNetworkCallback";
        static final int TRANSACTION_onWifiApConnectionChanged = 1;
        static final int TRANSACTION_onWifiApNetworkMessageReceived = 3;
        static final int TRANSACTION_onWifiDirectConnectionChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INsdNetworkCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INsdNetworkCallback)) {
                return new Proxy(obj);
            }
            return (INsdNetworkCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NsdNetwork _arg0;
            NsdNetwork _arg02;
            NsdNetworkCapabilities _arg03;
            NsdNetworkMessage _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = NsdNetwork.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onWifiApConnectionChanged(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = NsdNetwork.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onWifiDirectConnectionChanged(_arg02);
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = NsdNetworkCapabilities.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = NsdNetworkMessage.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onWifiApNetworkMessageReceived(_arg03, _arg1);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements INsdNetworkCallback {
            public static INsdNetworkCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onWifiApConnectionChanged(NsdNetwork network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onWifiApConnectionChanged(network);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onWifiDirectConnectionChanged(NsdNetwork network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onWifiDirectConnectionChanged(network);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onWifiApNetworkMessageReceived(NsdNetworkCapabilities capabilities, NsdNetworkMessage message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (capabilities != null) {
                        _data.writeInt(1);
                        capabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (message != null) {
                        _data.writeInt(1);
                        message.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onWifiApNetworkMessageReceived(capabilities, message);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INsdNetworkCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INsdNetworkCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
