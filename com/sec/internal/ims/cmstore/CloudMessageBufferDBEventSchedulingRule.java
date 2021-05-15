package com.sec.internal.ims.cmstore;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;

public class CloudMessageBufferDBEventSchedulingRule {
    private static final String TAG = CloudMessageBufferDBEventSchedulingRule.class.getSimpleName();

    private void onImpossibleCombination() {
        Log.d(TAG, "onImpossibleCombination");
    }

    private void onUnprocessedCombination() {
        Log.d(TAG, "onUnprocessedCombination");
    }

    private void onActionCanceledOutEvents() {
        Log.d(TAG, "onActionCanceledOutEvents");
    }

    public ParamSyncFlagsSet getSetFlagsForMsgOperation(int dbIndex, long bufferId, CloudMessageBufferDBConstants.DirectionFlag origDirection, CloudMessageBufferDBConstants.ActionStatusFlag origAction, CloudMessageBufferDBConstants.MsgOperationFlag msgOperation) {
        ParamSyncFlagsSet rule = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[msgOperation.ordinal()]) {
            case 1:
                if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                    if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                        if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                            if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                                if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                                    rule.mIsChanged = false;
                                    break;
                                }
                            } else if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                                if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                                    onImpossibleCombination();
                                    break;
                                } else {
                                    onUnprocessedCombination();
                                    break;
                                }
                            } else {
                                onImpossibleCombination();
                                break;
                            }
                        } else {
                            onImpossibleCombination();
                            break;
                        }
                    } else if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                        onImpossibleCombination();
                        break;
                    } else {
                        onUnprocessedCombination();
                        break;
                    }
                } else if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    onImpossibleCombination();
                    break;
                } else {
                    onUnprocessedCombination();
                    break;
                }
                break;
            case 2:
            case 3:
                if (!handleWhenSendFailOrReceived(origAction)) {
                    if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                        if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad) && origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Downloading)) {
                            rule.mIsChanged = true;
                            break;
                        }
                    } else {
                        rule.mIsChanged = false;
                        break;
                    }
                } else {
                    onUnprocessedCombination();
                    break;
                }
            case 4:
            case 5:
                if (!handleWhenSendFailOrReceived(origAction)) {
                    if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                        rule.mIsChanged = false;
                        break;
                    }
                } else {
                    onUnprocessedCombination();
                    break;
                }
                break;
            case 6:
                if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                    if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                        if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                            if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                                if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                                    rule.mIsChanged = false;
                                    break;
                                }
                            } else if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                                if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                                    onImpossibleCombination();
                                    break;
                                } else {
                                    onUnprocessedCombination();
                                    break;
                                }
                            } else {
                                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                                break;
                            }
                        } else if (handleActionStatusFlagInsertWhenRead(origDirection)) {
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                            break;
                        }
                    } else if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                        if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                            if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                                if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                                    if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                                        if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                                            onImpossibleCombination();
                                            break;
                                        }
                                    } else {
                                        rule.mIsChanged = false;
                                        break;
                                    }
                                } else {
                                    rule.mIsChanged = false;
                                    break;
                                }
                            } else {
                                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                                break;
                            }
                        } else {
                            onImpossibleCombination();
                            break;
                        }
                    } else {
                        rule.mIsChanged = false;
                        break;
                    }
                } else if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                        if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                            if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                                if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                                    if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                                        onImpossibleCombination();
                                        break;
                                    }
                                } else {
                                    onActionCanceledOutEvents();
                                    break;
                                }
                            } else {
                                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                                rule.mIsChanged = false;
                                break;
                            }
                        } else {
                            onActionCanceledOutEvents();
                            break;
                        }
                    } else {
                        rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                        rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                        break;
                    }
                } else {
                    onActionCanceledOutEvents();
                    break;
                }
                break;
            case 7:
                if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                    if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                        if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                            if (!origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                                if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                                    rule.mIsChanged = false;
                                    break;
                                }
                            } else if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                                onImpossibleCombination();
                                break;
                            } else {
                                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                                break;
                            }
                        } else if (!handleActionStatusFlagInsertWhenDelete(origDirection)) {
                            if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                                    onImpossibleCombination();
                                    break;
                                }
                            } else {
                                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                                break;
                            }
                        } else {
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                            break;
                        }
                    } else if (!handleActionStatusFlagDeleteWhenDelete(origDirection)) {
                        if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                            if (!origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                                    onImpossibleCombination();
                                    break;
                                }
                            } else {
                                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                                rule.mIsChanged = false;
                                break;
                            }
                        } else {
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                            break;
                        }
                    } else {
                        rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.None;
                        rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                        break;
                    }
                } else if (handleActionStatusFlagUpdateWhenDelete(origDirection)) {
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    break;
                }
                break;
        }
        String str = TAG;
        Log.d(str, "dbIndex: " + dbIndex + ", bufferId: " + bufferId + ", getSetFlagsForMsgOperation, origDir: " + origDirection + " origAction: " + origAction + " msgOperation: " + msgOperation + ", sync flag result :" + rule.toString());
        return rule;
    }

    private boolean handleWhenSendFailOrReceived(CloudMessageBufferDBConstants.ActionStatusFlag origAction) {
        return origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete) || origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None);
    }

    private boolean handleActionStatusFlagInsertWhenRead(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }

    private boolean handleActionStatusFlagUpdateWhenDelete(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail);
    }

    private boolean handleActionStatusFlagInsertWhenDelete(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice);
    }

    private boolean handleActionStatusFlagDeleteWhenDelete(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice);
    }

    public ParamSyncFlagsSet getSetFlagsForCldOperation(int dbIndex, long bufferId, CloudMessageBufferDBConstants.DirectionFlag origDirection, CloudMessageBufferDBConstants.ActionStatusFlag origAction, CloudMessageBufferDBConstants.ActionStatusFlag cldAction) {
        ParamSyncFlagsSet rule = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[cldAction.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                        if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                            rule.mIsChanged = true;
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                            onImpossibleCombination();
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                        if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                            rule.mIsChanged = false;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                            onActionCanceledOutEvents();
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                            rule.mIsChanged = false;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                            onActionCanceledOutEvents();
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                            rule.mIsChanged = false;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                            onImpossibleCombination();
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                        if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                            rule.mIsChanged = true;
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                            onImpossibleCombination();
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                        if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                            onUnprocessedCombination();
                        } else {
                            onImpossibleCombination();
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                        rule.mIsChanged = false;
                    }
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                rule.mIsChanged = false;
                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    onImpossibleCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    rule.mIsChanged = true;
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                    onImpossibleCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    onUnprocessedCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    onUnprocessedCombination();
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                rule.mIsChanged = false;
                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    onImpossibleCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    rule.mIsChanged = true;
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                    onImpossibleCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    onImpossibleCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    onImpossibleCombination();
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    rule.mIsChanged = true;
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.None;
                } else {
                    onImpossibleCombination();
                    rule.mIsChanged = false;
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                onImpossibleCombination();
                rule.mIsChanged = false;
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                rule.mIsChanged = false;
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
            if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                onActionCanceledOutEvents();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                onActionCanceledOutEvents();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                rule.mIsChanged = true;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                onImpossibleCombination();
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                rule.mIsChanged = true;
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                onImpossibleCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                rule.mIsChanged = false;
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
            if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                rule.mIsChanged = true;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                rule.mIsChanged = true;
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
            if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
            } else {
                onImpossibleCombination();
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
            rule.mIsChanged = false;
        }
        String str = TAG;
        Log.d(str, "dbIndex: " + dbIndex + ", bufferId: " + bufferId + ", getSetFlagsForCldOperation, origDir: " + origDirection + " origAction: " + origAction + " cldAction: " + cldAction + ", sync flag result :" + rule.toString());
        return rule;
    }

    public ParamSyncFlagsSet getSetFlagsForMsgResponse(int dbIndex, long bufferId, CloudMessageBufferDBConstants.DirectionFlag origDirection, CloudMessageBufferDBConstants.ActionStatusFlag origAction, CloudMessageBufferDBConstants.ActionStatusFlag msgResponse) {
        if (msgResponse.equals(origAction) && origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
            return new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        }
        if (!msgResponse.equals(origAction) && origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
            return new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice, origAction);
        }
        ParamSyncFlagsSet rule = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[msgResponse.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    if (isActionStatusFlag(origAction)) {
                        if (isDirectionFlag(origDirection)) {
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                        rule.mIsChanged = false;
                    }
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    onUnprocessedCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    rule.mIsChanged = false;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    rule.mIsChanged = false;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    onUnprocessedCombination();
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    onUnprocessedCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    rule.mIsChanged = false;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    rule.mIsChanged = false;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    onUnprocessedCombination();
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                onUnprocessedCombination();
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                onUnprocessedCombination();
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                rule.mIsChanged = false;
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
            onUnprocessedCombination();
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                onUnprocessedCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                onUnprocessedCombination();
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
            onUnprocessedCombination();
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
            onUnprocessedCombination();
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
            rule.mIsChanged = false;
        }
        String str = TAG;
        Log.d(str, "dbIndex: " + dbIndex + ", bufferId: " + bufferId + ", getSetFlagsForMsgResponse, origDir: " + origDirection + " origAction: " + origAction + " msgResponse: " + msgResponse + ", sync flag result :" + rule.toString());
        return rule;
    }

    private boolean isActionStatusFlag(CloudMessageBufferDBConstants.ActionStatusFlag origAction) {
        return origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete) || origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None);
    }

    private boolean isDirectionFlag(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }

    public ParamSyncFlagsSet getSetFlagsForCldResponse(int dbIndex, long bufferId, CloudMessageBufferDBConstants.DirectionFlag origDirection, CloudMessageBufferDBConstants.ActionStatusFlag origAction, CloudMessageBufferDBConstants.CloudResponseFlag cldResponse) {
        ParamSyncFlagsSet rule = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag[cldResponse.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                        if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                            onUnprocessedCombination();
                        } else if (handleActionStatusFlagUpdateWhenSetDelete(origDirection)) {
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                        if (handleActionStatusFlagDeleteWhenSetDelete(origDirection)) {
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                        } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                            onUnprocessedCombination();
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                        if (handleActionStatusFlagInsertOrNoneWhenSetDelete(origDirection)) {
                            rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                            rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                        }
                    } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                        rule.mIsChanged = false;
                    }
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                onUnprocessedCombination();
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    onUnprocessedCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                    rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                    onImpossibleCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    rule.mIsChanged = false;
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    onImpossibleCombination();
                } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    onUnprocessedCombination();
                }
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                onUnprocessedCombination();
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                onUnprocessedCombination();
            } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                rule.mIsChanged = false;
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
            if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                onUnprocessedCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                onImpossibleCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                onImpossibleCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                onUnprocessedCombination();
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                onUnprocessedCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                rule.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                rule.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                onImpossibleCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                rule.mIsChanged = false;
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                onImpossibleCombination();
            } else if (origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                onUnprocessedCombination();
            }
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
            onUnprocessedCombination();
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
            onUnprocessedCombination();
        } else if (origAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
            rule.mIsChanged = false;
        }
        String str = TAG;
        Log.d(str, "dbIndex: " + dbIndex + ", bufferId: " + bufferId + ", getSetFlagsForCldResponse, origDir: " + origDirection + " origAction: " + origAction + " cldResponse: " + cldResponse + ", sync flag result :" + rule.toString());
        return rule;
    }

    /* renamed from: com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        static {
            int[] iArr = new int[CloudMessageBufferDBConstants.CloudResponseFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag = iArr;
            try {
                iArr[CloudMessageBufferDBConstants.CloudResponseFlag.Inserted.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag[CloudMessageBufferDBConstants.CloudResponseFlag.SetRead.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag[CloudMessageBufferDBConstants.CloudResponseFlag.SetDelete.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            int[] iArr2 = new int[CloudMessageBufferDBConstants.ActionStatusFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag = iArr2;
            try {
                iArr2[CloudMessageBufferDBConstants.ActionStatusFlag.Update.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[CloudMessageBufferDBConstants.ActionStatusFlag.Insert.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[CloudMessageBufferDBConstants.ActionStatusFlag.Delete.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            int[] iArr3 = new int[CloudMessageBufferDBConstants.MsgOperationFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = iArr3;
            try {
                iArr3[CloudMessageBufferDBConstants.MsgOperationFlag.Receiving.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sent.ordinal()] = 2;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Received.ordinal()] = 3;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sending.ordinal()] = 4;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.SendFail.ordinal()] = 5;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Read.ordinal()] = 6;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Delete.ordinal()] = 7;
            } catch (NoSuchFieldError e13) {
            }
        }
    }

    private boolean handleActionStatusFlagDeleteWhenSetDelete(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice);
    }

    private boolean handleActionStatusFlagInsertOrNoneWhenSetDelete(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }

    private boolean handleActionStatusFlagUpdateWhenSetDelete(CloudMessageBufferDBConstants.DirectionFlag origDirection) {
        return origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || origDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }
}
