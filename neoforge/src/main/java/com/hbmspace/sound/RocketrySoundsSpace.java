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

package com.hbmspace.sound;

import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class RocketrySoundsSpace {

    public static final RegistryHandle<SoundEvent> ROCKET_IGNITION =
            Registrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_ignition");
    public static final RegistryHandle<SoundEvent> ROCKET_FLY_HEAVY =
            Registrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_fly_heavy");
    public static final RegistryHandle<SoundEvent> ROCKET_FLY_LIGHT =
            Registrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_fly_light");
    public static final RegistryHandle<SoundEvent> ROCKET_STAGE =
            Registrar.registerSound(HbmSpace.MOD_ID + ":entity.rocket_stage");
    public static final RegistryHandle<SoundEvent> HATCH_OPEN =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.hatch_open");
    public static final RegistryHandle<SoundEvent> HATCH_IMPACT =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.hatch_impact");

    public static final RegistryHandle<SoundEvent> ITEM_UNPACK_REMOVE =
            Registrar.registerSound(HbmSpace.MOD_ID + ":item.unpack_remove");
    public static final RegistryHandle<SoundEvent> ITEM_UNPACK_INSERT =
            Registrar.registerSound(HbmSpace.MOD_ID + ":item.unpack_insert");

    private RocketrySoundsSpace() {}
}
