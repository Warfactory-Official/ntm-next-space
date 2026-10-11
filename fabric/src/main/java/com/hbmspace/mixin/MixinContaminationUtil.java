// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ModDataComponents;
import com.hbm.util.ContaminationUtil;
import com.hbmspace.hazard.NeutronExposureSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContaminationUtil.class)
public abstract class MixinContaminationUtil {
    @Shadow
    private static ChatFormatting prefixFromRad(double rads) {
        throw new AssertionError();
    }

    @Inject(method = "getPlayerRads", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$fieldRate(
            LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(HbmLivingProps.getRadBuf(entity));
    }

    @Inject(
            method = "printGeigerData",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/player/Player;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
                            ordinal = 5))
    private static void hbmspace$activationReading(Player player, CallbackInfo ci) {
        double activation = ((int) (HbmLivingProps.getNeutron(player) * 10D)) / 10D;
        player.sendSystemMessage(
                Component.translatable("geiger.playerAct")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(" ")
                        .append(
                                Component.translatable(
                                                "desc.hbmspace.geiger.activation", activation)
                                        .withStyle(
                                                prefixFromRad(HbmLivingProps.getRadBuf(player)))));
    }

    @Inject(method = "contaminate", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$neutronExposure(
            LivingEntity entity,
            ContaminationUtil.HazardType hazard,
            ContaminationUtil.ContaminationType type,
            double amount,
            CallbackInfoReturnable<Boolean> cir) {
        if (hazard == ContaminationUtil.HazardType.NEUTRON) {
            cir.setReturnValue(NeutronExposureSpace.contaminate(entity, type, amount));
        }
    }

    @Inject(method = "getNeutronRads", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$activatedRadioactiveItems(
            ItemStack stack, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(NeutronExposureSpace.activation(stack) * stack.getCount());
    }

    @Inject(method = "neutronActivateItem", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$activation(
            ItemStack stack, float rad, float decay, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        Float before = stack.get(ModDataComponents.NEUTRON_ACTIVATION.get());
        float next = Math.max(0F, (before == null ? 0F : before) * decay + rad / stack.getCount());
        if (next < 1E-5F) {
            stack.remove(ModDataComponents.NEUTRON_ACTIVATION.get());
            cir.setReturnValue(before != null);
        } else {
            stack.set(ModDataComponents.NEUTRON_ACTIVATION.get(), next);
            cir.setReturnValue(before == null || next != before);
        }
    }

    @Inject(method = "neutronActivateInventory", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$allCarriedSlots(
            Player player, float rad, float decay, CallbackInfoReturnable<Boolean> cir) {
        boolean changed = false;
        for (int slot = 0; slot <= Inventory.SLOT_OFFHAND; slot++) {
            changed |=
                    ContaminationUtil.neutronActivateItem(
                            player.getInventory().getItem(slot), rad, decay);
        }
        cir.setReturnValue(changed);
    }
}
