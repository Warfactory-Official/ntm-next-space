// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.commands;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Weather;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class CommandWeatherPlanetary {

    private static final SimpleCommandExceptionType NO_CYCLE =
            new SimpleCommandExceptionType(
                    Component.translatable("commands.hbmspace.weather.no_cycle"));

    private enum Kind {
        CLEAR("clear"),
        RAIN("rain"),
        THUNDER("thunder");

        final String literal;

        Kind(String literal) {
            this.literal = literal;
        }
    }

    private CommandWeatherPlanetary() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandNode<CommandSourceStack> weather = dispatcher.getRoot().getChild("weather");
        for (Kind kind : Kind.values()) {
            CommandNode<CommandSourceStack> literal = weather.getChild(kind.literal);
            Command<CommandSourceStack> vanilla = literal.getCommand();
            Command<CommandSourceStack> vanillaTimed = literal.getChild("duration").getCommand();

            dispatcher.register(
                    Commands.literal("weather")
                            .then(
                                    Commands.literal(kind.literal)
                                            .executes(c -> run(c, kind, -1, vanilla))
                                            .then(
                                                    Commands.argument(
                                                                    "duration",
                                                                    TimeArgument.time(1))
                                                            .executes(
                                                                    c ->
                                                                            run(
                                                                                    c,
                                                                                    kind,
                                                                                    IntegerArgumentType
                                                                                            .getInteger(
                                                                                                    c,
                                                                                                    "duration"),
                                                                                    vanillaTimed)))));
        }
    }

    private static int run(
            CommandContext<CommandSourceStack> context,
            Kind kind,
            int duration,
            Command<CommandSourceStack> vanilla)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        if (WorldProviderCelestial.of(level) == null && !CelestialBody.inOrbit(level))
            return vanilla.run(context);

        Vec3 pos = source.getPosition();
        CelestialBody body =
                CelestialBody.getTarget(level, Mth.floor(pos.x), Mth.floor(pos.z)).body();
        CBT_Weather weather = CBT_Weather.ensureTrait(body);
        if (weather == null || !CBT_Weather.supportsWeather(body)) throw NO_CYCLE.create();

        RandomSource rand = level.getRandom();
        int ticks = duration == -1 ? (300 + rand.nextInt(600)) * 20 : duration;
        switch (kind) {
            case CLEAR -> weather.forceClear(rand, ticks);
            case RAIN -> weather.forceRain(rand, ticks);
            case THUNDER -> weather.forceThunder(ticks);
        }

        source.sendSuccess(
                () -> Component.translatable("commands.weather.set." + kind.literal), true);
        SolarSystemWorldSavedData.get(level).setDirty();
        return ticks;
    }
}
