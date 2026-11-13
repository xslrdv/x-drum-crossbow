package com.xslrdv.xdrumcrossbow.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xslrdv.xdrumcrossbow.item.DrumCrossbowItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Matrix4f;

public class DrumCrossbowClientExtensions implements IClientItemExtensions {

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack)) {
            return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
        }
        return HumanoidModel.ArmPose.CROSSBOW_HOLD;
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, PlayerRenderState playerState, HumanoidArm arm,
                                           ItemStack stack, float partialTick, float equipProcess, float swingProcess) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.translate(side * 0.56f, -0.52f + equipProcess * -0.6f, -0.72f);

        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && player.getCooldowns().isOnCooldown(stack)) {
            float cd = player.getCooldowns().getCooldownPercent(stack, partialTick);
            float progress = 1.0f - cd;

            float dip = (float) Math.sin(progress * Math.PI);

            poseStack.translate(side * -0.06f * dip, -0.25f * dip, 0.15f * dip);

            poseStack.mulPose(new Matrix4f().rotationZ(side * 0.40f * dip));
            poseStack.mulPose(new Matrix4f().rotationX(0.20f * dip));

            if (progress > 0.4f && progress < 0.65f) {
                float click = (float) Math.sin((progress - 0.4f) / 0.25f * Math.PI);
                poseStack.translate(0f, 0.03f * click, 0.05f * click);
                poseStack.mulPose(new Matrix4f().rotationY(side * -0.10f * click));
            }
        }

        float k = ClientRecoil.get(partialTick);
        if (k > 0f) {
            boolean aim = DrumCrossbowItem.isAiming(player);
            float back = aim ? ClientRecoil.AIM_BACK : ClientRecoil.HIP_BACK;
            float up = aim ? ClientRecoil.AIM_UP : ClientRecoil.HIP_UP;
            float pitch = aim ? ClientRecoil.AIM_PITCH : ClientRecoil.HIP_PITCH;

            poseStack.translate(0f, up * k, back * k);
            poseStack.mulPose(new Matrix4f().rotationX((float) Math.toRadians(pitch * k)));
        }
        return true;
    }
}
