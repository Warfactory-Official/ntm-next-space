// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.sound;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class RocketrySoundsSpace {

    public static final RegistryHandle<SoundEvent> ROCKET_IGNITION =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_ignition");
    public static final RegistryHandle<SoundEvent> ROCKET_FLY_HEAVY =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_fly_heavy");
    public static final RegistryHandle<SoundEvent> ROCKET_FLY_LIGHT =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_fly_light");
    public static final RegistryHandle<SoundEvent> ROCKET_STAGE =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_stage");
    public static final RegistryHandle<SoundEvent> HATCH_OPEN =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":block.hatch_open");
    public static final RegistryHandle<SoundEvent> HATCH_IMPACT =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":block.hatch_impact");

    public static final RegistryHandle<SoundEvent> ITEM_UNPACK_REMOVE =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":item.unpack_remove");
    public static final RegistryHandle<SoundEvent> ITEM_UNPACK_INSERT =
            NeoForgeRegistrar.registerSound(HbmSpace.MOD_ID + ":item.unpack_insert");

    private RocketrySoundsSpace() {}
}
