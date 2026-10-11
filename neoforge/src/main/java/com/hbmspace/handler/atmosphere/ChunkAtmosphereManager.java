// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbm.NuclearTech;
import com.hbm.api.block.BlockPlacements;
import com.hbm.api.environment.Environment;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.NeoForgeNetwork;
import com.hbm.platform.NeoForgeServerEvents;
import com.hbmspace.HbmSpace;
import org.slf4j.Logger;

public final class ChunkAtmosphereManager {

    public static final ChunkAtmosphereHandler proxy = new ChunkAtmosphereHandler();

    static final Logger LOGGER = NuclearTech.LOGGER;

    private ChunkAtmosphereManager() {}

    public static void init() {
        SpaceLivingProps.register();
        Environment.PROVIDER.install(HbmSpace.MOD_ID, new SpaceEnvironment());
        SpaceEnvironmentHooks.install();
        NeoForgeServerEvents.onServerTickPost(proxy::serverTick);
        NeoForgeServerEvents.onServerStopping(server -> proxy.serverStopping());
        BlockPlacements.onPlaced(proxy::placed);
        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        SpaceLivingSyncPayload.TYPE,
                        SpaceLivingSyncPayload.STREAM_CODEC,
                        SpaceLivingSyncPayload::handle,
                        PayloadHandlerThread.MAIN));
    }
}
