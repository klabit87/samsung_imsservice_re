package com.sec.internal.ims.servicemodules.ss;

import java.util.List;

/* compiled from: UtConfigData */
class Condition {
    int action = 1;
    int condition = -1;
    List<MEDIA> media;
    boolean state = true;

    Condition() {
    }

    public String toString() {
        return " state = " + this.state + " action = " + this.action + " condition = " + this.condition + " media = " + this.media;
    }
}
