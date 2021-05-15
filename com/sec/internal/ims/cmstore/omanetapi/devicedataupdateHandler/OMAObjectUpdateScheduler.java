package com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.cmstore.data.OperationEnum;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkUpdate;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteObjectFlag;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.BulkUpdate;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class OMAObjectUpdateScheduler extends BaseDeviceDataUpdateHandler {
    public static final String TAG = OMAObjectUpdateScheduler.class.getSimpleName();
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public OMAObjectUpdateScheduler(Looper looper, Context context, INetAPIEventListener APIEventListener, String line, SyncMsgType type, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper, context, APIEventListener, line, type, iCloudMessageManagerHelper);
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParam param) {
        String str = TAG;
        Log.i(str, "setWorkingQueue param: " + param);
        if (param != null) {
            Pair<String, String> pair = this.mBufferDBTranslation.getObjectIdFlagNamePairFromBufDb(param);
            if (!TextUtils.isEmpty((CharSequence) pair.first) && !TextUtils.isEmpty((CharSequence) pair.second)) {
                String str2 = TAG;
                Log.i(str2, "setWorkingQueue " + ((String) pair.second));
                if (FlagNames.Seen.equals(pair.second)) {
                    this.mWorkingQueue.offer(new CloudMessagePutObjectFlag(this, (String) pair.first, (String) pair.second, param, this.mICloudMessageManagerHelper));
                } else if (FlagNames.Deleted.equals(pair.second)) {
                    this.mWorkingQueue.offer(new CloudMessageDeleteIndividualObject(this, (String) pair.first, param, this.mICloudMessageManagerHelper));
                } else if (FlagNames.Flagged.equals(pair.second)) {
                    this.mWorkingQueue.offer(new CloudMessageDeleteObjectFlag(this, (String) pair.first, FlagNames.Seen, param, this.mICloudMessageManagerHelper));
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParamList paramlist) {
        List<Reference> bulkReadList;
        BufferDBChangeParamList bufferDBChangeParamList = paramlist;
        if (bufferDBChangeParamList != null && bufferDBChangeParamList.mChangelst != null && bufferDBChangeParamList.mChangelst.size() != 0) {
            String str = TAG;
            Log.i(str, "setWorkingQueue  isBulkUpdateEnabled: " + this.mICloudMessageManagerHelper.isBulkUpdateEnabled() + "mChangelst size: " + bufferDBChangeParamList.mChangelst.size());
            if (bufferDBChangeParamList.mChangelst.size() == 1) {
                setWorkingQueue(bufferDBChangeParamList.mChangelst.get(0));
                return;
            }
            List<Reference> bulkDeleteList = new ArrayList<>();
            List<Reference> bulkReadList2 = new ArrayList<>();
            List<Reference> bulkUnReadList = new ArrayList<>();
            BufferDBChangeParamList bulkDeleteParamList = new BufferDBChangeParamList();
            BufferDBChangeParamList bulkReadParamList = new BufferDBChangeParamList();
            BufferDBChangeParamList bulkUnReadParamList = new BufferDBChangeParamList();
            Iterator<BufferDBChangeParam> it = bufferDBChangeParamList.mChangelst.iterator();
            while (it.hasNext()) {
                BufferDBChangeParam param = it.next();
                if (param != null) {
                    Pair<String, String> pair = this.mBufferDBTranslation.getResourceUrlFlagNamePairFromBufDb(param);
                    if (TextUtils.isEmpty((CharSequence) pair.first) || TextUtils.isEmpty((CharSequence) pair.second)) {
                        bulkReadList = bulkReadList2;
                        BufferDBChangeParam bufferDBChangeParam = param;
                    } else if (!FlagNames.Seen.equals(pair.second)) {
                        Pair<String, String> pair2 = pair;
                        bulkReadList = bulkReadList2;
                        BufferDBChangeParam param2 = param;
                        if (FlagNames.Deleted.equals(pair2.second)) {
                            if (this.mICloudMessageManagerHelper.isBulkDeleteEnabled()) {
                                Reference reference = new Reference();
                                try {
                                    reference.resourceURL = new URL((String) pair2.first);
                                    bulkDeleteList.add(reference);
                                    bulkDeleteParamList.mChangelst.add(param2);
                                } catch (MalformedURLException e) {
                                    String str2 = TAG;
                                    Log.e(str2, e.getMessage() + "");
                                    e.printStackTrace();
                                }
                            } else {
                                String objId = (String) pair2.first;
                                this.mWorkingQueue.offer(new CloudMessageDeleteIndividualObject(this, objId.substring(objId.lastIndexOf(47) + 1), param2, this.mICloudMessageManagerHelper));
                            }
                        } else if (FlagNames.Flagged.equals(pair2.second)) {
                            if (this.mICloudMessageManagerHelper.isBulkUpdateEnabled()) {
                                Reference reference2 = new Reference();
                                try {
                                    reference2.resourceURL = new URL((String) pair2.first);
                                    bulkUnReadList.add(reference2);
                                    bulkUnReadParamList.mChangelst.add(param2);
                                } catch (MalformedURLException e2) {
                                    String str3 = TAG;
                                    Log.e(str3, e2.getMessage() + "");
                                    e2.printStackTrace();
                                }
                            } else {
                                Queue queue = this.mWorkingQueue;
                                CloudMessageDeleteObjectFlag cloudMessageDeleteObjectFlag = r1;
                                CloudMessageDeleteObjectFlag cloudMessageDeleteObjectFlag2 = new CloudMessageDeleteObjectFlag(this, (String) pair2.first, FlagNames.Seen, param2, this.mICloudMessageManagerHelper);
                                queue.offer(cloudMessageDeleteObjectFlag);
                            }
                        }
                    } else if (this.mICloudMessageManagerHelper.isBulkUpdateEnabled()) {
                        Reference reference3 = new Reference();
                        try {
                            reference3.resourceURL = new URL((String) pair.first);
                            bulkReadList2.add(reference3);
                            bulkReadParamList.mChangelst.add(param);
                        } catch (MalformedURLException e3) {
                            String str4 = TAG;
                            Log.e(str4, e3.getMessage() + "");
                            e3.printStackTrace();
                        }
                        bulkReadList = bulkReadList2;
                    } else {
                        String objId2 = (String) pair.first;
                        String objId3 = objId2.substring(objId2.lastIndexOf(47) + 1);
                        Queue queue2 = this.mWorkingQueue;
                        String str5 = objId3;
                        String str6 = objId3;
                        CloudMessagePutObjectFlag cloudMessagePutObjectFlag = r1;
                        Pair<String, String> pair3 = pair;
                        bulkReadList = bulkReadList2;
                        BufferDBChangeParam bufferDBChangeParam2 = param;
                        CloudMessagePutObjectFlag cloudMessagePutObjectFlag2 = new CloudMessagePutObjectFlag(this, str5, (String) pair.second, param, this.mICloudMessageManagerHelper);
                        queue2.offer(cloudMessagePutObjectFlag);
                    }
                    BufferDBChangeParamList bufferDBChangeParamList2 = paramlist;
                    bulkReadList2 = bulkReadList;
                }
            }
            processBulkDelete(bulkDeleteList, bulkDeleteParamList);
            processBulkSetRead(bulkReadList2, bulkReadParamList);
            processBulkSetUnRead(bulkUnReadList, bulkUnReadParamList);
        }
    }

    private void processBulkDelete(List<Reference> referenceList, BufferDBChangeParamList paramList) {
        int maxEntryBulkDelete;
        int i;
        OMAObjectUpdateScheduler oMAObjectUpdateScheduler = this;
        List<Reference> list = referenceList;
        if (list != null && referenceList.size() >= 1 && oMAObjectUpdateScheduler.mICloudMessageManagerHelper.isBulkDeleteEnabled()) {
            int maxEntryBulkDelete2 = oMAObjectUpdateScheduler.mICloudMessageManagerHelper.getMaxBulkDeleteEntry();
            Log.i(TAG, "getMaxBulkDeleteEntry: " + maxEntryBulkDelete2 + " listsize: " + referenceList.size());
            if (maxEntryBulkDelete2 <= 1) {
                maxEntryBulkDelete = 100;
            } else {
                maxEntryBulkDelete = maxEntryBulkDelete2;
            }
            if (referenceList.size() % maxEntryBulkDelete == 0) {
                i = referenceList.size() / maxEntryBulkDelete;
            } else {
                i = (referenceList.size() / maxEntryBulkDelete) + 1;
            }
            int round = i;
            int i2 = 0;
            while (i2 < round) {
                int start = i2 * maxEntryBulkDelete;
                int end = Math.min(referenceList.size(), (i2 + 1) * maxEntryBulkDelete);
                List<Reference> referenceList_split = list.subList(start, end);
                BufferDBChangeParamList paramList_split = new BufferDBChangeParamList();
                paramList_split.mChangelst = new ArrayList<>(paramList.mChangelst.subList(start, end));
                Log.i(TAG, "Start, End: " + start + " " + end + " newlistsize: " + referenceList_split.size());
                BulkDelete bulkdelete = oMAObjectUpdateScheduler.createNewBulkDeleteParam(referenceList_split);
                if (bulkdelete.objects.objectReference == null || bulkdelete.objects.objectReference.length <= 0) {
                } else {
                    Queue queue = oMAObjectUpdateScheduler.mWorkingQueue;
                    String str = oMAObjectUpdateScheduler.mLine;
                    CloudMessageBulkDeletion cloudMessageBulkDeletion = r0;
                    Queue queue2 = queue;
                    BulkDelete bulkDelete = bulkdelete;
                    CloudMessageBulkDeletion cloudMessageBulkDeletion2 = new CloudMessageBulkDeletion(this, bulkdelete, str, oMAObjectUpdateScheduler.mSyncMsgType, paramList_split, oMAObjectUpdateScheduler.mICloudMessageManagerHelper);
                    queue2.offer(cloudMessageBulkDeletion);
                }
                i2++;
                oMAObjectUpdateScheduler = this;
                list = referenceList;
            }
        }
    }

    private void processBulkSetRead(List<Reference> referenceList, BufferDBChangeParamList paramList) {
        int maxEntryBulkRead;
        int i;
        OMAObjectUpdateScheduler oMAObjectUpdateScheduler = this;
        List<Reference> list = referenceList;
        if (list != null && referenceList.size() >= 1 && oMAObjectUpdateScheduler.mICloudMessageManagerHelper.isBulkUpdateEnabled()) {
            int maxEntryBulkRead2 = oMAObjectUpdateScheduler.mICloudMessageManagerHelper.getMaxBulkDeleteEntry();
            Log.i(TAG, "processBulkSetRead: " + maxEntryBulkRead2 + " listsize: " + referenceList.size());
            if (maxEntryBulkRead2 <= 1) {
                maxEntryBulkRead = 100;
            } else {
                maxEntryBulkRead = maxEntryBulkRead2;
            }
            if (referenceList.size() % maxEntryBulkRead == 0) {
                i = referenceList.size() / maxEntryBulkRead;
            } else {
                i = (referenceList.size() / maxEntryBulkRead) + 1;
            }
            int round = i;
            int i2 = 0;
            while (i2 < round) {
                int start = i2 * maxEntryBulkRead;
                int end = Math.min(referenceList.size(), (i2 + 1) * maxEntryBulkRead);
                List<Reference> referenceList_split = list.subList(start, end);
                BufferDBChangeParamList paramList_split = new BufferDBChangeParamList();
                paramList_split.mChangelst = new ArrayList<>(paramList.mChangelst.subList(start, end));
                Log.i(TAG, "Start, End: " + start + " " + end + " newlistsize: " + referenceList_split.size());
                BulkUpdate bulkread = oMAObjectUpdateScheduler.createNewBulkUpdateParam(referenceList_split, new String[]{FlagNames.Seen}, OperationEnum.AddFlag);
                if (bulkread.objects.objectReference == null || bulkread.objects.objectReference.length <= 0) {
                } else {
                    Log.i(TAG, "send bulk update");
                    Queue queue = oMAObjectUpdateScheduler.mWorkingQueue;
                    String str = oMAObjectUpdateScheduler.mLine;
                    CloudMessageBulkUpdate cloudMessageBulkUpdate = r0;
                    Queue queue2 = queue;
                    BulkUpdate bulkUpdate = bulkread;
                    CloudMessageBulkUpdate cloudMessageBulkUpdate2 = new CloudMessageBulkUpdate(this, bulkread, str, oMAObjectUpdateScheduler.mSyncMsgType, paramList_split, oMAObjectUpdateScheduler.mICloudMessageManagerHelper);
                    queue2.offer(cloudMessageBulkUpdate);
                }
                i2++;
                oMAObjectUpdateScheduler = this;
                list = referenceList;
            }
        }
    }

    private void processBulkSetUnRead(List<Reference> referenceList, BufferDBChangeParamList paramList) {
        int maxEntryBulkUnRead;
        int i;
        OMAObjectUpdateScheduler oMAObjectUpdateScheduler = this;
        List<Reference> list = referenceList;
        if (list != null && referenceList.size() >= 1 && oMAObjectUpdateScheduler.mICloudMessageManagerHelper.isBulkUpdateEnabled()) {
            int maxEntryBulkUnRead2 = oMAObjectUpdateScheduler.mICloudMessageManagerHelper.getMaxBulkDeleteEntry();
            Log.i(TAG, "processBulkSetUnRead: " + maxEntryBulkUnRead2 + " listsize: " + referenceList.size());
            if (maxEntryBulkUnRead2 <= 1) {
                maxEntryBulkUnRead = 100;
            } else {
                maxEntryBulkUnRead = maxEntryBulkUnRead2;
            }
            if (referenceList.size() % maxEntryBulkUnRead == 0) {
                i = referenceList.size() / maxEntryBulkUnRead;
            } else {
                i = (referenceList.size() / maxEntryBulkUnRead) + 1;
            }
            int round = i;
            int i2 = 0;
            while (i2 < round) {
                int start = i2 * maxEntryBulkUnRead;
                int end = Math.min(referenceList.size(), (i2 + 1) * maxEntryBulkUnRead);
                List<Reference> referenceList_split = list.subList(start, end);
                BufferDBChangeParamList paramList_split = new BufferDBChangeParamList();
                paramList_split.mChangelst = new ArrayList<>(paramList.mChangelst.subList(start, end));
                Log.i(TAG, "Start, End: " + start + " " + end + " newlistsize: " + referenceList_split.size());
                BulkUpdate bulkunread = oMAObjectUpdateScheduler.createNewBulkUpdateParam(referenceList_split, new String[]{FlagNames.Seen}, OperationEnum.RemoveFlag);
                if (bulkunread.objects.objectReference == null || bulkunread.objects.objectReference.length <= 0) {
                } else {
                    Queue queue = oMAObjectUpdateScheduler.mWorkingQueue;
                    String str = oMAObjectUpdateScheduler.mLine;
                    CloudMessageBulkUpdate cloudMessageBulkUpdate = r0;
                    Queue queue2 = queue;
                    BulkUpdate bulkUpdate = bulkunread;
                    CloudMessageBulkUpdate cloudMessageBulkUpdate2 = new CloudMessageBulkUpdate(this, bulkunread, str, oMAObjectUpdateScheduler.mSyncMsgType, paramList_split, oMAObjectUpdateScheduler.mICloudMessageManagerHelper);
                    queue2.offer(cloudMessageBulkUpdate);
                }
                i2++;
                oMAObjectUpdateScheduler = this;
                list = referenceList;
            }
        }
    }
}
