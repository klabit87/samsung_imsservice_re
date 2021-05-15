package com.sec.internal.ims.config.exception;

public class InvalidHeaderException extends Exception {
    private static final long serialVersionUID = 8374723406515232560L;
    private String message = "";

    public InvalidHeaderException(String message2) {
        if (message2 != null) {
            this.message = message2;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
