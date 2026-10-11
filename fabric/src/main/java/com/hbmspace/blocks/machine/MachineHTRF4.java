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
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTRF4;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineHTRF4 extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, ILookOverlay {

    private static final int[] DIMENSIONS = {2, 2, 2, 2, 11, 9};

    public MachineHTRF4(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 11;
    }

    @Override
    public int getHeightOffset() {
        return 2;
    }

    @Override
    public Direction getDirModified(Direction dir) {
        return dir.getCounterClockWise();
    }

    private static BlockPos feedCell(BlockPos core, Direction facing) {
        return core.relative(facing.getClockWise().getOpposite(), 9);
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        BlockPos feed = feedCell(core, facing);
        visitor.cell(feed.relative(facing), MASK_EAST, ROLE_POWER_IN);
        visitor.cell(feed.relative(facing.getOpposite()), MASK_EAST, ROLE_POWER_IN);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        BlockPos feed = feedCell(core, facing);
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
        visitor.passiveCell(feed, MASK_ALL, domains);
        visitor.passiveCell(feed.relative(facing), MASK_ALL, domains);
        visitor.passiveCell(feed.relative(facing.getOpposite()), MASK_ALL, domains);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineHTRF4(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.HTRF4)
                .powerIn()
                .fluidIn()
                .fe()
                .faces(
                        BlockEntityMachineHTRF4.class,
                        (be, side) -> CelestialBody.inOrbit(be.getLevel()))
                .fluidFaces(
                        BlockEntityMachineHTRF4.class,
                        (be, face) -> CelestialBody.inOrbit(be.getLevel()));
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineHTRF4 thruster)) return;

        info.title(getName(), 0xFFFF00, 0x404000);

        if (!thruster.isFacingPrograde()) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.engineFacing"))
                            .append(" ! ! !"),
                    BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        info.line(
                (thruster.power == 0 ? ChatFormatting.RED : ChatFormatting.GREEN)
                        + BobMathUtil.getShortNumber(thruster.power)
                        + "HE");
        for (FluidTankNTM tank : thruster.tanks) {
            info.line(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    NTMFluidProperties.getDisplayName(tank.getTankType())
                                            .copy()
                                            .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                            .append(
                                    Component.translatable(
                                                    "unit.millibuckets",
                                                    tank.getFill() + "/" + tank.getMaxFill())
                                            .withStyle(ChatFormatting.WHITE)));
        }

        Direction facing = coreFacing(level.getBlockState(pos));
        BlockPos feed = feedCell(pos, facing);
        BlockPos hit = info.getHitPos();
        if (feed.equals(hit)) {
            info.line(Component.translatable("overlay.hbmspace.htrf4.plasma"));
        } else if (feed.relative(facing).equals(hit)
                || feed.relative(facing.getOpposite()).equals(hit)) {
            info.line(Component.translatable("overlay.hbmspace.htrf4.power"));
        }
    }
}
