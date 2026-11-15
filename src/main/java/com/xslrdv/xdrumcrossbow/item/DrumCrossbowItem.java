package com.xslrdv.xdrumcrossbow.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.xslrdv.xdrumcrossbow.entity.DrumArrowEntity;
import com.xslrdv.xdrumcrossbow.registry.ModComponents;
import com.xslrdv.xdrumcrossbow.registry.ModEntities;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class DrumCrossbowItem extends Item {
    public static final int MAX_AMMO = 10;
    public static final int FIRE_DELAY = 6;
    public static final int EMPTY_CLICK_DELAY = 3;
    public static final int RELOAD_TICKS = 30;

    public static final float ARROW_SPEED = 3.75f;
    public static final float HIP_INACCURACY = 0.5f;
    public static final float AIM_SPEED_MULTIPLIER = 0.7f;

    public static final double HIP_SIDE = 0.35;
    public static final double HIP_DOWN = 0.25;
    public static final double HIP_FORWARD = 0.5;
    public static final double CONVERGE_DISTANCE = 20.0;

    private static final Map<UUID, Long> LAST_SHOT = new HashMap<>();
    private static final Map<UUID, Long> RELOAD_FINISH = new HashMap<>();

    public DrumCrossbowItem(Properties props) {
        super(props
            .stacksTo(1)
            .durability(1024) // can shoot 1024 arrow to fully break
            .enchantable(1)
            .component(DataComponents.USE_EFFECTS, new UseEffects(false, true, AIM_SPEED_MULTIPLIER)));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack.is(newStack.getItem());
    }

    public static boolean isAiming(Player player) {
        return player.isUsingItem() && player.getUseItem().getItem() instanceof DrumCrossbowItem;
    }

    public static int getAmmo(ItemStack stack) {
        return stack.getOrDefault(ModComponents.AMMO.get(), 0);
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        ammo = Mth.clamp(ammo, 0, MAX_AMMO);
        stack.set(ModComponents.AMMO.get(), ammo);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
            new CustomModelData(List.of(), List.of(ammo > 0), List.of(), List.of()));
    }

    public static void tryShoot(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DrumCrossbowItem)) return;
        if (player.getCooldowns().isOnCooldown(stack)) return;

        Level level = player.level();
        long now = level.getGameTime();
        Long last = LAST_SHOT.get(player.getUUID());
        if (last != null && now - last < FIRE_DELAY) return;

        int ammo = getAmmo(stack);
        if (ammo <= 0) {
            if (last == null || now - last >= EMPTY_CLICK_DELAY) {
                LAST_SHOT.put(player.getUUID(), now);
                level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL,
                    SoundSource.PLAYERS, 1f, 1f);
            }
            return;
        }
        LAST_SHOT.put(player.getUUID(), now);

        int projectileCount = 1;
        if (level instanceof ServerLevel serverLevel) {
            projectileCount = EnchantmentHelper.processProjectileCount(serverLevel, stack, player, 1);
        }

        Vec3 aimTarget = getAimCollisionPoint(player, 128.0); // Точка коллизии взгляда

        for (int i = 0; i < projectileCount; i++) {
            // Our custom arrow
            DrumArrowEntity arrow = new DrumArrowEntity(ModEntities.DRUM_ARROW.get(), level, player, new ItemStack(Items.ARROW), stack);
            float angleOffset = (projectileCount > 1) ? ((i == 1) ? -10f : (i == 2) ? 10f : 0f) : 0f;

            if (isAiming(player)) {
                Vec3 spawn = player.getEyePosition(1.0F);
                arrow.setPos(spawn.x, spawn.y, spawn.z);

                Vec3 shootDir = aimTarget.subtract(spawn).normalize();
                if (angleOffset != 0f) {
                    shootDir = shootDir.yRot((float) Math.toRadians(-angleOffset));
                }
                arrow.shoot(shootDir.x, shootDir.y, shootDir.z, ARROW_SPEED, 0f);
            } else {
                shootFromHip(player, arrow, aimTarget, angleOffset);
            }

            if (i > 0 || player.getAbilities().instabuild) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(arrow);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT,
            SoundSource.PLAYERS, 1f, 1f);

        setAmmo(stack, ammo - 1);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    public static boolean hasArrows(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(Items.ARROW)) return true;
        }
        return false;
    }

    public static void startReload(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DrumCrossbowItem)) return;
        if (player.getCooldowns().isOnCooldown(stack)) return;

        int current = getAmmo(stack);
        if (current >= MAX_AMMO) return;

        if (!player.getAbilities().instabuild && !hasArrows(player)) {
            player.level().playSound(null, player.blockPosition(),
                SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1f, 1.2f);
            return;
        }

        int reloadTicks = RELOAD_TICKS;
        if (player.level() instanceof ServerLevel) {
            reloadTicks = Math.round(EnchantmentHelper.modifyCrossbowChargingTime(stack, player, RELOAD_TICKS));
            reloadTicks = Math.max(5, reloadTicks);
        }

        player.stopUsingItem();
        player.getCooldowns().addCooldown(stack, reloadTicks);
        RELOAD_FINISH.put(player.getUUID(), player.level().getGameTime() + reloadTicks);

        player.level().playSound(null, player.blockPosition(),
            SoundEvents.CROSSBOW_LOADING_START.value(), SoundSource.PLAYERS, 1f, 1f);
    }

    public static void tickReload(Player player) {
        Long finish = RELOAD_FINISH.get(player.getUUID());
        if (finish == null) return;

        long now = player.level().getGameTime();

        if (now == finish - (RELOAD_TICKS / 2)) {
            player.level().playSound(null, player.blockPosition(),
                SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.PLAYERS, 1f, 1f);
        }

        if (now >= finish) {
            RELOAD_FINISH.remove(player.getUUID());
            finishReload(player);
        }
    }

    private static void finishReload(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DrumCrossbowItem)) return;

        int current = getAmmo(stack);
        int need = MAX_AMMO - current;
        if (need <= 0) return;

        int taken = 0;
        if (player.getAbilities().instabuild) {
            taken = need;
        } else {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize() && taken < need; i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(Items.ARROW)) {
                    int t = Math.min(s.getCount(), need - taken);
                    s.shrink(t);
                    taken += t;
                }
            }
        }

        if (taken > 0) {
            setAmmo(stack, current + taken);
            player.level().playSound(null, player.blockPosition(),
                SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1f, 1f);
        }
    }

    public static void onPlayerLogout(UUID uuid) {
        LAST_SHOT.remove(uuid);
        RELOAD_FINISH.remove(uuid);
    }

private static void shootFromHip(Player player, DrumArrowEntity arrow, Vec3 aimTarget, float angleOffset) {
    Vec3 eye = player.getEyePosition(1.0F);
    Vec3 look = player.getViewVector(1.0F);

    double yaw = Math.toRadians(player.getYRot() + angleOffset);
    Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
    Vec3 up = right.cross(look);
    double side = player.getMainArm() == HumanoidArm.LEFT ? -1 : 1;

    Vec3 spawn = eye
        .add(right.scale(HIP_SIDE * side))
        .add(up.scale(-HIP_DOWN))
        .add(look.scale(HIP_FORWARD));

    Vec3 shootDir = aimTarget.subtract(spawn).normalize();
    if (angleOffset != 0f) {
        shootDir = shootDir.yRot((float) Math.toRadians(-angleOffset));
    }

    arrow.setPos(spawn.x, spawn.y, spawn.z);
    arrow.shoot(shootDir.x, shootDir.y, shootDir.z, ARROW_SPEED, HIP_INACCURACY);
}

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        int ammo = getAmmo(stack);

        tooltipAdder.accept(Component.translatable("tooltip.xdrumcrossbow.ammo", ammo)
            .withStyle(ChatFormatting.WHITE));
        tooltipAdder.accept(Component.translatable("tooltip.xdrumcrossbow.ammo_type")
            .withStyle(ChatFormatting.WHITE));
        tooltipAdder.accept(Component.translatable("tooltip.xdrumcrossbow.range")
            .withStyle(ChatFormatting.WHITE));
    }


    private static Vec3 getAimCollisionPoint(Player player, double maxRange) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(maxRange));

        // Check for collision with blocks
        HitResult blockHit = player.level().clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
        ));
        Vec3 targetPos = blockHit.getLocation();
        double closestDistSqr = eye.distanceToSqr(targetPos);

        // Check for collision with entities - Important
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(maxRange)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            player.level(), player, eye, targetPos, searchBox,
            entity -> !entity.isSpectator() && entity.isPickable(), 0.3f
        );
        if (entityHit != null && eye.distanceToSqr(entityHit.getLocation()) < closestDistSqr) {
            targetPos = entityHit.getLocation();
        }

        return targetPos;
    }
}
