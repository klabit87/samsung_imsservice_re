package org.xbill.DNS;

import java.io.PrintStream;

public class Compression {
    private static final int MAX_POINTER = 16383;
    private static final int TABLE_SIZE = 17;
    private Entry[] table = new Entry[17];
    private boolean verbose = Options.check("verbosecompression");

    private static class Entry {
        Name name;
        Entry next;
        int pos;

        private Entry() {
        }
    }

    public void add(int pos, Name name) {
        if (pos <= MAX_POINTER) {
            int row = (name.hashCode() & Integer.MAX_VALUE) % 17;
            Entry entry = new Entry();
            entry.name = name;
            entry.pos = pos;
            entry.next = this.table[row];
            this.table[row] = entry;
            if (this.verbose) {
                PrintStream printStream = System.err;
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Adding ");
                stringBuffer.append(name);
                stringBuffer.append(" at ");
                stringBuffer.append(pos);
                printStream.println(stringBuffer.toString());
            }
        }
    }

    public int get(Name name) {
        int pos = -1;
        for (Entry entry = this.table[(name.hashCode() & Integer.MAX_VALUE) % 17]; entry != null; entry = entry.next) {
            if (entry.name.equals(name)) {
                pos = entry.pos;
            }
        }
        if (this.verbose) {
            PrintStream printStream = System.err;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Looking for ");
            stringBuffer.append(name);
            stringBuffer.append(", found ");
            stringBuffer.append(pos);
            printStream.println(stringBuffer.toString());
        }
        return pos;
    }
}
