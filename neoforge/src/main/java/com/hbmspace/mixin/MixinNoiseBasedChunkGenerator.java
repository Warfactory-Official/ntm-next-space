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

import com.hbmspace.world.gen.Terrace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseBasedChunkGenerator {
    @WrapOperation(
            method =
                    "buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;"
                            + "Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;"
                            + "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;"
                            + "Lnet/minecraft/world/level/levelgen/blending/Blender;Ljava/util/Set;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/SurfaceSystem;buildSurface("
                                            + "Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;Z"
                                            + "Lnet/minecraft/world/level/levelgen/WorldGenerationContext;"
                                            + "Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;"
                                            + "Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;Ljava/util/Set;)V"))
    private void hbmspace$terrace(
            SurfaceSystem system,
            RandomState random,
            BiomeManager biomes,
            boolean legacyRandom,
            WorldGenerationContext context,
            ChunkAccess chunk,
            NoiseChunk noise,
            SurfaceRules.RuleSource rule,
            @Nullable Set<Holder<Biome>> possibleBiomes,
            Operation<Void> surface) {
        Terrace terrace =
                Terrace.of(((NoiseBasedChunkGenerator) (Object) this).generatorSettings());
        Terrace.Cover cover = terrace == null ? null : terrace.raise(chunk, biomes, random);
        surface.call(
                system, random, biomes, legacyRandom, context, chunk, noise, rule, possibleBiomes);
        if (cover != null) cover.lay();
    }
}
