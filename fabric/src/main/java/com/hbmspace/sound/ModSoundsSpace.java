// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.sound;

import com.hbm.items.machine.ItemCassette;
import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.sound.ModSounds;
import com.hbmspace.HbmSpace;
import net.minecraft.sounds.SoundEvent;

public final class ModSoundsSpace {
    public static final RegistryHandle<SoundEvent> BLOCK_TETO =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":block.teto");
    public static final RegistryHandle<SoundEvent> BLOCK_MIKU =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":block.miku");
    public static final RegistryHandle<SoundEvent> BLOCK_AKITA =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":block.akita");
    public static final RegistryHandle<SoundEvent> BLOCK_SQUEAKY_PAIN =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":block.squeaky_pain");

    public static final RegistryHandle<SoundEvent> MISC_LPW_START =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.lpwstart");
    public static final RegistryHandle<SoundEvent> MISC_LPW_STOP =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.lpwstop");
    public static final RegistryHandle<SoundEvent> MISC_LPW_LOOP =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.lpwloop");
    public static final RegistryHandle<SoundEvent> MISC_HTR_START =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.htrstart");
    public static final RegistryHandle<SoundEvent> MISC_HTR_STOP =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.htrstop");
    public static final RegistryHandle<SoundEvent> MISC_HTRF_START =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.htrfstart");
    public static final RegistryHandle<SoundEvent> MISC_HTR_LOOP =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.htrloop");
    public static final RegistryHandle<SoundEvent> MISC_STATION_HUM =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":misc.stationhum");

    public static final RegistryHandle<SoundEvent> PLAYER_PLSS_BREATHING =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":player.plss_breathing");
    public static final RegistryHandle<SoundEvent> PLAYER_OIL =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":player.oil");
    public static final RegistryHandle<SoundEvent> PLAYER_DASH =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":player.dash");
    public static final RegistryHandle<SoundEvent> PLAYER_DASH_RECHARGE =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":player.dash_recharge");
    public static final RegistryHandle<SoundEvent> ALARM_PING =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":alarm.ping");
    public static final RegistryHandle<SoundEvent> ALARM_DUCC =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":alarm.ducc");
    public static final RegistryHandle<SoundEvent> ALARM_MAMA =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":alarm.mama");
    public static final RegistryHandle<SoundEvent> ENTITY_JETLOOPBOMB =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":entity.jetloopbomb");

    static {
        ModSounds.SIREN_TRACK[ItemCassette.TrackType.DISEMBODIED.ordinal()] = ALARM_DUCC;
        ModSounds.SIREN_TRACK[ItemCassette.TrackType.SUICIDE.ordinal()] = ALARM_MAMA;
    }

    private ModSoundsSpace() {}
}
