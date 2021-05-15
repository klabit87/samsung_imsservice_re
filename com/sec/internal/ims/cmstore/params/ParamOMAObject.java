package com.sec.internal.ims.cmstore.params;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.TmoPushNotificationRecipients;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ParamOMAObject {
    private static final String TAG = ParamOMAObject.class.getSimpleName();
    public String CALL_DURATION = null;
    public String CALL_STARTTIMESTAMP = null;
    public String CALL_TYPE = null;
    public String CONTENT_DURATION = null;
    public String CONTENT_TYPE = null;
    public String CONTRIBUTION_ID = null;
    public String CONVERSATION_ID = null;
    public String DATE = null;
    public String DIRECTION = null;
    public String DISPOSITION_ORIGINAL_MESSAGEID = null;
    public String DISPOSITION_ORIGINAL_TO = null;
    public String DISPOSITION_STATUS = null;
    public String FROM = null;
    public boolean IS_CPM_GROUP = false;
    public boolean IS_OPEN_GROUP = false;
    public String MESSAGE_ID = null;
    public String MULTIPARTCONTENTTYPE = null;
    public String PARTICIPATING_DEVICE = null;
    public String SUBJECT = null;
    public String TEXT_CONTENT = null;
    public ArrayList<String> TO = new ArrayList<>();
    public String X_CNS_Greeting_Type = null;
    public String correlationId;
    public String correlationTag;
    public Long lastModSeq;
    public CloudMessageBufferDBConstants.ActionStatusFlag mFlag;
    public FlagList mFlagList = null;
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;
    public boolean mIsFromChangedObj = false;
    public boolean mIsGoforwardSync;
    public String mLine = null;
    public Set<ImsUri> mNomalizedOtherParticipants;
    public int mObjectType;
    private String mRawFromString = null;
    public boolean mReassembled = false;
    public URL parentFolder;
    public String parentFolderPath;
    public String path;
    public PayloadPartInfo[] payloadPart;
    public URL payloadURL;
    public URL resourceURL;

    private CloudMessageBufferDBConstants.ActionStatusFlag getCloudActionPerFlag(FlagList fglist) {
        Log.d(TAG, "getCloudActionPerFlag: " + fglist);
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
        if (fglist == null || fglist.flag == null) {
            return action;
        }
        if (this.mIsGoforwardSync) {
            for (String equalsIgnoreCase : fglist.flag) {
                if (equalsIgnoreCase.equalsIgnoreCase(FlagNames.Seen) && CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId() > action.getId()) {
                    action = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                }
            }
        } else {
            for (int i = 0; i < fglist.flag.length; i++) {
                CloudMessageBufferDBConstants.ActionStatusFlag tempValue = CloudMessageBufferDBConstants.ActionStatusFlag.None;
                if (fglist.flag[i].equalsIgnoreCase(FlagNames.Seen)) {
                    tempValue = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                } else if (fglist.flag[i].equalsIgnoreCase(FlagNames.Deleted)) {
                    tempValue = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                }
                if (tempValue.getId() > action.getId()) {
                    action = tempValue;
                }
            }
        }
        return action;
    }

    public ParamOMAObject(Object objt, boolean isGoForwardSync, int dbIndex, ICloudMessageManagerHelper iClouldMessageManagerHelper) {
        this.mIsGoforwardSync = isGoForwardSync;
        this.parentFolder = objt.parentFolder;
        this.parentFolderPath = objt.parentFolderPath;
        this.mFlag = getCloudActionPerFlag(objt.flags);
        this.mFlagList = objt.flags;
        this.resourceURL = objt.resourceURL;
        if (TextUtils.isEmpty(objt.path)) {
            this.path = "";
        } else {
            this.path = objt.path;
        }
        this.payloadURL = objt.payloadURL;
        this.payloadPart = objt.payloadPart;
        this.lastModSeq = objt.lastModSeq;
        this.correlationId = objt.correlationId;
        this.correlationTag = objt.correlationTag;
        this.mObjectType = dbIndex;
        this.mICloudMessageManagerHelper = iClouldMessageManagerHelper;
        this.mLine = Util.getLineTelUriFromObjUrl(this.resourceURL.toString());
        ArrayList<String> bccList = new ArrayList<>();
        for (int i = 0; i < objt.attributes.attribute.length; i++) {
            if (!(objt.attributes.attribute[i].name == null || objt.attributes.attribute[i].value == null)) {
                if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.DATE))) {
                    this.DATE = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.IS_CPM_GROUP))) {
                    this.IS_CPM_GROUP = "yes".equalsIgnoreCase(objt.attributes.attribute[i].value[0]);
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.IS_OPEN_GROUP))) {
                    this.IS_OPEN_GROUP = "yes".equalsIgnoreCase(objt.attributes.attribute[i].value[0]);
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.DIRECTION))) {
                    this.DIRECTION = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.FROM))) {
                    this.FROM = Util.getTelUri(objt.attributes.attribute[i].value[0]);
                    this.mRawFromString = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.TO))) {
                    if (objt.attributes.attribute[i].value != null) {
                        for (String telUri : objt.attributes.attribute[i].value) {
                            this.TO.add(Util.getTelUri(telUri));
                        }
                    }
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CC))) {
                    if (objt.attributes.attribute[i].value != null) {
                        for (String telUri2 : objt.attributes.attribute[i].value) {
                            this.TO.add(Util.getTelUri(telUri2));
                        }
                    }
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.BCC))) {
                    if (objt.attributes.attribute[i].value != null) {
                        for (String telUri3 : objt.attributes.attribute[i].value) {
                            bccList.add(Util.getTelUri(telUri3));
                        }
                    }
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.TEXT_CONTENT))) {
                    this.TEXT_CONTENT = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.SUBJECT))) {
                    this.SUBJECT = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CONVERSATION_ID))) {
                    this.CONVERSATION_ID = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CONTRIBUTION_ID))) {
                    this.CONTRIBUTION_ID = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.DISPOSITION_STATUS))) {
                    this.DISPOSITION_STATUS = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO))) {
                    this.DISPOSITION_ORIGINAL_TO = Util.getTelUri(objt.attributes.attribute[i].value[0]);
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID))) {
                    this.DISPOSITION_ORIGINAL_MESSAGEID = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.MULTIPARTCONTENTTYPE))) {
                    this.MULTIPARTCONTENTTYPE = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CONTENT_TYPE))) {
                    this.CONTENT_TYPE = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.MESSAGE_ID))) {
                    this.MESSAGE_ID = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CALL_DURATION))) {
                    this.CALL_DURATION = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CALL_STARTTIMESTAMP))) {
                    this.CALL_STARTTIMESTAMP = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CALL_TYPE))) {
                    this.CALL_TYPE = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.CONTENT_DURATION))) {
                    this.CONTENT_DURATION = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.PARTICIPATING_DEVICE))) {
                    this.PARTICIPATING_DEVICE = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.X_CNS_GREETING_TYPE))) {
                    this.X_CNS_Greeting_Type = objt.attributes.attribute[i].value[0];
                } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.MESSAGE_CONTEXT))) {
                    this.mObjectType = getMessageContextType(objt.attributes.attribute[i].value[0]);
                }
            }
        }
        if ("Out".equalsIgnoreCase(this.DIRECTION) && !bccList.isEmpty()) {
            this.TO.addAll(bccList);
        }
        if (this.mICloudMessageManagerHelper.shouldCorrectShortCode()) {
            recalculateCorrelationTag();
        }
        int i2 = this.mObjectType;
        if (16 == i2) {
            convertToOriginalUri(objt, i2);
        }
        int i3 = this.mObjectType;
        if (11 == i3 || 14 == i3) {
            updatePartcipantContentEmail();
        }
        this.mNomalizedOtherParticipants = getNormalizedParticipantsExcludeOwn();
    }

    private Set<ImsUri> getNormalizedParticipantsExcludeOwn() {
        Set<ImsUri> ret = new HashSet<>();
        Iterator<String> it = this.TO.iterator();
        while (it.hasNext()) {
            ImsUri value = ImsUri.parse(it.next());
            if (value != null) {
                ret.add(value);
            }
        }
        ret.add(ImsUri.parse(this.FROM));
        ret.remove(ImsUri.parse(this.mLine));
        return ret;
    }

    private void convertToOriginalUri(Object objt, int objectType) {
        Log.d(TAG, "convertToOriginalUri: " + objectType);
        this.TO.clear();
        for (int i = 0; i < objt.attributes.attribute.length; i++) {
            if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.FROM))) {
                this.FROM = objt.attributes.attribute[i].value[0];
            } else if (objt.attributes.attribute[i].name.equalsIgnoreCase(this.mICloudMessageManagerHelper.getMessageAttributeRegistration().get(IMessageAttributeInterface.TO)) && objt.attributes.attribute[i].value != null) {
                for (String add : objt.attributes.attribute[i].value) {
                    this.TO.add(add);
                }
            }
        }
    }

    private int getMessageContextType(String value) {
        String str = TAG;
        Log.d(str, "getMessageContextType: " + value);
        return this.mICloudMessageManagerHelper.getTypeUsingMessageContext(value);
    }

    public ParamOMAObject(ChangedObject objt, boolean isGoForwardSync, int dbIndex, String dataType) {
        boolean z = true;
        this.mIsFromChangedObj = true;
        this.mIsGoforwardSync = isGoForwardSync;
        this.parentFolder = objt.parentFolder;
        this.mFlag = getCloudActionPerFlag(objt.flags);
        this.mFlagList = objt.flags;
        this.resourceURL = objt.resourceURL;
        this.payloadURL = null;
        this.payloadPart = null;
        this.lastModSeq = objt.lastModSeq;
        this.correlationId = objt.correlationId;
        this.correlationTag = objt.correlationTag;
        if ("FT".equalsIgnoreCase(dataType)) {
            this.mObjectType = 12;
        } else if (CloudMessageProviderContract.DataTypes.CHAT.equalsIgnoreCase(dataType)) {
            this.mObjectType = 11;
        } else if ("GSO".equalsIgnoreCase(dataType)) {
            this.mObjectType = 34;
        } else {
            this.mObjectType = dbIndex;
        }
        this.mLine = Util.getLineTelUriFromObjUrl(this.resourceURL.toString());
        this.DATE = objt.extendedMessage.message_time;
        this.DIRECTION = objt.extendedMessage.direction;
        this.mReassembled = (objt.extendedMessage.reassembled == null || !CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(objt.extendedMessage.reassembled)) ? false : z;
        this.mRawFromString = objt.extendedMessage.sender;
        if ("In".equalsIgnoreCase(this.DIRECTION)) {
            this.FROM = Util.getTelUri(objt.extendedMessage.sender);
            for (TmoPushNotificationRecipients value : objt.extendedMessage.recipients) {
                this.TO.add(Util.getTelUri(value.uri));
            }
        } else {
            this.FROM = this.mLine;
            for (TmoPushNotificationRecipients value2 : objt.extendedMessage.recipients) {
                this.TO.add(Util.getTelUri(value2.uri));
            }
        }
        this.TEXT_CONTENT = objt.extendedMessage.content[0].content;
        this.CONTENT_TYPE = objt.extendedMessage.content[0].content_type;
        this.CONVERSATION_ID = objt.extendedMessage.content[0].rcsdata.conversation_id;
        this.CONTRIBUTION_ID = objt.extendedMessage.content[0].rcsdata.contribution_id;
        int i = this.mObjectType;
        if (11 == i || 14 == i) {
            updatePartcipantContentEmail();
        }
        this.mNomalizedOtherParticipants = getNormalizedParticipantsExcludeOwn();
    }

    private void updatePartcipantContentEmail() {
        String[] email;
        if (this.TEXT_CONTENT != null && (email = Util.parseEmailOverSlm(ImsUri.parse(this.mRawFromString), this.TEXT_CONTENT)) != null) {
            ImsUri parsedUri = ImsUri.parse("sip:" + email[0]);
            if (parsedUri != null) {
                this.FROM = parsedUri.toString();
            }
            this.TEXT_CONTENT = email[1];
        }
    }

    private void recalculateCorrelationTag() {
        if ("IN".equalsIgnoreCase(this.DIRECTION) && AmbsUtils.isInvalidShortCode(this.mRawFromString) && !TextUtils.isEmpty(this.correlationTag)) {
            String substring = this.mRawFromString.substring(1);
            this.mRawFromString = substring;
            this.FROM = Util.getTelUri(substring);
            this.correlationTag = AmbsUtils.generateSmsHashCode(this.mRawFromString, 1, this.TEXT_CONTENT);
            Log.d(TAG, "recalculateCorrelationTag: ");
        }
    }

    public String toString() {
        return "OMAConvertedObjectParam [mObjectType=" + this.mObjectType + ", mLine= " + IMSLog.checker(this.mLine) + ", correlationId= " + this.correlationId + ", correlationTag=" + this.correlationTag + ", resourceURL=" + IMSLog.checker(this.resourceURL) + ", mFlag=" + this.mFlag + ", DISPOSITION_STATUS=" + this.DISPOSITION_STATUS + ", mIsGoforwardSync=" + this.mIsGoforwardSync + ", TEXT_CONTENT=" + this.TEXT_CONTENT + ", DIRECTION=" + this.DIRECTION + ", DATE=" + this.DATE + ", MESSAGE_ID=" + this.MESSAGE_ID + ", CONTENT_TYPE=" + this.CONTENT_TYPE + ", X_CNS_Greeting_Type=" + this.X_CNS_Greeting_Type + ", mReassembled=" + this.mReassembled + ", mIsFromChangeObj=" + this.mIsFromChangedObj + " mNomalizedOtherParticipants=" + this.mNomalizedOtherParticipants + ", SUBJECT=" + this.SUBJECT + ", IS_CPM_GROUP=" + this.IS_CPM_GROUP + ", IS_OPEN_GROUP=" + this.IS_OPEN_GROUP + "]";
    }
}
