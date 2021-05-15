package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.database.sqlite.SQLiteFullException;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class WorkflowLocalFile extends WorkflowBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowLocalFile.class.getSimpleName();

    public WorkflowLocalFile(Looper looper, Context context, Handler handler, Mno mno, int phoneId) {
        super(looper, context, handler, mno, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = getNextWorkflow(1);
        int count = 20;
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec. and retry");
                sleep(10000);
                next = getNextWorkflow(1);
                e.printStackTrace();
            } catch (UnknownStatusException e2) {
                Log.i(LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                Log.i(LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                next = getNextWorkflow(1);
                e2.printStackTrace();
            } catch (SQLiteFullException e3) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                Log.i(LOG_TAG, "finish workflow");
                next = getNextWorkflow(8);
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec. and retry");
                sleep(1000);
                next = getNextWorkflow(1);
                e4.printStackTrace();
            }
            count--;
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleAutoconfig(int currentVersion) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "Load config from the local file");
        work();
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        if (type == 1) {
            return new WorkflowBase.Initialize() {
                public WorkflowBase.Workflow run() throws Exception {
                    WorkflowLocalFile workflowLocalFile = WorkflowLocalFile.this;
                    workflowLocalFile.addEventLog(WorkflowLocalFile.LOG_TAG + "local filename: " + ConfigUtil.LOCAL_CONFIG_FILE);
                    WorkflowLocalFile.this.mSharedInfo.setXml(ConfigUtil.getResourcesFromFile(WorkflowLocalFile.this.mContext, WorkflowLocalFile.this.mPhoneId, ConfigUtil.LOCAL_CONFIG_FILE, "utf-8"));
                    return WorkflowLocalFile.this.getNextWorkflow(6);
                }
            };
        }
        if (type == 6) {
            return new WorkflowBase.Parse() {
                public WorkflowBase.Workflow run() throws Exception {
                    String salesCode;
                    if (SimUtil.isSoftphoneEnabled()) {
                        salesCode = Mno.ATT.getMatchedSalesCode(OmcCode.get());
                    } else {
                        salesCode = WorkflowLocalFile.this.mMno.getMatchedSalesCode("");
                    }
                    Log.i(WorkflowLocalFile.LOG_TAG, "salesCode from mno = " + salesCode);
                    String salesCode2 = salesCode.toLowerCase(Locale.US);
                    if (ImsProfile.isRcsUpProfile(WorkflowLocalFile.this.mRcsProfile)) {
                        salesCode2 = salesCode2 + "_up";
                    }
                    Map<String, String> parsedXml = WorkflowLocalFile.parseJson(WorkflowLocalFile.this.mSharedInfo.getXml(), salesCode2);
                    if (parsedXml == null) {
                        throw new InvalidXmlException("no parsed xml data.");
                    } else if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                        throw new InvalidXmlException("config xml must contain atleast 2 items(version & validity).");
                    } else {
                        WorkflowLocalFile.this.mParamHandler.parseParamForLocalFile(parsedXml);
                        WorkflowLocalFile.this.mSharedInfo.setParsedXml(parsedXml);
                        return WorkflowLocalFile.this.getNextWorkflow(7);
                    }
                }
            };
        }
        if (type == 7) {
            return new WorkflowBase.Store() {
                public WorkflowBase.Workflow run() throws Exception {
                    WorkflowLocalFile.this.mParamHandler.setOpModeWithUserAccept(WorkflowLocalFile.this.mParamHandler.getUserAccept(WorkflowLocalFile.this.mSharedInfo.getParsedXml()), WorkflowLocalFile.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
                    return WorkflowLocalFile.this.getNextWorkflow(8);
                }
            };
        }
        if (type != 8) {
            return null;
        }
        return new WorkflowBase.Finish() {
            public WorkflowBase.Workflow run() throws Exception {
                WorkflowLocalFile workflowLocalFile = WorkflowLocalFile.this;
                workflowLocalFile.setLastErrorCode(workflowLocalFile.mLastErrorCodeNonRemote);
                IMSLog.i(WorkflowLocalFile.LOG_TAG, WorkflowLocalFile.this.mPhoneId, "all workflow finished");
                return null;
            }
        };
    }

    public boolean checkNetworkConnectivity() {
        return false;
    }

    public static void path(JsonElement je, String fullpath, Map<String, String> result) {
        if (!je.isJsonPrimitive()) {
            for (Map.Entry<String, JsonElement> entry : je.getAsJsonObject().entrySet()) {
                path(entry.getValue(), fullpath + "/" + entry.getKey(), result);
            }
            return;
        }
        result.put(fullpath, je.getAsString());
    }

    public static Map<String, String> parseJson(String f, String mno) {
        if (f == null) {
            return null;
        }
        JsonParser parser = new JsonParser();
        JsonReader reader = new JsonReader(new BufferedReader(new StringReader(f)));
        JsonElement element = parser.parse(reader);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObject al = element.getAsJsonObject();
        JsonObject mnoJson = null;
        for (Map.Entry<String, JsonElement> entry : al.entrySet()) {
            String[] split = entry.getKey().trim().split(",");
            int length = split.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (split[i].equals(mno)) {
                    mnoJson = entry.getValue().getAsJsonObject();
                    break;
                } else {
                    i++;
                }
            }
        }
        if (mnoJson == null) {
            return null;
        }
        Map<String, String> ret = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry2 : al.get("base").getAsJsonObject().entrySet()) {
            path(entry2.getValue(), "root/" + entry2.getKey(), ret);
        }
        for (Map.Entry<String, JsonElement> entry3 : mnoJson.entrySet()) {
            path(entry3.getValue(), "root/" + entry3.getKey(), ret);
        }
        return ret;
    }
}
