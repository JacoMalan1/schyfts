package com.codelog.schyfts.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocalDateFormatter {
    @Test
    public void testFormat() {
        LocalDate localDate = LocalDate.of(2021, 2, 1);
        assertEquals("01/02/2021", LocalDateFormatter.format(localDate));
    }
}
