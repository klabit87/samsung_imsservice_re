package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.IOneToOneFileTransferListener;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OneToOneFileTransferBroadcaster implements IOneToOneFileTransferBroadcaster {
    private static final String LOG_TAG = OneToOneFileTransferBroadcaster.class.getSimpleName();
    private Context mContext = null;
    private final RemoteCallbackList<IOneToOneFileTransferListener> mOneToOneFileTransferListeners = new RemoteCallbackList<>();

    public OneToOneFileTransferBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addOneToOneFileTransferListener(IOneToOneFileTransferListener listener) {
        this.mOneToOneFileTransferListeners.register(listener);
    }

    public void removeOneToOneFileTransferListener(IOneToOneFileTransferListener listener) {
        this.mOneToOneFileTransferListeners.unregister(listener);
    }

    public void broadcastTransferStateChanged(ContactId contact, String transferId, FileTransfer.State state, FileTransfer.ReasonCode reasonCode) {
        Log.d(LOG_TAG, "start : broadcastMessageStatusChanged()");
        int N = this.mOneToOneFileTransferListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mOneToOneFileTransferListeners.getBroadcastItem(i).onStateChanged(contact, transferId, state, reasonCode);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneFileTransferListeners.finishBroadcast();
    }

    public void broadcastTransferprogress(ContactId contact, String transferId, long currentSize, long totalSize) {
        Log.d(LOG_TAG, "start : broadcastTransferprogress()");
        int N = this.mOneToOneFileTransferListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mOneToOneFileTransferListeners.getBroadcastItem(i).onProgressUpdate(contact, transferId, currentSize, totalSize);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneFileTransferListeners.finishBroadcast();
    }

    public void broadcastDeleted(String contact, Set<String> transferIds) {
        Log.d(LOG_TAG, "start : broadcastDeleted()");
        ContactId contactId = new ContactId(contact);
        List<String> listIds = new ArrayList<>(transferIds);
        int N = this.mOneToOneFileTransferListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mOneToOneFileTransferListeners.getBroadcastItem(i).onDeleted(contactId, listIds);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneFileTransferListeners.finishBroadcast();
    }

    public void broadcastFileTransferInvitation(String fileTransferId) {
        Log.d(LOG_TAG, "start : broadcastFileTransferInvitation()");
        Intent invitation = new Intent("com.gsma.services.rcs.filetransfer.action.NEW_FILE_TRANSFER");
        invitation.putExtra("transferId", fileTransferId);
        this.mContext.sendBroadcast(invitation);
    }

    public void broadcastResumeFileTransfer(String filetransferId) {
        Log.d(LOG_TAG, "start : broadcastResumeFileTransfer()");
        Intent resumeFileTransfer = new Intent("com.gsma.services.rcs.filetransfer.action.RESUME_FILE_TRANSFER");
        resumeFileTransfer.putExtra("transferId", filetransferId);
        this.mContext.sendBroadcast(resumeFileTransfer);
    }

    public void broadcastUndeliveredFileTransfer(ContactId contact) {
        Log.d(LOG_TAG, "start : broadcastResumeFileTransfer()");
        Intent undeliveredFileTransfer = new Intent("com.gsma.services.rcs.filetransfer.action.UNDELIVERED_FILE_TRANSFERS");
        undeliveredFileTransfer.putExtra(ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, contact);
        this.mContext.sendBroadcast(undeliveredFileTransfer);
    }
}
