package com.sec.internal.ims.translate;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.translate.MapTranslator;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.SystemMessage;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucSystemRequest;
import com.sec.internal.ims.util.ImsUtil;
import java.util.HashMap;
import java.util.Map;

public class SystemRequestMessageTranslator implements TypeTranslator<SystemMessage, IEucSystemRequest> {
    private final MapTranslator<String, IEucSystemRequest.EucSystemRequestType> mEUCSystemRequestTypeTranslator;

    public SystemRequestMessageTranslator() {
        Map<String, IEucSystemRequest.EucSystemRequestType> translatorMap = new HashMap<>();
        translatorMap.put("urn:gsma:rcs:http-configuration:reconfigure", IEucSystemRequest.EucSystemRequestType.RECONFIGURE);
        this.mEUCSystemRequestTypeTranslator = new MapTranslator<>(translatorMap);
    }

    public IEucSystemRequest translate(SystemMessage value) throws TranslationException {
        Preconditions.checkNotNull(value);
        Preconditions.checkNotNull(value.base());
        IEucSystemRequest.IEUCMessageData defaultData = getData(value);
        IEucSystemRequest.IEUCMessageData optionalData = getDataAsOptional(value);
        ImsUri fromHeader = ImsUri.parse(value.base().remoteUri());
        final IEucSystemRequest.IEUCMessageData iEUCMessageData = defaultData;
        final SystemMessage systemMessage = value;
        final ImsUri imsUri = fromHeader;
        final String ownIdentity = EucTranslatorUtil.getOwnIdentity(ImsUtil.getHandle(value.base().handle()));
        final IEucSystemRequest.EucSystemRequestType translate = this.mEUCSystemRequestTypeTranslator.translate(value.type());
        final IEucSystemRequest.IEUCMessageData iEUCMessageData2 = optionalData;
        return new IEucSystemRequest() {
            public Map<String, IEucSystemRequest.IEUCMessageData> getLanguageMapping() {
                return new HashMap();
            }

            public IEucSystemRequest.IEUCMessageData getDefaultData() {
                return iEUCMessageData;
            }

            public String getEucId() {
                Preconditions.checkNotNull(systemMessage);
                Preconditions.checkNotNull(systemMessage.base());
                return systemMessage.base().id();
            }

            public ImsUri getFromHeader() {
                return imsUri;
            }

            public String getOwnIdentity() {
                return ownIdentity;
            }

            public long getTimestamp() {
                Preconditions.checkNotNull(systemMessage);
                Preconditions.checkNotNull(systemMessage.base());
                return systemMessage.base().timestamp();
            }

            public IEucSystemRequest.EucSystemRequestType getType() {
                return translate;
            }

            public IEucSystemRequest.IEUCMessageData getMessageData() {
                return iEUCMessageData2;
            }
        };
    }

    private IEucSystemRequest.IEUCMessageData getDataAsOptional(SystemMessage value) {
        if (value.data() != null) {
            return getData(value);
        }
        return null;
    }

    private IEucSystemRequest.IEUCMessageData getData(final SystemMessage value) {
        return new IEucSystemRequest.IEUCMessageData() {
            public String getData() {
                return value.data();
            }
        };
    }
}
