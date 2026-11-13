package com.xslrdv.xdrumcrossbow.recipe;

import com.mojang.serialization.MapCodec;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DrumCrossbowRepairRecipe extends CustomRecipe {
    public static final DrumCrossbowRepairRecipe INSTANCE = new DrumCrossbowRepairRecipe();
    public static final MapCodec<DrumCrossbowRepairRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, DrumCrossbowRepairRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<DrumCrossbowRepairRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public DrumCrossbowRepairRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 5) {
            return false;
        }

        boolean foundCrossbow = false;
        boolean foundIron = false;
        boolean foundString = false;
        boolean foundGold = false;
        boolean foundPlanks = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof DrumCrossbowItem) {
                if (foundCrossbow) {
                    return false;
                }
                foundCrossbow = true;
            } else if (stack.is(Items.IRON_INGOT)) {
                if (foundIron) {
                    return false;
                }
                foundIron = true;
            } else if (stack.is(Items.STRING)) {
                if (foundString) {
                    return false;
                }
                foundString = true;
            } else if (stack.is(Items.GOLD_NUGGET)) {
                if (foundGold) {
                    return false;
                }
                foundGold = true;
            } else if (stack.is(ItemTags.PLANKS)) {
                if (foundPlanks) {
                    return false;
                }
                foundPlanks = true;
            } else {
                return false;
            }
        }

        return foundCrossbow && foundIron && foundString && foundGold && foundPlanks;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof DrumCrossbowItem) {
                ItemStack result = stack.copyWithCount(1);
                result.setDamageValue(0);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
