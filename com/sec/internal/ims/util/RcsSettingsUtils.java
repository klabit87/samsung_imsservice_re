package com.sec.internal.ims.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.att.iqi.lib.BuildConfig;
import com.gsma.services.rcs.CommonServiceConfiguration;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class RcsSettingsUtils {
    private static final int COUNTRY_AREA_CODE_IDX = 1;
    private static final int COUNTRY_CODE_IDX = 0;
    private static final String COUNTRY_CODE_PREFIX = "+";
    private static final String ISO_ALPHA2_AFGHANISTAN = "af";
    private static final String ISO_ALPHA2_ALBANIA = "al";
    private static final String ISO_ALPHA2_ALGERIA = "dz";
    private static final String ISO_ALPHA2_AMERICAN_SAMOA = "as";
    private static final String ISO_ALPHA2_ANDORRA = "ad";
    private static final String ISO_ALPHA2_ANGOLA = "ao";
    private static final String ISO_ALPHA2_ANGUILLA = "ai";
    private static final String ISO_ALPHA2_ANTARCTICA = "aq";
    private static final String ISO_ALPHA2_ANTIGUA_BARBUDA = "ag";
    private static final String ISO_ALPHA2_ARABEMIRATES = "ae";
    private static final String ISO_ALPHA2_ARGENTINA = "ar";
    private static final String ISO_ALPHA2_ARMENIA = "am";
    private static final String ISO_ALPHA2_ARUBA = "aw";
    private static final String ISO_ALPHA2_AUSTRALIA = "au";
    private static final String ISO_ALPHA2_AUSTRIA = "at";
    private static final String ISO_ALPHA2_AZERBAIJAN = "az";
    private static final String ISO_ALPHA2_BAHAMAS = "bs";
    private static final String ISO_ALPHA2_BAHRAIN = "bh";
    private static final String ISO_ALPHA2_BANGLADESH = "bd";
    private static final String ISO_ALPHA2_BARBADOS = "bb";
    private static final String ISO_ALPHA2_BELARUS = "by";
    private static final String ISO_ALPHA2_BELGIUM = "be";
    private static final String ISO_ALPHA2_BELIZE = "bz";
    private static final String ISO_ALPHA2_BENIN = "bj";
    private static final String ISO_ALPHA2_BERMUDA = "bm";
    private static final String ISO_ALPHA2_BHUTAN = "bt";
    private static final String ISO_ALPHA2_BOLIVIA = "bo";
    private static final String ISO_ALPHA2_BOSNIA_HERZEGOVINA = "ba";
    private static final String ISO_ALPHA2_BOTSWANA = "bw";
    private static final String ISO_ALPHA2_BOUVET_ISLAND = "bv";
    private static final String ISO_ALPHA2_BRAZIL = "br";
    private static final String ISO_ALPHA2_BRITISH_INDIAN_OCEAN_TERRITORY = "io";
    private static final String ISO_ALPHA2_BRUNEI = "bn";
    private static final String ISO_ALPHA2_BULGARIA = "bg";
    private static final String ISO_ALPHA2_BURKINAFASO = "bf";
    private static final String ISO_ALPHA2_BURUNDI = "bi";
    private static final String ISO_ALPHA2_CABO_VERDE = "cv";
    private static final String ISO_ALPHA2_CAMBODIA = "kh";
    private static final String ISO_ALPHA2_CAMEROON = "cm";
    private static final String ISO_ALPHA2_CANADA = "ca";
    private static final String ISO_ALPHA2_CAYMAN_ISLANDS = "ky";
    private static final String ISO_ALPHA2_CENTRAL_AFRICAN_REPUBLIC = "cf";
    private static final String ISO_ALPHA2_CHAD = "td";
    private static final String ISO_ALPHA2_CHILE = "cl";
    private static final String ISO_ALPHA2_CHINA = "cn";
    private static final String ISO_ALPHA2_CHRISTMAS_ISLAND = "cx";
    private static final String ISO_ALPHA2_COCOS_ISLANDS = "cc";
    private static final String ISO_ALPHA2_COLOMBIA = "co";
    private static final String ISO_ALPHA2_COMOROS = "km";
    private static final String ISO_ALPHA2_CONGO = "cg";
    private static final String ISO_ALPHA2_CONGO_DEMOCRATIC_REPUBLIC = "cd";
    private static final String ISO_ALPHA2_COOK_ISLANDS = "ck";
    private static final String ISO_ALPHA2_COSTA_RICA = "cr";
    private static final String ISO_ALPHA2_COTE_D_IVOIRE = "ci";
    private static final String ISO_ALPHA2_CROATIA = "hr";
    private static final String ISO_ALPHA2_CUBA = "cu";
    private static final String ISO_ALPHA2_CYPRUS = "cy";
    private static final String ISO_ALPHA2_CZECHIA = "cz";
    private static final String ISO_ALPHA2_DENMARK = "dk";
    private static final String ISO_ALPHA2_DJIBOUTI = "dj";
    private static final String ISO_ALPHA2_DOMINICA = "dm";
    private static final String ISO_ALPHA2_DOMINICAN_REPUBLIC = "do";
    private static final String ISO_ALPHA2_ECUADOR = "ec";
    private static final String ISO_ALPHA2_EGYPT = "eg";
    private static final String ISO_ALPHA2_EL_SALVADOR = "sv";
    private static final String ISO_ALPHA2_EQUATORIAL = "gq";
    private static final String ISO_ALPHA2_ERITREA = "er";
    private static final String ISO_ALPHA2_ESTONIA = "ee";
    private static final String ISO_ALPHA2_ESWATINI = "sz";
    private static final String ISO_ALPHA2_ETHIOPIA = "et";
    private static final String ISO_ALPHA2_FALKLAND_ISLANDS = "fk";
    private static final String ISO_ALPHA2_FAROE_ISLANDS = "fo";
    private static final String ISO_ALPHA2_FIJI = "fj";
    private static final String ISO_ALPHA2_FINLAND = "fi";
    private static final String ISO_ALPHA2_FRANCE = "fr";
    private static final String ISO_ALPHA2_FRENCH_GUIANA = "gf";
    private static final String ISO_ALPHA2_FRENCH_POLYNESIA = "pf";
    private static final String ISO_ALPHA2_FRENCH_SOUTHERN_TERRITORIES = "tf";
    private static final String ISO_ALPHA2_GABON = "ga";
    private static final String ISO_ALPHA2_GAMBIA = "gm";
    private static final String ISO_ALPHA2_GEORGIA = "ge";
    private static final String ISO_ALPHA2_GERMANY = "de";
    private static final String ISO_ALPHA2_GHANA = "gh";
    private static final String ISO_ALPHA2_GIBRALTAR = "gi";
    private static final String ISO_ALPHA2_GREAT_BRITAIN = "gb";
    private static final String ISO_ALPHA2_GREECE = "gr";
    private static final String ISO_ALPHA2_GREENLAND = "gl";
    private static final String ISO_ALPHA2_GRENADA = "gd";
    private static final String ISO_ALPHA2_GUADELOUPE = "gp";
    private static final String ISO_ALPHA2_GUAM = "gu";
    private static final String ISO_ALPHA2_GUATEMALA = "gt";
    private static final String ISO_ALPHA2_GUINEA = "gn";
    private static final String ISO_ALPHA2_GUINEA_BISSAU = "gw";
    private static final String ISO_ALPHA2_GUYANA = "gy";
    private static final String ISO_ALPHA2_HAITI = "ht";
    private static final String ISO_ALPHA2_HEARD_MCDONALD_ISLANDS = "hm";
    private static final String ISO_ALPHA2_HOLYSEE = "va";
    private static final String ISO_ALPHA2_HONDURAS = "hn";
    private static final String ISO_ALPHA2_HONGKONG = "hk";
    private static final String ISO_ALPHA2_HUNGARY = "hu";
    private static final String ISO_ALPHA2_ICELAND = "is";
    private static final String ISO_ALPHA2_INDIA = "in";
    private static final String ISO_ALPHA2_INDONESIA = "id";
    private static final String ISO_ALPHA2_IRAN = "ir";
    private static final String ISO_ALPHA2_IRAQ = "iq";
    private static final String ISO_ALPHA2_IRELAND = "ie";
    private static final String ISO_ALPHA2_ISRAEL = "il";
    private static final String ISO_ALPHA2_ITALY = "it";
    private static final String ISO_ALPHA2_JAMAICA = "jm";
    private static final String ISO_ALPHA2_JAPAN = "jp";
    private static final String ISO_ALPHA2_JORDAN = "jo";
    private static final String ISO_ALPHA2_KAZAKHSTAN = "kz";
    private static final String ISO_ALPHA2_KENYA = "ke";
    private static final String ISO_ALPHA2_KIRIBATI = "ki";
    private static final String ISO_ALPHA2_KUWAIT = "kw";
    private static final String ISO_ALPHA2_KYRGYZSTAN = "kg";
    private static final String ISO_ALPHA2_LAO_PEOPLES_DEMOCRATIC_REPUBLIC = "la";
    private static final String ISO_ALPHA2_LATVIA = "lv";
    private static final String ISO_ALPHA2_LEBANON = "lb";
    private static final String ISO_ALPHA2_LESOTHO = "ls";
    private static final String ISO_ALPHA2_LIBERIA = "lr";
    private static final String ISO_ALPHA2_LIBYA = "ly";
    private static final String ISO_ALPHA2_LIECHTENSTEIN = "li";
    private static final String ISO_ALPHA2_LITHUANIA = "lt";
    private static final String ISO_ALPHA2_LUXEMBOURG = "lu";
    private static final String ISO_ALPHA2_MACAO = "mo";
    private static final String ISO_ALPHA2_MADAGASCAR = "mg";
    private static final String ISO_ALPHA2_MALAWI = "mw";
    private static final String ISO_ALPHA2_MALAYSIA = "my";
    private static final String ISO_ALPHA2_MALDIVES = "mv";
    private static final String ISO_ALPHA2_MALI = "ml";
    private static final String ISO_ALPHA2_MALTA = "mt";
    private static final String ISO_ALPHA2_MARSHALL_ISLANDS = "mh";
    private static final String ISO_ALPHA2_MARTINIQUE = "mq";
    private static final String ISO_ALPHA2_MAURITANIA = "mr";
    private static final String ISO_ALPHA2_MAURITIUS = "mu";
    private static final String ISO_ALPHA2_MAYOTTE = "yt";
    private static final String ISO_ALPHA2_MEXICO = "mx";
    private static final String ISO_ALPHA2_MICRONESIA = "fm";
    private static final String ISO_ALPHA2_MOLDOVA = "md";
    private static final String ISO_ALPHA2_MONACO = "mc";
    private static final String ISO_ALPHA2_MONGOLIA = "mn";
    private static final String ISO_ALPHA2_MONTENEGRO = "me";
    private static final String ISO_ALPHA2_MONTSERRAT = "ms";
    private static final String ISO_ALPHA2_MOROCCO = "ma";
    private static final String ISO_ALPHA2_MOZAMBIQUE = "mz";
    private static final String ISO_ALPHA2_MYANMAR = "mm";
    private static final String ISO_ALPHA2_NAMIBIA = "na";
    private static final String ISO_ALPHA2_NAURU = "nr";
    private static final String ISO_ALPHA2_NEPAL = "np";
    private static final String ISO_ALPHA2_NETHERLANDS = "nl";
    private static final String ISO_ALPHA2_NETHERLANDS_ANTILLES = "an";
    private static final String ISO_ALPHA2_NEW_CALEDONIA = "nc";
    private static final String ISO_ALPHA2_NEW_ZEALAND = "nz";
    private static final String ISO_ALPHA2_NICARAGUA = "ni";
    private static final String ISO_ALPHA2_NIGER = "ne";
    private static final String ISO_ALPHA2_NIGERIA = "ng";
    private static final String ISO_ALPHA2_NIUE = "nu";
    private static final String ISO_ALPHA2_NORFOLK_ISLAND = "nf";
    private static final String ISO_ALPHA2_NORTHERN_MARIANA_ISLANDS = "mp";
    private static final String ISO_ALPHA2_NORTH_KOREA = "kp";
    private static final String ISO_ALPHA2_NORTH_MACEDONIA = "mk";
    private static final String ISO_ALPHA2_NORWAY = "no";
    private static final String ISO_ALPHA2_OMAN = "om";
    private static final String ISO_ALPHA2_PAKISTAN = "pk";
    private static final String ISO_ALPHA2_PALAU = "pw";
    private static final String ISO_ALPHA2_PALESTINE = "ps";
    private static final String ISO_ALPHA2_PANAMA = "pa";
    private static final String ISO_ALPHA2_PAPUA_NEW_GUINEA = "pg";
    private static final String ISO_ALPHA2_PARAGUAY = "py";
    private static final String ISO_ALPHA2_PERU = "pe";
    private static final String ISO_ALPHA2_PHILIPPINES = "ph";
    private static final String ISO_ALPHA2_PITCAIRN = "pn";
    private static final String ISO_ALPHA2_POLAND = "pl";
    private static final String ISO_ALPHA2_PORTUGAL = "pt";
    private static final String ISO_ALPHA2_PUERTO_RICO = "pr";
    private static final String ISO_ALPHA2_QATAR = "qa";
    private static final String ISO_ALPHA2_REUNION = "re";
    private static final String ISO_ALPHA2_ROMANIA = "ro";
    private static final String ISO_ALPHA2_RUSSIA = "ru";
    private static final String ISO_ALPHA2_RWANDA = "rw";
    private static final String ISO_ALPHA2_SAINTKITTS = "kn";
    private static final String ISO_ALPHA2_SAINTVINCENT = "vc";
    private static final String ISO_ALPHA2_SAINT_HELENA = "sh";
    private static final String ISO_ALPHA2_SAINT_LUCIA = "lc";
    private static final String ISO_ALPHA2_SAINT_PIERRE_MIQUELON = "pm";
    private static final String ISO_ALPHA2_SAMOA = "ws";
    private static final String ISO_ALPHA2_SAN_MARINO = "sm";
    private static final String ISO_ALPHA2_SAO_TOME_PRINCIPE = "st";
    private static final String ISO_ALPHA2_SAUDI_ARABIA = "sa";
    private static final String ISO_ALPHA2_SENEGAL = "sn";
    private static final String ISO_ALPHA2_SERBIA = "rs";
    private static final String ISO_ALPHA2_SEYCHELLES = "sc";
    private static final String ISO_ALPHA2_SIERRA_LEONE = "sl";
    private static final String ISO_ALPHA2_SINGAPORE = "sg";
    private static final String ISO_ALPHA2_SLOVAKIA = "sk";
    private static final String ISO_ALPHA2_SLOVENIA = "si";
    private static final String ISO_ALPHA2_SOLOMON_ISLANDS = "sb";
    private static final String ISO_ALPHA2_SOMALIA = "so";
    private static final String ISO_ALPHA2_SOUTHAFRICA = "za";
    private static final String ISO_ALPHA2_SOUTH_GEORGIA = "gs";
    private static final String ISO_ALPHA2_SOUTH_KOREA = "kr";
    private static final String ISO_ALPHA2_SPAIN = "es";
    private static final String ISO_ALPHA2_SRILANKA = "lk";
    private static final String ISO_ALPHA2_SUDAN = "sd";
    private static final String ISO_ALPHA2_SURINAME = "sr";
    private static final String ISO_ALPHA2_SVALBARD_JAN_MAYEN = "sj";
    private static final String ISO_ALPHA2_SWEDEN = "se";
    private static final String ISO_ALPHA2_SWITZERLAND = "ch";
    private static final String ISO_ALPHA2_SYRIA = "sy";
    private static final String ISO_ALPHA2_TAIWAN = "tw";
    private static final String ISO_ALPHA2_TAJIKISTAN = "tj";
    private static final String ISO_ALPHA2_TANZANIA = "tz";
    private static final String ISO_ALPHA2_THAILAND = "th";
    private static final String ISO_ALPHA2_TIMOR_LESTE = "tl";
    private static final String ISO_ALPHA2_TOGO = "tg";
    private static final String ISO_ALPHA2_TOKELAU = "tk";
    private static final String ISO_ALPHA2_TONGA = "to";
    private static final String ISO_ALPHA2_TRINIDAD_TOBAGO = "tt";
    private static final String ISO_ALPHA2_TUNISIA = "tn";
    private static final String ISO_ALPHA2_TURKEY = "tr";
    private static final String ISO_ALPHA2_TURKMENISTAN = "tm";
    private static final String ISO_ALPHA2_TURKS_CAICOS_ISLANDS = "tc";
    private static final String ISO_ALPHA2_TUVALU = "tv";
    private static final String ISO_ALPHA2_UGANDA = "ug";
    private static final String ISO_ALPHA2_UKRAINE = "ua";
    private static final String ISO_ALPHA2_URUGUAY = "uy";
    private static final String ISO_ALPHA2_US = "us";
    private static final String ISO_ALPHA2_US_MINO_ROUTLYING_ISLANDS = "um";
    private static final String ISO_ALPHA2_UZBEKISTAN = "uz";
    private static final String ISO_ALPHA2_VANUATU = "vu";
    private static final String ISO_ALPHA2_VENEZUELA = "ve";
    private static final String ISO_ALPHA2_VIETNAM = "vn";
    private static final String ISO_ALPHA2_VIRGINISLANDS_BRITISH = "vg";
    private static final String ISO_ALPHA2_VIRGINISLANDS_US = "vi";
    private static final String ISO_ALPHA2_WALLISFUTUNA = "wf";
    private static final String ISO_ALPHA2_WESTERN_SAHARA = "eh";
    private static final String ISO_ALPHA2_YEMEN = "ye";
    private static final String ISO_ALPHA2_ZAMBIA = "zm";
    private static final String ISO_ALPHA2_ZIMBABWE = "zw";
    private static final String WHERE_CLAUSE = ("key" + "=?");
    private static final Map<String, String[]> sCountryCodes = new HashMap<String, String[]>() {
        private static final long serialVersionUID = 1;

        {
            put(RcsSettingsUtils.ISO_ALPHA2_ANDORRA, new String[]{"376"});
            put(RcsSettingsUtils.ISO_ALPHA2_ARABEMIRATES, new String[]{"971", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_AFGHANISTAN, new String[]{"93", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ANTIGUA_BARBUDA, new String[]{"1-268", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_ANGUILLA, new String[]{"1-264", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_ALBANIA, new String[]{"355", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ARMENIA, new String[]{"374", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_NETHERLANDS_ANTILLES, new String[]{"599", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ANGOLA, new String[]{"244", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ANTARCTICA, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_ARGENTINA, new String[]{"54", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_AMERICAN_SAMOA, new String[]{"1-684", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_AUSTRIA, new String[]{"43", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_AUSTRALIA, new String[]{"61", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ARUBA, new String[]{"297"});
            put(RcsSettingsUtils.ISO_ALPHA2_AZERBAIJAN, new String[]{"994", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BOSNIA_HERZEGOVINA, new String[]{"387", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BARBADOS, new String[]{"1-246", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_BANGLADESH, new String[]{"880", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BELGIUM, new String[]{"32", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BURKINAFASO, new String[]{"226"});
            put(RcsSettingsUtils.ISO_ALPHA2_BULGARIA, new String[]{"359", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BAHRAIN, new String[]{"973"});
            put(RcsSettingsUtils.ISO_ALPHA2_BURUNDI, new String[]{"257"});
            put(RcsSettingsUtils.ISO_ALPHA2_BENIN, new String[]{"229"});
            put(RcsSettingsUtils.ISO_ALPHA2_BERMUDA, new String[]{"1-441", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_BRUNEI, new String[]{"673"});
            put(RcsSettingsUtils.ISO_ALPHA2_BOLIVIA, new String[]{"591", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BRAZIL, new String[]{"55", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BAHAMAS, new String[]{"1-242", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_BHUTAN, new String[]{"975"});
            put(RcsSettingsUtils.ISO_ALPHA2_BOUVET_ISLAND, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_BOTSWANA, new String[]{"267"});
            put(RcsSettingsUtils.ISO_ALPHA2_BELARUS, new String[]{"375", "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_BELIZE, new String[]{"501"});
            put(RcsSettingsUtils.ISO_ALPHA2_CANADA, new String[]{"1", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_COCOS_ISLANDS, (Object) null);
            put("cd", new String[]{"243"});
            put(RcsSettingsUtils.ISO_ALPHA2_CENTRAL_AFRICAN_REPUBLIC, new String[]{"236"});
            put(RcsSettingsUtils.ISO_ALPHA2_CONGO, new String[]{"242"});
            put(RcsSettingsUtils.ISO_ALPHA2_SWITZERLAND, new String[]{"41", "0"});
            put("ci", new String[]{"225"});
            put(RcsSettingsUtils.ISO_ALPHA2_COOK_ISLANDS, new String[]{"682"});
            put("cl", new String[]{"56", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_CAMEROON, new String[]{"237"});
            put(RcsSettingsUtils.ISO_ALPHA2_CHINA, new String[]{"86", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_COLOMBIA, new String[]{"57", "09"});
            put(RcsSettingsUtils.ISO_ALPHA2_COSTA_RICA, new String[]{"506"});
            put(RcsSettingsUtils.ISO_ALPHA2_CUBA, new String[]{"53", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_CABO_VERDE, new String[]{"238"});
            put(RcsSettingsUtils.ISO_ALPHA2_CHRISTMAS_ISLAND, new String[]{"61"});
            put(RcsSettingsUtils.ISO_ALPHA2_CYPRUS, new String[]{"357"});
            put(RcsSettingsUtils.ISO_ALPHA2_CZECHIA, new String[]{"420", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_GERMANY, new String[]{"49", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_DJIBOUTI, new String[]{"253"});
            put(RcsSettingsUtils.ISO_ALPHA2_DENMARK, new String[]{"45"});
            put(RcsSettingsUtils.ISO_ALPHA2_DOMINICA, new String[]{"1-767", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_DOMINICAN_REPUBLIC, new String[]{"1-809", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_ALGERIA, new String[]{"213", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ECUADOR, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_ESTONIA, new String[]{"372"});
            put(RcsSettingsUtils.ISO_ALPHA2_EGYPT, new String[]{"20", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_WESTERN_SAHARA, new String[]{"212"});
            put(RcsSettingsUtils.ISO_ALPHA2_ERITREA, new String[]{"291", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SPAIN, new String[]{"34"});
            put(RcsSettingsUtils.ISO_ALPHA2_ETHIOPIA, new String[]{"251", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_FINLAND, new String[]{"358", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_FIJI, new String[]{"679"});
            put(RcsSettingsUtils.ISO_ALPHA2_FALKLAND_ISLANDS, new String[]{"500"});
            put(RcsSettingsUtils.ISO_ALPHA2_MICRONESIA, new String[]{"691", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_FAROE_ISLANDS, new String[]{"298"});
            put(RcsSettingsUtils.ISO_ALPHA2_FRANCE, new String[]{"33", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_GABON, new String[]{"241"});
            put(RcsSettingsUtils.ISO_ALPHA2_GREAT_BRITAIN, new String[]{"44", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_GRENADA, new String[]{"1-473", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_GEORGIA, new String[]{"955", "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_FRENCH_GUIANA, new String[]{"594", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_GHANA, new String[]{"233", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_GIBRALTAR, new String[]{"350"});
            put(RcsSettingsUtils.ISO_ALPHA2_GREENLAND, new String[]{"299"});
            put(RcsSettingsUtils.ISO_ALPHA2_GAMBIA, new String[]{"220"});
            put(RcsSettingsUtils.ISO_ALPHA2_GUINEA, new String[]{"224"});
            put(RcsSettingsUtils.ISO_ALPHA2_GUADELOUPE, new String[]{"590", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_EQUATORIAL, new String[]{"240"});
            put(RcsSettingsUtils.ISO_ALPHA2_GREECE, new String[]{"30"});
            put(RcsSettingsUtils.ISO_ALPHA2_SOUTH_GEORGIA, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_GUATEMALA, new String[]{"502"});
            put(RcsSettingsUtils.ISO_ALPHA2_GUAM, new String[]{"1-671", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_GUINEA_BISSAU, new String[]{"245"});
            put(RcsSettingsUtils.ISO_ALPHA2_GUYANA, new String[]{"592"});
            put(RcsSettingsUtils.ISO_ALPHA2_HONGKONG, new String[]{"852"});
            put(RcsSettingsUtils.ISO_ALPHA2_HEARD_MCDONALD_ISLANDS, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_HONDURAS, new String[]{"504"});
            put(RcsSettingsUtils.ISO_ALPHA2_CROATIA, new String[]{"385", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_HAITI, new String[]{"509"});
            put(RcsSettingsUtils.ISO_ALPHA2_HUNGARY, new String[]{"36", "06"});
            put("id", new String[]{"62", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_IRELAND, new String[]{"353", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ISRAEL, new String[]{"972", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_INDIA, new String[]{"91", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_BRITISH_INDIAN_OCEAN_TERRITORY, new String[]{"246"});
            put(RcsSettingsUtils.ISO_ALPHA2_IRAQ, new String[]{"964"});
            put(RcsSettingsUtils.ISO_ALPHA2_IRAN, new String[]{"98", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ICELAND, new String[]{"354"});
            put(RcsSettingsUtils.ISO_ALPHA2_ITALY, new String[]{"39"});
            put(RcsSettingsUtils.ISO_ALPHA2_JAMAICA, new String[]{"1-876", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_JORDAN, new String[]{"962", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_JAPAN, new String[]{"81", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_KENYA, new String[]{"254", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_KYRGYZSTAN, new String[]{"996", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_CAMBODIA, new String[]{"855", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_KIRIBATI, new String[]{"686"});
            put(RcsSettingsUtils.ISO_ALPHA2_COMOROS, new String[]{"269"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAINTKITTS, new String[]{"1-869", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_NORTH_KOREA, new String[]{"850"});
            put(RcsSettingsUtils.ISO_ALPHA2_SOUTH_KOREA, new String[]{"82", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_KUWAIT, new String[]{"965"});
            put(RcsSettingsUtils.ISO_ALPHA2_CAYMAN_ISLANDS, new String[]{"1-345", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_KAZAKHSTAN, new String[]{"7-7", "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_LAO_PEOPLES_DEMOCRATIC_REPUBLIC, new String[]{"856"});
            put(RcsSettingsUtils.ISO_ALPHA2_LEBANON, new String[]{"961", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAINT_LUCIA, new String[]{"1-758", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_LIECHTENSTEIN, new String[]{"423"});
            put(RcsSettingsUtils.ISO_ALPHA2_SRILANKA, new String[]{"94", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_LIBERIA, new String[]{"231"});
            put(RcsSettingsUtils.ISO_ALPHA2_LESOTHO, new String[]{"266"});
            put(RcsSettingsUtils.ISO_ALPHA2_LITHUANIA, new String[]{"370", "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_LUXEMBOURG, new String[]{"352"});
            put(RcsSettingsUtils.ISO_ALPHA2_LATVIA, new String[]{"371"});
            put(RcsSettingsUtils.ISO_ALPHA2_LIBYA, new String[]{"281", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MOROCCO, new String[]{"212", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MONACO, new String[]{"377"});
            put(RcsSettingsUtils.ISO_ALPHA2_MOLDOVA, new String[]{"373", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MONTENEGRO, new String[]{"382", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MADAGASCAR, new String[]{"261"});
            put(RcsSettingsUtils.ISO_ALPHA2_MARSHALL_ISLANDS, new String[]{"692", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_NORTH_MACEDONIA, new String[]{"389", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MALI, new String[]{"223"});
            put(RcsSettingsUtils.ISO_ALPHA2_MYANMAR, new String[]{"95"});
            put(RcsSettingsUtils.ISO_ALPHA2_MONGOLIA, new String[]{"976", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MACAO, new String[]{"853"});
            put(RcsSettingsUtils.ISO_ALPHA2_NORTHERN_MARIANA_ISLANDS, new String[]{"1-670", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_MARTINIQUE, new String[]{"596", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MAURITANIA, new String[]{"222"});
            put(RcsSettingsUtils.ISO_ALPHA2_MONTSERRAT, new String[]{"1-664", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_MALTA, new String[]{"356"});
            put(RcsSettingsUtils.ISO_ALPHA2_MAURITIUS, new String[]{"230"});
            put(RcsSettingsUtils.ISO_ALPHA2_MALDIVES, new String[]{"960"});
            put(RcsSettingsUtils.ISO_ALPHA2_MALAWI, new String[]{"265"});
            put(RcsSettingsUtils.ISO_ALPHA2_MEXICO, new String[]{"52", "01"});
            put(RcsSettingsUtils.ISO_ALPHA2_MALAYSIA, new String[]{"60", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MOZAMBIQUE, new String[]{"258"});
            put(RcsSettingsUtils.ISO_ALPHA2_NAMIBIA, new String[]{"264", "0"});
            put("nc", new String[]{"687"});
            put(RcsSettingsUtils.ISO_ALPHA2_NIGER, new String[]{"227"});
            put(RcsSettingsUtils.ISO_ALPHA2_NORFOLK_ISLAND, new String[]{"6723"});
            put(RcsSettingsUtils.ISO_ALPHA2_NIGERIA, new String[]{"234", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_NICARAGUA, new String[]{"505"});
            put(RcsSettingsUtils.ISO_ALPHA2_NETHERLANDS, new String[]{"31", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_NORWAY, new String[]{"47"});
            put(RcsSettingsUtils.ISO_ALPHA2_NEPAL, new String[]{"977", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_NAURU, new String[]{"674"});
            put(RcsSettingsUtils.ISO_ALPHA2_NIUE, new String[]{"683"});
            put(RcsSettingsUtils.ISO_ALPHA2_NEW_ZEALAND, new String[]{"64", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_OMAN, new String[]{"968"});
            put(RcsSettingsUtils.ISO_ALPHA2_PANAMA, new String[]{"507"});
            put(RcsSettingsUtils.ISO_ALPHA2_PERU, new String[]{"51", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_FRENCH_POLYNESIA, new String[]{"689"});
            put(RcsSettingsUtils.ISO_ALPHA2_PAPUA_NEW_GUINEA, new String[]{"675"});
            put(RcsSettingsUtils.ISO_ALPHA2_PHILIPPINES, new String[]{"63", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_PAKISTAN, new String[]{"92", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_POLAND, new String[]{"48"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAINT_PIERRE_MIQUELON, new String[]{"508"});
            put(RcsSettingsUtils.ISO_ALPHA2_PITCAIRN, new String[]{"870"});
            put(RcsSettingsUtils.ISO_ALPHA2_PUERTO_RICO, new String[]{"1-787", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_PALESTINE, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_PORTUGAL, new String[]{"351"});
            put(RcsSettingsUtils.ISO_ALPHA2_PALAU, new String[]{"680"});
            put(RcsSettingsUtils.ISO_ALPHA2_PARAGUAY, new String[]{"595", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_QATAR, new String[]{"974"});
            put(RcsSettingsUtils.ISO_ALPHA2_REUNION, new String[]{"262", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ROMANIA, new String[]{"40", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SERBIA, new String[]{"381", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_RUSSIA, new String[]{BuildConfig.VERSION_NAME, "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_RWANDA, new String[]{"250"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAUDI_ARABIA, new String[]{"966", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SOLOMON_ISLANDS, new String[]{"677"});
            put(RcsSettingsUtils.ISO_ALPHA2_SEYCHELLES, new String[]{"248"});
            put(RcsSettingsUtils.ISO_ALPHA2_SUDAN, new String[]{"249", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SWEDEN, new String[]{"46", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SINGAPORE, new String[]{"65"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAINT_HELENA, new String[]{"290"});
            put(RcsSettingsUtils.ISO_ALPHA2_SLOVENIA, new String[]{"386"});
            put(RcsSettingsUtils.ISO_ALPHA2_SVALBARD_JAN_MAYEN, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_SLOVAKIA, new String[]{"421", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SIERRA_LEONE, new String[]{"232", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAN_MARINO, new String[]{"378"});
            put(RcsSettingsUtils.ISO_ALPHA2_SENEGAL, new String[]{"221"});
            put(RcsSettingsUtils.ISO_ALPHA2_SOMALIA, new String[]{"252"});
            put(RcsSettingsUtils.ISO_ALPHA2_SURINAME, new String[]{"597", "0"});
            put("st", new String[]{"239"});
            put(RcsSettingsUtils.ISO_ALPHA2_EL_SALVADOR, new String[]{"503"});
            put(RcsSettingsUtils.ISO_ALPHA2_SYRIA, new String[]{"963", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ESWATINI, new String[]{"268"});
            put(RcsSettingsUtils.ISO_ALPHA2_TURKS_CAICOS_ISLANDS, new String[]{"1-649", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_CHAD, new String[]{"235"});
            put(RcsSettingsUtils.ISO_ALPHA2_FRENCH_SOUTHERN_TERRITORIES, (Object) null);
            put(RcsSettingsUtils.ISO_ALPHA2_TOGO, new String[]{"228"});
            put(RcsSettingsUtils.ISO_ALPHA2_THAILAND, new String[]{"66", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_TAJIKISTAN, new String[]{"992", "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_TOKELAU, new String[]{"690"});
            put(RcsSettingsUtils.ISO_ALPHA2_TIMOR_LESTE, new String[]{"670"});
            put(RcsSettingsUtils.ISO_ALPHA2_TURKMENISTAN, new String[]{"993", "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_TUNISIA, new String[]{"216"});
            put(RcsSettingsUtils.ISO_ALPHA2_TONGA, new String[]{"676"});
            put(RcsSettingsUtils.ISO_ALPHA2_TURKEY, new String[]{"90", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_TRINIDAD_TOBAGO, new String[]{"1-868", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_TUVALU, new String[]{"688"});
            put(RcsSettingsUtils.ISO_ALPHA2_TAIWAN, new String[]{"886", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_TANZANIA, new String[]{"255", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_UKRAINE, new String[]{"380", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_UGANDA, new String[]{"256", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_US_MINO_ROUTLYING_ISLANDS, new String[]{"1"});
            put(RcsSettingsUtils.ISO_ALPHA2_US, new String[]{"1", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_URUGUAY, new String[]{"598", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_UZBEKISTAN, new String[]{"998", "8"});
            put(RcsSettingsUtils.ISO_ALPHA2_HOLYSEE, new String[]{"379"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAINTVINCENT, new String[]{"1-784", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_VENEZUELA, new String[]{"58", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_VIRGINISLANDS_BRITISH, new String[]{"1-284", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_VIRGINISLANDS_US, new String[]{"1-340", "1"});
            put(RcsSettingsUtils.ISO_ALPHA2_VIETNAM, new String[]{"84", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_VANUATU, new String[]{"678"});
            put(RcsSettingsUtils.ISO_ALPHA2_WALLISFUTUNA, new String[]{"681"});
            put(RcsSettingsUtils.ISO_ALPHA2_SAMOA, new String[]{"685"});
            put(RcsSettingsUtils.ISO_ALPHA2_YEMEN, new String[]{"967", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_MAYOTTE, new String[]{"262"});
            put(RcsSettingsUtils.ISO_ALPHA2_SOUTHAFRICA, new String[]{"27", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ZAMBIA, new String[]{"260", "0"});
            put(RcsSettingsUtils.ISO_ALPHA2_ZIMBABWE, new String[]{"263", "0"});
        }
    };
    private static Uri sDatabaseUri = CommonServiceConfiguration.Settings.CONTENT_URI;
    private static RcsSettingsUtils sInstance = null;
    /* access modifiers changed from: private */
    public String LOG_TAG = RcsSettingsUtils.class.getSimpleName();
    private Context mContext = null;

    private RcsSettingsUtils(Context ctx) {
        this.mContext = ctx;
    }

    public void writeBoolean(String key, boolean value) {
        writeParameter(key, Boolean.toString(value));
    }

    public String readParameter(String key) {
        if (sInstance == null) {
            Log.e(this.LOG_TAG, "RcsInstance not created");
            return "";
        }
        Cursor c = this.mContext.getContentResolver().query(sDatabaseUri, (String[]) null, WHERE_CLAUSE, new String[]{key}, (String) null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String result = c.getString(c.getColumnIndexOrThrow(ImsConstants.Intents.EXTRA_UPDATED_VALUE));
                    Log.d(this.LOG_TAG, String.format("readParameter: %s = %s", new Object[]{key, result}));
                    if (c != null) {
                        c.close();
                    }
                    return result;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        Log.d(this.LOG_TAG, "readParameter cursor null");
        if (c != null) {
            c.close();
        }
        return "";
        throw th;
    }

    public int writeParameter(String key, String value) {
        if (sInstance == null || value == null) {
            return 0;
        }
        String str = this.LOG_TAG;
        IMSLog.s(str, "writeParameter: " + key + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + value);
        ContentValues values = new ContentValues();
        values.put(ImsConstants.Intents.EXTRA_UPDATED_VALUE, value);
        try {
            return this.mContext.getContentResolver().update(sDatabaseUri, values, WHERE_CLAUSE, new String[]{key});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        } catch (SQLException e2) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "SQL exception while insert Settings Parameter. " + e2);
            return 0;
        }
    }

    public void loadCCAndAC() {
        String countryCodeIso = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getSimCountryIso();
        if (TextUtils.isEmpty(countryCodeIso)) {
            Log.d(this.LOG_TAG, "setCCAndTc Can't read country code from SIM");
            return;
        }
        String[] countryCodeInfo = sCountryCodes.get(countryCodeIso);
        if (countryCodeInfo == null) {
            String str = this.LOG_TAG;
            Log.d(str, "there is no country code info about this country: " + countryCodeIso);
            return;
        }
        String val = countryCodeInfo[0];
        String str2 = this.LOG_TAG;
        Log.d(str2, "country code " + val);
        if (!TextUtils.isEmpty(val)) {
            setCountryCode(COUNTRY_CODE_PREFIX.concat(val));
        }
        if (countryCodeInfo.length > 1) {
            String val2 = countryCodeInfo[1];
            String str3 = this.LOG_TAG;
            Log.d(str3, "country area code " + val2);
            if (!TextUtils.isEmpty(val2)) {
                setCountryAreaCode(val2);
            }
        }
    }

    public void updateSettings() {
        Log.d(this.LOG_TAG, "updateSettings");
        ImsRegistration[] regInfos = ImsRegistry.getRegistrationManager().getRegistrationInfo();
        int profilesCnt = regInfos.length;
        Log.d(this.LOG_TAG, "update CONFIGURATION_VALIDITY");
        setConfigValid(profilesCnt != 0);
        Log.d(this.LOG_TAG, "update MY_CONTACT_ID");
        for (ImsRegistration regInfo : regInfos) {
            for (NameAddr addr : regInfo.getImpuList()) {
                ImsUri uri = addr.getUri();
                String contactId = PhoneUtils.extractNumberFromUri(uri.toString());
                if (contactId != null) {
                    setMyContactId(contactId);
                    IMSLog.s(this.LOG_TAG, String.format("Load Number: %s from uri: %s", new Object[]{contactId, uri.toString()}));
                    return;
                }
            }
        }
    }

    public void updateTapiSettings() {
        IMSLog.s(this.LOG_TAG, "updateTapiSettings");
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, SimUtil.getDefaultPhoneId()) != 1) {
            z = false;
        }
        boolean isRcsEnable = z;
        String str = this.LOG_TAG;
        IMSLog.s(str, "check isRcsEnabled:" + isRcsEnable);
        setDefaultMessagingMethod(isRcsEnable ? CommonServiceConfiguration.MessagingMethod.AUTOMATIC : CommonServiceConfiguration.MessagingMethod.NON_RCS);
        setServiceActivated(isRcsEnable);
        writeBoolean("ServiceAvailable", isRcsEnable);
    }

    private void registerSIMListener() {
        SimManagerFactory.getSimManager().registerSimCardEventListener(new SimEventListener());
    }

    public static synchronized RcsSettingsUtils getInstance(Context ctx) {
        RcsSettingsUtils rcsSettingsUtils;
        synchronized (RcsSettingsUtils.class) {
            if (sInstance == null) {
                RcsSettingsUtils rcsSettingsUtils2 = new RcsSettingsUtils(ctx);
                sInstance = rcsSettingsUtils2;
                rcsSettingsUtils2.registerSIMListener();
            }
            rcsSettingsUtils = sInstance;
        }
        return rcsSettingsUtils;
    }

    public static RcsSettingsUtils getInstance() {
        return sInstance;
    }

    public void setServiceActivated(boolean serviceActivated) {
        writeBoolean("ServiceActivated", serviceActivated);
    }

    public void setConfigValid(boolean configValid) {
        writeBoolean("ConfigurationValidity", configValid);
    }

    public String getCountryCode() {
        return readParameter("MyCountryCode");
    }

    public String getCountryAreaCode() {
        return readParameter("CountryAreaCode");
    }

    public void setCountryCode(String countryCode) {
        writeParameter("MyCountryCode", countryCode);
    }

    public void setCountryAreaCode(String countryAreaCode) {
        writeParameter("CountryAreaCode", countryAreaCode);
    }

    public void setMyContactId(String contact) {
        writeParameter("MyContactId", contact);
    }

    public void setDefaultMessagingMethod(CommonServiceConfiguration.MessagingMethod method) {
        writeParameter("DefaultMessagingMethod", method.toString());
    }

    private class SimEventListener implements ISimEventListener {
        private SimEventListener() {
        }

        public void onReady(int subId, boolean absent) {
            String access$100 = RcsSettingsUtils.this.LOG_TAG;
            Log.d(access$100, "onReady: subId=" + subId + " absent=" + absent);
            if (!absent) {
                RcsSettingsUtils.this.loadCCAndAC();
                PhoneUtils.initialize();
            }
        }
    }
}
