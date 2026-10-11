// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.orbit.OrbitalStation;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ClientSatellites {

    private static Map<ResourceKey<Level>, Map<Integer, Entry>> byLevel = Map.of();

    private ClientSatellites() {}

    public static Map<Integer, Entry> of(ResourceKey<Level> level) {
        return byLevel.getOrDefault(level, Map.of());
    }

    public static Map<Integer, Entry> current(Level level) {
        if (!CelestialBody.inOrbit(level)) return of(level.dimension());
        CelestialBody body = OrbitalStation.clientStation.orbiting;
        return body == null || body.dimension == null ? Map.of() : of(body.dimension);
    }

    static void replace(Map<ResourceKey<Level>, Map<Integer, Entry>> satellites) {
        byLevel = satellites;
    }

    public record Entry(SatelliteType type, SatelliteOrbit orbit) {}
}
