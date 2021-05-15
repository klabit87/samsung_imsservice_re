package com.sec.internal.ims.config;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceTelstra;
import com.sec.internal.ims.config.workflow.WorkflowAtt;
import com.sec.internal.ims.config.workflow.WorkflowBell;
import com.sec.internal.ims.config.workflow.WorkflowCmcc;
import com.sec.internal.ims.config.workflow.WorkflowInterop;
import com.sec.internal.ims.config.workflow.WorkflowJibe;
import com.sec.internal.ims.config.workflow.WorkflowLocalFile;
import com.sec.internal.ims.config.workflow.WorkflowLocalFilefromSDcard;
import com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice;
import com.sec.internal.ims.config.workflow.WorkflowRjil;
import com.sec.internal.ims.config.workflow.WorkflowSec;
import com.sec.internal.ims.config.workflow.WorkflowTmo;
import com.sec.internal.ims.config.workflow.WorkflowUp;
import com.sec.internal.ims.config.workflow.WorkflowVzw;
import com.sec.internal.ims.config.workflow.WorkflowVzwMvs;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.log.IMSLog;

public class CustomizationManager {
    private final String LOG_TAG;

    public CustomizationManager() {
        String simpleName = CustomizationManager.class.getSimpleName();
        this.LOG_TAG = simpleName;
        Log.i(simpleName, "init CustomizationManager");
    }

    public IWorkflow getConfigManager(Looper looper, Context context, Handler handler, int phoneId, boolean hasChatbotService) {
        Context context2 = context;
        Handler handler2 = handler;
        int i = phoneId;
        Mno mno = SimUtil.getSimMno(phoneId);
        int autoconfigSource = ConfigUtil.getAutoconfigSourceWithFeature(context2, i, 0);
        if (autoconfigSource == 2) {
            IMSLog.i(this.LOG_TAG, i, "get config from local file.");
            return new WorkflowLocalFile(looper, context, handler, mno, phoneId);
        } else if (autoconfigSource == 3) {
            IMSLog.i(this.LOG_TAG, i, "get config from SDcard.");
            return new WorkflowLocalFilefromSDcard(looper, context, handler, mno, phoneId);
        } else {
            String rcsProfile = ConfigUtil.getRcsProfileLoaderInternalWithFeature(context2, mno.getName(), i);
            IMSLog.c(LogClass.CZM_RCSP, i + "," + mno + ",RCSP:" + rcsProfile);
            if (ImsProfile.isRcsUpProfile(rcsProfile)) {
                String rcsAs = ConfigUtil.getAcsServerType(context2, i);
                IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
                boolean isRemoteConfigNeeded = mnoStrategy != null && mnoStrategy.isRemoteConfigNeeded(i);
                int isMvsAuthSupported = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.RCS_SUPPORT_MVS_AUTH, false);
                if (!ImsConstants.RCS_AS.JIBE.equals(rcsAs) || !hasChatbotService) {
                    boolean isMvsAuthSupported2 = isMvsAuthSupported;
                    if (ImsConstants.RCS_AS.SEC.equals(rcsAs)) {
                        IMSLog.i(this.LOG_TAG, i, "WorkflowSec");
                        return new WorkflowSec(looper, context, handler, mno, phoneId);
                    } else if (isRemoteConfigNeeded) {
                        String str = this.LOG_TAG;
                        IMSLog.i(str, i, "WorkflowVzw: isMvsAuthSupported: " + isMvsAuthSupported2);
                        return isMvsAuthSupported2 ? new WorkflowVzwMvs(looper, context, handler, mno, phoneId) : new WorkflowVzw(looper, context, handler, mno, phoneId);
                    } else if (mno.equals(Mno.BELL)) {
                        IMSLog.i(this.LOG_TAG, i, "WorkflowBell");
                        return new WorkflowBell(looper, context, handler, mno, phoneId);
                    } else if (mno.equals(Mno.RJIL)) {
                        IMSLog.i(this.LOG_TAG, i, "WorkflowRjil");
                        return new WorkflowRjil(looper, context, handler, mno, phoneId);
                    } else if (mno.equals(Mno.CMCC)) {
                        IMSLog.i(this.LOG_TAG, i, "WorkflowCmcc");
                        return new WorkflowCmcc(looper, context, handler, mno, phoneId);
                    } else if (ImsConstants.RCS_AS.INTEROP.equals(rcsAs)) {
                        IMSLog.i(this.LOG_TAG, i, "WorkflowInterop");
                        return new WorkflowInterop(looper, context, handler, mno, phoneId);
                    } else {
                        IMSLog.i(this.LOG_TAG, i, "WorkflowUp");
                        return new WorkflowUp(looper, context, handler, mno, phoneId);
                    }
                } else {
                    IMSLog.i(this.LOG_TAG, i, "WorkflowJibe");
                    int i2 = autoconfigSource;
                    int autoconfigSource2 = isMvsAuthSupported;
                    return new WorkflowJibe(looper, context, handler, mno, phoneId);
                }
            } else {
                if (mno.equals(Mno.CMCC)) {
                    IMSLog.i(this.LOG_TAG, i, "WorkflowCmcc");
                    return new WorkflowCmcc(looper, context, handler, mno, phoneId);
                } else if (mno.equals(Mno.RJIL)) {
                    IMSLog.i(this.LOG_TAG, i, "WorkflowRjil");
                    return new WorkflowRjil(looper, context, handler, mno, phoneId);
                } else if (mno.equals(Mno.ATT)) {
                    if (SimUtil.isSoftphoneEnabled()) {
                        IMSLog.i(this.LOG_TAG, i, "Use local config for SoftPhone");
                        return new WorkflowLocalFile(looper, context, handler, mno, phoneId);
                    }
                    IMSLog.i(this.LOG_TAG, i, "WorkflowAtt");
                    return new WorkflowAtt(looper, context, handler, mno, phoneId);
                } else if (mno.equals(Mno.TELSTRA)) {
                    IMSLog.i(this.LOG_TAG, i, "WorkflowPrimaryDevice for Telstra");
                    return new WorkflowPrimaryDevice(looper, context, handler, mno, new TelephonyAdapterPrimaryDeviceTelstra(context2, handler2, i), phoneId);
                } else if (mno.equals(Mno.TMOUS)) {
                    IMSLog.i(this.LOG_TAG, i, "WorkflowTmo");
                    return new WorkflowTmo(looper, context, handler, mno, phoneId);
                } else {
                    IMSLog.i(this.LOG_TAG, i, "WorkflowPrimaryDevice");
                    return new WorkflowPrimaryDevice(looper, context, handler, mno, new TelephonyAdapterPrimaryDevice(context2, handler2, i), phoneId);
                }
            }
        }
    }
}
