package com.sec.internal.ims.servicemodules.euc.persistence;

public class EucPersistenceException extends Exception {
    EucPersistenceException(String msg) {
        super(msg);
    }

    EucPersistenceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
