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
import com.hbmspace.blockentity.machine.BlockEntityXenonThruster;
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

public class MachineXenonThruster extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, ILookOverlay {

    private static final int[] DIMENSIONS = {1, 1, 0, 1, 1, 1};

    public MachineXenonThruster(Properties props) {
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
    public int getHeightOffset() {
        return 1;
    }

    @Override
    public int coreMask() {
        return MASK_NORTH;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);

        Direction rot = facing.getClockWise();
        visitor.cell(core.relative(rot.getOpposite()), MASK_NORTH);
        visitor.cell(core.relative(rot), MASK_NORTH);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        Direction rot = facing.getClockWise();
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
        visitor.passiveCell(core.relative(rot.getOpposite()), MASK_ALL, domains);
        visitor.passiveCell(core.relative(rot), MASK_ALL, domains);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityXenonThruster(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.XENON_THRUSTER)
                .powerIn()
                .fluidIn()
                .fe()
                .faces(
                        BlockEntityXenonThruster.class,
                        (be, side) -> CelestialBody.inOrbit(be.getLevel()))
                .fluidFaces(
                        BlockEntityXenonThruster.class,
                        (be, face) -> CelestialBody.inOrbit(be.getLevel()));
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityXenonThruster thruster)) return;

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
    }
}
