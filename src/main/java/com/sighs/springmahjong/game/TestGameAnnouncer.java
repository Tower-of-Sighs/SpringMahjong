package com.sighs.springmahjong.game;

import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.event.GameEvent;
import com.sighs.springmahjong.game.event.GameEventBus;
import com.sighs.springmahjong.game.model.GameSession;
import com.sighs.springmahjong.game.model.Seat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class TestGameAnnouncer {
    private final GameSession session;
    private final MinecraftServer server;

    public TestGameAnnouncer(GameSession session, MinecraftServer server) {
        this.session = session;
        this.server = server;
    }

    public void register(GameEventBus bus) {
        bus.register(GameEvent.DealEvent.class, this::onDeal);
        bus.register(GameEvent.TileDrawnEvent.class, this::onDraw);
        bus.register(GameEvent.TilePlayedEvent.class, this::onDiscard);
        bus.register(GameEvent.ReactionRequestEvent.class, this::onReactionRequest);
        bus.register(GameEvent.ReactionExecutedEvent.class, this::onReactionExecuted);
        bus.register(GameEvent.SettlementEvent.class, this::onSettlement);
        bus.register(GameEvent.GameEndEvent.class, this::onGameEnd);
    }

    private void onDeal(GameEvent.DealEvent event) {
        say("测试麻将桌已开始，剩余牌墙 " + event.wallSize());
        for (Seat seat : Seat.values()) {
            var hand = event.hands().get(seat);
            say(seat.name() + " 发牌 " + (hand == null ? 0 : hand.size()) + " 张");
        }
    }

    private void onDraw(GameEvent.TileDrawnEvent event) {
        say(event.seat().name() + " 摸牌 " + tileName(event.tile()));
    }

    private void onDiscard(GameEvent.TilePlayedEvent event) {
        int remaining = session.currentTable() == null ? -1 : session.currentTable().wall().remaining();
        say(event.seat().name() + " 打出 " + tileName(event.tile()) + "，剩余牌墙 " + remaining);
    }

    private void onReactionRequest(GameEvent.ReactionRequestEvent event) {
        say(event.seat().name() + " 可响应 " + event.options());
    }

    private void onReactionExecuted(GameEvent.ReactionExecutedEvent event) {
        say(event.seat().name() + " 执行副露 " + event.meldData());
    }

    private void onSettlement(GameEvent.SettlementEvent event) {
        say(event.winnerSeat().name() + " 和牌，番数 " + event.totalFan());
    }

    private void onGameEnd(GameEvent.GameEndEvent event) {
        say("测试麻将桌牌局结束：" + event.finalScores());
    }

    private void say(String message) {
        server.getPlayerList().broadcastSystemMessage(
            Component.literal("[测试麻将] " + message), false);
    }

    private String tileName(Tile tile) {
        if (tile == null) return "?";
        int id = tile.getId();
        if (id >= 1 && id <= 9) return cn(id) + "万";
        if (id >= 10 && id <= 18) return cn(id - 9) + "条";
        if (id >= 19 && id <= 27) return cn(id - 18) + "筒";
        return switch (id) {
            case Tile.TILE_E -> "东风";
            case Tile.TILE_S -> "南风";
            case Tile.TILE_W -> "西风";
            case Tile.TILE_N -> "北风";
            case Tile.TILE_C -> "红中";
            case Tile.TILE_F -> "发财";
            case Tile.TILE_P -> "白板";
            default -> tile.utf8();
        };
    }

    private String cn(int n) {
        return switch (n) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            case 6 -> "六";
            case 7 -> "七";
            case 8 -> "八";
            case 9 -> "九";
            default -> "?";
        };
    }
}
