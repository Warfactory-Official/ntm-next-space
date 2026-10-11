// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

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
