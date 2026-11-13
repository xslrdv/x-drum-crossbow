package com.xslrdv.xdrumcrossbow.event;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = XDrumCrossbow.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (e.getEntity().getMainHandItem().getItem() instanceof DrumCrossbowItem) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent e) {
        if (e.getEntity().getMainHandItem().getItem() instanceof DrumCrossbowItem) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        Player player = e.getEntity();
        if (!player.level().isClientSide()) {
            DrumCrossbowItem.tickReload(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        DrumCrossbowItem.onPlayerLogout(e.getEntity().getUUID());
    }
}
