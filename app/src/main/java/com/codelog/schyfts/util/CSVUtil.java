package com.codelog.schyfts.util;

import java.util.ArrayList;
import java.util.List;

public class CSVUtil {
    public static List<List<String>> parseCSV(String[] lines) {
        var result = new ArrayList<List<String>>(lines.length);

        for (var line : lines) {
            var fields = line.split(",", -1);
            var row = new ArrayList<>(List.of(fields));
            result.add(row);
        }

        return result;
    }
}
