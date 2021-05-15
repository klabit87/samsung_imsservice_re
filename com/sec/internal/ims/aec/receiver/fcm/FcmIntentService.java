package com.sec.internal.ims.aec.receiver.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.ims.aec.workflow.WorkflowFactory;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;
import com.sec.internal.log.AECLog;

public class FcmIntentService extends IntentService {
    private static final String LOG_TAG = FcmIntentService.class.getSimpleName();

    public FcmIntentService() {
        super(LOG_TAG);
    }

    /* access modifiers changed from: protected */
    public void onHandleIntent(Intent intent) {
        synchronized (LOG_TAG) {
            int phoneId = intent.getIntExtra("phoneId", 0);
            String senderId = intent.getStringExtra(AECNamespace.NotifExtras.SENDER_ID);
            try {
                if (TextUtils.isEmpty(senderId)) {
                    updateFcmToken(phoneId, (String) null, "fcm senderId not ready");
                } else {
                    String token = FirebaseInstanceId.getInstance().getToken(senderId, FirebaseMessaging.INSTANCE_ID_SCOPE);
                    if (TextUtils.isEmpty(token)) {
                        updateFcmToken(phoneId, (String) null, "fcm token not ready");
                    } else {
                        String str = LOG_TAG;
                        AECLog.s(str, senderId + ", " + token, phoneId);
                        updateFcmToken(phoneId, token, "fcm token ready");
                    }
                }
            } catch (Exception e) {
                updateFcmToken(phoneId, (String) null, e.getMessage());
            }
        }
    }

    private void updateFcmToken(int phoneId, String token, String message) {
        IWorkflowImpl workflow;
        WorkflowFactory workflowFactory = WorkflowFactory.getWorkflowFactory();
        if (workflowFactory != null && (workflow = workflowFactory.getWorkflow(phoneId)) != null) {
            workflow.updateFcmToken(token, message);
        }
    }
}
