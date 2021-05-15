package com.sec.internal.constants.ims.servicemodules.presence;

import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PresenceSubscription implements Cloneable {
    public static final int EXPIRED = 2;
    public static final int FAILED = 6;
    public static final int FETCH_DONE = 4;
    private static final String LOG_TAG = "PresenceSubscription";
    public static final int ONLINE = 1;
    public static final int REQUESTED = 0;
    public static final int RETRIED = 5;
    private Set<ImsUri> mDropUris = new HashSet();
    private int mExpiry;
    private String mId;
    private int mPhoneId;
    private int mRetryCount;
    private boolean mSingleFetch;
    private int mState;
    private Date mTimestamp;
    private CapabilityConstants.RequestType mType;
    private Set<ImsUri> mUriList = new HashSet();

    public PresenceSubscription(String subscriptionId) {
        this.mId = subscriptionId;
        this.mState = 0;
        this.mTimestamp = new Date();
        this.mExpiry = 0;
        this.mType = CapabilityConstants.RequestType.REQUEST_TYPE_NONE;
        this.mRetryCount = 0;
        this.mSingleFetch = true;
    }

    public Set<ImsUri> getUriList() {
        return this.mUriList;
    }

    public void addUri(ImsUri uri) {
        this.mUriList.add(uri);
    }

    public void addUriAll(List<ImsUri> uris) {
        this.mUriList.addAll(uris);
    }

    public void remove(ImsUri uri) {
        this.mUriList.remove(uri);
    }

    public boolean contains(ImsUri uri) {
        return this.mUriList.contains(uri);
    }

    public Set<ImsUri> getDropUris() {
        return this.mDropUris;
    }

    public void addDropUriAll(List<ImsUri> uris) {
        this.mDropUris.addAll(uris);
    }

    public void removeDropUri(ImsUri uri) {
        this.mDropUris.remove(uri);
    }

    public boolean containsDropUri(ImsUri uri) {
        return this.mDropUris.contains(uri);
    }

    public void setExpiry(int expiry) {
        this.mExpiry = expiry;
    }

    public int getExpiry() {
        return this.mExpiry;
    }

    public void setPhoneId(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public String getSubscriptionId() {
        return this.mId;
    }

    public void updateState(int state) {
        this.mState = state;
    }

    public int getState() {
        return this.mState;
    }

    public void updateTimestamp() {
        this.mTimestamp = new Date();
    }

    public Date getTimestamp() {
        return this.mTimestamp;
    }

    public void setSingleFetch(boolean singleFetch) {
        this.mSingleFetch = singleFetch;
    }

    public boolean isSingleFetch() {
        return this.mSingleFetch;
    }

    public boolean isExpired() {
        int i = this.mState;
        if (i == 2 || i == 4) {
            return true;
        }
        Date current = new Date();
        if (this.mState == 5 || current.getTime() - this.mTimestamp.getTime() < ((long) this.mExpiry) * 1000) {
            return false;
        }
        this.mState = 2;
        return true;
    }

    public void setRequestType(CapabilityConstants.RequestType type) {
        this.mType = type;
    }

    public CapabilityConstants.RequestType getRequestType() {
        return this.mType;
    }

    public int getRetryCount() {
        return this.mRetryCount;
    }

    public void retrySubscription() {
        this.mRetryCount++;
    }

    public boolean isLongLivedSubscription() {
        Date current = new Date();
        long offset = current.getTime() - this.mTimestamp.getTime();
        Log.d(LOG_TAG, "isLongLivedSubscription: interval from " + this.mTimestamp + " to " + current.getTime() + ", offset " + offset);
        return offset > 3000;
    }

    public PresenceSubscription clone() throws CloneNotSupportedException {
        return (PresenceSubscription) super.clone();
    }
}
