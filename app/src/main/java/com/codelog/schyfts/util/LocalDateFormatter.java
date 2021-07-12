package com.codelog.schyfts.util;

import java.time.LocalDate;

public class LocalDateFormatter {

    public static String format(LocalDate date) {

        String result = "%02d/%02d/%04d";
        return result.formatted(
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear()
        );

    }

}
