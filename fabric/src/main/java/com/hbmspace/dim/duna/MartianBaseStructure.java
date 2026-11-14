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

package com.hbmspace.dim.duna;

import com.hbm.world.structure.TemplateStructurePiece;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldTypeTeleport;
import com.hbmspace.world.gen.Terrace;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class MartianBaseStructure extends Structure {

    public static final MapCodec<MartianBaseStructure> CODEC =
            simpleCodec(MartianBaseStructure::new);
    private static final Identifier TEMPLATE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "duna_martian_base");
    private static final ResourceKey<NoiseGeneratorSettings> DUNA =
            ResourceKey.create(Registries.NOISE_SETTINGS, SpaceDimensions.DUNA.identifier());

    public MartianBaseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!WorldTypeTeleport.isMartian()
                || !(context.chunkGenerator() instanceof NoiseBasedChunkGenerator generator)
                || !generator.stable(DUNA)
                || !context.chunkPos().equals(new ChunkPos(0, 0))) return Optional.empty();
        Optional<StructureTemplate> template = context.structureTemplateManager().get(TEMPLATE);
        if (template.isEmpty()) return Optional.empty();

        int height =
                context.chunkGenerator()
                        .getBaseHeight(
                                0,
                                0,
                                Heightmap.Types.WORLD_SURFACE_WG,
                                context.heightAccessor(),
                                context.randomState());
        Terrace terrace = Terrace.of(generator.generatorSettings());
        if (terrace != null) {
            BiomeManager biomes =
                    new BiomeManager(
                            (x, y, z) ->
                                    context.biomeSource()
                                            .getNoiseBiome(
                                                    x, y, z, context.randomState().sampler()),
                            BiomeManager.obfuscateSeed(context.seed()));
            height =
                    terrace.firstFreeHeight(
                            0,
                            0,
                            height,
                            context.heightAccessor().getMaxY(),
                            biomes,
                            context.randomState());
        }
        Vec3i size = template.get().getSize();
        BlockPos center = new BlockPos(0, height - 1, 0);
        BlockPos origin = center.offset(-size.getX() / 2, 0, -size.getZ() / 2);
        return Optional.of(
                new GenerationStub(
                        center,
                        builder ->
                                builder.addPiece(
                                        new TemplateStructurePiece(
                                                MartianBaseTypes.PIECE.get(),
                                                TEMPLATE,
                                                template.get(),
                                                origin))));
    }

    @Override
    public StructureType<?> type() {
        return MartianBaseTypes.STRUCTURE.get();
    }
}
