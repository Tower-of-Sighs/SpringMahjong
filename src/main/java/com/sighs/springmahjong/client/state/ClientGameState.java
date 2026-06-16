package com.sighs.springmahjong.client.state;

import cc.sighs.gb_jMahjong.Pack;
import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.model.ReactionType;
import com.sighs.springmahjong.game.model.Seat;
import com.sighs.springmahjong.game.model.Wind;
import com.sighs.springmahjong.game.state.GameStage;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Client-side cached game state for rendering.
 * Updated by S2C packets (from ClientPacketHandler).
 * Read by MahjongTableRenderer and UI components.
 */
public class ClientGameState {
    public static final ClientGameState INSTANCE = new ClientGameState();

    private @Nullable UUID currentSessionId;
    private @Nullable BlockPos tablePos;
    private @Nullable Seat mySeat;
    private GameStage stage = GameStage.IDLE;

    // Hands
    private List<Tile> myHand = List.of();
    private int selectedTileIndex = -1;

    // Opponents: when spectating, contains actual tiles; when playing, empty
    private Map<Seat, List<Tile>> opponentHands = Map.of();

    // Public info
    private Map<Seat, List<Pack>> melds = Map.of();
    private Map<Seat, List<Tile>> discards = Map.of();
    private @Nullable Tile lastDraw;
    private int remainingWall;

    // Game info
    private Wind roundWind = Wind.EAST;
    private Seat dealerSeat = Seat.EAST;
    private @Nullable Seat currentTurn;
    private List<PatronInfo> allPatrons = List.of();

    // Pending interaction
    private @Nullable List<ReactionType> pendingReactions;
    private @Nullable List<List<Tile>> pendingChiOptions;

    // Patron lookup
    private final Map<UUID, PatronInfo> patronLookup = new HashMap<>();

    public record PatronInfo(UUID uuid, String name, @Nullable Seat seat, boolean isBot) {}

    public void reset() {
        currentSessionId = null;
        tablePos = null;
        mySeat = null;
        stage = GameStage.IDLE;
        myHand = List.of();
        selectedTileIndex = -1;
        opponentHands = Map.of();
        melds = Map.of();
        discards = Map.of();
        lastDraw = null;
        remainingWall = 0;
        roundWind = Wind.EAST;
        dealerSeat = Seat.EAST;
        currentTurn = null;
        allPatrons = List.of();
        pendingReactions = null;
        pendingChiOptions = null;
        patronLookup.clear();
    }

    // === Getters ===

    public @Nullable UUID getCurrentSessionId() { return currentSessionId; }
    public @Nullable BlockPos getTablePos() { return tablePos; }
    public @Nullable Seat getMySeat() { return mySeat; }
    public GameStage getStage() { return stage; }
    public List<Tile> getMyHand() { return myHand; }
    public int getSelectedTileIndex() { return selectedTileIndex; }
    public Map<Seat, List<Tile>> getOpponentHands() { return opponentHands; }
    public Map<Seat, List<Pack>> getMelds() { return melds; }
    public Map<Seat, List<Tile>> getDiscards() { return discards; }
    public @Nullable Tile getLastDraw() { return lastDraw; }
    public int getRemainingWall() { return remainingWall; }
    public Wind getRoundWind() { return roundWind; }
    public Seat getDealerSeat() { return dealerSeat; }
    public @Nullable Seat getCurrentTurn() { return currentTurn; }
    public List<PatronInfo> getAllPatrons() { return allPatrons; }
    public @Nullable List<ReactionType> getPendingReactions() { return pendingReactions; }
    public @Nullable List<List<Tile>> getPendingChiOptions() { return pendingChiOptions; }

    public boolean isMyTurn() { return currentTurn != null && currentTurn == mySeat; }
    public boolean isSitting() { return mySeat != null; }

    public boolean canSeeAllHands() {
        return mySeat == null; // spectating = all visible
    }

    // === Client network updates ===

    public void setSession(UUID sessionId) {
        this.currentSessionId = sessionId;
    }

    public void setSession(UUID sessionId, BlockPos pos) {
        this.currentSessionId = sessionId;
        this.tablePos = pos;
    }

    public void setSeat(@Nullable Seat seat) {
        this.mySeat = seat;
        if (seat == null) {
            pendingReactions = null;
            pendingChiOptions = null;
            selectedTileIndex = -1;
        }
    }

    public void setCurrentTurn(@Nullable Seat seat) { this.currentTurn = seat; }
    public void setRemainingWall(int count) { this.remainingWall = count; }
    public void setLastDraw(Tile tile) { this.lastDraw = tile; }

    public void updateMyHand(List<Tile> tiles) {
        this.myHand = new ArrayList<>(tiles);
        this.selectedTileIndex = -1;
    }

    public void addTile(Tile tile) {
        List<Tile> newHand = new ArrayList<>(myHand);
        newHand.add(tile);
        this.myHand = newHand;
    }

    public void removeTile(int index) {
        if (index >= 0 && index < myHand.size()) {
            List<Tile> newHand = new ArrayList<>(myHand);
            newHand.remove(index);
            this.myHand = newHand;
            if (selectedTileIndex >= myHand.size()) selectedTileIndex = -1;
        }
    }

    public void addDiscard(Seat seat, Tile tile) {
        Map<Seat, List<Tile>> newDiscards = new EnumMap<>(discards);
        List<Tile> seatDiscards = new ArrayList<>(newDiscards.getOrDefault(seat, List.of()));
        seatDiscards.add(tile);
        newDiscards.put(seat, seatDiscards);
        this.discards = newDiscards;
    }

    public void addMeld(Seat seat, Tile tile) {
        // Simplified meld tracking: just store the tile
        // Full meld data from Pack will be added later
    }

    public void setPendingReactions(List<ReactionType> options, List<List<Tile>> chiTiles) {
        this.pendingReactions = options;
        this.pendingChiOptions = chiTiles;
    }

    public void clearPendingReactions() {
        this.pendingReactions = null;
        this.pendingChiOptions = null;
    }

    // === Patron management ===

    public void updatePatrons(List<PatronInfo> patrons) {
        this.allPatrons = List.copyOf(patrons);
        patronLookup.clear();
        for (PatronInfo p : patrons) {
            patronLookup.put(p.uuid(), p);
        }
    }

    public void addPatron(PatronInfo patron) {
        List<PatronInfo> newList = new ArrayList<>(allPatrons);
        newList.add(patron);
        this.allPatrons = List.copyOf(newList);
        patronLookup.put(patron.uuid(), patron);
    }

    public void removePatron(UUID uuid) {
        patronLookup.remove(uuid);
        this.allPatrons = allPatrons.stream()
            .filter(p -> !p.uuid().equals(uuid))
            .toList();
    }

    public void setSeatFor(UUID uuid, Seat seat) {
        PatronInfo old = patronLookup.get(uuid);
        if (old != null) {
            PatronInfo updated = new PatronInfo(uuid, old.name(), seat, old.isBot());
            patronLookup.put(uuid, updated);
            this.allPatrons = allPatrons.stream()
                .map(p -> p.uuid().equals(uuid) ? updated : p)
                .toList();
        }
    }

    public void clearSeat(UUID uuid) {
        setSeatFor(uuid, null);
    }

    // === Actions ===

    public void selectTile(int index) {
        if (selectedTileIndex == index) {
            selectedTileIndex = -1; // deselect
        } else if (index >= 0 && index < myHand.size()) {
            selectedTileIndex = index;
        }
    }

    public void clearSelection() { selectedTileIndex = -1; }

    // === Snapshots ===
    public void updateState(com.sighs.springmahjong.game.model.TableSnapshot snapshot) {
        // Will be filled when TableSnapshot is implemented for network
    }
}
