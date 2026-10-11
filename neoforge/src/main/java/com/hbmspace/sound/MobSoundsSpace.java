// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.sound;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class MobSoundsSpace {
    public static final RegistryHandle<SoundEvent> ENTITY_BEEP =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.beep");
    public static final RegistryHandle<SoundEvent> BFA_SHOOT =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.bfashoot");
    public static final RegistryHandle<SoundEvent> BFA_TALK =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.bfatalk");
    public static final RegistryHandle<SoundEvent> STINGER_LOCK_ON =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":weapon.stingerlockon");

    private MobSoundsSpace() {}
}
