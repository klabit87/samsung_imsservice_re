package com.sun.mail.imap.protocol;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Response;
import java.io.PrintStream;
import java.util.Vector;
import javax.mail.internet.ParameterList;

public class BODYSTRUCTURE implements Item {
    private static int MULTI = 2;
    private static int NESTED = 3;
    private static int SINGLE = 1;
    static final char[] name = {'B', 'O', 'D', 'Y', 'S', 'T', 'R', 'U', 'C', 'T', 'U', 'R', 'E'};
    private static boolean parseDebug;
    public String attachment;
    public BODYSTRUCTURE[] bodies;
    public ParameterList cParams;
    public ParameterList dParams;
    public String description;
    public String disposition;
    public String encoding;
    public ENVELOPE envelope;
    public String id;
    public String[] language;
    public int lines = -1;
    public String md5;
    public int msgno;
    private int processedType;
    public int size = -1;
    public String subtype;
    public String type;

    static {
        boolean z = true;
        parseDebug = false;
        try {
            String s = System.getProperty("mail.imap.parse.debug");
            if (s == null || !s.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
                z = false;
            }
            parseDebug = z;
        } catch (SecurityException e) {
        }
    }

    public BODYSTRUCTURE(FetchResponse r) throws ParsingException {
        FetchResponse fetchResponse = r;
        if (parseDebug) {
            System.out.println("DEBUG IMAP: parsing BODYSTRUCTURE");
        }
        this.msgno = r.getNumber();
        if (parseDebug) {
            PrintStream printStream = System.out;
            printStream.println("DEBUG IMAP: msgno " + this.msgno);
        }
        r.skipSpaces();
        byte b = 40;
        if (r.readByte() != 40) {
            throw new ParsingException("BODYSTRUCTURE parse error: missing ``('' at start");
        } else if (r.peekByte() == 40) {
            if (parseDebug) {
                System.out.println("DEBUG IMAP: parsing multipart");
            }
            this.type = "multipart";
            this.processedType = MULTI;
            Vector v = new Vector(1);
            while (true) {
                v.addElement(new BODYSTRUCTURE(fetchResponse));
                r.skipSpaces();
                if (r.peekByte() != b) {
                    break;
                }
                b = 40;
            }
            BODYSTRUCTURE[] bodystructureArr = new BODYSTRUCTURE[v.size()];
            this.bodies = bodystructureArr;
            v.copyInto(bodystructureArr);
            this.subtype = r.readString();
            if (parseDebug) {
                PrintStream printStream2 = System.out;
                printStream2.println("DEBUG IMAP: subtype " + this.subtype);
            }
            if (r.readByte() != 41) {
                if (parseDebug) {
                    System.out.println("DEBUG IMAP: parsing extension data");
                }
                this.cParams = parseParameters(r);
                if (r.readByte() != 41) {
                    byte b2 = r.readByte();
                    if (b2 == 40) {
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: parse disposition");
                        }
                        this.disposition = r.readString();
                        if (parseDebug) {
                            PrintStream printStream3 = System.out;
                            printStream3.println("DEBUG IMAP: disposition " + this.disposition);
                        }
                        this.dParams = parseParameters(r);
                        if (r.readByte() != 41) {
                            throw new ParsingException("BODYSTRUCTURE parse error: missing ``)'' at end of disposition in multipart");
                        } else if (parseDebug) {
                            System.out.println("DEBUG IMAP: disposition DONE");
                        }
                    } else if (b2 == 78 || b2 == 110) {
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: disposition NIL");
                        }
                        fetchResponse.skip(2);
                    } else {
                        throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "bad multipart disposition, b " + b2);
                    }
                    byte readByte = r.readByte();
                    byte b3 = readByte;
                    if (readByte == 41) {
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: no body-fld-lang");
                        }
                    } else if (b3 == 32) {
                        if (r.peekByte() == 40) {
                            this.language = r.readStringList();
                            if (parseDebug) {
                                PrintStream printStream4 = System.out;
                                printStream4.println("DEBUG IMAP: language len " + this.language.length);
                            }
                        } else {
                            String l = r.readString();
                            if (l != null) {
                                this.language = new String[]{l};
                                if (parseDebug) {
                                    PrintStream printStream5 = System.out;
                                    printStream5.println("DEBUG IMAP: language " + l);
                                }
                            }
                        }
                        while (r.readByte() == 32) {
                            parseBodyExtension(r);
                        }
                    } else {
                        throw new ParsingException("BODYSTRUCTURE parse error: missing space after disposition");
                    }
                } else if (parseDebug) {
                    System.out.println("DEBUG IMAP: body parameters DONE");
                }
            } else if (parseDebug) {
                System.out.println("DEBUG IMAP: parse DONE");
            }
        } else {
            if (parseDebug) {
                System.out.println("DEBUG IMAP: single part");
            }
            this.type = r.readString();
            if (parseDebug) {
                PrintStream printStream6 = System.out;
                printStream6.println("DEBUG IMAP: type " + this.type);
            }
            this.processedType = SINGLE;
            this.subtype = r.readString();
            if (parseDebug) {
                PrintStream printStream7 = System.out;
                printStream7.println("DEBUG IMAP: subtype " + this.subtype);
            }
            if (this.type == null) {
                this.type = "application";
                this.subtype = "octet-stream";
            }
            this.cParams = parseParameters(r);
            if (parseDebug) {
                PrintStream printStream8 = System.out;
                printStream8.println("DEBUG IMAP: cParams " + this.cParams);
            }
            this.id = r.readString();
            if (parseDebug) {
                PrintStream printStream9 = System.out;
                printStream9.println("DEBUG IMAP: id " + this.id);
            }
            this.description = r.readString();
            if (parseDebug) {
                PrintStream printStream10 = System.out;
                printStream10.println("DEBUG IMAP: description " + this.description);
            }
            this.encoding = r.readString();
            if (parseDebug) {
                PrintStream printStream11 = System.out;
                printStream11.println("DEBUG IMAP: encoding " + this.encoding);
            }
            this.size = r.readNumber();
            if (parseDebug) {
                PrintStream printStream12 = System.out;
                printStream12.println("DEBUG IMAP: size " + this.size);
            }
            if (this.size >= 0) {
                if (this.type.equalsIgnoreCase("text")) {
                    this.lines = r.readNumber();
                    if (parseDebug) {
                        PrintStream printStream13 = System.out;
                        printStream13.println("DEBUG IMAP: lines " + this.lines);
                    }
                    if (this.lines < 0) {
                        throw new ParsingException("BODYSTRUCTURE parse error: bad ``lines'' element");
                    }
                } else if (!this.type.equalsIgnoreCase("message") || !this.subtype.equalsIgnoreCase("rfc822")) {
                    r.skipSpaces();
                    if (Character.isDigit((char) r.peekByte())) {
                        throw new ParsingException("BODYSTRUCTURE parse error: server erroneously included ``lines'' element with type " + this.type + "/" + this.subtype);
                    }
                } else {
                    this.processedType = NESTED;
                    this.envelope = new ENVELOPE(fetchResponse);
                    this.bodies = new BODYSTRUCTURE[]{new BODYSTRUCTURE(fetchResponse)};
                    this.lines = r.readNumber();
                    if (parseDebug) {
                        PrintStream printStream14 = System.out;
                        printStream14.println("DEBUG IMAP: lines " + this.lines);
                    }
                    if (this.lines < 0) {
                        throw new ParsingException("BODYSTRUCTURE parse error: bad ``lines'' element");
                    }
                }
                if (r.peekByte() == 41) {
                    r.readByte();
                    if (parseDebug) {
                        System.out.println("DEBUG IMAP: parse DONE");
                        return;
                    }
                    return;
                }
                this.md5 = r.readString();
                if (r.readByte() != 41) {
                    byte b4 = r.readByte();
                    if (b4 == 40) {
                        this.disposition = r.readString();
                        if (parseDebug) {
                            PrintStream printStream15 = System.out;
                            printStream15.println("DEBUG IMAP: disposition " + this.disposition);
                        }
                        this.dParams = parseParameters(r);
                        if (parseDebug) {
                            PrintStream printStream16 = System.out;
                            printStream16.println("DEBUG IMAP: dParams " + this.dParams);
                        }
                        if (r.readByte() != 41) {
                            throw new ParsingException("BODYSTRUCTURE parse error: missing ``)'' at end of disposition");
                        }
                    } else if (b4 == 78 || b4 == 110) {
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: disposition NIL");
                        }
                        fetchResponse.skip(2);
                    } else {
                        throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "bad single part disposition, b " + b4);
                    }
                    if (r.readByte() != 41) {
                        if (r.peekByte() == 40) {
                            this.language = r.readStringList();
                            if (parseDebug) {
                                PrintStream printStream17 = System.out;
                                printStream17.println("DEBUG IMAP: language len " + this.language.length);
                            }
                        } else {
                            String l2 = r.readString();
                            if (l2 != null) {
                                this.language = new String[]{l2};
                                if (parseDebug) {
                                    PrintStream printStream18 = System.out;
                                    printStream18.println("DEBUG IMAP: language " + l2);
                                }
                            }
                        }
                        while (r.readByte() == 32) {
                            parseBodyExtension(r);
                        }
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: all DONE");
                        }
                    } else if (parseDebug) {
                        System.out.println("DEBUG IMAP: disposition DONE");
                    }
                } else if (parseDebug) {
                    System.out.println("DEBUG IMAP: no MD5 DONE");
                }
            } else {
                throw new ParsingException("BODYSTRUCTURE parse error: bad ``size'' element");
            }
        }
    }

    public boolean isMulti() {
        return this.processedType == MULTI;
    }

    public boolean isSingle() {
        return this.processedType == SINGLE;
    }

    public boolean isNested() {
        return this.processedType == NESTED;
    }

    private ParameterList parseParameters(Response r) throws ParsingException {
        r.skipSpaces();
        ParameterList list = null;
        byte b = r.readByte();
        if (b == 40) {
            list = new ParameterList();
            do {
                String name2 = r.readString();
                if (parseDebug) {
                    PrintStream printStream = System.out;
                    printStream.println("DEBUG IMAP: parameter name " + name2);
                }
                if (name2 != null) {
                    String value = r.readString();
                    if (parseDebug) {
                        PrintStream printStream2 = System.out;
                        printStream2.println("DEBUG IMAP: parameter value " + value);
                    }
                    list.set(name2, value);
                } else {
                    throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "null name in parameter list");
                }
            } while (r.readByte() != 41);
            list.set((String) null, "DONE");
        } else if (b == 78 || b == 110) {
            if (parseDebug) {
                System.out.println("DEBUG IMAP: parameter list NIL");
            }
            r.skip(2);
        } else {
            throw new ParsingException("Parameter list parse error");
        }
        return list;
    }

    private void parseBodyExtension(Response r) throws ParsingException {
        r.skipSpaces();
        byte b = r.peekByte();
        if (b == 40) {
            r.skip(1);
            do {
                parseBodyExtension(r);
            } while (r.readByte() != 41);
        } else if (Character.isDigit((char) b)) {
            r.readNumber();
        } else {
            r.readString();
        }
    }
}
