package com.xslrdv.xdrumcrossbow.registry;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(XDrumCrossbow.MODID);

    public static final DeferredItem<Item> DRUM_CROSSBOW = ITEMS.registerItem("drum_crossbow", DrumCrossbowItem::new);
    
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
