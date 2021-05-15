package com.sec.sve;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IImsMediaEventListener extends IInterface {
    void onAudioInjectionEnded(long j, long j2) throws RemoteException;

    void onAudioRtpRtcpTimeout(int i, int i2) throws RemoteException;

    void onDtmfEvent(int i, int i2) throws RemoteException;

    void onRecordEvent(int i, int i2) throws RemoteException;

    void onRecordingStopped(long j, long j2, String str) throws RemoteException;

    void onRtpLossRate(int i, int i2, float f, float f2) throws RemoteException;

    void onRtpStats(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    void onTextReceive(int i, int i2, String str, int i3, int i4) throws RemoteException;

    void onTextRtpRtcpTimeout(int i, int i2) throws RemoteException;

    void onVideoEvent(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    public static class Default implements IImsMediaEventListener {
        public void onAudioRtpRtcpTimeout(int channel, int event) throws RemoteException {
        }

        public void onRtpLossRate(int channel, int interval, float lossrate, float jitter) throws RemoteException {
        }

        public void onRtpStats(int channel, int lossData, int delay, int jitter, int measuredPeriod, int direction) throws RemoteException {
        }

        public void onVideoEvent(int result, int event, int sessionId, int arg1, int arg2) throws RemoteException {
        }

        public void onTextReceive(int ch, int sessionId, String text, int length, int event) throws RemoteException {
        }

        public void onTextRtpRtcpTimeout(int channel, int event) throws RemoteException {
        }

        public void onDtmfEvent(int channel, int dtmfKey) throws RemoteException {
        }

        public void onRecordEvent(int sessionId, int errCode) throws RemoteException {
        }

        public void onAudioInjectionEnded(long startTime, long stopTime) throws RemoteException {
        }

        public void onRecordingStopped(long startTime, long stopTime, String recordingFilePath) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsMediaEventListener {
        private static final String DESCRIPTOR = "com.sec.sve.IImsMediaEventListener";
        static final int TRANSACTION_onAudioInjectionEnded = 9;
        static final int TRANSACTION_onAudioRtpRtcpTimeout = 1;
        static final int TRANSACTION_onDtmfEvent = 7;
        static final int TRANSACTION_onRecordEvent = 8;
        static final int TRANSACTION_onRecordingStopped = 10;
        static final int TRANSACTION_onRtpLossRate = 2;
        static final int TRANSACTION_onRtpStats = 3;
        static final int TRANSACTION_onTextReceive = 5;
        static final int TRANSACTION_onTextRtpRtcpTimeout = 6;
        static final int TRANSACTION_onVideoEvent = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsMediaEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsMediaEventListener)) {
                return new Proxy(obj);
            }
            return (IImsMediaEventListener) iin;
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
                        onAudioRtpRtcpTimeout(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRtpLossRate(data.readInt(), data.readInt(), data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRtpStats(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        onVideoEvent(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        onTextReceive(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        onTextRtpRtcpTimeout(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        onDtmfEvent(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRecordEvent(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        onAudioInjectionEnded(data.readLong(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRecordingStopped(data.readLong(), data.readLong(), data.readString());
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

        private static class Proxy implements IImsMediaEventListener {
            public static IImsMediaEventListener sDefaultImpl;
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

            public void onAudioRtpRtcpTimeout(int channel, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(event);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAudioRtpRtcpTimeout(channel, event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRtpLossRate(int channel, int interval, float lossrate, float jitter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(interval);
                    _data.writeFloat(lossrate);
                    _data.writeFloat(jitter);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRtpLossRate(channel, interval, lossrate, jitter);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRtpStats(int channel, int lossData, int delay, int jitter, int measuredPeriod, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th) {
                        th = th;
                        int i = lossData;
                        int i2 = delay;
                        int i3 = jitter;
                        int i4 = measuredPeriod;
                        int i5 = direction;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(lossData);
                        try {
                            _data.writeInt(delay);
                            try {
                                _data.writeInt(jitter);
                            } catch (Throwable th2) {
                                th = th2;
                                int i42 = measuredPeriod;
                                int i52 = direction;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i32 = jitter;
                            int i422 = measuredPeriod;
                            int i522 = direction;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i22 = delay;
                        int i322 = jitter;
                        int i4222 = measuredPeriod;
                        int i5222 = direction;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(measuredPeriod);
                        try {
                            _data.writeInt(direction);
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onRtpStats(channel, lossData, delay, jitter, measuredPeriod, direction);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int i52222 = direction;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    int i6 = channel;
                    int i7 = lossData;
                    int i222 = delay;
                    int i3222 = jitter;
                    int i42222 = measuredPeriod;
                    int i522222 = direction;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void onVideoEvent(int result, int event, int sessionId, int arg1, int arg2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    _data.writeInt(event);
                    _data.writeInt(sessionId);
                    _data.writeInt(arg1);
                    _data.writeInt(arg2);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onVideoEvent(result, event, sessionId, arg1, arg2);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onTextReceive(int ch, int sessionId, String text, int length, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ch);
                    _data.writeInt(sessionId);
                    _data.writeString(text);
                    _data.writeInt(length);
                    _data.writeInt(event);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onTextReceive(ch, sessionId, text, length, event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onTextRtpRtcpTimeout(int channel, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(event);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onTextRtpRtcpTimeout(channel, event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onDtmfEvent(int channel, int dtmfKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(dtmfKey);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDtmfEvent(channel, dtmfKey);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRecordEvent(int sessionId, int errCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(errCode);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRecordEvent(sessionId, errCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAudioInjectionEnded(long startTime, long stopTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(startTime);
                    _data.writeLong(stopTime);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAudioInjectionEnded(startTime, stopTime);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRecordingStopped(long startTime, long stopTime, String recordingFilePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(startTime);
                    _data.writeLong(stopTime);
                    _data.writeString(recordingFilePath);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRecordingStopped(startTime, stopTime, recordingFilePath);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsMediaEventListener impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IImsMediaEventListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
