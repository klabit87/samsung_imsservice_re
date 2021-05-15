package com.sec.internal.ims.translate;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.AckMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.EucContent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.TextLangPair;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.util.ImsUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AcknowledgementMessageTranslator implements TypeTranslator<AckMessage, IEucAcknowledgment> {
    public IEucAcknowledgment translate(AckMessage value) throws TranslationException {
        if (value == null || value.base() == null || value.content() == null) {
            throw new TranslationException("AcknowledgementMessageTranslator, incomplete or null message data!");
        }
        EucMessageDataCollector collector = new EucMessageDataCollector();
        ImsUri fromHeader = ImsUri.parse(value.base().remoteUri());
        String ownIdentity = EucTranslatorUtil.getOwnIdentity(ImsUtil.getHandle(value.base().handle()));
        EucContent eucContent = value.content();
        int i = 0;
        while (true) {
            boolean z = false;
            if (i >= eucContent.textsLength()) {
                break;
            }
            TextLangPair text = eucContent.texts(i);
            if (text != null) {
                String text2 = text.text();
                String lang = text.lang();
                if (eucContent.textsLength() == 1) {
                    z = true;
                }
                EucTranslatorUtil.checkTextLangPair(text2, lang, z);
                collector.addText(text.lang(), text.text());
            }
            i++;
        }
        for (int i2 = 0; i2 < eucContent.subjectsLength(); i2++) {
            TextLangPair subject = eucContent.subjects(i2);
            if (subject != null) {
                EucTranslatorUtil.checkTextLangPair(subject.text(), subject.lang(), eucContent.subjectsLength() == 1);
                collector.addSubject(subject.lang(), subject.text());
            }
        }
        collector.prepareMessageData();
        if (!collector.getAllElements().isEmpty()) {
            final EucMessageDataCollector eucMessageDataCollector = collector;
            final AckMessage ackMessage = value;
            final ImsUri imsUri = fromHeader;
            final String str = ownIdentity;
            return new IEucAcknowledgment() {
                public Map<String, IEucAcknowledgment.IEUCMessageData> getLanguageMapping() {
                    return eucMessageDataCollector.getAllElements();
                }

                public IEucAcknowledgment.IEUCMessageData getDefaultData() {
                    return eucMessageDataCollector.getDefaultElement();
                }

                public String getEucId() {
                    return ackMessage.base().id();
                }

                public ImsUri getFromHeader() {
                    return imsUri;
                }

                public String getOwnIdentity() {
                    return str;
                }

                public long getTimestamp() {
                    return ackMessage.base().timestamp();
                }
            };
        }
        throw new TranslationException("AcknowledgementMessageTranslator, failed to create EucMessageData objects, missing required fields in received EUC message!");
    }

    private class EucMessageDataCollector {
        private IEucAcknowledgment.IEUCMessageData mDefault;
        private final Map<String, IEucAcknowledgment.IEUCMessageData> mElements;
        private Set<String> mLanguages;
        private Map<String, String> mSubjects;
        private Map<String, String> mTexts;

        private EucMessageDataCollector() {
            this.mLanguages = new LinkedHashSet();
            this.mTexts = new LinkedHashMap();
            this.mSubjects = new LinkedHashMap();
            this.mElements = new HashMap();
            this.mDefault = null;
        }

        /* access modifiers changed from: package-private */
        public Map<String, IEucAcknowledgment.IEUCMessageData> getAllElements() {
            return this.mElements;
        }

        /* access modifiers changed from: package-private */
        public IEucAcknowledgment.IEUCMessageData getDefaultElement() {
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
        public void prepareMessageData() {
            for (String language : this.mLanguages) {
                add(language, createEucMessageData(EucTranslatorUtil.getValue(language, this.mTexts), EucTranslatorUtil.getValue(language, this.mSubjects)));
            }
            releaseTemporaryData();
        }

        private void releaseTemporaryData() {
            this.mLanguages = null;
            this.mTexts = null;
            this.mSubjects = null;
        }

        private void add(String lang, IEucAcknowledgment.IEUCMessageData element) {
            this.mElements.put(lang, element);
            if (this.mDefault == null) {
                this.mDefault = element;
            }
        }

        private IEucAcknowledgment.IEUCMessageData createEucMessageData(final String text, final String subject) {
            return new IEucAcknowledgment.IEUCMessageData() {
                public String getText() {
                    return text;
                }

                public String getSubject() {
                    return subject;
                }
            };
        }
    }
}
