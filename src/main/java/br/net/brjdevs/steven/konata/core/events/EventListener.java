package br.net.brjdevs.steven.konata.core.events;

import net.dv8tion.jda.core.events.Event;

public abstract class EventListener<T extends Event> {

    private Class<T> clazz;

    public EventListener(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getEvent() {
        return clazz;
    }
    public abstract void onEvent(T event);
}
