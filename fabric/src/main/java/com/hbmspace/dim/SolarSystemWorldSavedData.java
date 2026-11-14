/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

import com.hbmspace.HbmSpace;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

public class SolarSystemWorldSavedData extends SavedData {

    public static final SavedDataType<SolarSystemWorldSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "solar_system"),
                    SolarSystemWorldSavedData::new,
                    CompoundTag.CODEC.xmap(
                            SolarSystemWorldSavedData::load, SolarSystemWorldSavedData::save),
                    null);

    private static volatile @Nullable SolarSystemWorldSavedData live;

    private final Random rand = new Random();

    private final HashMap<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>>
            traitMap = new HashMap<>();
    private final HashMap<ChunkPos, OrbitalStation> stations = new HashMap<>();

    public SolarSystemWorldSavedData() {}

    public static SolarSystemWorldSavedData get() {
        SolarSystemWorldSavedData data = live;
        if (data == null) throw new IllegalStateException("no server has loaded the solar system");
        return data;
    }

    public static @Nullable SolarSystemWorldSavedData getIfLive() {
        return live;
    }

    public static SolarSystemWorldSavedData get(Level level) {
        return get(level.getServer());
    }

    public static SolarSystemWorldSavedData get(MinecraftServer server) {
        SolarSystemWorldSavedData data = server.getDataStorage().computeIfAbsent(TYPE);
        live = data;
        return data;
    }

    public static void publish(MinecraftServer server) {
        get(server);
    }

    public static void onServerStopping() {
        live = null;
    }

    private static SolarSystemWorldSavedData load(CompoundTag nbt) {
        SolarSystemWorldSavedData data = new SolarSystemWorldSavedData();

        for (CelestialBody body : CelestialBody.getAllBodies()) {
            CompoundTag bodyData = nbt.getCompound("b_" + body.name).orElse(null);
            if (bodyData == null) continue;

            HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                    new HashMap<>();
            for (Map.Entry<String, Class<? extends CelestialBodyTrait>> entry :
                    CelestialBodyTrait.traitMap.entrySet()) {
                CompoundTag traitData = bodyData.getCompound(entry.getKey()).orElse(null);
                if (traitData == null) continue;

                CelestialBodyTrait trait = CelestialBodyTrait.create(entry.getValue());
                trait.readFromNBT(traitData);
                traits.put(trait.getClass(), trait);
            }

            data.traitMap.put(body.name, traits);
        }

        ListTag stationList = nbt.getListOrEmpty("stations");
        for (int i = 0; i < stationList.size(); i++) {
            CompoundTag stationTag = stationList.getCompoundOrEmpty(i);
            int x = stationTag.getIntOr("x", 0);
            int z = stationTag.getIntOr("z", 0);

            OrbitalStation station =
                    new OrbitalStation(
                            CelestialBody.getBody(stationTag.getStringOr("orbiting", "")), x, z);
            station.target = CelestialBody.getBody(stationTag.getStringOr("target", ""));
            station.state = StationState.values()[stationTag.getIntOr("state", 0)];
            station.stateTimer = stationTag.getIntOr("stateTimer", 0);
            station.maxStateTimer = stationTag.getIntOr("maxStateTimer", 0);
            station.hasStation = stationTag.getBooleanOr("hasStation", false);
            station.name = stationTag.getStringOr("name", "");
            station.gravityMultiplier = stationTag.getFloatOr("gravity", 1);

            data.stations.put(new ChunkPos(x, z), station);
        }

        return data;
    }

    private CompoundTag save() {
        CompoundTag nbt = new CompoundTag();

        for (Map.Entry<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>>
                entry : traitMap.entrySet()) {
            CompoundTag bodyData = new CompoundTag();

            for (CelestialBodyTrait trait : entry.getValue().values()) {
                CompoundTag traitData = new CompoundTag();
                trait.writeToNBT(traitData);
                bodyData.put(
                        CelestialBodyTrait.traitMap.inverse().get(trait.getClass()), traitData);
            }

            nbt.put("b_" + entry.getKey(), bodyData);
        }

        ListTag stationList = new ListTag();
        for (OrbitalStation station : stations.values()) {
            CompoundTag stationTag = new CompoundTag();
            stationTag.putInt("x", station.dX);
            stationTag.putInt("z", station.dZ);
            stationTag.putString("orbiting", station.orbiting.name);
            stationTag.putString("target", station.target.name);
            stationTag.putInt("state", station.state.ordinal());
            stationTag.putInt("stateTimer", station.stateTimer);
            stationTag.putInt("maxStateTimer", station.maxStateTimer);
            stationTag.putBoolean("hasStation", station.hasStation);
            stationTag.putString("name", station.name);
            stationTag.putFloat("gravity", station.gravityMultiplier);

            stationList.add(stationTag);
        }
        nbt.put("stations", stationList);

        return nbt;
    }

    public void setTraits(String bodyName, CelestialBodyTrait... traits) {
        if (traits.length == 0) {
            clearTraits(bodyName);
            return;
        }

        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> newTraits =
                new HashMap<>();
        for (CelestialBodyTrait trait : traits) {
            newTraits.put(trait.getClass(), trait);
        }

        var previous = this.traitMap.put(bodyName, newTraits);
        retireInvasion(previous, newTraits);

        setDirty();
    }

    public void clearTraits(String bodyName) {
        retireInvasion(this.traitMap.remove(bodyName), null);

        setDirty();
    }

    private static void retireInvasion(
            @Nullable Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> previous,
            @Nullable Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> current) {
        if (previous != null
                && previous.get(CBT_Invasion.class) instanceof CBT_Invasion invasion
                && (current == null || current.get(CBT_Invasion.class) != invasion))
            invasion.removeBar();
    }

    public @Nullable HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> getTraits(
            String bodyName) {
        return traitMap.get(bodyName);
    }

    public HashMap<ChunkPos, OrbitalStation> getStations() {
        return stations;
    }

    public @Nullable OrbitalStation getStationFromPosition(int x, int z) {
        ChunkPos pos =
                new ChunkPos(
                        Mth.floor((float) x / OrbitalStation.STATION_SIZE),
                        Mth.floor((float) z / OrbitalStation.STATION_SIZE));
        return stations.get(pos);
    }

    public ChunkPos findFreeSpace() {
        int size = SpaceData.MAX_STATION_DISTANCE.get() / OrbitalStation.STATION_SIZE;

        ChunkPos pos = null;
        for (int i = 0; i < 512; i++) {
            pos = new ChunkPos(rand.nextInt(size * 2) - size, rand.nextInt(size * 2) - size);
            if (!stations.containsKey(pos)) return pos;
            if (i > 256 && !stations.get(pos).hasStation) return pos;
        }

        return pos;
    }

    public OrbitalStation addStation(CelestialBody orbiting) {
        ChunkPos pos = findFreeSpace();

        return addStation(pos.x(), pos.z(), orbiting);
    }

    public OrbitalStation addStation(int x, int z, CelestialBody orbiting) {
        ChunkPos pos = new ChunkPos(x, z);

        OrbitalStation station = stations.get(pos);

        if (station == null) {
            station = new OrbitalStation(orbiting, x, z);
            stations.put(pos, station);
        }

        setDirty();

        return station;
    }

    public void removeStation(OrbitalStation station) {
        removeStation(station.dX, station.dZ);
    }

    public void removeStation(int x, int z) {
        ChunkPos pos = new ChunkPos(x, z);

        OrbitalStation station = stations.get(pos);
        if (station == null || station.hasStation) return;

        stations.remove(pos);
    }

    private static volatile Map<
                    String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>>
            clientTraits = new HashMap<>();
    private static volatile @Nullable Thread clientThread;

    public static void updateClientTraits(
            @Nullable Map<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>>
                    traits) {
        clientThread = Thread.currentThread();
        clientTraits = traits == null ? new HashMap<>() : traits;
    }

    public static @Nullable HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>
            getClientTraits(String bodyName) {
        return clientTraits.get(bodyName);
    }

    static Map<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>>
            clientTraits() {
        return clientTraits;
    }

    static @Nullable Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> readTraits(
            String bodyName) {
        SolarSystemWorldSavedData data = live;
        if (data == null || Thread.currentThread() == clientThread)
            return clientTraits.get(bodyName);
        return data.getTraits(bodyName);
    }
}
