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

import com.hbm.api.environment.AtmosphereLayers;
import com.hbm.api.environment.LevelWeather;
import com.hbm.api.explosion.NukeDetonations;
import com.hbm.platform.PlayerLevelChange;
import com.hbm.platform.ServerEvents;
import com.hbmspace.dim.duna.DunaDustStorm;
import com.hbmspace.handler.CelestialNukeShockHandler;
import com.hbmspace.handler.ImpactEditsSpace;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class SkyHandler {

    static {
        AtmosphereLayers.register(new CelestialEnvironment());
        LevelWeather.register(new CelestialWeather());

        NukeDetonations.register(CelestialNukeShockHandler::trigger);
        final Consumer<ServerPlayer> callback = CelestialNukeShockHandler::sync;
        ServerEvents.JOIN.add(callback);
        final Consumer<MinecraftServer> callback2 = CelestialNukeShockHandler::onServerStopping;
        ServerEvents.STOPPING.add(callback2);
        final Consumer<ServerPlayer> callback3 =
                player -> DunaDustStorm.sync(player, player.level());
        ServerEvents.JOIN.add(callback3);
        final Consumer<ServerPlayer> callback4 =
                player -> DunaDustStorm.sync(player, player.level());
        ServerEvents.RESPAWN.add(callback4);
        final PlayerLevelChange callback5 =
                (player, origin, destination) -> DunaDustStorm.sync(player, destination);
        ServerEvents.LEVEL_CHANGE.add(callback5);

        ImpactEditsSpace.install();
    }

    private SkyHandler() {}
}
