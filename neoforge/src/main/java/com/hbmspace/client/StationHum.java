// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.sound.AudioSystem;
import com.hbm.sound.AudioWrapper;
import com.hbmspace.config.SpaceConfig;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.sound.ModSoundsSpace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

public final class StationHum {

    private static @Nullable AudioWrapper shipHum;

    private StationHum() {}

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player != null
                && mc.level != null
                && CelestialBody.inOrbit(mc.level)
                && SpaceLivingProps.hasGravity(player)) {
            if (shipHum == null || !shipHum.isPlaying()) {
                shipHum =
                        AudioSystem.getLoopedSound(
                                ModSoundsSpace.MISC_STATION_HUM.get(),
                                SoundSource.AMBIENT,
                                (float) player.getX(),
                                (float) player.getY(),
                                (float) player.getZ(),
                                SpaceConfig.shipHumVolume,
                                5.0F,
                                1.0F,
                                10);
                if (shipHum == null) return;
                shipHum.attachTo(player);
                shipHum.startSound();
            }

            shipHum.updateVolume(SpaceConfig.shipHumVolume);
            shipHum.keepAlive();
        } else if (shipHum != null) {
            shipHum.stopSound();
            shipHum = null;
        }
    }
}
