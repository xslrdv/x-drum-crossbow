package com.xslrdv.xdrumcrossbow.entity;

import java.util.HashSet;
import java.util.Set;

import com.xslrdv.xdrumcrossbow.registry.ModDamageTypes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DrumArrowEntity extends Arrow {

    private static final EntityDataAccessor<Byte> DATA_PIERCE_LEVEL =
        SynchedEntityData.defineId(DrumArrowEntity.class, EntityDataSerializers.BYTE);

    private double boltBaseDamage = 2.0;
    private final Set<Integer> piercedEntities = new HashSet<>();

    public DrumArrowEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    public DrumArrowEntity(EntityType<? extends Arrow> type, Level level, LivingEntity shooter, ItemStack pickupStack, ItemStack firedFromWeapon) {
        super(type, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());

        if (firedFromWeapon != null && !firedFromWeapon.isEmpty()) {
            var enchantmentLookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var piercingHolder = enchantmentLookup.getOrThrow(Enchantments.PIERCING);
            int pierce = firedFromWeapon.getEnchantmentLevel(piercingHolder);
            this.setBoltPierceLevel((byte) pierce);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PIERCE_LEVEL, (byte) 0);
    }

    public void setBoltPierceLevel(byte level) {
        this.entityData.set(DATA_PIERCE_LEVEL, level);
    }

    public byte getBoltPierceLevel() {
        return this.entityData.get(DATA_PIERCE_LEVEL);
    }

    public void setBoltBaseDamage(double damage) {
        this.boltBaseDamage = damage;
    }

    public double getBoltBaseDamage() {
        return this.boltBaseDamage;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !this.piercedEntities.contains(target.getId());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        byte pierceLevel = this.getBoltPierceLevel();

        // On client remove visual pullback (only if pierce limit is reached)
        if (this.level().isClientSide()) {
            if (target instanceof LivingEntity living && living.isAlive()) {
                this.piercedEntities.add(target.getId());
                if (this.piercedEntities.size() > pierceLevel) {
                    this.discard();
                }
            }
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            float speed = (float) this.getDeltaMovement().length();
            int damage = Mth.ceil(Mth.clamp((double) speed * this.boltBaseDamage, 0.0, 2.147483647E9));

            if (this.isCritArrow()) {
                long critBonus = this.random.nextInt(damage / 2 + 2);
                damage = (int) Math.min(critBonus + (long) damage, 2147483647L);
            }

            Entity owner = this.getOwner();
            DamageSource damageSource = this.damageSources().source(
                ModDamageTypes.DRUM_BOLT,
                this,
                owner != null ? owner : this
            );

            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.setLastHurtMob(target);
            }

            if (target.hurtServer(serverLevel, damageSource, (float) damage)) {
                if (target instanceof LivingEntity livingTarget) {
                    this.doPostHurtEffects(livingTarget);
                    EnchantmentHelper.doPostAttackEffects(serverLevel, livingTarget, damageSource);
                }
                this.playSound(this.getDefaultHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

                if (pierceLevel > 0) {
                    this.piercedEntities.add(target.getId());
                    this.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                    if (this.piercedEntities.size() > pierceLevel) {
                        this.discard();
                    }
                } else {
                    this.discard();
                }
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
                this.setYRot(this.getYRot() + 180.0F);
                this.yRotO += 180.0F;
            }
        }
    }

    // Gravity (vanilla 0.5f)
    @Override
    protected double getDefaultGravity() {
        return 0.025;
    }
}
