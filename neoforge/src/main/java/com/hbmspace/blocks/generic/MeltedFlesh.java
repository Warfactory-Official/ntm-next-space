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

package com.hbmspace.blocks.generic;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Temperature;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class MeltedFlesh extends Block {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    private final @Nullable Supplier<? extends Block> charred;
    private final ParticleOptions[] fumes;

    public MeltedFlesh(
            Properties properties,
            @Nullable Supplier<? extends Block> charred,
            ParticleOptions... fumes) {
        super(properties);
        this.charred = charred;
        this.fumes = fumes;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.ICE) || below.is(Blocks.PACKED_ICE)) return false;
        if (below.is(BlockTags.LEAVES)) return true;
        return below.isSolidRender() && below.is(BlockTags.BLOCKS_MOTION);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    private static boolean hot(Level level) {
        CBT_Temperature temperature = CelestialBody.getTrait(level, CBT_Temperature.class);
        return temperature != null && temperature.degrees > 100;
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (charred != null && hot(level))
            level.setBlockAndUpdate(pos, charred.get().defaultBlockState());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (fumes.length == 0 || !hot(level)) return;
        double x = pos.getX() + random.nextFloat();
        double y = pos.getY() + random.nextFloat() * 0.5F + 0.5F;
        double z = pos.getZ() + random.nextFloat();
        for (ParticleOptions fume : fumes) level.addParticle(fume, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
