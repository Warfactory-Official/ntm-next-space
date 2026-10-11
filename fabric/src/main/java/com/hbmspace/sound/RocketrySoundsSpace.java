// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.sound;

import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class RocketrySoundsSpace {

    public static final RegistryHandle<SoundEvent> ROCKET_IGNITION =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_ignition");
    public static final RegistryHandle<SoundEvent> ROCKET_FLY_HEAVY =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_fly_heavy");
    public static final RegistryHandle<SoundEvent> ROCKET_FLY_LIGHT =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_fly_light");
    public static final RegistryHandle<SoundEvent> ROCKET_STAGE =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_stage");
    public static final RegistryHandle<SoundEvent> HATCH_OPEN =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":block.hatch_open");
    public static final RegistryHandle<SoundEvent> HATCH_IMPACT =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":block.hatch_impact");

    public static final RegistryHandle<SoundEvent> ITEM_UNPACK_REMOVE =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":item.unpack_remove");
    public static final RegistryHandle<SoundEvent> ITEM_UNPACK_INSERT =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":item.unpack_insert");

    private RocketrySoundsSpace() {}
}
