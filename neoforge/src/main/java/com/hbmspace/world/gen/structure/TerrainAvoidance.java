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

import com.hbm.world.gen.WorldgenHeight;
import com.hbm.world.structure.LegacyJigsawStructure;
import com.hbm.world.structure.SellafieldCraterPiece;
import com.hbm.world.structure.SellafieldCraterStructure;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public final class TerrainAvoidance {
    private static final Set<ResourceKey<Structure>> STRUCTURES =
            Set.of(
                    key("mun_base"),
                    key("minmus_base"),
                    key("ike_artifact"),
                    key("ike_ancient_tomb"),
                    key("laythe_vertibird"),
                    key("laythe_nuke_sub"),
                    key("moho_base"));
    private static final ResourceKey<Structure> MOHO_BASE = key("moho_base");

    private TerrainAvoidance() {}

    private static ResourceKey<Structure> key(String path) {
        return ResourceKey.create(
                Registries.STRUCTURE, Identifier.fromNamespaceAndPath("hbmspace", path));
    }

    public static boolean applies(Holder<Structure> structure) {
        return structure.unwrapKey().map(STRUCTURES::contains).orElse(false);
    }

    public static boolean intersectsTerrain(
            Structure.GenerationContext context,
            ChunkGeneratorStructureState state,
            Holder<Structure> structure,
            List<StructurePiece> pieces,
            List<FeatureSorter.StepFeatureData> features) {
        if (CraterGrounding.applies(structure.value(), context)) return false;
        BoundingBox bounds =
                BoundingBox.encapsulatingBoxes(
                                pieces.stream().map(StructurePiece::getBoundingBox).toList())
                        .orElseThrow();
        boolean moho = structure.is(MOHO_BASE);
        PoolElementStructurePiece anchor =
                moho ? (PoolElementStructurePiece) pieces.getFirst() : null;
        int[] mohoHeights = moho ? MohoGrounding.upperHeights(context, anchor, features) : null;
        int[] mohoRaises = moho ? new int[mohoHeights.length] : null;
        if (moho
                && MohoGrounding.changesHeight(
                        context, (LegacyJigsawStructure) structure.value(), anchor, mohoHeights))
            return true;
        boolean solidSurface =
                context.chunkGenerator() instanceof NoiseBasedChunkGenerator noise
                        && (noise.generatorSettings().value().defaultFluid().isAir()
                                || noise.generatorSettings()
                                        .value()
                                        .defaultFluid()
                                        .is(BlockTags.BLOCKS_MOTION));
        int[][] heights = new int[pieces.size()][];
        for (var holder : state.possibleStructureSets()) {
            StructureSet set = holder.value();
            if (set.structures().stream().noneMatch(entry -> isTerrain(entry.structure().value())))
                continue;
            if (set.structures().size() != 1) {
                throw new IllegalStateException(
                        "Terrain avoidance requires a single terrain structure: "
                                + holder.getRegisteredName());
            }
            Structure terrain = set.structures().getFirst().structure().value();
            int reach =
                    switch (terrain) {
                        case SellafieldCraterStructure crater -> crater.maxRadius() + 2;
                        case VolcanoStructure volcano -> volcano.maxRadius() + 15;
                        case TiltedSpireStructure spire -> TiltedSpirePiece.REACH + 15;
                        default -> throw new IllegalStateException(terrain.toString());
                    };
            int centreOffset = terrain instanceof TiltedSpireStructure ? 0 : 8;
            int minX = (bounds.minX() - reach - centreOffset) >> 4;
            int maxX = (bounds.maxX() + reach - centreOffset) >> 4;
            int minZ = (bounds.minZ() - reach - centreOffset) >> 4;
            int maxZ = (bounds.maxZ() + reach - centreOffset) >> 4;
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (!set.placement().isStructureChunk(state, x, z)) continue;
                    var candidate =
                            new Structure.GenerationContext(
                                    context.registryAccess(),
                                    context.chunkGenerator(),
                                    context.biomeSource(),
                                    context.climateSampler(),
                                    context.randomState(),
                                    context.structureTemplateManager(),
                                    context.seed(),
                                    new ChunkPos(x, z),
                                    context.heightAccessor(),
                                    terrain.biomes()::contains);
                    var point = terrain.findValidGenerationPoint(candidate);
                    if (point.isEmpty()) continue;
                    StructurePiece stamp =
                            point.get().getPiecesBuilder().build().pieces().getFirst();
                    if (moho && stamp instanceof VolcanoPiece volcano) {
                        var core = anchor.getBoundingBox();
                        int index = 0;
                        for (int bz = core.minZ(); bz <= core.maxZ(); bz++)
                            for (int bx = core.minX(); bx <= core.maxX(); bx++) {
                                mohoRaises[index++] += volcano.maxAddedHeight(bx, bz);
                            }
                        continue;
                    }
                    if (moho && stamp instanceof SellafieldCraterPiece crater) {
                        for (var piece : pieces)
                            if (crater.intersectsCarving(piece.getBoundingBox())) return true;
                        var core = anchor.getBoundingBox();
                        int index = 0;
                        for (int bz = core.minZ(); bz <= core.maxZ(); bz++)
                            for (int bx = core.minX(); bx <= core.maxX(); bx++) {
                                if (crater.affectsColumn(bx, bz)) {
                                    mohoHeights[index] =
                                            Math.max(
                                                    mohoHeights[index],
                                                    context.chunkGenerator()
                                                            .getBaseHeight(
                                                                    bx,
                                                                    bz,
                                                                    Heightmap.Types
                                                                            .WORLD_SURFACE_WG,
                                                                    context.heightAccessor(),
                                                                    context.randomState()));
                                }
                                index++;
                            }
                        continue;
                    }
                    for (int i = 0; i < pieces.size(); i++) {
                        BoundingBox box = pieces.get(i).getBoundingBox();
                        if (!stamp.getBoundingBox()
                                .intersects(box.minX(), box.minZ(), box.maxX(), box.maxZ()))
                            continue;
                        boolean intersects =
                                switch (stamp) {
                                    case SellafieldCraterPiece crater ->
                                            solidSurface
                                                    ? crater.intersectsCarving(box)
                                                    : crater.intersectsHorizontal(box);
                                    case VolcanoPiece volcano -> volcano.intersectsHorizontal(box);
                                    case TiltedSpirePiece spire -> {
                                        if (heights[i] == null)
                                            heights[i] =
                                                    WorldgenHeight.baseHeights(
                                                            context.chunkGenerator(),
                                                            context.heightAccessor(),
                                                            context.randomState(),
                                                            box);
                                        yield spire.intersectsSurface(box, heights[i]);
                                    }
                                    default -> throw new IllegalStateException(stamp.toString());
                                };
                        if (intersects) return true;
                    }
                }
            }
        }
        if (moho) {
            for (int i = 0; i < mohoHeights.length; i++) mohoHeights[i] += mohoRaises[i];
            return MohoGrounding.changesHeight(
                    context, (LegacyJigsawStructure) structure.value(), anchor, mohoHeights);
        }
        return false;
    }

    private static boolean isTerrain(Structure structure) {
        return structure instanceof SellafieldCraterStructure
                || structure instanceof TiltedSpireStructure
                || structure instanceof VolcanoStructure;
    }
}
