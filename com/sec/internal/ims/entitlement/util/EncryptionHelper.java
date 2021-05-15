package com.sec.internal.ims.entitlement.util;

import android.database.Cursor;
import android.util.Base64;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionHelper {
    private static final String LOG_TAG = DateUtil.class.getSimpleName();
    private static Map<String, EncryptionHelper> mEncryptionHelpers = new HashMap();
    private Cipher mCipher;

    public static synchronized EncryptionHelper getInstance(String encryptionAlgorithm) {
        EncryptionHelper encryptionHelper;
        synchronized (EncryptionHelper.class) {
            if (mEncryptionHelpers.get(encryptionAlgorithm) == null) {
                mEncryptionHelpers.put(encryptionAlgorithm, new EncryptionHelper(encryptionAlgorithm));
            }
            encryptionHelper = mEncryptionHelpers.get(encryptionAlgorithm);
        }
        return encryptionHelper;
    }

    private EncryptionHelper(String encryptionAlgorithm) {
        try {
            this.mCipher = Cipher.getInstance(encryptionAlgorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "exception " + e.getMessage());
        }
    }

    public static SecretKey generateKey(String encryptionAlgorithm) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionAlgorithm);
        keyGenerator.init(256, secureRandom);
        return keyGenerator.generateKey();
    }

    public static SecretKey getSecretKey(Cursor cursor) {
        String encodedKey = cursor.getString(cursor.getColumnIndex(SoftphoneContract.AccountColumns.SECRET_KEY));
        if (encodedKey == null) {
            return null;
        }
        byte[] decodedKey = Base64.decode(encodedKey, 0);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
    }

    public String encrypt(String plainText, SecretKey secretKey) {
        try {
            this.mCipher.init(1, secretKey);
            return new String(Base64.encode(this.mCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)), 0), StandardCharsets.UTF_8);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "exception " + e.getMessage());
            return null;
        }
    }

    public String decrypt(String cryptedText, SecretKey secretKey) {
        if (cryptedText == null || secretKey == null) {
            return null;
        }
        try {
            this.mCipher.init(2, secretKey);
            return new String(this.mCipher.doFinal(Base64.decode(cryptedText, 0)), StandardCharsets.UTF_8);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "exception " + e.getMessage());
            return null;
        }
    }
}
