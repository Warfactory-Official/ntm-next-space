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

package com.hbmspace.items;

import com.hbm.registration.Reg;
import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;

public final class RecordsSpace {

    public enum Song {
        GS("music.record_god_speed", 353.152F),
        GP("music.record_goop", 240.789F),
        EL("music.record_ethereal", 197.739F);

        public static final int COMPARATOR_OUTPUT = 15;

        public final RegistryHandle<SoundEvent> sound;
        private final float lengthSeconds;

        Song(String soundName, float lengthSeconds) {
            this.sound = Registrar.registerSound(HbmSpace.MOD_ID + ":" + soundName);
            this.lengthSeconds = lengthSeconds;
        }

        public String path() {
            return name().toLowerCase(Locale.ROOT);
        }

        public float lengthSeconds() {
            return lengthSeconds;
        }

        public ResourceKey<JukeboxSong> key() {
            return ResourceKey.create(
                    Registries.JUKEBOX_SONG,
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path()));
        }
    }

    public static final Reg.Handle<Item> RECORD_GS = record(Song.GS);
    public static final Reg.Handle<Item> RECORD_GP = record(Song.GP);
    public static final Reg.Handle<Item> RECORD_EL = record(Song.EL);

    static {
        for (Reg.Handle<Item> record : List.of(RECORD_GS, RECORD_GP, RECORD_EL)) {
            Supplier<ItemStack> entry = () -> new ItemStack(record.get());
            Registrar.vanillaTabEntries.add(Map.entry(CreativeModeTabs.TOOLS_AND_UTILITIES, entry));
        }
    }

    private RecordsSpace() {}

    private static Reg.Handle<Item> record(Song song) {
        return HbmSpace.REG.item(
                "record_" + song.path(),
                Item::new,
                () ->
                        new Item.Properties()
                                .stacksTo(1)
                                .rarity(Rarity.RARE)
                                .jukeboxPlayable(song.key()));
    }
}
