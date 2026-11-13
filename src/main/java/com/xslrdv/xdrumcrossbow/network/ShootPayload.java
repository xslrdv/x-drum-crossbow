package com.xslrdv.xdrumcrossbow.network;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShootPayload() implements CustomPacketPayload {
    public static final Type<ShootPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(XDrumCrossbow.MODID, "shoot"));
    public static final StreamCodec<ByteBuf, ShootPayload> CODEC = StreamCodec.unit(new ShootPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShootPayload payload, IPayloadContext ctx) {
        DrumCrossbowItem.tryShoot(ctx.player());
    }
}
