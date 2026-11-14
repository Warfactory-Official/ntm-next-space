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

package com.hbmspace.world.gen.structure;

import com.hbmspace.world.gen.SpaceWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class TiltedSpireStructure extends Structure {

    public record Shape(
            int minSize,
            int maxSize,
            float minPoint,
            float maxPoint,
            float minTilt,
            float maxTilt,
            boolean curve,
            int mid) {

        static final MapCodec<Shape> CODEC =
                RecordCodecBuilder.mapCodec(
                        i ->
                                i.group(
                                                Codec.intRange(1, 64)
                                                        .fieldOf("min_size")
                                                        .forGetter(Shape::minSize),
                                                Codec.intRange(2, 64)
                                                        .fieldOf("max_size")
                                                        .forGetter(Shape::maxSize),
                                                Codec.FLOAT
                                                        .fieldOf("min_point")
                                                        .forGetter(Shape::minPoint),
                                                Codec.FLOAT
                                                        .fieldOf("max_point")
                                                        .forGetter(Shape::maxPoint),
                                                Codec.FLOAT
                                                        .fieldOf("min_tilt")
                                                        .forGetter(Shape::minTilt),
                                                Codec.FLOAT
                                                        .fieldOf("max_tilt")
                                                        .forGetter(Shape::maxTilt),
                                                Codec.BOOL.fieldOf("curve").forGetter(Shape::curve),
                                                Codec.INT.fieldOf("mid").forGetter(Shape::mid))
                                        .apply(i, Shape::new));
    }

    public static final MapCodec<TiltedSpireStructure> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            settingsCodec(i),
                                            NoiseParameters.CODEC
                                                    .fieldOf("noise")
                                                    .forGetter(s -> s.noise),
                                            Codec.FLOAT
                                                    .fieldOf("threshold")
                                                    .forGetter(s -> s.threshold),
                                            Codec.intRange(1, 1024)
                                                    .fieldOf("chance_high")
                                                    .forGetter(s -> s.chanceHigh),
                                            Codec.intRange(1, 1024)
                                                    .fieldOf("chance_low")
                                                    .forGetter(s -> s.chanceLow),
                                            Shape.CODEC.forGetter(s -> s.shape),
                                            BlockState.CODEC.fieldOf("rock").forGetter(s -> s.rock),
                                            BlockState.CODEC
                                                    .fieldOf("regolith")
                                                    .forGetter(s -> s.regolith))
                                    .apply(i, TiltedSpireStructure::new));

    private final Holder<NoiseParameters> noise;
    private final float threshold;
    private final int chanceHigh;
    private final int chanceLow;
    private final Shape shape;
    private final BlockState rock;
    private final BlockState regolith;

    public TiltedSpireStructure(
            StructureSettings settings,
            Holder<NoiseParameters> noise,
            float threshold,
            int chanceHigh,
            int chanceLow,
            Shape shape,
            BlockState rock,
            BlockState regolith) {
        super(settings);
        this.noise = noise;
        this.threshold = threshold;
        this.chanceHigh = chanceHigh;
        this.chanceLow = chanceLow;
        this.shape = shape;
        this.rock = rock;
        this.regolith = regolith;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        final ResourceKey<NormalNoise.NoiseParameters> key = noise.unwrapKey().orElseThrow();
        final double x = chunk.getMinBlockX();
        final double z = chunk.getMinBlockZ();
        double field = context.randomState().getOrCreateNoise(key).getValue(x, 0.0D, z);
        int chance = field > threshold * 2.0F - 1.0F ? chanceHigh : chanceLow;
        RandomSource random = context.random();
        if (random.nextInt(chance) != 0 || random.nextInt(chance) != 0) return Optional.empty();

        float radius = random.nextInt(shape.maxSize() - shape.minSize()) + shape.minSize();
        float stretch =
                1.0F
                        / (random.nextFloat()
                                        * random.nextFloat()
                                        * (shape.maxPoint() - shape.minPoint())
                                + shape.minPoint());
        float direction = random.nextFloat() * (float) Math.PI * 2.0F;
        float tilt = random.nextFloat() * (shape.maxTilt() - shape.minTilt()) + shape.minTilt();
        if (shape.curve()) tilt *= 0.01F;

        TiltedSpirePiece piece =
                new TiltedSpirePiece(
                        chunk,
                        context.heightAccessor(),
                        radius,
                        stretch,
                        (float) Math.cos(direction) * tilt,
                        (float) Math.sin(direction) * tilt,
                        shape.curve(),
                        shape.mid(),
                        rock,
                        regolith);
        return Optional.of(
                new GenerationStub(chunk.getWorldPosition(), builder -> builder.addPiece(piece)));
    }

    @Override
    public StructureType<?> type() {
        return SpaceWorldgen.TILTED_SPIRE.get();
    }
}
