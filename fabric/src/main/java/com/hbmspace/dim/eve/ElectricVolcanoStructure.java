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

package com.hbmspace.dim.eve;

import com.hbmspace.dim.eve.biome.BiomeGenBaseEve;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ElectricVolcanoStructure extends Structure {

    public static final MapCodec<ElectricVolcanoStructure> CODEC =
            simpleCodec(ElectricVolcanoStructure::new);

    private static final int SILT_FROM = 62;

    public ElectricVolcanoStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        WorldgenRandom random = context.random();
        int x = chunk.getMinBlockX() + random.nextInt(16) + 8;
        int z = chunk.getMinBlockZ() + random.nextInt(16) + 8;

        final int x2 = QuartPos.fromBlock(x);
        final int z2 = QuartPos.fromBlock(z);
        Holder<Biome> biome =
                context.biomeSource().getNoiseBiome(x2, 0, z2, context.randomState().sampler());
        if (!biome.is(BiomeGenBaseEve.evePlains.key)
                && !biome.is(BiomeGenBaseEve.eveMountains.key)) {
            return Optional.empty();
        }
        int y =
                context.chunkGenerator()
                                .getBaseHeight(
                                        x,
                                        z,
                                        Heightmap.Types.OCEAN_FLOOR_WG,
                                        context.heightAccessor(),
                                        context.randomState())
                        - 1;
        int top =
                context.chunkGenerator()
                                .getBaseHeight(
                                        x,
                                        z,
                                        Heightmap.Types.WORLD_SURFACE_WG,
                                        context.heightAccessor(),
                                        context.randomState())
                        - 1;
        if (y < SILT_FROM || top != y) return Optional.empty();

        long seed = random.nextLong();
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(
                new GenerationStub(
                        origin,
                        builder -> builder.addPiece(new ElectricVolcanoPiece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return EveStructureTypes.ELECTRIC_VOLCANO.get();
    }
}
