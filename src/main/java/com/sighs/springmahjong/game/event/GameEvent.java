package com.sighs.springmahjong.game.event;

import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.model.MeldData;
import com.sighs.springmahjong.game.model.ReactionType;
import com.sighs.springmahjong.game.model.Seat;
import com.sighs.springmahjong.game.model.Wind;

import java.util.List;
import java.util.Map;

public sealed interface GameEvent {

    record GameInitEvent(Wind roundWind, Seat dealerSeat, List<Seat> players) implements GameEvent {}
    record DealEvent(Map<Seat, List<Tile>> hands, int wallSize) implements GameEvent {}
    record RoundStartEvent(int roundNumber, int handNumber, Wind roundWind, Seat dealerSeat) implements GameEvent {}

    record TurnStartEvent(Seat seat) implements GameEvent {}
    record TileDrawnEvent(Seat seat, Tile tile) implements GameEvent {}
    record PostDrawEvent(Seat seat) implements GameEvent {}
    record TilePlayedEvent(Seat seat, Tile tile) implements GameEvent {}

    // 检查完所有玩家的可能反应后触发，携带每个座位可做的操作
    record PostPlayEvent(Seat seat, Tile tile,
                         Map<Seat, List<ReactionType>> possibleReactions) implements GameEvent {}

    record ReactionRequestEvent(Seat seat, List<ReactionType> options,
                                List<List<Tile>> chiOptions) implements GameEvent {}
    record ReactionEvent(Seat seat, ReactionType reaction,
                         MeldData data) implements GameEvent {}
    record ReactionExecutedEvent(Seat seat, MeldData meldData) implements GameEvent {}

    record SettlementEvent(Seat winnerSeat, boolean isSelfDraw,
                           Map<String, Integer> fanTypes, int totalFan,
                           Map<Seat, Integer> scoreChanges) implements GameEvent {}
    record RoundEndEvent(Wind nextRoundWind, Seat nextDealerSeat) implements GameEvent {}
    record GameEndEvent(Map<Seat, Integer> finalScores) implements GameEvent {}
}
