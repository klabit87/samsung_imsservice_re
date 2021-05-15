package org.xbill.DNS;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Lookup {
    public static final int HOST_NOT_FOUND = 3;
    public static final int SUCCESSFUL = 0;
    public static final int TRY_AGAIN = 2;
    public static final int TYPE_NOT_FOUND = 4;
    public static final int UNRECOVERABLE = 1;
    static /* synthetic */ Class class$org$xbill$DNS$Lookup;
    private static Map defaultCaches;
    private static int defaultNdots;
    private static Resolver defaultResolver;
    private static Name[] defaultSearchPath;
    private static final Name[] noAliases = new Name[0];
    private List aliases;
    private Record[] answers;
    private boolean badresponse;
    private String badresponse_error;
    private Cache cache;
    private int credibility;
    private int dclass;
    private boolean done;
    private boolean doneCurrent;
    private String error;
    private boolean foundAlias;
    private int iterations;
    private Name name;
    private boolean nametoolong;
    private boolean networkerror;
    private boolean nxdomain;
    private boolean referral;
    private Resolver resolver;
    private int result;
    private Name[] searchPath;
    private boolean temporary_cache;
    private boolean timedout;
    private int type;
    private boolean verbose;

    static {
        refreshDefault();
    }

    public static synchronized void refreshDefault() {
        synchronized (Lookup.class) {
            try {
                defaultResolver = new ExtendedResolver();
                defaultSearchPath = ResolverConfig.getCurrentConfig().searchPath();
                defaultCaches = new HashMap();
                defaultNdots = ResolverConfig.getCurrentConfig().ndots();
            } catch (UnknownHostException e) {
                throw new RuntimeException("Failed to initialize resolver");
            }
        }
    }

    public static synchronized Resolver getDefaultResolver() {
        Resolver resolver2;
        synchronized (Lookup.class) {
            resolver2 = defaultResolver;
        }
        return resolver2;
    }

    public static synchronized void setDefaultResolver(Resolver resolver2) {
        synchronized (Lookup.class) {
            defaultResolver = resolver2;
        }
    }

    public static synchronized Cache getDefaultCache(int dclass2) {
        Cache c;
        synchronized (Lookup.class) {
            DClass.check(dclass2);
            c = (Cache) defaultCaches.get(Mnemonic.toInteger(dclass2));
            if (c == null) {
                c = new Cache(dclass2);
                defaultCaches.put(Mnemonic.toInteger(dclass2), c);
            }
        }
        return c;
    }

    public static synchronized void setDefaultCache(Cache cache2, int dclass2) {
        synchronized (Lookup.class) {
            DClass.check(dclass2);
            defaultCaches.put(Mnemonic.toInteger(dclass2), cache2);
        }
    }

    public static synchronized Name[] getDefaultSearchPath() {
        Name[] nameArr;
        synchronized (Lookup.class) {
            nameArr = defaultSearchPath;
        }
        return nameArr;
    }

    public static synchronized void setDefaultSearchPath(Name[] domains) {
        synchronized (Lookup.class) {
            defaultSearchPath = domains;
        }
    }

    public static synchronized void setDefaultSearchPath(String[] domains) throws TextParseException {
        synchronized (Lookup.class) {
            if (domains == null) {
                defaultSearchPath = null;
                return;
            }
            Name[] newdomains = new Name[domains.length];
            for (int i = 0; i < domains.length; i++) {
                newdomains[i] = Name.fromString(domains[i], Name.root);
            }
            defaultSearchPath = newdomains;
        }
    }

    public static synchronized void setPacketLogger(PacketLogger logger) {
        synchronized (Lookup.class) {
            Client.setPacketLogger(logger);
        }
    }

    private final void reset() {
        this.iterations = 0;
        this.foundAlias = false;
        this.done = false;
        this.doneCurrent = false;
        this.aliases = null;
        this.answers = null;
        this.result = -1;
        this.error = null;
        this.nxdomain = false;
        this.badresponse = false;
        this.badresponse_error = null;
        this.networkerror = false;
        this.timedout = false;
        this.nametoolong = false;
        this.referral = false;
        if (this.temporary_cache) {
            this.cache.clearCache();
        }
    }

    public Lookup(Name name2, int type2, int dclass2) {
        Type.check(type2);
        DClass.check(dclass2);
        if (Type.isRR(type2) || type2 == 255) {
            this.name = name2;
            this.type = type2;
            this.dclass = dclass2;
            Class cls = class$org$xbill$DNS$Lookup;
            if (cls == null) {
                cls = class$("org.xbill.DNS.Lookup");
                class$org$xbill$DNS$Lookup = cls;
            }
            synchronized (cls) {
                this.resolver = getDefaultResolver();
                this.searchPath = getDefaultSearchPath();
                this.cache = getDefaultCache(dclass2);
            }
            this.credibility = 3;
            this.verbose = Options.check("verbose");
            this.result = -1;
            return;
        }
        throw new IllegalArgumentException("Cannot query for meta-types other than ANY");
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError().initCause(e);
        }
    }

    public Lookup(Name name2, int type2) {
        this(name2, type2, 1);
    }

    public Lookup(Name name2) {
        this(name2, 1, 1);
    }

    public Lookup(String name2, int type2, int dclass2) throws TextParseException {
        this(Name.fromString(name2), type2, dclass2);
    }

    public Lookup(String name2, int type2) throws TextParseException {
        this(Name.fromString(name2), type2, 1);
    }

    public Lookup(String name2) throws TextParseException {
        this(Name.fromString(name2), 1, 1);
    }

    public void setResolver(Resolver resolver2) {
        this.resolver = resolver2;
    }

    public void setSearchPath(Name[] domains) {
        this.searchPath = domains;
    }

    public void setSearchPath(String[] domains) throws TextParseException {
        if (domains == null) {
            this.searchPath = null;
            return;
        }
        Name[] newdomains = new Name[domains.length];
        for (int i = 0; i < domains.length; i++) {
            newdomains[i] = Name.fromString(domains[i], Name.root);
        }
        this.searchPath = newdomains;
    }

    public void setCache(Cache cache2) {
        if (cache2 == null) {
            this.cache = new Cache(this.dclass);
            this.temporary_cache = true;
            return;
        }
        this.cache = cache2;
        this.temporary_cache = false;
    }

    public void setNdots(int ndots) {
        if (ndots >= 0) {
            defaultNdots = ndots;
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Illegal ndots value: ");
        stringBuffer.append(ndots);
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    public void setCredibility(int credibility2) {
        this.credibility = credibility2;
    }

    private void follow(Name name2, Name oldname) {
        this.foundAlias = true;
        this.badresponse = false;
        this.networkerror = false;
        this.timedout = false;
        this.nxdomain = false;
        this.referral = false;
        int i = this.iterations + 1;
        this.iterations = i;
        if (i >= 6 || name2.equals(oldname)) {
            this.result = 1;
            this.error = "CNAME loop";
            this.done = true;
            return;
        }
        if (this.aliases == null) {
            this.aliases = new ArrayList();
        }
        this.aliases.add(oldname);
        lookup(name2);
    }

    private void processResponse(Name name2, SetResponse response) {
        if (response.isSuccessful()) {
            RRset[] rrsets = response.answers();
            List l = new ArrayList();
            for (RRset rrs : rrsets) {
                Iterator it = rrs.rrs();
                while (it.hasNext()) {
                    l.add(it.next());
                }
            }
            this.result = 0;
            this.answers = (Record[]) l.toArray(new Record[l.size()]);
            this.done = true;
        } else if (response.isNXDOMAIN()) {
            this.nxdomain = true;
            this.doneCurrent = true;
            if (this.iterations > 0) {
                this.result = 3;
                this.done = true;
            }
        } else if (response.isNXRRSET()) {
            this.result = 4;
            this.answers = null;
            this.done = true;
        } else if (response.isCNAME()) {
            follow(response.getCNAME().getTarget(), name2);
        } else if (response.isDNAME()) {
            try {
                follow(name2.fromDNAME(response.getDNAME()), name2);
            } catch (NameTooLongException e) {
                this.result = 1;
                this.error = "Invalid DNAME target";
                this.done = true;
            }
        } else if (response.isDelegation()) {
            this.referral = true;
        }
    }

    private void lookup(Name current) {
        SetResponse sr = this.cache.lookupRecords(current, this.type, this.credibility);
        if (this.verbose) {
            PrintStream printStream = System.err;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("lookup ");
            stringBuffer.append(current);
            stringBuffer.append(" ");
            stringBuffer.append(Type.string(this.type));
            printStream.println(stringBuffer.toString());
            System.err.println(sr);
        }
        processResponse(current, sr);
        if (!this.done && !this.doneCurrent) {
            Message query = Message.newQuery(Record.newRecord(current, this.type, this.dclass));
            try {
                Message response = this.resolver.send(query);
                int rcode = response.getHeader().getRcode();
                if (rcode != 0 && rcode != 3) {
                    this.badresponse = true;
                    this.badresponse_error = Rcode.string(rcode);
                } else if (!query.getQuestion().equals(response.getQuestion())) {
                    this.badresponse = true;
                    this.badresponse_error = "response does not match query";
                } else {
                    SetResponse sr2 = this.cache.addMessage(response);
                    if (sr2 == null) {
                        sr2 = this.cache.lookupRecords(current, this.type, this.credibility);
                    }
                    if (this.verbose) {
                        PrintStream printStream2 = System.err;
                        StringBuffer stringBuffer2 = new StringBuffer();
                        stringBuffer2.append("queried ");
                        stringBuffer2.append(current);
                        stringBuffer2.append(" ");
                        stringBuffer2.append(Type.string(this.type));
                        printStream2.println(stringBuffer2.toString());
                        System.err.println(sr2);
                    }
                    processResponse(current, sr2);
                }
            } catch (IOException e) {
                if (e instanceof InterruptedIOException) {
                    this.timedout = true;
                } else {
                    this.networkerror = true;
                }
            }
        }
    }

    private void resolve(Name current, Name suffix) {
        Name tname;
        this.doneCurrent = false;
        if (suffix == null) {
            tname = current;
        } else {
            try {
                tname = Name.concatenate(current, suffix);
            } catch (NameTooLongException e) {
                this.nametoolong = true;
                return;
            }
        }
        lookup(tname);
    }

    public Record[] run() {
        if (this.done) {
            reset();
        }
        if (!this.name.isAbsolute()) {
            if (this.searchPath != null) {
                if (this.name.labels() > defaultNdots) {
                    resolve(this.name, Name.root);
                }
                if (!this.done) {
                    int i = 0;
                    while (true) {
                        Name[] nameArr = this.searchPath;
                        if (i >= nameArr.length) {
                            break;
                        }
                        resolve(this.name, nameArr[i]);
                        if (this.done) {
                            return this.answers;
                        }
                        if (this.foundAlias) {
                            break;
                        }
                        i++;
                    }
                } else {
                    return this.answers;
                }
            } else {
                resolve(this.name, Name.root);
            }
        } else {
            resolve(this.name, (Name) null);
        }
        if (this.done == 0) {
            if (this.badresponse) {
                this.result = 2;
                this.error = this.badresponse_error;
                this.done = true;
            } else if (this.timedout) {
                this.result = 2;
                this.error = "timed out";
                this.done = true;
            } else if (this.networkerror) {
                this.result = 2;
                this.error = "network error";
                this.done = true;
            } else if (this.nxdomain) {
                this.result = 3;
                this.done = true;
            } else if (this.referral) {
                this.result = 1;
                this.error = "referral";
                this.done = true;
            } else if (this.nametoolong) {
                this.result = 1;
                this.error = "name too long";
                this.done = true;
            }
        }
        return this.answers;
    }

    private void checkDone() {
        if (!this.done || this.result == -1) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Lookup of ");
            stringBuffer.append(this.name);
            stringBuffer.append(" ");
            StringBuffer sb = new StringBuffer(stringBuffer.toString());
            if (this.dclass != 1) {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append(DClass.string(this.dclass));
                stringBuffer2.append(" ");
                sb.append(stringBuffer2.toString());
            }
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append(Type.string(this.type));
            stringBuffer3.append(" isn't done");
            sb.append(stringBuffer3.toString());
            throw new IllegalStateException(sb.toString());
        }
    }

    public Record[] getAnswers() {
        checkDone();
        return this.answers;
    }

    public Name[] getAliases() {
        checkDone();
        List list = this.aliases;
        if (list == null) {
            return noAliases;
        }
        return (Name[]) list.toArray(new Name[list.size()]);
    }

    public int getResult() {
        checkDone();
        return this.result;
    }

    public String getErrorString() {
        checkDone();
        String str = this.error;
        if (str != null) {
            return str;
        }
        int i = this.result;
        if (i == 0) {
            return "successful";
        }
        if (i == 1) {
            return "unrecoverable error";
        }
        if (i == 2) {
            return "try again";
        }
        if (i == 3) {
            return "host not found";
        }
        if (i == 4) {
            return "type not found";
        }
        throw new IllegalStateException("unknown result");
    }
}
