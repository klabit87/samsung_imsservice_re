package com.sun.activation.registries;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MailcapFile {
    private static boolean addReverse;
    private Map fallback_hash = new HashMap();
    private Map native_commands = new HashMap();
    private Map type_hash = new HashMap();

    static {
        addReverse = false;
        try {
            addReverse = Boolean.getBoolean("javax.activation.addreverse");
        } catch (Throwable th) {
        }
    }

    public MailcapFile(String new_fname) throws IOException {
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: file " + new_fname);
        }
        FileReader reader = null;
        try {
            reader = new FileReader(new_fname);
            parse(new BufferedReader(reader));
            try {
                reader.close();
            } catch (IOException e) {
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
    }

    public MailcapFile(InputStream is) throws IOException {
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: InputStream");
        }
        parse(new BufferedReader(new InputStreamReader(is, "iso-8859-1")));
    }

    public MailcapFile() {
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: default");
        }
    }

    public Map getMailcapList(String mime_type) {
        Map search_result = (Map) this.type_hash.get(mime_type);
        int separator = mime_type.indexOf(47);
        if (mime_type.substring(separator + 1).equals("*")) {
            return search_result;
        }
        Map wildcard_result = (Map) this.type_hash.get(String.valueOf(mime_type.substring(0, separator + 1)) + "*");
        if (wildcard_result == null) {
            return search_result;
        }
        if (search_result != null) {
            return mergeResults(search_result, wildcard_result);
        }
        return wildcard_result;
    }

    public Map getMailcapFallbackList(String mime_type) {
        Map search_result = (Map) this.fallback_hash.get(mime_type);
        int separator = mime_type.indexOf(47);
        if (mime_type.substring(separator + 1).equals("*")) {
            return search_result;
        }
        Map wildcard_result = (Map) this.fallback_hash.get(String.valueOf(mime_type.substring(0, separator + 1)) + "*");
        if (wildcard_result == null) {
            return search_result;
        }
        if (search_result != null) {
            return mergeResults(search_result, wildcard_result);
        }
        return wildcard_result;
    }

    public String[] getMimeTypes() {
        Set types = new HashSet(this.type_hash.keySet());
        types.addAll(this.fallback_hash.keySet());
        types.addAll(this.native_commands.keySet());
        return (String[]) types.toArray(new String[types.size()]);
    }

    public String[] getNativeCommands(String mime_type) {
        String[] cmds = null;
        List v = (List) this.native_commands.get(mime_type.toLowerCase(Locale.ENGLISH));
        if (v != null) {
            return (String[]) v.toArray(new String[v.size()]);
        }
        return cmds;
    }

    private Map mergeResults(Map first, Map second) {
        Map clonedHash = new HashMap(first);
        for (String verb : second.keySet()) {
            List cmdVector = (List) clonedHash.get(verb);
            if (cmdVector == null) {
                clonedHash.put(verb, second.get(verb));
            } else {
                List cmdVector2 = new ArrayList(cmdVector);
                cmdVector2.addAll((List) second.get(verb));
                clonedHash.put(verb, cmdVector2);
            }
        }
        return clonedHash;
    }

    public void appendToMailcap(String mail_cap) {
        if (LogSupport.isLoggable()) {
            LogSupport.log("appendToMailcap: " + mail_cap);
        }
        try {
            parse(new StringReader(mail_cap));
        } catch (IOException e) {
        }
    }

    private void parse(Reader reader) throws IOException {
        BufferedReader buf_reader = new BufferedReader(reader);
        String continued = null;
        while (true) {
            String readLine = buf_reader.readLine();
            String line = readLine;
            if (readLine != null) {
                String line2 = line.trim();
                try {
                    if (line2.charAt(0) != '#') {
                        if (line2.charAt(line2.length() - 1) == '\\') {
                            if (continued != null) {
                                continued = String.valueOf(continued) + line2.substring(0, line2.length() - 1);
                            } else {
                                continued = line2.substring(0, line2.length() - 1);
                            }
                        } else if (continued != null) {
                            try {
                                parseLine(String.valueOf(continued) + line2);
                            } catch (MailcapParseException e) {
                            }
                            continued = null;
                        } else {
                            try {
                                parseLine(line2);
                            } catch (MailcapParseException e2) {
                            }
                        }
                    }
                } catch (StringIndexOutOfBoundsException e3) {
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void parseLine(String mailcapEntry) throws MailcapParseException, IOException {
        String str = mailcapEntry;
        MailcapTokenizer tokenizer = new MailcapTokenizer(str);
        tokenizer.setIsAutoquoting(false);
        if (LogSupport.isLoggable()) {
            LogSupport.log("parse: " + str);
        }
        int currentToken = tokenizer.nextToken();
        int i = 2;
        if (currentToken != 2) {
            reportParseError(2, currentToken, tokenizer.getCurrentTokenValue());
        }
        String primaryType = tokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
        String subType = "*";
        int currentToken2 = tokenizer.nextToken();
        if (!(currentToken2 == 47 || currentToken2 == 59)) {
            reportParseError(47, 59, currentToken2, tokenizer.getCurrentTokenValue());
        }
        if (currentToken2 == 47) {
            int currentToken3 = tokenizer.nextToken();
            if (currentToken3 != 2) {
                reportParseError(2, currentToken3, tokenizer.getCurrentTokenValue());
            }
            subType = tokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
            currentToken2 = tokenizer.nextToken();
        }
        String mimeType = String.valueOf(primaryType) + "/" + subType;
        if (LogSupport.isLoggable()) {
            LogSupport.log("  Type: " + mimeType);
        }
        Map commands = new LinkedHashMap();
        if (currentToken2 != 59) {
            reportParseError(59, currentToken2, tokenizer.getCurrentTokenValue());
        }
        boolean z = true;
        tokenizer.setIsAutoquoting(true);
        int currentToken4 = tokenizer.nextToken();
        tokenizer.setIsAutoquoting(false);
        if (!(currentToken4 == 2 || currentToken4 == 59)) {
            reportParseError(2, 59, currentToken4, tokenizer.getCurrentTokenValue());
        }
        if (currentToken4 == 2) {
            List v = (List) this.native_commands.get(mimeType);
            if (v == null) {
                List v2 = new ArrayList();
                v2.add(str);
                this.native_commands.put(mimeType, v2);
            } else {
                v.add(str);
            }
        }
        if (currentToken4 != 59) {
            currentToken4 = tokenizer.nextToken();
        }
        int i2 = 5;
        if (currentToken4 == 59) {
            boolean isFallback = false;
            while (true) {
                int currentToken5 = tokenizer.nextToken();
                if (currentToken5 != i) {
                    reportParseError(i, currentToken5, tokenizer.getCurrentTokenValue());
                }
                String paramName = tokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
                int currentToken6 = tokenizer.nextToken();
                if (!(currentToken6 == 61 || currentToken6 == 59 || currentToken6 == i2)) {
                    reportParseError(61, 59, i2, currentToken6, tokenizer.getCurrentTokenValue());
                }
                if (currentToken6 == 61) {
                    tokenizer.setIsAutoquoting(z);
                    int currentToken7 = tokenizer.nextToken();
                    tokenizer.setIsAutoquoting(false);
                    if (currentToken7 != 2) {
                        reportParseError(2, currentToken7, tokenizer.getCurrentTokenValue());
                    }
                    String paramValue = tokenizer.getCurrentTokenValue();
                    if (paramName.startsWith("x-java-")) {
                        String commandName = paramName.substring(7);
                        if (!commandName.equals("fallback-entry") || !paramValue.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("    Command: " + commandName + ", Class: " + paramValue);
                            }
                            List classes = (List) commands.get(commandName);
                            if (classes == null) {
                                classes = new ArrayList();
                                commands.put(commandName, classes);
                            }
                            if (addReverse) {
                                classes.add(0, paramValue);
                            } else {
                                classes.add(paramValue);
                            }
                        } else {
                            isFallback = true;
                        }
                    }
                    currentToken6 = tokenizer.nextToken();
                }
                if (currentToken6 != 59) {
                    break;
                }
                i = 2;
                z = true;
                i2 = 5;
            }
            Map masterHash = isFallback ? this.fallback_hash : this.type_hash;
            Map curcommands = (Map) masterHash.get(mimeType);
            if (curcommands == null) {
                masterHash.put(mimeType, commands);
                return;
            }
            if (LogSupport.isLoggable()) {
                LogSupport.log("Merging commands for type " + mimeType);
            }
            for (String cmdName : curcommands.keySet()) {
                List ccv = (List) curcommands.get(cmdName);
                List<String> cv = (List) commands.get(cmdName);
                if (cv != null) {
                    for (String clazz : cv) {
                        if (!ccv.contains(clazz)) {
                            if (addReverse) {
                                ccv.add(0, clazz);
                            } else {
                                ccv.add(clazz);
                            }
                        }
                    }
                }
            }
            for (String cmdName2 : commands.keySet()) {
                if (!curcommands.containsKey(cmdName2)) {
                    curcommands.put(cmdName2, (List) commands.get(cmdName2));
                }
            }
        } else if (currentToken4 != 5) {
            reportParseError(5, 59, currentToken4, tokenizer.getCurrentTokenValue());
        }
    }

    protected static void reportParseError(int expectedToken, int actualToken, String actualTokenValue) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + " token.");
    }

    protected static void reportParseError(int expectedToken, int otherExpectedToken, int actualToken, String actualTokenValue) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + " or a " + MailcapTokenizer.nameForToken(otherExpectedToken) + " token.");
    }

    protected static void reportParseError(int expectedToken, int otherExpectedToken, int anotherExpectedToken, int actualToken, String actualTokenValue) throws MailcapParseException {
        if (LogSupport.isLoggable()) {
            LogSupport.log("PARSE ERROR: Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + ", a " + MailcapTokenizer.nameForToken(otherExpectedToken) + ", or a " + MailcapTokenizer.nameForToken(anotherExpectedToken) + " token.");
        }
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + ", a " + MailcapTokenizer.nameForToken(otherExpectedToken) + ", or a " + MailcapTokenizer.nameForToken(anotherExpectedToken) + " token.");
    }
}
