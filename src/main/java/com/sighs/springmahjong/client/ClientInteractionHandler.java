package com.sighs.springmahjong.client;

import com.sighs.springmahjong.client.state.ClientGameState;
import com.sighs.springmahjong.network.CStandUp;
import com.sighs.springmahjong.network.CDiscard;
import com.sighs.springmahjong.network.CReaction;
import com.sighs.springmahjong.game.model.ReactionType;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Handles client-side interactions:
 * - Detecting dismount (Shift) to send CStandUp
 * - Click handlers for tiles and action buttons
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientInteractionHandler {

    private static boolean wasShiftDown = false;
    private static boolean wasSittingLastTick = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ClientGameState state = ClientGameState.INSTANCE;

        if (mc.player == null || state.getCurrentSessionId() == null) return;

        boolean isSitting = mc.player.isPassenger()
            && mc.player.getVehicle() != null
            && mc.player.getVehicle().getType() == com.sighs.springmahjong.entity.ModEntities.SEAT.get();

        // Detect dismount (was sitting, now not)
        if (wasSittingLastTick && !isSitting && state.isSitting()) {
            ClientPacketDistributor.sendToServer(new CStandUp());
            state.setSeat(null);
        }

        wasSittingLastTick = isSitting;
    }

    /**
     * Called when a player clicks a tile in their hand.
     */
    public static void onTileClicked(int handIndex) {
        ClientGameState state = ClientGameState.INSTANCE;
        if (state.getMySeat() == null || !state.isMyTurn()) return;

        state.selectTile(handIndex);
    }

    /**
     * Called when the player clicks the discard button.
     */
    public static void onDiscardClicked() {
        ClientGameState state = ClientGameState.INSTANCE;
        if (state.getMySeat() == null || !state.isMyTurn()) return;

        int idx = state.getSelectedTileIndex();
        if (idx < 0 || idx >= state.getMyHand().size()) return;

        int tileId = state.getMyHand().get(idx).getId();
        state.removeTile(idx);
        state.clearSelection();
        state.clearPendingReactions();

        ClientPacketDistributor.sendToServer(new CDiscard(tileId));
    }

    /**
     * Called when the player clicks a reaction button (Chi/Pung/Kong/Win/Pass).
     */
    public static void onReactionClicked(ReactionType type, Integer chiTileId) {
        ClientGameState state = ClientGameState.INSTANCE;
        if (state.getMySeat() == null) return;

        ClientPacketDistributor.sendToServer(new CReaction(type.ordinal(), java.util.Optional.ofNullable(chiTileId)));
        state.clearPendingReactions();
    }
}

