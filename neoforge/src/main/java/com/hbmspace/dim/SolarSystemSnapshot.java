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

import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import com.hbmspace.util.AstronomyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.network.Utf8String;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class SolarSystemSnapshot {

    private SolarSystemSnapshot() {}

    public static void write(
            ByteBuf outputBuffer,
            SolarSystemWorldSavedData solarSystem,
            ResourceKey<Level> dimension) {
        Collection<CelestialBody> bodies = CelestialBody.getAllBodies();
        outputBuffer.writeInt(bodies.size());
        for (CelestialBody body : bodies) {
            Utf8String.write(outputBuffer, body.name, Short.MAX_VALUE);
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                    solarSystem.getTraits(body.name);
            if (traits != null) {
                for (Class<? extends CelestialBodyTrait> type : traits.keySet()) {
                    if (!CelestialBodyTrait.traitList.contains(type)) {
                        throw new IllegalStateException(
                                "Unregistered celestial trait: " + type.getName());
                    }
                }
                outputBuffer.writeBoolean(true);
                outputBuffer.writeInt(traits.size());
                for (int i = 0; i < CelestialBodyTrait.traitList.size(); i++) {
                    CelestialBodyTrait trait = traits.get(CelestialBodyTrait.traitList.get(i));
                    if (trait != null) {
                        outputBuffer.writeInt(i);
                        trait.writeToBytes(outputBuffer);
                    }
                }
            } else {
                outputBuffer.writeBoolean(false);
            }
        }

        Collection<OrbitalStation> stations = solarSystem.getStations().values();
        int stationCount = 0;
        for (OrbitalStation station : stations) {
            if (station.hasStation && station.orbiting.dimension == dimension) stationCount++;
        }
        outputBuffer.writeInt(stationCount);
        for (OrbitalStation station : stations) {
            if (station.hasStation && station.orbiting.dimension == dimension) {
                outputBuffer.writeInt(station.dX);
                outputBuffer.writeInt(station.dZ);
            }
        }
        outputBuffer.writeLong(AstronomyUtil.TIME_MULTIPLIER);
    }

    public static void apply(ByteBuf inputBuffer) {
        Map<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>> traitMap =
                new HashMap<>(SolarSystemWorldSavedData.clientTraits());
        List<OrbitalStation> stations = new ArrayList<>();

        read(inputBuffer, traitMap, stations);

        SolarSystemWorldSavedData.updateClientTraits(traitMap);
        OrbitalStation.orbitingStations = stations;
        CBT_Invasion.CLIENT_WARNING.run();
    }

    public static void read(
            ByteBuf inputBuffer,
            Map<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>> traitMap,
            List<OrbitalStation> stations) {
        int bodyCount = inputBuffer.readInt();
        Collection<CelestialBody> bodies = CelestialBody.getAllBodies();
        if (bodyCount != bodies.size())
            throw new DecoderException("Invalid celestial body count: " + bodyCount);
        for (CelestialBody body : bodies) {
            String name = Utf8String.read(inputBuffer, Short.MAX_VALUE);
            if (!body.name.equals(name))
                throw new DecoderException("Unexpected celestial body: " + name);
            if (inputBuffer.readBoolean()) {
                HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                        traitMap.computeIfAbsent(name, ignored -> new HashMap<>());
                HashSet<Class<? extends CelestialBodyTrait>> sentTraits = new HashSet<>();
                int traitCount = inputBuffer.readInt();
                if (traitCount < 0 || traitCount > CelestialBodyTrait.traitList.size()) {
                    throw new DecoderException("Invalid celestial trait count: " + traitCount);
                }
                for (int traitIndex = 0; traitIndex < traitCount; traitIndex++) {
                    int traitId = inputBuffer.readInt();
                    if (traitId < 0 || traitId >= CelestialBodyTrait.traitList.size()) {
                        throw new DecoderException("Invalid celestial trait id: " + traitId);
                    }
                    Class<? extends CelestialBodyTrait> type =
                            CelestialBodyTrait.traitList.get(traitId);
                    if (!sentTraits.add(type))
                        throw new DecoderException("Duplicate celestial trait: " + type.getName());
                    CelestialBodyTrait trait = traits.get(type);
                    if (trait == null) trait = CelestialBodyTrait.create(type);
                    trait.readFromBytes(inputBuffer);
                    traits.put(type, trait);
                }
                traits.keySet().removeIf(type -> !sentTraits.contains(type));
            } else {
                traitMap.remove(name);
            }
        }

        int stationCount = inputBuffer.readInt();
        if (stationCount < 0 || stationCount > inputBuffer.readableBytes() / (Integer.BYTES * 2)) {
            throw new DecoderException("Invalid orbital station count: " + stationCount);
        }
        for (int i = 0; i < stationCount; i++) {
            stations.add(new OrbitalStation(null, inputBuffer.readInt(), inputBuffer.readInt()));
        }
        long multiplier = inputBuffer.readLong();
        if (multiplier < 1)
            throw new DecoderException("Invalid celestial time multiplier: " + multiplier);

        AstronomyUtil.TIME_MULTIPLIER = multiplier;
    }
}
