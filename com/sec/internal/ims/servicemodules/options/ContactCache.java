package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.Contact;
import com.sec.internal.ims.servicemodules.presence.SocialPresenceStorage;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ContactCache {
    private static final int DELAY_REFRESH_COUNT = 300;
    private static final int DELAY_REFRESH_TIME = 300;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ContactCache.class.getSimpleName();
    private static final int MAX_COUNT = 1000;
    protected Handler mBackgroundHandler = null;
    private final List<CapabilitiesCache> mCapabilitiesCacheList = new CopyOnWriteArrayList();
    private ContactCacheHandler mContactCacheHandler = null;
    private int mContactCurCount = 0;
    private final Map<String, Contact> mContactList = new ConcurrentHashMap();
    private final Context mContext;
    /* access modifiers changed from: private */
    public String mCountryCode;
    private SimpleEventLog mEventLog;
    private boolean mIsBlockedContactChange = false;
    private boolean mIsBlockedInitialContactSyncBeforeRegi = false;
    private boolean mIsContactUpdated = false;
    private boolean mIsLimiting = false;
    private boolean mIsThrottle = false;
    private String mLastRawId = null;
    private long mLastRefreshTimeInMs = 0;
    private final List<ContactEventListener> mListeners = new CopyOnWriteArrayList();
    private Mno mMno = Mno.DEFAULT;
    private ContactObserver mObserver = null;
    private long mPrevRefreshTimeInMs = 0;
    private final List<String> mRemovedNumbers = new CopyOnWriteArrayList();
    private final AtomicBoolean mResyncRequired = new AtomicBoolean();
    private int mStartIndex = 0;
    private final AtomicBoolean mSyncInProgress = new AtomicBoolean();
    private UriGenerator mUriGenerator;
    private int mUserId = 0;

    public interface ContactEventListener {
        void onChanged();
    }

    public ContactCache(Context context, Map<Integer, CapabilitiesCache> capabilitiesMapList) {
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 20);
        this.mContactCacheHandler = new ContactCacheHandler();
        for (Map.Entry<Integer, CapabilitiesCache> e : capabilitiesMapList.entrySet()) {
            this.mCapabilitiesCacheList.add(capabilitiesMapList.get(e.getKey()));
        }
    }

    public void registerListener(ContactEventListener listener) {
        this.mListeners.add(listener);
    }

    public void unregisterListener(ContactEventListener listener) {
        this.mListeners.remove(listener);
    }

    public void start() {
        Log.i(LOG_TAG, "start:");
        this.mIsThrottle = false;
        HandlerThread thread = new HandlerThread("BackgroundHandler", 10);
        thread.start();
        this.mBackgroundHandler = new Handler(thread.getLooper());
        if (this.mObserver == null) {
            this.mObserver = new ContactObserver(new Handler());
            String str = LOG_TAG;
            Log.i(str, "start: Contact observer for userId " + this.mUserId);
            this.mUserId = Extensions.ActivityManager.getCurrentUser();
            try {
                Extensions.ContentResolver.registerContentObserver(this.mContext.getContentResolver(), ContactsContract.Contacts.CONTENT_URI, false, this.mObserver, this.mUserId);
            } catch (IllegalStateException e) {
            }
        }
    }

    public void stop() {
        Log.i(LOG_TAG, "stop:");
        if (this.mObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
        }
        this.mContactList.clear();
        this.mRemovedNumbers.clear();
        this.mLastRefreshTimeInMs = 0;
        this.mPrevRefreshTimeInMs = 0;
        this.mIsBlockedContactChange = false;
        this.mIsBlockedInitialContactSyncBeforeRegi = false;
    }

    public boolean isReady() {
        return this.mLastRefreshTimeInMs > 0;
    }

    public void setLastRefreshTime(long time) {
        String str = LOG_TAG;
        IMSLog.i(str, "setLastRefreshTime: mLastRefreshTimeInMs is " + time);
        this.mLastRefreshTimeInMs = time;
    }

    public long getLastRefreshTime() {
        String str = LOG_TAG;
        IMSLog.i(str, "getLastRefreshTime: mLastRefreshTimeInMs is " + this.mLastRefreshTimeInMs);
        return this.mLastRefreshTimeInMs;
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    public List<String> getNumberlistByContactId(String contactId) {
        Cursor cur;
        List<String> list = new ArrayList<>();
        String[] projection = {"data1", "data4"};
        String[] bindArgs = {contactId};
        try {
            cur = this.mContext.getContentResolver().query(getRemoteUriwithUserId(ContactsContract.CommonDataKinds.Phone.CONTENT_URI), projection, "contact_id = ?", bindArgs, (String) null);
            if (cur == null) {
                Log.e(LOG_TAG, "getNumberlistByContactId: no contact found");
                if (cur != null) {
                    cur.close();
                }
                return null;
            }
            String str = LOG_TAG;
            Log.i(str, "getNumberlistByContactId: found " + cur.getCount() + " contacts.");
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String number = cur.getString(0);
                    String e164number = cur.getString(1);
                    String number2 = checkNumberAndE164(number, e164number);
                    if (number2 != null) {
                        String e164number2 = changeE164ByNumber(number2, e164number);
                        if (e164number2 != null) {
                            list.add(e164number2);
                        }
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
            return list;
        } catch (RuntimeException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "getNumberlistByContactId: Exception " + e.getMessage());
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public Map<String, Contact> getContacts() {
        Map<String, Contact> contacts = new HashMap<>(this.mContactList);
        this.mContactList.clear();
        return contacts;
    }

    private Map<String, String> getAllCachedNumber() {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < this.mCapabilitiesCacheList.size(); i++) {
            for (Capabilities capex : this.mCapabilitiesCacheList.get(i).getCapabilities()) {
                if (capex.getContactId() != null) {
                    map.put(capex.getNumber(), capex.getNumber());
                }
            }
        }
        return map;
    }

    public List<String> getAndFlushRemovedNumbers() {
        List<String> list = new ArrayList<>(this.mRemovedNumbers);
        this.mRemovedNumbers.removeAll(list);
        return list;
    }

    private String checkNumberAndE164(String number, String e164number) {
        if (number == null) {
            return null;
        }
        if (number.startsWith("*67") || number.startsWith("*82")) {
            Log.i(LOG_TAG, "parsing for special character");
            number = number.substring(3);
        }
        if (number.contains("#") || number.contains("*") || number.contains(",") || number.contains("N")) {
            return null;
        }
        if ((e164number == null || "mx".equalsIgnoreCase(this.mCountryCode)) && !UriUtil.isValidNumber(number, this.mCountryCode)) {
            return null;
        }
        return number;
    }

    private String changeE164ByNumber(String number, String e164number) {
        if (e164number == null || "mx".equalsIgnoreCase(this.mCountryCode)) {
            return normalize(number);
        }
        return e164number;
    }

    private String normalize(String number) {
        String number2 = number.replaceAll("[- ()]", "");
        if (this.mUriGenerator != null && "US".equalsIgnoreCase(this.mCountryCode)) {
            return UriUtil.getMsisdnNumber(this.mUriGenerator.getNormalizedUri(number2, true));
        }
        ImsUri uri = UriUtil.parseNumber(number2, this.mCountryCode);
        if (uri != null) {
            return UriUtil.getMsisdnNumber(uri);
        }
        Log.e(LOG_TAG, "normalize: invalid number.");
        return number2;
    }

    private String[] setProjection() {
        return new String[]{CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, "raw_contact_id", SocialPresenceStorage.PresenceTable.DISPLAY_NAME, "data1", "data4"};
    }

    /* Debug info: failed to restart local var, previous not found, register: 21 */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0295 A[SYNTHETIC, Splitter:B:92:0x0295] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean refresh() {
        /*
            r21 = this;
            r1 = r21
            r2 = 0
            java.util.HashMap r0 = new java.util.HashMap
            r0.<init>()
            r3 = r0
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "refresh: cc "
            r4.append(r5)
            java.lang.String r5 = r1.mCountryCode
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.s(r0, r4)
            android.content.Context r0 = r1.mContext
            android.content.ContentResolver r10 = r0.getContentResolver()
            java.lang.String[] r11 = r21.setProjection()
            java.lang.String r12 = "contact_last_updated_timestamp > ?"
            r13 = 1
            java.lang.String[] r8 = new java.lang.String[r13]
            boolean r0 = r1.mIsLimiting
            if (r0 == 0) goto L_0x0038
            long r4 = r1.mPrevRefreshTimeInMs
            goto L_0x003a
        L_0x0038:
            long r4 = r1.mLastRefreshTimeInMs
        L_0x003a:
            java.lang.String r0 = java.lang.Long.toString(r4)
            r14 = 0
            r8[r14] = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r4 = "raw_contact_id LIMIT "
            r0.append(r4)
            int r4 = r1.mStartIndex
            r0.append(r4)
            java.lang.String r4 = ","
            r0.append(r4)
            r4 = 1000(0x3e8, float:1.401E-42)
            r0.append(r4)
            java.lang.String r15 = r0.toString()
            android.net.Uri r0 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            android.net.Uri r16 = r1.getRemoteUriwithUserId(r0)
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "refresh: mStartIndex = "
            r4.append(r5)
            int r5 = r1.mStartIndex
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r0, r4)
            com.sec.internal.helper.SimpleEventLog r0 = r1.mEventLog
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = LOG_TAG
            r4.append(r5)
            java.lang.String r5 = ": refresh: mStartIndex "
            r4.append(r5)
            int r5 = r1.mStartIndex
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r0.add(r4)
            r0 = 302383104(0x12060000, float:4.2282945E-28)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "N,REFR:"
            r4.append(r5)
            int r5 = r1.mStartIndex
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.c(r0, r4)
            r4 = r10
            r5 = r16
            r6 = r11
            r7 = r12
            r9 = r15
            android.database.Cursor r0 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ RuntimeException -> 0x02a1 }
            r4 = r0
            if (r4 != 0) goto L_0x00db
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00d5 }
            java.lang.String r5 = "refresh: no contact found"
            android.util.Log.e(r0, r5)     // Catch:{ all -> 0x00d5 }
            if (r4 == 0) goto L_0x00d4
            r4.close()     // Catch:{ RuntimeException -> 0x00cf }
            goto L_0x00d4
        L_0x00cf:
            r0 = move-exception
            r19 = r8
            goto L_0x02a4
        L_0x00d4:
            return r14
        L_0x00d5:
            r0 = move-exception
            r5 = r0
            r19 = r8
            goto L_0x0293
        L_0x00db:
            boolean r0 = r1.mIsLimiting     // Catch:{ all -> 0x028f }
            if (r0 != 0) goto L_0x00fc
            long r5 = r1.mLastRefreshTimeInMs     // Catch:{ all -> 0x00d5 }
            r1.mPrevRefreshTimeInMs = r5     // Catch:{ all -> 0x00d5 }
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00d5 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d5 }
            r5.<init>()     // Catch:{ all -> 0x00d5 }
            java.lang.String r6 = "refresh: set mPrevRefreshTimeInMs = "
            r5.append(r6)     // Catch:{ all -> 0x00d5 }
            long r6 = r1.mPrevRefreshTimeInMs     // Catch:{ all -> 0x00d5 }
            r5.append(r6)     // Catch:{ all -> 0x00d5 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00d5 }
            android.util.Log.i(r0, r5)     // Catch:{ all -> 0x00d5 }
        L_0x00fc:
            java.util.Date r0 = new java.util.Date     // Catch:{ all -> 0x028f }
            r0.<init>()     // Catch:{ all -> 0x028f }
            long r5 = r0.getTime()     // Catch:{ all -> 0x028f }
            r1.mLastRefreshTimeInMs = r5     // Catch:{ all -> 0x028f }
            int r0 = r4.getCount()     // Catch:{ all -> 0x028f }
            r1.mContactCurCount = r0     // Catch:{ all -> 0x028f }
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x028f }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x028f }
            r5.<init>()     // Catch:{ all -> 0x028f }
            java.lang.String r6 = "refresh: found "
            r5.append(r6)     // Catch:{ all -> 0x028f }
            int r6 = r1.mContactCurCount     // Catch:{ all -> 0x028f }
            r5.append(r6)     // Catch:{ all -> 0x028f }
            java.lang.String r6 = " contacts. mLastRefreshTimeInMs = "
            r5.append(r6)     // Catch:{ all -> 0x028f }
            long r6 = r1.mLastRefreshTimeInMs     // Catch:{ all -> 0x028f }
            r5.append(r6)     // Catch:{ all -> 0x028f }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x028f }
            android.util.Log.i(r0, r5)     // Catch:{ all -> 0x028f }
            com.sec.internal.helper.SimpleEventLog r0 = r1.mEventLog     // Catch:{ all -> 0x028f }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x028f }
            r5.<init>()     // Catch:{ all -> 0x028f }
            java.lang.String r6 = LOG_TAG     // Catch:{ all -> 0x028f }
            r5.append(r6)     // Catch:{ all -> 0x028f }
            java.lang.String r6 = ": refresh: found "
            r5.append(r6)     // Catch:{ all -> 0x028f }
            int r6 = r1.mContactCurCount     // Catch:{ all -> 0x028f }
            r5.append(r6)     // Catch:{ all -> 0x028f }
            java.lang.String r6 = " contacts."
            r5.append(r6)     // Catch:{ all -> 0x028f }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x028f }
            r0.add(r5)     // Catch:{ all -> 0x028f }
            r0 = 302383105(0x12060001, float:4.228295E-28)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x028f }
            r5.<init>()     // Catch:{ all -> 0x028f }
            java.lang.String r6 = "N,REFR:FOUND:"
            r5.append(r6)     // Catch:{ all -> 0x028f }
            int r6 = r1.mContactCurCount     // Catch:{ all -> 0x028f }
            r5.append(r6)     // Catch:{ all -> 0x028f }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x028f }
            com.sec.internal.log.IMSLog.c(r0, r5)     // Catch:{ all -> 0x028f }
            r0 = 0
            int r5 = r4.getCount()     // Catch:{ all -> 0x028f }
            if (r5 <= 0) goto L_0x0276
            r2 = 1
        L_0x0173:
            boolean r5 = r4.moveToNext()     // Catch:{ all -> 0x026f }
            if (r5 == 0) goto L_0x0245
            int r5 = r0 + 1
            int r0 = r5 % 300
            if (r0 != 0) goto L_0x0186
            r6 = 300(0x12c, double:1.48E-321)
            java.lang.Thread.sleep(r6)     // Catch:{ InterruptedException -> 0x0185 }
            goto L_0x0186
        L_0x0185:
            r0 = move-exception
        L_0x0186:
            java.lang.String r0 = r4.getString(r14)     // Catch:{ Exception -> 0x022f }
            java.lang.String r6 = r4.getString(r13)     // Catch:{ Exception -> 0x022f }
            r7 = 2
            java.lang.String r7 = r4.getString(r7)     // Catch:{ Exception -> 0x022f }
            r9 = 3
            java.lang.String r9 = r4.getString(r9)     // Catch:{ Exception -> 0x022f }
            r14 = 4
            java.lang.String r14 = r4.getString(r14)     // Catch:{ Exception -> 0x022f }
            java.lang.String r13 = r1.mLastRawId     // Catch:{ all -> 0x026f }
            if (r13 == 0) goto L_0x01d9
            int r13 = java.lang.Integer.parseInt(r6)     // Catch:{ all -> 0x026f }
            r18 = r2
            java.lang.String r2 = r1.mLastRawId     // Catch:{ all -> 0x01d1 }
            int r2 = java.lang.Integer.parseInt(r2)     // Catch:{ all -> 0x01d1 }
            if (r13 > r2) goto L_0x01cd
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x01d1 }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ all -> 0x01d1 }
            r13.<init>()     // Catch:{ all -> 0x01d1 }
            r19 = r8
            java.lang.String r8 = "refresh: ContactUpdated, rawId ="
            r13.append(r8)     // Catch:{ all -> 0x026a }
            r13.append(r6)     // Catch:{ all -> 0x026a }
            java.lang.String r8 = r13.toString()     // Catch:{ all -> 0x026a }
            android.util.Log.i(r2, r8)     // Catch:{ all -> 0x026a }
            r2 = 1
            r1.mIsContactUpdated = r2     // Catch:{ all -> 0x026a }
            goto L_0x01de
        L_0x01cd:
            r19 = r8
            r2 = 1
            goto L_0x01de
        L_0x01d1:
            r0 = move-exception
            r19 = r8
            r5 = r0
            r2 = r18
            goto L_0x0293
        L_0x01d9:
            r18 = r2
            r19 = r8
            r2 = 1
        L_0x01de:
            int r8 = r1.mContactCurCount     // Catch:{ all -> 0x026a }
            if (r5 != r8) goto L_0x01e4
            r1.mLastRawId = r6     // Catch:{ all -> 0x026a }
        L_0x01e4:
            java.lang.String r8 = r1.checkNumberAndE164(r9, r14)     // Catch:{ all -> 0x026a }
            if (r8 != 0) goto L_0x01eb
            goto L_0x023c
        L_0x01eb:
            java.lang.String r9 = r1.changeE164ByNumber(r8, r14)     // Catch:{ all -> 0x026a }
            java.lang.Object r13 = r3.get(r6)     // Catch:{ all -> 0x026a }
            com.sec.internal.ims.servicemodules.options.Contact r13 = (com.sec.internal.ims.servicemodules.options.Contact) r13     // Catch:{ all -> 0x026a }
            if (r13 != 0) goto L_0x0200
            java.util.Map<java.lang.String, com.sec.internal.ims.servicemodules.options.Contact> r14 = r1.mContactList     // Catch:{ all -> 0x026a }
            java.lang.Object r14 = r14.get(r6)     // Catch:{ all -> 0x026a }
            com.sec.internal.ims.servicemodules.options.Contact r14 = (com.sec.internal.ims.servicemodules.options.Contact) r14     // Catch:{ all -> 0x026a }
            r13 = r14
        L_0x0200:
            if (r13 != 0) goto L_0x0208
            com.sec.internal.ims.servicemodules.options.Contact r14 = new com.sec.internal.ims.servicemodules.options.Contact     // Catch:{ all -> 0x026a }
            r14.<init>(r0, r6)     // Catch:{ all -> 0x026a }
            r13 = r14
        L_0x0208:
            r13.setId(r0)     // Catch:{ all -> 0x026a }
            r13.setName(r7)     // Catch:{ all -> 0x026a }
            com.sec.internal.ims.servicemodules.options.Contact$ContactNumber r14 = new com.sec.internal.ims.servicemodules.options.Contact$ContactNumber     // Catch:{ all -> 0x026a }
            if (r9 != 0) goto L_0x0219
            r17 = 0
            r20 = r0
            r0 = r17
            goto L_0x0223
        L_0x0219:
            java.lang.String r2 = "[- ()]"
            r20 = r0
            java.lang.String r0 = ""
            java.lang.String r0 = r9.replaceAll(r2, r0)     // Catch:{ all -> 0x026a }
        L_0x0223:
            r14.<init>(r8, r0)     // Catch:{ all -> 0x026a }
            r0 = r14
            r13.insertContactNumberIntoList(r0)     // Catch:{ all -> 0x026a }
            r3.put(r6, r13)     // Catch:{ all -> 0x026a }
            goto L_0x023c
        L_0x022f:
            r0 = move-exception
            r18 = r2
            r19 = r8
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x026a }
            java.lang.String r6 = "Exception in cur.getString"
            android.util.Log.e(r2, r6)     // Catch:{ all -> 0x026a }
        L_0x023c:
            r0 = r5
            r2 = r18
            r8 = r19
            r13 = 1
            r14 = 0
            goto L_0x0173
        L_0x0245:
            r18 = r2
            r19 = r8
            com.sec.internal.helper.SimpleEventLog r2 = r1.mEventLog     // Catch:{ all -> 0x026a }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x026a }
            r5.<init>()     // Catch:{ all -> 0x026a }
            java.lang.String r6 = LOG_TAG     // Catch:{ all -> 0x026a }
            r5.append(r6)     // Catch:{ all -> 0x026a }
            java.lang.String r6 = "refresh: mLastRawId = "
            r5.append(r6)     // Catch:{ all -> 0x026a }
            java.lang.String r6 = r1.mLastRawId     // Catch:{ all -> 0x026a }
            r5.append(r6)     // Catch:{ all -> 0x026a }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x026a }
            r2.logAndAdd(r5)     // Catch:{ all -> 0x026a }
            r2 = r18
            goto L_0x0278
        L_0x026a:
            r0 = move-exception
            r5 = r0
            r2 = r18
            goto L_0x0293
        L_0x026f:
            r0 = move-exception
            r18 = r2
            r19 = r8
            r5 = r0
            goto L_0x0293
        L_0x0276:
            r19 = r8
        L_0x0278:
            java.util.Map<java.lang.String, com.sec.internal.ims.servicemodules.options.Contact> r5 = r1.mContactList     // Catch:{ all -> 0x028c }
            r5.clear()     // Catch:{ all -> 0x028c }
            java.util.Map<java.lang.String, com.sec.internal.ims.servicemodules.options.Contact> r5 = r1.mContactList     // Catch:{ all -> 0x028c }
            r5.putAll(r3)     // Catch:{ all -> 0x028c }
            r21.dumpContactList()     // Catch:{ all -> 0x028c }
            if (r4 == 0) goto L_0x028b
            r4.close()     // Catch:{ RuntimeException -> 0x029f }
        L_0x028b:
            return r2
        L_0x028c:
            r0 = move-exception
            r5 = r0
            goto L_0x0293
        L_0x028f:
            r0 = move-exception
            r19 = r8
            r5 = r0
        L_0x0293:
            if (r4 == 0) goto L_0x029e
            r4.close()     // Catch:{ all -> 0x0299 }
            goto L_0x029e
        L_0x0299:
            r0 = move-exception
            r6 = r0
            r5.addSuppressed(r6)     // Catch:{ RuntimeException -> 0x029f }
        L_0x029e:
            throw r5     // Catch:{ RuntimeException -> 0x029f }
        L_0x029f:
            r0 = move-exception
            goto L_0x02a4
        L_0x02a1:
            r0 = move-exception
            r19 = r8
        L_0x02a4:
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "refresh: Can not refresh : "
            r5.append(r6)
            java.lang.String r6 = r0.getMessage()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.e(r4, r5)
            r4 = 302383106(0x12060002, float:4.2282954E-28)
            java.lang.String r5 = "N,REFR:ER"
            com.sec.internal.log.IMSLog.c(r4, r5)
            r4 = 0
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.ContactCache.refresh():boolean");
    }

    private Uri getRemoteUriwithUserId(Uri contentUri) {
        return contentUri;
    }

    private void dumpContactList() {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (Contact contact : this.mContactList.values()) {
            count++;
            if (count > 40) {
                break;
            }
            builder.append("ContactId (");
            builder.append(contact.getId());
            builder.append(")");
            builder.append("    RawId: ");
            builder.append(contact.getRawId());
            builder.append(10);
            builder.append("    Name: ");
            builder.append(contact.getName());
            builder.append(10);
            Iterator<Contact.ContactNumber> it = contact.getContactNumberList().iterator();
            while (it.hasNext()) {
                Contact.ContactNumber cn = it.next();
                builder.append("    Number: ");
                builder.append(cn.getNumber());
                builder.append(10);
                builder.append("    E164: ");
                builder.append(cn.getNormalizedNumber());
                builder.append(10);
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, "dump:\n" + builder.toString());
    }

    public void setUriGenerator(UriGenerator uriGenerator) {
        this.mUriGenerator = uriGenerator;
    }

    public void setThrottleContactSync(boolean isThrottle) {
        Handler handler = this.mBackgroundHandler;
        if (handler != null) {
            handler.post(new Runnable(isThrottle) {
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ContactCache.this.lambda$setThrottleContactSync$0$ContactCache(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$setThrottleContactSync$0$ContactCache(boolean isThrottle) {
        Log.i(LOG_TAG, "setThrottleContactSync : " + isThrottle);
        if (this.mIsThrottle != isThrottle) {
            this.mIsThrottle = isThrottle;
            if (isThrottle) {
                return;
            }
            if (this.mResyncRequired.get() || this.mIsLimiting) {
                if (this.mIsLimiting) {
                    if (this.mResyncRequired.get()) {
                        processChangeDuringLimiting();
                    }
                    this.mStartIndex += 1000;
                    Log.i(LOG_TAG, "setThrottleContactSync : Limiting, mStartIndex = " + this.mStartIndex);
                }
                Log.i(LOG_TAG, "setThrottleContactSync : try to resync");
                sendMessageContactSync();
                this.mResyncRequired.set(false);
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 21 */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        android.util.Log.i(LOG_TAG, "processChangeDuringLimiting: rawId > mLastRawId, rawId = " + r6 + ", mLastRawId = " + r1.mLastRawId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ae, code lost:
        r16 = r4;
        r18 = r7;
     */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0173 A[SYNTHETIC, Splitter:B:62:0x0173] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x017d A[SYNTHETIC, Splitter:B:67:0x017d] */
    /* JADX WARNING: Removed duplicated region for block: B:84:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processChangeDuringLimiting() {
        /*
            r21 = this;
            r1 = r21
            java.lang.String r0 = LOG_TAG
            java.lang.String r2 = "processChangeDuringLimiting: Start."
            android.util.Log.i(r0, r2)
            r2 = 0
            android.content.Context r0 = r1.mContext
            android.content.ContentResolver r9 = r0.getContentResolver()
            java.lang.String[] r10 = r21.setProjection()
            java.lang.String r11 = "contact_last_updated_timestamp > ?"
            r0 = 1
            java.lang.String[] r7 = new java.lang.String[r0]
            long r3 = r1.mLastRefreshTimeInMs
            java.lang.String r3 = java.lang.Long.toString(r3)
            r12 = 0
            r7[r12] = r3
            java.lang.String r13 = "raw_contact_id"
            android.net.Uri r3 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            android.net.Uri r14 = r1.getRemoteUriwithUserId(r3)
            r3 = r9
            r4 = r14
            r5 = r10
            r6 = r11
            r8 = r13
            android.database.Cursor r3 = r3.query(r4, r5, r6, r7, r8)     // Catch:{ RuntimeException -> 0x0189 }
            if (r3 != 0) goto L_0x0046
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0040 }
            java.lang.String r4 = "processChangeDuringLimiting: no contact found"
            android.util.Log.e(r0, r4)     // Catch:{ all -> 0x0040 }
            goto L_0x0057
        L_0x0040:
            r0 = move-exception
            r4 = r0
            r18 = r7
            goto L_0x017b
        L_0x0046:
            int r4 = r3.getCount()     // Catch:{ all -> 0x0177 }
            if (r4 != 0) goto L_0x005b
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0040 }
            java.lang.String r5 = "processChangeDuringLimiting: found 0, removed"
            android.util.Log.i(r0, r5)     // Catch:{ all -> 0x0040 }
            r21.processRemovedContact()     // Catch:{ all -> 0x0040 }
        L_0x0057:
            r18 = r7
            goto L_0x0171
        L_0x005b:
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x0177 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0177 }
            r6.<init>()     // Catch:{ all -> 0x0177 }
            java.lang.String r8 = "processChangeDuringLimiting: found "
            r6.append(r8)     // Catch:{ all -> 0x0177 }
            r6.append(r4)     // Catch:{ all -> 0x0177 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0177 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x0177 }
            java.util.HashMap r5 = new java.util.HashMap     // Catch:{ all -> 0x0177 }
            r5.<init>()     // Catch:{ all -> 0x0177 }
        L_0x0077:
            boolean r6 = r3.moveToNext()     // Catch:{ all -> 0x0177 }
            if (r6 == 0) goto L_0x0145
            java.lang.String r6 = r3.getString(r0)     // Catch:{ all -> 0x0177 }
            int r8 = java.lang.Integer.parseInt(r6)     // Catch:{ all -> 0x0177 }
            java.lang.String r15 = r1.mLastRawId     // Catch:{ all -> 0x0177 }
            int r15 = java.lang.Integer.parseInt(r15)     // Catch:{ all -> 0x0177 }
            if (r8 <= r15) goto L_0x00b4
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0040 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0040 }
            r8.<init>()     // Catch:{ all -> 0x0040 }
            java.lang.String r12 = "processChangeDuringLimiting: rawId > mLastRawId, rawId = "
            r8.append(r12)     // Catch:{ all -> 0x0040 }
            r8.append(r6)     // Catch:{ all -> 0x0040 }
            java.lang.String r12 = ", mLastRawId = "
            r8.append(r12)     // Catch:{ all -> 0x0040 }
            java.lang.String r12 = r1.mLastRawId     // Catch:{ all -> 0x0040 }
            r8.append(r12)     // Catch:{ all -> 0x0040 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0040 }
            android.util.Log.i(r0, r8)     // Catch:{ all -> 0x0040 }
            r16 = r4
            r18 = r7
            goto L_0x0149
        L_0x00b4:
            r2 = 1
            java.lang.String r8 = r3.getString(r12)     // Catch:{ all -> 0x013e }
            r15 = 2
            java.lang.String r15 = r3.getString(r15)     // Catch:{ all -> 0x013e }
            r0 = 3
            java.lang.String r0 = r3.getString(r0)     // Catch:{ all -> 0x013e }
            r12 = 4
            java.lang.String r12 = r3.getString(r12)     // Catch:{ all -> 0x013e }
            java.lang.String r16 = r1.checkNumberAndE164(r0, r12)     // Catch:{ all -> 0x013e }
            r0 = r16
            if (r0 != 0) goto L_0x00d7
            r17 = r2
            r16 = r4
            r18 = r7
            goto L_0x0128
        L_0x00d7:
            java.lang.String r16 = r1.changeE164ByNumber(r0, r12)     // Catch:{ all -> 0x013e }
            r12 = r16
            java.lang.Object r16 = r5.get(r6)     // Catch:{ all -> 0x013e }
            com.sec.internal.ims.servicemodules.options.Contact r16 = (com.sec.internal.ims.servicemodules.options.Contact) r16     // Catch:{ all -> 0x013e }
            if (r16 != 0) goto L_0x00f7
            r17 = r2
            com.sec.internal.ims.servicemodules.options.Contact r2 = new com.sec.internal.ims.servicemodules.options.Contact     // Catch:{ all -> 0x00ef }
            r2.<init>(r8, r6)     // Catch:{ all -> 0x00ef }
            r16 = r2
            goto L_0x00fb
        L_0x00ef:
            r0 = move-exception
            r4 = r0
            r18 = r7
            r2 = r17
            goto L_0x017b
        L_0x00f7:
            r17 = r2
            r2 = r16
        L_0x00fb:
            r2.setId(r8)     // Catch:{ all -> 0x0137 }
            r2.setName(r15)     // Catch:{ all -> 0x0137 }
            r16 = r4
            com.sec.internal.ims.servicemodules.options.Contact$ContactNumber r4 = new com.sec.internal.ims.servicemodules.options.Contact$ContactNumber     // Catch:{ all -> 0x0137 }
            if (r12 != 0) goto L_0x0112
            r18 = 0
            r19 = r8
            r20 = r18
            r18 = r7
            r7 = r20
            goto L_0x011e
        L_0x0112:
            r18 = r7
            java.lang.String r7 = "[- ()]"
            r19 = r8
            java.lang.String r8 = ""
            java.lang.String r7 = r12.replaceAll(r7, r8)     // Catch:{ all -> 0x0132 }
        L_0x011e:
            r4.<init>(r0, r7)     // Catch:{ all -> 0x0132 }
            r2.insertContactNumberIntoList(r4)     // Catch:{ all -> 0x0132 }
            r5.put(r6, r2)     // Catch:{ all -> 0x0132 }
        L_0x0128:
            r4 = r16
            r2 = r17
            r7 = r18
            r0 = 1
            r12 = 0
            goto L_0x0077
        L_0x0132:
            r0 = move-exception
            r4 = r0
            r2 = r17
            goto L_0x017b
        L_0x0137:
            r0 = move-exception
            r18 = r7
            r4 = r0
            r2 = r17
            goto L_0x017b
        L_0x013e:
            r0 = move-exception
            r17 = r2
            r18 = r7
            r4 = r0
            goto L_0x017b
        L_0x0145:
            r16 = r4
            r18 = r7
        L_0x0149:
            if (r2 == 0) goto L_0x0171
            java.util.Map<java.lang.String, com.sec.internal.ims.servicemodules.options.Contact> r0 = r1.mContactList     // Catch:{ all -> 0x016e }
            r0.putAll(r5)     // Catch:{ all -> 0x016e }
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x016e }
            java.lang.String r4 = "processChangeDuringLimiting: Done. contact updated."
            android.util.Log.i(r0, r4)     // Catch:{ all -> 0x016e }
            java.util.List<com.sec.internal.ims.servicemodules.options.ContactCache$ContactEventListener> r0 = r1.mListeners     // Catch:{ all -> 0x016e }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ all -> 0x016e }
        L_0x015e:
            boolean r4 = r0.hasNext()     // Catch:{ all -> 0x016e }
            if (r4 == 0) goto L_0x0171
            java.lang.Object r4 = r0.next()     // Catch:{ all -> 0x016e }
            com.sec.internal.ims.servicemodules.options.ContactCache$ContactEventListener r4 = (com.sec.internal.ims.servicemodules.options.ContactCache.ContactEventListener) r4     // Catch:{ all -> 0x016e }
            r4.onChanged()     // Catch:{ all -> 0x016e }
            goto L_0x015e
        L_0x016e:
            r0 = move-exception
            r4 = r0
            goto L_0x017b
        L_0x0171:
            if (r3 == 0) goto L_0x0176
            r3.close()     // Catch:{ RuntimeException -> 0x0187 }
        L_0x0176:
            goto L_0x01a7
        L_0x0177:
            r0 = move-exception
            r18 = r7
            r4 = r0
        L_0x017b:
            if (r3 == 0) goto L_0x0186
            r3.close()     // Catch:{ all -> 0x0181 }
            goto L_0x0186
        L_0x0181:
            r0 = move-exception
            r5 = r0
            r4.addSuppressed(r5)     // Catch:{ RuntimeException -> 0x0187 }
        L_0x0186:
            throw r4     // Catch:{ RuntimeException -> 0x0187 }
        L_0x0187:
            r0 = move-exception
            goto L_0x018c
        L_0x0189:
            r0 = move-exception
            r18 = r7
        L_0x018c:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "processChangeDuringLimiting: Exception "
            r4.append(r5)
            java.lang.String r5 = r0.getMessage()
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r3, r4)
        L_0x01a7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.ContactCache.processChangeDuringLimiting():void");
    }

    private boolean processRemovedContact() {
        Log.i(LOG_TAG, "processRemovedContact: Start.");
        Map<String, String> cachedNumbersMap = getAllCachedNumber();
        if (cachedNumbersMap == null || cachedNumbersMap.size() == 0) {
            Log.i(LOG_TAG, "processRemovedContact: No cached numbers. return");
            return false;
        }
        List<String> allNumbersInContactDB = getAllNumbersInContactDB();
        if (allNumbersInContactDB == null || allNumbersInContactDB.size() == 0) {
            Log.i(LOG_TAG, "processRemovedContact: No numbers in Contact DB");
        } else {
            Log.i(LOG_TAG, "processRemovedContact: start remove");
            for (String number : allNumbersInContactDB) {
                cachedNumbersMap.remove(number);
            }
        }
        String str = LOG_TAG;
        Log.i(str, "processRemovedContact: Done. " + cachedNumbersMap.size() + " numbers removed.");
        this.mRemovedNumbers.addAll(cachedNumbersMap.values());
        if (this.mRemovedNumbers.size() > 0) {
            return true;
        }
        return false;
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    private List<String> getAllNumbersInContactDB() {
        Cursor cur;
        List<String> list = new ArrayList<>();
        String[] projection = {"data1", "data4"};
        try {
            cur = this.mContext.getContentResolver().query(getRemoteUriwithUserId(ContactsContract.CommonDataKinds.Phone.CONTENT_URI), projection, (String) null, (String[]) null, (String) null);
            if (cur == null) {
                Log.e(LOG_TAG, "getAllNumbersInContactDB: no contact found");
                if (cur != null) {
                    cur.close();
                }
                return null;
            }
            String str = LOG_TAG;
            Log.i(str, "getAllNumbersInContactDB: found " + cur.getCount() + " contacts.");
            int count = 0;
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    count++;
                    if (count % 300 == 0) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                        }
                    }
                    String number = cur.getString(0);
                    String e164number = cur.getString(1);
                    String number2 = checkNumberAndE164(number, e164number);
                    if (number2 != null) {
                        String e164number2 = changeE164ByNumber(number2, e164number);
                        if (e164number2 != null) {
                            list.add(e164number2);
                        }
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
            return list;
        } catch (RuntimeException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "getAllNumbersInContactDB: Exception " + e2.getMessage());
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    private class ContactObserver extends ContentObserver {
        ContactObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.i(ContactCache.LOG_TAG, "===== Contact updated. =====");
            if (ContactCache.this.mCountryCode == null) {
                Log.i(ContactCache.LOG_TAG, "No SIM available. bail.");
            } else {
                ContactCache.this.sendMessageContactSync();
            }
        }
    }

    private boolean isAllowedContactSync() {
        if (RcsUtils.DualRcs.isDualRcsSettings()) {
            for (int i = 0; i < 2; i++) {
                if (isAllowedContactSync(i)) {
                    return true;
                }
            }
            return false;
        } else if (isAllowedContactSync(SimUtil.getDefaultPhoneId())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAllowedInitialContactSyncBeforeRegi(int phoneId) {
        return ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.RCS.RCS_INITIAL_CONTACT_SYNC_BEFORE_REGI, true);
    }

    private boolean isAllowedContactSync(int phoneId) {
        boolean isRcsEnabled = RcsUtils.UiUtils.isRcsEnabledinSettings(this.mContext, phoneId);
        boolean isContactSyncInSwitchOff = ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.RCS.CONTACT_SYNC_IN_SWITCH_OFF, true);
        boolean rcsSwitch = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 0, phoneId) == 1;
        if (!isRcsEnabled) {
            IMSLog.s(LOG_TAG, phoneId, "isAllowedContactSync: rcs is off in customer.");
            return false;
        } else if (rcsSwitch || isContactSyncInSwitchOff) {
            IMSLog.s(LOG_TAG, phoneId, "isAllowedContactSync: contact sync is allowed");
            return true;
        } else {
            IMSLog.s(LOG_TAG, phoneId, "isAllowedContactSync: CONTACT_SYNC_IN_SWITCH_OFF is false.");
            return false;
        }
    }

    public boolean getIsBlockedContactChange() {
        return this.mIsBlockedContactChange;
    }

    public void setIsBlockedContactChange(boolean flag) {
        this.mIsBlockedContactChange = flag;
    }

    public boolean getBlockedInitialContactSyncBeforeRegi() {
        return this.mIsBlockedInitialContactSyncBeforeRegi;
    }

    public void sendMessageContactSync() {
        if (!isAllowedContactSync()) {
            Log.i(LOG_TAG, "sendMessageContactSync: block the contact sync.");
            this.mIsBlockedContactChange = true;
        } else {
            this.mIsBlockedContactChange = false;
        }
        if (isAllowedInitialContactSyncBeforeRegi(SimUtil.getDefaultPhoneId()) || this.mLastRefreshTimeInMs != 0) {
            this.mIsBlockedInitialContactSyncBeforeRegi = false;
        } else {
            Log.i(LOG_TAG, "sendMessageContactSync: block the initial contact sync before regi.");
            this.mIsBlockedInitialContactSyncBeforeRegi = true;
        }
        if (!this.mIsBlockedContactChange && !this.mIsBlockedInitialContactSyncBeforeRegi) {
            Log.i(LOG_TAG, "sendMessageContactSync: Try contact sync after 3 sec.");
            this.mContactCacheHandler.removeMessages(0);
            ContactCacheHandler contactCacheHandler = this.mContactCacheHandler;
            contactCacheHandler.sendMessageDelayed(contactCacheHandler.obtainMessage(0), 3000);
        }
    }

    class ContactCacheHandler extends Handler {
        static final int HANDLE_START_CONTACT_SYNC = 0;

        ContactCacheHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Log.i(ContactCache.LOG_TAG, "HANDLE_START_CONTACT_SYNC : ");
                int contactProviderStatus = ContactCache.this.getContactProviderStatus();
                if (contactProviderStatus == 0 || contactProviderStatus == 2) {
                    ContactCache.this.onStartContactSync();
                } else if (contactProviderStatus == 1) {
                    Log.i(ContactCache.LOG_TAG, "ContactProvider is in busy state");
                    IMSLog.c(LogClass.CC_START_SYNC, "N,CP:BUSY");
                    ContactCache.this.sendMessageContactSync();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onStartContactSync() {
        Log.i(LOG_TAG, "onStartContactSync : ");
        if (this.mSyncInProgress.get() || this.mIsThrottle) {
            this.mResyncRequired.set(true);
            String str = LOG_TAG;
            Log.i(str, "onStartContactSync : Sync In Progress. Sync will start later, mIsThrottle = " + this.mIsThrottle);
            return;
        }
        this.mSyncInProgress.set(true);
        startContactSync();
    }

    private void startContactSync(Mno mno) {
        String str = LOG_TAG;
        Log.i(str, "startContactSync: " + mno);
        this.mMno = mno;
        Handler handler = this.mBackgroundHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                public final void run() {
                    ContactCache.this.lambda$startContactSync$2$ContactCache();
                }
            });
        }
    }

    public /* synthetic */ void lambda$startContactSync$2$ContactCache() {
        if (this.mCountryCode == null) {
            if (this.mMno == Mno.DEFAULT) {
                Log.e(LOG_TAG, "startContactSync: operator is unknown. bail");
                this.mSyncInProgress.set(false);
                return;
            }
            this.mCountryCode = this.mMno.getCountryCode();
        }
        Log.i(LOG_TAG, "startContactSync: start caching contacts.");
        boolean isRefreshed = refresh();
        if (this.mContactCurCount == 1000) {
            this.mIsLimiting = true;
            setThrottleContactSync(true);
        } else {
            this.mIsLimiting = false;
            this.mStartIndex = 0;
        }
        this.mSyncInProgress.set(false);
        if (this.mResyncRequired.get()) {
            this.mResyncRequired.set(false);
            sendMessageContactSync();
        } else {
            if (!isRefreshed) {
                Log.i(LOG_TAG, "startContactSync: removed, check removed contacts.");
                isRefreshed = processRemovedContact();
            } else if (this.mIsContactUpdated) {
                this.mIsContactUpdated = false;
                processRemovedContact();
            }
            if (!Debug.isProductShip()) {
                new Thread(new Runnable() {
                    public final void run() {
                        ContactCache.this.lambda$startContactSync$1$ContactCache();
                    }
                }).start();
            }
        }
        if (isRefreshed) {
            Log.i(LOG_TAG, "startContactSync: Done. contact updated.");
            for (ContactEventListener listener : this.mListeners) {
                listener.onChanged();
            }
            return;
        }
        Log.i(LOG_TAG, "startContactSync: Done. contact no found.");
    }

    public /* synthetic */ void lambda$startContactSync$1$ContactCache() {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(LOG_TAG + ": Explicit GC after sync");
        System.gc();
        System.runFinalization();
    }

    private void startContactSync() {
        startContactSync(this.mMno);
    }

    public void setMno(Mno mno) {
        String str = LOG_TAG;
        Log.i(str, "setMno: " + mno);
        this.mMno = mno;
        if (this.mCountryCode == null && mno != Mno.DEFAULT) {
            this.mCountryCode = this.mMno.getCountryCode();
            String str2 = LOG_TAG;
            Log.i(str2, "setMno: mCountryCode = " + this.mCountryCode);
        }
    }

    public void resetRefreshTime() {
        this.mLastRefreshTimeInMs = 0;
        this.mPrevRefreshTimeInMs = 0;
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    public int getContactProviderStatus() {
        Cursor cur;
        int providerStatus = -1;
        try {
            cur = this.mContext.getContentResolver().query(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "provider_status"), new String[]{"status"}, (String) null, (String[]) null, (String) null);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    providerStatus = cur.getInt(0);
                }
            }
            if (cur != null) {
                cur.close();
            }
        } catch (Exception e) {
            String str = LOG_TAG;
            Log.e(str, "getContactProviderStatus: Exception " + e.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        String str2 = LOG_TAG;
        Log.i(str2, "getContactProviderStatus: " + providerStatus);
        return providerStatus;
        throw th;
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(LOG_TAG);
    }
}
