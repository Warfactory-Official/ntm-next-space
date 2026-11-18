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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.VeryBiasedToBottomInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;

public class CelestialCaveConfiguration extends CarvingSettings {

    public static final MapCodec<CelestialCaveConfiguration> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            CarvingSettings.CODEC.forGetter(c -> c),
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
                                            RegistryCodecs.holderSet(Registries.BLOCK)
                                                    .fieldOf("top")
                                                    .forGetter(c -> c.top),
                                            RegistryCodecs.holderSet(Registries.BLOCK)
                                                    .fieldOf("filler")
                                                    .forGetter(c -> c.filler))
                                    .apply(i, CelestialCaveConfiguration::new));

    final CaveWorldCarver geometry;
    public final FloatProvider horizontalRadiusMultiplier;
    public final FloatProvider verticalRadiusMultiplier;
    public final FloatProvider floorLevel;
    public final BlockState lava;
    public final HolderSet<Block> top;
    public final HolderSet<Block> filler;

    public CelestialCaveConfiguration(
            CarvingSettings carver,
            FloatProvider horizontalRadiusMultiplier,
            FloatProvider verticalRadiusMultiplier,
            FloatProvider floorLevel,
            BlockState lava,
            HolderSet<Block> top,
            HolderSet<Block> filler) {
        super(carver.probability, carver.y, carver.yScale, carver.lavaLevel, carver.replaceable);
        this.horizontalRadiusMultiplier = horizontalRadiusMultiplier;
        this.verticalRadiusMultiplier = verticalRadiusMultiplier;
        this.floorLevel = floorLevel;
        this.geometry =
                new CaveWorldCarver(
                        probability,
                        y,
                        VeryBiasedToBottomInt.of(0, 14),
                        TrapezoidFloat.of(0.0F, 3.0F, 1.0F),
                        true,
                        yScale,
                        horizontalRadiusMultiplier,
                        verticalRadiusMultiplier,
                        ConstantFloat.of(1.0F),
                        floorLevel);
        this.lava = lava;
        this.top = top;
        this.filler = filler;
    }
}
