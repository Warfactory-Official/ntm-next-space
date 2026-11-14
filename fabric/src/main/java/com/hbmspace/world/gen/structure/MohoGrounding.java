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

package com.hbmspace.world.gen.structure;

import com.hbm.world.WorldgenHash;
import com.hbm.world.feature.FractalSimplexNoise;
import com.hbm.world.gen.WorldgenHeight;
import com.hbm.world.gen.nbt.LegacyPlacement;
import com.hbm.world.structure.LegacyJigsawStructure;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.world.gen.CelestialBiomeSource;
import com.hbmspace.world.gen.feature.BasaltSpikesFeature;
import com.hbmspace.world.gen.feature.PlateauFeature;
import com.hbmspace.world.gen.feature.VoronoiPlatesFeature;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;

final class MohoGrounding {
    private MohoGrounding() {}

    static int[] upperHeights(
            Structure.GenerationContext context,
            PoolElementStructurePiece anchor,
            List<FeatureSorter.StepFeatureData> features) {
        var box = anchor.getBoundingBox();
        int[] ground =
                WorldgenHeight.baseHeights(
                        context.chunkGenerator(),
                        context.heightAccessor(),
                        context.randomState(),
                        box);
        int[] surface = new int[ground.length];
        int index = 0;
        for (int z = box.minZ(); z <= box.maxZ(); z++)
            for (int x = box.minX(); x <= box.maxX(); x++) {
                surface[index++] =
                        context.chunkGenerator()
                                .getBaseHeight(
                                        x,
                                        z,
                                        Heightmap.Types.WORLD_SURFACE_WG,
                                        context.heightAccessor(),
                                        context.randomState());
            }
        if (features.isEmpty()) return ground;
        var biomes =
                new BiomeManager(
                        (x2, y, z2) ->
                                context.biomeSource()
                                        .getNoiseBiome(x2, y, z2, context.randomState().sampler()),
                        BiomeManager.obfuscateSeed(context.seed()));
        var pos = new BlockPos.MutableBlockPos();
        var raw = features.getFirst().features();
        for (int featureIndex = 0; featureIndex < raw.size(); featureIndex++) {
            var placed = raw.get(featureIndex);
            var config = placed.feature().value().config();
            if (!(config instanceof VoronoiPlatesFeature.Config
                    || config instanceof PlateauFeature.Config
                    || config instanceof BasaltSpikesFeature.Config)) continue;
            if (!placed.placement().isEmpty())
                throw new IllegalStateException(
                        "Moho grounding requires chunk-origin terrain: " + placed);
            switch (config) {
                case VoronoiPlatesFeature.Config plates -> {
                    long seed =
                            VoronoiPlatesFeature.domainSeed(
                                    context.seed(), SpaceDimensions.MOHO.identifier());
                    index = 0;
                    for (int z = box.minZ(); z <= box.maxZ(); z++)
                        for (int x = box.minX(); x <= box.maxX(); x++) {
                            if (plates.biomes()
                                    .contains(biomes.getBiome(pos.set(x, plates.startY(), z)))) {
                                int top = VoronoiPlatesFeature.top(plates, seed, x, z);
                                if (top > plates.startY()) {
                                    ground[index] = Math.max(ground[index], top);
                                    surface[index] = Math.max(surface[index], top);
                                }
                            }
                            index++;
                        }
                }
                case PlateauFeature.Config plateau -> {
                    var field =
                            new FractalSimplexNoise(
                                    WorldgenHash.domainSeed(
                                            context.seed(),
                                            SpaceDimensions.MOHO.identifier(),
                                            plateau.domain()),
                                    4);
                    index = 0;
                    for (int z = box.minZ(); z <= box.maxZ(); z++)
                        for (int x = box.minX(); x <= box.maxX(); x++) {
                            int base = surface[index] - 1;
                            int top = PlateauFeature.top(plateau, field, x, z, base);
                            int addition = top - base;
                            if (addition > 0
                                    && mayContainBiome(
                                            context,
                                            plateau.biomes(),
                                            x,
                                            z,
                                            context.heightAccessor().getMinY() - 1 + addition,
                                            top)) {
                                ground[index] = Math.max(ground[index], top);
                                surface[index] = Math.max(surface[index], top);
                            }
                            index++;
                        }
                }
                case BasaltSpikesFeature.Config spikes -> {
                    var noise = context.randomState().getOrCreateNoise(Noises.SURFACE);
                    for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
                            var random = new WorldgenRandom(new XoroshiroRandomSource(0));
                            long seed = random.setDecorationSeed(context.seed(), cx * 16, cz * 16);
                            random.setFeatureSeed(seed, featureIndex, 0);
                            for (int x = cx * 16; x < cx * 16 + 16; x++) {
                                for (int z = cz * 16; z < cz * 16 + 16; z++) {
                                    if (!spikes.biomes()
                                                    .contains(
                                                            biomes.getBiome(
                                                                    pos.set(
                                                                            x,
                                                                            context.heightAccessor()
                                                                                    .getMinY(),
                                                                            z)))
                                            || !BasaltSpikesFeature.growsAt(noise, x, z)) continue;
                                    int rise =
                                            Math.max(0, BasaltSpikesFeature.thickness(random) - 1);
                                    if (x < box.minX()
                                            || x > box.maxX()
                                            || z < box.minZ()
                                            || z > box.maxZ()) continue;
                                    index = (z - box.minZ()) * box.getXSpan() + x - box.minX();
                                    ground[index] += rise;
                                    surface[index] = Math.max(surface[index], ground[index]);
                                }
                            }
                        }
                    }
                }
                default -> throw new IllegalStateException(config.toString());
            }
        }
        return ground;
    }

    static boolean changesHeight(
            Structure.GenerationContext context,
            LegacyJigsawStructure structure,
            PoolElementStructurePiece anchor,
            int[] upperHeights) {
        long total = 0;
        for (int height : upperHeights)
            total += Math.min(height, context.heightAccessor().getMaxY() + 1);
        int upper =
                (int) (total / upperHeights.length)
                        + ((LegacyPlacement) anchor.getElement()).heightOffset();
        int expected =
                context.chunkGenerator() instanceof FlatLevelSource
                        ? upper
                        : structure.clampHeight(upper);
        return expected != anchor.getBoundingBox().minY();
    }

    private static boolean mayContainBiome(
            Structure.GenerationContext context,
            HolderSet<Biome> biomes,
            int x,
            int z,
            int minY,
            int maxY) {
        int minQuartY =
                (Mth.clamp(
                                        minY,
                                        context.heightAccessor().getMinY(),
                                        context.heightAccessor().getMaxY())
                                - 2)
                        >> 2;
        int maxQuartY =
                ((Mth.clamp(
                                                maxY,
                                                context.heightAccessor().getMinY(),
                                                context.heightAccessor().getMaxY())
                                        - 2)
                                >> 2)
                        + 1;
        if (context.biomeSource() instanceof CelestialBiomeSource) minQuartY = maxQuartY = 0;
        int qx = (x - 2) >> 2, qz = (z - 2) >> 2;
        for (int qy = minQuartY; qy <= maxQuartY; qy++) {
            for (int dx = 0; dx <= 1; dx++)
                for (int dz = 0; dz <= 1; dz++) {
                    final int x2 = qx + dx;
                    final int z2 = qz + dz;
                    if (biomes.contains(
                            context.biomeSource()
                                    .getNoiseBiome(x2, qy, z2, context.randomState().sampler())))
                        return true;
                }
        }
        return false;
    }
}
