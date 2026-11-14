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

import com.hbm.api.tile.HeatHandler;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbmspace.blockentity.DysonEnergyHandler;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DysonConverterTUBlockEntity extends MachineBlockEntity
        implements DysonEnergyHandler, HeatHandler {

    public int heatEnergy;

    public DysonConverterTUBlockEntity(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_CONVERTER_TU.get(), pos, state, 0);
    }

    @Override
    public boolean provideEnergy(BlockPos pos, long energy) {
        if (!pos.equals(
                worldPosition
                        .relative(MultiblockCoreBlock.coreFacing(getBlockState()), 6)
                        .above())) {
            return false;
        }

        if (energy > Integer.MAX_VALUE) {
            heatEnergy = Integer.MAX_VALUE;
        } else {
            heatEnergy += energy;
            if (heatEnergy < 0) heatEnergy = Integer.MAX_VALUE;
        }
        setChanged();
        return true;
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void setHeatStored(int remaining) {
        heatEnergy = remaining;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        heatEnergy = input.getIntOr("heatEnergy", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("heatEnergy", heatEnergy);
    }
}
