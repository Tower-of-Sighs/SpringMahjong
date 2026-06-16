package com.sighs.springmahjong.game.event;

import com.sighs.springmahjong.game.state.GameStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Game-internal event bus. NOT the NeoForge EventBus — separate to avoid conflicts
 * and keep game events contained within a single GameSession.
 */
public class GameEventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("GameEventBus");

    private final Map<Class<?>, List<Consumer<?>>> handlers = new HashMap<>();
    private GameStage currentStage = GameStage.IDLE;
    private final Map<Class<?>, Set<GameStage>> validStages = new HashMap<>();

    public <T extends GameEvent> void register(Class<T> type, Consumer<T> handler) {
        handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
    }

    public <T extends GameEvent> void register(Class<T> type, Set<GameStage> allowedStages, Consumer<T> handler) {
        validStages.put(type, allowedStages);
        register(type, handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameEvent> void post(T event) {
        Set<GameStage> allowed = validStages.get(event.getClass());
        if (allowed != null && !allowed.contains(currentStage)) {
            LOGGER.warn("Event {} rejected: current stage={}, allowed={}",
                event.getClass().getSimpleName(), currentStage, allowed);
            return;
        }

        List<Consumer<?>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (Consumer<?> h : eventHandlers) {
                try {
                    ((Consumer<T>) h).accept(event);
                } catch (Exception e) {
                    LOGGER.error("Handler failed for {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }
    }

    public void setStage(GameStage stage) { this.currentStage = stage; }
    public GameStage getStage() { return currentStage; }

    public void clear() {
        handlers.clear();
        validStages.clear();
        currentStage = GameStage.IDLE;
    }
}
