package br.net.brjdevs.steven.konata.core.data.rethink;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DBObject {
    ExecutorService service = Executors.newSingleThreadExecutor();
    void save();
    void delete();
    default void saveAsync() {
        service.submit(this::save);
    }
    default void deleteAsync() {
        service.submit(this::delete);
    }
}
