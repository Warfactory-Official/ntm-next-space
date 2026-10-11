// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.Mats;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityMachineMagma;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineMagma extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {

    private static final int[] DIMENSIONS = {3, 3, 3, 3, 3, 3};

    public MachineMagma(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 3;
    }

    @Override
    public int getHeightOffset() {
        return 3;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        int back = 1 << facing.getOpposite().ordinal();
        for (BlockPos port : ports(core, facing)) visitor.cell(port, back, ROLE_POWER_IN);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        for (BlockPos port : ports(core, facing))
            visitor.passiveCell(port, MASK_ALL, PASSIVE_POWER_IN);
    }

    private static BlockPos[] ports(BlockPos core, Direction facing) {
        Direction rot = facing.getClockWise();
        BlockPos back = core.relative(facing, -3);
        return new BlockPos[] {
            back.below(),
            back.below(2),
            back.below().relative(rot),
            back.below().relative(rot, -1),
            back.below(2).relative(rot),
            back.below(2).relative(rot, -1)
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineMagma(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineMagma drill)) return;

        info.title(Component.translatable(getDescriptionId()), 0xFFFF00, 0x404000);
        int blink = BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00;
        if (!"moho".equals(CelestialBody.getBody(level).name)) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("desc.hbmspace.magma.notMoho"))
                            .append(" ! ! !"),
                    blink);
        } else if (!drill.validPosition) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("desc.hbmspace.magma.noLava"))
                            .append(" ! ! !"),
                    blink);
        } else {
            info.line(
                    (drill.power < BlockEntityMachineMagma.CONSUMPTION
                                    ? ChatFormatting.RED
                                    : ChatFormatting.GREEN)
                            + "Power: "
                            + BobMathUtil.getShortNumber(drill.power)
                            + "HE");
            for (MaterialStack stack : drill.liquids) {
                info.line(
                        stack.material.getLocalizedName()
                                + ": "
                                + Mats.formatAmount(stack.amount, false),
                        0xFFFF55);
            }
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.MAGMA).powerIn().fe();
    }
}
