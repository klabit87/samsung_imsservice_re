package com.samsung.android.cmcnsd.network;

public class NsdNetworkCallback {
    public void onAvailable(NsdNetworkCapabilities nsdNetworkCapabilities) {
    }

    public void onConnected(NsdNetwork nsdNetwork) {
    }

    public void onDisconnected(NsdNetwork nsdNetwork) {
    }

    public void onNetworkMessageReceived(NsdNetworkMessage nsdNetworkMessage) {
    }

    public String toString() {
        return "clazz=" + NsdNetworkCallback.class.getSimpleName() + "@" + hashCode();
    }
}
