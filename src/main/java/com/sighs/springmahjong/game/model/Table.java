package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Pack;
import cc.sighs.gb_jMahjong.Tile;

import javax.annotation.Nullable;
import java.util.*;

public class Table {
    private final Wall wall;
    private final Map<Seat, Player> players;
    private final Wind roundWind;
    private final DiscardPool discardPool;
    private final Seat dealerSeat;
    private Seat currentTurn;
    private com.sighs.springmahjong.game.state.GameStage stage;

    public Table(Wall wall, Map<Seat, Player> players, Wind roundWind, Seat dealerSeat) {
        this.wall = wall;
        this.players = new EnumMap<>(players);
        this.roundWind = roundWind;
        this.discardPool = new DiscardPool();
        this.dealerSeat = dealerSeat;
        this.currentTurn = dealerSeat;
        this.stage = com.sighs.springmahjong.game.state.GameStage.IDLE;
    }

    public Wall wall() { return wall; }
    public Map<Seat, Player> players() { return Collections.unmodifiableMap(players); }
    public Player getPlayer(Seat seat) { return players.get(seat); }
    public Wind roundWind() { return roundWind; }
    public DiscardPool discardPool() { return discardPool; }
    public Seat dealerSeat() { return dealerSeat; }
    public Seat currentTurn() { return currentTurn; }
    public void setCurrentTurn(Seat seat) { this.currentTurn = seat; }
    public com.sighs.springmahjong.game.state.GameStage stage() { return stage; }
    public void setStage(com.sighs.springmahjong.game.state.GameStage stage) { this.stage = stage; }

    public boolean hasGhost() {
        return players.values().stream().anyMatch(Player::isGhost);
    }

    public List<Seat> getActiveSeats() {
        return Arrays.stream(Seat.values())
            .filter(s -> players.containsKey(s))
            .toList();
    }

    public Seat nextSeat(Seat seat) {
        Seat next = seat.next();
        return next;
    }
}
