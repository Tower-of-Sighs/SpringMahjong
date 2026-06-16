package com.sighs.springmahjong.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import com.sighs.springmahjong.Springmahjong;

public record CStandUp() implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Springmahjong.MODID, "stand_up");
    public static final CustomPacketPayload.Type<CStandUp> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, CStandUp> STREAM_CODEC = StreamCodec.unit(new CStandUp());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
