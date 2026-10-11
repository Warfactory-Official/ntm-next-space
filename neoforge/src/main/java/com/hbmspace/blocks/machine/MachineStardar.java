// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbmspace.blockentity.machine.BlockEntityMachineStardar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineStardar extends BlockMultiblockCore implements ITickingBlock {

    private static final int[] DIMENSIONS = {0, 3, 2, 2, 2, 2};

    public MachineStardar(Properties properties) {
        super(properties);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 2;
    }

    @Override
    public int getHeightOffset() {
        return 3;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x != 0 || z != 0) visitor.cell(pos.setWithOffset(core, x, 0, z), MASK_NONE);
            }
        }
        for (int x = -2; x <= 2; x += 2) {
            for (int z = -2; z <= 2; z += 2) {
                if (x == 0 && z == 0) continue;
                for (int y = -3; y <= -1; y++)
                    visitor.cell(pos.setWithOffset(core, x, y, z), MASK_NONE);
            }
        }
        Direction rot = facing.getClockWise();
        int rx = facing.getStepX() * 2 - rot.getStepX();
        int rz = facing.getStepZ() * 2 - rot.getStepZ();
        visitor.cell(pos.setWithOffset(core, rx, -2, rz), MASK_NONE);
        visitor.cell(pos.setWithOffset(core, rx, -3, rz), MASK_NONE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineStardar(pos, state);
    }
}
