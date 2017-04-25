package br.net.brjdevs.steven.konata.core.data;

import br.net.brjdevs.steven.konata.core.TaskManager;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.commons.lang3.tuple.Pair;

public class DataCache<T extends DBObject> {

    private TSynchronizedLongObjectMap<Pair<T, Long>> data;

    private final long expiresIn;

    public DataCache(long expiresIn) {
        this.expiresIn = expiresIn;
        this.data = new TSynchronizedLongObjectMap<>(new TLongObjectHashMap<>());
        TaskManager.startAsyncTask("Data Cache Expire", (service) -> {
            for (long l : data.keys()) {
                Pair<T, Long> pair = data.get(l);
                if (pair.getRight() < System.currentTimeMillis())
                    data.remove(l);
            }
        }, 120);
    }

    public void put(long key, T object) {
        data.put(key, Pair.of(object, expiresIn + System.currentTimeMillis()));
    }

    public T get(long key) {
        T d;
        data.put(key, Pair.of(d = data.get(key).getKey(), expiresIn + System.currentTimeMillis()));
        return d;
    }

    public boolean containsKey(long key) {
        return data.containsKey(key);
    }
}
