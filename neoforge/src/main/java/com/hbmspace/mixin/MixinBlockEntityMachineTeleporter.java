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

import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.blockentity.Synced;
import com.hbm.blockentity.travel.TeleporterBlockEntity;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbmspace.blockentity.machine.SpaceTeleporterTank;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeleporterBlockEntity.class)
public abstract class MixinBlockEntityMachineTeleporter extends BlockEntity
        implements FluidTankAccess, FluidTankHandler, SpaceTeleporterTank {

    @Shadow public @Nullable GlobalPos target;

    @Unique private FluidTankNTM hbmspace$nmass;
    @Unique private FluidTankNTM[] hbmspace$receiving;

    protected MixinBlockEntityMachineTeleporter(
            BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hbmspace$createTank(BlockPos pos, BlockState state, CallbackInfo ci) {
        hbmspace$nmass = new FluidTankNTM(FluidsSpace.nmass(), 16_000);
        hbmspace$receiving = new FluidTankNTM[] {hbmspace$nmass};
    }

    @Override
    public FluidTankNTM hbmspace$tank() {
        return hbmspace$nmass;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return hbmspace$receiving;
    }

    @Override
    public int tankCount() {
        return hbmspace$receiving.length;
    }

    @Override
    public FluidTankNTM tank(int index) {
        return hbmspace$receiving[index];
    }

    @Override
    public long transferFluid(
            Fluid type, DataComponentPatch components, int pressure, long amount) {
        long refused = FluidTankHandler.super.transferFluid(type, components, pressure, amount);
        if (refused != amount) ((Synced) (Object) this).markChanged();
        return refused;
    }

    @ModifyReturnValue(method = "admitTravel", at = @At("RETURN"))
    private boolean hbmspace$chargeCrossing(boolean admitted, Entity entity) {
        if (!admitted || entity.level().dimension().equals(target.dimension())) return admitted;
        if (hbmspace$nmass.getFill() < 100) return false;
        hbmspace$nmass.setFill(hbmspace$nmass.getFill() - 100);
        setChanged();
        ((Synced) (Object) this).markChanged();
        return true;
    }

    @ModifyReturnValue(method = "canEmitMote", at = @At("RETURN"))
    private boolean hbmspace$chargedMotes(boolean charged) {
        return charged && hbmspace$nmass.getFill() >= 100;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void hbmspace$loadTank(ValueInput input, CallbackInfo ci) {
        input.child("tt").ifPresent(hbmspace$nmass::deserialize);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void hbmspace$saveTank(ValueOutput output, CallbackInfo ci) {
        hbmspace$nmass.serialize(output.child("tt"));
    }

    @Inject(method = "writeSyncUnit", at = @At("TAIL"))
    private void hbmspace$writeTank(int unit, ByteBuf output, CallbackInfo ci) {
        if (unit == 1) hbmspace$nmass.packetSerialize(output);
    }

    @Inject(method = "readSyncUnit", at = @At("TAIL"))
    private void hbmspace$readTank(int unit, ByteBuf input, CallbackInfo ci) {
        if (unit == 1) hbmspace$nmass.packetDeserialize(input);
    }
}
