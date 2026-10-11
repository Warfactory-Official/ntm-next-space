// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbmspace.blockentity.machine.BlockEntityMachineGasDock;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class MachineGasDock extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, ILookOverlay {

    private static final int[] DIMENSIONS = {0, 0, 1, 1, 1, 1};
    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.75, 1);

    public MachineGasDock(Properties props) {
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
    public @Nullable VoxelShape cellShape(
            int lx, int ly, int lz, Direction facing, boolean open, boolean forCollision) {
        return SHAPE;
    }

    @Override
    public int coreMask() {
        return MASK_DOWN;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        Direction east = facing.getCounterClockWise();
        for (int lx = -1; lx <= 1; lx++) {
            for (int lz = -1; lz <= 1; lz++) {
                if (lx == 0 && lz == 0) continue;
                int mask = MASK_DOWN;
                if (lx == 1) mask |= MASK_EAST;
                if (lx == -1) mask |= MASK_WEST;
                if (lz == 1) mask |= MASK_SOUTH;
                if (lz == -1) mask |= MASK_NORTH;
                visitor.cell(core.relative(facing, lz).relative(east, lx), mask);
            }
        }
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        passiveEverywhere(core, facing, visitor, PASSIVE_FLUID_IN);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineGasDock(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.GAS_DOCK).fluidIn().fluidOut();
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineGasDock dock)) return;

        info.title(getName().getString(), 0xFFFF00, 0x404000);
        for (int i = 0; i < dock.tanks.length; i++) {
            FluidTankNTM tank = dock.tanks[i];
            info.line(
                    (i < 1 ? ChatFormatting.RED + "<- " : ChatFormatting.GREEN + "-> ")
                            + ChatFormatting.RESET
                            + NTMFluidProperties.clientName(tank.getTankType())
                            + ": "
                            + tank.getFill()
                            + "/"
                            + tank.getMaxFill()
                            + "mB");
        }
    }
}
