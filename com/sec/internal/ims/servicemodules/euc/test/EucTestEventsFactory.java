package com.sec.internal.ims.servicemodules.euc.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.AckMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.BaseMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.EucContent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.NotificationMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.PersistentMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.RequestMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.SystemMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.TextLangPair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.VolatileMessage;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucSystemRequest;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.ims.translate.AcknowledgementMessageTranslator;
import com.sec.internal.ims.translate.EucTranslatorUtil;
import com.sec.internal.ims.translate.NotificationMessageTranslator;
import com.sec.internal.ims.translate.PersistentMessageTranslator;
import com.sec.internal.ims.translate.SystemRequestMessageTranslator;
import com.sec.internal.ims.translate.TypeTranslator;
import com.sec.internal.ims.translate.VolatileMessageTranslator;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EucTestEventsFactory implements IEucTestEventsFactory {
    private static final String LOG_MSG_INVALID_INTENT = "Invalid intent, ignoring! ";
    private static final String LOG_MSG_NO_EXTRAS = "Missing extras in the intent!";
    private static final String LOG_TAG = EucTestEventsFactory.class.getSimpleName();
    private final AcknowledgementMessageTranslator mAcknowledgementMessageTranslator = new AcknowledgementMessageTranslator();
    private final IEucFactory mEucFactory;
    private final NotificationMessageTranslator mNotificationMessageTranslator = new NotificationMessageTranslator();
    private final PersistentMessageTranslator mPersistentMessageTranslator = new PersistentMessageTranslator();
    private final SystemRequestMessageTranslator mSystemRequestMessageTranslator = new SystemRequestMessageTranslator();
    private final VolatileMessageTranslator mVolatileMessageTranslator = new VolatileMessageTranslator();

    public EucTestEventsFactory(IEucFactory factory) {
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(factory);
    }

    public IEucRequest createPersistent(Intent intent) {
        Log.d(LOG_TAG, "createPersistent");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras)) {
            logNoExtras();
            return null;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        builder.finish(PersistentMessage.createPersistentMessage(builder, buildRequestMessage(builder, extras)));
        return (IEucRequest) translateMessageToRequest(PersistentMessage.getRootAsPersistentMessage(builder.dataBuffer()), this.mPersistentMessageTranslator);
    }

    public IEucRequest createVolatile(Intent intent) {
        Log.d(LOG_TAG, "createVolatile");
        Bundle extras = intent.getExtras();
        if (extras == null) {
            logNoExtras();
            return null;
        }
        long timeout = extras.getLong(EucTestIntent.Extras.TIMEOUT);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        builder.finish(VolatileMessage.createVolatileMessage(builder, buildRequestMessage(builder, extras), timeout));
        return (IEucRequest) translateMessageToRequest(VolatileMessage.getRootAsVolatileMessage(builder.dataBuffer()), this.mVolatileMessageTranslator);
    }

    public IEucAcknowledgment createAcknowledgement(Intent intent) {
        Log.d(LOG_TAG, "createAcknowledgement");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras)) {
            logNoExtras();
            return null;
        }
        String status = extras.getString(EucTestIntent.Extras.ACK_STATUS);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int baseMessageOffset = buildBaseMessage(builder, extras);
        int contentOffset = buildEucContent(builder, extras);
        int ackStatus = 1;
        if (EucTestIntent.Extras.ACK_STATUS_OK.equals(status)) {
            Log.d(LOG_TAG, "createAcknowledgement, status ok");
            ackStatus = 0;
        } else if ("error".equals(status)) {
            Log.d(LOG_TAG, "createAcknowledgement, status error");
        } else {
            Log.d(LOG_TAG, "createAcknowledgement, unrecognized status, assuming error!");
        }
        builder.finish(AckMessage.createAckMessage(builder, baseMessageOffset, contentOffset, ackStatus));
        return (IEucAcknowledgment) translateMessageToRequest(AckMessage.getRootAsAckMessage(builder.dataBuffer()), this.mAcknowledgementMessageTranslator);
    }

    public IEucNotification createNotification(Intent intent) {
        Log.d(LOG_TAG, "createNotification");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras)) {
            logNoExtras();
            return null;
        }
        List<String> okButton = new ArrayList<>();
        if (extras.containsKey(EucTestIntent.Extras.OK_BUTTON_LIST)) {
            okButton = getArrayList(extras, EucTestIntent.Extras.OK_BUTTON_LIST);
        }
        List<String> okButtonLang = new ArrayList<>();
        if (extras.containsKey(EucTestIntent.Extras.OK_BUTTON_LANG_LIST)) {
            okButtonLang = getArrayList(extras, EucTestIntent.Extras.OK_BUTTON_LANG_LIST);
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int baseOffset = buildBaseMessage(builder, extras);
        int contentOffset = buildEucContent(builder, extras);
        int[] okButtonOffsets = new int[0];
        if (!okButton.isEmpty()) {
            Log.d(LOG_TAG, "createNotification, okButtons");
            okButtonOffsets = buildTextLangPairList(builder, okButton, okButtonLang);
        }
        builder.finish(NotificationMessage.createNotificationMessage(builder, baseOffset, contentOffset, NotificationMessage.createOkButtonsVector(builder, okButtonOffsets)));
        return (IEucNotification) translateMessageToRequest(NotificationMessage.getRootAsNotificationMessage(builder.dataBuffer()), this.mNotificationMessageTranslator);
    }

    public IEucSystemRequest createSystemRequest(Intent intent) {
        Log.d(LOG_TAG, "createSystemRequest");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras) || checkNoSystemRequestExtras(extras)) {
            logNoExtras();
            return null;
        }
        String type = extras.getString(EucTestIntent.Extras.SYSTEM_TYPE);
        String data = extras.getString(EucTestIntent.Extras.SYSTEM_DATA);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        builder.finish(SystemMessage.createSystemMessage(builder, buildBaseMessage(builder, extras), builder.createString((CharSequence) makeStrNotNull(type)), builder.createString((CharSequence) makeStrNotNull(data))));
        return (IEucSystemRequest) translateMessageToRequest(SystemMessage.getRootAsSystemMessage(builder.dataBuffer()), this.mSystemRequestMessageTranslator);
    }

    public AutoconfUserConsentData createUserConsent(Intent intent) {
        Log.d(LOG_TAG, "createUserConsent");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoUserConsentExtras(extras)) {
            logNoExtras();
            return null;
        }
        String title = extras.getString(EucTestIntent.Extras.TITLE);
        String message = extras.getString("message");
        boolean userAccept = extras.getBoolean(EucTestIntent.Extras.USER_ACCEPT);
        return new AutoconfUserConsentData(getTimestamp(extras), userAccept, title, message, extras.getString(EucTestIntent.Extras.SUBSCRIBER_IDENTITY));
    }

    private int buildBaseMessage(FlatBufferBuilder builder, Bundle extras) {
        int handle = extras.getInt(EucTestIntent.Extras.HANDLE);
        return BaseMessage.createBaseMessage(builder, (long) handle, builder.createString((CharSequence) extras.getString("id")), builder.createString((CharSequence) extras.getString("remote_uri")), getTimestamp(extras));
    }

    private int buildEucContent(FlatBufferBuilder builder, Bundle extras) {
        List<String> text = getArrayList(extras, EucTestIntent.Extras.TEXT_LIST);
        List<String> textLang = new ArrayList<>();
        if (extras.containsKey(EucTestIntent.Extras.TEXT_LANG_LIST)) {
            textLang = getArrayList(extras, EucTestIntent.Extras.TEXT_LANG_LIST);
        }
        List<String> subject = getArrayList(extras, EucTestIntent.Extras.SUBJECT_LIST);
        List<String> subjectLang = new ArrayList<>();
        if (extras.containsKey(EucTestIntent.Extras.SUBJECT_LANG_LIST)) {
            subjectLang = getArrayList(extras, EucTestIntent.Extras.SUBJECT_LANG_LIST);
        }
        Log.d(LOG_TAG, "buildEucContent, texts");
        int[] textOffsets = buildTextLangPairList(builder, text, textLang);
        Log.d(LOG_TAG, "buildEucContent, subjects");
        return EucContent.createEucContent(builder, EucContent.createTextsVector(builder, textOffsets), EucContent.createSubjectsVector(builder, buildTextLangPairList(builder, subject, subjectLang)));
    }

    private int[] buildTextLangPairList(FlatBufferBuilder builder, List<String> text, List<String> lang) {
        boolean z = false;
        if (lang.isEmpty()) {
            Preconditions.checkArgument(text.size() == 1, "If more than one element is presented a language (lang) attribute must be present with the two letter language codes according to the ISO 639-1");
            int textOffset = builder.createString((CharSequence) makeStrNotNull(text.get(0)));
            TextLangPair.startTextLangPair(builder);
            TextLangPair.addText(builder, textOffset);
            return new int[]{TextLangPair.endTextLangPair(builder)};
        }
        if (text.size() == lang.size()) {
            z = true;
        }
        Preconditions.checkArgument(z, "Text and language size does not match");
        int[] result = new int[text.size()];
        for (int i = 0; i < text.size(); i++) {
            result[i] = TextLangPair.createTextLangPair(builder, builder.createString((CharSequence) makeStrNotNull(text.get(i))), builder.createString((CharSequence) makeStrNotNull(lang.get(i))));
        }
        return result;
    }

    private int buildRequestMessage(FlatBufferBuilder builder, Bundle extras) {
        List<String> acceptButton;
        List<String> acceptButtonLang;
        List<String> rejectButton;
        List<String> rejectButtonLang;
        int[] acceptButtonOffsets;
        int[] rejectButtonOffsets;
        FlatBufferBuilder flatBufferBuilder = builder;
        Bundle bundle = extras;
        Log.d(LOG_TAG, "buildRequestMessage");
        boolean pin = bundle.getBoolean(EucTestIntent.Extras.PIN_INDICATION);
        boolean externalEucr = bundle.getBoolean(EucTestIntent.Extras.EXTERNAL_EUCR);
        List<String> acceptButton2 = new ArrayList<>();
        if (bundle.containsKey(EucTestIntent.Extras.ACCEPT_BUTTON_LIST)) {
            acceptButton = getArrayList(bundle, EucTestIntent.Extras.ACCEPT_BUTTON_LIST);
        } else {
            acceptButton = acceptButton2;
        }
        List<String> acceptButtonLang2 = new ArrayList<>();
        if (bundle.containsKey(EucTestIntent.Extras.ACCEPT_BUTTON_LANG_LIST)) {
            acceptButtonLang = getArrayList(bundle, EucTestIntent.Extras.ACCEPT_BUTTON_LANG_LIST);
        } else {
            acceptButtonLang = acceptButtonLang2;
        }
        List<String> rejectButton2 = new ArrayList<>();
        if (bundle.containsKey(EucTestIntent.Extras.REJECT_BUTTON_LIST)) {
            rejectButton = getArrayList(bundle, EucTestIntent.Extras.REJECT_BUTTON_LIST);
        } else {
            rejectButton = rejectButton2;
        }
        List<String> rejectButtonLang2 = new ArrayList<>();
        if (bundle.containsKey(EucTestIntent.Extras.REJECT_BUTTON_LANG_LIST)) {
            rejectButtonLang = getArrayList(bundle, EucTestIntent.Extras.REJECT_BUTTON_LANG_LIST);
        } else {
            rejectButtonLang = rejectButtonLang2;
        }
        int baseMessageOffset = buildBaseMessage(builder, extras);
        int contentOffset = buildEucContent(builder, extras);
        int[] acceptButtonOffsets2 = new int[0];
        if (!acceptButton.isEmpty()) {
            Log.d(LOG_TAG, "buildRequestMessage, acceptButtons");
            acceptButtonOffsets = buildTextLangPairList(flatBufferBuilder, acceptButton, acceptButtonLang);
        } else {
            acceptButtonOffsets = acceptButtonOffsets2;
        }
        int acceptButtonsVectorOffset = RequestMessage.createAcceptButtonsVector(flatBufferBuilder, acceptButtonOffsets);
        int[] rejectButtonOffsets2 = new int[0];
        if (!rejectButton.isEmpty()) {
            Log.d(LOG_TAG, "buildRequestMessage, rejectButtons");
            rejectButtonOffsets = buildTextLangPairList(flatBufferBuilder, rejectButton, rejectButtonLang);
        } else {
            rejectButtonOffsets = rejectButtonOffsets2;
        }
        int[] iArr = rejectButtonOffsets;
        int[] iArr2 = acceptButtonOffsets;
        return RequestMessage.createRequestMessage(builder, baseMessageOffset, contentOffset, acceptButtonsVectorOffset, RequestMessage.createRejectButtonsVector(flatBufferBuilder, rejectButtonOffsets), pin, externalEucr);
    }

    public IEucData createEucData(Intent intent) {
        Log.d(LOG_TAG, "createEucData");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoEucDataExtras(extras)) {
            logNoExtras();
            return null;
        }
        try {
            String ownIdentity = EucTranslatorUtil.getOwnIdentity(extras.getInt(EucTestIntent.Extras.HANDLE));
            ImsUri remoteUri = ImsUri.parse(extras.getString("remote_uri"));
            if (remoteUri == null) {
                return null;
            }
            String id = extras.getString("id");
            long timeout = extras.getLong(EucTestIntent.Extras.TIMEOUT);
            EucMessageKey eucMessageKey = new EucMessageKey(id, ownIdentity, timeout == 0 ? EucType.PERSISTENT : EucType.VOLATILE, remoteUri);
            String pin = extras.getString(EucTestIntent.Extras.USER_PIN);
            return this.mEucFactory.createEucData(eucMessageKey, pin != null, pin, extras.getBoolean(EucTestIntent.Extras.EXTERNAL_EUCR), extras.getBoolean(EucTestIntent.Extras.USER_ACCEPT) ? EucState.ACCEPTED_NOT_SENT : EucState.REJECTED_NOT_SENT, getTimestamp(extras), Long.valueOf(timeout));
        } catch (TranslationException e) {
            TranslationException translationException = e;
            return null;
        }
    }

    private long getTimestamp(Bundle extras) {
        long timestampFromExtras = extras.getLong("timestamp");
        return timestampFromExtras == 0 ? System.currentTimeMillis() : timestampFromExtras;
    }

    private List<String> getArrayList(Bundle extras, String key) {
        String[] array;
        Preconditions.checkNotNull(extras, "extras is null");
        Preconditions.checkNotNull(key, "key is null");
        List<String> list = extras.getStringArrayList(key);
        if (list == null && (array = extras.getStringArray(key)) != null) {
            list = Arrays.asList(array);
        }
        return list != null ? list : Collections.emptyList();
    }

    private <T, S> S translateMessageToRequest(T message, TypeTranslator<T, S> translator) {
        try {
            return translator.translate(message);
        } catch (TranslationException e) {
            logInvalidIntent(e);
            return null;
        }
    }

    private void logInvalidIntent(TranslationException e) {
        String str = LOG_TAG;
        Log.e(str, LOG_MSG_INVALID_INTENT + e.getMessage());
    }

    private void logNoExtras() {
        Log.e(LOG_TAG, "Invalid intent, ignoring! Missing extras in the intent!");
    }

    private boolean checkNoBaseExtras(Bundle extras) {
        return !extras.containsKey(EucTestIntent.Extras.HANDLE) || !extras.containsKey("id") || !extras.containsKey("remote_uri");
    }

    private boolean checkNoSystemRequestExtras(Bundle extras) {
        return !extras.containsKey(EucTestIntent.Extras.SYSTEM_TYPE) || !extras.containsKey(EucTestIntent.Extras.SYSTEM_DATA);
    }

    private boolean checkNoUserConsentExtras(Bundle extras) {
        return !extras.containsKey(EucTestIntent.Extras.TITLE) || !extras.containsKey("message") || !extras.containsKey(EucTestIntent.Extras.SUBSCRIBER_IDENTITY);
    }

    private boolean checkNoEucDataExtras(Bundle extras) {
        return !extras.containsKey(EucTestIntent.Extras.HANDLE) || !extras.containsKey("id") || !extras.containsKey("remote_uri");
    }

    private String makeStrNotNull(String str) {
        return str != null ? str : "";
    }
}
