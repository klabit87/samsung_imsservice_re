package com.sec.internal.constants.ims.servicemodules.volte2;

public class RrcConnectionEvent {
    RrcEvent mEvent;

    public enum RrcEvent {
        REJECTED,
        TIMER_EXPIRED
    }

    public RrcConnectionEvent(RrcEvent event) {
        this.mEvent = event;
    }

    public RrcEvent getEvent() {
        return this.mEvent;
    }
}
