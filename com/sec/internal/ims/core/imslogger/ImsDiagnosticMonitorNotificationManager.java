package com.sec.internal.ims.core.imslogger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.volte2.data.DedicatedBearerEvent;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImsDiagnosticMonitorNotificationManager extends Handler implements ISequentialInitializable, ISignallingNotifier, IImsDiagMonitor {
    private static final String DATEFORMAT = "MM.dd HH:mm:ss";
    private static final int EVENT_CHECK_PACKAGE_STATUS = 3;
    private static final int EVENT_VOPS_CHANGED = 1;
    public static final String IMS_DEBUG_INFO_TIMESTAMP = "Timestamp";
    public static final String IMS_DEBUG_INFO_TYPE = "DebugInfoType";
    public static final String IMS_LOCAL_ADDRESS = "LocalAddr";
    public static final String IMS_REMOTE_ADDRESS = "RemoteAddr";
    public static final int IMS_SETTINGS_EVENT_CALL = 18;
    public static final int IMS_SETTINGS_EVENT_DBR = 11;
    public static final int IMS_SETTINGS_EVENT_REGI = 17;
    public static final int IMS_SETTINGS_EVENT_SIP = 1;
    public static final int IMS_SETTINGS_EVENT_VPOS = 5;
    public static final String IMS_SIP_DIRECTION = "Direction";
    public static final String IMS_SIP_MESSAGE = "SipMsg";
    public static final String IMS_SIP_TYPE = "SipType";
    public static final String IMS_VOLTE_VOPS_INDICATION = "VoPSIndication";
    private static final String LOG_TAG = "ImsDiagMonitorNotiMgr";
    private static final int VOLTE_DEDICATED_BEARER_NOTIFY_EVENT = 2;
    private ExternalSupporter mExternalSupporter;
    private int mPackageCheckCount = 0;
    private SparseArray<String> mPdnStateMap;

    public ImsDiagnosticMonitorNotificationManager(Context context, Looper looper) {
        super(looper);
        this.mExternalSupporter = new ExternalSupporter(context);
    }

    public void initSequentially() {
        SparseArray<String> sparseArray = new SparseArray<>();
        this.mPdnStateMap = sparseArray;
        sparseArray.put(0, "APN_ALREADY_ACTIVE");
        this.mPdnStateMap.put(1, "APN_REQUEST_STARTED");
        this.mPdnStateMap.put(2, "APN_TYPE_NOT_AVAILABLE");
        this.mPdnStateMap.put(3, "APN_REQUEST_FAILED");
        this.mPdnStateMap.put(4, "APN_ALREADY_INACTIVE");
        sendEmptyMessage(3);
    }

    public boolean send(Object o) {
        this.mExternalSupporter.send(o);
        return true;
    }

    private ISignallingNotifier.PackageStatus checkPackageStatus() {
        return this.mExternalSupporter.checkPackageStatus();
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int i = msg.what;
        if (i == 1) {
            handleUpdateVoPSIndication(((Boolean) ((AsyncResult) msg.obj).result).booleanValue());
        } else if (i == 2) {
            handleDedicatedBearerEvent((DedicatedBearerEvent) ((AsyncResult) msg.obj).result);
        } else if (i == 3) {
            this.mPackageCheckCount++;
            if (checkPackageStatus() != ISignallingNotifier.PackageStatus.NOT_INSTALLED) {
                this.mExternalSupporter.initialize();
            } else if (this.mPackageCheckCount < 10) {
                Log.i(LOG_TAG, "Package was not installed, check again #" + this.mPackageCheckCount);
                sendEmptyMessageDelayed(3, 1000);
            }
        }
    }

    public void onIndication(int notifyType, String message, int msgType, int direction, String timestamp, String localIp, String remoteIp, String hexContents) {
        onIndication(notifyType, message, msgType, direction, -1, timestamp, localIp, remoteIp, hexContents);
    }

    public void onIndication(int notifyType, String message, int msgType, int direction, int phoneId, String timestamp, String localIp, String remoteIp, String hexContents) {
        final int i = notifyType;
        final int i2 = msgType;
        final int i3 = direction;
        final int i4 = phoneId;
        final String str = timestamp;
        final String str2 = localIp;
        final String str3 = remoteIp;
        final String str4 = message;
        final String str5 = hexContents;
        post(new Runnable() {
            public void run() {
                int i = i;
                if (i == 0 || i == 1) {
                    Bundle b = new Bundle();
                    b.putInt("notifyType", i);
                    b.putInt("msgType", i2);
                    b.putInt("direction", i3);
                    b.putInt("phoneId", i4);
                    b.putString("timestamp", str);
                    b.putString("localIp", str2);
                    b.putString("remoteIp", str3);
                    b.putString("message", str4);
                    if (str5.length() > 0) {
                        b.putByteArray("hexcontents", ImsDiagnosticMonitorNotificationManager.hexStringToByteArray(str5));
                    }
                    ImsDiagnosticMonitorNotificationManager.this.send(b);
                }
            }
        });
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        int checklen = len % 2 == 1 ? len - 1 : len;
        for (int i = 0; i < checklen; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void handleUpdateVoPSIndication(boolean enabledVoPS) {
        Intent i = new Intent();
        i.putExtra(IMS_VOLTE_VOPS_INDICATION, enabledVoPS ? "1" : "0");
        send(5, i);
    }

    private void handleDedicatedBearerEvent(DedicatedBearerEvent dedicated) {
        Intent i = new Intent();
        i.putExtra("DedicatedBearerQosStatus", dedicated.getBearerState());
        i.putExtra("DedicatedBearerQosQCI", dedicated.getQci());
        send(11, i);
    }

    public void handleRegistrationEvent(ImsRegistration regInfo, boolean registered) {
        if (regInfo == null || regInfo.getImsProfile() == null) {
            Log.e(LOG_TAG, "regInfo is null");
            return;
        }
        Intent i = new Intent();
        i.putExtra("regState", registered);
        i.putExtra("profileName", regInfo.getImsProfile().getName());
        i.putExtra(GlobalSettingsConstants.Registration.EXTENDED_SERVICES, regInfo.getServices().toString());
        i.putExtra("cmcType", regInfo.getImsProfile().getCmcType());
        send(17, i);
    }

    public void notifyCallStatus(int sessionId, String callState, int callType, String audioCodecName) {
        Intent i = new Intent();
        i.putExtra("sessionId", sessionId);
        i.putExtra("callState", callState);
        i.putExtra("callType", callType);
        i.putExtra("audioCodec", audioCodecName);
        send(18, i);
    }

    private void send(int event, Intent i) {
        i.putExtra(IMS_DEBUG_INFO_TYPE, event);
        i.putExtra(IMS_DEBUG_INFO_TIMESTAMP, new SimpleDateFormat(DATEFORMAT, Locale.US).format(new Date()));
        send(i);
    }
}
