package com.sighs.springmahjong.game.model;

import java.util.Arrays;

public enum Seat {
    EAST(0),
    SOUTH(1),
    WEST(2),
    NORTH(3);

    private final int index;

    Seat(int index) {
        this.index = index;
    }

    public int index() { return index; }

    public Seat next() { return values()[(index + 1) % 4]; }
    public Seat prev() { return values()[(index + 3) % 4]; }
    public Seat across() { return values()[(index + 2) % 4]; }

    public static Seat fromIndex(int idx) {
        return Arrays.stream(values()).filter(s -> s.index == idx).findFirst().orElse(EAST);
    }

    public static Seat dealer() { return EAST; }
}
