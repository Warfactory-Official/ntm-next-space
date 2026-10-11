// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbmspace.blockentity.machine.BlockEntityMachineCryoDistill;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MachineCryoDistill extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {
    private static final int[] DIMENSIONS = {3, 2, 3, 3, 2, 2};
    private static final int[][] BOXES = {
        {0, 0, 3, 3, 2, 2},
        {0, 2, 3, 0, 2, 2},
        {0, 2, -2, 3, 2, 2},
        {2, 0, 3, -1, 2, 2},
        {3, 0, -2, 3, 2, 2}
    };

    public MachineCryoDistill(Properties props) {
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
        return 2;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        for (int[] box : BOXES) MultiblockHandlerXR.visitBox(core, box, facing, visitor);

        Direction rot = facing.getClockWise();
        int front = 1 << facing.ordinal();
        for (int side : new int[] {-2, -1, 1, 2}) {
            visitor.cell(
                    core.below(2).relative(facing, 3).relative(rot, side), front, ROLE_FLUID_OUT);
        }
        int flank = 1 << rot.getOpposite().ordinal();
        visitor.cell(input(core, facing), flank, ROLE_FLUID_IN);
        visitor.cell(power(core, facing), flank, ROLE_POWER_IN);
    }

    private static BlockPos input(BlockPos core, Direction facing) {
        return core.below(2).relative(facing, -1).relative(facing.getClockWise(), -2);
    }

    private static BlockPos power(BlockPos core, Direction facing) {
        return core.below(2).relative(facing, -2).relative(facing.getClockWise(), -2);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
        Direction rot = facing.getClockWise();
        for (int side : new int[] {-2, -1, 1, 2}) {
            visitor.passiveCell(
                    core.below(2).relative(facing, 3).relative(rot, side), MASK_ALL, domains);
        }
        visitor.passiveCell(input(core, facing), MASK_ALL, domains);
        visitor.passiveCell(power(core, facing), MASK_ALL, domains);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineCryoDistill(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        BlockPos hit = info.getHitPos();
        if (hit == null) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineCryoDistill distill)) return;

        Direction facing = distill.getBlockState().getValue(FACING);
        Direction rot = facing.getClockWise();
        if (hit.equals(input(pos, facing))) {
            info.line(
                    ChatFormatting.GREEN
                            + "-> "
                            + ChatFormatting.RESET
                            + NTMFluidProperties.clientName(distill.tanks[0].getTankType()));
        }
        if (hit.equals(power(pos, facing))) {
            info.line(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + "Power");
        }
        for (int side : new int[] {-2, -1, 1, 2}) {
            if (!hit.equals(pos.below(2).relative(facing, 3).relative(rot, side))) continue;
            for (int i = 1; i < distill.tanks.length; i++) {
                info.line(
                        ChatFormatting.RED
                                + "<- "
                                + ChatFormatting.RESET
                                + NTMFluidProperties.clientName(distill.tanks[i].getTankType()));
            }
        }

        if (!info.isEmpty())
            info.title(Component.translatable(getDescriptionId()), 0xFFFF00, 0x404000);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.CRYO_DISTILL)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .fe()
                .faces(BlockEntityMachineCryoDistill.class, (be, side) -> side != Direction.DOWN)
                .fluidFaces(
                        BlockEntityMachineCryoDistill.class,
                        (be, face) -> face.side() != Direction.DOWN);
    }
}
