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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ChunkOddsPlacement extends RandomSpreadStructurePlacement {

    public static final MapCodec<ChunkOddsPlacement> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.floatRange(0.0F, 1.0F)
                                                    .fieldOf("frequency")
                                                    .forGetter(ChunkOddsPlacement::frequency),
                                            Codec.INT
                                                    .fieldOf("salt")
                                                    .forGetter(ChunkOddsPlacement::salt))
                                    .apply(i, ChunkOddsPlacement::new));

    public ChunkOddsPlacement(float frequency, int salt) {
        super(
                Vec3i.ZERO,
                FrequencyReductionMethod.DEFAULT,
                frequency,
                salt,
                Optional.empty(),
                1,
                0,
                RandomSpreadType.LINEAR);
    }

    @Override
    public boolean applyAdditionalChunkRestrictions(int sourceX, int sourceZ, long levelSeed) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(levelSeed + salt(), sourceX, sourceZ);
        return random.nextFloat() < frequency();
    }

    @Override
    public StructurePlacementType<?> type() {
        return SpaceWorldgen.CHUNK_ODDS.get();
    }
}
