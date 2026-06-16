package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Tile;

import java.util.*;

public class DiscardPool {
    private final List<DiscardEntry> entries = new ArrayList<>();

    public record DiscardEntry(Seat seat, Tile tile) {}

    public void add(Seat seat, Tile tile) {
        entries.add(new DiscardEntry(seat, tile));
    }

    public List<DiscardEntry> getAll() { return List.copyOf(entries); }

    /**
     * Removes and returns the last discard entry.
     * Used when a reaction (chi/pung/kong/win) claims the last discarded tile.
     */
    @javax.annotation.Nullable
    public DiscardEntry removeLast() {
        if (entries.isEmpty()) return null;
        return entries.remove(entries.size() - 1);
    }

    public List<Tile> getBySeat(Seat seat) {
        return entries.stream()
            .filter(e -> e.seat() == seat)
            .map(DiscardEntry::tile)
            .toList();
    }

    @javax.annotation.Nullable
    public Tile lastDiscard() {
        if (entries.isEmpty()) return null;
        return entries.getLast().tile();
    }

    @javax.annotation.Nullable
    public Seat lastDiscardSeat() {
        if (entries.isEmpty()) return null;
        return entries.getLast().seat();
    }

    public Map<Tile, Integer> tileCount() {
        Map<Tile, Integer> counts = new HashMap<>();
        for (DiscardEntry e : entries) {
            counts.merge(e.tile(), 1, Integer::sum);
        }
        return counts;
    }

    public void clear() { entries.clear(); }

    public int size() { return entries.size(); }
}
