package com.sun.mail.imap.protocol;

import java.util.Vector;

public class MessageSet {
    public int end;
    public int start;

    public MessageSet() {
    }

    public MessageSet(int start2, int end2) {
        this.start = start2;
        this.end = end2;
    }

    public int size() {
        return (this.end - this.start) + 1;
    }

    public static MessageSet[] createMessageSets(int[] msgs) {
        int j;
        Vector v = new Vector();
        for (int i = 0; i < msgs.length; i = (j - 1) + 1) {
            MessageSet ms = new MessageSet();
            ms.start = msgs[i];
            j = i + 1;
            while (j < msgs.length && msgs[j] == msgs[j - 1] + 1) {
                j++;
            }
            ms.end = msgs[j - 1];
            v.addElement(ms);
        }
        MessageSet[] msgsets = new MessageSet[v.size()];
        v.copyInto(msgsets);
        return msgsets;
    }

    public static String toString(MessageSet[] msgsets) {
        if (msgsets == null || msgsets.length == 0) {
            return null;
        }
        int i = 0;
        StringBuffer s = new StringBuffer();
        int size = msgsets.length;
        while (true) {
            int start2 = msgsets[i].start;
            int end2 = msgsets[i].end;
            if (end2 > start2) {
                s.append(start2);
                s.append(':');
                s.append(end2);
            } else {
                s.append(start2);
            }
            i++;
            if (i >= size) {
                return s.toString();
            }
            s.append(',');
        }
    }

    public static int size(MessageSet[] msgsets) {
        int count = 0;
        if (msgsets == null) {
            return 0;
        }
        for (MessageSet size : msgsets) {
            count += size.size();
        }
        return count;
    }
}
