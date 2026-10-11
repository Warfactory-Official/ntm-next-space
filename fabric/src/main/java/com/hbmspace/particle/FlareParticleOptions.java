// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FlareParticleOptions(float scale) implements ParticleOptions {

    public static final MapCodec<FlareParticleOptions> CODEC =
            Codec.FLOAT
                    .fieldOf("scale")
                    .xmap(FlareParticleOptions::new, FlareParticleOptions::scale);
    public static final StreamCodec<RegistryFriendlyByteBuf, FlareParticleOptions> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(FlareParticleOptions::new, FlareParticleOptions::scale).cast();

    @Override
    public ParticleType<?> getType() {
        return SpaceParticles.FLARE.get();
    }
}
