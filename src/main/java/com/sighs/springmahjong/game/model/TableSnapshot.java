package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Pack;
import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.state.GameStage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A snapshot of the game state for network synchronization.
 * Different fields are filled based on the recipient's role (sitting vs spectating).
 */
public record TableSnapshot(
    // Public info (all visible)
    Map<Seat, List<Pack>> melds,
    Map<Seat, List<Tile>> discards,
    Map<Seat, Integer> handSizes,
    Map<Seat, Integer> scores,
    @Nullable Seat currentTurn,
    int remainingWall,
    Wind roundWind,
    Seat dealerSeat,
    GameStage stage,

    // Role-specific
    @Nullable Seat yourSeat,
    @Nullable List<Tile> yourTiles,
    @Nullable Map<Seat, List<Tile>>  allHands,
    @Nullable Tile lastDraw,

    // Players
    List<PatronInfo> allPatrons
) {
    public record PatronInfo(String name, @Nullable Seat seat, boolean isBot) {}
}
