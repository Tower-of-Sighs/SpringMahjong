package com.sighs.springmahjong.network;

import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.event.GameEvent;
import com.sighs.springmahjong.game.event.GameEventBus;
import com.sighs.springmahjong.game.model.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Bridges GameEventBus events to network packets.
 * Dynamically routes data based on each Patron's activeSeat (sitting vs spectating).
 */
public class NetworkBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("NetworkBridge");

    private final GameSession session;
    private final MinecraftServer server;
    private boolean registered = false;

    public NetworkBridge(GameSession session, MinecraftServer server) {
        this.session = session;
        this.server = server;
    }

    public void register(GameEventBus bus) {
        if (registered) return;
        registered = true;

        bus.register(GameEvent.TileDrawnEvent.class, this::onDraw);
        bus.register(GameEvent.TilePlayedEvent.class, this::onDiscard);
        bus.register(GameEvent.ReactionExecutedEvent.class, this::onReactionExecuted);
        bus.register(GameEvent.SettlementEvent.class, this::onSettlement);
        bus.register(GameEvent.GameEndEvent.class, this::onGameEnd);
        bus.register(GameEvent.DealEvent.class, this::onDeal);
    }

    private void onDeal(GameEvent.DealEvent event) {
        for (Map.Entry<Seat, Player> entry : session.currentTable().players().entrySet()) {
            Seat seat = entry.getKey();
            List<Tile> hand = event.hands().get(seat);
            if (hand == null) continue;
            List<Integer> tileIds = hand.stream().map(Tile::getId).toList();
            sendToSeat(seat, new SDealHand(tileIds));
        }
    }

    private void onDraw(GameEvent.TileDrawnEvent event) {
        for (Patron p : session.getPatrons()) {
            if (!isPlayerOnline(p)) continue;
            if (p.getActiveSeat() == event.seat()) {
                sendTo(p, new SYourDraw(event.tile().getId()));
            } else if (p.isSitting()) {
                sendTo(p, new SOpponentDraw(event.seat().ordinal()));
            } else {
                sendTo(p, new SYourDraw(event.tile().getId()));
            }
        }
    }

    private void onDiscard(GameEvent.TilePlayedEvent event) {
        broadcast(new SBroadcastDiscard(event.seat().ordinal(), event.tile().getId()));
    }

    private void onReactionExecuted(GameEvent.ReactionExecutedEvent event) {
        MeldData meld = event.meldData();
        int tileId = 0;
        int meldType = 0;
        if (meld instanceof MeldData.Pung pung) { tileId = pung.tile().getId(); meldType = 2; }
        else if (meld instanceof MeldData.Kong kong) { tileId = kong.tile().getId(); meldType = 3; }
        else if (meld instanceof MeldData.Chi chi) { tileId = chi.tile().getId(); meldType = 1; }
        broadcast(new SBroadcastMeld(event.seat().ordinal(), tileId, meldType));
    }

    private void onSettlement(GameEvent.SettlementEvent event) {
        Map<Integer, Integer> changes = new HashMap<>();
        for (Map.Entry<Seat, Integer> e : event.scoreChanges().entrySet()) {
            changes.put(e.getKey().ordinal(), e.getValue());
        }
        broadcast(new SBroadcastSettlement(
            event.winnerSeat().ordinal(), event.totalFan(), changes));
    }

    private void onGameEnd(GameEvent.GameEndEvent event) {
        Map<Integer, Integer> finalScores = new HashMap<>();
        for (Map.Entry<Seat, Integer> e : event.finalScores().entrySet()) {
            finalScores.put(e.getKey().ordinal(), e.getValue());
        }
        broadcast(new SBroadcastGameEnd(finalScores));
    }

    // ========== Internal Helpers ==========

    private boolean isPlayerOnline(Patron patron) {
        return server.getPlayerList().getPlayer(patron.uuid()) != null;
    }

    private void sendTo(Patron patron, CustomPacketPayload payload) {
        ServerPlayer player = server.getPlayerList().getPlayer(patron.uuid());
        if (player != null) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private void sendToSeat(Seat seat, CustomPacketPayload payload) {
        for (Patron p : session.seatedPatrons().values()) {
            if (p.getActiveSeat() == seat) {
                sendTo(p, payload);
                break;
            }
        }
    }

    private void broadcast(CustomPacketPayload payload) {
        for (Patron p : session.getPatrons()) {
            if (isPlayerOnline(p)) {
                sendTo(p, payload);
            }
        }
    }
}
