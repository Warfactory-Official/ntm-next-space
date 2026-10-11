// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.api.environment.AtmosphereLayers;
import com.hbm.api.environment.LevelWeather;
import com.hbm.api.explosion.NukeDetonations;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.FabricNetwork;
import com.hbm.platform.FabricServerEvents;
import com.hbmspace.dim.duna.DunaDustStorm;
import com.hbmspace.dim.duna.DunaStormSyncPayload;
import com.hbmspace.handler.CelestialNukeShockHandler;
import com.hbmspace.handler.ImpactEditsSpace;
import com.hbmspace.handler.NukeShockSyncPayload;
import com.hbmspace.handler.SpinlaunchPayload;

public final class SkyHandler {

    static {
        AtmosphereLayers.register(new CelestialEnvironment());
        LevelWeather.register(new CelestialWeather());

        NukeDetonations.register(CelestialNukeShockHandler::trigger);
        FabricServerEvents.onPlayerJoin(CelestialNukeShockHandler::sync);
        FabricServerEvents.onServerStopping(CelestialNukeShockHandler::onServerStopping);
        FabricNetwork.clientbound.add(
                new FabricNetwork.Registration<>(
                        NukeShockSyncPayload.TYPE,
                        NukeShockSyncPayload.STREAM_CODEC,
                        NukeShockSyncPayload::handle,
                        PayloadHandlerThread.MAIN));

        FabricServerEvents.onPlayerJoin(player -> DunaDustStorm.sync(player, player.level()));
        FabricServerEvents.onPlayerRespawn(player -> DunaDustStorm.sync(player, player.level()));
        FabricServerEvents.onPlayerChangeLevel(
                (player, origin, destination) -> DunaDustStorm.sync(player, destination));
        FabricNetwork.clientbound.add(
                new FabricNetwork.Registration<>(
                        DunaStormSyncPayload.TYPE,
                        DunaStormSyncPayload.STREAM_CODEC,
                        DunaStormSyncPayload::handle,
                        PayloadHandlerThread.MAIN));

        FabricNetwork.clientbound.add(
                new FabricNetwork.Registration<>(
                        SpinlaunchPayload.TYPE,
                        SpinlaunchPayload.STREAM_CODEC,
                        SpinlaunchPayload::handle,
                        PayloadHandlerThread.MAIN));

        ImpactEditsSpace.install();
    }

    private SkyHandler() {}
}
