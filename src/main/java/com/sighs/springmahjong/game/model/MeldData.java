package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Tile;

import javax.annotation.Nullable;
import java.util.List;

public sealed interface MeldData {
    record Chi(Tile tile, List<Tile> tiles) implements MeldData {}
    record Pung(Tile tile) implements MeldData {}
    record Kong(Tile tile, KongType type) implements MeldData {}

    enum KongType {
        CONCEALED,
        EXPOSED,
        ADDED
    }

    static Chi chi(Tile tile, List<Tile> tiles) { return new Chi(tile, tiles); }
    static Pung pung(Tile tile) { return new Pung(tile); }
    static Kong kong(Tile tile, KongType type) { return new Kong(tile, type); }

    @Nullable Tile tile();
}
