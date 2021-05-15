package com.sun.mail.util;

import java.io.IOException;
import javax.mail.Folder;

public class FolderClosedIOException extends IOException {
    private static final long serialVersionUID = 4281122580365555735L;
    private transient Folder folder;

    public FolderClosedIOException(Folder folder2) {
        this(folder2, (String) null);
    }

    public FolderClosedIOException(Folder folder2, String message) {
        super(message);
        this.folder = folder2;
    }

    public Folder getFolder() {
        return this.folder;
    }
}
