package com.sighs.springmahjong.ai;

import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.event.GameEvent;
import com.sighs.springmahjong.game.event.GameEventBus;
import com.sighs.springmahjong.game.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * AI controller. Hooks into the GameEventBus to make decisions for bot players.
 * Responds synchronously via session.handleReaction() / session.executeDiscard().
 */
public class AiController {
    private static final Logger LOGGER = LoggerFactory.getLogger("AiController");

    private final GameSession session;
    private final DecisionEngine engine = new DecisionEngine();
    private final Set<Seat> botSeats = new HashSet<>();

    public AiController(GameSession session) {
        this.session = session;
    }

    public void register(GameEventBus bus) {
        refreshBotSeats();
        bus.register(GameEvent.PostDrawEvent.class, this::onPostDraw);
        bus.register(GameEvent.ReactionRequestEvent.class, this::onReactionRequest);
    }

    private void refreshBotSeats() {
        botSeats.clear();
        for (Patron p : session.seatedPatrons().values()) {
            if (p.isBot()) {
                botSeats.add(p.getActiveSeat());
            }
        }
    }

    private void onPostDraw(GameEvent.PostDrawEvent event) {
        if (!botSeats.contains(event.seat())) return;
        Table table = session.currentTable();
        if (table == null) return;

        Player player = table.getPlayer(event.seat());
        if (player == null || player.isGhost()) return;

        Tile toDiscard = engine.chooseDiscard(player.hand());
        if (toDiscard != null) {
            LOGGER.debug("[AI {}] discarding {}", event.seat(), toDiscard.utf8());
            session.executeDiscard(event.seat(), toDiscard);
        }
    }

    private void onReactionRequest(GameEvent.ReactionRequestEvent event) {
        if (!botSeats.contains(event.seat())) return;

        Reaction reaction = engine.chooseReaction(
            event.options(),
            event.chiOptions().isEmpty() ? null : event.chiOptions());

        LOGGER.debug("[AI {}] reaction: {}", event.seat(), reaction.type());
        session.handleReaction(event.seat(), reaction);
    }
}
