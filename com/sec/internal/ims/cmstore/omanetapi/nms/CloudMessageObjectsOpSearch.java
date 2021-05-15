package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.data.SortOrderEnum;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.TMOVariables;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.nms.ObjectsOpSearch;
import com.sec.internal.omanetapi.nms.data.Reference;
import com.sec.internal.omanetapi.nms.data.SearchCriteria;
import com.sec.internal.omanetapi.nms.data.SearchCriterion;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;
import com.sec.internal.omanetapi.nms.data.SortCriteria;
import com.sec.internal.omanetapi.nms.data.SortCriterion;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

public class CloudMessageObjectsOpSearch extends ObjectsOpSearch {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageObjectsOpSearch.class.getSimpleName();
    private static final long serialVersionUID = 513693735609008639L;
    private final String JSON_CURSOR_TAG = "cursor";
    private final String JSON_OBJECT_LIST_TAG = "objectList";
    private final SimpleDateFormat mFormatOfName;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    private final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CloudMessageObjectsOpSearch(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r18, java.lang.String r19, java.lang.String r20, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r21, boolean r22, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r23) {
        /*
            r17 = this;
            r8 = r17
            r9 = r20
            r10 = r23
            java.lang.String r0 = r23.getNmsHost()
            java.lang.String r1 = r23.getOMAApiVersion()
            java.lang.String r2 = r23.getStoreName()
            r8.<init>(r0, r1, r2, r9)
            java.lang.String r0 = "objectList"
            r8.JSON_OBJECT_LIST_TAG = r0
            java.lang.String r0 = "cursor"
            r8.JSON_CURSOR_TAG = r0
            java.text.SimpleDateFormat r0 = new java.text.SimpleDateFormat
            java.util.Locale r1 = java.util.Locale.getDefault()
            java.lang.String r2 = "yyyy-MM-dd'T'HH:mm:ss'Z'"
            r0.<init>(r2, r1)
            r8.mFormatOfName = r0
            java.lang.String r1 = "UTC"
            java.util.TimeZone r1 = java.util.TimeZone.getTimeZone(r1)
            r0.setTimeZone(r1)
            r11 = r18
            r8.mIAPICallFlowListener = r11
            r8.mICloudMessageManagerHelper = r10
            r3 = r17
            com.sec.internal.omanetapi.nms.data.SelectionCriteria r0 = new com.sec.internal.omanetapi.nms.data.SelectionCriteria
            r0.<init>()
            r12 = r0
            r13 = r21
            r14 = r22
            r8.constructSearchParam(r9, r13, r12, r14)
            boolean r0 = android.text.TextUtils.isEmpty(r19)
            if (r0 != 0) goto L_0x0054
            r15 = r19
            r12.fromCursor = r15
            goto L_0x0056
        L_0x0054:
            r15 = r19
        L_0x0056:
            java.lang.String r7 = r10.getValidTokenByLine(r9)
            java.lang.String r0 = r23.getContentType()
            r8.initCommonRequestHeaders(r0, r7)
            r0 = 1
            r8.initPostRequest(r12, r0)
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch$1 r6 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch$1
            r0 = r6
            r1 = r17
            r2 = r18
            r4 = r20
            r5 = r21
            r9 = r6
            r6 = r19
            r16 = r7
            r7 = r23
            r0.<init>(r2, r3, r4, r5, r6, r7)
            r8.setCallback(r9)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.<init>(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener, java.lang.String, java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType, boolean, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper):void");
    }

    private void constructSearchParam(String currentLine, SyncMsgType type, SelectionCriteria selectionCriteria, boolean needPresetSearchRemove) {
        String date;
        SyncMsgType syncMsgType = type;
        SelectionCriteria selectionCriteria2 = selectionCriteria;
        selectionCriteria2.maxEntries = this.mICloudMessageManagerHelper.getMaxSearchEntry();
        Reference searchScope = null;
        if (!SyncMsgType.DEFAULT.equals(syncMsgType)) {
            String date2 = "";
            String str = TAG;
            Log.i(str, "type: " + syncMsgType);
            if (!SyncMsgType.DEFAULT.equals(syncMsgType)) {
                String folderId = "";
                if (SyncMsgType.MESSAGE.equals(syncMsgType)) {
                    folderId = TMOVariables.TmoMessageFolderId.mRCSMessageStore;
                    date = this.mFormatOfName.format(Long.valueOf(System.currentTimeMillis() - TMOVariables.TmoMessageSyncPeriod.MESSAGE));
                } else if (SyncMsgType.VM.equals(syncMsgType)) {
                    folderId = TMOVariables.TmoMessageFolderId.mVVMailInbox;
                    date = this.mFormatOfName.format(Long.valueOf(System.currentTimeMillis() - TMOVariables.TmoMessageSyncPeriod.VVM));
                } else if (SyncMsgType.FAX.equals(syncMsgType)) {
                    folderId = TMOVariables.TmoMessageFolderId.mMediaFax;
                    long period = TMOVariables.TmoMessageSyncPeriod.FAX;
                    date = period == 0 ? this.mFormatOfName.format(0) : this.mFormatOfName.format(Long.valueOf(System.currentTimeMillis() - period));
                } else if (SyncMsgType.CALLLOG.equals(syncMsgType)) {
                    folderId = TMOVariables.TmoMessageFolderId.mCallHistory;
                    date = this.mFormatOfName.format(Long.valueOf(System.currentTimeMillis() - TMOVariables.TmoMessageSyncPeriod.CALL_LOG));
                } else if (SyncMsgType.VM_GREETINGS.equals(syncMsgType)) {
                    folderId = TMOVariables.TmoMessageFolderId.mVVMailGreeting;
                    long period2 = TMOVariables.TmoMessageSyncPeriod.GREETING;
                    date = period2 == 0 ? this.mFormatOfName.format(0) : this.mFormatOfName.format(Long.valueOf(System.currentTimeMillis() - period2));
                } else {
                    date = date2;
                }
                String protocol = this.mICloudMessageManagerHelper.getProtocol();
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(protocol).encodedAuthority(this.mICloudMessageManagerHelper.getNmsHost()).appendPath("nms").appendPath(this.mICloudMessageManagerHelper.getOMAApiVersion()).appendPath(this.mICloudMessageManagerHelper.getStoreName()).appendPath(currentLine).appendPath("folders").appendPath(folderId);
                try {
                    searchScope = new Reference();
                    searchScope.resourceURL = new URL(builder.build().toString());
                    date2 = date;
                } catch (MalformedURLException e) {
                    String str2 = TAG;
                    Log.e(str2, e.getMessage() + "");
                    searchScope.resourceURL = null;
                    date2 = date;
                }
            } else {
                String str3 = currentLine;
            }
            SearchCriteria searchCriteria = new SearchCriteria();
            SearchCriterion[] searchCriterion = {new SearchCriterion()};
            searchCriterion[0].type = "Date";
            SearchCriterion searchCriterion2 = searchCriterion[0];
            searchCriterion2.value = "minDate=" + date2;
            searchCriteria.criterion = searchCriterion;
            SortCriteria sortCriteria = new SortCriteria();
            SortCriterion[] sortCriterion = {new SortCriterion()};
            sortCriterion[0].type = "Date";
            sortCriterion[0].order = SortOrderEnum.Date;
            sortCriteria.criterion = sortCriterion;
            selectionCriteria2.searchScope = searchScope;
            selectionCriteria2.sortCriteria = sortCriteria;
            selectionCriteria2.searchCriteria = searchCriteria;
        } else if (ATTGlobalVariables.isGcmReplacePolling() && !needPresetSearchRemove) {
            SearchCriteria searchCriteria2 = new SearchCriteria();
            SearchCriterion[] searchCriterion3 = {new SearchCriterion()};
            searchCriterion3[0].type = "PresetSearch";
            searchCriterion3[0].name = "UPOneDotO";
            searchCriterion3[0].value = "";
            searchCriteria2.criterion = searchCriterion3;
            selectionCriteria2.searchCriteria = searchCriteria2;
        }
    }
}
