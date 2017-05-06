package br.net.brjdevs.steven.konata.core.data.rethink;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
    default void deleteAsync(Consumer<DBObject> callback) {
        service.submit(() -> {
            delete();
            callback.accept(this);
        });
    }
    default void saveAsync(Consumer<DBObject> callback) {
        service.submit(() -> {
            save();
            callback.accept(this);
        });
    }
}
