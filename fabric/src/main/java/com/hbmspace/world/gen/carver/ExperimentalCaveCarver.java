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

package com.hbmspace.world.gen.carver;

import com.hbmspace.world.gen.density.TerrainNoise;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
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
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class ExperimentalCaveCarver implements TerrainCarver {

    public static class Configuration extends CarvingSettings {

        public static final MapCodec<Configuration> CODEC =
                RecordCodecBuilder.mapCodec(
                        i ->
                                i.group(
                                                CarvingSettings.CODEC.forGetter(c -> c),
                                                Codec.FLOAT
                                                        .fieldOf("carving_scale")
                                                        .forGetter(c -> c.carvingScale),
                                                Codec.INT
                                                        .fieldOf("depth_threshold")
                                                        .forGetter(c -> c.depthThreshold),
                                                Codec.FLOAT
                                                        .fieldOf("large_cave_size")
                                                        .forGetter(c -> c.largeCaveSize),
                                                Codec.FLOAT
                                                        .fieldOf("small_cave_size")
                                                        .forGetter(c -> c.smallCaveSize),
                                                BlockState.CODEC
                                                        .fieldOf("lava")
                                                        .forGetter(c -> c.lava),
                                                RegistryCodecs.holderSet(Registries.BLOCK)
                                                        .fieldOf("top")
                                                        .forGetter(c -> c.top),
                                                RegistryCodecs.holderSet(Registries.BLOCK)
                                                        .fieldOf("filler")
                                                        .forGetter(c -> c.filler),
                                                RegistryCodecs.holderSet(Registries.BIOME)
                                                        .optionalFieldOf("biomes")
                                                        .forGetter(c -> c.biomes))
                                        .apply(i, Configuration::new));

        public final float carvingScale;
        public final int depthThreshold;
        public final float largeCaveSize;
        public final float smallCaveSize;
        public final BlockState lava;
        public final HolderSet<Block> top;
        public final HolderSet<Block> filler;
        public final Optional<HolderSet<Biome>> biomes;

        public Configuration(
                CarvingSettings carver,
                float carvingScale,
                int depthThreshold,
                float largeCaveSize,
                float smallCaveSize,
                BlockState lava,
                HolderSet<Block> top,
                HolderSet<Block> filler,
                Optional<HolderSet<Biome>> biomes) {
            super(
                    carver.probability,
                    carver.y,
                    carver.yScale,
                    carver.lavaLevel,
                    carver.replaceable);
            this.carvingScale = carvingScale;
            this.depthThreshold = depthThreshold;
            this.largeCaveSize = largeCaveSize;
            this.smallCaveSize = smallCaveSize;
            this.lava = lava;
            this.top = top;
            this.filler = filler;
            this.biomes = biomes;
        }
    }

    public static final MapCodec<ExperimentalCaveCarver> CODEC =
            Configuration.CODEC.xmap(ExperimentalCaveCarver::new, carver -> carver.configuration);
    public final Configuration configuration;

    public ExperimentalCaveCarver(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public MapCodec<ExperimentalCaveCarver> codec() {
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
        Cut cut = new Cut(context, configuration, chunk, biomeGetter, mask);
        int caves = random.nextInt(random.nextInt(random.nextInt(15) + 1) + 1);

        for (int cave = 0; cave < caves; cave++) {
            double x = sourceChunkPos.getBlockX(random.nextInt(16));
            double y = configuration.y.sample(random, context);
            double z = sourceChunkPos.getBlockZ(random.nextInt(16));
            int tunnels = 1;

            if (random.nextInt(4) == 0) {
                cut.node(
                        random.nextLong(),
                        x,
                        y,
                        z,
                        (1.0F + random.nextFloat()) * configuration.largeCaveSize,
                        0.0F,
                        0.0F,
                        -1,
                        0,
                        configuration.yScale.sample(random));
                tunnels += random.nextInt(4);
            }

            for (int i = 0; i < tunnels; i++) {
                float yaw = random.nextFloat() * (float) (Math.PI * 2);
                float pitch = (random.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float size = random.nextFloat() * 2.0F + random.nextFloat();
                if (random.nextInt(10) == 0) {
                    size *=
                            configuration.smallCaveSize
                                            * random.nextFloat()
                                            * random.nextFloat()
                                            * 3.0F
                                    + 1.0F;
                }
                cut.node(random.nextLong(), x, y, z, size, yaw, pitch, 0, 0, 1.0D);
            }
        }
        return true;
    }

    private final class Cut {

        private final TerrainCarving context;
        private final Configuration configuration;
        private final ChunkAccess chunk;
        private final Function<BlockPos, Holder<Biome>> biomeGetter;
        private final TerrainCarving mask;
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        Cut(
                TerrainCarving context,
                Configuration configuration,
                ChunkAccess chunk,
                Function<BlockPos, Holder<Biome>> biomeGetter,
                TerrainCarving mask) {
            this.context = context;
            this.configuration = configuration;
            this.chunk = chunk;
            this.biomeGetter = biomeGetter;
            this.mask = mask;
        }

        void node(
                long seed,
                double x,
                double y,
                double z,
                float size,
                float yaw,
                float pitch,
                int step,
                int distance,
                double heightFactor) {
            double centreX = chunk.getPos().getMiddleBlockX();
            double centreZ = chunk.getPos().getMiddleBlockZ();
            RandomSource random = RandomSource.create(seed);
            if (distance <= 0) {
                int maxDistance = SectionPos.sectionToBlockCoord(getRange() * 2 - 1);
                distance = maxDistance - random.nextInt(maxDistance / 4);
            }
            TerrainNoise noise = TerrainNoise.single(RandomSource.create(seed));
            float yawChange = 0.0F;
            float pitchChange = 0.0F;
            boolean room = step == -1;
            if (room) step = distance / 2;
            int branch = random.nextInt(distance / 2) + distance / 4;

            for (boolean steep = random.nextInt(6) == 0; step < distance; step++) {
                double radius =
                        1.5D + Mth.sin((float) step * (float) Math.PI / (float) distance) * size;
                double height = radius * heightFactor;
                float cos = Mth.cos(pitch);
                x += Mth.cos(yaw) * cos;
                y += Mth.sin(pitch);
                z += Mth.sin(yaw) * cos;

                double scale = 3.0D + Math.sin(step * 4);
                double bend = noise.noise(x * scale, y * scale, z * scale);
                if (y < configuration.depthThreshold) {
                    radius *= 3.0D + noise.noise(x * 0.1D, y * 0.1D, z * 0.1D);
                    height *= 2.0D + noise.noise(x * 0.1D, y * 0.2D, z * 0.1D);
                    pitch += (float) (bend * Mth.sin((float) (random.nextFloat() * Math.PI)));
                    yaw += (float) (bend * Mth.sin((float) (random.nextInt() * Math.PI)));
                }

                pitch *= steep ? 0.92F : 0.7F;
                pitch += pitchChange * 0.1F;
                yaw += yawChange * 0.1F;
                pitchChange *= 0.9F;
                yawChange *= 0.75F;
                pitchChange +=
                        (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
                yawChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

                if (!room && step == branch && size > 1.0F) {
                    for (float side : new float[] {-1.0F, 1.0F}) {
                        node(
                                random.nextLong(),
                                x,
                                y,
                                z,
                                random.nextFloat() * 0.5F + 0.5F,
                                yaw + side * (float) (Math.PI / 2),
                                pitch / 3.0F,
                                step,
                                distance,
                                1.0D);
                    }
                    return;
                }

                if (!room && random.nextInt(4) == 0) continue;
                if (!WorldCarver.canReach(chunk.getPos(), x, z, step, distance, size)) return;
                if (Math.abs(x - centreX) <= 16.0D + radius * 2.0D
                        && Math.abs(z - centreZ) <= 16.0D + radius * 2.0D
                        && !context.shellHolds(x, y, z, radius, height, true)) {
                    ellipsoid(noise, x, y, z, radius, height, bend);
                    if (room) return;
                }
            }
        }

        private void ellipsoid(
                TerrainNoise noise,
                double x,
                double y,
                double z,
                double radius,
                double height,
                double bend) {
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

            for (int bx = minX; bx < maxX; bx++) {
                int worldX = chunkPos.getBlockX(bx);
                double dx =
                        (worldX + 0.5D - x) / radius
                                + noise.noise(worldX * 0.05D, y * 0.05D, z * 0.05D) * 0.3D;
                for (int bz = minZ; bz < maxZ; bz++) {
                    int worldZ = chunkPos.getBlockZ(bz);
                    double dz =
                            (worldZ + 0.5D - z) / radius
                                    + noise.noise(x * 0.05D, y * 0.05D, worldZ * 0.05D) * 0.3D;
                    if (dx * dx + dz * dz >= 1.0D) continue;
                    boolean foundTop = false;
                    for (int by = maxY - 1; by >= minY; by--) {
                        double dy = (by + 0.5D - y) / height;
                        if (dy <= -0.7D || (dx * dx + dy * dy + dz * dz) * (1.0D + bend) >= 1.0D)
                            continue;
                        if (!context.accepts(bx, by, bz)) continue;
                        pos.set(worldX, by, worldZ);
                        if (chunk.getBlockState(pos).is(configuration.top)) foundTop = true;
                        dig(bx, bz, foundTop);
                    }
                }
            }
        }

        private void dig(int bx, int bz, boolean foundTop) {
            if (configuration.biomes.isPresent()
                    && !configuration.biomes.get().contains(biomeGetter.apply(pos))) {
                return;
            }
            BlockState state = chunk.getBlockState(pos);
            if (!state.is(configuration.replaceable) && state.getFluidState().isEmpty()) return;

            BlockState cut =
                    pos.getY() <= configuration.lavaLevel.resolveY(context)
                            ? configuration.lava
                            : Blocks.AIR.defaultBlockState();
            chunk.setBlockState(pos, cut);
            mask.set(bx, pos.getY(), bz);
            if (!cut.getFluidState().isEmpty()) chunk.markPosForPostProcessing(pos);
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
