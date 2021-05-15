package com.sec.internal.ims.core;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.NetworkState;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class NetworkEventController {
    private static final int EPDG_EVENT_TIMER = 121000;
    private static final String IMS_DM_START = "10";
    private static final String LOG_TAG = "RegiMgr-NetEvtCtr";
    protected ICmcAccountManager mCmcAccountManager;
    protected Context mContext;
    protected SimpleEventLog mEventLog;
    protected IImsFramework mImsFramework;
    private int mNetType = 0;
    private boolean mNwChanged = false;
    protected PdnController mPdnController;
    protected IRcsPolicyManager mRcsPolicyManager;
    protected RegistrationManagerHandler mRegHandler;
    protected RegistrationManagerBase mRegMan;
    private PendingIntent mRetryIntentOnPdnFail = null;
    protected List<ISimManager> mSimManagers;
    protected ITelephonyManager mTelephonyManager;
    protected boolean mUnprocessedOmadmConfig = false;
    protected IVolteServiceModule mVsm;
    private boolean mWiFi = false;

    NetworkEventController(Context ctx) {
        this.mContext = ctx;
    }

    NetworkEventController(Context ctx, PdnController pc, ITelephonyManager tm, List<ISimManager> sms, ICmcAccountManager cam, IRcsPolicyManager rpm, RegistrationManagerBase rmb, IImsFramework ifw) {
        this.mContext = ctx;
        this.mPdnController = pc;
        this.mTelephonyManager = tm;
        this.mSimManagers = sms;
        this.mCmcAccountManager = cam;
        this.mRcsPolicyManager = rpm;
        this.mRegMan = rmb;
        this.mImsFramework = ifw;
        this.mEventLog = new SimpleEventLog(ctx, LOG_TAG, 300);
    }

    public void setVolteServiceModule(IVolteServiceModule vsm) {
        this.mVsm = vsm;
    }

    public void setRegistrationHandler(RegistrationManagerHandler regHandler) {
        this.mRegHandler = regHandler;
    }

    public String getPcscfIpAddress(IRegisterTask task, String iface) {
        if (!task.getGovernor().hasValidPcscfIpList()) {
            List<String> pcscfList = RegistrationUtils.retrievePcscfByProfileSettings(task, this.mPdnController, this.mRcsPolicyManager, this.mTelephonyManager.getIsimPcscf());
            if (CollectionUtils.isNullOrEmpty((Collection<?>) pcscfList)) {
                return null;
            }
            List<String> ret = task.getGovernor().checkValidPcscfIp(lookupPcscfIfRequired(task, pcscfList, iface));
            if (CollectionUtils.isNullOrEmpty((Collection<?>) ret)) {
                return null;
            }
            task.getGovernor().updatePcscfIpList(ret);
            this.mRcsPolicyManager.updateDualRcsPcscfIp(task, ret);
            return task.getGovernor().getCurrentPcscfIp();
        }
        this.mRcsPolicyManager.updateDualRcsPcscfIp(task, (List<String>) null);
        return task.getGovernor().getCurrentPcscfIp();
    }

    private boolean isDomainPattern(String str) {
        if (!TextUtils.isEmpty(str) && str.matches("[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z][-a-zA-Z0-9]{0,62})+\\.?")) {
            return true;
        }
        return false;
    }

    private List<String> lookupPcscfIfRequired(IRegisterTask task, List<String> pcscfList, String iface) {
        List<String> dnses;
        long netId;
        IRegisterTask iRegisterTask = task;
        List<String> ret = new ArrayList<>();
        Iterator<String> it = pcscfList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String pcscf = it.next();
            if (NetworkUtil.isValidPcscfAddress(pcscf)) {
                List<String> dnses2 = this.mPdnController.getDnsServers((PdnEventListener) iRegisterTask);
                iRegisterTask.setPcscfHostname(pcscf);
                long netId2 = task.getNetworkConnected() == null ? 0 : task.getNetworkConnected().getNetworkHandle();
                IMSLog.i(LOG_TAG, task.getPhoneId(), "netId: " + netId2);
                if (!task.getProfile().getNeedNaptrDns()) {
                    netId = netId2;
                    dnses = dnses2;
                } else if (task.getMno() != Mno.CMCC || isDomainPattern(pcscf)) {
                    String transport = this.mRcsPolicyManager.selectRcsTransportType(iRegisterTask, "TLS");
                    if (!task.getProfile().getNeedIpv4Dns()) {
                        IMSLog.i(LOG_TAG, task.getPhoneId(), "not ipv4 dns");
                        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVING);
                        this.mRegMan.sendDnsQuery(task.getPhoneId(), iface, pcscf, dnses2, "NAPTR", transport, "", netId2);
                        break;
                    }
                    String ipver = this.mRcsPolicyManager.selectRcsDnsType(iRegisterTask, dnses2);
                    if (ipver != null) {
                        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVING);
                        List<String> list = dnses2;
                        this.mRegMan.sendDnsQuery(task.getPhoneId(), iface, pcscf, dnses2, "NAPTR", transport, ipver, netId2);
                        break;
                    }
                    netId = netId2;
                    dnses = dnses2;
                } else {
                    netId = netId2;
                    dnses = dnses2;
                }
                if (!SimUtil.isSoftphoneEnabled() || !this.mRegMan.getAresLookupRequired()) {
                    try {
                        int i = 0;
                        if (task.getProfile().getCmcType() != 0) {
                            InetAddress[] allByNameWithThread = RegistrationUtils.getAllByNameWithThread(iRegisterTask, pcscf);
                            int length = allByNameWithThread.length;
                            while (i < length) {
                                ret.add(allByNameWithThread[i].getHostAddress());
                                i++;
                            }
                        } else if (task.getNetworkConnected() != null) {
                            InetAddress[] allByName = task.getNetworkConnected().getAllByName(pcscf);
                            int length2 = allByName.length;
                            while (i < length2) {
                                ret.add(allByName[i].getHostAddress());
                                i++;
                            }
                        } else {
                            InetAddress[] allByName2 = InetAddress.getAllByName(pcscf);
                            int length3 = allByName2.length;
                            while (i < length3) {
                                ret.add(allByName2[i].getHostAddress());
                                i++;
                            }
                        }
                        iRegisterTask.setPcscfHostname(pcscf);
                        if (task.getProfile().getCmcType() >= 3) {
                            iRegisterTask.setPcscfHostname(task.getProfile().getDomain());
                        }
                    } catch (UnknownHostException e) {
                        IMSLog.i(LOG_TAG, task.getPhoneId(), "getPcscfIpAddresses: faild to resolve dns query .");
                        this.mRegMan.setAresLookupRequired(true);
                        if (task.getMno() == Mno.KT) {
                            task.getGovernor().retryDNSQuery();
                        }
                        if (task.getProfile().getCmcType() != 0) {
                            IMSLog.i(LOG_TAG, task.getPhoneId(), "CMC dns query failed");
                            break;
                        }
                    }
                } else {
                    iRegisterTask.setPcscfHostname(pcscf);
                    String pcscf2 = "_sip._tls." + pcscf;
                    iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVING);
                    ArrayList arrayList = new ArrayList();
                    if (dnses != null) {
                        for (String dns : dnses) {
                            if (NetworkUtil.isIPv4Address(dns)) {
                                arrayList.add(dns);
                            }
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        IMSLog.i(LOG_TAG, task.getPhoneId(), "ATT SoftPhone : found ipv4 dns");
                        this.mRegMan.sendDnsQuery(0, iface, pcscf2, arrayList, "SRV", "tcp", "IPV4", netId);
                    } else {
                        this.mRegMan.sendDnsQuery(0, iface, pcscf2, dnses, "SRV", "tcp", "", netId);
                    }
                }
            } else {
                IMSLog.i(LOG_TAG, task.getPhoneId(), "getPcscfIpAddresses: pcscf is not valid... continue : " + pcscf);
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public void onEpdgConnected(int phoneId) {
        IVolteServiceModule iVolteServiceModule;
        this.mRegHandler.removeMessages(135);
        this.mRegMan.updatePani(phoneId);
        IMSLog.i(LOG_TAG, phoneId, "onEpdgConnected:");
        if (RegistrationUtils.getNetworkEvent(phoneId) != null) {
            RegistrationUtils.getNetworkEvent(phoneId).isEpdgConnected = true;
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getProfile().getPdnType() == 11) {
                    IMSLog.i(LOG_TAG, phoneId, "onEpdgConnected: " + task.getState() + " mIsUpdateRegistering=" + task.mIsUpdateRegistering + " task=" + task.getProfile().getName() + " mno=" + task.mMno);
                    if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && !task.mIsUpdateRegistering) {
                        task.setReason("EPDG HO : L2W");
                        task.setEpdgHandoverInProgress(true);
                        task.setRegiRequestType(DiagnosisConstants.REGI_REQC.HAND_OVER);
                        if ((task.getMno() == Mno.TMOBILE || task.getMno() == Mno.TDC_DK) && (iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.hasCsCall(phoneId)) {
                            this.mRegMan.updateRegistration(task, true, false);
                        } else {
                            this.mRegMan.updateRegistration(task, true, true);
                        }
                    } else if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && task.mIsUpdateRegistering)) {
                        task.setReason("EPDG HO : L2W");
                        task.setEpdgHandoverInProgress(true);
                        task.setRegiRequestType(DiagnosisConstants.REGI_REQC.HAND_OVER);
                        this.mRegMan.updateRegistration(task, true, false);
                    } else {
                        this.mRegMan.tryRegister(phoneId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onEpdgDisconnected(int phoneId) {
        this.mRegHandler.removeMessages(135);
        this.mRegMan.updatePani(phoneId);
        IMSLog.i(LOG_TAG, phoneId, "onEpdgDisconnected:");
        if (RegistrationUtils.getNetworkEvent(phoneId) != null) {
            RegistrationUtils.getNetworkEvent(phoneId).isEpdgConnected = false;
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getProfile().getPdnType() == 11) {
                    IMSLog.i(LOG_TAG, phoneId, "onEpdgDisconnected: " + task.getState() + " mIsUpdateRegistering=" + task.mIsUpdateRegistering + " task=" + task.getProfile().getName() + " mno=" + task.mMno);
                    if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERED || task.mIsUpdateRegistering) {
                        if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && task.mIsUpdateRegistering)) {
                            task.setReason("EPDG HO : W2L");
                            task.setEpdgHandoverInProgress(true);
                            this.mRegMan.updateRegistration(task, true, false);
                        } else {
                            this.mRegMan.tryRegister(phoneId);
                        }
                    } else if (task.getGovernor().checkEmergencyInProgress()) {
                        IMSLog.i(LOG_TAG, phoneId, "onEpdgDisconnected: Skip re-registration due to Emergency registration");
                        RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(27, phoneId, 0, (Object) null), 300);
                        return;
                    } else {
                        task.setReason("EPDG HO : W2L");
                        task.setEpdgHandoverInProgress(true);
                        this.mRegMan.updateRegistration(task, true, true);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onEpdgDeregisterRequested(int phoneId) {
        this.mRegMan.sendDeregister(124, phoneId);
    }

    /* access modifiers changed from: protected */
    public void onPdnConnected(RegisterTask task) {
        if (task == null) {
            IMSLog.e(LOG_TAG, "task is null. Skip pdnConnected event");
            return;
        }
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        IMSLog.i(LOG_TAG, phoneId, "onPdnConnected: task=" + profile.getName() + " state=" + task.getState());
        if (task.getMno().isEur() && task.getPdnType() == 11) {
            task.getGovernor().releaseThrottle(6);
        }
        if (!task.getGovernor().needPendingPdnConnected()) {
            if (task.getMno().isChn() && profile.hasEmergencySupport()) {
                boolean hasEmergencyTask = false;
                Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((RegisterTask) it.next()).getProfile().hasEmergencySupport()) {
                            hasEmergencyTask = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!hasEmergencyTask) {
                    IMSLog.i(LOG_TAG, phoneId, "onPdnConnected: Emergency task already removed");
                    return;
                }
            }
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING)) {
                task.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
                task.getGovernor().resetPdnFailureInfo();
                profile.setUicclessEmergency(false);
                if (profile.hasEmergencySupport()) {
                    boolean needEmergencyReg = needEmergencyRegistration(task);
                    IMSLog.i(LOG_TAG, phoneId, "onPdnConnected: need emergency Registration: " + needEmergencyReg);
                    profile.setUicclessEmergency(needEmergencyReg ^ true);
                    if (this.mPdnController.isEmergencyEpdgConnected(phoneId)) {
                        task.setRegistrationRat(18);
                    }
                }
                task.getGovernor().onPdnConnected();
                this.mRegMan.tryRegister(task);
            }
        }
    }

    private boolean isTaskHasSepecificPdnType(IRegisterTask task) {
        return (task.getProfile() == null || task.getProfile().getPdnType() == -1) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void onPdnDisconnected(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onPdnDisconnected: " + task.getState());
        ISimManager sm = this.mSimManagers.get(task.getPhoneId());
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(task.getPhoneId());
        if (rtl != null) {
            if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                task.setDeregiReason(2);
            }
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                task.setReason("pdn disconnected - REGISTERED or REGISTERING");
                if (!isTaskHasSepecificPdnType(task)) {
                    this.mRegMan.tryDeregisterInternal(task, true, false);
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                } else if (task.getMno() == Mno.KDDI && task.getProfile().hasEmergencySupport()) {
                    this.mRegMan.tryDeregisterInternal(task, true, false);
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                } else if (!task.getMno().isAus() || sm == null || sm.isSimLoaded()) {
                    this.mRegMan.tryDeregisterInternal(task, true, true);
                } else {
                    this.mRegMan.tryDeregisterInternal(task, true, false);
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                }
            } else if (task.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY) {
                task.setReason("pdn disconnected - EMERGENCY");
                task.setDeregiReason(3);
                this.mRegMan.tryDeregisterInternal(task, true, false);
                this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                rtl.remove(task);
                SlotBasedConfig.getInstance(task.getPhoneId()).removeExtendedProfile(task.getProfile().getId());
            } else if (task.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
            } else {
                if (!isTaskHasSepecificPdnType(task)) {
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                } else if (task.getMno() == Mno.KDDI && task.getProfile().hasEmergencySupport()) {
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                } else if (task.getState() != RegistrationConstants.RegisterTaskState.IDLE) {
                    task.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
                }
                RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(2, Integer.valueOf(task.getPhoneId())), 1000);
                if (task.getMno() == Mno.VZW) {
                    task.getGovernor().releaseThrottle(5);
                }
            }
            if (task.getMno().isKor() && !task.isRcsOnly()) {
                task.getGovernor().releaseThrottle(5);
            }
            task.resetTaskOnPdnDisconnected();
            if (task.getMno().isKor()) {
                if (!task.isRcsOnly()) {
                    this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
                    task.getGovernor().resetPcscfPreference();
                    task.getGovernor().resetIPSecAllow();
                }
                task.getGovernor().resetAllRetryFlow();
            }
            RegistrationManagerHandler registrationManagerHandler2 = this.mRegHandler;
            registrationManagerHandler2.sendMessage(registrationManagerHandler2.obtainMessage(136, task.getPhoneId(), 0, (Object) null));
        }
    }

    private boolean needEmergencyRegistration(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm == null || sm.hasNoSim() || task.getMno().isKor()) {
            return false;
        }
        if (task.getMno() == Mno.USCC && !sm.isISimDataValid()) {
            return false;
        }
        if (task.getMno().isAus() && task.getRegistrationRat() == 13 && task.getPdnType() == 15 && (this.mPdnController.getVoiceRegState(phoneId) == 2 || this.mPdnController.getVoiceRegState(phoneId) == 1)) {
            Log.i(LOG_TAG, "needEmergencyRegistration[AUS]: limited mode but has valid SIM. Try register.");
            return true;
        } else if (task.getMno().isCanada() && this.mPdnController.hasEmergencyServiceOnly(task.getPhoneId())) {
            Log.i(LOG_TAG, "needEmergencyRegistration: limited mode. Dont Skip for Canada.");
            return true;
        } else if (task.getMno() != Mno.DOCOMO && this.mPdnController.hasEmergencyServiceOnly(task.getPhoneId())) {
            Log.i(LOG_TAG, "needEmergencyRegistration: limited mode. skip emergency registration.");
            return false;
        } else if (task.getMno() != Mno.VZW || (this.mTelephonyManager.validateMsisdn(SimUtil.getSubId(phoneId)) && !this.mRegMan.isSelfActivationRequired(phoneId))) {
            return true;
        } else {
            Log.i(LOG_TAG, "Get PCO 5. Skip emergency registration.");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void onCellLocationChanged(int phoneId) {
        this.mRegMan.updatePani(phoneId);
        if (RegistrationUtils.getNetworkEvent(phoneId) != null) {
            notifyNetworkEvent(-1, RegistrationUtils.getNetworkEvent(phoneId).isWifiConnected, phoneId);
            this.mRegMan.updateTimeInPlani(phoneId, false);
        }
    }

    private void updateRat(int network, int phoneId) {
        UriGeneratorFactory uf = UriGeneratorFactory.getInstance();
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            this.mRegMan.updateRat(task, network);
            ImsRegistration reg = task.mReg;
            if (reg != null) {
                for (NameAddr addr : reg.getImpuList()) {
                    ImsUri uri = addr.getUri();
                    if (uf.contains(uri)) {
                        uf.get(uri).updateRat(network);
                    }
                }
            }
        }
    }

    public int getNetType() {
        return this.mNetType;
    }

    public boolean isWiFi() {
        return this.mWiFi;
    }

    public boolean isNwChanged() {
        return this.mNwChanged;
    }

    public void setNwChanged(boolean changed) {
        this.mNwChanged = changed;
    }

    /* access modifiers changed from: package-private */
    public void onNetworkChanged(int network, boolean isWifiConnected, int phoneId) {
        boolean suspendByIrat = SlotBasedConfig.getInstance(phoneId).isSuspendedWhileIrat();
        IMSLog.i(LOG_TAG, phoneId, "onNetworkChanged: suspendByIrat=" + suspendByIrat);
        if (!suspendByIrat) {
            this.mRegMan.updatePani(phoneId);
            updateRat(network, phoneId);
            notifyNetworkEvent(network, isWifiConnected, phoneId);
            return;
        }
        this.mNetType = network;
        this.mWiFi = isWifiConnected;
        this.mNwChanged = true;
    }

    private void notifyNetworkEvent(int network, boolean isWifiConnected, int phoneId) {
        NetworkEvent ne = RegistrationUtils.getNetworkEvent(phoneId);
        NetworkState ns = this.mPdnController.getNetworkState(phoneId);
        if (ne != null && ns != null) {
            NetworkEvent event = NetworkEvent.buildNetworkEvent(phoneId, this.mSimManagers.get(phoneId).getSimMno() == Mno.TMOUS, network, this.mTelephonyManager.getVoiceNetworkType(), this.mTelephonyManager.getCallState(), isWifiConnected, ns.isEpdgConnected(), ns.isEpdgAVailable(), ne, ns);
            if (event != null && !event.equalsIgnoreEpdg(ne)) {
                onNetworkEventChanged(event, phoneId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reconnectPdn(RegisterTask task) {
        if (!task.getProfile().hasEmergencySupport()) {
            int rat = this.mRegMan.findBestNetwork(task.getPhoneId(), task.getProfile(), task.getGovernor());
            int pdn = RegistrationUtils.selectPdnType(task.getProfile(), rat);
            int phoneId = task.getPhoneId();
            NetworkEvent ne = RegistrationUtils.getNetworkEvent(phoneId);
            if (ne != null) {
                String netTypeName = TelephonyManagerExt.getNetworkTypeName(ne.network);
                if (rat == 0 && !ne.outOfService) {
                    IMSLog.i(LOG_TAG, phoneId, "Cancel ongoing PDN in " + netTypeName);
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                    if (task.getMno() == Mno.VZW && NetworkUtil.isLegacy3gppNetwork(RegistrationUtils.getNetworkEvent(phoneId).network)) {
                        task.getGovernor().releaseThrottle(5);
                    }
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                } else if (pdn != task.getPdnType()) {
                    IMSLog.i(LOG_TAG, task.getPhoneId(), "pdn type has been changed, reconnect.");
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                    task.setPdnType(pdn);
                    task.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
                    if (task.getGovernor().isMobilePreferredForRcs() && pdn == 0) {
                        PdnController pdnController = this.mPdnController;
                        int preferredPdnType = pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer());
                        IMSLog.i(LOG_TAG, phoneId, "reconnectPdn startTimsTimer rcs pdn = " + pdn);
                        if (preferredPdnType == 1) {
                            task.mGovernor.stopTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
                            this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                        }
                        task.mGovernor.startTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
                    }
                    this.mPdnController.startPdnConnectivity(pdn, task, RegistrationUtils.getPhoneIdForStartConnectivity(task));
                    if (task.getMno().isOneOf(Mno.VZW, Mno.KDDI, Mno.CTCMO, Mno.CTC) || (task.getMno().isKor() && !task.isRcsOnly() && !RegistrationUtils.isCmcProfile(task.getProfile()))) {
                        task.getGovernor().startTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRetryTimeExpired(int phoneId) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getProfile().getPdnType() == 11) {
                PendingIntent pendingIntent = this.mRetryIntentOnPdnFail;
                if (pendingIntent != null) {
                    AlarmTimer.stop(this.mContext, pendingIntent);
                    this.mRetryIntentOnPdnFail = null;
                }
                Log.i(LOG_TAG, "RetrySetupEventReceiver: release throttle pdn fail");
                task.getGovernor().releaseThrottle(4);
                RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(phoneId)));
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x008e, code lost:
        r0 = com.sec.internal.helper.SimUtil.getOppositeSimSlot(r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onPdnFailed(int r9, java.lang.String r10) {
        /*
            r8 = this;
            boolean r0 = android.text.TextUtils.isEmpty(r10)
            if (r0 != 0) goto L_0x00a7
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r0 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r9)
            java.util.Iterator r0 = r0.iterator()
        L_0x000e:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0088
            java.lang.Object r1 = r0.next()
            com.sec.internal.ims.core.RegisterTask r1 = (com.sec.internal.ims.core.RegisterTask) r1
            com.sec.ims.settings.ImsProfile r2 = r1.getProfile()
            int r2 = r2.getPdnType()
            r3 = 11
            if (r2 == r3) goto L_0x0027
            goto L_0x000e
        L_0x0027:
            r2 = 285278218(0x1101000a, float:1.0176318E-28)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r9)
            java.lang.String r4 = ",PDN FAIL:"
            r3.append(r4)
            r3.append(r10)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.c(r2, r3)
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r2 = r1.getGovernor()
            r2.onPdnRequestFailed(r10)
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r2 = r1.getGovernor()
            long r2 = r2.getRetryTimeOnPdnFail()
            r4 = 0
            int r4 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r4 <= 0) goto L_0x0087
            android.app.PendingIntent r4 = r8.mRetryIntentOnPdnFail
            if (r4 == 0) goto L_0x0062
            android.content.Context r5 = r8.mContext
            com.sec.internal.helper.AlarmTimer.stop(r5, r4)
            r4 = 0
            r8.mRetryIntentOnPdnFail = r4
        L_0x0062:
            android.content.Intent r4 = new android.content.Intent
            java.lang.String r5 = "android.intent.action.retryTimeExpired"
            r4.<init>(r5)
            java.lang.String r5 = "EXTRA_PHONE_ID"
            r4.putExtra(r5, r9)
            android.content.Context r5 = r8.mContext
            r6 = 0
            r7 = 134217728(0x8000000, float:3.85186E-34)
            android.app.PendingIntent r5 = android.app.PendingIntent.getBroadcast(r5, r6, r4, r7)
            r8.mRetryIntentOnPdnFail = r5
            android.content.Context r6 = r8.mContext
            com.sec.internal.helper.AlarmTimer.start(r6, r5, r2)
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r5 = r1.getGovernor()
            r6 = -1
            r5.setRetryTimeOnPdnFail(r6)
        L_0x0087:
            goto L_0x000e
        L_0x0088:
            boolean r0 = com.sec.internal.helper.SimUtil.isDualIMS()
            if (r0 == 0) goto L_0x00a7
            int r0 = com.sec.internal.helper.SimUtil.getOppositeSimSlot(r9)
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r1 = r8.mSimManagers
            java.lang.Object r1 = r1.get(r0)
            com.sec.internal.interfaces.ims.core.ISimManager r1 = (com.sec.internal.interfaces.ims.core.ISimManager) r1
            if (r1 == 0) goto L_0x00a7
            boolean r2 = r1.isSimAvailable()
            if (r2 == 0) goto L_0x00a7
            com.sec.internal.ims.core.RegistrationManagerHandler r2 = r8.mRegHandler
            r2.sendTryRegister(r0)
        L_0x00a7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.NetworkEventController.onPdnFailed(int, java.lang.String):void");
    }

    /* access modifiers changed from: package-private */
    public boolean hasRetryIntentOnPdnFail() {
        return this.mRetryIntentOnPdnFail != null;
    }

    /* access modifiers changed from: package-private */
    public void onCheckUnprocessedOmadmConfig() {
        if (this.mUnprocessedOmadmConfig) {
            Log.i(LOG_TAG, "onCheckUnprocessedOmadmConfig: triggerOmadmConfig");
            this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
            triggerOmadmConfig();
        }
    }

    /* access modifiers changed from: package-private */
    public void onDmConfigCompleted(boolean isCompleted) {
        StringBuilder sb = new StringBuilder();
        sb.append("onDmConfigCompleted: ");
        sb.append(isCompleted ? "SUCCESS" : "TIMEOUT");
        Log.i(LOG_TAG, sb.toString());
        if (this.mUnprocessedOmadmConfig && isCompleted) {
            this.mUnprocessedOmadmConfig = false;
        }
        this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.FINISHED);
        this.mRegHandler.removeDmConfigTimeout();
        for (int slot = 0; slot < this.mSimManagers.size(); slot++) {
            Iterator it = SlotBasedConfig.getInstance(slot).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().checkProfileUpdateFromDM(true);
            }
        }
        this.mRegMan.tryRegister();
    }

    /* access modifiers changed from: package-private */
    public void triggerOmadmConfig() {
        Log.i(LOG_TAG, "triggerOmadmConfig - mOmadmState : " + this.mRegMan.getOmadmState());
        if (this.mRegMan.getOmadmState() != RegistrationManager.OmadmConfigState.TRIGGERED) {
            this.mUnprocessedOmadmConfig = true;
            this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.TRIGGERED);
            this.mRegHandler.sendDmConfigTimeout(getClass().getSimpleName());
            setOmaDmStateDB(this.mRegMan.getOmadmState());
        }
    }

    /* access modifiers changed from: package-private */
    public void setOmaDmStateDB(RegistrationManager.OmadmConfigState omadmState) {
        if (SimUtil.getMno().isKor() && omadmState == RegistrationManager.OmadmConfigState.TRIGGERED) {
            Log.i(LOG_TAG, "setOmaDmStateDB : " + omadmState);
            try {
                ContentValues cv = new ContentValues();
                cv.put("dm_state", IMS_DM_START);
                this.mContext.getContentResolver().update(Uri.parse("content://com.ims.dm.ContentProvider/imsDmStart"), cv, (String) null, (String[]) null);
            } catch (Exception e) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                StringBuilder sb = new StringBuilder();
                sb.append("setOmaDmStateDB : update failure - ");
                sb.append(e.getMessage() != null ? e.getMessage() : "null");
                simpleEventLog.logAndAdd(sb.toString());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onNetworkEventChanged(NetworkEvent event, int phoneId) {
        boolean bExistRetryTimer;
        NetworkEvent networkEvent = event;
        int i = phoneId;
        NetworkEvent old = RegistrationUtils.getNetworkEvent(phoneId);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (old != null && rtl != null) {
            boolean bExistRetryTimer2 = false;
            boolean bHaveRegisteringTask = false;
            IMSLog.i(LOG_TAG, i, "onNetworkEventChanged:" + old.changedEvent(networkEvent));
            SlotBasedConfig.getInstance(phoneId).setNetworkEvent(networkEvent);
            this.mImsFramework.getServiceModuleManager().notifyNetworkChanged(networkEvent, i);
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getMno().isKor()) {
                    bHaveRegisteringTask = false;
                    bExistRetryTimer = task.getGovernor().isExistRetryTimer();
                } else {
                    bExistRetryTimer = bExistRetryTimer2;
                }
                handleSsacOnNetworkEventChanged(task, i, networkEvent, old);
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING && task.getMno().isKor()) {
                        bHaveRegisteringTask = true;
                    }
                    if (!handleNetworkEventOnRegister(i, task, networkEvent, old)) {
                        bExistRetryTimer2 = bExistRetryTimer;
                    }
                } else {
                    if (!task.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED, RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                        handleNetworkEventOnDeregistering(i, task, networkEvent, old);
                    } else if (!handleNetworkEventBeforeRegister(i, task, networkEvent, old)) {
                        bExistRetryTimer2 = bExistRetryTimer;
                    }
                }
                boolean bHaveRegisteringTask2 = bHaveRegisteringTask;
                handleNetworkEvent(phoneId, task, event, old, bExistRetryTimer, bHaveRegisteringTask2);
                bExistRetryTimer2 = bExistRetryTimer;
                bHaveRegisteringTask = bHaveRegisteringTask2;
            }
            tryCmcRegisterOnNetworkEventChanged(networkEvent, old);
            if (networkEvent.outOfService) {
                IMSLog.i(LOG_TAG, i, "out of service.");
                old.outOfService = true;
                SlotBasedConfig.getInstance(phoneId).setNetworkEvent(old);
                SlotBasedConfig.getInstance(phoneId).setNotifiedImsNotAvailable(false);
            }
            if (!TextUtils.equals(networkEvent.operatorNumeric, old.operatorNumeric)) {
                SlotBasedConfig.getInstance(phoneId).setNotifiedImsNotAvailable(false);
            }
            ISimManager sm = this.mSimManagers.get(i);
            if (sm != null) {
                updateUtOnNetworkEventChanged(i, sm.isSimAvailable(), networkEvent, old);
                if (!sm.getSimMno().isKor()) {
                    IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: tryRegister by phoneID");
                    this.mRegMan.tryRegister(i);
                } else {
                    RegisterTask task2 = (RegisterTask) this.mCmcAccountManager.getCmcRegisterTask(i);
                    if (task2 != null) {
                        if (!task2.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                            IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: tryRegister");
                            this.mRegMan.tryRegister(task2);
                        }
                    }
                }
                if (!this.mRegHandler.hasMessages(32)) {
                    this.mRegHandler.sendEmptyMessage(32);
                }
            }
        }
    }

    private boolean handleNetworkEventOnRegister(int phoneId, RegisterTask task, NetworkEvent event, NetworkEvent old) {
        IVolteServiceModule iVolteServiceModule;
        if (event.outOfService) {
            IMSLog.i(LOG_TAG, phoneId, "out of service.");
            handleOutOfServiceOnNetworkEvnentChanged(task, phoneId);
            return false;
        }
        if (task.getMno().isKor() && !task.isRcsOnly() && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING && event.network != task.getRegistrationRat()) {
            Log.i(LOG_TAG, "onNetworkEventChanged: setRegistrationRat");
            task.setRegistrationRat(event.network);
        }
        if (updateEpdgStatusOnNetworkEventChanged(task, event, old)) {
            return false;
        }
        if (task.getMno() == Mno.VZW) {
            if (NetworkUtil.isLegacy3gppNetwork(event.network) && old.network == 13 && this.mRegHandler.hasMessages(121, Integer.valueOf(phoneId))) {
                this.mRegHandler.removeMessages(121, Integer.valueOf(phoneId));
                SlotBasedConfig.getInstance(phoneId).enableSsac(true);
            }
            if (event.isVopsUpdated(old) != NetworkEvent.VopsState.ENABLED || !event.operatorNumeric.equals(old.operatorNumeric)) {
                if (task.getRegistrationRat() == 14 && event.network == 13) {
                    int TDelay = DmConfigHelper.readInt(this.mContext, "t_delay", 5, phoneId).intValue();
                    IMSLog.i(LOG_TAG, phoneId, "onNetworkChanged: C2L, Tdelay=" + TDelay);
                    if (TDelay > 0) {
                        this.mRegMan.addPendingUpdateRegistration(task, TDelay);
                        return false;
                    }
                }
                if (this.mRegHandler.hasMessages(806)) {
                    IMSLog.i(LOG_TAG, phoneId, "do not update registration due to HYS");
                    return false;
                }
            } else {
                int TVolteHys = DmConfigHelper.readInt(this.mContext, "tvolte_hys_timer", 60, phoneId).intValue();
                IMSLog.i(LOG_TAG, phoneId, "Pending re-regi to T_VoLTE_hys[" + TVolteHys + "] secs.");
                if (this.mRegHandler.hasMessages(806)) {
                    this.mRegHandler.removeMessages(806);
                }
                this.mRegHandler.sendEmptyMessageDelayed(806, ((long) TVolteHys) * 1000);
                this.mRegMan.addPendingUpdateRegistration(task, TVolteHys);
                return false;
            }
        } else if (task.getMno() == Mno.ATT) {
            if (this.mRegMan.getImsIconManager(task.getPhoneId()) != null) {
                this.mRegMan.getImsIconManager(task.getPhoneId()).updateRegistrationIcon(false);
            }
        } else if (task.getMno() == Mno.SPRINT && !task.isRcsOnly()) {
            if (!(old.isDataRoaming == event.isDataRoaming && old.isVoiceRoaming == event.isVoiceRoaming)) {
                Log.i(LOG_TAG, "onNetworkChanged: roaming event changed, check location cache");
                task.getGovernor().onLocationCacheExpiry();
            }
            if (!(old.isPsOnlyReg == event.isPsOnlyReg && old.isVoiceRoaming == event.isVoiceRoaming)) {
                Log.i(LOG_TAG, "onNetworkEventChanged: roaming or ps-voice-only mode changed in registering/registered state");
                task.getGovernor().onServiceStateDataChanged(event.isPsOnlyReg, event.isVoiceRoaming);
            }
        } else if (task.getMno() == Mno.CMCC && task.isRcsOnly() && event.network == 16 && (iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.hasCsCall(phoneId)) {
            Log.i(LOG_TAG, "RCS deregister during CS call - GSM : same as OOS");
            task.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(task, true, true);
            return false;
        }
        task.setReason("by network event changed");
        this.mRegMan.updateRegistration(task, false);
        return true;
    }

    private boolean handleNetworkEventBeforeRegister(int phoneId, RegisterTask task, NetworkEvent event, NetworkEvent old) {
        int i = phoneId;
        RegisterTask registerTask = task;
        NetworkEvent networkEvent = event;
        NetworkEvent networkEvent2 = old;
        boolean isVopsTurnedOn = event.isVopsUpdated(old) == NetworkEvent.VopsState.ENABLED;
        if (task.getMno() == Mno.TMOUS && networkEvent.network == networkEvent2.network && !networkEvent.isWifiConnected) {
            IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: WiFi has turned off. release throttle.");
            task.getGovernor().releaseThrottle(2);
        }
        if (!(networkEvent.voiceOverPs != VoPsIndication.NOT_SUPPORTED || task.getState() != RegistrationConstants.RegisterTaskState.CONNECTING || task.getPdnType() != 11 || task.getRegistrationRat() == 18 || task.getMno() == Mno.ATT || task.getMno() == Mno.VZW || task.getMno() == Mno.TRUEMOVE || task.getMno() == Mno.AIS || task.getMno() == Mno.SPRINT || task.getMno().isKor())) {
            this.mRegMan.stopPdnConnectivity(task.getPdnType(), registerTask);
            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
        }
        if (task.getMno().isOneOf(Mno.BOG, Mno.TELECOM_ITALY, Mno.RJIL, Mno.H3G, Mno.CU) && task.getState() == RegistrationConstants.RegisterTaskState.CONNECTING && task.getProfile().getPdn().equals(DeviceConfigManager.IMS) && networkEvent.outOfService) {
            this.mRegMan.stopPdnConnectivity(task.getPdnType(), registerTask);
            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
        }
        if (RegistrationUtils.isCmcProfile(task.getProfile()) && task.getState() == RegistrationConstants.RegisterTaskState.CONNECTING && networkEvent.isWifiConnected && !networkEvent2.isWifiConnected) {
            IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: Wifi connected in CMC profile. Stop the conneting PDN");
            this.mRegMan.stopPdnConnectivity(task.getPdnType(), registerTask);
            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
        }
        if ((task.getMno() == Mno.CMCC || task.getMno() == Mno.CU) && task.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            if (!task.getGovernor().isDelayedDeregisterTimerRunning()) {
                if (!(networkEvent.network == 13 || networkEvent.network == 20)) {
                    task.getGovernor().resetAllPcscfChecked();
                }
                if (this.mTelephonyManager.getCallState() != 0 && networkEvent.network == 16) {
                    return false;
                }
            } else if (networkEvent.network == 13 || networkEvent.network == 20) {
                IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: LTE attached while DelayedDeregisterTimer running.");
                this.mRegMan.onDelayedDeregister(registerTask);
                return false;
            } else {
                IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: Do not stop IMS PDN on delayedDeregisterTimer running");
                return false;
            }
        }
        if (task.getMno() == Mno.ROGERS && isHandoverBetweenEpdgAndLeagacy(networkEvent.network, task.getRegistrationRat())) {
            registerTask.setReason("Handover Between VoWifi and 2G/3G");
            if (task.getState() != RegistrationConstants.RegisterTaskState.IDLE) {
                this.mRegMan.stopPdnConnectivity(task.getPdnType(), registerTask);
                registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            if (task.getGovernor().isThrottled()) {
                task.getGovernor().releaseThrottle(6);
            }
        }
        boolean isOperatorChanged = !TextUtils.isEmpty(networkEvent2.operatorNumeric) && !TextUtils.isEmpty(networkEvent.operatorNumeric) && !TextUtils.equals(networkEvent2.operatorNumeric, networkEvent.operatorNumeric);
        if (task.getGovernor().isThrottled()) {
            if (task.getMno().isOneOf(Mno.TELUS, Mno.KOODO, Mno.ROGERS, Mno.VTR, Mno.EASTLINK) && isOperatorChanged) {
                task.getGovernor().releaseThrottle(9);
            }
        }
        if (task.getMno() == Mno.APT && isOperatorChanged && task.getGovernor().isThrottled()) {
            task.getGovernor().releaseThrottle(6);
        }
        if (!networkEvent.isDataRoaming || task.getGovernor().allowRoaming() || !this.mPdnController.isNetworkRequested(registerTask)) {
            if (task.getMno() == Mno.VZW) {
                if (!TextUtils.equals(networkEvent.operatorNumeric, networkEvent2.operatorNumeric)) {
                    task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_PLMN_CHANGED);
                    if (task.getGovernor().getThrottleState() == IRegistrationGovernor.ThrottleState.PERMANENTLY_STOPPED) {
                        Log.i(LOG_TAG, "PLMN changed but Permanent Stopped. Do nothing!");
                    } else {
                        if (networkEvent.voiceOverPs == VoPsIndication.SUPPORTED) {
                            task.getGovernor().startTimsTimer(RegistrationConstants.REASON_PLMN_CHANGED);
                        }
                        if (task.getGovernor().isThrottled()) {
                            task.getGovernor().releaseThrottle(9);
                        }
                    }
                }
                if ((task.getState() == RegistrationConstants.RegisterTaskState.CONNECTING && !networkEvent.outOfService && networkEvent2.outOfService && networkEvent.network == 13) || isVopsTurnedOn) {
                    task.getGovernor().startTimsTimer(isVopsTurnedOn ? RegistrationConstants.REASON_VOPS_CHANGED : RegistrationConstants.REASON_IMS_PDN_REQUEST);
                }
                if (networkEvent.network != networkEvent2.network && task.getGovernor().isThrottled()) {
                    task.getGovernor().releaseThrottle(6);
                }
            }
            if (task.getMno() == Mno.SPRINT && ImsProfile.hasVolteService(task.getProfile()) && !(networkEvent2.isPsOnlyReg == networkEvent.isPsOnlyReg && networkEvent2.isVoiceRoaming == networkEvent.isVoiceRoaming)) {
                Log.i(LOG_TAG, "onNetworkEventChanged: roaming or ps-voice-only mode changed in idle/connecting state");
                task.getGovernor().onServiceStateDataChanged(RegistrationUtils.getNetworkEvent(phoneId).isPsOnlyReg, RegistrationUtils.getNetworkEvent(phoneId).isVoiceRoaming);
            }
            if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED) && networkEvent2.network != networkEvent.network) {
                reconnectPdn(registerTask);
            }
            return true;
        }
        IMSLog.i(LOG_TAG, i, "stopPdnConnectivity(), task " + registerTask);
        this.mRegMan.stopPdnConnectivity(task.getPdnType(), registerTask);
        registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
        return false;
    }

    private boolean isHandoverBetweenEpdgAndLeagacy(int newNetwork, int regiRat) {
        return (newNetwork == regiRat || regiRat == 0 || ((newNetwork != 18 || regiRat == 13) && (newNetwork == 13 || regiRat != 18))) ? false : true;
    }

    private void handleNetworkEventOnDeregistering(int phoneId, RegisterTask task, NetworkEvent event, NetworkEvent old) {
        if (task.getMno() == Mno.SPRINT && ImsProfile.hasVolteService(task.getProfile()) && !(old.isPsOnlyReg == event.isPsOnlyReg && old.isVoiceRoaming == event.isVoiceRoaming)) {
            IMSLog.i(LOG_TAG, phoneId, "onNetworkEventChanged: roaming or ps-voice-only mode changed in de-registering state");
            task.getGovernor().onServiceStateDataChanged(RegistrationUtils.getNetworkEvent(phoneId).isPsOnlyReg, RegistrationUtils.getNetworkEvent(phoneId).isVoiceRoaming);
        }
        if (task.getMno() == Mno.CU && task.mKeepPdn && task.getDeregiReason() == 2 && this.mRegMan.findBestNetwork(task.getPhoneId(), task.getProfile(), task.getGovernor()) == 0) {
            IMSLog.i(LOG_TAG, phoneId, "CU, if not in LTE,will stop pdn when in deregistering state caused by pdn lost");
            task.mKeepPdn = false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0178, code lost:
        if (r8.isOneOf(com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING, com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURED) != false) goto L_0x017a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleNetworkEvent(int r7, com.sec.internal.ims.core.RegisterTask r8, com.sec.internal.constants.ims.os.NetworkEvent r9, com.sec.internal.constants.ims.os.NetworkEvent r10, boolean r11, boolean r12) {
        /*
            r6 = this;
            com.sec.internal.constants.Mno r0 = r8.getMno()
            boolean r0 = r0.isKor()
            if (r0 == 0) goto L_0x0192
            boolean r0 = r8.isRcsOnly()
            java.lang.String r1 = "RegiMgr-NetEvtCtr"
            r2 = 2
            r3 = 0
            r4 = 1
            if (r0 != 0) goto L_0x0078
            boolean r0 = r9.isDataRoaming
            if (r0 == 0) goto L_0x0078
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r8.getGovernor()
            boolean r0 = r0.allowRoaming()
            if (r0 == 0) goto L_0x0078
            int r0 = r9.network
            r5 = 13
            if (r0 == r5) goto L_0x002f
            int r0 = r9.network
            r5 = 20
            if (r0 != r5) goto L_0x0039
        L_0x002f:
            com.sec.internal.constants.ims.os.VoPsIndication r0 = r9.voiceOverPs
            com.sec.internal.constants.ims.os.VoPsIndication r5 = com.sec.internal.constants.ims.os.VoPsIndication.SUPPORTED
            if (r0 != r5) goto L_0x0039
            boolean r0 = r9.outOfService
            if (r0 == 0) goto L_0x0078
        L_0x0039:
            java.lang.String r0 = "device moved into VoLTE roaming disabled condition, stop PDN request and set state to IDLE"
            com.sec.internal.log.IMSLog.i(r1, r7, r0)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = r8.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            if (r0 != r5) goto L_0x0049
            r8.setDeregiReason(r2)
        L_0x0049:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r0 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[r2]
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            r0[r3] = r5
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            r0[r4] = r5
            boolean r0 = r8.isOneOf(r0)
            if (r0 == 0) goto L_0x0063
            java.lang.String r0 = "onNetworkEventChanged: REGISTERED or REGISTERING"
            r8.setReason(r0)
            com.sec.internal.ims.core.RegistrationManagerBase r0 = r6.mRegMan
            r0.tryDeregisterInternal(r8, r3, r3)
        L_0x0063:
            com.sec.internal.ims.core.RegistrationManagerBase r0 = r6.mRegMan
            int r5 = r8.getPdnType()
            r0.stopPdnConnectivity(r5, r8)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r8.setState(r0)
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r8.getGovernor()
            r0.resetAllRetryFlow()
        L_0x0078:
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r8.getGovernor()
            boolean r0 = r0.isThrottled()
            if (r0 == 0) goto L_0x00a7
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r8.getGovernor()
            boolean r0 = r0.needImsNotAvailable()
            if (r0 == 0) goto L_0x00a7
            java.lang.String r0 = r10.operatorNumeric
            java.lang.String r5 = r9.operatorNumeric
            boolean r0 = android.text.TextUtils.equals(r0, r5)
            if (r0 == 0) goto L_0x009e
            com.sec.internal.ims.core.PdnController r0 = r6.mPdnController
            boolean r0 = r0.isEpsOnlyReg(r7)
            if (r0 == 0) goto L_0x00a7
        L_0x009e:
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r8.getGovernor()
            r5 = 9
            r0.releaseThrottle(r5)
        L_0x00a7:
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r8.getGovernor()
            boolean r0 = r0.isMobilePreferredForRcs()
            if (r0 == 0) goto L_0x017d
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r5 = "onNetworkEventChanged: event.isDataStateConnected: "
            r0.append(r5)
            boolean r5 = r9.isDataStateConnected
            r0.append(r5)
            java.lang.String r5 = " old.isDataStateConnected: "
            r0.append(r5)
            boolean r5 = r10.isDataStateConnected
            r0.append(r5)
            java.lang.String r5 = " event.outOfService: "
            r0.append(r5)
            boolean r5 = r9.outOfService
            r0.append(r5)
            java.lang.String r5 = " old.outOfService: "
            r0.append(r5)
            boolean r5 = r10.outOfService
            r0.append(r5)
            java.lang.String r5 = " task.getPdnType() "
            r0.append(r5)
            int r5 = r8.getPdnType()
            r0.append(r5)
            java.lang.String r5 = " rat: "
            r0.append(r5)
            int r5 = r8.getRegistrationRat()
            r0.append(r5)
            java.lang.String r5 = " isWifiConnected: "
            r0.append(r5)
            com.sec.internal.ims.core.PdnController r5 = r6.mPdnController
            boolean r5 = r5.isWifiConnected()
            r0.append(r5)
            java.lang.String r5 = " "
            r0.append(r5)
            com.sec.ims.settings.ImsProfile r5 = r8.getProfile()
            java.lang.String r5 = r5.getName()
            r0.append(r5)
            java.lang.String r5 = "("
            r0.append(r5)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = r8.getState()
            r0.append(r5)
            java.lang.String r5 = ")"
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            com.sec.internal.log.IMSLog.i(r1, r7, r0)
            boolean r0 = r9.isDataStateConnected
            if (r0 == 0) goto L_0x0134
            boolean r0 = r10.isDataStateConnected
            if (r0 == 0) goto L_0x013c
        L_0x0134:
            boolean r0 = r9.outOfService
            if (r0 == 0) goto L_0x017d
            boolean r0 = r10.outOfService
            if (r0 != 0) goto L_0x017d
        L_0x013c:
            com.sec.internal.ims.core.PdnController r0 = r6.mPdnController
            boolean r0 = r0.isWifiConnected()
            if (r0 == 0) goto L_0x017d
            int r0 = r8.getPdnType()
            if (r0 != r4) goto L_0x017d
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r0 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[r2]
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            r0[r3] = r5
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            r0[r4] = r5
            boolean r0 = r8.isOneOf(r0)
            if (r0 != 0) goto L_0x017a
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r0 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[r2]
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.RESOLVING
            r0[r3] = r5
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.RESOLVED
            r0[r4] = r5
            boolean r0 = r8.isOneOf(r0)
            if (r0 != 0) goto L_0x017a
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r0 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[r2]
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING
            r0[r3] = r2
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURED
            r0[r4] = r2
            boolean r0 = r8.isOneOf(r0)
            if (r0 == 0) goto L_0x017d
        L_0x017a:
            r6.isPreferredPdnForRCSRegister(r8, r7, r4)
        L_0x017d:
            if (r11 != 0) goto L_0x018d
            if (r12 == 0) goto L_0x0182
            goto L_0x018d
        L_0x0182:
            java.lang.String r0 = "onNetworkEventChanged: sendTryRegister"
            com.sec.internal.log.IMSLog.i(r1, r7, r0)
            com.sec.internal.ims.core.RegistrationManagerHandler r0 = r6.mRegHandler
            r0.sendTryRegister(r7)
            goto L_0x0192
        L_0x018d:
            java.lang.String r0 = "onNetworkEventChanged: do not call sendTryRegister"
            com.sec.internal.log.IMSLog.i(r1, r7, r0)
        L_0x0192:
            com.sec.internal.constants.ims.os.NetworkEvent$VopsState r0 = r9.isVopsUpdated(r10)
            com.sec.internal.constants.ims.os.NetworkEvent$VopsState r1 = com.sec.internal.constants.ims.os.NetworkEvent.VopsState.DISABLED
            if (r0 != r1) goto L_0x019d
            r6.handleVopsDisabledOnNetworkEventChanged(r8, r7)
        L_0x019d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.NetworkEventController.handleNetworkEvent(int, com.sec.internal.ims.core.RegisterTask, com.sec.internal.constants.ims.os.NetworkEvent, com.sec.internal.constants.ims.os.NetworkEvent, boolean, boolean):void");
    }

    private boolean updateEpdgStatusOnNetworkEventChanged(RegisterTask task, NetworkEvent event, NetworkEvent old) {
        if (task.getProfile().isEpdgSupported()) {
            if (event.isEpdgHOEvent(old)) {
                return true;
            }
            Mno mno = task.getMno();
            if (mno == Mno.VZW) {
                if ((this.mPdnController.isEpdgConnected(task.getPhoneId()) && event.network != old.network) || (this.mPdnController.isEpdgAvailable(task.getPhoneId()) && event.network == 18 && old.network == 18 && event.isPsOnlyReg != old.isPsOnlyReg)) {
                    RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                    registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(135, task), 121000);
                    return true;
                }
            }
            if (mno.isOneOf(Mno.TELE2NL, Mno.VODAFONE_UK, Mno.VODAFONE_IRELAND, Mno.VODAFONE_ROMANIA, Mno.VODAFONE_ALBANIA, Mno.STC_KSA, Mno.VIRGIN, Mno.TELEFONICA_CZ) && event.isDataRoaming && !task.getGovernor().allowRoaming()) {
                Log.i(LOG_TAG, "onNetworkChanged: VoWiFi Roaming not support");
                this.mRegMan.tryDeregisterInternal(task, false, false);
                this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                task.getGovernor().resetAllRetryFlow();
            }
        }
        return false;
    }

    private void handleOutOfServiceOnNetworkEvnentChanged(RegisterTask task, int phoneId) {
        IVolteServiceModule iVolteServiceModule;
        IMSLog.i(LOG_TAG, phoneId, "out of service.");
        Mno mno = SimUtil.getSimMno(phoneId);
        if (task.getRegistrationRat() != 18 && ConfigUtil.isRcsEur(mno) && task.isRcsOnly()) {
            Log.i(LOG_TAG, "set EVENT_RCS_DELAYED_DEREGISTER");
            this.mRegHandler.removeMessages(142);
            RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(142), 30000);
        }
        if (mno == Mno.CMCC && task.isRcsOnly() && (iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.hasCsCall(phoneId)) {
            Log.i(LOG_TAG, "RCS deregister OOS during CS call");
            task.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(task, true, true);
        }
        if (mno == Mno.EE_ESN) {
            Log.i(LOG_TAG, "ESN send local deregi and PDN disconnect");
            this.mRegMan.tryDeregisterInternal(task, true, false);
        }
        if (this.mImsFramework.getBoolean(task.getPhoneId(), GlobalSettingsConstants.Registration.REMOVE_ICON_NOSVC, false) && this.mRegMan.getImsIconManager(task.getPhoneId()) != null) {
            this.mRegMan.getImsIconManager(task.getPhoneId()).updateRegistrationIcon(false);
        }
    }

    private void handleSsacOnNetworkEventChanged(RegisterTask task, int phoneId, NetworkEvent event, NetworkEvent old) {
        if (task.getMno() == Mno.VZW && this.mRegHandler.hasMessages(121, Integer.valueOf(phoneId)) && !TextUtils.equals(event.operatorNumeric, old.operatorNumeric)) {
            if (task.getImsRegistration() == null) {
                IMSLog.i(LOG_TAG, phoneId, "onNetworkEventChanged: remove SSAC re-regi");
                this.mRegHandler.removeMessages(121, Integer.valueOf(phoneId));
            }
            IMSLog.i(LOG_TAG, phoneId, "onNetworkEventChanged: set SSAC to default");
            SlotBasedConfig.getInstance(phoneId).enableSsac(true);
        }
    }

    private void handleVopsDisabledOnNetworkEventChanged(RegisterTask task, int phoneId) {
        if (task.getMno() == Mno.VZW) {
            task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_VOPS_CHANGED);
            this.mRegHandler.removeMessages(132);
            if ((this.mTelephonyManager.isNetworkRoaming() || ImsUtil.isCdmalessEnabled(phoneId)) && !this.mRegMan.getCsfbSupported(phoneId)) {
                this.mRegMan.notifyImsNotAvailable(task, true);
            }
        } else if (task.getMno().isJpn()) {
            task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_VOPS_CHANGED);
            this.mRegHandler.removeMessages(132);
            if (task.getMno() == Mno.KDDI && this.mTelephonyManager.isNetworkRoaming()) {
                this.mRegMan.notifyImsNotAvailable(task, true);
            }
        } else {
            if (task.getMno().isOneOf(Mno.CTC, Mno.CTCMO) && !this.mRegMan.getCsfbSupported(phoneId)) {
                task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_VOPS_CHANGED);
                this.mRegHandler.removeMessages(132);
                this.mRegMan.notifyImsNotAvailable(task, true);
            }
        }
    }

    private void tryCmcRegisterOnNetworkEventChanged(NetworkEvent event, NetworkEvent old) {
        if (!this.mCmcAccountManager.isCmcProfileAdded() && !event.outOfService && old.outOfService) {
            this.mCmcAccountManager.startCmcRegistration();
        }
    }

    private void updateUtOnNetworkEventChanged(int phoneId, boolean simAvailable, NetworkEvent event, NetworkEvent old) {
        boolean utSwitch = true;
        boolean disableUtInRoaming = !this.mImsFramework.getBoolean(phoneId, GlobalSettingsConstants.SS.ENABLE_IN_ROAMING, true);
        if (simAvailable && !event.outOfService && disableUtInRoaming) {
            if (old.isDataRoaming == event.isDataRoaming) {
                if (old.network == event.network) {
                    return;
                }
                if (!(event.network == 18 || old.network == 18)) {
                    return;
                }
            }
            boolean isWifi = event.network == 18;
            if (event.isDataRoaming && !isWifi) {
                utSwitch = false;
            }
            IUtServiceModule usm = this.mImsFramework.getServiceModuleManager().getUtServiceModule();
            if (usm != null) {
                usm.enableUt(phoneId, utSwitch);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPreferredPdnForRCSRegister(RegisterTask task, int phoneId, boolean needDeregi) {
        int pdnType;
        if ((!task.getMno().isKor() && task.getMno() != Mno.TMOBILE) || (pdnType = task.getPdnType()) == 11 || pdnType == 15) {
            return true;
        }
        if (task.getMno().isKor()) {
            boolean needDelayedDeregister = task.getGovernor().isNeedDelayedDeregister();
            PdnController pdnController = this.mPdnController;
            int preferredPdnType = pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer());
            IMSLog.i(LOG_TAG, "isPreferredPdnForRCSRegister: isNeedDelayedDeregister [" + needDelayedDeregister + "], preferred PDN [" + preferredPdnType + "], needDeregi [" + needDeregi + "]");
            if (!task.getGovernor().isMobilePreferredForRcs() || needDeregi || !needDelayedDeregister || preferredPdnType != 1) {
                deregisterByDefaultNwChanged(task, phoneId, needDeregi);
            } else if (!this.mRegHandler.hasMessages(18)) {
                this.mEventLog.logAndAdd(phoneId, "isPreferredPdnForRCSRegister : Delay event");
                RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(18, task), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            } else {
                this.mEventLog.logAndAdd(phoneId, "isPreferredPdnForRCSRegister : Now pending");
            }
            return true;
        } else if (!this.mPdnController.isWifiConnected() || !RegistrationUtils.isMobileConnected(this.mContext)) {
            return true;
        } else {
            int preferredPdnType2 = this.mPdnController.translateNetworkBearer(this.mPdnController.getDefaultNetworkBearer());
            boolean ret = true;
            if (pdnType != preferredPdnType2) {
                ret = false;
                deregisterByDefaultNwChanged(task, phoneId, needDeregi);
            }
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask pendingTask = (RegisterTask) it.next();
                if (!(pendingTask == task || !pendingTask.isRcsOnly() || pendingTask.getPdnType() == preferredPdnType2)) {
                    deregisterByDefaultNwChanged(pendingTask, phoneId, needDeregi);
                }
            }
            return ret;
        }
    }

    private void deregisterByDefaultNwChanged(RegisterTask task, int phoneId, boolean needDeregi) {
        IMSLog.i(LOG_TAG, phoneId, "deregisterByDefaultNwChanged: " + task.getProfile().getName() + "(" + task.getState() + ") needDeregi(" + needDeregi + ")");
        PdnController pdnController = this.mPdnController;
        boolean isWifiPreferred = pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer()) == 1;
        if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            if (task.mMno != Mno.TMOBILE && !task.mMno.isKor()) {
                return;
            }
            if (!task.getGovernor().isMobilePreferredForRcs() || isWifiPreferred || needDeregi) {
                task.setDeregiReason(12);
                this.mRegMan.tryDeregisterInternal(task, false, false);
            }
        } else if (task.getMno().isKor()) {
            task.setState(RegistrationConstants.RegisterTaskState.IDLE);
            if (task.getGovernor().isMobilePreferredForRcs() && isWifiPreferred) {
                IMSLog.i(LOG_TAG, phoneId, "deregisterByDefaultNwChanged: stop pdn");
                this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDefaultNetworkStateChanged(int phoneId) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RegisterTask pendingTask = (RegisterTask) it.next();
            if (pendingTask.isRcsOnly()) {
                isPreferredPdnForRCSRegister(pendingTask, phoneId, false);
                break;
            }
        }
        RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(2, Integer.valueOf(phoneId)), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
    }

    /* access modifiers changed from: package-private */
    public void handOffEventTimeout(int phoneId) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, "handOffEventTimeout: mNetType = " + getNetType() + ", mWiFi = " + isWiFi());
        this.mRegMan.suspendRegister(false, phoneId);
    }

    /* access modifiers changed from: protected */
    public void onEpdgIkeError(int phoneId) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            ((RegisterTask) it.next()).getGovernor().onPdnRequestFailed(RegistrationConstants.IKE_AUTH_ERROR);
        }
    }

    /* access modifiers changed from: protected */
    public void onIpsecDisconnected(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onIpsecDisconnected:");
        int voiceCallType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, phoneId);
        boolean wfcEnabled = VowifiConfig.isEnabled(this.mContext, phoneId);
        int wfcPrefMode = VowifiConfig.getPrefMode(this.mContext, 2, phoneId);
        IMSLog.i(LOG_TAG, phoneId, "onIpsecDisconnected: VoWiFi : " + wfcEnabled + ", pref: " + wfcPrefMode + ", callType : " + voiceCallType);
        if (wfcEnabled && wfcPrefMode == 2 && voiceCallType == 1) {
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getMno().isTw() && task.getProfile().isEpdgSupported() && task.getProfile().getPdnType() == 11) {
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                        this.mRegMan.tryDeregisterInternal(task, false, false);
                    } else {
                        if (task.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTED, RegistrationConstants.RegisterTaskState.CONNECTING)) {
                            Log.i(LOG_TAG, "Stop pdn when ipsec disconnected.");
                            this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                            task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onVoicePreferredChanged(int phoneId) {
        Log.i(LOG_TAG, "onVoicePreferredChanged:");
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        if (iVolteServiceModule == null) {
            Log.e(LOG_TAG, "VolteServiceModule is not create yet so retry after 3 seconds");
            RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(123, phoneId, 0, (Object) null), 3000);
            return;
        }
        iVolteServiceModule.onVoWiFiSwitched(phoneId);
        if (this.mPdnController.isEpdgConnected(phoneId)) {
            EpdgManager epdgMgr = this.mVsm.getEpdgManager();
            int dataRat = this.mTelephonyManager.getDataNetworkType();
            boolean w2lHoSoon = false;
            if (epdgMgr != null) {
                w2lHoSoon = epdgMgr.isPossibleW2LHOAfterCallEnd();
                Log.i(LOG_TAG, "W2L available : " + w2lHoSoon);
            }
            if (dataRat != 13 || !w2lHoSoon) {
                this.mRegMan.updateRegistration(phoneId);
            } else {
                Log.i(LOG_TAG, "EpdgEventReceiver, waiting for W2L HO event w/o re-regi");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLocalIpChanged(RegisterTask task) {
        if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            task.setReason("local IP changed");
            task.setDeregiReason(5);
            this.mRegMan.tryDeregisterInternal(task, true, true);
        }
        if (!task.getMno().isKor() || !task.isRcsOnly()) {
            this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
            task.getGovernor().resetPcscfPreference();
            task.getGovernor().resetIPSecAllow();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onConfigUpdated(String item, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onConfigUpdated: " + item);
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm == null) {
            return false;
        }
        if (!TextUtils.isEmpty(item)) {
            if (TextUtils.indexOf(item, ':') != -1) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                simpleEventLog.logAndAdd(phoneId, "Invalid DM item : " + item);
                IMSLog.c(LogClass.REGI_CONFIG_UPDATE, phoneId + ",INVLD DM: " + item);
                return false;
            }
            this.mRegHandler.notifyDmValueChanged(item, phoneId);
        }
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            task.getGovernor().onConfigUpdated();
            if (!sm.getSimMno().isKor() || this.mRegMan.getOmadmState() != RegistrationManager.OmadmConfigState.FINISHED || task.isRcsOnly()) {
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    task.setReason("IMS configuration changed");
                    this.mRegMan.updateRegistration(task, false);
                } else if (task.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
                    reconnectPdn(task);
                }
            } else {
                Log.i(LOG_TAG, "onConfigUpdated:  mOmadmState is FINISHED");
            }
        }
        return true;
    }
}
