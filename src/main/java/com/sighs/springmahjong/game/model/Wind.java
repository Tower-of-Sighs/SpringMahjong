package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Tile;

public enum Wind {
    EAST(Tile.TILE_E),
    SOUTH(Tile.TILE_S),
    WEST(Tile.TILE_W),
    NORTH(Tile.TILE_N);

    private final int tileId;

    Wind(int tileId) { this.tileId = tileId; }

    public int tileId() { return tileId; }
    public Wind next() { return values()[(ordinal() + 1) % 4]; }

    public static Wind fromTileId(int id) {
        for (Wind w : values()) {
            if (w.tileId == id) return w;
        }
        return EAST;
    }
}
