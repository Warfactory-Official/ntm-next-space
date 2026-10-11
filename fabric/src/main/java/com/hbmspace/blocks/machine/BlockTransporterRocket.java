// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbmspace.blockentity.machine.BlockEntityTransporterRocket;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.items.StationItemsSpace;
import com.hbmspace.main.ChunkLoaderManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockTransporterRocket extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock {

    private static final int[] DIMENSIONS = {1, 0, 1, 1, 1, 2};
    private static final int[] PAD = {0, 0, 1, 1, 1, 2};
    private static final int[] CORE_COLUMN = {1, 0, 1, 1, 0, 0};
    private static final int[] FAR_COLUMN = {1, 0, 1, 1, -2, 2};

    public BlockTransporterRocket(Properties props) {
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
        MultiblockHandlerXR.visitBox(core, PAD, facing, visitor);
        MultiblockHandlerXR.visitBox(core, CORE_COLUMN, facing, visitor);
        MultiblockHandlerXR.visitBox(core, FAR_COLUMN, facing, visitor);

        Direction rot = facing.getClockWise();
        for (int side = -1; side <= 1; side += 2) {
            visitor.cell(
                    core.relative(facing).relative(rot, side), MASK_SOUTH | MASK_UP, ROLE_FLUID);
            visitor.cell(
                    core.relative(facing, -1).relative(rot, side),
                    MASK_NORTH | MASK_UP,
                    ROLE_FLUID);
        }
        visitor.cell(core.relative(facing).relative(rot, -2), MASK_EAST, ROLE_FLUID_IN);
        visitor.cell(core.relative(facing, -1).relative(rot, -2), MASK_EAST, ROLE_FLUID_IN);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        Direction rot = facing.getClockWise();
        for (int forward = -1; forward <= 1; forward += 2) {
            for (int side = -1; side <= 1; side += 2) {
                visitor.passiveCell(
                        core.relative(facing, forward).relative(rot, side),
                        MASK_ALL,
                        PASSIVE_FLUID_IN);
            }
            visitor.passiveCell(
                    core.relative(facing, forward).relative(rot, -2), MASK_ALL, PASSIVE_FLUID_IN);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTransporterRocket(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.TRANSPORTER_ROCKET)
                .fluidIn()
                .fluidOut()
                .items();
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server)) return;

        FoldedOwner owner = ownerOf(level, pos);
        if (owner != null) ChunkLoaderManager.forceChunk(server, owner.pos());
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack held,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return held.is(StationItemsSpace.TRANSPORTER_LINKER.get())
                ? InteractionResult.PASS
                : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
