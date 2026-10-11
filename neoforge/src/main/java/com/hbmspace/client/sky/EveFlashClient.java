// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.eve.WorldProviderEve;
import com.hbmspace.sound.BodySoundsSpace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jspecify.annotations.Nullable;

public final class EveFlashClient {

    private static @Nullable ClientLevel level;
    private static float lastFlash;

    private EveFlashClient() {}

    static void tick(Minecraft mc) {
        ClientLevel current = mc.level;
        if (current != level) {
            level = current;
            lastFlash = 0;
        }
        if (current == null
                || mc.player == null
                || !(WorldProviderCelestial.of(current) instanceof WorldProviderEve eve)) return;

        float flash = WorldProviderEve.flash(eve.body(), current.getGameTime());
        if (lastFlash <= 0.01F && flash > 0.01F)
            mc.player.playSound(BodySoundsSpace.MISC_RUMBLE.get(), 10.0F, 1.0F);
        lastFlash = flash;
    }
}
