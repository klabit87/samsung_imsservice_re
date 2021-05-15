package com.sec.internal.ims.config.exception;

public class UnknownStatusException extends Exception {
    private static final long serialVersionUID = -8533200068421479731L;
    private String message = "";

    public UnknownStatusException(String message2) {
        if (message2 != null) {
            this.message = message2;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
