package com.sec.internal.ims.servicemodules.sms;

import android.os.Parcel;
import android.os.Parcelable;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import com.sec.internal.helper.os.Debug;

public class SmsEvent implements Parcelable {
    public static final Parcelable.Creator<SmsEvent> CREATOR = new Parcelable.Creator<SmsEvent>() {
        public SmsEvent createFromParcel(Parcel in) {
            return new SmsEvent(in);
        }

        public SmsEvent[] newArray(int size) {
            return new SmsEvent[size];
        }
    };
    public static final int SMSIP_CST_NOTI_INFO = 12;
    public static final int SMSIP_CST_RECEIVED = 11;
    private String mCallID = null;
    private String mContent = null;
    private String mContentType = null;
    private byte[] mData = null;
    private int mEventType = -1;
    private String mLocalUri = null;
    private int mMessageID = -1;
    private String mReason = null;
    private int mReasonCode = -1;
    private ImsRegistration mRegistration = null;
    private int mRetryAfter = -1;
    private int mRpRef = -1;
    private String mSmscAddr = null;
    private int mState = 0;
    private int mTpDcs = 0;
    private int mTpMr = 0;
    private int mTpPid = 0;

    public static class State {
        public static final int MO_RECEIVING_202_ACCEPTED = 102;
        public static final int MO_RECEIVING_CALLID = 101;
        public static final int MO_SENDING_START = 100;
        public static final int MT_RECEIVING_DELIVER_REPORT_ACK = 106;
        public static final int MT_RECEIVING_INCOMING_SMS = 103;
        public static final int MT_RECEIVING_STATUS_REPORT = 104;
        public static final int MT_SENDING_DELIVER_REPORT = 105;
        public static final int NONE = 0;
    }

    public SmsEvent(ImsRegistration reg, int eventType, int messageID, int reasonCode, String reason, byte[] data, String contentType, String callId, String smscAddr, int retryAfter) {
        this.mRegistration = reg;
        this.mData = data;
        this.mEventType = eventType;
        this.mMessageID = messageID;
        this.mReasonCode = reasonCode;
        this.mContentType = contentType;
        this.mCallID = callId;
        this.mSmscAddr = smscAddr;
        this.mRetryAfter = retryAfter;
        this.mReason = reason;
    }

    public SmsEvent(Parcel in) {
        int mDataLen = in.readInt();
        if (mDataLen > 0) {
            byte[] mDataTemp = new byte[mDataLen];
            in.readByteArray(mDataTemp);
            this.mData = mDataTemp;
        } else {
            this.mData = null;
        }
        this.mEventType = in.readInt();
        this.mMessageID = in.readInt();
        this.mReasonCode = in.readInt();
        this.mReason = in.readString();
        this.mContentType = in.readString();
        this.mCallID = in.readString();
        this.mSmscAddr = in.readString();
        this.mRetryAfter = in.readInt();
    }

    public SmsEvent() {
        setState(0);
        this.mRpRef = -1;
    }

    public ImsRegistration getImsRegistration() {
        return this.mRegistration;
    }

    public int getState() {
        return this.mState;
    }

    public int getRpRef() {
        return this.mRpRef;
    }

    public byte[] getData() {
        return this.mData;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public int getMessageID() {
        return this.mMessageID;
    }

    public int getReasonCode() {
        return this.mReasonCode;
    }

    public String getReason() {
        return this.mReason;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public String getCallID() {
        return this.mCallID;
    }

    public String getSmscAddr() {
        return this.mSmscAddr;
    }

    public String getLocalUri() {
        return this.mLocalUri;
    }

    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    public int getTpPid() {
        return this.mTpPid;
    }

    public int getTpDcs() {
        return this.mTpDcs;
    }

    public int getTpMr() {
        return this.mTpMr;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setImsRegistration(ImsRegistration reg) {
        this.mRegistration = reg;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public void setRpRef(int rpRef) {
        this.mRpRef = rpRef;
    }

    public void setData(byte[] data) {
        this.mData = data;
    }

    public void setEventType(int eventType) {
        this.mEventType = eventType;
    }

    public void setMessageID(int messageId) {
        this.mMessageID = messageId;
    }

    public void setReasonCode(int reasonCode) {
        this.mReasonCode = reasonCode;
    }

    public void setReason(String reason) {
        this.mReason = reason;
    }

    public void setContentType(String contentType) {
        this.mContentType = contentType;
    }

    public void setCallID(String callId) {
        this.mCallID = callId;
    }

    public void setSmscAddr(String smscAddr) {
        this.mSmscAddr = smscAddr;
    }

    public void setLocalUri(String localUri) {
        this.mLocalUri = localUri;
    }

    public void setRetryAfter(int retryAfter) {
        this.mRetryAfter = retryAfter;
    }

    public void setTpPid(int TpPid) {
        this.mTpPid = TpPid;
    }

    public void setTpDcs(int TpDcs) {
        this.mTpDcs = TpDcs;
    }

    public void setTpMr(int TpMr) {
        this.mTpMr = TpMr;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        byte[] bArr = this.mData;
        if (bArr != null) {
            out.writeInt(bArr.length);
            out.writeByteArray(this.mData);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.mEventType);
        out.writeInt(this.mMessageID);
        out.writeInt(this.mReasonCode);
        out.writeString(this.mReason);
        out.writeString(this.mContentType);
        out.writeString(this.mCallID);
        out.writeString(this.mSmscAddr);
        out.writeInt(this.mRetryAfter);
    }

    public String toString() {
        String str;
        switch (this.mState) {
            case 100:
                str = "" + "[OUTGOING] state MO_SENDING_START ";
                break;
            case 101:
                str = "" + "[OUTGOING] state MO_RECEIVING_CALLID ";
                break;
            case 102:
                str = "" + "[OUTGOING] state MO_RECEIVING_202_ACCEPTED ";
                break;
            case 103:
                str = "" + "[INCOMING] state MT_RECEIVING_INCOMING_SMS ";
                break;
            case 104:
                str = "" + "[INCOMING] state MT_RECEIVING_STATUS_REPORT ";
                break;
            case 105:
                str = "" + "[INCOMING] state MT_SENDING_DELIVER_REPORT ";
                break;
            case 106:
                str = "" + "[INCOMING] state MT_RECEIVING_DELIVER_REPORT_ACK ";
                break;
            default:
                str = "" + "[NONE] ";
                break;
        }
        if (this.mContentType != null) {
            str = str + "contentType [" + this.mContentType + "] ";
        }
        if (this.mMessageID >= 0) {
            str = str + "messageID [" + this.mMessageID + "] ";
        }
        if (this.mRpRef >= 0) {
            str = str + "rpRef [" + this.mRpRef + "] ";
        }
        if (this.mReasonCode >= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("reasonCode [");
            int i = this.mReasonCode;
            if (i >= 32768) {
                i -= 32768;
            }
            sb.append(i);
            sb.append("] ");
            str = sb.toString();
        }
        if (this.mReason != null) {
            str = str + "reason [" + this.mReason + "] ";
        }
        if (this.mCallID != null) {
            str = str + "callID [" + this.mCallID + "] ";
        }
        if (this.mSmscAddr != null) {
            if (!Debug.isProductShip()) {
                str = str + "smscAddr [" + this.mSmscAddr + "] ";
            } else {
                int displayNum = 3;
                if (this.mSmscAddr.startsWith("sip:") || this.mSmscAddr.startsWith("tel:")) {
                    displayNum = 3 + 4;
                }
                if (this.mSmscAddr.length() > displayNum) {
                    str = str + "smscAddr [" + this.mSmscAddr.substring(0, displayNum) + "] ";
                } else {
                    str = str + "smscAddr [" + this.mSmscAddr + "] ";
                }
            }
        }
        if (this.mRegistration == null) {
            return str;
        }
        return str + "regId [" + this.mRegistration.getHandle() + "] ";
    }

    public String toKeyDump() {
        String str = "";
        int i = this.mState;
        if (i >= 100 && i <= 106) {
            str = str + this.mState + ",";
        }
        if (this.mMessageID >= 0) {
            str = str + this.mMessageID + ",";
        }
        if (this.mTpMr >= 0) {
            str = str + this.mTpMr + ",";
        }
        if (this.mRpRef >= 0) {
            str = str + this.mRpRef + ",";
        }
        if (this.mReasonCode >= 0) {
            str = str + this.mReasonCode + ",";
        }
        if (this.mReason != null) {
            str = str + this.mReason + ",";
        }
        String str2 = this.mContentType;
        if (str2 == null) {
            return str;
        }
        if (str2.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
            return str + SmsMessage.FORMAT_3GPP;
        }
        return str + SmsMessage.FORMAT_3GPP2;
    }
}
