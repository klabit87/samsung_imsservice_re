package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class RegistrationUtils {
    private static final String LOG_TAG = "RegiMgr-Utils";

    private RegistrationUtils() {
    }

    static ImsProfile[] getProfileList(int phoneId) {
        List<ImsProfile> registrationProfileList = SlotBasedConfig.getInstance(phoneId).getProfiles();
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) registrationProfileList)) {
            ImsProfile[] profiles = new ImsProfile[registrationProfileList.size()];
            registrationProfileList.toArray(profiles);
            return profiles;
        }
        Map<Integer, ImsProfile> extendedProfileList = SlotBasedConfig.getInstance(phoneId).getExtendedProfiles();
        if (CollectionUtils.isNullOrEmpty((Map<?, ?>) extendedProfileList)) {
            return new ImsProfile[0];
        }
        ImsProfile[] profiles2 = new ImsProfile[extendedProfileList.size()];
        int listsize = 0;
        for (Integer intValue : extendedProfileList.keySet()) {
            profiles2[listsize] = extendedProfileList.get(Integer.valueOf(intValue.intValue()));
            listsize++;
        }
        return profiles2;
    }

    static ImsRegistration[] getRegistrationInfoByPhoneId(int phoneId, ImsRegistration[] registrationInfo) {
        List<ImsRegistration> list = new ArrayList<>();
        for (ImsRegistration regInfo : registrationInfo) {
            if (regInfo.getPhoneId() == phoneId) {
                list.add(regInfo);
            }
        }
        if (CollectionUtils.isNullOrEmpty((Collection<?>) list)) {
            return null;
        }
        return (ImsRegistration[]) list.toArray(new ImsRegistration[0]);
    }

    static NetworkEvent getNetworkEvent(int phoneId) {
        NetworkEvent ret = SlotBasedConfig.getInstance(phoneId).getNetworkEvent();
        if (ret == null) {
            IMSLog.i(LOG_TAG, phoneId, "getNetworkEvent is not exist. Return null..");
        }
        return ret;
    }

    static ImsRegistration getRegistrationInfo(int phoneId, int profileId) {
        if (profileId >= 0) {
            return SlotBasedConfig.getInstance(phoneId).getImsRegistrations().get(Integer.valueOf(profileId));
        }
        Log.i(LOG_TAG, "invalid profileId : " + profileId);
        return null;
    }

    static boolean isMobileConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null) {
            return false;
        }
        Network[] networks = cm.getAllNetworks();
        int length = networks.length;
        int i = 0;
        while (i < length) {
            Network network = networks[i];
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            if (nc == null) {
                return false;
            }
            if (!nc.hasTransport(0) || !nc.hasCapability(12)) {
                i++;
            } else {
                NetworkInfo ni = cm.getNetworkInfo(network);
                if (ni == null) {
                    return false;
                }
                IMSLog.i(LOG_TAG, "isMobileConnected: " + ni);
                if (!ni.isAvailable() || !ni.isConnected()) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    static boolean hasVolteService(int phoneId, ImsProfile profile) {
        NetworkEvent ne = getNetworkEvent(phoneId);
        if (ne == null) {
            return false;
        }
        return ImsProfile.hasVolteService(profile, ne.network);
    }

    static boolean hasRcsService(int phoneId, ImsProfile profile) {
        NetworkEvent ne = SlotBasedConfig.getInstance(phoneId).getNetworkEvent();
        if (ne == null) {
            return false;
        }
        if ((profile.getPdnType() == -1 || profile.getPdnType() == 1) && ne.isWifiConnected) {
            return ImsProfile.hasRcsService(profile, ImsProfile.NETWORK_TYPE.WIFI);
        }
        return ImsProfile.hasRcsService(profile, ne.network);
    }

    static boolean hasRcsService(int phoneId, ImsProfile profile, boolean isWifiConnected) {
        if ((profile.getPdnType() == -1 || profile.getPdnType() == 1) && isWifiConnected) {
            return ImsProfile.hasRcsService(profile, ImsProfile.NETWORK_TYPE.WIFI);
        }
        return hasRcsService(phoneId, profile);
    }

    static boolean supportCsTty(IRegisterTask task) {
        int ttyType = task.getProfile().getTtyType();
        return ttyType == 1 || ttyType == 3;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: CodeShrinkVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Don't wrap MOVE or CONST insns: 0x0038: MOVE  (r1v3 'impu' java.lang.String) = (r6v0 'impuFromRcsPolicyManager' java.lang.String)
        	at jadx.core.dex.instructions.args.InsnArg.wrapArg(InsnArg.java:164)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.assignInline(CodeShrinkVisitor.java:133)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.checkInline(CodeShrinkVisitor.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:65)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:35)
        */
    static java.lang.String getPublicUserIdentity(com.sec.ims.settings.ImsProfile r4, int r5, java.lang.String r6, com.sec.internal.interfaces.ims.core.ISimManager r7) {
        /*
            java.lang.String r0 = "RegiMgr-Utils"
            if (r4 == 0) goto L_0x0030
            java.util.List r1 = r4.getImpuList()
            int r1 = r1.size()
            if (r1 <= 0) goto L_0x0030
            java.util.List r1 = r4.getImpuList()
            r2 = 0
            java.lang.Object r1 = r1.get(r2)
            java.lang.String r1 = (java.lang.String) r1
            if (r1 == 0) goto L_0x0030
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getPublicUserIdentity: impu from ImsProfile - "
            r2.append(r3)
            r2.append(r1)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r0, r5, r2)
            return r1
        L_0x0030:
            if (r4 == 0) goto L_0x0050
            boolean r1 = com.sec.ims.settings.ImsProfile.hasVolteService(r4)
            if (r1 != 0) goto L_0x0050
            r1 = r6
            if (r1 == 0) goto L_0x0050
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getPublicUserIdentity: impu from autoconfig - "
            r2.append(r3)
            r2.append(r1)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r0, r5, r2)
            return r1
        L_0x0050:
            java.lang.String r1 = r7.getImpuFromSim()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getPublicUserIdentity: impu from sim - "
            r2.append(r3)
            r2.append(r1)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r0, r5, r2)
            if (r4 == 0) goto L_0x007e
            java.lang.String r0 = r4.getMnoName()
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.fromName(r0)
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CABLE_PANAMA
            if (r0 == r2) goto L_0x007a
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.ORANGE_DOMINICANA
            if (r0 != r2) goto L_0x007e
        L_0x007a:
            java.lang.String r1 = r7.getDerivedImpu()
        L_0x007e:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.getPublicUserIdentity(com.sec.ims.settings.ImsProfile, int, java.lang.String, com.sec.internal.interfaces.ims.core.ISimManager):java.lang.String");
    }

    private static String getPrivateUserIdentityfromIsim(int phoneId, ITelephonyManager tm, ISimManager sm, Mno mno) {
        int subId = SimUtil.getSubId(phoneId);
        if (subId < 0) {
            return "";
        }
        String impi = tm.getIsimImpi(subId);
        if (TextUtils.isEmpty(impi)) {
            impi = sm.getDerivedImpi();
        }
        int i = 0;
        if (mno.isOneOf(Mno.EE, Mno.EE_ESN) || mno.isKor()) {
            boolean isImpuFound = false;
            String[] impuArray = tm.getIsimImpu(subId);
            String domain = tm.getIsimDomain(subId);
            if (impuArray != null) {
                int length = impuArray.length;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (!TextUtils.isEmpty(impuArray[i])) {
                        isImpuFound = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            IMSLog.i(LOG_TAG, phoneId, "getPrivateUserIdentity: MNO=" + mno + ", found impu=" + isImpuFound + ", domain=" + domain + ", impi=" + IMSLog.checker(impi));
            if (mno.isKor()) {
                if (!isImpuFound) {
                    return sm.getDerivedImpi();
                }
                return impi;
            } else if (!isImpuFound || TextUtils.isEmpty(domain) || TextUtils.isEmpty(impi)) {
                return sm.getDerivedImpi();
            } else {
                return impi;
            }
        } else {
            if (mno.isOneOf(Mno.CABLE_PANAMA, Mno.ORANGE_DOMINICANA)) {
                return sm.getDerivedImpi();
            }
            return impi;
        }
    }

    static String getPrivateUserIdentity(Context context, ImsProfile profile, int phoneId, ITelephonyManager tm, IRcsPolicyManager rpm, ISimManager sm) {
        String impi;
        int index;
        String impi2 = profile.getImpi();
        if (!TextUtils.isEmpty(impi2)) {
            IMSLog.s(LOG_TAG, phoneId, "impi=" + impi2);
            return impi2;
        }
        Mno mno = Mno.fromName(profile.getMnoName());
        if (mno == Mno.VZW && !ConfigUtil.isRcsOnly(profile)) {
            int subId = SimUtil.getSubId(phoneId);
            if (subId < 0) {
                return "";
            }
            String imsi = tm.getSubscriberId(subId);
            if (IsNonDirectRoamingCase(context, sm, tm)) {
                String impi3 = tm.getIsimImpi(subId);
                if (!TextUtils.isEmpty(impi3) && (index = impi3.indexOf("@")) > 0) {
                    imsi = impi3.substring(0, index);
                }
                IMSLog.e(LOG_TAG, phoneId, "IMPI from ISIM is empty");
            }
            String imsiBasedImpi = imsi + "@" + getHomeNetworkDomain(context, profile, phoneId, tm, rpm, sm);
            IMSLog.s(LOG_TAG, phoneId, "imsiBasedImpi=" + imsiBasedImpi);
            return imsiBasedImpi;
        } else if (sm == null) {
            return "";
        } else {
            if (sm.hasIsim()) {
                impi = getPrivateUserIdentityfromIsim(phoneId, tm, sm, mno);
            } else {
                impi = sm.getDerivedImpi();
            }
            if (!ImsProfile.hasVolteService(profile)) {
                impi = rpm.getRcsPrivateUserIdentity(impi, profile, phoneId);
            }
            IMSLog.s(LOG_TAG, phoneId, "impi=" + impi);
            return impi;
        }
    }

    private static boolean IsNonDirectRoamingCase(Context context, ISimManager sm, ITelephonyManager telephonyManager) {
        String operator;
        String gid;
        if (sm == null) {
            IMSLog.i(LOG_TAG, 0, "IsNonDirectRoamingCase, get operator from TelephonyManager.");
            operator = ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).getSimOperator();
            gid = telephonyManager.getGroupIdLevel1();
        } else {
            IMSLog.i(LOG_TAG, 0, "IsNonDirectRoamingCase, get operator from SimManager.");
            operator = sm.getSimOperator();
            int subId = sm.getSubscriptionId();
            String gid2 = telephonyManager.getGroupIdLevel1(subId);
            IMSLog.i(LOG_TAG, subId + "," + operator + "," + gid2);
            gid = gid2;
        }
        if (!TextUtils.equals(operator, "20404") || !"BAE0000000000000".equalsIgnoreCase(gid)) {
            return false;
        }
        return true;
    }

    static String getHomeNetworkDomain(Context ctx, ImsProfile profile, int phoneId, ITelephonyManager tm, IRcsPolicyManager rpm, ISimManager sm) {
        String efDomain = tm.getIsimDomain(SimUtil.getSubId(phoneId));
        String domain = null;
        Mno mno = Mno.fromName(profile.getMnoName());
        IMSLog.i(LOG_TAG, phoneId, "getHomeNetworkDomain: mno=" + mno.getName() + " EFDOMAIN=" + efDomain + " domain from profile=" + profile.getDomain());
        boolean isPcsfPrefManual = true;
        if (mno == Mno.VZW && !ConfigUtil.isRcsOnly(profile)) {
            if (profile.getPcscfPreference() != 2) {
                isPcsfPrefManual = false;
            }
            domain = (TextUtils.isEmpty(efDomain) || isPcsfPrefManual) ? profile.getDomain() : efDomain;
        } else if (profile.isSoftphoneEnabled() || profile.isSamsungMdmnEnabled()) {
            Iterator it = profile.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String impu = (String) it.next();
                if (!TextUtils.isEmpty(impu) && impu.indexOf("@") > 0 && impu.contains("sip")) {
                    domain = impu.substring(impu.indexOf("@") + 1);
                    break;
                }
            }
        } else if (mno == Mno.GCF && !TextUtils.isEmpty(efDomain)) {
            return efDomain;
        } else {
            domain = TextUtils.isEmpty(profile.getDomain()) ? rpm.getRcsHomeNetworkDomain(profile, phoneId) : profile.getDomain();
            if (TextUtils.isEmpty(domain)) {
                domain = efDomain;
            }
        }
        String domain2 = isDerivedDomainFromImsiRequired(ctx, mno, profile, sm, phoneId, domain);
        IMSLog.i(LOG_TAG, phoneId, "getHomeNetworkDomain: domain=" + domain2);
        return domain2.replaceAll("[^\\x20-\\x7E]", "");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004e, code lost:
        if (r10.isOneOf(com.sec.internal.constants.Mno.H3G_DK, com.sec.internal.constants.Mno.H3G_SE, com.sec.internal.constants.Mno.METEOR_IRELAND, com.sec.internal.constants.Mno.EE, com.sec.internal.constants.Mno.EE_ESN, com.sec.internal.constants.Mno.VINAPHONE) != false) goto L_0x0050;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String isDerivedDomainFromImsiRequired(android.content.Context r9, com.sec.internal.constants.Mno r10, com.sec.ims.settings.ImsProfile r11, com.sec.internal.interfaces.ims.core.ISimManager r12, int r13, java.lang.String r14) {
        /*
            r0 = 0
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((int) r13)
            r2 = 2
            r3 = 3
            r4 = 1
            r5 = 0
            if (r1 == 0) goto L_0x0011
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r11)
            if (r1 != 0) goto L_0x006f
        L_0x0011:
            boolean r1 = r11.isSamsungMdmnEnabled()
            if (r1 != 0) goto L_0x006f
            boolean r1 = r10.isChn()
            if (r1 == 0) goto L_0x0027
            java.lang.String r1 = r11.getRcsProfile()
            boolean r1 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r1)
            if (r1 == 0) goto L_0x0050
        L_0x0027:
            boolean r1 = r10.isLatin()
            if (r1 != 0) goto L_0x0050
            r1 = 6
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r1]
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.H3G_DK
            r1[r5] = r6
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.H3G_SE
            r1[r4] = r6
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.METEOR_IRELAND
            r1[r2] = r6
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.EE
            r1[r3] = r6
            r6 = 4
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.EE_ESN
            r1[r6] = r7
            r6 = 5
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.VINAPHONE
            r1[r6] = r7
            boolean r1 = r10.isOneOf(r1)
            if (r1 == 0) goto L_0x006f
        L_0x0050:
            java.lang.String r1 = com.sec.internal.ims.util.ConfigUtil.getAcsServerType(r9, r13)
            java.lang.String r6 = "jibe"
            boolean r1 = r6.equalsIgnoreCase(r1)
            if (r1 != 0) goto L_0x006f
            if (r12 == 0) goto L_0x006f
            boolean r1 = r12.hasIsim()
            if (r1 == 0) goto L_0x006e
            boolean r1 = r12.isISimDataValid()
            if (r1 == 0) goto L_0x006e
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CABLE_PANAMA
            if (r10 != r1) goto L_0x006f
        L_0x006e:
            r0 = 1
        L_0x006f:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.ORANGE_DOMINICANA
            if (r10 != r1) goto L_0x0074
            r0 = 1
        L_0x0074:
            boolean r1 = android.text.TextUtils.isEmpty(r14)
            if (r1 == 0) goto L_0x008e
            r0 = 1
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TWM
            if (r10 != r1) goto L_0x008e
            java.util.Locale r1 = java.util.Locale.US
            java.lang.Object[] r2 = new java.lang.Object[r4]
            java.lang.String r3 = "ims.taiwanmobile.com"
            r2[r5] = r3
            java.lang.String r3 = "%s"
            java.lang.String r1 = java.lang.String.format(r1, r3, r2)
            return r1
        L_0x008e:
            r1 = r14
            if (r0 == 0) goto L_0x00e6
            r6 = 0
            if (r12 == 0) goto L_0x00a1
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TMOUS
            if (r10 != r7) goto L_0x009d
            java.lang.String r6 = r12.getHighestPriorityEhplmn()
            goto L_0x00a1
        L_0x009d:
            java.lang.String r6 = r12.getSimOperator()
        L_0x00a1:
            boolean r7 = android.text.TextUtils.isEmpty(r6)
            if (r7 == 0) goto L_0x00aa
            java.lang.String r2 = ""
            return r2
        L_0x00aa:
            java.util.Locale r7 = java.util.Locale.US
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r8 = r6.substring(r3)
            int r8 = java.lang.Integer.parseInt(r8)
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            r2[r5] = r8
            java.lang.String r3 = r6.substring(r5, r3)
            int r3 = java.lang.Integer.parseInt(r3)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r2[r4] = r3
            java.lang.String r3 = "ims.mnc%03d.mcc%03d.3gppnetwork.org"
            java.lang.String r1 = java.lang.String.format(r7, r3, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getHomeNetworkDomain: Use derived domain - operator "
            r2.append(r3)
            r2.append(r6)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "RegiMgr-Utils"
            android.util.Log.i(r3, r2)
        L_0x00e6:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.isDerivedDomainFromImsiRequired(android.content.Context, com.sec.internal.constants.Mno, com.sec.ims.settings.ImsProfile, com.sec.internal.interfaces.ims.core.ISimManager, int, java.lang.String):java.lang.String");
    }

    static void saveRegisteredImpu(Context context, ImsRegistration reg, ISimManager sm) {
        IMSLog.i(LOG_TAG, reg.getPhoneId(), "saveRegisteredImpu:");
        if (sm != null) {
            if (!sm.isSimLoaded()) {
                Log.i(LOG_TAG, "SIM not Loaded");
                return;
            }
            Uri uri = Uri.parse("content://com.sec.ims.settings/impu");
            String imsi = sm.getImsi();
            ContentValues values = new ContentValues();
            values.put("imsi", imsi);
            values.put("impu", reg.getPreferredImpu().getUri().toString());
            values.put("timestamp", Long.valueOf(new Date().getTime()));
            context.getContentResolver().insert(uri, values);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0087, code lost:
        if (r9.isOneOf(com.sec.internal.constants.Mno.TELEFONICA_GERMANY, com.sec.internal.constants.Mno.TELEFONICA_SPAIN) != false) goto L_0x0089;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean hasVoLteSim(int r8, com.sec.internal.constants.Mno r9, com.sec.internal.ims.core.SlotBasedConfig.RegisterTaskList r10) {
        /*
            java.lang.String r0 = "RegiMgr-Utils"
            r1 = 0
            if (r9 == 0) goto L_0x009e
            if (r10 != 0) goto L_0x0009
            goto L_0x009e
        L_0x0009:
            com.sec.ims.settings.ImsProfile[] r2 = getProfileList(r8)
            boolean r3 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.lang.Object[]) r2)
            if (r3 == 0) goto L_0x0019
            java.lang.String r3 = "hasVoLteSim - no matched profile with SIM"
            com.sec.internal.log.IMSLog.i(r0, r8, r3)
            return r1
        L_0x0019:
            java.lang.String r3 = com.sec.internal.helper.OmcCode.get()
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.fromSalesCode(r3)
            boolean r4 = r3.isKor()
            r5 = 1
            if (r4 == 0) goto L_0x0029
            return r5
        L_0x0029:
            java.util.Iterator r4 = r10.iterator()
        L_0x002d:
            boolean r6 = r4.hasNext()
            if (r6 == 0) goto L_0x004e
            java.lang.Object r6 = r4.next()
            com.sec.internal.ims.core.RegisterTask r6 = (com.sec.internal.ims.core.RegisterTask) r6
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r7 = r6.getGovernor()
            boolean r7 = r7.isNonVoLteSimByPdnFail()
            if (r7 == 0) goto L_0x004d
            int r4 = r6.getPhoneId()
            java.lang.String r5 = "hasVoLteSim - Pdn rejected by network"
            com.sec.internal.log.IMSLog.i(r0, r4, r5)
            return r1
        L_0x004d:
            goto L_0x002d
        L_0x004e:
            r4 = 2
            com.sec.internal.constants.Mno[] r6 = new com.sec.internal.constants.Mno[r4]
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TELEFONICA_UK
            r6[r1] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TELEFONICA_UK_LAB
            r6[r5] = r7
            boolean r6 = r9.isOneOf(r6)
            if (r6 == 0) goto L_0x006f
            com.sec.internal.ims.core.SlotBasedConfig r6 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r8)
            boolean r6 = r6.getEntitlementNsds()
            if (r6 != 0) goto L_0x006f
            java.lang.String r4 = "hasVoLteSim - Entitlement is not ready"
            com.sec.internal.log.IMSLog.i(r0, r8, r4)
            return r1
        L_0x006f:
            com.sec.internal.constants.ims.os.NetworkEvent r6 = getNetworkEvent(r8)
            boolean r7 = r9.isNordic()
            if (r7 != 0) goto L_0x0089
            com.sec.internal.constants.Mno[] r4 = new com.sec.internal.constants.Mno[r4]
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TELEFONICA_GERMANY
            r4[r1] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TELEFONICA_SPAIN
            r4[r5] = r7
            boolean r4 = r9.isOneOf(r4)
            if (r4 == 0) goto L_0x009d
        L_0x0089:
            if (r6 == 0) goto L_0x009d
            com.sec.internal.constants.ims.os.VoPsIndication r4 = r6.voiceOverPs
            com.sec.internal.constants.ims.os.VoPsIndication r7 = com.sec.internal.constants.ims.os.VoPsIndication.NOT_SUPPORTED
            if (r4 != r7) goto L_0x009d
            int r4 = r6.network
            r7 = 13
            if (r4 != r7) goto L_0x009d
            java.lang.String r4 = "hasVoLteSim - VoPS not supported in LTE"
            com.sec.internal.log.IMSLog.i(r0, r8, r4)
            return r1
        L_0x009d:
            return r5
        L_0x009e:
            java.lang.String r2 = "hasVoLteSim - no mno or no task"
            com.sec.internal.log.IMSLog.i(r0, r8, r2)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.hasVoLteSim(int, com.sec.internal.constants.Mno, com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList):boolean");
    }

    static boolean hasLoadedProfile(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "hasLoadedProfile:");
        return !CollectionUtils.isNullOrEmpty((Collection<?>) SlotBasedConfig.getInstance(phoneId).getProfiles()) || !CollectionUtils.isNullOrEmpty((Map<?, ?>) SlotBasedConfig.getInstance(phoneId).getExtendedProfiles());
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0040 A[EDGE_INSN: B:19:0x0040->B:14:0x0040 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x001d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void setVoLTESupportProperty(boolean r5, int r6) {
        /*
            java.lang.String r0 = "0"
            boolean r1 = com.sec.internal.helper.SimUtil.isMultiSimSupported()
            if (r1 != 0) goto L_0x0009
            return
        L_0x0009:
            if (r5 != 0) goto L_0x0040
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r6)
            java.util.List r1 = r1.getProfiles()
            java.util.Iterator r2 = r1.iterator()
        L_0x0017:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0040
            java.lang.Object r3 = r2.next()
            com.sec.ims.settings.ImsProfile r3 = (com.sec.ims.settings.ImsProfile) r3
            java.lang.String r4 = "smsip"
            boolean r4 = r3.hasService(r4)
            if (r4 != 0) goto L_0x003e
            java.lang.String r4 = "mmtel"
            boolean r4 = r3.hasService(r4)
            if (r4 != 0) goto L_0x003e
            java.lang.String r4 = "mmtel-video"
            boolean r4 = r3.hasService(r4)
            if (r4 == 0) goto L_0x003d
            goto L_0x003e
        L_0x003d:
            goto L_0x0017
        L_0x003e:
            java.lang.String r0 = "1"
        L_0x0040:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "setVoLTESupportProperty: volteSupported ["
            r1.append(r2)
            r1.append(r0)
            java.lang.String r2 = "]"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "RegiMgr-Utils"
            com.sec.internal.log.IMSLog.i(r2, r6, r1)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "persist.sys.ims.supportmmtel"
            r1.append(r2)
            int r2 = r6 + 1
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.os.SemSystemProperties.set(r1, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.setVoLTESupportProperty(boolean, int):void");
    }

    static boolean hasSimMobilityProfile(int phoneId) {
        for (ImsProfile p : SlotBasedConfig.getInstance(phoneId).getProfiles()) {
            if (p.getSimMobility()) {
                return true;
            }
        }
        return false;
    }

    static boolean pendingHasEmergencyTask(int phoneId, Mno simMno) {
        SlotBasedConfig.RegisterTaskList rtl = getPendingRegistrationInternal(phoneId);
        if (rtl == null || simMno != Mno.VZW) {
            return false;
        }
        Iterator it = rtl.iterator();
        while (it.hasNext()) {
            if (((RegisterTask) it.next()).getProfile().hasEmergencySupport()) {
                return true;
            }
        }
        return false;
    }

    protected static SlotBasedConfig.RegisterTaskList getPendingRegistrationInternal(int phoneId) {
        if (phoneId >= 0 && phoneId < SimUtil.getPhoneCount()) {
            return SlotBasedConfig.getInstance(phoneId).getRegistrationTasks();
        }
        IMSLog.e(LOG_TAG, "getPendingRegistrationInternal : Invalid phoneId : " + phoneId);
        return null;
    }

    static int selectPdnType(ImsProfile profile, int rat) {
        int pdn = profile.getPdnType();
        if (pdn == -1) {
            pdn = rat == 18 ? 1 : 0;
        }
        if (SimUtil.isSoftphoneEnabled() && pdn == 0) {
            pdn = 5;
        }
        Log.i(LOG_TAG, "selectPdnType: rat=" + rat + "pdn=" + pdn);
        return pdn;
    }

    static boolean checkAusEmergencyCall(Mno devMno, int phoneId, ISimManager sm) {
        if (!devMno.isAus()) {
            return false;
        }
        if (sm.getSimMno().isAus() || ImsUtil.getSystemProperty("gsm.operator.numeric", phoneId, "00101").startsWith("505")) {
            return true;
        }
        return false;
    }

    static int getPhoneIdForStartConnectivity(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        if (task.getPdnType() == 0) {
            phoneId = SimUtil.getDefaultPhoneId();
        }
        IMSLog.i(LOG_TAG, phoneId, "getPhoneIdForStartConnectivity: task: " + task);
        return phoneId;
    }

    static void sendEmergencyRegistrationFailed(IRegisterTask task) {
        Log.i(LOG_TAG, "sendEmergencyRegistrationFailed");
        task.setState(RegistrationConstants.RegisterTaskState.EMERGENCY);
        if (task.getResultMessage() != null) {
            task.getResultMessage().sendToTarget();
            task.setResultMessage((Message) null);
            return;
        }
        Log.i(LOG_TAG, "sendEmergencyRegistrationFailed, mResult is NULL");
    }

    static boolean isCmcProfile(ImsProfile p) {
        return p.getCmcType() != 0;
    }

    static List<RegisterTask> getPriorityRegiedTask(boolean isHigh, IRegisterTask task) {
        int phoneId = task.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "getPriorityRegiedTask : isPriority  High? " + isHigh);
        List<RegisterTask> tasks = new ArrayList<>();
        if (isCmcProfile(task.getProfile())) {
            return tasks;
        }
        Iterator it = getPendingRegistrationInternal(task.getPhoneId()).iterator();
        while (it.hasNext()) {
            RegisterTask anotherTask = (RegisterTask) it.next();
            if (!isCmcProfile(anotherTask.getProfile())) {
                if (anotherTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                    Set<String> svcOfAnotherTask = anotherTask.getProfile().getAllServiceSetFromAllNetwork();
                    svcOfAnotherTask.retainAll(task.getProfile().getAllServiceSetFromAllNetwork());
                    if (!svcOfAnotherTask.isEmpty()) {
                        if (isHigh) {
                            if (anotherTask.getProfile().getPriority() > task.getProfile().getPriority()) {
                                tasks.add(anotherTask);
                            }
                        } else if (!anotherTask.getProfile().hasEmergencySupport() && anotherTask.getProfile().getPriority() < task.getProfile().getPriority()) {
                            tasks.add(anotherTask);
                        }
                    }
                }
                if (task.getMno() == Mno.RJIL && isHigh) {
                    int phoneId2 = anotherTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId2, "Profile is in = " + anotherTask.getState());
                    if (!anotherTask.getProfile().hasEmergencySupport() && anotherTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED && anotherTask.getProfile().getPriority() > task.getProfile().getPriority()) {
                        IMSLog.i(LOG_TAG, anotherTask.getPhoneId(), "Priority task is pending");
                        tasks.add(anotherTask);
                    }
                }
            }
        }
        return tasks;
    }

    static int getDetailedDeregiReason(int deregiReason) {
        if (deregiReason == 32) {
            return 71;
        }
        if (deregiReason != 33) {
            return 42;
        }
        return 81;
    }

    static String handleExceptionalMnoName(Mno mno, int phoneId, ISimManager sm) {
        IMSLog.i(LOG_TAG, phoneId, "handleExceptionalMnoName:");
        String mnoName = mno.getName();
        if (sm == null) {
            return "";
        }
        if (mno == Mno.ATT && sm.hasVsim()) {
            return mnoName + ":softphone";
        } else if (!checkAusEmergencyCall(mno, phoneId, sm)) {
            return mnoName;
        } else {
            String nwOperator = ImsUtil.getSystemProperty("gsm.operator.numeric", phoneId, "00101");
            IMSLog.i(LOG_TAG, phoneId, "handleExceptionalMnoName: nwOperator: " + nwOperator);
            if ("50502".equals(nwOperator)) {
                return Mno.OPTUS.getName();
            }
            if ("50501".equals(nwOperator) || "50571".equals(nwOperator) || "50572".equals(nwOperator)) {
                return Mno.TELSTRA.getName();
            }
            if ("50503".equals(nwOperator) || "50506".equals(nwOperator)) {
                return Mno.VODAFONE_AUSTRALIA.getName();
            }
            "50514".equals(nwOperator);
            return mnoName;
        }
    }

    static void replaceProfilesOnTask(RegisterTask task) {
        IMSLog.i(LOG_TAG, task.getPhoneId(), "ReplaceProfilesOnTask:");
        List<ImsProfile> profiles = SlotBasedConfig.getInstance(task.getPhoneId()).getProfiles();
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) profiles)) {
            for (ImsProfile profile : profiles) {
                if (task.getProfile().getId() == profile.getId()) {
                    task.setProfile(profile);
                }
            }
        }
    }

    static boolean needToNotifyImsReady(ImsProfile p, int phoneId) {
        if ((p.isSoftphoneEnabled() || (p.isSamsungMdmnEnabled() && p.getCmcType() == 0)) && !p.hasEmergencySupport()) {
            return true;
        }
        if (!isCmcSecondaryType(p.getCmcType())) {
            return false;
        }
        if (getPendingRegistrationInternal(phoneId).size() == 1) {
            return true;
        }
        return false;
    }

    static boolean isDelayDeRegForNonDDSOnFlightModeChanged(RegisterTask curTask) {
        if (!SimUtil.isDualIMS() || !SemSystemProperties.get("ro.boot.hardware", "").contains("qcom")) {
            return false;
        }
        boolean DualIMSRegistered = false;
        SlotBasedConfig.RegisterTaskList rtl = getPendingRegistrationInternal(SimUtil.getOppositeSimSlot(curTask.getPhoneId()));
        if (rtl == null) {
            return false;
        }
        Iterator it = rtl.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RegisterTask task = (RegisterTask) it.next();
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING) && !task.isRcsOnly()) {
                DualIMSRegistered = true;
                break;
            }
        }
        if (!DualIMSRegistered || curTask.getPhoneId() == SimUtil.getDefaultPhoneId()) {
            return false;
        }
        IMSLog.i(LOG_TAG, curTask.getPhoneId(), "isDelayDeRegForNonDDSOnFlightModeChanged : true");
        return true;
    }

    static boolean needToNotifyImsPhoneRegistration(ImsRegistration registration, boolean registered, boolean isSD) {
        ImsProfile p = registration.getImsProfile();
        int phoneId = registration.getPhoneId();
        boolean isCmcRegiNotify = isCmcProfile(p);
        if ((!isCmcRegiNotify && !isSD) || !ImsProfile.hasVolteService(p)) {
            return true;
        }
        if (isCmcPrimaryType(p.getCmcType())) {
            IMSLog.i(LOG_TAG, phoneId, "skip notify PD regi");
            return false;
        }
        if (isSD) {
            Iterator it = getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                boolean isCurTaskCmc = isCmcProfile(task.getProfile());
                boolean isCurTaskRegistered = task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED;
                boolean isCurMmtel = ImsProfile.hasVolteService(task.getProfile());
                IMSLog.i(LOG_TAG, phoneId, "needToNotifyImsPhoneRegistration: isCmcRegiNotify:" + isCmcRegiNotify + " regi:" + registered + " isCurCmc:" + isCurTaskCmc + " isCurTaskRegistered:" + isCurTaskRegistered + " isCurTaskMmtel:" + isCurMmtel);
                if (isCmcRegiNotify && !isCurTaskCmc && isCurTaskRegistered && isCurMmtel) {
                    return false;
                }
                if (!isCmcRegiNotify && !registered && isCurTaskCmc && isCurTaskRegistered && isCurMmtel) {
                    return false;
                }
            }
        }
        return true;
    }

    static Set<String> filterserviceFbe(Context context, Set<String> services, ImsProfile profile) {
        if (services == null) {
            return new HashSet();
        }
        Set<String> filteredServices = new HashSet<>(services);
        if (!DeviceUtil.isUserUnlocked(context)) {
            Log.i(LOG_TAG, "filterserviceFbe: rcsonly=" + ConfigUtil.isRcsOnly(profile));
            if (ConfigUtil.isRcsOnly(profile)) {
                return new HashSet();
            }
            for (String service : ImsProfile.getChatServiceList()) {
                filteredServices.remove(service);
            }
        }
        return filteredServices;
    }

    static void updateImsIcon(IRegisterTask task) {
        Optional.ofNullable(SlotBasedConfig.getInstance(task.getPhoneId()).getIconManager()).ifPresent(new Consumer() {
            public final void accept(Object obj) {
                RegistrationUtils.lambda$updateImsIcon$0(IRegisterTask.this, (ImsIconManager) obj);
            }
        });
    }

    static /* synthetic */ void lambda$updateImsIcon$0(IRegisterTask task, ImsIconManager mgr) {
        if (!task.getProfile().hasEmergencySupport()) {
            mgr.updateRegistrationIcon(task.isSuspended());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0076 A[EDGE_INSN: B:15:0x0076->B:11:0x0076 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x001c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void initRttMode(android.content.Context r6) {
        /*
            java.lang.String r0 = "RegiMgr-Utils"
            java.lang.String r1 = "initRttMode"
            android.util.Log.i(r0, r1)
            r1 = 0
        L_0x0008:
            int r2 = com.sec.internal.helper.SimUtil.getPhoneCount()
            if (r1 >= r2) goto L_0x0079
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r2 = getPendingRegistrationInternal(r1)
            java.util.Iterator r2 = r2.iterator()
        L_0x0016:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0076
            java.lang.Object r3 = r2.next()
            com.sec.internal.ims.core.RegisterTask r3 = (com.sec.internal.ims.core.RegisterTask) r3
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            int r4 = r4.getTtyType()
            r5 = 3
            if (r4 == r5) goto L_0x003a
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            int r4 = r4.getTtyType()
            r5 = 4
            if (r4 != r5) goto L_0x0039
            goto L_0x003a
        L_0x0039:
            goto L_0x0016
        L_0x003a:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "initRttMode : "
            r2.append(r4)
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            java.lang.String r4 = r4.getName()
            r2.append(r4)
            java.lang.String r4 = " : "
            r2.append(r4)
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            int r4 = r4.getTtyType()
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            com.sec.internal.ims.core.SlotBasedConfig r2 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r1)
            boolean r4 = com.sec.internal.ims.util.ImsUtil.isRttModeOnFromCallSettings(r6, r1)
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r4)
            r2.setRTTMode(r4)
        L_0x0076:
            int r1 = r1 + 1
            goto L_0x0008
        L_0x0079:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.initRttMode(android.content.Context):void");
    }

    public static int findBestNetwork(int phoneId, ImsProfile profile, IRegistrationGovernor governor, boolean isPdnConnected, PdnController pdnController, IVolteServiceModule vsm, int voiceNetworkType, Context context) {
        int i = phoneId;
        NetworkEvent ne = getNetworkEvent(phoneId);
        if (ne == null) {
            return 0;
        }
        Mno mno = Mno.fromName(profile.getMnoName());
        int pdn = profile.getPdnType();
        Set<Integer> networkSet = profile.getNetworkSet();
        if (profile.hasEmergencySupport()) {
            return findBestEmergencyNetwork(phoneId, mno, pdnController, vsm, ne, profile);
        }
        if (determineWifi(phoneId, mno, pdn, networkSet, pdnController, profile, governor, ne, context)) {
            return 18;
        }
        int rat = getAvailableMobileNetwork(phoneId, mno, pdnController, ne, profile, isPdnConnected, voiceNetworkType);
        String netTypeName = TelephonyManagerExt.getNetworkTypeName(rat);
        if (networkSet.contains(Integer.valueOf(rat))) {
            if (!profile.getServiceSet(Integer.valueOf(rat)).isEmpty()) {
                if (pdnController.isNetworkAvailable(rat, pdn, i) && !ne.outOfService && (!ne.isDataRoaming || governor.allowRoaming() || ne.network == 18)) {
                    IMSLog.i(LOG_TAG, i, "findBestNetwork: " + netTypeName);
                    return rat;
                }
                printFailReason(phoneId, profile, rat, pdn, ne, pdnController, governor.allowRoaming(), netTypeName);
                return 0;
            }
        } else {
            ImsProfile imsProfile = profile;
        }
        PdnController pdnController2 = pdnController;
        printFailReason(phoneId, profile, rat, pdn, ne, pdnController, governor.allowRoaming(), netTypeName);
        return 0;
    }

    private static int findBestEmergencyNetwork(int phoneId, Mno mno, PdnController pdnController, IVolteServiceModule vsm, NetworkEvent ne, ImsProfile profile) {
        int vowifiRat;
        if (mno == Mno.VZW || mno.isCanada()) {
            if (pdnController.isEpdgConnected(phoneId)) {
                return 18;
            }
            return 13;
        } else if (mno.isTw()) {
            int rat = ne.network;
            Set<Integer> networkSet = profile.getNetworkSet();
            IMSLog.i(LOG_TAG, phoneId, "current RAT : " + rat + " contains network in profile: " + networkSet.contains(Integer.valueOf(rat)) + ", hasEmergnecy option: " + profile.hasEmergencySupport());
            if (networkSet.contains(Integer.valueOf(rat))) {
                return rat;
            }
            return 13;
        } else if (profile.getCommercializedProfile() != 0 && vsm != null && (vowifiRat = vsm.getVoWIFIEmergencyCallRat(phoneId)) != -1) {
            return vowifiRat;
        } else {
            if (ne.network == 20) {
                return 20;
            }
            return 13;
        }
    }

    private static boolean determineWifi(int phoneId, Mno mno, int pdn, Set<Integer> networkSet, PdnController pdnController, ImsProfile profile, IRegistrationGovernor governor, NetworkEvent ne, Context context) {
        int i = phoneId;
        Mno mno2 = mno;
        int i2 = pdn;
        PdnController pdnController2 = pdnController;
        int preferredPdnType = pdnController2.translateNetworkBearer(pdnController.getDefaultNetworkBearer());
        boolean needWifiNetwork = true;
        if (!mno.isKor()) {
            NetworkEvent networkEvent = ne;
            Context context2 = context;
        } else if (!governor.isMobilePreferredForRcs()) {
            NetworkEvent networkEvent2 = ne;
            Context context3 = context;
            if (preferredPdnType != 1) {
                needWifiNetwork = false;
            }
        } else if (!NetworkUtil.isMobileDataOn(context)) {
            NetworkEvent networkEvent3 = ne;
            Context context4 = context;
        } else if (!NetworkUtil.isMobileDataPressed(context)) {
            NetworkEvent networkEvent4 = ne;
            Context context5 = context;
        } else if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(context, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
            NetworkEvent networkEvent5 = ne;
        } else if (!ne.outOfService && !governor.hasNetworkFailure()) {
            needWifiNetwork = false;
        }
        if (i2 != -1 && i2 != 1) {
            Set<Integer> set = networkSet;
            ImsProfile imsProfile = profile;
        } else if (!networkSet.contains(18)) {
            ImsProfile imsProfile2 = profile;
        } else if (!profile.getServiceSet(18).isEmpty() && pdnController.isWifiConnected() && needWifiNetwork && (!ConfigUtil.isRcsOnly(profile) || mno2 != Mno.TMOBILE || preferredPdnType == 1)) {
            IMSLog.i(LOG_TAG, phoneId, "findBestNetwork: WIFI needWifiNetwork = " + needWifiNetwork);
            return true;
        }
        if (i2 == ConnectivityManagerExt.TYPE_WIFI_P2P) {
            IMSLog.i(LOG_TAG, phoneId, "findBestNetwork: WIFI-P2P (Wifi-Direct or Mobile-HotSpot connected)");
            return true;
        } else if ((mno2 != Mno.VZW && mno2 != Mno.SPRINT) || !pdnController2.isEpdgConnected(phoneId) || !pdnController.isWifiConnected()) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "findBestNetwork: WIFI (ePDG connected)");
            return true;
        }
    }

    private static int getAvailableMobileNetwork(int phoneId, Mno mno, PdnController pdnController, NetworkEvent ne, ImsProfile profile, boolean isPdnConnected, int voiceNetworkType) {
        int rat = ne.network;
        if (rat == 18 && !profile.isEpdgSupported()) {
            rat = NetworkEvent.blurNetworkType(voiceNetworkType);
        }
        if (!mno.isOneOf(Mno.RJIL, Mno.ORANGE_POLAND) || !isPdnConnected || rat != 18) {
            return rat;
        }
        if (pdnController.isEpdgConnected(phoneId) && pdnController.isWifiConnected()) {
            return rat;
        }
        IMSLog.i(LOG_TAG, "findBestNetwork: pdn is connected, but epdg is not connected");
        return NetworkEvent.blurNetworkType(voiceNetworkType);
    }

    private static void printFailReason(int phoneId, ImsProfile profile, int rat, int pdn, NetworkEvent ne, PdnController pdnController, boolean roamingAllowed, String netTypeName) {
        String reason = "";
        if (profile.getServiceSet(Integer.valueOf(rat)).isEmpty()) {
            reason = reason + " - serviceSet empty";
        }
        if (!pdnController.isNetworkAvailable(rat, pdn, phoneId)) {
            reason = reason + " - NetworkAvailable: false";
        }
        if (ne.outOfService) {
            reason = reason + " - OOS: true";
        }
        if (ne.isDataRoaming && !roamingAllowed && ne.network != 18) {
            reason = reason + "- Roaming not allowed";
        }
        if ("".equals(reason)) {
            reason = reason + " - networkSet empty";
        }
        IMSLog.i(LOG_TAG, phoneId, "Not found best network in " + netTypeName + reason);
    }

    public static boolean ignoreSendDeregister(int phoneId, Mno mno, RegisterTask task, int reason) {
        if ((reason == 3 && task.getPdnType() == 11) || (reason == 4 && task.getPdnType() != 11)) {
            Log.i(LOG_TAG, "Not matched pdn type. reason: " + reason + ",pdnType: " + task.getPdnType());
            return true;
        } else if (reason == 124 && !task.getProfile().isEpdgSupported()) {
            Log.i(LOG_TAG, "Ignore Epdg deregister due to not support epdg profile : " + task.getProfile().getName());
            return true;
        } else if (mno == Mno.CMCC && task.isRcsOnly() && (reason == 4 || reason == 3 || reason == 1)) {
            Log.i(LOG_TAG, "sendDeregister : 4 or 1: RCS not needed");
            return true;
        } else {
            int rat = getNetworkEvent(phoneId).network;
            if (mno == Mno.DOCOMO && rat != 13 && (reason == 4 || reason == 3)) {
                Log.i(LOG_TAG, "sendDeregister : DCM doesn't need to handle this on 3G");
                return true;
            } else if (reason != 143) {
                return false;
            } else {
                if (!task.isRcsOnly() || !task.getProfile().getNeedAutoconfig()) {
                    return true;
                }
                task.setReason("FORCE SMS PUSH");
                return false;
            }
        }
    }

    public static List<String> retrievePcscfByProfileSettings(IRegisterTask task, PdnController pdnController, IRcsPolicyManager policyManager, String[] pcscfFromAcsIsim) {
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        int pref = profile.getPcscfPreference();
        List<String> pcscfList = new ArrayList<>();
        if (pref == 0 || (pref == 4 && !task.isRcsOnly())) {
            pcscfList = pdnController.readPcscfFromLinkProperties(pdnController.getLinkProperties(task));
            if (task.getMno() == Mno.KT && CollectionUtils.isNullOrEmpty((Collection<?>) pcscfList)) {
                IMSLog.i(LOG_TAG, phoneId, "getPcscfAddresses: test pcscfList invalid call retryDNSQuery");
                task.getGovernor().retryDNSQuery();
            }
        } else if (pref == 3 || pref == 4) {
            pcscfList.add(policyManager.getRcsPcscfAddress(task.getProfile(), task.getPhoneId()));
        } else if (pref == 5) {
            pcscfList = retrievePcscfViaOmadm(task, pdnController);
        } else if (pref == 2) {
            pcscfList = profile.getPcscfList();
            if (pcscfList.size() == 0) {
                IMSLog.e(LOG_TAG, phoneId, "getPcscfAddress: No P-CSCF address found in profile " + profile.getName());
                return null;
            }
        } else if (pref == 1) {
            pcscfList = new ArrayList<>(Arrays.asList(pcscfFromAcsIsim));
        }
        IMSLog.i(LOG_TAG, phoneId, "getPcscfAddress: " + pcscfList);
        return pcscfList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0097 A[EDGE_INSN: B:23:0x0097->B:16:0x0097 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.util.List<java.lang.String> retrievePcscfViaOmadm(com.sec.internal.interfaces.ims.core.IRegisterTask r8, com.sec.internal.ims.core.PdnController r9) {
        /*
            int r0 = r8.getPhoneId()
            com.sec.ims.settings.ImsProfile r1 = r8.getProfile()
            java.lang.String r2 = r1.getMnoName()
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.fromName(r2)
            boolean r2 = r2.isKor()
            if (r2 == 0) goto L_0x00a7
            boolean r2 = r1.hasEmergencySupport()
            if (r2 == 0) goto L_0x00a7
            boolean r2 = r1.hasEmergencySupport()
            if (r2 == 0) goto L_0x00a2
            com.sec.internal.helper.os.LinkPropertiesWrapper r2 = r9.getLinkProperties(r8)
            java.util.List r2 = r9.readPcscfFromLinkProperties(r2)
            int r3 = r2.size()
            if (r3 != 0) goto L_0x00ab
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "getPcscfAddress: No P-CSCF address found in PCO "
            r3.append(r4)
            java.lang.String r4 = r1.getName()
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "RegiMgr-Utils"
            com.sec.internal.log.IMSLog.e(r4, r0, r3)
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r3 = getPendingRegistrationInternal(r0)
            java.util.Iterator r3 = r3.iterator()
        L_0x0052:
            boolean r5 = r3.hasNext()
            if (r5 == 0) goto L_0x0097
            java.lang.Object r5 = r3.next()
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            com.sec.ims.settings.ImsProfile r6 = r5.getProfile()
            boolean r6 = r6.hasEmergencySupport()
            if (r6 == 0) goto L_0x007a
            com.sec.ims.settings.ImsProfile r6 = r5.getProfile()
            java.lang.String r6 = r6.getName()
            java.lang.String r7 = "VoLTE"
            boolean r6 = r6.contains(r7)
            if (r6 == 0) goto L_0x0079
            goto L_0x007a
        L_0x0079:
            goto L_0x0052
        L_0x007a:
            com.sec.ims.settings.ImsProfile r3 = r5.getProfile()
            java.util.List r2 = r3.getPcscfList()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r6 = "getPcscfAddress: P-CSCF address found in VoLTE profile "
            r3.append(r6)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.e(r4, r0, r3)
        L_0x0097:
            int r3 = r2.size()
            if (r3 != 0) goto L_0x00ab
            java.util.List r2 = r1.getPcscfList()
            goto L_0x00ab
        L_0x00a2:
            java.util.List r2 = r1.getPcscfList()
            goto L_0x00ab
        L_0x00a7:
            java.util.List r2 = r1.getPcscfList()
        L_0x00ab:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.retrievePcscfViaOmadm(com.sec.internal.interfaces.ims.core.IRegisterTask, com.sec.internal.ims.core.PdnController):java.util.List");
    }

    static boolean isSatisfiedCarrierRequirement(int phoneId, ImsProfile profile, Mno profileMno, int gbaSupported, boolean isGBASupported) {
        if (profileMno == Mno.TMOUS && gbaSupported == 1 && !isGBASupported) {
            return false;
        }
        if (profileMno.isKor() && !ImsProfile.hasVolteService(profile)) {
            boolean supported = true;
            if (profileMno == Mno.SKT) {
                supported = OmcCode.isSKTOmcCode() || OmcCode.isKorOpenOmcCode();
            } else if (profileMno == Mno.KT) {
                supported = OmcCode.isKTTOmcCode() || OmcCode.isKorOpenOmcCode();
            } else if (profileMno == Mno.LGU) {
                supported = OmcCode.isLGTOmcCode() || OmcCode.isKorOpenOmcCode();
            }
            if (!supported) {
                IMSLog.i(LOG_TAG, phoneId, "buildTask: Not support device. skip RCS Profile.");
                return false;
            }
        }
        return true;
    }

    static boolean isCdmConfigured(IImsFramework imsFramework, int phoneId) {
        ICapabilityDiscoveryModule cdm = imsFramework.getServiceModuleManager().getCapabilityDiscoveryModule();
        return cdm == null || !cdm.isRunning() || cdm.isConfigured(phoneId);
    }

    static boolean determineUpdateRegistration(RegisterTask task, int previousRat, int rat, Set<String> oldSvc, Set<String> newSvc, boolean isForceReRegi) {
        int phoneId = task.getPhoneId();
        if (isForceReRegi) {
            IMSLog.i(LOG_TAG, phoneId, "updateRegistration: Force to do Re-register.");
            if (!"".equals(task.getReason())) {
                return true;
            }
            task.setReason("service changed by user");
            return true;
        } else if (task.getProfile().getReregiOnRatChange() == 0 || (task.getProfile().getReregiOnRatChange() == 1 && rat != previousRat)) {
            if (!(task.getImsRegistration() == null || rat == 18 || previousRat == 18)) {
                task.getImsRegistration().setCurrentRat(rat);
            }
            IMSLog.i(LOG_TAG, phoneId, "updateRegistration: no need to re-register due to the policy");
            return false;
        } else if (!newSvc.equals(oldSvc)) {
            if ("mobile data changed : 0".equals(task.getReason()) && task.isRcsOnly() && task.getMno() == Mno.CMCC) {
                return false;
            }
            IMSLog.i(LOG_TAG, phoneId, "updateRegistration: service has changed. Re-register.");
            task.setReason("service has changed");
            return true;
        } else if (!newSvc.equals(oldSvc)) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "updateRegistration: Same services. No need to re-register.");
            return false;
        }
    }

    public static InetAddress[] getAllByNameWithThread(IRegisterTask task, String host) throws UnknownHostException {
        int phoneId = task.getPhoneId();
        Network network = task.getNetworkConnected();
        try {
            long timeOut = System.currentTimeMillis() + 5000;
            LinkedList<InetAddress> retAddress = new LinkedList<>();
            AtomicBoolean failed = new AtomicBoolean(false);
            Thread t = new Thread(new Runnable(phoneId, network, host, retAddress, failed) {
                public final /* synthetic */ int f$0;
                public final /* synthetic */ Network f$1;
                public final /* synthetic */ String f$2;
                public final /* synthetic */ LinkedList f$3;
                public final /* synthetic */ AtomicBoolean f$4;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    RegistrationUtils.lambda$getAllByNameWithThread$1(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
            t.start();
            while (true) {
                if (System.currentTimeMillis() < timeOut) {
                    if (failed.get()) {
                        IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName: query failed");
                        break;
                    }
                    synchronized (retAddress) {
                        if (retAddress.size() > 0) {
                            IMSLog.s(LOG_TAG, phoneId, "getAllAddressByName: current result is " + retAddress);
                            InetAddress[] inetAddressArr = (InetAddress[]) retAddress.toArray(new InetAddress[retAddress.size()]);
                            return inetAddressArr;
                        }
                        try {
                            retAddress.wait(300);
                        } catch (Throwable th) {
                            IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName: wait failed");
                        }
                    }
                } else {
                    break;
                }
            }
            t.interrupt();
            IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName time out or failed");
            throw new UnknownHostException("cannot resolve host " + host);
        } catch (Throwable th2) {
            IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName: Unknown Host");
            throw new UnknownHostException("cannot resolve host " + host);
        }
    }

    static /* synthetic */ void lambda$getAllByNameWithThread$1(int phoneId, Network network, String host, LinkedList retAddress, AtomicBoolean failed) {
        InetAddress[] addresses;
        try {
            IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName: start runnable");
            if (network != null) {
                addresses = network.getAllByName(host);
            } else {
                addresses = InetAddress.getAllByName(host);
            }
            if (addresses != null && addresses.length > 0) {
                synchronized (retAddress) {
                    if (retAddress.size() <= 0) {
                        for (int i = 0; i < addresses.length; i++) {
                            retAddress.add(addresses[i]);
                            IMSLog.s(LOG_TAG, phoneId, "getAllAddressByName: getAllByName " + addresses[i]);
                        }
                        retAddress.notifyAll();
                    }
                }
            }
        } catch (Throwable e) {
            failed.set(true);
            e.printStackTrace();
        }
    }

    protected static boolean checkInitialRegistrationIsReady(RegisterTask task, List<IRegisterTask> rtl, boolean isApmOn, boolean isRoaming, boolean hasNoSIM, IRcsPolicyManager rcsPolicyManager, RegistrationManagerHandler regHandler) {
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        if (!profile.isNetworkEnabled(18) && isApmOn) {
            IMSLog.i(LOG_TAG, task.getPhoneId(), "tryRegister: Airplane mode is on");
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.FLIGHT_MODE_ON.getCode());
            return false;
        } else if (task.getGovernor().isThrottled()) {
            int phoneId2 = task.getPhoneId();
            IMSLog.i(LOG_TAG, phoneId2, "tryRegister: task " + profile.getName() + " is throttled.");
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                long retry = task.getGovernor().getNextRetryMillis();
                if (retry > 0) {
                    int phoneId3 = task.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId3, "tryRegister: retry in " + retry + "ms.");
                    regHandler.sendTryRegister(phoneId, retry);
                    IMSLog.lazer((IRegisterTask) task, "NOT_TRIGGERED : THROTTLED : " + retry + "ms");
                }
            }
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.REGI_THROTTLED.getCode());
            return false;
        } else if (rcsPolicyManager.pendingRcsRegister(task, rtl, task.getPhoneId())) {
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.PENDING_RCS_REGI.getCode());
            return false;
        } else {
            if (!profile.hasEmergencySupport()) {
                if (supportCsTty(task) && SlotBasedConfig.getInstance(task.getPhoneId()).getTTYMode() && task.getMno() != Mno.VZW && !task.getMno().isKor()) {
                    int phoneId4 = task.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId4, "RegisterTask : TtyType=" + profile.getTtyType() + " mTTYMode=" + SlotBasedConfig.getInstance(task.getPhoneId()).getTTYMode());
                    task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.CS_TTY.getCode());
                    return false;
                } else if (!getPriorityRegiedTask(true, task).isEmpty()) {
                    IMSLog.i(LOG_TAG, task.getPhoneId(), "checkHigherPriorityRegiedTask != null");
                    task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.HIGHER_PRIORITY.getCode());
                    return false;
                } else {
                    if (task.getMno().isOneOf(Mno.TELEFONICA_UK, Mno.TELEFONICA_UK_LAB) && !hasNoSIM && !task.isRcsOnly() && !SlotBasedConfig.getInstance(task.getPhoneId()).getEntitlementNsds()) {
                        IMSLog.i(LOG_TAG, task.getPhoneId(), "tryRegister: Entitlement is not ready");
                        task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ENTITLEMENT_NOT_READY.getCode());
                        return false;
                    } else if (!rcsPolicyManager.isRcsRoamingPref(task, isRoaming)) {
                        task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.RCS_ROAMING.getCode());
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public static boolean checkConfigForInitialRegistration(RegisterTask task, boolean isRcsAvailable, boolean isCdmConfigured, boolean isOmadmInProgress, boolean hasEmergencyCall, IRcsPolicyManager rcsPolicyManager, RegistrationManagerHandler regHandler, NetworkEventController netEvtController) {
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        if (rcsPolicyManager.tryRcsConfig(task)) {
            IMSLog.i(LOG_TAG, phoneId, "try RCS autoconfiguration");
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.TRY_RCS_CONFIG.getCode());
            return false;
        } else if (isRcsAvailable && profile.getNeedAutoconfig() && !isCdmConfigured && (!task.getMno().isKor() || task.getState() == RegistrationConstants.RegisterTaskState.CONNECTED)) {
            IMSLog.i(LOG_TAG, phoneId, "capability is not configured");
            regHandler.sendTryRegister(phoneId, 500);
            return false;
        } else if (!task.isNeedOmadmConfig() || !task.getGovernor().isOmadmConfigAvailable() || !isOmadmInProgress || (task.getMno().isKor() && task.getState() != RegistrationConstants.RegisterTaskState.CONNECTED)) {
            if (!profile.hasEmergencySupport()) {
                task.getGovernor().checkProfileUpdateFromDM(false);
            }
            if (task.getMno().isKor() && ConfigUtil.isRcsOnly(profile)) {
                task.getGovernor().checkAcsPcscfListChange();
            }
            if (task.getMno() != Mno.KDDI || !profile.hasEmergencySupport() || !hasEmergencyCall) {
                return true;
            }
            IMSLog.e(LOG_TAG, phoneId, "No Emergency Call is made,so dont try for Emergency Register");
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.KDDI_EMERGENCY.getCode());
            return false;
        } else {
            netEvtController.triggerOmadmConfig();
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DM_TRIGGERED.getCode());
            return false;
        }
    }

    static boolean needToSkipTryRegister(RegisterTask task, boolean needPendingRcsRegi, boolean hasNoSIM, boolean isDeregistering) {
        int phoneId = task.getPhoneId();
        if (!SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || phoneId == SimUtil.getDefaultPhoneId() || task.getProfile().getCmcType() != 0) {
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING, RegistrationConstants.RegisterTaskState.EMERGENCY) || needPendingRcsRegi) {
                return true;
            }
            if (task.getMno().isOneOf(Mno.TELEFONICA_UK, Mno.TELEFONICA_UK_LAB) && !hasNoSIM && !task.isRcsOnly() && !SlotBasedConfig.getInstance(phoneId).getEntitlementNsds()) {
                IMSLog.i(LOG_TAG, phoneId, "tryRegister: Entitlement is not ready");
                return true;
            } else if (!task.getProfile().hasEmergencySupport() && isDeregistering && !SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS())) {
                IMSLog.e(LOG_TAG, phoneId, "Deregistering is not completed");
                return true;
            } else if (task.getProfile().getEnableStatus() == 0) {
                IMSLog.i(LOG_TAG, phoneId, "tryRegister: profile is disabled. " + task.getProfile());
                return true;
            } else if (!task.isSuspended()) {
                return false;
            } else {
                IMSLog.i(LOG_TAG, phoneId, "tryRegister: suspened");
                return true;
            }
        } else {
            IMSLog.i(LOG_TAG, phoneId, "do not register for not dds sim");
            return true;
        }
    }

    static boolean isRcsRegistered(int phoneId, ImsRegistration[] registrationInfo) {
        ImsRegistration[] imsRegistrations = getRegistrationInfoByPhoneId(phoneId, registrationInfo);
        if (imsRegistrations != null) {
            for (ImsRegistration imsRegistration : imsRegistrations) {
                if (imsRegistration.hasRcsService()) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isCmcPrimaryType(int cmcType) {
        if (cmcType == 1 || cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    static boolean isCmcSecondaryType(int cmcType) {
        if (cmcType == 2 || cmcType == 4 || cmcType == 8) {
            return true;
        }
        return false;
    }

    static String replaceEnablerPlaceholderWithEnablerVersion(Context context, String rcsProfile, String sipUserAgent, int phoneId) {
        if (TextUtils.isEmpty(rcsProfile) || !sipUserAgent.contains("[ENABLER]")) {
            return sipUserAgent;
        }
        String upOmaEnablerVersion = getUpOmaEnablerVersion(rcsProfile, ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.UP_PROFILE, ""));
        if (!upOmaEnablerVersion.isEmpty()) {
            return sipUserAgent.replace("[ENABLER]", getImMsgTech(context, rcsProfile, phoneId) + "-client/" + upOmaEnablerVersion);
        }
        String sipUserAgent2 = sipUserAgent.replace("[ENABLER]", "IM-client/OMA1.0");
        Log.e(LOG_TAG, "replaceEnablerPlaceholderWithEnablerVersion: Cannot specify omaEnablerVersion for given rcs_profile and rcs_up_profile. Set enabler to IM-client/OMA1.0 as a default.");
        return sipUserAgent2;
    }

    private static String getImMsgTech(Context context, String rcsProfile, int phoneId) {
        String imMsgTech = RcsConfigurationHelper.getImMsgTech(context, rcsProfile, phoneId);
        if (ImConstants.ImMsgTech.SIMPLE_IM.toString().equals(imMsgTech)) {
            return "IM";
        }
        return imMsgTech;
    }

    private static String getUpOmaEnablerVersion(String rcsProfile, String precisedRcsUpProfile) {
        if (ImsProfile.isRcsUpTransitionProfile(precisedRcsUpProfile)) {
            return ImsConstants.OmaVersion.OMA_2_0;
        }
        if (ImsProfile.isRcsUp10Profile(rcsProfile)) {
            return ImsConstants.OmaVersion.OMA_2_1;
        }
        if (ImsProfile.isRcsUp2Profile(rcsProfile)) {
            return ImsConstants.OmaVersion.OMA_2_2;
        }
        return "";
    }
}
