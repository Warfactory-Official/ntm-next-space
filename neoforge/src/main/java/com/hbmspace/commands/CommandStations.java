/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

package com.hbmspace.commands;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.CelestialTeleporter;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.VOTVdriveItem.Destination;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.MissilePartsSpace;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

public final class CommandStations {

    private CommandStations() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ntmstations")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(
                                Commands.literal("launch")
                                        .executes(c -> asPlayer(c, CommandStations::launch)))
                        .then(
                                Commands.literal("tp")
                                        .executes(c -> asPlayer(c, CommandStations::teleport)))
                        .then(
                                Commands.literal("list")
                                        .executes(c -> asPlayer(c, CommandStations::list)))
                        .then(
                                Commands.literal("fetch")
                                        .executes(
                                                c ->
                                                        asPlayer(
                                                                c,
                                                                (source, player) ->
                                                                        message(
                                                                                source,
                                                                                "commands.station.invalid_station",
                                                                                true)))
                                        .then(
                                                Commands.argument(
                                                                "station",
                                                                StringArgumentType.greedyString())
                                                        .executes(
                                                                c ->
                                                                        asPlayer(
                                                                                c,
                                                                                (source, player) ->
                                                                                        fetch(
                                                                                                source,
                                                                                                player,
                                                                                                StringArgumentType
                                                                                                        .getString(
                                                                                                                c,
                                                                                                                "station")))))));
    }

    private static int asPlayer(
            CommandContext<CommandSourceStack> context,
            BiConsumer<CommandSourceStack, ServerPlayer> body) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            message(source, "commands.satellite.should_be_run_as_player", true);
            return 0;
        }
        body.accept(source, player);
        return 1;
    }

    private static void message(CommandSourceStack source, String key, boolean isError) {
        source.sendSystemMessage(
                Component.translatable(key)
                        .withStyle(isError ? ChatFormatting.RED : ChatFormatting.GREEN));
    }

    private static void literal(CommandSourceStack source, String text) {
        source.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.GREEN));
    }

    private static @Nullable Destination heldDestination(
            CommandSourceStack source, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof VOTVdriveItem)) {
            message(source, "commands.station.invalid_drive", true);
            return null;
        }
        return VOTVdriveItem.getDestination(stack);
    }

    private static String stationId(OrbitalStation station) {
        return "0x"
                + Integer.toHexString(ChunkPos.hash(station.dX, station.dZ))
                        .toUpperCase(Locale.ROOT);
    }

    private static void launch(CommandSourceStack source, ServerPlayer player) {
        Destination destination = heldDestination(source, player);
        if (destination == null) return;

        OrbitalStation.addStation(
                destination.x(), destination.z(), CelestialBody.getBody(player.level()));

        message(source, "commands.station.launched", false);
    }

    private static void teleport(CommandSourceStack source, ServerPlayer player) {
        Destination destination = heldDestination(source, player);
        if (destination == null) return;

        ResourceKey<Level> dimension = destination.body().getDimension();
        boolean orbit = dimension == SpaceDimensions.ORBIT;

        int x = destination.x();
        int z = destination.z();

        if (orbit) {
            x = x * OrbitalStation.STATION_SIZE + OrbitalStation.STATION_SIZE / 2;
            z = z * OrbitalStation.STATION_SIZE + OrbitalStation.STATION_SIZE / 2;
        }

        player.stopRiding();

        if (player.level().dimension() != dimension) {
            CelestialTeleporter.teleport(
                    player, dimension, x + 0.5D, orbit ? 130.0D : 300.0D, z + 0.5D, !orbit);
        } else if (orbit) {
            player.teleportTo(x + 0.5D, 130.0D, z + 0.5D);
        } else {
            int y = player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            player.teleportTo(x + 0.5D, y + 1, z + 0.5D);
        }

        if (orbit) {
            ServerLevel target = source.getServer().getLevel(SpaceDimensions.ORBIT);
            OrbitalStation.spawn(target, x, z);
        }

        message(source, "commands.station.teleported", false);
    }

    private static void list(CommandSourceStack source, ServerPlayer player) {
        boolean hasAnyStations = false;
        for (OrbitalStation station :
                SolarSystemWorldSavedData.get(player.level()).getStations().values()) {
            if (!station.hasStation) continue;

            String messageText = stationId(station);
            if (!station.name.isEmpty()) messageText += " - " + station.name;

            literal(source, messageText);

            hasAnyStations = true;
        }

        if (!hasAnyStations) message(source, "commands.station.no_stations", true);
    }

    private static void fetch(CommandSourceStack source, ServerPlayer player, String query) {
        String toMatch = query.trim();

        for (OrbitalStation station :
                SolarSystemWorldSavedData.get(player.level()).getStations().values()) {
            if (station.name.trim().equalsIgnoreCase(toMatch)
                    || stationId(station).equalsIgnoreCase(toMatch)) {
                ItemStack drive = MissilePartsSpace.HARD_DRIVE_FULL.stack(SolarSystem.Body.ORBIT);
                drive.set(
                        ModDataComponentsSpace.DRIVE.get(),
                        new VOTVdriveItem.Data(
                                station.dX,
                                station.dZ,
                                Optional.empty(),
                                true,
                                false,
                                station.name,
                                "",
                                false));

                player.getInventory().placeItemBackInInventory(drive, Prediction.SERVER_ONLY);

                message(source, "commands.station.drive_created", false);
                return;
            }
        }

        message(source, "commands.station.no_match", true);
    }
}
