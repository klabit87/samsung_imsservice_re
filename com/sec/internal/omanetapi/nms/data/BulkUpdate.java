package com.sec.internal.omanetapi.nms.data;

import com.sec.internal.constants.ims.cmstore.data.OperationEnum;

public class BulkUpdate {
    public FlagList flags;
    public ObjectReferenceList objects;
    public OperationEnum operation;
    public SelectionCriteria selectionCriteria;
}
