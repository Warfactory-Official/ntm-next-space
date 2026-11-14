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

package com.hbmspace.items.weapon;

import com.hbm.client.ClientEffects;
import com.hbm.items.weapon.CustomMissilePartItem.FuelType;
import com.hbm.items.weapon.CustomMissilePartItem.PartSize;
import com.hbm.items.weapon.CustomMissilePartItem.WarheadType;
import com.hbm.util.Blink;
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
                .withStyle(Blink.lit() ? ChatFormatting.GOLD : ChatFormatting.RED);
    }

    private static FuelType fuel(
            String name,
            String key,
            ChatFormatting color,
            ClientEffects.@Nullable Contrail contrail) {
        return new FuelType(name, "item.hbmspace.custom_missile_part.fuel." + key, color, contrail);
    }
}
