package br.net.brjdevs.steven.konata.core.utils;

import gnu.trove.map.TLongObjectMap;

public class TLongMapUtils {

    public static <T> T computeIfAbsent(TLongObjectMap<T> map, long key, T d) {
        if (!map.containsKey(key))
            map.put(key, d);
        return map.get(key);
    }
}
