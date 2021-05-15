package com.sec.internal.constants.ims.cmstore;

import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;

public class TMOConstants {

    public static class CallLogTypes {
        public static final String AUDIO = "audio";
        public static final String VIDEO = "video";
    }

    public static class TMOCmStrategy {
        public static final String FAX_FOLDER = "/Media Folder/Fax Media";
    }

    public static class TmoGcmPnsVariables {
        public static final String CALL = "Call";
        public static final String CHAT = "Chat";
        public static final String FAX = "Fax";
        public static final String FAX_D = "FaxD";
        public static final String FILE_TRANSFER = "FileTransfer";
        public static final String FULL_SYNC = "FullSync";
        public static final String GSO = "GSO";
        public static final String HISTORY = "History";
        public static final String IMDN = "IMDN";
        public static final String LMM = "LMM";
        public static final String MMS = "MMS";
        public static final String MOMT = "MOMT";
        public static final String NOTIFY = "Notify";
        public static final String RCS_PAGE = "RCSPage";
        public static final String RCS_SESSION = "RCSSession";
        public static final String SMS = "SMS";
        public static final String VM = "VM";
        public static final String VMTT = "VMTT";
        public static final String VVG = "VVG";
        public static final String VVM = "VVM";
        public static final String VVME = "VVME";
        public static final String VVMP = "VVMP";
    }

    public static class TmoHttpHeaderNames {
        public static final String DEVICE_ID = "device_id";
    }

    public static class TmoMessageContextValues extends MessageContextValues {
        public static final String callListMessage = "X-Call-History";
        public static final String chatMessage = "X-RCS-Chat";
        public static final String fileMessage = "X-RCS-FT";
        public static final String greetingvoice = "x-voice-grtng";
        public static final String gsomessage = "X-RCS-Chat-GSO";
        public static final String gsosession = "X-RCS-Chat-Session";
        public static final String imdnMessage = "imdn-message";
        public static final String standaloneMessageLLM = "X-RCS-LM";
        public static final String standaloneMessagePager = "X-RCS-PM";
    }
}
