package javax.mail.internet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ParameterList {
    private static boolean applehack;
    private static boolean decodeParameters;
    private static boolean decodeParametersStrict;
    private static boolean encodeParameters;
    private static final char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private String lastName;
    private Map list;
    private Set multisegmentNames;
    private Map slist;

    /* JADX WARNING: Removed duplicated region for block: B:14:0x002f A[Catch:{ SecurityException -> 0x005b }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0031 A[Catch:{ SecurityException -> 0x005b }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0043 A[Catch:{ SecurityException -> 0x005b }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0045 A[Catch:{ SecurityException -> 0x005b }] */
    static {
        /*
            r0 = 0
            encodeParameters = r0
            decodeParameters = r0
            decodeParametersStrict = r0
            applehack = r0
            java.lang.String r1 = "mail.mime.encodeparameters"
            java.lang.String r1 = java.lang.System.getProperty(r1)     // Catch:{ SecurityException -> 0x005b }
            r2 = 1
            java.lang.String r3 = "true"
            if (r1 == 0) goto L_0x001d
            boolean r4 = r1.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x005b }
            if (r4 == 0) goto L_0x001d
            r4 = r2
            goto L_0x001e
        L_0x001d:
            r4 = r0
        L_0x001e:
            encodeParameters = r4     // Catch:{ SecurityException -> 0x005b }
            java.lang.String r4 = "mail.mime.decodeparameters"
            java.lang.String r4 = java.lang.System.getProperty(r4)     // Catch:{ SecurityException -> 0x005b }
            r1 = r4
            if (r1 == 0) goto L_0x0031
            boolean r4 = r1.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x005b }
            if (r4 == 0) goto L_0x0031
            r4 = r2
            goto L_0x0032
        L_0x0031:
            r4 = r0
        L_0x0032:
            decodeParameters = r4     // Catch:{ SecurityException -> 0x005b }
            java.lang.String r4 = "mail.mime.decodeparameters.strict"
            java.lang.String r4 = java.lang.System.getProperty(r4)     // Catch:{ SecurityException -> 0x005b }
            r1 = r4
            if (r1 == 0) goto L_0x0045
            boolean r4 = r1.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x005b }
            if (r4 == 0) goto L_0x0045
            r4 = r2
            goto L_0x0046
        L_0x0045:
            r4 = r0
        L_0x0046:
            decodeParametersStrict = r4     // Catch:{ SecurityException -> 0x005b }
            java.lang.String r4 = "mail.mime.applefilenames"
            java.lang.String r4 = java.lang.System.getProperty(r4)     // Catch:{ SecurityException -> 0x005b }
            r1 = r4
            if (r1 == 0) goto L_0x0058
            boolean r3 = r1.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x005b }
            if (r3 == 0) goto L_0x0058
            r0 = r2
        L_0x0058:
            applehack = r0     // Catch:{ SecurityException -> 0x005b }
            goto L_0x005c
        L_0x005b:
            r0 = move-exception
        L_0x005c:
            r0 = 16
            char[] r0 = new char[r0]
            r0 = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70} // fill-array
            hex = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.<clinit>():void");
    }

    private static class Value {
        String charset;
        String encodedValue;
        String value;

        private Value() {
        }

        /* synthetic */ Value(Value value2) {
            this();
        }
    }

    private static class MultiValue extends ArrayList {
        String value;

        private MultiValue() {
        }

        /* synthetic */ MultiValue(MultiValue multiValue) {
            this();
        }
    }

    private static class ParamEnum implements Enumeration {
        private Iterator it;

        ParamEnum(Iterator it2) {
            this.it = it2;
        }

        public boolean hasMoreElements() {
            return this.it.hasNext();
        }

        public Object nextElement() {
            return this.it.next();
        }
    }

    public ParameterList() {
        this.list = new LinkedHashMap();
        this.lastName = null;
        if (decodeParameters) {
            this.multisegmentNames = new HashSet();
            this.slist = new HashMap();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0125, code lost:
        throw new javax.mail.internet.ParseException("Expected ';', got \"" + r1.getValue() + "\"");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ParameterList(java.lang.String r9) throws javax.mail.internet.ParseException {
        /*
            r8 = this;
            r8.<init>()
            javax.mail.internet.HeaderTokenizer r0 = new javax.mail.internet.HeaderTokenizer
            java.lang.String r1 = "()<>@,;:\\\"\t []/?="
            r0.<init>(r9, r1)
        L_0x000a:
            javax.mail.internet.HeaderTokenizer$Token r1 = r0.next()
            int r2 = r1.getType()
            r3 = -4
            if (r2 != r3) goto L_0x0016
            goto L_0x0029
        L_0x0016:
            char r4 = (char) r2
            r5 = 59
            r6 = -1
            java.lang.String r7 = "\""
            if (r4 != r5) goto L_0x00c3
            javax.mail.internet.HeaderTokenizer$Token r1 = r0.next()
            int r4 = r1.getType()
            if (r4 != r3) goto L_0x0032
        L_0x0029:
            boolean r1 = decodeParameters
            if (r1 == 0) goto L_0x0031
            r1 = 0
            r8.combineMultisegmentNames(r1)
        L_0x0031:
            return
        L_0x0032:
            int r3 = r1.getType()
            if (r3 != r6) goto L_0x00a8
            java.lang.String r3 = r1.getValue()
            java.util.Locale r4 = java.util.Locale.ENGLISH
            java.lang.String r3 = r3.toLowerCase(r4)
            javax.mail.internet.HeaderTokenizer$Token r1 = r0.next()
            int r4 = r1.getType()
            char r4 = (char) r4
            r5 = 61
            if (r4 != r5) goto L_0x008d
            javax.mail.internet.HeaderTokenizer$Token r1 = r0.next()
            int r2 = r1.getType()
            if (r2 == r6) goto L_0x0078
            r4 = -2
            if (r2 != r4) goto L_0x005d
            goto L_0x0078
        L_0x005d:
            javax.mail.internet.ParseException r4 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r6 = "Expected parameter value, got \""
            r5.<init>(r6)
            java.lang.String r6 = r1.getValue()
            r5.append(r6)
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L_0x0078:
            java.lang.String r4 = r1.getValue()
            r8.lastName = r3
            boolean r5 = decodeParameters
            if (r5 == 0) goto L_0x0086
            r8.putEncodedName(r3, r4)
            goto L_0x000a
        L_0x0086:
            java.util.Map r5 = r8.list
            r5.put(r3, r4)
            goto L_0x000a
        L_0x008d:
            javax.mail.internet.ParseException r4 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r6 = "Expected '=', got \""
            r5.<init>(r6)
            java.lang.String r6 = r1.getValue()
            r5.append(r6)
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L_0x00a8:
            javax.mail.internet.ParseException r3 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "Expected parameter name, got \""
            r4.<init>(r5)
            java.lang.String r5 = r1.getValue()
            r4.append(r5)
            r4.append(r7)
            java.lang.String r4 = r4.toString()
            r3.<init>(r4)
            throw r3
        L_0x00c3:
            boolean r3 = applehack
            if (r3 == 0) goto L_0x010b
            if (r2 != r6) goto L_0x010b
            java.lang.String r3 = r8.lastName
            if (r3 == 0) goto L_0x010b
            java.lang.String r4 = "name"
            boolean r3 = r3.equals(r4)
            if (r3 != 0) goto L_0x00df
            java.lang.String r3 = r8.lastName
            java.lang.String r4 = "filename"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x010b
        L_0x00df:
            java.util.Map r3 = r8.list
            java.lang.String r4 = r8.lastName
            java.lang.Object r3 = r3.get(r4)
            java.lang.String r3 = (java.lang.String) r3
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = java.lang.String.valueOf(r3)
            r4.<init>(r5)
            java.lang.String r5 = " "
            r4.append(r5)
            java.lang.String r5 = r1.getValue()
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            java.util.Map r5 = r8.list
            java.lang.String r6 = r8.lastName
            r5.put(r6, r4)
            goto L_0x000a
        L_0x010b:
            javax.mail.internet.ParseException r3 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "Expected ';', got \""
            r4.<init>(r5)
            java.lang.String r5 = r1.getValue()
            r4.append(r5)
            r4.append(r7)
            java.lang.String r4 = r4.toString()
            r3.<init>(r4)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.<init>(java.lang.String):void");
    }

    private void putEncodedName(String name, String value) throws ParseException {
        Value v;
        int star = name.indexOf(42);
        if (star < 0) {
            this.list.put(name, value);
        } else if (star == name.length() - 1) {
            this.list.put(name.substring(0, star), decodeValue(value));
        } else {
            String rname = name.substring(0, star);
            this.multisegmentNames.add(rname);
            this.list.put(rname, "");
            if (name.endsWith("*")) {
                v = new Value((Value) null);
                v.encodedValue = value;
                v.value = value;
                name = name.substring(0, name.length() - 1);
            } else {
                v = value;
            }
            this.slist.put(name, v);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 14 */
    private void combineMultisegmentNames(boolean keepConsistentOnFailure) throws ParseException {
        try {
            for (String name : this.multisegmentNames) {
                StringBuffer sb = new StringBuffer();
                MultiValue mv = new MultiValue((MultiValue) null);
                String charset = null;
                int segment = 0;
                while (true) {
                    String sname = String.valueOf(name) + "*" + segment;
                    Object v = this.slist.get(sname);
                    if (v == null) {
                        break;
                    }
                    mv.add(v);
                    String value = null;
                    if (v instanceof Value) {
                        Value vv = (Value) v;
                        String evalue = vv.encodedValue;
                        value = evalue;
                        if (segment == 0) {
                            Value vnew = decodeValue(evalue);
                            String str = vnew.charset;
                            vv.charset = str;
                            charset = str;
                            String str2 = vnew.value;
                            vv.value = str2;
                            value = str2;
                        } else if (charset == null) {
                            this.multisegmentNames.remove(name);
                            break;
                        } else {
                            String decodeBytes = decodeBytes(evalue, charset);
                            vv.value = decodeBytes;
                            value = decodeBytes;
                        }
                    } else {
                        value = (String) v;
                    }
                    sb.append(value);
                    this.slist.remove(sname);
                    segment++;
                }
                if (segment == 0) {
                    this.list.remove(name);
                } else {
                    mv.value = sb.toString();
                    this.list.put(name, mv);
                }
            }
            if (keepConsistentOnFailure || 1 != 0) {
                if (this.slist.size() > 0) {
                    for (Object v2 : this.slist.values()) {
                        if (v2 instanceof Value) {
                            Value vv2 = (Value) v2;
                            Value vnew2 = decodeValue(vv2.encodedValue);
                            vv2.charset = vnew2.charset;
                            vv2.value = vnew2.value;
                        }
                    }
                    this.list.putAll(this.slist);
                }
                this.multisegmentNames.clear();
                this.slist.clear();
            }
        } catch (NumberFormatException nex) {
            if (decodeParametersStrict) {
                throw new ParseException(nex.toString());
            }
        } catch (UnsupportedEncodingException uex) {
            if (decodeParametersStrict) {
                throw new ParseException(uex.toString());
            }
        } catch (StringIndexOutOfBoundsException ex) {
            if (decodeParametersStrict) {
                throw new ParseException(ex.toString());
            }
        } catch (Throwable th) {
            if (keepConsistentOnFailure || 0 != 0) {
                if (this.slist.size() > 0) {
                    for (Object v3 : this.slist.values()) {
                        if (v3 instanceof Value) {
                            Value vv3 = (Value) v3;
                            Value vnew3 = decodeValue(vv3.encodedValue);
                            vv3.charset = vnew3.charset;
                            vv3.value = vnew3.value;
                        }
                    }
                    this.list.putAll(this.slist);
                }
                this.multisegmentNames.clear();
                this.slist.clear();
            }
            throw th;
        }
    }

    public int size() {
        return this.list.size();
    }

    public String get(String name) {
        Object v = this.list.get(name.trim().toLowerCase(Locale.ENGLISH));
        if (v instanceof MultiValue) {
            return ((MultiValue) v).value;
        }
        if (v instanceof Value) {
            return ((Value) v).value;
        }
        return (String) v;
    }

    public void set(String name, String value) {
        if (name != null || value == null || !value.equals("DONE")) {
            String name2 = name.trim().toLowerCase(Locale.ENGLISH);
            if (decodeParameters) {
                try {
                    putEncodedName(name2, value);
                } catch (ParseException e) {
                    this.list.put(name2, value);
                }
            } else {
                this.list.put(name2, value);
            }
        } else if (decodeParameters && this.multisegmentNames.size() > 0) {
            try {
                combineMultisegmentNames(true);
            } catch (ParseException e2) {
            }
        }
    }

    public void set(String name, String value, String charset) {
        if (encodeParameters) {
            Value ev = encodeValue(value, charset);
            if (ev != null) {
                this.list.put(name.trim().toLowerCase(Locale.ENGLISH), ev);
            } else {
                set(name, value);
            }
        } else {
            set(name, value);
        }
    }

    public void remove(String name) {
        this.list.remove(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public Enumeration getNames() {
        return new ParamEnum(this.list.keySet().iterator());
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int used) {
        ToStringBuffer sb = new ToStringBuffer(used);
        for (String name : this.list.keySet()) {
            Object v = this.list.get(name);
            if (v instanceof MultiValue) {
                MultiValue vv = (MultiValue) v;
                String ns = String.valueOf(name) + "*";
                for (int i = 0; i < vv.size(); i++) {
                    Object va = vv.get(i);
                    if (va instanceof Value) {
                        sb.addNV(String.valueOf(ns) + i + "*", ((Value) va).encodedValue);
                    } else {
                        sb.addNV(String.valueOf(ns) + i, (String) va);
                    }
                }
            } else if (v instanceof Value) {
                sb.addNV(String.valueOf(name) + "*", ((Value) v).encodedValue);
            } else {
                sb.addNV(name, (String) v);
            }
        }
        return sb.toString();
    }

    private static class ToStringBuffer {
        private StringBuffer sb = new StringBuffer();
        private int used;

        public ToStringBuffer(int used2) {
            this.used = used2;
        }

        public void addNV(String name, String value) {
            String value2 = ParameterList.quote(value);
            this.sb.append("; ");
            this.used += 2;
            if (this.used + name.length() + value2.length() + 1 > 76) {
                this.sb.append("\r\n\t");
                this.used = 8;
            }
            StringBuffer stringBuffer = this.sb;
            stringBuffer.append(name);
            stringBuffer.append('=');
            int length = this.used + name.length() + 1;
            this.used = length;
            if (length + value2.length() > 76) {
                String s = MimeUtility.fold(this.used, value2);
                this.sb.append(s);
                int lastlf = s.lastIndexOf(10);
                if (lastlf >= 0) {
                    this.used += (s.length() - lastlf) - 1;
                } else {
                    this.used += s.length();
                }
            } else {
                this.sb.append(value2);
                this.used += value2.length();
            }
        }

        public String toString() {
            return this.sb.toString();
        }
    }

    /* access modifiers changed from: private */
    public static String quote(String value) {
        return MimeUtility.quote(value, HeaderTokenizer.MIME);
    }

    private static Value encodeValue(String value, String charset) {
        if (MimeUtility.checkAscii(value) == 1) {
            return null;
        }
        try {
            byte[] b = value.getBytes(MimeUtility.javaCharset(charset));
            StringBuffer sb = new StringBuffer(b.length + charset.length() + 2);
            sb.append(charset);
            sb.append("''");
            for (byte b2 : b) {
                char c = (char) (b2 & 255);
                if (c <= ' ' || c >= 127 || c == '*' || c == '\'' || c == '%' || HeaderTokenizer.MIME.indexOf(c) >= 0) {
                    sb.append('%');
                    sb.append(hex[c >> 4]);
                    sb.append(hex[c & 15]);
                } else {
                    sb.append(c);
                }
            }
            Value v = new Value((Value) null);
            v.charset = charset;
            v.value = value;
            v.encodedValue = sb.toString();
            return v;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static Value decodeValue(String value) throws ParseException {
        Value v = new Value((Value) null);
        v.encodedValue = value;
        v.value = value;
        try {
            int i = value.indexOf(39);
            if (i > 0) {
                String charset = value.substring(0, i);
                int li = value.indexOf(39, i + 1);
                if (li >= 0) {
                    String substring = value.substring(i + 1, li);
                    v.charset = charset;
                    v.value = decodeBytes(value.substring(li + 1), charset);
                    return v;
                } else if (!decodeParametersStrict) {
                    return v;
                } else {
                    throw new ParseException("Missing language in encoded value: " + value);
                }
            } else if (!decodeParametersStrict) {
                return v;
            } else {
                throw new ParseException("Missing charset in encoded value: " + value);
            }
        } catch (NumberFormatException nex) {
            if (decodeParametersStrict) {
                throw new ParseException(nex.toString());
            }
        } catch (UnsupportedEncodingException uex) {
            if (decodeParametersStrict) {
                throw new ParseException(uex.toString());
            }
        } catch (StringIndexOutOfBoundsException ex) {
            if (decodeParametersStrict) {
                throw new ParseException(ex.toString());
            }
        }
    }

    private static String decodeBytes(String value, String charset) throws UnsupportedEncodingException {
        byte[] b = new byte[value.length()];
        int i = 0;
        int bi = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '%') {
                c = (char) Integer.parseInt(value.substring(i + 1, i + 3), 16);
                i += 2;
            }
            b[bi] = (byte) c;
            i++;
            bi++;
        }
        return new String(b, 0, bi, MimeUtility.javaCharset(charset));
    }
}
