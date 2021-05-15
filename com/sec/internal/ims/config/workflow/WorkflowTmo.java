package com.sec.internal.ims.config.workflow;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class WorkflowTmo extends WorkflowBase {
    /* access modifiers changed from: private */
    public static final Uri CONFIG_PARAMS_URI = Uri.parse("content://com.samsung.ims.entitlementconfig.provider/config");
    private static final String DEVICE_CONFIG = "device_config";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowTmo.class.getSimpleName();
    private static final String TAG_AUTOCONFIG_HEAD = "<RCSConfig>";
    private static final String TAG_AUTOCONFIG_TAIL = "</RCSConfig>";
    private static final String TAG_NEW_XML_HEADER = "<?xml version=\"1.0\"?>";
    protected boolean isNoInitialData = false;
    protected boolean isObserverRegisted = false;
    /* access modifiers changed from: private */
    public ConfigurationParamObserver mConfigurationParamObserver;
    protected String mConfigurationParams;

    public WorkflowTmo(Looper looper, Context context, Handler handler, Mno mno, int phoneId) {
        super(looper, context, handler, mno, phoneId);
        this.mConfigurationParamObserver = new ConfigurationParamObserver(context);
        registerContentObserver();
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "WorkflowTmo message :" + msg.what);
        int i2 = msg.what;
        if (i2 == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "forced startAutoConfig");
            this.mStartForce = true;
        } else if (i2 != 1) {
            super.handleMessage(msg);
            return;
        }
        if (this.sIsConfigOngoing) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig:Already started");
            return;
        }
        this.sIsConfigOngoing = true;
        int oldVersion = getVersion();
        String str2 = LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.i(str2, i3, "AutoConfig:START, oldVersion=" + oldVersion);
        this.mPowerController.lock();
        work();
        int newVersion = getVersion();
        String str3 = LOG_TAG;
        int i4 = this.mPhoneId;
        IMSLog.i(str3, i4, "AutoConfig:FINISH, newVersion=" + newVersion);
        setCompleted(true);
        setLastErrorCode(this.mLastErrorCodeNonRemote);
        this.mModuleHandler.sendMessage(obtainMessage(3, oldVersion, newVersion, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.sIsConfigOngoing = false;
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = new Initialize();
        while (next != null) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "NoInitialDataException occur:" + e.getMessage());
                addEventLog(LOG_TAG + "No valid device config params, skip autoconfig");
                IMSLog.c(LogClass.WFTJ_EXCEPTION, this.mPhoneId + ",NODC");
                this.isNoInitialData = true;
                next = null;
                e.printStackTrace();
            } catch (Exception e2) {
                if (e2.getMessage() != null) {
                    String str2 = LOG_TAG;
                    Log.i(str2, "unknown exception occur:" + e2.getMessage());
                }
                next = null;
                e2.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        return null;
    }

    protected class Initialize implements WorkflowBase.Workflow {
        protected Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Initialize:");
            WorkflowBase.Workflow next = null;
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowTmo.this.getOpMode().ordinal()];
            if (i == 1 || i == 2 || i == 3) {
                next = new Fetch();
            } else if (i == 4 || i == 5) {
                next = null;
            }
            if (WorkflowTmo.this.mStartForce) {
                return new Fetch();
            }
            return next;
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowTmo$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        static {
            int[] iArr = new int[WorkflowBase.OpMode.values().length];
            $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode = iArr;
            try {
                iArr[WorkflowBase.OpMode.ACTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_TEMPORARY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DORMANT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_PERMANENTLY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    protected class Fetch implements WorkflowBase.Workflow {
        protected Fetch() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Fetch:");
            if (WorkflowTmo.this.mConfigurationParamObserver.getCurrentConfigurationParams()) {
                return new Parse();
            }
            return null;
        }
    }

    protected class Parse implements WorkflowBase.Workflow {
        protected Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Parse:");
            Map<String, String> parsedXml = WorkflowTmo.this.mXmlParser.parse(WorkflowTmo.this.mConfigurationParams);
            if (parsedXml == null) {
                throw new InvalidXmlException("no parsed xml data.");
            } else if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                throw new InvalidXmlException("config xml must contain at least 2 items(version & validity).");
            } else {
                WorkflowTmo.this.mParamHandler.moveHttpParam(parsedXml);
                parsedXml.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, WorkflowTmo.this.mConfigurationParams);
                WorkflowTmo.this.mSharedInfo.setParsedXml(parsedXml);
                return new Store();
            }
        }
    }

    protected class Store implements WorkflowBase.Workflow {
        protected Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Store:");
            WorkflowTmo workflowTmo = WorkflowTmo.this;
            workflowTmo.setOpMode(workflowTmo.getOpMode(workflowTmo.mSharedInfo.getParsedXml()), WorkflowTmo.this.mSharedInfo.getParsedXml());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode mode, Map<String, String> data) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "new operation mode :" + mode.name());
        int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[mode.ordinal()];
        if (i2 != 1) {
            if (i2 != 2) {
                if (i2 == 3) {
                    if (getVersion() != WorkflowBase.OpMode.DORMANT.value()) {
                        setVersionBackup(getVersion());
                    }
                    setVersion(mode.value());
                    return;
                } else if (!(i2 == 4 || i2 == 5)) {
                    return;
                }
            }
            clearStorage();
            setVersion(mode.value());
            setValidity(mode.value());
        } else if (data != null) {
            if (getVersion() >= getVersion(data)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "the same or lower version. update the data");
            }
            writeDataToStorage(data);
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "null data. remain previous mode & data");
        }
    }

    public boolean checkNetworkConnectivity() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "checkNetworkConnectivity is false because device config is used");
        return false;
    }

    private void registerContentObserver() {
        if (!this.isObserverRegisted) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "ConfigurationParamObserver is registed.");
            this.mConfigurationParamObserver.registerObserver();
            this.isObserverRegisted = true;
        }
    }

    private void unregisterContentObserver() {
        if (this.isObserverRegisted) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "ConfigurationParamObserver is unregisted.");
            this.mConfigurationParamObserver.unregisterObserver();
            this.isObserverRegisted = false;
        }
    }

    public void cleanup() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup workflow");
        unregisterContentObserver();
        this.isNoInitialData = false;
        this.mTelephony.cleanup();
    }

    public void onBootCompleted() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onBootCompleted");
        if (this.isNoInitialData) {
            this.isNoInitialData = false;
            sendEmptyMessage(0);
        }
    }

    private class ConfigurationParamObserver extends ContentObserver {
        private static final int AUTOCONFIG_START_DELAY = 2000;
        private Context mContext;

        public ConfigurationParamObserver(Context context) {
            super(new Handler());
            this.mContext = context;
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "device config is changed so start autoconfiguration.");
            WorkflowTmo workflowTmo = WorkflowTmo.this;
            workflowTmo.addEventLog(WorkflowTmo.LOG_TAG + "Device config is changed, start autoconfig");
            IMSLog.c(LogClass.WFTJ_ON_CHANGE, WorkflowTmo.this.mPhoneId + ",CHDC");
            WorkflowTmo.this.isNoInitialData = false;
            WorkflowTmo.this.sendEmptyMessageDelayed(0, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        }

        public void registerObserver() {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "ConfigurationParamObserver/registerObserver()");
            try {
                this.mContext.getContentResolver().registerContentObserver(WorkflowTmo.CONFIG_PARAMS_URI, false, this);
            } catch (SecurityException e) {
            }
        }

        public void unregisterObserver() {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "ConfigurationParamObserver/unregisterObserver()");
            try {
                this.mContext.getContentResolver().unregisterContentObserver(this);
            } catch (SecurityException e) {
            }
        }

        /* access modifiers changed from: private */
        public boolean getCurrentConfigurationParams() throws Exception {
            getAutoConfigFromDb(this.mContext.getContentResolver(), WorkflowTmo.CONFIG_PARAMS_URI);
            if (WorkflowTmo.this.mConfigurationParams == null) {
                Log.i(WorkflowTmo.LOG_TAG, "Not the correct imsi");
                return false;
            }
            createNewAutoConfigXml();
            return true;
        }

        private void getAutoConfigFromDb(ContentResolver resolver, Uri uri) {
            String value = "";
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(WorkflowTmo.this.mPhoneId);
            String imsi = sm == null ? "" : sm.getImsi();
            String access$000 = WorkflowTmo.LOG_TAG;
            int i = WorkflowTmo.this.mPhoneId;
            IMSLog.s(access$000, i, "imsi: " + imsi);
            Cursor cursor = resolver.query(uri, new String[]{"device_config"}, "imsi=?", new String[]{imsi}, (String) null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        value = cursor.getString(0);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            WorkflowTmo.this.mConfigurationParams = value;
            return;
            throw th;
        }

        private void createNewAutoConfigXml() throws Exception {
            int start = WorkflowTmo.this.mConfigurationParams.indexOf(WorkflowTmo.TAG_AUTOCONFIG_HEAD) + WorkflowTmo.TAG_AUTOCONFIG_HEAD.length();
            int end = WorkflowTmo.this.mConfigurationParams.indexOf(WorkflowTmo.TAG_AUTOCONFIG_TAIL);
            try {
                WorkflowTmo.this.mConfigurationParams = WorkflowTmo.this.mConfigurationParams.substring(start, end);
                WorkflowTmo workflowTmo = WorkflowTmo.this;
                workflowTmo.mConfigurationParams = WorkflowTmo.TAG_NEW_XML_HEADER + WorkflowTmo.this.mConfigurationParams;
            } catch (StringIndexOutOfBoundsException e) {
                WorkflowTmo.this.mConfigurationParams = "";
                throw new NoInitialDataException("Configuration Params in ContentProvider is not valid");
            }
        }
    }
}
