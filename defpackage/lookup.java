package defpackage;

import java.io.PrintStream;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

/* renamed from: lookup  reason: default package */
public class lookup {
    public static void printAnswer(String name, Lookup lookup) {
        PrintStream printStream = System.out;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name);
        stringBuffer.append(":");
        printStream.print(stringBuffer.toString());
        if (lookup.getResult() != 0) {
            PrintStream printStream2 = System.out;
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(" ");
            stringBuffer2.append(lookup.getErrorString());
            printStream2.print(stringBuffer2.toString());
        }
        System.out.println();
        Name[] aliases = lookup.getAliases();
        if (aliases.length > 0) {
            System.out.print("# aliases: ");
            for (int i = 0; i < aliases.length; i++) {
                System.out.print(aliases[i]);
                if (i < aliases.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
        if (lookup.getResult() == 0) {
            Record[] answers = lookup.getAnswers();
            for (Record println : answers) {
                System.out.println(println);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int type = 1;
        int start = 0;
        if (args.length > 2 && args[0].equals("-t")) {
            type = Type.value(args[1]);
            if (type >= 0) {
                start = 2;
            } else {
                throw new IllegalArgumentException("invalid type");
            }
        }
        for (int i = start; i < args.length; i++) {
            Lookup l = new Lookup(args[i], type);
            l.run();
            printAnswer(args[i], l);
        }
    }
}
