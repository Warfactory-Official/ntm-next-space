// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityAirPump;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import com.hbmspace.handler.atmosphere.IBlockSealable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockAirPump extends Block
        implements ITickingBlock, ILookOverlay, ICapabilityBlock, IBlockSealable {

    public BlockAirPump(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAirPump(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BlockEntityAirPump pump) {
            AtmosphereText.retypeGaseous(stack, player, pump.tank, pump);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isSealed(Level level, BlockPos pos) {
        return false;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAirPump pump)) return;

        info.title(getName(), 0xFFFF00, 0x404000);
        info.line(
                NTMFluidProperties.getDisplayName(pump.tank.getFluid())
                        .copy()
                        .append(": ")
                        .append(
                                Component.translatable(
                                        "unit.millibuckets",
                                        pump.tank.getFill() + "/" + pump.tank.getMaxFill())));

        int blink = BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00;
        if (pump.tank.getFill() <= 10) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noTank"))
                            .append(" ! ! !"),
                    blink);
        } else if (!pump.hasSeal()) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noSeal"))
                            .append(" ! ! !"),
                    blink);
        }

        info.line(Component.translatable("atmosphere.name").append(": "));

        boolean hasPressure = false;
        CBT_Atmosphere atmosphere = pump.currentAtmosphere;
        if (atmosphere != null) {
            for (CBT_Atmosphere.FluidEntry entry : atmosphere.fluids) {
                if (entry.pressure > 0.01) {
                    info.line(
                            Component.literal(" - ")
                                    .append(NTMFluidProperties.getDisplayName(entry.fluid))
                                    .append(" - ")
                                    .append(AtmosphereText.roundDecimal(entry.pressure, 3) + "atm")
                                    .withStyle(ChatFormatting.AQUA));
                    hasPressure = true;
                }
            }
        }

        if (!hasPressure)
            info.line(
                    Component.literal(" - ")
                            .append(Component.translatable("atmosphere.vacuum"))
                            .withStyle(ChatFormatting.AQUA));
        if (pump.isRecycling())
            info.line(
                    Component.translatable("atmosphere.recycling").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.AIR_PUMP).fluidIn();
    }
}
