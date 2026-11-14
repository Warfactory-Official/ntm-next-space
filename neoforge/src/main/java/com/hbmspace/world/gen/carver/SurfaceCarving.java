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

import net.minecraft.SharedConstants;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class SurfaceCarving {
    private SurfaceCarving() {}

    public static boolean mayChange(
            Structure.GenerationContext context, ProtoChunk chunk, int[] heights) {
        if (SharedConstants.DEBUG_DISABLE_CARVERS) return false;
        var generator = (NoiseBasedChunkGenerator) context.chunkGenerator();
        var carving = new WorldGenerationContext(generator, chunk.getHeightAccessorForGeneration());
        var resolver = generator.getBiomeSource().createUncachedResolver(context.randomState());
        var random = new WorldgenRandom(new LegacyRandomSource(0));
        var bounds = new CaveBounds(carving, chunk.isUpgrading(), heights);
        ChunkPos at = chunk.getPos();
        for (int dx = -8; dx <= 8; ++dx)
            for (int dz = -8; dz <= 8; ++dz) {
                ChunkPos from = new ChunkPos(at.x() + dx, at.z() + dz);
                var biome =
                        resolver.getNoiseBiome(
                                QuartPos.fromBlock(from.getMinBlockX()),
                                0,
                                QuartPos.fromBlock(from.getMinBlockZ()));
                int index = 0;
                for (var holder : generator.getBiomeGenerationSettings(biome).getCarvers()) {
                    var carver = holder.value();
                    random.setLargeFeatureSeed(context.seed() + index++, from.x(), from.z());
                    if (!carver.isStartChunk(random)) continue;
                    if (carver instanceof CelestialCaveCarver cave) {
                        cave.configuration().geometry.carve(carving, random, at, from, bounds);
                        if (bounds.touches) return true;
                    } else if (carver instanceof RavineCarver ravine) {
                        if (ravine.affectsSurface(
                                carving, ravine.configuration, chunk, random, from, heights))
                            return true;
                    } else {
                        return true;
                    }
                }
            }
        return false;
    }

    static boolean mayCut(
            WorldGenerationContext context,
            ChunkPos chunk,
            double x,
            double y,
            double z,
            double radius,
            double height,
            int[] heights) {
        int minX = Math.max(Mth.floor(x - radius) - chunk.getMinBlockX() - 1, 0);
        int maxX = Math.min(Mth.floor(x + radius) - chunk.getMinBlockX() + 1, 15);
        int minZ = Math.max(Mth.floor(z - radius) - chunk.getMinBlockZ() - 1, 0);
        int maxZ = Math.min(Mth.floor(z + radius) - chunk.getMinBlockZ() + 1, 15);
        int minY = Math.max(Mth.floor(y - height) - 1, context.getMinGenY() + 1);
        int maxY =
                Math.min(
                        Mth.floor(y + height) + 1,
                        context.getMinGenY() + context.getGenDepth() - 8);
        for (int bz = minZ; bz <= maxZ; ++bz)
            for (int bx = minX; bx <= maxX; ++bx) {
                int firstFree = heights[bz * 16 + bx];
                if (firstFree > minY && firstFree <= maxY + 1) return true;
            }
        return false;
    }

    private static final class CaveBounds implements CarverOutput {
        private final int minY;
        private final int maxY;
        private final int[] heights;
        private boolean touches;

        private CaveBounds(WorldGenerationContext context, boolean upgrading, int[] heights) {
            this.minY = context.getMinGenY() + 1;
            this.maxY = context.getMinGenY() + context.getGenDepth() - 1 - (upgrading ? 0 : 7);
            this.heights = heights;
        }

        @Override
        public int minY() {
            return minY;
        }

        @Override
        public int maxY() {
            return maxY;
        }

        @Override
        public void carve(int x, int y, int z) {
            int firstFree = heights[z * 16 + x];
            if (firstFree != Integer.MIN_VALUE && firstFree - 1 == y) touches = true;
        }
    }
}
