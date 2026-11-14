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

package com.hbmspace.saveddata.satellites;

import com.hbm.items.ModItems;
import com.hbm.items.special.SatelliteItem;
import com.hbm.packet.PacketWire;
import com.hbm.platform.ServerEvents;
import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteDeployment;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.BodyReach;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.packet.layout.SpaceSatelliteSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class SatellitesSpace {

    private static final Map<SatelliteType, float[]> COLORS = new IdentityHashMap<>();
    private static boolean changed;

    private SatellitesSpace() {}

    public static Map<ResourceKey<Level>, Map<Integer, ClientSatellites.Entry>> snapshot(
            MinecraftServer server) {
        Map<ResourceKey<Level>, Map<Integer, ClientSatellites.Entry>> byDimension = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            Map<Integer, ClientSatellites.Entry> satellites = new HashMap<>();
            for (Map.Entry<Integer, Satellite> entry :
                    SatelliteSavedData.get(level).sats.entrySet()) {
                Satellite satellite = entry.getValue();
                if (!isSpace(satellite.kind())) continue;
                satellites.put(
                        entry.getKey(),
                        new ClientSatellites.Entry(satellite.kind(), orbit(satellite)));
            }
            if (!satellites.isEmpty()) byDimension.put(level.dimension(), Map.copyOf(satellites));
        }
        return Map.copyOf(byDimension);
    }

    public static void install() {
        satellite(
                SatelliteType.MAPPER,
                0.538F,
                1.0F,
                0.523F,
                ModItems.SATELLITE_SPY,
                ModItems.SAT_MAPPER);
        satellite(
                SatelliteType.SCANNER,
                0.544F,
                0.680F,
                1.0F,
                ModItems.SATELLITE_SCANNER,
                ModItems.SAT_SCANNER);
        satellite(
                SatelliteType.RADAR,
                0.134F,
                1.0F,
                0.134F,
                ModItems.SATELLITE_RADAR,
                ModItems.SAT_RADAR);
        satellite(
                SatelliteType.DEATH_RAY,
                0.221F,
                0.663F,
                1.0F,
                ModItems.SATELLITE_DEATH_RAY,
                ModItems.SAT_LASER);
        satellite(
                SatelliteType.RESONATOR,
                1.0F,
                0.646F,
                0.181F,
                ModItems.SATELLITE_XENIUM_RESONATOR,
                ModItems.SAT_RESONATOR);
        satellite(SatelliteType.RELAY, 1.0F, 0.15F, 0.15F, ModItems.SATELLITE_RELAY);
        satellite(
                SatelliteType.MINER,
                0.46F,
                0.56F,
                0.68F,
                ModItems.SATELLITE_MINER_ASTRO,
                ModItems.SAT_MINER);
        satellite(
                SatelliteType.LUNAR_MINER,
                0.42F,
                0.54F,
                0.82F,
                ModItems.SATELLITE_MINER_LUNAR,
                ModItems.SAT_LUNAR_MINER);
        satellite(SatelliteType.HORIZONS, 0.0F, 0.0F, 0.0F, ModItems.SAT_GERALD);
        satellite(
                SatelliteType.PRECISION_LASER,
                0.221F,
                1.0F,
                0.663F,
                ModItems.SATELLITE_PRECISION_LASER);
        satellite(SatelliteType.DETECTOR, 0.1F, 0.2F, 0.3F, ModItems.SATELLITE_DETECTOR);
        satellite(SatelliteType.RAY_SCAN, 0.1F, 0.2F, 0.3F, ModItems.SATELLITE_RAY_SCAN);

        SatelliteDeployment.CARRIED.install(
                HbmSpace.MOD_ID, List.of(ModDataComponentsSpace.SATELLITE_ORBIT));
        SatelliteDeployment.ORBIT_LEVEL.install(HbmSpace.MOD_ID, SatellitesSpace::orbitLevel);
        SatelliteSavedData.AT.install(
                HbmSpace.MOD_ID,
                new SatelliteSavedData.Locator() {
                    @Override
                    public SatelliteSavedData at(ServerLevel level, int x, int z) {
                        return SatelliteSavedData.get(localLevel(level, x, z));
                    }

                    @Override
                    public List<ServerLevel> levels(ServerLevel level, int x, int z) {
                        var local = localLevel(level, x, z);
                        if (local.dimension() == Level.NETHER || local.dimension() == Level.END)
                            return List.of(local);
                        var levels = new ArrayList<ServerLevel>();
                        levels.add(local);
                        for (var candidate : level.getServer().getAllLevels()) {
                            if (candidate != local && BodyReach.isSurface(candidate.dimension()))
                                levels.add(candidate);
                        }
                        levels.subList(1, levels.size())
                                .sort(
                                        Comparator.comparing(
                                                candidate -> candidate.dimension().identifier()));
                        return levels;
                    }
                });
        SatelliteSavedData.CHANGED.install(HbmSpace.MOD_ID, () -> changed = true);
        SatelliteItem.DESCRIBE.install(HbmSpace.MOD_ID, SatelliteItems::describe);
        SatelliteItem.USE.install(HbmSpace.MOD_ID, SatelliteItems::use);

        final Consumer<ServerPlayer> callback =
                player ->
                        PacketWire.sendTo(
                                SpaceSatelliteSnapshot.encode(snapshot(player.level().getServer())),
                                player);
        ServerEvents.JOIN.add(callback);
        final Consumer<MinecraftServer> callback2 = SatellitesSpace::flush;
        ServerEvents.TICK_POST.add(callback2);
        final Consumer<MinecraftServer> callback3 = server -> changed = false;
        ServerEvents.STOPPING.add(callback3);
    }

    @SafeVarargs
    public static void satellite(
            SatelliteType type,
            float red,
            float green,
            float blue,
            RegistryHandle<? extends Item>... items) {
        COLORS.put(type, new float[] {red, green, blue});
        for (RegistryHandle<? extends Item> item : items) {
            Registrar.modifyDefaultComponents(
                    item,
                    components ->
                            components.set(
                                    ModDataComponentsSpace.SATELLITE_ORBIT.get(), defaults(type)));
        }
    }

    public static boolean isSpace(SatelliteType type) {
        return COLORS.containsKey(type);
    }

    public static SatelliteOrbit defaults(SatelliteType type) {
        float[] color = COLORS.get(type);
        return SatelliteOrbit.defaults(color[0], color[1], color[2]);
    }

    public static SatelliteOrbit orbit(Satellite satellite) {
        SatelliteOrbit orbit =
                satellite.carried().get(ModDataComponentsSpace.SATELLITE_ORBIT.get());
        return orbit != null ? orbit : defaults(satellite.kind());
    }

    public static SolarSystem.@Nullable Body home(@Nullable SatelliteType type) {
        if (type == SatelliteType.RELAY) return SolarSystem.Body.DUNA;
        if (type == SatelliteType.LUNAR_MINER) return SolarSystem.Body.MUN;
        if (type == SatelliteType.MINER) return SolarSystem.Body.DRES;
        return null;
    }

    private static ServerLevel orbitLevel(ServerLevel level, SatelliteType type) {
        SolarSystem.Body home = home(type);
        if (home == null) return level;
        ServerLevel target = level.getServer().getLevel(home.getDimension());
        return target != null ? target : level;
    }

    private static ServerLevel localLevel(ServerLevel level, int x, int z) {
        if (CelestialBody.inOrbit(level)) {
            ServerLevel body =
                    OrbitalStation.getStationFromPosition(x, z)
                            .orbiting
                            .getLevel(level.getServer());
            if (body != null) return body;
        }
        return level;
    }

    private static void flush(MinecraftServer server) {
        if (!changed) return;
        changed = false;
        PacketWire.sendToAll(SpaceSatelliteSnapshot.encode(snapshot(server)));
    }
}
