// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.machine.BlockEntityCondenser;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.data.SpaceData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityRadiator extends BlockEntityCondenser {

    public BlockEntityRadiator(BlockPos pos, BlockState state) {
        super(
                StationBlockEntitiesSpace.RADIATOR.get(),
                pos,
                state,
                SpaceData.RADIATOR_INPUT_TANK_SIZE.get(),
                SpaceData.RADIATOR_OUTPUT_TANK_SIZE.get());
    }

    @Override
    protected boolean vacuumProof() {
        return true;
    }
}
