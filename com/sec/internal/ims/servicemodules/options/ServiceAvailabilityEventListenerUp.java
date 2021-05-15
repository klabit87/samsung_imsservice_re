package com.sec.internal.ims.servicemodules.options;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.options.IServiceAvailabilityEventListener;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.Date;

public class ServiceAvailabilityEventListenerUp implements IServiceAvailabilityEventListener {
    private static final int EVT_UPDATE_CAP_TIMESTAMP = 1;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ServiceAvailabilityEventListenerUp.class.getSimpleName();
    /* access modifiers changed from: private */
    public final CapabilitiesCache mCapabilitiesList;
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public final UriGenerator mUriGenerator;

    public ServiceAvailabilityEventListenerUp(Looper looper, CapabilitiesCache capabilitiesCache, UriGenerator uriGenerator) {
        Preconditions.checkNotNull(looper);
        Preconditions.checkNotNull(capabilitiesCache);
        this.mCapabilitiesList = capabilitiesCache;
        this.mUriGenerator = uriGenerator;
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                String extFeature;
                Message message = msg;
                if (message.what == 1) {
                    ServiceAvailabilityUpdateData data = (ServiceAvailabilityUpdateData) message.obj;
                    ImsUri uri = data.getUri();
                    Date timestamp = data.getDate();
                    String ownIdentity = data.getOwnIdentity();
                    if (SimManagerFactory.getPhoneId(ownIdentity) == -1) {
                        String access$000 = ServiceAvailabilityEventListenerUp.LOG_TAG;
                        Log.e(access$000, "EVT_UPDATE_CAP_TIMESTAMP: failed to find phoneId for ownIdentity: " + IMSLog.checker(ownIdentity) + "!");
                        return;
                    }
                    if (ServiceAvailabilityEventListenerUp.this.mUriGenerator != null) {
                        uri = ServiceAvailabilityEventListenerUp.this.mUriGenerator.normalize(uri);
                    } else {
                        String access$0002 = ServiceAvailabilityEventListenerUp.LOG_TAG;
                        Log.e(access$0002, "mUriGenerator is null, URI[" + IMSLog.checker(uri) + "] may not be normalized!");
                    }
                    Capabilities caps = ServiceAvailabilityEventListenerUp.this.mCapabilitiesList.get(uri);
                    if (caps == null || !caps.isAvailable()) {
                        String access$0003 = ServiceAvailabilityEventListenerUp.LOG_TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("URI[");
                        sb.append(IMSLog.checker(uri));
                        sb.append("] ");
                        sb.append(caps);
                        Log.i(access$0003, sb.toString() != null ? "is offline, ignore." : "has no caps in db, ignore.");
                    } else if (data.getDate().after(caps.getTimestamp())) {
                        if (timestamp.after(new Date())) {
                            timestamp = new Date();
                        }
                        if (!CollectionUtils.isNullOrEmpty((Collection<?>) caps.getExtFeature())) {
                            extFeature = String.join(",", caps.getExtFeature());
                        } else {
                            extFeature = "";
                        }
                        ServiceAvailabilityEventListenerUp.this.mCapabilitiesList.update(caps.getUri(), caps.getFeature(), caps.getAvailableFeatures(), false, caps.getPidf(), caps.getLastSeen(), timestamp, caps.getPAssertedId(), caps.getIsTokenUsed(), extFeature, caps.getExpCapInfoExpiry());
                        String access$0004 = ServiceAvailabilityEventListenerUp.LOG_TAG;
                        Log.i(access$0004, "Timestamp for URI[" + IMSLog.checker(uri) + "] updated to " + timestamp.toString());
                    } else {
                        String access$0005 = ServiceAvailabilityEventListenerUp.LOG_TAG;
                        Log.i(access$0005, "Message timestamp is older than the last recorded timestamp for URI[" + IMSLog.checker(uri) + "], ignore.");
                    }
                }
            }
        };
    }

    public void onServiceAvailabilityUpdate(String ownIdentity, ImsUri uri, Date timestamp) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, new ServiceAvailabilityUpdateData(timestamp, ownIdentity, uri)));
    }

    private class ServiceAvailabilityUpdateData {
        private Date date;
        private String ownIdentity;
        private ImsUri uri;

        ServiceAvailabilityUpdateData(Date date2, String ownIdentity2, ImsUri uri2) {
            this.uri = uri2;
            this.date = date2;
            this.ownIdentity = ownIdentity2;
        }

        public ImsUri getUri() {
            return this.uri;
        }

        public Date getDate() {
            return this.date;
        }

        public String getOwnIdentity() {
            return this.ownIdentity;
        }
    }
}
