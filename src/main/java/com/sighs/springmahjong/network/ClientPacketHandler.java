package com.sighs.springmahjong.network;

import com.sighs.springmahjong.client.state.ClientGameState;
import com.sighs.springmahjong.game.model.ReactionType;
import com.sighs.springmahjong.game.model.Seat;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles S2C packets on the client side.
 * Updates ClientGameState and triggers UI updates.
 */
public class ClientPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ClientPacketHandler");

    public static void handleTableInfo(final STableInfo packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.reset();
            state.setSession(packet.yourUUID());
            state.updatePatrons(packet.patrons().stream()
                .map(p -> new ClientGameState.PatronInfo(
                    p.uuid(), p.name(),
                    p.seatOrdinal().map(Seat::fromIndex).orElse(null),
                    p.isBot()))
                .toList());
            LOGGER.info("Received table info: {} patrons", packet.patrons().size());
        });
    }

    public static void handlePatronJoined(final SPatronJoined packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.addPatron(new ClientGameState.PatronInfo(
                packet.patron().uuid(), packet.patron().name(),
                packet.patron().seatOrdinal().map(Seat::fromIndex).orElse(null),
                packet.patron().isBot()));
        });
    }

    public static void handlePatronLeft(final SPatronLeft packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState.INSTANCE.removePatron(packet.uuid());
        });
    }

    public static void handleSitDown(final SSitDown packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.setSeatFor(packet.uuid(), packet.seat());
        });
    }

    public static void handleStandUp(final SStandUp packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.clearSeat(packet.uuid());
        });
    }

    public static void handleStateSync(final SStateSync packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            ClientGameSnapshot snap = packet.snapshot();
            // Update public state (Optional fields in 26.1.2)
            snap.yourSeat().ifPresent(seat -> state.setSeat(Seat.fromIndex(seat)));
            snap.yourTiles().ifPresent(tiles ->
                state.updateMyHand(tiles.stream().map(cc.sighs.gb_jMahjong.Tile::new).toList()));
            snap.currentTurn().ifPresent(turn -> state.setCurrentTurn(Seat.fromIndex(turn)));
            state.setRemainingWall(snap.remainingWall());
            LOGGER.debug("State synced: stage={}, turn={}", snap.stageOrdinal(), snap.currentTurn());
        });
    }

    public static void handleDealHand(final SDealHand packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.updateMyHand(packet.tileIds().stream()
                .map(cc.sighs.gb_jMahjong.Tile::new).toList());
            LOGGER.info("Dealt {} tiles", packet.tileIds().size());
        });
    }

    public static void handleYourDraw(final SYourDraw packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.addTile(new cc.sighs.gb_jMahjong.Tile(packet.tileId()));
            state.setLastDraw(new cc.sighs.gb_jMahjong.Tile(packet.tileId()));
        });
    }

    public static void handleOpponentDraw(final SOpponentDraw packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.setCurrentTurn(Seat.fromIndex(packet.seatOrdinal()));
        });
    }

    public static void handleReactionRequest(final SReactionRequest packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            List<ReactionType> options = packet.options().stream()
                .map(i -> ReactionType.values()[i])
                .toList();
            List<List<cc.sighs.gb_jMahjong.Tile>> chiTiles = new ArrayList<>();
            for (List<Integer> group : packet.chiOptions()) {
                chiTiles.add(group.stream().map(cc.sighs.gb_jMahjong.Tile::new).toList());
            }
            state.setPendingReactions(options, chiTiles);
        });
    }

    public static void handleBroadcastTurn(final SBroadcastTurn packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState.INSTANCE.setCurrentTurn(Seat.fromIndex(packet.seatOrdinal()));
        });
    }

    public static void handleBroadcastDiscard(final SBroadcastDiscard packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.addDiscard(Seat.fromIndex(packet.seatOrdinal()),
                new cc.sighs.gb_jMahjong.Tile(packet.tileId()));
            state.clearSelection();
        });
    }

    public static void handleBroadcastMeld(final SBroadcastMeld packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameState state = ClientGameState.INSTANCE;
            state.addMeld(Seat.fromIndex(packet.seatOrdinal()),
                new cc.sighs.gb_jMahjong.Tile(packet.tileId()));
        });
    }

    public static void handleSettlement(final SBroadcastSettlement packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            LOGGER.info("Settlement: winner={}, totalFan={}",
                packet.winnerSeat(), packet.totalFan());
        });
    }

    public static void handleGameEnd(final SBroadcastGameEnd packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            LOGGER.info("Game ended! Final scores: {}", packet.finalScores());
        });
    }

    public static void handleError(final SError packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            LOGGER.warn("Server error [{}]: {}", packet.code(), packet.message());
        });
    }
}
