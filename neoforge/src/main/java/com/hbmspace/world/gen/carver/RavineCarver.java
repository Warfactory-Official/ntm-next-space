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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.material.Fluids;

public class RavineCarver implements TerrainCarver {

    public static class Configuration extends CarvingSettings {

        public static final MapCodec<Configuration> CODEC =
                RecordCodecBuilder.mapCodec(
                        i ->
                                i.group(
                                                CarvingSettings.CODEC.forGetter(c -> c),
                                                Codec.intRange(1, 256)
                                                        .fieldOf("strata_frequency")
                                                        .forGetter(c -> c.strataFrequency),
                                                Codec.FLOAT
                                                        .fieldOf("strata_scale")
                                                        .forGetter(c -> c.strataScale),
                                                Codec.DOUBLE
                                                        .fieldOf("taper")
                                                        .forGetter(c -> c.taper),
                                                Codec.DOUBLE
                                                        .fieldOf("width")
                                                        .forGetter(c -> c.width),
                                                Codec.BOOL
                                                        .fieldOf("underwater")
                                                        .forGetter(c -> c.underwater),
                                                RegistryCodecs.holderSet(Registries.BLOCK)
                                                        .fieldOf("top")
                                                        .forGetter(c -> c.top),
                                                RegistryCodecs.holderSet(Registries.BLOCK)
                                                        .fieldOf("filler")
                                                        .forGetter(c -> c.filler))
                                        .apply(i, Configuration::new));

        public final int strataFrequency;
        public final float strataScale;
        public final double taper;
        public final double width;
        public final boolean underwater;
        public final HolderSet<Block> top;
        public final HolderSet<Block> filler;

        public Configuration(
                CarvingSettings carver,
                int strataFrequency,
                float strataScale,
                double taper,
                double width,
                boolean underwater,
                HolderSet<Block> top,
                HolderSet<Block> filler) {
            super(
                    carver.probability,
                    carver.y,
                    carver.yScale,
                    carver.lavaLevel,
                    carver.replaceable);
            this.strataFrequency = strataFrequency;
            this.strataScale = strataScale;
            this.taper = taper;
            this.width = width;
            this.underwater = underwater;
            this.top = top;
            this.filler = filler;
        }
    }

    public static final MapCodec<RavineCarver> CODEC =
            Configuration.CODEC.xmap(RavineCarver::new, carver -> carver.configuration);
    public final Configuration configuration;

    public RavineCarver(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public MapCodec<RavineCarver> codec() {
        return CODEC;
    }

    @Override
    public boolean isStartChunk(RandomSource random) {
        return random.nextFloat() <= configuration.probability;
    }

    @Override
    public boolean carve(TerrainCarving context, RandomSource random, ChunkPos sourceChunkPos) {
        ChunkAccess chunk = context.chunk;
        Function<BlockPos, Holder<Biome>> biomeGetter = context.biomeGetter;
        TerrainCarving mask = context;
        trace(context, configuration, chunk, biomeGetter, random, sourceChunkPos, mask, null);
        return true;
    }

    public boolean affectsSurface(
            WorldGenerationContext context,
            Configuration configuration,
            ChunkAccess chunk,
            RandomSource random,
            ChunkPos sourceChunkPos,
            int[] heights) {
        return trace(context, configuration, chunk, null, random, sourceChunkPos, null, heights);
    }

    private boolean trace(
            WorldGenerationContext context,
            Configuration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            RandomSource random,
            ChunkPos sourceChunkPos,
            TerrainCarving mask,
            int[] heights) {
        double x = sourceChunkPos.getBlockX(random.nextInt(16));
        double y = configuration.y.sample(random, context);
        double z = sourceChunkPos.getBlockZ(random.nextInt(16));
        float yaw = random.nextFloat() * (float) (Math.PI * 2);
        float pitch = (random.nextFloat() - 0.5F) * 2.0F / 8.0F;
        float thickness = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
        double heightFactor = configuration.yScale.sample(random);
        return ravine(
                context,
                configuration,
                chunk,
                biomeGetter,
                random.nextLong(),
                x,
                y,
                z,
                thickness,
                yaw,
                pitch,
                heightFactor,
                mask,
                heights);
    }

    private boolean ravine(
            WorldGenerationContext context,
            Configuration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            long seed,
            double x,
            double y,
            double z,
            float thickness,
            float yaw,
            float pitch,
            double heightFactor,
            TerrainCarving mask,
            int[] heights) {
        RandomSource random = RandomSource.create(seed);
        int maxDistance = SectionPos.sectionToBlockCoord(getRange() * 2 - 1);
        int distance = maxDistance - random.nextInt(maxDistance / 4);
        float[] strata = new float[context.getGenDepth()];
        float stratum = 1.0F;
        for (int i = 0; i < strata.length; i++) {
            if (i == 0 || random.nextInt(configuration.strataFrequency) == 0) {
                stratum =
                        1.0F + random.nextFloat() * random.nextFloat() * configuration.strataScale;
            }
            strata[i] = stratum * stratum;
        }

        float yawChange = 0.0F;
        float pitchChange = 0.0F;
        ChunkPos chunkPos = chunk.getPos();
        for (int step = 0; step < distance; step++) {
            double radius =
                    1.5D + Mth.sin((float) step * (float) Math.PI / (float) distance) * thickness;
            double height = radius * heightFactor;
            radius *= random.nextFloat() * 0.25D + 0.75D;
            height *= random.nextFloat() * 0.25D + 0.75D;
            float cos = Mth.cos(pitch);
            x += Mth.cos(yaw) * cos;
            y += Mth.sin(pitch);
            z += Mth.sin(yaw) * cos;
            pitch *= 0.7F;
            pitch += pitchChange * 0.05F;
            yaw += yawChange * 0.05F;
            pitchChange *= 0.8F;
            yawChange *= 0.5F;
            pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (random.nextInt(4) == 0) continue;
            if (!WorldCarver.canReach(chunkPos, x, z, step, distance, thickness)) return false;
            if (Math.abs(x - chunkPos.getMiddleBlockX()) > 16.0D + radius * 2.0D
                    || Math.abs(z - chunkPos.getMiddleBlockZ()) > 16.0D + radius * 2.0D) continue;

            if (heights != null) {
                if (SurfaceCarving.mayCut(context, chunkPos, x, y, z, radius, height, heights))
                    return true;
                continue;
            }

            boolean flooded = ((TerrainCarving) context).shellHolds(x, y, z, radius, height, false);
            if (flooded && !configuration.underwater) continue;
            cut(
                    (TerrainCarving) context,
                    configuration,
                    chunk,
                    biomeGetter,
                    x,
                    y,
                    z,
                    radius,
                    height,
                    strata,
                    flooded,
                    mask);
        }
        return false;
    }

    private void cut(
            TerrainCarving context,
            Configuration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            double x,
            double y,
            double z,
            double radius,
            double height,
            float[] strata,
            boolean flooded,
            TerrainCarving mask) {
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
        int lavaLevel = configuration.lavaLevel.resolveY(context);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int bx = minX; bx < maxX; bx++) {
            double dx = (chunkPos.getBlockX(bx) + 0.5D - x) / radius;
            for (int bz = minZ; bz < maxZ; bz++) {
                double dz = (chunkPos.getBlockZ(bz) + 0.5D - z) / radius;
                if (dx * dx + dz * dz >= configuration.width) continue;
                boolean foundTop = false;
                for (int by = maxY - 1; by >= minY; by--) {
                    double dy = (by + 0.5D - y) / height;
                    if ((dx * dx + dz * dz) * strata[by - context.getMinGenY()]
                                    + dy * dy / configuration.taper
                            >= configuration.width) {
                        continue;
                    }
                    if (!context.accepts(bx, by, bz)) continue;
                    pos.set(chunkPos.getBlockX(bx), by, chunkPos.getBlockZ(bz));
                    BlockState state = chunk.getBlockState(pos);
                    if (state.is(configuration.top)) foundTop = true;
                    if (!state.is(configuration.replaceable)) continue;

                    BlockState carved;
                    if (by <= lavaLevel) {
                        carved =
                                flooded
                                        ? Blocks.OBSIDIAN.defaultBlockState()
                                        : Fluids.LAVA.defaultFluidState().createLegacyBlock();
                    } else {
                        carved =
                                flooded && by < 62
                                        ? Fluids.WATER.defaultFluidState().createLegacyBlock()
                                        : Blocks.AIR.defaultBlockState();
                    }
                    chunk.setBlockState(pos, carved);
                    mask.set(bx, by, bz);
                    if (!carved.getFluidState().isEmpty()) chunk.markPosForPostProcessing(pos);
                    if (foundTop) {
                        BlockPos below = pos.below();
                        if (chunk.getBlockState(below).is(configuration.filler)) {
                            context.topMaterial(biomeGetter, chunk, below, false)
                                    .ifPresent(top -> chunk.setBlockState(below, top));
                        }
                    }
                }
            }
        }
    }
}
