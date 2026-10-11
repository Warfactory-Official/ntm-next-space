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
import com.hbmspace.blockentity.machine.BlockEntityRadiator;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineRadiator extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, ILookOverlay {

    private static final int[] DIMENSIONS = {1, 1, 0, 0, 1, 1};

    public MachineRadiator(Properties props) {
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
    public int getHeightOffset() {
        return 1;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        corners(core, facing, pos -> visitor.cell(pos, MASK_NORTH, ROLE_FLUID));
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        corners(core, facing, pos -> visitor.passiveCell(pos, MASK_ALL, PASSIVE_FLUID_IN));
    }

    private static void corners(BlockPos core, Direction facing, Consumer<BlockPos> visitor) {
        Direction rot = facing.getClockWise();
        for (int side = -1; side <= 1; side += 2) {
            for (int dy = -1; dy <= 1; dy += 2) visitor.accept(core.relative(rot, side).above(dy));
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityRadiator(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.RADIATOR).fluidIn().fluidOut();
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityRadiator radiator)) return;
        info.title(getName().getString(), 0xFFFF00, 0x404000);
        info.line(tank(ChatFormatting.GREEN + "-> ", radiator.input));
        info.line(tank(ChatFormatting.RED + "<- ", radiator.output));
    }

    private static String tank(String arrow, FluidTankNTM tank) {
        return arrow
                + ChatFormatting.RESET
                + NTMFluidProperties.clientName(tank.getTankType())
                + ": "
                + tank.getFill()
                + "/"
                + tank.getMaxFill()
                + "mB";
    }
}
