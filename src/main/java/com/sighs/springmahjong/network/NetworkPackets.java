package com.sighs.springmahjong.network;

import com.sighs.springmahjong.game.model.Seat;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import com.sighs.springmahjong.Springmahjong;

import java.util.*;

// Helper: resolve generic CodecOperation method references for Java inference
// ByteBufCodecs::list and ::optional don't resolve well in chained generics
final class CodecHelpers {
    static final StreamCodec<ByteBuf, List<Integer>> INT_LIST =
        ByteBufCodecs.<ByteBuf, Integer>list().apply(ByteBufCodecs.INT);
    static final StreamCodec<ByteBuf, List<List<Integer>>> INT_LIST_LIST =
        ByteBufCodecs.<ByteBuf, List<Integer>>list().apply(INT_LIST);
    static final StreamCodec<ByteBuf, Optional<String>> OPT_STRING =
        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8);
    static final StreamCodec<ByteBuf, Optional<Integer>> OPT_INT =
        ByteBufCodecs.optional(ByteBufCodecs.INT);
}

/**
 * All network packet definitions for SpringMahjong.
 * In 26.1.2, nullable codecs use ByteBufCodecs.optional (returning Optional).
 */

// ======================= C2S Packets =======================

record CJoinTable(Optional<String> pin) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "join_table");
    public static final CustomPacketPayload.Type<CJoinTable> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, CJoinTable> STREAM_CODEC = StreamCodec.composite(
        CodecHelpers.OPT_STRING, CJoinTable::pin,
        CJoinTable::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record CLeaveTable() implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "leave_table");
    public static final CustomPacketPayload.Type<CLeaveTable> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, CLeaveTable> STREAM_CODEC = StreamCodec.unit(new CLeaveTable());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record CSitDown(Seat seat) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "sit_down");
    public static final CustomPacketPayload.Type<CSitDown> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, CSitDown> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, p -> p.seat().ordinal(),
        i -> new CSitDown(Seat.values()[i])
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record CPlayerReady() implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "player_ready");
    public static final CustomPacketPayload.Type<CPlayerReady> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, CPlayerReady> STREAM_CODEC = StreamCodec.unit(new CPlayerReady());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

// ======================= S2C Packets =======================

record STableInfo(UUID yourUUID, List<PatronEntry> patrons, boolean gameInProgress) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "table_info");
    public static final CustomPacketPayload.Type<STableInfo> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, STableInfo> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.core.UUIDUtil.STREAM_CODEC, STableInfo::yourUUID,
        PatronEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), STableInfo::patrons,
        ByteBufCodecs.BOOL, STableInfo::gameInProgress,
        STableInfo::new
    );
    record PatronEntry(UUID uuid, String name, Optional<Integer> seatOrdinal, boolean isBot) {
        public static final StreamCodec<ByteBuf, PatronEntry> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.core.UUIDUtil.STREAM_CODEC, PatronEntry::uuid,
            ByteBufCodecs.STRING_UTF8, PatronEntry::name,
            CodecHelpers.OPT_INT, PatronEntry::seatOrdinal,
            ByteBufCodecs.BOOL, PatronEntry::isBot,
            PatronEntry::new
        );
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SPatronJoined(STableInfo.PatronEntry patron) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "patron_joined");
    public static final CustomPacketPayload.Type<SPatronJoined> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SPatronJoined> STREAM_CODEC = StreamCodec.composite(
        STableInfo.PatronEntry.STREAM_CODEC, SPatronJoined::patron,
        SPatronJoined::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SPatronLeft(UUID uuid) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "patron_left");
    public static final CustomPacketPayload.Type<SPatronLeft> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SPatronLeft> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.core.UUIDUtil.STREAM_CODEC, SPatronLeft::uuid,
        SPatronLeft::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SSitDown(UUID uuid, Seat seat) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "s_sit_down");
    public static final CustomPacketPayload.Type<SSitDown> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SSitDown> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.core.UUIDUtil.STREAM_CODEC, SSitDown::uuid,
        ByteBufCodecs.INT, p -> p.seat().ordinal(),
        (uuid, i) -> new SSitDown(uuid, Seat.values()[i])
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SStandUp(UUID uuid) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "s_stand_up");
    public static final CustomPacketPayload.Type<SStandUp> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SStandUp> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.core.UUIDUtil.STREAM_CODEC, SStandUp::uuid,
        SStandUp::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SStateSync(ClientGameSnapshot snapshot) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "state_sync");
    public static final CustomPacketPayload.Type<SStateSync> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SStateSync> STREAM_CODEC = StreamCodec.composite(
        ClientGameSnapshot.STREAM_CODEC, SStateSync::snapshot,
        SStateSync::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SDealHand(List<Integer> tileIds) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "deal_hand");
    public static final CustomPacketPayload.Type<SDealHand> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SDealHand> STREAM_CODEC = StreamCodec.composite(
        CodecHelpers.INT_LIST, SDealHand::tileIds,
        SDealHand::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SYourDraw(int tileId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "your_draw");
    public static final CustomPacketPayload.Type<SYourDraw> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SYourDraw> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SYourDraw::tileId,
        SYourDraw::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SOpponentDraw(int seatOrdinal) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "opponent_draw");
    public static final CustomPacketPayload.Type<SOpponentDraw> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SOpponentDraw> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SOpponentDraw::seatOrdinal,
        SOpponentDraw::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SReactionRequest(List<Integer> options, List<List<Integer>> chiOptions, long timeoutMs) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "reaction_request");
    public static final CustomPacketPayload.Type<SReactionRequest> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SReactionRequest> STREAM_CODEC = StreamCodec.composite(
        CodecHelpers.INT_LIST, SReactionRequest::options,
        CodecHelpers.INT_LIST_LIST, SReactionRequest::chiOptions,
        ByteBufCodecs.LONG, SReactionRequest::timeoutMs,
        SReactionRequest::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SBroadcastTurn(int seatOrdinal) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "broadcast_turn");
    public static final CustomPacketPayload.Type<SBroadcastTurn> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SBroadcastTurn> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SBroadcastTurn::seatOrdinal,
        SBroadcastTurn::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SBroadcastDiscard(int seatOrdinal, int tileId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "broadcast_discard");
    public static final CustomPacketPayload.Type<SBroadcastDiscard> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SBroadcastDiscard> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SBroadcastDiscard::seatOrdinal,
        ByteBufCodecs.INT, SBroadcastDiscard::tileId,
        SBroadcastDiscard::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SBroadcastMeld(int seatOrdinal, int tileId, int meldType) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "broadcast_meld");
    public static final CustomPacketPayload.Type<SBroadcastMeld> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SBroadcastMeld> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SBroadcastMeld::seatOrdinal,
        ByteBufCodecs.INT, SBroadcastMeld::tileId,
        ByteBufCodecs.INT, SBroadcastMeld::meldType,
        SBroadcastMeld::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SBroadcastSettlement(int winnerSeat, int totalFan, Map<Integer, Integer> scoreChanges) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "settlement");
    public static final CustomPacketPayload.Type<SBroadcastSettlement> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SBroadcastSettlement> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SBroadcastSettlement::winnerSeat,
        ByteBufCodecs.INT, SBroadcastSettlement::totalFan,
        ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.INT, ByteBufCodecs.INT),
            SBroadcastSettlement::scoreChanges,
        SBroadcastSettlement::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SBroadcastGameEnd(Map<Integer, Integer> finalScores) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "game_end");
    public static final CustomPacketPayload.Type<SBroadcastGameEnd> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SBroadcastGameEnd> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.INT, ByteBufCodecs.INT),
            SBroadcastGameEnd::finalScores,
        SBroadcastGameEnd::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

record SError(int code, String message) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "error");
    public static final CustomPacketPayload.Type<SError> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SError> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SError::code,
        ByteBufCodecs.STRING_UTF8, SError::message,
        SError::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
