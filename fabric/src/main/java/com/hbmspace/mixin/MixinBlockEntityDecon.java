// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityDecon;
import com.hbmspace.hazard.NeutronExposureSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockEntityDecon.class)
public abstract class MixinBlockEntityDecon {
    @WrapOperation(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/extprop/HbmLivingProps;incrementRadiation(Lnet/minecraft/world/entity/LivingEntity;D)V"))
    private void hbmspace$deconActivation(
            LivingEntity entity, double amount, Operation<Void> original) {
        original.call(entity, amount);
        NeutronExposureSpace.decon(entity);
    }

    @Redirect(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/ContaminationUtil;"
                                            + "neutronActivateInventory(Lnet/minecraft/world/entity/player/Player;FF)Z"))
    private boolean hbmspace$replaceLegacyDecay(Player player, float rad, float decay) {
        return false;
    }
}
