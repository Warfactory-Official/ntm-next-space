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

    static void clear() {
        level = null;
        lastFlash = 0;
    }
}
