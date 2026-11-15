package com.xslrdv.xdrumcrossbow.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xslrdv.xdrumcrossbow.XDrumCrossbow;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;
import com.xslrdv.xdrumcrossbow.network.ReloadPayload;
import com.xslrdv.xdrumcrossbow.network.ShootPayload;
import com.xslrdv.xdrumcrossbow.registry.ModEntities;
import com.xslrdv.xdrumcrossbow.registry.ModItems;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = XDrumCrossbow.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static final float AIM_FOV_MULTIPLIER = 0.85f;

    public static KeyMapping RELOAD_KEY;

    @SubscribeEvent
    public static void onKeys(RegisterKeyMappingsEvent e) {
        RELOAD_KEY = new KeyMapping("key.xdrumcrossbow.reload", InputConstants.KEY_R, KeyMapping.Category.GAMEPLAY);
        e.register(RELOAD_KEY);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandItem();
        boolean holding = held.getItem() instanceof DrumCrossbowItem;

        if (RELOAD_KEY != null) {
            while (RELOAD_KEY.consumeClick()) {
                if (holding && !mc.player.getCooldowns().isOnCooldown(held)
                    && DrumCrossbowItem.getAmmo(held) < DrumCrossbowItem.MAX_AMMO) {
                    mc.player.stopUsingItem();
                    ClientPacketDistributor.sendToServer(new ReloadPayload());
                }
            }
        }

        if (holding && mc.options.keyAttack.isDown()) {
            if (!mc.player.getCooldowns().isOnCooldown(held)) {
                ClientPacketDistributor.sendToServer(new ShootPayload());
            }
        }
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && e.isAttack()
            && mc.player.getMainHandItem().getItem() instanceof DrumCrossbowItem) {
            e.setCanceled(true);
            e.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent e) {
        Player p = e.getPlayer();
        if (DrumCrossbowItem.isAiming(p) && !p.getCooldowns().isOnCooldown(p.getMainHandItem())) {
            e.setNewFovModifier(e.getNewFovModifier() * AIM_FOV_MULTIPLIER);
        }
    }

    @SubscribeEvent
    public static void onClientExtensions(RegisterClientExtensionsEvent e) {
        e.registerItem(new DrumCrossbowClientExtensions(), ModItems.DRUM_CROSSBOW.get());
    }

    @SubscribeEvent
    public static void onGui(RegisterGuiLayersEvent e) {
        e.registerAboveAll(Identifier.fromNamespaceAndPath(XDrumCrossbow.MODID, "ammo_hud"),
            ClientEvents::renderHud);
    }

    private static void renderHud(GuiGraphicsExtractor g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DrumCrossbowItem)) return;

        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.ARROW)) total += s.getCount();
        }

        String loaded = String.valueOf(DrumCrossbowItem.getAmmo(stack));
        String all = String.valueOf(total);
        Font font = mc.font;

        float bigScale = 2f, smallScale = 1f;
        int bigW = (int) (font.width(loaded) * bigScale);
        int smallW = (int) (font.width(all) * smallScale);
        int gap = 8;

        Component title = stack.getHoverName();
        int titleW = font.width(title);
        int totalAmmoW = bigW + gap + smallW;

        int marginRight = 60;
        int marginBottom = 46;

        int blockWidth = Math.max(titleW, totalAmmoW);

        int x = g.guiWidth() - marginRight - blockWidth;
        int yBottom = g.guiHeight() - marginBottom;

        int titleY = (int) (yBottom - font.lineHeight * bigScale) - font.lineHeight - 3;

        g.text(font, title, x, titleY, 0xFFFFFFFF, true);

        g.pose().pushMatrix();
        g.pose().translate(x, yBottom - font.lineHeight * bigScale);
        g.pose().scale(bigScale, bigScale);
        g.text(font, loaded, 0, 0, 0xFFFFFFFF, true);
        g.pose().popMatrix();

        g.pose().pushMatrix();
        g.pose().translate(x + bigW + gap, yBottom - font.lineHeight * bigScale);
        g.pose().scale(smallScale, smallScale);
        g.text(font, all, 0, 0, 0xFFAAAAAA, true);
        g.pose().popMatrix();
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRUM_ARROW.get(), TippableArrowRenderer::new);
    }
}
