package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharingListener;
import com.sec.internal.imscr.LogClass;
import java.util.List;

public class GeolocSharingEventBroadcaster implements IGeolocSharingEventBroadcaster {
    private static final String LOG_TAG = GeolocSharingEventBroadcaster.class.getSimpleName();
    private Context mContext;
    private final RemoteCallbackList<IGeolocSharingListener> mGeolocSharingListeners = new RemoteCallbackList<>();

    public GeolocSharingEventBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addEventListener(IGeolocSharingListener listener) {
        this.mGeolocSharingListeners.register(listener);
    }

    public void removeEventListener(IGeolocSharingListener listener) {
        this.mGeolocSharingListeners.unregister(listener);
    }

    public void broadcastGeolocSharingStateChanged(ContactId contact, String sharingId, GeolocSharing.State state, GeolocSharing.ReasonCode reasonCode) {
        Log.d(LOG_TAG, " broadcastGeolocSharingStateChanged()");
        int N = this.mGeolocSharingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mGeolocSharingListeners.getBroadcastItem(i).onStateChanged(contact, sharingId, state, reasonCode);
            } catch (RemoteException e) {
                if (e.getMessage() != null) {
                    Log.i(LOG_TAG, e.getMessage());
                } else {
                    e.printStackTrace();
                }
            }
        }
        this.mGeolocSharingListeners.finishBroadcast();
    }

    public void broadcastGeolocSharingprogress(ContactId contact, String sharingId, long currentSize, long totalSize) {
        Log.d(LOG_TAG, " broadcastGeolocSharingprogress()");
        int N = this.mGeolocSharingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mGeolocSharingListeners.getBroadcastItem(i).onProgressUpdate(contact, sharingId, currentSize, totalSize);
            } catch (RemoteException e) {
                if (e.getMessage() != null) {
                    Log.i(LOG_TAG, e.getMessage());
                } else {
                    e.printStackTrace();
                }
            }
        }
        this.mGeolocSharingListeners.finishBroadcast();
    }

    public void broadcastDeleted(ContactId contact, List<String> sharingIds) {
        Log.d(LOG_TAG, " broadcastDeleted()");
        int N = this.mGeolocSharingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mGeolocSharingListeners.getBroadcastItem(i).onDeleted(contact, sharingIds);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mGeolocSharingListeners.finishBroadcast();
    }

    public void broadcastGeolocSharingInvitation(String sharingId) {
        Log.d(LOG_TAG, " broadcastGeolocSharingInvitation()");
        Intent invitation = new Intent("com.gsma.services.rcs.sharing.geoloc.action.NEW_GEOLOC_SHARING");
        invitation.addFlags(16777216);
        invitation.addFlags(LogClass.SIM_EVENT);
        invitation.putExtra("sharingId", sharingId);
        this.mContext.sendBroadcast(invitation);
    }
}
