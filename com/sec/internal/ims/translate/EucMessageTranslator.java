package com.sec.internal.ims.translate;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.EucContent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.RequestMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.TextLangPair;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.util.ImsUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

class EucMessageTranslator {
    EucMessageTranslator() {
    }

    /* access modifiers changed from: protected */
    public IEucRequest translate(RequestMessage value, Long timeout, IEucRequest.EucRequestType type) throws TranslationException {
        RequestMessage requestMessage = value;
        if (requestMessage == null || value.base() == null || value.content() == null) {
            throw new TranslationException("EucMessageTranslator, incomplete or null message data!");
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
        for (int i3 = 0; i3 < value.acceptButtonsLength(); i3++) {
            TextLangPair accept = requestMessage.acceptButtons(i3);
            if (accept != null) {
                EucTranslatorUtil.checkTextLangPair(accept.text(), accept.lang(), value.acceptButtonsLength() == 1);
                collector.addAcceptButton(accept.lang(), accept.text());
            }
        }
        for (int i4 = 0; i4 < value.rejectButtonsLength(); i4++) {
            TextLangPair reject = requestMessage.rejectButtons(i4);
            if (reject != null) {
                EucTranslatorUtil.checkTextLangPair(reject.text(), reject.lang(), value.rejectButtonsLength() == 1);
                collector.addRejectButton(reject.lang(), reject.text());
            }
        }
        collector.prepareMessageData();
        if (!collector.getAllElements().isEmpty()) {
            final EucMessageDataCollector eucMessageDataCollector = collector;
            final RequestMessage requestMessage2 = value;
            final Long l = timeout;
            final ImsUri imsUri = fromHeader;
            final String str = ownIdentity;
            final IEucRequest.EucRequestType eucRequestType = type;
            return new IEucRequest() {
                public Map<String, IEucRequest.IEucMessageData> getLanguageMapping() {
                    return eucMessageDataCollector.getAllElements();
                }

                public IEucRequest.IEucMessageData getDefaultData() {
                    return eucMessageDataCollector.getDefaultElement();
                }

                public String getEucId() {
                    if (requestMessage2.base() != null) {
                        return requestMessage2.base().id();
                    }
                    return null;
                }

                public boolean isPinRequested() {
                    return requestMessage2.pin();
                }

                public boolean isExternal() {
                    return requestMessage2.externalEucr();
                }

                public Long getTimeOut() {
                    return l;
                }

                public ImsUri getFromHeader() {
                    return imsUri;
                }

                public String getOwnIdentity() {
                    return str;
                }

                public long getTimestamp() {
                    if (requestMessage2.base() != null) {
                        return requestMessage2.base().timestamp();
                    }
                    return 0;
                }

                public IEucRequest.EucRequestType getType() {
                    return eucRequestType;
                }
            };
        }
        throw new TranslationException("EucMessageTranslator, failed to create EucMessageData objects, missing required fields in received EUC message!");
    }

    private class EucMessageDataCollector {
        private Map<String, String> mAcceptButtons;
        private IEucRequest.IEucMessageData mDefault;
        private final Map<String, IEucRequest.IEucMessageData> mElements;
        private Set<String> mLanguages;
        private Map<String, String> mRejectButtons;
        private Map<String, String> mSubjects;
        private Map<String, String> mTexts;

        private EucMessageDataCollector() {
            this.mLanguages = new LinkedHashSet();
            this.mTexts = new LinkedHashMap();
            this.mSubjects = new LinkedHashMap();
            this.mAcceptButtons = new LinkedHashMap();
            this.mRejectButtons = new LinkedHashMap();
            this.mElements = new HashMap();
            this.mDefault = null;
        }

        /* access modifiers changed from: package-private */
        public Map<String, IEucRequest.IEucMessageData> getAllElements() {
            return this.mElements;
        }

        /* access modifiers changed from: package-private */
        public IEucRequest.IEucMessageData getDefaultElement() {
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
        public void addAcceptButton(String lang, String text) {
            this.mAcceptButtons.put(EucTranslatorUtil.addLanguage(lang, this.mLanguages), text);
        }

        /* access modifiers changed from: package-private */
        public void addRejectButton(String lang, String text) {
            this.mRejectButtons.put(EucTranslatorUtil.addLanguage(lang, this.mLanguages), text);
        }

        /* access modifiers changed from: package-private */
        public void prepareMessageData() {
            for (String language : this.mLanguages) {
                add(language, createEucMessageData(EucTranslatorUtil.getValue(language, this.mTexts), EucTranslatorUtil.getValue(language, this.mSubjects), EucTranslatorUtil.nullIfEmpty(this.mAcceptButtons.get(language)), EucTranslatorUtil.nullIfEmpty(this.mRejectButtons.get(language))));
            }
            releaseTemporaryData();
        }

        private void releaseTemporaryData() {
            this.mLanguages = null;
            this.mTexts = null;
            this.mSubjects = null;
            this.mAcceptButtons = null;
            this.mRejectButtons = null;
        }

        private void add(String lang, IEucRequest.IEucMessageData element) {
            this.mElements.put(lang, element);
            if (this.mDefault == null) {
                this.mDefault = element;
            }
        }

        private IEucRequest.IEucMessageData createEucMessageData(String text, String subject, String acceptButton, String rejectButton) {
            final String str = text;
            final String str2 = subject;
            final String str3 = rejectButton;
            final String str4 = acceptButton;
            return new IEucRequest.IEucMessageData() {
                public String getText() {
                    return str;
                }

                public String getSubject() {
                    return str2;
                }

                public String getRejectButton() {
                    return str3;
                }

                public String getAcceptButton() {
                    return str4;
                }
            };
        }
    }
}
