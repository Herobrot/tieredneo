package com.herobrot.tieredneo.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thread-safe weighted random selector. Replaces net.libz.util.SortList entirely.

 * Uses ThreadLocalRandom instead of a shared Random instance to eliminate lock
 * contention when multiple server threads generate loot or spawn mobs simultaneously.

 * Weights must be positive integers. Entries with weight <= 0 are silently ignored.
 * The probability of drawing entry X is: weight(X) / sum(all weights).
 */
public final class WeightedList<T> {

    private final List<Entry<T>> entries = new ArrayList<>();
    private int totalWeight = 0;

    // -------------------------------------------------------------------------
    // Building
    // -------------------------------------------------------------------------

    public void add(int weight, T value) {
        if (weight <= 0) return;
        entries.add(new Entry<>(weight, value));
        totalWeight += weight;
    }

    public boolean isEmpty() { return entries.isEmpty(); }
    public int size()        { return entries.size(); }

    /**
     * Returns a mutable copy sorted by weight ascending.

     * Used by ModifierUtils to find and dampen the heaviest entries before
     * applying reforge or luck modifiers. Returning a copy means the caller
     * can modify weights freely without affecting the original list.
     */
    public List<Entry<T>> sortedAscending() {
        List<Entry<T>> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingInt(Entry::weight));
        return sorted;
    }

    /**
     * Reconstructs a WeightedList from a (possibly modified) list of entries.
     * Used after dampening heavy entries in ModifierUtils.
     */
    public static <T> WeightedList<T> fromEntries(List<Entry<T>> entries) {
        WeightedList<T> list = new WeightedList<>();
        for (Entry<T> e : entries) list.add(e.weight(), e.value());
        return list;
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    /**
     * Picks a random value proportional to weight.
     * Uses ThreadLocalRandom — no shared state, no contention.
     * Returns null only if the list is empty.
     */
    public T draw() {
        if (entries.isEmpty()) return null;
        if (entries.size() == 1) return entries.getFirst().value();

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        for (Entry<T> entry : entries) {
            if (roll < entry.weight()) return entry.value();
            roll -= entry.weight();
        }
        // Floating-point drift guard: return last entry
        return entries.getLast().value();
    }

    // -------------------------------------------------------------------------

    public record Entry<T>(int weight, T value) {
        public Entry<T> withWeight(int newWeight) {
            return new Entry<>(newWeight, value);
        }
    }
}