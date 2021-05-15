package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import java.util.List;

public class ImSessionConferenceInfoUpdateEvent {
    public final String mChatId;
    public final ImConferenceInfoType mConferenceInfoType;
    public final ImIconData mIconData;
    public final int mMaxUserCount;
    public final String mOwnImsi;
    public final List<ImConferenceParticipantInfo> mParticipantsInfo;
    public final Object mRawHandle;
    public final ImSubjectData mSubjectData;

    public enum ImConferenceInfoType {
        FULL,
        PARTIAL,
        DELETED
    }

    public ImSessionConferenceInfoUpdateEvent(String chatId, ImConferenceInfoType conferenceInfoType, List<ImConferenceParticipantInfo> participantsInfo, int maxUserCount, ImSubjectData subjectData, Object rawHandle, String ownImsi, ImIconData iconData) {
        this.mChatId = chatId;
        this.mConferenceInfoType = conferenceInfoType;
        this.mParticipantsInfo = participantsInfo;
        this.mMaxUserCount = maxUserCount;
        this.mSubjectData = subjectData;
        this.mIconData = iconData;
        this.mRawHandle = rawHandle;
        this.mOwnImsi = ownImsi;
    }

    public String toString() {
        return "ImSessionConferenceInfoUpdateEvent [mChatId=" + this.mChatId + ", mConferenceInfoType=" + this.mConferenceInfoType + ", mParticipantsInfo=" + this.mParticipantsInfo + ", mMaxUserCount=" + this.mMaxUserCount + ", mSubjectData=" + this.mSubjectData + ", mIconData=" + this.mIconData + ", mRawHandle=" + this.mRawHandle + "]";
    }
}
