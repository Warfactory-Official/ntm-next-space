/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.CoolingTowerParticleOptions;
import com.hbmspace.blockentity.DysonEnergyHandler;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.GasExchange;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DysonConverterAnatmogenesisBlockEntity extends MachineBlockEntity
        implements DysonEnergyHandler, SyncUnitSchema {
    private static final long HE_TO_MB = 28_800_000;

    public Fluid fluid;

    {
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$fluid(NTMFluids.OXYGEN);
    }

    public boolean isEmitting;

    {
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$isEmitting(true);
    }

    public long gasProduced;
    public boolean isConverting;

    public DysonConverterAnatmogenesisBlockEntity(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_CONVERTER_ANATMOGENESIS.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$isConverting(gasProduced > 0);

        networkPackNT(250);
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$gasProduced(0);
    }

    @Override
    public void tickClient() {
        if (!isConverting) return;
        RandomSource random = level.getRandom();
        CoolingTowerParticleOptions plume =
                new CoolingTowerParticleOptions.Builder()
                        .setLift(0.5F)
                        .setBaseScale(0.8F)
                        .setMaxScale(4F)
                        .setLife(100 + random.nextInt(50))
                        .setColor(AirPumpBlockEntity.color(fluid))
                        .build();
        level.addParticle(
                plume,
                true,
                false,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 3.25,
                worldPosition.getZ() + 0.5,
                random.nextDouble() - 0.5,
                random.nextDouble() * 2,
                random.nextDouble() - 0.5);
    }

    @Override
    public boolean provideEnergy(BlockPos pos, long energy) {
        if (!pos.equals(
                worldPosition
                        .relative(MultiblockCoreBlock.coreFacing(getBlockState()), 5)
                        .above())) {
            return false;
        }

        long volume = energy / HE_TO_MB;
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$gasProduced(
                (long) (gasProduced + (volume)));

        if (isEmitting) GasExchange.release((ServerLevel) level, fluid, volume);
        else GasExchange.capture((ServerLevel) level, fluid, volume);

        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("fluid")
                .map(Identifier::tryParse)
                .flatMap(BuiltInRegistries.FLUID::getOptional)
                .ifPresent(read -> hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$fluid(read));
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$isEmitting(
                input.getBooleanOr("emit", true));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        output.putBoolean("emit", isEmitting);
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(isConverting);
        output.writeLong(gasProduced);
        output.writeInt(BuiltInRegistries.FLUID.getId(fluid));
        output.writeBoolean(isEmitting);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$isConverting(input.readBoolean());
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$gasProduced(input.readLong());
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$fluid(
                BuiltInRegistries.FLUID.byId(input.readInt()));
        hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$isEmitting(input.readBoolean());
    }

    public final void hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$fluid(Fluid next) {
        if (this.fluid != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.fluid);
            this.fluid = next;
            if (bound) bindSyncValue(next, 3, 1L);
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$isEmitting(boolean next) {
        if (this.isEmitting != next) {
            this.isEmitting = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$gasProduced(long next) {
        if (this.gasProduced != next) {
            this.gasProduced = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonConverterAnatmogenesisBlockEntity$isConverting(
            boolean next) {
        if (this.isConverting != next) {
            this.isConverting = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(
                DysonConverterAnatmogenesisBlockEntity.class, "fluid", 3, 1L);
    }
}
