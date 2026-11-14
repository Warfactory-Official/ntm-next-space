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

import com.hbm.items.machine.CassetteItem;
import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.sound.ModSounds;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class ModSoundsSpace {
    public static final RegistryHandle<SoundEvent> BLOCK_TETO =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.teto");
    public static final RegistryHandle<SoundEvent> BLOCK_MIKU =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.miku");
    public static final RegistryHandle<SoundEvent> BLOCK_AKITA =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.akita");
    public static final RegistryHandle<SoundEvent> BLOCK_SQUEAKY_PAIN =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.squeaky_pain");

    public static final RegistryHandle<SoundEvent> MISC_LPW_START =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.lpwstart");
    public static final RegistryHandle<SoundEvent> MISC_LPW_STOP =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.lpwstop");
    public static final RegistryHandle<SoundEvent> MISC_LPW_LOOP =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.lpwloop");
    public static final RegistryHandle<SoundEvent> MISC_HTR_START =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.htrstart");
    public static final RegistryHandle<SoundEvent> MISC_HTR_STOP =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.htrstop");
    public static final RegistryHandle<SoundEvent> MISC_HTRF_START =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.htrfstart");
    public static final RegistryHandle<SoundEvent> MISC_HTR_LOOP =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.htrloop");
    public static final RegistryHandle<SoundEvent> MISC_STATION_HUM =
            Registrar.registerSound(HbmSpace.MOD_ID + ":misc.stationhum");

    public static final RegistryHandle<SoundEvent> PLAYER_PLSS_BREATHING =
            Registrar.registerSound(HbmSpace.MOD_ID + ":player.plss_breathing");
    public static final RegistryHandle<SoundEvent> PLAYER_OIL =
            Registrar.registerSound(HbmSpace.MOD_ID + ":player.oil");
    public static final RegistryHandle<SoundEvent> PLAYER_DASH =
            Registrar.registerSound(HbmSpace.MOD_ID + ":player.dash");
    public static final RegistryHandle<SoundEvent> PLAYER_DASH_RECHARGE =
            Registrar.registerSound(HbmSpace.MOD_ID + ":player.dash_recharge");
    public static final RegistryHandle<SoundEvent> ALARM_PING =
            Registrar.registerSound(HbmSpace.MOD_ID + ":alarm.ping");
    public static final RegistryHandle<SoundEvent> ALARM_DUCC =
            Registrar.registerSound(HbmSpace.MOD_ID + ":alarm.ducc");
    public static final RegistryHandle<SoundEvent> ALARM_MAMA =
            Registrar.registerSound(HbmSpace.MOD_ID + ":alarm.mama");
    public static final RegistryHandle<SoundEvent> ENTITY_JETLOOPBOMB =
            Registrar.registerSound(HbmSpace.MOD_ID + ":entity.jetloopbomb");

    static {
        ModSounds.SIREN_TRACK[CassetteItem.TrackType.DISEMBODIED.ordinal()] = ALARM_DUCC;
        ModSounds.SIREN_TRACK[CassetteItem.TrackType.SUICIDE.ordinal()] = ALARM_MAMA;
    }

    private ModSoundsSpace() {}
}
