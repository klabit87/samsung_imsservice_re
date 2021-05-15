package com.sec.internal.helper;

import android.os.Message;

public class State implements IState {
    protected State() {
    }

    public void enter() {
    }

    public void exit() {
    }

    public boolean processMessage(Message msg) {
        return false;
    }

    public String getName() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(36) + 1);
    }
}
