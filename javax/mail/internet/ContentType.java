package javax.mail.internet;

import javax.mail.internet.HeaderTokenizer;

public class ContentType {
    private ParameterList list;
    private String primaryType;
    private String subType;

    public ContentType() {
    }

    public ContentType(String primaryType2, String subType2, ParameterList list2) {
        this.primaryType = primaryType2;
        this.subType = subType2;
        this.list = list2;
    }

    public ContentType(String s) throws ParseException {
        HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
        HeaderTokenizer.Token tk = h.next();
        if (tk.getType() == -1) {
            this.primaryType = tk.getValue();
            if (((char) h.next().getType()) == '/') {
                HeaderTokenizer.Token tk2 = h.next();
                if (tk2.getType() == -1) {
                    this.subType = tk2.getValue();
                    String rem = h.getRemainder();
                    if (rem != null) {
                        this.list = new ParameterList(rem);
                        return;
                    }
                    return;
                }
                throw new ParseException();
            }
            throw new ParseException();
        }
        throw new ParseException();
    }

    public String getPrimaryType() {
        return this.primaryType;
    }

    public String getSubType() {
        return this.subType;
    }

    public String getBaseType() {
        return String.valueOf(this.primaryType) + '/' + this.subType;
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

    public void setPrimaryType(String primaryType2) {
        this.primaryType = primaryType2;
    }

    public void setSubType(String subType2) {
        this.subType = subType2;
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
        if (this.primaryType == null || this.subType == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(this.primaryType);
        sb.append('/');
        sb.append(this.subType);
        ParameterList parameterList = this.list;
        if (parameterList != null) {
            sb.append(parameterList.toString(sb.length() + 14));
        }
        return sb.toString();
    }

    public boolean match(ContentType cType) {
        if (!this.primaryType.equalsIgnoreCase(cType.getPrimaryType())) {
            return false;
        }
        String sType = cType.getSubType();
        if (this.subType.charAt(0) == '*' || sType.charAt(0) == '*' || this.subType.equalsIgnoreCase(sType)) {
            return true;
        }
        return false;
    }

    public boolean match(String s) {
        try {
            return match(new ContentType(s));
        } catch (ParseException e) {
            return false;
        }
    }
}
