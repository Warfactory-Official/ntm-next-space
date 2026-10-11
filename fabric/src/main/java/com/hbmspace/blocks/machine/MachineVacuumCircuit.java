// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityMachineVacuumCircuit;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineVacuumCircuit extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {

    private static final int[] DIMENSIONS = {0, 0, 1, 1, 1, 1};

    public MachineVacuumCircuit(Properties props) {
        super(props);
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
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);

        visitor.cell(core.offset(1, 0, 1), MASK_EAST | MASK_SOUTH);
        visitor.cell(core.offset(1, 0, -1), MASK_EAST | MASK_NORTH);
        visitor.cell(core.offset(-1, 0, 1), MASK_WEST | MASK_SOUTH);
        visitor.cell(core.offset(-1, 0, -1), MASK_WEST | MASK_NORTH);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        passiveEverywhere(core, facing, visitor, PASSIVE_POWER_IN | PASSIVE_ITEMS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineVacuumCircuit(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineVacuumCircuit machine)) return;
        if (machine.canOperate) return;

        info.title(Component.translatable(getDescriptionId()), 0xFFFF00, 0x404000);
        info.line(
                Component.literal("! ! ! ")
                        .append(Component.translatable("atmosphere.noAir"))
                        .append(" ! ! !"),
                BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.VACUUM_CIRCUIT).powerIn().items().fe();
    }
}
