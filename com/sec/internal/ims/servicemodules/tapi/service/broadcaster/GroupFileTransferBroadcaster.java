package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.IGroupFileTransferListener;
import com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupFileTransferBroadcaster implements IGroupFileTransferBroadcaster {
    private static final String LOG_TAG = GroupFileTransferBroadcaster.class.getSimpleName();
    private Context mContext = null;
    private final RemoteCallbackList<IGroupFileTransferListener> mGroupFileTransferListeners = new RemoteCallbackList<>();

    public GroupFileTransferBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addGroupFileTransferListener(IGroupFileTransferListener listener) {
        this.mGroupFileTransferListeners.register(listener);
    }

    public void removeGroupFileTransferListener(IGroupFileTransferListener listener) {
        this.mGroupFileTransferListeners.unregister(listener);
    }

    public void broadcastTransferStateChanged(String chatId, String transferId, FileTransfer.State state, FileTransfer.ReasonCode reasonCode) {
        int N = this.mGroupFileTransferListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onStateChanged(chatId, transferId, state, reasonCode);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastTransferprogress(String chatId, String transferId, long currentSize, long totalSize) {
        int N = this.mGroupFileTransferListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onProgressUpdate(chatId, transferId, currentSize, totalSize);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastGroupDeliveryInfoStateChanged(String chatId, String transferId, ContactId contact, GroupDeliveryInfo.Status state, GroupDeliveryInfo.ReasonCode reasonCode) {
        int N = this.mGroupFileTransferListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onDeliveryInfoChanged(chatId, transferId, contact, state, reasonCode);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastDeleted(String chatId, Set<String> transferIds) {
        int N = this.mGroupFileTransferListeners.beginBroadcast();
        List<String> listIds = new ArrayList<>(transferIds);
        for (int i = 0; i < N; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onDeleted(chatId, listIds);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastFileTransferInvitation(String fileTransferId) {
        Intent invitation = new Intent("com.gsma.services.rcs.filetransfer.action.NEW_FILE_TRANSFER");
        invitation.putExtra("transferId", fileTransferId);
        this.mContext.sendBroadcast(invitation);
    }

    public void broadcastResumeFileTransfer(String filetransferId) {
        Intent resumeFileTransfer = new Intent("com.gsma.services.rcs.filetransfer.action.RESUME_FILE_TRANSFER");
        resumeFileTransfer.putExtra("transferId", filetransferId);
        this.mContext.sendBroadcast(resumeFileTransfer);
    }
}
