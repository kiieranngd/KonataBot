package br.net.brjdevs.steven.konata.core.events;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.LoadState;
import br.net.brjdevs.steven.konata.Shard;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EventManager implements IEventManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Event Manager");
    private ExecutorService service;
    private List<EventListener> listeners;
    private Shard shard;

    public EventManager(Shard shard) {
        this.shard = shard;
        listeners = new CopyOnWriteArrayList<>(new Reflections("br.net.brjdevs.steven.konata").getSubTypesOf(EventListener.class).stream().map(clazz -> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));

        service = Executors.newCachedThreadPool((r) -> {
            Thread t = new Thread(r);
            t.setName("Event Manager Shard " + shard.getId() + " - %d");
            return t;
        });
    }

    @Override
    public void register(Object listener) {
        if (!(listener instanceof EventListener))
            throw new IllegalArgumentException("Object must implement EventListener");
        listeners.add((EventListener) listener);
    }
    @Override
    public void unregister(Object listener) {
        if (!(listener instanceof EventListener))
            throw new IllegalArgumentException("Object must implement EventListener");
        listeners.remove(listener);
    }
    @Override
    @SuppressWarnings("unchecked")
    public void handle(Event event) {
        if (KonataBot.getLoadState() != LoadState.POSTLOAD)
            return;
        service.submit(() ->
            listeners.forEach(listener -> {
                try {
                    if (listener.getEvent().isInstance(event))
                        listener.onEvent(event);
                    KonataBot.getInstance().getLastEvents().set(shard.getId(), System.currentTimeMillis());
                } catch (Exception e) {
                    LOGGER.error("Failed to process event '" + event.getClass().getSimpleName() + "'", e);
                }
            })
        );
    }
    @Override
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(listeners);
    }
}
