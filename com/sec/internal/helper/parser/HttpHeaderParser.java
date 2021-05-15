package com.sec.internal.helper.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HttpHeaderParser {
    private static final String COMMA = ",";
    private static final String QUOTE = "\"";
    protected static final String REGEX_ALGORITHM = "algorithm[\\s]*=";
    protected static final String REGEX_NONCE = "nonce[\\s]*=";
    protected static final String REGEX_OPAQUE = "opaque[\\s]*=";
    protected static final String REGEX_QOP = "qop[\\s]*=";
    protected static final String REGEX_REALM = "realm[\\s]*=";
    protected static final String REGEX_STALE = "stale[\\s]*=";
    private static final String SPACE_REGEX = "[\\s]*";
    protected static final String SPACE_SEPERATOR_REGEX = "[\\s]*=";
    private Matcher paramMatcher = null;
    private Pattern paramPattern = null;

    /* access modifiers changed from: protected */
    public String getParamValue(String splitHeader) {
        String paramVal;
        if (splitHeader.startsWith(QUOTE)) {
            paramVal = splitHeader.substring(1, splitHeader.indexOf(QUOTE, 1));
        } else if (splitHeader.contains(COMMA)) {
            paramVal = splitHeader.substring(0, splitHeader.indexOf(COMMA));
        } else {
            paramVal = splitHeader;
        }
        if (paramVal.contains(COMMA)) {
            return paramVal.substring(0, paramVal.indexOf(COMMA));
        }
        return paramVal;
    }

    /* access modifiers changed from: protected */
    public String getSplitHeader(String regex, String headerVal) {
        Pattern compile = Pattern.compile(regex, 2);
        this.paramPattern = compile;
        Matcher matcher = compile.matcher(headerVal);
        this.paramMatcher = matcher;
        if (matcher.find()) {
            return headerVal.substring(this.paramMatcher.end()).trim();
        }
        return null;
    }
}
