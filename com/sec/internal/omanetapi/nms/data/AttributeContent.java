package com.sec.internal.omanetapi.nms.data;

public class AttributeContent {
    public String clientCorrelator;
    public String contentType;
    public String date;
    public String direction;
    public String from;
    public String messageContext;
    public String messageId;
    public String miMeVersion;
    public String reportRequested;
    public String[] to;

    public AttributeContent(String date2, String messageContext2, String messageId2, String mimeVersion, String direction2, String clientCorrelator2, String from2, String[] to2, String contentType2, String reportRequested2) {
        this.date = date2;
        this.messageContext = messageContext2;
        this.messageId = messageId2;
        this.miMeVersion = mimeVersion;
        this.direction = direction2;
        this.clientCorrelator = clientCorrelator2;
        this.from = from2;
        this.to = new String[to2.length];
        for (int i = 0; i < to2.length; i++) {
            this.to[i] = to2[i];
        }
        this.contentType = contentType2;
        this.reportRequested = reportRequested2;
    }
}
