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

import com.mojang.serialization.MapCodec;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public record CelestialCaveCarver(CelestialCaveConfiguration configuration)
        implements TerrainCarver {
    public static final MapCodec<CelestialCaveCarver> CODEC =
            CelestialCaveConfiguration.CODEC.xmap(
                    CelestialCaveCarver::new, CelestialCaveCarver::configuration);

    @Override
    public boolean isStartChunk(RandomSource random) {
        return configuration.geometry.isStartChunk(random);
    }

    @Override
    public boolean carve(
            final TerrainCarving context,
            final RandomSource random,
            final ChunkPos sourceChunkPos) {
        ChunkPos chunkPos = context.chunk.getPos();
        TerrainCarving output = context;
        int maxDistance = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
        int caveCount = configuration.geometry.count().sample(random);

        for (int cave = 0; cave < caveCount; cave++) {
            double x = sourceChunkPos.getBlockX(random.nextInt(16));
            double y = configuration.geometry.y().sample(random, context);
            double z = sourceChunkPos.getBlockZ(random.nextInt(16));
            double horizontalRadiusMultiplier =
                    configuration.geometry.horizontalRadiusMultiplier().sample(random);
            double verticalRadiusMultiplier =
                    configuration.geometry.verticalRadiusMultiplier().sample(random);
            double startVerticalRadiusMultiplier =
                    configuration.geometry.startVerticalRadiusMultiplier().sample(random);
            double floorLevel = configuration.geometry.floorLevel().sample(random);
            WorldCarver.CarveSkipChecker skipChecker =
                    (xd, yd, zd, worldY) -> shouldSkip(xd, yd, zd, floorLevel);
            int tunnels = 1;
            if (random.nextInt(4) == 0) {
                double yScale =
                        configuration.geometry.roomVerticalRadiusMultiplier().sample(random);
                float thickness = 1.0F + random.nextFloat() * 6.0F;
                this.createRoom(chunkPos, x, y, z, thickness, yScale, output, skipChecker);
                tunnels += random.nextInt(4);
            }

            for (int i = 0; i < tunnels; i++) {
                float horizontalRotation = random.nextFloat() * (float) (Math.PI * 2);
                float verticalRotation = (random.nextFloat() - 0.5F) / 4.0F;
                float thickness = this.getThickness(random);
                int distance = maxDistance - random.nextInt(maxDistance / 4);
                int initialStep = 0;
                this.createTunnel(
                        chunkPos,
                        random.nextLong(),
                        x,
                        y,
                        z,
                        horizontalRadiusMultiplier,
                        verticalRadiusMultiplier,
                        thickness,
                        horizontalRotation,
                        verticalRotation,
                        0,
                        distance,
                        startVerticalRadiusMultiplier,
                        output,
                        skipChecker);
            }
        }

        return true;
    }

    private float getThickness(final RandomSource random) {
        float thickness = configuration.geometry.thickness().sample(random);
        if (configuration.geometry.weirdThicknessBias() && random.nextInt(10) == 0) {
            thickness *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
        }

        return thickness;
    }

    private void createRoom(
            final ChunkPos chunkPos,
            final double x,
            final double y,
            final double z,
            final float thickness,
            final double yScale,
            final TerrainCarving output,
            final WorldCarver.CarveSkipChecker skipChecker) {
        double horizontalRadius = 1.5 + Mth.sin((float) (Math.PI / 2)) * thickness;
        double verticalRadius = horizontalRadius * yScale;
        output.carveEllipsoid(
                configuration,
                chunkPos,
                x + 1.0,
                y,
                z,
                horizontalRadius,
                verticalRadius,
                skipChecker);
    }

    private void createTunnel(
            final ChunkPos chunkPos,
            final long tunnelSeed,
            double x,
            double y,
            double z,
            final double horizontalRadiusMultiplier,
            final double verticalRadiusMultiplier,
            final float thickness,
            float horizontalRotation,
            float verticalRotation,
            final int step,
            final int dist,
            final double yScale,
            final TerrainCarving output,
            final WorldCarver.CarveSkipChecker skipChecker) {
        RandomSource random = RandomSource.createThreadLocalInstance(tunnelSeed);
        int splitPoint = random.nextInt(dist / 2) + dist / 4;
        boolean steep = random.nextInt(6) == 0;
        float yRota = 0.0F;
        float xRota = 0.0F;

        for (int currentStep = step; currentStep < dist; currentStep++) {
            double horizontalRadius =
                    1.5 + Mth.sin((float) Math.PI * currentStep / dist) * thickness;
            double verticalRadius = horizontalRadius * yScale;
            float cosX = Mth.cos(verticalRotation);
            x += Mth.cos(horizontalRotation) * cosX;
            y += Mth.sin(verticalRotation);
            z += Mth.sin(horizontalRotation) * cosX;
            verticalRotation *= steep ? 0.92F : 0.7F;
            verticalRotation += xRota * 0.1F;
            horizontalRotation += yRota * 0.1F;
            xRota *= 0.9F;
            yRota *= 0.75F;
            xRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (currentStep == splitPoint && thickness > 1.0F) {
                this.createTunnel(
                        chunkPos,
                        random.nextLong(),
                        x,
                        y,
                        z,
                        horizontalRadiusMultiplier,
                        verticalRadiusMultiplier,
                        random.nextFloat() * 0.5F + 0.5F,
                        horizontalRotation - (float) (Math.PI / 2),
                        verticalRotation / 3.0F,
                        currentStep,
                        dist,
                        1.0,
                        output,
                        skipChecker);
                this.createTunnel(
                        chunkPos,
                        random.nextLong(),
                        x,
                        y,
                        z,
                        horizontalRadiusMultiplier,
                        verticalRadiusMultiplier,
                        random.nextFloat() * 0.5F + 0.5F,
                        horizontalRotation + (float) (Math.PI / 2),
                        verticalRotation / 3.0F,
                        currentStep,
                        dist,
                        1.0,
                        output,
                        skipChecker);
                return;
            }

            if (random.nextInt(4) != 0) {
                if (!WorldCarver.canReach(chunkPos, x, z, currentStep, dist, thickness)) {
                    return;
                }

                output.carveEllipsoid(
                        configuration,
                        chunkPos,
                        x,
                        y,
                        z,
                        horizontalRadius * horizontalRadiusMultiplier,
                        verticalRadius * verticalRadiusMultiplier,
                        skipChecker);
            }
        }
    }

    private static boolean shouldSkip(
            final double xd, final double yd, final double zd, final double floorLevel) {
        return yd <= floorLevel ? true : xd * xd + yd * yd + zd * zd >= 1.0;
    }

    @Override
    public MapCodec<CelestialCaveCarver> codec() {
        return CODEC;
    }
}
