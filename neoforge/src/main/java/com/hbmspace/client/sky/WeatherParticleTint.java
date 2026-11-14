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

package com.hbmspace.client.sky;

import com.hbm.api.client.ParticleSpawns;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

public final class WeatherParticleTint implements ParticleSpawns.Listener {

    public static void register() {
        ParticleSpawns.register(new WeatherParticleTint());
    }

    @Override
    public void created(ParticleOptions options, double x, double y, double z, Particle particle) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(particle instanceof SingleQuadParticle quad)) return;
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null || !provider.hasWeatherCycle() || level.getRainLevel(1.0F) <= 0.0F)
            return;

        ParticleType<?> type = options.getType();
        boolean rain = type == ParticleTypes.RAIN;
        boolean drop =
                type == ParticleTypes.DRIPPING_WATER
                        || type == ParticleTypes.FALLING_WATER
                        || type == ParticleTypes.DRIPPING_DRIPSTONE_WATER
                        || type == ParticleTypes.FALLING_DRIPSTONE_WATER;
        if (!rain
                && type != ParticleTypes.SPLASH
                && !(drop && !waterSourceAbove(level, BlockPos.containing(x, y, z)))) {
            return;
        }

        int color = provider.getWeatherColor();
        quad.setColor(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color));
        if (rain) quad.setSpriteFromAge(new Fixed(generic(level.getRandom().nextInt(3))));
    }

    private static boolean waterSourceAbove(ClientLevel level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isFaceSturdy(level, above, Direction.UP)
                && level.getFluidState(above.above()).is(FluidTags.WATER);
    }

    private static TextureAtlasSprite generic(int index) {
        return Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.PARTICLES)
                .getSprite(Identifier.withDefaultNamespace("generic_" + index));
    }

    private record Fixed(TextureAtlasSprite sprite) implements SpriteSet {
        @Override
        public TextureAtlasSprite get(int index, int max) {
            return sprite;
        }

        @Override
        public TextureAtlasSprite get(RandomSource random) {
            return sprite;
        }

        @Override
        public TextureAtlasSprite first() {
            return sprite;
        }
    }
}
