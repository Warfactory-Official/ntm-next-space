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

package com.hbmspace.util.datafix;

import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class SpaceDataFixers {

    public static final String VERSION_KEY = "hbmspace_data_version";
    public static final int VERSION = 1;
    private static final Map<String, String> RENAMES =
            Map.of(
                    "hbmspace:multiblock_cell_h5_r60_m10", "hbm:multiblock_cell_h5_r60_m10",
                    "hbmspace:fluorine", "hbm:fluorine");

    private SpaceDataFixers() {}

    public static CompoundTag upgrade(CompoundTag tag) {
        if (tag.getIntOr(VERSION_KEY, 0) >= VERSION) return tag;
        rename(tag);
        tag.putInt(VERSION_KEY, VERSION);
        return tag;
    }

    public static void stamp(CompoundTag tag) {
        tag.putInt(VERSION_KEY, VERSION);
    }

    private static Tag rename(Tag tag) {
        switch (tag) {
            case StringTag(String value) -> {
                String renamed = RENAMES.get(value);
                return renamed == null ? tag : StringTag.valueOf(renamed);
            }
            case CompoundTag compound -> {
                for (String key : List.copyOf(compound.keySet())) {
                    Tag child = compound.get(key);
                    Tag fixed = rename(child);
                    String renamed = RENAMES.get(key);
                    if (renamed != null) {
                        compound.remove(key);
                        compound.put(renamed, fixed);
                    } else if (fixed != child) {
                        compound.put(key, fixed);
                    }
                }
                return compound;
            }
            case ListTag list -> {
                for (int i = 0; i < list.size(); i++) {
                    Tag child = list.get(i);
                    Tag fixed = rename(child);
                    if (fixed != child) list.set(i, fixed);
                }
                return list;
            }
            default -> {
                return tag;
            }
        }
    }
}
