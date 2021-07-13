package com.codelog.schyfts.util;

import java.util.Random;

public class RandomUtil {

    public static String getRandomString(int length, String characterSet, long seed) {
        Random rng = new Random(seed);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(characterSet.charAt(rng.nextInt(characterSet.length())));
        }

        return result.toString();
    }

    public static String getRandomString(int length, String characterSet) {
        return getRandomString(length, characterSet, System.nanoTime());
    }

}
