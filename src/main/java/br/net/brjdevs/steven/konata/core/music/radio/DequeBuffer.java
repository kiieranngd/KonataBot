package br.net.brjdevs.steven.konata.core.music.radio;

import java.util.concurrent.ConcurrentLinkedDeque;

public class DequeBuffer<T> extends ConcurrentLinkedDeque<T> {

    private final Runnable runnable;

    public DequeBuffer(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public T poll() {
        T t = super.poll();
        if (isEmpty()) {
            runnable.run();
        }
        return t;
    }
}
