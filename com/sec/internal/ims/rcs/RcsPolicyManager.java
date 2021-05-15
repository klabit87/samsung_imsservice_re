package com.sec.internal.ims.rcs;

import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.config.RcsConfig;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.DefaultRCSMnoStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.MnoStrategyCreator;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RcsPolicyManager extends Handler implements IRcsPolicyManager {
    private static final int EVENT_RCS_ALLOWED_CHANGED = 9;
    private static final int EVENT_RCS_POLICY_CHANGED = 10;
    private static final int EVENT_RCS_ROAMING_PREF = 8;
    private static final int EVT_SIM_READY = 0;
    private static final int EVT_SIM_REFRESH = 3;
    private static Map<Integer, IMnoStrategy> mRcsStrategy = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public static UriMatcher sUriMatcher;
    protected final Context context;
    private int mPcscfIdx = 0;
    private ContentObserver mRcsContentObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange, Uri uri) {
            int phoneId = SimUtil.getDefaultPhoneId();
            if (uri.getFragment() != null) {
                phoneId = UriUtil.getSimSlotFromUri(uri);
            }
            switch (RcsPolicyManager.sUriMatcher.match(uri)) {
                case 8:
                    RcsPolicyManager.this.onRcsRoamingPrefChanged(phoneId);
                    return;
                case 9:
                    RcsPolicyManager.this.onRCSAllowedChangedbyMDM();
                    return;
                case 10:
                    RcsPolicyManager.this.updateRcsStrategy(phoneId);
                    return;
                default:
                    return;
            }
        }
    };
    private IRegistrationManager mRegMgr;
    private List<ISimManager> mSimManagers;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        ImsConstants.SystemSettings.addUri(uriMatcher, ImsConstants.SystemSettings.RCS_ROAMING_PREF, 8);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.RCS_ALLOWED_URI, 9);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_GLOBAL, 10);
    }

    public RcsPolicyManager(Looper looper, Context context2, List simManagers) {
        super(looper);
        this.context = context2;
        this.mSimManagers = simManagers;
    }

    public void initSequentially() {
        for (ISimManager sm : this.mSimManagers) {
            sm.registerForSimReady(this, 0, (Object) null);
            sm.registerForSimRefresh(this, 3, (Object) null);
        }
        this.context.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.RCS_ROAMING_PREF.getUri(), false, this.mRcsContentObserver);
        this.context.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.RCS_ALLOWED_URI.getUri(), false, this.mRcsContentObserver);
        this.context.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.IMS_GLOBAL.getUri(), true, this.mRcsContentObserver);
        mRcsStrategy.clear();
        for (ISimManager sm2 : this.mSimManagers) {
            if (sm2 != null) {
                mRcsStrategy.put(Integer.valueOf(sm2.getSimSlotIndex()), new DefaultRCSMnoStrategy(this.context, sm2.getSimSlotIndex()));
            }
        }
    }

    public void setRegistrationManager(IRegistrationManager regMgr) {
        this.mRegMgr = regMgr;
    }

    public void handleMessage(Message msg) {
        Log.i(IRcsPolicyManager.LOG_TAG, "handleMessage:" + msg.what);
        int i = msg.what;
        if (i == 0 || i == 3) {
            updateRcsStrategy(((Integer) ((AsyncResult) msg.obj).result).intValue());
        }
    }

    /* access modifiers changed from: private */
    public void updateRcsStrategy(int phoneId) {
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm != null) {
            mRcsStrategy.put(Integer.valueOf(phoneId), MnoStrategyCreator.makeInstance(sm.getSimMno(), phoneId, this.context));
        }
    }

    public static IMnoStrategy getRcsStrategy(int phoneId) {
        return mRcsStrategy.get(Integer.valueOf(phoneId));
    }

    public static boolean loadRcsSettings(int phoneId, boolean forceReload) {
        return mRcsStrategy.get(Integer.valueOf(phoneId)).loadRcsSettings(forceReload);
    }

    public boolean pendingRcsRegister(IRegisterTask task, List<IRegisterTask> rtl, int phoneId) {
        IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "pendingRcsRegister: mDefaultPhoneId = " + SimUtil.getDefaultPhoneId());
        if ((ConfigUtil.isRcsEur(task.getMno()) || task.getMno() == Mno.CMCC) && task.isRcsOnly() && !RcsUtils.DualRcs.isDualRcsReg() && phoneId != SimUtil.getDefaultPhoneId()) {
            return true;
        }
        if ((task.getMno() == Mno.CMCC || task.getMno().isKor()) && task.isRcsOnly() && isWaitingRcsDeregister(task, rtl, task.getPhoneId())) {
            return true;
        }
        return false;
    }

    private boolean isWaitingRcsDeregister(IRegisterTask task, List<IRegisterTask> rtl, int phoneId) {
        for (IRegisterTask pendingTask : rtl) {
            if (pendingTask != task && pendingTask.isRcsOnly()) {
                if (pendingTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                    Log.i(IRcsPolicyManager.LOG_TAG, "isWaitingRcsDeregister: " + pendingTask.getProfile().getName() + "(" + pendingTask.getState() + ")");
                    return true;
                }
            }
        }
        return false;
    }

    public String getRcsPcscfAddress(ImsProfile profile, int phoneId) {
        IMSLog.d(IRcsPolicyManager.LOG_TAG, phoneId, "getRcsPcscfAddress:");
        String lboPcscfAddress = null;
        String ipType = null;
        if (Mno.fromName(profile.getMnoName()) == Mno.CMCC) {
            List<String> lboPcscfList = RcsConfigurationHelper.readListStringParam(this.context, ImsUtil.getPathWithPhoneId("address", phoneId));
            List<String> lboPcscfTypeList = RcsConfigurationHelper.readListStringParam(this.context, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE, phoneId));
            IMSLog.d(IRcsPolicyManager.LOG_TAG, phoneId, "getRcsPcscfAddress lboPcscfList:" + lboPcscfList + " lboPcscfTypeList:" + lboPcscfTypeList);
            if (!CollectionUtils.isNullOrEmpty((Collection<?>) lboPcscfList)) {
                if (this.mPcscfIdx >= lboPcscfList.size()) {
                    IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "getRcsPcscfAddress : reset pcscfIdx because lboPcscflist is changed");
                    this.mPcscfIdx = 0;
                }
                lboPcscfAddress = lboPcscfList.get(this.mPcscfIdx);
                ipType = lboPcscfTypeList.get(this.mPcscfIdx);
                IMSLog.d(IRcsPolicyManager.LOG_TAG, phoneId, "getRcsPcscfAddress mPcscfIdx:" + this.mPcscfIdx + " lboPcscfAddress:" + lboPcscfAddress + " ipType:" + ipType);
                this.mPcscfIdx = (this.mPcscfIdx + 1) % lboPcscfList.size();
            }
        } else {
            lboPcscfAddress = RcsConfigurationHelper.readStringParam(this.context, ImsUtil.getPathWithPhoneId("address", phoneId), (String) null);
            ipType = RcsConfigurationHelper.readStringParam(this.context, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE, phoneId), (String) null);
        }
        if (lboPcscfAddress == null) {
            return lboPcscfAddress;
        }
        int pos = lboPcscfAddress.indexOf(58);
        if (("ipv4".equalsIgnoreCase(ipType) || "IP Address".equalsIgnoreCase(ipType)) && pos > 0) {
            int port = Integer.parseInt(lboPcscfAddress.substring(pos + 1));
            profile.setSipPort(port);
            String lboPcscfAddress2 = lboPcscfAddress.substring(0, pos);
            IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "getPcscfAddresses: LBO-PCSCF=" + lboPcscfAddress2 + " port=" + port);
            return lboPcscfAddress2;
        } else if ("ipv6".equalsIgnoreCase(ipType)) {
            int bracketstart = lboPcscfAddress.indexOf(91);
            int bracketend = lboPcscfAddress.indexOf(93);
            int bracketendport = lboPcscfAddress.indexOf("]:");
            if (bracketendport > 0) {
                profile.setSipPort(Integer.parseInt(lboPcscfAddress.substring(bracketendport + 2)));
            }
            if (bracketstart == 0 && bracketend > 0) {
                lboPcscfAddress = lboPcscfAddress.substring(bracketstart + 1, bracketend);
            }
            IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "getPcscfAddresses: LBO-PCSCF=" + lboPcscfAddress + " port=" + profile.getSipPort());
            return lboPcscfAddress;
        } else if (!"FQDN".equalsIgnoreCase(ipType) || pos <= 0) {
            return lboPcscfAddress;
        } else {
            profile.setSipPort(Integer.parseInt(lboPcscfAddress.substring(pos + 1)));
            String lboPcscfAddress3 = lboPcscfAddress.substring(0, pos);
            IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "getPcscfAddresses: LBO-PCSCF=" + lboPcscfAddress3 + " port=" + profile.getSipPort());
            return lboPcscfAddress3;
        }
    }

    public String selectRcsDnsType(IRegisterTask task, List<String> dnses) {
        ImsProfile profile = task.getProfile();
        String rcsAs = ConfigUtil.getAcsServerType(this.context, task.getPhoneId());
        if ((!profile.getNeedIpv4Dns() && !ImsConstants.RCS_AS.JIBE.equals(rcsAs)) || dnses == null) {
            return "";
        }
        for (String dns : dnses) {
            if (NetworkUtil.isIPv4Address(dns)) {
                return "IPV4";
            }
        }
        return "";
    }

    public String selectRcsTransportType(IRegisterTask task, String defaulttransport) {
        String transport = defaulttransport;
        if (!task.isRcsOnly()) {
            return transport;
        }
        String transport2 = getRcsTransport(this.context, task.getPdnType(), task.getProfile(), task.getPhoneId());
        if ("udp-preferred".equals(transport2)) {
            transport2 = "udp";
        }
        return transport2.toUpperCase();
    }

    public String getRcsPrivateUserIdentity(String impi, ImsProfile profile, int phoneId) {
        if (Mno.fromName(profile.getMnoName()).isKor()) {
            return impi;
        }
        IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "RCS only");
        String rcsConfigImpi = RcsConfigurationHelper.readStringParam(this.context, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY, phoneId), (String) null);
        if (rcsConfigImpi == null) {
            return impi;
        }
        String impi2 = rcsConfigImpi;
        IMSLog.s(IRcsPolicyManager.LOG_TAG, phoneId, "impi: " + impi2);
        return impi2;
    }

    public String getRcsPublicUserIdentity(int phoneId) {
        return RcsConfigurationHelper.getImpu(this.context, phoneId);
    }

    public String getRcsHomeNetworkDomain(ImsProfile profile, int phoneId) {
        Mno mno = Mno.fromName(profile.getMnoName());
        IConfigModule cm = ImsRegistry.getConfigModule();
        if (((mno == Mno.ATT && !ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(ConfigUtil.getAcsServerType(this.context, phoneId))) || (mno == Mno.RJIL && ImsProfile.hasVolteService(profile, phoneId))) || !profile.getNeedAutoconfig() || !cm.isValidAcsVersion(phoneId)) {
            return "";
        }
        return RcsConfigurationHelper.readStringParam(this.context, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME, phoneId), (String) null);
    }

    public boolean isRcsRoamingPref(IRegisterTask task, boolean isRoaming) {
        int roamPref = UserConfiguration.getUserConfig(this.context, task.getPhoneId(), "rcs_roaming_pref", 2);
        Mno mno = task.getMno();
        NetworkEvent network = this.mRegMgr.getNetworkEvent(task.getPhoneId());
        if (network == null || ImsProfile.hasVolteService(task.getProfile(), network.network) || !ConfigUtil.isRcsEur(mno) || !isRoaming || roamPref != 0) {
            return true;
        }
        IMSLog.i(IRcsPolicyManager.LOG_TAG, task.getPhoneId(), "not allowed as per RCS preference");
        return false;
    }

    public void updateDualRcsPcscfIp(IRegisterTask task, List<String> ret) {
        if (task.isRcsOnly() && RcsUtils.DualRcs.isDualRcsReg()) {
            String curPcscfIp = task.getGovernor().getCurrentPcscfIp();
            boolean checkDualRcsPcscf = checkDualRcsPcscfIp(task);
            int phoneId = task.getPhoneId();
            IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "checkDualRcsPcscf: " + checkDualRcsPcscf + ", curPcscfIp: " + curPcscfIp);
            if (checkDualRcsPcscf) {
                task.getGovernor().increasePcscfIdx();
                if (ret != null) {
                    task.getGovernor().updatePcscfIpList(ret);
                }
            }
        }
    }

    public ImsUri.UriType getRcsNetworkUriType(int phoneId, String remoteUriType, boolean needAutoConfig) {
        return RcsConfigurationHelper.getNetworkUriType(this.context, remoteUriType, needAutoConfig, phoneId);
    }

    private boolean checkDualRcsPcscfIp(IRegisterTask task) {
        String curPcscfIp = task.getGovernor().getCurrentPcscfIp();
        int otherPhoneId = SimUtil.getOppositeSimSlot(task.getPhoneId());
        List<IRegisterTask> list = this.mRegMgr.getPendingRegistration(otherPhoneId);
        if (list == null) {
            return false;
        }
        for (IRegisterTask otherTask : list) {
            if (otherTask.isRcsOnly() && ((otherTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || otherTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) && RcsUtils.UiUtils.isSameRcsOperator(task.getProfile(), otherTask.getProfile()))) {
                String pcscf = otherTask.getGovernor().getCurrentPcscfIp();
                IMSLog.i(IRcsPolicyManager.LOG_TAG, otherPhoneId, "checkDualRcsPcscfIp: pcscf: " + pcscf);
                if (curPcscfIp.equals(pcscf)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String changeRcsIfacename(IRegisterTask task, IPdnController pdnController, String pcscf) {
        NetworkInterface netiface;
        try {
            if (NetworkUtil.isIPv4Address(pcscf)) {
                LinkPropertiesWrapper lp = pdnController.getLinkProperties(task);
                if (lp == null) {
                    Log.i(IRcsPolicyManager.LOG_TAG, "changeIfacename: LinkPropertiesWrapper null");
                    return null;
                }
                List<InetAddress> mLocalAddress = lp.getAllAddresses();
                if (mLocalAddress != null && !mLocalAddress.isEmpty()) {
                    for (InetAddress addr : mLocalAddress) {
                        if (NetworkUtil.isIPv4Address(addr.getHostAddress()) && (netiface = NetworkInterface.getByInetAddress(addr)) != null) {
                            String ret = netiface.getName();
                            Log.i(IRcsPolicyManager.LOG_TAG, "register: Change iface = " + ret);
                            return ret;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.d(IRcsPolicyManager.LOG_TAG, e.getMessage());
        }
        String ret2 = pdnController.getInterfaceName(task);
        Log.i(IRcsPolicyManager.LOG_TAG, "register: changeIfacename : no change - " + ret2);
        return ret2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006c, code lost:
        if (com.sec.internal.constants.ims.ImsConstants.RCS_AS.INTEROP.equals(r14) == false) goto L_0x0077;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x01e5 A[Catch:{ NumberFormatException -> 0x01e0, all -> 0x02d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00e9 A[SYNTHETIC, Splitter:B:51:0x00e9] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0178 A[Catch:{ all -> 0x02ac }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0197 A[Catch:{ NumberFormatException -> 0x01e0, all -> 0x02d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01a7 A[Catch:{ NumberFormatException -> 0x01e0, all -> 0x02d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01ab A[Catch:{ NumberFormatException -> 0x01e0, all -> 0x02d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01b3 A[Catch:{ NumberFormatException -> 0x01e0, all -> 0x02d9 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.os.Bundle getRcsConfigForUserAgent(com.sec.ims.settings.ImsProfile r23, com.sec.internal.constants.Mno r24, int r25, int r26) {
        /*
            r22 = this;
            r1 = r22
            r2 = r23
            r3 = r24
            r4 = r25
            r5 = r26
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            r6 = r0
            java.lang.String r7 = ""
            java.lang.String r8 = ""
            java.lang.String r9 = ""
            java.lang.String r10 = r23.getTransportName()
            java.lang.Class<com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager> r11 = com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager.class
            monitor-enter(r11)
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02ce }
            java.lang.String r12 = r23.getRcsProfile()     // Catch:{ all -> 0x02ce }
            java.lang.String r0 = com.sec.internal.ims.config.RcsConfigurationHelper.getImMsgTech((android.content.Context) r0, (java.lang.String) r12, (int) r5)     // Catch:{ all -> 0x02ce }
            r12 = 0
            int r13 = r23.getQValue()     // Catch:{ all -> 0x02ce }
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)     // Catch:{ all -> 0x02ce }
            android.content.Context r14 = r1.context     // Catch:{ all -> 0x02ce }
            java.lang.String r14 = com.sec.internal.ims.util.ConfigUtil.getAcsServerType(r14, r5)     // Catch:{ all -> 0x02ce }
            java.lang.String r15 = r23.getPassword()     // Catch:{ all -> 0x02ce }
            boolean r15 = android.text.TextUtils.isEmpty(r15)     // Catch:{ all -> 0x02ce }
            if (r15 == 0) goto L_0x00a3
            android.content.Context r15 = r1.context     // Catch:{ all -> 0x009c }
            r16 = r0
            java.lang.String r0 = "UserPwd"
            java.lang.String r0 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r0, r5)     // Catch:{ all -> 0x009c }
            r17 = r7
            java.lang.String r7 = r23.getPassword()     // Catch:{ all -> 0x0095 }
            java.lang.String r0 = com.sec.internal.ims.config.RcsConfigurationHelper.readStringParam(r15, r0, r7)     // Catch:{ all -> 0x0095 }
            r7 = r0
            java.lang.String r0 = "jibe"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x0090 }
            if (r0 != 0) goto L_0x006e
            java.lang.String r0 = "sec"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02d9 }
            if (r0 != 0) goto L_0x006e
            java.lang.String r0 = "interop"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02d9 }
            if (r0 == 0) goto L_0x0077
        L_0x006e:
            java.lang.String r0 = r23.getPassword()     // Catch:{ all -> 0x0090 }
            java.lang.String r0 = com.sec.internal.ims.util.ConfigUtil.decryptParam(r7, r0)     // Catch:{ all -> 0x0090 }
            r7 = r0
        L_0x0077:
            java.lang.String r0 = "RcsPolicyMgr"
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ all -> 0x0090 }
            r15.<init>()     // Catch:{ all -> 0x0090 }
            r18 = r8
            java.lang.String r8 = "getRcsConfigForUserAgent: Rcs Config password="
            r15.append(r8)     // Catch:{ all -> 0x00d2 }
            r15.append(r7)     // Catch:{ all -> 0x00d2 }
            java.lang.String r8 = r15.toString()     // Catch:{ all -> 0x00d2 }
            com.sec.internal.log.IMSLog.s(r0, r5, r8)     // Catch:{ all -> 0x00d2 }
            goto L_0x00c4
        L_0x0090:
            r0 = move-exception
            r18 = r8
            goto L_0x02d7
        L_0x0095:
            r0 = move-exception
            r18 = r8
            r7 = r17
            goto L_0x02d7
        L_0x009c:
            r0 = move-exception
            r17 = r7
            r18 = r8
            goto L_0x02d7
        L_0x00a3:
            r16 = r0
            r17 = r7
            r18 = r8
            java.lang.String r0 = r23.getPassword()     // Catch:{ all -> 0x02c4 }
            r7 = r0
            java.lang.String r0 = "RcsPolicyMgr"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x02bc }
            r8.<init>()     // Catch:{ all -> 0x02bc }
            java.lang.String r15 = "getRcsConfigForUserAgent: profile password="
            r8.append(r15)     // Catch:{ all -> 0x02bc }
            r8.append(r7)     // Catch:{ all -> 0x02bc }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x02bc }
            com.sec.internal.log.IMSLog.s(r0, r5, r8)     // Catch:{ all -> 0x02bc }
        L_0x00c4:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.RJIL     // Catch:{ all -> 0x02bc }
            if (r3 != r0) goto L_0x00d7
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r23)     // Catch:{ all -> 0x00d2 }
            if (r0 == 0) goto L_0x00cf
            goto L_0x00d7
        L_0x00cf:
            r8 = r18
            goto L_0x00e6
        L_0x00d2:
            r0 = move-exception
            r8 = r18
            goto L_0x02d7
        L_0x00d7:
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02bc }
            java.lang.String r8 = "realm"
            java.lang.String r8 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r8, r5)     // Catch:{ all -> 0x02bc }
            r15 = 0
            java.lang.String r0 = com.sec.internal.ims.config.RcsConfigurationHelper.readStringParam(r0, r8, r15)     // Catch:{ all -> 0x02bc }
            r8 = r0
        L_0x00e6:
            r0 = 1
            if (r4 != r0) goto L_0x0108
            android.content.Context r15 = r1.context     // Catch:{ all -> 0x0103 }
            java.lang.String r0 = "wifimedia"
            java.lang.String r0 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r0, r5)     // Catch:{ all -> 0x0103 }
            r19 = r9
            java.lang.String r9 = "MSRPoTLS"
            java.lang.String r0 = com.sec.internal.ims.config.RcsConfigurationHelper.readStringParam(r15, r0, r9)     // Catch:{ all -> 0x00fe }
            r9 = r0
            r20 = r10
            goto L_0x0157
        L_0x00fe:
            r0 = move-exception
            r9 = r19
            goto L_0x02d7
        L_0x0103:
            r0 = move-exception
            r19 = r9
            goto L_0x02d7
        L_0x0108:
            r19 = r9
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02b6 }
            java.lang.String r9 = "phone"
            java.lang.Object r0 = r0.getSystemService(r9)     // Catch:{ all -> 0x02b6 }
            android.telephony.TelephonyManager r0 = (android.telephony.TelephonyManager) r0     // Catch:{ all -> 0x02b6 }
            android.content.Context r9 = r1.context     // Catch:{ all -> 0x02b6 }
            java.lang.String r15 = "psmedia"
            java.lang.String r15 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r15, r5)     // Catch:{ all -> 0x02b6 }
            r20 = r10
            java.lang.String r10 = "MSRP"
            java.lang.String r9 = com.sec.internal.ims.config.RcsConfigurationHelper.readStringParam(r9, r15, r10)     // Catch:{ all -> 0x02b0 }
            r10 = r9
            android.content.Context r15 = r1.context     // Catch:{ all -> 0x02ac }
            java.lang.String r15 = com.sec.internal.ims.util.ConfigUtil.getRcsProfileWithFeature(r15, r5, r2)     // Catch:{ all -> 0x02ac }
            boolean r15 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r15)     // Catch:{ all -> 0x02ac }
            if (r15 == 0) goto L_0x0155
            boolean r15 = r0.isNetworkRoaming()     // Catch:{ all -> 0x02ac }
            if (r15 == 0) goto L_0x0152
            java.lang.String r15 = "jibe"
            boolean r15 = r15.equals(r14)     // Catch:{ all -> 0x02ac }
            if (r15 != 0) goto L_0x0152
            android.content.Context r15 = r1.context     // Catch:{ all -> 0x02ac }
            r18 = r0
            java.lang.String r0 = "psmediaroaming"
            java.lang.String r0 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r0, r5)     // Catch:{ all -> 0x02ac }
            java.lang.String r0 = com.sec.internal.ims.config.RcsConfigurationHelper.readStringParam(r15, r0, r10)     // Catch:{ all -> 0x02ac }
            r9 = r0
            goto L_0x0157
        L_0x0152:
            r18 = r0
            goto L_0x0157
        L_0x0155:
            r18 = r0
        L_0x0157:
            java.lang.String r0 = "RcsPolicyMgr"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x02ac }
            r10.<init>()     // Catch:{ all -> 0x02ac }
            java.lang.String r15 = "msrpTransType=("
            r10.append(r15)     // Catch:{ all -> 0x02ac }
            r10.append(r9)     // Catch:{ all -> 0x02ac }
            java.lang.String r15 = ")"
            r10.append(r15)     // Catch:{ all -> 0x02ac }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x02ac }
            com.sec.internal.log.IMSLog.s(r0, r5, r10)     // Catch:{ all -> 0x02ac }
            boolean r0 = com.sec.ims.settings.ImsProfile.hasVolteService(r23)     // Catch:{ all -> 0x02ac }
            if (r0 != 0) goto L_0x0197
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02ac }
            java.lang.String r0 = getRcsTransport(r0, r4, r2, r5)     // Catch:{ all -> 0x02ac }
            r10 = r0
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02d9 }
            java.lang.String r15 = "keep_alive_enabled"
            java.lang.String r15 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r15, r5)     // Catch:{ all -> 0x02d9 }
            r17 = 1
            java.lang.Boolean r2 = java.lang.Boolean.valueOf(r17)     // Catch:{ all -> 0x02d9 }
            java.lang.Boolean r0 = com.sec.internal.ims.config.RcsConfigurationHelper.readBoolParam(r0, r15, r2)     // Catch:{ all -> 0x02d9 }
            boolean r0 = r0.booleanValue()     // Catch:{ all -> 0x02d9 }
            r12 = r0
            goto L_0x0199
        L_0x0197:
            r10 = r20
        L_0x0199:
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02d9 }
            r2 = 0
            int r0 = com.sec.internal.ims.util.ConfigUtil.getAutoconfigSourceWithFeature(r0, r5, r2)     // Catch:{ all -> 0x02d9 }
            r2 = r0
            if (r2 != 0) goto L_0x01ab
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.CMCC     // Catch:{ all -> 0x02d9 }
            if (r3 != r0) goto L_0x01ab
            java.lang.String r0 = "CPM"
            r15 = r0
            goto L_0x01ad
        L_0x01ab:
            r15 = r16
        L_0x01ad:
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r23)     // Catch:{ all -> 0x02d9 }
            if (r0 == 0) goto L_0x01e5
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02d9 }
            r16 = r2
            java.lang.String r2 = "q-value"
            java.lang.String r2 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r2, r5)     // Catch:{ all -> 0x02d9 }
            java.lang.String r0 = com.sec.internal.ims.config.RcsConfigurationHelper.readStringParam(r0, r2)     // Catch:{ all -> 0x02d9 }
            r2 = r0
            boolean r0 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x02d9 }
            if (r0 != 0) goto L_0x01e7
            float r0 = java.lang.Float.parseFloat(r2)     // Catch:{ NumberFormatException -> 0x01e0 }
            r17 = 1148846080(0x447a0000, float:1000.0)
            float r0 = r0 * r17
            java.lang.Float r0 = java.lang.Float.valueOf(r0)     // Catch:{ NumberFormatException -> 0x01e0 }
            int r17 = r0.intValue()     // Catch:{ NumberFormatException -> 0x01e0 }
            java.lang.Integer r17 = java.lang.Integer.valueOf(r17)     // Catch:{ NumberFormatException -> 0x01e0 }
            r13 = r17
            goto L_0x01e7
        L_0x01e0:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x02d9 }
            goto L_0x01e7
        L_0x01e5:
            r16 = r2
        L_0x01e7:
            android.content.Context r0 = r1.context     // Catch:{ all -> 0x02d9 }
            java.lang.String r2 = "Timer_T1"
            java.lang.String r2 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r2, r5)     // Catch:{ all -> 0x02d9 }
            int r17 = r23.getTimer1()     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r17)     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r0 = com.sec.internal.ims.config.RcsConfigurationHelper.readIntParam(r0, r2, r3)     // Catch:{ all -> 0x02d9 }
            int r0 = r0.intValue()     // Catch:{ all -> 0x02d9 }
            android.content.Context r2 = r1.context     // Catch:{ all -> 0x02d9 }
            java.lang.String r3 = "Timer_T2"
            java.lang.String r3 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r3, r5)     // Catch:{ all -> 0x02d9 }
            int r17 = r23.getTimer2()     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r17)     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r2 = com.sec.internal.ims.config.RcsConfigurationHelper.readIntParam(r2, r3, r4)     // Catch:{ all -> 0x02d9 }
            int r2 = r2.intValue()     // Catch:{ all -> 0x02d9 }
            android.content.Context r3 = r1.context     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "Timer_T4"
            java.lang.String r4 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r4, r5)     // Catch:{ all -> 0x02d9 }
            int r17 = r23.getTimer4()     // Catch:{ all -> 0x02d9 }
            r21 = r14
            java.lang.Integer r14 = java.lang.Integer.valueOf(r17)     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r3 = com.sec.internal.ims.config.RcsConfigurationHelper.readIntParam(r3, r4, r14)     // Catch:{ all -> 0x02d9 }
            int r3 = r3.intValue()     // Catch:{ all -> 0x02d9 }
            android.content.Context r4 = r1.context     // Catch:{ all -> 0x02d9 }
            java.lang.String r14 = "RegRetryBaseTime"
            java.lang.String r14 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r14, r5)     // Catch:{ all -> 0x02d9 }
            int r17 = r23.getRegRetryBaseTime()     // Catch:{ all -> 0x02d9 }
            r18 = r3
            java.lang.Integer r3 = java.lang.Integer.valueOf(r17)     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r3 = com.sec.internal.ims.config.RcsConfigurationHelper.readIntParam(r4, r14, r3)     // Catch:{ all -> 0x02d9 }
            int r3 = r3.intValue()     // Catch:{ all -> 0x02d9 }
            android.content.Context r4 = r1.context     // Catch:{ all -> 0x02d9 }
            java.lang.String r14 = "RegRetryMaxTime"
            java.lang.String r14 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r14, r5)     // Catch:{ all -> 0x02d9 }
            int r17 = r23.getRegRetryMaxTime()     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r17)     // Catch:{ all -> 0x02d9 }
            java.lang.Integer r1 = com.sec.internal.ims.config.RcsConfigurationHelper.readIntParam(r4, r14, r1)     // Catch:{ all -> 0x02d9 }
            int r1 = r1.intValue()     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "password"
            r6.putString(r4, r7)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "realm"
            r6.putString(r4, r8)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "msrpTransType"
            r6.putString(r4, r9)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "transport"
            r6.putString(r4, r10)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "imMsgTech"
            r6.putString(r4, r15)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "useKeepAlive"
            r6.putBoolean(r4, r12)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "qVal"
            int r14 = r13.intValue()     // Catch:{ all -> 0x02d9 }
            r6.putInt(r4, r14)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "Timer_T1"
            r6.putInt(r4, r0)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "Timer_T2"
            r6.putInt(r4, r2)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "Timer_T4"
            r14 = r18
            r6.putInt(r4, r14)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "RegRetryBaseTime"
            r6.putInt(r4, r3)     // Catch:{ all -> 0x02d9 }
            java.lang.String r4 = "RegRetryMaxTime"
            r6.putInt(r4, r1)     // Catch:{ all -> 0x02d9 }
            monitor-exit(r11)     // Catch:{ all -> 0x02d9 }
            return r6
        L_0x02ac:
            r0 = move-exception
            r10 = r20
            goto L_0x02d7
        L_0x02b0:
            r0 = move-exception
            r9 = r19
            r10 = r20
            goto L_0x02d7
        L_0x02b6:
            r0 = move-exception
            r20 = r10
            r9 = r19
            goto L_0x02d7
        L_0x02bc:
            r0 = move-exception
            r19 = r9
            r20 = r10
            r8 = r18
            goto L_0x02d7
        L_0x02c4:
            r0 = move-exception
            r19 = r9
            r20 = r10
            r7 = r17
            r8 = r18
            goto L_0x02d7
        L_0x02ce:
            r0 = move-exception
            r17 = r7
            r18 = r8
            r19 = r9
            r20 = r10
        L_0x02d7:
            monitor-exit(r11)     // Catch:{ all -> 0x02d9 }
            throw r0
        L_0x02d9:
            r0 = move-exception
            goto L_0x02d7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.rcs.RcsPolicyManager.getRcsConfigForUserAgent(com.sec.ims.settings.ImsProfile, com.sec.internal.constants.Mno, int, int):android.os.Bundle");
    }

    /* access modifiers changed from: private */
    public void onRcsRoamingPrefChanged(int phoneId) {
        int rcsRoamingPref = UserConfiguration.getUserConfig(this.context, phoneId, "rcs_roaming_pref", 2);
        Log.i(IRcsPolicyManager.LOG_TAG, "onRcsRoamingPrefChanged: now [" + rcsRoamingPref + "]");
        this.mRegMgr.notifyRomaingSettingsChanged(rcsRoamingPref, phoneId);
    }

    /* access modifiers changed from: private */
    public void onRCSAllowedChangedbyMDM() {
        this.mRegMgr.notifyRCSAllowedChangedbyMDM();
    }

    public boolean tryRcsConfig(IRegisterTask task) {
        IConfigModule cm = ImsRegistry.getConfigModule();
        if (!cm.tryAutoconfiguration(task)) {
            return false;
        }
        int phoneId = task.getPhoneId();
        IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "tryRcsConfig for task : " + task.getProfile().getName());
        if (cm.getAcsTryReason(task.getPhoneId()) != DiagnosisConstants.RCSA_ATRE.INIT) {
            return true;
        }
        cm.setAcsTryReason(task.getPhoneId(), DiagnosisConstants.RCSA_ATRE.FROM_REGI);
        return true;
    }

    public boolean doRcsConfig(IRegisterTask task, List<IRegisterTask> rtl) {
        IConfigModule cm = ImsRegistry.getConfigModule();
        if (!cm.isWaitAutoconfig(task)) {
            return false;
        }
        int phoneId = task.getPhoneId();
        IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "doRcsConfig for task : " + task.getProfile().getName());
        if (cm.getAcsTryReason(task.getPhoneId()) == DiagnosisConstants.RCSA_ATRE.INIT) {
            cm.setAcsTryReason(task.getPhoneId(), DiagnosisConstants.RCSA_ATRE.FROM_REGI);
        }
        cm.triggerAutoConfig(false, task.getPhoneId(), rtl);
        return true;
    }

    public static String getRcsTransport(Context context2, int pdn, ImsProfile profile, int phoneId) {
        String transport;
        if (pdn == 1 || (RcsUtils.DualRcs.isDualRcsReg() && !SimUtil.isDdsSimSlot(phoneId))) {
            transport = RcsConfigurationHelper.readStringParam(context2, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_SIGNALLING, phoneId), "SIPoTLS");
        } else {
            TelephonyManager tm = (TelephonyManager) context2.getSystemService(PhoneConstants.PHONE_KEY);
            transport = RcsConfigurationHelper.readStringParam(context2, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING, phoneId), "SIPoUDP");
            String rcsTransportPs = transport;
            Mno mno = SimUtil.getSimMno(phoneId);
            String rcsAs = ConfigUtil.getAcsServerType(context2, phoneId);
            if (!(!ImsProfile.isRcsUpProfile(ConfigUtil.getRcsProfileWithFeature(context2, phoneId, profile)) || !tm.isNetworkRoaming() || mno == Mno.SPRINT || mno == Mno.VZW || mno == Mno.TCE || mno == Mno.ROGERS || ImsConstants.RCS_AS.JIBE.equals(rcsAs))) {
                transport = RcsConfigurationHelper.readStringParam(context2, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING_ROAMING, phoneId), rcsTransportPs);
            }
        }
        char c = 65535;
        int hashCode = transport.hashCode();
        if (hashCode != -1479406420) {
            if (hashCode != -1479406138) {
                if (hashCode == -1479405428 && transport.equals("SIPoUDP")) {
                    c = 0;
                }
            } else if (transport.equals("SIPoTLS")) {
                c = 2;
            }
        } else if (transport.equals("SIPoTCP")) {
            c = 1;
        }
        if (c == 0) {
            return "udp";
        }
        if (c == 1) {
            return "tcp";
        }
        if (c != 2) {
            return profile.getTransportName();
        }
        return "tls";
    }

    public static RcsConfig getRcsConfig(Context context2, Mno simMno, ImsProfile profile, boolean requestregi, int phoneId) {
        int pagerModeLimit;
        String confUri;
        ImsUri imsUri;
        String exploderUri;
        boolean privacyDisable;
        Context context3 = context2;
        ImsProfile imsProfile = profile;
        int i = phoneId;
        if (!RcsUtils.DualRcs.isRegAllowed(context3, i)) {
            return null;
        }
        if (requestregi) {
            int ftchunksize = ImsRegistry.getInt(i, GlobalSettingsConstants.RCS.FT_CHUNK_SIZE, 0);
            int ishchunksize = ImsRegistry.getInt(i, GlobalSettingsConstants.RCS.ISH_CHUNK_SIZE, 0);
            boolean isMsrpCema = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.MSRP_CEMA, false);
            boolean isConfSubscribeEnabled = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.CONF_SUBSCRIBE_ENABLED, true);
            if (ImsProfile.isRcsUpProfile(ConfigUtil.getRcsProfileWithFeature(context3, i, imsProfile))) {
                pagerModeLimit = RcsConfigurationHelper.readIntParam(context3, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE, i), 1300).intValue();
            } else {
                pagerModeLimit = ImsRegistry.getInt(i, GlobalSettingsConstants.RCS.PAGER_MODE_SIZE_LIMIT, 0);
            }
            boolean isAggrImdnSupported = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.AGGR_IMDN_SUPPORTED, false);
            ImsUri imsUri2 = RcsConfigurationHelper.readImsUriParam(context3, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.IM_CONF_FCTY_URI, i));
            if (imsUri2 != null) {
                confUri = imsUri2.toString();
            } else {
                confUri = "";
            }
            String confUri2 = FilePathGenerator.getFileDownloadPath(context3, true);
            String downloadsPath = confUri2 == null ? "" : confUri2;
            if (simMno != Mno.CMCC || ImsProfile.isRcsUpProfile(ConfigUtil.getRcsProfileWithFeature(context3, i, imsProfile))) {
                imsUri = RcsConfigurationHelper.readImsUriParam(context3, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.IM_EXPLODER_URI, i));
            } else {
                imsUri = RcsConfigurationHelper.readImsUriParam(context3, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.IM_MASS_FCTY_URI, i));
            }
            if (imsUri == null || "sip:foo@bar".equals(imsUri.toString())) {
                exploderUri = "";
            } else {
                exploderUri = imsUri.toString();
            }
            boolean useMsrpDiscardPort = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.MSRP_DISCARD_PORT, false);
            if (profile.getRcsProfileType() >= ImsProfile.RCS_PROFILE.UP_2_2.ordinal()) {
                privacyDisable = RcsConfigurationHelper.readBoolParam(context3, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.CHATBOT_PRIVACY_DISABLE, i)).booleanValue();
            } else {
                privacyDisable = true;
            }
            return new RcsConfig(ftchunksize, ishchunksize, confUri, isMsrpCema, downloadsPath, isConfSubscribeEnabled, exploderUri, pagerModeLimit, useMsrpDiscardPort, isAggrImdnSupported, privacyDisable, RcsConfigurationHelper.readIntParam(context3, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH, i), 1).intValue(), RcsConfigurationHelper.readStringParam(context3, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_END_USER_CONF_REQID, i), ""));
        } else if (!ImsConstants.RCS_AS.JIBE.equals(ConfigUtil.getAcsServerType(context3, i)) || !ConfigUtil.isRcsOnly(profile)) {
            return null;
        } else {
            String suspenduser = "";
            if (ImsConstants.SystemSettings.getRcsUserSetting(context3, 0, i) == 0) {
                suspenduser = "soft";
                Log.i(IRcsPolicyManager.LOG_TAG, "IMS deregister : RCS off from user");
            }
            return new RcsConfig(suspenduser);
        }
    }
}
