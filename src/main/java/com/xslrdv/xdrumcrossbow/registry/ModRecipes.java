package com.xslrdv.xdrumcrossbow.registry;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.recipe.DrumCrossbowRepairRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, XDrumCrossbow.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DrumCrossbowRepairRecipe>> DRUM_CROSSBOW_REPAIR =
        SERIALIZERS.register("crafting_special_drum_crossbow_repair", () -> DrumCrossbowRepairRecipe.SERIALIZER);

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
