package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.telephony.SignalStrength;
import android.util.Log;
import com.sec.ims.volte2.data.VolteConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.SignalStrengthWrapper;
import com.sec.internal.ims.core.RegistrationEvents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MobileCareController {
    public static final String ACTIONCALLDROP = "com.samsung.intent.action.IMS_CALL_DROP";
    public static final String CALLTYPE = "CallType";
    public static final String ERRORREASON = "ErrorReason";
    public static final String ERRORSTRING = "ErrorString";
    private static final String LOG_TAG = MobileCareController.class.getSimpleName();
    public static final String NETWORKTYPE = "NetworkType";
    public static final String RSRP = "RSRP";
    public static final String RSRQ = "RSRQ";
    public static final String TIMEINFO = "TimeInfo";
    private final Context mContext;
    private Set<Integer> mErrorSet = new HashSet();
    private int mLteBand = -1;
    private int[] mLteRsrp;
    private int[] mLteRsrq;
    private int[] mSignalLevel;

    public MobileCareController(Context context) {
        this.mContext = context;
        initErrorList();
        int phoneCount = SimManagerFactory.getAllSimManagers().size();
        int[] iArr = new int[phoneCount];
        this.mLteRsrp = iArr;
        this.mLteRsrq = new int[phoneCount];
        this.mSignalLevel = new int[phoneCount];
        Arrays.fill(iArr, -1);
        Arrays.fill(this.mLteRsrq, -1);
        Arrays.fill(this.mSignalLevel, -1);
    }

    private void initErrorList() {
        this.mErrorSet.add(400);
        this.mErrorSet.add(Integer.valueOf(AECNamespace.HttpResponseCode.METHOD_NOT_ALLOWED));
        this.mErrorSet.add(Integer.valueOf(RegistrationEvents.EVENT_DISCONNECT_PDN_BY_HD_VOICE_ROAMING_OFF));
        this.mErrorSet.add(408);
        this.mErrorSet.add(Integer.valueOf(NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE));
        this.mErrorSet.add(484);
        this.mErrorSet.add(500);
        this.mErrorSet.add(580);
        this.mErrorSet.add(Integer.valueOf(Id.REQUEST_VSH_STOP_SESSION));
        this.mErrorSet.add(1108);
        this.mErrorSet.add(1114);
        this.mErrorSet.add(Integer.valueOf(Id.REQUEST_SIP_DIALOG_OPEN));
        this.mErrorSet.add(1202);
        this.mErrorSet.add(1203);
        this.mErrorSet.add(1204);
        this.mErrorSet.add(Integer.valueOf(Id.REQUEST_CHATBOT_ANONYMIZE));
        this.mErrorSet.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS));
        this.mErrorSet.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID));
    }

    public boolean isEnabled() {
        return true;
    }

    public void sendMobileCareEvent(int phoneId, int callType, int error, String msg, boolean isePDG) {
        if (this.mErrorSet.contains(Integer.valueOf(error))) {
            boolean isVideo = ImsCallUtil.isVideoCall(callType);
            String str = LOG_TAG;
            Log.i(str, "sendMobileCareEvent : isVideo [" + isVideo + "] isePDG [" + isePDG + "] mRSRP [" + this.mLteRsrp[phoneId] + "] mRSRQ [" + this.mLteRsrq[phoneId] + "] mErrorCode [" + error + "] mErrorDesc [" + msg + "]");
            Intent callDropIntent = new Intent();
            callDropIntent.setAction(ACTIONCALLDROP);
            callDropIntent.putExtra(CALLTYPE, isVideo);
            callDropIntent.putExtra(NETWORKTYPE, isePDG);
            callDropIntent.putExtra(TIMEINFO, getCurrentTimeShort());
            callDropIntent.putExtra(ERRORREASON, error);
            callDropIntent.putExtra(ERRORSTRING, msg != null ? msg : VolteConstants.ErrorCode.toString(error));
            callDropIntent.putExtra(RSRP, this.mLteRsrp[phoneId]);
            callDropIntent.putExtra(RSRQ, this.mLteRsrq[phoneId]);
            this.mContext.sendBroadcast(callDropIntent);
            return;
        }
        Log.i(LOG_TAG, "sendMobileCareEvent : Don't need to send event");
    }

    private String getCurrentTimeShort() {
        Calendar calendar = Calendar.getInstance();
        String hour = new DecimalFormat("00").format((long) calendar.get(11));
        String minute = new DecimalFormat("00").format((long) calendar.get(12));
        String second = new DecimalFormat("00").format((long) calendar.get(13));
        String milliSecond = new DecimalFormat(NSDSNamespaces.NSDSMigration.DEFAULT_KEY).format((long) calendar.get(14));
        return hour + ":" + minute + ":" + second + "." + milliSecond;
    }

    public ContentValues getPSDataDetails(int phoneId, NetworkEvent networkEvent, boolean ratChanged) {
        ContentValues psItem = new ContentValues();
        psItem.put("BAND", Integer.valueOf(this.mLteBand));
        psItem.put(RSRP, Integer.valueOf(this.mLteRsrp[phoneId]));
        psItem.put(RSRQ, Integer.valueOf(this.mLteRsrq[phoneId]));
        psItem.put("NWTP", Integer.valueOf(networkEvent != null ? networkEvent.network : 0));
        psItem.put("RTCH", Integer.valueOf(ratChanged));
        return psItem;
    }

    public void onSignalStrengthsChanged(int phoneId, SignalStrength signalStrength) {
        if (signalStrength != null) {
            SignalStrengthWrapper ss = new SignalStrengthWrapper(signalStrength);
            this.mLteRsrp[phoneId] = ss.getLteRsrp();
            this.mLteRsrq[phoneId] = ss.getLteRsrq();
            this.mSignalLevel[phoneId] = ss.getLevel();
            return;
        }
        Log.i(LOG_TAG, "getLteSignalStrength is null");
        this.mLteRsrp[phoneId] = -1;
        this.mLteRsrq[phoneId] = -1;
        this.mSignalLevel[phoneId] = -1;
    }

    public void onLteBancChanged(String band) {
        try {
            this.mLteBand = Integer.parseInt(band);
        } catch (NumberFormatException e) {
            this.mLteBand = -1;
        }
        String str = LOG_TAG;
        Log.i(str, "Received LTE Band is " + band + ", mLteBand is " + this.mLteBand);
    }

    public int getSignalLevel(int phoneId) {
        return this.mSignalLevel[phoneId];
    }
}
