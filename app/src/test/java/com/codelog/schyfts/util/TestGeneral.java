package com.codelog.schyfts.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestGeneral {

    @Test
    public void testGetKeyByValue() {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(i, i * 300);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(i, General.getKeyByValue(map, i * 300));
        }
        assertNull(General.getKeyByValue(map, -1));
    }

}
