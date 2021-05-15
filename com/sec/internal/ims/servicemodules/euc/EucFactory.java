package com.sec.internal.ims.servicemodules.euc;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEuc;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class EucFactory implements IEucFactory {
    EucFactory() {
    }

    public Iterable<IEucQuery> combine(List<IEucData> eucDataList, List<IDialogData> dialogDataList) {
        Map<EucMessageKey, IEucQuery> keyMapping = new HashMap<>();
        for (IEucData eucData : eucDataList) {
            EUCQuery query = new EUCQuery(eucData);
            keyMapping.put(query.getEucData().getKey(), query);
        }
        for (IDialogData dialogData : dialogDataList) {
            IEucQuery query2 = keyMapping.get(dialogData.getKey());
            Preconditions.checkNotNull(query2, "Database Integrity Error");
            query2.addDialogData(dialogData);
        }
        return keyMapping.values();
    }

    public IEucQuery createEUC(IEucRequest euc) {
        EucType type;
        Long timeout;
        Preconditions.checkNotNull(euc);
        if (euc.getType() == IEucRequest.EucRequestType.PERSISTENT) {
            type = EucType.PERSISTENT;
        } else {
            type = EucType.VOLATILE;
        }
        if (euc.getTimeOut() == null) {
            timeout = null;
        } else {
            timeout = Long.valueOf(euc.getTimestamp() + (euc.getTimeOut().longValue() * 1000));
        }
        IEucData eucData = createEucData(euc, type, euc.isPinRequested(), euc.isExternal(), timeout);
        EUCQuery handle = new EUCQuery(eucData);
        for (Map.Entry<String, IEucRequest.IEucMessageData> entry : euc.getLanguageMapping().entrySet()) {
            handle.addDialogData(createDialogData(entry.getValue(), eucData.getKey(), entry.getKey()));
        }
        handle.addDialogData(createDialogData((IEucRequest.IEucMessageData) euc.getDefaultData(), eucData.getKey(), DeviceLocale.DEFAULT_LANG_VALUE));
        return handle;
    }

    public IEucQuery createEUC(IEucNotification euc) {
        Preconditions.checkNotNull(euc);
        IEucData eucData = createEucData(euc, EucType.NOTIFICATION, false, false, (Long) null);
        EUCQuery handle = new EUCQuery(eucData);
        for (Map.Entry<String, IEucNotification.IEucMessageData> entry : euc.getLanguageMapping().entrySet()) {
            handle.addDialogData(createDialogData(entry.getValue(), eucData.getKey(), entry.getKey()));
        }
        handle.addDialogData(createDialogData((IEucNotification.IEucMessageData) euc.getDefaultData(), eucData.getKey(), DeviceLocale.DEFAULT_LANG_VALUE));
        return handle;
    }

    public IEucQuery createEUC(IEucAcknowledgment euc) {
        Preconditions.checkNotNull(euc);
        IEucData eucData = createEucData(euc, EucType.ACKNOWLEDGEMENT, false, false, (Long) null);
        EUCQuery handle = new EUCQuery(eucData);
        for (Map.Entry<String, IEucAcknowledgment.IEUCMessageData> entry : euc.getLanguageMapping().entrySet()) {
            handle.addDialogData(createDialogData(entry.getValue(), eucData.getKey(), entry.getKey()));
        }
        if (euc.getDefaultData() != null) {
            handle.addDialogData(createDialogData(euc.getDefaultData(), eucData.getKey(), DeviceLocale.DEFAULT_LANG_VALUE));
        }
        return handle;
    }

    private <T> IEucData createEucData(IEuc<T> euc, EucType type, boolean isPinRequested, boolean isExternal, Long timeout) {
        return createEucData(new EucMessageKey(euc.getEucId(), euc.getOwnIdentity(), type, euc.getFromHeader()), isPinRequested, (String) null, isExternal, EucState.NONE, euc.getTimestamp(), timeout);
    }

    public IEucData createEucData(EucMessageKey eucMessageKey, boolean isPinRequested, String userPin, boolean isExternal, EucState state, long timestamp, Long timeout) {
        final EucMessageKey eucMessageKey2 = eucMessageKey;
        final boolean z = isPinRequested;
        final boolean z2 = isExternal;
        final EucState eucState = state;
        final long j = timestamp;
        final Long l = timeout;
        final String str = userPin;
        return new IEucData() {
            public EucMessageKey getKey() {
                return eucMessageKey2;
            }

            public String getId() {
                return eucMessageKey2.getEucId();
            }

            public boolean getPin() {
                return z;
            }

            public boolean getExternal() {
                return z2;
            }

            public EucState getState() {
                return eucState;
            }

            public EucType getType() {
                return eucMessageKey2.getEucType();
            }

            public ImsUri getRemoteUri() {
                return eucMessageKey2.getRemoteUri();
            }

            public String getOwnIdentity() {
                return eucMessageKey2.getOwnIdentity();
            }

            public long getTimestamp() {
                return j;
            }

            public Long getTimeOut() {
                return l;
            }

            public String getUserPin() {
                return str;
            }
        };
    }

    public IDialogData createDialogData(EucMessageKey eucMessageKey, String language, String subject, String text, String acceptButton, String rejectButton) {
        final EucMessageKey eucMessageKey2 = eucMessageKey;
        final String str = language;
        final String str2 = subject;
        final String str3 = text;
        final String str4 = acceptButton;
        final String str5 = rejectButton;
        return new IDialogData() {
            public EucMessageKey getKey() {
                return eucMessageKey2;
            }

            public String getLanguage() {
                return str;
            }

            public String getSubject() {
                return str2;
            }

            public String getText() {
                return str3;
            }

            public String getAcceptButton() {
                return str4;
            }

            public String getRejectButton() {
                return str5;
            }
        };
    }

    private IDialogData createDialogData(IEucRequest.IEucMessageData data, EucMessageKey eucMessageKey, String language) {
        return createDialogData(eucMessageKey, language, data.getSubject(), data.getText(), data.getAcceptButton(), data.getRejectButton());
    }

    private IDialogData createDialogData(IEucNotification.IEucMessageData data, EucMessageKey eucMessageKey, String language) {
        return createDialogData(eucMessageKey, language, data.getSubject(), data.getText(), data.getOkButton(), (String) null);
    }

    private IDialogData createDialogData(IEucAcknowledgment.IEUCMessageData data, EucMessageKey eucMessageKey, String language) {
        return createDialogData(eucMessageKey, language, data.getSubject(), data.getText(), (String) null, (String) null);
    }

    private static class EUCQuery implements IEucQuery {
        private final Map<String, IDialogData> mDialogMap = new HashMap();
        private IEucData mEUCData;

        EUCQuery(IEucData eucData) {
            this.mEUCData = eucData;
        }

        public void addDialogData(IDialogData dialog) {
            this.mDialogMap.put(dialog.getLanguage(), dialog);
        }

        public IEucData getEucData() {
            return this.mEUCData;
        }

        public IDialogData getDialogData(String lang) {
            IDialogData dialog = this.mDialogMap.get(lang);
            if (dialog == null) {
                return this.mDialogMap.get(DeviceLocale.DEFAULT_LANG_VALUE);
            }
            return dialog;
        }

        public boolean hasDialog(String lang) {
            return this.mDialogMap.containsKey(lang);
        }

        public Iterator<IDialogData> iterator() {
            return this.mDialogMap.values().iterator();
        }
    }
}
