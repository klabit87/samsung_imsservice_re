package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.net.Uri;
import java.util.Map;

public class HistoryLogMember {
    private Map<String, String> mColumnMapping;
    private int mProviderId;
    private String mTable;
    private String mUri;

    public HistoryLogMember(int id, String uri, String table, Map<String, String> columnMapping) {
        this.mProviderId = id;
        this.mUri = uri;
        this.mTable = table;
        this.mColumnMapping = columnMapping;
    }

    public int getProviderId() {
        return this.mProviderId;
    }

    public Uri getUri() {
        if (!this.mUri.contains(this.mTable)) {
            this.mUri += "/" + this.mTable;
        }
        return Uri.parse(this.mUri);
    }

    public Map<String, String> getColumnMapping() {
        return this.mColumnMapping;
    }
}
