package com.sighs.springmahjong.ai;

import cc.sighs.gb_jMahjong.Handtiles;
import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.game.model.MeldData;
import com.sighs.springmahjong.game.model.Reaction;
import com.sighs.springmahjong.game.model.ReactionType;
import com.sighs.springmahjong.game.model.Seat;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class DecisionEngine {

    public Tile chooseDiscard(Handtiles hand) {
        List<Tile> handTiles = new ArrayList<>(hand.lipai);
        if (handTiles.isEmpty()) return null;

        Tile last = hand.getLastLipai();

        // Don't discard the drawn tile if we have something worse
        Tile bestDiscard = handTiles.stream()
            .filter(t -> t.getId() != Tile.TILE_INVALID)
            .min(Comparator.comparingInt(t -> tileValue(t, hand)))
            .orElse(last);

        return bestDiscard;
    }

    private int tileValue(Tile tile, Handtiles hand) {
        int count = hand.lipaiTable.getOrDefault(tile.getId(), 0);
        if (count >= 3) return 100;
        if (count == 2) return 80;

        if (tile.isZi()) return 10;
        if (tile.isYaojiu()) return 20;
        if (hasNeighbor(tile, hand)) return 60;
        return 30;
    }

    private boolean hasNeighbor(Tile tile, Handtiles hand) {
        if (!tile.isShu()) return false;
        Tile prev = tile.pred();
        Tile next = tile.succ();
        return hand.lipaiTable.getOrDefault(prev.getId(), 0) > 0
            || hand.lipaiTable.getOrDefault(next.getId(), 0) > 0;
    }

    public Reaction chooseReaction(List<ReactionType> options,
                                    @Nullable List<List<Tile>> chiOptions) {
        if (options.contains(ReactionType.WIN)) {
            return Reaction.of(ReactionType.WIN);
        }
        if (options.contains(ReactionType.KONG)) {
            return Reaction.of(ReactionType.KONG);
        }
        if (options.contains(ReactionType.PUNG)) {
            return Reaction.of(ReactionType.PUNG);
        }
        if (options.contains(ReactionType.CHI) && chiOptions != null && !chiOptions.isEmpty()) {
            return Reaction.of(ReactionType.CHI, MeldData.chi(chiOptions.get(0).get(1),
                chiOptions.get(0)));
        }
        return Reaction.pass();
    }
}
