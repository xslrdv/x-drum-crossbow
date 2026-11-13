package com.xslrdv.xdrumcrossbow.registry;

import com.mojang.serialization.Codec;
import com.xslrdv.xdrumcrossbow.XDrumCrossbow;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, XDrumCrossbow.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AMMO =
        COMPONENTS.registerComponentType("ammo", b -> b
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}