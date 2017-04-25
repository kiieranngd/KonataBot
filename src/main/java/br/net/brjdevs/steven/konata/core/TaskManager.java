package br.net.brjdevs.steven.konata.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskManager {

    public static void startAsyncTask(String task, Consumer<ScheduledExecutorService> scheduled, int everySeconds) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, task + " [Executor]"));
        scheduledExecutorService.scheduleAtFixedRate(() -> scheduled.accept(scheduledExecutorService), 0, everySeconds, TimeUnit.SECONDS);
    }
}
