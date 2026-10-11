// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.world.loot;

import com.hbm.data.WorldData;
import com.hbm.registration.NeoForgeRegistrar;
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
        NeoForgeRegistrar.registerLootNumberProviderType("hbmspace:wand_loot_scaled", MAP_CODEC);
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
