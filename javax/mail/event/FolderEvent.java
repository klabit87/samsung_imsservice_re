package javax.mail.event;

import javax.mail.Folder;

public class FolderEvent extends MailEvent {
    public static final int CREATED = 1;
    public static final int DELETED = 2;
    public static final int RENAMED = 3;
    private static final long serialVersionUID = 5278131310563694307L;
    protected transient Folder folder;
    protected transient Folder newFolder;
    protected int type;

    public FolderEvent(Object source, Folder folder2, int type2) {
        this(source, folder2, folder2, type2);
    }

    public FolderEvent(Object source, Folder oldFolder, Folder newFolder2, int type2) {
        super(source);
        this.folder = oldFolder;
        this.newFolder = newFolder2;
        this.type = type2;
    }

    public int getType() {
        return this.type;
    }

    public Folder getFolder() {
        return this.folder;
    }

    public Folder getNewFolder() {
        return this.newFolder;
    }

    public void dispatch(Object listener) {
        int i = this.type;
        if (i == 1) {
            ((FolderListener) listener).folderCreated(this);
        } else if (i == 2) {
            ((FolderListener) listener).folderDeleted(this);
        } else if (i == 3) {
            ((FolderListener) listener).folderRenamed(this);
        }
    }
}
