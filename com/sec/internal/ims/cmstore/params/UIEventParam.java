package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.ATTConstants;

public class UIEventParam {
    public String mMessage;
    public ATTConstants.AttAmbsUIScreenNames mUIScreen;

    public UIEventParam(ATTConstants.AttAmbsUIScreenNames uiScreen, String message) {
        this.mUIScreen = uiScreen;
        this.mMessage = message;
    }

    public String toString() {
        return "UIEventParam [mUIScreen=" + this.mUIScreen + ", mMessage=" + this.mMessage + "]";
    }
}
