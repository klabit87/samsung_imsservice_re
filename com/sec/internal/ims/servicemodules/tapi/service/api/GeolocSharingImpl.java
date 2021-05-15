package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharing;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;

public class GeolocSharingImpl extends IGeolocSharing.Stub {
    private static final String LOG_TAG = GeolocSharingImpl.class.getSimpleName();
    private final FtMessage mGeoMsg;
    private final IGlsModule mGlsModule;

    public GeolocSharingImpl(FtMessage geoMsg, IGlsModule module) {
        this.mGeoMsg = geoMsg;
        this.mGlsModule = module;
    }

    public String getSharingId() throws ServerApiException {
        Log.i(LOG_TAG, "getSharingId()");
        return String.valueOf(this.mGeoMsg.getId());
    }

    public Geoloc getGeoloc() throws ServerApiException {
        Log.i(LOG_TAG, "getGeoloc()");
        FtMessage ftMessage = this.mGeoMsg;
        if (ftMessage == null || !MIMEContentType.LOCATION_PUSH.equals(ftMessage.getContentType())) {
            return null;
        }
        return getGeoInfo(this.mGeoMsg.getExtInfo());
    }

    public ContactId getRemoteContact() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "getRemoteContact=" + this.mGeoMsg.getRemoteUri());
        if (this.mGeoMsg.getRemoteUri() != null) {
            return new ContactId(this.mGeoMsg.getRemoteUri().toString());
        }
        return null;
    }

    public GeolocSharing.State getState() throws ServerApiException {
        GeolocSharing.State state = GeolocSharing.State.INVITED;
        ImDirection direction = this.mGeoMsg.getDirection();
        int stateId = this.mGeoMsg.getStateId();
        if (!(stateId == 0 || stateId == 1)) {
            if (stateId == 2) {
                return GeolocSharing.State.STARTED;
            }
            if (stateId == 3) {
                return GeolocSharing.State.RINGING;
            }
            if (stateId != 4) {
                if (stateId != 6) {
                    if (stateId != 7) {
                        return GeolocSharing.State.INVITED;
                    }
                }
            }
            if (ImDirection.INCOMING == direction) {
                return GeolocSharing.State.REJECTED;
            }
            if (ImDirection.OUTGOING == direction) {
                return GeolocSharing.State.ABORTED;
            }
            return state;
        }
        if (ImDirection.INCOMING == direction) {
            return GeolocSharing.State.INVITED;
        }
        if (ImDirection.OUTGOING == direction) {
            return GeolocSharing.State.INITIATING;
        }
        return state;
    }

    public GeolocSharing.ReasonCode getReasonCode() throws ServerApiException {
        GeolocSharing.ReasonCode reasonCode = GeolocSharing.ReasonCode.UNSPECIFIED;
        CancelReason cancel = this.mGeoMsg.getCancelReason();
        FtRejectReason reject = this.mGeoMsg.getRejectReason();
        if (reject != null) {
            return translatorRejectReason(reject);
        }
        return GeolocSharingServiceImpl.translateToReasonCode(cancel);
    }

    public String getMaapTrafficType() throws ServerApiException {
        String maapTrafficType = this.mGeoMsg.getMaapTrafficType();
        String str = LOG_TAG;
        Log.i(str, "getMaapTrafficType, maapTrafficType = [" + maapTrafficType + "]");
        return maapTrafficType;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.GeolocSharingImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason;

        static {
            int[] iArr = new int[FtRejectReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason = iArr;
            try {
                iArr[FtRejectReason.DECLINE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    private GeolocSharing.ReasonCode translatorRejectReason(FtRejectReason value) {
        if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason[value.ordinal()] != 1) {
            return GeolocSharing.ReasonCode.UNSPECIFIED;
        }
        return GeolocSharing.ReasonCode.FAILED_INITIATION;
    }

    public int getDirection() throws ServerApiException {
        return this.mGeoMsg.getDirection().getId();
    }

    public void acceptInvitation() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "Accept session invitation,id=" + this.mGeoMsg.getId());
        this.mGlsModule.acceptLocationShare((long) this.mGeoMsg.getId());
    }

    public void rejectInvitation() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "Reject session invitation,id=" + this.mGeoMsg.getId());
        this.mGlsModule.rejectLocationShare((long) this.mGeoMsg.getId());
    }

    public void abortSharing() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "Abort session invitation,id=" + this.mGeoMsg.getId());
        if (this.mGeoMsg.getStateId() != 3) {
            this.mGlsModule.cancelLocationShare((long) this.mGeoMsg.getId());
        }
    }

    private Geoloc getGeoInfo(String extInfo) {
        String label;
        String str = extInfo;
        if (str == null) {
            Log.d(LOG_TAG, "geolocation extinfo is null");
            return null;
        }
        String[] info = str.split(",");
        double latitude = Double.valueOf(info[0]).doubleValue();
        double longitude = Double.valueOf(info[1]).doubleValue();
        float accuracy = Float.valueOf(info[2]).floatValue();
        long expiration = Long.valueOf(info[3]).longValue();
        if (info.length != 5) {
            label = "";
        } else {
            label = info[4];
        }
        return new Geoloc(label, latitude, longitude, expiration, accuracy);
    }

    public long getTimeStamp() throws RemoteException {
        return this.mGeoMsg.getInsertedTimestamp();
    }
}
