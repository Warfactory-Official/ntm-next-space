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
import com.hbmspace.world.gen.carver.TerrainCarver;
import com.hbmspace.world.gen.carver.TerrainCarving;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseBasedChunkGenerator {
    @Unique private volatile byte hbmspace$terrainCarvers;

    @Unique
    private boolean hbmspace$usesTerrainCarvers() {
        byte kind = hbmspace$terrainCarvers;
        if (kind != 0) return kind == 2;
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator) (Object) this;
        for (Holder<Biome> biome : generator.getBiomeSource().possibleBiomes()) {
            for (Holder<WorldCarver> carver :
                    generator.getBiomeGenerationSettings(biome).getCarvers()) {
                if (carver.value() instanceof TerrainCarver) {
                    hbmspace$terrainCarvers = 2;
                    return true;
                }
            }
        }
        hbmspace$terrainCarvers = 1;
        return false;
    }

    @Shadow
    private BiomeGenerationSettings getBiomeGenerationSettingsForCarver(
            BiomeResolver resolver, ChunkPos pos) {
        throw new AssertionError();
    }

    @Shadow
    private void applyCarvingMask(
            ChunkAccess chunk,
            CarvingMask mask,
            RandomState random,
            MaterialRule material,
            WorldGenerationContext context,
            NoiseChunk noise,
            Function<BlockPos, Holder<Biome>> biomes,
            CarvingMask.@Nullable Filter filter) {
        throw new AssertionError();
    }

    @Inject(
            method =
                    "generateCarvers(Lnet/minecraft/world/level/chunk/ChunkAccess;"
                            + "Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/NoiseChunk;"
                            + "Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;"
                            + "Lnet/minecraft/server/level/WorldGenRegion;"
                            + "Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void hbmspace$carveTerrain(
            ChunkAccess chunk,
            Blender blender,
            NoiseChunk noise,
            RandomState randomState,
            BiomeManager biomeManager,
            @Nullable WorldGenRegion carverBiomeRegion,
            MaterialRule material,
            CallbackInfo ci) {
        if (!hbmspace$usesTerrainCarvers()) return;
        ci.cancel();
        if (SharedConstants.DEBUG_DISABLE_CARVERS
                || SharedConstants.debugVoidTerrain(chunk.getPos())) return;
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator) (Object) this;
        BiomeResolver resolver = generator.getBiomeSource().createUncachedResolver(randomState);
        WorldgenRandom random =
                new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        BiomeManager correctBiomes = biomeManager.withDifferentSource(resolver);
        Function<BlockPos, Holder<Biome>> biomes = correctBiomes::getBiome;
        CarvingMask.Filter filter = blender.getCarvingFilter();
        TerrainCarving context =
                new TerrainCarving(generator, chunk, randomState, noise, material, biomes, filter);
        ChunkPos pos = chunk.getPos();
        @Nullable CarvingMask pending = null;
        for (int dx = -8; dx <= 8; ++dx)
            for (int dz = -8; dz <= 8; ++dz) {
                ChunkPos sourcePos = new ChunkPos(pos.x() + dx, pos.z() + dz);
                BiomeGenerationSettings settings;
                if (carverBiomeRegion != null) {
                    ChunkAccess center = carverBiomeRegion.getChunk(sourcePos.x(), sourcePos.z());
                    settings =
                            center.carverBiome(
                                    () -> getBiomeGenerationSettingsForCarver(resolver, sourcePos));
                } else {
                    settings = getBiomeGenerationSettingsForCarver(resolver, sourcePos);
                }
                int index = 0;
                for (Holder<WorldCarver> holder : settings.getCarvers()) {
                    WorldCarver carver = holder.value();
                    random.setLargeFeatureSeed(
                            randomState.seed() + index, sourcePos.x(), sourcePos.z());
                    if (carver.isStartChunk(random)) {
                        if (carver instanceof TerrainCarver terrain) {
                            if (pending != null) {
                                if (!pending.isEmpty()) {
                                    hbmspace$flushNative(
                                            chunk,
                                            pending,
                                            randomState,
                                            material,
                                            context,
                                            noise,
                                            biomes,
                                            filter);
                                }
                                pending = null;
                            }
                            terrain.carve(context, random, sourcePos);
                        } else {
                            if (pending == null)
                                pending = new CarvingMask(context.minY(), context.maxY());
                            carver.carve(context, random, pos, sourcePos, pending);
                        }
                    }
                    ++index;
                }
            }
        if (pending != null && !pending.isEmpty()) {
            hbmspace$flushNative(
                    chunk, pending, randomState, material, context, noise, biomes, filter);
        }
    }

    @Unique
    private void hbmspace$flushNative(
            ChunkAccess chunk,
            CarvingMask pending,
            RandomState random,
            MaterialRule material,
            TerrainCarving context,
            NoiseChunk noise,
            Function<BlockPos, Holder<Biome>> biomes,
            CarvingMask.@Nullable Filter filter) {
        CarvingMask cuts = context.unvisitedNativeCuts(pending);
        if (!cuts.isEmpty()) {
            try (Zone ignored = Profiler.get().zone("applyCarvingMask")) {
                applyCarvingMask(chunk, cuts, random, material, context, noise, biomes, filter);
            }
        }
    }

    @WrapOperation(
            method =
                    "buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;"
                            + "Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/levelgen/RandomState;"
                            + "Lnet/minecraft/world/level/biome/BiomeManager;Ljava/util/Set;"
                            + "Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/material/MaterialSystem;buildSurface("
                                            + "Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;"
                                            + "Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/chunk/ChunkAccess;"
                                            + "Lnet/minecraft/world/level/levelgen/NoiseChunk;"
                                            + "Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;Ljava/util/Set;)V"),
            require = 1,
            expect = 1,
            allow = 1)
    private void hbmspace$terrace(
            MaterialSystem system,
            RandomState random,
            BiomeManager biomes,
            WorldGenerationContext context,
            ChunkAccess chunk,
            NoiseChunk noise,
            MaterialRule rule,
            @Nullable Set<Holder<Biome>> possibleBiomes,
            Operation<Void> surface) {
        Terrace terrace =
                Terrace.of(((NoiseBasedChunkGenerator) (Object) this).generatorSettings());
        Terrace.Cover cover = terrace == null ? null : terrace.raise(chunk, biomes, random);
        surface.call(system, random, biomes, context, chunk, noise, rule, possibleBiomes);
        if (cover != null) cover.lay();
    }
}
