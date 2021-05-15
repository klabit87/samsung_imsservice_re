package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.sec.internal.constants.Mno;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.util.ConfigUtil;
import java.util.Map;

public class WorkflowLocalFilefromSDcard extends WorkflowLocalFile {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowLocalFilefromSDcard.class.getSimpleName();
    Mno mMno = Mno.DEFAULT;

    public WorkflowLocalFilefromSDcard(Looper looper, Context context, Handler handler, Mno mno, int phoneId) {
        super(looper, context, handler, mno, phoneId);
        this.mMno = mno;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        if (type == 1) {
            return new WorkflowBase.Initialize() {
                public WorkflowBase.Workflow run() throws Exception {
                    WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard = WorkflowLocalFilefromSDcard.this;
                    workflowLocalFilefromSDcard.addEventLog(WorkflowLocalFilefromSDcard.LOG_TAG + "local filename: " + ConfigUtil.SDCARD_CONFIG_FILE);
                    WorkflowLocalFilefromSDcard.this.mSharedInfo.setXml(ConfigUtil.getResourcesFromFile(WorkflowLocalFilefromSDcard.this.mContext, WorkflowLocalFilefromSDcard.this.mPhoneId, ConfigUtil.SDCARD_CONFIG_FILE, "utf-8"));
                    return WorkflowLocalFilefromSDcard.this.getNextWorkflow(6);
                }
            };
        }
        if (type == 6) {
            return new WorkflowBase.Parse() {
                public WorkflowBase.Workflow run() throws Exception {
                    Map<String, String> parsedXml = WorkflowLocalFilefromSDcard.this.mXmlParser.parse(WorkflowLocalFilefromSDcard.this.mSharedInfo.getXml());
                    if (parsedXml == null) {
                        throw new InvalidXmlException("no parsed xml data.");
                    } else if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                        throw new InvalidXmlException("config xml must contain atleast 2 items(version & validity).");
                    } else {
                        WorkflowLocalFilefromSDcard.this.mParamHandler.parseParamForLocalFile(parsedXml);
                        WorkflowLocalFilefromSDcard.this.mParamHandler.moveHttpParam(parsedXml);
                        WorkflowLocalFilefromSDcard.this.mSharedInfo.setParsedXml(parsedXml);
                        return WorkflowLocalFilefromSDcard.this.getNextWorkflow(7);
                    }
                }
            };
        }
        if (type != 7) {
            return super.getNextWorkflow(type);
        }
        return new WorkflowBase.Store() {
            public WorkflowBase.Workflow run() throws Exception {
                boolean userAccept = WorkflowLocalFilefromSDcard.this.mParamHandler.getUserAccept(WorkflowLocalFilefromSDcard.this.mSharedInfo.getParsedXml());
                WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard = WorkflowLocalFilefromSDcard.this;
                workflowLocalFilefromSDcard.mStartForce = userAccept ? true : workflowLocalFilefromSDcard.mStartForce;
                WorkflowLocalFilefromSDcard.this.mParamHandler.setOpModeWithUserAccept(userAccept, WorkflowLocalFilefromSDcard.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
                return WorkflowLocalFilefromSDcard.this.getNextWorkflow(8);
            }
        };
    }
}
