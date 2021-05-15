package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.att.iqi.lib.BuildConfig;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.helper.DmConfigHelper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DmProfileLoader {
    public static final String LOG_TAG = "DmProfileLoader";
    protected static boolean mIsKorOp = false;
    protected static List<String> mLboPcscfAddrList = new CopyOnWriteArrayList();
    protected static int mLboPcscfPort = -1;
    protected static boolean mSmsOverIms = false;
    protected static ContentValues mValueList = new ContentValues();

    public static ImsProfile getProfile(Context context, ImsProfile profile, int phoneId) {
        return getFromDmStorage(context, profile, phoneId);
    }

    private static ImsProfile getFromDmStorage(Context context, ImsProfile base, int phoneId) {
        int portIndex;
        Context context2 = context;
        int i = phoneId;
        mLboPcscfAddrList = new CopyOnWriteArrayList();
        mLboPcscfPort = -1;
        mIsKorOp = Mno.fromName(base.getMnoName()).isKor();
        Log.e(LOG_TAG, "mIsKorOp: " + mIsKorOp);
        for (Map.Entry<String, String> entry : DmConfigHelper.read(context2, "omadm/*", i).entrySet()) {
            String value = entry.getValue();
            String uriPath = entry.getKey().replaceFirst(DmConfigModule.DM_PATH, "");
            setValueWithUri(uriPath, value);
            if (uriPath != null && !TextUtils.isEmpty(value) && uriPath.contains("./3GPP_IMS/LBO_P-CSCF_Address") && uriPath.endsWith("Address")) {
                if (value.indexOf(91) < 0 || value.indexOf(93) < 0) {
                    String address = value;
                    int portIndex2 = value.indexOf(58);
                    if (portIndex2 >= 0) {
                        mLboPcscfPort = Integer.parseInt(value.substring(portIndex2 + 1));
                        address = value.substring(0, portIndex2);
                    }
                    mLboPcscfAddrList.add(address);
                } else {
                    if (value.indexOf("]:") > 0 && (portIndex = value.indexOf("]:")) >= 0) {
                        mLboPcscfPort = Integer.parseInt(value.substring(portIndex + 2));
                    }
                    mLboPcscfAddrList.add(value.substring(value.indexOf(91) + 1, value.indexOf(93)));
                }
                if (mLboPcscfPort == -1) {
                    mLboPcscfPort = base.getSipPort();
                }
            }
        }
        if (mLboPcscfAddrList.isEmpty() && !base.getPcscfList().isEmpty()) {
            mLboPcscfAddrList.addAll(base.getPcscfList());
            mLboPcscfPort = base.getSipPort();
        }
        if (mIsKorOp) {
            mSmsOverIms = TextUtils.equals(NvConfiguration.get(context2, "sms_over_ip_network_indication", "", i), "1");
            Log.e(LOG_TAG, "mSmsOverIms: " + mSmsOverIms);
        } else {
            mSmsOverIms = NvConfiguration.getSmsIpNetworkIndi(context2, i);
        }
        ImsProfile profile = new ImsProfile(base);
        updateDbInfoToProfile(profile);
        return profile;
    }

    private static void updateDbInfoToProfile(ImsProfile profile) {
        Log.e(LOG_TAG, "updateDbInfoToProfile");
        if (getIntValue("12") > 0) {
            profile.setTimer1(getIntValue("12"));
        }
        if (getIntValue("13") > 0) {
            profile.setTimer2(getIntValue("13"));
        }
        if (getIntValue("14") > 0) {
            profile.setTimer4(getIntValue("14"));
        }
        if (getIntValue("15") > 0) {
            profile.setTimerA(getIntValue("15"));
        }
        if (getIntValue("16") > 0) {
            profile.setTimerB(getIntValue("16"));
        }
        if (getIntValue("17") > 0) {
            profile.setTimerC(getIntValue("17"));
        }
        if (getIntValue("18") > 0) {
            profile.setTimerD(getIntValue("18"));
        }
        if (getIntValue("19") > 0) {
            profile.setTimerE(getIntValue("19"));
        }
        if (getIntValue("20") > 0) {
            profile.setTimerF(getIntValue("20"));
        }
        if (getIntValue("21") > 0) {
            profile.setTimerG(getIntValue("21"));
        }
        if (getIntValue("22") > 0) {
            profile.setTimerH(getIntValue("22"));
        }
        if (getIntValue("23") > 0) {
            profile.setTimerI(getIntValue("23"));
        }
        if (getIntValue("24") > 0) {
            profile.setTimerJ(getIntValue("24"));
        }
        if (getIntValue("25") > 0) {
            profile.setTimerK(getIntValue("25"));
        }
        setInt(profile, "amrnboa_payload", getIntValue("66"));
        setInt(profile, "amrnbbe_payload", getIntValue("67"));
        setInt(profile, "amrwboa_payload", getIntValue("64"));
        setInt(profile, "amrwbbe_payload", getIntValue("65"));
        setInt(profile, "dtmf_nb_payload", getIntValue("71"));
        setInt(profile, "dtmf_wb_payload", getIntValue("70"));
        setInt(profile, "h264_qvga_payload", getIntValue("69"));
        setInt(profile, "h264_vga_payload", getIntValue("68"));
        setInt(profile, "h264_vgal_payload", getIntValue("108"));
        setInt(profile, "h263_qcif_payload", getIntValue("132"));
        setInt(profile, "audio_port_start", getIntValue("60"));
        setInt(profile, "audio_port_end", getIntValue("61"));
        setInt(profile, "video_port_start", getIntValue("62"));
        setInt(profile, "video_port_end", getIntValue("63"));
        boolean z = false;
        if (getStringValue("129") != null) {
            setInt(profile, "evs_payload", getIntValue("129"));
            profile.put("enable_evs_codec", Boolean.valueOf(getIntValue("129") > 0));
        }
        if (getStringValue("131") != null) {
            setString(profile, "evs_default_bitrate", getStringValue("131"));
        }
        if (getStringValue("130") != null) {
            setString(profile, "evs_default_bandwidth", getStringValue("130"));
        }
        profile.setSmsPsi(getStringValue("73"));
        profile.setLboPcscfAddressList(mLboPcscfAddrList);
        profile.setLboPcscfPort(mLboPcscfPort);
        setString(profile, "amrnb_mode", getStringValue("6"));
        setString(profile, "amrwb_mode", getStringValue(BuildConfig.VERSION_NAME));
        setInt(profile, "publish_timer", getIntValue("36"));
        setInt(profile, "extended_publish_timer", getIntValue("37"));
        setInt(profile, "cap_cache_exp", getIntValue("26"));
        setInt(profile, "cap_poll_interval", getIntValue("27"));
        setInt(profile, "src_throttle_publish", getIntValue("28"));
        setInt(profile, "poll_list_sub_exp", getIntValue("35"));
        profile.put("enable_gzip", Boolean.valueOf(getIntValue("38") == 1));
        setInt(profile, "subscribe_max_entry", getIntValue("29"));
        profile.setSupportSmsOverIms(mSmsOverIms);
        setInt(profile, "dm_polling_period", getIntValue("90"));
        if (getIntValue("116") >= 0) {
            profile.put("support_ipsec", Boolean.valueOf(getIntValue("116") == 1));
        }
        if (mIsKorOp && getIntValue("116") != -100000) {
            profile.put("support_ipsec", Boolean.valueOf(getIntValue("116") == 1));
        }
        if (getIntValue("72") >= 0) {
            if (getIntValue("72") == 1) {
                z = true;
            }
            profile.put("volte_service_status", Boolean.valueOf(z));
        }
        if (getStringValue("55") != null && getStringValue("55").equals("0")) {
            profile.put("audio_capabilities", DiagnosisConstants.RCSM_ORST_REGI);
        }
        setInt(profile, "h265_hd720p_payload", getIntValue("159"));
        setInt(profile, "reg_retry_base_time", getIntValue("84"));
        setInt(profile, "reg_retry_max_time", getIntValue("85"));
    }

    private static int getIntValue(String index) {
        int ret = -1;
        if (mIsKorOp) {
            ret = -100000;
        }
        try {
            return Integer.parseInt((String) mValueList.get(index));
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "no Value for " + index);
            return ret;
        }
    }

    private static String getStringValue(String index) {
        return (String) mValueList.get(index);
    }

    private static int getUriIndex(String uri) {
        for (DATA.DM_FIELD_INFO dmFieldInfo : DATA.DM_FIELD_LIST) {
            String name = dmFieldInfo.getName();
            if (dmFieldInfo.getType() != 0) {
                name = "./3GPP_IMS/" + name;
            }
            if (uri.equals(name)) {
                return dmFieldInfo.getIndex();
            }
        }
        return -1;
    }

    public static void setValueWithUri(String uri, String value) {
        int fieldIndex;
        if (uri != null && value != null && (fieldIndex = getUriIndex(uri)) != -1) {
            mValueList.put(Integer.toString(fieldIndex), value);
        }
    }

    private static void setInt(ImsProfile profile, String key, int value) {
        if (mIsKorOp) {
            if (value != -100000) {
                profile.put(key, Integer.valueOf(value));
            }
        } else if (value > 0) {
            profile.put(key, Integer.valueOf(value));
        }
    }

    private static void setString(ImsProfile profile, String key, String value) {
        if (value != null) {
            profile.put(key, value);
        }
    }
}
