package javax.mail;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

public class Flags implements Cloneable, Serializable {
    private static final int ANSWERED_BIT = 1;
    private static final int DELETED_BIT = 2;
    private static final int DRAFT_BIT = 4;
    private static final int FLAGGED_BIT = 8;
    private static final int RECENT_BIT = 16;
    private static final int SEEN_BIT = 32;
    private static final int USER_BIT = Integer.MIN_VALUE;
    private static final long serialVersionUID = 6243590407214169028L;
    private int system_flags = 0;
    private Hashtable user_flags = null;

    public static final class Flag {
        public static final Flag ANSWERED = new Flag(1);
        public static final Flag DELETED = new Flag(2);
        public static final Flag DRAFT = new Flag(4);
        public static final Flag FLAGGED = new Flag(8);
        public static final Flag RECENT = new Flag(16);
        public static final Flag SEEN = new Flag(32);
        public static final Flag USER = new Flag(Flags.USER_BIT);
        /* access modifiers changed from: private */
        public int bit;

        private Flag(int bit2) {
            this.bit = bit2;
        }
    }

    public Flags() {
    }

    public Flags(Flags flags) {
        this.system_flags = flags.system_flags;
        Hashtable hashtable = flags.user_flags;
        if (hashtable != null) {
            this.user_flags = (Hashtable) hashtable.clone();
        }
    }

    public Flags(Flag flag) {
        this.system_flags = 0 | flag.bit;
    }

    public Flags(String flag) {
        Hashtable hashtable = new Hashtable(1);
        this.user_flags = hashtable;
        hashtable.put(flag.toLowerCase(Locale.ENGLISH), flag);
    }

    public void add(Flag flag) {
        this.system_flags |= flag.bit;
    }

    public void add(String flag) {
        if (this.user_flags == null) {
            this.user_flags = new Hashtable(1);
        }
        this.user_flags.put(flag.toLowerCase(Locale.ENGLISH), flag);
    }

    public void add(Flags f) {
        this.system_flags |= f.system_flags;
        if (f.user_flags != null) {
            if (this.user_flags == null) {
                this.user_flags = new Hashtable(1);
            }
            Enumeration e = f.user_flags.keys();
            while (e.hasMoreElements()) {
                String s = (String) e.nextElement();
                this.user_flags.put(s, f.user_flags.get(s));
            }
        }
    }

    public void remove(Flag flag) {
        this.system_flags &= ~flag.bit;
    }

    public void remove(String flag) {
        Hashtable hashtable = this.user_flags;
        if (hashtable != null) {
            hashtable.remove(flag.toLowerCase(Locale.ENGLISH));
        }
    }

    public void remove(Flags f) {
        this.system_flags &= ~f.system_flags;
        Hashtable hashtable = f.user_flags;
        if (hashtable != null && this.user_flags != null) {
            Enumeration e = hashtable.keys();
            while (e.hasMoreElements()) {
                this.user_flags.remove(e.nextElement());
            }
        }
    }

    public boolean contains(Flag flag) {
        return (this.system_flags & flag.bit) != 0;
    }

    public boolean contains(String flag) {
        Hashtable hashtable = this.user_flags;
        if (hashtable == null) {
            return false;
        }
        return hashtable.containsKey(flag.toLowerCase(Locale.ENGLISH));
    }

    public boolean contains(Flags f) {
        int i = f.system_flags;
        if ((this.system_flags & i) != i) {
            return false;
        }
        Hashtable hashtable = f.user_flags;
        if (hashtable == null) {
            return true;
        }
        if (this.user_flags == null) {
            return false;
        }
        Enumeration e = hashtable.keys();
        while (e.hasMoreElements()) {
            if (!this.user_flags.containsKey(e.nextElement())) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Flags)) {
            return false;
        }
        Flags f = (Flags) obj;
        if (f.system_flags != this.system_flags) {
            return false;
        }
        if (f.user_flags == null && this.user_flags == null) {
            return true;
        }
        Hashtable hashtable = f.user_flags;
        if (hashtable == null || this.user_flags == null || hashtable.size() != this.user_flags.size()) {
            return false;
        }
        Enumeration e = f.user_flags.keys();
        while (e.hasMoreElements()) {
            if (!this.user_flags.containsKey(e.nextElement())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = this.system_flags;
        Hashtable hashtable = this.user_flags;
        if (hashtable != null) {
            Enumeration e = hashtable.keys();
            while (e.hasMoreElements()) {
                hash += ((String) e.nextElement()).hashCode();
            }
        }
        return hash;
    }

    public Flag[] getSystemFlags() {
        Vector v = new Vector();
        if ((this.system_flags & 1) != 0) {
            v.addElement(Flag.ANSWERED);
        }
        if ((this.system_flags & 2) != 0) {
            v.addElement(Flag.DELETED);
        }
        if ((this.system_flags & 4) != 0) {
            v.addElement(Flag.DRAFT);
        }
        if ((this.system_flags & 8) != 0) {
            v.addElement(Flag.FLAGGED);
        }
        if ((this.system_flags & 16) != 0) {
            v.addElement(Flag.RECENT);
        }
        if ((this.system_flags & 32) != 0) {
            v.addElement(Flag.SEEN);
        }
        if ((this.system_flags & USER_BIT) != 0) {
            v.addElement(Flag.USER);
        }
        Flag[] f = new Flag[v.size()];
        v.copyInto(f);
        return f;
    }

    public String[] getUserFlags() {
        Vector v = new Vector();
        Hashtable hashtable = this.user_flags;
        if (hashtable != null) {
            Enumeration e = hashtable.elements();
            while (e.hasMoreElements()) {
                v.addElement(e.nextElement());
            }
        }
        String[] f = new String[v.size()];
        v.copyInto(f);
        return f;
    }

    public Object clone() {
        Flags f = null;
        try {
            f = (Flags) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        Hashtable hashtable = this.user_flags;
        if (!(hashtable == null || f == null)) {
            f.user_flags = (Hashtable) hashtable.clone();
        }
        return f;
    }
}
