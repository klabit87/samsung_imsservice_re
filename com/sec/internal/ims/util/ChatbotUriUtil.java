package com.sec.internal.ims.util;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import gov.nist.javax.sip.address.SipUri;
import java.util.Collection;
import java.util.Collections;

public class ChatbotUriUtil {
    private static final String LOG_TAG = ChatbotUriUtil.class.getSimpleName();

    private ChatbotUriUtil() {
    }

    public static boolean isChatbotUri(ImsUri uri, int phoneId) {
        return hasChatbotUri(Collections.singleton(uri), phoneId);
    }

    public static boolean hasChatbotUri(Collection<ImsUri> uris, int phoneId) {
        Capabilities capabilities;
        if (uris == null) {
            return false;
        }
        ICapabilityDiscoveryModule capModule = null;
        if (ImsRegistry.isReady()) {
            capModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        }
        for (ImsUri uri : uris) {
            if (hasUriBotPlatform(uri) || hasChatbotRoleSession(uri) || isKnownBotServiceId(uri)) {
                return true;
            }
            if (capModule != null && (capabilities = capModule.getCapabilities(uri, CapabilityRefreshType.DISABLED, phoneId)) != null && capabilities.hasFeature(Capabilities.FEATURE_CHATBOT_ROLE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasChatbotUri(Collection<ImsUri> uris) {
        return hasChatbotUri(uris, SimUtil.getSimSlotPriority());
    }

    public static boolean hasUriBotPlatform(ImsUri uri) {
        if (uri == null) {
            return false;
        }
        Boolean isBotServiceId = false;
        Boolean isSipUriType = Boolean.valueOf(uri.getUriType() == ImsUri.UriType.SIP_URI);
        String botServiceIdPrefixList = ImsRegistry.getString(SimUtil.getSimSlotPriority(), GlobalSettingsConstants.RCS.BOT_SERVICE_ID_PREFIX_LIST, "");
        if (!TextUtils.isEmpty(botServiceIdPrefixList)) {
            String[] split = botServiceIdPrefixList.split(",");
            int length = split.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String idPrefix = split[i];
                if (isSipUriType.booleanValue() && uri.getHost().contains(idPrefix)) {
                    isBotServiceId = true;
                    break;
                }
                i++;
            }
        }
        if (!isSipUriType.booleanValue() || TextUtils.isEmpty(uri.getHost()) || !isBotServiceId.booleanValue()) {
            return false;
        }
        return true;
    }

    public static boolean hasChatbotRoleSession(ImsUri uri) {
        return ImCache.getInstance().isChatbotRoleUri(uri);
    }

    public static boolean isKnownBotServiceId(ImsUri uri) {
        return uri != null && BotServiceIdTranslator.getInstance().contains(uri.toString()).booleanValue();
    }

    public static void removeUriParameters(ImsUri imsUri) {
        if (imsUri != null) {
            SipUri uri = imsUri.uri();
            if (uri.isSipURI()) {
                uri.removeParameters();
                uri.removeHeaders();
            }
        }
    }

    public static void updateChatbotCapability(int phoneId, ImsUri uri, boolean isChatbotRole) {
        int i = phoneId;
        ICapabilityDiscoveryModule capModule = null;
        if (ImsRegistry.isReady()) {
            capModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        }
        if (capModule != null) {
            Capabilities capabilities = capModule.getCapabilitiesCache(i).get(uri);
            if (capabilities != null) {
                if (isChatbotRole) {
                    capabilities.addFeature(Capabilities.FEATURE_CHATBOT_ROLE);
                } else {
                    capabilities.removeFeature(Capabilities.FEATURE_CHATBOT_ROLE);
                }
                String str = LOG_TAG;
                Log.i(str, "addChatbotCapability : capabilities" + capabilities);
                String extFeature = "";
                if (!CollectionUtils.isNullOrEmpty((Collection<?>) capabilities.getExtFeature())) {
                    extFeature = String.join(",", capabilities.getExtFeature());
                }
                capModule.getCapabilitiesCache(i).update(capabilities.getUri(), capabilities.getFeature(), capabilities.getAvailableFeatures(), false, capabilities.getPidf(), capabilities.getLastSeen(), capabilities.getTimestamp(), capabilities.getPAssertedId(), capabilities.getIsTokenUsed(), extFeature, capabilities.getExpCapInfoExpiry());
                return;
            }
            return;
        }
        ImsUri imsUri = uri;
    }
}
