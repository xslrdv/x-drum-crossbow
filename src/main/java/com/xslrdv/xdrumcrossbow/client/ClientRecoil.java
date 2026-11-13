package com.xslrdv.xdrumcrossbow.client;

import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = XDrumCrossbow.MODID, value = Dist.CLIENT)
public class ClientRecoil {
    public static final int DURATION = 5;

    public static final float HIP_BACK = 0.07f;
    public static final float HIP_UP = 0.015f;
    public static final float HIP_PITCH = 4.0f;

    public static final float AIM_BACK = 0.03f;
    public static final float AIM_UP = 0.005f;
    public static final float AIM_PITCH = 1.5f;

    private static float prev = 0f;
    private static float cur = 0f;
    private static int lastAmmo = -1;

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post e) {
        LocalPlayer player = Minecraft.getInstance().player;
        prev = cur;
        cur = Math.max(0f, cur - 1f / DURATION);

        if (player == null) {
            prev = cur = 0f;
            lastAmmo = -1;
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof DrumCrossbowItem) {
            int ammo = DrumCrossbowItem.getAmmo(held);
            if (lastAmmo >= 0 && ammo < lastAmmo) {
                cur = 1f;
            }
            lastAmmo = ammo;
        } else {
            lastAmmo = -1;
        }
    }

    public static float get(float partialTick) {
        float v = Mth.lerp(partialTick, prev, cur);
        return v * v;
    }
}
