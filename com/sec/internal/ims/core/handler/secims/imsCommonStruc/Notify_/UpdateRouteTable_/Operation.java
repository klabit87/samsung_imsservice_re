package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.UpdateRouteTable_;

public final class Operation {
    public static final int ADD = 0;
    public static final int REMOVE = 1;
    public static final int UPDATE = 2;
    public static final String[] names = {"ADD", "REMOVE", "UPDATE"};

    private Operation() {
    }

    public static String name(int e) {
        return names[e];
    }
}
