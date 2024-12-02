package com.itranswarp.exchange.util;

import java.security.SecureRandom;

public class RandomUtil {

  private static final SecureRandom SECURE_RANDOM = createSecureRamdom();

    public static final String ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String DIGITS = "0123456789";
    public static final String WORDS = ALPHABET_UPPER + ALPHABET_LOWER + DIGITS;

    public static byte[] createRandomBytaes(int length){
        byte[] buffer = new byte[length];
        SECURE_RANDOM.nextBytes(buffer);
        return buffer;
    }
    private static final SecureRandom createSecureRamdom(){
        return new SecureRandom();
    }

    public static String createRandomString(int length){
        return createRandomString(WORDS,length);
    }

    public static String createRandomString(String charList,int length){
        char[] buffer = new char[length];
        int n = charList.length();
        for (int i = 0; i < length; i++) {
            buffer[i] = charList.charAt(SECURE_RANDOM.nextInt(n));
        }
        return new String(buffer);
    }
}
