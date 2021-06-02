package com.codelog.schyfts.util;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RosterUtils {

    public static <K, V> List<Pair<K, V>> getKeyValues(Map<K, V> map) {
        List<Pair<K, V>> results = new ArrayList<>();

        var keys = map.keySet();
        for (var key : keys)
            results.add(new Pair<>(key, map.get(key)));

        return results;
    }

    public static List<Pair<Object, Pair<String, String>>> getFreeSlots(Map<String, String>[] dayitems) {
        List<Pair<Object, Pair<String, String>>> freeSlots = new ArrayList<>();

        for (int j = 0; j < 2; j++) {

            var keys = dayitems[j].keySet();
            var nextDayKeys = dayitems[(j + 1) % 2];
            for (var key : keys) {
                var value = dayitems[j].get(key);
                var nextDayValue = dayitems[(j + 1) % 2].get(key);

                if (key.toLowerCase().contains("loc") && value.equals("") && nextDayValue.equals("")) {
                    freeSlots.add(new Pair<>(dayitems[j], new Pair<>(key, value)));
                } else if (j == 0 && value.equals("OFF") && nextDayValue.equals(value)) {
                    freeSlots.add(new Pair<>(dayitems[j], new Pair<>(key, value)));
                }

            }
        }

        for (int i = 0; i < freeSlots.size(); i++) {

            var kv = freeSlots.get(i).getValue();

            if (kv.getKey().contains("loc")) {
                var tmp = freeSlots.get(i);
                var idx = freeSlots.size() - 5 + Integer.parseInt("" + kv.getKey().charAt(3)) - 1;

                freeSlots.set(i, freeSlots.get(idx));
                freeSlots.set(idx, tmp);
            }

        }

        return freeSlots;
    }

}
