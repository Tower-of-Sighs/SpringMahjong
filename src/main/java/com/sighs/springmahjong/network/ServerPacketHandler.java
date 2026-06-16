package com.sighs.springmahjong.network;

import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.model.GameManager;
import com.sighs.springmahjong.game.model.GameSession;
import com.sighs.springmahjong.game.model.MeldData;
import com.sighs.springmahjong.game.model.Patron;
import com.sighs.springmahjong.game.model.Reaction;
import com.sighs.springmahjong.game.model.ReactionType;
import com.sighs.springmahjong.game.model.Seat;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles C2S packets on the server side.
 * Delegates to GameSession for all game logic.
 */
public class ServerPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerPacketHandler");

    public static void handleJoinTable(final CJoinTable packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            GameSession session = GameManager.getInstance()
                .getOrCreate(player.blockPosition());

            if (session.getPatron(player.getUUID()) == null) {
                session.addPatron(player.getUUID(), player.getName().getString(), false);
                LOGGER.info("{} joined table", player.getName().getString());
            }

            // Send table info back (PatronEntry now uses Optional<Integer> for seat)
            PacketDistributor.sendToPlayer(player, new STableInfo(player.getUUID(),
                session.seatedPatrons().entrySet().stream()
                    .map(e -> new STableInfo.PatronEntry(
                        e.getValue().uuid(), e.getValue().name(),
                        java.util.Optional.of(e.getKey().ordinal()), e.getValue().isBot()))
                    .toList(),
                session.currentTable() != null));
        });
    }

    public static void handleLeaveTable(final CLeaveTable packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session != null) {
                session.standUp(player.getUUID());
                session.removePatron(player.getUUID());
            }
        });
    }

    public static void handleSitDown(final CSitDown packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session != null) {
                session.sitDown(player.getUUID(), packet.seat());
                LOGGER.info("{} sat at {}", player.getName().getString(), packet.seat());
            }
        });
    }

    public static void handleStandUp(final CStandUp packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session != null) {
                session.standUp(player.getUUID());
                LOGGER.info("{} stood up", player.getName().getString());
            }
        });
    }

    public static void handlePlayerReady(final CPlayerReady packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session != null && session.seatedPatrons().size() >= 4) {
                session.startGame();
            }
        });
    }

    public static void handleDiscard(final CDiscard packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session == null || session.currentTable() == null) return;

            Patron patron = session.getPatron(player.getUUID());
            if (patron == null || !patron.isSitting()) return;

            Seat seat = patron.getActiveSeat();
            session.executeDiscard(seat, new Tile(packet.tileId()));
        });
    }

    public static void handleReaction(final CReaction packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session == null) return;

            Patron patron = session.getPatron(player.getUUID());
            if (patron == null || !patron.isSitting()) return;

            Seat seat = patron.getActiveSeat();
            ReactionType type = ReactionType.values()[packet.reactionType()];
            // chiTileId is Optional<Integer> in 26.1.2
            MeldData data = packet.chiTileId().isPresent() && type == ReactionType.CHI
                ? MeldData.chi(new Tile(packet.chiTileId().get()), List.of())
                : null;

            session.handleReaction(seat, new Reaction(type, data));
        });
    }
}
