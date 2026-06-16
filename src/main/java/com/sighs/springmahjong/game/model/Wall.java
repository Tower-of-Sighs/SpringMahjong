package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Tile;

import java.util.*;

public class Wall {
    private final ArrayDeque<Tile> tiles;
    private final int kongSupplementStartIndex;
    private int kongCount;

    public Wall() {
        this(new Random().nextLong());
    }

    public Wall(long seed) {
        this.tiles = new ArrayDeque<>(buildTiles());
        this.kongSupplementStartIndex = Math.max(0, tiles.size() - 14);
        shuffle(new Random(seed));
        this.kongCount = 0;
    }

    // Tile ID ranges
    private static final int WAN_START = 1, WAN_END = 9;
    private static final int TIAO_START = 10, TIAO_END = 18;
    private static final int BING_START = 19, BING_END = 27;
    private static final int FENG_START = 28, FENG_END = 31;
    private static final int JIAN_START = 32, JIAN_END = 34;
    private static final int HUA_START = 35, HUA_END = 42;

    private static List<Tile> buildTiles() {
        List<Tile> list = new ArrayList<>();
        // 数牌 × 4
        for (int id = WAN_START; id <= BING_END; id++) {
            for (int copy = 0; copy < 4; copy++) list.add(new Tile(id));
        }
        // 字牌 × 4
        for (int id = FENG_START; id <= JIAN_END; id++) {
            for (int copy = 0; copy < 4; copy++) list.add(new Tile(id));
        }
        // 花牌 × 1
        for (int id = HUA_START; id <= HUA_END; id++) {
            list.add(new Tile(id));
        }
        return list;
    }

    private void shuffle(Random random) {
        List<Tile> asList = new ArrayList<>(tiles);
        Collections.shuffle(asList, random);
        tiles.clear();
        tiles.addAll(asList);
    }

    public Map<Seat, List<Tile>> deal() {
        Map<Seat, List<Tile>> hands = new HashMap<>();
        for (Seat seat : Seat.values()) {
            List<Tile> hand = new ArrayList<>();
            for (int i = 0; i < 13; i++) {
                hand.add(tiles.pollFirst());
            }
            hands.put(seat, hand);
        }
        return hands;
    }

    public Tile draw() {
        if (tiles.size() <= 14 && kongCount > 0) {
            return null; // 荒牌
        }
        return tiles.pollFirst();
    }

    public Tile kongDraw() {
        // 从杠底补牌
        if (tiles.isEmpty()) return null;
        Tile t = tiles.pollLast();
        kongCount++;
        return t;
    }

    public int remaining() { return tiles.size(); }

    public boolean canDraw() { return tiles.size() > 0; }

    public boolean isLastTile() { return tiles.size() <= 1; }

    public long getSeed() { return 0; } // 种子用于 replay
}
