package com.sec.internal.omanetapi.nms.data;

import java.util.ArrayList;

public class GroupState {
    public String contributionid;
    public String group_type;
    public String lastfocussessionid;
    public ArrayList<Part> participantList = new ArrayList<>();
    public String subject;
    public String timestamp;

    public String toString() {
        return "timestamp=" + this.timestamp + ";lastfocussessionid=" + this.lastfocussessionid + ";subject=" + this.subject + ";group_type=" + this.group_type + ";contributionid=" + this.contributionid + ";participantList=" + this.participantList.toString();
    }
}
