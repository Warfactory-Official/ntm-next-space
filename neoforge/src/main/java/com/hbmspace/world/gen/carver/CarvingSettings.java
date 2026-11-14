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

package com.hbmspace.world.gen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarvingSettings {
    public static final MapCodec<CarvingSettings> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.floatRange(0.0F, 1.0F)
                                                    .fieldOf("probability")
                                                    .forGetter(c -> c.probability),
                                            HeightProvider.CODEC.fieldOf("y").forGetter(c -> c.y),
                                            FloatProviders.CODEC
                                                    .fieldOf("y_scale")
                                                    .forGetter(c -> c.yScale),
                                            VerticalAnchor.CODEC
                                                    .fieldOf("lava_level")
                                                    .forGetter(c -> c.lavaLevel),
                                            RegistryCodecs.holderSet(Registries.BLOCK)
                                                    .fieldOf("replaceable")
                                                    .forGetter(c -> c.replaceable))
                                    .apply(i, CarvingSettings::new));
    public final float probability;
    public final HeightProvider y;
    public final FloatProvider yScale;
    public final VerticalAnchor lavaLevel;
    public final HolderSet<Block> replaceable;

    public CarvingSettings(
            float probability,
            HeightProvider y,
            FloatProvider yScale,
            VerticalAnchor lavaLevel,
            HolderSet<Block> replaceable) {
        this.probability = probability;
        this.y = y;
        this.yScale = yScale;
        this.lavaLevel = lavaLevel;
        this.replaceable = replaceable;
    }
}
