package javax.mail;

public class ReadOnlyFolderException extends MessagingException {
    private static final long serialVersionUID = 5711829372799039325L;
    private transient Folder folder;

    public ReadOnlyFolderException(Folder folder2) {
        this(folder2, (String) null);
    }

    public ReadOnlyFolderException(Folder folder2, String message) {
        super(message);
        this.folder = folder2;
    }

    public Folder getFolder() {
        return this.folder;
    }
}
