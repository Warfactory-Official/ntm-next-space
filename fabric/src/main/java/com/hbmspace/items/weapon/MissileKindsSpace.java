// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.weapon;

import com.hbm.client.ClientEffects;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.items.weapon.ItemCustomMissilePart.WarheadType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class MissileKindsSpace {
    public static final PartSize SIZE_25 =
            PartSize.register(
                    "SIZE_25", () -> Component.translatable("item.hbm.missile.part.size.none"));
    public static final PartSize SIZE_30 =
            PartSize.register(
                    "SIZE_30", () -> Component.translatable("item.hbm.missile.part.size.none"));

    public static final WarheadType APOLLO = new WarheadType("APOLLO", () -> flicker("apollo"));
    public static final WarheadType SATELLITE =
            new WarheadType("SATELLITE", () -> flicker("satellite"));

    public static final FuelType ANY = fuel("ANY", "any", ChatFormatting.GRAY, null);
    public static final FuelType HYDRAZINE =
            fuel("HYDRAZINE", "hydrazine", ChatFormatting.AQUA, ClientEffects.Contrail.KEROSENE);
    public static final FuelType METHALOX =
            fuel("METHALOX", "methalox", ChatFormatting.YELLOW, ClientEffects.Contrail.KEROSENE);
    public static final FuelType KEROLOX =
            fuel("KEROLOX", "kerolox", ChatFormatting.LIGHT_PURPLE, null);

    private MissileKindsSpace() {}

    private static Component flicker(String name) {
        return Component.translatable("item.hbmspace.warhead.desc." + name)
                .withStyle(
                        System.currentTimeMillis() % 1000 < 500
                                ? ChatFormatting.GOLD
                                : ChatFormatting.RED);
    }

    private static FuelType fuel(
            String name,
            String key,
            ChatFormatting color,
            ClientEffects.@Nullable Contrail contrail) {
        return new FuelType(name, "item.hbmspace.custom_missile_part.fuel." + key, color, contrail);
    }
}
