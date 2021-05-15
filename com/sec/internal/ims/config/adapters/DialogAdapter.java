package com.sec.internal.ims.config.adapters;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.sec.ims.settings.ImsProfile;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.helper.userconsent.HyperlinkUtils;
import com.sec.internal.helper.userconsent.IHyperlinkOnClickListener;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IDialogAdapter;
import com.sec.internal.log.IMSLog;
import java.util.concurrent.Semaphore;

public class DialogAdapter extends Handler implements IDialogAdapter {
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String CANCEL_TC_NOTIFICATION = "com.samsung.rcs.framework.dialogadapter.action.CANCEL_TC_NOTIFICATION";
    static final int HANDLE_CREATE_SHOW_ACCEPT_REJECT = 0;
    static final int HANDLE_CREATE_SHOW_AUTOCONFIG = 5;
    static final int HANDLE_CREATE_SHOW_MSISDN = 2;
    static final int HANDLE_SIM_STATE_ABSENT = 6;
    static final String KEY_PHONE_ID = "phone_id";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = DialogAdapter.class.getSimpleName();
    static final int RCS_MSISDN_PROMPT_NOTIFICATION = 56846849;
    static final int RCS_TC_NOTIFICATION = 11012013;
    public static final String SHOW_MSISDN_POPUP = "com.samsung.rcs.framework.dialogadapter.action.SHOW_MSISDN_POPUP";
    public static final String SHOW_TC_POPUP = "com.samsung.rcs.framework.dialogadapter.action.SHOW_TC_POPUP";
    static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private String mAccept;
    /* access modifiers changed from: private */
    public boolean mAcceptReject;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public String mCountryCode;
    /* access modifiers changed from: private */
    public AlertDialog mDialog;
    private DialogNotiReceiver mDialogNotiReceiver;
    private String mMessage;
    /* access modifiers changed from: private */
    public String mMsisdn;
    /* access modifiers changed from: private */
    public boolean mNextCancel;
    /* access modifiers changed from: private */
    public NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public int mPhoneId;
    private Receiver mReceiver;
    private ReceiverForTcPopup mReceiverForTcPopup;
    private String mReject;
    /* access modifiers changed from: private */
    public final Semaphore mSemaphore;
    /* access modifiers changed from: private */
    public boolean mSkip;
    /* access modifiers changed from: private */
    public boolean mSupportNotiBar;
    /* access modifiers changed from: private */
    public boolean mTcPopupFlag;
    /* access modifiers changed from: private */
    public ITelephonyManager mTelephony;
    private String mTitle;
    /* access modifiers changed from: private */
    public boolean mYesNo;

    public DialogAdapter(Context context, Handler handler, int phoneId) {
        this(context, handler);
        this.mPhoneId = phoneId;
    }

    public DialogAdapter(Context context, Handler handler) {
        super(handler.getLooper());
        this.mTcPopupFlag = false;
        this.mSemaphore = new Semaphore(0);
        this.mDialog = null;
        this.mTitle = null;
        this.mMessage = null;
        this.mAccept = null;
        this.mReject = null;
        this.mPhoneId = 0;
        this.mCountryCode = null;
        this.mAcceptReject = false;
        this.mYesNo = false;
        this.mNextCancel = false;
        this.mSkip = false;
        this.mMsisdn = "";
        this.mSupportNotiBar = true;
        this.mTelephony = null;
        this.mDialogNotiReceiver = new DialogNotiReceiver();
        this.mReceiverForTcPopup = new ReceiverForTcPopup();
        this.mReceiver = new Receiver();
        IMSLog.i(LOG_TAG, this.mPhoneId, "Init DialogAdapter");
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mTelephony = TelephonyManagerWrapper.getInstance(this.mContext);
        registerReceivers();
    }

    private void registerReceivers() {
        IntentFilter dialogNotiFilter = new IntentFilter();
        dialogNotiFilter.addAction(SHOW_TC_POPUP);
        dialogNotiFilter.addAction(CANCEL_TC_NOTIFICATION);
        dialogNotiFilter.addAction(SHOW_MSISDN_POPUP);
        this.mContext.registerReceiver(this.mDialogNotiReceiver, dialogNotiFilter);
        Context context = this.mContext;
        Receiver receiver = this.mReceiver;
        context.registerReceiver(receiver, receiver.getIntentFilter());
    }

    private class DialogNotiReceiver extends BroadcastReceiver {
        private DialogNotiReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String access$100 = DialogAdapter.LOG_TAG;
            IMSLog.i(access$100, "DialogNotiReceiver : " + action);
            if (context.getContentResolver() != null && intent.getExtras() != null && intent.getExtras().getInt(DialogAdapter.KEY_PHONE_ID, -1) == DialogAdapter.this.mPhoneId) {
                if (DialogAdapter.SHOW_TC_POPUP.equals(action) && !DialogAdapter.this.mTcPopupFlag) {
                    DialogAdapter.this.sendEmptyMessage(0);
                } else if (DialogAdapter.CANCEL_TC_NOTIFICATION.equals(action)) {
                    boolean unused = DialogAdapter.this.mTcPopupFlag = false;
                    DialogAdapter.this.mNotificationManager.cancel(DialogAdapter.this.mPhoneId + DialogAdapter.RCS_TC_NOTIFICATION);
                } else if (DialogAdapter.SHOW_MSISDN_POPUP.equals(action)) {
                    DialogAdapter.this.mNotificationManager.cancel(DialogAdapter.this.mPhoneId + DialogAdapter.RCS_MSISDN_PROMPT_NOTIFICATION);
                    DialogAdapter.this.sendEmptyMessage(2);
                }
            }
        }
    }

    private class ReceiverForTcPopup extends BroadcastReceiver {
        private ReceiverForTcPopup() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String reason = intent.getExtras().getString("reason");
            String access$100 = DialogAdapter.LOG_TAG;
            IMSLog.i(access$100, "ReceiverForTcPopup : " + action + ", reason : " + reason);
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) && DialogAdapter.this.mDialog != null && reason != null) {
                if (reason.equals(DialogAdapter.SYSTEM_DIALOG_REASON_RECENT_APPS) || reason.equals(DialogAdapter.SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    boolean unused = DialogAdapter.this.mTcPopupFlag = false;
                    DialogAdapter.this.mDialog.dismiss();
                    DialogAdapter.this.unregisterReceiverForTcPopup();
                }
            }
        }
    }

    private void registerReceiverForTcPopup() {
        IntentFilter intentFilterTcPopup = new IntentFilter();
        intentFilterTcPopup.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mContext.registerReceiver(this.mReceiverForTcPopup, intentFilterTcPopup);
    }

    /* access modifiers changed from: private */
    public void unregisterReceiverForTcPopup() {
        this.mContext.unregisterReceiver(this.mReceiverForTcPopup);
    }

    private class Receiver extends BroadcastReceiver {
        private IntentFilter mIntentFilter;

        public Receiver() {
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED") && 1 == DialogAdapter.this.mTelephony.getSimState() && DialogAdapter.this.mDialog != null) {
                IMSLog.i(DialogAdapter.LOG_TAG, "sim state: HANDLE_SIM_STATE_ABSENT");
                DialogAdapter dialogAdapter = DialogAdapter.this;
                dialogAdapter.sendMessage(dialogAdapter.obtainMessage(6, dialogAdapter.mDialog));
            }
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }

    private void showRcsNotification(int type, String title, String message) {
        PendingIntent pendingIntent;
        String str = LOG_TAG;
        IMSLog.i(str, "showshowRcsNotification, type: " + type);
        String tcNoti = this.mContext.getResources().getString(R.string.app_name);
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(tcNoti, tcNoti, 2));
        Notification.Builder mBuilder = new Notification.Builder(this.mContext, tcNoti);
        mBuilder.setSmallIcon(R.drawable.stat_notify_rcs_service_avaliable);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        if (type == RCS_TC_NOTIFICATION) {
            Intent tcIntent = new Intent(SHOW_TC_POPUP);
            tcIntent.putExtra(KEY_PHONE_ID, this.mPhoneId);
            pendingIntent = PendingIntent.getBroadcast(this.mContext, this.mPhoneId + type, tcIntent, 134217728);
            String message2 = Html.fromHtml(message, 0).toString();
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(message2);
        } else if (type != RCS_MSISDN_PROMPT_NOTIFICATION) {
            IMSLog.i(LOG_TAG, "showshowRcsNotification: unsupported type!");
            return;
        } else {
            Intent msisdnIntent = new Intent(SHOW_MSISDN_POPUP);
            msisdnIntent.putExtra(KEY_PHONE_ID, this.mPhoneId);
            pendingIntent = PendingIntent.getBroadcast(this.mContext, this.mPhoneId + type, msisdnIntent, 134217728);
            mBuilder.setContentTitle(this.mContext.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title) + " [SIM" + (this.mPhoneId + 1) + "]");
            mBuilder.setContentText(Html.fromHtml(this.mContext.getResources().getString(R.string.dialog_text_rcs_config_msisdn_text)));
        }
        if (pendingIntent != null) {
            mBuilder.setContentIntent(pendingIntent);
        }
        this.mNotificationManager.notify(this.mPhoneId + type, mBuilder.build());
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        int i2 = msg.what;
        if (i2 == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "accept, reject dialog create & show");
            this.mSupportNotiBar = true;
            if (msg.obj != null) {
                this.mSupportNotiBar = ((Boolean) msg.obj).booleanValue();
            }
            String str2 = LOG_TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str2, i3, "support_notification_for_TnC : " + this.mSupportNotiBar);
            if (this.mSupportNotiBar) {
                registerReceiverForTcPopup();
            }
            AlertDialog create = createAcceptRejectBuilder(this.mContext, this.mTitle, this.mMessage, this.mAccept, this.mReject).create();
            this.mDialog = create;
            create.getWindow().addFlags(65792);
            this.mDialog.getWindow().setType(2038);
            this.mDialog.setCancelable(this.mSupportNotiBar);
            this.mTcPopupFlag = true;
            this.mDialog.show();
        } else if (i2 == 2) {
            AlertDialog create2 = createMsisdnBuilder(this.mContext).create();
            this.mDialog = create2;
            create2.getWindow().setSoftInputMode(32);
            if (!"2017A".equals(SemSystemProperties.get("ro.build.scafe.version"))) {
                this.mDialog.getWindow().addFlags(65536);
            }
            this.mDialog.getWindow().setType(2038);
            this.mDialog.setCancelable(false);
            this.mDialog.show();
            this.mDialog.getButton(-1).setEnabled(false);
        } else if (i2 == 5) {
            this.mDialog = createAutoconfigBuilder(this.mContext).create();
            if (SemSystemProperties.get("ro.build.scafe.cream").contains("white")) {
                this.mDialog.getWindow().setType(2038);
            } else {
                this.mDialog.getWindow().addFlags(65792);
                this.mDialog.getWindow().setType(2038);
            }
            this.mDialog.setCancelable(false);
            this.mDialog.show();
        } else if (i2 != 6) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unknown message!!");
        } else {
            AlertDialog dialog2 = (AlertDialog) msg.obj;
            if (dialog2 != null) {
                dialog2.dismiss();
                this.mSemaphore.release();
                IMSLog.i(LOG_TAG, this.mPhoneId, "dismiss Dialog for getMsisdn");
            }
        }
    }

    private boolean isStringValid(String toValidate) {
        return toValidate != null && !toValidate.isEmpty();
    }

    private static boolean shouldShowButton(String toValidate) {
        return "1".equals(toValidate);
    }

    public boolean getAcceptReject(String title, String message, String accept, String reject) {
        return getAcceptReject(title, message, accept, reject, this.mPhoneId);
    }

    public boolean getAcceptReject(String title, String message, String accept, String reject, int phoneId) {
        this.mTitle = title;
        this.mMessage = message;
        this.mAccept = accept;
        this.mReject = reject;
        this.mPhoneId = phoneId;
        String str = LOG_TAG;
        IMSLog.i(str, "phoneId : " + phoneId);
        if (!isStringValid(this.mTitle) || !isStringValid(this.mMessage) || (!shouldShowButton(this.mAccept) && !shouldShowButton(this.mReject))) {
            String str2 = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str2, i, "popup dialog cancelled mTitle :" + this.mTitle + "mMessage : " + this.mMessage + "mAccept : " + this.mAccept + "mReject :" + this.mReject);
            return true;
        }
        showRcsNotification(RCS_TC_NOTIFICATION, title, message);
        boolean isSetupWizardCompleted = Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1;
        if (!this.mTcPopupFlag && isSetupWizardCompleted) {
            sendMessage(obtainMessage(0, Boolean.valueOf(ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.SUPPORT_NOTIFICATION_FOR_TNC, true))));
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "wait yes or no");
        if (shallRcsRegisterByDefault(reject)) {
            this.mAcceptReject = true;
        } else {
            try {
                this.mSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String str3 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str3, i2, "receive yes or no:" + this.mAcceptReject);
        this.mTcPopupFlag = false;
        return this.mAcceptReject;
    }

    public String getMsisdn(String countryCode, String oldMsisdn) {
        this.mMsisdn = oldMsisdn;
        String str = LOG_TAG;
        Log.d(str, "Setting old msisdn: " + IMSLog.checker(oldMsisdn) + " entered earlier by user");
        return getMsisdn(countryCode);
    }

    public String getMsisdn(String countryCode) {
        this.mCountryCode = countryCode;
        boolean isSetupWizardCompleted = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1) {
            isSetupWizardCompleted = true;
        }
        if (isSetupWizardCompleted) {
            sendEmptyMessage(2);
        } else {
            showRcsNotification(RCS_MSISDN_PROMPT_NOTIFICATION, (String) null, (String) null);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "wait MSISDN");
        try {
            this.mSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "mYesNo: " + this.mYesNo + " mSkip: " + this.mSkip);
        if (this.mYesNo) {
            Log.d(LOG_TAG, "receive MSISDN:" + IMSLog.checker(this.mMsisdn));
        } else if (this.mSkip) {
            this.mMsisdn = "skip";
        }
        return this.mMsisdn;
    }

    public boolean getNextCancel() {
        sendEmptyMessage(5);
        IMSLog.i(LOG_TAG, this.mPhoneId, "wait Next or Cancel");
        try {
            this.mSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getNextCancel : " + this.mNextCancel);
        return this.mNextCancel;
    }

    private AlertDialog.Builder createAcceptRejectBuilder(Context context, String title, String message, String accept, final String reject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 16974546);
        ScrollView scrollView = (ScrollView) LayoutInflater.from(this.mContext).inflate(R.layout.notification_dialog, (ViewGroup) null);
        LinearLayout linearLayout = (LinearLayout) scrollView.findViewById(R.id.notification_dialog);
        builder.setView(scrollView);
        if (title != null) {
            builder.setTitle(title);
        }
        TextView msg = (TextView) linearLayout.findViewById(R.id.messagebox);
        if (message != null) {
            HyperlinkUtils.processLinks(msg, message, new IHyperlinkOnClickListener() {
                public void onClick(View view, Uri uri) {
                    Intent i = new Intent("android.intent.action.VIEW");
                    i.setData(uri);
                    i.setFlags(LogClass.SIM_EVENT);
                    try {
                        DialogAdapter.this.mContext.startActivity(i);
                        if (DialogAdapter.this.mDialog != null) {
                            DialogAdapter.this.mDialog.cancel();
                        }
                    } catch (ActivityNotFoundException activityNotFound) {
                        IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, activityNotFound.getMessage());
                        Toast.makeText(DialogAdapter.this.mContext, R.string.hyperlink_format_not_supported_exception, 0).show();
                    }
                }
            });
        }
        if ("1".equals(accept)) {
            String posButton = context.getResources().getString(R.string.dialog_text_rcs_config_ok);
            if ("1".equals(reject)) {
                posButton = context.getResources().getString(R.string.dialog_text_rcs_config_accept);
            }
            builder.setPositiveButton(posButton, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent cancelTcIntent = new Intent(DialogAdapter.CANCEL_TC_NOTIFICATION);
                    cancelTcIntent.putExtra(DialogAdapter.KEY_PHONE_ID, DialogAdapter.this.mPhoneId);
                    DialogAdapter.this.mContext.sendBroadcast(cancelTcIntent);
                    dialog.dismiss();
                    if (DialogAdapter.this.mSupportNotiBar) {
                        DialogAdapter.this.unregisterReceiverForTcPopup();
                    }
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "PositiveButton");
                    boolean unused = DialogAdapter.this.mAcceptReject = true;
                    if (!DialogAdapter.this.shallRcsRegisterByDefault(reject)) {
                        DialogAdapter.this.mSemaphore.release();
                    }
                }
            });
        }
        if ("1".equals(reject)) {
            builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_config_reject), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent cancelTcIntent = new Intent(DialogAdapter.CANCEL_TC_NOTIFICATION);
                    cancelTcIntent.putExtra(DialogAdapter.KEY_PHONE_ID, DialogAdapter.this.mPhoneId);
                    DialogAdapter.this.mContext.sendBroadcast(cancelTcIntent);
                    dialog.dismiss();
                    if (DialogAdapter.this.mSupportNotiBar) {
                        DialogAdapter.this.unregisterReceiverForTcPopup();
                    }
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NegativeButton");
                    boolean unused = DialogAdapter.this.mAcceptReject = false;
                    DialogAdapter.this.mSemaphore.release();
                }
            });
        }
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "onCancel");
                boolean unused = DialogAdapter.this.mTcPopupFlag = false;
                dialog.dismiss();
                if (DialogAdapter.this.mSupportNotiBar) {
                    DialogAdapter.this.unregisterReceiverForTcPopup();
                }
            }
        });
        return builder;
    }

    private AlertDialog.Builder createMsisdnBuilder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 16974546);
        if (RcsUtils.DualRcs.isDualRcsReg()) {
            builder.setTitle(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title) + "[SIM" + (this.mPhoneId + 1) + "]");
        } else {
            builder.setTitle(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title));
        }
        LayoutInflater factory = LayoutInflater.from(this.mContext);
        ScrollView scrollView = (ScrollView) factory.inflate(R.layout.notification_dialog, (ViewGroup) null);
        LinearLayout linearLayout = (LinearLayout) scrollView.findViewById(R.id.notification_dialog);
        View inputBoxWithLabel = factory.inflate(R.layout.notification_inputbox, (ViewGroup) null);
        ((TextView) linearLayout.findViewById(R.id.messagebox)).setText(Html.fromHtml(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_text), 0));
        final EditText input = (EditText) inputBoxWithLabel.findViewById(R.id.input);
        linearLayout.addView(inputBoxWithLabel);
        if (!"".equals(this.mMsisdn) && !"skip".equals(this.mMsisdn)) {
            input.setText(this.mMsisdn);
        }
        input.setInputType(3);
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                String access$100 = DialogAdapter.LOG_TAG;
                int access$200 = DialogAdapter.this.mPhoneId;
                IMSLog.i(access$100, access$200, "input:" + s.toString());
                if (ImsCallUtil.validatePhoneNumber(s.toString(), DialogAdapter.this.mCountryCode).length() != 0) {
                    DialogAdapter.this.mDialog.getButton(-1).setEnabled(true);
                } else {
                    DialogAdapter.this.mDialog.getButton(-1).setEnabled(false);
                }
            }
        });
        builder.setView(scrollView);
        builder.setPositiveButton(context.getResources().getString(R.string.dialog_text_rcs_config_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "PositiveButton");
                String unused = DialogAdapter.this.mMsisdn = ImsCallUtil.validatePhoneNumber(input.getText().toString(), DialogAdapter.this.mCountryCode);
                boolean unused2 = DialogAdapter.this.mYesNo = true;
                boolean unused3 = DialogAdapter.this.mSkip = false;
                DialogAdapter.this.mSemaphore.release();
            }
        });
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        if (!isEnableUPInImsprofile() || !mno.isVodafone()) {
            builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_config_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NegativeButton");
                    String unused = DialogAdapter.this.mMsisdn = "";
                    boolean unused2 = DialogAdapter.this.mYesNo = false;
                    boolean unused3 = DialogAdapter.this.mSkip = false;
                    DialogAdapter.this.mSemaphore.release();
                }
            });
        }
        if (isEnableUPInImsprofile()) {
            builder.setNeutralButton(context.getResources().getString(R.string.dialog_text_rcs_config_skip), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NeutralButton");
                    String unused = DialogAdapter.this.mMsisdn = "";
                    boolean unused2 = DialogAdapter.this.mYesNo = false;
                    boolean unused3 = DialogAdapter.this.mSkip = true;
                    DialogAdapter.this.mSemaphore.release();
                }
            });
        }
        return builder;
    }

    private AlertDialog.Builder createAutoconfigBuilder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 16974546);
        builder.setTitle(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title));
        builder.setMessage(Html.fromHtml(context.getResources().getString(R.string.dialog_text_rcs_config_autoconfig_text), 0));
        builder.setPositiveButton(context.getResources().getString(R.string.dialog_text_rcs_config_next), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "PositiveButton");
                boolean unused = DialogAdapter.this.mNextCancel = true;
                DialogAdapter.this.mSemaphore.release();
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_config_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NegativeButton");
                boolean unused = DialogAdapter.this.mNextCancel = false;
                DialogAdapter.this.mSemaphore.release();
            }
        });
        return builder;
    }

    private boolean isEnableUPInImsprofile() {
        return ImsProfile.isRcsUpProfile(ImsRegistry.getRcsProfileType(this.mPhoneId));
    }

    /* access modifiers changed from: private */
    public boolean shallRcsRegisterByDefault(String reject) {
        if (!SimUtil.getSimMno(this.mPhoneId).isOneOf(Mno.TELEFONICA_GERMANY, Mno.TELEFONICA_SPAIN, Mno.TELEFONICA_UK) || "1".equals(reject)) {
            return false;
        }
        return true;
    }

    public void cleanup() {
        DialogNotiReceiver dialogNotiReceiver = this.mDialogNotiReceiver;
        if (dialogNotiReceiver != null) {
            this.mContext.unregisterReceiver(dialogNotiReceiver);
        }
        Receiver receiver = this.mReceiver;
        if (receiver != null) {
            this.mContext.unregisterReceiver(receiver);
        }
    }
}
