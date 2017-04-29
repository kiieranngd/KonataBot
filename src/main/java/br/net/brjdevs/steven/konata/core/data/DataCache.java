package br.net.brjdevs.steven.konata.core.data;

import br.net.brjdevs.steven.konata.core.TaskManager;
import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.commons.lang3.tuple.Pair;

public class DataCache<T extends DBObject> extends TSynchronizedLongObjectMap<Pair<T, Long>> {

    private final long expiresIn;

    public DataCache(long expiresIn) {
        super(new TLongObjectHashMap<>());
        this.expiresIn = expiresIn;
        TaskManager.startAsyncTask("Data Cache Expire", (service) -> {
            for (long l : super.keys()) {
                Pair<T, Long> pair = super.get(l);
                if (pair.getRight() < System.currentTimeMillis())
                    super.remove(l);
            }
        }, 120);
    }
    public void put(long key, T object) {
        super.put(key, Pair.of(object, expiresIn + System.currentTimeMillis()));
    }

    public T getData(long key) {
        return get(key).getKey();
    }

    public Pair<T, Long> get(long key) {
        return put(key, Pair.of(super.get(key).getKey(), expiresIn + System.currentTimeMillis()));
    }
}
