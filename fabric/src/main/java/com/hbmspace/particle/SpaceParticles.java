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

package com.hbmspace.particle;

import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class SpaceParticles {
    public static final RegistryHandle<SimpleParticleType> DUST_STORM =
            Registrar.registerParticleType(
                    HbmSpace.MOD_ID + ":dust_storm", () -> new SimpleParticleType(true) {});
    public static final RegistryHandle<ParticleType<FlareParticleOptions>> FLARE =
            Registrar.registerParticleType(
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
            Registrar.registerParticleType(
                    HbmSpace.MOD_ID + ":duoewe", () -> new SimpleParticleType(true) {});
    public static final RegistryHandle<SimpleParticleType> DUODEC =
            Registrar.registerParticleType(
                    HbmSpace.MOD_ID + ":duodec", () -> new SimpleParticleType(true) {});

    private SpaceParticles() {}
}
