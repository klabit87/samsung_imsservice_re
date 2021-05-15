package com.sec.internal.ims.core.handler.secims;

import android.os.Bundle;
import android.os.Message;
import com.sec.ims.Dialog;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.util.HashMap;
import java.util.List;

public interface IStackIF {
    void acceptCallTransfer(int i, int i2, boolean z, int i3, String str, Message message);

    void answerCall(int i, int i2, int i3, String str);

    void cancelTransferCall(int i, int i2, Message message);

    void clearAllCallInternal(int i);

    void conference(int i, String str, int i2, String str2, String str3, String[] strArr, String str4, String str5, String str6, String str7, String str8, boolean z, Message message);

    void createUA(UaProfile uaProfile, Message message);

    void deleteTcpClientSocket(int i);

    void deleteUA(int i, Message message);

    void deregister(int i, boolean z, Message message);

    void endCall(int i, int i2, SipReason sipReason, Message message);

    void extendToConfCall(int i, String str, int i2, String str2, String str3, String[] strArr, int i3, String str4, String str5, String str6, String str7, String str8, boolean z);

    void handleCmcCsfb(int i, int i2);

    void handleDtmf(int i, int i2, int i3, int i4, int i5, Message message);

    void holdCall(int i, int i2, Message message);

    void holdVideo(int i, int i2, Message message);

    void makeCall(int i, String str, String str2, int i2, String str3, String str4, String str5, int i3, AdditionalContents additionalContents, String str6, String str7, HashMap<String, String> hashMap, String str8, boolean z, List<String> list, int i4, Bundle bundle, String str9, Message message);

    void mergeCall(int i, int i2, int i3, String str, int i4, String str2, String str3, String str4, String str5, String str6, String str7, String str8, boolean z, HashMap<String, String> hashMap, Message message);

    void modifyCallType(int i, int i2, int i3);

    void modifyVideoQuality(int i, int i2, int i3);

    void networkSuspended(int i, boolean z);

    void progressIncomingCall(int i, int i2, HashMap<String, String> hashMap, Message message);

    void publishDialog(int i, String str, String str2, String str3, int i2, Message message);

    void pullingCall(int i, String str, String str2, String str3, Dialog dialog, List<String> list, Message message);

    void register(int i, String str, int i2, int i3, List<String> list, List<String> list2, Capabilities capabilities, List<String> list3, String str2, String str3, Message message);

    void registerUaListener(int i, StackEventListener stackEventListener);

    void rejectCall(int i, int i2, SipError sipError, Message message);

    void rejectModifyCallType(int i, int i2);

    void replyModifyCallType(int i, int i2, int i3, int i4, String str);

    void requestPublish(int i, PresenceInfo presenceInfo, Message message);

    void requestUnpublish(int i);

    void resumeCall(int i, int i2, Message message);

    void resumeVideo(int i, int i2, Message message);

    void send(ResipStackRequest resipStackRequest);

    void sendAuthResponse(int i, int i2, String str);

    void sendCmcInfo(int i, int i2, AdditionalContents additionalContents);

    void sendInfo(int i, int i2, int i3, int i4, AdditionalContents additionalContents, Message message);

    void sendMediaEvent(int i, int i2, int i3, int i4);

    void sendSms(int i, String str, String str2, String str3, String str4, String str5, String str6, Message message);

    void sendSmsResponse(int i, String str, int i2);

    void sendSmsRpAckResponse(int i, String str);

    void sendText(int i, int i2, String str, int i3);

    void setPreferredImpu(int i, String str);

    void startCamera(int i, int i2, int i3);

    void startCmcRecord(int i, int i2, int i3, int i4, long j, int i5, String str, int i6, int i7, int i8, int i9, int i10, long j2, String str2);

    void startLocalRingBackTone(int i, int i2, int i3, int i4, Message message);

    void startRecord(int i, int i2, String str);

    void startVideoEarlyMedia(int i, int i2);

    void stopCamera(int i);

    void stopLocalRingBackTone(int i);

    void stopRecord(int i, int i2);

    void transferCall(int i, int i2, String str, int i3, Message message);

    void unRegisterUaListener(int i);

    void updateAudioInterface(int i, String str, Message message);

    void updateCall(int i, int i2, int i3, int i4, String str);

    void updateCmcExtCallCount(int i, int i2);

    void updateConfCall(int i, int i2, int i3, int i4, String str);

    void updateGeolocation(int i, LocationInfo locationInfo);

    void updatePani(int i, List<String> list);

    void updateRat(int i, int i2);

    void updateTimeInPlani(int i, long j);

    void updateVceConfig(int i, boolean z);
}
