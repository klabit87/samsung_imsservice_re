package javax.mail.internet;

import javax.mail.internet.HeaderTokenizer;

public class ContentDisposition {
    private String disposition;
    private ParameterList list;

    public ContentDisposition() {
    }

    public ContentDisposition(String disposition2, ParameterList list2) {
        this.disposition = disposition2;
        this.list = list2;
    }

    public ContentDisposition(String s) throws ParseException {
        HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
        HeaderTokenizer.Token tk = h.next();
        if (tk.getType() == -1) {
            this.disposition = tk.getValue();
            String rem = h.getRemainder();
            if (rem != null) {
                this.list = new ParameterList(rem);
                return;
            }
            return;
        }
        throw new ParseException();
    }

    public String getDisposition() {
        return this.disposition;
    }

    public String getParameter(String name) {
        ParameterList parameterList = this.list;
        if (parameterList == null) {
            return null;
        }
        return parameterList.get(name);
    }

    public ParameterList getParameterList() {
        return this.list;
    }

    public void setDisposition(String disposition2) {
        this.disposition = disposition2;
    }

    public void setParameter(String name, String value) {
        if (this.list == null) {
            this.list = new ParameterList();
        }
        this.list.set(name, value);
    }

    public void setParameterList(ParameterList list2) {
        this.list = list2;
    }

    public String toString() {
        String str = this.disposition;
        if (str == null) {
            return null;
        }
        if (this.list == null) {
            return str;
        }
        StringBuffer sb = new StringBuffer(str);
        sb.append(this.list.toString(sb.length() + 21));
        return sb.toString();
    }
}
