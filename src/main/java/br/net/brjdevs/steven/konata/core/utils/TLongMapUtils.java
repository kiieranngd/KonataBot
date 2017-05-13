package br.net.brjdevs.steven.konata.core.utils;

import gnu.trove.map.TLongObjectMap;

import java.util.function.LongFunction;

public class TLongMapUtils {

    public static <T> T computeIfAbsent(TLongObjectMap<T> map, long key, LongFunction<T> function) {
        if (!map.containsKey(key))
            map.put(key, function.apply(key));
        return map.get(key);
    }

    public static <T> T getOrDefault(TLongObjectMap<T> map, long key, T tDefault) {
        return map.containsKey(key) ? map.get(key) : tDefault;
    }
}
