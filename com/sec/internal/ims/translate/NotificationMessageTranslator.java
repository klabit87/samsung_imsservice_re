package com.sec.internal.ims.translate;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.EucContent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.NotificationMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.TextLangPair;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.util.ImsUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NotificationMessageTranslator implements TypeTranslator<NotificationMessage, IEucNotification> {
    public IEucNotification translate(NotificationMessage value) throws TranslationException {
        if (value == null || value.base() == null || value.content() == null) {
            throw new TranslationException("NotificationMessageTranslator, incomplete or null message data!");
        }
        EucMessageDataCollector collector = new EucMessageDataCollector();
        ImsUri fromHeader = ImsUri.parse(value.base().remoteUri());
        String ownIdentity = EucTranslatorUtil.getOwnIdentity(ImsUtil.getHandle(value.base().handle()));
        EucContent content = value.content();
        int i = 0;
        while (true) {
            boolean z = false;
            if (i >= content.textsLength()) {
                break;
            }
            TextLangPair text = content.texts(i);
            if (text != null) {
                String text2 = text.text();
                String lang = text.lang();
                if (content.textsLength() == 1) {
                    z = true;
                }
                EucTranslatorUtil.checkTextLangPair(text2, lang, z);
                collector.addText(text.lang(), text.text());
            }
            i++;
        }
        for (int i2 = 0; i2 < content.subjectsLength(); i2++) {
            TextLangPair subject = content.subjects(i2);
            if (subject != null) {
                EucTranslatorUtil.checkTextLangPair(subject.text(), subject.lang(), content.subjectsLength() == 1);
                collector.addSubject(subject.lang(), subject.text());
            }
        }
        for (int i3 = 0; i3 < value.okButtonsLength(); i3++) {
            TextLangPair okButton = value.okButtons(i3);
            if (okButton != null) {
                EucTranslatorUtil.checkTextLangPair(okButton.text(), okButton.lang(), value.okButtonsLength() == 1);
                collector.addOkButton(okButton.lang(), okButton.text());
            }
        }
        collector.prepareMessageData();
        if (!collector.getAllElements().isEmpty()) {
            final EucMessageDataCollector eucMessageDataCollector = collector;
            final NotificationMessage notificationMessage = value;
            final ImsUri imsUri = fromHeader;
            final String str = ownIdentity;
            return new IEucNotification() {
                public Map<String, IEucNotification.IEucMessageData> getLanguageMapping() {
                    return eucMessageDataCollector.getAllElements();
                }

                public IEucNotification.IEucMessageData getDefaultData() {
                    return eucMessageDataCollector.getDefaultElement();
                }

                public String getEucId() {
                    if (notificationMessage.base() != null) {
                        return notificationMessage.base().id();
                    }
                    return null;
                }

                public ImsUri getFromHeader() {
                    return imsUri;
                }

                public String getOwnIdentity() {
                    return str;
                }

                public long getTimestamp() {
                    if (notificationMessage.base() != null) {
                        return notificationMessage.base().timestamp();
                    }
                    return 0;
                }
            };
        }
        throw new TranslationException("NotificationMessageTranslator, failed to create EucMessageData objects, missing required fields in received EUC message!");
    }

    private class EucMessageDataCollector {
        private IEucNotification.IEucMessageData mDefault;
        private final Map<String, IEucNotification.IEucMessageData> mElements;
        private Set<String> mLanguages;
        private Map<String, String> mOkButtons;
        private Map<String, String> mSubjects;
        private Map<String, String> mTexts;

        private EucMessageDataCollector() {
            this.mLanguages = new LinkedHashSet();
            this.mTexts = new LinkedHashMap();
            this.mSubjects = new LinkedHashMap();
            this.mOkButtons = new LinkedHashMap();
            this.mElements = new HashMap();
            this.mDefault = null;
        }

        /* access modifiers changed from: package-private */
        public Map<String, IEucNotification.IEucMessageData> getAllElements() {
            return this.mElements;
        }

        /* access modifiers changed from: package-private */
        public IEucNotification.IEucMessageData getDefaultElement() {
            return this.mDefault;
        }

        /* access modifiers changed from: package-private */
        public void addText(String lang, String text) {
            this.mTexts.put(EucTranslatorUtil.addLanguage(lang, this.mLanguages), text);
        }

        /* access modifiers changed from: package-private */
        public void addSubject(String lang, String text) {
            this.mSubjects.put(EucTranslatorUtil.addLanguage(lang, this.mLanguages), text);
        }

        /* access modifiers changed from: package-private */
        public void addOkButton(String lang, String text) {
            this.mOkButtons.put(EucTranslatorUtil.addLanguage(lang, this.mLanguages), text);
        }

        /* access modifiers changed from: package-private */
        public void prepareMessageData() {
            for (String language : this.mLanguages) {
                add(language, createEucMessageData(EucTranslatorUtil.getValue(language, this.mTexts), EucTranslatorUtil.getValue(language, this.mSubjects), EucTranslatorUtil.nullIfEmpty(this.mOkButtons.get(language))));
            }
            releaseTemporaryData();
        }

        private void releaseTemporaryData() {
            this.mLanguages = null;
            this.mTexts = null;
            this.mSubjects = null;
            this.mOkButtons = null;
        }

        private void add(String lang, IEucNotification.IEucMessageData element) {
            this.mElements.put(lang, element);
            if (this.mDefault == null) {
                this.mDefault = element;
            }
        }

        private IEucNotification.IEucMessageData createEucMessageData(final String text, final String subject, final String okButton) {
            return new IEucNotification.IEucMessageData() {
                public String getText() {
                    return text;
                }

                public String getSubject() {
                    return subject;
                }

                public String getOkButton() {
                    return okButton;
                }
            };
        }
    }
}
