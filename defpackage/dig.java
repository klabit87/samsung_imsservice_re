package defpackage;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.List;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Type;

/* renamed from: dig  reason: default package */
public class dig {
    static int dclass = 1;
    static Name name = null;
    static int type = 1;

    static void usage() {
        System.out.println("Usage: dig [@server] name [<type>] [<class>] [options]");
        System.exit(0);
    }

    static void doQuery(Message response, long ms) throws IOException {
        System.out.println("; java dig 0.0");
        System.out.println(response);
        PrintStream printStream = System.out;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(";; Query time: ");
        stringBuffer.append(ms);
        stringBuffer.append(" ms");
        printStream.println(stringBuffer.toString());
    }

    static void doAXFR(Message response) throws IOException {
        PrintStream printStream = System.out;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("; java dig 0.0 <> ");
        stringBuffer.append(name);
        stringBuffer.append(" axfr");
        printStream.println(stringBuffer.toString());
        if (response.isSigned()) {
            System.out.print(";; TSIG ");
            if (response.isVerified()) {
                System.out.println(EucTestIntent.Extras.ACK_STATUS_OK);
            } else {
                System.out.println("failed");
            }
        }
        if (response.getRcode() != 0) {
            System.out.println(response);
            return;
        }
        Record[] records = response.getSectionArray(1);
        for (Record println : records) {
            System.out.println(println);
        }
        System.out.print(";; done (");
        System.out.print(response.getHeader().getCount(1));
        System.out.print(" records, ");
        System.out.print(response.getHeader().getCount(3));
        System.out.println(" additional)");
    }

    public static void main(String[] argv) throws IOException {
        int arg;
        String addrStr;
        String key;
        String ednsStr;
        String portStr;
        String server = null;
        SimpleResolver res = null;
        boolean printQuery = false;
        if (argv.length < 1) {
            usage();
        }
        int arg2 = 0;
        try {
            if (argv[0].startsWith("@")) {
                server = argv[0].substring(1);
                arg2 = 0 + 1;
            }
            if (server != null) {
                res = new SimpleResolver(server);
            } else {
                res = new SimpleResolver();
            }
            int arg3 = arg2 + 1;
            String nameString = argv[arg2];
            if (nameString.equals("-x")) {
                arg = arg3 + 1;
                name = ReverseMap.fromAddress(argv[arg3]);
                type = 12;
                dclass = 1;
            } else {
                name = Name.fromString(nameString, Name.root);
                int value = Type.value(argv[arg3]);
                type = value;
                if (value < 0) {
                    type = 1;
                } else {
                    arg3++;
                }
                int value2 = DClass.value(argv[arg3]);
                dclass = value2;
                if (value2 < 0) {
                    dclass = 1;
                    arg = arg3;
                } else {
                    arg = arg3 + 1;
                }
            }
            while (argv[arg].startsWith("-") && argv[arg].length() > 1) {
                char charAt = argv[arg].charAt(1);
                if (charAt == 'b') {
                    if (argv[arg].length() > 2) {
                        addrStr = argv[arg].substring(2);
                    } else {
                        arg++;
                        addrStr = argv[arg];
                    }
                    try {
                        res.setLocalAddress(InetAddress.getByName(addrStr));
                    } catch (Exception e) {
                        System.out.println("Invalid address");
                        return;
                    }
                } else if (charAt == 'i') {
                    res.setIgnoreTruncation(true);
                } else if (charAt == 'k') {
                    if (argv[arg].length() > 2) {
                        key = argv[arg].substring(2);
                    } else {
                        arg++;
                        key = argv[arg];
                    }
                    res.setTSIGKey(TSIG.fromString(key));
                } else if (charAt == 't') {
                    res.setTCP(true);
                } else if (charAt == 'd') {
                    res.setEDNS(0, 0, 32768, (List) null);
                } else if (charAt == 'e') {
                    if (argv[arg].length() > 2) {
                        ednsStr = argv[arg].substring(2);
                    } else {
                        arg++;
                        ednsStr = argv[arg];
                    }
                    int edns = Integer.parseInt(ednsStr);
                    if (edns >= 0) {
                        if (edns <= 1) {
                            res.setEDNS(edns);
                        }
                    }
                    PrintStream printStream = System.out;
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("Unsupported EDNS level: ");
                    stringBuffer.append(edns);
                    printStream.println(stringBuffer.toString());
                    return;
                } else if (charAt == 'p') {
                    if (argv[arg].length() > 2) {
                        portStr = argv[arg].substring(2);
                    } else {
                        arg++;
                        portStr = argv[arg];
                    }
                    int port = Integer.parseInt(portStr);
                    if (port >= 0) {
                        if (port <= 65536) {
                            res.setPort(port);
                        }
                    }
                    System.out.println("Invalid port");
                    return;
                } else if (charAt != 'q') {
                    System.out.print("Invalid option: ");
                    System.out.println(argv[arg]);
                } else {
                    printQuery = true;
                }
                arg++;
            }
        } catch (ArrayIndexOutOfBoundsException e2) {
            if (name == null) {
                usage();
            }
        }
        if (res == null) {
            res = new SimpleResolver();
        }
        Message query = Message.newQuery(Record.newRecord(name, type, dclass));
        if (printQuery) {
            System.out.println(query);
        }
        long startTime = System.currentTimeMillis();
        Message response = res.send(query);
        long endTime = System.currentTimeMillis();
        if (type == 252) {
            doAXFR(response);
        } else {
            doQuery(response, endTime - startTime);
        }
    }
}
