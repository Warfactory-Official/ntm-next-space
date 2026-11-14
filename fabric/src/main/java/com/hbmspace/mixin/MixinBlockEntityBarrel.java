/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.storage.FluidBarrelBlockEntity;
import com.hbm.explosion.vanillant.ComposableExplosion;
import com.hbm.interfaces.OverpressureHandler;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbmspace.explosion.BarrelPressureSpace;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBarrelBlockEntity.class)
public abstract class MixinBlockEntityBarrel implements OverpressureHandler {
    @Shadow @Final public FluidTankNTM tank;
    @Unique private boolean hbmspace$overpressured;
    @Unique private static final FluidTankNTM[] hbmspace$NO_TANKS = {};

    @Override
    public void explode(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || hbmspace$overpressured) return;
        Fluid fluid = tank.getTankType();
        int power = Math.min(tank.getFill() / 100, 90);
        if ((fluid != NTMFluids.AMAT && fluid != NTMFluids.ASCHRAB) || power <= 0) {
            BarrelPressureSpace.ordinary(server, pos);
            return;
        }
        hbmspace$overpressured = true;
        BarrelPressureSpace.retire(server, pos);
        BarrelPressureSpace.emit(server, pos, fluid, power);
    }

    @Inject(method = "onGraphLoad", at = @At("HEAD"), cancellable = true)
    private void hbmspace$retiredGraphLoad(ServerLevel level, CallbackInfo ci) {
        if (hbmspace$overpressured) {
            BarrelPressureSpace.retire(
                    level, ((FluidBarrelBlockEntity) (Object) this).getBlockPos());
            ci.cancel();
        }
    }

    @Inject(method = "refreshBufferNodes", at = @At("HEAD"), cancellable = true)
    private void hbmspace$retiredBufferNodes(CallbackInfo ci) {
        if (hbmspace$overpressured) ci.cancel();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void hbmspace$readRetirement(ValueInput input, CallbackInfo ci) {
        hbmspace$overpressured = input.getBooleanOr("hbmspace_overpressured", false);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void hbmspace$saveRetirement(ValueOutput output, CallbackInfo ci) {
        if (hbmspace$overpressured) output.putBoolean("hbmspace_overpressured", true);
    }

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void hbmspace$retiredTick(CallbackInfo ci) {
        if (hbmspace$overpressured) {
            ((FluidBarrelBlockEntity) (Object) this).networkPackNT(50);
            ci.cancel();
        }
    }

    @ModifyReturnValue(
            method = {"getDemand", "getFluidAvailable", "getProviderSpeed", "getReceiverSpeed"},
            at = @At("RETURN"))
    private long hbmspace$retiredCapacity(long capacity) {
        return hbmspace$overpressured ? 0L : capacity;
    }

    @ModifyReturnValue(method = "getSendingTanks", at = @At("RETURN"))
    private FluidTankNTM[] hbmspace$retiredTanks(FluidTankNTM[] tanks) {
        return hbmspace$overpressured ? hbmspace$NO_TANKS : tanks;
    }

    @Inject(method = "transferFluid", at = @At("HEAD"), cancellable = true)
    private void hbmspace$refuseRefill(
            Fluid fluid,
            DataComponentPatch components,
            int pressure,
            long amount,
            CallbackInfoReturnable<Long> cir) {
        if (hbmspace$overpressured) cir.setReturnValue(amount);
    }

    @WrapOperation(
            method = "checkHazards",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V",
                            ordinal = 0))
    private void hbmspace$antimatterSpill(
            Level level,
            Entity source,
            double x,
            double y,
            double z,
            float strength,
            boolean fire,
            Level.ExplosionInteraction interaction,
            Operation<Void> original) {
        new ComposableExplosion(level, x - 0.5, y - 0.5, z - 0.5, strength).antimatter().explode();
    }

    @Inject(method = "useUpFluid", at = @At("HEAD"), cancellable = true)
    private void hbmspace$refuseDrain(
            Fluid fluid,
            DataComponentPatch components,
            int pressure,
            long amount,
            CallbackInfo ci) {
        if (hbmspace$overpressured) ci.cancel();
    }
}
