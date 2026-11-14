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

package com.hbmspace.commands;

import com.hbm.items.ModItems;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.saveddata.satellites.SatelliteDeployment;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class CommandSatelliteOrbit {

    private CommandSatelliteOrbit() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ntmsatellites")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(
                                Commands.literal("orbit")
                                        .then(
                                                Commands.argument(
                                                                "frequency",
                                                                IntegerArgumentType.integer())
                                                        .executes(CommandSatelliteOrbit::orbit))));
    }

    private static int orbit(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(
                    Component.translatable("commands.satellite.should_be_run_as_player")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        ItemStack held = player.getMainHandItem();
        int frequency = IntegerArgumentType.getInteger(context, "frequency");
        if (!(held.getItem() instanceof SatelliteFrequencyProvider)
                || held.is(ModItems.SAT_CHIP.get())
                || !SatelliteDeployment.launch(
                        source.getLevel(),
                        held,
                        frequency,
                        player.getX(),
                        player.getY(),
                        player.getZ())) {
            source.sendFailure(
                    Component.translatable("commands.satellite.not_a_satellite")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        held.shrink(1);
        source.sendSuccess(
                () ->
                        Component.translatable("commands.satellite.satellite_orbited")
                                .withStyle(ChatFormatting.GREEN),
                false);
        return 1;
    }
}
