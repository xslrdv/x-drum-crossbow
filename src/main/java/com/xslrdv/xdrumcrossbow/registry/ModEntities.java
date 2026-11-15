package com.xslrdv.xdrumcrossbow.registry;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.entity.DrumArrowEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(XDrumCrossbow.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DrumArrowEntity>> DRUM_ARROW = ENTITIES.registerEntityType(
        "drum_arrow",
        DrumArrowEntity::new,
        MobCategory.MISC,
        builder -> builder
            .sized(0.5F, 0.5F) // vanilla block collision box
            .clientTrackingRange(4) // client tracking distance (in chunks)
            .updateInterval(20) // packet synchronization interval (in ticks)
    );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}