package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.log.IMSLog;

public class TelephonyAdapterPrimaryDeviceTelstra extends TelephonyAdapterPrimaryDeviceBase {
    private static final String LOG_TAG = TelephonyAdapterPrimaryDeviceTelstra.class.getSimpleName();
    private static final String STANDARD_IMPI_TEMPLATE = "<imsi>@ims.mnc<mnc>.mcc<mcc>.3gppnetwork.org";

    public TelephonyAdapterPrimaryDeviceTelstra(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void handleReceivedDataSms(Message msg, boolean isForceConfigRequest, boolean useWaitingForOtp) {
        String smsBody = (String) msg.obj;
        if (smsBody == null || !smsBody.contains(SMS_CONFIGURATION_REQUEST)) {
            super.handleReceivedDataSms(msg, isForceConfigRequest, useWaitingForOtp);
            return;
        }
        if (isValidReconfigSmsFormatImsi(smsBody)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isValidReconfigSmsFormatImsi");
        } else if (isValidReconfigSmsFormatStandardImpi(smsBody)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isValidReconfigSmsFormatStandardImpi");
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "invalid reconfiguration SMS format for Telstra");
            return;
        }
        sendSmsPushForConfigRequest(isForceConfigRequest);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        int i2 = msg.what;
        if (i2 == 1) {
            handleReceivedDataSms(msg, true, false);
        } else if (i2 != 3) {
            super.handleMessage(msg);
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "getting otp is timed out");
            handleOtpTimeout(false);
        }
    }

    private boolean isValidReconfigSmsFormatImsi(String smsBody) {
        if (smsBody.contains(getImsi() + SMS_CONFIGURATION_REQUEST)) {
            return true;
        }
        return false;
    }

    private boolean isValidReconfigSmsFormatStandardImpi(String smsBody) {
        if (smsBody.contains(genStandardImpi() + SMS_CONFIGURATION_REQUEST)) {
            return true;
        }
        if (smsBody.contains(getTelstraImpi() + SMS_CONFIGURATION_REQUEST)) {
            return true;
        }
        return false;
    }

    private String genStandardImpi() {
        if (getImsi() == null || getMcc() == null || getMnc() == null) {
            return STANDARD_IMPI_TEMPLATE;
        }
        String impi = STANDARD_IMPI_TEMPLATE.replace("<imsi>", getImsi()).replace("<mcc>", getMcc()).replace("<mnc>", getMnc());
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "Generated standard IMPI: " + impi);
        return impi;
    }

    private String getTelstraImpi() {
        String impi = RcsConfigurationHelper.readStringParam(this.mContext, ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "get Telstra Impi: " + impi);
        return impi;
    }
}
