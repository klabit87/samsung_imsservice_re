package com.sec.internal.ims.config;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class RcsConfigurationHelper {
    private static final String LOG_TAG = "RcsConfigurationHelper";

    public static Boolean readBoolParam(Context context, String param) {
        return Boolean.valueOf("1".equals(readStringParam(context, param)));
    }

    public static Boolean readBoolParam(Context context, String param, Boolean def) {
        Boolean ret = def;
        String value = readStringParam(context, param);
        if (value != null) {
            return Boolean.valueOf("1".equals(value));
        }
        return ret;
    }

    public static Integer readIntParam(Context context, String param, Integer def) {
        Integer ret = def;
        String value = readStringParam(context, param);
        if (value == null) {
            return ret;
        }
        try {
            return Integer.valueOf(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ret;
        }
    }

    public static Long readLongParam(Context context, String param, Long def) {
        Long ret = def;
        String value = readStringParam(context, param);
        if (value == null) {
            return ret;
        }
        try {
            return Long.valueOf(Long.parseLong(value));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ret;
        }
    }

    public static ImsUri readImsUriParam(Context context, String param) {
        String value = readStringParam(context, param);
        if (!TextUtils.isEmpty(value)) {
            return ImsUri.parse(value);
        }
        return null;
    }

    public static List<ImsUri> readListImsUriParam(Context context, String param) {
        ImsUri uri;
        List<ImsUri> ret = new ArrayList<>();
        for (String value : readListStringParam(context, param)) {
            if (!TextUtils.isEmpty(value) && (uri = ImsUri.parse(value)) != null) {
                ret.add(uri);
            }
        }
        return ret;
    }

    public static String readStringParam(Context context, String param) {
        Map<String, String> readData = readParam(context, param);
        if (readData == null || readData.isEmpty()) {
            return null;
        }
        Iterator<Map.Entry<String, String>> it = readData.entrySet().iterator();
        if (it.hasNext()) {
            return it.next().getValue();
        }
        return null;
    }

    public static String readStringParam(Context context, String param, String def) {
        Map<String, String> readData = readParam(context, param);
        if (readData != null && !readData.isEmpty()) {
            Iterator<Map.Entry<String, String>> it = readData.entrySet().iterator();
            if (it.hasNext()) {
                return it.next().getValue();
            }
        }
        return def;
    }

    public static List<String> readListStringParam(Context context, String param) {
        List<String> ret = new ArrayList<>();
        Map<String, String> readData = readParam(context, param);
        if (readData != null && !readData.isEmpty()) {
            for (Map.Entry<String, String> entry : readData.entrySet()) {
                ret.add(entry.getValue());
            }
        }
        return ret;
    }

    private static Map<String, String> readParam(Context context, String param) {
        Uri uri = ConfigConstants.CONTENT_URI;
        Uri uri2 = getUriParamWithPhoneId(uri, "parameter/" + param);
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Cursor c = context.getContentResolver().query(uri2, (String[]) null, (String) null, (String[]) null, (String) null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    if (c.getColumnCount() != 1 || !"NODATA".equals(c.getColumnName(0))) {
                        for (int i = 0; i < c.getColumnCount(); i++) {
                            result.put(c.getColumnName(i), c.getString(i));
                        }
                    } else {
                        if (c != null) {
                            c.close();
                        }
                        return null;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (c != null) {
            c.close();
        }
        return result;
        throw th;
    }

    public static Boolean readBoolParamWithPath(Context context, String path) {
        return Boolean.valueOf("1".equals(readStringParamWithPath(context, path)));
    }

    public static String readStringParamWithPath(Context context, String path) {
        Map<String, String> readData = readParamWithPath(context, path);
        if (readData == null || readData.isEmpty()) {
            return null;
        }
        Iterator<Map.Entry<String, String>> it = readData.entrySet().iterator();
        if (it.hasNext()) {
            return it.next().getValue();
        }
        return null;
    }

    private static Map<String, String> readParamWithPath(Context context, String path) {
        Uri uri = getUriParamWithPhoneId(ConfigConstants.CONTENT_URI, path);
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Cursor c = context.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    if (c.getColumnCount() != 1 || !"NODATA".equals(c.getColumnName(0))) {
                        for (int i = 0; i < c.getColumnCount(); i++) {
                            result.put(c.getColumnName(i), c.getString(i));
                        }
                    } else {
                        if (c != null) {
                            c.close();
                        }
                        return null;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (c != null) {
            c.close();
        }
        return result;
        throw th;
    }

    public static String getUuid(Context context, int phoneId) {
        ConfigData configData = getConfigData(context, "root/application/*", phoneId);
        if (configData != null) {
            return configData.readString("uuid_Value", "");
        }
        IMSLog.i(LOG_TAG, "getUuid: configData is not found");
        return "";
    }

    public static List<String> getRcsEnabledServiceList(Context context, int phoneId, String rcsProfile) {
        List<String> svcList = new ArrayList<>();
        ConfigData configData = getConfigData(context, "root/application/*", phoneId);
        if (configData == null) {
            IMSLog.i(LOG_TAG, "getRcsEnabledServiceList: configData is not found");
            return svcList;
        }
        if (configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, 0).intValue() != 2) {
            svcList.add("options");
            svcList.add("presence");
        }
        updateImEnabledServices(phoneId, configData, svcList, rcsProfile);
        if (configData.readBool(ConfigConstants.ConfigTable.SERVICES_IS_AUTH, false).booleanValue()) {
            svcList.add("is");
        }
        if (configData.readInt(ConfigConstants.ConfigTable.SERVICES_VS_AUTH, 0).intValue() != 0) {
            svcList.add("vs");
        }
        if (!TextUtils.isEmpty(configData.readString(ConfigConstants.ConfigTable.EXT_END_USER_CONF_REQID, ""))) {
            svcList.add("euc");
        }
        if (configData.readBool(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, false).booleanValue()) {
            svcList.add("gls");
        }
        updateComposerEnabledServices(phoneId, configData, svcList);
        svcList.add("profile");
        svcList.add("plug-in");
        if (configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE, false).booleanValue()) {
            svcList.add("lastseen");
        }
        IMSLog.i(LOG_TAG, phoneId, "getRcsEnabledServiceList: svcList = " + svcList);
        return svcList;
    }

    private static void updateImEnabledServices(int phoneId, ConfigData configData, List<String> svcList, String rcsProfile) {
        if (configData.readBool(ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH, false).booleanValue()) {
            svcList.add("im");
        }
        boolean ftAuth = configData.readBool(ConfigConstants.ConfigTable.SERVICES_FT_AUTH, false).booleanValue();
        String ftHttpCsUri = configData.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI, "");
        ImConstants.FtMech ftDefaultMech = getFtDefaultTech(configData, rcsProfile, phoneId);
        if (!ImsProfile.isRcsUp2Profile(rcsProfile)) {
            if (ftAuth) {
                svcList.add("ft");
            }
            if (!TextUtils.isEmpty(ftHttpCsUri) && ftDefaultMech == ImConstants.FtMech.HTTP) {
                svcList.add("ft_http");
            }
        } else if (ftAuth && !TextUtils.isEmpty(ftHttpCsUri)) {
            svcList.add("ft_http");
        }
        if (configData.readInt(ConfigConstants.ConfigTable.SERVICES_SLM_AUTH, 0).intValue() != 0) {
            svcList.add("slm");
        }
        if (configData.readInt(ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH, 1).intValue() != 0) {
            svcList.add(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
        }
    }

    private static void updateComposerEnabledServices(int phoneId, ConfigData configData, List<String> svcList) {
        int val = configData.readInt(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, 0).intValue();
        IMSLog.i(LOG_TAG, phoneId, "updateComposerEnabledServices: composer auth = " + val);
        if (val == 1 || val == 3 || configData.readBool(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, false).booleanValue() || configData.readBool(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, false).booleanValue() || configData.readBool(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, false).booleanValue()) {
            svcList.add("ec");
        }
    }

    public static ConfigData getConfigData(Context context, String path, int phoneId) {
        return new ConfigData(context, ImsUtil.getPathWithPhoneId(path, phoneId));
    }

    protected static Uri getUriParamWithPhoneId(Uri configUri, String param) {
        int simSlot;
        if (param == null) {
            return null;
        }
        if (param.contains("#simslot0")) {
            simSlot = 0;
        } else if (param.contains("#simslot1")) {
            simSlot = 1;
        } else {
            simSlot = SimUtil.getSimSlotPriority();
        }
        Uri.Builder buildUpon = Uri.parse(configUri + param.replaceAll("#simslot\\d", "")).buildUpon();
        return buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + simSlot).build();
    }

    public static class ConfigData {
        Map<String, String> mDataMap = new TreeMap();

        public ConfigData(Map dataMap) {
            this.mDataMap = dataMap;
        }

        public ConfigData(Context context, String path) {
            Uri uri = RcsConfigurationHelper.getUriParamWithPhoneId(ConfigConstants.CONTENT_URI, path);
            Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Cursor c = context.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        if (c.getColumnCount() != 1 || !"NODATA".equals(c.getColumnName(0))) {
                            for (int i = 0; i < c.getColumnCount(); i++) {
                                result.put(c.getColumnName(i), c.getString(i));
                            }
                        } else if (c != null) {
                            c.close();
                            return;
                        } else {
                            return;
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (c != null) {
                c.close();
            }
            this.mDataMap.putAll(result);
            return;
            throw th;
        }

        public Boolean readBool(String param, Boolean def) {
            Boolean ret = def;
            String value = readFromMap(param);
            if (!TextUtils.isEmpty(value)) {
                return Boolean.valueOf("1".equals(value));
            }
            return ret;
        }

        public Integer readInt(String param, Integer def) {
            Integer ret = def;
            String value = readFromMap(param);
            if (TextUtils.isEmpty(value)) {
                return ret;
            }
            try {
                return Integer.valueOf(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return ret;
            }
        }

        public Integer readIntWithinRange(String param, Integer def, Integer min, Integer max) {
            Integer ret = readInt(param, def);
            if (ret.intValue() < min.intValue() || ret.intValue() > max.intValue()) {
                return def;
            }
            return ret;
        }

        public Long readLong(String param, Long def) {
            Long ret = def;
            String value = readFromMap(param);
            if (TextUtils.isEmpty(value)) {
                return ret;
            }
            try {
                return Long.valueOf(Long.parseLong(value));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return ret;
            }
        }

        public String readString(String param, String def) {
            String value = readFromMap(param);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
            return def;
        }

        public List<String> readListString(String param) {
            return readListFromMap(param);
        }

        public Uri readUri(String param, Uri def) {
            String value = readFromMap(param);
            if (!TextUtils.isEmpty(value)) {
                return Uri.parse(value);
            }
            return def;
        }

        public ImsUri readImsUri(String param, ImsUri def) {
            String value = readFromMap(param);
            if (!TextUtils.isEmpty(value)) {
                return ImsUri.parse(value);
            }
            return def;
        }

        private String readFromMap(String param) {
            IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + param);
            Map<String, String> map = this.mDataMap;
            if (map == null || map.isEmpty() || param == null) {
                IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: cannot read the param");
                return "";
            }
            String path = ConfigContract.PATH_TABLE.get(param.toLowerCase(Locale.US));
            IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: path: " + path);
            if (path == null) {
                IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: path is null");
                return "";
            }
            String ret = this.mDataMap.get(path);
            if (ret == null || ret.isEmpty()) {
                String ret2 = RcsConfigurationHelper.readFromSecondMap(path, param.toLowerCase(Locale.US), this.mDataMap);
                IMSLog.s(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + param + " value: " + ret2);
                if (path.equalsIgnoreCase("root/application/0/ext/uuid_Value")) {
                    IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: " + param + "'s value is null, trying to get from old path");
                    ret2 = this.mDataMap.get("root/application/1/other/uuid_Value".toLowerCase(Locale.US));
                }
                if (ret2 != null && !ret2.isEmpty()) {
                    IMSLog.s(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + param + " value: " + ret2);
                    return ret2;
                } else if (!path.equalsIgnoreCase("root/application/0/ext/uuid_Value")) {
                    return ret2;
                } else {
                    IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: " + param + "'s value is null, trying to get from UP20 path");
                    return this.mDataMap.get("root/application/0/3gpp_ims/ext/gsma/uuid_Value".toLowerCase(Locale.US));
                }
            } else {
                IMSLog.s(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + param + " value: " + ret);
                return ret;
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: java.util.ArrayList} */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private java.util.List<java.lang.String> readListFromMap(java.lang.String r10) {
            /*
                r9 = this;
                java.util.ArrayList r0 = new java.util.ArrayList
                r0.<init>()
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "readListFromMap: param: "
                r1.append(r2)
                r1.append(r10)
                java.lang.String r1 = r1.toString()
                java.lang.String r3 = "RcsConfigurationHelper"
                com.sec.internal.log.IMSLog.i(r3, r1)
                java.util.Map<java.lang.String, java.lang.String> r1 = r9.mDataMap
                if (r1 == 0) goto L_0x00ff
                boolean r1 = r1.isEmpty()
                if (r1 != 0) goto L_0x00ff
                if (r10 != 0) goto L_0x002a
                goto L_0x00ff
            L_0x002a:
                java.lang.String r1 = "capdiscoveryallowedprefixes"
                boolean r1 = r1.equalsIgnoreCase(r10)
                if (r1 == 0) goto L_0x003f
                java.lang.String r1 = "root/application/1/capdiscovery/capdiscoverywhitelist/capdiscoveryallowedprefixes/prefix"
                java.util.Map<java.lang.String, java.lang.String> r2 = r9.mDataMap
                java.lang.String r3 = "root/application/1/capdiscovery/capdiscoverywhitelist/capdiscoveryallowedprefixes/prefix"
                java.util.List r0 = com.sec.internal.ims.config.RcsConfigurationHelper.getCapAllowedPrefixes(r3, r2)
                return r0
            L_0x003f:
                java.lang.String r1 = "public_user_identity"
                boolean r1 = r1.equalsIgnoreCase(r10)
                if (r1 == 0) goto L_0x0083
                java.lang.String r1 = "root/application/0/public_user_identity_list/"
                java.util.Map<java.lang.String, java.lang.String> r2 = r9.mDataMap
                java.lang.String r3 = "root/application/0/public_user_identity_list/"
                java.util.List r0 = com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(r3, r10, r2)
                java.lang.String r2 = "root/application/0/public_user_identity_list/0/public_user_identities/"
                if (r0 == 0) goto L_0x0061
                boolean r3 = r0.isEmpty()
                if (r3 != 0) goto L_0x0061
                r3 = r0
                goto L_0x006a
            L_0x0061:
                java.util.Map<java.lang.String, java.lang.String> r3 = r9.mDataMap
                java.lang.String r4 = "root/application/0/public_user_identity_list/0/public_user_identities/"
                java.util.List r3 = com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(r4, r10, r3)
            L_0x006a:
                r0 = r3
                java.lang.String r3 = "root/application/0/3gpp_ims/public_user_identity_list/"
                if (r0 == 0) goto L_0x0078
                boolean r4 = r0.isEmpty()
                if (r4 != 0) goto L_0x0078
                r4 = r0
                goto L_0x0081
            L_0x0078:
                java.util.Map<java.lang.String, java.lang.String> r4 = r9.mDataMap
                java.lang.String r5 = "root/application/0/3gpp_ims/public_user_identity_list/"
                java.util.List r4 = com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(r5, r10, r4)
            L_0x0081:
                r0 = r4
                return r0
            L_0x0083:
                java.lang.String r1 = "address"
                boolean r1 = r1.equalsIgnoreCase(r10)
                if (r1 != 0) goto L_0x0093
                java.lang.String r1 = "addresstype"
                boolean r1 = r1.equalsIgnoreCase(r10)
                if (r1 == 0) goto L_0x00e4
            L_0x0093:
                java.lang.String r1 = "root/application/0/lbo_p-cscf_address/"
                java.util.Map<java.lang.String, java.lang.String> r4 = r9.mDataMap
                java.lang.String r5 = "root/application/0/lbo_p-cscf_address/"
                java.util.List r0 = com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(r5, r10, r4)
                java.lang.String r4 = "root/application/0/lbo_p-cscf_address/0/lbo_p-cscf_addresses/"
                if (r0 == 0) goto L_0x00ac
                boolean r5 = r0.isEmpty()
                if (r5 != 0) goto L_0x00ac
                r5 = r0
                goto L_0x00b5
            L_0x00ac:
                java.util.Map<java.lang.String, java.lang.String> r5 = r9.mDataMap
                java.lang.String r6 = "root/application/0/lbo_p-cscf_address/0/lbo_p-cscf_addresses/"
                java.util.List r5 = com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(r6, r10, r5)
            L_0x00b5:
                r0 = r5
                java.lang.String r5 = "root/application/0/3gpp_ims/lbo_p-cscf_address/"
                if (r0 == 0) goto L_0x00c3
                boolean r6 = r0.isEmpty()
                if (r6 != 0) goto L_0x00c3
                r6 = r0
                goto L_0x00cc
            L_0x00c3:
                java.util.Map<java.lang.String, java.lang.String> r6 = r9.mDataMap
                java.lang.String r7 = "root/application/0/3gpp_ims/lbo_p-cscf_address/"
                java.util.List r6 = com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(r7, r10, r6)
            L_0x00cc:
                r0 = r6
                java.lang.String r6 = "root/application/0/3gpp_ims/lbo_p-cscf_addresses/"
                if (r0 == 0) goto L_0x00da
                boolean r7 = r0.isEmpty()
                if (r7 != 0) goto L_0x00da
                r7 = r0
                goto L_0x00e3
            L_0x00da:
                java.util.Map<java.lang.String, java.lang.String> r7 = r9.mDataMap
                java.lang.String r8 = "root/application/0/3gpp_ims/lbo_p-cscf_addresses/"
                java.util.List r7 = com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(r8, r10, r7)
            L_0x00e3:
                r0 = r7
            L_0x00e4:
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                r1.append(r2)
                r1.append(r10)
                java.lang.String r2 = " value: "
                r1.append(r2)
                r1.append(r0)
                java.lang.String r1 = r1.toString()
                com.sec.internal.log.IMSLog.s(r3, r1)
                return r0
            L_0x00ff:
                java.lang.String r1 = "readListFromMap: cannot read the param"
                com.sec.internal.log.IMSLog.i(r3, r1)
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.RcsConfigurationHelper.ConfigData.readListFromMap(java.lang.String):java.util.List");
        }
    }

    public static String getUserName(Context context, int phoneId) {
        String userName = readStringParam(context, ImsUtil.getPathWithPhoneId("UserName", phoneId), "");
        IMSLog.s(LOG_TAG, "userName: " + userName);
        return userName;
    }

    public static String getImpu(Context context, int phoneId) {
        List<ImsUri> puiList = readListImsUriParam(context, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY, phoneId));
        for (ImsUri uri : puiList) {
            if (uri.getUriType() == ImsUri.UriType.SIP_URI) {
                return uri.toString();
            }
        }
        if (puiList.size() <= 0) {
            return null;
        }
        StringBuilder sr = new StringBuilder();
        sr.append("sip:");
        sr.append(puiList.get(0).getMsisdn());
        sr.append("@");
        sr.append(readStringParam(context, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME, phoneId), (String) null));
        IMSLog.s(LOG_TAG, "getImpuFromProfile::ConvertingTELtoSIP: " + sr.toString());
        return sr.toString();
    }

    public static ImsUri.UriType getNetworkUriType(Context context, String remoteUriType, boolean autoconfig, int phoneId) {
        ImsUri.UriType uriType = ImsUri.UriType.TEL_URI;
        int rcsUriFmt = readIntParam(context, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_INT_URL_FORMAT, phoneId), -1).intValue();
        IMSLog.i(LOG_TAG, phoneId, "getNetworkUriType: rcsUriFmt[" + rcsUriFmt + "]");
        if (autoconfig && rcsUriFmt >= 0) {
            return rcsUriFmt == 1 ? ImsUri.UriType.SIP_URI : ImsUri.UriType.TEL_URI;
        } else if ("sip".equalsIgnoreCase(remoteUriType)) {
            return ImsUri.UriType.SIP_URI;
        } else {
            return uriType;
        }
    }

    public static String readFromSecondMap(String path, String param, Map<String, String> dataMap) {
        String readData;
        String readData2;
        IMSLog.i(LOG_TAG, "readFromSecondMap: param: " + param + " path: " + path);
        if (isRootAppUp20Param(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/0/3gpp_ims/");
            return dataMap.get(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_UP20_PATH + param);
        } else if (ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH.equalsIgnoreCase(path)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/0/3gpp_ims/ext/gsma/");
            return dataMap.get("root/application/0/3gpp_ims/ext/gsma/" + param);
        } else if (ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH.equalsIgnoreCase(path)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/0/3gpp_ims/ext/gsma/");
            return dataMap.get("root/application/0/3gpp_ims/ext/gsma/" + param);
        } else if (isJoynParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/capdiscovery/ext/joyn/");
            return dataMap.get(ConfigConstants.ConfigPath.CAPDISCOVERY_EXT_JOYN_PATH + param);
        } else if (ConfigConstants.ConfigTable.PRESENCE_LOC_INFO_MAX_VALID_TIME.equalsIgnoreCase(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/presence/location/");
            return dataMap.get(ConfigConstants.ConfigPath.PRESENCE_LOCATION_PATH + param);
        } else if (isImFtExtParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/im/ext/");
            return dataMap.get(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH + param);
        } else if (isChatParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/chat/");
            return dataMap.get(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + param);
        } else if (isFiletransferParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/filetransfer/");
            return dataMap.get(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + param);
        } else if (ConfigConstants.ConfigTable.IM_EXPLODER_URI.equalsIgnoreCase(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/standalonemsg/");
            return dataMap.get(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + param);
        } else if (isStandaloneMsgUp20Param(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/standalonemsg/");
            return dataMap.get(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + param);
        } else if (isCpmParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/cpm/");
            return dataMap.get(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH + param);
        } else if (isMessageStoreParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/messagestore/");
            return dataMap.get(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH + param);
        } else if (ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH.equalsIgnoreCase(path)) {
            String readData3 = dataMap.get(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP10_PATH + param);
            if (readData3 == null || readData3.isEmpty()) {
                readData2 = dataMap.get(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP20_PATH + param);
            } else {
                readData2 = readData3;
            }
            return readData2;
        } else if (isJoynUxParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/serviceproviderext/joyn/ux/");
            return dataMap.get(ConfigConstants.ConfigPath.JOYN_UX_PATH + param);
        } else if (isClientControlJoynMessagingParam(param)) {
            String readData4 = dataMap.get(ConfigConstants.ConfigPath.JOYN_MESSAGING_CHARACTERISTIC_PATH + param);
            if (readData4 == null || readData4.isEmpty()) {
                readData = dataMap.get(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + param);
            } else {
                readData = readData4;
            }
            return readData;
        } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY.equalsIgnoreCase(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/capdiscovery/");
            return dataMap.get(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH + param);
        } else if (isClientControlMessagingParam(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/");
            return dataMap.get(ConfigConstants.ConfigPath.MESSAGING_CHARACTERISTIC_PATH + param);
        } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(param)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/filetransfer/");
            return dataMap.get(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + param);
        } else if (!isEnrichedCallingParam(param)) {
            return null;
        } else {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/4/");
            return dataMap.get(ConfigConstants.ConfigPath.ENRICHED_CALLING_CHARACTERISTIC_PATH + param);
        }
    }

    private static boolean isRootAppUp20Param(String param) {
        if (ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isJoynParam(String param) {
        if (ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isImFtExtParam(String param) {
        if (ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isChatParam(String param) {
        if (ConfigConstants.ConfigTable.IM_AUT_ACCEPT.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_CONF_FCTY_URI.equalsIgnoreCase(param) || "max_adhoc_group_size".equalsIgnoreCase(param) || "MaxSize".equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_TIMER_IDLE.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isFiletransferParam(String param) {
        if (ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_WARN_SIZE.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_DEFAULT_MECH.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isStandaloneMsgUp20Param(String param) {
        if (ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE.equalsIgnoreCase(param) || "MaxSize".equalsIgnoreCase(param) || ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isCpmParam(String param) {
        if (ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_NAME.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_PWD.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isMessageStoreParam(String param) {
        if ("EventRpting".equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_AUTH_ARCHIVE.equalsIgnoreCase(param) || "SMSStore".equalsIgnoreCase(param) || "MMSStore".equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isJoynUxParam(String param) {
        if (ConfigConstants.ConfigTable.UX_MESSAGING_UX.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isClientControlJoynMessagingParam(String param) {
        if (ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isClientControlMessagingParam(String param) {
        if (ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private static boolean isEnrichedCallingParam(String param) {
        if (ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.DATAOFF_CONTENT_SHARE.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.DATAOFF_PRE_AND_POST_CALL.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    public static List<String> getCapAllowedPrefixes(String path, Map<String, String> dataMap) {
        int index = 1;
        List<String> ret = new ArrayList<>();
        String readData = dataMap.get(path + Integer.toString(1));
        while (readData != null && !readData.isEmpty()) {
            ret.add(readData);
            index++;
            readData = dataMap.get(path + Integer.toString(index));
        }
        return ret;
    }

    /* JADX WARNING: type inference failed for: r9v0, types: [java.util.Map<java.lang.String, java.lang.String>, java.util.Map] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.List<java.lang.String> getPublicUserIdAndLboPcscfAddr(java.lang.String r7, java.lang.String r8, java.util.Map<java.lang.String, java.lang.String> r9) {
        /*
            java.lang.String r0 = ""
            java.lang.String r1 = "root/application/0/public_user_identity_list/"
            boolean r1 = r1.equalsIgnoreCase(r7)
            if (r1 != 0) goto L_0x002a
            java.lang.String r1 = "root/application/0/lbo_p-cscf_address/"
            boolean r1 = r1.equalsIgnoreCase(r7)
            if (r1 == 0) goto L_0x0015
            goto L_0x002a
        L_0x0015:
            java.lang.String r1 = "root/application/0/3gpp_ims/public_user_identity_list/"
            boolean r1 = r1.equalsIgnoreCase(r7)
            if (r1 != 0) goto L_0x0027
            java.lang.String r1 = "root/application/0/3gpp_ims/lbo_p-cscf_address/"
            boolean r1 = r1.equalsIgnoreCase(r7)
            if (r1 == 0) goto L_0x002c
        L_0x0027:
            java.lang.String r0 = "/node/"
            goto L_0x002c
        L_0x002a:
            java.lang.String r0 = "/"
        L_0x002c:
            java.lang.String r1 = ""
            boolean r2 = r1.equals(r0)
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            boolean r4 = r1.equals(r0)
            if (r4 == 0) goto L_0x005a
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r7)
            r4.append(r8)
            java.lang.String r5 = java.lang.Integer.toString(r2)
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            java.lang.Object r4 = r9.get(r4)
            java.lang.String r4 = (java.lang.String) r4
            goto L_0x0079
        L_0x005a:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r7)
            java.lang.String r5 = java.lang.Integer.toString(r2)
            r4.append(r5)
            r4.append(r0)
            r4.append(r8)
            java.lang.String r4 = r4.toString()
            java.lang.Object r4 = r9.get(r4)
            java.lang.String r4 = (java.lang.String) r4
        L_0x0079:
        L_0x007a:
            if (r4 == 0) goto L_0x00cb
            boolean r5 = r4.isEmpty()
            if (r5 != 0) goto L_0x00cb
            r3.add(r4)
            int r2 = r2 + 1
            boolean r5 = r1.equals(r0)
            if (r5 == 0) goto L_0x00aa
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r7)
            r5.append(r8)
            java.lang.String r6 = java.lang.Integer.toString(r2)
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            java.lang.Object r5 = r9.get(r5)
            java.lang.String r5 = (java.lang.String) r5
            goto L_0x00c9
        L_0x00aa:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r7)
            java.lang.String r6 = java.lang.Integer.toString(r2)
            r5.append(r6)
            r5.append(r0)
            r5.append(r8)
            java.lang.String r5 = r5.toString()
            java.lang.Object r5 = r9.get(r5)
            java.lang.String r5 = (java.lang.String) r5
        L_0x00c9:
            r4 = r5
            goto L_0x007a
        L_0x00cb:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.RcsConfigurationHelper.getPublicUserIdAndLboPcscfAddr(java.lang.String, java.lang.String, java.util.Map):java.util.List");
    }

    public static boolean isUp2NonTransitional(String rcsProfile, int phoneId) {
        return ImsProfile.isRcsUp2Profile(rcsProfile) && !ImsProfile.isRcsUpTransitionProfile(ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.UP_PROFILE, ""));
    }

    public static ImConstants.ImMsgTech getImMsgTech(ConfigData data, String rcsProfile, int phoneId) {
        if (isUp2NonTransitional(rcsProfile, phoneId)) {
            return ImConstants.ImMsgTech.CPM;
        }
        return data.readInt(ConfigConstants.ConfigTable.IM_IM_MSG_TECH, 0).intValue() == 0 ? ImConstants.ImMsgTech.SIMPLE_IM : ImConstants.ImMsgTech.CPM;
    }

    public static String getImMsgTech(Context context, String rcsProfile, int phoneId) {
        return getImMsgTech(getConfigData(context, "root/*", phoneId), rcsProfile, phoneId).toString();
    }

    public static ImConstants.FtMech getFtDefaultTech(ConfigData data, String rcsProfile, int phoneId) {
        if (isUp2NonTransitional(rcsProfile, phoneId)) {
            return ImConstants.FtMech.HTTP;
        }
        if (DiagnosisConstants.RCSM_KEY_MSRP.equals(data.readString(ConfigConstants.ConfigTable.IM_FT_DEFAULT_MECH, DiagnosisConstants.RCSM_KEY_MSRP))) {
            return ImConstants.FtMech.MSRP;
        }
        return ImConstants.FtMech.HTTP;
    }
}
