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

package com.hbmspace.world.loot;

import com.hbm.data.WorldData;
import com.hbm.registration.Registrar;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record WandLootScaledNumberProvider(NumberProvider value) implements NumberProvider {
    public static final MapCodec<WandLootScaledNumberProvider> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            NumberProviders.CODEC
                                                    .fieldOf("value")
                                                    .forGetter(WandLootScaledNumberProvider::value))
                                    .apply(i, WandLootScaledNumberProvider::new));

    public static void register() {
        Registrar.registerLootNumberProviderType("hbmspace:wand_loot_scaled", MAP_CODEC);
    }

    public static int scaledRolls(int drawn, double factor) {
        return (int) Math.floor(drawn * factor);
    }

    @Override
    public MapCodec<WandLootScaledNumberProvider> codec() {
        return MAP_CODEC;
    }

    @Override
    public int getInt(LootContext context) {
        return scaledRolls(value.getInt(context), WorldData.LOOT_AMOUNT_FACTOR.get());
    }

    @Override
    public float getFloat(LootContext context) {
        return getInt(context);
    }
}
