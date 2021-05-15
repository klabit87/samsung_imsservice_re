package com.sec.internal.ims.core.cmc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CmcInfo {
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACTIVATION = "activation";
    public static final String CALL_FORKING_ENABLED = "call_forking_enabled";
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_TYPE = "device_type";
    public static final String HAS_SD = "has_sd";
    public static final String LINE_ID = "line_id";
    public static final String LINE_IMPU = "line_impu";
    public static final String LINE_OWNER_DEVICE_ID = "line_owner_device_id";
    public static final String LINE_SLOT_INDEX = "line_slot_index";
    public static final String NETWORK_PREF = "network_pref";
    public static final String OOBE = "oobe";
    public static final String PCSCF_ADDR_LIST = "pcscf_addr_list";
    public static final String SAME_WIFI_NETWORK_ONLY = "same_wifi_network_only";
    public static final String SA_SERVER_URL = "sa_server_url";
    private static Map<String, DataType> mTypeMap;
    String mAccessToken;
    boolean mActivation;
    boolean mCallforkingEnabled;
    String mDeviceId;
    String mDeviceType;
    boolean mHasSd;
    boolean mIsSameWifiNetworkOnly;
    String mLineId;
    String mLineImpu;
    String mLineOwnerDeviceId;
    int mLineSlotIndex;
    int mNetworkPref;
    boolean mOobe;
    List<String> mPcscfAddrList;
    String mSaServerUrl;

    public enum DataType {
        BOOLEAN,
        INTEGER,
        STRING,
        LIST,
        NOT_DEFINED
    }

    static {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        mTypeMap = linkedHashMap;
        linkedHashMap.put(ACTIVATION, DataType.BOOLEAN);
        mTypeMap.put(LINE_SLOT_INDEX, DataType.INTEGER);
        mTypeMap.put("device_type", DataType.STRING);
        mTypeMap.put("device_id", DataType.STRING);
        mTypeMap.put("access_token", DataType.STRING);
        mTypeMap.put("line_id", DataType.STRING);
        mTypeMap.put(LINE_OWNER_DEVICE_ID, DataType.STRING);
        mTypeMap.put(LINE_IMPU, DataType.STRING);
        mTypeMap.put(SA_SERVER_URL, DataType.STRING);
        mTypeMap.put(PCSCF_ADDR_LIST, DataType.LIST);
        mTypeMap.put(CALL_FORKING_ENABLED, DataType.BOOLEAN);
        mTypeMap.put(HAS_SD, DataType.BOOLEAN);
        mTypeMap.put(NETWORK_PREF, DataType.INTEGER);
        mTypeMap.put(OOBE, DataType.BOOLEAN);
        mTypeMap.put(SAME_WIFI_NETWORK_ONLY, DataType.BOOLEAN);
    }

    public CmcInfo() {
        this.mOobe = false;
        this.mActivation = false;
        this.mLineSlotIndex = -1;
        this.mDeviceType = "";
        this.mDeviceId = "";
        this.mAccessToken = "";
        this.mLineId = "";
        this.mLineOwnerDeviceId = "";
        this.mLineImpu = "";
        this.mSaServerUrl = "";
        this.mPcscfAddrList = null;
        this.mCallforkingEnabled = true;
        this.mHasSd = true;
        this.mNetworkPref = 1;
        this.mIsSameWifiNetworkOnly = false;
        this.mPcscfAddrList = new ArrayList();
    }

    public static Set<String> getInfoNameSet() {
        return mTypeMap.keySet();
    }

    public static boolean isDumpPrintAvailable(String name) {
        if (!mTypeMap.containsKey(name)) {
            return false;
        }
        if (name.equals("device_type") || mTypeMap.get(name) == DataType.INTEGER || mTypeMap.get(name) == DataType.BOOLEAN) {
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CmcInfo cmcInfo = (CmcInfo) obj;
        if (this.mOobe != cmcInfo.mOobe || this.mActivation != cmcInfo.mActivation || this.mLineSlotIndex != cmcInfo.mLineSlotIndex || !this.mDeviceType.equals(cmcInfo.mDeviceType) || !this.mDeviceId.equals(cmcInfo.mDeviceId) || !this.mAccessToken.equals(cmcInfo.mAccessToken) || !this.mLineId.equals(cmcInfo.mLineId) || !this.mLineOwnerDeviceId.equals(cmcInfo.mLineOwnerDeviceId) || !this.mLineImpu.equals(cmcInfo.mLineImpu) || !this.mSaServerUrl.equals(cmcInfo.mSaServerUrl) || this.mHasSd != cmcInfo.mHasSd || this.mNetworkPref != cmcInfo.mNetworkPref || this.mCallforkingEnabled != cmcInfo.mCallforkingEnabled || this.mIsSameWifiNetworkOnly != cmcInfo.mIsSameWifiNetworkOnly) {
            return false;
        }
        List<String> newPcscfList = new ArrayList<>();
        newPcscfList.addAll(this.mPcscfAddrList);
        newPcscfList.removeAll(cmcInfo.mPcscfAddrList);
        if (this.mPcscfAddrList.size() != cmcInfo.mPcscfAddrList.size() || !newPcscfList.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean compareWithName(String name, CmcInfo targetInfo) {
        if (!mTypeMap.containsKey(name)) {
            return false;
        }
        Object src = getValueWithName(name);
        Object target = targetInfo.getValueWithName(name);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType[mTypeMap.get(name).ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        return false;
                    }
                    if (!(src == null || target == null)) {
                        List<Object> srcList = (List) src;
                        List<Object> targetList = (List) target;
                        if (srcList.size() == targetList.size() && srcList.containsAll(targetList) && targetList.containsAll(srcList)) {
                            return true;
                        }
                    }
                    if (src == null && target == null) {
                        return true;
                    }
                    return false;
                } else if (src != null && target != null) {
                    return ((String) src).equals((String) target);
                } else {
                    if (src == null && target == null) {
                        return true;
                    }
                    return false;
                }
            } else if (((Integer) src).intValue() == ((Integer) target).intValue()) {
                return true;
            } else {
                return false;
            }
        } else if (((Boolean) src).booleanValue() == ((Boolean) target).booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    /* renamed from: com.sec.internal.ims.core.cmc.CmcInfo$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType;

        static {
            int[] iArr = new int[DataType.values().length];
            $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType = iArr;
            try {
                iArr[DataType.BOOLEAN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType[DataType.INTEGER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType[DataType.STRING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType[DataType.LIST.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public boolean checkValidWithName(String name) {
        if (!mTypeMap.containsKey(name)) {
            return false;
        }
        Object value = getValueWithName(name);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType[mTypeMap.get(name).ordinal()];
        if (i == 3) {
            String strValue = (String) value;
            if (strValue == null || strValue.isEmpty()) {
                return false;
            }
            return true;
        } else if (i != 4) {
            return true;
        } else {
            List<Object> listValue = (List) value;
            if (listValue == null || listValue.size() <= 0) {
                return false;
            }
            return true;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Object getValueWithName(java.lang.String r2) {
        /*
            r1 = this;
            int r0 = r2.hashCode()
            switch(r0) {
                case -2110410035: goto L_0x00a5;
                case -1938933922: goto L_0x009b;
                case -1796389996: goto L_0x0090;
                case -1542869117: goto L_0x0086;
                case -1224433130: goto L_0x007b;
                case -521278354: goto L_0x006f;
                case -482757372: goto L_0x0063;
                case -176479184: goto L_0x0057;
                case -19583596: goto L_0x004c;
                case 3416611: goto L_0x0041;
                case 25209764: goto L_0x0036;
                case 176901446: goto L_0x002b;
                case 858710529: goto L_0x001f;
                case 1202939004: goto L_0x0014;
                case 2041217302: goto L_0x0009;
                default: goto L_0x0007;
            }
        L_0x0007:
            goto L_0x00af
        L_0x0009:
            java.lang.String r0 = "activation"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 1
            goto L_0x00b0
        L_0x0014:
            java.lang.String r0 = "line_slot_index"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 2
            goto L_0x00b0
        L_0x001f:
            java.lang.String r0 = "call_forking_enabled"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 11
            goto L_0x00b0
        L_0x002b:
            java.lang.String r0 = "line_id"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 6
            goto L_0x00b0
        L_0x0036:
            java.lang.String r0 = "device_id"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 4
            goto L_0x00b0
        L_0x0041:
            java.lang.String r0 = "oobe"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 0
            goto L_0x00b0
        L_0x004c:
            java.lang.String r0 = "network_pref"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 13
            goto L_0x00b0
        L_0x0057:
            java.lang.String r0 = "pcscf_addr_list"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 10
            goto L_0x00b0
        L_0x0063:
            java.lang.String r0 = "sa_server_url"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 9
            goto L_0x00b0
        L_0x006f:
            java.lang.String r0 = "same_wifi_network_only"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 14
            goto L_0x00b0
        L_0x007b:
            java.lang.String r0 = "has_sd"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 12
            goto L_0x00b0
        L_0x0086:
            java.lang.String r0 = "device_type"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 3
            goto L_0x00b0
        L_0x0090:
            java.lang.String r0 = "line_impu"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 8
            goto L_0x00b0
        L_0x009b:
            java.lang.String r0 = "access_token"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 5
            goto L_0x00b0
        L_0x00a5:
            java.lang.String r0 = "line_owner_device_id"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 7
            goto L_0x00b0
        L_0x00af:
            r0 = -1
        L_0x00b0:
            switch(r0) {
                case 0: goto L_0x00f7;
                case 1: goto L_0x00f0;
                case 2: goto L_0x00e9;
                case 3: goto L_0x00e6;
                case 4: goto L_0x00e3;
                case 5: goto L_0x00e0;
                case 6: goto L_0x00dd;
                case 7: goto L_0x00da;
                case 8: goto L_0x00d7;
                case 9: goto L_0x00d4;
                case 10: goto L_0x00d1;
                case 11: goto L_0x00ca;
                case 12: goto L_0x00c3;
                case 13: goto L_0x00bc;
                case 14: goto L_0x00b5;
                default: goto L_0x00b3;
            }
        L_0x00b3:
            r0 = 0
            return r0
        L_0x00b5:
            boolean r0 = r1.mIsSameWifiNetworkOnly
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)
            return r0
        L_0x00bc:
            int r0 = r1.mNetworkPref
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            return r0
        L_0x00c3:
            boolean r0 = r1.mHasSd
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)
            return r0
        L_0x00ca:
            boolean r0 = r1.mCallforkingEnabled
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)
            return r0
        L_0x00d1:
            java.util.List<java.lang.String> r0 = r1.mPcscfAddrList
            return r0
        L_0x00d4:
            java.lang.String r0 = r1.mSaServerUrl
            return r0
        L_0x00d7:
            java.lang.String r0 = r1.mLineImpu
            return r0
        L_0x00da:
            java.lang.String r0 = r1.mLineOwnerDeviceId
            return r0
        L_0x00dd:
            java.lang.String r0 = r1.mLineId
            return r0
        L_0x00e0:
            java.lang.String r0 = r1.mAccessToken
            return r0
        L_0x00e3:
            java.lang.String r0 = r1.mDeviceId
            return r0
        L_0x00e6:
            java.lang.String r0 = r1.mDeviceType
            return r0
        L_0x00e9:
            int r0 = r1.mLineSlotIndex
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            return r0
        L_0x00f0:
            boolean r0 = r1.mActivation
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)
            return r0
        L_0x00f7:
            boolean r0 = r1.mOobe
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.cmc.CmcInfo.getValueWithName(java.lang.String):java.lang.Object");
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        for (String name : mTypeMap.keySet()) {
            builder.append(name);
            builder.append(":");
            builder.append(getValueWithName(name));
            builder.append(", ");
        }
        if (builder.lastIndexOf(", ") != -1) {
            builder.delete(builder.lastIndexOf(", "), builder.length());
        }
        builder.append(">");
        return builder.toString();
    }
}
