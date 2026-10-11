// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbmspace.blockentity.machine.BlockEntityMachineDriveProcessor;
import com.hbmspace.blocks.RocketryBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineDriveProcessor extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock {

    private static final int[] DIMENSIONS = {0, 0, 0, 0, 0, 1};

    public MachineDriveProcessor(Properties properties) {
        super(properties);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int coreMask() {
        return MASK_ALL;
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        passiveEverywhere(core, facing, visitor, PASSIVE_POWER_IN);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineDriveProcessor(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(RocketryBlocksSpace.DRIVE_PROCESSOR_BE).powerIn().fe();
    }
}
