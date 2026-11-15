package com.xslrdv.xdrumcrossbow.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DrumArrowEntity extends Arrow {

    public DrumArrowEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    public DrumArrowEntity(EntityType<? extends Arrow> type, Level level, LivingEntity shooter, ItemStack pickupStack, ItemStack firedFromWeapon) {
        super(type, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    // Disable  air resistance (vanilla 0.99F)
    @Override
    protected float getWaterInertia() {
        return 1.0F;
    }

    // Gravity (vanilla 0.5f)
    @Override
    protected double getDefaultGravity() {
        return 0.025;
    }
}