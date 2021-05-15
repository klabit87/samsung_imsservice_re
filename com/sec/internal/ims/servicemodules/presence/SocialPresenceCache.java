package com.sec.internal.ims.servicemodules.presence;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SocialPresenceCache {
    private static final int CACHE_SIZE = 200;
    private static final String LOG_TAG = "SocialPresenceCache";
    private static final int PERSIST_MAX_SIZE = 100;
    private static final int PERSIST_TIMEOUT = 2000;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsPersistPosted = false;
    /* access modifiers changed from: private */
    public boolean mPersistTimeout = false;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private LinkedHashMap<ImsUri, PresenceInfo> mPresenceInfoList = new LinkedHashMap<ImsUri, PresenceInfo>() {
        /* access modifiers changed from: protected */
        public boolean removeEldestEntry(Map.Entry<ImsUri, PresenceInfo> entry) {
            return size() == 201;
        }
    };
    /* access modifiers changed from: private */
    public SocialPresenceStorage mPresenceStorage = null;
    private Handler mPresenceStorageHandler;
    private HandlerThread mThread = new HandlerThread("SocialPresenceStorage", 10);
    private ArrayList<ImsUri> mUriListToDelete = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<ImsUri> mUriListToUpdate = new ArrayList<>();

    public SocialPresenceCache(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        initPresenceStorage();
    }

    private void initPresenceStorage() {
        this.mThread.start();
        this.mPresenceStorageHandler = new Handler(this.mThread.getLooper());
        this.mPresenceStorage = new SocialPresenceStorage(this.mContext, this, this.mPhoneId);
    }

    private void resetPresenceStorage() {
        this.mPresenceStorageHandler.post(new Runnable() {
            public void run() {
                SocialPresenceCache.this.mPresenceStorage.reset();
            }
        });
    }

    /* access modifiers changed from: private */
    public void tryPersist(final boolean force) {
        this.mPresenceStorageHandler.post(new Runnable() {
            public void run() {
                if (force || SocialPresenceCache.this.mPersistTimeout || SocialPresenceCache.this.mUriListToUpdate.size() >= 100) {
                    int access$300 = SocialPresenceCache.this.mPhoneId;
                    IMSLog.i(SocialPresenceCache.LOG_TAG, access$300, "tryPersist: force = " + force + ", timeout = " + SocialPresenceCache.this.mPersistTimeout);
                    boolean unused = SocialPresenceCache.this.mIsPersistPosted = false;
                    boolean unused2 = SocialPresenceCache.this.mPersistTimeout = false;
                    SocialPresenceCache.this.mPresenceStorage.persist();
                } else if (!SocialPresenceCache.this.mIsPersistPosted) {
                    boolean unused3 = SocialPresenceCache.this.mIsPersistPosted = true;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (SocialPresenceCache.this.mUriListToUpdate.size() > 0) {
                                int access$300 = SocialPresenceCache.this.mPhoneId;
                                IMSLog.i(SocialPresenceCache.LOG_TAG, access$300, "tryPersist: try remainder " + SocialPresenceCache.this.mUriListToUpdate.size());
                                boolean unused = SocialPresenceCache.this.mPersistTimeout = true;
                                SocialPresenceCache.this.tryPersist(false);
                            }
                        }
                    }, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                }
            }
        });
    }

    public void add(PresenceInfo pi) {
        if (pi != null && pi.getTelUri() != null) {
            this.mPresenceInfoList.put(ImsUri.parse(pi.getTelUri()), pi);
        }
    }

    public void remove(List<ImsUri> uris) {
        if (uris != null) {
            IMSLog.s(LOG_TAG, "remove: " + uris);
            if (uris.size() > 0) {
                for (ImsUri uri : uris) {
                    this.mPresenceInfoList.remove(uri);
                }
                this.mUriListToDelete.addAll(uris);
                tryPersist(true);
            }
        }
    }

    public void update(ImsUri uri, PresenceInfo pi) {
        this.mPresenceInfoList.put(uri, pi);
        this.mUriListToUpdate.add(uri);
        tryPersist(false);
    }

    public PresenceInfo get(ImsUri uri) {
        if (uri == null) {
            return null;
        }
        PresenceInfo pi = this.mPresenceInfoList.get(uri);
        if (pi == null) {
            IMSLog.s(LOG_TAG, this.mPhoneId, "get: not found. presenceInfo from db");
            pi = this.mPresenceStorage.get(uri);
            if (pi != null) {
                this.mPresenceInfoList.put(uri, pi);
            }
        }
        return pi;
    }

    public Map<ImsUri, PresenceInfo> get(List<ImsUri> telUriList) {
        if (telUriList == null) {
            return null;
        }
        return this.mPresenceStorage.get(telUriList);
    }

    public void clear() {
        this.mPresenceInfoList.clear();
        resetPresenceStorage();
    }

    public String toString() {
        return "SocialPresenceCache: " + this.mPresenceInfoList.keySet();
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
}
