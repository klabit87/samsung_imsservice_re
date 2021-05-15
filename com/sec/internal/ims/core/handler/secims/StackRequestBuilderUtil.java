package com.sec.internal.ims.core.handler.secims;

import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.options.Capabilities;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class StackRequestBuilderUtil {
    private static final String LOG_TAG = StackRequestBuilderUtil.class.getSimpleName();
    private static Map<Integer, Mno> translateMnoInverseMap = ((Map) translateMnoMap.entrySet().stream().collect(Collectors.toMap($$Lambda$MglhcRNC4x_Kjvf2MfPy5icuxg.INSTANCE, $$Lambda$XVqwAY2ifvQC2NsVu_T_lUVldO4.INSTANCE)));
    private static Map<Mno, Integer> translateMnoMap;

    StackRequestBuilderUtil() {
    }

    static {
        HashMap hashMap = new HashMap();
        translateMnoMap = hashMap;
        hashMap.put(Mno.VODAFONE, 7);
        translateMnoMap.put(Mno.SAMSUNG, 10);
        translateMnoMap.put(Mno.ROGERS, 11);
        translateMnoMap.put(Mno.CHATR, 217);
        translateMnoMap.put(Mno.ZTAR, 219);
        translateMnoMap.put(Mno.CTF, 218);
        translateMnoMap.put(Mno.MOBILICITY, 220);
        translateMnoMap.put(Mno.BELL, 12);
        translateMnoMap.put(Mno.GCF, 13);
        translateMnoMap.put(Mno.TELUS, 14);
        translateMnoMap.put(Mno.MDMN, 18);
        translateMnoMap.put(Mno.EASTLINK, 124);
        translateMnoMap.put(Mno.VTR, 129);
        translateMnoMap.put(Mno.WIND, 130);
        translateMnoMap.put(Mno.SASKTEL, 137);
        translateMnoMap.put(Mno.KOODO, Integer.valueOf(MNO.KOODO));
        translateMnoMap.put(Mno.ATT, 1);
        translateMnoMap.put(Mno.TMOUS, 2);
        translateMnoMap.put(Mno.VZW, 3);
        translateMnoMap.put(Mno.SPRINT, 8);
        translateMnoMap.put(Mno.USCC, 15);
        translateMnoMap.put(Mno.ALTICE, 215);
        translateMnoMap.put(Mno.SKT, 4);
        translateMnoMap.put(Mno.KT, 6);
        translateMnoMap.put(Mno.LGU, 5);
        translateMnoMap.put(Mno.CMCC, 9);
        translateMnoMap.put(Mno.CTC, 16);
        translateMnoMap.put(Mno.CU, 17);
        translateMnoMap.put(Mno.CSL, 19);
        translateMnoMap.put(Mno.HK3, 20);
        translateMnoMap.put(Mno.PCCW, 21);
        translateMnoMap.put(Mno.CMHK, 22);
        translateMnoMap.put(Mno.SMARTONE, 23);
        translateMnoMap.put(Mno.CTM, 24);
        translateMnoMap.put(Mno.MACAU_THREE, 25);
        translateMnoMap.put(Mno.MACAU_SMARTONE, 26);
        translateMnoMap.put(Mno.CTCMO, 27);
        translateMnoMap.put(Mno.APT, 28);
        translateMnoMap.put(Mno.TWM, 29);
        translateMnoMap.put(Mno.FET, 30);
        translateMnoMap.put(Mno.TSTAR, 31);
        translateMnoMap.put(Mno.CHT, 32);
        translateMnoMap.put(Mno.DOCOMO, 33);
        translateMnoMap.put(Mno.KDDI, 34);
        translateMnoMap.put(Mno.SOFTBANK, 35);
        translateMnoMap.put(Mno.RAKUTEN_JAPAN, Integer.valueOf(MNO.RAKUTEN_JAPAN));
        translateMnoMap.put(Mno.EE, 36);
        translateMnoMap.put(Mno.ORANGE, 37);
        translateMnoMap.put(Mno.TELENOR_NORWAY, 40);
        translateMnoMap.put(Mno.TELENOR_DK, 41);
        translateMnoMap.put(Mno.TELENOR_SWE, 144);
        translateMnoMap.put(Mno.TDC_DK, 42);
        translateMnoMap.put(Mno.ELISA_FINLAND, 44);
        translateMnoMap.put(Mno.DNA_FINLAND, 45);
        translateMnoMap.put(Mno.TELE2NL, 47);
        translateMnoMap.put(Mno.SWISSCOM, 48);
        translateMnoMap.put(Mno.AUSTRIA_A1, 49);
        translateMnoMap.put(Mno.SFR, 50);
        translateMnoMap.put(Mno.TELECOM_ITALY, 51);
        translateMnoMap.put(Mno.TMOBILE, 52);
        translateMnoMap.put(Mno.TMOBILE_CZ, 97);
        translateMnoMap.put(Mno.VODAFONE_ITALY, 53);
        translateMnoMap.put(Mno.VODAFONE_SPAIN, 66);
        translateMnoMap.put(Mno.VODAFONE_UK, 67);
        translateMnoMap.put(Mno.VODAFONE_NETHERLAND, 68);
        translateMnoMap.put(Mno.VODAFONE_IRELAND, 69);
        translateMnoMap.put(Mno.VODAFONE_PORTUGAL, 70);
        translateMnoMap.put(Mno.VODAFONE_GREECE, 71);
        translateMnoMap.put(Mno.VODAFONE_HUNGARY, 72);
        translateMnoMap.put(Mno.VODAFONE_CROATIA, Integer.valueOf(MNO.VODAFONE_CROATIA));
        translateMnoMap.put(Mno.VODAFONE_ROMANIA, Integer.valueOf(MNO.VODAFONE_ROMANIA));
        translateMnoMap.put(Mno.VODAFONE_ALBANIA, Integer.valueOf(MNO.VODAFONE_ALBANIA));
        translateMnoMap.put(Mno.VODAFONE_CZ, 113);
        translateMnoMap.put(Mno.PROXIMUS, 56);
        translateMnoMap.put(Mno.H3G, 57);
        translateMnoMap.put(Mno.METEOR_IRELAND, 62);
        translateMnoMap.put(Mno.TELEFONICA_GERMANY, 65);
        translateMnoMap.put(Mno.PLAY, 79);
        translateMnoMap.put(Mno.ORANGE_SWITZERLAND, 89);
        translateMnoMap.put(Mno.ORANGE_POLAND, 92);
        translateMnoMap.put(Mno.ORANGE_SPAIN, 93);
        translateMnoMap.put(Mno.ORANGE_ROMANIA, 94);
        translateMnoMap.put(Mno.ORANGE_SLOVAKIA, 112);
        translateMnoMap.put(Mno.ORANGE_MOLDOVA, Integer.valueOf(MNO.ORANGE_MOLDOVA));
        translateMnoMap.put(Mno.ORANGE_MOROCCO, Integer.valueOf(MNO.ORANGE_MOROCCO));
        translateMnoMap.put(Mno.ORANGE_SENEGAL, Integer.valueOf(MNO.ORANGE_SENEGAL));
        translateMnoMap.put(Mno.TELEFONICA_UK, 98);
        translateMnoMap.put(Mno.MTS_RUSSIA, 99);
        translateMnoMap.put(Mno.MEGAFON_RUSSIA, 135);
        translateMnoMap.put(Mno.TMOBILE_PL, 105);
        translateMnoMap.put(Mno.KPN_NED, 106);
        translateMnoMap.put(Mno.DIGI_HUNGARY, 147);
        translateMnoMap.put(Mno.TMOBILE_HUNGARY, 107);
        translateMnoMap.put(Mno.TMOBILE_ROMANIA, Integer.valueOf(MNO.TMOBILE_ROMANIA));
        translateMnoMap.put(Mno.TMOBILE_NED, 108);
        translateMnoMap.put(Mno.TMOBILE_GREECE, 109);
        translateMnoMap.put(Mno.H3G_AT, 114);
        translateMnoMap.put(Mno.TELEMACH_SVN, 115);
        translateMnoMap.put(Mno.BOG, 116);
        translateMnoMap.put(Mno.PTR, 127);
        translateMnoMap.put(Mno.TMOBILE_CROATIA, 117);
        translateMnoMap.put(Mno.TMOBILE_SLOVAKIA, 118);
        translateMnoMap.put(Mno.TELEFONICA_CZ, 119);
        translateMnoMap.put(Mno.TELIA_EE, 120);
        translateMnoMap.put(Mno.SUNRISE_CH, 121);
        translateMnoMap.put(Mno.ZEB, 125);
        translateMnoMap.put(Mno.BEELINE_RUSSIA, 148);
        translateMnoMap.put(Mno.SBERBANK_RUSSIA, 149);
        translateMnoMap.put(Mno.VELCOM_BY, Integer.valueOf(MNO.VELCOM_BY));
        translateMnoMap.put(Mno.TANGO_LUXEMBOURG, Integer.valueOf(MNO.TANGO_LUXEMBOURG));
        translateMnoMap.put(Mno.UPC_CH, Integer.valueOf(MNO.UPC_CH));
        translateMnoMap.put(Mno.BTOP, Integer.valueOf(MNO.BTOP_UK));
        translateMnoMap.put(Mno.TMOBILE_AUSTRIA, Integer.valueOf(MNO.TMOBILE_AUSTRIA));
        translateMnoMap.put(Mno.VIVACOM_BULGARIA, Integer.valueOf(MNO.VIVACOM_BG));
        translateMnoMap.put(Mno.MAGTICOM_GE, Integer.valueOf(MNO.MAGTICOM_GE));
        translateMnoMap.put(Mno.EE_ESN, Integer.valueOf(MNO.EVR_ESN));
        translateMnoMap.put(Mno.TELEFONICA_SLOVAKIA, Integer.valueOf(MNO.TELEFONICA_SLOVAKIA));
        translateMnoMap.put(Mno.TELEFONICA_SPAIN, Integer.valueOf(MNO.TELEFONICA_SPAIN));
        translateMnoMap.put(Mno.WIND_GREECE, 202);
        translateMnoMap.put(Mno.WINDTRE, Integer.valueOf(MNO.WINDTRE_IT));
        translateMnoMap.put(Mno.SKY, Integer.valueOf(MNO.SKY_GB));
        translateMnoMap.put(Mno.VIRGIN, 204);
        translateMnoMap.put(Mno.GAMMA, 205);
        translateMnoMap.put(Mno.SMARTY, 206);
        translateMnoMap.put(Mno.SUPERDRUG, 207);
        translateMnoMap.put(Mno.TMOBILE_MK, 225);
        translateMnoMap.put(Mno.TMOBILE_ME, 226);
        translateMnoMap.put(Mno.TELEKOM_ALBANIA, 227);
        translateMnoMap.put(Mno.MEO_PORTUGAL, 234);
        translateMnoMap.put(Mno.TELE2_SWE, 38);
        translateMnoMap.put(Mno.TELIA_SWE, 39);
        translateMnoMap.put(Mno.TELIA_NORWAY, 111);
        translateMnoMap.put(Mno.H3G_SE, 60);
        translateMnoMap.put(Mno.H3G_DK, 122);
        translateMnoMap.put(Mno.TELIA_FINLAND, Integer.valueOf(MNO.TELIA_FI));
        translateMnoMap.put(Mno.ICENET_NORWAY, Integer.valueOf(MNO.ICENET_NORWAY));
        translateMnoMap.put(Mno.VIVA_KUWAIT, 43);
        translateMnoMap.put(Mno.VIVA_BAHRAIN, 151);
        translateMnoMap.put(Mno.OOREDOO_KUWAIT, 139);
        translateMnoMap.put(Mno.OOREDOO_QATAR, 210);
        translateMnoMap.put(Mno.ETISALAT_UAE, 46);
        translateMnoMap.put(Mno.VODACOM_SOUTHAFRICA, 55);
        translateMnoMap.put(Mno.CELLC_SOUTHAFRICA, 75);
        translateMnoMap.put(Mno.VODAFONE_TURKEY, 77);
        translateMnoMap.put(Mno.AVEA_TURKEY, 78);
        translateMnoMap.put(Mno.TURKCELL_TURKEY, 76);
        translateMnoMap.put(Mno.MAROC_MOROCCO, 110);
        translateMnoMap.put(Mno.MCCI_IRAN, 132);
        translateMnoMap.put(Mno.ZAIN_KSA, 141);
        translateMnoMap.put(Mno.STC_KSA, Integer.valueOf(MNO.STC_KSA));
        translateMnoMap.put(Mno.TELIA_DK, 150);
        translateMnoMap.put(Mno.ZAIN_KUWAIT, 142);
        translateMnoMap.put(Mno.MTN_SOUTHAFRICA, Integer.valueOf(MNO.MTN_SOUTHAFRICA));
        translateMnoMap.put(Mno.MTN_IRAN, Integer.valueOf(MNO.MTN_IRAN));
        translateMnoMap.put(Mno.MTN_GHANA, Integer.valueOf(MNO.MTN_GHANA));
        translateMnoMap.put(Mno.BATELCO_BAHRAIN, Integer.valueOf(MNO.BATELCO_BAHRAIN));
        translateMnoMap.put(Mno.ETISALAT_EG, 203);
        translateMnoMap.put(Mno.ZAIN_JO, 221);
        translateMnoMap.put(Mno.RJIL, 54);
        translateMnoMap.put(Mno.DLOG, 90);
        translateMnoMap.put(Mno.VODAFONE_INDIA, 102);
        translateMnoMap.put(Mno.AIRTEL, 140);
        translateMnoMap.put(Mno.IDEA_INDIA, Integer.valueOf(MNO.IDEA_INDIA));
        translateMnoMap.put(Mno.BSNL, 201);
        translateMnoMap.put(Mno.GRAMEENPHONE, 200);
        translateMnoMap.put(Mno.TELSTRA, 58);
        translateMnoMap.put(Mno.VODAFONE_AUSTRALIA, 61);
        translateMnoMap.put(Mno.OPTUS, 74);
        translateMnoMap.put(Mno.VODAFONE_NEWZEALAND, Integer.valueOf(MNO.VODAFONE_NZ));
        translateMnoMap.put(Mno.AIS, 59);
        translateMnoMap.put(Mno.STARHUB, 63);
        translateMnoMap.put(Mno.MOBILEONE, 64);
        translateMnoMap.put(Mno.YTL, 73);
        translateMnoMap.put(Mno.DIGI, 103);
        translateMnoMap.put(Mno.SMARTFREN, 80);
        translateMnoMap.put(Mno.TELKOMSEL, 128);
        translateMnoMap.put(Mno.SINGTEL, 81);
        translateMnoMap.put(Mno.DTAC, 87);
        translateMnoMap.put(Mno.SMART_CAMBODIA, 104);
        translateMnoMap.put(Mno.GLOBE_PH, 123);
        translateMnoMap.put(Mno.VIETTEL, 134);
        translateMnoMap.put(Mno.UMOBILE, Integer.valueOf(MNO.UMOBILE));
        translateMnoMap.put(Mno.TPG_SG, Integer.valueOf(MNO.TPG_SG));
        translateMnoMap.put(Mno.VODAFONE_EG, 223);
        translateMnoMap.put(Mno.NTEL_NIGERIA, Integer.valueOf(MNO.NTEL_NIGERIA));
        translateMnoMap.put(Mno.JTL_KENYA, 152);
        translateMnoMap.put(Mno.SAFARICOM_KENYA, Integer.valueOf(MNO.SAFARICOM_KENYA));
        translateMnoMap.put(Mno.CABLE_PANAMA, 83);
        translateMnoMap.put(Mno.MOVISTAR_COLOMBIA, 84);
        translateMnoMap.put(Mno.MOVISTAR_PERU, 85);
        translateMnoMap.put(Mno.AVANTEL_COLOMBIA, 86);
        translateMnoMap.put(Mno.MOVISTAR_ARGENTINA, 88);
        translateMnoMap.put(Mno.VIVO_BRAZIL, 91);
        translateMnoMap.put(Mno.CLARO_PERU, 95);
        translateMnoMap.put(Mno.ENTEL_PERU, 96);
        translateMnoMap.put(Mno.CLARO_ARGENTINA, 100);
        translateMnoMap.put(Mno.TIGO_GUATEMALA, 101);
        translateMnoMap.put(Mno.TIM_BRAZIL, 126);
        translateMnoMap.put(Mno.TCE, 131);
        translateMnoMap.put(Mno.CLARO_BRAZIL, 133);
        translateMnoMap.put(Mno.MOVISTAR_CHILE, 136);
        translateMnoMap.put(Mno.ENTEL_BOLIVIA, 138);
        translateMnoMap.put(Mno.MOVISTAR_URUGUAY, 143);
        translateMnoMap.put(Mno.ATT_MEXICO, 145);
        translateMnoMap.put(Mno.MOVISTAR_ECUADOR, 146);
        translateMnoMap.put(Mno.ALIV_BAHAMAS, 153);
        translateMnoMap.put(Mno.PERSONAL_ARGENTINA, Integer.valueOf(MNO.PERSONAL_ARGENTINA));
        translateMnoMap.put(Mno.ALTAN_MEXICO, Integer.valueOf(MNO.ALTAN_MEXICO));
        translateMnoMap.put(Mno.AIRBUS_MEXICO, 211);
        translateMnoMap.put(Mno.CLARO_COLOMBIA, Integer.valueOf(MNO.CLARO_COLOMBIA));
        translateMnoMap.put(Mno.MOVISTAR_PANAMA, Integer.valueOf(MNO.MOVISTAR_PANAMA));
        translateMnoMap.put(Mno.MOVISTAR_MEXICO, Integer.valueOf(MNO.MOVISTAR_MEXICO));
        translateMnoMap.put(Mno.CLARO_CHILE, Integer.valueOf(MNO.CLARO_CHILE));
        translateMnoMap.put(Mno.WOM_CHILE, Integer.valueOf(MNO.WOM_CHILE));
        translateMnoMap.put(Mno.CLARO_URUGUAY, Integer.valueOf(MNO.CLARO_URUGUAY));
        translateMnoMap.put(Mno.ENTEL_CHILE, Integer.valueOf(MNO.ENTEL_CHILE));
        translateMnoMap.put(Mno.TIGO_COLOMBIA, Integer.valueOf(MNO.TIGO_COLOMBIA));
        translateMnoMap.put(Mno.TIGO_BOLIVIA, 208);
        translateMnoMap.put(Mno.CLARO_DOMINICAN, 209);
        translateMnoMap.put(Mno.CLARO_PARAGUAY, 212);
        translateMnoMap.put(Mno.OI_BRAZIL, 213);
        translateMnoMap.put(Mno.ORANGE_DOMINICANA, 214);
        translateMnoMap.put(Mno.CLARO_ECUADOR, 224);
        translateMnoMap.put(Mno.CLARO_GUATEMALA, 228);
        translateMnoMap.put(Mno.CLARO_PANAMA, 229);
        translateMnoMap.put(Mno.CLARO_COSTARICA, 230);
        translateMnoMap.put(Mno.CLARO_HONDURAS, 231);
        translateMnoMap.put(Mno.CLARO_ELSALVADOR, 232);
        translateMnoMap.put(Mno.CLARO_NICARAGUA, 233);
        translateMnoMap.put(Mno.CLARO_PUERTO, 235);
        translateMnoMap.put(Mno.GENERIC_IR92, 222);
    }

    static int translateMno(Mno mno) {
        Integer stackMno = translateMnoMap.get(mno);
        if (stackMno == null) {
            Log.e(LOG_TAG, "not added translate Mno Map");
            stackMno = 0;
        }
        return stackMno.intValue();
    }

    static Mno translateMnoInverse(int stackMno) {
        Mno mno = translateMnoInverseMap.get(Integer.valueOf(stackMno));
        if (mno == null) {
            Log.e(LOG_TAG, "not added translate Mno Inverse Map");
        }
        return mno;
    }

    static int[] getStringOffsetArray(FlatBufferBuilder builder, Iterable<String> stringList, int size) {
        int[] stringArray = new int[size];
        int i = 0;
        for (String service : stringList) {
            stringArray[i] = builder.createString((CharSequence) service);
            i++;
        }
        return stringArray;
    }

    static List<Integer> translateFeatureTag(long feature) {
        List<Integer> list = new ArrayList<>();
        String str = LOG_TAG;
        Log.i(str, "feature Tag" + feature);
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_MMTEL)) {
            Log.i(LOG_TAG, "feature Tag MMTEL");
            list.add(9);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_MMTEL_VIDEO)) {
            Log.i(LOG_TAG, "feature Tag MMTEL Video");
            list.add(6);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_MMTEL_CALL_COMPOSER)) {
            Log.i(LOG_TAG, "feature Tag MMTEL Call-composer");
            list.add(53);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_IPCALL)) {
            Log.i(LOG_TAG, "feature Tag IPCALL");
            list.add(0);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_IPCALL_VIDEO)) {
            Log.i(LOG_TAG, "feature Tag IPCALL VIDEO");
            list.add(1);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_IPCALL_VIDEO_ONLY)) {
            Log.i(LOG_TAG, "feature Tag IPCALL Video Only");
            list.add(2);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_PRESENCE_DISCOVERY)) {
            Log.i(LOG_TAG, "feature Tag PRESENCE_DISCOVERY");
            list.add(19);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_CHAT_CPM)) {
            Log.i(LOG_TAG, "feature Tag CHAT");
            list.add(20);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_CHAT_SIMPLE_IM)) {
            Log.i(LOG_TAG, "feature Tag SESSION_MODE_MSG");
            list.add(20);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_SF_GROUP_CHAT)) {
            Log.i(LOG_TAG, "feature Tag SF_GROUP_CHAT");
            list.add(21);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_FT)) {
            Log.i(LOG_TAG, "feature Tag FT");
            list.add(22);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_FT_HTTP)) {
            Log.i(LOG_TAG, "feature Tag FT_HTTP");
            list.add(25);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_FT_HTTP_EXTRA)) {
            Log.i(LOG_TAG, "feature Tag FT_HTTP_EXTRA");
            list.add(33);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_FT_STORE)) {
            Log.i(LOG_TAG, "feature Tag FT_STORE");
            list.add(24);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_FT_THUMBNAIL)) {
            Log.i(LOG_TAG, "feature Tag FT_THUMBNAIL");
            list.add(23);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_ISH)) {
            Log.i(LOG_TAG, "feature Tag ISH");
            list.add(18);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_VSH)) {
            Log.i(LOG_TAG, "feature Tag VSH");
            list.add(3);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_VSH_OUTSIDE_CALL)) {
            Log.i(LOG_TAG, "feature Tag VSH_OUTSIDE_CALL");
            list.add(26);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_INTEGRATED_MSG)) {
            Log.i(LOG_TAG, "feature Tag INTEGRATED_MSG");
            list.add(27);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_STANDALONE_MSG)) {
            Log.i(LOG_TAG, "feature Tag STANDALONE_MSG");
            list.add(10);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_SOCIAL_PRESENCE)) {
            Log.i(LOG_TAG, "feature Tag SOCIAL_PRESENCE");
            list.add(28);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_GEOLOCATION_PUSH)) {
            Log.i(LOG_TAG, "feature Tag GEOLOCATION_PUSH");
            list.add(31);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_GEOLOCATION_PULL)) {
            Log.i(LOG_TAG, "feature Tag GEOLOCATION_PULL");
            list.add(29);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_GEOLOCATION_PULL_FT)) {
            Log.i(LOG_TAG, "feature Tag GEOLOCATION_PULL_FT");
            list.add(30);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_ENRICHED_CALL_COMPOSER)) {
            Log.i(LOG_TAG, "feature Tag CALL_COMPOSER");
            list.add(14);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_ENRICHED_SHARED_MAP)) {
            Log.i(LOG_TAG, "feature Tag SHARED_MAP");
            list.add(15);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_ENRICHED_SHARED_SKETCH)) {
            Log.i(LOG_TAG, "feature Tag SHARED_SKETCH");
            list.add(17);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_ENRICHED_POST_CALL)) {
            Log.i(LOG_TAG, "feature Tag POST_CALL");
            list.add(16);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_STICKER)) {
            Log.i(LOG_TAG, "feature Tag STICKER");
            list.add(37);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_FT_VIA_SMS)) {
            Log.i(LOG_TAG, "feature Tag FT_VIA_SMS");
            list.add(38);
        }
        if (CapabilityUtil.hasFeature(feature, (long) Capabilities.FEATURE_GEO_VIA_SMS)) {
            Log.i(LOG_TAG, "feature Tag GEO_VIA_SMS");
            list.add(39);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_PUBLIC_MSG)) {
            Log.i(LOG_TAG, "feature Tag PUBLIC_MSG");
            list.add(42);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_LAST_SEEN_ACTIVE)) {
            Log.i(LOG_TAG, "feature Tag LAST_SEEN_ACTIVE");
            list.add(43);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_CHATBOT_CHAT_SESSION)) {
            Log.i(LOG_TAG, "feature Tag CHATBOT_CHAT_SESSION");
            list.add(44);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_CHATBOT_STANDALONE_MSG)) {
            Log.i(LOG_TAG, "feature Tag CHATBOT_STANDALONE_MSG");
            list.add(45);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_CHATBOT_EXTENDED_MSG)) {
            Log.i(LOG_TAG, "feature Tag EXTENDED_BOT_MSG");
            list.add(46);
        }
        if (CapabilityUtil.hasFeature(feature, Capabilities.FEATURE_CHATBOT_ROLE)) {
            Log.i(LOG_TAG, "feature Tag CHATBOT_ROLE");
            list.add(51);
        }
        return list;
    }

    static int translateConfigDualIms() {
        String config = SimUtil.getConfigDualIMS();
        if (SimConstants.DSDS_SI_DDS.equals(config) && SimUtil.getPhoneCount() == 2) {
            return 1;
        }
        if (SimConstants.DSDS_DI.equals(config)) {
            return 3;
        }
        if (SimConstants.DSDA_DI.equals(config)) {
            return 4;
        }
        return 0;
    }

    static List<Integer> translateExtraHeader(FlatBufferBuilder builder, HashMap<?, ?> map) {
        List<Integer> list = new ArrayList<>();
        for (Object obj : map.keySet()) {
            String key = obj.toString();
            list.add(Integer.valueOf(Pair.createPair(builder, builder.createString((CharSequence) key), builder.createString((CharSequence) map.get(key).toString()))));
        }
        return list;
    }
}
