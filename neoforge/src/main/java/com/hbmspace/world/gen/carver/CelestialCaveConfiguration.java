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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;

public class CelestialCaveConfiguration extends CaveCarverConfiguration {

    public static final Codec<CelestialCaveConfiguration> CODEC =
            RecordCodecBuilder.create(
                    i ->
                            i.group(
                                            CarverConfiguration.CODEC.forGetter(c -> c),
                                            FloatProviders.CODEC
                                                    .fieldOf("horizontal_radius_multiplier")
                                                    .forGetter(c -> c.horizontalRadiusMultiplier),
                                            FloatProviders.CODEC
                                                    .fieldOf("vertical_radius_multiplier")
                                                    .forGetter(c -> c.verticalRadiusMultiplier),
                                            FloatProviders.codec(-1.0F, 1.0F)
                                                    .fieldOf("floor_level")
                                                    .forGetter(c -> c.floorLevel),
                                            BlockState.CODEC.fieldOf("lava").forGetter(c -> c.lava),
                                            RegistryCodecs.homogeneousList(Registries.BLOCK)
                                                    .fieldOf("top")
                                                    .forGetter(c -> c.top),
                                            RegistryCodecs.homogeneousList(Registries.BLOCK)
                                                    .fieldOf("filler")
                                                    .forGetter(c -> c.filler))
                                    .apply(i, CelestialCaveConfiguration::new));

    public final BlockState lava;
    public final HolderSet<Block> top;
    public final HolderSet<Block> filler;

    public CelestialCaveConfiguration(
            CarverConfiguration carver,
            FloatProvider horizontalRadiusMultiplier,
            FloatProvider verticalRadiusMultiplier,
            FloatProvider floorLevel,
            BlockState lava,
            HolderSet<Block> top,
            HolderSet<Block> filler) {
        super(carver, horizontalRadiusMultiplier, verticalRadiusMultiplier, floorLevel);
        this.lava = lava;
        this.top = top;
        this.filler = filler;
    }
}
