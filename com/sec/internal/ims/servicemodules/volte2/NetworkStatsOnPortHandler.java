package com.sec.internal.ims.servicemodules.volte2;

import android.net.NetworkStats;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.sec.internal.constants.Mno;

public class NetworkStatsOnPortHandler extends Handler {
    private static final String LOG_TAG = "NetworkStatsOnPortHandler";
    public static final int START = 1;
    public static final int STOP = 2;
    private String mIface = "";
    private int mLocalVideoRtcp = 0;
    private int mLocalVideoRtp = 0;
    Mno mMno = Mno.DEFAULT;
    private int mPhoneId = 0;
    private int mRemoteVideoRtcp = 0;
    private int mRemoteVideoRtp = 0;
    private boolean mReportingNetworkStatsOnPort = false;

    public NetworkStatsOnPortHandler(int phoneId, Mno mno, Looper looper) {
        super(looper);
        this.mMno = mno;
        this.mPhoneId = phoneId;
    }

    private void start() {
        Log.i(LOG_TAG, "NetworkStatsOnPort Start");
        if (this.mMno == Mno.ROGERS || this.mMno == Mno.DOCOMO || this.mMno.isChn() || this.mMno.isHkMo() || this.mMno.isKor()) {
            Log.i(LOG_TAG, "skip startNetworkStatsOnPorts. (vendor req)");
        } else if (this.mReportingNetworkStatsOnPort) {
            Log.i(LOG_TAG, "startNetworkStatsOnPorts: already triggered, ignore");
        } else {
            try {
                if (this.mLocalVideoRtp != 0) {
                    if (this.mRemoteVideoRtp != 0) {
                        Log.i(LOG_TAG, "startNetworkStatsOnPorts: LocalVideoRtpPort(" + this.mLocalVideoRtp + ") RemoteVideoRtpPort(" + this.mRemoteVideoRtp + ")");
                        startNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtp, this.mRemoteVideoRtp);
                    }
                }
                if (!(this.mLocalVideoRtcp == 0 || this.mRemoteVideoRtcp == 0)) {
                    Log.i(LOG_TAG, "startNetworkStatsOnPorts: LocalVideoRtcpPort(" + this.mLocalVideoRtcp + ") RemoteVideoRtcpPort(" + this.mRemoteVideoRtcp + ")");
                    startNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtcp, this.mRemoteVideoRtcp);
                }
                this.mReportingNetworkStatsOnPort = true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void startNetworkStatsOnPorts(String iface, int sPort, int dPort) {
        INetworkManagementService sNwMgrService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        if (sNwMgrService != null) {
            try {
                sNwMgrService.startNetworkStatsOnPorts(iface, sPort, dPort);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopNetworkStatsOnPorts(String iface, int sPort, int dPort) {
        INetworkManagementService sNwMgrService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        if (sNwMgrService != null) {
            try {
                sNwMgrService.stopNetworkStatsOnPorts(iface, sPort, dPort);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        Log.i(LOG_TAG, "NetworkStatsOnPort Stop");
        if (this.mMno == Mno.ROGERS || this.mMno == Mno.VTR) {
            Log.i(LOG_TAG, "skip stopNetworkStatsOnPorts. (vendor req)");
        } else if (!this.mReportingNetworkStatsOnPort) {
            Log.i(LOG_TAG, "stopNetworkStatsOnPorts - startNetworkStatsOnPorts not called, ignore");
        } else {
            try {
                if (this.mLocalVideoRtp != 0) {
                    if (this.mRemoteVideoRtp != 0) {
                        Log.i(LOG_TAG, "stopNetworkStatsOnPorts: LocalVideoRtpPort(" + this.mLocalVideoRtp + ") RemoteVideoRtpPort(" + this.mRemoteVideoRtp + ")");
                        stopNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtp, this.mRemoteVideoRtp);
                    }
                }
                if (!(this.mLocalVideoRtcp == 0 || this.mRemoteVideoRtcp == 0)) {
                    Log.i(LOG_TAG, "stopNetworkStatsOnPorts: LocalVideoRtcpPort(" + this.mLocalVideoRtcp + ") RemoteVideoRtcpPort(" + this.mRemoteVideoRtcp + ")");
                    stopNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtcp, this.mRemoteVideoRtcp);
                }
                this.mReportingNetworkStatsOnPort = false;
                setVideoPort(0, 0, 0, 0);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleMessage(Message msg) {
        if (msg != null) {
            Log.i(LOG_TAG, "handleMessage " + msg.what);
            int i = msg.what;
            if (i == 1) {
                start();
            } else if (i != 2) {
                Log.i(LOG_TAG, "Ignore Network Stat Event " + msg.what);
            } else {
                stop();
            }
        }
    }

    public synchronized long getNetworkStatsVideoCall() {
        long dataUsage;
        INetworkManagementService sNwMgrService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        dataUsage = 0;
        if (sNwMgrService != null) {
            try {
                NetworkStats networkStatsRtp = sNwMgrService.getNetworkStatsVideoCall(this.mIface, this.mLocalVideoRtp, this.mRemoteVideoRtp);
                NetworkStats networkStatsRtcp = sNwMgrService.getNetworkStatsVideoCall(this.mIface, this.mLocalVideoRtcp, this.mRemoteVideoRtcp);
                if (networkStatsRtp != null) {
                    Log.i(LOG_TAG, "getNetworkStatsVideoCall networkStatsRtp : " + networkStatsRtp.getTotalBytes());
                    dataUsage = 0 + networkStatsRtp.getTotalBytes();
                }
                if (networkStatsRtcp != null) {
                    Log.i(LOG_TAG, "getNetworkStatsVideoCall networkStatsRtcp : " + networkStatsRtcp.getTotalBytes());
                    dataUsage += networkStatsRtcp.getTotalBytes();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return dataUsage;
    }

    public synchronized void setVideoPort(int localRtp, int remoteRtp, int localRtcp, int remoteRtcp) {
        this.mLocalVideoRtp = localRtp;
        this.mRemoteVideoRtp = remoteRtp;
        this.mLocalVideoRtcp = localRtcp;
        this.mRemoteVideoRtcp = remoteRtcp;
    }

    /* Debug info: failed to restart local var, previous not found, register: 0 */
    public synchronized void setInterface(String iface) {
        if (iface != null) {
            this.mIface = iface;
        }
    }
}
