package com.sighs.springmahjong.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * Lightweight snapshot sent to clients for state synchronization.
 * Uses manual StreamCodec to bypass composite() 8-field limit.
 */
public record ClientGameSnapshot(
    Map<Integer, List<Integer>> melds,
    Map<Integer, List<Integer>> discards,
    Map<Integer, Integer> handSizes,
    Map<Integer, Integer> scores,
    Optional<Integer> currentTurn,
    int remainingWall,
    int roundWindOrdinal,
    int dealerSeatOrdinal,
    int stageOrdinal,
    Optional<Integer> yourSeat,
    Optional<List<Integer>> yourTiles,
    Optional<Map<Integer, List<Integer>>> allHands,
    Optional<Integer> lastDraw
) {
    // Helper codecs
    private static final StreamCodec<ByteBuf, List<Integer>> INT_LIST =
        ByteBufCodecs.<ByteBuf, Integer>list().apply(ByteBufCodecs.INT);
    private static final StreamCodec<ByteBuf, Map<Integer, List<Integer>>> INT_LIST_MAP =
        ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.INT, INT_LIST);
    private static final StreamCodec<ByteBuf, Map<Integer, Integer>> INT_INT_MAP =
        ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.INT, ByteBufCodecs.INT);

    // Manual codec to handle 14 fields (composite supports max 8)
    public static final StreamCodec<ByteBuf, ClientGameSnapshot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClientGameSnapshot decode(ByteBuf buf) {
            return new ClientGameSnapshot(
                INT_LIST_MAP.decode(buf),
                INT_LIST_MAP.decode(buf),
                INT_INT_MAP.decode(buf),
                INT_INT_MAP.decode(buf),
                ByteBufCodecs.optional(ByteBufCodecs.INT).decode(buf),
                ByteBufCodecs.INT.decode(buf),
                ByteBufCodecs.INT.decode(buf),
                ByteBufCodecs.INT.decode(buf),
                ByteBufCodecs.INT.decode(buf),
                ByteBufCodecs.optional(ByteBufCodecs.INT).decode(buf),
                ByteBufCodecs.optional(INT_LIST).decode(buf),
                ByteBufCodecs.optional(INT_LIST_MAP).decode(buf),
                ByteBufCodecs.optional(ByteBufCodecs.INT).decode(buf)
            );
        }

        @Override
        public void encode(ByteBuf buf, ClientGameSnapshot v) {
            INT_LIST_MAP.encode(buf, v.melds());
            INT_LIST_MAP.encode(buf, v.discards());
            INT_INT_MAP.encode(buf, v.handSizes());
            INT_INT_MAP.encode(buf, v.scores());
            ByteBufCodecs.optional(ByteBufCodecs.INT).encode(buf, v.currentTurn());
            ByteBufCodecs.INT.encode(buf, v.remainingWall());
            ByteBufCodecs.INT.encode(buf, v.roundWindOrdinal());
            ByteBufCodecs.INT.encode(buf, v.dealerSeatOrdinal());
            ByteBufCodecs.INT.encode(buf, v.stageOrdinal());
            ByteBufCodecs.optional(ByteBufCodecs.INT).encode(buf, v.yourSeat());
            ByteBufCodecs.optional(INT_LIST).encode(buf, v.yourTiles());
            ByteBufCodecs.optional(INT_LIST_MAP).encode(buf, v.allHands());
            ByteBufCodecs.optional(ByteBufCodecs.INT).encode(buf, v.lastDraw());
        }
    };
}
