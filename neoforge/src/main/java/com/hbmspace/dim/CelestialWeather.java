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
