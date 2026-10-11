// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.deco;

import com.hbm.blockentity.BlockEntityGeysir;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityGeysirSpace extends BlockEntityGeysir {

    public BlockEntityGeysirSpace(BlockPos pos, BlockState state) {
        super(TerrainBlockEntitiesSpace.GEYSIR.get(), pos, state);
    }
}
