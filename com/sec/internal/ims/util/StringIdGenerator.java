package com.sec.internal.ims.util;

import android.text.TextUtils;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.StringGenerator;
import com.sec.internal.log.IMSLog;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class StringIdGenerator {
    private static final int FILETRANSFERID_MAX_LEN = 32;
    private static final int FILETRANSFERID_MIN_LEN = 10;
    private static final String LOG_TAG = StringIdGenerator.class.getSimpleName();
    private static final int SUBSCRIPTIONID_MAX_LEN = 32;
    private static final int SUBSCRIPTIONID_MIN_LEN = 10;

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public static String generateChatId(Set<ImsUri> uriList, String imsi, boolean withTimeStamp, int chatMode) {
        Preconditions.checkNotNull(uriList, "Passed URI Set is null.");
        String str = LOG_TAG;
        IMSLog.s(str, "generateChatId(Set<URI> participants = " + uriList.toString() + " ) withTimeStamp: " + withTimeStamp);
        StringBuilder feedingData = new StringBuilder();
        if (withTimeStamp) {
            feedingData.append(new Timestamp(new Date().getTime()).toString());
        }
        for (ImsUri p : uriList) {
            feedingData.append(p.toString());
        }
        if (!TextUtils.isEmpty(imsi)) {
            feedingData.append(imsi);
        }
        feedingData.append(chatMode);
        try {
            String feedingString = feedingData.toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "feeding data: " + feedingString);
            return HashManager.generateHash(feedingData.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("NoSuchAlgorithmException caught when trying to generate chatId");
        }
    }

    public static String generateChatId(Set<ImsUri> uriList, boolean withTimeStamp, int chatMode) {
        Preconditions.checkNotNull(uriList, "Passed URI Set is null.");
        String str = LOG_TAG;
        IMSLog.s(str, "generateChatId(Set<URI> participants = " + uriList.toString() + " ) withTimeStamp: " + withTimeStamp + "ChatMode: " + chatMode);
        StringBuilder feedingData = new StringBuilder();
        if (withTimeStamp) {
            feedingData.append(new Timestamp(new Date().getTime()).toString());
        }
        for (ImsUri p : uriList) {
            feedingData.append(p.toString());
        }
        feedingData.append(chatMode);
        try {
            String feedingString = feedingData.toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "feeding data: " + feedingString);
            return HashManager.generateHash(feedingData.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("NoSuchAlgorithmException caught when trying to generate chatId");
        }
    }

    public static String generateImdn() {
        return generateUuid();
    }

    public static String generateFileTransferId() {
        return StringGenerator.generateString(10, 32);
    }

    public static String generateSubscriptionId() {
        return StringGenerator.generateString(10, 32);
    }

    public static String generateContributionId() {
        return UUID.randomUUID().toString();
    }

    public static String generateConversationId() {
        return UUID.randomUUID().toString();
    }
}
