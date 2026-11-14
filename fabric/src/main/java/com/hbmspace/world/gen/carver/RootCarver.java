/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class RootCarver extends WorldCarver<RootCarver.Configuration> {

    private static final CarveSkipChecker ELLIPSOID =
            (context, xd, yd, zd, y) -> yd <= -0.7D || xd * xd + yd * yd + zd * zd >= 1.0D;

    public static class Configuration extends CarverConfiguration {

        public static final Codec<Configuration> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                CarverConfiguration.CODEC.forGetter(c -> c),
                                                BlockState.CODEC
                                                        .fieldOf("fill")
                                                        .forGetter(c -> c.fill),
                                                BlockState.CODEC
                                                        .fieldOf("lava_fill")
                                                        .forGetter(c -> c.lavaFill),
                                                RegistryCodecs.homogeneousList(Registries.BLOCK)
                                                        .fieldOf("top")
                                                        .forGetter(c -> c.top),
                                                RegistryCodecs.homogeneousList(Registries.BLOCK)
                                                        .fieldOf("filler")
                                                        .forGetter(c -> c.filler),
                                                BlockState.CODEC
                                                        .fieldOf("cover")
                                                        .forGetter(c -> c.cover))
                                        .apply(i, Configuration::new));

        public final BlockState fill;
        public final BlockState lavaFill;
        public final HolderSet<Block> top;
        public final HolderSet<Block> filler;
        public final BlockState cover;

        public Configuration(
                CarverConfiguration carver,
                BlockState fill,
                BlockState lavaFill,
                HolderSet<Block> top,
                HolderSet<Block> filler,
                BlockState cover) {
            super(
                    carver.probability,
                    carver.y,
                    carver.yScale,
                    carver.lavaLevel,
                    carver.debugSettings,
                    carver.replaceable);
            this.fill = fill;
            this.lavaFill = lavaFill;
            this.top = top;
            this.filler = filler;
            this.cover = cover;
        }
    }

    public RootCarver() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean isStartChunk(Configuration configuration, RandomSource random) {
        return random.nextFloat() <= configuration.probability;
    }

    @Override
    public boolean carve(
            CarvingContext context,
            Configuration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos sourceChunkPos,
            CarvingMask mask) {
        int maxDistance = SectionPos.sectionToBlockCoord(getRange() * 2 - 1);
        int caves = random.nextInt(random.nextInt(random.nextInt(15) + 1) + 1);

        for (int cave = 0; cave < caves; cave++) {
            double x = sourceChunkPos.getBlockX(random.nextInt(16));
            double y = configuration.y.sample(random, context);
            double z = sourceChunkPos.getBlockZ(random.nextInt(16));
            int tunnels = 1;

            if (random.nextInt(4) == 0) {
                double radius =
                        1.5D + Mth.sin((float) (Math.PI / 2)) * (1.0F + random.nextFloat() * 6.0F);
                carveEllipsoid(
                        context,
                        configuration,
                        chunk,
                        biomeGetter,
                        aquifer,
                        x + 1.0D,
                        y,
                        z,
                        radius,
                        radius * configuration.yScale.sample(random),
                        mask,
                        ELLIPSOID);
                tunnels += random.nextInt(4);
            }

            for (int i = 0; i < tunnels; i++) {
                float yaw = random.nextFloat() * (float) (Math.PI * 2);
                float pitch = (random.nextFloat() - 0.5F) * 2.0F / 16.0F;
                float width = random.nextFloat() * 0.5F + random.nextFloat();
                if (random.nextInt(10) == 0)
                    width *= random.nextFloat() * random.nextFloat() * 2.5F + 1.0F;

                int distance = maxDistance - random.nextInt(maxDistance / 4);
                tunnel(
                        context,
                        configuration,
                        chunk,
                        biomeGetter,
                        random.nextLong(),
                        aquifer,
                        x,
                        y,
                        z,
                        width,
                        yaw,
                        pitch,
                        0,
                        distance,
                        2.25D,
                        mask);
            }
        }
        return true;
    }

    private void tunnel(
            CarvingContext context,
            Configuration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            long seed,
            Aquifer aquifer,
            double x,
            double y,
            double z,
            float width,
            float yaw,
            float pitch,
            int step,
            int distance,
            double heightFactor,
            CarvingMask mask) {
        RandomSource random = RandomSource.create(seed);
        int branch = random.nextInt(distance / 2) + distance / 4;
        boolean steep = random.nextInt(6) == 0;
        float yawChange = 0.0F;
        float pitchChange = 0.0F;

        for (; step < distance; step++) {
            double radius = 1.5D + Mth.sin((float) Math.PI * step / distance) * width;
            float cos = Mth.cos(pitch);
            x += Mth.cos(yaw) * cos;
            y += Mth.sin(pitch);
            z += Mth.sin(yaw) * cos;
            pitch *= steep ? 0.92F : 0.7F;
            pitch += pitchChange * 0.1F;
            yaw += yawChange * 0.1F;
            pitchChange *= 0.9F;
            yawChange *= 0.75F;
            pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (step == branch && width > 1.0F) {
                for (float side : new float[] {-1.0F, 1.0F}) {
                    tunnel(
                            context,
                            configuration,
                            chunk,
                            biomeGetter,
                            random.nextLong(),
                            aquifer,
                            x,
                            y,
                            z,
                            random.nextFloat() * 0.25F + 0.5F,
                            yaw + side * (float) (Math.PI / 2),
                            pitch / 3.0F,
                            step,
                            distance,
                            1.0D,
                            mask);
                }
                return;
            }

            if (random.nextInt(4) != 0) {
                if (!canReach(chunk.getPos(), x, z, step, distance, width)) return;
                carveEllipsoid(
                        context,
                        configuration,
                        chunk,
                        biomeGetter,
                        aquifer,
                        x,
                        y,
                        z,
                        radius,
                        radius * heightFactor,
                        mask,
                        ELLIPSOID);
            }
        }
    }

    @Override
    protected boolean carveBlock(
            CarvingContext context,
            Configuration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            CarvingMask mask,
            BlockPos.MutableBlockPos blockPos,
            BlockPos.MutableBlockPos helperPos,
            Aquifer aquifer,
            MutableBoolean foundTop) {
        BlockState state = chunk.getBlockState(blockPos);
        if (state.is(configuration.top)) foundTop.setTrue();
        if (!state.is(configuration.replaceable)) return false;

        chunk.setBlockState(
                blockPos,
                blockPos.getY() <= configuration.lavaLevel.resolveY(context)
                        ? configuration.lavaFill
                        : configuration.fill);
        helperPos.setWithOffset(blockPos, Direction.UP);
        if (foundTop.isTrue() && chunk.getBlockState(helperPos).is(configuration.filler)) {
            chunk.setBlockState(helperPos, configuration.cover);
        }
        return true;
    }
}
