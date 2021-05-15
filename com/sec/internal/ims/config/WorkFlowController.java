package com.sec.internal.ims.config;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.CmccStrategy;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class WorkFlowController {
    private static final String LOG_TAG = "WorkFlowController";
    private final Map<Integer, ACSConfig> mAcsConfigs = new ConcurrentHashMap();
    private final Context mContext;
    private final SparseArray<String> mImsiList = new SparseArray<>();
    private final SparseBooleanArray mIsAcsFirstTryList = new SparseBooleanArray();
    private final SparseArray<String> mMsisdnList = new SparseArray<>();
    private final SparseArray<String> mRcsProfileList = new SparseArray<>();
    private final SparseArray<IWorkflow> mWorkflowList = new SparseArray<>();

    WorkFlowController(Context context) {
        this.mContext = context;
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            this.mRcsProfileList.put(sm.getSimSlotIndex(), "");
            this.mIsAcsFirstTryList.put(sm.getSimSlotIndex(), false);
            this.mAcsConfigs.put(Integer.valueOf(sm.getSimSlotIndex()), new ACSConfig());
            if (sm.getSimMno().isKor()) {
                getAcsConfig(sm.getSimSlotIndex()).resetAcsSettings();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IWorkflow getWorkflow(int phoneId) {
        return this.mWorkflowList.get(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void removeWorkFlow(int phoneId) {
        this.mWorkflowList.remove(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void initWorkflow(int phoneId, IWorkflow workflow) {
        if (workflow != null) {
            this.mWorkflowList.put(phoneId, workflow);
            workflow.init();
            saveIsAcsFirstTry(phoneId);
            IMSLog.i(LOG_TAG, phoneId, "workflow is created and init");
        }
    }

    /* access modifiers changed from: package-private */
    public ACSConfig getAcsConfig(int phoneId) {
        return this.mAcsConfigs.get(Integer.valueOf(phoneId));
    }

    /* access modifiers changed from: package-private */
    public void putRcsProfile(int phoneId, String rcsProfile) {
        this.mRcsProfileList.put(phoneId, rcsProfile);
    }

    /* access modifiers changed from: package-private */
    public String getRcsProfile(int phoneId) {
        return this.mRcsProfileList.get(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void saveIsAcsFirstTry(int phoneId) {
        boolean firstTry = true;
        try {
            if (getWorkflow(phoneId) != null && !TextUtils.isEmpty(getWorkflow(phoneId).read("root/token/token").get("root/token/token"))) {
                Log.i(LOG_TAG, "isACSFirstTry: token is valid");
                firstTry = false;
            }
        } catch (NullPointerException | NumberFormatException e) {
            Log.i(LOG_TAG, "exception on reading config. return true ");
            e.printStackTrace();
        }
        this.mIsAcsFirstTryList.put(phoneId, firstTry);
    }

    /* access modifiers changed from: package-private */
    public boolean getIsAcsFirstTry(int phoneId) {
        return this.mIsAcsFirstTryList.get(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void removeIsAcsFirstTry(int phoneId) {
        this.mIsAcsFirstTryList.delete(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void clearToken(int phoneId) {
        IWorkflow workflow = this.mWorkflowList.get(phoneId);
        if (workflow != null) {
            workflow.clearToken();
        }
    }

    /* access modifiers changed from: package-private */
    public void onBootCompleted() {
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            IWorkflow workflow = this.mWorkflowList.get(i);
            if (workflow != null) {
                workflow.onBootCompleted();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSimRefresh(int phoneId) {
        if (this.mWorkflowList.get(phoneId) != null) {
            this.mWorkflowList.get(phoneId).cleanup();
            IMSLog.i(LOG_TAG, phoneId, "onSimRefresh: remove workflow");
            this.mWorkflowList.remove(phoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public int getMsisdnSkipCount(int phoneId) {
        try {
            if (getWorkflow(phoneId) == null) {
                return 0;
            }
            int skipcount = Integer.parseInt(getWorkflow(phoneId).read(ConfigConstants.PATH.MSISDN_SKIP_COUNT).get(ConfigConstants.PATH.MSISDN_SKIP_COUNT));
            Log.i(LOG_TAG, "MsisdnSkipCount : " + skipcount);
            return skipcount;
        } catch (NullPointerException | NumberFormatException e) {
            Log.i(LOG_TAG, "exception on reading config. return 0");
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int getCurrentRcsConfigVersion(int phoneId) {
        try {
            if (getWorkflow(phoneId) != null) {
                return Integer.parseInt(getWorkflow(phoneId).read("root/vers/version").get("root/vers/version"));
            }
            return 0;
        } catch (NullPointerException | NumberFormatException e) {
            Log.i(LOG_TAG, "exception on reading config. return version 0");
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteConfiguration(int phoneId) {
        IWorkflow workflow = this.mWorkflowList.get(phoneId);
        if (workflow != null) {
            workflow.clearAutoConfigStorage();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSimInfochanged(int phoneId, boolean isRemoteConfigNeeded) {
        boolean retChanged = false;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null) {
            Log.i(LOG_TAG, "changedSimInfo: SimManager null");
            return false;
        }
        String imsi = sm.getImsi();
        if (TextUtils.isEmpty(imsi)) {
            Log.i(LOG_TAG, "changedSimInfo: getImsi null or empty");
            return false;
        }
        String imsi_saved = this.mImsiList.get(phoneId);
        if (!TextUtils.equals(imsi_saved, imsi) && !isRemoteConfigNeeded) {
            IMSLog.i(LOG_TAG, phoneId, "changedSimInfo: imsi is changed, " + toLastString(imsi_saved) + " ==> " + toLastString(imsi));
            imsi_saved = imsi;
            retChanged = true;
        }
        this.mImsiList.put(phoneId, imsi_saved);
        Mno mno = sm.getSimMno();
        if (!(RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) instanceof CmccStrategy) || mno == null || mno == Mno.CMCC) {
            String rcsAs = ConfigUtil.getAcsServerType(this.mContext, phoneId);
            if (mno == Mno.SPRINT || ImsConstants.RCS_AS.JIBE.equals(rcsAs) || ImsConstants.RCS_AS.SEC.equals(rcsAs) || isRemoteConfigNeeded) {
                String msisdn = sm.getLine1Number();
                if (msisdn == null) {
                    IMSLog.i(LOG_TAG, phoneId, "changedSimInfo: getLine1Number null");
                    return false;
                }
                String msisdn_saved = this.mMsisdnList.get(phoneId);
                if (!TextUtils.equals(msisdn_saved, msisdn)) {
                    IMSLog.i(LOG_TAG, phoneId, "changedSimInfo: msisdn is changed, " + toLastString(msisdn_saved) + " ==> " + toLastString(msisdn));
                    msisdn_saved = msisdn;
                    retChanged = true;
                }
                this.mMsisdnList.put(phoneId, msisdn_saved);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("changedSimInfo: ");
            sb.append(retChanged ? "changed" : "not changed");
            IMSLog.i(LOG_TAG, phoneId, sb.toString());
            return retChanged;
        }
        Log.i(LOG_TAG, "changedSimInfo: Non CMCC sim, not suport RCS: " + mno);
        return false;
    }

    public IStorageAdapter getStorage(int phoneId) {
        IWorkflow workflow = this.mWorkflowList.get(phoneId);
        if (workflow != null) {
            return workflow.getStorage();
        }
        return null;
    }

    private String toLastString(String orgStr) {
        if (orgStr == null || orgStr.length() <= 2) {
            return "";
        }
        return orgStr.substring(orgStr.length() - 2);
    }

    public void dump() {
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            Optional.ofNullable(this.mWorkflowList.get(i)).ifPresent($$Lambda$rz2ocVrocDvgkeoikXYnhHkbINU.INSTANCE);
        }
    }
}
