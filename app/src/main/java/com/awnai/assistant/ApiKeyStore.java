package com.awnai.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class ApiKeyStore {

    private static final String STORE = "awn_secure_store";
    private static final String KEY_ALIAS = "awn_gemini_key";
    private static final String PREF_CIPHER = "encrypted_key";
    private static final String PREF_IV = "encryption_iv";

    private ApiKeyStore() {}

    private static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry)
                keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }

        KeyGenerator generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        );

        generator.init(new KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        );

        return generator.generateKey();
    }

    public static void save(Context context, String apiKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());

        byte[] encrypted = cipher.doFinal(
            apiKey.getBytes(StandardCharsets.UTF_8)
        );

        SharedPreferences prefs = context.getSharedPreferences(
            STORE, Context.MODE_PRIVATE
        );

        prefs.edit()
            .putString(PREF_CIPHER, android.util.Base64.encodeToString(
                encrypted, android.util.Base64.NO_WRAP))
            .putString(PREF_IV, android.util.Base64.encodeToString(
                cipher.getIV(), android.util.Base64.NO_WRAP))
            .apply();
    }

    public static String load(Context context) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(
            STORE, Context.MODE_PRIVATE
        );

        String encrypted = prefs.getString(PREF_CIPHER, null);
        String iv = prefs.getString(PREF_IV, null);

        if (encrypted == null || iv == null) return "";

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
            Cipher.DECRYPT_MODE,
            getSecretKey(),
            new GCMParameterSpec(128,
                android.util.Base64.decode(iv, android.util.Base64.NO_WRAP))
        );

        byte[] decrypted = cipher.doFinal(
            android.util.Base64.decode(encrypted, android.util.Base64.NO_WRAP)
        );

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
