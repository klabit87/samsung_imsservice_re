package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.FileTransferLog;
import com.gsma.services.rcs.filetransfer.IFileTransfer;
import com.gsma.services.rcs.filetransfer.IFileTransferService;
import com.gsma.services.rcs.filetransfer.IFileTransferServiceConfiguration;
import com.gsma.services.rcs.filetransfer.IGroupFileTransferListener;
import com.gsma.services.rcs.filetransfer.IOneToOneFileTransferListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GroupFileTransferBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneFileTransferBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class FileTransferingServiceImpl extends IFileTransferService.Stub implements IFtEventListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = FileTransferingServiceImpl.class.getSimpleName();
    static Context mContext = null;
    private static Hashtable<String, IFileTransfer> mIFtSessions = new Hashtable<>();
    private GroupFileTransferBroadcaster mGroupFileTransferBroadcaster = null;
    private IImModule mImModule = null;
    private Object mLock = new Object();
    private OneToOneFileTransferBroadcaster mOneToOneFileTransferBroadcaster = null;
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public FileTransferingServiceImpl(Context context, IImModule service) {
        mContext = context;
        this.mImModule = service;
        this.mOneToOneFileTransferBroadcaster = new OneToOneFileTransferBroadcaster(mContext);
        this.mGroupFileTransferBroadcaster = new GroupFileTransferBroadcaster(mContext);
        this.mImModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA, this);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager manager = ImsRegistry.getRegistrationManager();
        if (manager == null) {
            return false;
        }
        for (ImsRegistration reg : manager.getRegistrationInfo()) {
            if (reg.hasService("ft") || reg.hasService("ft_http")) {
                return true;
            }
        }
        return false;
    }

    public void addEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.register(listener);
        }
    }

    public void removeEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.unregister(listener);
        }
    }

    public IFileTransferServiceConfiguration getConfiguration() throws ServerApiException {
        return new FileTransferServiceConfigurationImpl(this.mImModule.getImConfig());
    }

    public List<IBinder> getFileTransfers() throws ServerApiException {
        Log.d(LOG_TAG, "getFileTransfers get all transfered file.");
        ArrayList<IBinder> result = new ArrayList<>(mIFtSessions.size());
        Enumeration<IFileTransfer> e = mIFtSessions.elements();
        while (e.hasMoreElements()) {
            result.add(e.nextElement().asBinder());
        }
        return result;
    }

    public IFileTransfer getFileTransfer(String transferId) throws ServerApiException {
        return mIFtSessions.get(transferId);
    }

    public OneToOneFileTransferImpl getFileTransferByID(String transferId) {
        return mIFtSessions.get(transferId);
    }

    public IFileTransfer transferFile(ContactId contact, Uri file, FileTransfer.Disposition disposition, boolean attachFileIcon) throws ServerApiException {
        FileDisposition fileDisposition;
        String telUri = "tel:" + PhoneUtils.extractNumberFromUri(contact.toString());
        String fileName = FileUtils.getFilePathFromUri(mContext, file);
        Log.d(LOG_TAG, "transferFile, fileName = " + fileName);
        try {
            IImModule iImModule = this.mImModule;
            ImsUri parse = ImsUri.parse(telUri);
            EnumSet of = EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED);
            if (disposition == FileTransfer.Disposition.RENDER) {
                try {
                    fileDisposition = FileDisposition.RENDER;
                } catch (InterruptedException e) {
                    e = e;
                    String str = fileName;
                } catch (ExecutionException e2) {
                    e = e2;
                    String str2 = fileName;
                    e.printStackTrace();
                    return null;
                }
            } else {
                fileDisposition = FileDisposition.ATTACH;
            }
            String str3 = fileName;
            try {
                FtMessage ftMessage = iImModule.attachFileToSingleChat(0, fileName, parse, of, (String) null, (String) null, false, false, false, false, (String) null, fileDisposition).get();
                if (ftMessage == null) {
                    Log.e(LOG_TAG, "attachFileToSingleChat failed, return null!");
                    return null;
                }
                OneToOneFileTransferImpl transferImpl = new OneToOneFileTransferImpl(ftMessage, this.mImModule);
                addFileTransferingSession(String.valueOf(ftMessage.getId()), transferImpl);
                return transferImpl;
            } catch (InterruptedException e3) {
                e = e3;
                e.printStackTrace();
                return null;
            } catch (ExecutionException e4) {
                e = e4;
                e.printStackTrace();
                return null;
            }
        } catch (InterruptedException e5) {
            e = e5;
            String str4 = fileName;
            e.printStackTrace();
            return null;
        } catch (ExecutionException e6) {
            e = e6;
            String str5 = fileName;
            e.printStackTrace();
            return null;
        }
    }

    public void addOneToOneFileTransferListener(IOneToOneFileTransferListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mOneToOneFileTransferBroadcaster.addOneToOneFileTransferListener(listener);
        }
    }

    public void removeOneToOneFileTransferListener(IOneToOneFileTransferListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mOneToOneFileTransferBroadcaster.removeOneToOneFileTransferListener(listener);
        }
    }

    public void addGroupFileTransferListener(IGroupFileTransferListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupFileTransferBroadcaster.addGroupFileTransferListener(listener);
        }
    }

    public void removeGroupFileTransferListener(IGroupFileTransferListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupFileTransferBroadcaster.removeGroupFileTransferListener(listener);
        }
    }

    public static void addFileTransferingSession(String sessionId, OneToOneFileTransferImpl transfer) {
        if (!mIFtSessions.containsKey(sessionId)) {
            mIFtSessions.put(sessionId, transfer);
        }
    }

    public static void removeFileTransferingSession(String sessionId) {
        if (mIFtSessions.containsKey(sessionId)) {
            mIFtSessions.remove(sessionId);
        }
    }

    public void removeFileTransferingSessions(List<String> sessionIds) {
        for (String sessionId : sessionIds) {
            mIFtSessions.remove(sessionId);
        }
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        synchronized (this.mLock) {
            int N = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public IFileTransfer transferFileToGroupChat(String chatId, Uri file, FileTransfer.Disposition disposition, boolean attachFileIcon) throws ServerApiException {
        FileDisposition fileDisposition;
        if (!canTransferFileToGroupChat(chatId)) {
            return null;
        }
        String fileName = FileUtils.getFilePathFromUri(mContext, file);
        String str = LOG_TAG;
        Log.d(str, "transferFileToGroupChat, fileName = " + fileName);
        try {
            IImModule iImModule = this.mImModule;
            EnumSet of = EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED);
            if (disposition == FileTransfer.Disposition.RENDER) {
                try {
                    fileDisposition = FileDisposition.RENDER;
                } catch (InterruptedException e) {
                    e = e;
                    String str2 = fileName;
                } catch (ExecutionException e2) {
                    e = e2;
                    String str3 = fileName;
                    e.printStackTrace();
                    return null;
                }
            } else {
                fileDisposition = FileDisposition.ATTACH;
            }
            String str4 = fileName;
            try {
                FtMessage ftMessage = iImModule.attachFileToGroupChat(chatId, fileName, of, (String) null, (String) null, false, false, false, false, (String) null, fileDisposition).get();
                if (ftMessage == null) {
                    Log.e(LOG_TAG, "attachFileToGroupChat failed, return null!");
                    return null;
                }
                OneToOneFileTransferImpl transferImpl = new OneToOneFileTransferImpl(ftMessage, this.mImModule);
                addFileTransferingSession(String.valueOf(ftMessage.getId()), transferImpl);
                return transferImpl;
            } catch (InterruptedException e3) {
                e = e3;
                e.printStackTrace();
                return null;
            } catch (ExecutionException e4) {
                e = e4;
                e.printStackTrace();
                return null;
            }
        } catch (InterruptedException e5) {
            e = e5;
            String str5 = fileName;
            e.printStackTrace();
            return null;
        } catch (ExecutionException e6) {
            e = e6;
            String str6 = fileName;
            e.printStackTrace();
            return null;
        }
    }

    public boolean isAllowedTotransferFile(ContactId contact) throws ServerApiException {
        Capabilities capx = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
        if (capx == null || !capx.hasFeature(Capabilities.FEATURE_FT)) {
            return false;
        }
        return true;
    }

    public boolean canTransferFileToGroupChat(String chatId) throws ServerApiException {
        if (ImCache.getInstance().getImSession(chatId) != null) {
            return true;
        }
        String str = LOG_TAG;
        Log.d(str, "attachFileToGroupChat: chat not exist - " + chatId);
        return false;
    }

    public void markFileTransferAsRead(String transferId) throws ServerApiException {
        FtMessage message = ImCache.getInstance().getFtMessage(Integer.valueOf(transferId).intValue());
        if (message != null) {
            List<String> msgIds = new ArrayList<>();
            msgIds.add(transferId);
            this.mImModule.readMessages(message.getChatId(), msgIds);
            message.updateNotificationStatus(NotificationStatus.DELIVERED);
            this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(message.getChatId()))), transferId, FileTransfer.State.DISPLAYED, FileTransfer.ReasonCode.UNSPECIFIED);
        }
    }

    public void notifyChangeForDelete() {
        mContext.getContentResolver().notifyChange(FileTransferLog.CONTENT_URI, (ContentObserver) null);
    }

    public void deleteOneToOneFileTransfers() throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteOneToOneFileTransfers()");
        Map<String, Set<String>> msgs = getFileTransfers(false, "is_filetransfer = 1");
        if (msgs != null) {
            List<String> listMsgs = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
                listMsgs.addAll(entry.getValue());
                String uriString = getImSessionByChatId(entry.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneFileTransferBroadcaster.broadcastDeleted(PhoneUtils.extractNumberFromUri(uriString), entry.getValue());
                }
            }
            this.mImModule.deleteMessages(listMsgs, false);
            removeFileTransferingSessions(listMsgs);
            notifyChangeForDelete();
        }
    }

    public void deleteGroupFileTransfers() throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteGroupFileTransfers()");
        Map<String, Set<String>> msgs = getFileTransfers(true, "is_filetransfer = 1");
        if (msgs != null) {
            List<String> listMsgs = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
                listMsgs.addAll(entry.getValue());
                synchronized (this.mLock) {
                    this.mGroupFileTransferBroadcaster.broadcastDeleted(entry.getKey(), entry.getValue());
                }
            }
            this.mImModule.deleteMessages(listMsgs, false);
            removeFileTransferingSessions(listMsgs);
            notifyChangeForDelete();
        }
    }

    public void deleteOneToOneFileTransfersByContactId(ContactId contact) throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteOneToOneFileTransfersByContactId()");
        Set<ImsUri> uris = new HashSet<>();
        uris.add(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contact.toString())));
        ImSession session = ImCache.getInstance().getImSessionByParticipants(uris, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        if (session == null) {
            Log.e(LOG_TAG, "deleteOneToOneFileTransfersByContactId, no session for ft");
            return;
        }
        Map<String, Set<String>> msgs = getFileTransfers(false, "is_filetransfer = 1 and chat_id = '" + session.getChatId() + "'");
        if (msgs != null) {
            List<String> listMsgs = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
                listMsgs.addAll(entry.getValue());
                String uriString = getImSessionByChatId(entry.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneFileTransferBroadcaster.broadcastDeleted(PhoneUtils.extractNumberFromUri(uriString), entry.getValue());
                }
            }
            this.mImModule.deleteMessages(listMsgs, false);
            removeFileTransferingSessions(listMsgs);
            notifyChangeForDelete();
        }
    }

    public void deleteGroupFileTransfersByChatId(String chatId) throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteGroupFileTransfersByChatId()");
        Map<String, Set<String>> msgs = getFileTransfers(true, "is_filetransfer = 1 and chat_id = '" + chatId + "'");
        if (msgs != null) {
            List<String> listMsgs = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
                listMsgs.addAll(entry.getValue());
                synchronized (this.mLock) {
                    this.mGroupFileTransferBroadcaster.broadcastDeleted(entry.getKey(), entry.getValue());
                }
            }
            this.mImModule.deleteMessages(listMsgs, false);
            removeFileTransferingSessions(listMsgs);
            notifyChangeForDelete();
        }
    }

    public void deleteFileTransfer(String transferId) throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteFileTransfer()");
        List<String> list = new ArrayList<>();
        list.add(transferId);
        this.mImModule.deleteMessages(list, false);
        OneToOneFileTransferImpl fileTransfer = getFileTransferByID(transferId);
        if (fileTransfer != null) {
            Set<String> msgs = new HashSet<>();
            msgs.add(transferId);
            try {
                boolean isGroup = fileTransfer.isGroupTransfer();
                String chatId = fileTransfer.getChatId();
                ContactId contactId = fileTransfer.getRemoteContact();
                if (isGroup) {
                    this.mGroupFileTransferBroadcaster.broadcastDeleted(chatId, msgs);
                } else if (contactId != null) {
                    this.mOneToOneFileTransferBroadcaster.broadcastDeleted(contactId.toString(), msgs);
                } else {
                    return;
                }
                removeFileTransferingSessions(list);
                notifyChangeForDelete();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setAutoAccept(boolean enable) throws ServerApiException {
        if (isFtAutoAcceptedModeChangeable()) {
            this.mImModule.setAutoAcceptFt(1);
            return;
        }
        throw new ServerApiException("Auto accept mode is not changeable");
    }

    public void setAutoAcceptInRoaming(boolean enable) throws ServerApiException {
        if (!isFtAutoAcceptedModeChangeable()) {
            throw new ServerApiException("Auto accept mode is not changeable");
        } else if (isFileTransferAutoAccepted()) {
            this.mImModule.setAutoAcceptFt(2);
        } else {
            throw new ServerApiException("Auto accept mode in normal conditions must be enabled");
        }
    }

    public void setImageResizeOption(int option) throws ServerApiException {
        ContentValues data = new ContentValues();
        data.put(ImSettings.KEY_IMAGE_RESIZE_OPTION, String.valueOf(option));
        mContext.getContentResolver().insert(ConfigConstants.CONTENT_URI, data);
    }

    public List<String> getUndeliveredFileTransfers(ContactId contact) throws ServerApiException {
        Log.d(LOG_TAG, "start : getUndeliveredFileTransfers()");
        ImsUri uri = ImsUri.parse("tel:" + contact.toString());
        Set<ImsUri> uris = new HashSet<>();
        uris.add(uri);
        ImSession session = ImCache.getInstance().getImSessionByParticipants(uris, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        List<String> msgIds = new ArrayList<>();
        if (session == null) {
            return msgIds;
        }
        Cursor cursor = ImCache.getInstance().queryMessages(new String[]{"_id"}, "chat_id = '" + session.getChatId() + "' and " + "notification_status" + " = " + NotificationStatus.NONE.getId() + " and " + "direction" + " = " + ImDirection.OUTGOING.getId() + " and " + ImContract.ChatItem.IS_FILE_TRANSFER + " = 1", (String[]) null, (String) null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    msgIds.add(String.valueOf(cursor.getInt(0)));
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return msgIds;
        throw th;
    }

    public void markUndeliveredFileTransfersAsProcessed(List<String> transferIds) throws ServerApiException {
        Log.d(LOG_TAG, "start : markUndeliveredFileTransfersAsProcessed()");
        ImCache cache = ImCache.getInstance();
        for (String msgId : transferIds) {
            ImMessage message = cache.getImMessage(Integer.valueOf(msgId).intValue());
            if (message != null) {
                message.updateStatus(ImConstants.Status.SENT);
                cache.removeFromPendingList(Integer.valueOf(msgId).intValue());
            }
        }
    }

    public void handleTransferState(FtMessage msg) {
        FileTransfer.ReasonCode reasonCode;
        CancelReason cancel = msg.getCancelReason();
        FtRejectReason reject = msg.getRejectReason();
        FileTransfer.ReasonCode reasonCode2 = FileTransfer.ReasonCode.UNSPECIFIED;
        int msgState = msg.getStateId();
        ImDirection direction = msg.getDirection();
        if (reject != null) {
            reasonCode = ftRejectReasonTranslator(reject);
        } else {
            reasonCode = ftCancelReasonTranslator(cancel);
        }
        FileTransfer.State state = FileTransfer.State.FAILED;
        switch (msgState) {
            case 0:
            case 6:
                if (ImDirection.INCOMING != direction) {
                    state = FileTransfer.State.INITIATING;
                    break;
                } else {
                    state = FileTransfer.State.INVITED;
                    break;
                }
            case 1:
                if (ImDirection.INCOMING == direction) {
                    state = FileTransfer.State.ACCEPTING;
                    break;
                }
                break;
            case 2:
            case 9:
                state = FileTransfer.State.STARTED;
                break;
            case 3:
                state = FileTransfer.State.TRANSFERRED;
                break;
            case 4:
            case 7:
                state = FileTransfer.State.ABORTED;
                break;
            case 5:
            case 8:
                state = FileTransfer.State.QUEUED;
                break;
        }
        ContactId contact = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(msg.getChatId())));
        ImSession session = ImCache.getInstance().getImSession(msg.getChatId());
        if (session == null) {
            String str = LOG_TAG;
            Log.d(str, "handleTransferState: " + state + ", cannot get ImSession from chatId : " + msg.getChatId());
            return;
        }
        if (session.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(msg.getChatId(), String.valueOf(msg.getId()), state, reasonCode);
        } else {
            this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(contact, String.valueOf(msg.getId()), state, reasonCode);
        }
        this.mOneToOneFileTransferBroadcaster.broadcastUndeliveredFileTransfer(contact);
        removeFileTransferingSession(String.valueOf(msg.getId()));
    }

    public void handleTransferingProgress(FtMessage msg) {
        long currentSize = msg.getTransferredBytes();
        long totalSize = msg.getFileSize();
        ContactId contact = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(msg.getChatId())));
        ImCache cache = ImCache.getInstance();
        ImSession session = cache.getImSession(msg.getChatId());
        if (session == null) {
            String str = LOG_TAG;
            Log.d(str, "handleTransferingProgress, cannot get ImSession from chatId : " + msg.getChatId());
        } else if (session.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferprogress(msg.getChatId(), String.valueOf(msg.getId()), currentSize, totalSize);
            ImCache imCache = cache;
        } else {
            ImCache imCache2 = cache;
            this.mOneToOneFileTransferBroadcaster.broadcastTransferprogress(contact, String.valueOf(msg.getId()), currentSize, totalSize);
        }
    }

    public void handleContentTransfered(FtMessage msg) {
        ContactId contact = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(msg.getChatId())));
        ImSession session = ImCache.getInstance().getImSession(msg.getChatId());
        if (session == null) {
            String str = LOG_TAG;
            Log.d(str, "handleContentTransfered, cannot get ImSession from chatId : " + msg.getChatId());
        } else if (session.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(msg.getChatId(), String.valueOf(msg.getId()), FileTransfer.State.TRANSFERRED, FileTransfer.ReasonCode.UNSPECIFIED);
        } else {
            this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(contact, String.valueOf(msg.getId()), FileTransfer.State.TRANSFERRED, FileTransfer.ReasonCode.UNSPECIFIED);
        }
    }

    public void handleTransferReceived(FtMessage msg) {
        ContactId contact = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(msg.getChatId())));
        ImSession session = ImCache.getInstance().getImSession(msg.getChatId());
        if (session == null) {
            String str = LOG_TAG;
            Log.d(str, "handleTransferReceived, cannot get ImSession from chatId : " + msg.getChatId());
        } else if (session.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(msg.getChatId(), String.valueOf(msg.getId()), FileTransfer.State.INVITED, FileTransfer.ReasonCode.UNSPECIFIED);
            this.mGroupFileTransferBroadcaster.broadcastFileTransferInvitation(String.valueOf(msg.getId()));
        } else {
            this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(contact, String.valueOf(msg.getId()), FileTransfer.State.INVITED, FileTransfer.ReasonCode.UNSPECIFIED);
            this.mOneToOneFileTransferBroadcaster.broadcastFileTransferInvitation(String.valueOf(msg.getId()));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x009d, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessageDeliveryStatus(com.sec.internal.ims.servicemodules.im.MessageBase r13, com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r14) {
        /*
            r12 = this;
            java.lang.String r0 = r13.getChatId()
            java.lang.String r0 = r12.getImSessionByChatId(r0)
            com.gsma.services.rcs.contact.ContactId r1 = new com.gsma.services.rcs.contact.ContactId
            java.lang.String r2 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r0)
            r1.<init>(r2)
            com.gsma.services.rcs.filetransfer.FileTransfer$State r2 = com.gsma.services.rcs.filetransfer.FileTransfer.State.FAILED
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r3 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED
            if (r3 != r14) goto L_0x001a
            com.gsma.services.rcs.filetransfer.FileTransfer$State r2 = com.gsma.services.rcs.filetransfer.FileTransfer.State.DELIVERED
            goto L_0x0020
        L_0x001a:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r3 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED
            if (r3 != r14) goto L_0x0020
            com.gsma.services.rcs.filetransfer.FileTransfer$State r2 = com.gsma.services.rcs.filetransfer.FileTransfer.State.DISPLAYED
        L_0x0020:
            java.lang.Object r9 = r12.mLock
            monitor-enter(r9)
            com.sec.internal.ims.servicemodules.im.ImCache r3 = com.sec.internal.ims.servicemodules.im.ImCache.getInstance()     // Catch:{ all -> 0x009e }
            r10 = r3
            java.lang.String r3 = r13.getChatId()     // Catch:{ all -> 0x009e }
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r10.getImSession(r3)     // Catch:{ all -> 0x009e }
            r11 = r3
            if (r11 != 0) goto L_0x0057
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x009e }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x009e }
            r4.<init>()     // Catch:{ all -> 0x009e }
            java.lang.String r5 = "handleMessageDeliveryStatus: "
            r4.append(r5)     // Catch:{ all -> 0x009e }
            r4.append(r2)     // Catch:{ all -> 0x009e }
            java.lang.String r5 = ", cannot get ImSession from chatId : "
            r4.append(r5)     // Catch:{ all -> 0x009e }
            java.lang.String r5 = r13.getChatId()     // Catch:{ all -> 0x009e }
            r4.append(r5)     // Catch:{ all -> 0x009e }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x009e }
            android.util.Log.d(r3, r4)     // Catch:{ all -> 0x009e }
            monitor-exit(r9)     // Catch:{ all -> 0x009e }
            return
        L_0x0057:
            boolean r3 = r11.isGroupChat()     // Catch:{ all -> 0x009e }
            if (r3 == 0) goto L_0x008d
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GroupFileTransferBroadcaster r3 = r12.mGroupFileTransferBroadcaster     // Catch:{ all -> 0x009e }
            java.lang.String r4 = r13.getChatId()     // Catch:{ all -> 0x009e }
            int r5 = r13.getId()     // Catch:{ all -> 0x009e }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x009e }
            com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo$Status r7 = com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo.Status.DELIVERED     // Catch:{ all -> 0x009e }
            com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo$ReasonCode r8 = com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x009e }
            r6 = r1
            r3.broadcastGroupDeliveryInfoStateChanged(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x009e }
            int r3 = r13.getNotDisplayedCounter()     // Catch:{ all -> 0x009e }
            if (r3 != 0) goto L_0x009c
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GroupFileTransferBroadcaster r3 = r12.mGroupFileTransferBroadcaster     // Catch:{ all -> 0x009e }
            java.lang.String r4 = r13.getChatId()     // Catch:{ all -> 0x009e }
            int r5 = r13.getId()     // Catch:{ all -> 0x009e }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x009e }
            com.gsma.services.rcs.filetransfer.FileTransfer$ReasonCode r6 = com.gsma.services.rcs.filetransfer.FileTransfer.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x009e }
            r3.broadcastTransferStateChanged(r4, r5, r2, r6)     // Catch:{ all -> 0x009e }
            goto L_0x009c
        L_0x008d:
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneFileTransferBroadcaster r3 = r12.mOneToOneFileTransferBroadcaster     // Catch:{ all -> 0x009e }
            int r4 = r13.getId()     // Catch:{ all -> 0x009e }
            java.lang.String r4 = java.lang.String.valueOf(r4)     // Catch:{ all -> 0x009e }
            com.gsma.services.rcs.filetransfer.FileTransfer$ReasonCode r5 = com.gsma.services.rcs.filetransfer.FileTransfer.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x009e }
            r3.broadcastTransferStateChanged(r1, r4, r2, r5)     // Catch:{ all -> 0x009e }
        L_0x009c:
            monitor-exit(r9)     // Catch:{ all -> 0x009e }
            return
        L_0x009e:
            r3 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x009e }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl.handleMessageDeliveryStatus(com.sec.internal.ims.servicemodules.im.MessageBase, com.sec.internal.constants.ims.servicemodules.im.NotificationStatus):void");
    }

    public void onFileTransferCreated(FtMessage msg) {
        if (mIFtSessions.containsKey(String.valueOf(msg.getId()))) {
            this.mImModule.sendFile((long) msg.getId());
            ImSession session = ImCache.getInstance().getImSession(msg.getChatId());
            if (session == null) {
                String str = LOG_TAG;
                Log.d(str, "onFileTransferCreated, cannot get ImSession from chatId : " + msg.getChatId());
            } else if (session.isGroupChat()) {
                this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(msg.getChatId(), String.valueOf(msg.getId()), FileTransfer.State.STARTED, FileTransfer.ReasonCode.UNSPECIFIED);
            } else {
                this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(msg.getChatId()))), String.valueOf(msg.getId()), FileTransfer.State.STARTED, FileTransfer.ReasonCode.UNSPECIFIED);
            }
        }
    }

    public void onFileTransferAttached(FtMessage msg) {
    }

    public void onFileTransferReceived(FtMessage msg) {
        addFileTransferingSession(String.valueOf(msg.getId()), new OneToOneFileTransferImpl(msg, this.mImModule));
        handleTransferReceived(msg);
    }

    public void onTransferProgressReceived(FtMessage msg) {
        handleTransferingProgress(msg);
    }

    public void onTransferStarted(FtMessage msg) {
    }

    public void onTransferCompleted(FtMessage msg) {
        handleContentTransfered(msg);
    }

    public void onTransferCanceled(FtMessage msg) {
        handleTransferState(msg);
    }

    public void onImdnNotificationReceived(FtMessage msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
        handleMessageDeliveryStatus(msg, msg.getNotificationStatus());
    }

    public void onFileResizingNeeded(FtMessage msg, long resizeLimit) {
    }

    public void onCancelRequestFailed(FtMessage msg) {
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
    }

    public static FileTransfer.ReasonCode ftCancelReasonTranslator(CancelReason value) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[value.ordinal()]) {
            case 1:
                return FileTransfer.ReasonCode.ABORTED_BY_USER;
            case 2:
                return FileTransfer.ReasonCode.ABORTED_BY_REMOTE;
            case 3:
                return FileTransfer.ReasonCode.ABORTED_BY_SYSTEM;
            case 4:
                return FileTransfer.ReasonCode.REJECTED_BY_REMOTE;
            case 5:
                return FileTransfer.ReasonCode.REJECTED_BY_TIMEOUT;
            case 6:
                return FileTransfer.ReasonCode.REJECTED_LOW_SPACE;
            case 7:
                return FileTransfer.ReasonCode.REJECTED_MAX_SIZE;
            default:
                return FileTransfer.ReasonCode.UNSPECIFIED;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason;

        static {
            int[] iArr = new int[FtRejectReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason = iArr;
            try {
                iArr[FtRejectReason.FORBIDDEN_MAX_SIZE_EXCEEDED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason[FtRejectReason.DECLINE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            int[] iArr2 = new int[CancelReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason = iArr2;
            try {
                iArr2[CancelReason.CANCELED_BY_USER.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CANCELED_BY_REMOTE.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CANCELED_BY_SYSTEM.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REJECTED_BY_REMOTE.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.TIME_OUT.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.LOW_MEMORY.ordinal()] = 6;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.TOO_LARGE.ordinal()] = 7;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.NOT_AUTHORIZED.ordinal()] = 8;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_BLOCKED.ordinal()] = 9;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.ERROR.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE.ordinal()] = 11;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.VALIDITY_EXPIRED.ordinal()] = 12;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.INVALID_REQUEST.ordinal()] = 13;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_USER_INVALID.ordinal()] = 14;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.NO_RESPONSE.ordinal()] = 15;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.FORBIDDEN_NO_RETRY_FALLBACK.ordinal()] = 16;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CONTENT_REACHED_DOWNSIZE.ordinal()] = 17;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.LOCALLY_ABORTED.ordinal()] = 18;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CONNECTION_RELEASED.ordinal()] = 19;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.DEVICE_UNREGISTERED.ordinal()] = 20;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.UNKNOWN.ordinal()] = 21;
            } catch (NoSuchFieldError e23) {
            }
        }
    }

    public static FileTransfer.ReasonCode ftRejectReasonTranslator(FtRejectReason value) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason[value.ordinal()];
        if (i == 1) {
            return FileTransfer.ReasonCode.REJECTED_MAX_SIZE;
        }
        if (i != 2) {
            return FileTransfer.ReasonCode.UNSPECIFIED;
        }
        return FileTransfer.ReasonCode.FAILED_INITIATION;
    }

    public String getImSessionByChatId(String chatId) {
        ImSession imSession = ImCache.getInstance().getImSession(chatId);
        if (imSession == null) {
            return null;
        }
        List<String> participantsString = imSession.getParticipantsString();
        if (participantsString.size() > 0) {
            return participantsString.get(0);
        }
        return null;
    }

    public Map<String, Set<String>> getFileTransfers(boolean isGroup, String selection) {
        ImCache cache = ImCache.getInstance();
        Map<String, Set<String>> msgs = new TreeMap<>();
        Cursor cursorDb = cache.queryMessages(new String[]{"_id", "chat_id"}, selection, (String[]) null, (String) null);
        if (cursorDb != null) {
            try {
                if (cursorDb.getCount() != 0) {
                    while (cursorDb.moveToNext()) {
                        String chatIdString = cursorDb.getString(cursorDb.getColumnIndexOrThrow("chat_id"));
                        ImSession session = cache.getImSession(chatIdString);
                        if (session != null) {
                            if (session.isGroupChat() == isGroup) {
                                addRecord(chatIdString, String.valueOf(cursorDb.getInt(cursorDb.getColumnIndexOrThrow("_id"))), msgs);
                            }
                        }
                    }
                    if (cursorDb != null) {
                        cursorDb.close();
                    }
                    return msgs;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        Log.e(LOG_TAG, "getFileTransfers: Message not found.");
        if (cursorDb != null) {
            cursorDb.close();
        }
        return null;
        throw th;
    }

    private void addRecord(String chatIdString, String idString, Map<String, Set<String>> msgs) {
        Set<String> setMsgs = msgs.get(chatIdString);
        if (setMsgs == null) {
            Set<String> setMsgs2 = new HashSet<>();
            setMsgs2.add(idString);
            msgs.put(chatIdString, setMsgs2);
            return;
        }
        setMsgs.add(idString);
    }

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public boolean isFtAutoAcceptedModeChangeable() {
        RcsSettingsUtils rcsSetting = RcsSettingsUtils.getInstance();
        if (rcsSetting != null) {
            return Boolean.parseBoolean(rcsSetting.readParameter(ImSettings.AUTO_ACCEPT_FT_CHANGEABLE));
        }
        return false;
    }

    public boolean isFileTransferAutoAccepted() {
        return this.mImModule.getImConfig().isFtAutAccept();
    }

    public void clearFileTransferDeliveryExpiration(List<String> list) throws RemoteException {
    }

    public boolean isAllowedToTransferFile(ContactId contact) throws RemoteException {
        Capabilities capx;
        if (contact == null || (capx = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getCapabilities(contact.toString(), (long) Capabilities.FEATURE_FT, SimUtil.getDefaultPhoneId())) == null || !capx.hasFeature(Capabilities.FEATURE_FT)) {
            return false;
        }
        return true;
    }

    public IFileTransfer transferAudioMessage(ContactId contact, Uri file) throws RemoteException {
        String telUri = "tel:" + PhoneUtils.extractNumberFromUri(contact.toString());
        String fileName = FileUtils.getFilePathFromUri(mContext, file);
        Log.d(LOG_TAG, "transferAudioMessage, fileName = " + fileName);
        String str = fileName;
        String messageId = null;
        try {
            messageId = String.valueOf(this.mImModule.attachFileToSingleChat(0, fileName, ImsUri.parse(telUri), EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), (String) null, "application/audio-message", false, false, false, false, (String) null, FileDisposition.ATTACH).get().getId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e2) {
            e2.printStackTrace();
        }
        if (messageId == null) {
            Log.e(LOG_TAG, "attachFileToSingleChat failed, return null!");
            return null;
        }
        OneToOneFileTransferImpl transferImpl = new OneToOneFileTransferImpl(messageId, this.mImModule);
        addFileTransferingSession(messageId, transferImpl);
        return transferImpl;
    }
}
