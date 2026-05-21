package com.cyrenaica.cyrenaicaserver.database.databaseManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import com.cyrenaica.cyrenaicaserver.tools.Tools;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

//salt: derna@libya123456789
//pass: palstin2023

public class Encryption {

    protected Context context;

    private static final char[] SEKRIT          = {'P', 'a', 'l', 's', 't', 'i', 'n'} ; // INSERT A RANDOM PASSWORD HERE.
    private static final String CIPHER_PREF_KEY = "PREF_DB_CIPHER";
    private static final String DB_PREF_KEY     = "PREF_DB_PASSWORD";

    public Encryption(Context c){
        context  = c;
    }

    @SuppressLint("HardwareIds")
    public String encrypt(String value ) {

        try {
            final byte[] bytes          = value!=null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key               = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
            Cipher pbeCipher            = Cipher.getInstance("PBEWithMD5AndDES");

            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(StandardCharsets.UTF_8), 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),StandardCharsets.UTF_8);

        }
        catch( Exception e ) {
            throw new RuntimeException(e);
        }

    }

    @SuppressLint("HardwareIds")
    public String decrypt(String value){
        try {
            final byte[] bytes           = value!=null ? Base64.decode(value,Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory  = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key                = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
            Cipher pbeCipher             = Cipher.getInstance("PBEWithMD5AndDES");

            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID).getBytes(StandardCharsets.UTF_8), 20));

            return new String(pbeCipher.doFinal(bytes),StandardCharsets.UTF_8);

        }
        catch( Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveDbPassword(String Password){
        String encryptedKey  = encrypt(DB_PREF_KEY);
        String encryptedPass = encrypt(Password);
        Tools.setStringPref(context, encryptedKey, encryptedPass);
    }

    public String getDbPassword(){
        String encryptedKey  = encrypt(DB_PREF_KEY);
        return decrypt(Tools.getStringPref(context, encryptedKey));
    }

    public boolean searchDbPassword(){
        String encryptedKey  = encrypt(DB_PREF_KEY);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return mPrefs.getAll().containsKey(encryptedKey);
    }

    public void saveDbCipher(String Cipher){
        String encryptedKey  = encrypt(CIPHER_PREF_KEY);
        String encryptedPass = encrypt(Cipher);
        Tools.setStringPref(context, encryptedKey, encryptedPass);
    }

    public String getDbCipher(){
        String encryptedKey  = encrypt(CIPHER_PREF_KEY);
        return decrypt(Tools.getStringPref(context, encryptedKey));
    }
}
