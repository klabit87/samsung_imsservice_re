package com.sec.internal.ims.csh;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;

public interface IVshRemoteClient extends IInterface {
    int closeVshSource(long j, Surface surface, boolean z) throws RemoteException;

    int openVshSource(long j, Surface surface, int i, int i2, int i3, int i4) throws RemoteException;

    int setOrientationListenerType(int i, int i2) throws RemoteException;

    public static class Default implements IVshRemoteClient {
        public int openVshSource(long videoShareId, Surface surface, int width, int height, int orientation, int color) throws RemoteException {
            return 0;
        }

        public int closeVshSource(long videoShareId, Surface surface, boolean endShare) throws RemoteException {
            return 0;
        }

        public int setOrientationListenerType(int type, int orientation) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVshRemoteClient {
        private static final String DESCRIPTOR = "com.sec.internal.ims.csh.IVshRemoteClient";
        static final int TRANSACTION_closeVshSource = 2;
        static final int TRANSACTION_openVshSource = 1;
        static final int TRANSACTION_setOrientationListenerType = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVshRemoteClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVshRemoteClient)) {
                return new Proxy(obj);
            }
            return (IVshRemoteClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Surface _arg1;
            Surface _arg12;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                long _arg0 = data.readLong();
                if (data.readInt() != 0) {
                    _arg1 = (Surface) Surface.CREATOR.createFromParcel(parcel);
                } else {
                    _arg1 = null;
                }
                int _result = openVshSource(_arg0, _arg1, data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                parcel2.writeInt(_result);
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                long _arg02 = data.readLong();
                if (data.readInt() != 0) {
                    _arg12 = (Surface) Surface.CREATOR.createFromParcel(parcel);
                } else {
                    _arg12 = null;
                }
                int _result2 = closeVshSource(_arg02, _arg12, data.readInt() != 0);
                reply.writeNoException();
                parcel2.writeInt(_result2);
                return true;
            } else if (i == 3) {
                parcel.enforceInterface(DESCRIPTOR);
                int _result3 = setOrientationListenerType(data.readInt(), data.readInt());
                reply.writeNoException();
                parcel2.writeInt(_result3);
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IVshRemoteClient {
            public static IVshRemoteClient sDefaultImpl;
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

            public int openVshSource(long videoShareId, Surface surface, int width, int height, int orientation, int color) throws RemoteException {
                Surface surface2 = surface;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(videoShareId);
                        if (surface2 != null) {
                            _data.writeInt(1);
                            surface2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(width);
                        } catch (Throwable th) {
                            th = th;
                            int i = height;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        int i2 = width;
                        int i3 = height;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(height);
                        _data.writeInt(orientation);
                        _data.writeInt(color);
                        if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int openVshSource = Stub.getDefaultImpl().openVshSource(videoShareId, surface, width, height, orientation, color);
                        _reply.recycle();
                        _data.recycle();
                        return openVshSource;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    long j = videoShareId;
                    int i22 = width;
                    int i32 = height;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int closeVshSource(long videoShareId, Surface surface, boolean endShare) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(videoShareId);
                    int i = 1;
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!endShare) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().closeVshSource(videoShareId, surface, endShare);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setOrientationListenerType(int type, int orientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(orientation);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setOrientationListenerType(type, orientation);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVshRemoteClient impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IVshRemoteClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
