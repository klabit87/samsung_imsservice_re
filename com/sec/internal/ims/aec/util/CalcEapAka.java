package com.sec.internal.ims.aec.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.interfaces.ims.core.ISimManager;

public class CalcEapAka extends Handler {
    private final int HANDLE_REQUEST_SIM_AUTHENTICATION = 0;
    private final int HANDLE_RESPONSE_SIM_AUTHENTICATION = 1;
    private final String mImsi;
    private final int mPhoneId;
    private Message mReplyTo;

    public CalcEapAka(int phoneId, String imsi) {
        this.mPhoneId = phoneId;
        this.mImsi = imsi;
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            requestSimAuthentication(msg.getData().getString(WwwAuthenticateHeader.HEADER_PARAM_NONCE), msg.getData().getString("akaChallenge"));
        } else if (i == 1) {
            processSimAuthResponse(msg.getData().getString("akaChallenge"), (String) msg.obj, msg.getData().getString("imsiEap"));
        }
    }

    public void requestEapChallengeResp(Message replyTo, String akaChallenge) {
        this.mReplyTo = replyTo;
        Message msg = obtainMessage();
        msg.what = 0;
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(akaChallenge)) {
            bundle.putString(WwwAuthenticateHeader.HEADER_PARAM_NONCE, AKAEapAuthHelper.getNonce(akaChallenge));
            bundle.putString("akaChallenge", akaChallenge);
            msg.setData(bundle);
        }
        sendMessage(msg);
    }

    public String decodeChallenge(String akaChallenge) {
        return AKAEapAuthHelper.decodeChallenge(akaChallenge);
    }

    private void requestSimAuthentication(String nonce, String akaChallenge) {
        try {
            Message msg = obtainMessage(1);
            Bundle dataMap = new Bundle();
            dataMap.putString("akaChallenge", akaChallenge);
            dataMap.putString("imsiEap", getImsiEap());
            msg.setData(dataMap);
            ISimManager simMgr = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            if (simMgr != null) {
                simMgr.requestIsimAuthentication(nonce, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSimAuthResponse(String akaChallenge, String simResponse, String imsiEap) {
        Message message = this.mReplyTo;
        if (message != null) {
            message.obj = AKAEapAuthHelper.generateChallengeResponse(akaChallenge, simResponse, imsiEap);
            this.mReplyTo.sendToTarget();
        }
    }

    public String getImsiEap() throws Exception {
        String mnc;
        String mcc;
        ISimManager simMgr = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (simMgr != null) {
            String operator = simMgr.getSimOperator();
            if (operator.length() == 5) {
                mcc = operator.substring(0, 3);
                mnc = "0" + operator.substring(3, 5);
            } else if (operator.length() == 6) {
                mcc = operator.substring(0, 3);
                mnc = operator.substring(3, 6);
            } else {
                throw new Exception("getImsiEap: invalid operator");
            }
            return "0" + this.mImsi + "@nai.epc.mnc" + mnc + ".mcc" + mcc + UtUtils.DOMAIN_NAME;
        }
        throw new Exception("getImsiEap: sim manager not ready");
    }
}
