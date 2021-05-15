package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_;

public final class State {
    public static final int ACTIVE = 1;
    public static final int INIT = 0;
    public static final int TERMINATED = 2;
    public static final String[] names = {"INIT", "ACTIVE", "TERMINATED"};

    private State() {
    }

    public static String name(int e) {
        return names[e];
    }
}
