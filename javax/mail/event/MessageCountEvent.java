package javax.mail.event;

import javax.mail.Folder;
import javax.mail.Message;

public class MessageCountEvent extends MailEvent {
    public static final int ADDED = 1;
    public static final int REMOVED = 2;
    private static final long serialVersionUID = -7447022340837897369L;
    protected transient Message[] msgs;
    protected boolean removed;
    protected int type;

    public MessageCountEvent(Folder folder, int type2, boolean removed2, Message[] msgs2) {
        super(folder);
        this.type = type2;
        this.removed = removed2;
        this.msgs = msgs2;
    }

    public int getType() {
        return this.type;
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public Message[] getMessages() {
        return this.msgs;
    }

    public void dispatch(Object listener) {
        if (this.type == 1) {
            ((MessageCountListener) listener).messagesAdded(this);
        } else {
            ((MessageCountListener) listener).messagesRemoved(this);
        }
    }
}
