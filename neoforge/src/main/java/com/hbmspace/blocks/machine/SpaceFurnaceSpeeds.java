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

package com.hbmspace.blocks.machine;

import com.hbm.blockentity.machine.thermal.BrickFurnaceBlockEntity;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SpaceFurnaceSpeeds {
    private SpaceFurnaceSpeeds() {}

    public static void install() {
        BrickFurnaceBlockEntity.EXTRA_SPEED.install(HbmSpace.MOD_ID, SpaceFurnaceSpeeds::speed);
    }

    private static int speed(ItemStack stack) {
        return Entries.SPEEDS.getInt(stack.getItem());
    }

    private static final class Entries {
        private static final Reference2IntOpenHashMap<Item> SPEEDS = create();

        private static Reference2IntOpenHashMap<Item> create() {
            var speeds = new Reference2IntOpenHashMap<Item>(11);
            speeds.defaultReturnValue(1);
            speeds.put(ModBlocksSpace.DUNA_COBBLE.asItem(), 2);
            speeds.put(ModBlocksSpace.DRES_ROCK.asItem(), 2);
            speeds.put(TerrainBlocksSpace.IKE_REGOLITH.asItem(), 2);
            speeds.put(TerrainBlocksSpace.EVE_ROCK.asItem(), 2);
            speeds.put(ModBlocksSpace.MOHO_REGOLITH.asItem(), 2);
            speeds.put(ModBlocksSpace.MOON_ROCK.asItem(), 2);
            speeds.put(ModBlocksSpace.MINMUS_REGOLITH.asItem(), 2);
            speeds.put(ModBlocksSpace.DUNA_SANDS.asItem(), 2);
            speeds.put(TerrainBlocksSpace.LAYTHE_SILT.asItem(), 2);
            speeds.put(TerrainBlocksSpace.EVE_SILT.asItem(), 2);
            speeds.put(BuiltInRegistries.ITEM.getValue(RegistryUtil.id("moon_turf")), 2);
            return speeds;
        }
    }
}
