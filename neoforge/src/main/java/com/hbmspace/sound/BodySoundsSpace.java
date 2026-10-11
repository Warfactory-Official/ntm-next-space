// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.sound;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class BodySoundsSpace {
    public static final RegistryHandle<SoundEvent> MISC_RUMBLE =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.rumble");

    private BodySoundsSpace() {}
}
