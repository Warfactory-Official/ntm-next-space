// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.MachineIntake;
import com.hbm.capability.MachineCaps;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphericEmitter;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class MachineAtmosphericEmitter extends MachineIntake {

    private static final int[] DIMENSIONS = {2, 0, 1, 0, 1, 0};

    public MachineAtmosphericEmitter(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
        Direction rot = facing.getClockWise();
        BlockPos back = core.relative(facing.getOpposite());
        visitor.passiveCell(back, MASK_ALL, domains);
        visitor.passiveCell(core.relative(rot), MASK_ALL, domains);
        visitor.passiveCell(back.relative(rot), MASK_ALL, domains);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAtmosphericEmitter(pos, state);
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
        if (AtmosphereText.identified(stack) == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()
                && level.getBlockEntity(core) instanceof BlockEntityAtmosphericEmitter emitter) {
            AtmosphereText.retypeGaseous(stack, player, emitter.tank, emitter);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAtmosphericEmitter emitter)) return;

        info.title(getName().getString(), 0xFFFF00, 0x404000);
        info.line(
                (emitter.isRunning() ? ChatFormatting.GREEN : ChatFormatting.RED)
                        + "Power: "
                        + BobMathUtil.getShortNumber(emitter.power)
                        + "HE");
        info.line(
                ChatFormatting.GREEN
                        + "-> "
                        + ChatFormatting.RESET
                        + NTMFluidProperties.clientName(emitter.tank.getFluid())
                        + ": "
                        + emitter.tank.getFill()
                        + "/"
                        + emitter.tank.getMaxFill()
                        + "mB");
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_EMITTER)
                .powerIn()
                .fluidIn()
                .fe();
    }
}
