// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public final class SpaceCommands {

    private SpaceCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandTotalTime.register(dispatcher);
        CommandWeatherPlanetary.register(dispatcher);
        CommandStations.register(dispatcher);
        CommandSatelliteOrbit.register(dispatcher);
    }
}
