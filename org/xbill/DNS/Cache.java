package org.xbill.DNS;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Cache {
    private static final int defaultMaxEntries = 50000;
    private CacheMap data;
    private int dclass;
    private int maxcache;
    private int maxncache;

    private interface Element {
        int compareCredibility(int i);

        boolean expired();

        int getType();
    }

    /* access modifiers changed from: private */
    public static int limitExpire(long ttl, long maxttl) {
        if (maxttl >= 0 && maxttl < ttl) {
            ttl = maxttl;
        }
        long expire = (System.currentTimeMillis() / 1000) + ttl;
        if (expire < 0 || expire > TTL.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) expire;
    }

    private static class CacheRRset extends RRset implements Element {
        private static final long serialVersionUID = 5971755205903597024L;
        int credibility;
        int expire;

        public CacheRRset(Record rec, int cred, long maxttl) {
            this.credibility = cred;
            this.expire = Cache.limitExpire(rec.getTTL(), maxttl);
            addRR(rec);
        }

        public CacheRRset(RRset rrset, int cred, long maxttl) {
            super(rrset);
            this.credibility = cred;
            this.expire = Cache.limitExpire(rrset.getTTL(), maxttl);
        }

        public final boolean expired() {
            return ((int) (System.currentTimeMillis() / 1000)) >= this.expire;
        }

        public final int compareCredibility(int cred) {
            return this.credibility - cred;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(" cl = ");
            sb.append(this.credibility);
            return sb.toString();
        }
    }

    private static class NegativeElement implements Element {
        int credibility;
        int expire;
        Name name;
        int type;

        public NegativeElement(Name name2, int type2, SOARecord soa, int cred, long maxttl) {
            this.name = name2;
            this.type = type2;
            long cttl = soa != null ? soa.getMinimum() : 0;
            this.credibility = cred;
            this.expire = Cache.limitExpire(cttl, maxttl);
        }

        public int getType() {
            return this.type;
        }

        public final boolean expired() {
            return ((int) (System.currentTimeMillis() / 1000)) >= this.expire;
        }

        public final int compareCredibility(int cred) {
            return this.credibility - cred;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (this.type == 0) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("NXDOMAIN ");
                stringBuffer.append(this.name);
                sb.append(stringBuffer.toString());
            } else {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("NXRRSET ");
                stringBuffer2.append(this.name);
                stringBuffer2.append(" ");
                stringBuffer2.append(Type.string(this.type));
                sb.append(stringBuffer2.toString());
            }
            sb.append(" cl = ");
            sb.append(this.credibility);
            return sb.toString();
        }
    }

    private static class CacheMap extends LinkedHashMap {
        private int maxsize = -1;

        CacheMap(int maxsize2) {
            super(16, 0.75f, true);
            this.maxsize = maxsize2;
        }

        /* access modifiers changed from: package-private */
        public int getMaxSize() {
            return this.maxsize;
        }

        /* access modifiers changed from: package-private */
        public void setMaxSize(int maxsize2) {
            this.maxsize = maxsize2;
        }

        /* access modifiers changed from: protected */
        public boolean removeEldestEntry(Map.Entry eldest) {
            return this.maxsize >= 0 && size() > this.maxsize;
        }
    }

    public Cache(int dclass2) {
        this.maxncache = -1;
        this.maxcache = -1;
        this.dclass = dclass2;
        this.data = new CacheMap(defaultMaxEntries);
    }

    public Cache() {
        this(1);
    }

    public Cache(String file) throws IOException {
        this.maxncache = -1;
        this.maxcache = -1;
        this.data = new CacheMap(defaultMaxEntries);
        Master m = new Master(file);
        while (true) {
            Record nextRecord = m.nextRecord();
            Record record = nextRecord;
            if (nextRecord != null) {
                addRecord(record, 0, m);
            } else {
                return;
            }
        }
    }

    private synchronized Object exactName(Name name) {
        return this.data.get(name);
    }

    private synchronized void removeName(Name name) {
        this.data.remove(name);
    }

    private synchronized Element[] allElements(Object types) {
        if (types instanceof List) {
            List typelist = (List) types;
            return (Element[]) typelist.toArray(new Element[typelist.size()]);
        }
        return new Element[]{(Element) types};
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    private synchronized Element oneElement(Name name, Object types, int type, int minCred) {
        Element found = null;
        if (type != 255) {
            if (types instanceof List) {
                List list = (List) types;
                int i = 0;
                while (true) {
                    if (i >= list.size()) {
                        break;
                    }
                    Element set = (Element) list.get(i);
                    if (set.getType() == type) {
                        found = set;
                        break;
                    }
                    i++;
                }
            } else {
                Element set2 = (Element) types;
                if (set2.getType() == type) {
                    found = set2;
                }
            }
            if (found == null) {
                return null;
            }
            if (found.expired()) {
                removeElement(name, type);
                return null;
            } else if (found.compareCredibility(minCred) < 0) {
                return null;
            } else {
                return found;
            }
        } else {
            throw new IllegalArgumentException("oneElement(ANY)");
        }
    }

    private synchronized Element findElement(Name name, int type, int minCred) {
        Object types = exactName(name);
        if (types == null) {
            return null;
        }
        return oneElement(name, types, type, minCred);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005b, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void addElement(org.xbill.DNS.Name r7, org.xbill.DNS.Cache.Element r8) {
        /*
            r6 = this;
            monitor-enter(r6)
            org.xbill.DNS.Cache$CacheMap r0 = r6.data     // Catch:{ all -> 0x005c }
            java.lang.Object r0 = r0.get(r7)     // Catch:{ all -> 0x005c }
            if (r0 != 0) goto L_0x0010
            org.xbill.DNS.Cache$CacheMap r1 = r6.data     // Catch:{ all -> 0x005c }
            r1.put(r7, r8)     // Catch:{ all -> 0x005c }
            monitor-exit(r6)
            return
        L_0x0010:
            int r1 = r8.getType()     // Catch:{ all -> 0x005c }
            boolean r2 = r0 instanceof java.util.List     // Catch:{ all -> 0x005c }
            if (r2 == 0) goto L_0x003b
            r2 = r0
            java.util.List r2 = (java.util.List) r2     // Catch:{ all -> 0x005c }
            r3 = 0
        L_0x001c:
            int r4 = r2.size()     // Catch:{ all -> 0x005c }
            if (r3 >= r4) goto L_0x0036
            java.lang.Object r4 = r2.get(r3)     // Catch:{ all -> 0x005c }
            org.xbill.DNS.Cache$Element r4 = (org.xbill.DNS.Cache.Element) r4     // Catch:{ all -> 0x005c }
            int r5 = r4.getType()     // Catch:{ all -> 0x005c }
            if (r5 != r1) goto L_0x0033
            r2.set(r3, r8)     // Catch:{ all -> 0x005c }
            monitor-exit(r6)
            return
        L_0x0033:
            int r3 = r3 + 1
            goto L_0x001c
        L_0x0036:
            r2.add(r8)     // Catch:{ all -> 0x005c }
            goto L_0x005a
        L_0x003b:
            r2 = r0
            org.xbill.DNS.Cache$Element r2 = (org.xbill.DNS.Cache.Element) r2     // Catch:{ all -> 0x005c }
            int r3 = r2.getType()     // Catch:{ all -> 0x005c }
            if (r3 != r1) goto L_0x004a
            org.xbill.DNS.Cache$CacheMap r3 = r6.data     // Catch:{ all -> 0x005c }
            r3.put(r7, r8)     // Catch:{ all -> 0x005c }
            goto L_0x005a
        L_0x004a:
            java.util.LinkedList r3 = new java.util.LinkedList     // Catch:{ all -> 0x005c }
            r3.<init>()     // Catch:{ all -> 0x005c }
            r3.add(r2)     // Catch:{ all -> 0x005c }
            r3.add(r8)     // Catch:{ all -> 0x005c }
            org.xbill.DNS.Cache$CacheMap r4 = r6.data     // Catch:{ all -> 0x005c }
            r4.put(r7, r3)     // Catch:{ all -> 0x005c }
        L_0x005a:
            monitor-exit(r6)
            return
        L_0x005c:
            r7 = move-exception
            monitor-exit(r6)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Cache.addElement(org.xbill.DNS.Name, org.xbill.DNS.Cache$Element):void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004a, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void removeElement(org.xbill.DNS.Name r6, int r7) {
        /*
            r5 = this;
            monitor-enter(r5)
            org.xbill.DNS.Cache$CacheMap r0 = r5.data     // Catch:{ all -> 0x004b }
            java.lang.Object r0 = r0.get(r6)     // Catch:{ all -> 0x004b }
            if (r0 != 0) goto L_0x000b
            monitor-exit(r5)
            return
        L_0x000b:
            boolean r1 = r0 instanceof java.util.List     // Catch:{ all -> 0x004b }
            if (r1 == 0) goto L_0x0039
            r1 = r0
            java.util.List r1 = (java.util.List) r1     // Catch:{ all -> 0x004b }
            r2 = 0
        L_0x0013:
            int r3 = r1.size()     // Catch:{ all -> 0x004b }
            if (r2 >= r3) goto L_0x0038
            java.lang.Object r3 = r1.get(r2)     // Catch:{ all -> 0x004b }
            org.xbill.DNS.Cache$Element r3 = (org.xbill.DNS.Cache.Element) r3     // Catch:{ all -> 0x004b }
            int r4 = r3.getType()     // Catch:{ all -> 0x004b }
            if (r4 != r7) goto L_0x0035
            r1.remove(r2)     // Catch:{ all -> 0x004b }
            int r4 = r1.size()     // Catch:{ all -> 0x004b }
            if (r4 != 0) goto L_0x0033
            org.xbill.DNS.Cache$CacheMap r4 = r5.data     // Catch:{ all -> 0x004b }
            r4.remove(r6)     // Catch:{ all -> 0x004b }
        L_0x0033:
            monitor-exit(r5)
            return
        L_0x0035:
            int r2 = r2 + 1
            goto L_0x0013
        L_0x0038:
            goto L_0x0049
        L_0x0039:
            r1 = r0
            org.xbill.DNS.Cache$Element r1 = (org.xbill.DNS.Cache.Element) r1     // Catch:{ all -> 0x004b }
            int r2 = r1.getType()     // Catch:{ all -> 0x004b }
            if (r2 == r7) goto L_0x0044
            monitor-exit(r5)
            return
        L_0x0044:
            org.xbill.DNS.Cache$CacheMap r2 = r5.data     // Catch:{ all -> 0x004b }
            r2.remove(r6)     // Catch:{ all -> 0x004b }
        L_0x0049:
            monitor-exit(r5)
            return
        L_0x004b:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Cache.removeElement(org.xbill.DNS.Name, int):void");
    }

    public synchronized void clearCache() {
        this.data.clear();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void addRecord(org.xbill.DNS.Record r7, int r8, java.lang.Object r9) {
        /*
            r6 = this;
            monitor-enter(r6)
            org.xbill.DNS.Name r0 = r7.getName()     // Catch:{ all -> 0x0035 }
            int r1 = r7.getRRsetType()     // Catch:{ all -> 0x0035 }
            boolean r2 = org.xbill.DNS.Type.isRR(r1)     // Catch:{ all -> 0x0035 }
            if (r2 != 0) goto L_0x0011
            monitor-exit(r6)
            return
        L_0x0011:
            org.xbill.DNS.Cache$Element r2 = r6.findElement(r0, r1, r8)     // Catch:{ all -> 0x0035 }
            if (r2 != 0) goto L_0x0023
            org.xbill.DNS.Cache$CacheRRset r3 = new org.xbill.DNS.Cache$CacheRRset     // Catch:{ all -> 0x0035 }
            int r4 = r6.maxcache     // Catch:{ all -> 0x0035 }
            long r4 = (long) r4     // Catch:{ all -> 0x0035 }
            r3.<init>((org.xbill.DNS.Record) r7, (int) r8, (long) r4)     // Catch:{ all -> 0x0035 }
            r6.addRRset(r3, r8)     // Catch:{ all -> 0x0035 }
            goto L_0x0033
        L_0x0023:
            int r3 = r2.compareCredibility(r8)     // Catch:{ all -> 0x0035 }
            if (r3 != 0) goto L_0x0033
            boolean r3 = r2 instanceof org.xbill.DNS.Cache.CacheRRset     // Catch:{ all -> 0x0035 }
            if (r3 == 0) goto L_0x0033
            r3 = r2
            org.xbill.DNS.Cache$CacheRRset r3 = (org.xbill.DNS.Cache.CacheRRset) r3     // Catch:{ all -> 0x0035 }
            r3.addRR(r7)     // Catch:{ all -> 0x0035 }
        L_0x0033:
            monitor-exit(r6)
            return
        L_0x0035:
            r7 = move-exception
            monitor-exit(r6)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Cache.addRecord(org.xbill.DNS.Record, int, java.lang.Object):void");
    }

    public synchronized void addRRset(RRset rrset, int cred) {
        CacheRRset crrset;
        long ttl = rrset.getTTL();
        Name name = rrset.getName();
        int type = rrset.getType();
        Element element = findElement(name, type, 0);
        if (ttl != 0) {
            if (element != null && element.compareCredibility(cred) <= 0) {
                element = null;
            }
            if (element == null) {
                if (rrset instanceof CacheRRset) {
                    crrset = (CacheRRset) rrset;
                } else {
                    crrset = new CacheRRset(rrset, cred, (long) this.maxcache);
                }
                addElement(name, crrset);
            }
        } else if (element != null && element.compareCredibility(cred) <= 0) {
            removeElement(name, type);
        }
    }

    public synchronized void addNegative(Name name, int type, SOARecord soa, int cred) {
        long ttl = 0;
        if (soa != null) {
            ttl = soa.getTTL();
        }
        Element element = findElement(name, type, 0);
        if (ttl != 0) {
            if (element != null && element.compareCredibility(cred) <= 0) {
                element = null;
            }
            if (element == null) {
                addElement(name, new NegativeElement(name, type, soa, cred, (long) this.maxncache));
            }
        } else if (element != null && element.compareCredibility(cred) <= 0) {
            removeElement(name, type);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 18 */
    /* access modifiers changed from: protected */
    public synchronized SetResponse lookup(Name name, int type, int minCred) {
        Name tname;
        int i = type;
        int i2 = minCred;
        synchronized (this) {
            int labels = name.labels();
            int tlabels = labels;
            while (tlabels >= 1) {
                boolean isRoot = tlabels == 1;
                boolean isExact = tlabels == labels;
                if (isRoot) {
                    tname = Name.root;
                    Name name2 = name;
                } else if (isExact) {
                    tname = name;
                    Name name3 = name;
                } else {
                    tname = new Name(name, labels - tlabels);
                }
                Object types = this.data.get(tname);
                if (types != null) {
                    if (isExact && i == 255) {
                        SetResponse sr = new SetResponse(6);
                        Element[] elements = allElements(types);
                        int added = 0;
                        for (Element element : elements) {
                            if (element.expired()) {
                                removeElement(tname, element.getType());
                            } else if (element instanceof CacheRRset) {
                                if (element.compareCredibility(i2) >= 0) {
                                    sr.addRRset((CacheRRset) element);
                                    added++;
                                }
                            }
                        }
                        if (added > 0) {
                            return sr;
                        }
                    } else if (isExact) {
                        Element element2 = oneElement(tname, types, i, i2);
                        if (element2 != null && (element2 instanceof CacheRRset)) {
                            SetResponse sr2 = new SetResponse(6);
                            sr2.addRRset((CacheRRset) element2);
                            return sr2;
                        } else if (element2 != null) {
                            SetResponse sr3 = new SetResponse(2);
                            return sr3;
                        } else {
                            Element element3 = oneElement(tname, types, 5, i2);
                            if (element3 != null && (element3 instanceof CacheRRset)) {
                                SetResponse setResponse = new SetResponse(4, (CacheRRset) element3);
                                return setResponse;
                            }
                        }
                    } else {
                        Element element4 = oneElement(tname, types, 39, i2);
                        if (element4 != null && (element4 instanceof CacheRRset)) {
                            SetResponse setResponse2 = new SetResponse(5, (CacheRRset) element4);
                            return setResponse2;
                        }
                    }
                    Element element5 = oneElement(tname, types, 2, i2);
                    if (element5 != null && (element5 instanceof CacheRRset)) {
                        SetResponse setResponse3 = new SetResponse(3, (CacheRRset) element5);
                        return setResponse3;
                    } else if (!isExact) {
                        continue;
                    } else if (oneElement(tname, types, 0, i2) != null) {
                        SetResponse ofType = SetResponse.ofType(1);
                        return ofType;
                    }
                }
                tlabels--;
            }
            Name name4 = name;
            SetResponse ofType2 = SetResponse.ofType(0);
            return ofType2;
        }
    }

    public SetResponse lookupRecords(Name name, int type, int minCred) {
        return lookup(name, type, minCred);
    }

    private RRset[] findRecords(Name name, int type, int minCred) {
        SetResponse cr = lookupRecords(name, type, minCred);
        if (cr.isSuccessful()) {
            return cr.answers();
        }
        return null;
    }

    public RRset[] findRecords(Name name, int type) {
        return findRecords(name, type, 3);
    }

    public RRset[] findAnyRecords(Name name, int type) {
        return findRecords(name, type, 2);
    }

    private final int getCred(int section, boolean isAuth) {
        if (section == 1) {
            return isAuth ? 4 : 3;
        }
        if (section == 2) {
            return isAuth ? 4 : 3;
        }
        if (section == 3) {
            return 1;
        }
        throw new IllegalArgumentException("getCred: invalid section");
    }

    private static void markAdditional(RRset rrset, Set names) {
        if (rrset.first().getAdditionalName() != null) {
            Iterator it = rrset.rrs();
            while (it.hasNext()) {
                Name name = ((Record) it.next()).getAdditionalName();
                if (name != null) {
                    names.add(name);
                }
            }
        }
    }

    /* JADX WARNING: type inference failed for: r17v6, types: [org.xbill.DNS.Record] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.xbill.DNS.SetResponse addMessage(org.xbill.DNS.Message r25) {
        /*
            r24 = this;
            r1 = r24
            r2 = r25
            org.xbill.DNS.Header r0 = r25.getHeader()
            r3 = 5
            boolean r4 = r0.getFlag(r3)
            org.xbill.DNS.Record r5 = r25.getQuestion()
            org.xbill.DNS.Header r0 = r25.getHeader()
            int r6 = r0.getRcode()
            r0 = 0
            r7 = 0
            java.lang.String r8 = "verbosecache"
            boolean r8 = org.xbill.DNS.Options.check(r8)
            r9 = 3
            if (r6 == 0) goto L_0x0027
            if (r6 != r9) goto L_0x0029
        L_0x0027:
            if (r5 != 0) goto L_0x002b
        L_0x0029:
            r3 = 0
            return r3
        L_0x002b:
            org.xbill.DNS.Name r10 = r5.getName()
            int r11 = r5.getType()
            int r12 = r5.getDClass()
            r13 = r10
            java.util.HashSet r14 = new java.util.HashSet
            r14.<init>()
            r15 = 1
            org.xbill.DNS.RRset[] r9 = r2.getSectionRRsets(r15)
            r17 = 0
            r3 = r17
            r23 = r7
            r7 = r0
            r0 = r23
        L_0x004b:
            int r15 = r9.length
            r18 = r5
            if (r3 >= r15) goto L_0x0127
            r15 = r9[r3]
            int r15 = r15.getDClass()
            if (r15 == r12) goto L_0x0063
            r21 = r0
            r20 = r8
            r22 = r11
            r19 = r12
            r12 = 5
            goto L_0x0118
        L_0x0063:
            r15 = r9[r3]
            int r15 = r15.getType()
            r19 = r9[r3]
            org.xbill.DNS.Name r5 = r19.getName()
            r20 = r8
            r19 = r12
            r12 = 1
            int r8 = r1.getCred(r12, r4)
            if (r15 == r11) goto L_0x007e
            r12 = 255(0xff, float:3.57E-43)
            if (r11 != r12) goto L_0x00ae
        L_0x007e:
            boolean r12 = r5.equals(r13)
            if (r12 == 0) goto L_0x00ae
            r12 = r9[r3]
            r1.addRRset(r12, r8)
            r7 = 1
            if (r13 != r10) goto L_0x00a0
            if (r0 != 0) goto L_0x0098
            org.xbill.DNS.SetResponse r12 = new org.xbill.DNS.SetResponse
            r21 = r7
            r7 = 6
            r12.<init>(r7)
            r0 = r12
            goto L_0x009a
        L_0x0098:
            r21 = r7
        L_0x009a:
            r7 = r9[r3]
            r0.addRRset(r7)
            goto L_0x00a2
        L_0x00a0:
            r21 = r7
        L_0x00a2:
            r7 = r9[r3]
            markAdditional(r7, r14)
            r22 = r11
            r7 = r21
            r12 = 5
            goto L_0x011a
        L_0x00ae:
            r12 = 5
            if (r15 != r12) goto L_0x00df
            boolean r12 = r5.equals(r13)
            if (r12 == 0) goto L_0x00df
            r12 = r9[r3]
            r1.addRRset(r12, r8)
            if (r13 != r10) goto L_0x00cc
            org.xbill.DNS.SetResponse r12 = new org.xbill.DNS.SetResponse
            r21 = r0
            r0 = 4
            r22 = r11
            r11 = r9[r3]
            r12.<init>(r0, r11)
            r0 = r12
            goto L_0x00d0
        L_0x00cc:
            r21 = r0
            r22 = r11
        L_0x00d0:
            r11 = r9[r3]
            org.xbill.DNS.Record r11 = r11.first()
            org.xbill.DNS.CNAMERecord r11 = (org.xbill.DNS.CNAMERecord) r11
            org.xbill.DNS.Name r11 = r11.getTarget()
            r13 = r11
            r12 = 5
            goto L_0x011a
        L_0x00df:
            r21 = r0
            r22 = r11
            r0 = 39
            if (r15 != r0) goto L_0x0117
            boolean r0 = r13.subdomain(r5)
            if (r0 == 0) goto L_0x0117
            r0 = r9[r3]
            r1.addRRset(r0, r8)
            if (r13 != r10) goto L_0x00ff
            org.xbill.DNS.SetResponse r0 = new org.xbill.DNS.SetResponse
            r11 = r9[r3]
            r12 = 5
            r0.<init>(r12, r11)
            r21 = r0
            goto L_0x0100
        L_0x00ff:
            r12 = 5
        L_0x0100:
            r0 = r9[r3]
            org.xbill.DNS.Record r0 = r0.first()
            r11 = r0
            org.xbill.DNS.DNAMERecord r11 = (org.xbill.DNS.DNAMERecord) r11
            org.xbill.DNS.Name r0 = r13.fromDNAME(r11)     // Catch:{ NameTooLongException -> 0x0111 }
            r13 = r0
            r0 = r21
            goto L_0x011a
        L_0x0111:
            r0 = move-exception
            r12 = r0
            r0 = r12
            r0 = r21
            goto L_0x012f
        L_0x0117:
            r12 = 5
        L_0x0118:
            r0 = r21
        L_0x011a:
            int r3 = r3 + 1
            r5 = r18
            r12 = r19
            r8 = r20
            r11 = r22
            r15 = 1
            goto L_0x004b
        L_0x0127:
            r21 = r0
            r20 = r8
            r22 = r11
            r19 = r12
        L_0x012f:
            r3 = 2
            org.xbill.DNS.RRset[] r5 = r2.getSectionRRsets(r3)
            r8 = 0
            r11 = 0
            r12 = 0
        L_0x0137:
            int r15 = r5.length
            if (r12 >= r15) goto L_0x016e
            r15 = r5[r12]
            int r15 = r15.getType()
            r3 = 6
            if (r15 != r3) goto L_0x0152
            r15 = r5[r12]
            org.xbill.DNS.Name r15 = r15.getName()
            boolean r15 = r13.subdomain(r15)
            if (r15 == 0) goto L_0x0152
            r8 = r5[r12]
            goto L_0x016a
        L_0x0152:
            r15 = r5[r12]
            int r15 = r15.getType()
            r3 = 2
            if (r15 != r3) goto L_0x016a
            r3 = r5[r12]
            org.xbill.DNS.Name r3 = r3.getName()
            boolean r3 = r13.subdomain(r3)
            if (r3 == 0) goto L_0x016a
            r3 = r5[r12]
            r11 = r3
        L_0x016a:
            int r12 = r12 + 1
            r3 = 2
            goto L_0x0137
        L_0x016e:
            if (r7 != 0) goto L_0x01c1
            r3 = 3
            if (r6 != r3) goto L_0x0175
            r12 = 0
            goto L_0x0177
        L_0x0175:
            r12 = r22
        L_0x0177:
            if (r6 == r3) goto L_0x019a
            if (r8 != 0) goto L_0x019a
            if (r11 != 0) goto L_0x017e
            goto L_0x019a
        L_0x017e:
            r3 = 2
            int r3 = r1.getCred(r3, r4)
            r1.addRRset(r11, r3)
            markAdditional(r11, r14)
            if (r0 != 0) goto L_0x0197
            org.xbill.DNS.SetResponse r15 = new org.xbill.DNS.SetResponse
            r17 = r3
            r3 = 3
            r15.<init>(r3, r11)
            r0 = r15
            r3 = r17
            goto L_0x01c0
        L_0x0197:
            r17 = r3
            goto L_0x01c0
        L_0x019a:
            r3 = 2
            int r3 = r1.getCred(r3, r4)
            r15 = 0
            if (r8 == 0) goto L_0x01aa
            org.xbill.DNS.Record r17 = r8.first()
            r15 = r17
            org.xbill.DNS.SOARecord r15 = (org.xbill.DNS.SOARecord) r15
        L_0x01aa:
            r1.addNegative(r13, r12, r15, r3)
            if (r0 != 0) goto L_0x01bc
            r17 = r3
            r3 = 3
            if (r6 != r3) goto L_0x01b6
            r3 = 1
            goto L_0x01b7
        L_0x01b6:
            r3 = 2
        L_0x01b7:
            org.xbill.DNS.SetResponse r0 = org.xbill.DNS.SetResponse.ofType(r3)
            goto L_0x01be
        L_0x01bc:
            r17 = r3
        L_0x01be:
            r3 = r17
        L_0x01c0:
            goto L_0x01d0
        L_0x01c1:
            if (r6 != 0) goto L_0x01d0
            if (r11 == 0) goto L_0x01d0
            r3 = 2
            int r3 = r1.getCred(r3, r4)
            r1.addRRset(r11, r3)
            markAdditional(r11, r14)
        L_0x01d0:
            r3 = 3
            org.xbill.DNS.RRset[] r12 = r2.getSectionRRsets(r3)
            r3 = 0
        L_0x01d6:
            int r15 = r12.length
            if (r3 >= r15) goto L_0x0211
            r15 = r12[r3]
            int r15 = r15.getType()
            r2 = 1
            if (r15 == r2) goto L_0x01ed
            r2 = 28
            if (r15 == r2) goto L_0x01ed
            r2 = 38
            if (r15 == r2) goto L_0x01ed
            r16 = r5
            goto L_0x020a
        L_0x01ed:
            r2 = r12[r3]
            org.xbill.DNS.Name r2 = r2.getName()
            boolean r17 = r14.contains(r2)
            if (r17 != 0) goto L_0x01fc
            r16 = r5
            goto L_0x020a
        L_0x01fc:
            r17 = r2
            r16 = r5
            r2 = 3
            int r5 = r1.getCred(r2, r4)
            r2 = r12[r3]
            r1.addRRset(r2, r5)
        L_0x020a:
            int r3 = r3 + 1
            r2 = r25
            r5 = r16
            goto L_0x01d6
        L_0x0211:
            r16 = r5
            if (r20 == 0) goto L_0x022b
            java.io.PrintStream r2 = java.lang.System.out
            java.lang.StringBuffer r3 = new java.lang.StringBuffer
            r3.<init>()
            java.lang.String r5 = "addMessage: "
            r3.append(r5)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            r2.println(r3)
        L_0x022b:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Cache.addMessage(org.xbill.DNS.Message):org.xbill.DNS.SetResponse");
    }

    public void flushSet(Name name, int type) {
        removeElement(name, type);
    }

    public void flushName(Name name) {
        removeName(name);
    }

    public void setMaxNCache(int seconds) {
        this.maxncache = seconds;
    }

    public int getMaxNCache() {
        return this.maxncache;
    }

    public void setMaxCache(int seconds) {
        this.maxcache = seconds;
    }

    public int getMaxCache() {
        return this.maxcache;
    }

    public int getSize() {
        return this.data.size();
    }

    public int getMaxEntries() {
        return this.data.getMaxSize();
    }

    public void setMaxEntries(int entries) {
        this.data.setMaxSize(entries);
    }

    public int getDClass() {
        return this.dclass;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        synchronized (this) {
            for (Object allElements : this.data.values()) {
                Element[] elements = allElements(allElements);
                for (Element append : elements) {
                    sb.append(append);
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
