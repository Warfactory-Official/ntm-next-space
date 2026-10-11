// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.machine.BlockEntityDeuteriumExtractor;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.inventory.fluid.FluidsSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityAtmoTower extends BlockEntityDeuteriumExtractor {

    public static final long MAX_POWER = 1_000_000L;

    public BlockEntityAtmoTower(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.ATMO_TOWER.get(), pos, state, 50_000, 5_000);
        tanks[0].setTankType(FluidsSpace.earthair());
        tanks[1].setTankType(FluidsSpace.nitrogen());
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }
}
