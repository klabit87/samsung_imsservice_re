package com.sec.sve;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICmcMediaEventListener extends IInterface {
    void onCmcRecordEvent(int i, int i2, int i3) throws RemoteException;

    void onCmcRecorderStoppedEvent(int i, int i2, String str) throws RemoteException;

    void onRelayChannelEvent(int i, int i2) throws RemoteException;

    void onRelayEvent(int i, int i2) throws RemoteException;

    void onRelayRtpStats(int i, int i2, int i3, int i4, int i5, int i6, int i7) throws RemoteException;

    void onRelayStreamEvent(int i, int i2, int i3) throws RemoteException;

    public static class Default implements ICmcMediaEventListener {
        public void onRelayEvent(int streamId, int event) throws RemoteException {
        }

        public void onRelayStreamEvent(int streamId, int event, int sessionId) throws RemoteException {
        }

        public void onRelayRtpStats(int streamId, int sessionId, int lossData, int delay, int jitter, int measuredPeriod, int direction) throws RemoteException {
        }

        public void onRelayChannelEvent(int channel, int event) throws RemoteException {
        }

        public void onCmcRecordEvent(int sessionId, int event, int arg) throws RemoteException {
        }

        public void onCmcRecorderStoppedEvent(int startTime, int stopTime, String filePath) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICmcMediaEventListener {
        private static final String DESCRIPTOR = "com.sec.sve.ICmcMediaEventListener";
        static final int TRANSACTION_onCmcRecordEvent = 5;
        static final int TRANSACTION_onCmcRecorderStoppedEvent = 6;
        static final int TRANSACTION_onRelayChannelEvent = 4;
        static final int TRANSACTION_onRelayEvent = 1;
        static final int TRANSACTION_onRelayRtpStats = 3;
        static final int TRANSACTION_onRelayStreamEvent = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICmcMediaEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICmcMediaEventListener)) {
                return new Proxy(obj);
            }
            return (ICmcMediaEventListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRelayEvent(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRelayStreamEvent(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRelayRtpStats(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRelayChannelEvent(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        onCmcRecordEvent(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        onCmcRecorderStoppedEvent(data.readInt(), data.readInt(), data.readString());
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

        private static class Proxy implements ICmcMediaEventListener {
            public static ICmcMediaEventListener sDefaultImpl;
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

            public void onRelayEvent(int streamId, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    _data.writeInt(event);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRelayEvent(streamId, event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRelayStreamEvent(int streamId, int event, int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    _data.writeInt(event);
                    _data.writeInt(sessionId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRelayStreamEvent(streamId, event, sessionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRelayRtpStats(int streamId, int sessionId, int lossData, int delay, int jitter, int measuredPeriod, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(streamId);
                    } catch (Throwable th) {
                        th = th;
                        int i = sessionId;
                        int i2 = lossData;
                        int i3 = delay;
                        int i4 = jitter;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(sessionId);
                    } catch (Throwable th2) {
                        th = th2;
                        int i22 = lossData;
                        int i32 = delay;
                        int i42 = jitter;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(lossData);
                        try {
                            _data.writeInt(delay);
                        } catch (Throwable th3) {
                            th = th3;
                            int i422 = jitter;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(jitter);
                            _data.writeInt(measuredPeriod);
                            _data.writeInt(direction);
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onRelayRtpStats(streamId, sessionId, lossData, delay, jitter, measuredPeriod, direction);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        int i322 = delay;
                        int i4222 = jitter;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    int i5 = streamId;
                    int i6 = sessionId;
                    int i222 = lossData;
                    int i3222 = delay;
                    int i42222 = jitter;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void onRelayChannelEvent(int channel, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(event);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRelayChannelEvent(channel, event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCmcRecordEvent(int sessionId, int event, int arg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(event);
                    _data.writeInt(arg);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCmcRecordEvent(sessionId, event, arg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCmcRecorderStoppedEvent(int startTime, int stopTime, String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startTime);
                    _data.writeInt(stopTime);
                    _data.writeString(filePath);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCmcRecorderStoppedEvent(startTime, stopTime, filePath);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICmcMediaEventListener impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static ICmcMediaEventListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
