package org.xbill.DNS;

import com.sec.internal.helper.header.AuthenticationHeaders;
import java.io.IOException;
import java.util.Arrays;

public abstract class EDNSOption {
    private final int code;

    /* access modifiers changed from: package-private */
    public abstract void optionFromWire(DNSInput dNSInput) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract String optionToString();

    /* access modifiers changed from: package-private */
    public abstract void optionToWire(DNSOutput dNSOutput);

    public static class Code {
        public static final int CLIENT_SUBNET = 8;
        public static final int NSID = 3;
        private static Mnemonic codes;

        private Code() {
        }

        static {
            Mnemonic mnemonic = new Mnemonic("EDNS Option Codes", 2);
            codes = mnemonic;
            mnemonic.setMaximum(Message.MAXLENGTH);
            codes.setPrefix("CODE");
            codes.setNumericAllowed(true);
            codes.add(3, "NSID");
            codes.add(8, "CLIENT_SUBNET");
        }

        public static String string(int code) {
            return codes.getText(code);
        }

        public static int value(String s) {
            return codes.getValue(s);
        }
    }

    public EDNSOption(int code2) {
        this.code = Record.checkU16(AuthenticationHeaders.HEADER_PARAM_CODE, code2);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(Code.string(this.code));
        sb.append(": ");
        sb.append(optionToString());
        sb.append("}");
        return sb.toString();
    }

    public int getCode() {
        return this.code;
    }

    /* access modifiers changed from: package-private */
    public byte[] getData() {
        DNSOutput out = new DNSOutput();
        optionToWire(out);
        return out.toByteArray();
    }

    static EDNSOption fromWire(DNSInput in) throws IOException {
        EDNSOption option;
        int code2 = in.readU16();
        int length = in.readU16();
        if (in.remaining() >= length) {
            int save = in.saveActive();
            in.setActive(length);
            if (code2 == 3) {
                option = new NSIDOption();
            } else if (code2 != 8) {
                option = new GenericEDNSOption(code2);
            } else {
                option = new ClientSubnetOption();
            }
            option.optionFromWire(in);
            in.restoreActive(save);
            return option;
        }
        throw new WireParseException("truncated option");
    }

    public static EDNSOption fromWire(byte[] b) throws IOException {
        return fromWire(new DNSInput(b));
    }

    /* access modifiers changed from: package-private */
    public void toWire(DNSOutput out) {
        out.writeU16(this.code);
        int lengthPosition = out.current();
        out.writeU16(0);
        optionToWire(out);
        out.writeU16At((out.current() - lengthPosition) - 2, lengthPosition);
    }

    public byte[] toWire() throws IOException {
        DNSOutput out = new DNSOutput();
        toWire(out);
        return out.toByteArray();
    }

    public boolean equals(Object arg) {
        if (arg == null || !(arg instanceof EDNSOption)) {
            return false;
        }
        EDNSOption opt = (EDNSOption) arg;
        if (this.code != opt.code) {
            return false;
        }
        return Arrays.equals(getData(), opt.getData());
    }

    public int hashCode() {
        int hashval = 0;
        for (byte b : getData()) {
            hashval += (hashval << 3) + (b & 255);
        }
        return hashval;
    }
}
