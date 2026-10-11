// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbmspace.blockentity.machine.BlockEntityMachineMilkReformer;
import com.hbmspace.blocks.ModBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineMilkReformer extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock {
    private static final int[] DIMENSIONS = {6, 0, 1, 1, 1, 1};

    public MachineMilkReformer(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);

        visitor.cell(core.offset(1, 0, 1), MASK_EAST | MASK_SOUTH);
        visitor.cell(core.offset(1, 0, -1), MASK_EAST | MASK_NORTH);
        visitor.cell(core.offset(-1, 0, 1), MASK_WEST | MASK_SOUTH);
        visitor.cell(core.offset(-1, 0, -1), MASK_WEST | MASK_NORTH);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
        visitor.passiveCell(core.offset(1, 0, 1), MASK_ALL, domains);
        visitor.passiveCell(core.offset(1, 0, -1), MASK_ALL, domains);
        visitor.passiveCell(core.offset(-1, 0, 1), MASK_ALL, domains);
        visitor.passiveCell(core.offset(-1, 0, -1), MASK_ALL, domains);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineMilkReformer(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(ModBlockEntitiesSpace.MILK_REFORMER)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .items()
                .fe()
                .faces(BlockEntityMachineMilkReformer.class, (be, side) -> side != Direction.DOWN)
                .fluidFaces(
                        BlockEntityMachineMilkReformer.class,
                        (be, face) -> face.side() != Direction.DOWN);
    }
}
