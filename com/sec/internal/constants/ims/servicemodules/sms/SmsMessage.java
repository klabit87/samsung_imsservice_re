package com.sec.internal.constants.ims.servicemodules.sms;

import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.util.BitwiseInputStream;
import com.android.internal.util.BitwiseOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SmsMessage {
    public static final int CDMA_NETWORK_TYPE = 1;
    private static final int CDMA_SMS_DIGIT_MODE_4_BIT = 0;
    private static final int CDMA_SMS_DIGIT_MODE_8_BIT = 1;
    public static final int DELIVER_MESSAGE_TYPE = 1;
    public static final int DIGIT_MODE_4BIT_DTMF = 4;
    public static final int DIGIT_MODE_8BIT_CHAR = 8;
    public static final int ENCODING_7BIT_ASCII = 2;
    public static final int ENCODING_GSM_7BIT_ALPHABET = 9;
    public static final int ENCODING_GSM_DCS = 10;
    public static final int ENCODING_IA5 = 3;
    public static final int ENCODING_IS91_EXTENDED_PROTOCOL = 1;
    public static final int ENCODING_KOREAN = 6;
    public static final int ENCODING_LATIN = 8;
    public static final int ENCODING_LATIN_HEBREW = 7;
    public static final int ENCODING_OCTET = 0;
    public static final int ENCODING_SHIFT_JIS = 5;
    public static final int ENCODING_UNICODE_16 = 4;
    public static final int ERROR_NONE = 0;
    public static final int ERROR_PERMANENT = 3;
    public static final int ERROR_TEMPORARY = 2;
    public static final int FAIL_CAUSE_ENCODING_PROBLEM = 96;
    public static final int FAIL_CAUSE_INVALID_TELESERVICE_ID = 4;
    public static final int FAIL_CAUSE_OTHER_TERMINAL_PROBLEM = 39;
    public static final int FAIL_CAUSE_RESOURCE_SHORTAGE = 35;
    public static final String FORMAT_3GPP = "3gpp";
    public static final String FORMAT_3GPP2 = "3gpp2";
    public static final int GSM_NETWORK_TYPE = 2;
    public static final int IPC_ADDRESS = 3;
    public static final int IPC_BEARER_DATA = 25;
    public static final int IPC_BEARER_REPLY = 5;
    public static final int IPC_SERVICE_CATEGORY = 2;
    public static final int IPC_SMS_FORMAT_PP = 1;
    public static final int IPC_SMS_FORMAT_SR = 2;
    public static final int IPC_SUBADDRESS = 4;
    public static final int IPC_TELESERVICE_ID = 1;
    public static final String LOG_TAG = "SmsMessage";
    public static final int MESSAGE_TYPE_CANCELLATION = 3;
    public static final int MESSAGE_TYPE_DELIVER = 1;
    public static final int MESSAGE_TYPE_DELIVERY_ACK = 4;
    public static final int MESSAGE_TYPE_DELIVER_REPORT = 7;
    public static final int MESSAGE_TYPE_READ_ACK = 6;
    public static final int MESSAGE_TYPE_SUBMIT = 2;
    public static final int MESSAGE_TYPE_SUBMIT_REPORT = 8;
    public static final int MESSAGE_TYPE_USER_ACK = 5;
    public static final int NUMBER_MODE_DATA_NETWORK = 1;
    public static final int NUMBER_MODE_NOT_DATA_NETWORK = 0;
    public static final int PARAM_ID_BEARER_DATA = 8;
    public static final int PARAM_ID_BEARER_REPLY_OPTION = 6;
    public static final int PARAM_ID_CAUSE_CODES = 7;
    public static final int PARAM_ID_DESTINATION_ADDRESS = 4;
    public static final int PARAM_ID_DESTINATION_SUB_ADDRESS = 5;
    public static final int PARAM_ID_ORIGINATING_ADDRESS = 2;
    public static final int PARAM_ID_ORIGINATING_SUB_ADDRESS = 3;
    public static final int PARAM_ID_SERVICE_CATEGORY = 1;
    public static final int PARAM_ID_TELESERVICE = 0;
    public static final int PARAM_LENGTH_TELESERVICE = 2;
    public static final int STATUS_REPORT_MESSAGE_TYPE = 2;
    private static final byte SUBPARAM_ALERT_ON_MESSAGE_DELIVERY = 12;
    private static final byte SUBPARAM_CALLBACK_NUMBER = 14;
    private static final byte SUBPARAM_DEFERRED_DELIVERY_TIME_ABSOLUTE = 6;
    private static final byte SUBPARAM_DEFERRED_DELIVERY_TIME_RELATIVE = 7;
    private static final byte SUBPARAM_ID_LAST_DEFINED = 23;
    private static final byte SUBPARAM_LANGUAGE_INDICATOR = 13;
    private static final byte SUBPARAM_MESSAGE_CENTER_TIME_STAMP = 3;
    private static final byte SUBPARAM_MESSAGE_DEPOSIT_INDEX = 17;
    private static final byte SUBPARAM_MESSAGE_DISPLAY_MODE = 15;
    private static final byte SUBPARAM_MESSAGE_IDENTIFIER = 0;
    private static final byte SUBPARAM_MESSAGE_STATUS = 20;
    private static final byte SUBPARAM_NUMBER_OF_MESSAGES = 11;
    private static final byte SUBPARAM_PRIORITY_INDICATOR = 8;
    private static final byte SUBPARAM_PRIVACY_INDICATOR = 9;
    private static final byte SUBPARAM_REPLY_OPTION = 10;
    private static final byte SUBPARAM_SERVICE_CATEGORY_PROGRAM_DATA = 18;
    private static final byte SUBPARAM_USER_DATA = 1;
    private static final byte SUBPARAM_USER_RESPONSE_CODE = 2;
    private static final byte SUBPARAM_VALIDITY_PERIOD_ABSOLUTE = 4;
    private static final byte SUBPARAM_VALIDITY_PERIOD_RELATIVE = 5;
    private byte[] mAddressByte;
    private byte[] mBearerData = new byte[0];
    private int mBearerDataLength;
    private int mBearerReplyOptionValue;
    private int mCauseCode;
    private int mContentType;
    private int mCur = 0;
    private String mDestAddress;
    private int mDigitMode;
    private int mErrorClass;
    private int mMessageRef;
    private int mMessageType;
    private int mMsgId = 0;
    private int mMsgType;
    private int mNetworktype;
    private int mNoOfAddressDigit;
    private int mNumberMode;
    private int mNumberPlan;
    private int mReplySeqNo;
    private String mScAddress = null;
    private int mServiceCategory;
    private boolean mStatusReportRequested;
    private int mTeleServiceid;
    private byte[] mTpdu;
    private int mUserDataHeader;

    private static class CodingException extends Exception {
        public CodingException(String s) {
            super(s);
        }
    }

    public byte[] parseSubmitPdu(byte[] data, String format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(300);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            if (format.equals(FORMAT_3GPP2)) {
                parseOutgoingCdmaSms(data);
                dos.write(0);
                dos.write(0);
                dos.write(2);
                dos.writeChar(this.mTeleServiceid);
                byte[] encodeCdmaAddr = encodeCdmaAddress(4);
                if (encodeCdmaAddr != null) {
                    dos.write(encodeCdmaAddr);
                }
                dos.write(8);
                dos.write(this.mBearerDataLength);
                dos.write(this.mBearerData);
                if (this.mBearerDataLength != 0) {
                    byte[] messageId = new byte[6];
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(this.mBearerData));
                    if (dis.read(messageId, 0, 6) < 0) {
                        Log.d(LOG_TAG, "parseSubmitPdu: messageId - the end of the stream has been reached.");
                    }
                    dis.close();
                    try {
                        decodeMessageId(new BitwiseInputStream(messageId));
                    } catch (BitwiseInputStream.AccessException e) {
                        e.printStackTrace();
                    }
                }
                decodeBearerData(this.mBearerData);
                this.mTpdu = baos.toByteArray();
            } else {
                dos.write(data);
                this.mTpdu = baos.toByteArray();
                parseOutgoingGsmSms();
            }
            dos.close();
            return this.mTpdu;
        } catch (IOException ex) {
            Log.e(LOG_TAG, "createFromPdu: conversion from byte array to object failed: " + ex);
            try {
                dos.close();
                return null;
            } catch (IOException e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }

    public byte[] convertToFrameworkSmsFormat(byte[] pdu) {
        parseCdmaDeliverPdu(pdu);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(300);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(this.mMessageType);
            dos.writeInt(this.mTeleServiceid);
            dos.writeInt(this.mServiceCategory);
            dos.write(this.mDigitMode);
            dos.write(this.mNumberMode);
            dos.write(this.mNetworktype);
            dos.write(this.mNumberPlan);
            dos.write(this.mNoOfAddressDigit);
            dos.write(this.mAddressByte);
            dos.writeInt(this.mBearerReplyOptionValue);
            dos.write(this.mReplySeqNo);
            dos.write(this.mErrorClass);
            dos.write(this.mCauseCode);
            dos.writeInt(this.mBearerDataLength);
            dos.write(this.mBearerData);
            dos.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "createFromPdu: conversion from byte array to object failed: " + ex);
            try {
                dos.close();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    public void parseCdmaDeliverPdu(byte[] pdu) {
        ByteArrayInputStream bais = new ByteArrayInputStream(pdu);
        DataInputStream dis = new DataInputStream(bais);
        try {
            this.mMessageType = dis.readByte();
            while (dis.available() > 0) {
                int parameterId = dis.readByte();
                int parameterLen = dis.readUnsignedByte();
                byte[] parameterData = new byte[parameterLen];
                Log.v(LOG_TAG, "parameterId = " + parameterId);
                switch (parameterId) {
                    case 0:
                        this.mTeleServiceid = dis.readUnsignedShort();
                        break;
                    case 1:
                        this.mServiceCategory = dis.readUnsignedShort();
                        break;
                    case 2:
                    case 4:
                        byte[] address = new byte[parameterLen];
                        if (dis.read(address) < 0) {
                            Log.v(LOG_TAG, "parseCdmaDeliverPdu: address - the end of the stream has been reached.");
                        }
                        parseCdmaAddress(address);
                        break;
                    case 3:
                    case 5:
                        break;
                    case 6:
                        if (dis.read(parameterData, 0, parameterLen) < 0) {
                            Log.d(LOG_TAG, "parseOutgoingCdmaSms: parameterData - the end of the stream has been reached.");
                        }
                        this.mBearerReplyOptionValue = new BitwiseInputStream(parameterData).read(6);
                        break;
                    case 7:
                        if (dis.read(parameterData, 0, parameterLen) < 0) {
                            Log.d(LOG_TAG, "parseOutgoingCdmaSms: parameterData - the end of the stream has been reached.");
                        }
                        BitwiseInputStream ccBis = new BitwiseInputStream(parameterData);
                        this.mReplySeqNo = ccBis.readByteArray(6)[0];
                        byte b = ccBis.readByteArray(2)[0];
                        this.mErrorClass = b;
                        if (b == 0) {
                            break;
                        } else {
                            this.mCauseCode = ccBis.readByteArray(8)[0];
                            break;
                        }
                    case 8:
                        if (dis.read(parameterData, 0, parameterLen) < 0) {
                            Log.d(LOG_TAG, "parseOutgoingCdmaSms: parameterData - the end of the stream has been reached.");
                        }
                        this.mBearerDataLength = parameterLen;
                        this.mBearerData = parameterData;
                        if (parameterLen == 0) {
                            break;
                        } else {
                            byte[] messageId = new byte[6];
                            ByteArrayInputStream bais2 = new ByteArrayInputStream(this.mBearerData);
                            DataInputStream dis2 = new DataInputStream(bais2);
                            if (dis2.read(messageId, 0, 6) < 0) {
                                Log.e(LOG_TAG, "parseCdmaDeliverPdu: messageId - the end of the stream has been reached.");
                            }
                            try {
                                decodeMessageId(new BitwiseInputStream(messageId));
                            } catch (BitwiseInputStream.AccessException e) {
                                e.printStackTrace();
                            }
                            bais2.close();
                            dis2.close();
                            break;
                        }
                    default:
                        throw new Exception("unsupported parameterId (" + parameterId + ")");
                }
            }
            bais.close();
            dis.close();
        } catch (Exception ex) {
            Log.e(LOG_TAG, "parseCdmaDeliverPdu: conversion from pdu to SmsMessage failed" + ex);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 14 */
    public void parseOutgoingCdmaSms(byte[] moPdu) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(moPdu));
        int pduLength = moPdu.length;
        try {
            this.mTeleServiceid = dis.readInt();
            dis.readInt();
            this.mServiceCategory = dis.readInt();
            this.mDigitMode = dis.readByte();
            this.mNumberMode = dis.readByte();
            this.mNetworktype = dis.readByte();
            this.mNumberPlan = dis.readByte();
            byte readByte = dis.readByte();
            this.mNoOfAddressDigit = readByte;
            if (readByte <= pduLength) {
                byte[] parameterData = new byte[pduLength];
                if (dis.read(parameterData, 0, readByte) < 0) {
                    Log.d(LOG_TAG, "parseOutgoingCdmaSms: parameterData - the end of the stream has been reached.");
                }
                BitwiseInputStream addrBis = new BitwiseInputStream(parameterData);
                byte[] data = new byte[this.mNoOfAddressDigit];
                this.mAddressByte = new byte[this.mNoOfAddressDigit];
                if (this.mDigitMode == 0) {
                    for (int index = 0; index < this.mNoOfAddressDigit; index++) {
                        addrBis.read(4);
                        data[index] = convertDtmfToAscii((byte) (addrBis.read(4) & 15));
                    }
                } else if (this.mDigitMode == 1) {
                    for (int index2 = 0; index2 < this.mNoOfAddressDigit; index2++) {
                        data[index2] = (byte) (addrBis.read(8) & 255);
                    }
                }
                this.mAddressByte = data;
                this.mDestAddress = new String(this.mAddressByte);
                dis.readByte();
                dis.readByte();
                dis.readByte();
                int readUnsignedByte = dis.readUnsignedByte();
                this.mBearerDataLength = readUnsignedByte;
                if (readUnsignedByte <= pduLength) {
                    byte[] bArr = new byte[readUnsignedByte];
                    this.mBearerData = bArr;
                    if (dis.read(bArr, 0, readUnsignedByte) < 0) {
                        Log.d(LOG_TAG, "parseOutgoingCdmaSms: parameterData - the end of the stream has been reached.");
                    }
                    decodeBearerData(this.mBearerData);
                    dis.close();
                    return;
                }
                throw new RuntimeException("parseOutgoingCdmaSms: Invalid pdu, bearerDataLength " + this.mBearerDataLength + " > pdu len " + pduLength);
            }
            throw new RuntimeException("createFromPdu: Invalid pdu, addr.numberOfDigits " + this.mNoOfAddressDigit + " > pdu len " + pduLength);
        } catch (IOException ex) {
            throw new RuntimeException("parseOutgoingCdmaSms1: conversion from byte array to object failed: " + ex, ex);
        } catch (Exception ex2) {
            Log.e(LOG_TAG, "parseOutgoingCdmaSms2: conversion from byte array to object failed: " + ex2);
        }
    }

    public void parseOutgoingGsmSms() {
        this.mScAddress = getSCAddress();
        Log.d(LOG_TAG, "parseOutgoingGsmSms() : mScAddress " + this.mScAddress);
        int firstByte = getByte();
        int mti = firstByte & 3;
        this.mStatusReportRequested = (firstByte & 32) == 32;
        if (mti == 1) {
            this.mCur++;
            this.mDestAddress = getGsmAddress();
        }
    }

    public int getMessageType() {
        return this.mMsgType;
    }

    public boolean getStatusReportRequested() {
        return this.mStatusReportRequested;
    }

    public void parseDeliverPdu(byte[] data, String format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(300);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.write(data);
            this.mTpdu = baos.toByteArray();
            if (format.equals(FORMAT_3GPP)) {
                this.mScAddress = getSCAddress();
                int mti = getByte() & 3;
                if (mti != 0) {
                    if (mti == 2) {
                        this.mMsgType = 2;
                        this.mMessageRef = getByte();
                        return;
                    } else if (mti != 3) {
                        return;
                    }
                }
                this.mMsgType = 1;
                return;
            }
            this.mMessageRef = getByte();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "getMessageType: conversion from byte array to object failed: " + ex);
            try {
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getMessageRef() {
        return this.mMessageRef;
    }

    private String getGsmAddress() {
        byte[] bArr = this.mTpdu;
        int i = this.mCur;
        int addressLength = bArr[i] & 255;
        int lengthBytes = ((addressLength + 1) / 2) + 2;
        byte[] origBytes = new byte[lengthBytes];
        System.arraycopy(bArr, i, origBytes, 0, lengthBytes);
        if ((origBytes[1] & 255) == 5) {
            return GsmAlphabet.gsm7BitPackedToString(origBytes, 2, (addressLength * 4) / 7);
        }
        byte lastByte = origBytes[lengthBytes - 1];
        if ((addressLength & 1) == 1) {
            int i2 = lengthBytes - 1;
            origBytes[i2] = (byte) (origBytes[i2] | 240);
        }
        String result = PhoneNumberUtils.calledPartyBCDToString(origBytes, 1, lengthBytes - 1);
        origBytes[lengthBytes - 1] = lastByte;
        return result;
    }

    private String getSCAddress() {
        String ret;
        int len = getByte();
        if (len == 0) {
            ret = null;
        } else {
            try {
                ret = PhoneNumberUtils.calledPartyBCDToString(this.mTpdu, this.mCur, len);
            } catch (RuntimeException tr) {
                Log.d(LOG_TAG, "invalid SC address: ", tr);
                ret = null;
            }
        }
        this.mCur += len;
        return ret;
    }

    private int getByte() {
        byte[] bArr = this.mTpdu;
        int i = this.mCur;
        this.mCur = i + 1;
        return bArr[i] & 255;
    }

    public static byte convertDtmfToAscii(byte dtmfDigit) {
        switch (dtmfDigit) {
            case 0:
                return 48;
            case 1:
                return 49;
            case 2:
                return 50;
            case 3:
                return 51;
            case 4:
                return 52;
            case 5:
                return 53;
            case 6:
                return 54;
            case 7:
                return 55;
            case 8:
                return 56;
            case 9:
                return 57;
            case 10:
                return 48;
            case 11:
                return 42;
            case 12:
                return 35;
            case 13:
                return 65;
            case 14:
                return 66;
            case 15:
                return 67;
            default:
                return 32;
        }
    }

    private void parseCdmaAddress(byte[] address) {
        int digitSize;
        BitwiseInputStream bis = new BitwiseInputStream(address);
        try {
            this.mDigitMode = bis.read(1);
            this.mNumberMode = bis.read(1);
            if (this.mDigitMode == 0) {
                digitSize = 4;
            } else {
                digitSize = 8;
            }
            if (this.mDigitMode == 1) {
                this.mNetworktype = bis.read(3);
            }
            if (this.mDigitMode == 1 && this.mNumberMode == 0) {
                this.mNumberPlan = bis.read(4);
            }
            int read = bis.read(8);
            this.mNoOfAddressDigit = read;
            this.mAddressByte = new byte[read];
            for (int i = 0; i < this.mNoOfAddressDigit; i++) {
                this.mAddressByte[i] = (byte) (bis.read(digitSize) + 48);
                if (this.mAddressByte[i] == 58) {
                    this.mAddressByte[i] = 48;
                }
            }
        } catch (BitwiseInputStream.AccessException e) {
            Log.e(LOG_TAG, "bitwiseinputstream exception is thrown");
        }
    }

    private byte[] encodeCdmaAddress(int paramId) {
        int digitSize;
        BitwiseOutputStream bos = new BitwiseOutputStream(50);
        try {
            bos.write(8, paramId);
            bos.write(8, getAddressParameterLength());
            bos.write(1, this.mDigitMode);
            bos.write(1, this.mNumberMode);
            if (this.mDigitMode == 1) {
                bos.write(3, this.mNetworktype);
            }
            if (this.mDigitMode == 1 && this.mNumberMode == 0) {
                bos.write(4, this.mNumberPlan);
            }
            bos.write(8, this.mNoOfAddressDigit);
            if (this.mDigitMode == 0) {
                digitSize = 4;
            } else {
                digitSize = 8;
            }
            for (int i = 0; i < this.mNoOfAddressDigit; i++) {
                if (digitSize == 4) {
                    bos.write(digitSize, parseToDtmf(this.mAddressByte[i]));
                } else {
                    bos.write(digitSize, this.mAddressByte[i]);
                }
            }
            return bos.toByteArray();
        } catch (BitwiseOutputStream.AccessException e) {
            Log.e(LOG_TAG, "bitwise exception is thrown");
            e.printStackTrace();
            return null;
        }
    }

    private static int parseToDtmf(byte addressByte) {
        if (addressByte >= 49 && addressByte <= 57) {
            return addressByte - 48;
        }
        if (addressByte == 48) {
            return 10;
        }
        if (addressByte == 42) {
            return 11;
        }
        if (addressByte == 35) {
            return 12;
        }
        return 0;
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    public void decodeBearerData(byte[] smsData) {
        try {
            BitwiseInputStream inStream = new BitwiseInputStream(smsData);
            int foundSubparamMask = 0;
            int userDatalength = 0;
            while (inStream.available() > 0) {
                int subparamId = inStream.read(8);
                int subpramlength = inStream.read(8);
                int subparamIdBit = 1 << subparamId;
                Log.d(LOG_TAG, "subparamId = " + subparamId + " length = " + subpramlength);
                if ((foundSubparamMask & subparamIdBit) != 0 && subparamId >= 0) {
                    if (subparamId <= 23) {
                        throw new CodingException("illegal duplicate subparameter (" + subparamId + ")");
                    }
                }
                if (subparamId == 0) {
                    for (int i = 0; i < subpramlength; i++) {
                        inStream.read(8);
                    }
                } else if (subparamId == 1) {
                    userDatalength = subpramlength;
                    for (int i2 = 0; i2 < subpramlength; i2++) {
                        inStream.read(8);
                    }
                } else if (subparamId != 10) {
                    for (int i3 = 0; i3 < subpramlength; i3++) {
                        inStream.read(8);
                    }
                } else {
                    decodeReplyOption(inStream);
                }
                if (1 != 0 && subparamId >= 0 && subparamId <= 23) {
                    foundSubparamMask |= subparamIdBit;
                }
            }
            if ((foundSubparamMask & 1) == 0) {
                throw new CodingException("missing MESSAGE_IDENTIFIER subparam");
            } else if (userDatalength != 0 && this.mUserDataHeader == 1) {
                Log.e(LOG_TAG, "UserData has header");
            }
        } catch (BitwiseInputStream.AccessException ex) {
            Log.e(LOG_TAG, "BearerData decode failed: " + ex);
        } catch (CodingException ex2) {
            Log.e(LOG_TAG, "BearerData decode failed: " + ex2);
        }
    }

    private void decodeMessageId(BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        inStream.skip(8);
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 24) {
            int paramBits2 = paramBits - 24;
            int bearerMsgType = inStream.read(4);
            int hasUserDataHeader = inStream.read(4);
            this.mMsgId = (inStream.read(8) << 8) | inStream.read(8);
            this.mMsgType = bearerMsgType;
            this.mUserDataHeader = hasUserDataHeader;
            if (paramBits2 > 0) {
                Log.d(LOG_TAG, "MESSAGE_IDENTIFIER decode succeeded (extra bits = " + paramBits2 + ")");
            }
            inStream.skip(paramBits2);
        }
    }

    private void decodeReplyOption(BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        inStream.read(1);
        this.mStatusReportRequested = inStream.read(1) == 1;
        inStream.read(1);
        inStream.read(1);
        inStream.read(4);
    }

    private int getAddressParameterLength() {
        int digitSize;
        int numOfBits = 0 + 1 + 1;
        if (this.mDigitMode == 1) {
            numOfBits += 3;
        }
        if (this.mDigitMode == 1 && this.mNumberMode == 0) {
            numOfBits += 4;
        }
        int numOfBits2 = numOfBits + 8;
        if (this.mDigitMode == 0) {
            digitSize = 4;
        } else {
            digitSize = 8;
        }
        int numOfBits3 = numOfBits2 + (this.mAddressByte.length * digitSize);
        if (numOfBits3 % 8 == 0) {
            return numOfBits3 / 8;
        }
        return 1 + (numOfBits3 / 8);
    }

    public String getDestinationAddress() {
        return this.mDestAddress;
    }

    public byte[] getAddressBytes() {
        return this.mAddressByte;
    }

    public int getMsgID() {
        return this.mMsgId;
    }

    public int getContentType() {
        return this.mContentType;
    }

    public byte[] getTpdu() {
        return this.mTpdu;
    }

    public int getErrorCause() {
        return this.mCauseCode;
    }

    public int getErrorClass() {
        return this.mErrorClass;
    }

    public String getServiceCenterAddress() {
        return this.mScAddress;
    }
}
