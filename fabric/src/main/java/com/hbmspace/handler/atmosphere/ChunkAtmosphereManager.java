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

package com.hbmspace.handler.atmosphere;

import com.hbm.NuclearTech;
import com.hbm.api.block.BlockPlacements;
import com.hbm.api.environment.Environment;
import com.hbm.platform.ServerEvents;
import com.hbmspace.HbmSpace;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public final class ChunkAtmosphereManager {

    public static final ChunkAtmosphereHandler proxy = new ChunkAtmosphereHandler();

    static final Logger LOGGER = NuclearTech.LOGGER;

    private ChunkAtmosphereManager() {}

    public static void init() {
        SpaceLivingProps.register();
        Environment.PROVIDER.install(HbmSpace.MOD_ID, new SpaceEnvironment());
        SpaceEnvironmentHooks.install();
        final Consumer<MinecraftServer> callback = proxy::serverTick;
        ServerEvents.TICK_POST.add(callback);
        final Consumer<MinecraftServer> callback2 = server -> proxy.serverStopping();
        ServerEvents.STOPPING.add(callback2);
        BlockPlacements.onPlaced(proxy::placed);
    }
}
