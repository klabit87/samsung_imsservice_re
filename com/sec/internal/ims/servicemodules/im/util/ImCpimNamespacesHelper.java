package com.sec.internal.ims.servicemodules.im.util;

import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.MaapNamespace;
import com.sec.internal.constants.ims.servicemodules.im.RcsNamespace;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;

public class ImCpimNamespacesHelper {
    public static ImDirection extractImDirection(int phoneId, ImCpimNamespaces imCpimNameSpaces) {
        ImDirection direction = ImDirection.INCOMING;
        if (imCpimNameSpaces == null) {
            return direction;
        }
        String extractedDirection = imCpimNameSpaces.getFirstHeaderValue("MD", "direction");
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (mnoStrategy != null) {
            return mnoStrategy.convertToImDirection(extractedDirection);
        }
        return direction;
    }

    public static String extractMaapTrafficType(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(MaapNamespace.NAME, "Traffic-Type");
        }
        return null;
    }

    public static String extractRcsReferenceId(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, RcsNamespace.REFERENCE_ID_KEY);
        }
        return null;
    }

    public static String extractRcsReferenceType(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, RcsNamespace.REFERENCE_TYPE_KEY);
        }
        return null;
    }

    public static String extractRcsReferenceValue(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, RcsNamespace.REFERENCE_VALUE_KEY);
        }
        return null;
    }

    public static String extractRcsTrafficType(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, "Traffic-Type");
        }
        return null;
    }
}
