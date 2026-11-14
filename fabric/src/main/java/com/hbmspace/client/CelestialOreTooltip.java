/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.client;

import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.config.SpaceConfig;
import com.hbmspace.dim.SolarSystem.Body;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class CelestialOreTooltip {

    private static @Nullable Map<Item, CelestialOre> ores;

    private CelestialOreTooltip() {}

    public static void append(ItemStack stack, List<Component> lines) {
        if (!SpaceConfig.showOreLocations) return;
        if (stack.is(Items.COAL_ORE)) {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.foundOn")
                            .withStyle(ChatFormatting.GOLD));
            lines.add(body(Body.KERBIN, ChatFormatting.AQUA));
            return;
        }
        CelestialOre ore = ores().get(stack.getItem());
        if (ore == null) return;
        Set<Body> on = ore.spawnsOn();
        int bodies = Body.values().length;
        if (on.isEmpty()) return;
        if (on.size() == bodies) {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.anywhere")
                            .withStyle(ChatFormatting.GOLD));
        } else if (on.size() == bodies - 1) {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.anywhereExcept")
                            .withStyle(ChatFormatting.GOLD));
            for (Body body : Body.values()) {
                if (!on.contains(body)) lines.add(body(body, ChatFormatting.RED));
            }
        } else {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.foundOn")
                            .withStyle(ChatFormatting.GOLD));
            for (Body body : on) lines.add(body(body, ChatFormatting.AQUA));
        }
    }

    private static Component body(Body body, ChatFormatting colour) {
        return Component.translatable(
                        "desc.hbmspace.ore.body", Component.translatable("body." + body.name))
                .withStyle(colour);
    }

    private static Map<Item, CelestialOre> ores() {
        Map<Item, CelestialOre> map = ores;
        if (map != null) return map;
        map = new HashMap<>();
        for (CelestialOre ore : CelestialOre.values()) {
            map.put(ore.host().asItem(), ore);
            for (Body body : ore.bodies()) map.put(ore.block(body).asItem(), ore);
        }
        map.put(Items.NETHER_QUARTZ_ORE, CelestialOre.QUARTZ);
        ores = map;
        return map;
    }
}
