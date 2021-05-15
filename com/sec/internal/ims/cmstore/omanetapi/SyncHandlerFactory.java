package com.sec.internal.ims.cmstore.omanetapi;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.LineManager;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.CalllogDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.FaxDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.MessageDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.VvmDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.OMAObjectUpdateScheduler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.CallLogSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.FaxSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.MessageSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.VvmGreetingSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.VvmSyncHandler;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncHandlerFactory {
    public static final String TAG = SyncHandlerFactory.class.getSimpleName();
    private Context mContext;
    private Map<SyncParam, BaseDataChangeHandler> mDataChangeHandlerPool = new HashMap();
    private Map<SyncParam, BaseDeviceDataUpdateHandler> mDeviceDataUpdatePool = new HashMap();
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final INetAPIEventListener mINetAPIEventListener;
    private final LineManager mLineManager;
    private Looper mLooper;
    private Map<SyncParam, BaseSyncHandler> mSyncHandlerPool = new HashMap();
    private final IUIEventCallback mUIInterface;

    public SyncHandlerFactory(Looper looper, Context context, INetAPIEventListener APIEventListener, IUIEventCallback uicallback, LineManager linemanager, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        this.mContext = context;
        this.mLooper = looper;
        this.mINetAPIEventListener = APIEventListener;
        this.mUIInterface = uicallback;
        this.mLineManager = linemanager;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        registerLineListener();
    }

    private void registerLineListener() {
        this.mLineManager.registerLineStatusOberser(new LineManager.LineStatusObserver() {
            public void onLineAdded(String line) {
                String str = SyncHandlerFactory.TAG;
                Log.i(str, "onLineAdded: " + IMSLog.checker(line));
            }
        });
    }

    public BaseSyncHandler getSyncHandlerInstance(SyncParam param) {
        if (this.mSyncHandlerPool.containsKey(param)) {
            return this.mSyncHandlerPool.get(param);
        }
        String line = param.mLine;
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[param.mType.ordinal()];
        if (i == 1) {
            BaseSyncHandler msg = new MessageSyncHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mSyncHandlerPool.put(param, msg);
            return msg;
        } else if (i == 2) {
            BaseSyncHandler greeting = new VvmGreetingSyncHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.VM_GREETINGS, this.mICloudMessageManagerHelper);
            this.mSyncHandlerPool.put(param, greeting);
            return greeting;
        } else if (i == 3) {
            BaseSyncHandler vm = new VvmSyncHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.VM, this.mICloudMessageManagerHelper);
            this.mSyncHandlerPool.put(param, vm);
            return vm;
        } else if (i == 4) {
            BaseSyncHandler fax = new FaxSyncHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.FAX, this.mICloudMessageManagerHelper);
            this.mSyncHandlerPool.put(param, fax);
            return fax;
        } else if (i != 5) {
            BaseSyncHandler defaulthandler = new MessageSyncHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mSyncHandlerPool.put(param, defaulthandler);
            return defaulthandler;
        } else {
            BaseSyncHandler calllog = new CallLogSyncHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.CALLLOG, this.mICloudMessageManagerHelper);
            this.mSyncHandlerPool.put(param, calllog);
            return calllog;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.SyncHandlerFactory$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType;

        static {
            int[] iArr = new int[SyncMsgType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType = iArr;
            try {
                iArr[SyncMsgType.MESSAGE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[SyncMsgType.VM_GREETINGS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[SyncMsgType.VM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[SyncMsgType.FAX.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[SyncMsgType.CALLLOG.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public List<BaseSyncHandler> getAllSyncHandlerInstances() {
        List<BaseSyncHandler> rel = new ArrayList<>();
        for (Map.Entry<SyncParam, BaseSyncHandler> pair : this.mSyncHandlerPool.entrySet()) {
            rel.add(pair.getValue());
        }
        return rel;
    }

    public void clearAllSyncHandlerInstances() {
        this.mSyncHandlerPool.clear();
    }

    public List<BaseSyncHandler> getAllSyncHandlerInstancesByLine(String line) {
        List<BaseSyncHandler> rel = new ArrayList<>();
        if (TextUtils.isEmpty(line)) {
            return rel;
        }
        for (Map.Entry<SyncParam, BaseSyncHandler> pair : this.mSyncHandlerPool.entrySet()) {
            if (pair.getKey().mLine.equals(line)) {
                rel.add(pair.getValue());
            }
        }
        return rel;
    }

    public BaseDataChangeHandler getDataChangeHandlerInstance(SyncParam param) {
        if (this.mDataChangeHandlerPool.containsKey(param)) {
            return this.mDataChangeHandlerPool.get(param);
        }
        String line = param.mLine;
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[param.mType.ordinal()];
        if (i == 1) {
            BaseDataChangeHandler msg = new MessageDataChangeHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(param, msg);
            return msg;
        } else if (i == 2 || i == 3) {
            BaseDataChangeHandler vm = new VvmDataChangeHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.VM, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(param, vm);
            return vm;
        } else if (i == 4) {
            BaseDataChangeHandler fax = new FaxDataChangeHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.FAX, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(param, fax);
            return fax;
        } else if (i != 5) {
            BaseDataChangeHandler defaulthandler = new MessageDataChangeHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(param, defaulthandler);
            return defaulthandler;
        } else {
            BaseDataChangeHandler calllog = new CalllogDataChangeHandler(this.mLooper, this.mContext, this.mINetAPIEventListener, this.mUIInterface, line, SyncMsgType.CALLLOG, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(param, calllog);
            return calllog;
        }
    }

    public List<BaseDataChangeHandler> getAllDataChangeHandlerInstances() {
        List<BaseDataChangeHandler> rel = new ArrayList<>();
        for (Map.Entry<SyncParam, BaseDataChangeHandler> pair : this.mDataChangeHandlerPool.entrySet()) {
            rel.add(pair.getValue());
        }
        return rel;
    }

    public void clearAllDataChangeHandlerInstances() {
        this.mDataChangeHandlerPool.clear();
    }

    public List<BaseDataChangeHandler> getAllDataChangeHandlerInstancesByLine(String line) {
        List<BaseDataChangeHandler> rel = new ArrayList<>();
        if (TextUtils.isEmpty(line)) {
            return rel;
        }
        for (Map.Entry<SyncParam, BaseDataChangeHandler> pair : this.mDataChangeHandlerPool.entrySet()) {
            if (pair.getKey().mLine.equals(line)) {
                rel.add(pair.getValue());
            }
        }
        return rel;
    }

    public BaseDeviceDataUpdateHandler getDeviceDataUpdateHandlerInstance(SyncParam param) {
        BaseDeviceDataUpdateHandler handler;
        String str = TAG;
        Log.d(str, "getDeviceDataUpdateHandlerInstance: " + param);
        if (this.mDeviceDataUpdatePool.containsKey(param)) {
            return this.mDeviceDataUpdatePool.get(param);
        }
        String line = param.mLine;
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[param.mType.ordinal()];
        if (i == 1) {
            handler = new OMAObjectUpdateScheduler(this.mLooper, this.mContext, this.mINetAPIEventListener, line, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(param, handler);
        } else if (i == 3) {
            handler = new OMAObjectUpdateScheduler(this.mLooper, this.mContext, this.mINetAPIEventListener, line, SyncMsgType.VM, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(param, handler);
        } else if (i == 4) {
            handler = new OMAObjectUpdateScheduler(this.mLooper, this.mContext, this.mINetAPIEventListener, line, SyncMsgType.FAX, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(param, handler);
        } else if (i != 5) {
            handler = new OMAObjectUpdateScheduler(this.mLooper, this.mContext, this.mINetAPIEventListener, line, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(param, handler);
        } else {
            handler = new OMAObjectUpdateScheduler(this.mLooper, this.mContext, this.mINetAPIEventListener, line, SyncMsgType.CALLLOG, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(param, handler);
        }
        handler.start();
        return handler;
    }

    public List<BaseDeviceDataUpdateHandler> getAllDeviceDataUpdateHandlerInstances() {
        List<BaseDeviceDataUpdateHandler> rel = new ArrayList<>();
        for (Map.Entry<SyncParam, BaseDeviceDataUpdateHandler> pair : this.mDeviceDataUpdatePool.entrySet()) {
            rel.add(pair.getValue());
        }
        return rel;
    }

    public void clearAllDeviceDataUpdateHandlerInstances() {
        this.mDeviceDataUpdatePool.clear();
    }

    public List<BaseDeviceDataUpdateHandler> getAllDeviceDataUpdateHandlerInstancesByLine(String line) {
        List<BaseDeviceDataUpdateHandler> rel = new ArrayList<>();
        if (TextUtils.isEmpty(line)) {
            return rel;
        }
        for (Map.Entry<SyncParam, BaseDeviceDataUpdateHandler> pair : this.mDeviceDataUpdatePool.entrySet()) {
            if (pair.getKey().mLine.equals(line)) {
                rel.add(pair.getValue());
            }
        }
        return rel;
    }
}
