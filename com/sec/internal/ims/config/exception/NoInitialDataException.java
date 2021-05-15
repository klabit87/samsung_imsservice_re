package com.sec.internal.ims.config.exception;

public class NoInitialDataException extends Exception {
    private static final long serialVersionUID = -1037078209338059005L;
    private String message = "";

    public NoInitialDataException(String message2) {
        if (message2 != null) {
            this.message = message2;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
