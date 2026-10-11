// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.api.environment.AtmosphereLayers;
import com.hbm.api.environment.LevelWeather;
import com.hbm.api.explosion.NukeDetonations;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.NeoForgeNetwork;
import com.hbm.platform.NeoForgeServerEvents;
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
        NeoForgeServerEvents.onPlayerJoin(CelestialNukeShockHandler::sync);
        NeoForgeServerEvents.onServerStopping(CelestialNukeShockHandler::onServerStopping);
        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        NukeShockSyncPayload.TYPE,
                        NukeShockSyncPayload.STREAM_CODEC,
                        NukeShockSyncPayload::handle,
                        PayloadHandlerThread.MAIN));

        NeoForgeServerEvents.onPlayerJoin(player -> DunaDustStorm.sync(player, player.level()));
        NeoForgeServerEvents.onPlayerRespawn(player -> DunaDustStorm.sync(player, player.level()));
        NeoForgeServerEvents.onPlayerChangeLevel(
                (player, origin, destination) -> DunaDustStorm.sync(player, destination));
        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        DunaStormSyncPayload.TYPE,
                        DunaStormSyncPayload.STREAM_CODEC,
                        DunaStormSyncPayload::handle,
                        PayloadHandlerThread.MAIN));

        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        SpinlaunchPayload.TYPE,
                        SpinlaunchPayload.STREAM_CODEC,
                        SpinlaunchPayload::handle,
                        PayloadHandlerThread.MAIN));

        ImpactEditsSpace.install();
    }

    private SkyHandler() {}
}
