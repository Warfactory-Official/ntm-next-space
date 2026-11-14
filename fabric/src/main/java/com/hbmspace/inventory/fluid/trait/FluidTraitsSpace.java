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

package com.hbmspace.inventory.fluid.trait;

import com.hbm.inventory.fluid.trait.FluidTraitCodecs;
import com.hbm.inventory.fluid.trait.FluidTraitRegistry;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.inventory.fluid.trait.FluidTraitSimpleSpace.FT_EXPLOSIVE;
import com.hbmspace.inventory.fluid.trait.FluidTraitSimpleSpace.FT_ULTRAKILL;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class FluidTraitsSpace {
    static {
        FluidTraitRegistry.insertAfter("pheromone", "rocket", FT_Rocket.class);
        FluidTraitRegistry.insertAfter("rocket", "terraformer", FT_Terraformer.class);
        FluidTraitRegistry.insertAfter("unsiphonable", "uk", FT_ULTRAKILL.class);
        FluidTraitRegistry.insertAfter("uk", "explosive", FT_EXPLOSIVE.class);

        FluidTraitCodecs.register(
                "rocket",
                RecordCodecBuilder.<FT_Rocket>mapCodec(
                        i ->
                                i.group(
                                                Codec.INT
                                                        .fieldOf("isp")
                                                        .forGetter(FT_Rocket::getISP),
                                                Codec.LONG
                                                        .fieldOf("thrust")
                                                        .forGetter(FT_Rocket::getThrust))
                                        .apply(i, FT_Rocket::new)));
        FluidTraitCodecs.register(
                "terraformer",
                RecordCodecBuilder.<FT_Terraformer>mapCodec(
                        i ->
                                i.group(
                                                SolarSystem.Body.CODEC
                                                        .fieldOf("target")
                                                        .forGetter(FT_Terraformer::getTarget))
                                        .apply(i, FT_Terraformer::new)));
        FluidTraitCodecs.registerFlag("uk", FT_ULTRAKILL.INSTANCE);
        FluidTraitCodecs.registerFlag("explosive", FT_EXPLOSIVE.INSTANCE);
    }

    private FluidTraitsSpace() {}
}
