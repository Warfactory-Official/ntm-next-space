// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.sound;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class SkySoundsSpace {
    public static final RegistryHandle<SoundEvent> MUSIC_GAME_SPACE =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":music.game.space");
    public static final RegistryHandle<SoundEvent> SPINSHOT =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.spinshot");
    public static final RegistryHandle<SoundEvent> SPINCHARGE =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.spincharge");
    public static final RegistryHandle<SoundEvent> DYSON_BEAM =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":block.dyson_beam");

    private SkySoundsSpace() {}
}
