package javax.activation;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

public class MimeTypeParameterList {
    private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";
    private Hashtable parameters = new Hashtable();

    public MimeTypeParameterList() {
    }

    public MimeTypeParameterList(String parameterList) throws MimeTypeParseException {
        parse(parameterList);
    }

    /* access modifiers changed from: protected */
    public void parse(String parameterList) throws MimeTypeParseException {
        int length;
        String value;
        if (parameterList != null && (length = parameterList.length()) > 0) {
            int i = skipWhiteSpace(parameterList, 0);
            while (i < length) {
                char charAt = parameterList.charAt(i);
                char c = charAt;
                if (charAt != ';') {
                    break;
                }
                int i2 = skipWhiteSpace(parameterList, i + 1);
                if (i2 < length) {
                    int lastIndex = i2;
                    while (i2 < length && isTokenChar(parameterList.charAt(i2))) {
                        i2++;
                    }
                    String name = parameterList.substring(lastIndex, i2).toLowerCase(Locale.ENGLISH);
                    int i3 = skipWhiteSpace(parameterList, i2);
                    if (i3 >= length || parameterList.charAt(i3) != '=') {
                        throw new MimeTypeParseException("Couldn't find the '=' that separates a parameter name from its value.");
                    }
                    int i4 = skipWhiteSpace(parameterList, i3 + 1);
                    if (i4 < length) {
                        char c2 = parameterList.charAt(i4);
                        if (c2 == '\"') {
                            int i5 = i4 + 1;
                            if (i5 < length) {
                                int lastIndex2 = i5;
                                while (i5 < length) {
                                    c2 = parameterList.charAt(i5);
                                    if (c2 == '\"') {
                                        break;
                                    }
                                    if (c2 == '\\') {
                                        i5++;
                                    }
                                    i5++;
                                }
                                if (c2 == '\"') {
                                    value = unquote(parameterList.substring(lastIndex2, i5));
                                    i4 = i5 + 1;
                                } else {
                                    throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                                }
                            } else {
                                throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                            }
                        } else if (isTokenChar(c2)) {
                            int lastIndex3 = i4;
                            while (i4 < length && isTokenChar(parameterList.charAt(i4))) {
                                i4++;
                            }
                            value = parameterList.substring(lastIndex3, i4);
                        } else {
                            throw new MimeTypeParseException("Unexpected character encountered at index " + i4);
                        }
                        this.parameters.put(name, value);
                        i = skipWhiteSpace(parameterList, i4);
                    } else {
                        throw new MimeTypeParseException("Couldn't find a value for parameter named " + name);
                    }
                } else {
                    return;
                }
            }
            if (i < length) {
                throw new MimeTypeParseException("More characters encountered in input than expected.");
            }
        }
    }

    public int size() {
        return this.parameters.size();
    }

    public boolean isEmpty() {
        return this.parameters.isEmpty();
    }

    public String get(String name) {
        return (String) this.parameters.get(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public void set(String name, String value) {
        this.parameters.put(name.trim().toLowerCase(Locale.ENGLISH), value);
    }

    public void remove(String name) {
        this.parameters.remove(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public Enumeration getNames() {
        return this.parameters.keys();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.ensureCapacity(this.parameters.size() * 16);
        Enumeration keys = this.parameters.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            buffer.append("; ");
            buffer.append(key);
            buffer.append('=');
            buffer.append(quote((String) this.parameters.get(key)));
        }
        return buffer.toString();
    }

    private static boolean isTokenChar(char c) {
        return c > ' ' && c < 127 && TSPECIALS.indexOf(c) < 0;
    }

    private static int skipWhiteSpace(String rawdata, int i) {
        int length = rawdata.length();
        while (i < length && Character.isWhitespace(rawdata.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String quote(String value) {
        boolean needsQuotes = false;
        int length = value.length();
        for (int i = 0; i < length && !needsQuotes; i++) {
            needsQuotes = !isTokenChar(value.charAt(i));
        }
        if (!needsQuotes) {
            return value;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.ensureCapacity((int) (((double) length) * 1.5d));
        buffer.append('\"');
        for (int i2 = 0; i2 < length; i2++) {
            char c = value.charAt(i2);
            if (c == '\\' || c == '\"') {
                buffer.append('\\');
            }
            buffer.append(c);
        }
        buffer.append('\"');
        return buffer.toString();
    }

    private static String unquote(String value) {
        int valueLength = value.length();
        StringBuffer buffer = new StringBuffer();
        buffer.ensureCapacity(valueLength);
        boolean escaped = false;
        for (int i = 0; i < valueLength; i++) {
            char currentChar = value.charAt(i);
            if (!escaped && currentChar != '\\') {
                buffer.append(currentChar);
            } else if (escaped) {
                buffer.append(currentChar);
                escaped = false;
            } else {
                escaped = true;
            }
        }
        return buffer.toString();
    }
}
