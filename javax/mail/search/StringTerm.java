package javax.mail.search;

public abstract class StringTerm extends SearchTerm {
    private static final long serialVersionUID = 1274042129007696269L;
    protected boolean ignoreCase;
    protected String pattern;

    protected StringTerm(String pattern2) {
        this.pattern = pattern2;
        this.ignoreCase = true;
    }

    protected StringTerm(String pattern2, boolean ignoreCase2) {
        this.pattern = pattern2;
        this.ignoreCase = ignoreCase2;
    }

    public String getPattern() {
        return this.pattern;
    }

    public boolean getIgnoreCase() {
        return this.ignoreCase;
    }

    /* access modifiers changed from: protected */
    public boolean match(String s) {
        int len = s.length() - this.pattern.length();
        for (int i = 0; i <= len; i++) {
            boolean z = this.ignoreCase;
            String str = this.pattern;
            if (s.regionMatches(z, i, str, 0, str.length())) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StringTerm)) {
            return false;
        }
        StringTerm st = (StringTerm) obj;
        if (this.ignoreCase) {
            if (!st.pattern.equalsIgnoreCase(this.pattern) || st.ignoreCase != this.ignoreCase) {
                return false;
            }
            return true;
        } else if (!st.pattern.equals(this.pattern) || st.ignoreCase != this.ignoreCase) {
            return false;
        } else {
            return true;
        }
    }

    public int hashCode() {
        return this.ignoreCase ? this.pattern.hashCode() : ~this.pattern.hashCode();
    }
}
