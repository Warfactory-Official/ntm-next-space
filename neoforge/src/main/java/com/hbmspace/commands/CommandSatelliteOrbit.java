// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.commands;

import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.saveddata.satellites.Satellite;
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
        if (!(held.getItem() instanceof ISatChip)
                || held.is(ModItems.SAT_CHIP.get())
                || !Satellite.orbit(
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
