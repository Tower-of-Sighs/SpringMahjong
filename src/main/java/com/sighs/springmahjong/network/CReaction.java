package com.sighs.springmahjong.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import com.sighs.springmahjong.Springmahjong;
import java.util.Optional;

public record CReaction(int reactionType, Optional<Integer> chiTileId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "reaction");
    public static final CustomPacketPayload.Type<CReaction> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, CReaction> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, CReaction::reactionType,
        CodecHelpers.OPT_INT, CReaction::chiTileId,
        (reactionType, chiTileId) -> new CReaction(reactionType, chiTileId)
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
