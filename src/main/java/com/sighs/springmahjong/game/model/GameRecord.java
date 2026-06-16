package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Fan;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Complete record of a single mahjong game (4 rounds).
 * Used for replay and statistics.
 */
public record GameRecord(
    UUID gameId,
    long timestamp,
    long randomSeed,
    GameSettings settings,
    List<HandRecord> hands,
    Map<Seat, PlayerProfile> players
) {
    public record PlayerProfile(UUID uuid, String name, Seat seat, boolean isBot) {}

    public record HandRecord(
        int handNumber,
        Wind roundWind,
        Seat dealerSeat,
        @Nullable Seat winner,
        boolean isSelfDraw,
        @Nullable Seat discarder,
        int totalFan,
        Map<String, Integer> fanDetail,
        Map<Seat, Integer> scoreChanges,
        Map<Seat, Integer> scoresAfter
    ) {}
}
