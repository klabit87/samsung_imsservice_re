package com.sec.internal.ims.aec.workflow;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;

public class WorkflowFactory {
    private static SparseArray<IWorkflowImpl> mWorkflowArray = new SparseArray<>();
    private static WorkflowFactory mWorkflowFactory = null;
    private static SparseArray<HandlerThread> mWorkflowThreads = new SparseArray<>();
    private final Context mContext;

    public WorkflowFactory(Context context) {
        this.mContext = context;
    }

    public static synchronized WorkflowFactory getWorkflowFactory() {
        WorkflowFactory workflowFactory;
        synchronized (WorkflowFactory.class) {
            workflowFactory = mWorkflowFactory;
        }
        return workflowFactory;
    }

    public static synchronized WorkflowFactory createWorkflowFactory(Context context) {
        WorkflowFactory workflowFactory;
        synchronized (WorkflowFactory.class) {
            if (mWorkflowFactory == null) {
                mWorkflowFactory = new WorkflowFactory(context);
            }
            workflowFactory = mWorkflowFactory;
        }
        return workflowFactory;
    }

    private synchronized IWorkflowImpl newInstance(String server, HandlerThread handlerThread, Handler handler) {
        IWorkflowImpl workflow;
        workflow = null;
        if ("ts43".equalsIgnoreCase(server)) {
            workflow = new WorkflowTS43(this.mContext, handlerThread.getLooper(), handler);
        }
        return workflow;
    }

    public synchronized boolean createWorkflow(int phoneId, String imsi, String mno, String server, Handler handler) {
        HandlerThread handlerThread = new HandlerThread("Workflow" + phoneId + mno);
        handlerThread.start();
        IWorkflowImpl workflow = newInstance(server, handlerThread, handler);
        if (workflow == null) {
            handlerThread.quit();
            return false;
        }
        mWorkflowArray.append(phoneId, workflow);
        if (mWorkflowThreads.get(phoneId) != null) {
            mWorkflowThreads.get(phoneId).quit();
        }
        mWorkflowThreads.append(phoneId, handlerThread);
        workflow.initWorkflow(phoneId, imsi, mno);
        return true;
    }

    public synchronized IWorkflowImpl getWorkflow(int phoneId) {
        return mWorkflowArray.get(phoneId);
    }

    public synchronized SparseArray<IWorkflowImpl> getAllWorkflow() {
        return mWorkflowArray;
    }

    public synchronized void clearWorkflow(int phoneId) {
        mWorkflowArray.remove(phoneId);
        if (mWorkflowThreads.get(phoneId) != null) {
            mWorkflowThreads.get(phoneId).quit();
            mWorkflowThreads.remove(phoneId);
        }
    }
}
