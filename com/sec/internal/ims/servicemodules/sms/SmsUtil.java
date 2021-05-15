package com.sec.internal.ims.servicemodules.sms;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SemHqmManager;
import android.os.SemSystemProperties;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.CscFeatureTagIMS;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.log.IMSLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SmsUtil {
    private static final String LOG_TAG = SmsServiceModule.class.getSimpleName();
    private static final int MSG_ID_BASE = 1000;
    private static int mIncommingMagId = 1000;
    private static int mRPMsgRef = 0;

    protected static String getNetworkPreferredUri(ImsRegistration regInfo, String number, boolean isTel) {
        ImsUri scaUri;
        if (number.startsWith("sip:") || number.startsWith("tel:")) {
            scaUri = ImsUri.parse(number);
        } else if (regInfo == null) {
            return null;
        } else {
            UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(regInfo.getPreferredImpu().getUri());
            Mno mno = SimUtil.getSimMno(regInfo.getPhoneId());
            if (isTel) {
                if (mno == Mno.VZW) {
                    String domain = regInfo.getDomain();
                    if (number.startsWith("+") || TextUtils.isEmpty(domain)) {
                        scaUri = ImsUri.parse("tel:" + number);
                    } else {
                        scaUri = ImsUri.parse("tel:" + number + ";phone-context=" + domain);
                    }
                } else {
                    scaUri = uriGenerator.getNormalizedUri(number);
                }
            } else if (mno == Mno.TMOUS || mno == Mno.MTN_SOUTHAFRICA) {
                scaUri = uriGenerator.getNetworkPreferredUri(number);
            } else {
                scaUri = uriGenerator.getNetworkPreferredUri((ImsUri.UriType) null, number);
            }
        }
        return scaUri.toString();
    }

    protected static String getLocalUri(ImsRegistration regInfo) {
        if (regInfo == null) {
            return "";
        }
        NameAddr pImpu = regInfo.getPreferredImpu();
        ImsUri impu = pImpu == null ? null : pImpu.getUri();
        if (SimUtil.getSimMno(regInfo.getPhoneId()) == Mno.ATT) {
            for (NameAddr addr : regInfo.getImpuList()) {
                if (addr.getUri().toString().startsWith("tel:")) {
                    impu = addr.getUri();
                }
            }
        }
        if (impu == null) {
            return "";
        }
        return impu.toString();
    }

    protected static int getIncreasedRPRef() {
        int i = mRPMsgRef + 1;
        mRPMsgRef = i;
        if (i >= 254) {
            mRPMsgRef = 0;
        }
        return mRPMsgRef;
    }

    protected static int getRPMsgRef() {
        return mRPMsgRef;
    }

    protected static int getMessageIdByCallId(ConcurrentHashMap<Integer, SmsEvent> pendingQueue, String callId) {
        for (Map.Entry<Integer, SmsEvent> entry : pendingQueue.entrySet()) {
            int key = entry.getKey().intValue();
            if (callId.equals(entry.getValue().getCallID())) {
                return key;
            }
        }
        return -1;
    }

    protected static int getMessageIdByPdu(ConcurrentHashMap<Integer, SmsEvent> pendingQueue, byte[] data) {
        if (data == null || data.length <= 1) {
            return -1;
        }
        int rpRef = data[1] & 255;
        for (Map.Entry<Integer, SmsEvent> entry : pendingQueue.entrySet()) {
            int key = entry.getKey().intValue();
            if (rpRef == entry.getValue().getRpRef()) {
                return key;
            }
        }
        return -1;
    }

    protected static int getNewMsgId() {
        int i = mIncommingMagId;
        if (i == 65535) {
            mIncommingMagId = 1000;
        } else {
            mIncommingMagId = i + 1;
        }
        return mIncommingMagId;
    }

    protected static int getIncommingMagId() {
        return mIncommingMagId;
    }

    protected static boolean disallowReregistration(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.BOG || mno == Mno.ORANGE || mno == Mno.ORANGE_POLAND || mno == Mno.DIGI || mno == Mno.TELECOM_ITALY || mno == Mno.VODAFONE || mno == Mno.TELEKOM_ALBANIA || mno.isTmobile() || mno == Mno.VODAFONE_NEWZEALAND || mno == Mno.WINDTRE) {
            return true;
        }
        return false;
    }

    private static boolean isKORMnoName(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.SKT || mno == Mno.KT || mno == Mno.LGU) {
            return true;
        }
        return false;
    }

    protected static boolean isServiceAvailable(TelephonyManager tm, int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        int networkType = tm.getDataNetworkType(SimUtil.getSubId(phoneId));
        return (mno == Mno.ATT || disallowReregistration(phoneId)) ? "Softphone".equals(SemCscFeature.getInstance().getString(CscFeatureTagIMS.TAG_CSCFEATURE_IMS_CONFIGMDMNTYPE)) || networkType == 13 || networkType == 20 || networkType == 18 : !isKORMnoName(phoneId) || networkType == 13 || networkType == 20;
    }

    protected static boolean isProhibited(ImsRegistration regInfo) {
        ImsRegistration reg;
        if (regInfo != null) {
            Map<Integer, ImsRegistration> mRegistrationList = ImsRegistry.getRegistrationManager().getRegistrationList();
            ImsProfile profile = regInfo.getImsProfile();
            if (!(profile == null || (reg = mRegistrationList.get(Integer.valueOf(profile.getId()))) == null)) {
                regInfo.setProhibited(reg.isProhibited());
                return reg.isProhibited();
            }
        }
        return false;
    }

    protected static void broadcastDcnNumber(Context context, int defaultPhoneId) {
        String dcn = DmConfigHelper.read(context, ConfigConstants.ConfigPath.OMADM_DCN_NUMBER, "", defaultPhoneId);
        if (!TextUtils.isEmpty(dcn)) {
            String str = LOG_TAG;
            Log.d(str, "broadcastDcnNumber : " + "DCN_NUMBER" + ", value : " + IMSLog.checker(dcn));
            Intent intent = new Intent(ImsConstants.Intents.ACTION_DM_CHANGED);
            intent.putExtra(ImsConstants.Intents.EXTRA_UPDATED_ITEM, "DCN_NUMBER");
            intent.putExtra(ImsConstants.Intents.EXTRA_UPDATED_VALUE, dcn);
            intent.putExtra("phoneId", defaultPhoneId);
            context.sendBroadcast(intent);
        }
    }

    protected static void broadcastSCBMState(Context context, boolean phoneInSCBMState, int phoneId) {
        String str = LOG_TAG;
        Log.d(str, "broadcastSCBMState : " + phoneInSCBMState + ", phoneId : " + phoneId);
        Intent intent = new Intent(ImsConstants.Intents.ACTION_SMS_CALLBACK_MODE_CHANGED_INTERNAL);
        intent.addFlags(16777216);
        intent.putExtra("phoneInSCBMState", phoneInSCBMState);
        intent.putExtra("phoneId", phoneId);
        context.sendBroadcast(intent);
    }

    protected static void onSipError(ImsRegistration regInfo, int reasonCode, String reason) {
        IRegistrationGovernor governor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(regInfo.getHandle());
        if (governor != null) {
            governor.onSipError("smsip", new SipError(reasonCode, reason));
        }
    }

    protected static void sendDailyReport(Context context, int phoneId) {
        ContentValues cv = new ContentValues();
        cv.put("SMTI", 1);
        cv.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        cv.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(phoneId, context, DiagnosisConstants.FEATURE_DRCS, cv);
    }

    protected static void sendISMOInfoToHQM(Context context, String result, int SIP, String RPAck, boolean Regi, int phoneId) {
        if (SIP == 408 || SIP == 708) {
            result = DiagnosisConstants.RCSM_ORST_REGI;
        } else if (SIP >= 200 && SIP < 300) {
            result = "0";
        }
        ContentValues cv = new ContentValues();
        if (result.equals("0")) {
            cv.put("SOIS", 1);
        } else {
            cv.put("SOIF", 1);
        }
        cv.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        cv.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(phoneId, context, DiagnosisConstants.FEATURE_DRCS, cv);
        StringBuilder sb = new StringBuilder();
        sb.append("\"ORST\":\"");
        sb.append(result);
        if (SIP != 0) {
            sb.append("\",\"OSIP\":\"");
            sb.append(Integer.toString(SIP));
        } else if (RPAck != null) {
            sb.append("\",\"ORPC\":\"");
            sb.append(RPAck);
        }
        sendSMSInfoToHQM(context, "ISMO", sb.toString(), Regi, phoneId);
    }

    protected static void sendSMOTInfoToHQM(Context context, String MOMT, String ITER, boolean Regi, int phoneId) {
        sendSMSInfoToHQM(context, "SMOT", "\"MOMT\":\"" + MOMT + "\",\"ITER\":\"" + ITER, Regi, phoneId);
    }

    private static void sendSMSInfoToHQM(Context context, String feature, String customdataset, boolean Regi, int phoneId) {
        int phoneId2;
        String numeric;
        String[] numericList = SemSystemProperties.get("gsm.operator.numeric", "00101#").trim().split(",");
        boolean isRoaming = SemSystemProperties.getBoolean("gsm.operator.isroaming", false);
        if (phoneId < 0) {
            phoneId2 = 0;
        } else {
            phoneId2 = phoneId;
        }
        if (numericList.length > phoneId2) {
            String numeric2 = numericList[phoneId2];
            if (numeric2.length() > 6) {
                numeric = numeric2.substring(0, 6);
            } else {
                while (numeric2.length() < 6) {
                    numeric2 = numeric2 + "#";
                }
                numeric = numeric2;
            }
        } else {
            numeric = "00101#";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\"PLMN\":\"");
        sb.append(numeric + "\",");
        sb.append("\"RgSt\":\"");
        if (isRoaming) {
            sb.append("6\",");
            boolean z = Regi;
        } else if (Regi) {
            sb.append("2\",");
        } else {
            sb.append("1\",");
        }
        sb.append(customdataset);
        String CSDA = Telephony.Sms.getDefaultSmsPackage(context);
        if (CSDA != null) {
            sb.append("\",\"CSDA\":\"");
            sb.append(CSDA);
        }
        sb.append("\"");
        String basic_customDataSet = sb.toString();
        Log.i(LOG_TAG, "[SMS BigData] sendSMSInfoToHQM : feature- " + feature + ", data- " + basic_customDataSet);
        try {
            sendHWParamToHQM(context, 0, DiagnosisConstants.COMPONENT_ID, feature, "sm", "0.0", ImsConstants.RCS_AS.SEC, "", basic_customDataSet, "");
        } catch (Exception e) {
        }
    }

    protected static boolean sendHWParamToHQM(Context context, int type, String id, String feature, String hitType, String ver, String manufacture, String dev_custom_dataset, String custom_dataset, String pri_custom_dataset) throws RemoteException {
        Context context2 = context;
        SemHqmManager hqm = (SemHqmManager) context.getSystemService("HqmManagerService");
        if (hqm == null) {
            return false;
        }
        return hqm.sendHWParamToHQM(type, id, feature, hitType, ver, manufacture, dev_custom_dataset, custom_dataset, pri_custom_dataset);
    }
}
