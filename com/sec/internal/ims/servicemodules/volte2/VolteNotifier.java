package com.sec.internal.ims.servicemodules.volte2;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.sec.ims.DialogEvent;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IRttEventListener;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IVolteServiceEventListener;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.ims.servicemodules.volte2.data.DedicatedBearerEvent;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VolteNotifier {
    public static final String LOG_TAG = VolteNotifier.class.getSimpleName();
    private final Map<Integer, RemoteCallbackList<IImsCallEventListener>> mCallStateListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<ISemCmcRecordingListener>> mCmcRecordingListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<IDialogEventListener>> mDialogEventListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<IVolteServiceEventListener>> mListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<IRttEventListener>> mRttEventListeners = new ConcurrentHashMap();

    public void registerForVolteServiceEvent(int phoneId, IVolteServiceEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "registerForVolteServiceEvent to phone#" + phoneId);
        if (!this.mListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        this.mListeners.get(Integer.valueOf(phoneId)).register(listener);
    }

    public void deRegisterForVolteServiceEvent(int phoneId, IVolteServiceEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "deRegisterForVolteServiceEvent to phone#" + phoneId);
        if (this.mListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mListeners.get(Integer.valueOf(phoneId)).unregister(listener);
        }
    }

    public void registerRttEventListener(int phoneId, IRttEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "registerRttEventListener to phone#" + phoneId);
        if (!this.mRttEventListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mRttEventListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        this.mRttEventListeners.get(Integer.valueOf(phoneId)).register(listener);
    }

    public void unregisterRttEventListener(int phoneId, IRttEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "unregisterRttEventListener to phone#" + phoneId);
        if (this.mRttEventListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mRttEventListeners.get(Integer.valueOf(phoneId)).unregister(listener);
        }
    }

    public void registerCmcRecordingListener(int phoneId, ISemCmcRecordingListener listener) {
        String str = LOG_TAG;
        Log.i(str, "registerCmcRecordingListener to phone#" + phoneId);
        if (!this.mCmcRecordingListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mCmcRecordingListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        this.mCmcRecordingListeners.get(Integer.valueOf(phoneId)).register(listener);
    }

    public void unregisterCmcRecordingListener(int phoneId, ISemCmcRecordingListener listener) {
        String str = LOG_TAG;
        Log.i(str, "unregisterCmcRecordingListener to phone#" + phoneId);
        if (this.mCmcRecordingListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mCmcRecordingListeners.get(Integer.valueOf(phoneId)).unregister(listener);
        }
    }

    public void registerDialogEventListener(int phoneId, IDialogEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "registerDialogEventListener to phone#" + phoneId);
        if (!this.mDialogEventListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mDialogEventListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        this.mDialogEventListeners.get(Integer.valueOf(phoneId)).register(listener);
    }

    public void unregisterDialogEventListener(int phoneId, IDialogEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "unregisterDialogEventListener to phone#" + phoneId);
        if (this.mDialogEventListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mDialogEventListeners.get(Integer.valueOf(phoneId)).unregister(listener);
        }
    }

    public void registerForCallStateEvent(int phoneId, IImsCallEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "registerForCallStateEvent to phone#" + phoneId);
        if (!this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mCallStateListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        this.mCallStateListeners.get(Integer.valueOf(phoneId)).register(listener);
    }

    public void deregisterForCallStateEvent(int phoneId, IImsCallEventListener listener) {
        String str = LOG_TAG;
        Log.i(str, "deregisterForCallStateEvent to phone#" + phoneId);
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).unregister(listener);
        }
    }

    public synchronized void notifyOnPulling(int phoneId, int pullingCallId) {
        if (this.mListeners.containsKey(Integer.valueOf(phoneId))) {
            RemoteCallbackList<IVolteServiceEventListener> listeners = this.mListeners.get(Integer.valueOf(phoneId));
            int length = listeners.beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    listeners.getBroadcastItem(i).onPullingCall(pullingCallId);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onPullingCall event!", e);
                }
            }
            listeners.finishBroadcast();
        }
    }

    public synchronized void notifyOnIncomingCall(int phoneId, int incomingCallId) {
        if (this.mListeners.containsKey(Integer.valueOf(phoneId))) {
            RemoteCallbackList<IVolteServiceEventListener> listeners = this.mListeners.get(Integer.valueOf(phoneId));
            int length = listeners.beginBroadcast();
            Log.i(LOG_TAG + '/' + phoneId, "onIncomingCall: listeners length = " + length);
            for (int i = 0; i < length; i++) {
                try {
                    listeners.getBroadcastItem(i).onIncomingCall(incomingCallId);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify incoming call event!", e);
                }
            }
            listeners.finishBroadcast();
        }
    }

    private ImsCallInfo makeImsCallInfo(ImsCallSession session) {
        return new ImsCallInfo(session.getCallId(), session.getCallProfile().getCallType(), session.getCallProfile().isDowngradedVideoCall(), session.getCallProfile().isDowngradedAtEstablish(), session.getDedicatedBearerState(1), session.getDedicatedBearerState(2), session.getDedicatedBearerState(8), session.getErrorCode(), session.getErrorMessage(), session.getCallProfile().getDialingNumber(), session.getCallProfile().getDirection(), session.getCallProfile().isConferenceCall());
    }

    public synchronized void notifyIncomingPreAlerting(ImsCallSession session) {
        int phoneId = session.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo callInfo = makeImsCallInfo(session);
            for (int i = 0; i < length; i++) {
                try {
                    this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onIncomingPreAlerting(callInfo, session.getCallProfile().getDialingNumber());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public synchronized void notifyIncomingCallEvent(ImsCallSession session) {
        int phoneId = session.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo callInfo = makeImsCallInfo(session);
            callInfo.setSamsungMdmnCall(session.getCallProfile().isSamsungMdmnCall());
            callInfo.setNumberPlus(session.getCallProfile().getNumberPlus());
            callInfo.setCmcDeviceId(session.getCallProfile().getCmcDeviceId());
            for (int i = 0; i < length; i++) {
                try {
                    this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onIncomingCall(callInfo, session.getCallProfile().getDialingNumber());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public synchronized void notifyCallStateEvent(CallStateEvent event, ImsCallSession session) {
        int phoneId = session.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo callInfo = makeImsCallInfo(session);
            callInfo.setSamsungMdmnCall(session.getCallProfile().isSamsungMdmnCall());
            callInfo.setCmcDeviceId(session.getCallProfile().getCmcDeviceId());
            for (int i = 0; i < length; i++) {
                IImsCallEventListener listener = this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i);
                try {
                    switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[event.getState().ordinal()]) {
                        case 1:
                            listener.onCallStarted(callInfo);
                            break;
                        case 2:
                            listener.onCallEstablished(callInfo);
                            break;
                        case 3:
                            listener.onCallModifyRequested(callInfo, session.getCallProfile().getCallType());
                            break;
                        case 4:
                            listener.onCallModified(callInfo);
                            break;
                        case 5:
                            if (event.getUpdatedParticipantsList().size() <= 0) {
                                break;
                            } else {
                                listener.onConferenceParticipantAdded(callInfo, event.getUpdatedParticipantsList().get(0).getUri());
                                break;
                            }
                        case 6:
                            if (event.getUpdatedParticipantsList().size() <= 0) {
                                break;
                            } else {
                                listener.onConferenceParticipantRemoved(callInfo, event.getUpdatedParticipantsList().get(0).getUri());
                                break;
                            }
                        case 7:
                            listener.onCallHeld(callInfo, true, false);
                            break;
                        case 8:
                            listener.onCallHeld(callInfo, false, true);
                            break;
                        case 9:
                            listener.onCallHeld(callInfo, true, true);
                            break;
                        case 10:
                        case 11:
                            listener.onCallEnded(callInfo, session.getErrorCode());
                            break;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.VolteNotifier$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;

        static {
            int[] iArr = new int[CallStateEvent.CALL_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = iArr;
            try {
                iArr[CallStateEvent.CALL_STATE.CALLING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ESTABLISHED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.MODIFY_REQUESTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.MODIFIED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_ADDED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_REMOVED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_LOCAL.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_REMOTE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_BOTH.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ENDED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ERROR.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
        }
    }

    public synchronized void onDedicatedBearerEvent(ImsCallSession session, DedicatedBearerEvent event) {
        int phoneId = session.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo callInfo = makeImsCallInfo(session);
            for (int i = 0; i < length; i++) {
                try {
                    this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onDedicatedBearerEvent(callInfo, event.getBearerState(), event.getQci());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00ad, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void notifyOnRtpLossRate(int r9, com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti r10) {
        /*
            r8 = this;
            monitor-enter(r8)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00b1 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b1 }
            r1.<init>()     // Catch:{ all -> 0x00b1 }
            java.lang.String r2 = "onRtpLossRateNoti: interval"
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            int r2 = r10.getInterval()     // Catch:{ all -> 0x00b1 }
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            java.lang.String r2 = " : LossRate"
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            float r2 = r10.getLossRate()     // Catch:{ all -> 0x00b1 }
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            java.lang.String r2 = " : Jitter "
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            float r2 = r10.getJitter()     // Catch:{ all -> 0x00b1 }
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            java.lang.String r2 = " : Notification"
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            int r2 = r10.getNotification()     // Catch:{ all -> 0x00b1 }
            r1.append(r2)     // Catch:{ all -> 0x00b1 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00b1 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00b1 }
            java.util.Map<java.lang.Integer, android.os.RemoteCallbackList<com.sec.ims.volte2.IImsCallEventListener>> r0 = r8.mCallStateListeners     // Catch:{ all -> 0x00b1 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x00b1 }
            boolean r0 = r0.containsKey(r1)     // Catch:{ all -> 0x00b1 }
            if (r0 == 0) goto L_0x00af
            java.util.Map<java.lang.Integer, android.os.RemoteCallbackList<com.sec.ims.volte2.IImsCallEventListener>> r0 = r8.mCallStateListeners     // Catch:{ all -> 0x00b1 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x00b1 }
            java.lang.Object r0 = r0.get(r1)     // Catch:{ all -> 0x00b1 }
            android.os.RemoteCallbackList r0 = (android.os.RemoteCallbackList) r0     // Catch:{ all -> 0x00b1 }
            monitor-enter(r0)     // Catch:{ all -> 0x00b1 }
            java.util.Map<java.lang.Integer, android.os.RemoteCallbackList<com.sec.ims.volte2.IImsCallEventListener>> r1 = r8.mCallStateListeners     // Catch:{ all -> 0x00aa }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x00aa }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x00aa }
            android.os.RemoteCallbackList r1 = (android.os.RemoteCallbackList) r1     // Catch:{ all -> 0x00aa }
            int r1 = r1.beginBroadcast()     // Catch:{ all -> 0x00aa }
            r2 = 0
        L_0x006a:
            if (r2 >= r1) goto L_0x0099
            java.util.Map<java.lang.Integer, android.os.RemoteCallbackList<com.sec.ims.volte2.IImsCallEventListener>> r3 = r8.mCallStateListeners     // Catch:{ all -> 0x00aa }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x00aa }
            java.lang.Object r3 = r3.get(r4)     // Catch:{ all -> 0x00aa }
            android.os.RemoteCallbackList r3 = (android.os.RemoteCallbackList) r3     // Catch:{ all -> 0x00aa }
            android.os.IInterface r3 = r3.getBroadcastItem(r2)     // Catch:{ all -> 0x00aa }
            com.sec.ims.volte2.IImsCallEventListener r3 = (com.sec.ims.volte2.IImsCallEventListener) r3     // Catch:{ all -> 0x00aa }
            int r4 = r10.getInterval()     // Catch:{ RemoteException -> 0x0092 }
            float r5 = r10.getLossRate()     // Catch:{ RemoteException -> 0x0092 }
            float r6 = r10.getJitter()     // Catch:{ RemoteException -> 0x0092 }
            int r7 = r10.getNotification()     // Catch:{ RemoteException -> 0x0092 }
            r3.onRtpLossRateNoti(r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0092 }
            goto L_0x0096
        L_0x0092:
            r4 = move-exception
            r4.printStackTrace()     // Catch:{ all -> 0x00aa }
        L_0x0096:
            int r2 = r2 + 1
            goto L_0x006a
        L_0x0099:
            java.util.Map<java.lang.Integer, android.os.RemoteCallbackList<com.sec.ims.volte2.IImsCallEventListener>> r2 = r8.mCallStateListeners     // Catch:{ all -> 0x00aa }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x00aa }
            java.lang.Object r2 = r2.get(r3)     // Catch:{ all -> 0x00aa }
            android.os.RemoteCallbackList r2 = (android.os.RemoteCallbackList) r2     // Catch:{ all -> 0x00aa }
            r2.finishBroadcast()     // Catch:{ all -> 0x00aa }
            monitor-exit(r0)     // Catch:{ all -> 0x00aa }
            goto L_0x00af
        L_0x00aa:
            r1 = move-exception
        L_0x00ab:
            monitor-exit(r0)     // Catch:{ all -> 0x00ad }
            throw r1     // Catch:{ all -> 0x00b1 }
        L_0x00ad:
            r1 = move-exception
            goto L_0x00ab
        L_0x00af:
            monitor-exit(r8)
            return
        L_0x00b1:
            r9 = move-exception
            monitor-exit(r8)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteNotifier.notifyOnRtpLossRate(int, com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti):void");
    }

    public synchronized void notifyOnCmcRecordingEvent(int phoneId, int event, int extra) {
        if (this.mCmcRecordingListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mCmcRecordingListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            for (int i = 0; i < length; i++) {
                ISemCmcRecordingListener listener = this.mCmcRecordingListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i);
                if (event >= 800) {
                    try {
                        listener.onInfo(event, extra);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "failed notify cmc recording event!", e);
                    }
                } else if (event > 0 && event < 700) {
                    listener.onError(event, extra);
                }
            }
            this.mCmcRecordingListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public synchronized void notifyOnDialogEvent(DialogEvent de) {
        if (this.mDialogEventListeners.containsKey(Integer.valueOf(de.getPhoneId()))) {
            int length = this.mDialogEventListeners.get(Integer.valueOf(de.getPhoneId())).beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mDialogEventListeners.get(Integer.valueOf(de.getPhoneId())).getBroadcastItem(i).onDialogEvent(de);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify dialog event!", e);
                }
            }
            this.mDialogEventListeners.get(Integer.valueOf(de.getPhoneId())).finishBroadcast();
        }
    }

    public void onSendRttSessionModifyRequest(int phoneId, ImsCallSession inCallSession, boolean mode) {
        if (inCallSession != null && this.mRttEventListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mRttEventListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            IMSLog.c(LogClass.VOLTE_RECV_REQUEST_RTT, phoneId + "," + inCallSession.getSessionId() + "," + mode);
            String str = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("onSendRttSessionModifyRequest : mode = ");
            sb.append(mode);
            Log.i(str, sb.toString());
            for (int i = 0; i < length; i++) {
                try {
                    this.mRttEventListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onSendRttSessionModifyRequest(inCallSession.getCallId(), mode);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onSendRttSessionModifyRequest!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public void onSendRttSessionModifyResponse(int phoneId, ImsCallSession inCallSession, boolean mode, boolean result) {
        if (inCallSession != null && this.mRttEventListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mRttEventListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            IMSLog.c(LogClass.VOLTE_RECV_RESPONSE_RTT, phoneId + "," + inCallSession.getSessionId() + "," + mode + "," + result);
            String str = LOG_TAG;
            Log.i(str, "onSendRttSessionModifyResponse : mode = " + mode + ", result = " + result + ", Listeners length : " + length);
            for (int i = 0; i < length; i++) {
                try {
                    IRttEventListener listener = this.mRttEventListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i);
                    if (listener != null) {
                        listener.onSendRttSessionModifyResponse(inCallSession.getCallId(), mode, result);
                    }
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onSendRttSessionModifyResponse!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public void notifyOnRttEventBySession(int phoneId, TextInfo textInfo) {
        if (this.mRttEventListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mRttEventListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            String str = LOG_TAG;
            Log.i(str, "notifyOnRttEventBySession : getText = " + textInfo.getText() + " , len : " + textInfo.getTextLen() + ", SessionId : " + textInfo.getSessionId() + ", Listeners length : " + length);
            for (int i = 0; i < length; i++) {
                try {
                    this.mRttEventListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onRttEventBySession(textInfo.getSessionId(), textInfo.getText());
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onTextReceived event!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public void notifyOnRttEvent(int phoneId, TextInfo textInfo) {
        if (this.mRttEventListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mRttEventListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            String str = LOG_TAG;
            Log.i(str, "notifyOnRttEvent : getText = " + textInfo.getText() + " , len : " + textInfo.getTextLen() + ", Listeners length : " + length);
            for (int i = 0; i < length; i++) {
                try {
                    this.mRttEventListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onRttEvent(textInfo.getText());
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onTextInfo event!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }
}
