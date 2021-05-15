package com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGetVvmProfile;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageVvmProfileAttributePut;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageVvmProfileUpdate;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.ObjectReferenceList;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VvmHandler extends Handler implements IAPICallFlowListener, IControllerCommonInterface {
    private static final String TAG = VvmHandler.class.getSimpleName();
    private final BufferDBTranslation mBufferDbTranslation;
    private final ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final INetAPIEventListener mINetAPIEventListener;
    private final RegistrantList mUpdateFromCloudRegistrants = new RegistrantList();

    public VvmHandler(Looper looper, Context context, INetAPIEventListener netApiEventListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mBufferDbTranslation = new BufferDBTranslation(context, iCloudMessageManagerHelper);
        this.mINetAPIEventListener = netApiEventListener;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    public void registerForUpdateFromCloud(Handler h, int what, Object obj) {
        this.mUpdateFromCloudRegistrants.add(new Registrant(h, what, obj));
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String str = TAG;
        Log.i(str, "message: " + msg.what);
        OMASyncEventType type = OMASyncEventType.valueOf(msg.what);
        if (type != null) {
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[type.ordinal()]) {
                case 1:
                    setUpVvmDataUpdate((BufferDBChangeParamList) msg.obj);
                    return;
                case 2:
                    BufferDBChangeParam greetingparam = (BufferDBChangeParam) msg.obj;
                    HttpController.getInstance().execute(new CloudMessageGreetingSearch(this, "", greetingparam.mLine, greetingparam, this.mICloudMessageManagerHelper));
                    return;
                case 3:
                    deleteGreeting((ParamOMAresponseforBufDB) msg.obj);
                    return;
                case 4:
                    if (msg.obj != null) {
                        deleteGreetingAndSearch((ParamOMAresponseforBufDB) msg.obj);
                        return;
                    }
                    return;
                case 5:
                    uploadGreeting((ParamOMAresponseforBufDB) msg.obj);
                    return;
                case 6:
                    ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.VVM_FAX_ERROR_WITH_NO_RETRY).setBufferDBChangeParam((BufferDBChangeParam) msg.obj);
                    Message errorMsg = obtainMessage(OMASyncEventType.VVM_NOTIFY.getId());
                    errorMsg.obj = builder.build();
                    sendMessage(errorMsg);
                    return;
                case 7:
                    Message succeedmsg = obtainMessage(OMASyncEventType.VVM_NOTIFY.getId());
                    succeedmsg.obj = msg.obj;
                    sendMessage(succeedmsg);
                    return;
                case 8:
                    this.mINetAPIEventListener.onOmaAuthenticationFailed((ParamOMAresponseforBufDB) msg.obj, 0);
                    return;
                case 9:
                    notifyBufferDB((ParamOMAresponseforBufDB) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        static {
            int[] iArr = new int[OMASyncEventType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = iArr;
            try {
                iArr[OMASyncEventType.EVENT_VVM_DATA_UPDATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SEARCH_GREETING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.DELETE_GREETING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.UPLOAD_GREETING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.VVM_CHANGE_ERROR.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.VVM_CHANGE_SUCCEED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREDENTIAL_EXPIRED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.VVM_NOTIFY.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    private void deleteGreetingAndSearch(ParamOMAresponseforBufDB responseParam) {
        String str = TAG;
        Log.i(str, "deleteGreetingAndSearch: " + responseParam);
        if (responseParam != null) {
            deleteGreeting(responseParam);
            BufferDBChangeParam greetingparam = responseParam.getBufferDBChangeParam();
            String line = greetingparam.mLine;
            HttpController.getInstance().execute(new CloudMessageGreetingSearch(this, responseParam.getSearchCursor(), line, greetingparam, this.mICloudMessageManagerHelper));
        }
    }

    private void deleteGreeting(ParamOMAresponseforBufDB deleteparam) {
        Log.i(TAG, "deleteGreeting: " + deleteparam);
        if (deleteparam != null) {
            ObjectList list = deleteparam.getObjectList();
            if (list == null || list.object == null || list.object.length == 0) {
                Message upload = obtainMessage(OMASyncEventType.UPLOAD_GREETING.getId());
                upload.obj = deleteparam;
                sendMessage(upload);
                return;
            }
            BulkDelete bulkdelete = new BulkDelete();
            bulkdelete.objects = new ObjectReferenceList();
            List<Reference> referenceList = new ArrayList<>();
            for (Object obj : list.object) {
                Reference ref = new Reference();
                ref.resourceURL = obj.resourceURL;
                referenceList.add(ref);
            }
            bulkdelete.objects.objectReference = (Reference[]) referenceList.toArray(new Reference[referenceList.size()]);
            HttpController.getInstance().execute(new CloudMessageGreetingBulkDeletion(this, bulkdelete, deleteparam.getLine(), deleteparam.getSyncMsgType(), deleteparam.getBufferDBChangeParam(), this.mICloudMessageManagerHelper));
        }
    }

    private void uploadGreeting(ParamOMAresponseforBufDB uploadparam) {
        if (uploadparam != null) {
            BufferDBChangeParam greetingbufparam = uploadparam.getBufferDBChangeParam();
            Pair<Object, HttpPostBody> pair = this.mBufferDbTranslation.getVVMGreetingObjectPairFromBufDb(greetingbufparam);
            ParamVvmUpdate.VvmGreetingType greetingtype = this.mBufferDbTranslation.getVVMGreetingTypeFromBufDb(greetingbufparam);
            String str = TAG;
            Log.i(str, "uploadGreeting: " + uploadparam + " greetingtype: " + greetingtype);
            if (ParamVvmUpdate.VvmGreetingType.Default.equals(greetingtype)) {
                ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED).setBufferDBChangeParam(greetingbufparam);
                Message defaultGreetingMsg = obtainMessage(OMASyncEventType.VVM_CHANGE_SUCCEED.getId());
                defaultGreetingMsg.obj = builder.build();
                sendMessage(defaultGreetingMsg);
            } else if (pair == null || pair.second == null || ((HttpPostBody) pair.second).getMultiparts() == null || ((HttpPostBody) pair.second).getMultiparts().size() <= 0) {
                sendMessage(obtainMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId(), uploadparam.getBufferDBChangeParam()));
            } else {
                HttpController.getInstance().execute(new CloudMessageCreateAllObjects(this, new ParamObjectUpload(pair, greetingbufparam), this.mICloudMessageManagerHelper));
            }
        }
    }

    private void gotoHandlerEvent(int event, Object param) {
        if (param != null) {
            sendMessage(obtainMessage(event, param));
        } else {
            sendEmptyMessage(event);
        }
    }

    public void sendVvmUpdate(BufferDBChangeParamList paramList) {
        Message msg = obtainMessage(OMASyncEventType.EVENT_VVM_DATA_UPDATE.getId());
        msg.obj = paramList;
        sendMessage(msg);
    }

    public void onGoToEvent(int event, Object param) {
        gotoHandlerEvent(event, param);
    }

    public void onMoveOnToNext(IHttpAPICommonInterface request, Object param) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request, String callFlow) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request) {
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface request, int event, Object param) {
        this.mINetAPIEventListener.onOmaSuccess(request);
        gotoHandlerEvent(event, param);
    }

    public void onFailedCall(IHttpAPICommonInterface request, String errorCode) {
        sendEmptyMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId());
    }

    public void onFailedCall(IHttpAPICommonInterface request, BufferDBChangeParam param) {
        sendMessage(obtainMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId(), param));
    }

    public void onFailedCall(IHttpAPICommonInterface request) {
        sendEmptyMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId());
    }

    public void onFailedEvent(int event, Object param) {
        gotoHandlerEvent(event, param);
    }

    public void onOverRequest(IHttpAPICommonInterface request, String errorCode, int retryAfter) {
        sendMessage(obtainMessage(OMASyncEventType.SELF_RETRY.getId(), Integer.valueOf(retryAfter)));
    }

    public void onFixedFlow(int event) {
        sendEmptyMessage(event);
    }

    public void onFixedFlowWithMessage(Message msg) {
        if (!(msg == null || msg.obj == null)) {
            String str = TAG;
            Log.i(str, "onFixedFlowWithMessage message is " + msg.what);
        }
        sendMessage(msg);
    }

    public void start() {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void stop() {
    }

    public boolean update(int eventType) {
        return sendEmptyMessage(eventType);
    }

    public boolean updateMessage(Message msg) {
        return sendMessage(msg);
    }

    public boolean updateDelay(int eventType, long delay) {
        String str = TAG;
        Log.i(str, "updateDelay: eventType: " + eventType + " delay: " + delay);
        return sendMessageDelayed(obtainMessage(eventType), delay);
    }

    public boolean updateDelayRetry(int eventType, long delay) {
        return false;
    }

    private void notifyBufferDB(ParamOMAresponseforBufDB param) {
        if (param == null) {
            Log.e(TAG, "notifyBufferDB ParamOMAresponseforBufDB is null");
        }
        this.mUpdateFromCloudRegistrants.notifyRegistrants(new AsyncResult((Object) null, param, (Throwable) null));
    }

    private void setUpVvmDataUpdate(BufferDBChangeParamList paramList) {
        String str = TAG;
        Log.i(str, "setUpVvmDataUpdate param: " + paramList);
        Iterator<BufferDBChangeParam> it = paramList.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam param = it.next();
            if (param != null) {
                VvmServiceProfile profile = new VvmServiceProfile();
                switch (param.mDBIndex) {
                    case 18:
                        Message msgforgreeting = obtainMessage(OMASyncEventType.SEARCH_GREETING.getId());
                        msgforgreeting.obj = param;
                        sendMessage(msgforgreeting);
                        break;
                    case 19:
                        this.mBufferDbTranslation.getVVMServiceProfileFromBufDb(param, profile);
                        HttpController.getInstance().execute(new CloudMessageVvmProfileUpdate(this, profile, param, this.mICloudMessageManagerHelper));
                        break;
                    case 20:
                        ParamVvmUpdate.VvmTypeChange type = this.mBufferDbTranslation.getVVMServiceProfileFromBufDb(param, profile);
                        if (type != null) {
                            String str2 = TAG;
                            Log.i(str2, "setUpVvmDataUpdate :VvmTypeChange " + type.getId());
                            if (!type.equals(ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT)) {
                                if (!type.equals(ParamVvmUpdate.VvmTypeChange.ACTIVATE) && !type.equals(ParamVvmUpdate.VvmTypeChange.DEACTIVATE)) {
                                    if (!type.equals(ParamVvmUpdate.VvmTypeChange.FULLPROFILE)) {
                                        break;
                                    } else {
                                        HttpController.getInstance().execute(new CloudMessageGetVvmProfile(this, param, this.mICloudMessageManagerHelper));
                                        break;
                                    }
                                } else {
                                    HttpController.getInstance().execute(new CloudMessageVvmProfileAttributePut(this, profile, param, this.mICloudMessageManagerHelper));
                                    break;
                                }
                            } else {
                                HttpController.getInstance().execute(new CloudMessageVvmProfileUpdate(this, profile, param, this.mICloudMessageManagerHelper));
                                break;
                            }
                        } else {
                            break;
                        }
                        break;
                }
            }
        }
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
    }
}
