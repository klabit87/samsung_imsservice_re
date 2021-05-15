package javax.mail.event;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;

public class TransportEvent extends MailEvent {
    public static final int MESSAGE_DELIVERED = 1;
    public static final int MESSAGE_NOT_DELIVERED = 2;
    public static final int MESSAGE_PARTIALLY_DELIVERED = 3;
    private static final long serialVersionUID = -4729852364684273073L;
    protected transient Address[] invalid;
    protected transient Message msg;
    protected int type;
    protected transient Address[] validSent;
    protected transient Address[] validUnsent;

    public TransportEvent(Transport transport, int type2, Address[] validSent2, Address[] validUnsent2, Address[] invalid2, Message msg2) {
        super(transport);
        this.type = type2;
        this.validSent = validSent2;
        this.validUnsent = validUnsent2;
        this.invalid = invalid2;
        this.msg = msg2;
    }

    public int getType() {
        return this.type;
    }

    public Address[] getValidSentAddresses() {
        return this.validSent;
    }

    public Address[] getValidUnsentAddresses() {
        return this.validUnsent;
    }

    public Address[] getInvalidAddresses() {
        return this.invalid;
    }

    public Message getMessage() {
        return this.msg;
    }

    public void dispatch(Object listener) {
        int i = this.type;
        if (i == 1) {
            ((TransportListener) listener).messageDelivered(this);
        } else if (i == 2) {
            ((TransportListener) listener).messageNotDelivered(this);
        } else {
            ((TransportListener) listener).messagePartiallyDelivered(this);
        }
    }
}
