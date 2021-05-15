package com.sec.internal.omanetapi.nms.data;

import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.NotifyObject;

public class NmsEvent {
    public ChangedFolder changedFolder;
    public ChangedObject changedObject;
    public DeletedFolder deletedFolder;
    public DeletedObject deletedObject;
    public DeletedFolder expiredFolder;
    public DeletedObject expiredObject;
    public NmsEventObjectAppendix message;
    public NotifyObject notifyObject;
    public Object object;
    public ResetBox resetBox;
}
