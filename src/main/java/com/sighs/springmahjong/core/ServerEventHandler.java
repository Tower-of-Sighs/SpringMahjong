package com.sighs.springmahjong.core;

import com.sighs.springmahjong.Springmahjong;
import com.sighs.springmahjong.game.model.GameManager;
import com.sighs.springmahjong.game.model.GameSession;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Server-side lifecycle management:
 * - Player disconnect → ghost mode + grace period
 * - Periodic cleanup of empty sessions
 * - Server stop → clean up all sessions
 */
@EventBusSubscriber(modid = Springmahjong.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session != null) {
                session.standUp(player.getUUID());
                session.handleDisconnect(player.getUUID(), 30_000);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerReconnect(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
            if (session != null) {
                session.handleReconnect(player.getUUID());
            }
        }
    }

    /** Periodic cleanup of empty sessions (~5 seconds). */
    private static int cleanupTicks = 0;
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        cleanupTicks++;
        if (cleanupTicks >= 100) {
            cleanupTicks = 0;
            GameManager.getInstance().cleanupEmptySessions();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GameManager.getInstance().clearAll();
    }
}
