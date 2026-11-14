/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.blocks;

import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.PlantBreathingHandler;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class NTSaplingBlock extends VegetationBlock
        implements BonemealableBlock, PlantBreathingHandler {
    public static final MapCodec<NTSaplingBlock> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            TreeGrower.CODEC
                                                    .fieldOf("tree")
                                                    .forGetter(b -> b.treeGrower),
                                            propertiesCodec())
                                    .apply(i, NTSaplingBlock::new));
    private static final VoxelShape SHAPE = Block.column(12.8, 0.0, 12.8);

    private final TreeGrower treeGrower;

    public NTSaplingBlock(TreeGrower treeGrower, Properties properties) {
        super(properties);
        this.treeGrower = treeGrower;
    }

    @Override
    protected MapCodec<? extends NTSaplingBlock> codec() {
        return CODEC;
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
            treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
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
        treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
    }
}
