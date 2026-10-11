// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityAlgaeFilm;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

public class BlockAlgaeFilm extends HorizontalDirectionalBlock
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {

    public static final MapCodec<BlockAlgaeFilm> CODEC = simpleCodec(BlockAlgaeFilm::new);

    public BlockAlgaeFilm(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAlgaeFilm(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(TerrainBlockEntitiesSpace.ALGAE_FILM)
                .fluidIn()
                .fluidOut()
                .fluidFaces(
                        BlockEntityAlgaeFilm.class,
                        (be, face) ->
                                face.side().getAxis()
                                        == be.getBlockState()
                                                .getValue(FACING)
                                                .getClockWise()
                                                .getAxis());
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAlgaeFilm film)) return;

        info.title(getName(), 0xFFFF00, 0x404000);
        if (!film.canOperate) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noGravity"))
                            .append(" ! ! !"),
                    BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
        }
        line(info, Component.literal("-> ").withStyle(ChatFormatting.GREEN), film.tanks[0]);
        line(info, Component.literal("<- ").withStyle(ChatFormatting.RED), film.tanks[1]);
    }

    private static void line(LookInfo info, Component prefix, FluidTankNTM tank) {
        info.line(
                prefix.copy()
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
