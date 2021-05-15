package defpackage;

import com.google.android.gms.actions.SearchIntents;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TTL;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Tokenizer;
import org.xbill.DNS.Type;

/* renamed from: update  reason: default package */
public class update {
    int defaultClass = 1;
    long defaultTTL;
    PrintStream log = null;
    Message query;
    Resolver res;
    Message response;
    String server = null;
    Name zone = Name.root;

    /* access modifiers changed from: package-private */
    public void print(Object o) {
        System.out.println(o);
        PrintStream printStream = this.log;
        if (printStream != null) {
            printStream.println(o);
        }
    }

    public Message newMessage() {
        Message msg = new Message();
        msg.getHeader().setOpcode(5);
        return msg;
    }

    public update(InputStream in) throws IOException {
        String line;
        List<BufferedReader> inputs = new LinkedList<>();
        List istreams = new LinkedList();
        this.query = newMessage();
        inputs.add(new BufferedReader(new InputStreamReader(in)));
        istreams.add(in);
        while (true) {
            do {
                try {
                    InputStream is = (InputStream) istreams.get(0);
                    BufferedReader br = (BufferedReader) inputs.get(0);
                    if (is == System.in) {
                        System.out.print("> ");
                    }
                    line = br.readLine();
                    if (line == null) {
                        br.close();
                        inputs.remove(0);
                        istreams.remove(0);
                        if (inputs.isEmpty()) {
                            return;
                        }
                    }
                } catch (TextParseException tpe) {
                    System.out.println(tpe.getMessage());
                } catch (InterruptedIOException e) {
                    System.out.println("Operation timed out");
                } catch (SocketException e2) {
                    System.out.println("Socket error");
                } catch (IOException ioe) {
                    System.out.println(ioe);
                }
            } while (line == null);
            if (this.log != null) {
                PrintStream printStream = this.log;
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("> ");
                stringBuffer.append(line);
                printStream.println(stringBuffer.toString());
            }
            if (line.length() == 0) {
                continue;
            } else if (line.charAt(0) != '#') {
                line = line.charAt(0) == '>' ? line.substring(1) : line;
                Tokenizer st = new Tokenizer(line);
                Tokenizer.Token token = st.get();
                if (!token.isEOL()) {
                    String operation = token.value;
                    if (operation.equals("server")) {
                        this.server = st.getString();
                        this.res = new SimpleResolver(this.server);
                        Tokenizer.Token token2 = st.get();
                        if (token2.isString()) {
                            this.res.setPort(Short.parseShort(token2.value));
                        }
                    } else if (operation.equals("key")) {
                        String keyname = st.getString();
                        String keydata = st.getString();
                        if (this.res == null) {
                            this.res = new SimpleResolver(this.server);
                        }
                        this.res.setTSIGKey(new TSIG(keyname, keydata));
                    } else if (operation.equals("edns")) {
                        if (this.res == null) {
                            this.res = new SimpleResolver(this.server);
                        }
                        this.res.setEDNS(st.getUInt16());
                    } else if (operation.equals("port")) {
                        if (this.res == null) {
                            this.res = new SimpleResolver(this.server);
                        }
                        this.res.setPort(st.getUInt16());
                    } else if (operation.equals("tcp")) {
                        if (this.res == null) {
                            this.res = new SimpleResolver(this.server);
                        }
                        this.res.setTCP(true);
                    } else if (operation.equals("class")) {
                        String classStr = st.getString();
                        int newClass = DClass.value(classStr);
                        if (newClass > 0) {
                            this.defaultClass = newClass;
                        } else {
                            StringBuffer stringBuffer2 = new StringBuffer();
                            stringBuffer2.append("Invalid class ");
                            stringBuffer2.append(classStr);
                            print(stringBuffer2.toString());
                        }
                    } else if (operation.equals("ttl")) {
                        this.defaultTTL = st.getTTL();
                    } else {
                        if (!operation.equals("origin")) {
                            if (!operation.equals("zone")) {
                                if (operation.equals("require")) {
                                    doRequire(st);
                                } else if (operation.equals("prohibit")) {
                                    doProhibit(st);
                                } else if (operation.equals("add")) {
                                    doAdd(st);
                                } else if (operation.equals(SoftphoneContract.SoftphoneAddress.DELETE)) {
                                    doDelete(st);
                                } else if (operation.equals("glue")) {
                                    doGlue(st);
                                } else {
                                    if (!operation.equals("help")) {
                                        if (!operation.equals("?")) {
                                            if (operation.equals("echo")) {
                                                print(line.substring(4).trim());
                                            } else if (operation.equals("send")) {
                                                sendUpdate();
                                                this.query = newMessage();
                                            } else if (operation.equals("show")) {
                                                print(this.query);
                                            } else if (operation.equals("clear")) {
                                                this.query = newMessage();
                                            } else if (operation.equals(SearchIntents.EXTRA_QUERY)) {
                                                doQuery(st);
                                            } else {
                                                if (!operation.equals("quit")) {
                                                    if (!operation.equals("q")) {
                                                        if (operation.equals("file")) {
                                                            doFile(st, inputs, istreams);
                                                        } else if (operation.equals("log")) {
                                                            doLog(st);
                                                        } else if (operation.equals("assert")) {
                                                            if (!doAssert(st)) {
                                                                return;
                                                            }
                                                        } else if (operation.equals("sleep")) {
                                                            try {
                                                                Thread.sleep(st.getUInt32());
                                                            } catch (InterruptedException e3) {
                                                            }
                                                        } else if (operation.equals("date")) {
                                                            Date now = new Date();
                                                            Tokenizer.Token token3 = st.get();
                                                            if (!token3.isString() || !token3.value.equals("-ms")) {
                                                                print(now);
                                                            } else {
                                                                print(Long.toString(now.getTime()));
                                                            }
                                                        } else {
                                                            StringBuffer stringBuffer3 = new StringBuffer();
                                                            stringBuffer3.append("invalid keyword: ");
                                                            stringBuffer3.append(operation);
                                                            print(stringBuffer3.toString());
                                                        }
                                                    }
                                                }
                                                if (this.log != null) {
                                                    this.log.close();
                                                }
                                                for (BufferedReader tbr : inputs) {
                                                    tbr.close();
                                                }
                                                System.exit(0);
                                            }
                                        }
                                    }
                                    Tokenizer.Token token4 = st.get();
                                    if (token4.isString()) {
                                        help(token4.value);
                                    } else {
                                        help((String) null);
                                    }
                                }
                            }
                        }
                        this.zone = st.getName(Name.root);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendUpdate() throws IOException {
        if (this.query.getHeader().getCount(2) == 0) {
            print("Empty update message.  Ignoring.");
            return;
        }
        if (this.query.getHeader().getCount(0) == 0) {
            Name updzone = this.zone;
            int dclass = this.defaultClass;
            if (updzone == null) {
                Record[] recs = this.query.getSectionArray(2);
                int i = 0;
                while (true) {
                    if (i < recs.length) {
                        if (updzone == null) {
                            updzone = new Name(recs[i].getName(), 1);
                        }
                        if (recs[i].getDClass() != 254 && recs[i].getDClass() != 255) {
                            dclass = recs[i].getDClass();
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
            this.query.addRecord(Record.newRecord(updzone, 6, dclass), 0);
        }
        if (this.res == null) {
            this.res = new SimpleResolver(this.server);
        }
        Message send = this.res.send(this.query);
        this.response = send;
        print(send);
    }

    /* access modifiers changed from: package-private */
    public Record parseRR(Tokenizer st, int classValue, long TTLValue) throws IOException {
        long ttl;
        String s;
        Name name = st.getName(this.zone);
        String s2 = st.getString();
        try {
            long ttl2 = TTL.parseTTL(s2);
            s2 = st.getString();
            ttl = ttl2;
        } catch (NumberFormatException e) {
            ttl = TTLValue;
        }
        if (DClass.value(s2) >= 0) {
            classValue = DClass.value(s2);
            s = st.getString();
        } else {
            s = s2;
        }
        int value = Type.value(s);
        int type = value;
        if (value >= 0) {
            Record record = Record.fromString(name, type, classValue, ttl, st, this.zone);
            if (record != null) {
                return record;
            }
            throw new IOException("Parse error");
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Invalid type: ");
        stringBuffer.append(s);
        throw new IOException(stringBuffer.toString());
    }

    /* access modifiers changed from: package-private */
    public void doRequire(Tokenizer st) throws IOException {
        Record record;
        Name name = st.getName(this.zone);
        Tokenizer.Token token = st.get();
        if (token.isString()) {
            int value = Type.value(token.value);
            int type = value;
            if (value >= 0) {
                boolean iseol = st.get().isEOL();
                st.unget();
                if (!iseol) {
                    record = Record.fromString(name, type, this.defaultClass, 0, st, this.zone);
                } else {
                    record = Record.newRecord(name, type, 255, 0);
                }
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid type: ");
                stringBuffer.append(token.value);
                throw new IOException(stringBuffer.toString());
            }
        } else {
            Tokenizer.Token token2 = token;
            record = Record.newRecord(name, 255, 255, 0);
        }
        this.query.addRecord(record, 1);
        print(record);
    }

    /* access modifiers changed from: package-private */
    public void doProhibit(Tokenizer st) throws IOException {
        int type;
        Name name = st.getName(this.zone);
        Tokenizer.Token token = st.get();
        if (token.isString()) {
            int value = Type.value(token.value);
            type = value;
            if (value < 0) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid type: ");
                stringBuffer.append(token.value);
                throw new IOException(stringBuffer.toString());
            }
        } else {
            type = 255;
        }
        Record record = Record.newRecord(name, type, 254, 0);
        this.query.addRecord(record, 1);
        print(record);
    }

    /* access modifiers changed from: package-private */
    public void doAdd(Tokenizer st) throws IOException {
        Record record = parseRR(st, this.defaultClass, this.defaultTTL);
        this.query.addRecord(record, 2);
        print(record);
    }

    /* access modifiers changed from: package-private */
    public void doDelete(Tokenizer st) throws IOException {
        Record record;
        String s;
        Name name = st.getName(this.zone);
        Tokenizer.Token token = st.get();
        if (token.isString()) {
            String s2 = token.value;
            if (DClass.value(s2) >= 0) {
                s = st.getString();
            } else {
                s = s2;
            }
            int value = Type.value(s);
            int type = value;
            if (value >= 0) {
                boolean iseol = st.get().isEOL();
                st.unget();
                if (!iseol) {
                    record = Record.fromString(name, type, 254, 0, st, this.zone);
                } else {
                    record = Record.newRecord(name, type, 255, 0);
                }
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid type: ");
                stringBuffer.append(s);
                throw new IOException(stringBuffer.toString());
            }
        } else {
            Tokenizer.Token token2 = token;
            record = Record.newRecord(name, 255, 255, 0);
        }
        this.query.addRecord(record, 2);
        print(record);
    }

    /* access modifiers changed from: package-private */
    public void doGlue(Tokenizer st) throws IOException {
        Record record = parseRR(st, this.defaultClass, this.defaultTTL);
        this.query.addRecord(record, 3);
        print(record);
    }

    /* access modifiers changed from: package-private */
    public void doQuery(Tokenizer st) throws IOException {
        int type = 1;
        int dclass = this.defaultClass;
        Name name = st.getName(this.zone);
        Tokenizer.Token token = st.get();
        if (token.isString()) {
            type = Type.value(token.value);
            if (type >= 0) {
                Tokenizer.Token token2 = st.get();
                if (token2.isString() && (dclass = DClass.value(token2.value)) < 0) {
                    throw new IOException("Invalid class");
                }
            } else {
                throw new IOException("Invalid type");
            }
        }
        Message newQuery = Message.newQuery(Record.newRecord(name, type, dclass));
        if (this.res == null) {
            this.res = new SimpleResolver(this.server);
        }
        Message send = this.res.send(newQuery);
        this.response = send;
        print(send);
    }

    /* access modifiers changed from: package-private */
    public void doFile(Tokenizer st, List inputs, List istreams) throws IOException {
        InputStream is;
        String s = st.getString();
        try {
            if (s.equals("-")) {
                is = System.in;
            } else {
                is = new FileInputStream(s);
            }
            istreams.add(0, is);
            inputs.add(0, new BufferedReader(new InputStreamReader(is)));
        } catch (FileNotFoundException e) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(s);
            stringBuffer.append(" not found");
            print(stringBuffer.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void doLog(Tokenizer st) throws IOException {
        String s = st.getString();
        try {
            this.log = new PrintStream(new FileOutputStream(s));
        } catch (Exception e) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Error opening ");
            stringBuffer.append(s);
            print(stringBuffer.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean doAssert(Tokenizer st) throws IOException {
        String field = st.getString();
        String expected = st.getString();
        String value = null;
        boolean flag = true;
        if (this.response == null) {
            print("No response has been received");
            return true;
        }
        if (field.equalsIgnoreCase("rcode")) {
            int rcode = this.response.getHeader().getRcode();
            if (rcode != Rcode.value(expected)) {
                value = Rcode.string(rcode);
                flag = false;
            }
        } else if (field.equalsIgnoreCase("serial")) {
            Record[] answers = this.response.getSectionArray(1);
            if (answers.length < 1 || !(answers[0] instanceof SOARecord)) {
                print("Invalid response (no SOA)");
            } else {
                long serial = ((SOARecord) answers[0]).getSerial();
                if (serial != Long.parseLong(expected)) {
                    value = Long.toString(serial);
                    flag = false;
                }
            }
        } else if (field.equalsIgnoreCase("tsig")) {
            if (!this.response.isSigned()) {
                value = "unsigned";
            } else if (this.response.isVerified()) {
                value = EucTestIntent.Extras.ACK_STATUS_OK;
            } else {
                value = "failed";
            }
            if (!value.equalsIgnoreCase(expected)) {
                flag = false;
            }
        } else {
            int value2 = Section.value(field);
            int section = value2;
            if (value2 >= 0) {
                int count = this.response.getHeader().getCount(section);
                if (count != Integer.parseInt(expected)) {
                    value = new Integer(count).toString();
                    flag = false;
                }
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid assertion keyword: ");
                stringBuffer.append(field);
                print(stringBuffer.toString());
            }
        }
        if (!flag) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("Expected ");
            stringBuffer2.append(field);
            stringBuffer2.append(" ");
            stringBuffer2.append(expected);
            stringBuffer2.append(", received ");
            stringBuffer2.append(value);
            print(stringBuffer2.toString());
            while (true) {
                Tokenizer.Token token = st.get();
                if (!token.isString()) {
                    break;
                }
                print(token.value);
            }
            st.unget();
        }
        return flag;
    }

    static void help(String topic) {
        System.out.println();
        if (topic == null) {
            System.out.println("The following are supported commands:\nadd      assert   class    clear    date     delete\necho     edns     file     glue     help     key\nlog      port     prohibit query    quit     require\nsend     server   show     sleep    tcp      ttl\nzone     #\n");
            return;
        }
        String topic2 = topic.toLowerCase();
        if (topic2.equals("add")) {
            System.out.println("add <name> [ttl] [class] <type> <data>\n\nspecify a record to be added\n");
        } else if (topic2.equals("assert")) {
            System.out.println("assert <field> <value> [msg]\n\nasserts that the value of the field in the last\nresponse matches the value specified.  If not,\nthe message is printed (if present) and the\nprogram exits.  The field may be any of <rcode>,\n<serial>, <tsig>, <qu>, <an>, <au>, or <ad>.\n");
        } else if (topic2.equals("class")) {
            System.out.println("class <class>\n\nclass of the zone to be updated (default: IN)\n");
        } else if (topic2.equals("clear")) {
            System.out.println("clear\n\nclears the current update packet\n");
        } else if (topic2.equals("date")) {
            System.out.println("date [-ms]\n\nprints the current date and time in human readable\nformat or as the number of milliseconds since the\nepoch");
        } else if (topic2.equals(SoftphoneContract.SoftphoneAddress.DELETE)) {
            System.out.println("delete <name> [ttl] [class] <type> <data> \ndelete <name> <type> \ndelete <name>\n\nspecify a record or set to be deleted, or that\nall records at a name should be deleted\n");
        } else if (topic2.equals("echo")) {
            System.out.println("echo <text>\n\nprints the text\n");
        } else if (topic2.equals("edns")) {
            System.out.println("edns <level>\n\nEDNS level specified when sending messages\n");
        } else if (topic2.equals("file")) {
            System.out.println("file <file>\n\nopens the specified file as the new input source\n(- represents stdin)\n");
        } else if (topic2.equals("glue")) {
            System.out.println("glue <name> [ttl] [class] <type> <data>\n\nspecify an additional record\n");
        } else if (topic2.equals("help")) {
            System.out.println("help\nhelp [topic]\n\nprints a list of commands or help about a specific\ncommand\n");
        } else if (topic2.equals("key")) {
            System.out.println("key <name> <data>\n\nTSIG key used to sign messages\n");
        } else if (topic2.equals("log")) {
            System.out.println("log <file>\n\nopens the specified file and uses it to log output\n");
        } else if (topic2.equals("port")) {
            System.out.println("port <port>\n\nUDP/TCP port messages are sent to (default: 53)\n");
        } else if (topic2.equals("prohibit")) {
            System.out.println("prohibit <name> <type> \nprohibit <name>\n\nrequire that a set or name is not present\n");
        } else if (topic2.equals(SearchIntents.EXTRA_QUERY)) {
            System.out.println("query <name> [type [class]] \n\nissues a query\n");
        } else if (topic2.equals("q") || topic2.equals("quit")) {
            System.out.println("quit\n\nquits the program\n");
        } else if (topic2.equals("require")) {
            System.out.println("require <name> [ttl] [class] <type> <data> \nrequire <name> <type> \nrequire <name>\n\nrequire that a record, set, or name is present\n");
        } else if (topic2.equals("send")) {
            System.out.println("send\n\nsends and resets the current update packet\n");
        } else if (topic2.equals("server")) {
            System.out.println("server <name> [port]\n\nserver that receives send updates/queries\n");
        } else if (topic2.equals("show")) {
            System.out.println("show\n\nshows the current update packet\n");
        } else if (topic2.equals("sleep")) {
            System.out.println("sleep <milliseconds>\n\npause for interval before next command\n");
        } else if (topic2.equals("tcp")) {
            System.out.println("tcp\n\nTCP should be used to send all messages\n");
        } else if (topic2.equals("ttl")) {
            System.out.println("ttl <ttl>\n\ndefault ttl of added records (default: 0)\n");
        } else if (topic2.equals("zone") || topic2.equals("origin")) {
            System.out.println("zone <zone>\n\nzone to update (default: .\n");
        } else if (topic2.equals("#")) {
            System.out.println("# <text>\n\na comment\n");
        } else {
            PrintStream printStream = System.out;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Topic '");
            stringBuffer.append(topic2);
            stringBuffer.append("' unrecognized\n");
            printStream.println(stringBuffer.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream in = null;
        if (args.length >= 1) {
            try {
                in = new FileInputStream(args[0]);
            } catch (FileNotFoundException e) {
                PrintStream printStream = System.out;
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(args[0]);
                stringBuffer.append(" not found.");
                printStream.println(stringBuffer.toString());
                System.exit(1);
            }
        } else {
            in = System.in;
        }
        new update(in);
    }
}
