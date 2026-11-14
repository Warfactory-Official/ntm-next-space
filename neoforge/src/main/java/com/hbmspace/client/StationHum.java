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

package com.hbmspace.client;

import com.hbmspace.config.SpaceConfig;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.sound.ModSoundsSpace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

public final class StationHum extends AbstractTickableSoundInstance {

    private static @Nullable StationHum playing;

    private final LocalPlayer player;

    private StationHum(LocalPlayer player) {
        super(
                ModSoundsSpace.MISC_STATION_HUM.get(),
                SoundSource.AMBIENT,
                SoundInstance.createUnseededRandom());
        this.player = player;
        looping = true;
        delay = 0;
        relative = true;
        volume = SpaceConfig.shipHumVolume;
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (playing != null && mc.getSoundManager().isActive(playing)) return;
        if (mc.player != null && aboard(mc.player)) {
            playing = new StationHum(mc.player);
            mc.getSoundManager().play(playing);
        }
    }

    public static void disconnected() {
        if (playing != null) {
            playing.stop();
            playing = null;
        }
    }

    private static boolean aboard(LocalPlayer player) {
        return CelestialBody.inOrbit(player.level()) && SpaceLivingProps.hasGravity(player);
    }

    @Override
    public void tick() {
        if (player.isRemoved() || !aboard(player)) stop();
        else volume = SpaceConfig.shipHumVolume;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
