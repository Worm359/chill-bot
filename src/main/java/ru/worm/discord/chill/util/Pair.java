package ru.worm.discord.chill.util;

import java.util.function.Consumer;

public class Pair<T1, T2> {
    private T1 first;
    private T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    public T2 getSecond() {
        return second;
    }

    public void setSecond(T2 second) {
        this.second = second;
    }

    public static <T1> void forEach(Pair<T1, T1> p, Consumer<T1> consumer) {
        consumer.accept(p.getFirst());
        consumer.accept(p.getSecond());
    }
}
