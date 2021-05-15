package com.sec.internal.ims.cmstore.omanetapi.nms.data;

import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.AttributeList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeTranslator implements IMessageAttributeInterface {
    private List<Attribute> mAttributeList = new ArrayList();
    private Map<String, String> mMessageAttributeRegistration;

    public AttributeTranslator(ICloudMessageManagerHelper iClouldMessageManagerHelper) {
        this.mMessageAttributeRegistration = iClouldMessageManagerHelper.getMessageAttributeRegistration();
    }

    public AttributeList getAttributeList() {
        List<Attribute> list = this.mAttributeList;
        AttributeList attrList = new AttributeList();
        attrList.attribute = (Attribute[]) list.toArray(new Attribute[this.mAttributeList.size()]);
        return attrList;
    }

    public void setDate(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DATE)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DATE);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setFrom(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.FROM)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.FROM);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setTo(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.TO)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.TO);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setBCC(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.BCC)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.BCC);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setCC(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CC)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CC);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setDirection(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DIRECTION)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DIRECTION);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setMessageContext(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.MESSAGE_CONTEXT)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.MESSAGE_CONTEXT);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setSubject(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.SUBJECT)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.SUBJECT);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setOpenGroup(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.IS_OPEN_GROUP)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.IS_OPEN_GROUP);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setMessageId(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.MESSAGE_ID)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.MESSAGE_ID);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setMimeVersion(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.MIME_VERSION)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.MIME_VERSION);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setCpmGroup(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.IS_CPM_GROUP)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.IS_CPM_GROUP);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setClientCorrelator(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CLIENT_CORRELATOR)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CLIENT_CORRELATOR);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setContentType(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CONTENT_TYPE)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CONTENT_TYPE);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setConversationId(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CONVERSATION_ID)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CONVERSATION_ID);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setDispositionStatus(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_STATUS)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_STATUS);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setDispositionType(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_TYPE)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_TYPE);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setDispositionOriginalMessageID(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setDispositionOriginalTo(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setReportRequested(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.REPORT_REQUESTED)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.REPORT_REQUESTED);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setPwd(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.PWD)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.PWD);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setOldPwd(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.OLD_PWD)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.OLD_PWD);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setGreetingType(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.X_CNS_GREETING_TYPE)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.X_CNS_GREETING_TYPE);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setContentDuration(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CONTENT_DURATION)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CONTENT_DURATION);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setEmailAddress(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.EMAILADDRESS)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.EMAILADDRESS);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }

    public void setVVMOn(String[] value) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.VVMOn)) {
            Attribute attr = new Attribute();
            attr.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.VVMOn);
            attr.value = value;
            this.mAttributeList.add(attr);
        }
    }
}
