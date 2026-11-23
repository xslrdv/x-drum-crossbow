package com.xslrdv.xdrumcrossbow.registry;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> DRUM_BOLT = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(XDrumCrossbow.MODID, "drum_crossbow_bolt")
    );
}
