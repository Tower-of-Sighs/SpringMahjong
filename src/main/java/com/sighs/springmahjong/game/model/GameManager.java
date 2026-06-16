package com.sighs.springmahjong.game.model;

import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Global GameSession manager.
 * Cleans up empty sessions: if a session has no active non-bot patrons, it's eligible for removal.
 */
public class GameManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("GameManager");
    private static final GameManager INSTANCE = new GameManager();

    private final Map<BlockPos, GameSession> sessions = new HashMap<>();

    public static GameManager getInstance() { return INSTANCE; }

    public GameSession getOrCreate(BlockPos pos) {
        return getOrCreate(pos, GameSettings.DEBUG);
    }

    public GameSession getOrCreate(BlockPos pos, GameSettings settings) {
        return sessions.computeIfAbsent(pos, k -> new GameSession(settings));
    }

    public @Nullable GameSession findByPlayer(UUID uuid) {
        return sessions.values().stream()
            .filter(s -> s.getPatron(uuid) != null)
            .findFirst().orElse(null);
    }

    public void removeSession(BlockPos pos) {
        GameSession session = sessions.get(pos);
        if (session != null) {
            session.terminate();
            sessions.remove(pos);
        }
    }

    /** Clean up sessions where all real players have left. */
    public void cleanupEmptySessions() {
        Iterator<Map.Entry<BlockPos, GameSession>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, GameSession> entry = it.next();
            GameSession session = entry.getValue();
            // Check if session has no sitting human players and no active game
            boolean hasActivePlayers = session.seatedPatrons().values().stream()
                .anyMatch(p -> !p.isBot());
            boolean gameRunning = session.currentTable() != null;
            if (!hasActivePlayers && !gameRunning) {
                LOGGER.info("Cleaning up empty session at {}", entry.getKey());
                session.terminate();
                it.remove();
            }
        }
    }

    /** Called when a table block is broken — end the game immediately. */
    public void onTableBroken(BlockPos pos) {
        GameSession session = sessions.get(pos);
        if (session != null) {
            LOGGER.warn("Table broken at {}, ending game", pos);
            session.terminate();
            sessions.remove(pos);
        }
    }

    public Map<BlockPos, GameSession> getActiveSessions() {
        return Map.copyOf(sessions);
    }

    public void clearAll() { sessions.clear(); }
}
