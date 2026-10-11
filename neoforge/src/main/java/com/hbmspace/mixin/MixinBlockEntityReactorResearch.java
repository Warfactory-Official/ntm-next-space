// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityReactorResearch;
import com.hbmspace.hazard.NeutronExposureSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEntityReactorResearch.class)
public abstract class MixinBlockEntityReactorResearch {
    @WrapOperation(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/handler/radiation/RadiationSystemNT;incrementRad(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;D)V"))
    private void hbmspace$researchNeutrons(
            ServerLevel level, BlockPos pos, double rad, Operation<Void> original) {
        original.call(level, pos, rad);
        NeutronExposureSpace.radiate(
                level,
                pos,
                (float) ((BlockEntityReactorResearch) (Object) this).heat
                        / BlockEntityReactorResearch.MAX_HEAT
                        * 50F);
    }
}
