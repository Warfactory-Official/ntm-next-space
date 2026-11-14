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
import com.hbm.world.gen.nbt.LegacyPlacement;
import com.hbm.world.structure.OilBubblePiece;
import com.hbm.world.structure.OilBubbleStructure;
import com.hbm.world.structure.SellafieldCraterPiece;
import com.hbm.world.structure.SellafieldCraterStructure;
import com.hbmspace.world.gen.carver.SurfaceCarving;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public final class CraterGrounding {
    private static final Identifier MUN_BASE =
            Identifier.fromNamespaceAndPath("hbmspace", "mun_base");
    private static final Identifier MINMUS_BASE =
            Identifier.fromNamespaceAndPath("hbmspace", "minmus_base");
    private static final ResourceKey<NoiseGeneratorSettings> MUN_TERRAIN =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath("hbmspace", "mun"));
    private static final ResourceKey<NoiseGeneratorSettings> MINMUS_TERRAIN =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS,
                    Identifier.fromNamespaceAndPath("hbmspace", "minmus"));

    private CraterGrounding() {}

    public static boolean applies(Structure structure, Structure.GenerationContext context) {
        Identifier name =
                context.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(structure);
        if (context.chunkGenerator().getClass() != NoiseBasedChunkGenerator.class) return false;
        var settings = ((NoiseBasedChunkGenerator) context.chunkGenerator()).generatorSettings();
        return MUN_BASE.equals(name) && settings.is(MUN_TERRAIN)
                || MINMUS_BASE.equals(name) && settings.is(MINMUS_TERRAIN);
    }

    public static int[] groundAverages(
            Structure.GenerationContext context, List<StructurePiece> pieces) {
        var surface = new Surface(context, pieces);
        surface.sample();
        return surface.averages(pieces);
    }

    private static boolean[] sampled(List<StructurePiece> pieces) {
        boolean[] out = new boolean[pieces.size()];
        boolean anchor = false;
        for (int i = 0; i < pieces.size(); i++) {
            var placement =
                    (LegacyPlacement) ((PoolElementStructurePiece) pieces.get(i)).getElement();
            out[i] = placement.alignToTerrain() || !anchor;
            if (!placement.alignToTerrain()) anchor = true;
        }
        return out;
    }

    private static final class Surface {
        private final Structure.GenerationContext context;
        private final BoundingBox bounds;
        private final int minChunkX;
        private final int minChunkZ;
        private final int chunkSpanX;
        private final boolean[] needed;
        private final int[] heights;
        private final ProtoChunk[] chunks;
        private final boolean[] filled;

        private Surface(Structure.GenerationContext context, List<StructurePiece> pieces) {
            this.context = context;
            boolean[] samples = sampled(pieces);
            List<BoundingBox> boxes = new ArrayList<>();
            for (int i = 0; i < pieces.size(); i++)
                if (samples[i]) boxes.add(pieces.get(i).getBoundingBox());
            bounds = BoundingBox.encapsulatingBoxes(boxes).orElseThrow();
            minChunkX = bounds.minX() >> 4;
            minChunkZ = bounds.minZ() >> 4;
            chunkSpanX = (bounds.maxX() >> 4) - minChunkX + 1;
            int count = chunkSpanX * ((bounds.maxZ() >> 4) - minChunkZ + 1);
            needed = new boolean[count * 256];
            heights = new int[needed.length];
            Arrays.fill(heights, Integer.MIN_VALUE);
            chunks = new ProtoChunk[count];
            filled = new boolean[count];
            for (var box : boxes) {
                for (int z = box.minZ(); z <= box.maxZ(); z++)
                    for (int x = box.minX(); x <= box.maxX(); x++) {
                        needed[index(x, z)] = true;
                    }
            }
        }

        private int index(int x, int z) {
            return (((z >> 4) - minChunkZ) * chunkSpanX + (x >> 4) - minChunkX) * 256
                    + (z & 15) * 16
                    + (x & 15);
        }

        private ProtoChunk fill(int slot) {
            if (!filled[slot]) {
                WorldgenHeight.fillUnstructuredTerrain(context, chunks[slot]);
                filled[slot] = true;
            }
            return chunks[slot];
        }

        private void sample() {
            var generator = (NoiseBasedChunkGenerator) context.chunkGenerator();
            var fluid = generator.generatorSettings().value().defaultFluid();
            if (!fluid.isAir() && !fluid.blocksMotion()) {
                throw new IllegalStateException("Crater grounding requires an air or solid sea");
            }
            var palettes = PalettedContainerFactory.create(context.registryAccess());
            int[] top = new int[256];
            for (int slot = 0; slot < chunks.length; slot++) {
                int cx = minChunkX + slot % chunkSpanX, cz = minChunkZ + slot / chunkSpanX;
                int x0 = 16, z0 = 16, x1 = -1, z1 = -1;
                for (int z = 0; z < 16; z++)
                    for (int x = 0; x < 16; x++) {
                        if (!needed[slot * 256 + z * 16 + x]) continue;
                        x0 = Math.min(x0, x);
                        x1 = Math.max(x1, x);
                        z0 = Math.min(z0, z);
                        z1 = Math.max(z1, z);
                    }
                if (x1 < x0) continue;
                var box =
                        new BoundingBox(
                                cx * 16 + x0,
                                context.heightAccessor().getMinY(),
                                cz * 16 + z0,
                                cx * 16 + x1,
                                context.heightAccessor().getMinY(),
                                cz * 16 + z1);
                int[] raw =
                        WorldgenHeight.baseHeights(
                                generator, context.heightAccessor(), context.randomState(), box);
                Arrays.fill(top, Integer.MIN_VALUE);
                for (int z = z0; z <= z1; z++)
                    for (int x = x0; x <= x1; x++) {
                        int at = z * 16 + x;
                        if (needed[slot * 256 + at])
                            top[at] = raw[(z - z0) * box.getXSpan() + x - x0];
                    }
                chunks[slot] =
                        new ProtoChunk(
                                new ChunkPos(cx, cz),
                                UpgradeData.EMPTY,
                                context.heightAccessor(),
                                palettes,
                                null);
                if (SurfaceCarving.mayChange(context, chunks[slot], top)) {
                    ProtoChunk chunk = fill(slot);
                    for (int z = z0; z <= z1; z++)
                        for (int x = x0; x <= x1; x++) {
                            int at = z * 16 + x;
                            if (needed[slot * 256 + at])
                                top[at] = WorldgenHeight.ground(chunk, cx * 16 + x, cz * 16 + z);
                        }
                }
                System.arraycopy(top, 0, heights, slot * 256, 256);
            }
            var state =
                    generator.createState(
                            context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
                            context.randomState(),
                            context.seed());
            List<SellafieldCraterPiece> craters = new ArrayList<>();
            for (var holder : state.possibleStructureSets()) {
                StructureSet set = holder.value();
                boolean terrain = false;
                for (var candidate : set.structures()) {
                    if (candidate.structure().value() instanceof SellafieldCraterStructure
                            || candidate.structure().value() instanceof OilBubbleStructure)
                        terrain = true;
                }
                if (!terrain) continue;
                if (set.structures().size() != 1)
                    throw new IllegalStateException(
                            "Mixed terrain set: " + holder.getRegisteredName());
                var entry = set.structures().getFirst();
                boolean crater = entry.structure().value() instanceof SellafieldCraterStructure;
                int reach =
                        crater
                                ? ((SellafieldCraterStructure) entry.structure().value())
                                                .maxRadius()
                                        + 2
                                : 0;
                for (int z = (bounds.minZ() - reach - 8) >> 4;
                        z <= (bounds.maxZ() + reach - 8) >> 4;
                        z++) {
                    for (int x = (bounds.minX() - reach - 8) >> 4;
                            x <= (bounds.maxX() + reach - 8) >> 4;
                            x++) {
                        if (!set.placement().isStructureChunk(state, x, z)) continue;
                        var candidate =
                                new Structure.GenerationContext(
                                        context.registryAccess(),
                                        generator,
                                        context.biomeSource(),
                                        context.randomState(),
                                        context.structureTemplateManager(),
                                        context.seed(),
                                        new ChunkPos(x, z),
                                        context.heightAccessor(),
                                        entry.structure().value().biomes()::contains);
                        var point = entry.structure().value().findValidGenerationPoint(candidate);
                        if (point.isEmpty()) continue;
                        var stamp = point.get().getPiecesBuilder().build().pieces().getFirst();
                        if (stamp instanceof SellafieldCraterPiece bowl) {
                            if (!bowl.hasSolidFill())
                                throw new IllegalStateException(
                                        "Crater grounding requires solid refill");
                            craters.add(bowl);
                        } else {
                            var pocket = (OilBubblePiece) stamp;
                            int bx = pocket.scarX(), bz = pocket.scarZ();
                            int at = index(bx, bz);
                            if (bx < bounds.minX()
                                    || bx > bounds.maxX()
                                    || bz < bounds.minZ()
                                    || bz > bounds.maxZ()
                                    || !needed[at]) continue;
                            ProtoChunk chunk = fill(at / 256);
                            pocket.projectHole(chunk);
                            heights[at] = WorldgenHeight.ground(chunk, bx, bz);
                        }
                    }
                }
            }
            for (var crater : craters) {
                for (int slot = 0; slot < chunks.length; slot++) {
                    if (chunks[slot] == null) continue;
                    ChunkPos at = chunks[slot].getPos();
                    for (int z = 0; z < 16; z++)
                        for (int x = 0; x < 16; x++) {
                            int i = slot * 256 + z * 16 + x;
                            if (needed[i])
                                heights[i] =
                                        crater.groundHeight(
                                                heights[i],
                                                at.getBlockX(x),
                                                at.getBlockZ(z),
                                                context.heightAccessor().getMinY());
                        }
                }
            }
        }

        private int[] averages(List<StructurePiece> pieces) {
            int[] result = new int[pieces.size()];
            boolean[] samples = sampled(pieces);
            for (int i = 0; i < pieces.size(); i++) {
                if (!samples[i]) continue;
                var box = pieces.get(i).getBoundingBox();
                long total = 0;
                for (int z = box.minZ(); z <= box.maxZ(); z++)
                    for (int x = box.minX(); x <= box.maxX(); x++) {
                        total += heights[index(x, z)];
                    }
                result[i] = (int) (total / ((long) box.getXSpan() * box.getZSpan()));
            }
            return result;
        }
    }
}
