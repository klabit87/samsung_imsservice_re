package com.sec.internal.ims.servicemodules.ss;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.XmlElement;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class UtUtils {
    public static final String DOMAIN_NAME = ".3gppnetwork.org";
    private static final String LOG_TAG = UtUtils.class.getSimpleName();
    private static final Pattern PATTERN_TEL_NUMBER = Pattern.compile("[+]?[#*\\-.()0-9]+");
    private static final Pattern PATTERN_WHITE_SPACES = Pattern.compile("\\s+");
    public static final String XCAP_DOMAIN_NAME = ".pub.3gppnetwork.org";
    public static final String XMLNS_CP = "urn:ietf:params:xml:ns:common-policy";
    public static final String XMLNS_SS = "http://uri.etsi.org/ngn/params/xml/simservs/xcap";

    public static XmlElement makeMultipleXml(CallForwardingData data, Mno mno) {
        XmlElement xml = new XmlElement(mno == Mno.VIVACOM_BULGARIA ? UtElement.ELEMENT_CF_SS : "communication-diversion").addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, CloudMessageProviderContract.JsonData.TRUE);
        if (mno == Mno.VIVACOM_BULGARIA) {
            xml.addAttribute("xmlns:ss", XMLNS_SS);
        }
        if (data.replyTimer > 0) {
            XmlElement noreplytimer = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER);
            if (mno == Mno.VIVACOM_BULGARIA) {
                noreplytimer.setNamespace("ss");
            }
            noreplytimer.setValue(Integer.toString(data.replyTimer));
            xml.addChildElement(noreplytimer);
        }
        XmlElement ruleset = new XmlElement(UtElement.ELEMENT_CP_RULE_SET);
        for (SsRuleData.SsRule rules : data.rules) {
            ruleset.addChildElement(makeSingleXml((CallForwardingData.Rule) rules, false, mno));
        }
        xml.addChildElement(ruleset);
        return xml;
    }

    public static XmlElement makeNoReplyTimerXml(int time, int phoneId) {
        XmlElement mainElement;
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.KDDI || mno == Mno.VIVACOM_BULGARIA) {
            mainElement = new XmlElement("ss:NoReplyTimer");
        } else {
            mainElement = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER);
        }
        mainElement.setValue(Integer.toString(time));
        return mainElement;
    }

    public static XmlElement makeMultipleXml(CallBarringData data, int type, Mno mno) {
        String name;
        if (type == 105) {
            name = mno == Mno.VIVACOM_BULGARIA ? UtElement.ELEMENT_OCB_SS : UtElement.ELEMENT_OCB;
        } else {
            name = mno == Mno.VIVACOM_BULGARIA ? UtElement.ELEMENT_ICB_SS : UtElement.ELEMENT_ICB;
        }
        XmlElement xml = new XmlElement(name).addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, CloudMessageProviderContract.JsonData.TRUE);
        if (mno == Mno.VIVACOM_BULGARIA) {
            xml.addAttribute("xmlns:ss", XMLNS_SS);
        }
        XmlElement ruleset = new XmlElement(UtElement.ELEMENT_CP_RULE_SET);
        if (mno == Mno.VIVACOM_BULGARIA) {
            ruleset.addAttribute("xmlns:cp", XMLNS_CP);
        }
        for (SsRuleData.SsRule rules : data.rules) {
            ruleset.addChildElement(makeSingleXml((CallBarringData.Rule) rules, mno));
        }
        xml.addChildElement(ruleset);
        return xml;
    }

    public static XmlElement makeSingleXml(String root, boolean active) {
        XmlElement mainElement = new XmlElement(root);
        mainElement.addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, active ? CloudMessageProviderContract.JsonData.TRUE : ConfigConstants.VALUE.INFO_COMPLETED);
        return mainElement;
    }

    public static XmlElement makeSingleXml(String root, int condition, boolean support_ss) {
        XmlElement mainElement = new XmlElement(root);
        XmlElement nodeElement = new XmlElement(UtElement.ELEMENT_DEFAULT_BEHAV);
        if (support_ss) {
            mainElement.setNamespace("ss");
            mainElement.addAttribute("xmlns:ss", XMLNS_SS);
            nodeElement.setNamespace("ss");
        }
        mainElement.addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, condition == 0 ? ConfigConstants.VALUE.INFO_COMPLETED : CloudMessageProviderContract.JsonData.TRUE);
        nodeElement.setValue(condition == 1 ? UtElement.ELEMENT_CLI_RESTRICTED : UtElement.ELEMENT_CLI_NOT_RESTRICTED);
        mainElement.addChildElement(nodeElement);
        return mainElement;
    }

    public static XmlElement makeSingleXml(CallForwardingData.Rule rules, boolean support_ss, Mno mno) {
        return makeSingleXml(rules, support_ss, mno, 0);
    }

    private static XmlElement setMediaElement(MEDIA media, Mno mno) {
        XmlElement xmlMedia = new XmlElement("media");
        if (mno == Mno.VIVACOM_BULGARIA) {
            xmlMedia.setNamespace("ss");
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA[media.ordinal()];
        if (i == 1) {
            xmlMedia.setValue(TMOConstants.CallLogTypes.AUDIO);
        } else if (i == 2) {
            xmlMedia.setValue(TMOConstants.CallLogTypes.VIDEO);
        }
        if (media != MEDIA.ALL) {
            return xmlMedia;
        }
        return null;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.ss.UtUtils$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA;

        static {
            int[] iArr = new int[MEDIA.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA = iArr;
            try {
                iArr[MEDIA.AUDIO.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA[MEDIA.VIDEO.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static XmlElement makeSingleXml(CallForwardingData.Rule rules, boolean support_ss, Mno mno, int timer) {
        XmlElement forward;
        XmlElement rule = new XmlElement(UtElement.ELEMENT_CP_RULE);
        rule.addAttribute("id", rules.ruleId);
        XmlElement condition = new XmlElement(UtElement.ELEMENT_CDT);
        if (!rules.conditions.state) {
            XmlElement deactivate = new XmlElement("rule-deactivated");
            if (support_ss) {
                deactivate.setNamespace("ss");
            }
            condition.addChildElement(deactivate);
        }
        String mCondition = doconvertCondition(rules.conditions.condition);
        if (!mCondition.isEmpty()) {
            XmlElement condition_xml = new XmlElement(mCondition);
            if (support_ss) {
                condition_xml.setNamespace("ss");
            }
            condition.addChildElement(condition_xml);
        }
        if (rules.conditions.media != null && rules.conditions.media.size() > 0) {
            for (MEDIA m : rules.conditions.media) {
                XmlElement xmlMedia = setMediaElement(m, mno);
                if (xmlMedia != null) {
                    condition.addChildElement(xmlMedia);
                }
            }
        }
        rule.addChildElement(condition);
        XmlElement actions = new XmlElement("cp:actions");
        XmlElement target = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.TARGET);
        if (support_ss) {
            forward = new XmlElement("ss:forward-to");
            target.setNamespace("ss");
        } else {
            forward = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.FORWARD_TO);
        }
        if (!TextUtils.isEmpty(rules.fwdElm.target)) {
            target.setValue(rules.fwdElm.target);
            forward.addChildElement(target);
        } else if (!rules.conditions.state && TextUtils.isEmpty(rules.fwdElm.target)) {
            if (mno != Mno.ATT) {
                forward.addChildElement(target);
            } else if (rules.conditions.action == 4) {
                forward.addChildElement(target);
            }
        }
        if (rules.fwdElm.fwdElm != null && rules.fwdElm.fwdElm.size() > 0) {
            for (int i = 0; i < rules.fwdElm.fwdElm.size(); i++) {
                XmlElement notify = new XmlElement(rules.fwdElm.fwdElm.get(i).id);
                notify.setValue(Boolean.toString(rules.fwdElm.fwdElm.get(i).status));
                forward.addChildElement(notify);
            }
        }
        actions.addChildElement(forward);
        if (timer > 0) {
            XmlElement noreplytimer = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER);
            if (support_ss) {
                noreplytimer.setNamespace("ss");
            }
            noreplytimer.setValue(Integer.toString(timer));
            actions.addChildElement(noreplytimer);
        }
        rule.addChildElement(actions);
        return rule;
    }

    public static XmlElement makeSingleXml(CallBarringData.Rule rules, Mno mno) {
        XmlElement rule = new XmlElement(UtElement.ELEMENT_CP_RULE);
        rule.addAttribute("id", rules.ruleId);
        XmlElement conditions = new XmlElement(UtElement.ELEMENT_CDT);
        String mCondition = doconvertCbCondition(rules.conditions.condition);
        if (!mCondition.isEmpty()) {
            if (rules.conditions.condition == 10 && mno == Mno.KDDI) {
                XmlElement identity = new XmlElement(UtElement.ELEMENT_IDENTITY);
                for (String uri : rules.target) {
                    XmlElement oneId = new XmlElement(UtElement.ELEMENT_ONE);
                    oneId.addAttribute("id", uri);
                    identity.addChildElement(oneId);
                }
                conditions.addChildElement(identity);
            } else {
                XmlElement condition = new XmlElement(mCondition);
                if (mno == Mno.VIVACOM_BULGARIA && !mCondition.startsWith("ss:") && !mCondition.startsWith("cp:")) {
                    condition.setNamespace("ss");
                }
                conditions.addChildElement(condition);
            }
        }
        if (!(rules.conditions.condition == 10 || rules.conditions.condition == 6 || rules.conditions.media == null || rules.conditions.media.size() <= 0)) {
            for (MEDIA m : rules.conditions.media) {
                XmlElement xmlMedia = setMediaElement(m, mno);
                if (xmlMedia != null) {
                    conditions.addChildElement(xmlMedia);
                }
            }
        }
        if (!rules.conditions.state) {
            XmlElement deactivate = new XmlElement("rule-deactivated");
            if (mno == Mno.VIVACOM_BULGARIA) {
                deactivate.setNamespace("ss");
            }
            conditions.addChildElement(deactivate);
        }
        XmlElement actions = new XmlElement("cp:actions");
        XmlElement allow = new XmlElement("allow");
        if (mno == Mno.KDDI || mno == Mno.VIVACOM_BULGARIA) {
            allow.setNamespace("ss");
        }
        allow.setValue(ConfigConstants.VALUE.INFO_COMPLETED);
        actions.addChildElement(allow);
        if (mno == Mno.VIVACOM_BULGARIA || mno == Mno.BATELCO_BAHRAIN || mno == Mno.WIND_GREECE || mno == Mno.CLARO_DOMINICAN) {
            for (ActionElm elm : rules.actions) {
                actions.addChildElement(new XmlElement(elm.name, elm.value));
            }
        }
        rule.addChildElement(conditions);
        rule.addChildElement(actions);
        return rule;
    }

    public static String doconvertCondition(int condition) {
        if (condition == 1) {
            return "busy";
        }
        if (condition == 2) {
            return "no-answer";
        }
        if (condition == 3) {
            return "not-reachable";
        }
        if (condition != 6) {
            return "";
        }
        return "not-registered";
    }

    public static String doconvertCbCondition(int condition) {
        String str = LOG_TAG;
        Log.i(str, "convertICBtype type :" + condition);
        if (condition == 3) {
            return "international";
        }
        if (condition == 4) {
            return "international-exHC";
        }
        if (condition == 5) {
            return "roaming";
        }
        if (condition == 6) {
            return "ss:anonymous";
        }
        if (condition != 10) {
            return "";
        }
        return UtElement.ELEMENT_IDENTITY;
    }

    public static int doconvertCBType(boolean action, int type) {
        switch (type) {
            case 1:
            case 5:
            case 6:
            case 9:
            case 10:
                return action ? 103 : 102;
            case 2:
            case 3:
            case 4:
            case 8:
                return action ? 105 : 104;
            case 7:
                return action ? 119 : 118;
            default:
                return 0;
        }
    }

    public static int doconvertMediaTypeToSsClass(List<MEDIA> list) {
        if (list == null) {
            return 255;
        }
        if (list.contains(MEDIA.VIDEO)) {
            return 16;
        }
        if (list.contains(MEDIA.AUDIO)) {
            return 1;
        }
        return 255;
    }

    public static MEDIA convertToMedia(int ssClass) {
        if (ssClass == 1) {
            return MEDIA.AUDIO;
        }
        if (ssClass != 16) {
            return MEDIA.ALL;
        }
        return MEDIA.VIDEO;
    }

    public static int convertCbTypeToBitMask(int cbType) {
        if (cbType == 1) {
            return 8;
        }
        if (cbType == 2) {
            return 1;
        }
        if (cbType == 3) {
            return 2;
        }
        if (cbType == 4) {
            return 4;
        }
        if (cbType == 5) {
            return 10;
        }
        Log.e(LOG_TAG, "unexpected cbType");
        return 0;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int doConvertIpVersion(java.lang.String r8) {
        /*
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "doConvertIpVersion type : "
            r1.append(r2)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            int r0 = r8.hashCode()
            r1 = 0
            r2 = 6
            r3 = 5
            r4 = 4
            r5 = 3
            r6 = 2
            r7 = 1
            switch(r0) {
                case -1937632495: goto L_0x0062;
                case -1937599096: goto L_0x0058;
                case -1935785453: goto L_0x004e;
                case -1935752054: goto L_0x0044;
                case -1181903067: goto L_0x003a;
                case 114167: goto L_0x002f;
                case 104588379: goto L_0x0025;
                default: goto L_0x0024;
            }
        L_0x0024:
            goto L_0x006c
        L_0x0025:
            java.lang.String r0 = "naptr"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r6
            goto L_0x006d
        L_0x002f:
            java.lang.String r0 = "srv"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r5
            goto L_0x006d
        L_0x003a:
            java.lang.String r0 = "ipv4v6"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r2
            goto L_0x006d
        L_0x0044:
            java.lang.String r0 = "ipv6pref"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r3
            goto L_0x006d
        L_0x004e:
            java.lang.String r0 = "ipv6only"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r7
            goto L_0x006d
        L_0x0058:
            java.lang.String r0 = "ipv4pref"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r4
            goto L_0x006d
        L_0x0062:
            java.lang.String r0 = "ipv4only"
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r1
            goto L_0x006d
        L_0x006c:
            r0 = -1
        L_0x006d:
            switch(r0) {
                case 0: goto L_0x0076;
                case 1: goto L_0x0075;
                case 2: goto L_0x0074;
                case 3: goto L_0x0073;
                case 4: goto L_0x0072;
                case 5: goto L_0x0071;
                case 6: goto L_0x0071;
                default: goto L_0x0070;
            }
        L_0x0070:
            return r1
        L_0x0071:
            return r2
        L_0x0072:
            return r3
        L_0x0073:
            return r4
        L_0x0074:
            return r5
        L_0x0075:
            return r6
        L_0x0076:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtUtils.doConvertIpVersion(java.lang.String):int");
    }

    private static String buildDomain(Mno mno, String defaultDomain, String operator) {
        if (!defaultDomain.endsWith("ims.mncXXX.mccXXX.pub.3gppnetwork.org")) {
            return defaultDomain;
        }
        String mcc = NSDSNamespaces.NSDSMigration.DEFAULT_KEY;
        String mnc = NSDSNamespaces.NSDSMigration.DEFAULT_KEY;
        if (!TextUtils.isEmpty(operator) && operator.length() >= 5) {
            mcc = operator.substring(0, 3);
            mnc = operator.substring(3);
            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            } else if (mnc.length() == 1) {
                mnc = "00" + mnc;
            }
        }
        return defaultDomain.replace("mncXXX", "mnc" + mnc).replace("mccXXX", "mcc" + mcc);
    }

    public static String getNAFDomain(Context context, int phoneId) {
        String xcapDomain;
        String domain;
        String defaultDomain = ImsRegistry.getString(phoneId, GlobalSettingsConstants.SS.AUTH_PROXY_IP, "");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null) {
            return defaultDomain;
        }
        Mno mno = sm.getSimMno();
        String operator = sm.getSimOperator();
        int enableGBA = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.SS.ENABLE_GBA, 0);
        if (sm.hasNoSim()) {
            return defaultDomain;
        }
        if (!TextUtils.isEmpty(defaultDomain) && (domain = buildDomain(mno, defaultDomain, operator)) != null) {
            return domain;
        }
        if (sm.hasIsim()) {
            String xcapDomain2 = sm.getImpi();
            if (xcapDomain2 == null) {
                return defaultDomain;
            }
            if (enableGBA == 1 && mno == Mno.BELL) {
                xcapDomain = "naf." + xcapDomain2.substring(xcapDomain2.indexOf(64) + 1);
                Log.i(LOG_TAG, "xcapDomain :" + xcapDomain);
            } else if (mno == Mno.CMCC) {
                xcapDomain = "xcap." + xcapDomain2.substring(xcapDomain2.indexOf(64) + 1);
                int foundMnc = xcapDomain.indexOf("mnc");
                if (foundMnc > 0) {
                    xcapDomain = xcapDomain.replace(xcapDomain.substring(foundMnc, foundMnc + 6), "mnc000");
                }
            } else {
                xcapDomain = "xcap." + xcapDomain2.substring(xcapDomain2.indexOf(64) + 1);
            }
            String xcapDomain3 = xcapDomain.toLowerCase(Locale.US);
            if (xcapDomain3.contains("3gppnetwork.org")) {
                return xcapDomain3.replace("3gppnetwork.org", "pub.3gppnetwork.org");
            }
            return xcapDomain3;
        } else if (operator == null || operator.length() < 5) {
            return defaultDomain;
        } else {
            try {
                String mcc = operator.substring(0, 3);
                String mnc = operator.substring(3);
                if (mno == Mno.CMCC) {
                    mnc = NSDSNamespaces.NSDSMigration.DEFAULT_KEY;
                } else if (mno == Mno.CTC) {
                    mcc = "460";
                    mnc = "011";
                } else if (mno == Mno.CTCMO) {
                    mcc = "455";
                    mnc = "007";
                }
                return "xcap.ims.mnc" + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(Integer.parseInt(mnc))}) + ".mcc" + mcc + XCAP_DOMAIN_NAME;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return defaultDomain;
            }
        }
    }

    public static String getBSFDomain(Context context, int phoneId) {
        String defaultBsfip = ImsRegistry.getString(phoneId, GlobalSettingsConstants.SS.BSF_IP, "");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null) {
            return defaultBsfip;
        }
        String operator = sm.getSimOperator();
        if (sm.hasNoSim() || !TextUtils.isEmpty(defaultBsfip)) {
            if (!defaultBsfip.endsWith("mncXXX.mccXXX.pub.3gppnetwork.org")) {
                return defaultBsfip;
            }
            String mcc = NSDSNamespaces.NSDSMigration.DEFAULT_KEY;
            String mnc = NSDSNamespaces.NSDSMigration.DEFAULT_KEY;
            if (!TextUtils.isEmpty(operator) && operator.length() >= 5) {
                mcc = operator.substring(0, 3);
                mnc = operator.substring(3);
                if (mnc.length() == 2) {
                    mnc = "0" + mnc;
                } else if (mnc.length() == 1) {
                    mnc = "00" + mnc;
                }
            }
            return defaultBsfip.replace("mncXXX", "mnc" + mnc).replace("mccXXX", "mcc" + mcc);
        } else if (sm.hasIsim()) {
            String impi = sm.getImpi();
            if (impi == null) {
                Log.e(LOG_TAG, "NULL IMPI received from SIM :: Returning DEFAULT BSFIP !!");
                return defaultBsfip;
            }
            int domainIndex = impi.indexOf(64);
            if (domainIndex <= 0 || domainIndex == impi.length()) {
                return defaultBsfip;
            }
            String impiDomain = impi.substring(domainIndex + 1).trim();
            if (impiDomain.endsWith(DOMAIN_NAME)) {
                int domainIndex2 = impiDomain.indexOf(DOMAIN_NAME);
                if (domainIndex2 <= 0) {
                    return defaultBsfip;
                }
                return "bsf." + impiDomain.substring(0, domainIndex2) + XCAP_DOMAIN_NAME;
            }
            return "bsf." + impiDomain;
        } else if (operator == null || operator.length() < 5) {
            return defaultBsfip;
        } else {
            try {
                String mcc2 = operator.substring(0, 3);
                String mnc2 = operator.substring(3);
                return "bsf.mnc" + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(Integer.parseInt(mnc2))}) + ".mcc" + mcc2 + XCAP_DOMAIN_NAME;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return defaultBsfip;
            }
        }
    }

    public static String generate3GPPDomain(ISimManager sm) {
        if (sm == null) {
            return null;
        }
        String operator = sm.getSimOperator();
        if (operator == null || operator.length() < 5) {
            Log.e(LOG_TAG, "Invalid operator.");
            return null;
        }
        try {
            String mcc = operator.substring(0, 3);
            String mnc = operator.substring(3);
            return "ims.mnc" + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(Integer.parseInt(mnc))}) + ".mcc" + mcc + DOMAIN_NAME;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNumberFromURI(String source) {
        String result = "";
        if (TextUtils.isEmpty(source)) {
            return result;
        }
        String source2 = PATTERN_WHITE_SPACES.matcher(source).replaceAll(result);
        if (PATTERN_TEL_NUMBER.matcher(source2).matches()) {
            return source2;
        }
        ImsUri imsUri = ImsUri.parse(source2);
        if (imsUri != null) {
            result = imsUri.getMsisdn();
        }
        if (result == null) {
            result = "";
        }
        if (!PATTERN_TEL_NUMBER.matcher(result).matches()) {
            return "";
        }
        return result;
    }

    public static String makeInternationNumber(String number, String prefix) {
        String phoneNumber = number;
        if (!phoneNumber.startsWith("0")) {
            return prefix + phoneNumber;
        }
        return prefix + phoneNumber.substring(1);
    }

    public static String removeUriPlusPrefix(String uri, Mno mno) {
        String trimUri = uri;
        if (mno == Mno.KDDI) {
            return removeUriPlusPrefix(uri, "+81", "0");
        }
        return trimUri;
    }

    private static String removeUriPlusPrefix(String uri, String prefix, String replace) {
        String trimUri = uri;
        if (uri == null || uri.length() < prefix.length() + 1 || !uri.startsWith(prefix)) {
            return trimUri;
        }
        return uri.replace(prefix, replace);
    }

    public static String getAcceptEncoding(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.H3G || mno == Mno.SMARTFREN || mno == Mno.TMOUS || mno == Mno.TELE2_RUSSIA) {
            return "";
        }
        return "*";
    }

    public static boolean isCallBarringType(int ssType) {
        if (ssType == 102 || ssType == 103 || ssType == 104 || ssType == 105) {
            return true;
        }
        return false;
    }

    public static String cleanBarringNum(String oldBarringNum) {
        if (oldBarringNum.toLowerCase().contains("hidden")) {
            return oldBarringNum;
        }
        return oldBarringNum.replaceAll("-", "");
    }

    public static boolean isBsfDisableTls(int phoneId) {
        ISimManager simManager = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (simManager == null) {
            return false;
        }
        if (simManager.getSimMno().isOneOf(Mno.AIS)) {
            return true;
        }
        return false;
    }
}
