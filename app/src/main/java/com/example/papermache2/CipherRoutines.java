package com.example.papermache2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherRoutines {
    final static int  NO_OF_STRETCHING_ROUNDS = 128;

    static void LogByteArray(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i] & 0xff);
            sb.append(", ");
        }
        sb.append(" ]");
//        Log.d("LogByteArray", sb.toString());
    }//static void LogByteArray(byte[] arr) {

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] IV) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }//public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] IV) {

    static byte[] StretchKey(String key)  throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        int i=1;
        byte[] digest = md.digest(key.getBytes());
        while (i<NO_OF_STRETCHING_ROUNDS) {
            digest = md.digest(digest);
            i++;
        }
        return Arrays.copyOfRange(digest, 0, 32);
    }//static byte[] StretchKey(String key)  throws NoSuchAlgorithmException {


}//public class CipherRoutines {
