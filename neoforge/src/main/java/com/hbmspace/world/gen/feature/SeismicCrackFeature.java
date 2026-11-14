/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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

import com.hbm.world.NtmWorldgenFields;
import com.hbm.world.feature.FractalSimplexNoise;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class SeismicCrackFeature extends Feature<SeismicCrackFeature.Config> {

    public record Config(
            Identifier domain,
            double scale,
            double threshold,
            int depth,
            BlockState fill,
            HolderSet<Biome> biomes)
            implements FeatureConfiguration {

        public static final Codec<Config> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                Identifier.CODEC
                                                        .fieldOf("domain")
                                                        .forGetter(Config::domain),
                                                Codec.DOUBLE
                                                        .fieldOf("scale")
                                                        .forGetter(Config::scale),
                                                Codec.DOUBLE
                                                        .fieldOf("threshold")
                                                        .forGetter(Config::threshold),
                                                Codec.intRange(0, 64)
                                                        .fieldOf("depth")
                                                        .forGetter(Config::depth),
                                                BlockState.CODEC
                                                        .fieldOf("fill")
                                                        .forGetter(Config::fill),
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

    public SeismicCrackFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        WorldGenLevel level = context.level();
        FractalSimplexNoise field = NtmWorldgenFields.get(level).fractal(config.domain(), 4);
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        boolean placed = false;

        for (int x = origin.getX(); x < origin.getX() + 16; x++) {
            for (int z = origin.getZ(); z < origin.getZ() + 16; z++) {
                if (!config.biomes().contains(level.getBiome(pos.set(x, level.getMinY(), z))))
                    continue;
                if (field.get(x * config.scale(), z * config.scale()) <= config.threshold())
                    continue;

                int bedrock = Integer.MIN_VALUE;
                for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (state.is(Blocks.BEDROCK)) {
                        if (bedrock == Integer.MIN_VALUE) bedrock = y;
                    } else if (!state.isAir()) {
                        level.setBlock(pos, air, Block.UPDATE_CLIENTS);
                    }
                }
                if (bedrock == Integer.MIN_VALUE) continue;
                for (int y = bedrock + 1;
                        y <= Math.min(bedrock + config.depth(), level.getMaxY());
                        y++) {
                    level.setBlock(pos.set(x, y, z), config.fill(), Block.UPDATE_CLIENTS);
                }
                placed = true;
            }
        }
        return placed;
    }
}
