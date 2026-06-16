package com.sighs.springmahjong.game.model;

import javax.annotation.Nullable;
import java.util.UUID;

public class Patron {
    private final UUID uuid;
    private final String name;
    private final boolean isBot;
    private @Nullable Seat activeSeat;

    public Patron(UUID uuid, String name, boolean isBot) {
        this.uuid = uuid;
        this.name = name;
        this.isBot = isBot;
        this.activeSeat = null;
    }

    public UUID uuid() { return uuid; }
    public String name() { return name; }
    public boolean isBot() { return isBot; }

    @Nullable
    public Seat getActiveSeat() { return activeSeat; }

    public void sitDown(Seat seat) { this.activeSeat = seat; }
    public void standUp() { this.activeSeat = null; }

    public boolean isSitting() { return activeSeat != null; }
    public boolean isSpectating() { return activeSeat == null; }
    public boolean canSeeAllHands() { return isSpectating(); }
    public boolean isDealer() { return activeSeat == Seat.EAST; }
}
