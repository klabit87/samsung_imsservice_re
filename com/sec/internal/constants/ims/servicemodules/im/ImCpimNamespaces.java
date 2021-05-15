package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ImCpimNamespaces {
    private final Map<String, CpimNamespace> mNamespaces = new HashMap();

    public static class CpimNamespace {
        protected Map<String, ArrayList<String>> mHeaders = new HashMap();
        protected String mName;
        protected String mUri;

        public CpimNamespace(String name, String uri) {
            this.mName = name;
            this.mUri = uri;
        }

        public void addHeader(String header, String value) {
            String h = header.toLowerCase(Locale.US);
            if (this.mHeaders.containsKey(h)) {
                this.mHeaders.get(h).add(value);
                return;
            }
            ArrayList<String> list = new ArrayList<>();
            list.add(value);
            this.mHeaders.put(h, list);
        }

        public String toString() {
            return "CpimNamespace [mName=" + this.mName + ", mUri=" + this.mUri + ", headers=" + this.mHeaders + "]";
        }
    }

    public void addNamespace(String name, String uri) {
        this.mNamespaces.put(name, new CpimNamespace(name, uri));
    }

    public CpimNamespace getNamespace(String name) {
        return this.mNamespaces.get(name);
    }

    public String getFirstHeaderValue(String namespace, String header) {
        String h = header.toLowerCase(Locale.US);
        if (!this.mNamespaces.containsKey(namespace)) {
            return null;
        }
        CpimNamespace ns = this.mNamespaces.get(namespace);
        if (ns.mHeaders.containsKey(h)) {
            return (String) ns.mHeaders.get(h).get(0);
        }
        return null;
    }

    public String toString() {
        return "ImCpimNamespaces [mNamespaces=" + IMSLog.checker(this.mNamespaces) + "]";
    }
}
