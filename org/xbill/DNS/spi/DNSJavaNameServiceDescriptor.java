package org.xbill.DNS.spi;

import java.lang.reflect.Proxy;
import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

public class DNSJavaNameServiceDescriptor implements NameServiceDescriptor {
    static /* synthetic */ Class class$sun$net$spi$nameservice$NameService;
    private static NameService nameService;

    static {
        Class cls = class$sun$net$spi$nameservice$NameService;
        if (cls == null) {
            cls = class$("sun.net.spi.nameservice.NameService");
            class$sun$net$spi$nameservice$NameService = cls;
        }
        ClassLoader loader = cls.getClassLoader();
        Class[] clsArr = new Class[1];
        Class cls2 = class$sun$net$spi$nameservice$NameService;
        if (cls2 == null) {
            cls2 = class$("sun.net.spi.nameservice.NameService");
            class$sun$net$spi$nameservice$NameService = cls2;
        }
        clsArr[0] = cls2;
        nameService = (NameService) Proxy.newProxyInstance(loader, clsArr, new DNSJavaNameService());
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError().initCause(e);
        }
    }

    public NameService createNameService() {
        return nameService;
    }

    public String getType() {
        return "dns";
    }

    public String getProviderName() {
        return "dnsjava";
    }
}
