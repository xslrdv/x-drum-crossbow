package com.xslrdv.xdrumcrossbow.network;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReloadPayload() implements CustomPacketPayload {
    public static final Type<ReloadPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(XDrumCrossbow.MODID, "reload"));
    public static final StreamCodec<ByteBuf, ReloadPayload> CODEC = StreamCodec.unit(new ReloadPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReloadPayload payload, IPayloadContext ctx) {
        DrumCrossbowItem.startReload(ctx.player());
    }
}
