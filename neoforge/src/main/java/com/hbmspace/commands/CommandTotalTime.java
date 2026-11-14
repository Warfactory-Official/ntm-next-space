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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.ServerLevelData;

public final class CommandTotalTime {

    private CommandTotalTime() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("totaltime")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument(
                                                                "time",
                                                                IntegerArgumentType.integer(0))
                                                        .executes(CommandTotalTime::set)))
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument(
                                                                "time",
                                                                IntegerArgumentType.integer(0))
                                                        .executes(CommandTotalTime::add))));
    }

    private static int set(CommandContext<CommandSourceStack> ctx) {
        int time = IntegerArgumentType.getInteger(ctx, "time");
        setGameTime(ctx.getSource().getServer(), time);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.time.set", time), true);
        return 1;
    }

    private static int add(CommandContext<CommandSourceStack> ctx) {
        int time = IntegerArgumentType.getInteger(ctx, "time");
        MinecraftServer server = ctx.getSource().getServer();
        setGameTime(server, server.overworld().getGameTime() + time);
        ctx.getSource()
                .sendSuccess(() -> Component.translatable("commands.time.added", time), true);
        return 1;
    }

    private static void setGameTime(MinecraftServer server, long time) {
        ServerLevelData data = server.getWorldData().overworldData();
        data.setGameTime(time);
        server.forceGameTimeSynchronization();
    }
}
