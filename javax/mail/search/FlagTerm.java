package javax.mail.search;

import javax.mail.Flags;
import javax.mail.Message;

public final class FlagTerm extends SearchTerm {
    private static final long serialVersionUID = -142991500302030647L;
    protected Flags flags;
    protected boolean set;

    public FlagTerm(Flags flags2, boolean set2) {
        this.flags = flags2;
        this.set = set2;
    }

    public Flags getFlags() {
        return (Flags) this.flags.clone();
    }

    public boolean getTestSet() {
        return this.set;
    }

    public boolean match(Message msg) {
        try {
            Flags f = msg.getFlags();
            if (!this.set) {
                Flags.Flag[] sf = this.flags.getSystemFlags();
                for (Flags.Flag contains : sf) {
                    if (f.contains(contains)) {
                        return false;
                    }
                }
                String[] s = this.flags.getUserFlags();
                for (String contains2 : s) {
                    if (f.contains(contains2)) {
                        return false;
                    }
                }
                return true;
            } else if (f.contains(this.flags)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FlagTerm)) {
            return false;
        }
        FlagTerm ft = (FlagTerm) obj;
        if (ft.set != this.set || !ft.flags.equals(this.flags)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.set ? this.flags.hashCode() : ~this.flags.hashCode();
    }
}
