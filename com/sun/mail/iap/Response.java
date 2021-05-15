package com.sun.mail.iap;

import com.sun.mail.util.ASCIIUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

public class Response {
    public static final int BAD = 12;
    public static final int BYE = 16;
    public static final int CONTINUATION = 1;
    public static final int NO = 8;
    public static final int OK = 4;
    public static final int SYNTHETIC = 32;
    public static final int TAGGED = 2;
    public static final int TAG_MASK = 3;
    public static final int TYPE_MASK = 28;
    public static final int UNTAGGED = 3;
    private static final int increment = 100;
    protected byte[] buffer = null;
    protected int index;
    protected int pindex;
    protected int size;
    protected String tag = null;
    protected int type = 0;

    public Response(String s) {
        byte[] bytes = ASCIIUtility.getBytes(s);
        this.buffer = bytes;
        this.size = bytes.length;
        parse();
    }

    public Response(Protocol p) throws IOException, ProtocolException {
        ByteArray response = p.getInputStream().readResponse(p.getResponseBuffer());
        this.buffer = response.getBytes();
        this.size = response.getCount() - 2;
        parse();
    }

    public Response(Response r) {
        this.index = r.index;
        this.size = r.size;
        this.buffer = r.buffer;
        this.type = r.type;
        this.tag = r.tag;
    }

    public static Response byeResponse(Exception ex) {
        Response r = new Response(("* BYE JavaMail Exception: " + ex.toString()).replace(13, ' ').replace(10, ' '));
        r.type = 32 | r.type;
        return r;
    }

    private void parse() {
        this.index = 0;
        byte[] bArr = this.buffer;
        if (bArr[0] == 43) {
            this.type |= 1;
            this.index = 0 + 1;
            return;
        }
        if (bArr[0] == 42) {
            this.type |= 3;
            this.index = 0 + 1;
        } else {
            this.type |= 2;
            this.tag = readAtom();
        }
        int mark = this.index;
        String s = readAtom();
        if (s == null) {
            s = "";
        }
        if (s.equalsIgnoreCase("OK")) {
            this.type |= 4;
        } else if (s.equalsIgnoreCase("NO")) {
            this.type |= 8;
        } else if (s.equalsIgnoreCase("BAD")) {
            this.type |= 12;
        } else if (s.equalsIgnoreCase("BYE")) {
            this.type |= 16;
        } else {
            this.index = mark;
        }
        this.pindex = this.index;
    }

    public void skipSpaces() {
        while (true) {
            int i = this.index;
            if (i < this.size && this.buffer[i] == 32) {
                this.index = i + 1;
            } else {
                return;
            }
        }
    }

    public void skipToken() {
        while (true) {
            int i = this.index;
            if (i < this.size && this.buffer[i] != 32) {
                this.index = i + 1;
            } else {
                return;
            }
        }
    }

    public void skip(int count) {
        this.index += count;
    }

    public byte peekByte() {
        int i = this.index;
        if (i < this.size) {
            return this.buffer[i];
        }
        return 0;
    }

    public byte readByte() {
        int i = this.index;
        if (i >= this.size) {
            return 0;
        }
        byte[] bArr = this.buffer;
        this.index = i + 1;
        return bArr[i];
    }

    public String readAtom() {
        return readAtom(0);
    }

    public String readAtom(char delim) {
        skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        int start = this.index;
        while (true) {
            int i = this.index;
            if (i >= this.size) {
                break;
            }
            byte b = this.buffer[i];
            byte b2 = b;
            if (b <= 32 || b2 == 40 || b2 == 41 || b2 == 37 || b2 == 42 || b2 == 34 || b2 == 92 || b2 == Byte.MAX_VALUE || (delim != 0 && b2 == delim)) {
                break;
            }
            this.index++;
        }
        return ASCIIUtility.toString(this.buffer, start, this.index);
    }

    public String readString(char delim) {
        skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        int start = this.index;
        while (true) {
            int i = this.index;
            if (i < this.size && this.buffer[i] != delim) {
                this.index = i + 1;
            }
        }
        return ASCIIUtility.toString(this.buffer, start, this.index);
    }

    public String[] readStringList() {
        byte[] bArr;
        int i;
        skipSpaces();
        byte[] bArr2 = this.buffer;
        int i2 = this.index;
        if (bArr2[i2] != 40) {
            return null;
        }
        this.index = i2 + 1;
        Vector v = new Vector();
        do {
            v.addElement(readString());
            bArr = this.buffer;
            i = this.index;
            this.index = i + 1;
        } while (bArr[i] != 41);
        int size2 = v.size();
        if (size2 <= 0) {
            return null;
        }
        String[] s = new String[size2];
        v.copyInto(s);
        return s;
    }

    public int readNumber() {
        skipSpaces();
        int start = this.index;
        while (true) {
            int i = this.index;
            if (i >= this.size || !Character.isDigit((char) this.buffer[i])) {
                int i2 = this.index;
            } else {
                this.index++;
            }
        }
        int i22 = this.index;
        if (i22 <= start) {
            return -1;
        }
        try {
            return ASCIIUtility.parseInt(this.buffer, start, i22);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public long readLong() {
        skipSpaces();
        int start = this.index;
        while (true) {
            int i = this.index;
            if (i >= this.size || !Character.isDigit((char) this.buffer[i])) {
                int i2 = this.index;
            } else {
                this.index++;
            }
        }
        int i22 = this.index;
        if (i22 <= start) {
            return -1;
        }
        try {
            return ASCIIUtility.parseLong(this.buffer, start, i22);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String readString() {
        return (String) parseString(false, true);
    }

    public ByteArrayInputStream readBytes() {
        ByteArray ba = readByteArray();
        if (ba != null) {
            return ba.toByteArrayInputStream();
        }
        return null;
    }

    public ByteArray readByteArray() {
        if (!isContinuation()) {
            return (ByteArray) parseString(false, false);
        }
        skipSpaces();
        byte[] bArr = this.buffer;
        int i = this.index;
        return new ByteArray(bArr, i, this.size - i);
    }

    public String readAtomString() {
        return (String) parseString(true, true);
    }

    private Object parseString(boolean parseAtoms, boolean returnString) {
        byte[] bArr;
        int i;
        byte[] bArr2;
        int i2;
        skipSpaces();
        byte[] bArr3 = this.buffer;
        int i3 = this.index;
        byte b = bArr3[i3];
        if (b == 34) {
            this.index = i3 + 1;
            int start = this.index;
            int copyto = this.index;
            while (true) {
                bArr2 = this.buffer;
                i2 = this.index;
                byte b2 = bArr2[i2];
                byte b3 = b2;
                if (b2 == 34) {
                    break;
                }
                if (b3 == 92) {
                    this.index = i2 + 1;
                }
                int i4 = this.index;
                if (i4 != copyto) {
                    byte[] bArr4 = this.buffer;
                    bArr4[copyto] = bArr4[i4];
                }
                copyto++;
                this.index++;
            }
            this.index = i2 + 1;
            if (returnString) {
                return ASCIIUtility.toString(bArr2, start, copyto);
            }
            return new ByteArray(bArr2, start, copyto - start);
        } else if (b == 123) {
            int start2 = i3 + 1;
            this.index = start2;
            while (true) {
                bArr = this.buffer;
                i = this.index;
                if (bArr[i] == 125) {
                    break;
                }
                this.index = i + 1;
            }
            try {
                int count = ASCIIUtility.parseInt(bArr, start2, i);
                int start3 = this.index + 3;
                this.index = start3 + count;
                if (returnString) {
                    return ASCIIUtility.toString(this.buffer, start3, start3 + count);
                }
                return new ByteArray(this.buffer, start3, count);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (parseAtoms) {
            int start4 = this.index;
            String s = readAtom();
            if (returnString) {
                return s;
            }
            return new ByteArray(this.buffer, start4, this.index);
        } else if (b != 78 && b != 110) {
            return null;
        } else {
            this.index += 3;
            return null;
        }
    }

    public int getType() {
        return this.type;
    }

    public boolean isContinuation() {
        return (this.type & 3) == 1;
    }

    public boolean isTagged() {
        return (this.type & 3) == 2;
    }

    public boolean isUnTagged() {
        return (this.type & 3) == 3;
    }

    public boolean isOK() {
        return (this.type & 28) == 4;
    }

    public boolean isNO() {
        return (this.type & 28) == 8;
    }

    public boolean isBAD() {
        return (this.type & 28) == 12;
    }

    public boolean isBYE() {
        return (this.type & 28) == 16;
    }

    public boolean isSynthetic() {
        return (this.type & 32) == 32;
    }

    public String getTag() {
        return this.tag;
    }

    public String getRest() {
        skipSpaces();
        return ASCIIUtility.toString(this.buffer, this.index, this.size);
    }

    public void reset() {
        this.index = this.pindex;
    }

    public String toString() {
        return ASCIIUtility.toString(this.buffer, 0, this.size);
    }
}
