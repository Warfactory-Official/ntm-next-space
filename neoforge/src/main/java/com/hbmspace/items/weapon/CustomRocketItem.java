/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * Copyright (C) 2026 detree05. All rights reserved.
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

package com.hbmspace.items.weapon;

import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.saveddata.satellites.SatellitesSpace;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class CustomRocketItem extends Item implements SatelliteFrequencyProvider {

    public CustomRocketItem(Properties properties) {
        super(properties);
    }

    public static ItemStack build(RocketStruct rocket) {
        ItemStack stack = new ItemStack(MissilePartsSpace.ROCKET_CUSTOM.get());
        stack.set(ModDataComponentsSpace.ROCKET.get(), rocket.copy());
        if (rocket.satFreq != 0) SatelliteFrequencyProvider.setFrequency(stack, rocket.satFreq);
        SatelliteType payload =
                rocket.capsule == null ? null : SatelliteType.fromItem(rocket.capsule.item);
        if (rocket.satOrbit != null) {
            stack.set(ModDataComponentsSpace.SATELLITE_ORBIT.get(), rocket.satOrbit);
        } else if (payload != null && SatellitesSpace.isSpace(payload)) {
            stack.set(
                    ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                    SatellitesSpace.defaults(payload));
        }
        return stack;
    }

    public static ItemStack build(RocketStruct rocket, boolean hasFuel) {
        ItemStack stack = build(rocket);
        setFuel(stack, hasFuel);
        return stack;
    }

    public static @Nullable RocketStruct get(ItemStack stack) {
        if (!(stack.getItem() instanceof CustomRocketItem)) return null;
        RocketStruct rocket = stack.get(ModDataComponentsSpace.ROCKET.get());
        return rocket == null ? null : rocket.copy();
    }

    public static boolean hasFuel(ItemStack stack) {
        return stack.has(ModDataComponentsSpace.ROCKET_FUELED.get());
    }

    public static void setFuel(ItemStack stack, boolean hasFuel) {
        if (hasFuel) {
            stack.set(ModDataComponentsSpace.ROCKET_FUELED.get(), Unit.INSTANCE);
        } else {
            stack.remove(ModDataComponentsSpace.ROCKET_FUELED.get());
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        RocketStruct rocket = get(stack);
        if (rocket == null) return;

        if (rocket.capsule != null) {
            adder.accept(
                    line(
                            "payload",
                            rocket.capsule.item.getName(rocket.capsule.item.getDefaultInstance())));
        }
        adder.accept(line("stages", Component.literal(String.valueOf(rocket.stages.size()))));

        if (hasFuel(stack)) {
            adder.accept(
                    Component.translatable("desc.item.customRocket.fueled")
                            .withStyle(ChatFormatting.GRAY));
        }

        int freq = SatelliteFrequencyProvider.frequency(stack);
        if (freq != 0) adder.accept(line("frequency", Component.literal(String.valueOf(freq))));
    }

    private static Component line(String key, Component value) {
        return Component.empty()
                .append(
                        Component.translatable("desc.item.customRocket." + key)
                                .append(": ")
                                .withStyle(ChatFormatting.BOLD))
                .append(Component.empty().withStyle(ChatFormatting.GRAY).append(value));
    }
}
