package org.xbill.DNS.spi;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.TextParseException;

public class DNSJavaNameService implements InvocationHandler {
    static /* synthetic */ Class array$$B = null;
    static /* synthetic */ Class array$Ljava$net$InetAddress = null;
    private static final String domainProperty = "sun.net.spi.nameservice.domain";
    private static final String nsProperty = "sun.net.spi.nameservice.nameservers";
    private static final String v6Property = "java.net.preferIPv6Addresses";
    private boolean preferV6 = false;

    protected DNSJavaNameService() {
        String nameServers = System.getProperty(nsProperty);
        String domain = System.getProperty(domainProperty);
        String v6 = System.getProperty(v6Property);
        if (nameServers != null) {
            StringTokenizer st = new StringTokenizer(nameServers, ",");
            String[] servers = new String[st.countTokens()];
            int n = 0;
            while (st.hasMoreTokens()) {
                servers[n] = st.nextToken();
                n++;
            }
            try {
                Lookup.setDefaultResolver(new ExtendedResolver(servers));
            } catch (UnknownHostException e) {
                System.err.println("DNSJavaNameService: invalid sun.net.spi.nameservice.nameservers");
            }
        }
        if (domain != null) {
            try {
                Lookup.setDefaultSearchPath(new String[]{domain});
            } catch (TextParseException e2) {
                System.err.println("DNSJavaNameService: invalid sun.net.spi.nameservice.domain");
            }
        }
        if (v6 != null && v6.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            this.preferV6 = true;
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class cls;
        Class cls2;
        try {
            if (method.getName().equals("getHostByAddr")) {
                return getHostByAddr(args[0]);
            }
            if (method.getName().equals("lookupAllHostAddr")) {
                InetAddress[] addresses = lookupAllHostAddr(args[0]);
                Class returnType = method.getReturnType();
                if (array$Ljava$net$InetAddress == null) {
                    cls = class$("[Ljava.net.InetAddress;");
                    array$Ljava$net$InetAddress = cls;
                } else {
                    cls = array$Ljava$net$InetAddress;
                }
                if (returnType.equals(cls)) {
                    return addresses;
                }
                if (array$$B == null) {
                    cls2 = class$("[[B");
                    array$$B = cls2;
                } else {
                    cls2 = array$$B;
                }
                if (returnType.equals(cls2)) {
                    int naddrs = addresses.length;
                    byte[][] byteAddresses = new byte[naddrs][];
                    for (int i = 0; i < naddrs; i++) {
                        byteAddresses[i] = addresses[i].getAddress();
                    }
                    return byteAddresses;
                }
            }
            throw new IllegalArgumentException("Unknown function name or arguments.");
        } catch (Throwable e) {
            System.err.println("DNSJavaNameService: Unexpected error.");
            e.printStackTrace();
            throw e;
        }
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError().initCause(e);
        }
    }

    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        try {
            Name name = new Name(host);
            Record[] records = null;
            if (this.preferV6) {
                records = new Lookup(name, 28).run();
            }
            if (records == null) {
                records = new Lookup(name, 1).run();
            }
            if (records == null && !this.preferV6) {
                records = new Lookup(name, 28).run();
            }
            if (records != null) {
                InetAddress[] array = new InetAddress[records.length];
                for (int i = 0; i < records.length; i++) {
                    Record record = records[i];
                    if (records[i] instanceof ARecord) {
                        array[i] = ((ARecord) records[i]).getAddress();
                    } else {
                        array[i] = ((AAAARecord) records[i]).getAddress();
                    }
                }
                return array;
            }
            throw new UnknownHostException(host);
        } catch (TextParseException e) {
            throw new UnknownHostException(host);
        }
    }

    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        Record[] records = new Lookup(ReverseMap.fromAddress(InetAddress.getByAddress(addr)), 12).run();
        if (records != null) {
            return ((PTRRecord) records[0]).getTarget().toString();
        }
        throw new UnknownHostException();
    }
}
