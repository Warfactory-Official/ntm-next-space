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

package com.hbmspace.world;

import com.hbm.data.WorldData;
import com.hbm.platform.BiomeInjection;
import com.hbm.world.BiomeTarget;
import com.hbm.world.HbmFeatureAttachments.Attachment;
import com.hbm.world.HbmPlacedFeatures;
import com.hbmspace.HbmSpace;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class OverworldFeaturesSpace {

    public static final int FLOWER_ODDS = 32;

    public static final ResourceKey<PlacedFeature> STRAWBERRY = key("strawberry");
    public static final ResourceKey<PlacedFeature> MINT = key("mint");
    public static final ResourceKey<PlacedFeature> ORE_ZINC = key("ore_zinc");

    public static final List<ResourceKey<PlacedFeature>> REMOVED =
            List.of(HbmPlacedFeatures.ORE_AUSTRALIUM, HbmPlacedFeatures.ORE_GNEISS_LITHIUM);

    private static final BiomeTarget PLAINS = BiomeTarget.vanilla("plains", "sunflower_plains");

    public static final List<Attachment> ALL =
            List.of(
                    new Attachment(
                            STRAWBERRY,
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            PLAINS,
                            List.of()),
                    new Attachment(
                            MINT, GenerationStep.Decoration.VEGETAL_DECORATION, PLAINS, List.of()),
                    new Attachment(
                            ORE_ZINC,
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            BiomeTarget.tags(BiomeTags.IS_OVERWORLD),
                            List.of(WorldData.OVERWORLD_ORES)));

    static {
        for (Attachment attachment : ALL) {
            BiomeInjection.addFeaturesToBiome(
                    attachment.biomes(), attachment.step(), attachment.feature());
        }
    }

    private OverworldFeaturesSpace() {}

    private static ResourceKey<PlacedFeature> key(String path) {
        return ResourceKey.create(
                Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path));
    }
}
