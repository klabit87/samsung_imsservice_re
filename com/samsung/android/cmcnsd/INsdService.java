package com.samsung.android.cmcnsd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.samsung.android.cmcnsd.INsdNetworkCallback;
import com.samsung.android.cmcnsd.network.NsdNetworkCapabilities;
import com.samsung.android.cmcnsd.network.NsdNetworkMessage;

public interface INsdService extends IInterface {
    boolean acquireNetwork(int i, NsdNetworkCapabilities nsdNetworkCapabilities) throws RemoteException;

    boolean registerNetworkCallback(int i, INsdNetworkCallback iNsdNetworkCallback) throws RemoteException;

    void releaseNetwork(int i) throws RemoteException;

    boolean sendNetworkMessage(int i, String str, NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException;

    boolean unregisterNetworkCallback(INsdNetworkCallback iNsdNetworkCallback) throws RemoteException;

    public static class Default implements INsdService {
        public boolean registerNetworkCallback(int hashCode, INsdNetworkCallback callback) throws RemoteException {
            return false;
        }

        public boolean unregisterNetworkCallback(INsdNetworkCallback callback) throws RemoteException {
            return false;
        }

        public boolean acquireNetwork(int hashCode, NsdNetworkCapabilities capabilities) throws RemoteException {
            return false;
        }

        public void releaseNetwork(int hashCode) throws RemoteException {
        }

        public boolean sendNetworkMessage(int hashCode, String deviceId, NsdNetworkCapabilities capabilities, NsdNetworkMessage message) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INsdService {
        private static final String DESCRIPTOR = "com.samsung.android.cmcnsd.INsdService";
        static final int TRANSACTION_acquireNetwork = 3;
        static final int TRANSACTION_registerNetworkCallback = 1;
        static final int TRANSACTION_releaseNetwork = 4;
        static final int TRANSACTION_sendNetworkMessage = 5;
        static final int TRANSACTION_unregisterNetworkCallback = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INsdService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INsdService)) {
                return new Proxy(obj);
            }
            return (INsdService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NsdNetworkCapabilities _arg1;
            NsdNetworkCapabilities _arg2;
            NsdNetworkMessage _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean _result = registerNetworkCallback(data.readInt(), INsdNetworkCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean _result2 = unregisterNetworkCallback(INsdNetworkCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = NsdNetworkCapabilities.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                boolean _result3 = acquireNetwork(_arg0, _arg1);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                releaseNetwork(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                String _arg12 = data.readString();
                if (data.readInt() != 0) {
                    _arg2 = NsdNetworkCapabilities.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                if (data.readInt() != 0) {
                    _arg3 = NsdNetworkMessage.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                boolean _result4 = sendNetworkMessage(_arg02, _arg12, _arg2, _arg3);
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements INsdService {
            public static INsdService sDefaultImpl;
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

            public boolean registerNetworkCallback(int hashCode, INsdNetworkCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hashCode);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean z = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerNetworkCallback(hashCode, callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unregisterNetworkCallback(INsdNetworkCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean z = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterNetworkCallback(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean acquireNetwork(int hashCode, NsdNetworkCapabilities capabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hashCode);
                    boolean _result = true;
                    if (capabilities != null) {
                        _data.writeInt(1);
                        capabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acquireNetwork(hashCode, capabilities);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releaseNetwork(int hashCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hashCode);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseNetwork(hashCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendNetworkMessage(int hashCode, String deviceId, NsdNetworkCapabilities capabilities, NsdNetworkMessage message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hashCode);
                    _data.writeString(deviceId);
                    boolean _result = true;
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
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendNetworkMessage(hashCode, deviceId, capabilities, message);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INsdService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INsdService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
