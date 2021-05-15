package javax.mail;

public class FolderClosedException extends MessagingException {
    private static final long serialVersionUID = 1687879213433302315L;
    private transient Folder folder;

    public FolderClosedException(Folder folder2) {
        this(folder2, (String) null);
    }

    public FolderClosedException(Folder folder2, String message) {
        super(message);
        this.folder = folder2;
    }

    public Folder getFolder() {
        return this.folder;
    }
}
