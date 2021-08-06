package com.codelog.schyfts.util;

import java.security.SecureRandom;

public class RandomUtil {

    public static String getRandomString(int length, String characterSet, byte[] seed) {
        SecureRandom rng = new SecureRandom();
        rng.setSeed(seed);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(characterSet.charAt(rng.nextInt(characterSet.length())));
        }

        return result.toString();
    }

    public static String getRandomString(int length, String characterSet) {
        return getRandomString(length, characterSet, new SecureRandom().generateSeed(32));
    }

}
