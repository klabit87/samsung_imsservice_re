package com.samsung.android.cmcp2phelper.data;

import android.text.TextUtils;
import android.util.Log;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class CphMessage {
    static final String CPH_CMC_DEVICE_ID = "cph_cmc_device_id";
    static final String CPH_CMC_LINE_ID = "cph_cmc_line_id";
    static final String CPH_CMC_RESP_IP = "cph_cmc_resp_ip";
    static final String CPH_CMC_RESP_PORT = "cph_cmc_resp_port";
    static final String CPH_CMC_TIME = "cph_cmc_time";
    static final String CPH_CMC_VERSION = "cph_cmc_version";
    static final String CPH_MESSAGE_TYPE = "cph_message_type";
    public static final String LOG_TAG = ("mdec/" + CphMessage.class.getSimpleName());
    double mCmcVersion;
    String mDeviceId;
    String mLineId;
    int mMsgType;
    String mResponderIP;
    int mResponderPort;
    JSONObject message;

    public String toString() {
        String log = this.message.toString();
        try {
            JSONObject json4Log = new JSONObject(log);
            json4Log.put(CPH_CMC_LINE_ID, "xxx");
            return json4Log.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return log;
        }
    }

    public void printMessage() {
        String str = LOG_TAG;
        Log.d(str, "print message : " + this.message.toString());
    }

    public boolean isValid() {
        if (this.message == null) {
            return false;
        }
        return true;
    }

    public String getResponderIP() {
        return this.mResponderIP;
    }

    public int getResponderPort() {
        return this.mResponderPort;
    }

    public int getMsgType() {
        return this.mMsgType;
    }

    public double getCmcVersion() {
        return this.mCmcVersion;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public String getLineId() {
        return this.mLineId;
    }

    public CphMessage(String deviceId, String lineId) {
        this.mMsgType = 2;
        this.mCmcVersion = 2.0d;
        this.mDeviceId = deviceId;
        this.mLineId = lineId;
        this.mResponderIP = "";
        this.mResponderPort = 0;
        makeJsonObject();
    }

    public CphMessage(int messageType, double cmcVersion, String deviceId, String impu, String responderIP, int responderPort) {
        this.mMsgType = messageType;
        this.mCmcVersion = cmcVersion;
        this.mDeviceId = deviceId;
        this.mLineId = impu;
        this.mResponderIP = responderIP;
        this.mResponderPort = responderPort;
        makeJsonObject();
    }

    public CphMessage(int messageType, double cmcVersion, String deviceId, String lineId) {
        this.mMsgType = messageType;
        this.mCmcVersion = cmcVersion;
        this.mDeviceId = deviceId;
        this.mLineId = lineId;
        this.mResponderIP = "";
        this.mResponderPort = 0;
        makeJsonObject();
    }

    public CphMessage(JSONObject object) {
        this.message = object;
        parseFromJson(object);
    }

    public CphMessage(DatagramPacket packet) {
        try {
            String recv = new String(packet.getData(), StandardCharsets.UTF_8);
            String str = LOG_TAG;
            Log.d(str, "new cphMessage : " + recv);
            this.message = new JSONObject(recv);
            String str2 = LOG_TAG;
            Log.d(str2, "json cphMessage : " + toString());
            parseFromJson(this.message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseFromJson(JSONObject object) {
        try {
            this.mMsgType = object.getInt(CPH_MESSAGE_TYPE);
            this.mCmcVersion = object.getDouble(CPH_CMC_VERSION);
            this.mDeviceId = object.getString(CPH_CMC_DEVICE_ID);
            this.mLineId = object.getString(CPH_CMC_LINE_ID);
            this.mResponderIP = object.getString(CPH_CMC_RESP_IP);
            this.mResponderPort = object.getInt(CPH_CMC_RESP_PORT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public byte[] getByte() {
        JSONObject jSONObject = this.message;
        if (jSONObject != null) {
            return jSONObject.toString().getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    private void makeJsonObject() {
        JSONObject jSONObject = new JSONObject();
        this.message = jSONObject;
        try {
            jSONObject.put(CPH_MESSAGE_TYPE, this.mMsgType);
            this.message.put(CPH_CMC_VERSION, this.mCmcVersion);
            this.message.put(CPH_CMC_DEVICE_ID, this.mDeviceId);
            this.message.put(CPH_CMC_LINE_ID, this.mLineId);
            if (!TextUtils.isEmpty(this.mResponderIP)) {
                this.message.put(CPH_CMC_RESP_IP, this.mResponderIP);
            }
            if (this.mResponderPort != 0) {
                this.message.put(CPH_CMC_RESP_PORT, this.mResponderPort);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
