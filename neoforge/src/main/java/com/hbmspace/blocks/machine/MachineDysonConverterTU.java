// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterTU;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineDysonConverterTU extends BlockMultiblockCore implements EntityBlock {

    private static final int[] DIMENSIONS = {2, 0, 1, 6, 1, 1};

    public MachineDysonConverterTU(Properties properties) {
        super(properties);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 6;
    }

    @Override
    public boolean heatSourceAt(int lx, int ly, int lz) {
        return lx == 0 && lz == 0 && (ly == 1 || ly == 2);
    }

    @Override
    protected boolean proxyCellAt(int lx, int ly, int lz) {
        return heatSourceAt(lx, ly, lz);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDysonConverterTU(pos, state);
    }
}
