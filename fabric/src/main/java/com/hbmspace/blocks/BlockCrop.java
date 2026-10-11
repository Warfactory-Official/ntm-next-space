// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.IPlantableBreathing;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockCrop extends CropBlock implements IPlantableBreathing {

    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 4.0);

    private final Supplier<? extends BlockState> soil;
    private final Predicate<CBT_Atmosphere> atmosphere;
    private final Supplier<? extends ItemLike> seed;
    private final boolean canHydro;
    private final MapCodec<BlockCrop> ownCodec;

    public BlockCrop(
            Properties properties,
            Supplier<? extends BlockState> soil,
            Predicate<CBT_Atmosphere> atmosphere,
            Supplier<? extends ItemLike> seed,
            boolean canHydro) {
        super(properties);
        this.soil = soil;
        this.atmosphere = atmosphere;
        this.seed = seed;
        this.canHydro = canHydro;
        this.ownCodec = simpleCodec(p -> new BlockCrop(p, soil, atmosphere, seed, canHydro));
    }

    public boolean canHydro() {
        return canHydro;
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return ownCodec;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canBreathe(@Nullable CBT_Atmosphere atmosphere) {
        return atmosphere != null && this.atmosphere.test(atmosphere);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(soil.get().getBlock());
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = getAge(state);
        if (age < getMaxAge()) level.setBlock(pos, getStateForAge(age + 1), UPDATE_CLIENTS);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }
}
