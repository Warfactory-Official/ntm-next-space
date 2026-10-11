// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class BlockTallPlantWater extends DoublePlantBlock implements LiquidBlockContainer {

    public static final MapCodec<BlockTallPlantWater> CODEC = simpleCodec(BlockTallPlantWater::new);

    public BlockTallPlantWater(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends BlockTallPlantWater> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(TerrainBlocksSpace.LAYTHE_SILT.get());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        FluidState above = context.getLevel().getFluidState(context.getClickedPos().above());
        return above.is(FluidTags.WATER) && above.isFull() ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(
            @Nullable LivingEntity user,
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            Fluid type) {
        return false;
    }

    @Override
    public boolean placeLiquid(
            LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }
}
