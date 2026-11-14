/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class BasaltSpikesFeature extends Feature<BasaltSpikesFeature.Config> {

    public record Config(BlockState block, HolderSet<Biome> biomes)
            implements FeatureConfiguration {

        public static final Codec<Config> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                BlockState.CODEC
                                                        .fieldOf("block")
                                                        .forGetter(Config::block),
                                                RegistryCodecs.<Biome>homogeneousList(
                                                                (ResourceKey<
                                                                                ? extends
                                                                                        Registry<
                                                                                                Biome>>)
                                                                        Registries.BIOME)
                                                        .fieldOf("biomes")
                                                        .forGetter(Config::biomes))
                                        .apply(i, Config::new));
    }

    public BasaltSpikesFeature() {
        super(Config.CODEC);
    }

    public static boolean growsAt(NormalNoise surface, int x, int z) {
        double noise = surface.getValue(x, 0.0D, z) * 8.25D;
        return !(noise > -0.25D && noise < 1.0D
                || noise > 1.5D && noise < 2.0D
                || noise > 2.5D && noise < 3.0D);
    }

    public static int thickness(RandomSource random) {
        return (int) Math.ceil(Math.pow(random.nextDouble(), 4.0D) * 8.0D);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        NormalNoise surface =
                level.getLevel().getChunkSource().randomState().getOrCreateNoise(Noises.SURFACE);
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int x = origin.getX(); x < origin.getX() + 16; x++) {
            for (int z = origin.getZ(); z < origin.getZ() + 16; z++) {
                if (!config.biomes().contains(level.getBiome(pos.set(x, level.getMinY(), z))))
                    continue;
                if (!growsAt(surface, x, z)) continue;

                int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                int height = thickness(random);
                for (int i = 0; i < height && y + i <= level.getMaxY(); i++) {
                    level.setBlock(pos.set(x, y + i, z), config.block(), Block.UPDATE_CLIENTS);
                    placed = true;
                }
            }
        }
        return placed;
    }
}
