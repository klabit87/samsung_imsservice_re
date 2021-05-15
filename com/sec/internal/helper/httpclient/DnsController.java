package com.sec.internal.helper.httpclient;

import android.net.Network;
import com.sec.internal.constants.Mno;
import com.sec.internal.log.IMSLog;
import com.squareup.okhttp.Dns;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.xbill.DNS.Message;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;

public class DnsController implements Dns {
    private static final int BUF_SIZE = 2048;
    private static final int DNS_PORT = 53;
    private static final String TAG = "DnsController";
    public static final int TYPE_A = 1;
    public static final int TYPE_AAAA = 2;
    public static final int TYPE_AAAA_PREF = 6;
    public static final int TYPE_A_PREF = 5;
    public static final int TYPE_NAPTR = 3;
    static List<InetAddress> mListBsf = new ArrayList();
    static List<InetAddress> mListNaf = new ArrayList();
    private static String mPreBsfname = "";
    private static String mPreNafname = "";
    int bsfRetryCounter;
    boolean isNaf;
    InetAddress mDnsAddress;
    List<InetAddress> mDnsAddresses;
    int mDnsType;
    Mno mMno;
    Network mNetwork;
    List<SRVRecord> mSrvRecord = new ArrayList();
    int retryCounter;

    public DnsController(int retryCounter2, int bsfRetryCounter2, Network net, List<InetAddress> dnsAddr, int type, boolean isNaf2, Mno mno) {
        this.retryCounter = retryCounter2;
        this.bsfRetryCounter = bsfRetryCounter2;
        this.mNetwork = net;
        this.mDnsAddresses = dnsAddr;
        this.isNaf = isNaf2;
        this.mDnsType = type;
        this.mMno = mno;
    }

    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        List<InetAddress> list = new ArrayList<>();
        IMSLog.d(TAG, "lookup: send DNS with hostname: " + hostname + ",mPreNafname:" + mPreNafname + ",mPreBsfname:" + mPreBsfname);
        if ((mListNaf.size() == 0 || !hostname.equals(mPreNafname)) && this.isNaf) {
            mListNaf.clear();
            sendDns(hostname);
            mPreNafname = hostname;
        } else if ((mListBsf.size() == 0 || !hostname.equals(mPreBsfname)) && !this.isNaf) {
            mListBsf.clear();
            sendDns(hostname);
            mPreBsfname = hostname;
        }
        if (this.isNaf && mListNaf.size() > 0) {
            return Collections.singletonList(mListNaf.get(this.retryCounter));
        }
        if (mListBsf.size() > 0) {
            return Collections.singletonList(mListBsf.get(this.bsfRetryCounter));
        }
        return list;
    }

    public int getRetryCounter() {
        return this.retryCounter;
    }

    public void setNaf(boolean naf) {
        this.isNaf = naf;
    }

    private void sendDns(String domain) {
        IMSLog.d(TAG, "Requst dns query with " + this.mDnsType);
        int i = this.mDnsType;
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    getNaptrRecord(domain);
                    return;
                } else if (i != 5) {
                    if (i != 6) {
                        return;
                    }
                }
            }
            getDnsManualAAAA(domain);
            return;
        }
        getDnsManualA(domain);
    }

    private void getNaptrRecord(String domain) {
        String query;
        Record[] naptrRecords = getDnsNAPTR(domain);
        if (naptrRecords == null || naptrRecords.length <= 0) {
            IMSLog.e(TAG, "sendDns: NAPTR is null");
            if (domain.startsWith("_http.")) {
                query = domain;
            } else {
                query = "_http._tcp." + domain;
            }
            Record[] srvRecords = getDnsSRV(query);
            if (srvRecords == null || srvRecords.length <= 0) {
                IMSLog.e(TAG, "sendDns: SRV direct error");
                getDnsA(domain);
                return;
            }
            sortSRV(srvRecords);
            for (SRVRecord i : this.mSrvRecord) {
                getDnsA(i.getTarget().toString());
            }
            return;
        }
        for (Record temp : naptrRecords) {
            if (temp != null && temp.getType() == 35) {
                NAPTRRecord nRecord = (NAPTRRecord) temp;
                if (nRecord.getService().equalsIgnoreCase("HTTP+D2T")) {
                    Record[] srvRecords2 = getDnsSRV(nRecord.getReplacement().toString());
                    if (srvRecords2 == null || srvRecords2.length <= 0) {
                        getDnsA(domain);
                    } else {
                        sortSRV(srvRecords2);
                        for (SRVRecord i2 : this.mSrvRecord) {
                            getDnsA(i2.getTarget().toString());
                        }
                    }
                }
            }
        }
    }

    private Record[] getDnsQuery(String domain, int type) throws IOException {
        String mDomain;
        String str = domain;
        Record[] result = null;
        DatagramSocket udpSocket = new DatagramSocket();
        if (str.endsWith(".")) {
            mDomain = str;
        } else {
            mDomain = str + ".";
        }
        try {
            try {
                Record question = Record.newRecord(Name.fromString(mDomain), type, 1);
                Message query = Message.newQuery(question);
                this.mNetwork.bindSocket(udpSocket);
                byte[] sendData = query.toWire();
                udpSocket.send(new DatagramPacket(sendData, sendData.length, this.mDnsAddress, 53));
                byte[] recvData = new byte[2048];
                DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
                udpSocket.setSoTimeout(1000);
                udpSocket.receive(recvPacket);
                Message answer = new Message(recvPacket.getData());
                int resultCode = answer.getRcode();
                StringBuilder sb = new StringBuilder();
                Record record = question;
                sb.append("result is ");
                sb.append(String.valueOf(resultCode));
                IMSLog.d(TAG, sb.toString());
                if (resultCode == 0) {
                    result = answer.getSectionArray(1);
                }
                udpSocket.close();
                return result;
            } catch (IOException e) {
                IMSLog.e(TAG, "DNS query timeout, try next type or IP");
                udpSocket.close();
                return null;
            } catch (NullPointerException e2) {
                ex = e2;
                try {
                    IMSLog.e(TAG, ex.getMessage());
                    udpSocket.close();
                    return null;
                } catch (Throwable th) {
                    e = th;
                    udpSocket.close();
                    throw e;
                }
            }
        } catch (IOException e3) {
            int i = type;
            IMSLog.e(TAG, "DNS query timeout, try next type or IP");
            udpSocket.close();
            return null;
        } catch (NullPointerException e4) {
            ex = e4;
            int i2 = type;
            IMSLog.e(TAG, ex.getMessage());
            udpSocket.close();
            return null;
        } catch (Throwable th2) {
            e = th2;
            int i3 = type;
            udpSocket.close();
            throw e;
        }
    }

    private Record[] getDnsNAPTR(String domain) {
        IMSLog.d(TAG, "getDnsNAPTR() called with: domain = [" + domain + "]");
        try {
            this.mDnsAddress = this.mDnsAddresses.get(0);
            return getDnsQuery(domain, 35);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Record[] getDnsSRV(String domain) {
        IMSLog.d(TAG, "getDnsSRV() called with: domain = [" + domain + "]");
        try {
            this.mDnsAddress = this.mDnsAddresses.get(0);
            return getDnsQuery(domain, 33);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Record[] getDnsManualAAAA(String domain) {
        IMSLog.d(TAG, "getDnsManualAAAA() called with: domain = [" + domain + "]");
        Record[] result = null;
        for (InetAddress dns : this.mDnsAddresses) {
            this.mDnsAddress = dns;
            result = getManualDnsQuery(domain, 28);
            if (result == null || result.length <= 0) {
                if (this.mDnsType == 6) {
                    result = getManualDnsQuery(domain, 1);
                    if (result != null && result.length > 0) {
                        break;
                    } else if (result == null) {
                        IMSLog.d(TAG, "AAAA and A type query failed,try next IP");
                    }
                }
            } else {
                break;
            }
        }
        return result;
    }

    private Record[] getDnsManualA(String domain) {
        IMSLog.d(TAG, "getDnsManualA() called with: domain = [" + domain + "]");
        Record[] result = null;
        for (InetAddress dns : this.mDnsAddresses) {
            this.mDnsAddress = dns;
            result = getManualDnsQuery(domain, 1);
            if (result == null || result.length <= 0) {
                if (this.mDnsType == 5) {
                    result = getManualDnsQuery(domain, 28);
                    if (result != null && result.length > 0) {
                        break;
                    } else if (result == null) {
                        IMSLog.d(TAG, "A and AAAA type query failed,try next IP");
                    }
                }
            } else {
                break;
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0032 A[Catch:{ IOException -> 0x009c }] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0097 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private org.xbill.DNS.Record[] getManualDnsQuery(java.lang.String r11, int r12) {
        /*
            r10 = this;
            java.lang.String r0 = "DnsController"
            r1 = 0
            org.xbill.DNS.Record[] r2 = r10.getDnsQuery(r11, r12)     // Catch:{ IOException -> 0x009c }
            r1 = r2
            if (r1 == 0) goto L_0x009b
            int r2 = r1.length     // Catch:{ IOException -> 0x009c }
            if (r2 <= 0) goto L_0x009b
            int r2 = r1.length     // Catch:{ IOException -> 0x009c }
            r3 = 0
        L_0x000f:
            if (r3 >= r2) goto L_0x009b
            r4 = r1[r3]     // Catch:{ IOException -> 0x009c }
            r5 = 0
            r6 = 1
            if (r12 != r6) goto L_0x0022
            r6 = r4
            org.xbill.DNS.ARecord r6 = (org.xbill.DNS.ARecord) r6     // Catch:{ IOException -> 0x009c }
            if (r6 == 0) goto L_0x0021
            java.net.InetAddress r7 = r6.getAddress()     // Catch:{ IOException -> 0x009c }
            r5 = r7
        L_0x0021:
            goto L_0x0030
        L_0x0022:
            r6 = 28
            if (r12 != r6) goto L_0x0021
            r6 = r4
            org.xbill.DNS.AAAARecord r6 = (org.xbill.DNS.AAAARecord) r6     // Catch:{ IOException -> 0x009c }
            if (r6 == 0) goto L_0x0030
            java.net.InetAddress r7 = r6.getAddress()     // Catch:{ IOException -> 0x009c }
            r5 = r7
        L_0x0030:
            if (r5 == 0) goto L_0x0097
            com.sec.internal.constants.Mno r6 = r10.mMno     // Catch:{ IOException -> 0x009c }
            boolean r6 = r6.isChn()     // Catch:{ IOException -> 0x009c }
            if (r6 == 0) goto L_0x004c
            java.lang.String r6 = r5.getHostAddress()     // Catch:{ IOException -> 0x009c }
            java.lang.String r7 = "::"
            boolean r6 = r6.startsWith(r7)     // Catch:{ IOException -> 0x009c }
            if (r6 == 0) goto L_0x004c
            java.lang.String r6 = "chn not supported IPv6 addr"
            com.sec.internal.log.IMSLog.d(r0, r6)     // Catch:{ IOException -> 0x009c }
            goto L_0x0097
        L_0x004c:
            boolean r6 = r10.isNaf     // Catch:{ IOException -> 0x009c }
            if (r6 == 0) goto L_0x0074
            r6 = 0
            java.util.List<java.net.InetAddress> r7 = mListNaf     // Catch:{ IOException -> 0x009c }
            java.util.Iterator r7 = r7.iterator()     // Catch:{ IOException -> 0x009c }
        L_0x0057:
            boolean r8 = r7.hasNext()     // Catch:{ IOException -> 0x009c }
            if (r8 == 0) goto L_0x006c
            java.lang.Object r8 = r7.next()     // Catch:{ IOException -> 0x009c }
            java.net.InetAddress r8 = (java.net.InetAddress) r8     // Catch:{ IOException -> 0x009c }
            boolean r9 = r8.equals(r5)     // Catch:{ IOException -> 0x009c }
            if (r9 == 0) goto L_0x006b
            r6 = 1
            goto L_0x006c
        L_0x006b:
            goto L_0x0057
        L_0x006c:
            if (r6 != 0) goto L_0x0073
            java.util.List<java.net.InetAddress> r7 = mListNaf     // Catch:{ IOException -> 0x009c }
            r7.add(r5)     // Catch:{ IOException -> 0x009c }
        L_0x0073:
            goto L_0x0097
        L_0x0074:
            r6 = 0
            java.util.List<java.net.InetAddress> r7 = mListBsf     // Catch:{ IOException -> 0x009c }
            java.util.Iterator r7 = r7.iterator()     // Catch:{ IOException -> 0x009c }
        L_0x007b:
            boolean r8 = r7.hasNext()     // Catch:{ IOException -> 0x009c }
            if (r8 == 0) goto L_0x0090
            java.lang.Object r8 = r7.next()     // Catch:{ IOException -> 0x009c }
            java.net.InetAddress r8 = (java.net.InetAddress) r8     // Catch:{ IOException -> 0x009c }
            boolean r9 = r8.equals(r5)     // Catch:{ IOException -> 0x009c }
            if (r9 == 0) goto L_0x008f
            r6 = 1
            goto L_0x0090
        L_0x008f:
            goto L_0x007b
        L_0x0090:
            if (r6 != 0) goto L_0x0097
            java.util.List<java.net.InetAddress> r7 = mListBsf     // Catch:{ IOException -> 0x009c }
            r7.add(r5)     // Catch:{ IOException -> 0x009c }
        L_0x0097:
            int r3 = r3 + 1
            goto L_0x000f
        L_0x009b:
            goto L_0x00a0
        L_0x009c:
            r2 = move-exception
            r2.printStackTrace()
        L_0x00a0:
            com.sec.internal.constants.Mno r2 = r10.mMno
            boolean r2 = r2.isChn()
            if (r2 == 0) goto L_0x00c7
            boolean r2 = r10.isNaf
            if (r2 == 0) goto L_0x00b4
            java.util.List<java.net.InetAddress> r2 = mListNaf
            int r2 = r2.size()
            if (r2 == 0) goto L_0x00c0
        L_0x00b4:
            boolean r2 = r10.isNaf
            if (r2 != 0) goto L_0x00c7
            java.util.List<java.net.InetAddress> r2 = mListBsf
            int r2 = r2.size()
            if (r2 != 0) goto L_0x00c7
        L_0x00c0:
            java.lang.String r2 = "chn find no valid addr, return null"
            com.sec.internal.log.IMSLog.d(r0, r2)
            r0 = 0
            return r0
        L_0x00c7:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.DnsController.getManualDnsQuery(java.lang.String, int):org.xbill.DNS.Record[]");
    }

    private void getDnsA(String domain) {
        try {
            InetAddress result = this.mNetwork.getByName(domain);
            IMSLog.d(TAG, "getDnsA: " + result);
            if (this.isNaf) {
                boolean contain = false;
                Iterator<InetAddress> it = mListNaf.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (it.next().equals(result)) {
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    mListNaf.add(result);
                }
                return;
            }
            boolean contain2 = false;
            Iterator<InetAddress> it2 = mListBsf.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                } else if (it2.next().equals(result)) {
                    contain2 = true;
                    break;
                }
            }
            if (!contain2) {
                mListBsf.add(result);
            }
        } catch (NullPointerException | UnknownHostException e) {
            IMSLog.e(TAG, "getDnsA: error with domain: " + domain);
        }
    }

    private void sortSRV(Record[] records) {
        this.mSrvRecord.clear();
        for (SRVRecord sRecord : records) {
            if (this.mSrvRecord.size() == 0) {
                IMSLog.d(TAG, "sortSRV: 1st Record");
                this.mSrvRecord.add(sRecord);
            } else {
                boolean record_add = false;
                for (int i = 0; i < this.mSrvRecord.size() && !record_add; i++) {
                    SRVRecord temp = this.mSrvRecord.get(i);
                    if (sRecord.getPriority() < temp.getPriority()) {
                        IMSLog.d(TAG, "sortSRV: Update SRV better, lower priority");
                        this.mSrvRecord.add(i, sRecord);
                        record_add = true;
                    } else if (sRecord.getWeight() > temp.getWeight()) {
                        IMSLog.d(TAG, "sortSRV: Update SRV better, higher weight");
                        this.mSrvRecord.add(i, sRecord);
                        record_add = true;
                    }
                }
                if (!record_add) {
                    this.mSrvRecord.add(sRecord);
                }
            }
        }
    }

    public static int getNafAddrSize() {
        return mListNaf.size();
    }

    public static int getBsfAddrSize() {
        return mListBsf.size();
    }

    public static void correctServerAddr(int retryCounter2, int bsfRetryCounter2) {
        if (retryCounter2 > 0 && retryCounter2 < mListNaf.size()) {
            mListNaf.remove(retryCounter2);
            mListNaf.add(0, mListNaf.get(retryCounter2));
        }
        if (bsfRetryCounter2 > 0 && bsfRetryCounter2 < mListBsf.size()) {
            mListBsf.remove(bsfRetryCounter2);
            mListBsf.add(0, mListBsf.get(bsfRetryCounter2));
        }
    }
}
