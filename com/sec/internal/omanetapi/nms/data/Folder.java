package com.sec.internal.omanetapi.nms.data;

import java.net.URL;

public class Folder {
    public AttributeList attributes;
    public long lastModSeq;
    public String name;
    public ObjectReferenceList objects;
    public URL parentFolder;
    public String parentFolderPath;
    public String path;
    public URL resourceURL;
    public FolderReferenceList subFolders;
}
