package com.sighs.springmahjong.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import com.sighs.springmahjong.Springmahjong;

public record CDiscard(int tileId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "discard");
    public static final CustomPacketPayload.Type<CDiscard> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, CDiscard> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, CDiscard::tileId,
        CDiscard::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
