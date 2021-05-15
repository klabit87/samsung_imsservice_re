package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.diagnosis.RcsHqmAgent;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CapabilitiesCache {
    private static final String LOG_TAG = "CapabilitiesCache";
    private static final int PERSIST_MAX_SIZE = 100;
    private static final int PERSIST_TIMEOUT = 3000;
    private final Map<ImsUri, Capabilities> mCapabilitiesList = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public CapabilityStorage mCapabilityStorage = null;
    private Handler mCapabilityStorageHandler;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsPersistPosted = false;
    /* access modifiers changed from: private */
    public boolean mPersistTimeout = false;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private HandlerThread mThread = new HandlerThread("CapabilityStorage", 10);
    private ArrayList<ImsUri> mUriListToDelete = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<ImsUri> mUriListToUpdate = new ArrayList<>();

    public CapabilitiesCache(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        initCapabilityStorage();
    }

    private void initCapabilityStorage() {
        this.mThread.start();
        this.mCapabilityStorageHandler = new Handler(this.mThread.getLooper());
        this.mCapabilityStorage = new CapabilityStorage(this.mContext, this, this.mPhoneId);
    }

    public void loadCapabilityStorage() {
        this.mCapabilitiesList.clear();
        this.mCapabilityStorageHandler.post(new Runnable() {
            public void run() {
                CapabilitiesCache.this.mCapabilityStorage.load();
            }
        });
    }

    private void resetCapabilityStorage() {
        this.mCapabilityStorageHandler.post(new Runnable() {
            public void run() {
                CapabilitiesCache.this.mCapabilityStorage.reset();
            }
        });
    }

    /* access modifiers changed from: private */
    public void tryPersist(final boolean force) {
        this.mCapabilityStorageHandler.post(new Runnable() {
            public void run() {
                if (force || CapabilitiesCache.this.mPersistTimeout || CapabilitiesCache.this.mUriListToUpdate.size() >= 100) {
                    int access$300 = CapabilitiesCache.this.mPhoneId;
                    IMSLog.i(CapabilitiesCache.LOG_TAG, access$300, "tryPersist: force = " + force + ", timeout = " + CapabilitiesCache.this.mPersistTimeout);
                    boolean unused = CapabilitiesCache.this.mIsPersistPosted = false;
                    boolean unused2 = CapabilitiesCache.this.mPersistTimeout = false;
                    CapabilitiesCache.this.mCapabilityStorage.persist();
                } else if (!CapabilitiesCache.this.mIsPersistPosted) {
                    boolean unused3 = CapabilitiesCache.this.mIsPersistPosted = true;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (CapabilitiesCache.this.mUriListToUpdate.size() > 0) {
                                int access$300 = CapabilitiesCache.this.mPhoneId;
                                IMSLog.i(CapabilitiesCache.LOG_TAG, access$300, "tryPersist: try remainder " + CapabilitiesCache.this.mUriListToUpdate.size());
                                boolean unused = CapabilitiesCache.this.mPersistTimeout = true;
                                CapabilitiesCache.this.tryPersist(false);
                            }
                        }
                    }, 3000);
                }
            }
        });
    }

    private void persistToContactDB(final Capabilities capex, final boolean isNotifyUpdated) {
        this.mCapabilityStorageHandler.post(new Runnable() {
            public void run() {
                CapabilitiesCache.this.mCapabilityStorage.persistToContactDB(capex, isNotifyUpdated);
            }
        });
    }

    public void deleteNonRcsDataFromContactDB() {
        this.mCapabilityStorageHandler.post(new Runnable() {
            public void run() {
                CapabilitiesCache.this.mCapabilityStorage.deleteNonRcsDataFromContactDB();
            }
        });
    }

    public Collection<Capabilities> getCapabilities() {
        return Collections.unmodifiableCollection(this.mCapabilitiesList.values());
    }

    public Collection<Capabilities> getRcsUserCapabilities() {
        List<Capabilities> rcsUsers = new ArrayList<>();
        for (Capabilities capex : this.mCapabilitiesList.values()) {
            if (capex.getFeature() > 0) {
                rcsUsers.add(capex);
            }
        }
        return rcsUsers;
    }

    /* access modifiers changed from: private */
    public int getAmountCapabilities() {
        return this.mCapabilityStorage.getAmountCapabilities();
    }

    /* access modifiers changed from: private */
    public int getAmountRcsCapabilities() {
        return this.mCapabilityStorage.getAmountRcsCapabilities();
    }

    public void sendRCSCInfoToHQM() {
        this.mCapabilityStorageHandler.post(new Runnable() {
            public void run() {
                Map<String, String> rcsmKeys = new LinkedHashMap<>();
                rcsmKeys.put(DiagnosisConstants.RCSC_KEY_NCAP, String.valueOf(CapabilitiesCache.this.getAmountCapabilities()));
                rcsmKeys.put(DiagnosisConstants.RCSC_KEY_NRCS, String.valueOf(CapabilitiesCache.this.getAmountRcsCapabilities()));
                RcsHqmAgent.sendRCSInfoToHQM(CapabilitiesCache.this.mContext, "RCSC", CapabilitiesCache.this.mPhoneId, rcsmKeys);
            }
        });
    }

    public void add(Capabilities capex) {
        if (capex == null || capex.getUri() == null) {
            Log.i(LOG_TAG, "add: null CapexInfo.");
            return;
        }
        ImsUri uri = capex.getUri();
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "add: " + uri);
        this.mCapabilitiesList.put(uri, capex);
    }

    public void remove(List<ImsUri> uris) {
        if (uris != null) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "remove: " + uris);
            if (uris.size() > 0) {
                for (ImsUri uri : uris) {
                    this.mCapabilitiesList.remove(uri);
                }
                this.mUriListToDelete.addAll(uris);
                tryPersist(true);
            }
        }
    }

    public boolean update(ImsUri uri, long features, long availFeatures, boolean isPolling, String pidf, long lastSeen, Date timestamp, List<ImsUri> pAssertedIds, boolean isTokenUsed, String extFeature, int expCapInfoExpiry) {
        String str;
        Capabilities capex;
        Capabilities.FetchType type;
        ImsUri imsUri = uri;
        long j = features;
        long j2 = lastSeen;
        List<ImsUri> list = pAssertedIds;
        String str2 = extFeature;
        if (imsUri == null) {
            return false;
        }
        if (hasCapabilities(uri)) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "update: Known Uri = " + imsUri);
            capex = this.mCapabilitiesList.get(imsUri);
            str = LOG_TAG;
        } else {
            int i2 = this.mPhoneId;
            IMSLog.s(LOG_TAG, i2, "update: Add new capabilities from Unknown Uri = " + imsUri);
            String msisdnNumber = UriUtil.getMsisdnNumber(uri);
            str = LOG_TAG;
            capex = new Capabilities(uri, msisdnNumber, (String) null, -1, (String) null);
            add(capex);
        }
        if (capex == null) {
            IMSLog.e(str, this.mPhoneId, "update: capex is null");
            return false;
        }
        int i3 = this.mPhoneId;
        IMSLog.i(str, i3, "update: feature changed " + capex.getFeature() + " to " + j);
        boolean hasCapChanged = isAvailable(j) != isAvailable(capex.getFeature()) || (isAvailable(j) && j != capex.getFeature());
        if (isPolling) {
            type = Capabilities.FetchType.FETCH_TYPE_POLL;
        } else {
            type = Capabilities.FetchType.FETCH_TYPE_OTHER;
        }
        capex.setFetchType(type);
        capex.setTimestamp(timestamp);
        capex.setUri(imsUri);
        capex.setFeatures(j);
        capex.setAvailableFeatures(availFeatures);
        capex.setAvailiable(isAvailable(j));
        capex.setPidf(pidf);
        Capabilities.FetchType fetchType = type;
        capex.setPhoneId(this.mPhoneId);
        if (str2 != null) {
            capex.setExtFeature(new ArrayList<>(Arrays.asList(str2.split(","))));
        }
        Log.i(str, "update: setting last seen in capabilities " + j2);
        capex.setLastSeen(j2);
        capex.setIsTokenUsed(isTokenUsed);
        capex.setExpCapInfoExpiry(expCapInfoExpiry);
        if (list != null) {
            capex.setPAssertedId(list);
        }
        if (capex.getLegacyLatching() && (capex.isFeatureAvailable(Capabilities.FEATURE_CHAT_CPM) || capex.isFeatureAvailable(Capabilities.FEATURE_CHAT_SIMPLE_IM) || capex.isFeatureAvailable(Capabilities.FEATURE_FT) || capex.isFeatureAvailable(Capabilities.FEATURE_FT_HTTP) || capex.isFeatureAvailable(Capabilities.FEATURE_FT_STORE))) {
            capex.setLegacyLatching(false);
            IMSLog.i(str, this.mPhoneId, "update: Legacy Latching clear !!");
        }
        this.mUriListToUpdate.add(imsUri);
        tryPersist(false);
        persistToContactDB(capex, hasCapChanged);
        return hasCapChanged;
    }

    public void updateContactInfo(ImsUri uri, String number, String id, String name) {
        Capabilities capex;
        if (hasCapabilities(uri)) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "updateContactInfo: update " + uri);
            capex = this.mCapabilitiesList.get(uri);
            capex.updateCapabilities(number, id, name);
        } else {
            int i2 = this.mPhoneId;
            IMSLog.s(LOG_TAG, i2, "updateContactInfo: new capabilities update for uri " + uri);
            capex = new Capabilities(uri, number, id, -1, name);
            add(capex);
        }
        capex.setFetchType(Capabilities.FetchType.FETCH_TYPE_OTHER);
        capex.setTimestamp(new Date());
        capex.setPhoneId(this.mPhoneId);
        persistToContactDB(capex, false);
    }

    public void persistCachedUri(ImsUri uri) {
        if (hasCapabilities(uri)) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "persistCachedUri: uri = " + uri);
            this.mUriListToUpdate.add(uri);
            tryPersist(false);
        }
    }

    public boolean isAvailable(long features) {
        return (features == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) || features == ((long) Capabilities.FEATURE_NON_RCS_USER) || features == ((long) Capabilities.FEATURE_NOT_UPDATED)) ? false : true;
    }

    public Capabilities get(int id) {
        for (Capabilities capex : this.mCapabilitiesList.values()) {
            if (capex.getId() == ((long) id)) {
                return capex;
            }
        }
        Log.e(LOG_TAG, "get: not found. Id " + id);
        return null;
    }

    public Capabilities get(ImsUri uri) {
        if (uri == null) {
            return null;
        }
        return this.mCapabilitiesList.get(uri);
    }

    private boolean hasCapabilities(ImsUri uri) {
        return uri != null && this.mCapabilitiesList.containsKey(uri);
    }

    public void clear() {
        this.mCapabilitiesList.clear();
        resetCapabilityStorage();
    }

    public List<ImsUri> getUpdatedUriList() {
        List<ImsUri> list = new ArrayList<>(this.mUriListToUpdate);
        this.mUriListToUpdate.clear();
        return list;
    }

    public List<ImsUri> getTrashedUriList() {
        List<ImsUri> list = new ArrayList<>(this.mUriListToDelete);
        this.mUriListToDelete.clear();
        return list;
    }

    public String toString() {
        return "CapabilitiesCache: " + this.mCapabilitiesList.keySet();
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        if (Extensions.Build.IS_DEBUGGABLE) {
            for (Capabilities cap : this.mCapabilitiesList.values()) {
                IMSLog.dump(LOG_TAG, cap.toString());
            }
        }
        IMSLog.decreaseIndent(LOG_TAG);
    }
}
