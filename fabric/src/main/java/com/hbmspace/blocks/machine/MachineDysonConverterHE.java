// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterHE;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineDysonConverterHE extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock {

    private static final int[] DIMENSIONS = {2, 0, 4, 4, 1, 1};

    public MachineDysonConverterHE(Properties properties) {
        super(properties);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 4;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        visitor.cell(core.relative(facing.getOpposite(), 4), MASK_NORTH);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDysonConverterHE(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(SkyBlockEntitiesSpace.DYSON_CONVERTER_HE).powerOut().fe();
    }
}
