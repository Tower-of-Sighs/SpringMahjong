package com.sighs.springmahjong.game.model;

import cc.sighs.gb_jMahjong.*;
import com.sighs.springmahjong.game.event.GameEvent;
import com.sighs.springmahjong.game.event.GameEventBus;
import com.sighs.springmahjong.game.state.GameStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {
    private static final Logger LOGGER = LoggerFactory.getLogger("GameSession");

    private final UUID sessionId = UUID.randomUUID();
    private final Map<UUID, Patron> patrons = new ConcurrentHashMap<>();
    private final GameSettings settings;
    private final GameEventBus eventBus = new GameEventBus();
    private final TurnTimer turnTimer = new TurnTimer();

    private volatile boolean terminated = false;
    private Wind roundWind = Wind.EAST;
    private int handInRound = 0;
    private int totalHands = 0;
    private Seat dealerSeat = Seat.EAST;
    private @Nullable Table currentTable;

    // Pending actions waiting for player response
    private final Map<Seat, PendingReaction> pendingReactionRequests = new LinkedHashMap<>();
    private final Queue<Seat> pendingReactionQueue = new LinkedList<>();
    private Seat currentDiscarder;
    private int currentMaxPriority;
    private final java.util.ArrayDeque<Runnable> pendingTicks = new java.util.ArrayDeque<>();
    private int tickDepth = 0;
    private static final int MAX_TICK_DEPTH = 1000;
    private record PendingReaction(List<ReactionType> options, Seat discarder) {}

    public GameSession(GameSettings settings) {
        this.settings = settings;
    }

    public UUID sessionId() { return sessionId; }
    public GameEventBus eventBus() { return eventBus; }
    public GameSettings settings() { return settings; }
    public @Nullable Table currentTable() { return currentTable; }

    // ========== Patron Management ==========

    public Patron addPatron(UUID uuid, String name, boolean isBot) {
        Patron p = new Patron(uuid, name, isBot);
        patrons.put(uuid, p);
        return p;
    }

    public void removePatron(UUID uuid) {
        patrons.remove(uuid);
        checkAllPlayersLeft();
    }
    public @Nullable Patron getPatron(UUID uuid) { return patrons.get(uuid); }
    public Collection<Patron> getPatrons() { return Collections.unmodifiableCollection(patrons.values()); }

    public void sitDown(UUID uuid, Seat seat) {
        Patron p = patrons.get(uuid);
        if (p == null) return;
        // Check if seat already taken by another patron
        for (Patron other : patrons.values()) {
            if (other != p && other.getActiveSeat() == seat) return;
        }

        // Check if there's a bot occupying this seat; remove it
        Patron existingBot = null;
        for (Patron other : patrons.values()) {
            if (other != p && other.getActiveSeat() == seat && other.isBot()) {
                existingBot = other;
                break;
            }
        }
        if (existingBot != null) {
            // Remove the bot patron 鈥?human takes over
            if (currentTable != null) {
                Player botPlayer = currentTable.getPlayer(seat);
                if (botPlayer != null) botPlayer.setGhost(false);
            }
            patrons.remove(existingBot.uuid());
        }

        p.sitDown(seat);
        if (currentTable != null) {
            Player player = currentTable.getPlayer(seat);
            if (player != null) {
                player.setGhost(false);
                LOGGER.info("Player {} recovered from ghost at seat {}", uuid, seat);
            }
        }
    }

    public void standUp(UUID uuid) {
        if (terminated) return;
        Patron p = patrons.get(uuid);
        if (p == null || !p.isSitting()) return;
        Seat seat = p.getActiveSeat();
        LOGGER.info("Player {} stands up from {}", p.name(), seat);
        p.standUp();
        if (currentTable != null) {
            Player player = currentTable.getPlayer(seat);
            if (player != null) {
                player.setGhost(true);
                LOGGER.info("Player at {} is now ghost (AI will auto-play)", seat);
            }
        }
        checkAllPlayersLeft();
    }

    public boolean isSitting(UUID uuid) {
        Patron p = patrons.get(uuid);
        return p != null && p.isSitting();
    }

    public boolean isSpectating(UUID uuid) {
        Patron p = patrons.get(uuid);
        return p != null && p.isSpectating();
    }

    public Map<Seat, Patron> seatedPatrons() {
        Map<Seat, Patron> result = new EnumMap<>(Seat.class);
        for (Patron p : patrons.values()) {
            if (p.isSitting()) result.put(p.getActiveSeat(), p);
        }
        return result;
    }

    public boolean isSeatAvailable(Seat seat) {
        return seatedPatrons().values().stream().noneMatch(p -> p.getActiveSeat() == seat);
    }

    public void fillEmptySeatsWithBots() {
        for (Seat seat : Seat.values()) {
            if (isSeatAvailable(seat)) {
                Patron bot = addPatron(
                    UUID.nameUUIDFromBytes(("Bot:" + seat.name()).getBytes()),
                    "Bot-" + seat.name(), true);
                bot.sitDown(seat);
            }
        }
    }

    // ========== Disconnect / Reconnect ==========

    private final Map<UUID, Long> disconnectDeadlines = new HashMap<>();

    public void handleDisconnect(UUID uuid, long gracePeriodMs) {
        Patron patron = patrons.get(uuid);
        if (patron == null || !patron.isSitting()) return;

        long deadline = System.currentTimeMillis() + gracePeriodMs;
        disconnectDeadlines.put(uuid, deadline);

        if (currentTable != null) {
            Player player = currentTable.getPlayer(patron.getActiveSeat());
            if (player != null) player.setGhost(true);
        }
    }

    public void handleReconnect(UUID uuid) {
        disconnectDeadlines.remove(uuid);
        Patron patron = patrons.get(uuid);
        if (patron != null && patron.isSitting() && currentTable != null) {
            Player player = currentTable.getPlayer(patron.getActiveSeat());
            if (player != null && player.isGhost()) {
                player.setGhost(false);
            }
        }
    }

    // ========== Lifecycle Management ==========

    /** Check if any real (non-bot) player is still in this session. */
    public boolean hasRealPlayers() {
        return patrons.values().stream().anyMatch(p -> !p.isBot());
    }

    /** Check if all sitting players are bots (no humans at the table). */
    public boolean allSittingAreBots() {
        return seatedPatrons().values().stream().allMatch(Patron::isBot);
    }

    /** Immediately end the game and clean up. Used when a table is broken or session is empty. */
    public void terminate() {
        if (terminated) return;
        terminated = true;
        turnTimer.cancel();
        pendingReactionQueue.clear();
        pendingReactionRequests.clear();
        pendingTicks.clear();
        LOGGER.info("Session {} terminated (currentTable={})", sessionId, currentTable);
    }

    public boolean isTerminated() { return terminated; }

    /**
     * Check if the game should end due to all real players leaving.
     * Called after each standUp/disconnect.
     */
    private void checkAllPlayersLeft() {
        if (terminated) return;
        if (!hasRealPlayers()) {
            LOGGER.warn("All real players left the table. Ending game.");
            if (currentTable != null) {
                eventBus.setStage(GameStage.FINISHED);
                eventBus.post(new GameEvent.GameEndEvent(getFinalScores()));
            }
            terminate();
        }
    }

    // ========== Game Flow ==========

    public void startGame() {
        if (seatedPatrons().size() < 4) {
            if (settings.autoFillBots()) {
                fillEmptySeatsWithBots();
            } else {
                LOGGER.warn("Not enough players to start game");
                return;
            }
        }
        totalHands = 0;
        handInRound = 0;
        roundWind = Wind.EAST;
        startNewHand();
    }

    private void startNewHand() {
        if (totalHands >= settings.maxRounds() && settings.maxRounds() > 0) {
            eventBus.post(new GameEvent.GameEndEvent(getFinalScores()));
            return;
        }

        Wall wall = new Wall();
        Map<Seat, List<Tile>> hands = wall.deal();

        Map<Seat, Player> players = new EnumMap<>(Seat.class);
        for (Map.Entry<Seat, Patron> entry : seatedPatrons().entrySet()) {
            Seat seat = entry.getKey();
            Player player = new Player(entry.getValue().name(), seat);
            if (settings.timedBotTurns() && entry.getValue().isBot()) {
                player.setGhost(true);
            }
            List<Tile> tiles = hands.get(seat);
            for (Tile t : tiles) player.hand().lipai.add(t);

            if (seat == dealerSeat) {
                // Dealer draws 14th tile immediately, no sentinel needed
                Tile extra = wall.draw();
                if (extra != null) player.hand().lipai.add(extra);
            } else {
                // Non-dealer: 13 tiles + sentinel placeholder for future draw
                player.hand().lipai.add(new Tile(Tile.TILE_INVALID));
            }
            player.hand().generateTable();
            players.put(seat, player);
        }

        currentTable = new Table(wall, players, roundWind, dealerSeat);
        for (Player p : players.values()) {
            p.hand().setQuanfeng(roundWind.tileId());
            p.hand().setMenfeng(p.seat().index() < 4 ? Tile.TILE_E + p.seat().index() : Tile.TILE_E);
        }

        eventBus.setStage(GameStage.DEALING);
        eventBus.post(new GameEvent.DealEvent(hands, wall.remaining()));
        eventBus.setStage(GameStage.DRAWING);

        currentTable.setCurrentTurn(dealerSeat);
        totalHands++;

        // Dealer's first turn: skip draw, go to discard
        Player dealer = currentTable.getPlayer(dealerSeat);
        if (dealer != null) {
            eventBus.post(new GameEvent.TileDrawnEvent(dealerSeat, dealer.hand().getLastLipai()));
            eventBus.setStage(GameStage.DISCARDING);
            eventBus.post(new GameEvent.PostDrawEvent(dealerSeat));

            if (dealer.isGhost()) {
                scheduleGhostDiscard(dealerSeat, dealer);
            } else {
                turnTimer.start(settings.timeControl().discardTimeMs(), () -> {
                    executeDiscard(dealerSeat, dealer.lastTile());
                });
            }
        }
        // Drain pending ticks for bot games (AI responses in dealer's first turn)
        if (isAllBots() && !pendingTicks.isEmpty()) {
            drainPendingTicks();
        }
    }

    // ========== Turn Flow (all players go through this) ==========

    /**
     * Begin a player's turn. Handles drawing AND delegates discard decision
     * via PostDrawEvent so that AI controllers and network bridges can intercept.
     */
    private void beginTurn(Seat seat) {
        Player player = currentTable.getPlayer(seat);
        if (player == null) { advanceToNextTurn(seat); return; }

        Tile drawn = currentTable.wall().draw();
        if (drawn == null) {
            // Draw game 鈥?wall exhausted (鑽掔墝娴佸眬)
            List<Seat> tenpaiSeats = new ArrayList<>();
            for (Seat s : Seat.values()) {
                Player p = currentTable.getPlayer(s);
                if (p == null) continue;
                Fan f = new Fan();
                if (!f.calcTing(p.hand()).isEmpty()) tenpaiSeats.add(s);
            }
            ScoringSystem.ScoreResult drawResult = ScoringSystem.calculateDraw(tenpaiSeats, currentTable.players());
            eventBus.setStage(GameStage.SETTLEMENT);
            eventBus.post(new GameEvent.RoundEndEvent(roundWind, dealerSeat));
            advanceToNextHand(true); // 娴佸眬杩炲簞
            return;
        }

        player.drawTile(drawn);
        eventBus.post(new GameEvent.TileDrawnEvent(seat, drawn));

        // Check self-draw win
        Fan fan = new Fan();
        if (fan.judgeHu(player.hand()) != 0) {
            handleWin(seat, true, fan);
            return;
        }

        // Check concealed kong (TODO: Phase 5)
        // Check supplement flower (TODO: Phase 5)

        // Delegate discard decision to whoever is listening
        eventBus.setStage(GameStage.DISCARDING);
        eventBus.post(new GameEvent.PostDrawEvent(seat));

        if (player.isGhost()) {
            scheduleGhostDiscard(seat, player);
        } else {
            // Human or AI: handled via ReactionRequest from the event bus
            // AI controllers call session.executeDiscard() directly
            // Set up timer timeout for humans
            if (!isAllBots()) {
                turnTimer.start(settings.timeControl().discardTimeMs(), () -> {
                    executeDiscard(seat, player.lastTile());
                });
            }
        }
        // Drain pending ticks to process bot game loop iteratively
        drainPendingTicks();
    }

    // ========== Discard ==========

    /**
     * Called when a player (human, AI, or ghost) decides which tile to discard.
     */
    public void executeDiscard(Seat seat, Tile tile) {
        if (terminated || currentTable == null) return;
        turnTimer.cancel();
        Player player = currentTable.getPlayer(seat);
        if (player == null || eventBus.getStage() != GameStage.DISCARDING) {
            LOGGER.warn("executeDiscard rejected: terminated={}, stage={}", terminated, eventBus.getStage());
            return;
        }

        // Validate tile is in hand
        int idx = player.hand().lipai.indexOf(tile);
        if (idx < 0) {
            LOGGER.warn("Player {} tried to discard tile {} not in hand", seat, tile.utf8());
            return;
        }

        // Remove the tile from hand
        player.hand().lipai.remove(idx);
        // Ensure TILE_INVALID sentinel is at the end
        ensureSentinel(player);
        player.hand().generateTable();

        currentTable.discardPool().add(seat, tile);
        eventBus.post(new GameEvent.TilePlayedEvent(seat, tile));

        // Check opponent reactions
        eventBus.setStage(GameStage.REACTING);
        if (isAllBots()) {
            pendingTicks.addLast(() -> checkReactions(seat));
            drainPendingTicks();
        } else {
            checkReactions(seat);
        }
    }

    // ========== Reactions ==========

    private void checkReactions(Seat discarder) {
        Tile lastDiscard = currentTable.discardPool().lastDiscard();
        if (lastDiscard == null) { advanceToNextTurn(discarder); return; }

        Map<Seat, List<ReactionType>> allOptions = new EnumMap<>(Seat.class);
        this.currentDiscarder = discarder;

        for (Seat seat : Seat.values()) {
            if (seat == discarder) continue;
            Player p = currentTable.getPlayer(seat);
            if (p == null) continue;

            List<ReactionType> options = new ArrayList<>();

            // Win check (using GB-jMahjong)
            Fan fan = new Fan();
            Handtiles testHand = cloneHand(p.hand());
            testHand.setTile(lastDiscard);
            if (fan.judgeHu(testHand) != 0) {
                options.add(ReactionType.WIN);
            }
            // Pung check
            if (p.hand().lipaiTable.getOrDefault(lastDiscard.getId(), 0) >= 2) {
                options.add(ReactionType.PUNG);
            }
            // Exposed kong check
            if (p.hand().lipaiTable.getOrDefault(lastDiscard.getId(), 0) >= 3) {
                options.add(ReactionType.KONG);
            }
            // Chi check (only upper seat)
            if (seat == discarder.next() && lastDiscard.isShu()) {
                options.add(ReactionType.CHI);
            }
            options.add(ReactionType.PASS); // Always can pass
            allOptions.put(seat, options);
        }

        // Priority order: WIN(100) > KONG(80) > PUNG(60) > CHI(40) > PASS(0)
        currentMaxPriority = allOptions.values().stream()
            .flatMap(Collection::stream)
            .mapToInt(ReactionType::priority)
            .max().orElse(0);

        if (currentMaxPriority <= ReactionType.PASS.priority()) {
            advanceToNextTurn(discarder);
            return;
        }

        // Build ordered reactor queue: discarder.next 鈫?across 鈫?prev
        pendingReactionQueue.clear();
        pendingReactionRequests.clear();

        Seat[] order = {discarder.next(), discarder.across(), discarder.prev()};
        for (Seat reactor : order) {
            List<ReactionType> rawOptions = allOptions.get(reactor);
            if (rawOptions == null || rawOptions.isEmpty()) continue;

            // Filter to only max-priority options (if WIN exists, only WIN/PASS)
            List<ReactionType> filtered = rawOptions.stream()
                .filter(r -> r.priority() == currentMaxPriority || r == ReactionType.PASS)
                .toList();

            if (filtered.size() > 1 || (filtered.size() == 1 && filtered.get(0) != ReactionType.PASS)) {
                // This player has actionable options
                Player p = currentTable.getPlayer(reactor);
                if (p != null && p.isGhost()) continue; // Ghost auto-passes
                pendingReactionQueue.add(reactor);
                pendingReactionRequests.put(reactor, new PendingReaction(filtered, discarder));
            }
        }

        if (pendingReactionQueue.isEmpty()) {
            advanceToNextTurn(discarder);
            return;
        }

        // Ask the first reactor
        askNextReactor();
    }

    private void askNextReactor() {
        if (pendingReactionQueue.isEmpty()) {
            advanceToNextTurn(currentDiscarder);
            return;
        }

        Seat seat = pendingReactionQueue.poll();
        PendingReaction request = pendingReactionRequests.get(seat);
        if (request == null) {
            if (isAllBots()) {
                pendingTicks.addFirst(this::askNextReactor);
            } else {
                askNextReactor();
            }
            return;
        }

        Tile lastDiscard = currentTable.discardPool().lastDiscard();
        Player p = currentTable.getPlayer(seat);
        List<List<Tile>> chiTiles = (lastDiscard != null && p != null
            && seat == request.discarder().next() && lastDiscard.isShu()
            && request.options().contains(ReactionType.CHI))
            ? generateChiOptions(p, lastDiscard) : List.of();

        eventBus.post(new GameEvent.ReactionRequestEvent(seat, request.options(), chiTiles));
    }

    /**
     * Called by AiController or NetworkBridge when a player responds to a reaction request.
     */
    public void handleReaction(Seat seat, Reaction reaction) {
        if (terminated) return;
        PendingReaction request = pendingReactionRequests.remove(seat);
        if (request == null) {
            LOGGER.warn("handleReaction for {} but no pending request (likely timeout/terminated)", seat);
            return;
        }

        turnTimer.cancel();

        if (reaction.type() == ReactionType.PASS) {
            if (isAllBots()) {
                pendingTicks.addFirst(this::askNextReactor);
            } else {
                askNextReactor();
            }
        } else {
            executeReaction(seat, reaction, request.discarder());
        }
    }

    private void executeReaction(Seat seat, Reaction reaction, Seat discarder) {
        turnTimer.cancel();
        pendingReactionQueue.clear();
        pendingReactionRequests.clear();
        Player player = currentTable.getPlayer(seat);
        Tile lastDiscard = currentTable.discardPool().lastDiscard();
        if (player == null || lastDiscard == null) return;

        // Remove the discard tile from the pool (claim it for the reaction)
        currentTable.discardPool().removeLast();

        switch (reaction.type()) {
            case WIN -> {
                // Replace sentinel with the discard tile to form winning hand
                player.hand().setTile(lastDiscard);
                player.hand().generateTable();
                Fan fan = new Fan();
                fan.countFan(player.hand());
                handleWin(seat, false, fan);
            }
            case PUNG -> {
                removeTilesFromHand(player, lastDiscard.getId(), 2);
                // 2 removed, 11 left + TILE_INVALID sentinel already exists
                Pack pungPack = new Pack(Pack.PACK_TYPE_KEZI, lastDiscard);
                player.hand().fulu.add(pungPack);
                ensureSentinel(player);
                player.hand().generateTable();

                eventBus.post(new GameEvent.ReactionExecutedEvent(seat, MeldData.pung(lastDiscard)));

                currentTable.setCurrentTurn(seat);
                enqueueBeginTurn(seat);
            }
            case KONG -> {
                removeTilesFromHand(player, lastDiscard.getId(), 3);
                Pack kongPack = new Pack(Pack.PACK_TYPE_GANG, lastDiscard);
                player.hand().fulu.add(kongPack);
                ensureSentinel(player);
                player.hand().generateTable();

                Tile kongTile = currentTable.wall().kongDraw();
                if (kongTile != null) {
                    player.drawTile(kongTile);
                }
                currentTable.setCurrentTurn(seat);
                enqueueBeginTurn(seat);
            }
            case CHI -> {
                // Use MeldData to determine which tiles were claimed
                List<Tile> chiTiles = (reaction.data() instanceof MeldData.Chi chi)
                    ? chi.tiles() : List.of();
                Tile midTile;
                if (chiTiles.size() == 2) {
                    // The 3 chi tiles are: chiTiles[0], chiTiles[1], lastDiscard
                    // Determine the middle tile
                    List<Tile> allThree = new ArrayList<>(chiTiles);
                    allThree.add(lastDiscard);
                    allThree.sort(null);
                    midTile = allThree.get(1);

                    // Remove the non-discard tiles from hand
                    for (Tile t : chiTiles) {
                        player.hand().lipai.remove(t);
                    }
                } else {
                    midTile = lastDiscard;
                }

                Pack chiPack = new Pack(Pack.PACK_TYPE_SHUNZI, midTile);
                player.hand().fulu.add(chiPack);
                ensureSentinel(player);
                player.hand().generateTable();

                eventBus.post(new GameEvent.ReactionExecutedEvent(seat,
                    MeldData.chi(lastDiscard, chiTiles)));

                currentTable.setCurrentTurn(seat);
                enqueueBeginTurn(seat);
            }
            default -> advanceToNextTurn(discarder);
        }
    }

    // ========== Utilities ==========

    private void ensureSentinel(Player player) {
        // The last lipai element must be TILE_INVALID for drawTile/setTile to work
        if (player.hand().lipai.isEmpty()
            || player.hand().getLastLipai().getId() != Tile.TILE_INVALID) {
            player.hand().lipai.add(new Tile(Tile.TILE_INVALID));
        }
    }

    private void removeTilesFromHand(Player player, int tileId, int count) {
        int removed = 0;
        Iterator<Tile> it = player.hand().lipai.iterator();
        while (it.hasNext() && removed < count) {
            if (it.next().getId() == tileId) {
                it.remove();
                removed++;
            }
        }
    }

    private Handtiles cloneHand(Handtiles original) {
        Handtiles copy = new Handtiles();
        for (Tile t : original.lipai) {
            copy.lipai.add(new Tile(t));
        }
        for (Pack p : original.fulu) {
            copy.fulu.add(new Pack(p.getType(), p.getMiddleTile(), p.getZuhelongType(), p.getOffer()));
        }
        copy.generateTable();
        return copy;
    }

    private List<List<Tile>> generateChiOptions(Player player, Tile tile) {
        if (!tile.isShu()) return List.of();
        Map<Integer, Integer> table = player.hand().lipaiTable;

        List<List<Tile>> options = new ArrayList<>();
        int id = tile.getId();

        // The discard tile can be low(0), mid(1), or high(2) of the chi sequence
        // Three possible patterns: tile-2,tile-1,tile / tile-1,tile,tile+1 / tile,tile+1,tile+2
        int[][] offsets = {{-2, -1, 0}, {-1, 0, 1}, {0, 1, 2}};

        for (int[] off : offsets) {
            int low = id + off[0];
            int mid = id + off[1];
            int high = id + off[2];

            // Validate same suit (all must be within [1-9] of the same suit)
            if (getSuit(low) != getSuit(id) || getSuit(mid) != getSuit(id) || getSuit(high) != getSuit(id)) {
                continue;
            }
            if (low < 1 || high > 27) continue;

            // Check we have the other 2 tiles (not counting the discard)
            int needLow = (off[0] == 0) ? 0 : table.getOrDefault(low, 0);
            int needMid = (off[1] == 0) ? 0 : table.getOrDefault(mid, 0);
            int needHigh = (off[2] == 0) ? 0 : table.getOrDefault(high, 0);

            if (needLow > 0 && needMid > 0 && needHigh > 0) {
                List<Tile> group = new ArrayList<>();
                if (off[0] != 0) group.add(new Tile(low));
                if (off[1] != 0) group.add(new Tile(mid));
                if (off[2] != 0) group.add(new Tile(high));
                options.add(group);
            }
        }

        return options;
    }

    private int getSuit(int id) {
        if (id >= 1 && id <= 9) return 1;    // wan
        if (id >= 10 && id <= 18) return 2;   // tiao
        if (id >= 19 && id <= 27) return 3;   // bing
        return 0;
    }

    // ========== Win / Round Advancement ==========

    private void handleWin(Seat winnerSeat, boolean isSelfDraw, Fan fan) {
        turnTimer.cancel();
        pendingReactionQueue.clear();
        pendingReactionRequests.clear();
        Player winner = currentTable.getPlayer(winnerSeat);
        if (winner == null) return;

        int totalFan = Math.max(1, fan.totFanRes);
        Seat loserSeat = isSelfDraw ? null : currentTable.discardPool().lastDiscardSeat();

        ScoringSystem.ScoreResult result = ScoringSystem.calculateWin(
            winnerSeat, isSelfDraw, totalFan, dealerSeat, loserSeat,
            currentTable().players());

        eventBus.setStage(GameStage.SETTLEMENT);
        boolean dealerKeepsSeat = winnerSeat == dealerSeat;
        eventBus.post(new GameEvent.SettlementEvent(winnerSeat, isSelfDraw,
            Map.of(), totalFan, result.scoreChanges()));
        advanceToNextHand(dealerKeepsSeat);
    }private void advanceToNextHand(boolean dealerKeepsSeat) {
        eventBus.setStage(GameStage.SETTLEMENT);
        if (!dealerKeepsSeat) {
            // 杞簞: dealer moves to next seat
            dealerSeat = dealerSeat.next();
        }
        // If dealer won or draw (娴佸眬杩炲簞), dealerSeat unchanged (杩炲簞).
        handInRound++;
        if (handInRound >= 4) {
            handInRound = 0;
            roundWind = roundWind.next();
            if (roundWind == Wind.EAST) {
                // 4 rounds complete (鏉卞崡瑗垮寳) 鈥?game over
                eventBus.post(new GameEvent.GameEndEvent(getFinalScores()));
                return;
            }
        }
        startNewHand();
    }

    private void advanceToNextTurn(Seat currentSeat) {
        pendingReactionQueue.clear();
        pendingReactionRequests.clear();
        Seat next = currentSeat.next();
        currentTable.setCurrentTurn(next);
        eventBus.setStage(GameStage.DRAWING);
        if (isAllBots()) {
            pendingTicks.addLast(() -> beginTurn(next));
        } else {
            beginTurn(next);
        }
    }

    // ========== Iterative Game Loop (prevents StackOverflow in bot games) ==========

    /**
     * Enqueue beginTurn if in bot mode (to break recursion), else call directly.
     */
    private void enqueueBeginTurn(Seat seat) {
        if (isAllBots()) {
            pendingTicks.addLast(() -> beginTurn(seat));
        } else {
            beginTurn(seat);
        }
    }

    private void scheduleGhostDiscard(Seat seat, Player player) {
        long timeoutMs = settings.timeControl().discardTimeMs();
        if (timeoutMs > 0 && timeoutMs < Long.MAX_VALUE) {
            turnTimer.start(timeoutMs, () -> executeDiscard(seat, player.lastTile()));
        } else {
            pendingTicks.addLast(() -> executeDiscard(seat, player.lastTile()));
        }
    }

    private boolean isAllBots() {
        return patrons.values().stream().allMatch(p -> p.isBot() || p.isSpectating());
    }

    /**
     * Drains queued game ticks iteratively, preventing recursive stack overflow
     * when all players are bots making synchronous decisions.
     */
    private void drainPendingTicks() {
        tickDepth = 0;
        while (!pendingTicks.isEmpty() && tickDepth < MAX_TICK_DEPTH) {
            Runnable task = pendingTicks.pollFirst();
            if (task != null) {
                tickDepth++;
                task.run();
            }
        }
        if (!pendingTicks.isEmpty()) {
            LOGGER.warn("drainPendingTicks hit max depth ({}), clearing {} remaining", MAX_TICK_DEPTH, pendingTicks.size());
            pendingTicks.clear();
        }
    }

    private Map<Seat, Integer> getFinalScores() {
        Map<Seat, Integer> scores = new EnumMap<>(Seat.class);
        if (currentTable != null) {
            for (Map.Entry<Seat, Player> e : currentTable.players().entrySet()) {
                scores.put(e.getKey(), e.getValue().score());
            }
        }
        return scores;
    }
}

