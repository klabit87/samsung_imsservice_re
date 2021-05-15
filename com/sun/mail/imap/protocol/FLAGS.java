package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import javax.mail.Flags;

public class FLAGS extends Flags implements Item {
    static final char[] name = {'F', 'L', 'A', 'G', 'S'};
    private static final long serialVersionUID = 439049847053756670L;
    public int msgno;

    public FLAGS(IMAPResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        String[] flags = r.readSimpleList();
        if (flags != null) {
            for (String s : flags) {
                if (s.length() < 2 || s.charAt(0) != '\\') {
                    add(s);
                } else {
                    char upperCase = Character.toUpperCase(s.charAt(1));
                    if (upperCase == '*') {
                        add(Flags.Flag.USER);
                    } else if (upperCase == 'A') {
                        add(Flags.Flag.ANSWERED);
                    } else if (upperCase != 'D') {
                        if (upperCase == 'F') {
                            add(Flags.Flag.FLAGGED);
                        } else if (upperCase == 'R') {
                            add(Flags.Flag.RECENT);
                        } else if (upperCase != 'S') {
                            add(s);
                        } else {
                            add(Flags.Flag.SEEN);
                        }
                    } else if (s.length() >= 3) {
                        char c = s.charAt(2);
                        if (c == 'e' || c == 'E') {
                            add(Flags.Flag.DELETED);
                        } else if (c == 'r' || c == 'R') {
                            add(Flags.Flag.DRAFT);
                        }
                    } else {
                        add(s);
                    }
                }
            }
        }
    }
}
