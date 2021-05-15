package com.sec.internal.ims.core.iil;

import java.io.ByteArrayOutputStream;

public class IilImsPreference {
    private static final int IMS_PREFERENCE_NUMBERS = 14;
    private byte eccPreference;
    private byte enableSmsFallback;
    private byte enableSmsOverIms;
    private byte enableSmsWriteUicc;
    private byte eutranDomain;
    private byte imsSupportType;
    private byte smsFormat;
    private byte srvccVersion;
    private byte ssCsfb;
    private byte ssDomain;
    private byte supportVolteRoaming;
    private byte ussdDomain;
    private byte utranDomain;

    public IilImsPreference() {
        this.smsFormat = -1;
        this.enableSmsOverIms = -1;
        this.enableSmsWriteUicc = -1;
        this.enableSmsFallback = -1;
        this.eutranDomain = -1;
        this.utranDomain = -1;
        this.ssDomain = -1;
        this.ussdDomain = -1;
        this.eccPreference = -1;
        this.ssCsfb = -1;
        this.imsSupportType = -1;
        this.srvccVersion = -1;
        this.supportVolteRoaming = -1;
    }

    public IilImsPreference(byte testValue) {
        this.smsFormat = testValue;
        this.enableSmsOverIms = testValue;
        this.enableSmsWriteUicc = testValue;
        this.enableSmsFallback = testValue;
        this.eutranDomain = testValue;
        this.utranDomain = testValue;
        this.ssDomain = testValue;
        this.ussdDomain = testValue;
        this.eccPreference = testValue;
        this.ssCsfb = testValue;
        this.imsSupportType = testValue;
        this.srvccVersion = testValue;
        this.supportVolteRoaming = testValue;
    }

    public void setSmsFormat(byte format) {
        this.smsFormat = format;
    }

    public void setSmsOverIms(byte enableOverIms) {
        this.enableSmsOverIms = enableOverIms;
    }

    public void setSmsWriteUicc(byte enableWriteUicc) {
        this.enableSmsWriteUicc = enableWriteUicc;
    }

    public void setSmsFallbackPreference(byte enableFallback) {
        this.enableSmsFallback = enableFallback;
    }

    public void setEutranDomain(byte domain) {
        this.eutranDomain = domain;
    }

    public void setUtranDomain(byte domain) {
        this.utranDomain = domain;
    }

    public void setSsDomain(byte domain) {
        this.ssDomain = domain;
    }

    public void setUssdDomain(byte domain) {
        this.ussdDomain = domain;
    }

    public void setEccPreference(byte eccPrefer) {
        this.eccPreference = eccPrefer;
    }

    public void setSsCsfb(byte enableSsCsfb) {
        this.ssCsfb = enableSsCsfb;
    }

    public void setImsSupportType(byte imsSupportType2) {
        this.imsSupportType = imsSupportType2;
    }

    public void setSrvccVersion(byte srvccVersion2) {
        this.srvccVersion = srvccVersion2;
    }

    public void setSupportVolteRoaming(byte supportVolteRoaming2) {
        this.supportVolteRoaming = supportVolteRoaming2;
    }

    public byte getSmsFormat() {
        return this.smsFormat;
    }

    public byte getSmsOverIms() {
        return this.enableSmsOverIms;
    }

    public byte getSmsWriteUicc() {
        return this.enableSmsWriteUicc;
    }

    public byte getSmsFallbackPreference() {
        return this.enableSmsFallback;
    }

    public byte getEutranDomain() {
        return this.eutranDomain;
    }

    public byte getUtranDomain() {
        return this.utranDomain;
    }

    public byte getSsDomain() {
        return this.ssDomain;
    }

    public byte getUssdDomain() {
        return this.ussdDomain;
    }

    public byte getEccPreference() {
        return this.eccPreference;
    }

    public byte getSsCsfb() {
        return this.ssCsfb;
    }

    public byte getImsSupportType() {
        return this.imsSupportType;
    }

    public byte getSrvccVersion() {
        return this.srvccVersion;
    }

    public byte getSupportVolteRoaming() {
        return this.supportVolteRoaming;
    }

    public static byte[] toByteArray(IilImsPreference imsPreference, int notiType) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(112);
        outStream.write(imsPreference.getSmsFormat());
        outStream.write(imsPreference.getSmsOverIms());
        outStream.write(imsPreference.getSmsWriteUicc());
        outStream.write(imsPreference.getSmsFallbackPreference());
        outStream.write(imsPreference.getEutranDomain());
        outStream.write(imsPreference.getUtranDomain());
        outStream.write(imsPreference.getSsDomain());
        outStream.write(imsPreference.getUssdDomain());
        outStream.write(imsPreference.getEccPreference());
        outStream.write(imsPreference.getSsCsfb());
        outStream.write(imsPreference.getImsSupportType());
        outStream.write(imsPreference.getSrvccVersion());
        outStream.write(imsPreference.getSupportVolteRoaming());
        outStream.write((byte) notiType);
        return outStream.toByteArray();
    }

    public String toString() {
        return "smsFormat " + this.smsFormat + " enableSmsOverIms " + this.enableSmsOverIms + " enableSmsWriteUicc " + this.enableSmsWriteUicc + " enableSmsFallback " + this.enableSmsFallback + " eutranDomain " + this.eutranDomain + " utranDomain " + this.utranDomain + " ssDomain " + this.ssDomain + " ussdDomain " + this.ussdDomain + " eccPreference " + this.eccPreference + " ssCsfb " + this.ssCsfb + " imsSupportType " + this.imsSupportType + " srvccVersion " + this.srvccVersion + " supportVolteRoaming " + this.supportVolteRoaming;
    }
}
