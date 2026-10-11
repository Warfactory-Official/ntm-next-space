// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.particle;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class SpaceParticles {
    public static final RegistryHandle<SimpleParticleType> DUST_STORM =
            NeoForgeRegistrar.registerParticleType(
                    HbmSpace.MOD_ID + ":dust_storm", () -> new SimpleParticleType(true) {});
    public static final RegistryHandle<ParticleType<FlareParticleOptions>> FLARE =
            NeoForgeRegistrar.registerParticleType(
                    HbmSpace.MOD_ID + ":flare",
                    () ->
                            new ParticleType<FlareParticleOptions>(true) {
                                @Override
                                public MapCodec<FlareParticleOptions> codec() {
                                    return FlareParticleOptions.CODEC;
                                }

                                @Override
                                public StreamCodec<
                                                ? super RegistryFriendlyByteBuf,
                                                FlareParticleOptions>
                                        streamCodec() {
                                    return FlareParticleOptions.STREAM_CODEC;
                                }
                            });

    public static final RegistryHandle<SimpleParticleType> DUOEWE =
            NeoForgeRegistrar.registerParticleType(
                    HbmSpace.MOD_ID + ":duoewe", () -> new SimpleParticleType(true) {});
    public static final RegistryHandle<SimpleParticleType> DUODEC =
            NeoForgeRegistrar.registerParticleType(
                    HbmSpace.MOD_ID + ":duodec", () -> new SimpleParticleType(true) {});

    private SpaceParticles() {}
}
