package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Handtiles;
import cc.sighs.gb_jMahjong.Tile;

public class Player {
    private final String name;
    private final Seat seat;
    private final Handtiles hand;
    private int score;
    private boolean isGhost;

    public Player(String name, Seat seat) {
        this.name = name;
        this.seat = seat;
        this.hand = new Handtiles();
        this.score = 0;
        this.isGhost = false;
    }

    public String name() { return name; }
    public Seat seat() { return seat; }
    public Handtiles hand() { return hand; }
    public int score() { return score; }

    public boolean isGhost() { return isGhost; }
    public void setGhost(boolean ghost) { this.isGhost = ghost; }
    public boolean isDealer() { return seat == Seat.EAST; }

    public void addScore(int delta) { this.score += delta; }
    public void setScore(int score) { this.score = score; }

    public void drawTile(Tile tile) { hand.drawTile(tile); }
    public Tile discardTile() { return hand.discardTile(); }
    public Tile lastTile() { return hand.getLastLipai(); }
}
