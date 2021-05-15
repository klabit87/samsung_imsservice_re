package com.msc.sa.aidl;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISACallback extends IInterface {
    void onReceiveAccessToken(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveAuthCode(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveChecklistValidation(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveDisclaimerAgreement(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceivePasswordConfirmation(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveSCloudAccessToken(int i, boolean z, Bundle bundle) throws RemoteException;

    public static class Default implements ISACallback {
        public void onReceiveAccessToken(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceiveChecklistValidation(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceiveDisclaimerAgreement(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceiveAuthCode(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceiveSCloudAccessToken(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceivePasswordConfirmation(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISACallback {
        private static final String DESCRIPTOR = "com.msc.sa.aidl.ISACallback";
        static final int TRANSACTION_onReceiveAccessToken = 1;
        static final int TRANSACTION_onReceiveAuthCode = 4;
        static final int TRANSACTION_onReceiveChecklistValidation = 2;
        static final int TRANSACTION_onReceiveDisclaimerAgreement = 3;
        static final int TRANSACTION_onReceivePasswordConfirmation = 6;
        static final int TRANSACTION_onReceiveSCloudAccessToken = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISACallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISACallback)) {
                return new Proxy(obj);
            }
            return (ISACallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            Bundle _arg22;
            Bundle _arg23;
            Bundle _arg24;
            Bundle _arg25;
            Bundle _arg26;
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        onReceiveAccessToken(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        onReceiveChecklistValidation(_arg02, _arg1, _arg22);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        onReceiveDisclaimerAgreement(_arg03, _arg1, _arg23);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg24 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        onReceiveAuthCode(_arg04, _arg1, _arg24);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg25 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        onReceiveSCloudAccessToken(_arg05, _arg1, _arg25);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg26 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        onReceivePasswordConfirmation(_arg06, _arg1, _arg26);
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ISACallback {
            public static ISACallback sDefaultImpl;
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

            public void onReceiveAccessToken(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestID);
                    _data.writeInt(isSuccess ? 1 : 0);
                    if (resultData != null) {
                        _data.writeInt(1);
                        resultData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveAccessToken(requestID, isSuccess, resultData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onReceiveChecklistValidation(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestID);
                    _data.writeInt(isSuccess ? 1 : 0);
                    if (resultData != null) {
                        _data.writeInt(1);
                        resultData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveChecklistValidation(requestID, isSuccess, resultData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onReceiveDisclaimerAgreement(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestID);
                    _data.writeInt(isSuccess ? 1 : 0);
                    if (resultData != null) {
                        _data.writeInt(1);
                        resultData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveDisclaimerAgreement(requestID, isSuccess, resultData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onReceiveAuthCode(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestID);
                    _data.writeInt(isSuccess ? 1 : 0);
                    if (resultData != null) {
                        _data.writeInt(1);
                        resultData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveAuthCode(requestID, isSuccess, resultData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onReceiveSCloudAccessToken(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestID);
                    _data.writeInt(isSuccess ? 1 : 0);
                    if (resultData != null) {
                        _data.writeInt(1);
                        resultData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveSCloudAccessToken(requestID, isSuccess, resultData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onReceivePasswordConfirmation(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestID);
                    _data.writeInt(isSuccess ? 1 : 0);
                    if (resultData != null) {
                        _data.writeInt(1);
                        resultData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceivePasswordConfirmation(requestID, isSuccess, resultData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISACallback impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static ISACallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
