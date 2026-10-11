// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.fluid.trait;

import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTraitCodecs;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.inventory.fluid.trait.FluidTraitSimpleSpace.FT_EXPLOSIVE;
import com.hbmspace.inventory.fluid.trait.FluidTraitSimpleSpace.FT_ULTRAKILL;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class FluidTraitsSpace {
    static {
        FluidTrait.registerTrait("rocket", FT_Rocket.class, "pheromone");
        FluidTrait.registerTrait("terraformer", FT_Terraformer.class, "rocket");
        FluidTrait.registerTrait("uk", FT_ULTRAKILL.class, "unsiphonable");
        FluidTrait.registerTrait("explosive", FT_EXPLOSIVE.class, "uk");

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
