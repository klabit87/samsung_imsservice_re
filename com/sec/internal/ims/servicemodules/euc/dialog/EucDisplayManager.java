package com.sec.internal.ims.servicemodules.euc.dialog;

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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.sec.imsservice.R;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.userconsent.HyperlinkUtils;
import com.sec.internal.helper.userconsent.IHyperlinkOnClickListener;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EucDisplayManager implements IEucDisplayManager {
    private static final String EUC_KEY = "euc_key";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = EucDisplayManager.class.getSimpleName();
    private static final String SHOW_EUC_DIALOG = "com.sec.internal.ims.servicemodules.euc.dialog.action.SHOW_EUC_DIALOG";
    private static final String START_NOT_CALLED_EXCEPTION_MESSAGE = "start was not called!";
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final HashMap<EucMessageKey, AlertDialog> mDialogs = new HashMap<>();
    private final EucNotificationReceiver mEucNotificationReceiver = new EucNotificationReceiver();
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final NotificationManager mNotificationManager = ((NotificationManager) this.mContext.getSystemService("notification"));
    private boolean mStartCalled = false;

    private class EucNotificationReceiver extends BroadcastReceiver {
        private EucNotificationReceiver() {
        }

        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            String access$000 = EucDisplayManager.LOG_TAG;
            Log.d(access$000, "EucNotificationReceiver: " + action);
            if (EucDisplayManager.SHOW_EUC_DIALOG.equals(action) && intent.getExtras() != null) {
                EucDisplayManager.this.mHandler.post(new Runnable() {
                    public void run() {
                        EucMessageKey key = EucMessageKey.unmarshall(intent.getExtras().getByteArray(EucDisplayManager.EUC_KEY));
                        EucDisplayManager.this.mNotificationManager.cancel(key.hashCode());
                        if (EucDisplayManager.this.mDialogs.containsKey(key)) {
                            ((AlertDialog) EucDisplayManager.this.mDialogs.get(key)).show();
                        }
                    }
                });
            }
        }
    }

    public EucDisplayManager(Context context, Handler serviceModuleHandler) {
        this.mContext = context;
        this.mHandler = serviceModuleHandler;
    }

    public void start() throws IllegalStateException {
        Log.d(LOG_TAG, "start");
        Preconditions.checkState(!this.mStartCalled, "start was already called!");
        this.mStartCalled = true;
        IntentFilter eucIntentFilter = new IntentFilter();
        eucIntentFilter.addAction(SHOW_EUC_DIALOG);
        this.mContext.registerReceiver(this.mEucNotificationReceiver, eucIntentFilter);
    }

    public void stop() throws IllegalStateException {
        Log.d(LOG_TAG, "stop");
        Preconditions.checkState(this.mStartCalled, "stop was already called!");
        this.mStartCalled = false;
        this.mContext.unregisterReceiver(this.mEucNotificationReceiver);
    }

    public void display(final IEucQuery euc, final String lang, final IEucDisplayManager.IDisplayCallback callback) throws IllegalStateException {
        Preconditions.checkState(this.mStartCalled, START_NOT_CALLED_EXCEPTION_MESSAGE);
        this.mHandler.post(new Runnable() {
            final IEucData eucData;
            final EucMessageKey eucMessageKey = new EucMessageKey(this.eucData.getId(), this.eucData.getOwnIdentity(), this.eucType, this.eucData.getRemoteUri());
            final EucType eucType;
            final boolean hasPin = this.eucData.getPin();

            {
                IEucData eucData2 = euc.getEucData();
                this.eucData = eucData2;
                this.eucType = eucData2.getType();
            }

            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(EucDisplayManager.this.mContext, 16974546);
                View inputBoxWithLabel = LayoutInflater.from(EucDisplayManager.this.mContext).inflate(R.layout.euc_inputbox, (ViewGroup) null);
                final EditText input = (EditText) inputBoxWithLabel.findViewById(R.id.input);
                if (this.hasPin) {
                    input.setInputType(18);
                    inputBoxWithLabel.findViewById(R.id.pin_layout).setVisibility(0);
                    ((CheckBox) inputBoxWithLabel.findViewById(R.id.show_pin)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            } else {
                                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            }
                        }
                    });
                }
                builder.setView(inputBoxWithLabel);
                builder.setTitle(euc.getDialogData(lang).getSubject());
                String acceptLabel = euc.getDialogData(lang).getAcceptButton();
                if (acceptLabel == null) {
                    int i = AnonymousClass5.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[this.eucType.ordinal()];
                    if (i == 1 || i == 2) {
                        acceptLabel = EucDisplayManager.this.mContext.getResources().getString(R.string.dialog_text_rcs_config_accept);
                    } else if (i == 3 || i == 4) {
                        acceptLabel = EucDisplayManager.this.mContext.getResources().getString(R.string.dialog_text_rcs_config_ok);
                    } else {
                        throw new IllegalStateException("Unsupported euc type for display!");
                    }
                }
                String rejectLabel = euc.getDialogData(lang).getRejectButton();
                if (rejectLabel == null) {
                    rejectLabel = EucDisplayManager.this.mContext.getResources().getString(R.string.dialog_text_rcs_config_reject);
                }
                builder.setPositiveButton(acceptLabel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AnonymousClass1.this.onClickAction(EucResponseData.Response.ACCEPT, input);
                    }
                });
                if (this.eucType == EucType.PERSISTENT || this.eucType == EucType.VOLATILE) {
                    builder.setNegativeButton(rejectLabel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AnonymousClass1.this.onClickAction(EucResponseData.Response.DECLINE, input);
                        }
                    });
                }
                AlertDialog dialog = builder.create();
                if (this.hasPin) {
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        public void onShow(DialogInterface dialog) {
                            final Button acceptButton = ((AlertDialog) dialog).getButton(-1);
                            final Button rejectButton = ((AlertDialog) dialog).getButton(-2);
                            acceptButton.setEnabled(false);
                            rejectButton.setEnabled(false);
                            input.addTextChangedListener(new TextWatcher() {
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }

                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    String userInput = s.toString();
                                    acceptButton.setEnabled(!userInput.isEmpty());
                                    rejectButton.setEnabled(!userInput.isEmpty());
                                }

                                public void afterTextChanged(Editable s) {
                                }
                            });
                        }
                    });
                }
                Window window = dialog.getWindow();
                if (window != null) {
                    window.setType(2038);
                }
                dialog.setCancelable(false);
                EucDisplayManager.this.mDialogs.put(this.eucMessageKey, dialog);
                dialog.show();
                HyperlinkUtils.processLinks((TextView) inputBoxWithLabel.findViewById(R.id.message), euc.getDialogData(lang).getText(), new IHyperlinkOnClickListener() {
                    public void onClick(View view, Uri uri) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setData(uri);
                        intent.setFlags(LogClass.SIM_EVENT);
                        try {
                            EucDisplayManager.this.mContext.startActivity(intent);
                            if (EucDisplayManager.this.mDialogs.containsKey(AnonymousClass1.this.eucMessageKey)) {
                                ((AlertDialog) EucDisplayManager.this.mDialogs.get(AnonymousClass1.this.eucMessageKey)).dismiss();
                                EucDisplayManager.this.showEucNotification(euc.getDialogData(lang).getSubject(), euc.getDialogData(lang).getText(), AnonymousClass1.this.eucMessageKey);
                            }
                        } catch (ActivityNotFoundException activityNotFound) {
                            Log.e(EucDisplayManager.LOG_TAG, activityNotFound.getMessage());
                            Toast.makeText(EucDisplayManager.this.mContext, R.string.hyperlink_format_not_supported_exception, 0).show();
                        }
                    }
                });
            }

            /* access modifiers changed from: private */
            public void onClickAction(final EucResponseData.Response response, final EditText pinInput) {
                EucDisplayManager.this.mHandler.post(new Runnable() {
                    public void run() {
                        callback.onSuccess(response, AnonymousClass1.this.hasPin ? pinInput.getText().toString() : null);
                    }
                });
                EucDisplayManager.this.mDialogs.remove(this.eucMessageKey);
            }
        });
    }

    /* renamed from: com.sec.internal.ims.servicemodules.euc.dialog.EucDisplayManager$5  reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType;

        static {
            int[] iArr = new int[EucType.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType = iArr;
            try {
                iArr[EucType.PERSISTENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[EucType.VOLATILE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[EucType.ACKNOWLEDGEMENT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[EucType.NOTIFICATION.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public void hide(final EucMessageKey eucMessageKey) throws IllegalStateException {
        String str = LOG_TAG;
        Log.i(str, "hide: getEucId: " + eucMessageKey.getEucId());
        String str2 = LOG_TAG;
        IMSLog.s(str2, "hide: eucMessageKey: " + eucMessageKey);
        Preconditions.checkState(this.mStartCalled, START_NOT_CALLED_EXCEPTION_MESSAGE);
        this.mHandler.post(new Runnable() {
            public void run() {
                if (EucDisplayManager.this.mDialogs.containsKey(eucMessageKey)) {
                    ((AlertDialog) EucDisplayManager.this.mDialogs.get(eucMessageKey)).dismiss();
                    EucDisplayManager.this.mDialogs.remove(eucMessageKey);
                }
                EucDisplayManager.this.mNotificationManager.cancel(eucMessageKey.hashCode());
            }
        });
    }

    public void hideAllForType(final EucType type) throws IllegalStateException {
        String str = LOG_TAG;
        Log.i(str, "hideAllForType: type: " + type);
        Preconditions.checkState(this.mStartCalled, START_NOT_CALLED_EXCEPTION_MESSAGE);
        this.mHandler.post(new Runnable() {
            public void run() {
                Iterator<Map.Entry<EucMessageKey, AlertDialog>> it = EucDisplayManager.this.mDialogs.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<EucMessageKey, AlertDialog> entry = it.next();
                    if (type == entry.getKey().getEucType()) {
                        entry.getValue().dismiss();
                        EucDisplayManager.this.mNotificationManager.cancel(entry.getKey().hashCode());
                        it.remove();
                    }
                }
            }
        });
    }

    public void hideAllForOwnIdentity(final String ownIdentity) throws IllegalStateException {
        Log.i(LOG_TAG, "hideAllForOwnIdentity");
        String str = LOG_TAG;
        IMSLog.s(str, "hideAllForOwnIdentity: ownIdentity: " + ownIdentity);
        Preconditions.checkState(this.mStartCalled, START_NOT_CALLED_EXCEPTION_MESSAGE);
        this.mHandler.post(new Runnable() {
            public void run() {
                Iterator<Map.Entry<EucMessageKey, AlertDialog>> it = EucDisplayManager.this.mDialogs.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<EucMessageKey, AlertDialog> entry = it.next();
                    if (ownIdentity.equals(entry.getKey().getOwnIdentity())) {
                        entry.getValue().dismiss();
                        EucDisplayManager.this.mNotificationManager.cancel(entry.getKey().hashCode());
                        it.remove();
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void showEucNotification(String title, String message, EucMessageKey key) {
        String str = LOG_TAG;
        Log.i(str, "showEucNotification: title: " + title + ", message: " + message + ", getEucId: " + key.getEucId());
        String str2 = LOG_TAG;
        IMSLog.s(str2, "showEucNotification: title: " + title + ", message: " + message + ", key: " + key);
        String message2 = Html.fromHtml(message, 0).toString();
        Intent intent = new Intent(SHOW_EUC_DIALOG);
        intent.putExtra(EUC_KEY, key.marshall());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, key.hashCode(), intent, 134217728);
        String channelId = this.mContext.getResources().getString(R.string.app_name);
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(channelId, channelId, 2));
        Notification.Builder mBuilder = new Notification.Builder(this.mContext, channelId);
        mBuilder.setSmallIcon(R.drawable.stat_notify_rcs_service_avaliable);
        mBuilder.setContentTitle(title);
        mBuilder.setAutoCancel(false);
        mBuilder.setContentText(message2);
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setStyle(new Notification.BigTextStyle().bigText(message2));
        this.mNotificationManager.notify(key.hashCode(), mBuilder.build());
    }
}
