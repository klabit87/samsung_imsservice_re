package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class GenericEDNSOption extends EDNSOption {
    private byte[] data;

    GenericEDNSOption(int code) {
        super(code);
    }

    public GenericEDNSOption(int code, byte[] data2) {
        super(code);
        this.data = Record.checkByteArrayLength("option data", data2, Message.MAXLENGTH);
    }

    /* access modifiers changed from: package-private */
    public void optionFromWire(DNSInput in) throws IOException {
        this.data = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void optionToWire(DNSOutput out) {
        out.writeByteArray(this.data);
    }

    /* access modifiers changed from: package-private */
    public String optionToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<");
        stringBuffer.append(base16.toString(this.data));
        stringBuffer.append(">");
        return stringBuffer.toString();
    }
}
