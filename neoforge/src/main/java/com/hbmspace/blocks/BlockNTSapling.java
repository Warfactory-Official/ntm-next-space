// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.dim.tekto.TTree;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.IPlantableBreathing;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockNTSapling extends VegetationBlock
        implements BonemealableBlock, IPlantableBreathing {

    private static final VoxelShape SHAPE = Block.column(12.8, 0.0, 12.8);

    private final Supplier<TTree> tree;
    private final MapCodec<BlockNTSapling> ownCodec;

    public BlockNTSapling(Properties properties, Supplier<TTree> tree) {
        super(properties);
        this.tree = tree;
        this.ownCodec = simpleCodec(p -> new BlockNTSapling(p, tree));
    }

    @Override
    protected MapCodec<? extends BlockNTSapling> codec() {
        return ownCodec;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canBreathe(@Nullable CBT_Atmosphere atmosphere) {
        return atmosphere != null
                && (atmosphere.hasFluid(FluidsSpace.tektoair(), 0.1)
                        || atmosphere.hasFluid(NTMFluids.CHLORINE, 0.1));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(TerrainBlocksSpace.RUBBER_SILT.get())
                || state.is(FloraBlocksSpace.RUBBER_GRASS.get())
                || state.is(FloraBlocksSpace.RUBBER_FARMLAND.get());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos))
                && super.canSurvive(state, level, pos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            grow(level, pos, state, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.45F;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos, state, random);
    }

    private void grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
        if (!tree.get()
                .generate(
                        level, new Random(random.nextLong()), pos.getX(), pos.getY(), pos.getZ())) {
            level.setBlock(pos, state, UPDATE_CLIENTS);
        }
    }
}
