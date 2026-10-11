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
import com.hbmspace.blockentity.machine.BlockEntityMachineAlkylation;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class MachineAlkylation extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {

    private static final int[] DIMENSIONS = {3, 0, 2, 2, 1, 1};

    public MachineAlkylation(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 2;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        for (Direction side :
                new Direction[] {facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos flank = core.relative(side);
            int out = 1 << side.ordinal();
            visitor.cell(flank, out);
            visitor.cell(flank.relative(facing, 2), out | 1 << facing.ordinal());
            visitor.cell(flank.relative(facing, -2), out | 1 << facing.getOpposite().ordinal());
        }
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
        for (Direction side :
                new Direction[] {facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos flank = core.relative(side);
            visitor.passiveCell(flank, MASK_ALL, domains);
            visitor.passiveCell(flank.relative(facing, 2), MASK_ALL, domains);
            visitor.passiveCell(flank.relative(facing, -2), MASK_ALL, domains);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineAlkylation(pos, state);
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack stack,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        Fluid fluid = AtmosphereText.identified(stack);
        if (fluid == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()
                && level.getBlockEntity(core) instanceof BlockEntityMachineAlkylation alkylation) {
            alkylation.tanks[0].setTankTypeByIdentifier(fluid);
            alkylation.setChanged();
            AtmosphereText.changedType(player, fluid);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineAlkylation alkylation)) return;

        info.title(getName().getString(), 0xFFFF00, 0x404000);
        info.line(
                (alkylation.power < BlockEntityMachineAlkylation.MAX_POWER / 20
                                ? ChatFormatting.RED
                                : ChatFormatting.GREEN)
                        + "Power: "
                        + BobMathUtil.getShortNumber(alkylation.power)
                        + "HE");
        for (int i = 0; i < alkylation.tanks.length; i++) {
            FluidTankNTM tank = alkylation.tanks[i];
            if (tank.getTankType() == null) continue;
            info.line(
                    (i < 2 ? ChatFormatting.GREEN + "-> " : ChatFormatting.RED + "<- ")
                            + ChatFormatting.RESET
                            + NTMFluidProperties.clientName(tank.getFluid())
                            + ": "
                            + tank.getFill()
                            + "/"
                            + tank.getMaxFill()
                            + "mB");
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.ALKYLATION)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .fe()
                .faces(BlockEntityMachineAlkylation.class, (be, side) -> side != Direction.DOWN)
                .fluidFaces(
                        BlockEntityMachineAlkylation.class,
                        (be, face) -> face.side() != Direction.DOWN);
    }
}
