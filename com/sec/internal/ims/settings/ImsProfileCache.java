package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.ims.settings.ImsProfile;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ImsProfileCache {
    private final String TAG = ImsProfileCache.class.getSimpleName();
    private ImsAutoUpdate mAutoUpdate;
    private final Context mContext;
    private boolean mIsMvno;
    private String mMnoName;
    private int mNextId = 1;
    private String mPMnoName;
    private int mPhoneId = -1;
    private final Map<Integer, ImsProfile> mProfileMap = new ArrayMap();

    public ImsProfileCache(Context context, String mnoName, int phoneId) {
        this.mContext = context;
        this.mMnoName = mnoName;
        int delimeterIndex = mnoName.indexOf(Mno.MVNO_DELIMITER);
        if (delimeterIndex != -1) {
            this.mIsMvno = true;
            this.mPMnoName = this.mMnoName.substring(0, delimeterIndex);
        } else {
            this.mIsMvno = false;
            this.mPMnoName = "";
        }
        this.mAutoUpdate = ImsAutoUpdate.getInstance(this.mContext, phoneId);
        this.mPhoneId = phoneId;
    }

    private boolean isVersionUpdated() {
        String pdaVer = SemSystemProperties.get("ro.build.PDA", "");
        String cscVer = SemSystemProperties.get("ril.official_cscver", "");
        String prevVer = ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, "buildinfo", "");
        if (!prevVer.equals(pdaVer + "_" + cscVer)) {
            return true;
        }
        return false;
    }

    private void saveBuildInfo() {
        String pdaVer = SemSystemProperties.get("ro.build.PDA", "");
        String cscVer = SemSystemProperties.get("ril.official_cscver", "");
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, "buildinfo", pdaVer + "_" + cscVer);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00c6, code lost:
        r4 = r10.mProfileMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00c8, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r10.mProfileMap.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d0, code lost:
        if (r10.mIsMvno == false) goto L_0x00eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d6, code lost:
        if (r2.isEmpty() == false) goto L_0x00e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d8, code lost:
        android.util.Log.e(r10.TAG, "load: This mno is MVNO but no profile defined. Use Parent profiles");
        r10.mProfileMap.putAll(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e5, code lost:
        r10.mProfileMap.putAll(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00eb, code lost:
        r10.mProfileMap.putAll(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00f0, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void load(boolean r11) {
        /*
            r10 = this;
            int r0 = r10.mPhoneId
            android.content.Context r1 = r10.mContext
            java.lang.String r2 = "imsprofile"
            r3 = 0
            android.content.SharedPreferences r0 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r0, r1, r2, r3, r3)
            java.util.Map r1 = r0.getAll()
            boolean r2 = r1.isEmpty()
            if (r2 != 0) goto L_0x00f8
            boolean r2 = r10.isVersionUpdated()
            if (r2 != 0) goto L_0x00f8
            com.sec.internal.ims.settings.ImsAutoUpdate r2 = r10.mAutoUpdate
            boolean r2 = r2.isUpdateNeeded()
            if (r2 != 0) goto L_0x00f8
            if (r11 == 0) goto L_0x0027
            goto L_0x00f8
        L_0x0027:
            java.lang.String r2 = "buildinfo"
            r1.remove(r2)
            android.util.ArrayMap r2 = new android.util.ArrayMap
            r2.<init>()
            android.util.ArrayMap r3 = new android.util.ArrayMap
            r3.<init>()
            monitor-enter(r10)
            java.util.Collection r4 = r1.values()     // Catch:{ all -> 0x00f5 }
            java.util.Iterator r4 = r4.iterator()     // Catch:{ all -> 0x00f5 }
        L_0x003f:
            boolean r5 = r4.hasNext()     // Catch:{ all -> 0x00f5 }
            if (r5 == 0) goto L_0x00c5
            java.lang.Object r5 = r4.next()     // Catch:{ all -> 0x00f5 }
            r6 = r5
            java.lang.String r6 = (java.lang.String) r6     // Catch:{ all -> 0x00f5 }
            com.sec.ims.settings.ImsProfile r7 = new com.sec.ims.settings.ImsProfile     // Catch:{ all -> 0x00f5 }
            r7.<init>(r6)     // Catch:{ all -> 0x00f5 }
            java.lang.String r8 = r7.getName()     // Catch:{ all -> 0x00f5 }
            boolean r8 = android.text.TextUtils.isEmpty(r8)     // Catch:{ all -> 0x00f5 }
            if (r8 == 0) goto L_0x006a
            java.lang.String r4 = r10.TAG     // Catch:{ all -> 0x00f5 }
            java.lang.String r8 = "Invalid ImsProfile from sharedpref, reset to default"
            android.util.Log.e(r4, r8)     // Catch:{ all -> 0x00f5 }
            r10.clearAllFromStorage()     // Catch:{ all -> 0x00f5 }
            r10.initFromResource()     // Catch:{ all -> 0x00f5 }
            monitor-exit(r10)     // Catch:{ all -> 0x00f5 }
            return
        L_0x006a:
            int r8 = r10.mNextId     // Catch:{ all -> 0x00f5 }
            int r9 = r7.getId()     // Catch:{ all -> 0x00f5 }
            int r9 = r9 + 1
            int r8 = java.lang.Math.max(r8, r9)     // Catch:{ all -> 0x00f5 }
            r10.mNextId = r8     // Catch:{ all -> 0x00f5 }
            boolean r8 = r10.mIsMvno     // Catch:{ all -> 0x00f5 }
            if (r8 == 0) goto L_0x00ac
            java.lang.String r8 = r7.getMnoName()     // Catch:{ all -> 0x00f5 }
            java.lang.String r9 = r10.mPMnoName     // Catch:{ all -> 0x00f5 }
            boolean r8 = android.text.TextUtils.equals(r8, r9)     // Catch:{ all -> 0x00f5 }
            if (r8 == 0) goto L_0x0094
            int r8 = r7.getId()     // Catch:{ all -> 0x00f5 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00f5 }
            r3.put(r8, r7)     // Catch:{ all -> 0x00f5 }
            goto L_0x00c3
        L_0x0094:
            java.lang.String r8 = r7.getMnoName()     // Catch:{ all -> 0x00f5 }
            java.lang.String r9 = r10.mMnoName     // Catch:{ all -> 0x00f5 }
            boolean r8 = android.text.TextUtils.equals(r8, r9)     // Catch:{ all -> 0x00f5 }
            if (r8 == 0) goto L_0x00c3
            int r8 = r7.getId()     // Catch:{ all -> 0x00f5 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00f5 }
            r2.put(r8, r7)     // Catch:{ all -> 0x00f5 }
            goto L_0x00c3
        L_0x00ac:
            java.lang.String r8 = r7.getMnoName()     // Catch:{ all -> 0x00f5 }
            java.lang.String r9 = r10.mMnoName     // Catch:{ all -> 0x00f5 }
            boolean r8 = android.text.TextUtils.equals(r8, r9)     // Catch:{ all -> 0x00f5 }
            if (r8 == 0) goto L_0x00c3
            int r8 = r7.getId()     // Catch:{ all -> 0x00f5 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00f5 }
            r2.put(r8, r7)     // Catch:{ all -> 0x00f5 }
        L_0x00c3:
            goto L_0x003f
        L_0x00c5:
            monitor-exit(r10)     // Catch:{ all -> 0x00f5 }
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r4 = r10.mProfileMap
            monitor-enter(r4)
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r5 = r10.mProfileMap     // Catch:{ all -> 0x00f2 }
            r5.clear()     // Catch:{ all -> 0x00f2 }
            boolean r5 = r10.mIsMvno     // Catch:{ all -> 0x00f2 }
            if (r5 == 0) goto L_0x00eb
            boolean r5 = r2.isEmpty()     // Catch:{ all -> 0x00f2 }
            if (r5 == 0) goto L_0x00e5
            java.lang.String r5 = r10.TAG     // Catch:{ all -> 0x00f2 }
            java.lang.String r6 = "load: This mno is MVNO but no profile defined. Use Parent profiles"
            android.util.Log.e(r5, r6)     // Catch:{ all -> 0x00f2 }
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r5 = r10.mProfileMap     // Catch:{ all -> 0x00f2 }
            r5.putAll(r3)     // Catch:{ all -> 0x00f2 }
            goto L_0x00f0
        L_0x00e5:
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r5 = r10.mProfileMap     // Catch:{ all -> 0x00f2 }
            r5.putAll(r2)     // Catch:{ all -> 0x00f2 }
            goto L_0x00f0
        L_0x00eb:
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r5 = r10.mProfileMap     // Catch:{ all -> 0x00f2 }
            r5.putAll(r2)     // Catch:{ all -> 0x00f2 }
        L_0x00f0:
            monitor-exit(r4)     // Catch:{ all -> 0x00f2 }
            goto L_0x0105
        L_0x00f2:
            r5 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x00f2 }
            throw r5
        L_0x00f5:
            r4 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x00f5 }
            throw r4
        L_0x00f8:
            java.lang.String r2 = r10.TAG
            java.lang.String r3 = "load: map empty or version update or autoupdate needed or SIM MNO changed."
            android.util.Log.d(r2, r3)
            r10.clearAllFromStorage()
            r10.initFromResource()
        L_0x0105:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsProfileCache.load(boolean):void");
    }

    private static void removeNote(JsonElement elem) {
        try {
            JsonObject jsonObj = elem.getAsJsonObject();
            while (jsonObj.has("note")) {
                jsonObj.remove("note");
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void initFromResource() {
        List<ImsProfile> mergedProfileList = init(false);
        Log.d(this.TAG, "initFromResource : Save to storage");
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, 0, false).edit();
        Map<Integer, ImsProfile> profileMap = new ArrayMap<>();
        Map<Integer, ImsProfile> pProfileMap = new ArrayMap<>();
        for (ImsProfile p : mergedProfileList) {
            editor.putString(String.valueOf(p.getId()), p.toJson());
            if (this.mIsMvno) {
                if (TextUtils.equals(p.getMnoName(), this.mPMnoName)) {
                    pProfileMap.put(Integer.valueOf(p.getId()), p);
                } else if (TextUtils.equals(p.getMnoName(), this.mMnoName)) {
                    profileMap.put(Integer.valueOf(p.getId()), p);
                }
            } else if (TextUtils.equals(p.getMnoName(), this.mMnoName)) {
                profileMap.put(Integer.valueOf(p.getId()), p);
            }
        }
        editor.apply();
        Log.d(this.TAG, "initFromResource : Prepare local cache");
        synchronized (this.mProfileMap) {
            this.mProfileMap.clear();
            if (!this.mIsMvno) {
                this.mProfileMap.putAll(profileMap);
            } else if (profileMap.isEmpty()) {
                Log.e(this.TAG, "init: This mno is MVNO but no profile defined. Use Parent profiles");
                this.mProfileMap.putAll(pProfileMap);
            } else {
                this.mProfileMap.putAll(profileMap);
            }
        }
        saveBuildInfo();
    }

    private List<ImsProfile> init(boolean initAll) {
        JsonArray array;
        List<ImsProfile> mergedProfileList = new ArrayList<>();
        Log.d(this.TAG, "init : imsprofile.json");
        JsonParser parser = new JsonParser();
        JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(this.mContext.getResources().openRawResource(R.raw.imsprofile))));
        JsonElement element = parser.parse(reader);
        try {
            reader.close();
        } catch (IOException e) {
            IOException iOException = e;
            Log.e(this.TAG, "init: Close failed. Keep going");
        }
        JsonArray array2 = element.getAsJsonObject().getAsJsonArray("profile");
        if (array2 == null || array2.isJsonNull()) {
            Log.e(this.TAG, "init: parse failed.");
            return mergedProfileList;
        }
        JsonElement updatedArray = this.mAutoUpdate.applyImsProfileUpdate(array2);
        if (updatedArray.isJsonNull() || !updatedArray.isJsonArray()) {
            array = array2;
        } else {
            array = updatedArray.getAsJsonArray();
        }
        List<JsonObject> profileList = new ArrayList<>();
        JsonElement defaultObj = JsonNull.INSTANCE;
        Iterator it = array.iterator();
        JsonElement defaultObj2 = defaultObj;
        while (it.hasNext()) {
            JsonElement elem = (JsonElement) it.next();
            JsonElement obj = elem.getAsJsonObject();
            if (TextUtils.equals(elem.getAsJsonObject().get("name").getAsString(), "default")) {
                defaultObj2 = obj;
            } else if (initAll || obj.get("mnoname").getAsString().startsWith(this.mMnoName.split(":")[0])) {
                profileList.add(obj);
            }
        }
        if (defaultObj2.isJsonNull()) {
            Log.e(this.TAG, "init: No default profile.");
            return mergedProfileList;
        }
        String str = this.TAG;
        Log.d(str, "init: Found " + profileList.size() + " profiles to merge.");
        synchronized (this) {
            this.mNextId = 1;
            for (JsonObject elem2 : profileList) {
                JsonElement merged = JsonUtil.merge(defaultObj2, elem2);
                if (merged.isJsonNull()) {
                    Log.e(this.TAG, "init: merge failed! check json is valid.");
                } else {
                    removeNote(merged);
                    ImsProfile profile = new ImsProfile(merged.toString());
                    int i = this.mNextId;
                    this.mNextId = i + 1;
                    profile.setId(i);
                    mergedProfileList.add(profile);
                }
            }
            String str2 = this.TAG;
            Log.d(str2, "init: merge completed. " + (this.mNextId - 1) + " profiles initiated.");
        }
        return mergedProfileList;
    }

    public void resetToDefault() {
        clearAllFromStorage();
        load(false);
    }

    public List<ImsProfile> getProfileListByMnoName(String mnoName) {
        return getProfileListByMnoName(mnoName, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: java.util.ArrayList} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<com.sec.ims.settings.ImsProfile> getProfileListByMnoName(java.lang.String r11, boolean r12) {
        /*
            r10 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            boolean r1 = android.text.TextUtils.isEmpty(r11)
            if (r1 != 0) goto L_0x014d
            java.lang.String r1 = r10.mMnoName
            boolean r1 = android.text.TextUtils.equals(r1, r11)
            if (r1 == 0) goto L_0x005b
            java.lang.String r1 = r10.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getProfileList by loaded mno - "
            r2.append(r3)
            r2.append(r11)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r1 = r10.mProfileMap
            monitor-enter(r1)
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r2 = r10.mProfileMap     // Catch:{ all -> 0x0058 }
            java.util.Collection r2 = r2.values()     // Catch:{ all -> 0x0058 }
            java.util.Iterator r2 = r2.iterator()     // Catch:{ all -> 0x0058 }
        L_0x0036:
            boolean r3 = r2.hasNext()     // Catch:{ all -> 0x0058 }
            if (r3 == 0) goto L_0x0055
            java.lang.Object r3 = r2.next()     // Catch:{ all -> 0x0058 }
            com.sec.ims.settings.ImsProfile r3 = (com.sec.ims.settings.ImsProfile) r3     // Catch:{ all -> 0x0058 }
            if (r12 == 0) goto L_0x004c
            java.lang.String r4 = "mmtel"
            boolean r4 = r3.hasService(r4)     // Catch:{ all -> 0x0058 }
            if (r4 == 0) goto L_0x0054
        L_0x004c:
            com.sec.ims.settings.ImsProfile r4 = new com.sec.ims.settings.ImsProfile     // Catch:{ all -> 0x0058 }
            r4.<init>(r3)     // Catch:{ all -> 0x0058 }
            r0.add(r4)     // Catch:{ all -> 0x0058 }
        L_0x0054:
            goto L_0x0036
        L_0x0055:
            monitor-exit(r1)     // Catch:{ all -> 0x0058 }
            goto L_0x011e
        L_0x0058:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0058 }
            throw r2
        L_0x005b:
            r1 = 0
            char r2 = com.sec.internal.constants.Mno.MVNO_DELIMITER
            int r2 = r11.indexOf(r2)
            java.lang.String r3 = ""
            r4 = -1
            if (r2 == r4) goto L_0x006d
            r1 = 1
            r4 = 0
            java.lang.String r3 = r11.substring(r4, r2)
        L_0x006d:
            java.lang.String r4 = r10.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "getProfileList by new mno - "
            r5.append(r6)
            r5.append(r11)
            java.lang.String r6 = ", loaded mno - "
            r5.append(r6)
            java.lang.String r6 = r10.mMnoName
            r5.append(r6)
            java.lang.String r6 = ", isMvno - "
            r5.append(r6)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r4, r5)
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            java.util.Map r5 = r10.getAllProfileFromStorage()
            java.util.Collection r5 = r5.values()
            java.util.Iterator r5 = r5.iterator()
        L_0x00a6:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x010f
            java.lang.Object r6 = r5.next()
            com.sec.ims.settings.ImsProfile r6 = (com.sec.ims.settings.ImsProfile) r6
            java.lang.String r7 = r6.getMnoName()
            boolean r7 = android.text.TextUtils.equals(r7, r11)
            if (r7 == 0) goto L_0x00ce
            if (r12 == 0) goto L_0x00c6
            java.lang.String r7 = "mmtel"
            boolean r7 = r6.hasService(r7)
            if (r7 == 0) goto L_0x00ce
        L_0x00c6:
            com.sec.ims.settings.ImsProfile r7 = new com.sec.ims.settings.ImsProfile
            r7.<init>(r6)
            r0.add(r7)
        L_0x00ce:
            if (r1 == 0) goto L_0x010e
            java.lang.String r7 = r6.getMnoName()
            boolean r7 = android.text.TextUtils.equals(r7, r3)
            if (r7 == 0) goto L_0x010e
            java.lang.String r7 = r10.TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "getProfileList by new mno - "
            r8.append(r9)
            r8.append(r11)
            java.lang.String r9 = ", Parent mno - "
            r8.append(r9)
            java.lang.String r9 = r6.getMnoName()
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r7, r8)
            if (r12 == 0) goto L_0x0106
            java.lang.String r7 = "mmtel"
            boolean r7 = r6.hasService(r7)
            if (r7 == 0) goto L_0x010e
        L_0x0106:
            com.sec.ims.settings.ImsProfile r7 = new com.sec.ims.settings.ImsProfile
            r7.<init>(r6)
            r4.add(r7)
        L_0x010e:
            goto L_0x00a6
        L_0x010f:
            if (r1 == 0) goto L_0x011e
            boolean r5 = r0.isEmpty()
            if (r5 == 0) goto L_0x011e
            java.lang.Object r5 = r4.clone()
            r0 = r5
            java.util.ArrayList r0 = (java.util.ArrayList) r0
        L_0x011e:
            if (r12 == 0) goto L_0x014d
            java.util.Map r1 = r10.getAllProfileFromStorage()
            java.util.Collection r1 = r1.values()
            java.util.Iterator r1 = r1.iterator()
        L_0x012c:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x014d
            java.lang.Object r2 = r1.next()
            com.sec.ims.settings.ImsProfile r2 = (com.sec.ims.settings.ImsProfile) r2
            java.lang.String r3 = r2.getMnoName()
            java.lang.String r4 = "GoogleGC_ALL"
            boolean r3 = android.text.TextUtils.equals(r3, r4)
            if (r3 == 0) goto L_0x014c
            com.sec.ims.settings.ImsProfile r3 = new com.sec.ims.settings.ImsProfile
            r3.<init>(r2)
            r0.add(r3)
        L_0x014c:
            goto L_0x012c
        L_0x014d:
            java.lang.String r1 = r10.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getProfileListByMnoName: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsProfileCache.getProfileListByMnoName(java.lang.String, boolean):java.util.List");
    }

    public List<ImsProfile> getProfileListByMdmnType(String mdmnType) {
        ArrayList<ImsProfile> result = new ArrayList<>();
        synchronized (this.mProfileMap) {
            for (ImsProfile p : this.mProfileMap.values()) {
                if (TextUtils.equals(p.getMdmnType(), mdmnType)) {
                    result.add(new ImsProfile(p));
                }
            }
        }
        if (result.isEmpty()) {
            Log.d(this.TAG, "not found from loaded profile by mdmn type");
            for (ImsProfile p2 : getAllProfileFromStorage().values()) {
                if (TextUtils.equals(p2.getMdmnType(), mdmnType)) {
                    result.add(new ImsProfile(p2));
                }
            }
        }
        String str = this.TAG;
        Log.d(str, "getProfileListByMdmnType: " + result);
        return result;
    }

    public List<ImsProfile> getAllProfileList() {
        return new ArrayList(getAllProfileFromStorage().values());
    }

    public ImsProfile getProfile(int id) {
        synchronized (this.mProfileMap) {
            if (!this.mProfileMap.containsKey(Integer.valueOf(id))) {
                return getAllProfileFromStorage().get(Integer.valueOf(id));
            }
            ImsProfile imsProfile = this.mProfileMap.get(Integer.valueOf(id));
            return imsProfile;
        }
    }

    public int insert(ImsProfile profile) {
        synchronized (this) {
            int i = this.mNextId;
            this.mNextId = i + 1;
            profile.setId(i);
        }
        synchronized (this.mProfileMap) {
            this.mProfileMap.put(Integer.valueOf(profile.getId()), profile);
        }
        saveToStorage(profile);
        return profile.getId();
    }

    public int update(int id, ContentValues cv) {
        ImsProfile profile = getProfile(id);
        if (profile == null) {
            Log.e(this.TAG, "update: profile not found.");
            return 0;
        }
        profile.update(cv);
        synchronized (this.mProfileMap) {
            if (this.mProfileMap.containsKey(Integer.valueOf(id))) {
                this.mProfileMap.put(Integer.valueOf(profile.getId()), profile);
            }
        }
        saveToStorage(profile);
        return 1;
    }

    public void remove(int id) {
        synchronized (this.mProfileMap) {
            this.mProfileMap.remove(Integer.valueOf(id));
        }
        removeFromStorage(id);
    }

    private void saveToStorage(ImsProfile profile) {
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, String.valueOf(profile.getId()), profile.toJson());
    }

    private void removeFromStorage(int id) {
        ImsSharedPrefHelper.remove(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, String.valueOf(id));
    }

    private void clearAllFromStorage() {
        ImsSharedPrefHelper.clear(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE);
    }

    private Map<Integer, ImsProfile> getAllProfileFromStorage() {
        synchronized (this.mProfileMap) {
            if (this.mProfileMap.isEmpty()) {
                load(false);
            }
        }
        Map<Integer, ImsProfile> profileMap = new ArrayMap<>();
        for (ImsProfile p : init(true)) {
            profileMap.put(Integer.valueOf(p.getId()), p);
        }
        return profileMap;
    }

    public void dump() {
        synchronized (this.mProfileMap) {
            IMSLog.dump(this.TAG, "Dump of ImsProfileCache:");
            IMSLog.increaseIndent(this.TAG);
            this.mProfileMap.values().forEach(new Consumer() {
                public final void accept(Object obj) {
                    ImsProfileCache.this.lambda$dump$0$ImsProfileCache((ImsProfile) obj);
                }
            });
            IMSLog.decreaseIndent(this.TAG);
        }
    }

    public /* synthetic */ void lambda$dump$0$ImsProfileCache(ImsProfile profile) {
        IMSLog.dump(this.TAG, profile.toString());
    }

    public boolean updateMno(ContentValues mnoInfo) {
        String pMnoName;
        boolean isMvno;
        if (mnoInfo == null) {
            return false;
        }
        String newMnoName = mnoInfo.getAsString("mnoname");
        String newMvnoName = mnoInfo.getAsString(ISimManager.KEY_MVNO_NAME);
        if (!TextUtils.isEmpty(newMvnoName)) {
            isMvno = true;
            pMnoName = newMnoName;
            newMnoName = newMnoName + ":" + newMvnoName;
        } else {
            isMvno = false;
            pMnoName = "";
        }
        if (newMnoName == null || TextUtils.equals(newMnoName, this.mMnoName)) {
            return false;
        }
        Log.d(this.TAG, "updateMno: Mno Changed from " + this.mMnoName + " to " + newMnoName);
        this.mIsMvno = isMvno;
        this.mPMnoName = pMnoName;
        this.mMnoName = newMnoName;
        if (isMvno) {
            Log.d(this.TAG, "updateMno: This mno is MVNO, Parent Mno is " + this.mPMnoName);
        }
        load(true);
        return true;
    }
}
