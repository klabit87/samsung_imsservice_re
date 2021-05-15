package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;

public class ParamSyncFlagsSet {
    public CloudMessageBufferDBConstants.ActionStatusFlag mAction;
    public long mBufferId;
    public CloudMessageBufferDBConstants.DirectionFlag mDirection;
    public boolean mIsChanged = true;

    public ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag direction, CloudMessageBufferDBConstants.ActionStatusFlag action) {
        this.mDirection = direction;
        this.mAction = action;
    }

    public void setIsChangedActionAndDirection(boolean isChanged, CloudMessageBufferDBConstants.ActionStatusFlag action, CloudMessageBufferDBConstants.DirectionFlag direction) {
        this.mIsChanged = isChanged;
        this.mAction = action;
        this.mDirection = direction;
    }

    public String toString() {
        return "ParamSyncFlagsSet [mDirection=" + this.mDirection + ", mAction=" + this.mAction + ", mIsChanged=" + this.mIsChanged + "]";
    }
}
