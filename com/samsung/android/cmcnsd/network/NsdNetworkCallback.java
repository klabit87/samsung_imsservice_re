package com.samsung.android.cmcnsd.network;

public class NsdNetworkCallback {
    public void onAvailable(NsdNetworkCapabilities capabilities) {
    }

    public void onConnected(NsdNetwork network) {
    }

    public void onDisconnected(NsdNetwork network) {
    }

    public void onNetworkMessageReceived(NsdNetworkMessage message) {
    }

    public String toString() {
        return "clazz=" + getClass().getSimpleName() + "@" + hashCode();
    }
}
