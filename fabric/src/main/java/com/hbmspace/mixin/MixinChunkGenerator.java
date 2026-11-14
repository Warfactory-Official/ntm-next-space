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

package com.hbmspace.mixin;

import com.hbm.world.structure.StructureGrounding;
import com.hbmspace.world.gen.structure.CraterGrounding;
import com.hbmspace.world.gen.structure.TerrainAvoidance;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator implements StructureGrounding {
    @Shadow @Final private Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;

    @Override
    public boolean modifiesGround(Structure structure, Structure.GenerationContext context) {
        return CraterGrounding.applies(structure, context);
    }

    @Override
    public int[] groundAverages(Structure.GenerationContext context, List<StructurePiece> pieces) {
        return CraterGrounding.groundAverages(context, pieces);
    }

    @Inject(
            method =
                    "tryGenerateStructure(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
                            + "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;"
                            + "Lnet/minecraft/world/level/levelgen/RandomState;"
                            + "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;J"
                            + "Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/ChunkPos;"
                            + "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/biome/Climate$Sampler;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void hbmspace$avoidTerrain(
            StructureSet.StructureSelectionEntry selected,
            StructureManager manager,
            RegistryAccess registries,
            RandomState random,
            StructureTemplateManager templates,
            long seed,
            ChunkAccess chunk,
            ChunkPos pos,
            ResourceKey<Level> dimension,
            Climate.Sampler sampler,
            CallbackInfoReturnable<Boolean> cir) {
        if (!TerrainAvoidance.applies(selected.structure())) return;
        ChunkGenerator generator = (ChunkGenerator) (Object) this;
        Structure structure = selected.structure().value();
        var context =
                new Structure.GenerationContext(
                        registries,
                        generator,
                        generator.getBiomeSource(),
                        sampler,
                        random,
                        templates,
                        seed,
                        pos,
                        chunk,
                        structure.biomes()::contains);
        if (CraterGrounding.applies(structure, context)) return;
        StructureStart previous = manager.getStartForStructure(structure, chunk);
        int references = previous == null ? 0 : previous.getReferences();
        StructureStart start =
                structure.generate(
                        selected.structure(),
                        dimension,
                        registries,
                        generator,
                        generator.getBiomeSource(),
                        sampler,
                        random,
                        templates,
                        seed,
                        pos,
                        references,
                        chunk,
                        structure.biomes()::contains);
        if (!start.isValid()) {
            cir.setReturnValue(false);
            return;
        }
        ChunkGeneratorStructureState state =
                generator.createState(
                        registries.lookupOrThrow(Registries.STRUCTURE_SET), random, seed);
        if (!TerrainAvoidance.intersectsTerrain(
                context, state, selected.structure(), start.getPieces(), featuresPerStep.get())) {
            manager.setStartForStructure(structure, start, chunk);
        }
        cir.setReturnValue(true);
    }
}
