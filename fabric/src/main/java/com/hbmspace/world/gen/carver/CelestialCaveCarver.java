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

package com.hbmspace.world.gen.carver;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class CelestialCaveCarver extends CaveWorldCarver {

    public CelestialCaveCarver() {
        super(
                CelestialCaveConfiguration.CODEC.<CaveCarverConfiguration>xmap(
                        c -> c, c -> (CelestialCaveConfiguration) c));
    }

    @Override
    protected boolean carveEllipsoid(
            CarvingContext context,
            CaveCarverConfiguration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            Aquifer aquifer,
            double x,
            double y,
            double z,
            double horizontalRadius,
            double verticalRadius,
            CarvingMask mask,
            CarveSkipChecker skipChecker) {
        if (shellHolds(context, chunk, x, y, z, horizontalRadius, verticalRadius, false))
            return false;
        return super.carveEllipsoid(
                context,
                configuration,
                chunk,
                biomeGetter,
                aquifer,
                x,
                y,
                z,
                horizontalRadius,
                verticalRadius,
                mask,
                skipChecker);
    }

    @Override
    protected boolean carveBlock(
            CarvingContext context,
            CaveCarverConfiguration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            CarvingMask mask,
            BlockPos.MutableBlockPos blockPos,
            BlockPos.MutableBlockPos helperPos,
            Aquifer aquifer,
            MutableBoolean foundTop) {
        CelestialCaveConfiguration config = (CelestialCaveConfiguration) configuration;
        BlockState state = chunk.getBlockState(blockPos);
        if (state.is(config.top)) foundTop.setTrue();
        if (!state.is(config.replaceable)) return false;

        BlockState cut = blockPos.getY() <= config.lavaLevel.resolveY(context) ? config.lava : AIR;
        chunk.setBlockState(blockPos, cut);
        if (!cut.getFluidState().isEmpty()) chunk.markPosForPostProcessing(blockPos);
        if (foundTop.isTrue())
            retop(
                    context,
                    chunk,
                    biomeGetter,
                    helperPos.setWithOffset(blockPos, Direction.DOWN),
                    config);
        return true;
    }

    static void retop(
            CarvingContext context,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            BlockPos pos,
            CelestialCaveConfiguration config) {
        if (!chunk.getBlockState(pos).is(config.filler)) return;
        context.topMaterial(biomeGetter, chunk, pos, false)
                .ifPresent(top -> chunk.setBlockState(pos, top));
    }

    static boolean shellHolds(
            CarvingContext context,
            ChunkAccess chunk,
            double x,
            double y,
            double z,
            double radius,
            double height,
            boolean anyFluid) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = Math.max(Mth.floor(x - radius) - chunkPos.getMinBlockX() - 1, 0);
        int maxX = Math.min(Mth.floor(x + radius) - chunkPos.getMinBlockX() + 1, 16);
        int minY = Math.max(Mth.floor(y - height) - 1, context.getMinGenY() + 1);
        int maxY =
                Math.min(
                        Mth.floor(y + height) + 1,
                        context.getMinGenY() + context.getGenDepth() - 8);
        int minZ = Math.max(Mth.floor(z - radius) - chunkPos.getMinBlockZ() - 1, 0);
        int maxZ = Math.min(Mth.floor(z + radius) - chunkPos.getMinBlockZ() + 1, 16);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int bx = minX; bx < maxX; bx++) {
            for (int bz = minZ; bz < maxZ; bz++) {
                boolean wall = bx == minX || bx == maxX - 1 || bz == minZ || bz == maxZ - 1;
                for (int by = maxY + 1; by >= minY - 1; by--) {
                    if (!wall && by != maxY + 1 && by != minY - 1) continue;
                    pos.set(chunkPos.getBlockX(bx), by, chunkPos.getBlockZ(bz));
                    FluidState fluid = chunk.getFluidState(pos);
                    if (anyFluid ? !fluid.isEmpty() : fluid.is(FluidTags.WATER)) return true;
                }
            }
        }
        return false;
    }
}
