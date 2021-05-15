package com.sec.internal.helper;

import java.util.Date;

public class RetryTimerUtil {
    public static int getRetryAfter(String retryAfter) {
        if (retryAfter == null) {
            return -1;
        }
        try {
            if (!retryAfter.isEmpty()) {
                return Integer.parseInt(retryAfter);
            }
            return -1;
        } catch (NumberFormatException e) {
            return (int) ((new Date(retryAfter).getTime() - new Date().getTime()) / 1000);
        }
    }
}
