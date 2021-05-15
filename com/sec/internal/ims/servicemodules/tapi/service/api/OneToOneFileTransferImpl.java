package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.IFileTransfer;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OneToOneFileTransferImpl extends IFileTransfer.Stub {
    private static final String LOG_TAG = OneToOneFileTransferImpl.class.getSimpleName();
    private FtMessage mFtMsg = null;
    private IImModule mImModule = null;

    public OneToOneFileTransferImpl(String msgId, IImModule imModule) {
        this.mFtMsg = imModule.getFtMessage(Integer.valueOf(msgId).intValue());
        this.mImModule = imModule;
    }

    public OneToOneFileTransferImpl(FtMessage msg, IImModule imModule) {
        this.mFtMsg = msg;
        this.mImModule = imModule;
    }

    public String getChatId() throws ServerApiException {
        return this.mFtMsg.getChatId();
    }

    public String getTransferId() throws ServerApiException {
        return String.valueOf(this.mFtMsg.getId());
    }

    public Uri getFile() throws ServerApiException {
        return Uri.fromFile(new File(this.mFtMsg.getFilePath()));
    }

    public FileTransfer.Disposition getFileDisposition() throws ServerApiException {
        return this.mFtMsg.getFileDisposition() == FileDisposition.RENDER ? FileTransfer.Disposition.RENDER : FileTransfer.Disposition.ATTACH;
    }

    public Uri getFileIcon() throws ServerApiException {
        String path = this.mFtMsg.getThumbnailPath();
        if (path == null) {
            path = this.mFtMsg.getFilePath();
        }
        return Uri.fromFile(new File(path));
    }

    public ContactId getRemoteContact() throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "mFtMsg.getContactUri() = " + this.mFtMsg.getRemoteUri());
        if (this.mFtMsg.getRemoteUri() == null) {
            return null;
        }
        return new ContactId(this.mFtMsg.getRemoteUri().toString());
    }

    public String getFileName() throws ServerApiException {
        return this.mFtMsg.getFileName();
    }

    public long getFileSize() throws ServerApiException {
        return this.mFtMsg.getFileSize();
    }

    public String getFileType() throws ServerApiException {
        return this.mFtMsg.getContentType();
    }

    public FileTransfer.State getState() throws ServerApiException {
        Log.d(LOG_TAG, "getState");
        FileTransfer.State state = FileTransfer.State.FAILED;
        ImDirection direction = this.mFtMsg.getDirection();
        int stateId = this.mFtMsg.getStateId();
        if (!(stateId == 0 || stateId == 1)) {
            if (stateId == 2) {
                return FileTransfer.State.STARTED;
            }
            if (stateId == 3) {
                return FileTransfer.State.TRANSFERRED;
            }
            if (stateId != 4) {
                if (stateId != 6) {
                    if (stateId != 7) {
                        return FileTransfer.State.FAILED;
                    }
                }
            }
            return FileTransfer.State.ABORTED;
        }
        if (ImDirection.INCOMING == direction) {
            return FileTransfer.State.INVITED;
        }
        if (ImDirection.OUTGOING == direction) {
            return FileTransfer.State.INITIATING;
        }
        return state;
    }

    public int getDirection() throws ServerApiException {
        return this.mFtMsg.getDirection().getId();
    }

    public void acceptInvitation() throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "acceptInvitation id:" + this.mFtMsg.getId());
        this.mImModule.acceptFileTransfer(this.mFtMsg.getId());
    }

    public void rejectInvitation() throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "rejectInvitation id:" + this.mFtMsg.getId());
        this.mImModule.rejectFileTransfer(this.mFtMsg.getId());
    }

    public void abortTransfer() throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "abortTransfer id:" + getTransferId());
        this.mImModule.cancelFileTransfer(this.mFtMsg.getId());
    }

    public void pauseTransfer() throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "pauseTransfer id:" + this.mFtMsg.getId());
        this.mImModule.cancelFileTransfer(this.mFtMsg.getId());
    }

    public void resumeTransfer() throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "resumeTransfer id:" + this.mFtMsg.getId());
        if (this.mFtMsg.getDirection().equals(ImDirection.OUTGOING)) {
            this.mImModule.resumeSendingTransfer(this.mFtMsg.getId(), true);
        } else {
            this.mImModule.resumeReceivingTransfer(this.mFtMsg.getId());
        }
    }

    public void resendTransfer() throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "resendTransfer id:" + this.mFtMsg.getId());
        if (FileTransfer.State.FAILED == getState()) {
            this.mFtMsg.sendFile();
        }
    }

    public boolean isGroupTransfer() throws RemoteException {
        ImSession session = this.mImModule.getImSession(this.mFtMsg.getChatId());
        if (session != null) {
            return session.isGroupChat();
        }
        return false;
    }

    public String getFileIconMimeType() throws RemoteException {
        return FileUtils.getContentTypeFromFileName(this.mFtMsg.getThumbnailPath());
    }

    public String getMimeType() throws RemoteException {
        return this.mFtMsg.getContentType();
    }

    public FileTransfer.ReasonCode getReasonCode() throws RemoteException {
        Log.d(LOG_TAG, "getReasonCode");
        FileTransfer.ReasonCode reasonCode = FileTransfer.ReasonCode.UNSPECIFIED;
        CancelReason cancel = this.mFtMsg.getCancelReason();
        FtRejectReason reject = this.mFtMsg.getRejectReason();
        if (reject != null) {
            return FileTransferingServiceImpl.ftRejectReasonTranslator(reject);
        }
        return FileTransferingServiceImpl.ftCancelReasonTranslator(cancel);
    }

    public boolean canPauseTransfer() throws RemoteException {
        return false;
    }

    public boolean canResendTransfer() throws RemoteException {
        return true;
    }

    public long getTimestamp() throws RemoteException {
        return this.mFtMsg.getInsertedTimestamp();
    }

    public long getTimestampSent() throws RemoteException {
        if (ImDirection.OUTGOING == this.mFtMsg.getDirection()) {
            return this.mFtMsg.getInsertedTimestamp();
        }
        return this.mFtMsg.getSentTimestamp();
    }

    public long getFileExpiration() throws RemoteException {
        long time = 0;
        try {
            time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(this.mFtMsg.getFileExpire()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            String str = LOG_TAG;
            Log.e(str, e2.toString() + "time is 0");
            time = 0;
        }
        String str2 = LOG_TAG;
        Log.d(str2, "getFileExpiration, time:" + time);
        return time;
    }

    public long getFileIconExpiration() throws RemoteException {
        return getFileExpiration();
    }

    public boolean isAllowedToResumeTransfer() throws RemoteException {
        return true;
    }

    public long getTimestampDelivered() throws RemoteException {
        if (this.mFtMsg.isOutgoing()) {
            return this.mFtMsg.getDeliveredTimestamp();
        }
        return 0;
    }

    public long getTimestampDisplayed() throws RemoteException {
        if (this.mFtMsg.isOutgoing()) {
            return this.mFtMsg.getDisplayedTimestamp().longValue();
        }
        return 0;
    }

    public boolean isRead() throws RemoteException {
        if (this.mFtMsg.getStatus() == ImConstants.Status.READ) {
            return true;
        }
        return false;
    }

    public boolean isExpiredDelivery() throws RemoteException {
        if (this.mFtMsg.getDispositionNotification().size() == 0) {
            return true;
        }
        return false;
    }
}
