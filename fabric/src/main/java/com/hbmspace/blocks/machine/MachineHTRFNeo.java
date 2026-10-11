// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blockentity.machine.fusion.FusionPorts;
import com.hbm.blocks.machine.fusion.BlockFusionMachine;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTRNeo;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineHTRFNeo extends BlockFusionMachine implements ICapabilityBlock, ILookOverlay {

    private static final int[] DIMENSIONS = {2, 2, 2, 2, 11, 9};

    public MachineHTRFNeo(Properties props) {
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

    private static BlockPos plasmaCell(BlockPos core, Direction facing) {
        return core.relative(facing.getClockWise().getOpposite(), 9);
    }

    private static BlockPos[] coolantPorts(BlockPos core, Direction facing) {
        Direction rot = facing.getClockWise();
        BlockPos far = core.relative(rot.getOpposite(), 5).below(2);
        BlockPos near = core.relative(rot).below(2);
        return new BlockPos[] {
            far.relative(facing.getOpposite(), 2),
            near.relative(facing.getOpposite(), 2),
            far.relative(facing, 2),
            near.relative(facing, 2)
        };
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        BlockPos[] ports = coolantPorts(core, facing);
        visitor.cell(ports[0], MASK_NORTH);
        visitor.cell(ports[1], MASK_NORTH);
        visitor.cell(ports[2], MASK_SOUTH);
        visitor.cell(ports[3], MASK_SOUTH);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
        visitor.passiveCell(plasmaCell(core, facing), MASK_ALL, domains);
        for (BlockPos port : coolantPorts(core, facing))
            visitor.passiveCell(port, MASK_ALL, domains);
    }

    @Override
    public List<FusionPorts.Port> links(BlockPos core, Direction facing) {
        return List.of(
                new FusionPorts.Port(
                        FusionPorts.Kind.PLASMA,
                        plasmaCell(core, facing),
                        facing.getClockWise().getOpposite()));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineHTRNeo(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.HTRF4_NEO)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .fe();
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineHTRNeo thruster)) return;

        info.title(getName(), 0xFFFF00, 0x404000);

        if (!thruster.isFacingPrograde()) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.engineFacing"))
                            .append(" ! ! !"),
                    BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        int heat = (int) Math.ceil(thruster.temperature);
        for (Component text :
                List.of(
                        Component.translatable(
                                "overlay.hbmspace.htrf4neo.energy",
                                Component.literal(BobMathUtil.getShortNumber(thruster.plasmaEnergy))
                                        .withStyle(
                                                thruster.plasmaEnergy == 0
                                                        ? ChatFormatting.RED
                                                        : ChatFormatting.GREEN)),
                        Component.translatable(
                                "overlay.hbmspace.htrf4neo.power_level",
                                Component.literal(BobMathUtil.getShortNumber(thruster.power))
                                        .withStyle(
                                                thruster.power < thruster.getMaxPower()
                                                        ? ChatFormatting.RED
                                                        : ChatFormatting.GREEN)),
                        Component.translatable(
                                "overlay.hbmspace.htrf4neo.temperature",
                                Component.literal(Integer.toString(heat))
                                        .withStyle(
                                                heat > 123
                                                        ? ChatFormatting.RED
                                                        : ChatFormatting.AQUA)))) {
            var line = Component.empty();
            var color = TextColor.WHITE;
            for (Component part : text.toFlatList()) {
                var style = part.getStyle();
                if (style.getColor() != null) color = style.getColor();
                line.append(part.copy().withStyle(style.withColor(color)));
            }
            info.line(line);
        }

        FluidTankNTM cold = thruster.coolantTanks[0];
        FluidTankNTM hot = thruster.coolantTanks[1];
        info.line(
                ChatFormatting.GREEN
                        + "-> "
                        + ChatFormatting.RESET
                        + NTMFluidProperties.clientName(cold.getTankType())
                        + ": "
                        + cold.getFill()
                        + "/"
                        + cold.getMaxFill()
                        + "mB");
        info.line(
                ChatFormatting.RED
                        + "<- "
                        + ChatFormatting.RESET
                        + NTMFluidProperties.clientName(hot.getTankType())
                        + ": "
                        + hot.getFill()
                        + "/"
                        + hot.getMaxFill()
                        + "mB");

        if (!thruster.isCool()) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("overlay.hbmspace.htrf4neo.cooling"))
                            .append(" ! ! !"),
                    BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
        }

        if (plasmaCell(pos, coreFacing(level.getBlockState(pos))).equals(info.getHitPos())) {
            info.line(Component.translatable("overlay.hbmspace.htrf4neo.feed"));
        }
    }
}
