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

import java.util.BitSet;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public final class TerrainCarving extends WorldGenerationContext implements CarvingMask.Visitor {
    public final ChunkAccess chunk;
    public final Function<BlockPos, Holder<Biome>> biomeGetter;
    private final RandomState random;
    private final NoiseChunk noise;
    private final MaterialRule material;
    private final CarvingMask.@Nullable Filter filter;
    private final int minY;
    private final int maxY;
    private final int height;
    private final BitSet visited;
    private @Nullable CarvingMask collecting;

    public TerrainCarving(
            NoiseBasedChunkGenerator generator,
            ChunkAccess chunk,
            RandomState random,
            NoiseChunk noise,
            MaterialRule material,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            CarvingMask.@Nullable Filter filter) {
        super(generator, chunk.getHeightAccessorForGeneration());
        this.chunk = chunk;
        this.random = random;
        this.noise = noise;
        this.material = material;
        this.biomeGetter = biomeGetter;
        this.filter = filter;
        this.minY = getMinGenY() + 1;
        this.maxY = getMinGenY() + getGenDepth() - 1 - (chunk.isUpgrading() ? 0 : 7);
        this.height = maxY - minY + 1;
        this.visited = new BitSet(256 * height);
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public boolean get(int x, int y, int z) {
        return visited.get(y - minY + (z + (x << 4)) * height);
    }

    public void set(int x, int y, int z) {
        visited.set(y - minY + (z + (x << 4)) * height);
    }

    public boolean accepts(int x, int y, int z) {
        return y >= minY && y <= maxY && (filter == null || filter.test(x, y, z));
    }

    public CarvingMask unvisitedNativeCuts(CarvingMask raw) {
        collecting = new CarvingMask(minY, maxY);
        raw.visit(this);
        CarvingMask result = collecting;
        collecting = null;
        return result;
    }

    @Override
    public void visitColumn(int x, int z, int bottomY, int topY) {
        for (int y = bottomY; y <= topY; ++y) {
            if (!get(x, y, z)) {
                set(x, y, z);
                collecting.carve(x, y, z);
            }
        }
    }

    public Optional<BlockState> topMaterial(
            Function<BlockPos, Holder<Biome>> biomes, ChunkAccess at, BlockPos pos, boolean fluid) {
        return random.surfaceSystem()
                .topMaterial(
                        material, random, this, biomes, at, noise.cachingSamplers(), pos, fluid);
    }

    public boolean carveEllipsoid(
            CarvingSettings configuration,
            ChunkPos chunkPos,
            double x,
            double y,
            double z,
            double horizontalRadius,
            double verticalRadius,
            WorldCarver.CarveSkipChecker checker) {
        if (configuration instanceof CelestialCaveConfiguration
                && shellHolds(x, y, z, horizontalRadius, verticalRadius, false)) return false;
        double centerX = chunkPos.getMiddleBlockX();
        double centerZ = chunkPos.getMiddleBlockZ();
        double maxDelta = 16.0D + horizontalRadius * 2.0D;
        if (Math.abs(x - centerX) > maxDelta || Math.abs(z - centerZ) > maxDelta) return false;
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int minXIndex = Math.max(Mth.floor(x - horizontalRadius) - chunkMinX - 1, 0);
        int maxXIndex = Math.min(Mth.floor(x + horizontalRadius) - chunkMinX, 15);
        int lowY = Math.max(Mth.floor(y - verticalRadius) - 1, minY);
        int highY = Math.min(Mth.floor(y + verticalRadius) + 1, maxY);
        int minZIndex = Math.max(Mth.floor(z - horizontalRadius) - chunkMinZ - 1, 0);
        int maxZIndex = Math.min(Mth.floor(z + horizontalRadius) - chunkMinZ, 15);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos helper = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int bx = minXIndex; bx <= maxXIndex; ++bx) {
            int worldX = chunkPos.getBlockX(bx);
            double dx = (worldX + 0.5D - x) / horizontalRadius;
            for (int bz = minZIndex; bz <= maxZIndex; ++bz) {
                int worldZ = chunkPos.getBlockZ(bz);
                double dz = (worldZ + 0.5D - z) / horizontalRadius;
                if (dx * dx + dz * dz >= 1.0D) continue;
                boolean foundTop = false;
                for (int by = highY; by > lowY; --by) {
                    double dy = (by - 0.5D - y) / verticalRadius;
                    if (checker.shouldSkip(dx, dy, dz, by) || get(bx, by, bz)) continue;
                    set(bx, by, bz);
                    if (!accepts(bx, by, bz)) continue;
                    pos.set(worldX, by, worldZ);
                    BlockState state = chunk.getBlockState(pos);
                    if (configuration instanceof CelestialCaveConfiguration cave) {
                        if (state.is(cave.top)) foundTop = true;
                        if (!state.is(cave.replaceable)) continue;
                        BlockState cut =
                                by <= cave.lavaLevel.resolveY(this)
                                        ? cave.lava
                                        : Blocks.AIR.defaultBlockState();
                        chunk.setBlockState(pos, cut);
                        if (!cut.getFluidState().isEmpty()) chunk.markPosForPostProcessing(pos);
                        helper.setWithOffset(pos, Direction.DOWN);
                        if (foundTop && chunk.getBlockState(helper).is(cave.filler)) {
                            BlockState top =
                                    topMaterial(biomeGetter, chunk, helper, false).orElse(null);
                            if (top != null) chunk.setBlockState(helper, top);
                        }
                    } else if (configuration instanceof RootCarver.Configuration root) {
                        if (state.is(root.top)) foundTop = true;
                        if (!state.is(root.replaceable)) continue;
                        chunk.setBlockState(
                                pos,
                                by <= root.lavaLevel.resolveY(this) ? root.lavaFill : root.fill);
                        helper.setWithOffset(pos, Direction.UP);
                        if (foundTop && chunk.getBlockState(helper).is(root.filler))
                            chunk.setBlockState(helper, root.cover);
                    } else {
                        throw new IllegalArgumentException(
                                "No Space ellipsoid cut for " + configuration.getClass().getName());
                    }
                    changed = true;
                }
            }
        }
        return changed;
    }

    public boolean shellHolds(
            double x, double y, double z, double radius, double verticalRadius, boolean anyFluid) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = Math.max(Mth.floor(x - radius) - chunkPos.getMinBlockX() - 1, 0);
        int maxX = Math.min(Mth.floor(x + radius) - chunkPos.getMinBlockX() + 1, 16);
        int lowY = Math.max(Mth.floor(y - verticalRadius) - 1, minY);
        int highY = Math.min(Mth.floor(y + verticalRadius) + 1, maxY);
        int minZ = Math.max(Mth.floor(z - radius) - chunkPos.getMinBlockZ() - 1, 0);
        int maxZ = Math.min(Mth.floor(z + radius) - chunkPos.getMinBlockZ() + 1, 16);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int bx = minX; bx < maxX; ++bx)
            for (int bz = minZ; bz < maxZ; ++bz) {
                boolean wall = bx == minX || bx == maxX - 1 || bz == minZ || bz == maxZ - 1;
                for (int by = highY + 1; by >= lowY - 1; --by) {
                    if (!wall && by != highY + 1 && by != lowY - 1) continue;
                    pos.set(chunkPos.getBlockX(bx), by, chunkPos.getBlockZ(bz));
                    FluidState fluid = chunk.getFluidState(pos);
                    if (anyFluid ? !fluid.isEmpty() : fluid.is(FluidTags.WATER)) return true;
                }
            }
        return false;
    }
}
