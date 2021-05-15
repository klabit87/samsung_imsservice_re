package javax.mail.event;

import javax.mail.Store;

public class StoreEvent extends MailEvent {
    public static final int ALERT = 1;
    public static final int NOTICE = 2;
    private static final long serialVersionUID = 1938704919992515330L;
    protected String message;
    protected int type;

    public StoreEvent(Store store, int type2, String message2) {
        super(store);
        this.type = type2;
        this.message = message2;
    }

    public int getMessageType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public void dispatch(Object listener) {
        ((StoreListener) listener).notification(this);
    }
}
