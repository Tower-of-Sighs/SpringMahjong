package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.Fan;
import cc.sighs.gb_jMahjong.Tile;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Complete guobiao mahjong scoring system.
 * Handles: base point calculation, dealer/non-dealer payment, chip conversion.
 */
public class ScoringSystem {

    /** Calculate points for a win. Returns score changes for all seats. */
    public static ScoreResult calculateWin(Seat winner, boolean isSelfDraw,
                                           int totalFan, Seat dealerSeat,
                                           @Nullable Seat discarderSeat,
                                           Map<Seat, Player> players) {
        int basePoints = 8 * Math.max(totalFan, 1);
        Map<Seat, Integer> changes = new EnumMap<>(Seat.class);
        for (Seat s : Seat.values()) changes.put(s, 0);

        if (isSelfDraw) {
            selfDrawPayment(winner, basePoints, dealerSeat, changes);
        } else {
            if (discarderSeat != null) {
                discardPayment(winner, discarderSeat, basePoints, dealerSeat, changes);
            }
        }

        // Apply changes
        for (Map.Entry<Seat, Integer> e : changes.entrySet()) {
            Player p = players.get(e.getKey());
            if (p != null) p.addScore(e.getValue());
        }

        return new ScoreResult(totalFan, basePoints, Map.copyOf(changes));
    }

    /** Calculate draw game settlement (noten-bappu). */
    public static ScoreResult calculateDraw(List<Seat> tenpaiSeats,
                                            Map<Seat, Player> players) {
        Map<Seat, Integer> changes = new EnumMap<>(Seat.class);
        for (Seat s : Seat.values()) changes.put(s, 0);

        if (!tenpaiSeats.isEmpty()) {
            // 听牌者向未听牌者支付（国标简化：听牌每人收 8 点）
            for (Seat noten : Seat.values()) {
                if (!tenpaiSeats.contains(noten)) {
                    for (Seat tenpai : tenpaiSeats) {
                        changes.put(tenpai, changes.get(tenpai) + 8);
                        changes.put(noten, changes.get(noten) - 8);
                    }
                }
            }
        }

        for (Map.Entry<Seat, Integer> e : changes.entrySet()) {
            Player p = players.get(e.getKey());
            if (p != null) p.addScore(e.getValue());
        }

        return new ScoreResult(0, 0, Map.copyOf(changes));
    }

    // ========== Payment Helpers ==========

    private static void selfDrawPayment(Seat winner, int base, Seat dealer,
                                         Map<Seat, Integer> changes) {
        int total = 0;
        for (Seat s : Seat.values()) {
            if (s == winner) continue;
            int pay = (s == dealer) ? base * 2 : base;
            changes.put(s, -pay);
            total += pay;
        }
        changes.put(winner, total);
    }

    private static void discardPayment(Seat winner, Seat loser, int base,
                                        Seat dealer, Map<Seat, Integer> changes) {
        int multiplier = (winner == dealer || loser == dealer) ? 6 : 4;
        int pay = base * multiplier;
        changes.put(loser, -pay);
        changes.put(winner, pay);
    }

    // ========== Chips ==========

    public record ChipCount(int man, int sen, int hyaku, int ju) {
        public static ChipCount fromPoints(int points) {
            int abs = Math.abs(points);
            return new ChipCount(
                abs / 10000,
                (abs % 10000) / 1000,
                (abs % 1000) / 100,
                (abs % 100) / 10
            );
        }
        public int total() { return man * 10000 + sen * 1000 + hyaku * 100 + ju * 10; }
    }

    // ========== Result ==========

    public record ScoreResult(int totalFan, int basePoints, Map<Seat, Integer> scoreChanges) {}
}
