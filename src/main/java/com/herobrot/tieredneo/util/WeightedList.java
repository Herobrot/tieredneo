package com.herobrot.tieredneo.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class WeightedList<T> {
    private final List<Entry<T>> entries = new ArrayList<>();
    private int totalWeight = 0;

    public void add(int weight, T value) {
        if (weight <= 0) return;
        entries.add(new Entry<>(weight, value));
        totalWeight += weight;
    }

    public boolean isEmpty() {return entries.isEmpty();}

    public int size() {return entries.size();}

    public List<Entry<T>> sortedAscending() {
        List<Entry<T>> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingInt(Entry::weight));
        return sorted;
    }

    public static <T> WeightedList<T> fromEntries(List<Entry<T>> entries) {
        WeightedList<T> list = new WeightedList<>();
        for (Entry<T> e : entries) list.add(e.weight(), e.value());
        return list;
    }

    public T draw() {
        if (entries.isEmpty()) return null;
        if (entries.size() == 1) return entries.getFirst().value();

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        for (Entry<T> entry : entries) {
            if (roll < entry.weight()) return entry.value();
            roll -= entry.weight();
        }

        return entries.getLast().value();
    }

    public record Entry<T>(int weight, T value) {
        public Entry<T> withWeight(int newWeight) {
            return new Entry<>(newWeight, value);
        }
    }
}