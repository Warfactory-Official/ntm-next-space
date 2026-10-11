// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.tile.IHeatSource;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbmspace.blockentity.IDysonConverter;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityDysonConverterTU extends BlockEntityMachineBase
        implements IDysonConverter, IHeatSource {

    public int heatEnergy;

    public BlockEntityDysonConverterTU(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_CONVERTER_TU.get(), pos, state, 0);
    }

    @Override
    public boolean provideEnergy(BlockPos pos, long energy) {
        if (!pos.equals(
                worldPosition
                        .relative(BlockMultiblockCore.coreFacing(getBlockState()), 6)
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
    public int getHeatStored(Level level, BlockPos pos) {
        return BlockMultiblockCore.vendsHeatAt(this, pos) ? heatEnergy : 0;
    }

    @Override
    public void useUpHeat(Level level, BlockPos pos, int heat) {
        if (!BlockMultiblockCore.vendsHeatAt(this, pos)) return;
        heatEnergy = Math.max(0, heatEnergy - heat);
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
