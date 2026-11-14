/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.dim;

import com.hbm.api.entity.PlayerRespawns;
import com.hbm.packet.ClientEventConstants;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.PlayerInformLayout;
import com.hbm.platform.ServerEvents;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.dres.WorldProviderDres;
import com.hbmspace.dim.duna.WorldProviderDuna;
import com.hbmspace.dim.ike.WorldProviderIke;
import com.hbmspace.dim.minmus.WorldProviderMinmus;
import com.hbmspace.dim.moho.WorldProviderMoho;
import com.hbmspace.dim.moon.WorldProviderMoon;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.dim.trait.CBT_War;
import com.hbmspace.dim.trait.CBT_Weather;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.packet.layout.SpaceSolarSystem;
import com.hbmspace.util.AstronomyUtil;
import com.hbmspace.world.gen.Terrace;
import com.hbmspace.world.gen.layer.CelestialLayers;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class CelestialHandler {

    static {
        WorldProviderCelestial.register(new WorldProviderEarth());
        WorldProviderCelestial.register(new WorldProviderMoon());

        WorldProviderCelestial.register(new WorldProviderMinmus());
        CelestialLayers.register(SpaceDimensions.MINMUS.identifier(), WorldProviderMinmus::layers);

        WorldProviderCelestial.register(new WorldProviderDres());
        CelestialLayers.register(SpaceDimensions.DRES.identifier(), WorldProviderDres::layers);

        WorldProviderCelestial.register(new WorldProviderMoho());
        CelestialLayers.register(SpaceDimensions.MOHO.identifier(), WorldProviderMoho::layers);

        WorldProviderCelestial.register(new WorldProviderIke());

        WorldProviderCelestial.register(new WorldProviderDuna());
        CelestialLayers.register(SpaceDimensions.DUNA.identifier(), WorldProviderDuna::layers);
        Terrace.register(SpaceDimensions.DUNA.identifier(), WorldProviderDuna::terrace);

        final Consumer<ServerLevel> callback =
                level -> {
                    if (level.dimension() == Level.OVERWORLD)
                        SolarSystemWorldSavedData.publish(level.getServer());
                };
        NeoForge.EVENT_BUS.addListener(
                (LevelEvent.Load event) -> {
                    if (event.getLevel() instanceof ServerLevel level2) callback.accept(level2);
                });
        final Consumer<MinecraftServer> callback2 =
                server -> {
                    SolarSystemWorldSavedData.onServerStopping();
                    AstronomyUtil.TIME_MULTIPLIER = 1;
                };
        ServerEvents.STOPPING.add(callback2);
        final Consumer<MinecraftServer> callback3 = CelestialHandler::onServerTickStart;
        ServerEvents.TICK_PRE.add(callback3);
        final Consumer<MinecraftServer> callback4 = CelestialHandler::onServerTickEnd;
        ServerEvents.TICK_POST.add(callback4);
        final BiConsumer<Entity, ServerLevel> callback5 = SpaceGravity::onLoad;
        ServerEvents.ENTITY_LOAD.add(callback5);
        final BiConsumer<Entity, ServerLevel> callback6 = CBT_Invasion::onEntityUnload;
        NeoForge.EVENT_BUS.addListener(
                (EntityLeaveLevelEvent event2) -> {
                    if (event2.getLevel() instanceof ServerLevel level3)
                        callback6.accept(event2.getEntity(), level3);
                });
        PlayerRespawns.register(WorldProviderCelestial::respawn);
        WorldTypeTeleport.register();
        CityLights.register();
    }

    private CelestialHandler() {}

    public static void onSimulationTickStart(MinecraftServer server) {
        if (!server.tickRateManager().runsNormally()) {
            for (CelestialBody body : CelestialBody.getAllBodies()) {
                CBT_Invasion invasion = body.getTrait(CBT_Invasion.class);
                if (invasion != null) invasion.updateBar(server, body);
            }
            return;
        }
        CBT_Weather.updateGlobalWeather(server);
        for (CelestialBody body : CelestialBody.getAllBodies()) {
            List<CelestialBodyTrait> traits = new ArrayList<>(body.currentTraits().values());
            for (CelestialBodyTrait trait : traits) {
                if (!(trait instanceof CBT_War)) trait.update(false, body);
            }
        }

        CelestialBody.updateSwarms();

        ServerLevel orbit = server.getLevel(SpaceDimensions.ORBIT);
        if (orbit != null) {
            for (Entity entity : orbit.getAllEntities()) {
                if (entity instanceof ItemEntity item) {
                    Vec3 motion = item.getDeltaMovement();
                    item.setDeltaMovement(motion.x * 0.9D, item.getGravity(), motion.z * 0.9D);
                }
            }
        }
    }

    private static void onServerTickStart(MinecraftServer server) {
        ServerLevel orbit = server.getLevel(SpaceDimensions.ORBIT);
        if (orbit != null) {
            for (ServerPlayer player : List.copyOf(orbit.players())) {
                if (!player.isSpectator() && !(player.getVehicle() instanceof RideableRocketEntity))
                    checkOrbitalGrid(player);
            }
        }
    }

    private static void onServerTickEnd(MinecraftServer server) {
        if (server.tickRateManager().runsNormally() && server.overworld().getGameTime() % 20 == 0) {
            for (CelestialBody body : CelestialBody.getLandableBodies()) {
                var traits = body.getTraits();
                if (CelestialBody.updateChemistry(traits)) body.setTraits(traits);
            }
        }

        boolean warProjectiles = false;
        for (CelestialBody body : CelestialBody.getAllBodies()) {
            CBT_War war = body.getTrait(CBT_War.class);
            if (war != null) {
                warProjectiles |= !war.getProjectiles().isEmpty();
                war.update(false, body);
            }
        }
        if (warProjectiles || server.overworld().getGameTime() % 5 == 1) {
            SolarSystemWorldSavedData solarSystem = SolarSystemWorldSavedData.get(server);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PacketWire.sendTo(
                        SpaceSolarSystem.encode(solarSystem, player.level().dimension()), player);
            }
        }
    }

    private static void checkOrbitalGrid(ServerPlayer player) {
        double rx = Math.abs(player.getX()) % OrbitalStation.STATION_SIZE;
        double rz = Math.abs(player.getZ()) % OrbitalStation.STATION_SIZE;

        int minBuffer = OrbitalStation.BUFFER_SIZE;
        int maxBuffer = OrbitalStation.STATION_SIZE - minBuffer;

        int minWarning = OrbitalStation.BUFFER_SIZE + OrbitalStation.WARNING_SIZE;
        int maxWarning = OrbitalStation.STATION_SIZE - minWarning;

        if (rx < minWarning || rx > maxWarning || rz < minWarning || rz > maxWarning) {
            PacketWire.sendTo(
                    PlayerInformLayout.encode(
                            Component.translatable("info.orbitfall").withStyle(ChatFormatting.RED),
                            ClientEventConstants.PLAYER_INFORM_ID_GAS_HAZARD,
                            3_000),
                    player);
        }

        if (rx < minBuffer || rx > maxBuffer || rz < minBuffer || rz > maxBuffer) {
            OrbitalStation station =
                    OrbitalStation.getStationFromPosition(player.getBlockX(), player.getBlockZ());
            RandomSource rand = player.getRandom();
            int range = SpaceData.MAX_PROBE_DISTANCE.get();
            CelestialTeleporter.teleport(
                    player,
                    station.orbiting.dimension,
                    rand.nextInt(range * 2) - range,
                    800,
                    rand.nextInt(range * 2) - range,
                    false);
        }
    }
}
