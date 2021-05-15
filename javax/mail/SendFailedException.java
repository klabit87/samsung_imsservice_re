package javax.mail;

public class SendFailedException extends MessagingException {
    private static final long serialVersionUID = -6457531621682372913L;
    protected transient Address[] invalid;
    protected transient Address[] validSent;
    protected transient Address[] validUnsent;

    public SendFailedException() {
    }

    public SendFailedException(String s) {
        super(s);
    }

    public SendFailedException(String s, Exception e) {
        super(s, e);
    }

    public SendFailedException(String msg, Exception ex, Address[] validSent2, Address[] validUnsent2, Address[] invalid2) {
        super(msg, ex);
        this.validSent = validSent2;
        this.validUnsent = validUnsent2;
        this.invalid = invalid2;
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
}
