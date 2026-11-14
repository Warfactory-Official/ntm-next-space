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

public final class SkySoundsSpace {
    public static final RegistryHandle<SoundEvent> MUSIC_GAME_SPACE =
            Registrar.registerSound(HbmSpace.MOD_ID + ":music.game.space");
    public static final RegistryHandle<SoundEvent> SPINSHOT =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.spinshot");
    public static final RegistryHandle<SoundEvent> SPINCHARGE =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.spincharge");
    public static final RegistryHandle<SoundEvent> DYSON_BEAM =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.dyson_beam");

    private SkySoundsSpace() {}
}
