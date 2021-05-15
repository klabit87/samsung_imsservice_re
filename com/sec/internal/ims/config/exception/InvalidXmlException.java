package com.sec.internal.ims.config.exception;

public class InvalidXmlException extends Exception {
    private static final long serialVersionUID = -1084933356219231606L;
    private String message = "";

    public InvalidXmlException(String message2) {
        if (message2 != null) {
            this.message = message2;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
