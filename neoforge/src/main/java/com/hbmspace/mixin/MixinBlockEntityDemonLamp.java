// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityDemonLamp;
import com.hbm.util.ContaminationUtil;
import com.hbmspace.hazard.NeutronExposureSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEntityDemonLamp.class)
public abstract class MixinBlockEntityDemonLamp {
    @WrapOperation(
            method = "radiate",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/ContaminationUtil;"
                                            + "contaminate(Lnet/minecraft/world/entity/LivingEntity;Lcom/hbm/util/ContaminationUtil$HazardType;"
                                            + "Lcom/hbm/util/ContaminationUtil$ContaminationType;D)Z"))
    private static boolean hbmspace$neutronLamp(
            LivingEntity entity,
            ContaminationUtil.HazardType hazard,
            ContaminationUtil.ContaminationType type,
            double dose,
            Operation<Boolean> original,
            @Local(argsOnly = true) BlockPos pos) {
        if (entity.getX() == pos.getX() + 0.5D
                && entity.getEyeY() == pos.getY() + 0.5D
                && entity.getZ() == pos.getZ() + 0.5D) dose = BlockEntityDemonLamp.RADS;
        boolean applied = original.call(entity, hazard, type, dose);
        NeutronExposureSpace.irradiate(entity, (float) dose);
        return applied;
    }
}
