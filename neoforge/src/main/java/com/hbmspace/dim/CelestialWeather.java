// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.api.environment.LevelWeather;
import com.hbmspace.dim.trait.CBT_Weather;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public final class CelestialWeather implements LevelWeather.Owner {

    @Override
    public boolean advance(ServerLevel level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null && !CelestialBody.inOrbit(level)) return false;

        CBT_Weather weather =
                provider != null && provider.hasWeatherCycle()
                        ? CBT_Weather.ensureTrait(provider.body())
                        : null;
        if (weather == null) {
            apply(level, 0.0F, 0.0F);
        } else {
            apply(level, weather.rainStrength, weather.thunderStrength);
        }
        if (provider != null) provider.updateWeather(level);
        return true;
    }

    @Override
    public void sleptThrough(ServerLevel level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null) return;

        CBT_Weather weather = CBT_Weather.ensureTrait(provider.body());
        if (weather != null) {
            RandomSource rand = level.getRandom();
            weather.forceClear(rand, rand.nextInt(168000) + 12000);
            SolarSystemWorldSavedData.get(level).setDirty();
        }

        apply(level, 0.0F, 0.0F);
        provider.resetRainAndThunder(level);
    }

    private static void apply(ServerLevel level, float rain, float thunder) {
        float oldRain = level.getRainLevel(1.0F);
        float oldThunder = level.getThunderLevel(1.0F);
        level.setRainLevel(rain);
        level.setThunderLevel(thunder);

        if (oldRain != level.getRainLevel(1.0F)) {
            level.getServer()
                    .getPlayerList()
                    .broadcastAll(
                            new ClientboundGameEventPacket(
                                    ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, rain),
                            level.dimension());
        }
        if (oldThunder != level.getThunderLevel(1.0F)) {
            level.getServer()
                    .getPlayerList()
                    .broadcastAll(
                            new ClientboundGameEventPacket(
                                    ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunder),
                            level.dimension());
        }
    }
}
