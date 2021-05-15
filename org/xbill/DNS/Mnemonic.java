package org.xbill.DNS;

import java.util.HashMap;

class Mnemonic {
    static final int CASE_LOWER = 3;
    static final int CASE_SENSITIVE = 1;
    static final int CASE_UPPER = 2;
    private static Integer[] cachedInts = new Integer[64];
    private String description;
    private int max = Integer.MAX_VALUE;
    private boolean numericok;
    private String prefix;
    private HashMap strings = new HashMap();
    private HashMap values = new HashMap();
    private int wordcase;

    static {
        int i = 0;
        while (true) {
            Integer[] numArr = cachedInts;
            if (i < numArr.length) {
                numArr[i] = new Integer(i);
                i++;
            } else {
                return;
            }
        }
    }

    public Mnemonic(String description2, int wordcase2) {
        this.description = description2;
        this.wordcase = wordcase2;
    }

    public void setMaximum(int max2) {
        this.max = max2;
    }

    public void setPrefix(String prefix2) {
        this.prefix = sanitize(prefix2);
    }

    public void setNumericAllowed(boolean numeric) {
        this.numericok = numeric;
    }

    public static Integer toInteger(int val) {
        if (val >= 0) {
            Integer[] numArr = cachedInts;
            if (val < numArr.length) {
                return numArr[val];
            }
        }
        return new Integer(val);
    }

    public void check(int val) {
        if (val < 0 || val > this.max) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(this.description);
            stringBuffer.append(" ");
            stringBuffer.append(val);
            stringBuffer.append("is out of range");
            throw new IllegalArgumentException(stringBuffer.toString());
        }
    }

    private String sanitize(String str) {
        int i = this.wordcase;
        if (i == 2) {
            return str.toUpperCase();
        }
        if (i == 3) {
            return str.toLowerCase();
        }
        return str;
    }

    private int parseNumeric(String s) {
        try {
            int val = Integer.parseInt(s);
            if (val < 0 || val > this.max) {
                return -1;
            }
            return val;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void add(int val, String str) {
        check(val);
        Integer value = toInteger(val);
        String str2 = sanitize(str);
        this.strings.put(str2, value);
        this.values.put(value, str2);
    }

    public void addAlias(int val, String str) {
        check(val);
        Integer value = toInteger(val);
        this.strings.put(sanitize(str), value);
    }

    public void addAll(Mnemonic source) {
        if (this.wordcase == source.wordcase) {
            this.strings.putAll(source.strings);
            this.values.putAll(source.values);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(source.description);
        stringBuffer.append(": wordcases do not match");
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    public String getText(int val) {
        check(val);
        String str = (String) this.values.get(toInteger(val));
        if (str != null) {
            return str;
        }
        String str2 = Integer.toString(val);
        if (this.prefix == null) {
            return str2;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.prefix);
        stringBuffer.append(str2);
        return stringBuffer.toString();
    }

    public int getValue(String str) {
        int val;
        String str2 = sanitize(str);
        Integer value = (Integer) this.strings.get(str2);
        if (value != null) {
            return value.intValue();
        }
        String str3 = this.prefix;
        if (str3 != null && str2.startsWith(str3) && (val = parseNumeric(str2.substring(this.prefix.length()))) >= 0) {
            return val;
        }
        if (this.numericok != 0) {
            return parseNumeric(str2);
        }
        return -1;
    }
}
