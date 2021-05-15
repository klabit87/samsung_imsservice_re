package org.xbill.DNS;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Message implements Cloneable {
    public static final int MAXLENGTH = 65535;
    static final int TSIG_FAILED = 4;
    static final int TSIG_INTERMEDIATE = 2;
    static final int TSIG_SIGNED = 3;
    static final int TSIG_UNSIGNED = 0;
    static final int TSIG_VERIFIED = 1;
    private static RRset[] emptyRRsetArray = new RRset[0];
    private static Record[] emptyRecordArray = new Record[0];
    private Header header;
    private TSIGRecord querytsig;
    private List[] sections;
    int sig0start;
    private int size;
    int tsigState;
    private int tsigerror;
    private TSIG tsigkey;
    int tsigstart;

    private Message(Header header2) {
        this.sections = new List[4];
        this.header = header2;
    }

    public Message(int id) {
        this(new Header(id));
    }

    public Message() {
        this(new Header());
    }

    public static Message newQuery(Record r) {
        Message m = new Message();
        m.header.setOpcode(0);
        m.header.setFlag(7);
        m.addRecord(r, 0);
        return m;
    }

    public static Message newUpdate(Name zone) {
        return new Update(zone);
    }

    Message(DNSInput in) throws IOException {
        this(new Header(in));
        boolean isUpdate = this.header.getOpcode() == 5;
        boolean truncated = this.header.getFlag(6);
        int i = 0;
        while (i < 4) {
            try {
                int count = this.header.getCount(i);
                if (count > 0) {
                    this.sections[i] = new ArrayList(count);
                }
                for (int j = 0; j < count; j++) {
                    int pos = in.current();
                    Record rec = Record.fromWire(in, i, isUpdate);
                    this.sections[i].add(rec);
                    if (i == 3) {
                        if (rec.getType() == 250) {
                            this.tsigstart = pos;
                        }
                        if (rec.getType() == 24 && ((SIGRecord) rec).getTypeCovered() == 0) {
                            this.sig0start = pos;
                        }
                    }
                }
                i++;
            } catch (WireParseException e) {
                if (!truncated) {
                    throw e;
                }
            }
        }
        this.size = in.current();
    }

    public Message(byte[] b) throws IOException {
        this(new DNSInput(b));
    }

    public Message(ByteBuffer byteBuffer) throws IOException {
        this(new DNSInput(byteBuffer));
    }

    public void setHeader(Header h) {
        this.header = h;
    }

    public Header getHeader() {
        return this.header;
    }

    public void addRecord(Record r, int section) {
        List[] listArr = this.sections;
        if (listArr[section] == null) {
            listArr[section] = new LinkedList();
        }
        this.header.incCount(section);
        this.sections[section].add(r);
    }

    public boolean removeRecord(Record r, int section) {
        List[] listArr = this.sections;
        if (listArr[section] == null || !listArr[section].remove(r)) {
            return false;
        }
        this.header.decCount(section);
        return true;
    }

    public void removeAllRecords(int section) {
        this.sections[section] = null;
        this.header.setCount(section, 0);
    }

    public boolean findRecord(Record r, int section) {
        List[] listArr = this.sections;
        return listArr[section] != null && listArr[section].contains(r);
    }

    public boolean findRecord(Record r) {
        for (int i = 1; i <= 3; i++) {
            List[] listArr = this.sections;
            if (listArr[i] != null && listArr[i].contains(r)) {
                return true;
            }
        }
        return false;
    }

    public boolean findRRset(Name name, int type, int section) {
        if (this.sections[section] == null) {
            return false;
        }
        for (int i = 0; i < this.sections[section].size(); i++) {
            Record r = (Record) this.sections[section].get(i);
            if (r.getType() == type && name.equals(r.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean findRRset(Name name, int type) {
        if (findRRset(name, type, 1) || findRRset(name, type, 2) || findRRset(name, type, 3)) {
            return true;
        }
        return false;
    }

    public Record getQuestion() {
        List l = this.sections[0];
        if (l == null || l.size() == 0) {
            return null;
        }
        return (Record) l.get(0);
    }

    public TSIGRecord getTSIG() {
        int count = this.header.getCount(3);
        if (count == 0) {
            return null;
        }
        Record rec = (Record) this.sections[3].get(count - 1);
        if (rec.type != 250) {
            return null;
        }
        return (TSIGRecord) rec;
    }

    public boolean isSigned() {
        int i = this.tsigState;
        return i == 3 || i == 1 || i == 4;
    }

    public boolean isVerified() {
        return this.tsigState == 1;
    }

    public OPTRecord getOPT() {
        Record[] additional = getSectionArray(3);
        for (int i = 0; i < additional.length; i++) {
            if (additional[i] instanceof OPTRecord) {
                return (OPTRecord) additional[i];
            }
        }
        return null;
    }

    public int getRcode() {
        int rcode = this.header.getRcode();
        OPTRecord opt = getOPT();
        if (opt != null) {
            return rcode + (opt.getExtendedRcode() << 4);
        }
        return rcode;
    }

    public Record[] getSectionArray(int section) {
        List[] listArr = this.sections;
        if (listArr[section] == null) {
            return emptyRecordArray;
        }
        List l = listArr[section];
        return (Record[]) l.toArray(new Record[l.size()]);
    }

    private static boolean sameSet(Record r1, Record r2) {
        return r1.getRRsetType() == r2.getRRsetType() && r1.getDClass() == r2.getDClass() && r1.getName().equals(r2.getName());
    }

    public RRset[] getSectionRRsets(int section) {
        if (this.sections[section] == null) {
            return emptyRRsetArray;
        }
        List sets = new LinkedList();
        Record[] recs = getSectionArray(section);
        Set hash = new HashSet();
        for (int i = 0; i < recs.length; i++) {
            Name name = recs[i].getName();
            boolean newset = true;
            if (hash.contains(name)) {
                int j = sets.size() - 1;
                while (true) {
                    if (j < 0) {
                        break;
                    }
                    RRset set = (RRset) sets.get(j);
                    if (set.getType() == recs[i].getRRsetType() && set.getDClass() == recs[i].getDClass() && set.getName().equals(name)) {
                        set.addRR(recs[i]);
                        newset = false;
                        break;
                    }
                    j--;
                }
            }
            if (newset) {
                sets.add(new RRset(recs[i]));
                hash.add(name);
            }
        }
        return (RRset[]) sets.toArray(new RRset[sets.size()]);
    }

    /* access modifiers changed from: package-private */
    public void toWire(DNSOutput out) {
        this.header.toWire(out);
        Compression c = new Compression();
        for (int i = 0; i < 4; i++) {
            if (this.sections[i] != null) {
                for (int j = 0; j < this.sections[i].size(); j++) {
                    ((Record) this.sections[i].get(j)).toWire(out, i, c);
                }
            }
        }
    }

    private int sectionToWire(DNSOutput out, int section, Compression c, int maxLength) {
        int n = this.sections[section].size();
        int pos = out.current();
        int rendered = 0;
        int skipped = 0;
        Record lastrec = null;
        for (int i = 0; i < n; i++) {
            Record rec = (Record) this.sections[section].get(i);
            if (section != 3 || !(rec instanceof OPTRecord)) {
                if (lastrec != null && !sameSet(rec, lastrec)) {
                    pos = out.current();
                    rendered = i;
                }
                lastrec = rec;
                rec.toWire(out, section, c);
                if (out.current() > maxLength) {
                    out.jump(pos);
                    return (n - rendered) + skipped;
                }
            } else {
                skipped++;
            }
        }
        return skipped;
    }

    private boolean toWire(DNSOutput out, int maxLength) {
        boolean z;
        DNSOutput dNSOutput = out;
        boolean z2 = false;
        if (maxLength < 12) {
            return false;
        }
        int tempMaxLength = maxLength;
        TSIG tsig = this.tsigkey;
        if (tsig != null) {
            tempMaxLength -= tsig.recordLength();
        }
        OPTRecord opt = getOPT();
        byte[] optBytes = null;
        int i = 3;
        if (opt != null) {
            optBytes = opt.toWire(3);
            tempMaxLength -= optBytes.length;
        }
        int startpos = out.current();
        this.header.toWire(dNSOutput);
        Compression c = new Compression();
        int flags = this.header.getFlagsByte();
        int additionalCount = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= 4) {
                break;
            }
            if (this.sections[i2] == null) {
                z = z2;
            } else {
                int skipped = sectionToWire(dNSOutput, i2, c, tempMaxLength);
                if (skipped == 0 || i2 == i) {
                    z = z2;
                    if (i2 == 3) {
                        additionalCount = this.header.getCount(i2) - skipped;
                    }
                } else {
                    flags = Header.setFlag(flags, 6, true);
                    dNSOutput.writeU16At(this.header.getCount(i2) - skipped, startpos + 4 + (i2 * 2));
                    int j = i2 + 1;
                    while (j < i) {
                        dNSOutput.writeU16At(0, startpos + 4 + (j * 2));
                        j++;
                        i = 3;
                    }
                }
            }
            i2++;
            z2 = z;
            i = 3;
        }
        if (optBytes != null) {
            dNSOutput.writeByteArray(optBytes);
            additionalCount++;
        }
        if (flags != this.header.getFlagsByte()) {
            dNSOutput.writeU16At(flags, startpos + 2);
        }
        if (additionalCount != this.header.getCount(3)) {
            dNSOutput.writeU16At(additionalCount, startpos + 10);
        }
        TSIG tsig2 = this.tsigkey;
        if (tsig2 == null) {
            return true;
        }
        tsig2.generate(this, out.toByteArray(), this.tsigerror, this.querytsig).toWire(dNSOutput, 3, c);
        dNSOutput.writeU16At(additionalCount + 1, startpos + 10);
        return true;
    }

    public byte[] toWire() {
        DNSOutput out = new DNSOutput();
        toWire(out);
        this.size = out.current();
        return out.toByteArray();
    }

    public byte[] toWire(int maxLength) {
        DNSOutput out = new DNSOutput();
        toWire(out, maxLength);
        this.size = out.current();
        return out.toByteArray();
    }

    public void setTSIG(TSIG key, int error, TSIGRecord querytsig2) {
        this.tsigkey = key;
        this.tsigerror = error;
        this.querytsig = querytsig2;
    }

    public int numBytes() {
        return this.size;
    }

    public String sectionToString(int i) {
        if (i > 3) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Record[] records = getSectionArray(i);
        for (Record rec : records) {
            if (i == 0) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(";;\t");
                stringBuffer.append(rec.name);
                sb.append(stringBuffer.toString());
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append(", type = ");
                stringBuffer2.append(Type.string(rec.type));
                sb.append(stringBuffer2.toString());
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append(", class = ");
                stringBuffer3.append(DClass.string(rec.dclass));
                sb.append(stringBuffer3.toString());
            } else {
                sb.append(rec);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (getOPT() != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(this.header.toStringWithRcode(getRcode()));
            stringBuffer.append("\n");
            sb.append(stringBuffer.toString());
        } else {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(this.header);
            stringBuffer2.append("\n");
            sb.append(stringBuffer2.toString());
        }
        if (isSigned()) {
            sb.append(";; TSIG ");
            if (isVerified()) {
                sb.append(EucTestIntent.Extras.ACK_STATUS_OK);
            } else {
                sb.append("invalid");
            }
            sb.append(10);
        }
        for (int i = 0; i < 4; i++) {
            if (this.header.getOpcode() != 5) {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append(";; ");
                stringBuffer3.append(Section.longString(i));
                stringBuffer3.append(":\n");
                sb.append(stringBuffer3.toString());
            } else {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append(";; ");
                stringBuffer4.append(Section.updString(i));
                stringBuffer4.append(":\n");
                sb.append(stringBuffer4.toString());
            }
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append(sectionToString(i));
            stringBuffer5.append("\n");
            sb.append(stringBuffer5.toString());
        }
        StringBuffer stringBuffer6 = new StringBuffer();
        stringBuffer6.append(";; Message size: ");
        stringBuffer6.append(numBytes());
        stringBuffer6.append(" bytes");
        sb.append(stringBuffer6.toString());
        return sb.toString();
    }

    public Object clone() {
        Message m = new Message();
        int i = 0;
        while (true) {
            List[] listArr = this.sections;
            if (i < listArr.length) {
                if (listArr[i] != null) {
                    m.sections[i] = new LinkedList(this.sections[i]);
                }
                i++;
            } else {
                m.header = (Header) this.header.clone();
                m.size = this.size;
                return m;
            }
        }
    }
}
