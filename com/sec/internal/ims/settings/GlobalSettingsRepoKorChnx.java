package com.sec.internal.ims.settings;

import android.content.Context;
import android.os.SemSystemProperties;

public class GlobalSettingsRepoKorChnx extends GlobalSettingsRepoBase {
    private final String LOG_TAG = GlobalSettingsRepoKorChnx.class.getSimpleName();
    private boolean mIsCTCImsMpsEnabled = false;

    public GlobalSettingsRepoKorChnx(Context context, int phoneId) {
        super(context, phoneId);
    }

    /* JADX INFO: finally extract failed */
    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:698)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:698)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public boolean updateMno(android.content.ContentValues r34) {
        /*
            r33 = this;
            r8 = r33
            r9 = r34
            java.lang.Object r1 = r8.mLock
            monitor-enter(r1)
            java.lang.String r0 = "hassim"
            java.lang.Boolean r0 = r9.getAsBoolean(r0)     // Catch:{ all -> 0x04a8 }
            r10 = 0
            if (r0 != 0) goto L_0x0017
            java.lang.Boolean r2 = java.lang.Boolean.valueOf(r10)     // Catch:{ all -> 0x04a8 }
            r0 = r2
            r11 = r0
            goto L_0x0018
        L_0x0017:
            r11 = r0
        L_0x0018:
            java.lang.String r0 = "mnoname"
            java.lang.String r0 = r9.getAsString(r0)     // Catch:{ all -> 0x04a8 }
            r12 = r0
            java.lang.String r0 = "imsSwitchType"
            java.lang.Integer r0 = r9.getAsInteger(r0)     // Catch:{ all -> 0x04a8 }
            if (r0 != 0) goto L_0x002e
            java.lang.Integer r2 = java.lang.Integer.valueOf(r10)     // Catch:{ all -> 0x04a8 }
            r0 = r2
            r13 = r0
            goto L_0x002f
        L_0x002e:
            r13 = r0
        L_0x002f:
            java.lang.String r0 = "imsi"
            java.lang.String r0 = r9.getAsString(r0)     // Catch:{ all -> 0x04a8 }
            r14 = r0
            monitor-exit(r1)     // Catch:{ all -> 0x04a8 }
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simSlot["
            r1.append(r2)
            int r2 = r8.mPhoneId
            r1.append(r2)
            java.lang.String r2 = "] updateMno: hasSIM:"
            r1.append(r2)
            r1.append(r11)
            java.lang.String r2 = ", cscImsSettings:"
            r1.append(r2)
            r1.append(r13)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r33.logMnoInfo(r34)
            boolean r15 = r33.getPrevGcEnabled()
            java.lang.String r0 = "globalgcenabled"
            boolean r7 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r9, r0, r10)
            if (r15 == r7) goto L_0x0071
            r1 = 1
            goto L_0x0072
        L_0x0071:
            r1 = r10
        L_0x0072:
            r6 = r1
            r8.setIsGcEnabledChange(r6)
            java.lang.String r1 = r8.LOG_TAG
            int r2 = r8.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "updateMno: prevGcEnabled: "
            r3.append(r4)
            r3.append(r15)
            java.lang.String r4 = ", newGcEnabled: "
            r3.append(r4)
            r3.append(r7)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r1, r2, r3)
            if (r6 == 0) goto L_0x00b0
            r2 = 0
            r3 = -1
            r4 = 0
            r5 = -1
            r16 = 1
            int r17 = r33.readRcsDefaultEnabled()
            r1 = r33
            r18 = r6
            r6 = r16
            r10 = r7
            r7 = r17
            r1.setSettingsFromSp(r2, r3, r4, r5, r6, r7)
            goto L_0x00b3
        L_0x00b0:
            r18 = r6
            r10 = r7
        L_0x00b3:
            r8.setPrevGcEnabled(r10)
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.fromName(r12)
            int r1 = r8.mPhoneId
            android.content.Context r2 = r8.mContext
            java.lang.String r3 = "globalsettings"
            r4 = 0
            android.content.SharedPreferences r6 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r1, r2, r3, r4, r4)
            android.content.Context r1 = r8.mContext
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.RCS_USER_SETTING1
            java.lang.String r2 = r2.getName()
            int r3 = r8.mPhoneId
            int r5 = com.sec.internal.helper.DmConfigHelper.getImsUserSetting(r1, r2, r3)
            java.lang.String r4 = r8.getPreviousImsi(r6)
            boolean r1 = android.text.TextUtils.isEmpty(r14)
            if (r1 != 0) goto L_0x00e5
            boolean r1 = android.text.TextUtils.equals(r14, r4)
            if (r1 != 0) goto L_0x00e5
            r1 = 1
            goto L_0x00e6
        L_0x00e5:
            r1 = 0
        L_0x00e6:
            r17 = r1
            r8.mMnoinfo = r9
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING
            android.content.Context r2 = r8.mContext
            int r3 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING_UNKNOWN
            int r1 = r1.get(r2, r3)
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.SKT
            if (r7 == r2) goto L_0x00fc
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.KT
            if (r7 != r2) goto L_0x0133
        L_0x00fc:
            int r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING_UNKNOWN
            if (r1 != r2) goto L_0x0133
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING
            android.content.Context r3 = r8.mContext
            int r0 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING_ENABLED
            r2.set(r3, r0)
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "simSlot["
            r2.append(r3)
            int r3 = r8.mPhoneId
            r2.append(r3)
            java.lang.String r3 = " updateMno: roamingHDVoiceOn has no value. set default value as 1 in the first place"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r0.logAndAdd(r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r0 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING
            android.content.Context r2 = r8.mContext
            int r3 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING_UNKNOWN
            int r1 = r0.get(r2, r3)
            r3 = r1
            goto L_0x0134
        L_0x0133:
            r3 = r1
        L_0x0134:
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simSlot["
            r1.append(r2)
            int r2 = r8.mPhoneId
            r1.append(r2)
            java.lang.String r2 = " updateMno: roamingHDVoiceOn ["
            r1.append(r2)
            r1.append(r3)
            java.lang.String r2 = "]"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            android.content.Context r0 = r8.mContext
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_SLOT1
            java.lang.String r1 = r1.getName()
            int r2 = r8.mPhoneId
            int r0 = com.sec.internal.helper.DmConfigHelper.getImsUserSetting(r0, r1, r2)
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CTC
            if (r7 == r1) goto L_0x019c
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CTCMO
            if (r7 != r1) goto L_0x0173
            r19 = r3
            goto L_0x019e
        L_0x0173:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CU
            if (r7 == r1) goto L_0x017f
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CMCC
            if (r7 != r1) goto L_0x017c
            goto L_0x017f
        L_0x017c:
            r19 = r3
            goto L_0x01c1
        L_0x017f:
            boolean r1 = isSupport5GConcept()
            if (r1 == 0) goto L_0x0199
            boolean r1 = com.sec.internal.helper.OmcCode.isMainlandChinaOmcCode()
            if (r1 == 0) goto L_0x0199
            if (r0 == 0) goto L_0x0199
            android.content.Context r1 = r8.mContext
            int r2 = r8.mPhoneId
            r19 = r3
            r3 = 0
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setVoiceCallType(r1, r3, r2)
            r0 = 0
            goto L_0x01c1
        L_0x0199:
            r19 = r3
            goto L_0x01c1
        L_0x019c:
            r19 = r3
        L_0x019e:
            boolean r1 = isSupport5GConcept()
            if (r1 != 0) goto L_0x01b0
            java.lang.String r1 = "ro.product.first_api_level"
            r2 = 0
            int r1 = android.os.SemSystemProperties.getInt(r1, r2)
            r2 = 29
            if (r1 < r2) goto L_0x01c1
        L_0x01b0:
            boolean r1 = com.sec.internal.helper.OmcCode.isChinaOmcCode()
            if (r1 == 0) goto L_0x01c1
            if (r0 == 0) goto L_0x01c1
            android.content.Context r1 = r8.mContext
            int r2 = r8.mPhoneId
            r3 = 0
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setVoiceCallType(r1, r3, r2)
            r0 = 0
        L_0x01c1:
            boolean r1 = r7.isHkMo()
            if (r1 != 0) goto L_0x01cb
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CTCMO
            if (r7 != r1) goto L_0x01cc
        L_0x01cb:
            r0 = 0
        L_0x01cc:
            int r3 = r33.readRcsDefaultEnabled()
            boolean r1 = r33.updateRequires(r34)
            if (r1 != 0) goto L_0x023f
            com.sec.internal.helper.SimpleEventLog r1 = r8.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r20 = r0
            java.lang.String r0 = "simSlot["
            r2.append(r0)
            int r0 = r8.mPhoneId
            r2.append(r0)
            java.lang.String r0 = "] updateMno: update not requires"
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r1.logAndAdd(r0)
            r8.initRcsUserSetting(r5, r3)
            if (r17 == 0) goto L_0x023b
            android.content.SharedPreferences$Editor r0 = r6.edit()
            java.lang.String r1 = "imsi"
            r0.putString(r1, r14)
            com.sec.internal.helper.SimpleEventLog r1 = r8.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r21 = r3
            java.lang.String r3 = "simSlot["
            r2.append(r3)
            int r3 = r8.mPhoneId
            r2.append(r3)
            java.lang.String r3 = "] imsi changed:"
            r2.append(r3)
            java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r4)
            r2.append(r3)
            java.lang.String r3 = " --> "
            r2.append(r3)
            java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r14)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            r0.apply()
            goto L_0x023d
        L_0x023b:
            r21 = r3
        L_0x023d:
            r0 = 0
            return r0
        L_0x023f:
            r20 = r0
            r21 = r3
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simSlot["
            r1.append(r2)
            int r2 = r8.mPhoneId
            r1.append(r2)
            java.lang.String r2 = "] updateMno: update requires"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            java.lang.String r3 = r8.getPreviousMno(r6)
            java.lang.String r0 = "globalgcenabled"
            r1 = 0
            boolean r2 = r6.getBoolean(r0, r1)
            r33.reset()
            android.content.SharedPreferences$Editor r1 = r6.edit()
            java.lang.String r0 = "globalgcenabled"
            r1.putBoolean(r0, r2)
            r1.apply()
            java.lang.String r0 = r8.LOG_TAG
            r22 = r1
            int r1 = r8.mPhoneId
            r23 = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r24 = r4
            java.lang.String r4 = "updateMno: ["
            r2.append(r4)
            r2.append(r3)
            java.lang.String r4 = "] => ["
            r2.append(r4)
            r2.append(r12)
            java.lang.String r4 = "]"
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.d(r0, r1, r2)
            boolean r0 = r7.isKor()
            if (r0 == 0) goto L_0x02b6
            boolean r0 = android.text.TextUtils.equals(r12, r3)
            if (r0 != 0) goto L_0x02b6
            r0 = -1
            r4 = r0
            goto L_0x02b8
        L_0x02b6:
            r4 = r20
        L_0x02b8:
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simSlot["
            r1.append(r2)
            int r2 = r8.mPhoneId
            r1.append(r2)
            java.lang.String r2 = "] voicecall_type_"
            r1.append(r2)
            java.lang.String r2 = r7.getName()
            r1.append(r2)
            java.lang.String r2 = ": ["
            r1.append(r2)
            r1.append(r4)
            java.lang.String r2 = "]"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r0 = 0
            r1 = 0
            r2 = -1
            if (r4 == r2) goto L_0x02f3
            r0 = 1
            r2 = r0
            goto L_0x0384
        L_0x02f3:
            int r2 = r8.mPhoneId
            boolean r2 = r8.needResetCallSettingBySim(r2)
            if (r2 == 0) goto L_0x0321
            com.sec.internal.helper.SimpleEventLog r2 = r8.mEventLog
            r25 = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r26 = r1
            java.lang.String r1 = "simSlot["
            r0.append(r1)
            int r1 = r8.mPhoneId
            r0.append(r1)
            java.lang.String r1 = "] reset voice and vt call settings db by simcard change"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r2.logAndAdd(r0)
            r0 = 1
            r1 = 1
            r2 = r0
            goto L_0x0384
        L_0x0321:
            r25 = r0
            r26 = r1
            boolean r0 = android.text.TextUtils.equals(r12, r3)
            if (r0 != 0) goto L_0x0380
            boolean r0 = android.text.TextUtils.isEmpty(r3)
            if (r0 != 0) goto L_0x0380
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simSlot["
            r1.append(r2)
            int r2 = r8.mPhoneId
            r1.append(r2)
            java.lang.String r2 = "] reset voice and video call settings db by simcard change"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r0 = 1
            r1 = 1
            boolean r2 = r8.getPreviousGcfMode(r6)
            r25 = r0
            boolean r0 = com.sec.internal.helper.os.DeviceUtil.getGcfMode()
            if (r2 == r0) goto L_0x037e
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r26 = r1
            java.lang.String r1 = "simSlot["
            r2.append(r1)
            int r1 = r8.mPhoneId
            r2.append(r1)
            java.lang.String r1 = "] reset voice and video call settings db by GCF ON"
            r2.append(r1)
            java.lang.String r1 = r2.toString()
            r0.logAndAdd(r1)
            goto L_0x0380
        L_0x037e:
            r26 = r1
        L_0x0380:
            r2 = r25
            r1 = r26
        L_0x0384:
            android.content.Context r0 = r8.mContext
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r25 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VILTE_SLOT1
            r26 = r1
            java.lang.String r1 = r25.getName()
            r25 = r3
            int r3 = r8.mPhoneId
            int r3 = com.sec.internal.helper.DmConfigHelper.getImsUserSetting(r0, r1, r3)
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r27 = r5
            java.lang.String r5 = "simSlot["
            r1.append(r5)
            int r5 = r8.mPhoneId
            r1.append(r5)
            java.lang.String r5 = "] videocall_type_"
            r1.append(r5)
            java.lang.String r5 = r7.getName()
            r1.append(r5)
            java.lang.String r5 = ": ["
            r1.append(r5)
            r1.append(r3)
            java.lang.String r5 = "]"
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            boolean r0 = com.sec.internal.helper.os.DeviceUtil.getGcfMode()
            if (r0 != 0) goto L_0x03f7
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r5 = "simSlot["
            r1.append(r5)
            int r5 = r8.mPhoneId
            r1.append(r5)
            java.lang.String r5 = "] NOT Temporal SIM swapped: Set Video DB - "
            r1.append(r5)
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r0 = -1
            if (r3 == r0) goto L_0x03f7
            r1 = 1
            r5 = r1
            goto L_0x03f9
        L_0x03f7:
            r5 = r26
        L_0x03f9:
            com.sec.internal.helper.SimpleEventLog r0 = r8.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r20 = r3
            java.lang.String r3 = "isNeedToSetVoLTE : "
            r1.append(r3)
            r1.append(r2)
            java.lang.String r3 = "isNeedToSetViLTE : "
            r1.append(r3)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r0 = 0
            r26 = -1
            r1 = r33
            r28 = r2
            r29 = r21
            r21 = r20
            r20 = r25
            r3 = r4
            r25 = r4
            r4 = r5
            r30 = r27
            r27 = r5
            r5 = r21
            r31 = r6
            r6 = r0
            r32 = r7
            r7 = r26
            r1.setSettingsFromSp(r2, r3, r4, r5, r6, r7)
            java.lang.Object r2 = r8.mLock
            monitor-enter(r2)
            boolean r0 = r11.booleanValue()     // Catch:{ all -> 0x049f }
            int r1 = r13.intValue()     // Catch:{ all -> 0x049f }
            r8.loadGlobalSettingsFromJson(r0, r12, r1, r9)     // Catch:{ all -> 0x049f }
            monitor-exit(r2)     // Catch:{ all -> 0x049f }
            int r0 = r33.readRcsDefaultEnabled()
            boolean r1 = r8.mVersionUpdated
            if (r1 == 0) goto L_0x0496
            com.sec.internal.helper.SimpleEventLog r1 = r8.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "updateMno : rcs_default_enabled: ["
            r2.append(r3)
            r7 = r29
            r2.append(r7)
            java.lang.String r3 = "] => ["
            r2.append(r3)
            r2.append(r0)
            java.lang.String r3 = "]"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            if (r7 == r0) goto L_0x048e
            com.sec.internal.helper.SimpleEventLog r1 = r8.mEventLog
            java.lang.String r2 = "Reset rcs_user_setting because rcs_default_enabled is changed about same SIM"
            r1.logAndAdd(r2)
            r2 = 0
            r3 = -1
            r4 = 0
            r5 = -1
            r6 = 1
            r1 = r33
            r26 = r7
            r7 = r0
            r1.setSettingsFromSp(r2, r3, r4, r5, r6, r7)
            goto L_0x0490
        L_0x048e:
            r26 = r7
        L_0x0490:
            r1 = 0
            r8.mVersionUpdated = r1
            r3 = r30
            goto L_0x049d
        L_0x0496:
            r26 = r29
            r3 = r30
            r8.initRcsUserSetting(r3, r0)
        L_0x049d:
            r1 = 1
            return r1
        L_0x049f:
            r0 = move-exception
            r26 = r29
            r3 = r30
        L_0x04a4:
            monitor-exit(r2)     // Catch:{ all -> 0x04a6 }
            throw r0
        L_0x04a6:
            r0 = move-exception
            goto L_0x04a4
        L_0x04a8:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x04a8 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.GlobalSettingsRepoKorChnx.updateMno(android.content.ContentValues):boolean");
    }

    private static boolean isSupport5GConcept() {
        try {
            if (Integer.parseInt(SemSystemProperties.get("ro.telephony.default_network", "0,0").trim().split(",")[0]) >= 23) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
