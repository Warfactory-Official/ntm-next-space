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

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.util.SkyColumns;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SolarPanelBlockEntity extends MachineBlockEntity implements EnergyHandler {
    public static final long MAX_POWER = 1_000;

    public long power;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.SOLAR_PANEL.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        int sun = level.getBrightness(LightLayer.SKY, worldPosition) - level.getSkyDarken() - 11;
        if (sun <= 0 || !seesSky(worldPosition.above())) return;

        long charged = Math.min(power + getOutput(sun), MAX_POWER);
        if (charged == power) return;
        power = charged;
        setChanged();
    }

    private boolean seesSky(BlockPos pos) {
        return pos.getY()
                >= SkyColumns.lowestSourceY(level.getChunkAt(pos), pos.getX(), pos.getZ());
    }

    public long getOutput(int sun) {
        return Mth.ceil(
                sun
                        * 25
                        * WorldProviderCelestial.getSunPower(
                                level, worldPosition.getX(), worldPosition.getZ()));
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        power = input.getLongOr("power", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
    }
}
