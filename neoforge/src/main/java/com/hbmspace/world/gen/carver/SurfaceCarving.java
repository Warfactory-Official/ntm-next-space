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
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class SurfaceCarving {
    private SurfaceCarving() {}

    public static boolean mayChange(
            Structure.GenerationContext context, ProtoChunk chunk, int[] heights) {
        if (SharedConstants.DEBUG_DISABLE_CARVERS) return false;
        var generator = (NoiseBasedChunkGenerator) context.chunkGenerator();
        var carving =
                new CarvingContext(
                        generator,
                        context.registryAccess(),
                        chunk.getHeightAccessorForGeneration(),
                        null,
                        context.randomState(),
                        generator.generatorSettings().value().surfaceRule());
        var random = new WorldgenRandom(new LegacyRandomSource(0));
        var caves = new CaveBounds(heights);
        ChunkPos at = chunk.getPos();
        for (int dx = -8; dx <= 8; dx++)
            for (int dz = -8; dz <= 8; dz++) {
                ChunkPos from = new ChunkPos(at.x() + dx, at.z() + dz);
                var biome =
                        generator
                                .getBiomeSource()
                                .getNoiseBiome(
                                        QuartPos.fromBlock(from.getMinBlockX()),
                                        0,
                                        QuartPos.fromBlock(from.getMinBlockZ()),
                                        context.randomState().sampler());
                int index = 0;
                for (var holder : generator.getBiomeGenerationSettings(biome).getCarvers()) {
                    var configured = holder.value();
                    random.setLargeFeatureSeed(context.seed() + index++, from.x(), from.z());
                    if (!configured.isStartChunk(random)) continue;
                    if (configured.worldCarver().getClass() == CelestialCaveCarver.class
                            && configured.config() instanceof CelestialCaveConfiguration config) {
                        caves.carve(carving, config, chunk, null, random, null, from, null);
                        if (caves.touches) return true;
                    } else if (configured.worldCarver().getClass() == RavineCarver.class
                            && configured.config() instanceof RavineCarver.Configuration config) {
                        var ravine = (RavineCarver) configured.worldCarver();
                        if (ravine.affectsSurface(carving, config, chunk, random, from, heights))
                            return true;
                    } else {
                        return true;
                    }
                }
            }
        return false;
    }

    static boolean mayCut(
            CarvingContext context,
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
        for (int bz = minZ; bz <= maxZ; bz++)
            for (int bx = minX; bx <= maxX; bx++) {
                int firstFree = heights[bz * 16 + bx];
                if (firstFree > minY && firstFree <= maxY + 1) return true;
            }
        return false;
    }

    private static final class CaveBounds extends CaveWorldCarver {
        private final int[] heights;
        private boolean touches;

        private CaveBounds(int[] heights) {
            super(CaveCarverConfiguration.CODEC);
            this.heights = heights;
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
                double radius,
                double height,
                CarvingMask mask,
                CarveSkipChecker checker) {
            touches |= mayCut(context, chunk.getPos(), x, y, z, radius, height, heights);
            return false;
        }
    }
}
