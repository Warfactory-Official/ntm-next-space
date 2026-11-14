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

import com.hbmspace.world.gen.SpaceWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class VolcanoStructure extends Structure {

    public static final MapCodec<VolcanoStructure> CODEC =
            RecordCodecBuilder.<VolcanoStructure>mapCodec(
                            i ->
                                    i.group(
                                                    settingsCodec(i),
                                                    Codec.intRange(1, 256)
                                                            .fieldOf("min_size")
                                                            .forGetter(s -> s.minSize),
                                                    Codec.intRange(2, 256)
                                                            .fieldOf("max_size")
                                                            .forGetter(s -> s.maxSize),
                                                    BlockState.CODEC
                                                            .fieldOf("core")
                                                            .forGetter(s -> s.core),
                                                    BlockState.CODEC
                                                            .fieldOf("material")
                                                            .forGetter(s -> s.material))
                                            .apply(i, VolcanoStructure::new))
                    .validate(
                            structure ->
                                    structure.maxSize > structure.minSize
                                            ? DataResult.success(structure)
                                            : DataResult.error(
                                                    () -> "max_size must exceed min_size"));

    private final int minSize;
    private final int maxSize;
    private final BlockState core;
    private final BlockState material;

    public VolcanoStructure(
            StructureSettings settings,
            int minSize,
            int maxSize,
            BlockState core,
            BlockState material) {
        super(settings);
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.core = core;
        this.material = material;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        BlockPos centre = new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
        int radius = minSize + context.random().nextInt(maxSize - minSize);
        int minY = context.heightAccessor().getMinY();
        int maxY = context.heightAccessor().getMaxY();
        return Optional.of(
                new GenerationStub(
                        centre,
                        builder ->
                                builder.addPiece(
                                        new VolcanoPiece(
                                                centre, radius, minY, maxY, core, material))));
    }

    public int maxRadius() {
        return maxSize - 1;
    }

    @Override
    public StructureType<?> type() {
        return SpaceWorldgen.VOLCANO.get();
    }
}
