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

package com.hbmspace.dim;

import com.hbm.platform.ServerEvents;
import com.hbmspace.HbmSpace;
import com.hbmspace.extprop.SpacePlayerProps;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class WorldTypeTeleport {

    public static final ResourceKey<NoiseGeneratorSettings> NOISE =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS,
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "martian"));

    private static volatile boolean martian;

    private WorldTypeTeleport() {}

    static void register() {
        final Consumer<ServerLevel> callback =
                level -> {
                    if (level.dimension() == Level.OVERWORLD)
                        martian = marks(level.getChunkSource().getGenerator());
                };
        NeoForge.EVENT_BUS.addListener(
                (LevelEvent.Load event) -> {
                    if (event.getLevel() instanceof ServerLevel level2) callback.accept(level2);
                });
        final Consumer<ServerPlayer> callback2 = WorldTypeTeleport::onPlayerJoin;
        ServerEvents.JOIN.add(callback2);
    }

    public static boolean marks(ChunkGenerator generator) {
        return generator instanceof NoiseBasedChunkGenerator noise
                && noise.generatorSettings().is(NOISE);
    }

    public static boolean isMartian() {
        return martian;
    }

    private static void onPlayerJoin(ServerPlayer player) {
        if (!martian || SpacePlayerProps.hasWarped(player)) return;
        CelestialTeleporter.teleport(player, SpaceDimensions.DUNA, 0, 0, 0, true);
        SpacePlayerProps.setWarped(player, true);
    }
}
