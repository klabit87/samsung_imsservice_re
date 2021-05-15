package javax.mail.event;

import javax.mail.Message;

public class MessageChangedEvent extends MailEvent {
    public static final int ENVELOPE_CHANGED = 2;
    public static final int FLAGS_CHANGED = 1;
    private static final long serialVersionUID = -4974972972105535108L;
    protected transient Message msg;
    protected int type;

    public MessageChangedEvent(Object source, int type2, Message msg2) {
        super(source);
        this.msg = msg2;
        this.type = type2;
    }

    public int getMessageChangeType() {
        return this.type;
    }

    public Message getMessage() {
        return this.msg;
    }

    public void dispatch(Object listener) {
        ((MessageChangedListener) listener).messageChanged(this);
    }
}
