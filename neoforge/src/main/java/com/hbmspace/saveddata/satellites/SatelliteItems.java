/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-FileCopyrightText: 2025 RealSilverMoon
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * Copyright (C) 2025 RealSilverMoon. All rights reserved.
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

package com.hbmspace.saveddata.satellites;

import com.hbm.client.ClientPlayerAccess;
import com.hbm.client.ModifierKeys;
import com.hbm.inventory.container.HeldItemMenu;
import com.hbm.items.ModItems;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.SatelliteDeployment;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbm.saveddata.satellites.ScienceSatellite;
import com.hbm.util.TooltipStyle;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.handler.RocketPart;
import com.hbmspace.inventory.container.RocketryMenusSpace;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.weapon.MissileEditsSpace;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class SatelliteItems {
    private static final int HAND_LAUNCH_MASS = 16_000;
    private static final Map<SatelliteType, List<String>> DESCRIPTIONS = new IdentityHashMap<>();

    static {
        description(SatelliteType.RELAY, "foeq");
        description(
                SatelliteType.HORIZONS,
                "gerald.single_use",
                "gerald.orbital_module",
                "gerald.melter");
        description(SatelliteType.DEATH_RAY, "laser");
        description(SatelliteType.MAPPER, "mapper");
        description(SatelliteType.MINER, "miner");
        description(SatelliteType.LUNAR_MINER, "lunar_miner");
        description(SatelliteType.RADAR, "radar");
        description(SatelliteType.RESONATOR, "resonator");
        description(SatelliteType.SCANNER, "scanner");
    }

    private SatelliteItems() {}

    public static void description(SatelliteType type, String... keys) {
        DESCRIPTIONS.put(type, List.of(keys));
    }

    static void describe(
            ItemStack stack,
            SatelliteType type,
            Item.TooltipContext context,
            TooltipFlag flag,
            Consumer<Component> adder,
            Runnable ntm) {
        if (!SatellitesSpace.isSpace(type)) {
            ntm.run();
            return;
        }

        MissileEditsSpace.describeSatellite(mass(type), adder);

        SatelliteOrbit orbit =
                stack.getOrDefault(
                        ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                        SatellitesSpace.defaults(type));
        if (ModifierKeys.detailsHeld()) {
            entry(adder, "frequency", String.valueOf(SatelliteFrequencyProvider.frequency(stack)));
            entry(adder, "owner", orbit.owner());
            entry(adder, "speed", speed(orbit.altitude()) + "km/s");
            entry(adder, "altitude", format(orbit.altitude()) + "km");
            entry(adder, "inclination", format(orbit.inclination()) + "\u00B0");
            entry(adder, "phase", phase(orbit.phaseOffset()) + "\u00B0");
            entry(
                    adder,
                    "color",
                    String.format(
                            Locale.ROOT,
                            "#%02X%02X%02X",
                            channel(orbit.red()),
                            channel(orbit.green()),
                            channel(orbit.blue())));
        } else {
            adder.accept(TooltipStyle.holdDetails());
        }

        for (String key : DESCRIPTIONS.getOrDefault(type, List.of())) {
            adder.accept(Component.translatable("item.hbmspace.sat.desc." + key));
        }

        if (byHand(type)) {
            adder.accept(
                    Component.translatable("item.hbmspace.sat.desc.launch_by_hand")
                            .withStyle(ChatFormatting.GOLD));
            Player player = ClientPlayerAccess.player();
            if (player != null && CelestialBody.inOrbit(player.level())) {
                adder.accept(
                        Component.translatable("item.hbmspace.sat.desc.deploy_orbit")
                                .withStyle(ChatFormatting.BOLD));
            }
        }
    }

    static InteractionResult use(
            ItemStack stack,
            @Nullable SatelliteType type,
            Level level,
            Player player,
            InteractionHand hand) {
        boolean module =
                stack.is(ModItems.SATELLITE_SCIENCE_SENSOR.get())
                        || stack.is(ModItems.SATELLITE_SCIENCE_ASSEMBLER.get());
        if (!SatellitesSpace.isSpace(type) && type != SatelliteType.SCIENCE && !module)
            return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (type == SatelliteType.SCIENCE || module) return InteractionResult.PASS;
            HeldItemMenu.open(
                    player, hand, RocketryMenusSpace.SATELLITE_SETTINGS, Component.empty());
            return InteractionResult.SUCCESS;
        }

        if (!(module ? RocketPart.of(stack.getItem()).mass <= HAND_LAUNCH_MASS : byHand(type))
                || !CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (module && level.isClientSide()) return InteractionResult.PASS;

        if (level instanceof ServerLevel server) {
            ServerLevel target =
                    CelestialBody.getTarget(level, player.getBlockX(), player.getBlockZ())
                            .body()
                            .getLevel(server.getServer());
            if (target == null) return InteractionResult.PASS;
            int frequency = SatelliteFrequencyProvider.frequency(stack);
            if (module
                    && !(SatelliteSavedData.at(
                                            target,
                                            player.getBlockX(),
                                            player.getBlockZ(),
                                            frequency)
                                    .getSatFromFreq(frequency)
                            instanceof ScienceSatellite)) {
                return InteractionResult.PASS;
            }
            SatelliteDeployment.launch(
                    target, stack, frequency, player.getX(), player.getY(), player.getZ());
            player.sendSystemMessage(Component.translatable("chat.hbmspace.satellite.launched"));
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    public static boolean byHand(SatelliteType type) {
        RocketPart part = RocketPart.of(type.item());
        return part != null && part.mass <= HAND_LAUNCH_MASS;
    }

    private static int mass(SatelliteType type) {
        RocketPart part = RocketPart.of(type.item());
        return part == null ? 0 : part.mass;
    }

    private static void entry(Consumer<Component> adder, String key, String value) {
        adder.accept(
                Component.translatable("item.hbmspace.sat.desc." + key)
                        .append(": ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(value).withStyle(ChatFormatting.GOLD)));
    }

    public static String format(float value) {
        return value == (int) value ? Integer.toString((int) value) : Float.toString(value);
    }

    public static String speed(float altitude) {
        return format(Math.round(SatelliteOrbit.orbitSpeedKmPerSecond(altitude) * 10.0F) / 10.0F);
    }

    public static String phase(float phaseOffset) {
        return format(Math.round(SatelliteOrbit.normalizePhaseOffset(phaseOffset) * 10.0F) / 10.0F);
    }

    public static int channel(float value) {
        return Mth.clamp(Math.round(value * 255F), 0, 255);
    }
}
