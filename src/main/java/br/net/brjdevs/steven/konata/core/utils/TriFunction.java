package br.net.brjdevs.steven.konata.core.utils;

public interface TriFunction<T, U, K, R> {
    R apply(T t, U u, K k);
}
