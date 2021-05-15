package com.sec.internal.ims.aec.receiver.fcm;

import android.util.SparseArray;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sec.internal.ims.aec.workflow.WorkflowFactory;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;
import com.sec.internal.log.AECLog;

public class FcmInstanceIdService extends FirebaseInstanceIdService {
    private static final String LOG_TAG = FcmInstanceIdService.class.getSimpleName();

    public void onTokenRefresh() {
        AECLog.i(LOG_TAG, "onTokenRefresh");
        WorkflowFactory workflowFactory = WorkflowFactory.getWorkflowFactory();
        if (workflowFactory != null) {
            SparseArray<IWorkflowImpl> workflowArray = workflowFactory.getAllWorkflow();
            for (int i = 0; i < workflowArray.size(); i++) {
                IWorkflowImpl workflow = workflowArray.get(workflowArray.keyAt(i));
                if (workflow != null) {
                    workflow.refreshFcmToken();
                }
            }
        }
    }
}
