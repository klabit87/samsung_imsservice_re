package com.sec.internal.interfaces.ims.core;

import android.net.Network;
import java.util.List;

public interface PdnEventListener {
    void onConnected(int networkType, Network network) {
    }

    void onDisconnected(int networkType, boolean isPdnUp) {
    }

    void onSuspended(int networkType) {
    }

    void onLocalIpChanged(int networkType, boolean isStackedIpChanged) {
    }

    void onPcscfAddressChanged(int networkType, List<String> list) {
    }

    void onResumed(int networkType) {
    }

    void onSuspendedBySnapshot(int networkType) {
    }

    void onResumedBySnapshot(int networkType) {
    }

    void onNetworkRequestFail() {
    }
}
